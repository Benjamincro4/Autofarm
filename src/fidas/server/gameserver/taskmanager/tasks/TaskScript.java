package fidas.server.gameserver.taskmanager.tasks;

import java.io.File;

import javax.script.ScriptException;

import fidas.server.gameserver.scripting.L2ScriptEngineManager;
import fidas.server.gameserver.taskmanager.Task;
import fidas.server.gameserver.taskmanager.TaskManager.ExecutedTask;

public class TaskScript extends Task
{
	public static final String NAME = "script";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		final File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, "cron/" + task.getParams()[2]);
		if (file.isFile())
		{
			try
			{
				L2ScriptEngineManager.getInstance().executeScript(file);
			}
			catch (ScriptException e)
			{
				_log.warning("Failed loading: " + task.getParams()[2]);
				L2ScriptEngineManager.getInstance().reportScriptFileError(file, e);
			}
			catch (Exception e)
			{
				_log.warning("Failed loading: " + task.getParams()[2]);
			}
		}
		else
		{
			_log.warning("File Not Found: " + task.getParams()[2]);
		}
	}
}