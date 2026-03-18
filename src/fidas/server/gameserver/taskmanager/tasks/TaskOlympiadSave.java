package fidas.server.gameserver.taskmanager.tasks;

import fidas.server.gameserver.model.olympiad.Olympiad;
import fidas.server.gameserver.taskmanager.Task;
import fidas.server.gameserver.taskmanager.TaskManager;
import fidas.server.gameserver.taskmanager.TaskManager.ExecutedTask;
import fidas.server.gameserver.taskmanager.TaskTypes;

public class TaskOlympiadSave extends Task
{
	public static final String NAME = "olympiad_save";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		if (Olympiad.getInstance().inCompPeriod())
		{
			Olympiad.getInstance().saveOlympiadStatus();
			_log.info("Olympiad System: Data updated.");
		}
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "900000", "1800000", "");
	}
}