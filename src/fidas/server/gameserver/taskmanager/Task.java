package fidas.server.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import fidas.server.gameserver.taskmanager.TaskManager.ExecutedTask;

public abstract class Task
{
	protected final Logger _log = Logger.getLogger(getClass().getName());
	
	public void initializate()
	{
	}
	
	public ScheduledFuture<?> launchSpecial(ExecutedTask instance)
	{
		return null;
	}
	
	public abstract String getName();
	
	public abstract void onTimeElapsed(ExecutedTask task);
	
	public void onDestroy()
	{
	}
}