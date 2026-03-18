package fidas.server.gameserver.events.events;

import javolution.util.FastMap;
import fidas.server.gameserver.events.AbstractEvent;
import fidas.server.gameserver.events.Config;
import fidas.server.gameserver.events.io.Out;
import fidas.server.gameserver.events.model.EventPlayer;
import fidas.server.gameserver.events.model.TeamEventStatus;

public class VIPTvT extends AbstractEvent
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
						divideIntoTeams(2);
						teleportToTeamPos();
						preparePlayers();
						createPartyOfTeam(1);
						createPartyOfTeam(2);
						selectNewVipOfTeam(1);
						selectNewVipOfTeam(2);
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
						if (winnerTeam == 0)
						{
							winnerTeam = getWinnerTeam();
						}
						
						giveReward(getPlayersOfTeam(winnerTeam));
						setStatus(EventState.INACTIVE);
						announce("Congratulation! The " + teams.get(winnerTeam).getName() + " team won the event with " + teams.get(winnerTeam).getScore() + " kills!");
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
		TELEPORT,
		INACTIVE
	}
	
	EventState eventState;
	
	private final Core task;
	
	private final FastMap<Integer, EventPlayer> vips;
	
	@SuppressWarnings("synthetic-access")
	public VIPTvT(Integer containerId)
	{
		super(containerId);
		eventId = 8;
		createNewTeam(1, "Blue", Config.getInstance().getColor(getId(), "Blue"), Config.getInstance().getPosition(getId(), "Blue", 1));
		createNewTeam(2, "Red", Config.getInstance().getColor(getId(), "Red"), Config.getInstance().getPosition(getId(), "Red", 1));
		task = new Core();
		vips = new FastMap<>();
		winnerTeam = 0;
		clock = new EventClock(Config.getInstance().getInt(getId(), "matchTime"));
	}
	
	@Override
	protected void endEvent()
	{
		winnerTeam = players.head().getNext().getValue().getMainTeam();
		
		setStatus(EventState.END);
		clock.stop();
		
	}
	
	@Override
	protected String getScorebar()
	{
		return "" + teams.get(1).getName() + ": " + teams.get(1).getScore() + "  " + teams.get(2).getName() + ": " + teams.get(2).getScore() + "  Time: " + clock.getTimeInString();
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
		if (vips.get(1).equals(victim))
		{
			teams.get(2).increaseScore();
			killer.increaseScore();
			selectNewVipOfTeam(1);
		}
		if (vips.get(2).equals(victim))
		{
			teams.get(1).increaseScore();
			killer.increaseScore();
			selectNewVipOfTeam(2);
		}
		
		addToResurrector(victim);
	}
	
	@Override
	public void onLogout(EventPlayer player)
	{
		super.onLogout(player);
		
		if (vips.get(1).equals(player))
		{
			selectNewVipOfTeam(1);
		}
		if (vips.get(2).equals(player))
		{
			selectNewVipOfTeam(2);
		}
		
	}
	
	@Override
	public void schedule(int time)
	{
		Out.tpmScheduleGeneral(task, time);
	}
	
	void selectNewVipOfTeam(int team)
	{
		if (vips.get(team) != null)
		{
			int[] nameColor = teams.get(vips.get(team).getMainTeam()).getTeamColor();
			vips.get(team).setNameColor(nameColor[0], nameColor[1], nameColor[2]);
		}
		
		EventPlayer newvip = getRandomPlayerFromTeam(team);
		vips.getEntry(team).setValue(newvip);
		if (team == 1)
		{
			int[] c = Config.getInstance().getColor(getId(), "BlueVIP");
			newvip.setNameColor(c[0], c[1], c[2]);
		}
		
		if (team == 2)
		{
			int[] c = Config.getInstance().getColor(getId(), "RedVIP");
			newvip.setNameColor(c[0], c[1], c[2]);
		}
		
		if (!newvip.isDead())
		{
			newvip.healToMax();
		}
		
		newvip.broadcastUserInfo();
		
	}
	
	void setStatus(EventState s)
	{
		eventState = s;
	}
	
	@Override
	public void start()
	{
		vips.put(1, null);
		vips.put(2, null);
		setStatus(EventState.START);
		schedule(1);
	}
	
	@Override
	public void createStatus()
	{
		status = new TeamEventStatus(containerId);
	}
}