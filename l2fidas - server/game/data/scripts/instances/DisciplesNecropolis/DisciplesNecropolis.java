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
package instances.DisciplesNecropolis;

import quests.Q196_SevenSignsSealOfTheEmperor.Q196_SevenSignsSealOfTheEmperor;
import javolution.util.FastList;

import fidas.server.gameserver.GeoData;
import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.instancemanager.InstanceManager;
import fidas.server.gameserver.model.actor.L2Attackable;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2DoorInstance;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Instance;
import fidas.server.gameserver.model.instancezone.InstanceWorld;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.clientpackets.Say2;
import fidas.server.gameserver.network.serverpackets.CreatureSay;
import fidas.server.gameserver.network.serverpackets.NpcSay;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.gameserver.util.Util;
import fidas.server.util.Rnd;

/**
 ** @author Gnacik
 **
 ** 2010-12-10 Based on official server Naia
 */
public class DisciplesNecropolis extends Quest
{
	private static final String qn = "DisciplesNecropolis";
	// Values
	private static final int INSTANCE_ID = 112;
	// NPC's
	private static final int _promise = 32585;
	private static final int _leon = 32587;
	private static final int _finalgk = 32657;
	private static final int _device = 27384;
	
	private static final int[] _on_kill = { 27371, 27372, 27373, 27374, 27375, 27376, 27377, 27378, 27379 };
	// Itms
	private static final int _sword = 15310;
	private static final int _seal_of_binding = 13846;
	// Strings
	private static final String[] _txt_anakim = {
		"For the eternity of Einhasad!!!",
		"Dear Shillien's offspring! You are not capable of confronting us!",
		"I'll show you the real power of Einhasad!",
		"Dear Military Force of Light! Go destroy the offspring of Shillien!!!"
	};
	private static final String[] _txt_lilith = {
		"You, such a fool! The victory over this war belongs to Shilien!!!",
		"Anakim! In the name of Great Shilien, I will cut your throat!",
		"How dare you try to contend against me in strength? Ridiculous.",
		"You cannot be the match of Lilith. I'll teach you a lesson!"
	};
	private static final String[] _priv_anakim = {
		"My power's weakening.. Hurry and turn on the sealing device!!!",
		"All 4 sealing devices must be turned on!!!",
		"Lilith's attack is getting stronger! Go ahead and turn it on!",
		"Dear %s, give me more strength.",
	};
	
	// Other
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	private static final int[][] TELEPORTS = {
		{ -89553, 216087, -7488 },
		{ 171854, -17476, -4896 }
	};
	// Doors
	private static final int DOOR_1 = 17240102;
	private static final int DOOR_2 = 17240104;
	private static final int DOOR_3 = 17240106;
	private static final int DOOR_4 = 17240108;
	private static final int DOOR_5 = 17240110;
	private static final int DOOR_LAST = 17240111;
	
	private static final int[][] _room_1_spawns =  {
		{ 27371, -89629, 217918, -7490, 1722 },
		{ 27372, -89442, 217915, -7493, 47429 },
		{ 27373, -89067, 217917, -7490, 32333 },
		{ 27374, -88839, 217920, -7490, 31924 }
	};
	private static final int[][] _room_2_spawns = {
		{ 27371, -88599, 220762, -7490, 48815 },
		{ 27373, -88605, 220581, -7490, 34065 },
		{ 27374, -88848, 220389, -7492, 14325 },
		{ 27372, -88594, 220295, -7490, 14903 },
		{ 27373, -88603, 220095, -7490, 15086 },
		{ 27371, -88597, 219798, -7490, 16126 }
	};
	private static final int[][] _room_3_spawns = {
		{ 27373, -86945, 220374, -7492, 31385 },
		{ 27373, -86813, 220636, -7490, 32663 },
		{ 27374, -86613, 220634, -7490, 32263 },
		{ 27374, -87060, 220498, -7492, 32767 },
		{ 27371, -87317, 220628, -7490, 45514 },
		{ 27372, -87169, 220637, -7490, 32708 },
		{ 27371, -87745, 220638, -7493, 65287 },
		{ 27372, -87721, 220786, -7492, 58176 }
	};
	private static final int[][] _room_4_spawns = {
		{ 27373, -84991, 219061, -7492, 16756 },
		{ 27378, -85333, 219246, -7490, 737 },
		{ 27371, -85060, 219232, -7493, 8191 },
		{ 27374, -85155, 219436, -7492, 64692 },
		{ 27379, -85222, 219269, -7490, 4482 },
		{ 27372, -84858, 219316, -7490, 17459 },
		{ 27374, -85280, 219066, -7492, 4934 },
		{ 27377, -85547, 219301, -7490, 65359 },
		{ 27373, -85635, 219137, -7490, 1369 },
		{ 27375, -85557, 219108, -7492, 65238 }
	};
	private static final int[][] _room_5_spawns = {
		{ 27379, -87437, 216648, -7490, 15955 },
		{ 27377, -87544, 216970, -7495, 11485 },
		{ 27375, -87286, 217005, -7495, 8578 },
		{ 27378, -87459, 217163, -7490, 9569 },
		{ 27377, -87347, 217384, -7490, 2091 },
		{ 27375, -87534, 217346, -7490, 64433 },
		{ 27379, -87701, 217469, -7495, 1835 },
		{ 27372, -87457, 217731, -7490, 48371 },
		{ 27378, -87445, 217486, -7490, 51622 },
		{ 27371, -87651, 217697, -7492, 54262 },
		{ 27373, -87563, 217952, -7492, 50734 },
		{ 27374, -87276, 217878, -7492, 46304 }
	};
	private static final int[][] _devices_spawns = {
		{ 27384, -83177, 216137, -7520, 32768 },
		{ 27384, -82588, 216754, -7520, 32768 },
		{ 27384, -83177, 217353, -7520, 32768 },
		{ 27384, -83804, 216754, -7520, 32768 }
	};
	private static final int[][] _anakim_spawns = {
		{ 32718, -83179, 216479, -7504, 16384 },
		{ 32719, -83321, 216507, -7492, 16166 },
		{ 32720, -83086, 216519, -7495, 15910 },
		{ 32721, -83031, 216604, -7492, 17071 }
	};
	private static final int[][] _lilith_spawns = {
		{ 32715, -83175, 217021, -7504, 49151 },
		{ 32717, -83003, 216909, -7492, 48274 },
		{ 32716, -83327, 216938, -7492, 50768 }
	};
	private static final int[][] _skills = {
		{ 32715, 6187 },	// Lilith
		{ 32716, 6188 },	// Lilith's Steward
		{ 32717, 6190 },	// Lilith's Elite
		{ 32718, 6191 },	// Anakim
		{ 32719, 6192 },	// Anakim's Guardian
		{ 32720, 6194 },	// Anakim's Guard
		{ 32721, 6195 }		// Anakim's Executor
	};
	
	private class DiscipleWorld extends InstanceWorld
	{
		public FastList<L2Npc> room_1 = null;
		public FastList<L2Npc> room_2 = null;
		public FastList<L2Npc> room_3 = null;
		public FastList<L2Npc> room_4 = null;
		public FastList<L2Npc> room_5 = null;
		public boolean fight;
		
		public FastList<L2Npc> anakim_group = null;
		public FastList<L2Npc> lilith_group = null;
		public FastList<L2Npc> devices = null;
		
		public DiscipleWorld()
		{
			room_1 = new FastList<>();
			room_2 = new FastList<>();
			room_3 = new FastList<>();
			room_4 = new FastList<>();
			room_5 = new FastList<>();
			fight = false;
			anakim_group = new FastList<>();
			lilith_group = new FastList<>();
			devices = new FastList<>();
		}
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
	}
	
	protected void spawnNPC(DiscipleWorld world)
	{
		for(int[] spawn : _room_1_spawns)
		{
			L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
			world.room_1.add(npc);
		}
		for(int[] spawn : _room_2_spawns)
		{
			L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
			world.room_2.add(npc);
		}
		for(int[] spawn : _room_3_spawns)
		{
			L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
			world.room_3.add(npc);
		}
		for(int[] spawn : _room_4_spawns)
		{
			L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
			world.room_4.add(npc);
		}
		for(int[] spawn : _room_5_spawns)
		{
			L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
			world.room_5.add(npc);
		}
		for(int[] spawn : _lilith_spawns)
		{
			L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
			npc.isShowName();
			world.lilith_group.add(npc);
		}
		for(int[] spawn : _devices_spawns)
		{
			L2Npc npc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
			world.devices.add(npc);
		}
	}
	
	protected void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof DiscipleWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			if (inst != null)
			{
				teleportPlayer(player, TELEPORTS[ENTER], world.getInstanceId());
			}
			return;
		}
		final int instanceId = InstanceManager.getInstance().createDynamicInstance("DisciplesNecropolis.xml");

		world = new DiscipleWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(INSTANCE_ID);
		world.setStatus(0);
		InstanceManager.getInstance().addWorld(world);
		spawnNPC((DiscipleWorld)world);
		
		world.addAllowed(player.getObjectId());
		teleportPlayer(player, TELEPORTS[ENTER], instanceId);

		_log.info("DisciplesNecropolis instance started: " + instanceId + " created by player: " + player.getName());
		return;
	}

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (!(tmpworld instanceof DiscipleWorld))
			return "";

		DiscipleWorld world = (DiscipleWorld) tmpworld;

		if (event.equalsIgnoreCase("teleport_player"))
		{
			world.fight = false;

			if (player != null)
				teleportPlayer(player, TELEPORTS[ENTER], world.getInstanceId());
			
			for (L2Npc np : world.anakim_group)
			{
				if(np != null)
					np.deleteMe();
			}
			for (L2Npc np : world.lilith_group)
			{
				if(np != null)
					np.deleteMe();
			}
			for (L2Npc np : world.devices)
			{
				if(np != null)
					np.deleteMe();
			}
		}
		else if (event.equalsIgnoreCase("start_fight"))
		{
			world.fight = true;
			openDoor(world, DOOR_LAST);
			for(int[] spawn : _anakim_spawns)
			{
				L2Npc enpc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.getInstanceId());
				enpc.isShowName();
				enpc.sendInfo(player);
				world.anakim_group.add(enpc);
			}
			startQuestTimer("fighting", 1000, null, player);
		}
		else if (event.equalsIgnoreCase("fighting"))
		{
			if(world.fight)
			{
				for(L2Npc caster : world.anakim_group)
				{
					if(caster != null && !caster.isCastingNow() && Rnd.get(3) == 0)
						makeCast(caster, world.lilith_group);
					if(caster.getNpcId() == 32718 && Rnd.get(20) == 0)
						player.sendPacket(new NpcSay(caster.getObjectId(), Say2.SHOUT, caster.getNpcId(), _txt_anakim[Rnd.get(0, _txt_anakim.length-1)]));					
					if(caster.getNpcId() == 32718 && Rnd.get(20) == 0)
						player.sendPacket(new NpcSay(caster.getObjectId(), Say2.TELL, caster.getNpcId(), _priv_anakim[Rnd.get(0, _priv_anakim.length-1)].replaceAll("%", player.getName())));					
				}
				for(L2Npc caster : world.lilith_group)
				{
					if(caster != null && !caster.isCastingNow() && Rnd.get(3) == 0)
						makeCast(caster, world.anakim_group);
					if(caster.getNpcId() == 32715 && Rnd.get(20) == 0)
						player.sendPacket(new NpcSay(caster.getObjectId(), Say2.SHOUT, caster.getNpcId(), _txt_lilith[Rnd.get(0, _txt_lilith.length-1)]));
				}
				for(L2Npc caster : world.devices)
				{
					if(caster != null && !caster.isCastingNow() && Rnd.get(3) == 0)
						caster.doCast(SkillTable.getInstance().getInfo(5980, Rnd.get(1,4)));
				}			
				startQuestTimer("fighting", 500, null, player);
			}
		}
		return null;
	}
	
	private void makeCast(L2Npc npc, FastList<L2Npc> targets)
	{
		L2Npc rnd_target = targets.get(Rnd.get(0, targets.size()-1));
		npc.setTarget(rnd_target);
		for(int[] skill : _skills)
		{
			if (skill[0] == npc.getNpcId())
			{
				L2Skill skill_to_cast = SkillTable.getInstance().getInfo(skill[1], 1);
				npc.doCast(skill_to_cast);
				return;
			}
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);

		int npcId = npc.getNpcId();
	
		if (npcId == _promise)
		{
			if (player.isGM())
			{
				enterInstance(player);
				return null;
			}
			
			QuestState qst = player.getQuestState(Q196_SevenSignsSealOfTheEmperor.class.getSimpleName());
			if(qst == null)
				return null;
			
			if(player.getLevel() < 79)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(player);
				player.sendPacket(sm);
				return null;
			}
			else if (qst.getInt("cond") == 3 || qst.getInt("cond") == 4)
			{
				enterInstance(player);
				return null;
			}
		}
		else if (npcId == _leon)
		{
			st.takeItems(_sword, -1);
			teleportPlayer(player,TELEPORTS[EXIT],0);
			return "exit.htm";
		}
		else if (npcId == _finalgk)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
			if (!(tmpworld instanceof DiscipleWorld))
				return null;
			DiscipleWorld world = (DiscipleWorld) tmpworld;
			if (!world.fight)
			{
				startQuestTimer("start_fight", 30000, npc, player);
				player.showQuestMovie(12);				
			}
			return null;
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (!(tmpworld instanceof DiscipleWorld))
			return "";

		DiscipleWorld world = (DiscipleWorld) tmpworld;

		checkDoors(npc, world);
	
		if (npc.getNpcId() == _device)
		{
			if (world.devices.contains(npc))
				world.devices.remove(npc);
			
			if (world.devices.isEmpty())
			{
				player.sendPacket(SystemMessage.getSystemMessage(3060));
				startQuestTimer("teleport_player", 25000, npc, player);
				player.showQuestMovie(13);
			}
			
			L2Npc newnpc = addSpawn(_device, npc, true);
			newnpc.setRHandId(15281);
			newnpc.isShowName();
			newnpc.sendInfo(player);
			st.giveItems(_seal_of_binding, 1);
		}
		String txt = null;
		switch(npc.getNpcId())
		{
			case 27372:
			case 27378:
				txt = "Lord Shilen... some day... you will accomplish... this mission...";
				break;
			case 27373:
			case 27376:
			case 27379:
				txt = "Why are you getting in our way?";
				break;
			case 27377:
				txt = "For Shilen!";
				break;			
		}
		if(txt != null)
			player.sendPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), txt));
		
		return super.onKill(npc, player, isPet);
	}
	
	private synchronized void checkDoors(L2Npc npc, DiscipleWorld world)
	{
		// Remove NPC's
		if (world.room_1.contains(npc))
			world.room_1.remove(npc);
		if (world.room_2.contains(npc))
			world.room_2.remove(npc);
		if (world.room_3.contains(npc))
			world.room_3.remove(npc);
		if (world.room_4.contains(npc))
			world.room_4.remove(npc);
		if (world.room_5.contains(npc))
			world.room_5.remove(npc);
		
		if (world.room_1.isEmpty() || world.room_1.size() == 0)
			openDoor(world, DOOR_1);
		if (world.room_2.isEmpty()  || world.room_2.size() == 0)
			openDoor(world, DOOR_2);
		if (world.room_3.isEmpty()  || world.room_3.size() == 0)
			openDoor(world, DOOR_3);
		if (world.room_4.isEmpty()  || world.room_4.size() == 0)
			openDoor(world, DOOR_4);
		if (world.room_5.isEmpty()  || world.room_5.size() == 0)
			openDoor(world, DOOR_5);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (Util.contains(_on_kill, npc.getNpcId()))
		{
			if (GeoData.getInstance().canSeeTarget(npc, player))
			{
				((L2Attackable) npc).addDamageHate(player, 0, 999);
				
				npc.setTarget(player);
				npc.setRunning();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			String txt = null;
			switch(npc.getNpcId())
			{
				case 27379:
				case 27371: txt = "This place once belonged to Lord Shilen"; break;
				case 27372: txt = "Who dares enter this place?"; break;
				case 27373: txt = "Those who are afraid should get away and those who are brave should fight!"; break;
				case 27377: txt = "Leave now!";
			}
			if(txt != null)
				player.sendPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), txt));
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	private void openDoor(DiscipleWorld world, int number)
	{
		L2DoorInstance door = InstanceManager.getInstance().getInstance(world.getInstanceId()).getDoor(number);
		if (door != null)
			door.openMe();
	}
	
	@Override
	public String onEnterWorld(L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		st.takeItems(_sword, -1);
		return null;
	}
	
	public DisciplesNecropolis(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		setOnEnterWorld(true);
		
		addStartNpc(_promise);
		addTalkId(_promise);
		addTalkId(_leon);
		for(int mob : _on_kill)
		{
			addKillId(mob);
			addAggroRangeEnterId(mob);
		}
		addKillId(_device);
	}

	public static void main(String[] args)
	{
		new DisciplesNecropolis(-1, qn, "instances");
	}
}