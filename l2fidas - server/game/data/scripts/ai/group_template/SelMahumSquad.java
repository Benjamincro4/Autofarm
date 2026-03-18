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
package ai.group_template;

import ai.npc.AbstractNpcAI;

import fidas.server.gameserver.GameTimeController;
import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.datatables.SpawnTable;
import fidas.server.gameserver.model.L2CharPosition;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.L2Spawn;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2MonsterInstance;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.network.NpcStringId;
import fidas.server.gameserver.network.clientpackets.Say2;
import fidas.server.gameserver.network.serverpackets.NpcSay;
import fidas.server.gameserver.util.Util;
import fidas.server.util.Rnd;

/**
 * Sel Mahum Training Ground AI for squads and chefs.
 * @author GKR
 */

public class SelMahumSquad extends AbstractNpcAI
{
	
	// NPC's
	private static final int CHEF = 18908;
	private static final int FIRE = 18927;
	private static final int STOVE = 18933;
	
	private static final int OHS_Weapon = 15280;
	private static final int THS_Weapon = 15281;
	
	// Sel Mahum Squad Leaders
	private static final int[] SQUAD_LEADERS =
	{
		22786,
		22787,
		22788
	};
	
	private static final NpcStringId[] CHEF_FSTRINGS =
	{
		NpcStringId.I_BROUGHT_THE_FOOD,
		NpcStringId.COME_AND_EAT
	};
	
	private static final int FIRE_EFFECT_BURN = 1;
	private static final int FIRE_EFFECT_NONE = 2;
	
	private static final int MAHUM_EFFECT_EAT = 1;
	private static final int MAHUM_EFFECT_SLEEP = 2;
	private static final int MAHUM_EFFECT_NONE = 3;
	
	// Script AI parameters naming
	// Squad Leaders
	private static final int DESTINATION_X = 1;
	private static final int DESTINATION_Y = 2;
	private static final int FULL_BARREL_REWARDING_PLAYER = 3;
	private static final int BUSY_STATE = 4;
	
	// Chefs
	private static final int INVUL_REMOVE_TIMER_STARTED = 1;
	private static final int REWARD_TIME_GONE = 2;
	
	public SelMahumSquad(String name, String descr)
	{
		super(name, descr);
		
		addAttackId(CHEF);
		addEventReceivedId(CHEF, FIRE, STOVE);
		addKillId(CHEF);
		addNodeArrivedId(CHEF);
		addSkillSeeId(STOVE);
		addSpawnId(CHEF, FIRE);
		addSpellFinishedId(CHEF);
		registerMobs(SQUAD_LEADERS, QuestEventType.ON_ATTACK, QuestEventType.ON_EVENT_RECEIVED, QuestEventType.ON_FACTION_CALL, QuestEventType.ON_MOVE_FINISHED, QuestEventType.ON_SPAWN);
		
		// Send event to monsters, that was spawned through SpawnTable at server start (it is impossible to track first spawn)
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawns(CHEF))
		{
			onSpawn(spawn.getLastSpawn());
		}
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawns(FIRE))
		{
			onSpawn(spawn.getLastSpawn());
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("chef_disable_reward")) // 2019005
		{
			npc.setScriptValue(REWARD_TIME_GONE, 1); // i_ai6 = 1
		}
		
		else if (event.equalsIgnoreCase("chef_heal_player")) // 2019003
		{
			healPlayer(npc, player);
		}
		
		else if (event.equalsIgnoreCase("chef_remove_invul") && npc.isMonster()) // 2019006
		{
			npc.setIsInvul(false);
			npc.setScriptValue(INVUL_REMOVE_TIMER_STARTED, 0); // i_ai5 = 0
			
			if ((player != null) && !player.isDead() && npc.getKnownList().knowsThePlayer(player))
			{
				((L2MonsterInstance) npc).addDamageHate(player, 0, 999);
				((L2MonsterInstance) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player, null);
			}
		}
		
		else if (event.equalsIgnoreCase("chef_set_invul") && !npc.isDead())
		{
			npc.setIsInvul(true);
		}
		
		else if (event.equalsIgnoreCase("fire"))
		{
			startQuestTimer("fire", 30000 + Rnd.get(5000), npc, null);
			npc.setDisplayEffect(FIRE_EFFECT_NONE);
			
			if (Rnd.get(GameTimeController.getInstance().isNowNight() ? 2 : 4) < 1)
			{
				npc.setDisplayEffect(FIRE_EFFECT_BURN); // fire burns
				npc.broadcastEvent("SCE_CAMPFIRE_START", 600, null);
			}
			else
			{
				npc.setDisplayEffect(FIRE_EFFECT_NONE); // fire goes out
				npc.broadcastEvent("SCE_CAMPFIRE_END", 600, null);
			}
		}
		
		else if (event.equalsIgnoreCase("fire_arrived"))
		{
			// myself.i_quest0 = 1;
			npc.setIsRunning(false);
			npc.setTarget(npc);
			
			if (npc.isNoRndWalk()) // i_ai0 == 1
			{
				npc.doCast(SkillTable.getInstance().getInfo(6331, 1));
				npc.setDisplayEffect(MAHUM_EFFECT_SLEEP);
			}
			if (npc.isScriptValue(BUSY_STATE, 1)) // Eating - i_ai3 = 1
			{
				npc.doCast(SkillTable.getInstance().getInfo(6332, 1));
				npc.setDisplayEffect(MAHUM_EFFECT_EAT);
			}
			
			startQuestTimer("remove_effects", 300000, npc, null);
		}
		
		else if (event.equalsIgnoreCase("notify_dinner"))
		{
			npc.broadcastEvent("SCE_DINNER_EAT", 600, null);
		}
		
		else if (event.equalsIgnoreCase("remove_effects"))
		{
			// myself.i_quest0 = 0;
			npc.setIsRunning(true);
			npc.setDisplayEffect(MAHUM_EFFECT_NONE);
		}
		
		else if (event.equalsIgnoreCase("reset_full_bottle_prize"))
		{
			npc.setScriptValue(FULL_BARREL_REWARDING_PLAYER, 0);
		}
		
		else if (event.equalsIgnoreCase("return_from_fire") && npc.isMonster() && !npc.isDead())
		{
			((L2MonsterInstance) npc).returnHome();
		}
		
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if ((npc.getNpcId() == CHEF) && npc.isScriptValue(BUSY_STATE, 0)) // i_ai2 == 0
		{
			if (npc.isScriptValue(INVUL_REMOVE_TIMER_STARTED, 0)) // i_ai5 == 0
			{
				startQuestTimer("chef_remove_invul", 180000, npc, attacker); // 2019004
				startQuestTimer("chef_disable_reward", 60000, npc, null); // 2019005
				npc.setScriptValue(INVUL_REMOVE_TIMER_STARTED, 1);
			}
			startQuestTimer("chef_heal_player", 1000, npc, attacker); // 2019003
			startQuestTimer("chef_set_invul", 60000, npc, null); // 2019006
			npc.setScriptValue(BUSY_STATE, 1);
		}
		
		else if (Util.contains(SQUAD_LEADERS, npc.getNpcId()))
		{
			handlePreAttackMotion(npc);
		}
		
		return null;
	}
	
	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		handlePreAttackMotion(npc);
		return null;
	}
	
	@Override
	public String onEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		if (eventName.equals("SCE_DINNER_CHECK") && (receiver.getNpcId() == FIRE))
		{
			receiver.setDisplayEffect(FIRE_EFFECT_BURN);
			L2Npc stove = addSpawn(STOVE, receiver.getX(), receiver.getY(), receiver.getZ() + 100, 0, false, 0);
			stove.setSummoner(receiver);
			startQuestTimer("notify_dinner", 2000, receiver, null); // @SCE_DINNER_EAT
			sender.broadcastPacket(new NpcSay(sender.getObjectId(), Say2.NPC_ALL, sender.getNpcId(), CHEF_FSTRINGS[Rnd.get(2)]), 1250);
		}
		else if (eventName.equals("SCE_CAMPFIRE_START") && Util.contains(SQUAD_LEADERS, receiver.getNpcId()) && !receiver.isNoRndWalk() && !receiver.isDead() && (receiver.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK))
		{
			receiver.setIsNoRndWalk(true); // Moving to fire - i_ai0 = 1
			receiver.setIsRunning(true);
			int[] coords = Util.getPointInRange(sender, 100, 200);
			receiver.stopMove(null);
			receiver.setScriptValue(DESTINATION_X, coords[0]);
			receiver.setScriptValue(DESTINATION_Y, coords[1]);
			receiver.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(coords[0], coords[1], sender.getZ(), receiver.getHeading()));
		}
		else if (eventName.equals("SCE_CAMPFIRE_END"))
		{
			if ((receiver.getNpcId() == STOVE) && (receiver.getSummoner() == sender))
			{
				receiver.deleteMe();
			}
			else if (Util.contains(SQUAD_LEADERS, receiver.getNpcId()) && (receiver.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK))
			{
				receiver.setIsNoRndWalk(false);
				receiver.setScriptValue(BUSY_STATE, 0);
				receiver.setRHandId(THS_Weapon);
				startQuestTimer("return_from_fire", 3000, receiver, null);
			}
		}
		else if (eventName.equals("SCE_DINNER_EAT") && Util.contains(SQUAD_LEADERS, receiver.getNpcId()) && receiver.isScriptValue(BUSY_STATE, 0) && !receiver.isDead() && (receiver.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK))
		{
			if (receiver.isNoRndWalk()) // i_ai0 == 1
			{
				receiver.setRHandId(THS_Weapon);
			}
			receiver.setIsNoRndWalk(true); // Moving to fire - i_ai0 = 1
			receiver.setScriptValue(BUSY_STATE, 1); // Eating - i_ai3 = 1
			receiver.setIsRunning(true);
			NpcSay ns;
			if (Rnd.get(3) < 1)
			{
				ns = new NpcSay(receiver.getObjectId(), Say2.NPC_ALL, receiver.getNpcId(), NpcStringId.LOOKS_DELICIOUS);
			}
			else
			{
				ns = new NpcSay(receiver.getObjectId(), Say2.NPC_ALL, receiver.getNpcId(), NpcStringId.LETS_GO_EAT);
			}
			receiver.broadcastPacket(ns);
			int[] coords = Util.getPointInRange(sender, 100, 200);
			receiver.stopMove(null);
			receiver.setScriptValue(DESTINATION_X, coords[0]);
			receiver.setScriptValue(DESTINATION_Y, coords[1]);
			receiver.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(coords[0], coords[1], sender.getZ(), receiver.getHeading()));
		}
		
		else if (eventName.equals("SCE_SOUP_FAILURE") && Util.contains(SQUAD_LEADERS, receiver.getNpcId()))
		{
			receiver.setScriptValue(FULL_BARREL_REWARDING_PLAYER, reference.getObjectId()); // TODO: Use it in 289 quest
			startQuestTimer("reset_full_bottle_prize", 180000, receiver, null);
		}
		
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.isMonster() && npc.isScriptValue(REWARD_TIME_GONE, 0))
		{
			((L2MonsterInstance) npc).dropItem(killer, 15492, 1);
		}
		cancelQuestTimer("chef_remove_invul", npc, null);
		cancelQuestTimer("chef_disable_reward", npc, null);
		cancelQuestTimer("chef_heal_player", npc, null);
		cancelQuestTimer("chef_set_invul", npc, null);
		
		return null;
	}
	
	@Override
	public String onMoveFinished(L2Npc npc)
	{
		// Npc moves to fire
		if (npc.isNoRndWalk() && (npc.getX() == npc.getScriptValue(DESTINATION_X)) && (npc.getY() == npc.getScriptValue(DESTINATION_Y)))
		{
			npc.setRHandId(OHS_Weapon);
			startQuestTimer("fire_arrived", 3000, npc, null);
		}
		return null;
	}
	
	@Override
	public String onNodeArrived(L2Npc npc)
	{
		npc.broadcastEvent("SCE_DINNER_CHECK", 300, null);
		return null;
	}
	
	@Override
	// @SCE_SOUP_FAILURE
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if ((npc.getNpcId() == STOVE) && (skill.getId() == 9075) && Util.contains(targets, npc))
		{
			npc.doCast(SkillTable.getInstance().getInfo(6688, 1));
			npc.broadcastEvent("SCE_SOUP_FAILURE", 600, caster);
		}
		
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
		{
			if (npc.getNpcId() == CHEF)
			{
				npc.setIsInvul(false);
			}
			
			else if (npc.getNpcId() == FIRE)
			{
				startQuestTimer("fire", 1000, npc, null);
			}
			
			else if (Util.contains(SQUAD_LEADERS, npc.getNpcId()))
			{
				npc.setDisplayEffect(3);
				npc.setIsNoRndWalk(false);
			}
		}
		return null;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if ((skill != null) && (skill.getId() == 6330))
		{
			healPlayer(npc, player);
		}
		
		return null;
	}
	
	private void healPlayer(L2Npc npc, L2PcInstance player)
	{
		if ((player != null) && !player.isDead() && !npc.isScriptValue(INVUL_REMOVE_TIMER_STARTED, 1) && ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK) || (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST)))
		{
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(6330, 1));
		}
		else
		{
			cancelQuestTimer("chef_set_invul", npc, null);
			npc.setScriptValue(BUSY_STATE, 0); // i_ai2 = 0
			npc.setScriptValue(INVUL_REMOVE_TIMER_STARTED, 0); // i_ai5 = 0
			npc.setIsRunning(false);
		}
	}
	
	private void handlePreAttackMotion(L2Npc attacked)
	{
		cancelQuestTimer("remove_effects", attacked, null);
		attacked.setScriptValue(BUSY_STATE, 0);
		attacked.setIsNoRndWalk(false); // i_ai0 == 0
		attacked.setDisplayEffect(MAHUM_EFFECT_NONE);
		if (attacked.getRightHandItem() == OHS_Weapon)
		{
			attacked.setRHandId(THS_Weapon);
		}
		// TODO: Check about i_quest0
		
	}
	
	public static void main(String[] args)
	{
		new SelMahumSquad(SelMahumSquad.class.getSimpleName(), "ai");
	}
}