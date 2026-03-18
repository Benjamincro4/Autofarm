package fidas.server.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.taskmanager.Task;
import fidas.server.gameserver.taskmanager.TaskManager;
import fidas.server.gameserver.taskmanager.TaskTypes;
import fidas.server.gameserver.taskmanager.TaskManager.ExecutedTask;

public class TaskReportPointsRestore extends Task
{
        private static final String NAME = "report_points_restore";
        
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
                        PreparedStatement update = con.prepareStatement("UPDATE accounts SET bot_report_points = 7");
                        update.execute();
                        update.close();
                        System.out.println("Sucessfully restored Bot Report Points for all accounts!");
                }
                catch(SQLException sqle)
                {
                        sqle.printStackTrace();
                }
                finally
                {
                        try {} catch(Exception e) { e.printStackTrace(); }
                }
        }
        
        @Override
        public void initializate()
        {
                super.initializate();
                TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:00:00", "");
        }
}