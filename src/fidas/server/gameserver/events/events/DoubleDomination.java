package fidas.server.gameserver.events.events;

import javolution.util.FastMap;
import fidas.server.gameserver.events.AbstractEvent;
import fidas.server.gameserver.events.Config;
import fidas.server.gameserver.events.container.NpcContainer;
import fidas.server.gameserver.events.io.Out;
import fidas.server.gameserver.events.model.EventNpc;
import fidas.server.gameserver.events.model.EventPlayer;
import fidas.server.gameserver.events.model.TeamEventStatus;

public class DoubleDomination extends AbstractEvent
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
						unSpawnZones();
						setStatus(EventState.INACTIVE);
						announce("Congratulation! The " + teams.get(winnerTeam).getName() + " team won the event with " + teams.get(winnerTeam).getScore() + " points!");
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
	
	private final FastMap<EventNpc, Integer> zones;
	
	private int time;
	
	private int holder;
	
	@SuppressWarnings("synthetic-access")
	public DoubleDomination(Integer containerId)
	{
		super(containerId);
		eventId = 3;
		createNewTeam(1, "Blue", Config.getInstance().getColor(getId(), "Blue"), Config.getInstance().getPosition(getId(), "Blue", 1));
		createNewTeam(2, "Red", Config.getInstance().getColor(getId(), "Red"), Config.getInstance().getPosition(getId(), "Red", 1));
		task = new Core();
		zones = new FastMap<>();
		winnerTeam = 0;
		time = 0;
		holder = 0;
		clock = new EventClock(Config.getInstance().getInt(getId(), "matchTime"));
	}
	
	@Override
	protected void clockTick()
	{
		int team1 = 0;
		int team2 = 0;
		
		for (EventNpc zone : zones.keySet())
		{
			for (EventPlayer player : getPlayerList())
			{
				switch (player.getMainTeam())
				{
					case 1:
						if (Math.sqrt(player.getPlanDistanceSq(zone.getNpc())) <= Config.getInstance().getInt(getId(), "zoneRadius"))
						{
							team1++;
						}
						break;
					
					case 2:
						if (Math.sqrt(player.getPlanDistanceSq(zone.getNpc())) <= Config.getInstance().getInt(getId(), "zoneRadius"))
						{
							team2++;
						}
						break;
				}
			}
			
			if (team1 > team2)
			{
				zones.getEntry(zone).setValue(1);
			}
			
			if (team2 > team1)
			{
				zones.getEntry(zone).setValue(2);
			}
			
			if (team1 == team2)
			{
				zones.getEntry(zone).setValue(0);
			}
			
			team1 = 0;
			team2 = 0;
		}
		
		if (zones.containsValue(1) && (!zones.containsValue(0) && !zones.containsValue(2)))
		{
			if (holder != 1)
			{
				announce(getPlayerList(), "The " + teams.get(1).getName() + " team captured both zones. Score in 10sec!");
				holder = 1;
				time = 0;
			}
			
			if (time == (Config.getInstance().getInt(getId(), "timeToScore") - 1))
			{
				for (EventPlayer player : getPlayersOfTeam(1))
				{
					player.increaseScore();
				}
				teams.get(1).increaseScore();
				teleportToTeamPos();
				time = 0;
				announce(getPlayerList(), "The " + teams.get(1).getName() + " team scored!");
				holder = 0;
			}
			else
			{
				time++;
			}
			
		}
		else if (zones.containsValue(2) && (!zones.containsValue(0) && !zones.containsValue(1)))
		{
			if (holder != 2)
			{
				announce(getPlayerList(), "The " + teams.get(2).getName() + " team captured both zones. Score in 10sec!");
				holder = 1;
				time = 0;
			}
			
			if (time == (Config.getInstance().getInt(getId(), "timeToScore") - 1))
			{
				for (EventPlayer player : getPlayersOfTeam(2))
				{
					player.increaseScore();
				}
				teams.get(2).increaseScore();
				teleportToTeamPos();
				time = 0;
				announce(getPlayerList(), "The " + teams.get(2).getName() + " team scored!");
				holder = 0;
			}
			else
			{
				time++;
			}
		}
		else
		{
			if (holder != 0)
			{
				announce(getPlayerList(), "Canceled!");
			}
			
			holder = 0;
			time = 0;
		}
		
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
		addToResurrector(victim);
	}
	
	@Override
	public void schedule(int time)
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
		int[] z1pos = Config.getInstance().getPosition(getId(), "Zone", 1);
		int[] z2pos = Config.getInstance().getPosition(getId(), "Zone", 2);
		zones.put(NpcContainer.getInstance().createNpc(z1pos[0], z1pos[1], z1pos[2], Config.getInstance().getInt(getId(), "zoneNpcId"), instanceId), 0);
		zones.put(NpcContainer.getInstance().createNpc(z2pos[0], z2pos[1], z2pos[2], Config.getInstance().getInt(getId(), "zoneNpcId"), instanceId), 0);
		setStatus(EventState.START);
		schedule(1);
	}
	
	void unSpawnZones()
	{
		for (EventNpc s : zones.keySet())
		{
			s.unspawn();
			zones.remove(s);
		}
	}
	
	@Override
	public void createStatus()
	{
		status = new TeamEventStatus(containerId);
	}
}