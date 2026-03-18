package fidas.server.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.taskmanager.Task;
import fidas.server.gameserver.taskmanager.TaskManager;
import fidas.server.gameserver.taskmanager.TaskManager.ExecutedTask;
import fidas.server.gameserver.taskmanager.TaskTypes;

public class TaskDailySkillReuseClean extends Task
{
	private static final String NAME = "daily_skill_clean";
	
	private static final int[] _daily_skills =
	{
		2510,
		22180
	};
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (int skill_id : _daily_skills)
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE skill_id=?;"))
				{
					ps.setInt(1, skill_id);
					ps.execute();
				}
			}
		}
		catch (Exception e)
		{
			_log.severe(getClass().getSimpleName() + ": Could not reset daily skill reuse: " + e);
		}
		_log.info("Daily skill reuse cleaned.");
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}