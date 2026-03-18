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
package quests.Q512_BladeUnderFoot;

import java.util.HashMap;
import java.util.Map;

import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.instancemanager.FortManager;
import fidas.server.gameserver.instancemanager.GlobalVariablesManager;
import fidas.server.gameserver.instancemanager.InstanceManager;
import fidas.server.gameserver.model.L2Party;
import fidas.server.gameserver.model.Location;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.L2Playable;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.actor.instance.L2RaidBossInstance;
import fidas.server.gameserver.model.entity.Castle;
import fidas.server.gameserver.model.entity.Fort;
import fidas.server.gameserver.model.entity.Instance;
import fidas.server.gameserver.model.holders.SkillHolder;
import fidas.server.gameserver.model.instancezone.InstanceWorld;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.quest.State;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.gameserver.util.Util;
import fidas.server.util.Rnd;

public final class Q512_BladeUnderFoot extends Quest
{
	private class CAUWorld extends InstanceWorld
	{
	}
	
	public static class CastleDungeon
	{
		private final int INSTANCEID;
		private final int _wardenId;
		
		public CastleDungeon(int iId, int wardenId)
		{
			INSTANCEID = iId;
			_wardenId = wardenId;
		}
		
		public int getInstanceId()
		{
			return INSTANCEID;
		}
		
		public long getReEnterTime()
		{
			String tmp = GlobalVariablesManager.getInstance().getStoredVariable("Castle_dungeon_" + Integer.toString(_wardenId));
			
			return tmp == null ? 0 : Long.parseLong(tmp);
		}
		
		public void setReEnterTime(long time)
		{
			GlobalVariablesManager.getInstance().storeVariable("Castle_dungeon_" + Integer.toString(_wardenId), Long.toString(time));
		}
	}
	
	private static final boolean debug = false;
	private static final long REENTER_INTERVAL = 14400000;
	private static final long RAID_SPAWN_DELAY = 120000;
	
	private final Map<Integer, CastleDungeon> _castleDungeons = new HashMap<>(9);
	
	// QUEST ITEMS
	private static final int DL_MARK = 9798;
	
	// REWARDS
	private static final int KNIGHT_EPALUETTE = 9912;
	
	// MONSTER TO KILL -- Only last 3 Raids (lvl ordered) give DL_MARK
	public static final int[] RAIDS1 =
	{
		25546,
		25549,
		25552
	};
	public static final int[] RAIDS2 =
	{
		25553,
		25554,
		25557,
		25560
	};
	public static final int[] RAIDS3 =
	{
		25563,
		25566,
		25569
	};
	
	private static final SkillHolder RAID_CURSE = new SkillHolder(5456, 1);
	
	private String checkConditions(L2PcInstance player)
	{
		if (debug)
		{
			return null;
		}
		L2Party party = player.getParty();
		if (party == null)
		{
			return "CastleWarden-03.htm";
		}
		if (party.getLeader() != player)
		{
			return getHtm(player.getHtmlPrefix(), "CastleWarden-04.htm").replace("%leader%", party.getLeader().getName());
		}
		for (L2PcInstance partyMember : party.getMembers())
		{
			QuestState st = partyMember.getQuestState(getName());
			if ((st == null) || (st.getInt("cond") < 1))
			{
				return getHtm(player.getHtmlPrefix(), "CastleWarden-05.htm").replace("%player%", partyMember.getName());
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				return getHtm(player.getHtmlPrefix(), "CastleWarden-06.htm").replace("%player%", partyMember.getName());
			}
		}
		return null;
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	protected String enterInstance(L2PcInstance player, String template, int[] coords, CastleDungeon dungeon, String ret)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof CAUWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return "";
			}
			teleportPlayer(player, coords, world.getInstanceId());
			return "";
		}
		// New instance
		if (ret != null)
		{
			return ret;
		}
		ret = checkConditions(player);
		if (ret != null)
		{
			return ret;
		}
		L2Party party = player.getParty();
		int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		Instance ins = InstanceManager.getInstance().getInstance(instanceId);
		ins.setSpawnLoc(new Location(player));
		world = new InstanceWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(dungeon.getInstanceId());
		world.setStatus(0);
		dungeon.setReEnterTime(System.currentTimeMillis() + REENTER_INTERVAL);
		InstanceManager.getInstance().addWorld(world);
		_log.info("Castle BladeUnderFoot started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid((CAUWorld) world), RAID_SPAWN_DELAY);
		
		// teleport players
		if (player.getParty() == null)
		{
			teleportPlayer(player, coords, instanceId);
			world.addAllowed(player.getObjectId());
		}
		else
		{
			for (L2PcInstance partyMember : party.getMembers())
			{
				teleportPlayer(partyMember, coords, instanceId);
				world.addAllowed(partyMember.getObjectId());
				if (partyMember.getQuestState(getName()) == null)
				{
					newQuestState(partyMember);
				}
			}
		}
		return getHtm(player.getHtmlPrefix(), "CastleWarden-08.htm").replace("%clan%", player.getClan().getName());
	}
	
	private class spawnRaid implements Runnable
	{
		private final CAUWorld _world;
		
		public spawnRaid(CAUWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			try
			{
				int spawnId;
				if (_world.getStatus() == 0)
				{
					spawnId = RAIDS1[Rnd.get(RAIDS1.length)];
				}
				else if (_world.getStatus() == 1)
				{
					spawnId = RAIDS2[Rnd.get(RAIDS2.length)];
				}
				else
				{
					spawnId = RAIDS3[Rnd.get(RAIDS3.length)];
				}
				L2Npc raid = addSpawn(spawnId, 53319, 245814, -6576, 0, false, 0, false, _world.getInstanceId());
				if (raid instanceof L2RaidBossInstance)
				{
					((L2RaidBossInstance) raid).setUseRaidCurse(false);
				}
			}
			catch (Exception e)
			{
				_log.warning("Castle BladeUnderFoot Raid Spawn error: " + e);
			}
		}
	}
	
	private String checkFortCondition(L2PcInstance player, L2Npc npc, boolean isEnter)
	{
		Castle castle = npc.getCastle();
		CastleDungeon dungeon = _castleDungeons.get(npc.getNpcId());
		if ((player == null) || (castle == null) || (dungeon == null))
		{
			return "CastleWarden-01.htm";
		}
		if ((player.getClan() == null) || (player.getClan().getCastleId() != castle.getCastleId()))
		{
			return "CastleWarden-01.htm";
		}
		boolean noContract = true;
		for (Fort fortress : FortManager.getInstance().getForts())
		{
			if (fortress.getCastleId() > 0)
			{
				noContract = false;
				break;
			}
		}
		if (noContract)
		{
			return "CastleWarden-02.htm";
		}
		else if (isEnter && (dungeon.getReEnterTime() > System.currentTimeMillis()))
		{
			return "CastleWarden-07.htm";
		}
		
		return null;
	}
	
	private void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if ((st != null) && (st.getInt("cond") == 1))
		{
			st.giveItems(DL_MARK, 140);
			st.playSound("ItemSound.quest_itemget");
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("enter"))
		{
			int[] tele = new int[3];
			tele[0] = 53322;
			tele[1] = 246380;
			tele[2] = -6580;
			return enterInstance(player, "castledungeon.xml", tele, _castleDungeons.get(npc.getNpcId()), checkFortCondition(player, npc, true));
		}
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("CastleWarden-10.htm"))
		{
			if (cond == 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("CastleWarden-15.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		String ret = checkFortCondition(player, npc, false);
		if (ret != null)
		{
			return ret;
		}
		else if (st != null)
		{
			int npcId = npc.getNpcId();
			int cond = 0;
			if (st.getState() == State.CREATED)
			{
				st.set("cond", "0");
			}
			else
			{
				cond = st.getInt("cond");
			}
			if (_castleDungeons.containsKey(npcId) && (cond == 0))
			{
				if (player.getLevel() >= 70)
				{
					htmltext = "CastleWarden-09.htm";
				}
				else
				{
					htmltext = "CastleWarden-00.htm";
					st.exitQuest(true);
				}
			}
			else if (_castleDungeons.containsKey(npcId) && (cond > 0) && (st.getState() == State.STARTED))
			{
				long count = st.getQuestItemsCount(DL_MARK);
				if ((cond == 1) && (count > 0))
				{
					htmltext = "CastleWarden-14.htm";
					st.takeItems(DL_MARK, count);
					st.rewardItems(KNIGHT_EPALUETTE, count);
				}
				else if ((cond == 1) && (count == 0))
				{
					htmltext = "CastleWarden-10.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		L2Playable attacker = (isSummon ? player.getSummon() : player);
		if ((attacker.getLevel() - npc.getLevel()) >= 9)
		{
			if ((attacker.getBuffCount() > 0) || (attacker.getDanceCount() > 0))
			{
				npc.setTarget(attacker);
				npc.doSimultaneousCast(RAID_CURSE.getSkill());
			}
			else if (player.getParty() != null)
			{
				for (L2PcInstance pmember : player.getParty().getMembers())
				{
					if ((pmember.getBuffCount() > 0) || (pmember.getDanceCount() > 0))
					{
						npc.setTarget(pmember);
						npc.doSimultaneousCast(RAID_CURSE.getSkill());
					}
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof CAUWorld)
		{
			CAUWorld world = (CAUWorld) tmpworld;
			if (Util.contains(RAIDS3, npc.getNpcId()))
			{
				if (player.getParty() != null)
				{
					for (L2PcInstance pl : player.getParty().getMembers())
					{
						rewardPlayer(pl);
					}
				}
				else
				{
					rewardPlayer(player);
				}
				
				Instance instanceObj = InstanceManager.getInstance().getInstance(world.getInstanceId());
				instanceObj.setDuration(360000);
				instanceObj.removeNpcs();
			}
			else
			{
				world.incStatus();
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid(world), RAID_SPAWN_DELAY);
			}
		}
		return null;
	}
	
	public Q512_BladeUnderFoot(int questId, String name, String descr)
	{
		super(questId, name, descr);
		_castleDungeons.put(36403, new CastleDungeon(13, 36403));
		_castleDungeons.put(36404, new CastleDungeon(14, 36404));
		_castleDungeons.put(36405, new CastleDungeon(15, 36405));
		_castleDungeons.put(36406, new CastleDungeon(16, 36406));
		_castleDungeons.put(36407, new CastleDungeon(17, 36407));
		_castleDungeons.put(36408, new CastleDungeon(18, 36408));
		_castleDungeons.put(36409, new CastleDungeon(19, 36409));
		_castleDungeons.put(36410, new CastleDungeon(20, 36410));
		_castleDungeons.put(36411, new CastleDungeon(21, 36411));
		
		for (int i : _castleDungeons.keySet())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		
		for (int i : RAIDS1)
		{
			addKillId(i);
		}
		for (int i : RAIDS2)
		{
			addKillId(i);
		}
		for (int i : RAIDS3)
		{
			addKillId(i);
		}
		
		for (int i = 25546; i <= 25571; i++)
		{
			addAttackId(i);
		}
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the)
		new Q512_BladeUnderFoot(512, Q512_BladeUnderFoot.class.getSimpleName(), "Blade Under Foot");
	}
}