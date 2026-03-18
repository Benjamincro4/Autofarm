package fidas.server.gameserver.taskmanager.tasks;

import fidas.server.gameserver.taskmanager.Task;
import fidas.server.gameserver.taskmanager.TaskManager.ExecutedTask;

public final class TaskCleanUp extends Task
{
	public static final String NAME = "clean_up";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		System.runFinalization();
		System.gc();
	}
}