package fidas.server.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import fidas.server.Config;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.model.actor.L2Attackable;
import fidas.server.gameserver.model.actor.L2Character;

public class DecayTaskManager
{
	protected static final Logger _log = Logger.getLogger(DecayTaskManager.class.getName());
	
	protected final Map<L2Character, Long> _decayTasks = new FastMap<L2Character, Long>().shared();
	
	protected DecayTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(), 10000, Config.DECAY_TIME_TASK);
	}
	
	public static DecayTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void addDecayTask(L2Character actor)
	{
		addDecayTask(actor, 0);
	}
	
	public void addDecayTask(L2Character actor, int interval)
	{
		_decayTasks.put(actor, System.currentTimeMillis() + interval);
	}
	
	public void cancelDecayTask(L2Character actor)
	{
		_decayTasks.remove(actor);
	}
	
	protected class DecayScheduler implements Runnable
	{
		@Override
		public void run()
		{
			final long current = System.currentTimeMillis();
			try
			{
				final Iterator<Entry<L2Character, Long>> it = _decayTasks.entrySet().iterator();
				Entry<L2Character, Long> e;
				L2Character actor;
				Long next;
				int delay;
				while (it.hasNext())
				{
					e = it.next();
					actor = e.getKey();
					next = e.getValue();
					if ((actor == null) || (next == null))
					{
						continue;
					}
					if (actor.isRaid() && !actor.isRaidMinion())
					{
						delay = Config.RAID_BOSS_DECAY_TIME;
					}
					else if ((actor instanceof L2Attackable) && (((L2Attackable) actor).isSpoil() || ((L2Attackable) actor).isSeeded()))
					{
						delay = Config.SPOILED_DECAY_TIME;
					}
					else
					{
						delay = Config.NPC_DECAY_TIME;
					}
					if ((current - next) > delay)
					{
						actor.onDecay();
						it.remove();
					}
				}
			}
			catch (Exception e)
			{
				// TODO: Find out the reason for exception. Unless caught here, mob decay would stop.
				_log.log(Level.WARNING, "Error in DecayScheduler: " + e.getMessage(), e);
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		ret.append("============= DecayTask Manager Report ============");
		ret.append(Config.EOL);
		ret.append("Tasks count: ");
		ret.append(_decayTasks.size());
		ret.append(Config.EOL);
		ret.append("Tasks dump:");
		ret.append(Config.EOL);
		
		Long current = System.currentTimeMillis();
		for (L2Character actor : _decayTasks.keySet())
		{
			ret.append("Class/Name: ");
			ret.append(actor.getClass().getSimpleName());
			ret.append('/');
			ret.append(actor.getName());
			ret.append(" decay timer: ");
			ret.append(current - _decayTasks.get(actor));
			ret.append(Config.EOL);
		}
		
		return ret.toString();
	}
	
	/**
	 * <u><b><font color="FF0000">Read only</font></b></u>
	 * @return
	 */
	public Map<L2Character, Long> getTasks()
	{
		return _decayTasks;
	}
	
	private static class SingletonHolder
	{
		protected static final DecayTaskManager _instance = new DecayTaskManager();
	}
}