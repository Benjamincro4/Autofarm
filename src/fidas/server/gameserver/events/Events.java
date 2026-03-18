package fidas.server.gameserver.events;

import fidas.server.gameserver.events.functions.Buffer;
import fidas.server.gameserver.events.functions.Scheduler;
import fidas.server.gameserver.events.functions.Vote;
import fidas.server.gameserver.events.io.Out;

public class Events
{
	public static void eventStart()
	{
		Config.getInstance();
		
		if (Config.getInstance().getBoolean(0, "voteEnabled"))
		{
			Vote.getInstance();
		}
		if (Config.getInstance().getBoolean(0, "schedulerEnabled"))
		{
			Scheduler.getInstance();
		}
		if (Config.getInstance().getBoolean(0, "eventBufferEnabled"))
		{
			Buffer.getInstance();
		}
		
		Out.registerHandlers();
	}
}