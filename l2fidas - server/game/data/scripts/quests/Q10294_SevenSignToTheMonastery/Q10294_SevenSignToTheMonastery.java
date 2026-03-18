package quests.Q10294_SevenSignToTheMonastery;

import quests.Q10293_SevenSignForbiddenBook.Q10293_SevenSignForbiddenBook;

import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.quest.State;
import fidas.server.gameserver.network.serverpackets.OnEventTrigger;
import fidas.server.gameserver.network.serverpackets.SocialAction;
import fidas.server.gameserver.util.Util;
import fidas.server.util.Rnd;

public final class Q10294_SevenSignToTheMonastery extends Quest
{
	private static final int Elcadia = 32784;
	private static final int Elcadia_Support = 32785;
	private static final int ErissEvilThoughts = 32792;
	private static final int RelicGuardian = 32803;
	private static final int EtisVanEtina = 32808;
	private static final int JudeVanEtinasEvilThoughts = 32888;
	private static final int SolinasEvilThoughts = 32793;
	private static final int WestRelicWatcher = 32804;
	private static final int NorthRelicWatcher = 32805;
	private static final int EastRelicWatcher = 32806;
	private static final int SouthRelicWatcher = 32807;
	private static final int WestTeleportControlDevice = 32816;
	private static final int NorthTeleportControlDevice = 32817;
	private static final int EastTeleportControlDevice = 32818;
	private static final int SouthTeleportControlDevice = 32819;
	private static final int WestReadingDesk1 = 32821;
	private static final int WestReadingDesk2 = 32822;
	private static final int WestReadingDesk3 = 32823;
	private static final int WestReadingDesk4 = 32824;
	private static final int NorthReadingDesk1 = 32825;
	private static final int NorthReadingDesk2 = 32826;
	private static final int NorthReadingDesk3 = 32827;
	private static final int NorthReadingDesk4 = 32828;
	private static final int EastReadingDesk1 = 32829;
	private static final int EastReadingDesk2 = 32830;
	private static final int EastReadingDesk3 = 32831;
	private static final int EastReadingDesk4 = 32832;
	private static final int SouthReadingDesk1 = 32833;
	private static final int SouthReadingDesk2 = 32834;
	private static final int SouthReadingDesk3 = 32835;
	private static final int SouthReadingDesk4 = 32836;
	private static final int[] emptydesks =
	{
		WestReadingDesk2,
		WestReadingDesk3,
		WestReadingDesk4,
		NorthReadingDesk2,
		NorthReadingDesk3,
		NorthReadingDesk1,
		EastReadingDesk2,
		EastReadingDesk1,
		EastReadingDesk4,
		SouthReadingDesk1,
		SouthReadingDesk3,
		SouthReadingDesk4
	};
	
	private static final int[] desks =
	{
		WestReadingDesk1,
		NorthReadingDesk4,
		EastReadingDesk3,
		SouthReadingDesk2
	};
	
	private static int[] TALK =
	{
		Elcadia,
		ErissEvilThoughts,
		Elcadia_Support,
		RelicGuardian,
		WestRelicWatcher,
		NorthRelicWatcher,
		EastRelicWatcher,
		SouthRelicWatcher,
		WestTeleportControlDevice,
		NorthTeleportControlDevice,
		EastTeleportControlDevice,
		SouthTeleportControlDevice,
		EtisVanEtina,
		JudeVanEtinasEvilThoughts,
		SolinasEvilThoughts
	};
	
	private boolean isAllBooksFinded(QuestState st)
	{
		return (st.getInt("book_32821") + st.getInt("book_32828") + st.getInt("book_32831") + st.getInt("book_32834")) >= 4;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		int green = st.getInt("green");
		int blue = st.getInt("blue");
		int red = st.getInt("red");
		if ("32784-03.html".equals(event))
		{
			st.set("cond", "1");
			st.set("green", "0");
			st.set("blue", "0");
			st.set("red", "0");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if ("32792-03.html".equals(event))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if ("32804-04.html".equals(event))
		{
			if (st.getInt("book_32821") > 0)
			{
				htmltext = "32804-05.html";
			}
		}
		else if ("32805-04.html".equals(event))
		{
			if (st.getInt("book_32828") > 0)
			{
				htmltext = "32805-05.html";
			}
		}
		else if ("32806-04.html".equals(event))
		{
			if (st.getInt("book_32831") > 0)
			{
				htmltext = "32806-05.html";
			}
		}
		else if ("32807-04.html".equals(event))
		{
			if (st.getInt("book_32834") > 0)
			{
				htmltext = "32807-05.html";
			}
		}
		else if ("32785-05.html".equals(event))
		{
			st.playSound("ItemSound.quest_middle");
			st.set("book_" + npc.getNpcId(), "1");
			npc.setDisplayEffect(1);
			npc.setTarget(npc);
			npc.doCast(SkillTable.getInstance().getInfo(9077, 1));
			if (green == 0)
			{
				if (npc.getNpcId() == EastReadingDesk3)
				{
					st.set("green", "1");
					addSpawn(JudeVanEtinasEvilThoughts, 87704, -249496, -8320, 49152, false, 0L, false, player.getInstanceId());
					addSpawn(27407, player.getX() + Rnd.get(300), player.getY() + Rnd.get(300), -8320, 0, false, 0L, false, player.getInstanceId());
					addSpawn(27407, player.getX() + Rnd.get(300), player.getY() + Rnd.get(300), -8320, 49152, false, 0L, false, player.getInstanceId());
					addSpawn(27407, player.getX() + Rnd.get(300), player.getY() + Rnd.get(300), -8320, 49152, false, 0L, false, player.getInstanceId());
					return null;
				}
			}
			if (blue == 0)
			{
				if (npc.getNpcId() == SouthReadingDesk2)
				{
					st.set("blue", "1");
					addSpawn(SolinasEvilThoughts, 86680, -246728, -8320, 0, false, 0L, false, player.getInstanceId());
					return null;
				}
			}
			if (red == 0)
			{
				if (npc.getNpcId() == NorthReadingDesk4)
				{
					st.set("red", "1");
					addSpawn(JudeVanEtinasEvilThoughts, 84840, -252392, -8320, 49152, false, 0L, false, player.getInstanceId());
					return null;
				}
			}
			if (isAllBooksFinded(st))
			{
				player.sendPacket(new OnEventTrigger(22100500, false));
				player.sendPacket(new OnEventTrigger(22100502, true));
				st.startQuestTimer("CloseMovie", 4000L);
				return null;
			}
		}
		else if ("32792-08.html".equals(event))
		{
			if (player.isSubClassActive())
			{
				htmltext = "32792-09.html";
			}
			else
			{
				st.addExpAndSp(25000000, 2500000);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				return null;
			}
		}
		else
		{
			if ("OpenMovie".equals(event))
			{
				player.showQuestMovie(24);
				player.sendPacket(new OnEventTrigger(22100500, true));
				player.sendPacket(new OnEventTrigger(21100100, true));
				player.sendPacket(new OnEventTrigger(21100202, true));
				player.sendPacket(new OnEventTrigger(21100200, true));
				player.sendPacket(new OnEventTrigger(21100204, true));
				player.sendPacket(new OnEventTrigger(21100206, true));
				return null;
			}
			if ("CloseMovie".equals(event))
			{
				player.showQuestMovie(25);
				return null;
			}
			if ("NotReady".equals(event))
			{
				return null;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		switch (st.getState())
		{
			case 0:
				QuestState ForbiddenBook = player.getQuestState(Q10293_SevenSignForbiddenBook.class.getSimpleName());
				if ((player.getLevel() >= 81) && (ForbiddenBook.isCompleted()))
				{
					htmltext = "32784-01.htm";
				}
				else
				{
					htmltext = "32784-00.htm";
					st.exitQuest(true);
				}
				break;
			case 1:
				if (npcId == Elcadia)
				{
					if (cond != 1)
					{
						break;
					}
					htmltext = "32784-04.html";
				}
				else if (npcId == Elcadia_Support)
				{
					if (cond == 1)
					{
						htmltext = "32785-01.html";
					}
					else if (cond == 2)
					{
						htmltext = "32785-02.html";
					}
					else
					{
						if (cond != 3)
						{
							break;
						}
						htmltext = "32785-03.html";
					}
				}
				else if (npcId == ErissEvilThoughts)
				{
					if (cond == 1)
					{
						htmltext = "32792-01.html";
					}
					else if (cond == 2)
					{
						htmltext = "32792-06.html";
					}
					else
					{
						if (cond != 3)
						{
							break;
						}
						htmltext = "32792-07.html";
					}
				}
				else if (npcId == RelicGuardian)
				{
					if (cond == 2)
					{
						if (isAllBooksFinded(st))
						{
							st.set("cond", "3");
							htmltext = "32803-04.html";
							st.playSound("ItemSound.quest_middle");
						}
						else
						{
							htmltext = "32803-01.html";
						}
					}
					else
					{
						if (cond != 3)
						{
							break;
						}
						htmltext = "32803-05.html";
					}
				}
				else if (npcId == WestRelicWatcher)
				{
					htmltext = "32804-01.html";
				}
				else if (npcId == NorthRelicWatcher)
				{
					htmltext = "32805-01.html";
				}
				else if (npcId == EastRelicWatcher)
				{
					htmltext = "32806-01.html";
				}
				else if (npcId == SouthRelicWatcher)
				{
					htmltext = "32807-01.html";
				}
				else if (Util.contains(emptydesks, npcId))
				{
					htmltext = "empty_desk.html";
				}
				else if (Util.contains(desks, npcId))
				{
					htmltext = npcId + "-01.html";
				}
				else if (npcId == EtisVanEtina)
				{
					htmltext = "32808-01.htm";
				}
				else if (npcId == JudeVanEtinasEvilThoughts)
				{
					htmltext = "32888-01.htm";
				}
				else
				{
					if (npcId != SolinasEvilThoughts)
					{
						break;
					}
					htmltext = "32793-01.htm";
				}
				break;
			case 2:
				htmltext = getNoQuestMsg(player);
		}
		
		return htmltext;
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		if ((st.getState() == 1) && (st.getState() != 2))
		{
			if (st.getInt("cond") > 1)
			{
				if (npcId == WestTeleportControlDevice)
				{
					htmltext = "32816-01.html";
				}
				else if (npcId == NorthTeleportControlDevice)
				{
					htmltext = "32817-01.html";
				}
				else if (npcId == EastTeleportControlDevice)
				{
					htmltext = "32818-01.html";
				}
				else if (npcId == SouthTeleportControlDevice)
				{
					htmltext = "32819-01.html";
				}
			}
		}
		return htmltext;
	}
	
	public Q10294_SevenSignToTheMonastery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Elcadia);
		addFirstTalkId(new int[]
		{
			WestTeleportControlDevice,
			NorthTeleportControlDevice,
			EastTeleportControlDevice,
			SouthTeleportControlDevice
		});
		for (int NPC : TALK)
		{
			addTalkId(NPC);
		}
		addTalkId(desks);
		addTalkId(emptydesks);
	}
	
	public static void main(String[] args)
	{
		new Q10294_SevenSignToTheMonastery(10294, Q10294_SevenSignToTheMonastery.class.getSimpleName(), "Seven Signs, To the Monastery of Silence");
	}
}