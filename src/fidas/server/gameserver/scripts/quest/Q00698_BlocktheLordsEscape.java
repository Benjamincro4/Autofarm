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
package fidas.server.gameserver.scripts.quest;

import fidas.server.Config;
import fidas.server.gameserver.instancemanager.SoIManager;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.quest.State;
import fidas.server.util.Rnd;

public class Q00698_BlocktheLordsEscape extends Quest
{
	private static final int TEPIOS = 32603;
	private static final int VESPER_STONE = 14052;
	
	public Q00698_BlocktheLordsEscape(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32603-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if ((player.getLevel() < 75) || (player.getLevel() > 85))
				{
					htmltext = "32603-00.htm";
					st.exitQuest(true);
				}
				if (SoIManager.getCurrentStage() != 5)
				{
					htmltext = "32603-00a.htm";
					st.exitQuest(true);
				}
				htmltext = "32603-01.htm";
				break;
			case State.STARTED:
				if ((cond == 1) && (st.getInt("defenceDone") == 1))
				{
					htmltext = "32603-05.htm";
					st.giveItems(VESPER_STONE, (int) Config.RATE_QUEST_REWARD * Rnd.get(5, 8));
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(true);
				}
				else
				{
					htmltext = "32603-04.htm";
				}
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q00698_BlocktheLordsEscape(698, Q00698_BlocktheLordsEscape.class.getSimpleName(), "Block the Lords Escape");
	}
}