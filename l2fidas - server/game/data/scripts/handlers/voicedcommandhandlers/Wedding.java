/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import fidas.server.Config;
import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.GameTimeController;
import fidas.server.gameserver.SevenSigns;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.instancemanager.CoupleManager;
import fidas.server.gameserver.instancemanager.GrandBossManager;
import fidas.server.gameserver.instancemanager.SiegeManager;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.effects.AbnormalEffect;
import fidas.server.gameserver.model.entity.L2Event;
import fidas.server.gameserver.model.entity.TvTEvent;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.model.zone.ZoneId;
import fidas.server.gameserver.network.DialogId;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.ActionFailed;
import fidas.server.gameserver.network.serverpackets.ConfirmDlg;
import fidas.server.gameserver.network.serverpackets.MagicSkillUse;
import fidas.server.gameserver.network.serverpackets.SetupGauge;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.gameserver.util.Broadcast;

/**
 * @author evill33t
 */
public class Wedding implements IVoicedCommandHandler
{
	static final Logger _log = Logger.getLogger(Wedding.class.getName());
	private static final String[] _voicedCommands =
	{
		"divorce",
		"engage",
		"gotolove"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (activeChar == null)
		{
			return false;
		}
		if (command.startsWith("engage"))
		{
			return engage(activeChar);
		}
		else if (command.startsWith("divorce"))
		{
			return divorce(activeChar);
		}
		else if (command.startsWith("gotolove"))
		{
			return goToLove(activeChar);
		}
		return false;
	}
	
	public boolean divorce(L2PcInstance activeChar)
	{
		if (activeChar.getPartnerId() == 0)
		{
			return false;
		}
		
		int _partnerId = activeChar.getPartnerId();
		int _coupleId = activeChar.getCoupleId();
		long AdenaAmount = 0;
		
		if (activeChar.isMarried())
		{
			activeChar.sendMessage("Voce agora esta divorciado.");
			
			AdenaAmount = (activeChar.getAdena() / 100) * Config.L2JMOD_WEDDING_DIVORCE_COSTS;
			activeChar.getInventory().reduceAdena("Wedding", AdenaAmount, activeChar, null);
			
		}
		else
		{
			activeChar.sendMessage("Seu casamento foi interrompido.");
		}
		
		final L2PcInstance partner = L2World.getInstance().getPlayer(_partnerId);
		if (partner != null)
		{
			partner.setPartnerId(0);
			if (partner.isMarried())
			{
				partner.sendMessage("Seu esposo(a) decidiu divorciar de voce.");
			}
			else
			{
				partner.sendMessage("Seu noivo(a) decidiu romper o noivado com voce.");
			}
			
			// give adena
			if (AdenaAmount > 0)
			{
				partner.addAdena("WEDDING", AdenaAmount, null, false);
			}
		}
		CoupleManager.getInstance().deleteCouple(_coupleId);
		return true;
	}
	
	public boolean engage(L2PcInstance activeChar)
	{
		if (activeChar.getTarget() == null)
		{
			activeChar.sendMessage("Voce nao clicou em ninguem.");
			return false;
		}
		else if (!(activeChar.getTarget() instanceof L2PcInstance))
		{
			activeChar.sendMessage("Voce tem que pedir a outro jogador para noivar com voce.");
			return false;
		}
		else if (activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage("Voce ja esta noivo de alguem.");
			if (Config.L2JMOD_WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD); // give player a Big Head
				// lets recycle the sevensigns debuffs
				int skillId;
				
				int skillLevel = 1;
				
				if (activeChar.getLevel() > 40)
				{
					skillLevel = 2;
				}
				
				if (activeChar.isMageClass())
				{
					skillId = 4362;
				}
				else
				{
					skillId = 4361;
				}
				
				final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				if (activeChar.getFirstEffect(skill) == null)
				{
					skill.getEffects(activeChar, activeChar);
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(skill);
					activeChar.sendPacket(sm);
				}
			}
			return false;
		}
		final L2PcInstance ptarget = (L2PcInstance) activeChar.getTarget();
		// check if player target himself
		if (ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("Ha algo de errado, voce esta tentando sair com voce mesmo?");
			return false;
		}
		
		if (ptarget.isMarried())
		{
			activeChar.sendMessage("Jogador ja casado.");
			return false;
		}
		
		if (ptarget.isEngageRequest())
		{
			activeChar.sendMessage("Jogador pediu alguem em casamento.");
			return false;
		}
		
		if (ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage("Jogador ja esta envolvido com outra pessoa.");
			return false;
		}
		
		if ((ptarget.getAppearance().getSex() == activeChar.getAppearance().getSex()) && !Config.L2JMOD_WEDDING_SAMESEX)
		{
			activeChar.sendMessage("O casamento homosexual nao e permitido no servidor!");
			return false;
		}
		
		// check if target has player on friendlist
		boolean FoundOnFriendList = false;
		int objectId;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT friendId FROM character_friends WHERE charId=?");
			statement.setInt(1, ptarget.getObjectId());
			final ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				objectId = rset.getInt("friendId");
				if (objectId == activeChar.getObjectId())
				{
					FoundOnFriendList = true;
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("nao pode ler dados amigo:" + e);
		}
		
		if (!FoundOnFriendList)
		{
			activeChar.sendMessage("O jogador que voce quer pedir em casamento nao esta na sua lista de amigos, voce deve primeiro coloca-lo em sua lista de amigos.");
			return false;
		}
		
		ptarget.setEngageRequest(true, activeChar.getObjectId());
		// $s1
		ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1.getId()).addString(activeChar.getName() + " esta pedindo ele(a) em noivado? Voce quer iniciar um novo relacionamento?");
		ptarget.sendPacket(dlg);
		ptarget.setDialogId(DialogId.WEDDING);
		return true;
	}
	
	public boolean goToLove(L2PcInstance activeChar)
	{
		if (!activeChar.isMarried())
		{
			activeChar.sendMessage("Voce na esta casado(a).");
			return false;
		}
		
		if (activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage("Nao foi possivel encontrar seu noivo(a) no banco de dados - Informar a um GM.");
			_log.severe("Married but couldn't find parter for " + activeChar.getName());
			return false;
		}
		
		if (GrandBossManager.getInstance().getZone(activeChar) != null)
		{
			activeChar.sendMessage("Voce esta dentro de uma zona de Boss.");
			return false;
		}
		
		if (activeChar.isCombatFlagEquipped())
		{
			activeChar.sendMessage("Enquanto voce estiver segurando uma Combat Flag ou em Territory Ward voce nao podera teleportar para o seu amor!");
			return false;
		}
		
		if (activeChar.isCursedWeaponEquipped())
		{
			activeChar.sendMessage("Enquanto voce estiver segurando uma Arma Maldita voce nao podera ir para o seu amor!");
			return false;
		}
		
		if (GrandBossManager.getInstance().getZone(activeChar) != null)
		{
			activeChar.sendMessage("Voce esta dentro de uma zona de Boss.");
			return false;
		}
		
		if (activeChar.isInJail())
		{
			activeChar.sendMessage("Voce esta na Prisao!");
			return false;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("Voce esta na Olimpiada.");
			return false;
		}
		
		if (L2Event.isParticipant(activeChar))
		{
			activeChar.sendMessage("Voce esta em um Evento.");
			return false;
		}
		
		if (activeChar.isInDuel())
		{
			activeChar.sendMessage("Voce esta em um Duelo!");
			return false;
		}
		
		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("Voce esta em modo de observacao.");
			return false;
		}
		
		if ((SiegeManager.getInstance().getSiege(activeChar) != null) && SiegeManager.getInstance().getSiege(activeChar).getIsInProgress())
		{
			activeChar.sendMessage("Voce esta em um cerco ao castelo, voce nao pode ir para o seu parceiro.");
			return false;
		}
		
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("Voce esta em um festival.");
			return false;
		}
		
		if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("Voce esta na fenda dimensional.");
			return false;
		}
		
		// Thanks nbd
		if (!TvTEvent.onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (activeChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
		{
			activeChar.sendMessage("Voce esta na area que bloqueia convocacao.");
			return false;
		}
		
		final L2PcInstance partner = L2World.getInstance().getPlayer(activeChar.getPartnerId());
		if ((partner == null) || !partner.isOnline())
		{
			activeChar.sendMessage("Seu parceiro nao esta online.");
			return false;
		}
		
		if (activeChar.getInstanceId() != partner.getInstanceId())
		{
			activeChar.sendMessage("Seu parceiro esta em outro mundo!");
			return false;
		}
		
		if (partner.isInJail())
		{
			activeChar.sendMessage("Seu parceiro esta na Prisao.");
			return false;
		}
		
		if (partner.isCursedWeaponEquipped())
		{
			activeChar.sendMessage("Seu parceiro esta segurando uma Arma Maldita e voce nao pode ir ate ele(a)!");
			return false;
		}
		
		if (GrandBossManager.getInstance().getZone(partner) != null)
		{
			activeChar.sendMessage("Seu parceiro esta em uma zona de Boss.");
			return false;
		}
		
		if (partner.isInOlympiadMode())
		{
			activeChar.sendMessage("Seu parceiro esta nas Olimpiadas agora.");
			return false;
		}
		
		if (L2Event.isParticipant(partner))
		{
			activeChar.sendMessage("Seu parceiro esta em um Evento.");
			return false;
		}
		
		if (partner.isInDuel())
		{
			activeChar.sendMessage("Seu parceiro esta em um Duelo.");
			return false;
		}
		
		if (partner.isFestivalParticipant())
		{
			activeChar.sendMessage("Seu parceiro esta em um festival.");
			return false;
		}
		
		if (partner.isInParty() && partner.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("Seu parceiro esta na fenda dimensional.");
			return false;
		}
		
		if (partner.inObserverMode())
		{
			activeChar.sendMessage("Seu parceiro esta em modo de Observacao.");
			return false;
		}
		
		if ((SiegeManager.getInstance().getSiege(partner) != null) && SiegeManager.getInstance().getSiege(partner).getIsInProgress())
		{
			activeChar.sendMessage("Seu parceiro esta em um cerco ao castelo, voce nao pode ir ate ele(a) agora.");
			return false;
		}
		
		if (partner.isIn7sDungeon() && !activeChar.isIn7sDungeon())
		{
			final int playerCabal = SevenSigns.getInstance().getPlayerCabal(activeChar.getObjectId());
			final boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
			final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
			
			if (isSealValidationPeriod)
			{
				if (playerCabal != compWinner)
				{
					activeChar.sendMessage("Seu parceiro esta no Seven Signs Dungeon e voce nao esta no Cabal vencedor!");
					return false;
				}
			}
			else
			{
				if (playerCabal == SevenSigns.CABAL_NULL)
				{
					activeChar.sendMessage("Seu parceiro esta no Seven Signs Dungeon e voce nao esta registrado!");
					return false;
				}
			}
		}
		
		if (!TvTEvent.onEscapeUse(partner.getObjectId()))
		{
			activeChar.sendMessage("Seu parceiro esta em um Evento.");
			return false;
		}
		
		if (partner.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
		{
			activeChar.sendMessage("Seu parceiro esta na area que bloqueia convocacao.");
			return false;
		}
		
		final int teleportTimer = Config.L2JMOD_WEDDING_TELEPORT_DURATION * 1000;
		activeChar.sendMessage("Depois " + (teleportTimer / 60000) + " min. voce sera teletransportado para o seu parceiro.");
		activeChar.getInventory().reduceAdena("Wedding", Config.L2JMOD_WEDDING_TELEPORT_PRICE, activeChar, null);
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		final MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, teleportTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 900);
		final SetupGauge sg = new SetupGauge(0, teleportTimer);
		activeChar.sendPacket(sg);
		// End SoE Animation section
		
		final EscapeFinalizer ef = new EscapeFinalizer(activeChar, partner.getX(), partner.getY(), partner.getZ(), partner.isIn7sDungeon());
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
		activeChar.forceIsCasting(GameTimeController.getGameTicks() + (teleportTimer / GameTimeController.MILLIS_IN_TICK));
		
		return true;
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final int _partnerx;
		private final int _partnery;
		private final int _partnerz;
		private final boolean _to7sDungeon;
		
		EscapeFinalizer(L2PcInstance activeChar, int x, int y, int z, boolean to7sDungeon)
		{
			_activeChar = activeChar;
			_partnerx = x;
			_partnery = y;
			_partnerz = z;
			_to7sDungeon = to7sDungeon;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
			{
				return;
			}
			
			if ((SiegeManager.getInstance().getSiege(_partnerx, _partnery, _partnerz) != null) && SiegeManager.getInstance().getSiege(_partnerx, _partnery, _partnerz).getIsInProgress())
			{
				_activeChar.sendMessage("Seu parceiro esta em um cerco ao castelo, voce nao pode ir ate ele(a) agora.");
				return;
			}
			
			_activeChar.setIsIn7sDungeon(_to7sDungeon);
			_activeChar.enableAllSkills();
			_activeChar.setIsCastingNow(false);
			
			try
			{
				_activeChar.teleToLocation(_partnerx, _partnery, _partnerz);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
