package fidas.server.gameserver.events.events;

import fidas.server.gameserver.events.AbstractEvent;
import fidas.server.gameserver.events.Config;
import fidas.server.gameserver.events.container.NpcContainer;
import fidas.server.gameserver.events.io.Out;
import fidas.server.gameserver.events.model.EventNpc;
import fidas.server.gameserver.events.model.EventPlayer;
import fidas.server.gameserver.events.model.TeamEventStatus;

public class CTF extends AbstractEvent
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
						spawnFlagsAndHolders();
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
						unspawnFlagsAndHolders();
						if (playerWithRedFlag != null)
						{
							unequipFlag(playerWithRedFlag);
						}
						if (playerWithBlueFlag != null)
						{
							unequipFlag(playerWithBlueFlag);
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
	
	private EventNpc redFlagNpc;
	private EventNpc blueFlagNpc;
	private EventNpc redHolderNpc;
	private EventNpc blueHolderNpc;
	private int redFlagStatus;
	private int blueFlagStatus;
	EventPlayer playerWithRedFlag;
	EventPlayer playerWithBlueFlag;
	
	@SuppressWarnings("synthetic-access")
	public CTF(Integer containerId)
	{
		super(containerId);
		eventId = 10;
		createNewTeam(1, "Blue", Config.getInstance().getColor(getId(), "Blue"), Config.getInstance().getPosition(getId(), "Blue", 1));
		createNewTeam(2, "Red", Config.getInstance().getColor(getId(), "Red"), Config.getInstance().getPosition(getId(), "Red", 1));
		task = new Core();
		winnerTeam = 0;
		playerWithRedFlag = null;
		playerWithBlueFlag = null;
		blueFlagStatus = 0;
		redFlagStatus = 0;
		clock = new EventClock(Config.getInstance().getInt(getId(), "matchTime"));
	}
	
	@Override
	protected void endEvent()
	{
		winnerTeam = players.head().getNext().getValue().getMainTeam();
		
		setStatus(EventState.END);
		clock.stop();
		
	}
	
	private void equipFlag(EventPlayer player, int flag)
	{
		player.unequipWeapon();
		player.equipNewItem(6718);
		
		switch (flag)
		{
			case 1:
				playerWithBlueFlag = player;
				announce(getPlayerList(), player.getName() + " took the Blue flag!");
				blueFlagNpc.unspawn();
				break;
			case 2:
				playerWithRedFlag = player;
				announce(getPlayerList(), player.getName() + " took the Red flag!");
				redFlagNpc.unspawn();
				break;
			default:
				break;
		}
		player.broadcastUserInfo();
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
		if ((playerWithRedFlag != null) && playerWithRedFlag.equals(victim))
		{
			announce(getPlayerList(), victim.getName() + " dropped the Red flag!");
			redFlagStatus = 2;
			unequipFlag(victim);
			redFlagNpc = NpcContainer.getInstance().createNpc(victim.getOwnerLoc().getX(), victim.getOwnerLoc().getY(), victim.getOwnerLoc().getZ(), Config.getInstance().getInt(getId(), "redFlagId"), instanceId);
		}
		if ((playerWithBlueFlag != null) && playerWithBlueFlag.equals(victim))
		{
			announce(getPlayerList(), victim.getName() + " dropped the Blue flag!");
			blueFlagStatus = 2;
			unequipFlag(victim);
			blueFlagNpc = NpcContainer.getInstance().createNpc(victim.getOwnerLoc().getX(), victim.getOwnerLoc().getY(), victim.getOwnerLoc().getZ(), Config.getInstance().getInt(getId(), "blueFlagId"), instanceId);
		}
		
		addToResurrector(victim);
	}
	
	@Override
	public void onKill(EventPlayer victim, EventPlayer killer)
	{
		super.onKill(victim, killer);
		
	}
	
	@Override
	public void onLogout(EventPlayer player)
	{
		super.onLogout(player);
		
		if (playerWithRedFlag.equals(player))
		{
			announce(getPlayerList(), player.getName() + " dropped the Red flag!");
			redFlagStatus = 2;
			unequipFlag(player);
			redFlagNpc = NpcContainer.getInstance().createNpc(player.getOwnerLoc().getX(), player.getOwnerLoc().getY(), player.getOwnerLoc().getZ(), Config.getInstance().getInt(getId(), "redFlagId"), instanceId);
		}
		if (playerWithBlueFlag.equals(player))
		{
			announce(getPlayerList(), player.getName() + " dropped the Blue flag!");
			blueFlagStatus = 2;
			unequipFlag(player);
			blueFlagNpc = NpcContainer.getInstance().createNpc(player.getOwnerLoc().getX(), player.getOwnerLoc().getY(), player.getOwnerLoc().getZ(), Config.getInstance().getInt(getId(), "blueFlagId"), instanceId);
		}
		
	}
	
	@Override
	public boolean onTalkNpc(Integer npcId, EventPlayer player)
	{
		EventNpc npc = NpcContainer.getInstance().getNpc(npcId);
		
		if (npc == null)
		{
			return false;
		}
		
		if (!(npc.equals(blueFlagNpc) || npc.equals(blueHolderNpc) || npc.equals(redFlagNpc) || npc.equals(redHolderNpc)))
		{
			return false;
		}
		if (npc.equals(blueHolderNpc))
		{
			if (player.equals(playerWithRedFlag) && (blueFlagStatus == 0))
			{
				announce(getPlayerList(), "The Blue team scored!");
				teams.get(player.getMainTeam()).increaseScore();
				player.increaseScore();
				returnFlag(2);
			}
		}
		if (npc.equals(redHolderNpc))
		{
			if (player.equals(playerWithBlueFlag) && (redFlagStatus == 0))
			{
				announce(getPlayerList(), "The Red team scored!");
				teams.get(player.getMainTeam()).increaseScore();
				player.increaseScore();
				returnFlag(1);
			}
		}
		if (npc.equals(blueFlagNpc))
		{
			if (blueFlagStatus == 2)
			{
				if (player.getMainTeam() == 1)
				{
					returnFlag(1);
				}
				
				if (player.getMainTeam() == 2)
				{
					equipFlag(player, 1);
				}
			}
			if (blueFlagStatus == 0)
			{
				if (player.getMainTeam() == 2)
				{
					equipFlag(player, 1);
					blueFlagNpc.unspawn();
					blueFlagStatus = 1;
				}
			}
			
		}
		if (npc.equals(redFlagNpc))
		{
			if (redFlagStatus == 2)
			{
				if (player.getMainTeam() == 2)
				{
					returnFlag(2);
				}
				
				if (player.getMainTeam() == 1)
				{
					equipFlag(player, 2);
				}
			}
			if (redFlagStatus == 0)
			{
				if (player.getMainTeam() == 1)
				{
					equipFlag(player, 2);
					redFlagNpc.unspawn();
					redFlagStatus = 1;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean onUseItem(EventPlayer player, Integer item)
	{
		if (((playerWithRedFlag != null) && playerWithRedFlag.equals(player)) || ((playerWithBlueFlag != null) && playerWithBlueFlag.equals(player)))
		{
			return false;
		}
		
		return true;
	}
	
	private void returnFlag(int flag)
	{
		int[] pos;
		switch (flag)
		{
			case 1:
				if (playerWithBlueFlag != null)
				{
					unequipFlag(playerWithBlueFlag);
				}
				if (blueFlagStatus == 2)
				{
					blueFlagNpc.unspawn();
				}
				
				pos = Config.getInstance().getPosition(getId(), "BlueFlag", 1);
				blueFlagNpc = NpcContainer.getInstance().createNpc(pos[0], pos[1], pos[2], Config.getInstance().getInt(getId(), "blueFlagId"), instanceId);
				blueFlagStatus = 0;
				announce(getPlayerList(), "The Blue flag returned!");
				break;
			
			case 2:
				if (playerWithRedFlag != null)
				{
					unequipFlag(playerWithRedFlag);
				}
				if (redFlagStatus == 2)
				{
					redFlagNpc.unspawn();
				}
				
				pos = Config.getInstance().getPosition(getId(), "RedFlag", 1);
				redFlagNpc = NpcContainer.getInstance().createNpc(pos[0], pos[1], pos[2], Config.getInstance().getInt(getId(), "redFlagId"), instanceId);
				redFlagStatus = 0;
				announce(getPlayerList(), "The Red flag returned!");
				break;
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
	
	void spawnFlagsAndHolders()
	{
		int[] pos = Config.getInstance().getPosition(getId(), "BlueFlag", 1);
		blueFlagNpc = NpcContainer.getInstance().createNpc(pos[0], pos[1], pos[2], Config.getInstance().getInt(getId(), "blueFlagId"), instanceId);
		blueHolderNpc = NpcContainer.getInstance().createNpc(pos[0], pos[1], pos[2], Config.getInstance().getInt(getId(), "blueFlagHolderId"), instanceId);
		
		pos = Config.getInstance().getPosition(getId(), "RedFlag", 1);
		redFlagNpc = NpcContainer.getInstance().createNpc(pos[0], pos[1], pos[2], Config.getInstance().getInt(getId(), "redFlagId"), instanceId);
		redHolderNpc = NpcContainer.getInstance().createNpc(pos[0], pos[1], pos[2], Config.getInstance().getInt(getId(), "redFlagHolderId"), instanceId);
	}
	
	@Override
	public void start()
	{
		setStatus(EventState.START);
		schedule(1);
	}
	
	void unequipFlag(EventPlayer player)
	{
		player.unequipAndRemove(6718);
		
		if (player.equals(playerWithRedFlag))
		{
			playerWithRedFlag = null;
		}
		if (player.equals(playerWithBlueFlag))
		{
			playerWithBlueFlag = null;
		}
	}
	
	void unspawnFlagsAndHolders()
	{
		blueFlagNpc.unspawn();
		blueHolderNpc.unspawn();
		redFlagNpc.unspawn();
		redHolderNpc.unspawn();
	}
	
	@Override
	public void createStatus()
	{
		status = new TeamEventStatus(containerId);
	}
}