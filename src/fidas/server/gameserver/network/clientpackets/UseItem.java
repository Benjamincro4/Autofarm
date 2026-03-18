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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

import fidas.server.Config;
import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.GameTimeController;
import fidas.server.gameserver.GmListTable;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.ai.CtrlEvent;
import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.ai.NextAction;
import fidas.server.gameserver.ai.NextAction.NextActionCallback;
import fidas.server.gameserver.events.EventsInterface;
import fidas.server.gameserver.handler.IItemHandler;
import fidas.server.gameserver.handler.ItemHandler;
import fidas.server.gameserver.instancemanager.FortSiegeManager;
import fidas.server.gameserver.model.L2Clan;
import fidas.server.gameserver.model.PcCondOverride;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.base.Race;
import fidas.server.gameserver.model.holders.SkillHolder;
import fidas.server.gameserver.model.itemcontainer.Inventory;
import fidas.server.gameserver.model.items.L2EtcItem;
import fidas.server.gameserver.model.items.L2Item;
import fidas.server.gameserver.model.items.L2Weapon;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.model.items.type.L2ArmorType;
import fidas.server.gameserver.model.items.type.L2WeaponType;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.model.skills.L2SkillType;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.ActionFailed;
import fidas.server.gameserver.network.serverpackets.EtcStatusUpdate;
import fidas.server.gameserver.network.serverpackets.ExUseSharedGroupItem;
import fidas.server.gameserver.network.serverpackets.ItemList;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.gameserver.util.Util;

public final class UseItem extends L2GameClientPacket
{
	private static final String _C__19_USEITEM = "[C] 19 UseItem";
	
	private int _objectId;
	private boolean _ctrlPressed;
	private int _itemId;
	
	/** Weapon Equip Task */
	private static class WeaponEquipTask implements Runnable
	{
		L2ItemInstance item;
		L2PcInstance activeChar;
		
		protected WeaponEquipTask(L2ItemInstance it, L2PcInstance character)
		{
			item = it;
			activeChar = character;
		}
		
		@Override
		public void run()
		{
			// If character is still engaged in strike we should not change weapon
			if (activeChar.isAttackingNow())
			{
				return;
			}
			
			// Equip or unEquip
			activeChar.useEquippableItem(item, false);
		}
	}
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
					
		if (Config.DEBUG)
		{
			_log.log(Level.INFO, activeChar + ": use item " + _objectId);
		}
		
		// Flood protect UseItem
		if (!getClient().getFloodProtectors().getUseItem().tryPerformAction("use item"))
		{
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.cancelActiveTrade();
		}
		
		if (activeChar.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}
		
		if (item.getItem().getType2() == L2Item.TYPE2_QUEST)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			return;
		}
		
		// No UseItem is allowed while the player is in special conditions
		if (activeChar.isStunned() || activeChar.isParalyzed() || activeChar.isSleeping() || activeChar.isAfraid() || activeChar.isAlikeDead())
		{
			return;
		}
		
		// Char cannot use item when dead
		if (activeChar.isDead() || !activeChar.getInventory().canManipulateWithItemId(item.getItemId()))
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (!item.isEquipped() && !item.getItem().checkCondition(activeChar, activeChar, true))
		{
			return;
		}
		
		_itemId = item.getItemId();
		if (activeChar.isFishing() && ((_itemId < 6535) || (_itemId > 6540)))
		{
			// You cannot do anything else while fishing
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (activeChar.getKarma() > 0))
		{
			SkillHolder[] skills = item.getItem().getSkills();
			if (skills != null)
			{
				for (SkillHolder sHolder : skills)
				{
					L2Skill skill = sHolder.getSkill();
					if ((skill != null) && ((skill.getSkillType() == L2SkillType.TELEPORT) || (skill.getSkillType() == L2SkillType.RECALL)))
					{
						return;
					}
				}
			}
		}
		
		// If the item has reuse time and it has not passed.
		// Message from reuse delay must come from item.
		final int reuseDelay = item.getReuseDelay();
		final int sharedReuseGroup = item.getSharedReuseGroup();
		if (reuseDelay > 0)
		{
			final long reuse = activeChar.getItemRemainingReuseTime(item.getObjectId());
			if (reuse > 0)
			{
				reuseData(activeChar, item);
				sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuse, reuseDelay);
				return;
			}
			
			final long reuseOnGroup = activeChar.getReuseDelayOnGroup(sharedReuseGroup);
			if (reuseOnGroup > 0)
			{
				reuseData(activeChar, item);
				sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuseOnGroup, reuseDelay);
				return;
			}
		}

		if (EventsInterface.isParticipating(activeChar.getObjectId()))
		{
			if (!EventsInterface.onUseItem(activeChar.getObjectId(), item.getItemId(), item.getObjectId()))
			{
				return;
			}
		}

		if (item.isEquipable())
		{
			if (!activeChar.isGM() && item.getEnchantLevel() > Config.MAX_ENCHANT_LEVEL_PROTECT )
			{                              
				activeChar.sendMessage("You have been kicked for using an item overenchanted!");
				//activeChar.closeNetConnection();
				return;
			}

			if (!activeChar.isGM() && (item.getEnchantLevel() > Config.MAX_ENCHANT_LEVEL))
			{
				// Destroy the item
				activeChar.getInventory().destroyItem(null, item, activeChar, null);
				
				// kick or ban the player
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has Overenchanted  item! Kicked! ", Config.DEFAULT_PUNISH);
			}
			
			// Don't allow to put formal wear while a cursed weapon is equipped.
			if (activeChar.isCursedWeaponEquipped() && (_itemId == 6408))
			{
				return;
			}
			
			// Equip or unEquip
			if (FortSiegeManager.getInstance().isCombat(_itemId))
			{
				return; // no message
			}
			
			if (activeChar.isCombatFlagEquipped())
			{
				return;
			}
			
			switch (item.getItem().getBodyPart())
			{
				case L2Item.SLOT_LR_HAND:
				case L2Item.SLOT_L_HAND:
				case L2Item.SLOT_R_HAND:
				{
					// Prevent players to equip weapon while wearing combat flag
					if ((activeChar.getActiveWeaponItem() != null) && (activeChar.getActiveWeaponItem().getItemId() == 9819))
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					
					if (activeChar.isMounted())
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					if (activeChar.isDisarmed())
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					
					// Don't allow weapon/shield equipment if a cursed weapon is equipped.
					if (activeChar.isCursedWeaponEquipped())
					{
						return;
					}
					
					// Don't allow other Race to Wear Kamael exclusive Weapons.
					if (!item.isEquipped() && item.isWeapon() && !activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS))
					{
						L2Weapon wpn = (L2Weapon) item.getItem();
						
						switch (activeChar.getRace())
						{
							case Kamael:
							{
								switch (wpn.getItemType())
								{
									case NONE:
										activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
										return;
								}
								break;
							}
							case Human:
							case Dwarf:
							case Elf:
							case DarkElf:
							case Orc:
							{
								switch (wpn.getItemType())
								{
									case RAPIER:
									case CROSSBOW:
									case ANCIENTSWORD:
										activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
										return;
								}
								break;
							}
						}
					}
					break;
				}
				case L2Item.SLOT_CHEST:
				case L2Item.SLOT_BACK:
				case L2Item.SLOT_GLOVES:
				case L2Item.SLOT_FEET:
				case L2Item.SLOT_HEAD:
				case L2Item.SLOT_FULL_ARMOR:
				case L2Item.SLOT_LEGS:
				{
					if ((activeChar.getRace() == Race.Kamael) && ((item.getItem().getItemType() == L2ArmorType.HEAVY) || (item.getItem().getItemType() == L2ArmorType.MAGIC)))
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					break;
				}
				case L2Item.SLOT_DECO:
				{
					if (!item.isEquipped() && (activeChar.getInventory().getMaxTalismanCount() == 0))
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
				}
			}
			
			if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
			{
				// Creating next action class.
				final NextAction nextAction = new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, new NextActionCallback()
				{
					@Override
					public void doWork()
					{
						activeChar.useEquippableItem(item, true);
					}
				});
				
				// Binding next action to AI.
				activeChar.getAI().setNextAction(nextAction);
			}
			else if (activeChar.isAttackingNow())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new WeaponEquipTask(item, activeChar), (activeChar.getAttackEndTime() - GameTimeController.getGameTicks()) * GameTimeController.MILLIS_IN_TICK);
			}
			else
			{
				activeChar.useEquippableItem(item, true);
			}
		}
		else
		{
			if (activeChar.isCastingNow() && !(item.isPotion() || item.isElixir()))
			{
				return;
			}

			final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			if (((weaponItem != null) && (weaponItem.getItemType() == L2WeaponType.FISHINGROD)) && (((_itemId >= 6519) && (_itemId <= 6527)) || ((_itemId >= 7610) && (_itemId <= 7613)) || ((_itemId >= 7807) && (_itemId <= 7809)) || ((_itemId >= 8484) && (_itemId <= 8486)) || ((_itemId >= 8505) && (_itemId <= 8513))))
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipment.
				sendPacket(new ItemList(activeChar, false));
				return;
			}
			
			final L2EtcItem etcItem = item.getEtcItem();
			final IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
			if (handler == null)
			{
				if ((etcItem != null) && (etcItem.getHandlerName() != null))
				{
					_log.log(Level.WARNING, "Unmanaged Item handler: " + etcItem.getHandlerName() + " for Item Id: " + _itemId + "!");
				}
				else if (Config.DEBUG)
				{
					_log.log(Level.WARNING, "No Item handler registered for Item Id: " + _itemId + "!");
				}
				return;
			}
			
			boolean useItem = useCustomItems(activeChar, item);
			if(useItem){
				// Item reuse time should be added if the item is successfully used.
				// Skill reuse delay is done at handlers.itemhandlers.ItemSkillsTemplate;
				if (handler.useItem(activeChar, item, _ctrlPressed))
				{
					if (reuseDelay > 0)
					{
						activeChar.addTimeStampItem(item, reuseDelay);
						sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuseDelay, reuseDelay);
					}
				}
			}
		}
	}
	
	private boolean useCustomItems(L2PcInstance activeChar, L2ItemInstance item){
		boolean useItem = true;
		int itemId = item.getItemId();
		switch(itemId) {
			case 22601:
				// VIP+ 7d
				if(!activeChar.isAio()){
					int diasVip = Integer.parseInt("7");					
					activeChar.setAio(true);
					activeChar.setVip(true);
					activeChar.setEndTime("aio", diasVip);
					if(activeChar.getAioEndTime() > activeChar.getVipEndTime()){
						activeChar.setEndTime("vip", diasVip);
					}
					try (Connection connection = L2DatabaseFactory.getInstance().getConnection())
					{
						PreparedStatement statement = connection.prepareStatement("UPDATE characters SET aio=1, vip=1, aio_end=?, vip_end=? WHERE CharId=?");
						statement.setLong(1, activeChar.getAioEndTime());
						statement.setLong(2, activeChar.getVipEndTime());
						statement.setInt(3, activeChar.getObjectId());
						statement.execute();
						statement.close();
						connection.close();
						
						if(Config.ALLOW_AIO_NCOLOR && activeChar.isAio()){
							activeChar.getAppearance().setNameColor(Config.AIO_NCOLOR);
						}						
						if(Config.ALLOW_AIO_TCOLOR && activeChar.isAio()){
							activeChar.getAppearance().setTitleColor(Config.AIO_TCOLOR);
						}
						
						activeChar.rewardAioSkills();
						activeChar.rewardVipSkills();
						activeChar.broadcastUserInfo();
						activeChar.sendPacket(new EtcStatusUpdate(activeChar));
						activeChar.sendSkillList();
						_client.sendPacket(SystemMessage.sendString("Congratulations! You have become a VIP+ member for the next 7 days."));
						GmListTable.broadcastMessageToGMs("GM give premium status Vip+ to "+ activeChar.getName() + " for 7 days");
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING,"Not possible to give premium status Vip+ to player:", e);
						useItem = false;
					}
				}else{
					_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, you are already VIP+ member."));
					useItem = false;
				}
				break;
			case 22602:
				// VIP+ 30d
				if(!activeChar.isAio()){
					int diasVip = Integer.parseInt("30");					
					activeChar.setAio(true);
					activeChar.setVip(true);
					activeChar.setEndTime("aio", diasVip);
					if(activeChar.getAioEndTime() > activeChar.getVipEndTime()){
						activeChar.setEndTime("vip", diasVip);
					}
					try (Connection connection = L2DatabaseFactory.getInstance().getConnection())
					{
						PreparedStatement statement = connection.prepareStatement("UPDATE characters SET aio=1, vip=1, aio_end=?, vip_end=? WHERE CharId=?");
						statement.setLong(1, activeChar.getAioEndTime());
						statement.setLong(2, activeChar.getVipEndTime());
						statement.setInt(3, activeChar.getObjectId());
						statement.execute();
						statement.close();
						connection.close();
						
						if(Config.ALLOW_AIO_NCOLOR && activeChar.isAio()){
							activeChar.getAppearance().setNameColor(Config.AIO_NCOLOR);
						}						
						if(Config.ALLOW_AIO_TCOLOR && activeChar.isAio()){
							activeChar.getAppearance().setTitleColor(Config.AIO_TCOLOR);
						}
						
						activeChar.rewardAioSkills();
						activeChar.rewardVipSkills();
						activeChar.broadcastUserInfo();
						activeChar.sendPacket(new EtcStatusUpdate(activeChar));
						activeChar.sendSkillList();
						_client.sendPacket(SystemMessage.sendString("Congratulations! You have become a VIP+ member for the next 30 days."));
						GmListTable.broadcastMessageToGMs("GM give premium status Vip+ to "+ activeChar.getName() + " for 30 days");
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING,"Not possible to give premium status Vip+ to player:", e);
						useItem = false;
					}
				}else{
					_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, you are already VIP+ member."));
					useItem = false;
				}
				break;
			case 22603:
				// VIP 7d
				if(!activeChar.isVip()){
					int diasVip = Integer.parseInt("7");
					activeChar.setVip(true);
					activeChar.setEndTime("vip", diasVip);
					try (Connection connection = L2DatabaseFactory.getInstance().getConnection())
					{
						PreparedStatement statement = connection.prepareStatement("UPDATE characters SET vip=1, vip_end=? WHERE CharId=?");
						statement.setLong(1, activeChar.getVipEndTime());
						statement.setInt(2, activeChar.getObjectId());
						statement.execute();
						statement.close();
						connection.close();
						
						if(Config.ALLOW_VIP_NCOLOR && activeChar.isVip()){
							activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);
						}						
						if(Config.ALLOW_VIP_TCOLOR && activeChar.isVip()){
							activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);
						}
						
						activeChar.rewardVipSkills();
						activeChar.broadcastUserInfo();
						activeChar.sendPacket(new EtcStatusUpdate(activeChar));
						activeChar.sendSkillList();
						_client.sendPacket(SystemMessage.sendString("Congratulations! You have become a VIP member for the next 7 days."));
						GmListTable.broadcastMessageToGMs("GM give premium status Vip to "+ activeChar.getName() + " for 7 days");
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING,"Not possible to give premium status Vip to player:", e);
						useItem = false;
					}
				}else{
					if(activeChar.isAio()){
						_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, you are already VIP+ member."));
					}else{
						_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, you are already VIP member."));
					}
					useItem = false;
				}
				break;
			case 22604:
				//VIP 30d
				if(!activeChar.isVip()){
					int diasVip = Integer.parseInt("30");
					activeChar.setVip(true);
					activeChar.setEndTime("vip", diasVip);
					try (Connection connection = L2DatabaseFactory.getInstance().getConnection())
					{
						PreparedStatement statement = connection.prepareStatement("UPDATE characters SET vip=1, vip_end=? WHERE CharId=?");
						statement.setLong(1, activeChar.getVipEndTime());
						statement.setInt(2, activeChar.getObjectId());
						statement.execute();
						statement.close();
						connection.close();
						
						if(Config.ALLOW_VIP_NCOLOR && activeChar.isVip()){
							activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);
						}						
						if(Config.ALLOW_VIP_TCOLOR && activeChar.isVip()){
							activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);
						}
						
						activeChar.rewardVipSkills();
						activeChar.broadcastUserInfo();
						activeChar.sendPacket(new EtcStatusUpdate(activeChar));
						activeChar.sendSkillList();
						_client.sendPacket(SystemMessage.sendString("Congratulations! You have become a VIP member for the next 30 days."));
						GmListTable.broadcastMessageToGMs("GM give premium status Vip to "+ activeChar.getName() + " for 30 days");
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING,"Not possible to give premium status Vip to player:", e);
						useItem = false;
					}
				}else{
					if(activeChar.isAio()){
						_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, you are already VIP+ member."));
					}else{
						_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, you are already VIP member."));
					}
					useItem = false;
				}
				break;
			case 22605:
				// Noble
				if(!activeChar.isNoble()){
					activeChar.setNoble(true);
					_client.sendPacket(SystemMessage.sendString("Congratulations! You have become Noble."));
				}else{					
					_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, you are already noble."));
					useItem = false;
				}
				break;
			case 22606:
				// Clan + clanskills
				L2Clan clan = activeChar.getClan();
				if(clan != null){
					if(activeChar.isClanLeader()){
						int curClanLevel = activeChar.getClan().getLevel();
						if(curClanLevel < 8 || activeChar.getClan().getSkills().size() < 22){
							if(curClanLevel < 8){
								activeChar.getClan().changeLevel(8);
							}
							activeChar.ClanSkills();
							activeChar.sendPacket(new EtcStatusUpdate(activeChar));
							_client.sendPacket(SystemMessage.sendString("Congratulations! Your clan is now level " + activeChar.getClan().getLevel() + " and all clan skills have been added."));
						}else{	
							_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, your clan is level 8 or higher and has all clan skills."));
							useItem = false;
						}
					}else{
						_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, you are not the clan leader."));
						useItem = false;
					}
				}else{
					_client.sendPacket(SystemMessage.sendString(item.getName() + " cannot be used, you do not have clan"));
					useItem = false;
				}
				
				break;
			case 22609:
				// Extra XP
				System.out.println("Usando Extra XP");
				break;
			case 22610:
				// Extra Adena & Drop
				System.out.println("Usando Extra Adena & Drop");
				break;
			case 22611:
				// Extra Spoil
				System.out.println("Usando Extra Spoil");
				break;
		}
		return useItem;
	}
	
	private void reuseData(L2PcInstance activeChar, L2ItemInstance item)
	{
		SystemMessage sm = null;
		final long remainingTime = activeChar.getItemRemainingReuseTime(item.getObjectId());
		final int hours = (int) (remainingTime / 3600000L);
		final int minutes = (int) (remainingTime % 3600000L) / 60000;
		final int seconds = (int) ((remainingTime / 1000) % 60);
		if (hours > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addItemName(item);
			sm.addNumber(hours);
			sm.addNumber(minutes);
		}
		else if (minutes > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addItemName(item);
			sm.addNumber(minutes);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addItemName(item);
		}
		sm.addNumber(seconds);
		activeChar.sendPacket(sm);
	}
	
	private void sendSharedGroupUpdate(L2PcInstance activeChar, int group, long remaining, int reuse)
	{
		if (group > 0)
		{
			activeChar.sendPacket(new ExUseSharedGroupItem(_itemId, group, remaining, reuse));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__19_USEITEM;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return !Config.SPAWN_PROTECTION_ALLOWED_ITEMS.contains(_itemId);
	}
}
