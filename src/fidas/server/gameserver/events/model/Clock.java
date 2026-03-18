package fidas.server.gameserver.events.model;

import fidas.server.gameserver.events.io.Out;

public abstract class Clock implements Runnable
{
	protected int counter;
	
	public Clock(int time)
	{
		counter = time;
	}
	
	public abstract void clockBody();
	
	public int getTimeInInt()
	{
		return counter;
	}
	
	public String getTimeInString()
	{
		String mins = "" + (counter / 60);
		String secs = ((counter % 60) < 10 ? "0" + (counter % 60) : "" + (counter % 60));
		return "" + mins + ":" + secs + "";
	}
	
	protected abstract void onZero();
	
	@Override
	public void run()
	{
		clockBody();
		
		if (counter == 0)
		{
			onZero();
		}
		else
		{
			counter--;
			Out.tpmScheduleGeneral(this, 1000);
		}
	}
	
	public void start()
	{
		Out.tpmScheduleGeneral(this, 1);
	}
	
	public void stop()
	{
		counter = 0;
	}
}