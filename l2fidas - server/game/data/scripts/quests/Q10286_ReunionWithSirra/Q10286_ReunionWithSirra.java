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
package quests.Q10286_ReunionWithSirra;

import quests.Q10285_MeetingSirra.Q10285_MeetingSirra;

import fidas.server.gameserver.instancemanager.InstanceManager;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Instance;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.quest.State;

public class Q10286_ReunionWithSirra extends Quest
{
	// NPC's
	private static final int RAFFORTY = 32020;
	private static final int STEWARD = 32029;
	private static final int JINIA = 32760;
	private static final int SIRRA = 32762;
	private static final int JINIA2 = 32781;
	
	private static final int _blackCore = 15470;
	
	// Freya Sync
	private static int freyaStand = 29179;
	private static int freyaStand_hard = 29180;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getNpcId() == RAFFORTY)
		{
			if (event.equalsIgnoreCase("32020-04.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.set("progress", "1");
				st.playSound("ItemSound.quest_accept");
			}
			
			else if (event.equalsIgnoreCase("32020-05.htm") && (st.getInt("progress") == 1))
			{
				st.set("Ex", "0");
			}
		}
		
		else if (npc.getNpcId() == JINIA)
		{
			if (event.equalsIgnoreCase("32760-06.htm"))
			{
				addSpawn(SIRRA, -23905, -8790, -5384, 56238, false, 0, false, npc.getInstanceId());
				st.set("Ex", "1");
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
			
			else if (event.equalsIgnoreCase("32760-09.htm") && (st.getInt("progress") == 1) && (st.getInt("Ex") == 2))
			{
				st.set("progress", "2");
				// destroy instance after 1 min
				Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				inst.setDuration(60000);
				inst.setEmptyDestroyTime(0);
			}
		}
		
		else if (npc.getNpcId() == SIRRA)
		{
			if (event.equalsIgnoreCase("32762-04.htm") && (st.getInt("progress") == 1) && (st.getInt("Ex") == 1))
			{
				if (st.getQuestItemsCount(_blackCore) == 0)
				{
					st.giveItems(_blackCore, 5);
				}
				
				st.set("Ex", "2");
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if ((npc.getNpcId() == STEWARD) && event.equalsIgnoreCase("go") && (player.getLevel() >= 82))
		{
			player.teleToLocation(103045, -124361, -2768); // 10286 Quest
			htmltext = "";
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		QuestState _prev = player.getQuestState(Q10285_MeetingSirra.class.getSimpleName());
		
		if ((npc.getNpcId() == RAFFORTY) && (_prev != null) && (_prev.getState() == State.COMPLETED) && (st == null) && (player.getLevel() >= 82))
		{
			return "32020-00.htm";
		}
		npc.showChatWindow(player);
		
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getNpcId() == RAFFORTY)
		{
			switch (st.getState())
			{
				case State.CREATED:
					QuestState _prev = player.getQuestState(Q10285_MeetingSirra.class.getSimpleName());
					if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 82))
					{
						htmltext = "32020-01.htm";
					}
					else
					{
						htmltext = "32020-03.htm";
					}
					break;
				case State.STARTED:
					if (st.getInt("progress") == 1)
					{
						htmltext = "32020-06.htm";
					}
					else if (st.getInt("progress") == 2)
					{
						htmltext = "32020-09.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		}
		
		else if ((npc.getNpcId() == JINIA) && (st.getInt("progress") == 1))
		{
			switch (st.getInt("Ex"))
			{
				case 0:
					return "32760-01.htm";
				case 1:
					return "32760-07.htm";
				case 2:
					return "32760-08.htm";
			}
		}
		
		else if ((npc.getNpcId() == SIRRA) && (st.getInt("progress") == 1))
		{
			switch (st.getInt("Ex"))
			{
				case 1:
					return "32762-01.htm";
				case 2:
					return "32762-05.htm";
			}
		}
		
		else if ((npc.getNpcId() == JINIA2) && (st.getInt("progress") == 2))
		{
			htmltext = "32781-01.htm";
		}
		
		else if ((npc.getNpcId() == JINIA2) && (st.getInt("progress") == 3))
		{
			st.addExpAndSp(2152200, 181070);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
			htmltext = "32781-08.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		{
			if ((npc.getNpcId() == freyaStand_hard) && (npc.getNpcId() == freyaStand))
			{
				st.setState(State.COMPLETED);
				st.playSound("ItemSound.quest_finish");
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	public Q10286_ReunionWithSirra(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY);
		addTalkId(JINIA);
		addTalkId(JINIA2);
		addTalkId(SIRRA);
		addKillId(freyaStand_hard, freyaStand);
		addStartNpc(STEWARD);
		addTalkId(STEWARD);
	}
	
	public static void main(String[] args)
	{
		new Q10286_ReunionWithSirra(10286, Q10286_ReunionWithSirra.class.getSimpleName(), "Reunion With Sirra");
	}
}