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
package quests.Q00288_HandleWithCare;

import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.quest.State;
import fidas.server.util.Rnd;

public class Q00288_HandleWithCare extends Quest
{
	// NPCs
	private static final int _ankumi = 32741;
	// Items
	private static final int _mid_scale = 15498;
	private static final int _high_scale = 15497;
	
	private static final int[][] _rewards =
	{
		{
			959,
			1
		}, // Scroll: Enchant Weapon (S)
		{
			960,
			3
		}, // Scroll: Enchant Armor (S)
		{
			9557,
			1
		}
	// Divine Attribute Crystal
	};
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getNpcId() == _ankumi)
		{
			if (event.equalsIgnoreCase("32741-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
			}
			else if (event.equalsIgnoreCase("32741-07.htm"))
			{
				if (st.hasQuestItems(_mid_scale))
				{
					st.takeItems(_mid_scale, 1);
					int[] _rnd = _rewards[Rnd.get(0, _rewards.length - 1)];
					st.giveItems(_rnd[0], Rnd.get(1, _rnd[1]));
				}
				else if (st.hasQuestItems(_high_scale))
				{
					st.takeItems(_high_scale, 1);
					int[] _rnd = _rewards[Rnd.get(0, _rewards.length - 2)];
					st.giveItems(_rnd[0], Rnd.get(1, _rnd[1]));
					st.giveItems(_rewards[2][0], 1);
				}
				st.unset("cond");
				st.unset("drop");
				st.exitQuest(true);
			}
		}
		return htmltext;
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
		
		if (npc.getNpcId() == _ankumi)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 82)
					{
						htmltext = "32741-01.htm";
					}
					else
					{
						htmltext = "32741-00.htm";
					}
					break;
				case State.STARTED:
					if ((st.getInt("cond") == 2) && st.hasQuestItems(_mid_scale))
					{
						htmltext = "32741-05.htm";
					}
					else if ((st.getInt("cond") == 3) && st.hasQuestItems(_high_scale))
					{
						htmltext = "32741-06.htm";
					}
					else
					{
						htmltext = "32741-04.htm";
					}
					break;
			}
		}
		return htmltext;
	}
	
	public Q00288_HandleWithCare(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_ankumi);
		addTalkId(_ankumi);
	}
	
	public static void main(String[] args)
	{
		new Q00288_HandleWithCare(288, Q00288_HandleWithCare.class.getSimpleName(), "Handle With Care");
	}
}
