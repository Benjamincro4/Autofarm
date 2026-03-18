package fidas.server.gameserver.events.events;

import fidas.server.gameserver.events.AbstractEvent;
import fidas.server.gameserver.events.Config;
import fidas.server.gameserver.events.io.Out;
import fidas.server.gameserver.events.model.EventPlayer;
import fidas.server.gameserver.events.model.SingleEventStatus;

public class DM extends AbstractEvent
{
	public static boolean enabled = true;
	
	private class Core implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				switch (eventState)
				{
					case START:
						divideIntoTeams(1);
						teleportToTeamPos();
						preparePlayers();
						startParalize();
						setStatus(EventState.FIGHT);
						schedule(10000);
						break;
					
					case FIGHT:
						unParalize();
						setStatus(EventState.END);
						clock.start();
						break;
					
					case END:
						clock.stop();
						EventPlayer winner = getPlayerWithMaxScore();
						giveReward(winner);
						setStatus(EventState.INACTIVE);
						announce("Congratulation! " + winner.getName() + " won the event with " + winner.getScore() + " kills!");
						eventEnded();
						break;
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				announce("Error! Event ended.");
				eventEnded();
			}
		}
	}
	
	private enum EventState
	{
		START,
		FIGHT,
		END,
		INACTIVE
	}
	
	EventState eventState;
	
	private final Core task;
	
	@SuppressWarnings("synthetic-access")
	public DM(Integer containerId)
	{
		super(containerId);
		eventId = 1;
		createNewTeam(1, "All", Config.getInstance().getColor(getId(), "All"), Config.getInstance().getPosition(getId(), "All", 1));
		task = new Core();
		clock = new EventClock(Config.getInstance().getInt(getId(), "matchTime"));
	}
	
	@Override
	protected void endEvent()
	{
		setStatus(EventState.END);
		clock.stop();
		
	}
	
	@Override
	protected String getScorebar()
	{
		return "Max: " + getPlayerWithMaxScore().getScore() + "  Time: " + clock.getTimeInString() + "";
	}
	
	@Override
	protected void onClockZero()
	{
		setStatus(EventState.END);
		schedule(1);
	}
	
	@Override
	public void onDie(EventPlayer victim, EventPlayer killer)
	{
		super.onDie(victim, killer);
		addToResurrector(victim);
	}
	
	@Override
	public void onKill(EventPlayer victim, EventPlayer killer)
	{
		super.onKill(victim, killer);
		killer.increaseScore();
	}
	
	@Override
	protected void schedule(int time)
	{
		Out.tpmScheduleGeneral(task, time);
	}
	
	void setStatus(EventState s)
	{
		eventState = s;
	}
	
	@Override
	public void start()
	{
		setStatus(EventState.START);
		schedule(1);
	}
	
	@Override
	public void createStatus()
	{
		status = new SingleEventStatus(containerId);
	}
}