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

import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.datatables.SpawnTable;
import fidas.server.gameserver.model.L2CharPosition;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.L2Spawn;
import fidas.server.gameserver.model.actor.L2Attackable;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.NpcStringId;
import fidas.server.gameserver.network.clientpackets.Say2;
import fidas.server.gameserver.network.serverpackets.NpcSay;
import fidas.server.gameserver.util.Util;
import fidas.server.util.Rnd;

/**
 * Sel Mahum Training Ground AI for drill groups.
 * @author GKR
 */

public class SelMahumDrill extends AbstractNpcAI
{
	// Sel Mahum Drill Sergeant, Sel Mahum Training Officer, Sel Mahum Drill Sergeant respectively
	private static final int[] MAHUM_CHIEFS =
	{
		22775,
		22776,
		22778
	};
	
	// Sel Mahum Recruit, Sel Mahum Recruit, Sel Mahum Soldier, Sel Mahum Recruit, Sel Mahum Soldier respectively
	private static final int[] MAHUM_SOLDIERS =
	{
		22780,
		22782,
		22783,
		22784,
		22785
	};
	
	private static final int[] CHIEF_SOCIAL_ACTIONS =
	{
		1,
		4,
		5,
		7
	};
	private static final Actions[] SOLDIER_SOCIAL_ACTIONS =
	{
		Actions.SCE_TRAINING_ACTION_A,
		Actions.SCE_TRAINING_ACTION_B,
		Actions.SCE_TRAINING_ACTION_C,
		Actions.SCE_TRAINING_ACTION_D
	};
	
	/**
	 * 1801112 - Who is mucking with my recruits!?! 1801113 - You are entering a world of hurt!
	 */
	private static final NpcStringId[] CHIEF_FSTRINGS =
	{
		NpcStringId.HOW_DARE_YOU_ATTACK_MY_RECRUITS,
		NpcStringId.WHO_IS_DISRUPTING_THE_ORDER
	};
	
	/**
	 * 1801114 - They done killed da Sarge... Run!! 1801115 - Don't Panic... Okay, Panic!
	 */
	private static final NpcStringId[] SOLDIER_FSTRINGS =
	{
		NpcStringId.THE_DRILLMASTER_IS_DEAD,
		NpcStringId.LINE_UP_THE_RANKS
	};
	
	// Chiefs event broadcast range
	private static final int TRAINING_RANGE = 1000;
	
	// Script AI parameters naming
	private static final int SOCIAL_ACTION_ALT_BEHAVIOR = 1;
	private static final int SOCIAL_ACTION_NEXT_INDEX = 2;
	private static final int SOCIAL_ACTION_REMAINED_COUNT = 3;
	private static final int BUSY_STATE = 4;
	
	private enum Actions
	{
		SCE_TRAINING_ACTION_A(4, -1, 2, 2333),
		SCE_TRAINING_ACTION_B(1, -1, 2, 4333),
		SCE_TRAINING_ACTION_C(6, 5, 4, 1000),
		SCE_TRAINING_ACTION_D(7, -1, 2, 1000);
		
		private final int _socialActionId;
		private final int _altSocialActionId;
		private final int _repeatCount;
		private final int _repeatInterval;
		
		private Actions(int socialActionId, int altSocialActionId, int repeatCount, int repeatInterval)
		{
			_socialActionId = socialActionId;
			_altSocialActionId = altSocialActionId;
			_repeatCount = repeatCount;
			_repeatInterval = repeatInterval;
		}
		
		protected int getSocialActionId()
		{
			return _socialActionId;
		}
		
		protected int getAltSocialActionId()
		{
			return _altSocialActionId;
		}
		
		protected int getRepeatCount()
		{
			return _repeatCount;
		}
		
		protected int getRepeatInterval()
		{
			return _repeatInterval;
		}
	}
	
	public SelMahumDrill(String name, String descr)
	{
		super(name, descr);
		
		registerMobs(MAHUM_CHIEFS, QuestEventType.ON_EVENT_RECEIVED, QuestEventType.ON_KILL, QuestEventType.ON_SPAWN);
		registerMobs(MAHUM_SOLDIERS, QuestEventType.ON_ATTACK, QuestEventType.ON_EVENT_RECEIVED, QuestEventType.ON_SPAWN);
		
		for (int npcId : MAHUM_CHIEFS)
		{
			for (L2Spawn npcSpawn : SpawnTable.getInstance().getSpawns(npcId))
			{
				onSpawn(npcSpawn.getLastSpawn());
			}
		}
		
		// Start global return home timer
		startQuestTimer("return_home", 120000, null, null, true);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("do_social_action"))
		{
			if ((npc != null) && !npc.isDead())
			{
				if (Util.contains(MAHUM_CHIEFS, npc.getNpcId()))
				{
					if (npc.isScriptValue(BUSY_STATE, 0) && (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) && (npc.getX() == npc.getSpawn().getLocx()) && (npc.getY() == npc.getSpawn().getLocy()))
					{
						int idx = Rnd.get(6);
						if (idx <= (CHIEF_SOCIAL_ACTIONS.length - 1))
						{
							npc.broadcastSocialAction(CHIEF_SOCIAL_ACTIONS[idx]);
							npc.setScriptValue(SOCIAL_ACTION_NEXT_INDEX, idx); // Pass social action index to soldiers via script value
							npc.broadcastEvent("do_social_action", TRAINING_RANGE, null);
						}
					}
					
					startQuestTimer("do_social_action", 15000, npc, null);
				}
				else if (Util.contains(MAHUM_SOLDIERS, npc.getNpcId()))
				{
					handleSocialAction(npc, SOLDIER_SOCIAL_ACTIONS[npc.getScriptValue(SOCIAL_ACTION_NEXT_INDEX)], false);
				}
			}
		}
		
		else if (event.equalsIgnoreCase("reset_busy_state"))
		{
			if (npc != null)
			{
				npc.setScriptValue(BUSY_STATE, 0);
				npc.disableCoreAI(false);
			}
		}
		
		else if (event.equalsIgnoreCase("return_home"))
		{
			for (int npcId : MAHUM_SOLDIERS)
			{
				for (L2Spawn npcSpawn : SpawnTable.getInstance().getSpawns(npcId))
				{
					onSpawn(npcSpawn.getLastSpawn());
					
					if ((npcSpawn.getLastSpawn() != null) && !npcSpawn.getLastSpawn().isDead() && npcSpawn.getLastSpawn().hasAIValue("groupId") && ((npcSpawn.getLastSpawn().getX() != npcSpawn.getLastSpawn().getSpawn().getLocx()) || (npcSpawn.getLastSpawn().getY() != npcSpawn.getLastSpawn().getSpawn().getLocy())) && ((npcSpawn.getLastSpawn().getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) || (npcSpawn.getLastSpawn().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)))
					{
						npcSpawn.getLastSpawn().setHeading(npcSpawn.getHeading());
						npcSpawn.getLastSpawn().teleToLocation(npcSpawn.getLocx(), npcSpawn.getLocy(), npcSpawn.getLocz());
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (Rnd.get(10) < 1)
		{
			npc.broadcastEvent("ATTACKED", 1000, null);
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		if ((receiver != null) && !receiver.isDead() && receiver.hasAIValue("groupId") && (receiver.getAIValue("groupId") == sender.getAIValue("groupId")))
		{
			if (Util.contains(MAHUM_SOLDIERS, receiver.getNpcId()))
			{
				if (eventName.equals("do_social_action"))
				{
					int actionIndex = sender.getScriptValue(SOCIAL_ACTION_NEXT_INDEX);
					receiver.setScriptValue(SOCIAL_ACTION_NEXT_INDEX, actionIndex);
					handleSocialAction(receiver, SOLDIER_SOCIAL_ACTIONS[actionIndex], true);
				}
				else if (eventName.equals("CHIEF_DIED"))
				{
					if (Rnd.get(4) < 1)
					{
						receiver.broadcastPacket(new NpcSay(receiver.getObjectId(), Say2.NPC_ALL, receiver.getNpcId(), SOLDIER_FSTRINGS[Rnd.get(2)]));
					}
					
					if (receiver.isAttackable())
					{
						((L2Attackable) receiver).clearAggroList();
					}
					receiver.disableCoreAI(true);
					receiver.setScriptValue(BUSY_STATE, 1);
					receiver.setIsRunning(true);
					receiver.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((receiver.getX() + Rnd.get(-800, 800)), (receiver.getY() + Rnd.get(-800, 800)), receiver.getZ(), receiver.getHeading()));
					startQuestTimer("reset_busy_state", 5000, receiver, null);
				}
			}
			
			else if (eventName.equals("ATTACKED") && Util.contains(MAHUM_CHIEFS, receiver.getNpcId()))
			{
				receiver.broadcastPacket(new NpcSay(receiver.getObjectId(), Say2.NPC_ALL, receiver.getNpcId(), CHIEF_FSTRINGS[Rnd.get(2)]));
			}
		}
		
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		npc.broadcastEvent("CHIEF_DIED", TRAINING_RANGE, null);
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
		{
			if (Util.contains(MAHUM_CHIEFS, npc.getNpcId()))
			{
				startQuestTimer("do_social_action", 15000, npc, null);
			}
			
			else if (Util.contains(MAHUM_SOLDIERS, npc.getNpcId()) && (Rnd.get(18) < 1))
			{
				npc.setScriptValue(SOCIAL_ACTION_ALT_BEHAVIOR, 1);
			}
			
			// Restore AI handling by core
			npc.disableCoreAI(false);
		}
		
		return null;
	}
	
	private void handleSocialAction(L2Npc npc, Actions action, boolean firstCall)
	{
		if (!npc.isScriptValue(BUSY_STATE, 0) || (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ACTIVE) || (npc.getX() != npc.getSpawn().getLocx()) || (npc.getY() != npc.getSpawn().getLocy()))
		{
			return;
		}
		
		int socialActionId = npc.isScriptValue(SOCIAL_ACTION_ALT_BEHAVIOR, 0) ? action.getSocialActionId() : action.getAltSocialActionId();
		
		if (socialActionId < 0)
		{
			return;
		}
		
		if (firstCall)
		{
			npc.setScriptValue(SOCIAL_ACTION_REMAINED_COUNT, action.getRepeatCount());
		}
		
		npc.broadcastSocialAction(socialActionId);
		
		int remainedCount = npc.getScriptValue(SOCIAL_ACTION_REMAINED_COUNT);
		if (remainedCount > 0)
		{
			npc.setScriptValue(SOCIAL_ACTION_REMAINED_COUNT, (remainedCount - 1));
			startQuestTimer("do_social_action", action.getRepeatInterval(), npc, null);
		}
	}
	
	public static void main(String[] args)
	{
		new SelMahumDrill(SelMahumDrill.class.getSimpleName(), "ai");
	}
}