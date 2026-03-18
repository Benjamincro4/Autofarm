package fidas.server.gameserver.events.events;

import fidas.server.gameserver.events.AbstractEvent;
import fidas.server.gameserver.events.Config;
import fidas.server.gameserver.events.io.Out;
import fidas.server.gameserver.events.model.EventPlayer;
import fidas.server.gameserver.events.model.SingleEventStatus;

public class Mutant extends AbstractEvent
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
						transformMutant(getRandomPlayer());
						setStatus(EventState.END);
						clock.start();
						break;
					
					case END:
						clock.stop();
						untransformMutant();
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
	
	private EventPlayer mutant;
	
	@SuppressWarnings("synthetic-access")
	public Mutant(Integer containerId)
	{
		super(containerId);
		eventId = 13;
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
	
	/*
	 * (non-Javadoc)
	 * @see net.phoenixengine.AbstractEvent#onClockZero()
	 */
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
		if (killer.getStatus() == 1)
		{
			killer.increaseScore();
		}
		if ((killer.getStatus() == 0) && (victim.getStatus() == 1))
		{
			transformMutant(killer);
		}
	}
	
	@Override
	public void onLogout(EventPlayer player)
	{
		super.onLogout(player);
		
		if (mutant.equals(player))
		{
			transformMutant(getRandomPlayer());
		}
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
	
	void transformMutant(EventPlayer player)
	{
		player.setNameColor(255, 0, 0);
		player.transform(303);
		player.addSkill(Config.getInstance().getInt(getId(), "mutantBuffId"), 1);
		player.setStatus(1);
		untransformMutant();
		player.broadcastUserInfo();
		mutant = player;
		
	}
	
	void untransformMutant()
	{
		if (mutant != null)
		{
			mutant.setNameColor(Config.getInstance().getColor(getId(), "All")[0], Config.getInstance().getColor(getId(), "All")[1], Config.getInstance().getColor(getId(), "All")[2]);
			mutant.untransform();
			mutant.removeSkill(Config.getInstance().getInt(getId(), "mutantBuffId"), 1);
			mutant.setStatus(0);
			mutant.broadcastUserInfo();
			mutant = null;
		}
	}
	
	@Override
	public void createStatus()
	{
		status = new SingleEventStatus(containerId);
	}
}