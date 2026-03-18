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
package instances.SeerUgoros;

import java.util.concurrent.ScheduledFuture;

import quests.Q00288_HandleWithCare.Q00288_HandleWithCare;
import quests.Q00423_TakeYourBestShot.Q00423_TakeYourBestShot;
import ai.npc.AbstractNpcAI;

import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.instancemanager.MapRegionManager;
import fidas.server.gameserver.instancemanager.ZoneManager;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.L2Attackable;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.model.zone.L2ZoneType;
import fidas.server.gameserver.network.NpcStringId;
import fidas.server.gameserver.network.clientpackets.Say2;
import fidas.server.gameserver.network.serverpackets.NpcSay;
import fidas.server.gameserver.util.Util;
import fidas.server.util.Rnd;

public class SeerUgoros extends AbstractNpcAI
{
	protected ScheduledFuture<?> _thinkTask = null;
	private static final int _ugoros_pass = 15496;
	private static final int _mid_scale = 15498;
	private static final int _high_scale = 15497;
	private static final int _ugoros_zone = 20706;
	private static final int _seer_ugoros = 18863;
	private static final int _batracos = 32740;
	private static final int _weed_id = 18867;
	protected static L2Npc _ugoros = null;
	protected static L2Npc _weed = null;
	protected static boolean _weed_attack = false;
	private static boolean _weed_killed_by_player = false;
	private static boolean _killed_one_weed = false;
	protected static L2PcInstance _player = null;
	private static final byte ALIVE = 0;
	private static final byte FIGHTING = 1;
	private static final byte DEAD = 2;
	protected static byte STATE = DEAD;
	private static final L2Skill _ugoros_skill = SkillTable.getInstance().getInfo(6426, 1);
	
	public SeerUgoros(String name, String descr)
	{
		super(name, descr);
		
		addStartNpc(_batracos);
		addTalkId(_batracos);
		
		addAttackId(_weed_id);
		
		addKillId(_seer_ugoros);
		
		startQuestTimer("ugoros_respawn", 60000, null, null);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("ugoros_respawn") && (_ugoros == null))
		{
			_ugoros = addSpawn(_seer_ugoros, 96804, 85604, -3720, 34360, false, 0);
			broadcastInRegion(_ugoros, NpcStringId.LISTEN_OH_TANTAS_I_HAVE_RETURNED_THE_PROPHET_YUGOROS_OF_THE_BLACK_ABYSS_IS_WITH_ME_SO_DO_NOT_BE_AFRAID);
			STATE = ALIVE;
			startQuestTimer("ugoros_shout", 120000, null, null);
		}
		else if (event.equalsIgnoreCase("ugoros_shout"))
		{
			if (STATE == FIGHTING)
			{
				L2ZoneType _zone = ZoneManager.getInstance().getZoneById(_ugoros_zone);
				if (_player == null)
				{
					STATE = ALIVE;
				}
				else if (!_zone.isCharacterInZone(_player))
				{
					STATE = ALIVE;
					_player = null;
				}
			}
			else if (STATE == ALIVE)
			{
				broadcastInRegion(_ugoros, NpcStringId.LISTEN_OH_TANTAS_THE_BLACK_ABYSS_IS_FAMISHED_FIND_SOME_FRESH_OFFERINGS);
			}
			
			startQuestTimer("ugoros_shout", 120000, null, null);
		}
		else if (event.equalsIgnoreCase("ugoros_attack"))
		{
			if (_player != null)
			{
				changeAttackTarget(_player);
				NpcSay packet = new NpcSay(_ugoros.getObjectId(), Say2.NPC_ALL, _ugoros.getNpcId(), NpcStringId.WELCOME_S1_LET_US_SEE_IF_YOU_HAVE_BROUGHT_A_WORTHY_OFFERING_FOR_THE_BLACK_ABYSS);
				packet.addStringParameter(_player.getName().toString());
				_ugoros.broadcastPacket(packet);
				if (_thinkTask != null)
				{
					_thinkTask.cancel(true);
				}
				
				_thinkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ThinkTask(), 1000, 3000);
			}
		}
		else if (event.equalsIgnoreCase("weed_check"))
		{
			if ((_weed_attack == true) && (_ugoros != null) && (_weed != null))
			{
				if (_weed.isDead() && !_weed_killed_by_player)
				{
					_killed_one_weed = true;
					_weed = null;
					_weed_attack = false;
					_ugoros.getStatus().setCurrentHp(_ugoros.getStatus().getCurrentHp() + (_ugoros.getMaxHp() * 0.2));
					_ugoros.broadcastPacket(new NpcSay(_ugoros.getObjectId(), 0, _ugoros.getNpcId(), NpcStringId.WHAT_A_FORMIDABLE_FOE_BUT_I_HAVE_THE_ABYSS_WEED_GIVEN_TO_ME_BY_THE_BLACK_ABYSS_LET_ME_SEE));
				}
				else
				{
					startQuestTimer("weed_check", 2000, null, null);
				}
			}
			else
			{
				_weed = null;
				_weed_attack = false;
			}
		}
		else if (event.equalsIgnoreCase("ugoros_expel"))
		{
			if (_player != null)
			{
				_player.teleToLocation(94701, 83053, -3580);
				_player = null;
			}
		}
		else if (event.equalsIgnoreCase("teleportInside"))
		{
			if ((player != null) && (STATE == ALIVE))
			{
				if (player.getInventory().getItemByItemId(_ugoros_pass) != null)
				{
					STATE = FIGHTING;
					_player = player;
					_killed_one_weed = false;
					player.teleToLocation(95984, 85692, -3720);
					player.destroyItemByItemId("SeerUgoros", _ugoros_pass, 1, npc, true);
					startQuestTimer("ugoros_attack", 2000, null, null);
					QuestState st = player.getQuestState(Q00288_HandleWithCare.class.getSimpleName());
					if (st != null)
					{
						st.set("drop", "1");
					}
				}
				else
				{
					QuestState st = player.getQuestState(Q00423_TakeYourBestShot.class.getSimpleName());
					if (st == null)
					{
						return "<html><body>Gatekeeper Batracos:<br>You look too inexperienced to make a journey to see Tanta Seer Ugoros. If you can convince Chief Investigator Johnny that you should go, then I will let you pass. Johnny has been everywhere and done everything. He may not be of my people but he has my respect, and anyone who has his will in turn have mine as well.<br></body></html>";
					}
					
					return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";
				}
			}
			else
			{
				return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";
			}
		}
		else if (event.equalsIgnoreCase("teleport_back"))
		{
			if (player != null)
			{
				player.teleToLocation(94792, 83542, -3424);
				_player = null;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.isDead())
		{
			return null;
		}
		
		if (npc.getNpcId() == _weed_id)
		{
			if ((_ugoros != null) && (_weed != null) && npc.equals(_weed))
			{
				_weed = null;
				_weed_attack = false;
				_weed_killed_by_player = true;
				_ugoros.broadcastPacket(new NpcSay(_ugoros.getObjectId(), 0, _ugoros.getNpcId(), NpcStringId.NO_HOW_DARE_YOU_STOP_ME_FROM_USING_THE_ABYSS_WEED_DO_YOU_KNOW_WHAT_YOU_HAVE_DONE));
				
				if (_thinkTask != null)
				{
					_thinkTask.cancel(true);
				}
				
				_thinkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ThinkTask(), 500, 3000);
			}
			npc.doDie(attacker);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (npc.getNpcId() == _seer_ugoros)
		{
			if (_thinkTask != null)
			{
				_thinkTask.cancel(true);
				_thinkTask = null;
			}
			STATE = DEAD;
			broadcastInRegion(_ugoros, NpcStringId.AH_HOW_COULD_I_LOSE_OH_BLACK_ABYSS_RECEIVE_ME);
			_ugoros = null;
			addSpawn(_batracos, 96782, 85918, -3720, 34360, false, 50000);
			startQuestTimer("ugoros_expel", 50000, null, null);
			startQuestTimer("ugoros_respawn", 60000, null, null);
			QuestState st = player.getQuestState(Q00288_HandleWithCare.class.getSimpleName());
			if ((st != null) && (st.getInt("cond") == 1) && (st.getInt("drop") == 1))
			{
				if (_killed_one_weed)
				{
					player.addItem("SeerUgoros", _mid_scale, 1, npc, true);
					st.set("cond", "2");
				}
				else
				{
					player.addItem("SeerUgoros", _high_scale, 1, npc, true);
					st.set("cond", "3");
				}
				st.unset("drop");
			}
		}
		return null;
	}
	
	private void broadcastInRegion(L2Npc npc, NpcStringId npcString)
	{
		if (npc == null)
		{
			return;
		}
		
		NpcSay cs = new NpcSay(npc.getObjectId(), 1, npc.getNpcId(), npcString);
		int region = MapRegionManager.getInstance().getMapRegionLocId(npc.getX(), npc.getY());
		for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
		{
			if (region == MapRegionManager.getInstance().getMapRegionLocId(player.getX(), player.getY()))
			{
				if (Util.checkIfInRange(6000, npc, player, false))
				{
					player.sendPacket(cs);
				}
			}
		}
	}
	
	private class ThinkTask implements Runnable
	{
		protected ThinkTask()
		{
		}
		
		@Override
		public void run()
		{
			L2ZoneType _zone = ZoneManager.getInstance().getZoneById(_ugoros_zone);
			
			if ((STATE == FIGHTING) && (_player != null) && _zone.isCharacterInZone(_player) && !_player.isDead())
			{
				if (_weed_attack && (_weed != null))
				{
				}
				else if (Rnd.get(10) < 6)
				{
					_weed = null;
					for (L2Character _char : _ugoros.getKnownList().getKnownCharactersInRadius(2000))
					{
						if ((_char instanceof L2Attackable) && !_char.isDead() && (((L2Attackable) _char).getNpcId() == _weed_id))
						{
							_weed_attack = true;
							_weed = (L2Attackable) _char;
							changeAttackTarget(_weed);
							startQuestTimer("weed_check", 1000, null, null);
							break;
						}
					}
					if (_weed == null)
					{
						changeAttackTarget(_player);
					}
				}
				else
				{
					changeAttackTarget(_player);
				}
			}
			else
			{
				STATE = ALIVE;
				_player = null;
				if (_thinkTask != null)
				{
					_thinkTask.cancel(true);
					_thinkTask = null;
				}
			}
		}
	}
	
	protected void changeAttackTarget(L2Character _attack)
	{
		((L2Attackable) _ugoros).getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		((L2Attackable) _ugoros).clearAggroList();
		((L2Attackable) _ugoros).setTarget(_attack);
		
		if (_attack instanceof L2Attackable)
		{
			_weed_killed_by_player = false;
			_ugoros.disableSkill(_ugoros_skill, 100000);
			((L2Attackable) _ugoros).setIsRunning(true);
			((L2Attackable) _ugoros).addDamageHate(_attack, 0, Integer.MAX_VALUE);
		}
		else
		{
			_ugoros.enableSkill(_ugoros_skill);
			((L2Attackable) _ugoros).addDamageHate(_attack, 0, 99);
			((L2Attackable) _ugoros).setIsRunning(false);
		}
		((L2Attackable) _ugoros).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _attack);
	}
	
	public static void main(String[] args)
	{
		new SeerUgoros(SeerUgoros.class.getSimpleName(), "instances");
	}
}