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
package handlers.usercommandhandlers;

import fidas.server.Config;
import fidas.server.gameserver.GameTimeController;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.handler.IUserCommandHandler;
import fidas.server.gameserver.instancemanager.MapRegionManager;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.TvTEvent;
import fidas.server.gameserver.model.entity.TvTRoundEvent;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.network.serverpackets.ActionFailed;
import fidas.server.gameserver.network.serverpackets.MagicSkillUse;
import fidas.server.gameserver.network.serverpackets.SetupGauge;
import fidas.server.gameserver.util.Broadcast;

/**
 * Unstuck user command.
 */
public class Unstuck implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		// Thanks nbd
		if (!TvTEvent.onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (!TvTRoundEvent.onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if(!Config.ENABLE_UNSTUCK_PVP)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		int unstuckTimer = (activeChar.getAccessLevel().isGm() ? 1000 : Config.UNSTUCK_INTERVAL * 1000);
		
		if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || activeChar.isCombatFlagEquipped())
		{
			return false;
		}
		activeChar.forceIsCasting(GameTimeController.getGameTicks() + (unstuckTimer / GameTimeController.MILLIS_IN_TICK));
		
		L2Skill escape = SkillTable.getInstance().getInfo(2099, 1); // 5 minutes escape
		L2Skill GM_escape = SkillTable.getInstance().getInfo(2100, 1); // 1 second escape
		if (activeChar.getAccessLevel().isGm())
		{
			if (GM_escape != null)
			{
				activeChar.doCast(GM_escape);
				return true;
			}
			activeChar.sendMessage("You use Escape: 1 second.");
		}
		else if ((Config.UNSTUCK_INTERVAL == 300) && (escape != null))
		{
			activeChar.doCast(escape);
			return true;
		}
		else
		{
			if (Config.UNSTUCK_INTERVAL > 100)
			{
				activeChar.sendMessage("You use Escape: " + (unstuckTimer / 60000) + " minutes.");
			}
			else
			{
				activeChar.sendMessage("You use Escape: " + (unstuckTimer / 1000) + " seconds.");
			}
		}
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		MagicSkillUse msk = new MagicSkillUse(activeChar, Config.UNSTUCK_ANIMATION_ID, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 900);
		SetupGauge sg = new SetupGauge(0, unstuckTimer);
		activeChar.sendPacket(sg);
		// End SoE Animation section
		
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(new EscapeFinalizer(activeChar), unstuckTimer));
		
		return true;
	}
	
	private static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		
		protected EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
			{
				return;
			}
			
			_activeChar.setIsIn7sDungeon(false);
			_activeChar.enableAllSkills();
			_activeChar.setIsCastingNow(false);
			_activeChar.setInstanceId(0);
			_activeChar.teleToLocation(MapRegionManager.TeleportWhereType.Town);
		}
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}