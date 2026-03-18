/*
 * Copyright (C) 2004-2013 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fidas.server.gameserver.network.clientpackets;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javolution.util.FastList;
import fidas.server.Config;
import fidas.server.gameserver.Announcements;
import fidas.server.gameserver.LoginServerThread;
import fidas.server.gameserver.SevenSigns;
import fidas.server.gameserver.TaskPriority;
import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.communitybbs.Manager.RegionBBSManager;
import fidas.server.gameserver.custom.PvPColorSystem;
import fidas.server.gameserver.datatables.AdminTable;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.datatables.SkillTreesData;
import fidas.server.gameserver.instancemanager.BotManager;
import fidas.server.gameserver.instancemanager.CHSiegeManager;
import fidas.server.gameserver.instancemanager.CastleManager;
import fidas.server.gameserver.instancemanager.ClanHallManager;
import fidas.server.gameserver.instancemanager.CoupleManager;
import fidas.server.gameserver.instancemanager.CursedWeaponsManager;
import fidas.server.gameserver.instancemanager.DimensionalRiftManager;
import fidas.server.gameserver.instancemanager.FortManager;
import fidas.server.gameserver.instancemanager.FortSiegeManager;
import fidas.server.gameserver.instancemanager.InstanceManager;
import fidas.server.gameserver.instancemanager.MailManager;
import fidas.server.gameserver.instancemanager.MapRegionManager;
import fidas.server.gameserver.instancemanager.PetitionManager;
import fidas.server.gameserver.instancemanager.QuestManager;
import fidas.server.gameserver.instancemanager.SiegeManager;
import fidas.server.gameserver.instancemanager.TerritoryWarManager;
import fidas.server.gameserver.model.L2Clan;
import fidas.server.gameserver.model.L2Macro;
import fidas.server.gameserver.model.L2ShortCut;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.PcCondOverride;
import fidas.server.gameserver.model.actor.instance.L2ClassMasterInstance;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Castle;
import fidas.server.gameserver.model.entity.Couple;
import fidas.server.gameserver.model.entity.Fort;
import fidas.server.gameserver.model.entity.FortSiege;
import fidas.server.gameserver.model.entity.Hitman;
import fidas.server.gameserver.model.entity.L2Event;
import fidas.server.gameserver.model.entity.Siege;
import fidas.server.gameserver.model.entity.TvTEvent;
import fidas.server.gameserver.model.entity.TvTRoundEvent;
import fidas.server.gameserver.model.entity.clanhall.AuctionableHall;
import fidas.server.gameserver.model.entity.clanhall.SiegableHall;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.zone.ZoneId;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.communityserver.CommunityServerThread;
import fidas.server.gameserver.network.communityserver.writepackets.WorldInfo;
import fidas.server.gameserver.network.serverpackets.Die;
import fidas.server.gameserver.network.serverpackets.EtcStatusUpdate;
import fidas.server.gameserver.network.serverpackets.ExBasicActionList;
import fidas.server.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import fidas.server.gameserver.network.serverpackets.ExNevitAdventEffect;
import fidas.server.gameserver.network.serverpackets.ExNevitAdventPointInfoPacket;
import fidas.server.gameserver.network.serverpackets.ExNevitAdventTimeChange;
import fidas.server.gameserver.network.serverpackets.ExNoticePostArrived;
import fidas.server.gameserver.network.serverpackets.ExNotifyPremiumItem;
import fidas.server.gameserver.network.serverpackets.ExPCCafePointInfo;
import fidas.server.gameserver.network.serverpackets.ExRedSky;
import fidas.server.gameserver.network.serverpackets.ExShowContactList;
import fidas.server.gameserver.network.serverpackets.ExShowScreenMessage;
import fidas.server.gameserver.network.serverpackets.ExStorageMaxCount;
import fidas.server.gameserver.network.serverpackets.ExVoteSystemInfo;
import fidas.server.gameserver.network.serverpackets.FriendList;
import fidas.server.gameserver.network.serverpackets.HennaInfo;
import fidas.server.gameserver.network.serverpackets.ItemList;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.network.serverpackets.PlaySound;
import fidas.server.gameserver.network.serverpackets.PledgeShowMemberListAll;
import fidas.server.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import fidas.server.gameserver.network.serverpackets.PledgeSkillList;
import fidas.server.gameserver.network.serverpackets.PledgeStatusChanged;
import fidas.server.gameserver.network.serverpackets.QuestList;
import fidas.server.gameserver.network.serverpackets.ShortCutInit;
import fidas.server.gameserver.network.serverpackets.ShortCutRegister;
import fidas.server.gameserver.network.serverpackets.SkillCoolTime;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.gameserver.scripting.scriptengine.listeners.player.PlayerSpawnListener;
import fidas.server.gameserver.util.Util;
import fidas.server.util.Base64;

/**
 * Enter World Packet Handler
 * <p>
 * <p>
 * 0000: 03
 * <p>
 * packet format rev87 
 * <p>
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__11_ENTERWORLD = "[C] 11 EnterWorld";
	
	private static FastList<PlayerSpawnListener> listeners = new FastList<PlayerSpawnListener>().shared();
	
	long _daysleft;
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	private final int[][] tracert = new int[5][4];

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}
	
	@Override
	protected void readImpl()
	{
		readB(new byte[32]); // Unknown Byte Array
		readD(); // Unknown Value
		readD(); // Unknown Value
		readD(); // Unknown Value
		readD(); // Unknown Value
		readB(new byte[32]); // Unknown Byte Array
		readD(); // Unknown Value
		for (int i = 0; i < 5; i++)
		{
			for (int o = 0; o < 4; o++)
			{
				tracert[i][o] = readC();
			}
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		PvPColorSystem pvpcolor = new PvPColorSystem();
		pvpcolor.updateNameColor(activeChar);
		pvpcolor.updateTitleColor(activeChar);
		
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar returned 'null'.");
			getClient().closeNow();
			return;
		}
		
		String[] adress = new String[5];
		for (int i = 0; i < 5; i++)
		{
			adress[i] = tracert[i][0] + "." + tracert[i][1] + "." + tracert[i][2] + "." + tracert[i][3];
		}
		
		LoginServerThread.getInstance().sendClientTracert(activeChar.getAccountName(), adress);
		
		getClient().setClientTracert(tracert);
		
		// Restore to instanced area if enabled
		if (Config.RESTORE_PLAYER_INSTANCE)
		{
			activeChar.setInstanceId(InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId()));
		}
		else
		{
			int instanceId = InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId());
			if (instanceId > 0)
			{
				InstanceManager.getInstance().getInstance(instanceId).removePlayer(activeChar.getObjectId());
			}
		}
		
		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
			{
				_log.warning("User already exists in Object ID map! User " + activeChar.getName() + " is a character clone.");
			}
		}
		
		if (Config.PROTECT_ENCHANT_ENABLE)
		{
			for (L2ItemInstance i : activeChar.getInventory().getItems())
			{
				if (!activeChar.isGM())
				{       
					if (i.isEquipable())
					{
						if (i.getEnchantLevel() > Config.MAX_ENCHANT_LEVEL_PROTECT)
						{
							//Delete Item Over enchanted
							activeChar.getInventory().destroyItem(null, i, activeChar, null);
							//Message to Player
							activeChar.sendMessage("[Server]:You have items above the allowed level!");
							activeChar.sendMessage("[Server]:This action is prohibited, you will be expelled!");
							//If Audit is only a Kick, with this the player goes in Jail for 1.200 minutes
							activeChar.setPunishLevel(L2PcInstance.PunishLevel.JAIL, Config.ENCHANT_PROTECT_PUNISH);
							//Log in console
							_log.info("#### ATTENCTION ####");
							_log.info(i+" item has been removed from player.");
						}
					}
				}
			}
		} 

		// Apply special GM properties to the GM when entering
		if (activeChar.isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE && AdminTable.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
			{
				activeChar.setIsInvul(true);
			}
			
			if(Config.GM_SUPER_HASTE)
			{
				SkillTable.getInstance().getInfo(7029, 3).getEffects(activeChar, activeChar);
			}
			
			if (Config.GM_STARTUP_INVISIBLE && AdminTable.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel()))
			{
				activeChar.getAppearance().setInvisible();
			}
			
			if (Config.GM_STARTUP_SILENCE && AdminTable.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
			{
				activeChar.setSilenceMode(true);
			}
			
			if (Config.GM_STARTUP_DIET_MODE && AdminTable.getInstance().hasAccess("admin_diet", activeChar.getAccessLevel()))
			{
				activeChar.setDietMode(true);
				activeChar.refreshOverloaded();
			}
			
			if (Config.GM_STARTUP_AUTO_LIST && AdminTable.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()))
			{
				AdminTable.getInstance().addGm(activeChar, false);
			}
			else
			{
				AdminTable.getInstance().addGm(activeChar, true);
			}
			
			if (Config.GM_GIVE_SPECIAL_SKILLS)
			{
				SkillTreesData.getInstance().addSkills(activeChar, false);
			}
			
			if (Config.GM_GIVE_SPECIAL_AURA_SKILLS)
			{
				SkillTreesData.getInstance().addSkills(activeChar, true);
			}
		}
		
		// Set dead status if applies
		if (activeChar.getCurrentHp() < 0.5)
		{
			activeChar.setIsDead(true);
			
            // Make Sky Red For 7 Seconds.
            ExRedSky packet = new ExRedSky(7);
            sendPacket(packet);
            
            // Play Custom Game Over Music
            PlaySound death_music = new PlaySound(1, "Game_Over", 0, 0, 0, 0, 0);
            sendPacket(death_music);
		}
		
		boolean showClanNotice = false;
		
		// Clan related checks are here
		if (activeChar.getClan() != null)
		{
			activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));
			
			notifyClanMembers(activeChar);
			
			notifySponsorOrApprentice(activeChar);
			
			AuctionableHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
			
			if (clanHall != null)
			{
				if (!clanHall.getPaid())
				{
					activeChar.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
				}
			}
			
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(siege.getCastle().getCastleId());
				}
				
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(siege.getCastle().getCastleId());
				}
			}
			
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(siege.getFort().getFortId());
				}
				
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(siege.getFort().getFortId());
				}
			}
			
			for (SiegableHall hall : CHSiegeManager.getInstance().getConquerableHalls().values())
			{
				if (!hall.isInSiege())
				{
					continue;
				}
				
				if (hall.isRegistered(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(hall.getId());
					activeChar.setIsInHideoutSiege(true);
				}
			}
			
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));
			
			// Residential skills support
			if (activeChar.getClan().getCastleId() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
			}
			
			if (activeChar.getClan().getFortId() > 0)
			{
				FortManager.getInstance().getFortByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
			}
			
			showClanNotice = activeChar.getClan().isNoticeEnabled();
		}
		
		if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar) > 0)
		{
			if (TerritoryWarManager.getInstance().isTWInProgress())
			{
				activeChar.setSiegeState((byte) 1);
			}
			activeChar.setSiegeSide(TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar));
		}
		
		// Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod() && (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) != SevenSigns.CABAL_NULL))
		{
			int cabal = SevenSigns.getInstance().getPlayerCabal(activeChar.getObjectId());
			if (cabal != SevenSigns.CABAL_NULL)
			{
				if (cabal == SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
				{
					activeChar.addSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
				}
				else
				{
					activeChar.addSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
				}
			}
		}
		else
		{
			activeChar.removeSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
			activeChar.removeSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
		}
		
		if (Config.ENABLE_VITALITY && Config.RECOVER_VITALITY_ON_RECONNECT)
		{
			float points = (Config.RATE_RECOVERY_ON_RECONNECT * (System.currentTimeMillis() - activeChar.getLastAccess())) / 60000;
			if (points > 0)
			{
				activeChar.updateVitalityPoints(points, false, true);
			}
		}
		
		activeChar.checkRecoBonusTask();
		
		activeChar.broadcastUserInfo();
		
		// Crear macros por defecto para personajes nuevos
		giveDefaultMacrosIfNeeded(activeChar);
		
		// Send Macro List
		activeChar.getMacros().sendUpdate();
		
		// Send Item List
		sendPacket(new ItemList(activeChar, false));
		
		// Send GG check
		activeChar.queryGameGuard();
		
		// Send Teleport Bookmark List
		sendPacket(new ExGetBookMarkInfoPacket(activeChar));
		
		// Send Shortcuts
		sendPacket(new ShortCutInit(activeChar));
		
		// Send Action list
		activeChar.sendPacket(ExBasicActionList.getStaticPacket(activeChar));
		
		// Send Skill list
		activeChar.sendSkillList();
		
		// Send Dye Information
		activeChar.sendPacket(new HennaInfo(activeChar));
		
		Quest.playerEnter(activeChar);
		
		if (!Config.DISABLE_TUTORIAL)
		{
			loadTutorial(activeChar);
		}
		
		for (Quest quest : QuestManager.getInstance().getAllManagedScripts())
		{
			if ((quest != null) && quest.getOnEnterWorld())
			{
				quest.notifyEnterWorld(activeChar);
			}
		}
		activeChar.sendPacket(new QuestList());
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}
		
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		activeChar.getInventory().applyItemSkills();
		
		if (L2Event.isParticipant(activeChar))
		{
			L2Event.restorePlayerEventStatus(activeChar);
		}
		
		// Wedding Checks
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar, activeChar.getPartnerId());
		}
		
		if (activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquippedId()).cursedOnLogin();
		}
		
		activeChar.updateEffectIcons();
		
		if (Config.PC_BANG_ENABLED)
		{
			if (activeChar.getPcBangPoints() > 0)
			{
				activeChar.sendPacket(new ExPCCafePointInfo(activeChar.getPcBangPoints(), 0, 1));
			}
			else
			{
				activeChar.sendPacket(new ExPCCafePointInfo());
			}
		}
		
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
		// Expand Skill
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		
		sendPacket(new FriendList(activeChar));
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addString(activeChar.getName());
		for (int id : activeChar.getFriendList())
		{
			L2Object obj = L2World.getInstance().findObject(id);
			if (obj != null)
			{
				obj.sendPacket(sm);
			}
		}
		
		activeChar.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);
		//Information when character is logged
        activeChar.sendMessage("Welcome " + activeChar.getName() + ".");
        
		if(Config.SHOW_ONLINE_PLAYERS_ON_LOGIN)
		{
			int totalPlayers = L2World.getInstance().getAllPlayers().size();
			int randomFakePlayers = 0;			
			Random random = new Random();
			if(totalPlayers <= 10){
				randomFakePlayers = random.nextInt(6);
			}else if(totalPlayers <= 50){
				randomFakePlayers = 30 + random.nextInt(6);
			}else if(totalPlayers <= 100){
				randomFakePlayers = 60 + random.nextInt(6);
			}else if(totalPlayers <= 300){
				randomFakePlayers = 100 + random.nextInt(6);
			}else if(totalPlayers <= 600){
				randomFakePlayers = 150 + random.nextInt(6);
			}else{
				randomFakePlayers = 250 + random.nextInt(6);
			}
	        int totalPlayersMod = totalPlayers + randomFakePlayers;
			activeChar.sendMessage("There are " + totalPlayersMod + " players online.") ;
		}
        
		if (showClanNotice)
		{
			NpcHtmlMessage notice = new NpcHtmlMessage(1);
			notice.setFile(activeChar.getHtmlPrefix(), "data/html/clanNotice.htm");
			notice.replace("%clan_name%", activeChar.getClan().getName());
			notice.replace("%notice_text%", activeChar.getClan().getNotice());
			notice.disableValidation();
			sendPacket(notice);
		}
		else if (Config.SERVER_NEWS)
		{
			String serverNews = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/servnews.htm");
			if (serverNews != null)
			{
				sendPacket(new NpcHtmlMessage(1, serverNews));
			}
		}
		
		// Bot manager punishment
		if (Config.ENABLE_BOTREPORT)
		{
			BotManager.getInstance().onEnter(activeChar);
		}
		
		// Clan Leader Color Name
		if (!activeChar.isGM() && ((activeChar.getClan() != null) && activeChar.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED && (activeChar.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)))
		{
			activeChar.getAppearance().setNameColor(Config.CLAN_LEADER_COLOR);
		}
		
		if (Config.ANNOUNCE_CASTLE_LORDS)
		{
			notifyCastleOwner(activeChar);
		}
		
		if (Config.PETITIONING_ALLOWED)
		{
			PetitionManager.getInstance().checkPetitionMessages(activeChar);
		}
		
		if (activeChar.isAlikeDead()) // dead or fake dead
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}
		
		activeChar.onPlayerEnter();
		
		sendPacket(new SkillCoolTime(activeChar));
		sendPacket(new ExVoteSystemInfo(activeChar));
		sendPacket(new ExNevitAdventEffect(0));
		sendPacket(new ExNevitAdventPointInfoPacket(activeChar));
		sendPacket(new ExNevitAdventTimeChange(activeChar.getAdventTime(), true));
		sendPacket(new ExShowContactList(activeChar));
		
		activeChar.sendAdventPointMsg();
		
		for (L2ItemInstance i : activeChar.getInventory().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
			if (i.isShadowItem() && i.isEquipped())
			{
				i.decreaseMana(false);
			}
		}
		for (L2ItemInstance i : activeChar.getWarehouse().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
		}
		for (L2ItemInstance items : activeChar.getInventory().getItems())
		{
			if (!activeChar.isGM() && items.isEquipable() && (items.getEnchantLevel() > Config.MAX_ENCHANT_LEVEL))
			{
				activeChar.getInventory().destroyItem(null, items, activeChar, null);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has an enchanted item above what is allowed! ", Config.DEFAULT_PUNISH);
				_log.info(items + " item removed from " + activeChar);
			}
		}
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		}
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		}
		
		// remove combat flag before teleporting
		if (activeChar.getInventory().getItemByItemId(9819) != null)
		{
			Fort fort = FortManager.getInstance().getFort(activeChar);
			
			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getFortId());
			}
			else
			{
				int slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
				activeChar.getInventory().unEquipItemInBodySlot(slot);
				activeChar.destroyItem("CombatFlag", activeChar.getInventory().getItemByItemId(9819), null, true);
			}
		}
		
		// Attacker or spectator logging in to a siege zone.
		// Actually should be checked for inside castle only?
		if (!activeChar.canOverrideCond(PcCondOverride.ZONE_CONDITIONS) && activeChar.isInsideZone(ZoneId.SIEGE) && (!activeChar.isInSiege() || (activeChar.getSiegeState() < 2)))
		{
			activeChar.teleToLocation(MapRegionManager.TeleportWhereType.Town);
		}
		
		if (Config.ALLOW_MAIL)
		{
			if (MailManager.getInstance().hasUnreadPost(activeChar))
			{
				sendPacket(ExNoticePostArrived.valueOf(false));
			}
		}
		
		RegionBBSManager.getInstance().changeCommunityBoard();
		CommunityServerThread.getInstance().sendPacket(new WorldInfo(activeChar, null, WorldInfo.TYPE_UPDATE_PLAYER_STATUS));
		
		if (activeChar.isAio())
		{
			onEnterAio(activeChar);
		}
		
		if (Config.ALLOW_AIO_NCOLOR && activeChar.isAio())
		{
			activeChar.getAppearance().setNameColor(Config.AIO_NCOLOR);
		}
		
		if (Config.ALLOW_AIO_TCOLOR && activeChar.isAio())
		{
			activeChar.getAppearance().setTitleColor(Config.AIO_TCOLOR);
		}
		
		if(activeChar.isVip())
		{
			onEnterVip(activeChar);
		}
		
		if(Config.ALLOW_VIP_NCOLOR && activeChar.isVip())
			activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);
			               
		if(Config.ALLOW_VIP_TCOLOR && activeChar.isVip())
			activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);

		TvTEvent.onLogin(activeChar);
		TvTRoundEvent.onLogin(activeChar);
		
		if (Config.WELCOME_MESSAGE_ENABLED)
		{
			activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_MESSAGE_TEXT, Config.WELCOME_MESSAGE_TIME));
		}
		
		L2ClassMasterInstance.showQuestionMark(activeChar);
		if (Config.ALLOW_HITMAN_GDE)
		{
			Hitman.getInstance().onEnterWorld(activeChar);
		}
		
		int birthday = activeChar.checkBirthDay();
		if (birthday == 0)
		{
			activeChar.sendPacket(SystemMessageId.YOUR_BIRTHDAY_GIFT_HAS_ARRIVED);
			// activeChar.sendPacket(new ExBirthdayPopup()); Removed in H5?
		}
		else if (birthday != -1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY);
			sm.addString(Integer.toString(birthday));
			activeChar.sendPacket(sm);
		}
		
		if (!activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(ExNotifyPremiumItem.STATIC_PACKET);
		}
		
		for (PlayerSpawnListener listener : listeners)
		{
			listener.onSpawn(activeChar);
		}
	}
	
	/**
	 * @param cha
	 */
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();
		
		for (Couple cl : CoupleManager.getInstance().getCouples())
		{
			if ((cl.getPlayer1Id() == _chaid) || (cl.getPlayer2Id() == _chaid))
			{
				if (cl.getMaried())
				{
					cha.setMarried(true);
				}
				
				cha.setCoupleId(cl.getId());
				
				if (cl.getPlayer1Id() == _chaid)
				{
					cha.setPartnerId(cl.getPlayer2Id());
				}
				else
				{
					cha.setPartnerId(cl.getPlayer1Id());
				}
			}
		}
	}
	
	/**
	 * @param cha
	 * @param partnerId
	 */
	private void notifyPartner(L2PcInstance cha, int partnerId)
	{
		if (cha.getPartnerId() != 0)
		{
			int objId = cha.getPartnerId();
			
			try
			{
				L2PcInstance partner = L2World.getInstance().getPlayer(objId);
				
				if (partner != null)
				{
					partner.sendMessage("Your Partner has logged in.");
				}
				
				partner = null;
			}
			catch (ClassCastException cce)
			{
				_log.warning("Wedding Error: ID " + objId + " is now owned by a(n) " + L2World.getInstance().findObject(objId).getClass().getSimpleName());
			}
		}
	}
	

	private void onEnterAio(L2PcInstance activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long aioEndDay = activeChar.getAioEndTime();
		if (now > aioEndDay)
		{
			activeChar.setAio(false);
			activeChar.setAioEndTime(0);
			activeChar.lostAioSkills();

			long vipEndDay = activeChar.getVipEndTime();
			long vipDaysleft = (vipEndDay - now) / 86400000;
			long aioDaysleft = (aioEndDay - now) / 86400000;
			if(aioDaysleft == vipDaysleft){
				activeChar.setVip(false);
				activeChar.setVipEndTime(0);
				activeChar.lostVipSkills();
			}
			activeChar.sendMessage("[VIP+ System]: Removed your VIP+ status, period ended.");
		}
		else
		{
			Date dt = new Date(aioEndDay);
			_daysleft = (aioEndDay - now) / 86400000;
			if (_daysleft > 30)
			{
				activeChar.sendMessage("[VIP+ System]: Period ends on " + df.format(dt) + ". Enjoy the game.");
			}
			else if (_daysleft > 0)
			{
				activeChar.sendMessage("[VIP+ System]: Left " + (int) _daysleft + " day(s) for the VIP+ period to end.");
			}
			else if (_daysleft < 1)
			{
				long hour = (aioEndDay - now) / 3600000;
				activeChar.sendMessage("[VIP+ System]: Left " + (int) hour + " hour(s) for the VIP+ period to end.");
			}
		}
	}

       private void onEnterVip(L2PcInstance activeChar)
       {
    	   if(activeChar.getAioEndTime() == 0){
               long now = Calendar.getInstance().getTimeInMillis(); // = 1.731.267.780.000   30 días en milisegundos son 2.592.000.000
               long endDay = activeChar.getVipEndTime();			// 		 2.592.000.000	
               if(now > endDay)										//   1.733.856.830.815
               {
                       activeChar.setVip(false);
                       activeChar.setVipEndTime(0);
                       activeChar.sendMessage("[VIP System]: Removed your VIP status, period ended.");
               }
               else
               {
                       Date dt = new Date(endDay);
                       _daysleft = (endDay - now)/86400000;
                       if(_daysleft > 30)
                       {
                               activeChar.sendMessage("[VIP System]: Period ends on " + df.format(dt) + ". Enjoy the game.");
                       }
                       else if(_daysleft > 0)
                       {
                               activeChar.sendMessage("[VIP System]: Left " + (int)_daysleft + " day(s) for the VIP period to end.");
                       }
                       else if(_daysleft < 1)
                       {
                               long hour = (endDay - now)/3600000;
                               activeChar.sendMessage("[VIP System]: Left " + (int)hour + " hour(s) for the VIP period to end.");
                       }
               }
    	   }
       }

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		
		// This null check may not be needed anymore since notifyClanMembers is called from within a null check already. Please remove if we're certain it's ok to do so.
		if (clan != null)
		{
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			msg = null;
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}
	
	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = L2World.getInstance().getPlayer(activeChar.getSponsor());
			
			if (sponsor != null)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = L2World.getInstance().getPlayer(activeChar.getApprentice());
			
			if (apprentice != null)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}
	
	/**
	 * @param string
	 * @return
	 */
	private String getText(String string)
	{
		try
		{
			String result = new String(Base64.decode(string), "UTF-8");
			return result;
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
	
	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		
		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
		
	}
	
	private void giveDefaultMacrosIfNeeded(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		// Solo una vez en la vida del personaje.
		if (player.getNewbie() != 1)		{
			return;
		}
		
		final String config = Config.CREATE_NEWBIE_MACRO;
		if ((config == null) || config.trim().isEmpty())
		{
			return;
		}

		// Si por cualquier motivo ya tiene macros, no duplicamos y marcamos como procesado.
		if (player.getMacros().getAllMacroses().length > 0)
		{
			player.setNewbie(0);
			player.store();
			return;
		}
		
		String[] macroEntries = config.split(";");
		boolean createdAny = false;
		
		for (String entry : macroEntries)
		{
			if ((entry == null) || entry.trim().isEmpty())
			{
				continue;
			}
			
			try
			{
				String[] parts = entry.split(",", 6);
				if (parts.length != 6)
				{
					continue;
				}
				
				String acronym = parts[0].trim();
				String name = parts[1].trim();
				int icon = Integer.parseInt(parts[2].trim());
				String command = parts[3].trim();
				int bar = Integer.parseInt(parts[4].trim());
				int slot = Integer.parseInt(parts[5].trim());
				
				if (name.isEmpty() || command.isEmpty())
				{
					continue;
				}
				
				if (acronym.isEmpty())
				{
					acronym = "";
				}
				else if (acronym.length() > 4)
				{
					acronym = acronym.substring(0, 4);
				}
				
				if ((slot < 0) || (slot > 11))
				{
					continue;
				}
				
				if ((bar < 0) || (bar > 9))
				{
					continue;
				}
				
				L2Macro macro = createDefaultMacro(player, acronym, name, icon, command);
				registerMacroShortcut(player, macro.id, slot, bar);
				createdAny = true;
			}
			catch (Exception e)
			{
				_log.warning("CreateNewbieMacro: error procesando entrada -> " + entry + " | " + e.getMessage());
			}
		}
				
		if (createdAny)
		{
			player.setNewbie(0);
			player.store();
		}
	}
	
	private L2Macro createDefaultMacro(L2PcInstance player, String acronym, String name, int icon, String command)
	{
		L2Macro.L2MacroCmd[] commands =
		{
			new L2Macro.L2MacroCmd(1, L2Macro.CMD_TYPE_ACTION, 0, 0, command)
		};
		
		L2Macro macro = new L2Macro(0, icon, name, "", acronym, commands);
		player.registerMacro(macro);
		return macro;
	}
	
	private void registerMacroShortcut(L2PcInstance player, int macroId, int slot, int page)
	{
		L2ShortCut shortCut = new L2ShortCut(slot, page, L2ShortCut.TYPE_MACRO, macroId, 1, 1);
		player.registerShortCut(shortCut);
		player.sendPacket(new ShortCutRegister(shortCut));
	}

	@Override
	public String getType()
	{
		return _C__11_ENTERWORLD;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
	
	// Player spawn listeners
	/**
	 * Adds a spawn listener
	 * @param listener
	 */
	public static void addSpawnListener(PlayerSpawnListener listener)
	{
		if (!listeners.contains(listener))
		{
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a spawn listener
	 * @param listener
	 */
	public static void removeSpawnListener(PlayerSpawnListener listener)
	{
		listeners.remove(listener);
	}
	
	private void notifyCastleOwner(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		
		if (clan != null)
		{
			if (clan.getCastleId() > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(clan.getCastleId());
				if ((castle != null) && (activeChar.getObjectId() == clan.getLeaderId()))
				{
					Announcements.getInstance().announceToAll("Lord " + activeChar.getName() + " Ruler Of " + castle.getName() + " Castle is Now Online!");
				}
			}
		}
	}
}
