/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fidas.server.gameserver.model.actor.instance;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;

import fidas.server.Config;
import fidas.server.gameserver.datatables.EnchantItemData;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.model.EnchantScroll;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.templates.L2NpcTemplate;
import fidas.server.gameserver.model.items.L2Armor;
import fidas.server.gameserver.model.items.L2Item;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.ChooseInventoryItem;
import fidas.server.gameserver.network.serverpackets.EnchantResult;
import fidas.server.gameserver.network.serverpackets.InventoryUpdate;
import fidas.server.gameserver.network.serverpackets.ItemList;
import fidas.server.gameserver.network.serverpackets.MagicSkillUse;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.network.serverpackets.StatusUpdate;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.gameserver.util.Util;
import fidas.server.util.Rnd;
//import fidas.server.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;

/**
 * @author Wyatt TODO:FIXME visual bug related with target item
 */
public final class L2EnchanterInstance extends L2Npc
{
	boolean w = false;
	
	public L2EnchanterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2EnchanterInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance activeChar, String actualCommand)
	{
		StringTokenizer st = new StringTokenizer(actualCommand, " ");
		actualCommand = st.nextToken();
		
		if (actualCommand.equalsIgnoreCase("enchant") && !activeChar.isImmobilized())
		{
			int i = Integer.parseInt(st.nextToken());
			int q;
			try
			{
				q = Integer.parseInt(st.nextToken());
			}
			catch (NumberFormatException enf)
			{
				activeChar.sendMessage("Enter a valid number.");
				return;
			}
			catch (NoSuchElementException enf)
			{
				activeChar.sendMessage("Enter a valid number.");
				return;
			}
			if (i == 5)
			{
				w = true;
			}
			else
			{
				w = false;
			}
			L2ItemInstance t = activeChar.getInventory().getPaperdollItem(i);
			
			if (t == null)
			{
				activeChar.sendMessage("You must have equiped an item in order to enchant it.");
				return;
			}
			
			int l = t.getItem().getCrystalType();
			int d = 0;
			
			switch (l)
			{
				case 1:
					if (w)
					{
						d = 955;
					}
					else
					{
						d = 956;
					}
					break;
				case 2:
					if (w)
					{
						d = 951;
					}
					else
					{
						d = 952;
					}
					break;
				case 3:
					if (w)
					{
						d = 947;
					}
					else
					{
						d = 948;
					}
					break;
				case 4:
					if (w)
					{
						d = 729;
					}
					else
					{
						d = 730;
					}
					break;
				case 5:
					if (w)
					{
						d = 959;
					}
					else
					{
						d = 960;
					}
					break;
				case 6:
					if (w)
					{
						d = 959;
					}
					else
					{
						d = 960;
					}
					break;
				case 7:
					if (w)
					{
						d = 959;
					}
					else
					{
						d = 960;
					}
					break;
			}
			
			if (d == 0)
			{
				activeChar.sendMessage("The item grade is not enchantable.");
				return;
			}
			
			if (activeChar.getInventory().getInventoryItemCount(d, -1) < q)
			{
				activeChar.sendMessage("You don't have enough scrolls of enchant.");
				return;
			}
			
			try
			{
				activeChar.setIsImmobilized(true);
				activeChar.setIsEnchanting(true);
				for (int u = 0; u < q; u++)
				{
					if (!enchant(activeChar, i, d))
					{
						continue;
					}
					Thread.sleep(2000);
				}
				activeChar.setIsImmobilized(false);
				activeChar.setIsEnchanting(false);
			}
			catch (InterruptedException e)
			{
			}
		}
		else
		{
			super.onBypassFeedback(activeChar, actualCommand);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		NpcHtmlMessage page = new NpcHtmlMessage(5);
		TextBuilder replyMSG = new TextBuilder();
		replyMSG.append("<html><title>Enchanter Menu</title><body>");
		replyMSG.append("<br>Hi " + player.getName() + "! This is a Npc Enchanter that will automatic land the number of enchants that you specify here in your weapon/jewel/armor. It's not a safe enchant npc, so for every enchant it will takes the % chance of success of your server (if its ++ of the safe enchant value).<br>");
		replyMSG.append("<center><table width=270><tr><td><font color=LEVEL>Number of enchants:</font></td><td><edit var=\"qbox\" width=80 height=15></td></tr></table><br>");
		replyMSG.append("<center><table width=290 border=0 bgcolor=444444><tr><td><center><button value=\"Weapon\" action=\"bypass -h npc_" + getObjectId() + "_enchant 5 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td><center><button value=\"Shield\" action=\"bypass -h npc_" + getObjectId() + "_enchant 7 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td><center><button value=\"Helmet\" action=\"bypass -h npc_" + getObjectId() + "_enchant 1 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td><center><button value=\"Chest\" action=\"bypass -h npc_" + getObjectId() + "_enchant 6 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr></table><br>");
		replyMSG.append("<center><table width=290 border=0 bgcolor=444444><tr><td><center><button value=\"Leggings\" action=\"bypass -h npc_" + getObjectId() + "_enchant 11 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td><center><button value=\"Gloves\" action=\"bypass -h npc_" + getObjectId() + "_enchant 10 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td><center><button value=\"Boots\" action=\"bypass -h npc_" + getObjectId() + "_enchant 12 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td><center><button value=\"Necklace\" action=\"bypass -h npc_" + getObjectId() + "_enchant 4 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr></table><br>");
		replyMSG.append("<center><table width=290 border=0 bgcolor=444444><tr><td><center><button value=\"L. Earring\" action=\"bypass -h npc_" + getObjectId() + "_enchant 9 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td><center><button value=\"R. Earring\" action=\"bypass -h npc_" + getObjectId() + "_enchant 8 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td><center><button value=\"L. Ring\" action=\"bypass -h npc_" + getObjectId() + "_enchant 14 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td><center><button value=\"R. Ring\" action=\"bypass -h npc_" + getObjectId() + "_enchant 13 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr></table><br>");
		replyMSG.append("<center><table width=290 border=0 bgcolor=444444><tr><td width=72></td><td width=72><center><button value=\"Shirt\" action=\"bypass -h npc_" + getObjectId() + "_enchant 0 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td width=72><center><button value=\"Belt\" action=\"bypass -h npc_" + getObjectId() + "_enchant 24 $qbox\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td><td width=72></td></tr></table><br>");
		replyMSG.append("</body></html>");
		page.setHtml(replyMSG.toString());
		player.sendPacket(page);
	}
	
	private static boolean enchant(L2PcInstance activeChar, int i, int d) throws InterruptedException
	{
		L2ItemInstance t = activeChar.getInventory().getPaperdollItem(i);
		if (t == null)
		{
			return false;
		}
		L2ItemInstance s = activeChar.getInventory().getItemByItemId(d);
		activeChar.setActiveEnchantItem(s);
		activeChar.sendPacket(new ChooseInventoryItem(s.getItemId()));
		// activeChar.sendPacket(new ExPutEnchantTargetItemResult(7575)); TODO:Target item icon not done. It shows you the last item you enchanted through normal method.
		Thread.sleep(1000);
		enchantproccess(activeChar, t.getObjectId(), t, d);
		return true;
	}
	
	private static void enchantproccess(L2PcInstance activeChar, int _objectId, L2ItemInstance t, int d)
	{
		if ((activeChar == null) || (_objectId == 0))
		{
			return;
		}
		
		if (!activeChar.isOnline() || activeChar.getClient().isDetached())
		{
			activeChar.setActiveEnchantItem(null);
			return;
		}
		
		if (activeChar.isProcessingTransaction() || activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE));
			activeChar.setActiveEnchantItem(null);
			return;
		}
		
		L2ItemInstance item = t;
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();
		
		if ((item == null) || (scroll == null))
		{
			activeChar.setActiveEnchantItem(null);
			return;
		}
		
		EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
		if (scrollTemplate == null)
		{
			return;
		}
		
		synchronized (item)
		{
			double chance = scrollTemplate.getChance(item, null);
			L2Skill enchant4Skill = null;
			L2Item it = item.getItem();
			if ((item.getOwnerId() != activeChar.getObjectId()) || (item.isEnchantable() == 0) || (chance < 0))
			{
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
				activeChar.setActiveEnchantItem(null);
				activeChar.sendPacket(new EnchantResult(2, 0, 0));
				return;
			}
			
			if (Rnd.get(100) < chance)
			{
				item.setEnchantLevel(item.getEnchantLevel() + 1);
				item.updateDatabase();
				activeChar.sendPacket(new EnchantResult(0, 0, 0));
				int minEnchantAnnounce = item.isArmor() ? 6 : 7;
				int maxEnchantAnnounce = item.isArmor() ? 0 : 15;
				if ((item.getEnchantLevel() == minEnchantAnnounce) || (item.getEnchantLevel() == maxEnchantAnnounce))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_SUCCESSFULY_ENCHANTED_A_S2_S3);
					sm.addCharName(activeChar);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item);
					activeChar.broadcastPacket(sm);
					
					L2Skill skill = SkillTable.FrequentSkill.FIREWORK.getSkill();
					if (skill != null)
					{
						activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
					}
				}
				
				if ((it instanceof L2Armor) && (item.getEnchantLevel() == 4) && activeChar.getInventory().getItemByObjectId(item.getObjectId()).isEquipped())
				{
					enchant4Skill = ((L2Armor) it).getEnchant4Skill();
					if (enchant4Skill != null)
					{
						activeChar.addSkill(enchant4Skill, false);
						activeChar.sendSkillList();
					}
				}
			}
			else
			{
				if (scrollTemplate.isSafe())
				{
					activeChar.sendPacket(new EnchantResult(5, 0, 0));
				}
				else
				{
					if (item.isEquipped())
					{
						if (item.getEnchantLevel() > 0)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
							sm.addNumber(item.getEnchantLevel());
							sm.addItemName(item);
							activeChar.sendPacket(sm);
						}
						else
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
							sm.addItemName(item);
							activeChar.sendPacket(sm);
						}
						
						L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
						InventoryUpdate iu = new InventoryUpdate();
						for (L2ItemInstance itm : unequiped)
						{
							iu.addModifiedItem(itm);
						}
						
						activeChar.sendPacket(iu);
						activeChar.broadcastUserInfo();
					}
					
					if (scrollTemplate.isBlessed())
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BLESSED_ENCHANT_FAILED));
						item.setEnchantLevel(0);
						item.updateDatabase();
						activeChar.sendPacket(new EnchantResult(3, 0, 0));
					}
					else
					{
						int crystalId = item.getItem().getCrystalItemId();
						int count = item.getCrystalCount() - ((item.getItem().getCrystalCount() + 1) / 2);
						if (count < 1)
						{
							count = 1;
						}
						
						L2ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
						if (destroyItem == null)
						{
							Util.handleIllegalPlayerAction(activeChar, "Unable to delete item on enchant failure from player " + activeChar.getName() + ", possible cheater !", Config.DEFAULT_PUNISH);
							activeChar.setActiveEnchantItem(null);
							activeChar.sendPacket(new EnchantResult(2, 0, 0));
							return;
						}
						
						L2ItemInstance crystals = null;
						if (crystalId != 0)
						{
							crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, destroyItem);
							
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(crystals);
							sm.addItemNumber(count);
							activeChar.sendPacket(sm);
						}
						
						if (!Config.FORCE_INVENTORY_UPDATE)
						{
							InventoryUpdate iu = new InventoryUpdate();
							if (destroyItem.getCount() == 0)
							{
								iu.addRemovedItem(destroyItem);
							}
							else
							{
								iu.addModifiedItem(destroyItem);
							}
							
							if (crystals != null)
							{
								iu.addItem(crystals);
							}
							
							activeChar.sendPacket(iu);
						}
						else
						{
							activeChar.sendPacket(new ItemList(activeChar, true));
						}
						L2World world = L2World.getInstance();
						world.removeObject(destroyItem);
						if (crystalId == 0)
						{
							activeChar.sendPacket(new EnchantResult(4, 0, 0));
						}
						else
						{
							activeChar.sendPacket(new EnchantResult(1, crystalId, count));
						}
					}
				}
			}
			activeChar.destroyItemByItemId("Echanter Npc", d, 1, activeChar, true);
			StatusUpdate su = new StatusUpdate(activeChar);
			su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
			activeChar.sendPacket(su);
			activeChar.sendPacket(new ItemList(activeChar, false));
			activeChar.broadcastUserInfo();
			activeChar.setActiveEnchantItem(null);
		}
	}
}