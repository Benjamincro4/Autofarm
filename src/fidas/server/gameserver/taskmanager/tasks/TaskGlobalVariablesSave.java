package fidas.server.gameserver.taskmanager.tasks;

import fidas.server.gameserver.instancemanager.GlobalVariablesManager;
import fidas.server.gameserver.taskmanager.Task;
import fidas.server.gameserver.taskmanager.TaskManager;
import fidas.server.gameserver.taskmanager.TaskManager.ExecutedTask;
import fidas.server.gameserver.taskmanager.TaskTypes;

public class TaskGlobalVariablesSave extends Task
{
	public static final String NAME = "global_varibales_save";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		GlobalVariablesManager.getInstance().saveVars();
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "500000", "1800000", "");
	}
}