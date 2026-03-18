package custom.NoblesseManager;

import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.instancemanager.QuestManager;
import fidas.server.gameserver.network.serverpackets.UserInfo;

/*********************************
 * Custom Npc 'Noblesse Manager' *
 ********************************/
 
public class NoblesseManager extends Quest
{
	// Npc
	private static final int NpcId = 49296; // Custom Npc
	// Item 
	private static final int ItemId = 6673; // Festival Adena
	private static final int NOBLESS_TIARA = 7694;
	// Level check
	private static final int LEVEL = 75;
  
  	public NoblesseManager(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(NpcId);
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
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
		return "49296.htm";           
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName()); 
    
		if (event.equalsIgnoreCase("noblesse"))
		{    
			if (st.getQuestItemsCount(ItemId) >= 10 && player.getLevel() >= LEVEL)  // 10x Festival Adena  and "LEVEL" check
			{
				st.takeItems(ItemId,10);
				player.setNoble(!player.isNoble());
				st.giveItems(NOBLESS_TIARA,1);       
				player.sendPacket(new UserInfo(player));
				player.sendMessage("Congratulations! You are now Noblesse!");
				return null;            
			}
			return "49296-no.htm";
		}
		else if (event.equalsIgnoreCase("49296-1.htm"))    
		{
			if (player.isNoble())
			{
				return "49296-already.htm";
			}
		}
		return htmltext;
	}
    
	public static void main(String[] args)
	{
		new NoblesseManager(-1, "NoblesseManager", "custom");
		_log.info("NoblesseManager: Loaded successfully.");    
	}
}      