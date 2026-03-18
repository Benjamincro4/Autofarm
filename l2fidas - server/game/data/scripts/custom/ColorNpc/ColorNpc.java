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
package custom.ColorNpc;

import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.instancemanager.QuestManager;
import fidas.server.gameserver.network.serverpackets.UserInfo;

/******************************
 * @author Gladicek           *
 * Custom Npc 'Color Npc'     *
 *****************************/
 
 public class ColorNpc extends Quest
{
  // Npc
	private static final int NpcId = 49295; // Custom Npc
  // Item 
  private static final int ItemId = 6673; // Festival Adena
  
  	public ColorNpc(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(NpcId);
    addFirstTalkId(NpcId);
    addTalkId(NpcId);
	}
                
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName()); 
   
    // Green
		if (event.equalsIgnoreCase("1"))
		{
     if(st.getQuestItemsCount(ItemId) >= 1)
     {
       st.takeItems(ItemId,1);
       st.getPlayer().getAppearance().setTitleColor(0x009900);
       player.sendPacket(new UserInfo(player));
       player.sendMessage("Your title color has been changed!");
       return null;
     }
	return "49295-no.htm";      
    }
    // Blue
		else if (event.equalsIgnoreCase("2"))
		{
     if(st.getQuestItemsCount(ItemId) >= 1)
     {
       st.takeItems(ItemId,1);
       st.getPlayer().getAppearance().setTitleColor(0xff7f00);
       player.sendPacket(new UserInfo(player));
       player.sendMessage("Your title color has been changed!");
       return null;
     }
	return "49295-no.htm";      
    }
    // Purple
		else if (event.equalsIgnoreCase("3"))
		{
     if(st.getQuestItemsCount(ItemId) >= 1)
     {
       st.takeItems(ItemId,1);
       st.getPlayer().getAppearance().setTitleColor(0xff00ff);
       player.sendPacket(new UserInfo(player));
       player.sendMessage("Your title color has been changed!");
       return null;
     }
	return "49295-no.htm";      
    }
    // Yellow
		else if (event.equalsIgnoreCase("4"))
		{
     if(st.getQuestItemsCount(ItemId) >= 1)
     {
       st.takeItems(ItemId,1);
       st.getPlayer().getAppearance().setTitleColor(0x00ffff);
       player.sendPacket(new UserInfo(player));
       player.sendMessage("Your title color has been changed!"); 
       return null;
     }
	return "49295-no.htm";      
    }
    // Red
		else if (event.equalsIgnoreCase("5"))
		{
     if(st.getQuestItemsCount(ItemId) >= 1)
     {
       st.takeItems(ItemId,1);
       st.getPlayer().getAppearance().setTitleColor(0x0000ff);
       player.sendPacket(new UserInfo(player));
       player.sendMessage("Your title color has been changed!");  
       return null;
     }
	return "49295-no.htm";      
    }
    // Orange
		else if (event.equalsIgnoreCase("6"))
		{
     if(st.getQuestItemsCount(ItemId) >= 1)
     {
       st.takeItems(ItemId,1);
       st.getPlayer().getAppearance().setTitleColor(0x0099ff);
       player.sendPacket(new UserInfo(player));
       player.sendMessage("Your title color has been changed!");
       return null;
     }
	return "49295-no.htm";      
    }
    //  Light Green
		else if (event.equalsIgnoreCase("7"))
		{
     if(st.getQuestItemsCount(ItemId) >= 1)
     {
       st.takeItems(ItemId,1);
       st.getPlayer().getAppearance().setTitleColor(0x70db93);
       player.sendPacket(new UserInfo(player));
       player.sendMessage("Your title color has been changed!");
       return null;
     }
	return "49295-no.htm";      
    }
    // Gray
		else if (event.equalsIgnoreCase("8"))
		{
     if(st.getQuestItemsCount(ItemId) >= 1)
     {
       st.takeItems(ItemId,1);
       st.getPlayer().getAppearance().setTitleColor(0x9f9f9f);
       player.sendPacket(new UserInfo(player));
       player.sendMessage("Your title color has been changed!");
       return null;
     }
	return "49295-no.htm";      
    }
    // Aqua
		else if (event.equalsIgnoreCase("9"))
		{
     if(st.getQuestItemsCount(ItemId) >= 1)
     {
       st.takeItems(ItemId,1);
       st.getPlayer().getAppearance().setTitleColor(0xffff00);
       player.sendPacket(new UserInfo(player));
       player.sendMessage("Your title color has been changed!");
       return null;
     }
	return "49295-no.htm";      
    }                                
   return htmltext;
	}
  
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
    }  
		return "49295.htm";
	}    
    
	public static void main(String[] args)
	{
		new ColorNpc(-1, "ColorNpc", "custom");
    _log.info("ColorNpc: Loaded successfully.");
	}
}   