package fidas.server.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fidas.server.Config;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.scripting.L2ScriptEngineManager;
import fidas.server.gameserver.scripting.ScriptManager;
import fidas.server.util.L2FastMap;

public class QuestManager extends ScriptManager<Quest>
{
	protected static final Logger _log = Logger.getLogger(QuestManager.class.getName());
	
	private final Map<String, Quest> _quests = new L2FastMap<>(true);
	
	protected QuestManager()
	{
	}
	
	public final boolean reload(String questFolder)
	{
		Quest q = getQuest(questFolder);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	/**
	 * Reloads a the quest given by questId.<BR>
	 * <B>NOTICE: Will only work if the quest name is equal the quest folder name</B>
	 * @param questId The id of the quest to be reloaded
	 * @return true if reload was successful, false otherwise
	 */
	public final boolean reload(int questId)
	{
		Quest q = getQuest(questId);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	public final void reloadAllQuests()
	{
		_log.info(getClass().getSimpleName() + ": Reloading all server scripts.");
		// unload all scripts
		for (Quest quest : _quests.values())
		{
			if (quest != null)
			{
				quest.unload(false);
			}
		}
		_quests.clear();
		
		try
		{
			// now load all scripts
			L2ScriptEngineManager.getInstance().executeScriptList(new File(Config.DATAPACK_ROOT, "data/scripts.cfg"));
			QuestManager.getInstance().report();
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed loading scripts.cfg, no script going to be loaded!", e);
		}
	}
	
	public final void report()
	{
		_log.info(getClass().getSimpleName() + ": " + _quests.size() + " quests");
	}
	
	public final void save()
	{
		for (Quest q : _quests.values())
		{
			q.saveGlobalData();
		}
	}
	
	public final Quest getQuest(String name)
	{
		return _quests.get(name);
	}
	
	public final Quest getQuest(int questId)
	{
		for (Quest q : _quests.values())
		{
			if (q.getQuestIntId() == questId)
			{
				return q;
			}
		}
		return null;
	}
	
	public final void addQuest(Quest newQuest)
	{
		if (newQuest == null)
		{
			throw new IllegalArgumentException("Quest argument cannot be null");
		}
		Quest old = _quests.get(newQuest.getName());
		
		// FIXME: unloading the old quest at this point is a tad too late.
		// the new quest has already initialized itself and read the data, starting
		// an unpredictable number of tasks with that data. The old quest will now
		// save data which will never be read.
		// However, requesting the newQuest to re-read the data is not necessarily a
		// good option, since the newQuest may have already started timers, spawned NPCs
		// or taken any other action which it might re-take by re-reading the data.
		// the current solution properly closes the running tasks of the old quest but
		// ignores the data; perhaps the least of all evils...
		if (old != null)
		{
			old.unload();
			_log.info(getClass().getSimpleName() + ": Replaced: (" + old.getName() + ") with a new version (" + newQuest.getName() + ")");
			
		}
		_quests.put(newQuest.getName(), newQuest);
	}
	
	public final boolean removeQuest(Quest q)
	{
		return _quests.remove(q.getName()) != null;
	}
	
	@Override
	public Iterable<Quest> getAllManagedScripts()
	{
		return _quests.values();
	}
	
	@Override
	public boolean unload(Quest ms)
	{
		ms.saveGlobalData();
		return removeQuest(ms);
	}
	
	@Override
	public String getScriptManagerName()
	{
		return getClass().getSimpleName();
	}
	
	public static final QuestManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final QuestManager _instance = new QuestManager();
	}
}