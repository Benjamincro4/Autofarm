package fidas.server.gameserver.taskmanager.tasks;

import java.util.Calendar;

import fidas.server.gameserver.instancemanager.SoIManager;
import fidas.server.gameserver.taskmanager.Task;
import fidas.server.gameserver.taskmanager.TaskManager;
import fidas.server.gameserver.taskmanager.TaskManager.ExecutedTask;
import fidas.server.gameserver.taskmanager.TaskTypes;

public class SoIStageUpdater extends Task
{
	private static final String NAME = "soi_update";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
		{
			SoIManager.setCurrentStage(1);
			_log.info("Seed of Infinity update Task: Seed updated successfuly.");
		}
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(getName(), TaskTypes.TYPE_GLOBAL_TASK, "1", "12:00:00", "");
	}
}