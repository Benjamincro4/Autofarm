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
package instances.MonasteryGrandLibrary;

import java.util.List;

import javolution.util.FastList;
import javolution.util.FastMap;
import quests.Q10294_SevenSignToTheMonastery.Q10294_SevenSignToTheMonastery;
import quests.Q10295_SevenSignSolinasTomb.Q10295_SevenSignSolinasTomb;
import quests.Q10296_SevenSignPoweroftheSeal.Q10296_SevenSignPoweroftheSeal;

import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.instancemanager.InstanceManager;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Instance;
import fidas.server.gameserver.model.instancezone.InstanceWorld;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.quest.State;
import fidas.server.gameserver.network.NpcStringId;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.NpcSay;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.util.Rnd;

public class MonasteryGrandLibrary extends Quest
{
	private static final String qn = "MonasteryGrandLibrary";
	// Values
	private static final int INSTANCE_ID = 151;
	// NPC's
	private static final int OddGlobe = 32815;
	private static final int Elcadia_Support = 32785;
	private static final int ErissEvilThoughts = 32792;
	private static final int SolinasEvilThoughts = 32793;
	private static final int RelicGuardian = 32803;
	private static final int WestRelicWatcher = 32804;
	private static final int NorthRelicWatcher = 32805;
	private static final int EastRelicWatcher = 32806;
	private static final int SouthRelicWatcher = 32807;
	private static final int EtisVanEtina = 32808;
	private static final int WestTeleportControlDevice = 32816;
	private static final int NorthTeleportControlDevice = 32817;
	private static final int EastTeleportControlDevice = 32818;
	private static final int SouthTeleportControlDevice = 32819;
	private static final int JudeVanEtinasEvilThoughts = 32888;
	private static final int TeleportControlDevice1 = 32837;
	private static final int TeleportControlDevice2 = 32842;
	private static final int TombOfTheSaintess = 32843;
	private static final int AltarOfHallows_Staff = 32857;
	private static final int AltarOfHallows_Sword = 32858;
	private static final int AltarOfHallows_Scroll = 32859;
	private static final int AltarOfHallows_Shield = 32860;
	// Teleport's
	private static final int ENTER = 0;
	private static final int HOLYGRAL = 1;
	private static final int EXIT = 2;
	private static final int TW = 3;
	private static final int TN = 4;
	private static final int TE = 5;
	private static final int TS = 6;
	private static final int RTE = 7;
	private static final int RTG = 8;
	private static final int ENTER1 = 9;
	private static final int MTS = 10;
	private static final int MTS2 = 11;
	private static final int EE = 12;
	private static final int[][] TELEPORTS =
	{
		{
			120664,
			-86968,
			-3392
		}, // Enter
		{
			85937,
			-249618,
			-8320
		}, // HolyBurailOpen
		{
			115944,
			-86952,
			-3392
		}, // ExitInstance
		{
			82434,
			-249546,
			-8320
		}, // TeleWest
		{
			85691,
			-252426,
			-8320
		}, // TeleNorth
		{
			88573,
			-249556,
			-8320
		}, // TeleEast
		{
			85675,
			-246630,
			-8320
		}, // TeleSouth
		{
			120727,
			-86868,
			-3392
		}, // ReturnToEris
		{
			85937,
			-249618,
			-8320
		}, // ReturnToGuardian
		{
			45545,
			-249423,
			-6760
		}, // Enter1
		{
			56033,
			-252944,
			-6760
		}, // MoveToSaintness
		{
			55955,
			-250394,
			-6760
		}, // MoveToSaintness2
		{
			76707,
			-241022,
			-10832
		}
	// EtisEtina
	};
	
	private static final int[] TALK =
	{
		OddGlobe,
		Elcadia_Support,
		WestTeleportControlDevice,
		NorthTeleportControlDevice,
		EastTeleportControlDevice,
		SouthTeleportControlDevice,
		RelicGuardian,
		WestRelicWatcher,
		NorthRelicWatcher,
		EastRelicWatcher,
		SouthRelicWatcher,
		ErissEvilThoughts,
		EtisVanEtina,
		JudeVanEtinasEvilThoughts,
		SolinasEvilThoughts,
		TeleportControlDevice1,
		TeleportControlDevice2,
		TombOfTheSaintess,
		AltarOfHallows_Staff,
		AltarOfHallows_Sword,
		AltarOfHallows_Scroll,
		AltarOfHallows_Shield
	};
	
	private static final int[] MageBuff =
	{
		6725,
		6721,
		6722,
		6717
	};
	private static final int[] FighterBuff =
	{
		6714,
		6715,
		6716,
		6717
	};
	
	private final FastMap<Integer, InstanceHolder> instanceWorlds = new FastMap<>();
	
	public static class InstanceHolder
	{
		List<L2Npc> mobs = new FastList<>();
	}
	
	private class MonasteryGrandLibraryWorld extends InstanceWorld
	{
		public MonasteryGrandLibraryWorld()
		{
		}
	}
	
	private void teleportPlayer(L2Npc npc, L2PcInstance player, int[] coords, int instanceId)
	{
		InstanceHolder holder = instanceWorlds.get(instanceId);
		if ((holder == null) && (instanceId > 0))
		{
			holder = new InstanceHolder();
			instanceWorlds.put(instanceId, holder);
		}
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
		cancelQuestTimer("check_follow", npc, player);
		
		if (holder != null)
		{
			for (L2Npc h : holder.mobs)
			{
				h.deleteMe();
			}
			holder.mobs.clear();
		}
		if (instanceId > 0)
		{
			L2Npc support = addSpawn(Elcadia_Support, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, player.getInstanceId());
			holder.mobs.add(support);
			startQuestTimer("check_follow", 3000, support, player);
		}
	}
	
	protected void enterInstance(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof MonasteryGrandLibraryWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			if (inst != null)
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], world.getInstanceId());
			}
			return;
		}
		final int instanceId = InstanceManager.getInstance().createDynamicInstance("MonasteryGrandLibrary.xml");
		
		world = new MonasteryGrandLibraryWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(INSTANCE_ID);
		world.setStatus(0);
		InstanceManager.getInstance().addWorld(world);
		
		world.addAllowed(player.getObjectId());
		
		teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);
		return;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		QuestState qs = player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName());
		QuestState qs1 = player.getQuestState(Q10295_SevenSignSolinasTomb.class.getSimpleName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		if ("check_follow".equals(event))
		{
			cancelQuestTimer("check_follow", npc, player);
			npc.getAI().stopFollow();
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			if ((qs != null) && (qs.getCond() == 2))
			{
				if (Rnd.chance(10))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.IT_SEEMS_THAT_YOU_CANNOT_REMEMBER_TO_THE_ROOM_OF_THE_WATCHER_WHO_FOUND_THE_BOOK));
				}
				else
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.REMEMBER_THE_CONTENT_OF_THE_BOOKS_THAT_YOU_FOUND_YOU_CANT_TAKE_THEM_OUT_WITH_YOU));
				}
			}
			if ((qs != null) && (qs.getCond() == 3))
			{
				if (Rnd.chance(8))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.YOUR_WORK_HERE_IS_DONE_SO_RETURN_TO_THE_CENTRAL_GUARDIAN));
				}
			}
			if ((qs1 != null) && (qs1.getCond() == 1))
			{
				if (Rnd.chance(5))
				{
					if (Rnd.chance(10))
					{
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.TO_REMOVE_THE_BARRIER_YOU_MUST_FIND_THE_RELICS_THAT_FIT_THE_BARRIER_AND_ACTIVATE_THE_DEVICE));
					}
					else if (Rnd.chance(15))
					{
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.THE_GUARDIAN_OF_THE_SEAL_DOESNT_SEEM_TO_GET_INJURED_AT_ALL_UNTIL_THE_BARRIER_IS_DESTROYED));
					}
					else
					{
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.THE_DEVICE_LOCATED_IN_THE_ROOM_IN_FRONT_OF_THE_GUARDIAN_OF_THE_SEAL_IS_DEFINITELY_THE_BARRIER_THAT_CONTROLS_THE_GUARDIANS_POWER));
					}
				}
			}
			if (player.getCurrentHp() < (player.getMaxHp() * 0.8D))
			{
				npc.setTarget(player);
				npc.doSimultaneousCast(SkillTable.getInstance().getInfo(6724, 1));
			}
			if (player.getCurrentMp() < (player.getMaxMp() * 0.5D))
			{
				npc.setTarget(player);
				npc.doSimultaneousCast(SkillTable.getInstance().getInfo(6728, 1));
			}
			startQuestTimer("check_follow", 20000, npc, player);
			return "";
		}
		else if ("enter".equals(event))
		{
			enterInstance(npc, player);
			return null;
		}
		if (npc.getNpcId() == 32792)
		{
			if ("Enter1".equals(event))
			{
				if (qs1.getInt("seal_removed") != 1)
				{
					qs1.startQuestTimer("StartMovie", 1000);
					teleportPlayer(npc, player, TELEPORTS[ENTER1], player.getInstanceId());
				}
				else
				{
					teleportPlayer(npc, player, TELEPORTS[MTS2], player.getInstanceId());
				}
				return null;
			}
			if ("Enter3".equals(event))
			{
				if ((qs.getInt("book_" + 32821) + qs.getInt("book_" + 32828) + qs.getInt("book_" + 32831) + qs.getInt("book_" + 32834)) != 4)
				{
					qs.startQuestTimer("OpenMovie", 4000);
				}
				teleportPlayer(npc, player, TELEPORTS[HOLYGRAL], player.getInstanceId());
				return null;
			}
			else if ("video".equals(event))
			{
				player.showQuestMovie(29);
				startQuestTimer("teleport", 60000, npc, player);
				cancelQuestTimer("check_follow", npc, player);
				InstanceHolder holder = instanceWorlds.get(player.getInstanceId());
				if (holder != null)
				{
					for (L2Npc h : holder.mobs)
					{
						h.deleteMe();
					}
					holder.mobs.clear();
				}
				return null;
			}
			else if ("teleport".equals(event))
			{
				teleportPlayer(npc, player, TELEPORTS[EE], player.getInstanceId());
				return null;
			}
		}
		else if ("buff".equals(event))
		{
			if (player.isMageClass())
			{
				for (int h : MageBuff)
				{
					npc.setTarget(player);
					npc.doSimultaneousCast(SkillTable.getInstance().getInfo(h, 1));
				}
			}
			else
			{
				for (int h : FighterBuff)
				{
					npc.setTarget(player);
					npc.doSimultaneousCast(SkillTable.getInstance().getInfo(h, 1));
				}
			}
			return null;
		}
		else if ("Exit".equals(event))
		{
			cancelQuestTimer("check_follow", npc, player);
			InstanceHolder holder = instanceWorlds.get(player.getInstanceId());
			if (holder != null)
			{
				for (L2Npc h : holder.mobs)
				{
					h.deleteMe();
				}
				holder.mobs.clear();
			}
			teleportPlayer(npc, player, TELEPORTS[EXIT], 0);
			return null;
		}
		else if ("TeleWest".equals(event))
		{
			teleportPlayer(npc, player, TELEPORTS[TW], player.getInstanceId());
			return null;
		}
		else if (event.equalsIgnoreCase("TeleNorth"))
		{
			teleportPlayer(npc, player, TELEPORTS[TN], player.getInstanceId());
			return null;
		}
		else if ("TeleEast".equals(event))
		{
			teleportPlayer(npc, player, TELEPORTS[TE], player.getInstanceId());
			return null;
		}
		else if ("TeleSouth".equals(event))
		{
			teleportPlayer(npc, player, TELEPORTS[TS], player.getInstanceId());
			return null;
		}
		else if ("ReturnToEris".equals(event))
		{
			teleportPlayer(npc, player, TELEPORTS[RTE], player.getInstanceId());
			return null;
		}
		else if ("ReturnToGuardian".equals(event))
		{
			teleportPlayer(npc, player, TELEPORTS[RTG], player.getInstanceId());
			return null;
		}
		else if ("MoveToSaintess".equals(event))
		{
			teleportPlayer(npc, player, TELEPORTS[MTS], player.getInstanceId());
			return null;
		}
		else if ("MoveToSaintess2".equals(event))
		{
			teleportPlayer(npc, player, TELEPORTS[MTS2], player.getInstanceId());
			return null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (npcId == OddGlobe)
		{
			if ((player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()) != null) && (player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()).getState() == State.STARTED))
			{
				enterInstance(npc, player);
				return null;
			}
			if ((player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()) != null) && (player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()).getState() == State.COMPLETED) && (player.getQuestState(Q10295_SevenSignSolinasTomb.class.getSimpleName()) == null))
			{
				enterInstance(npc, player);
				return null;
			}
			if ((player.getQuestState(Q10295_SevenSignSolinasTomb.class.getSimpleName()) != null) && (player.getQuestState(Q10295_SevenSignSolinasTomb.class.getSimpleName()).getState() != State.COMPLETED))
			{
				enterInstance(npc, player);
				return null;
			}
			if ((player.getQuestState(Q10295_SevenSignSolinasTomb.class.getSimpleName()) != null) && (player.getQuestState(Q10295_SevenSignSolinasTomb.class.getSimpleName()).getState() == State.COMPLETED) && (player.getQuestState(Q10296_SevenSignPoweroftheSeal.class.getSimpleName()) == null))
			{
				enterInstance(npc, player);
				return null;
			}
			if ((player.getQuestState(Q10296_SevenSignPoweroftheSeal.class.getSimpleName()) != null) && (player.getQuestState(Q10296_SevenSignPoweroftheSeal.class.getSimpleName()).getState() != State.COMPLETED))
			{
				enterInstance(npc, player);
				return null;
			}
			htmltext = "<html><body>Odd Globe:<br>The huge mechanism in the form of the globe. Around silent cottons are heard and in air presence of any unknown energy, which which in a way doesn't come under to your influence is felt.</body></html>";
		}
		return htmltext;
	}
	
	public MonasteryGrandLibrary(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(OddGlobe);
		addStartNpc(WestTeleportControlDevice);
		addStartNpc(NorthTeleportControlDevice);
		addStartNpc(EastTeleportControlDevice);
		addStartNpc(SouthTeleportControlDevice);
		for (int NPC : TALK)
		{
			addTalkId(NPC);
		}
	}
	
	public static void main(String[] args)
	{
		new MonasteryGrandLibrary(-1, qn, "instances");
	}
}
