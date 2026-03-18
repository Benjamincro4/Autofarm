package fidas.server.gameserver.taskmanager.tasks;

import fidas.server.gameserver.Shutdown;
import fidas.server.gameserver.taskmanager.Task;
import fidas.server.gameserver.taskmanager.TaskManager.ExecutedTask;

public final class TaskRestart extends Task
{
	public static final String NAME = "restart";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		Shutdown handler = new Shutdown(Integer.parseInt(task.getParams()[2]), true);
		handler.start();
	}
}