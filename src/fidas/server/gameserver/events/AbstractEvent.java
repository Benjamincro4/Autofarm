package fidas.server.gameserver.events;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javolution.util.FastList;
import javolution.util.FastMap;
import fidas.server.gameserver.events.container.EventContainer;
import fidas.server.gameserver.events.container.PlayerContainer;
import fidas.server.gameserver.events.functions.Buffer;
import fidas.server.gameserver.events.functions.Vote;
import fidas.server.gameserver.events.io.Out;
import fidas.server.gameserver.events.model.Clock;
import fidas.server.gameserver.events.model.EventPlayer;
import fidas.server.gameserver.events.model.EventStatus;
import fidas.server.gameserver.events.model.PLoc;
import fidas.server.gameserver.network.serverpackets.ExShowScreenMessage;

public abstract class AbstractEvent
{
	private class AbstractCore implements Runnable
	{
		
		@Override
		public void run()
		{
			
			switch (phase)
			{
				case REGISTER:
					announce("The next event will be: " + Config.getInstance().getString(c.getId(), "eventName"));
					registerCountdown = new RegisterCountdown(Config.getInstance().getInt(0, "registerTime"));
					registerCountdown.start();
					break;
				
				case CHECK:
					if (players.size() < Config.getInstance().getInt(getId(), "minPlayers"))
					{
						announce("Theres not enough participant!");
						
						reset();
						
						if (Config.getInstance().getBoolean(0, "voteEnabled"))
						{
							Vote.getInstance().checkIfCurrent(c);
						}
						
						EventContainer.getInstance().removeEvent(containerId);
						
					}
					else
					{
						announce("Event started!");
						msgToAll("[Event Manager]: You'll be teleported to the event in 10 seconds.");
						if (Config.getInstance().getBoolean(0, "showEscapeEffect"))
						{
							showEscapeEffectOnAll();
						}
						setAbstractPhase(AbstractPhase.START);
						abstractSchedule(10000);
					}
					break;
				
				case START:
					setAbstractPhase(AbstractPhase.RUNNING);
					start();
					break;
				
				case RESET:
					teleBackEveryone();
					if (Config.getInstance().getBoolean(0, "voteEnabled"))
					{
						Vote.getInstance().checkIfCurrent(c);
					}
					reset();
					EventContainer.getInstance().removeEvent(containerId);
					break;
			}
		}
	}
	
	public enum AbstractPhase
	{
		REGISTER,
		CHECK,
		START,
		RUNNING,
		RESET
	}
	
	public class EventClock extends Clock
	{
		
		public EventClock(int time)
		{
			super(time);
		}
		
		@Override
		public void clockBody()
		{
			clockTick();
			scorebartext = getScorebar();
			for (EventPlayer player : getPlayerList())
			{
				player.scorebarPacket(scorebartext);
			}
		}
		
		@Override
		protected void onZero()
		{
			onClockZero();
		}
	}
	
	public class RegisterCountdown extends Clock
	{
		public RegisterCountdown(int time)
		{
			super(time);
		}
		
		@Override
		public void clockBody()
		{
			switch (counter)
			{
				case 1800:
				case 1200:
				case 600:
				case 300:
				case 60:
					announce("" + (counter / 60) + " minutes left to register.");
					break;
				case 30:
				case 10:
				case 5:
					announce("" + counter + " seconds left to register.");
					break;
			}
		}
		
		@Override
		protected void onZero()
		{
			setAbstractPhase(AbstractPhase.CHECK);
			abstractSchedule(1);
			
		}
	}
	
	private class ResurrectorTask implements Runnable
	{
		private final EventPlayer player;
		
		ResurrectorTask(EventPlayer p)
		{
			player = p;
			Out.tpmScheduleGeneral(this, 7000);
		}
		
		@Override
		public void run()
		{
			if ((player != null) && (player.getEvent() != null))
			{
				player.doRevive();
				
				if (Config.getInstance().getBoolean(0, "eventBufferEnabled"))
				{
					Buffer.getInstance().buffPlayer(player);
				}
				
				player.healToMax();
				teleportToTeamPos(player);
			}
			
		}
	}
	
	final AbstractEvent c = this;
	
	protected int containerId;
	
	public int instanceId;
	
	AbstractPhase phase;
	
	private final AbstractCore abstractCore;
	
	RegisterCountdown registerCountdown;
	
	public int eventId;
	
	public FastMap<Integer, Team> teams;
	
	public FastList<EventPlayer> players;
	
	String scorebartext;
	
	public int winnerTeam;
	
	public Random rnd = new Random();
	
	public EventClock clock;
	
	private final Calendar started;
	
	protected EventStatus status;
	
	public abstract void createStatus();
	
	@SuppressWarnings("synthetic-access")
	protected AbstractEvent(int cId)
	{
		containerId = cId;
		instanceId = cId + 9100;
		Out.createInstance(instanceId);
		Out.setPvPInstance(instanceId);
		teams = new FastMap<>();
		players = new FastList<>();
		abstractCore = new AbstractCore();
		started = Calendar.getInstance();
		setAbstractPhase(AbstractPhase.REGISTER);
		abstractSchedule(1);
	}
	
	void abstractSchedule(int time)
	{
		Out.tpmScheduleGeneral(abstractCore, time);
	}
	
	protected void addToResurrector(EventPlayer player)
	{
		new ResurrectorTask(player);
	}
	
	protected void announce(FastList<EventPlayer> list, String text)
	{
		for (EventPlayer player : list)
		{
			player.sendCreatureMessage("[Event] " + text);
		}
	}
	
	public void announce(String text)
	{
		Out.broadcastCreatureSay("[" + Config.getInstance().getString(getId(), "shortName") + "]: " + text);
	}
	
	public boolean canAttack(EventPlayer player, EventPlayer target)
	{
		return true;
	}
	
	private boolean canRegister(EventPlayer player)
	{
		if (player.isInJail())
		{
			player.sendMessage("[Event Manager]: You cant register from jail.");
			return false;
		}
		
		if (player.isInSiege())
		{
			player.sendMessage("[Event Manager]: You cant register while a siege.");
			return false;
		}
		
		if (player.isInDuel())
		{
			player.sendMessage("[Event Manager]: You cant register while you're dueling.");
			return false;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("[Event Manager]: You cant register while you're in the olympiad games.");
			return false;
		}
		
		if (player.getKarma() > 0)
		{
			player.sendMessage("[Event Manager]: You cant register if you have karma.");
			return false;
		}
		
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage("[Event Manager]: You cant register with a cursed weapon.");
			return false;
		}
		if (player.getLevel() > Config.getInstance().getInt(getId(), "maxLvl"))
		{
			player.sendMessage("[Event Manager]: You're higher than the max level allowed.");
			return false;
		}
		if (player.getLevel() < Config.getInstance().getInt(getId(), "minLvl"))
		{
			player.sendMessage("[Event Manager]: You're lower than the min level allowed.");
			return false;
		}
		
		return true;
	}
	
	protected void clockTick()
	{
	}
	
	public int countOfPositiveStatus()
	{
		int count = 0;
		for (EventPlayer player : getPlayerList())
		{
			if (player.getStatus() >= 0)
			{
				count++;
			}
		}
		
		return count;
	}
	
	protected void createNewTeam(int id, String name, int[] color, int[] startPos)
	{
		teams.put(id, new Team(id, name, color, startPos));
	}
	
	public void createPartyOfTeam(int teamId)
	{
		int count = 0;
		
		FastList<EventPlayer> list = new FastList<>();
		
		for (EventPlayer p : players)
		{
			if (p.getMainTeam() == teamId)
			{
				list.add(p);
			}
		}
		
		FastList<EventPlayer> sublist = new FastList<>();
		for (EventPlayer player : list)
		{
			if (((count % 9) == 0) && ((list.size() - count) != 1))
			{
				if (sublist.size() == 0)
				{
					sublist.add(player);
				}
				else
				{
					Out.createParty2(sublist);
					sublist.reset();
					sublist.add(player);
				}
			}
			if ((count % 9) < 9)
			{
				sublist.add(player);
			}
			count++;
		}
	}
	
	public void divideIntoTeams(int number)
	{
		FastList<EventPlayer> temp = new FastList<>(players);
		
		int i = 0;
		while (temp.size() != 0)
		{
			i++;
			EventPlayer player = temp.get(rnd.nextInt(temp.size()));
			player.setMainTeam(i);
			temp.remove(player);
			if (i == number)
			{
				i = 0;
			}
		}
	}
	
	public void dropBomb(EventPlayer player)
	{
	}
	
	protected abstract void endEvent();
	
	public void eventEnded()
	{
		msgToAll("[Event Manager]: You will be teleported back in 10 seconds.");
		
		for (EventPlayer player : players)
		{
			player.restoreTitle();
		}
		
		setAbstractPhase(AbstractPhase.RESET);
		abstractSchedule(10000);
	}
	
	public void startParalize()
	{
		for (EventPlayer player : players)
		{
			if (player.isCastingNow())
			{
				player.abortCast();
			}
			player.setIsParalyzed(true);
			player.sendPacket(new ExShowScreenMessage("Wait until the event starts...", 10000));
		}
	}
	
	public void unParalize()
	{
		for (EventPlayer player : players)
		{
			player.setIsParalyzed(false);
			player.sendPacket(new ExShowScreenMessage("You can sart now!", 2000));
			player.setTitle("Score: 0");
		}
	}
	
	public AbstractPhase getAbstractPhase()
	{
		return phase;
	}
	
	public int getId()
	{
		return eventId;
	}
	
	public FastList<EventPlayer> getPlayerList()
	{
		return players;
	}
	
	public FastList<EventPlayer> getPlayersOfTeam(int team)
	{
		FastList<EventPlayer> list = new FastList<>();
		
		for (EventPlayer player : getPlayerList())
		{
			if (player.getMainTeam() == team)
			{
				list.add(player);
			}
		}
		
		return list;
	}
	
	protected Team getPlayersTeam(EventPlayer player)
	{
		return teams.get(player.getMainTeam());
	}
	
	public FastList<EventPlayer> getPlayersWithStatus(int status)
	{
		FastList<EventPlayer> list = new FastList<>();
		
		for (EventPlayer player : getPlayerList())
		{
			if (player.getStatus() == status)
			{
				list.add(player);
			}
		}
		
		return list;
	}
	
	public EventPlayer getPlayerWithMaxScore()
	{
		EventPlayer max;
		max = players.head().getNext().getValue();
		for (EventPlayer player : players)
		{
			if (player.getScore() > max.getScore())
			{
				max = player;
			}
		}
		
		return max;
	}
	
	public EventPlayer getRandomPlayer()
	{
		FastList<EventPlayer> temp = new FastList<>();
		for (EventPlayer player : players)
		{
			temp.add(player);
		}
		
		return temp.get(rnd.nextInt(temp.size()));
	}
	
	protected EventPlayer getRandomPlayerFromTeam(int team)
	{
		FastList<EventPlayer> temp = new FastList<>();
		for (EventPlayer player : players)
		{
			if (player.getMainTeam() == team)
			{
				temp.add(player);
			}
		}
		
		return temp.get(rnd.nextInt(temp.size()));
	}
	
	public String getRegisterTimeLeft()
	{
		return registerCountdown.getTimeInString();
	}
	
	public Team getTeam(int id)
	{
		return teams.get(id);
	}
	
	public EventClock getClock()
	{
		return clock;
	}
	
	protected abstract String getScorebar();
	
	public int getWinnerTeam()
	{
		FastList<Team> t = new FastList<>();
		
		for (Team team : teams.values())
		{
			if (t.size() == 0)
			{
				t.add(team);
				continue;
			}
			
			if (team.getScore() > t.getFirst().getScore())
			{
				t.clear();
				t.add(team);
				continue;
			}
			if (team.getScore() == t.getFirst().getScore())
			{
				t.add(team);
			}
			
		}
		
		if (t.size() > 1)
		{
			return t.get(rnd.nextInt(t.size())).getId();
		}
		return t.getFirst().getId();
	}
	
	public void giveReward(EventPlayer player)
	{
		FastList<InetAddress> ip = new FastList<>();
		
		FastMap<Integer, Integer> rewardmap = Config.getInstance().getRewards(getId(), "winner");
		
		for (Map.Entry<Integer, Integer> entry : rewardmap.entrySet())
		{
			player.addItem(entry.getKey(), entry.getValue(), true);
		}
		
		List<EventPlayer> losers = new LinkedList<>();
		losers.addAll(getPlayerList());
		losers.remove(player);
		
		FastMap<Integer, Integer> loserRewards = Config.getInstance().getRewards(getId(), "loser");
		
		for (EventPlayer loser : losers)
		{
			InetAddress ipc = player.getInetAddress();
			
			if (ipc == null)
			{
				continue;
			}
			
			if (ip.contains(ipc))
			{
				continue;
			}
			
			ip.add(ipc);
			
			for (Map.Entry<Integer, Integer> entry : loserRewards.entrySet())
			{
				loser.addItem(entry.getKey(), entry.getValue(), true);
			}
		}
	}
	
	public void giveReward(FastList<EventPlayer> players)
	{
		FastList<InetAddress> ip = new FastList<>();
		
		FastMap<Integer, Integer> rewardmap = Config.getInstance().getRewards(getId(), "winner");
		
		for (EventPlayer player : players)
		{
			InetAddress ipc = player.getInetAddress();
			
			if (ipc == null)
			{
				continue;
			}
			
			if (ip.contains(ipc))
			{
				continue;
			}
			
			ip.add(ipc);
			
			for (Map.Entry<Integer, Integer> entry : rewardmap.entrySet())
			{
				player.addItem(entry.getKey(), entry.getValue(), true);
			}
			
		}
		
		List<EventPlayer> losers = new LinkedList<>();
		losers.addAll(getPlayerList());
		losers.remove(players);
		
		FastMap<Integer, Integer> loserRewards = Config.getInstance().getRewards(getId(), "loser");
		
		for (EventPlayer loser : losers)
		{
			InetAddress ipc = loser.getInetAddress();
			
			if (ip.contains(ipc))
			{
				continue;
			}
			
			ip.add(ipc);
			
			for (Map.Entry<Integer, Integer> entry : loserRewards.entrySet())
			{
				loser.addItem(entry.getKey(), entry.getValue(), true);
			}
		}
		
	}
	
	public boolean isRunning()
	{
		if (phase == AbstractPhase.RUNNING)
		{
			return true;
		}
		return false;
	}
	
	public void msgToAll(String text)
	{
		for (EventPlayer player : players)
		{
			player.sendMessage(text);
		}
	}
	
	public int numberOfTeams()
	{
		return teams.size();
	}
	
	protected abstract void onClockZero();
	
	public void onDie(EventPlayer victim, EventPlayer killer)
	{
		return;
	}
	
	public void onHit(EventPlayer actor, EventPlayer target)
	{
	}
	
	public void onKill(EventPlayer victim, EventPlayer killer)
	{
		return;
	}
	
	public void onLogout(EventPlayer player)
	{
		if (players.contains(player))
		{
			removePlayer(player);
		}
		
		player.teleport(player.getOrigLoc(), 0, true, 0);
		player.restoreData();
		PlayerContainer.getInstance().deleteInfo(player);
		
		if (teams.size() == 1)
		{
			if (getPlayerList().size() == 1)
			{
				endEvent();
				return;
			}
		}
		
		if (teams.size() > 1)
		{
			int t = players.head().getNext().getValue().getMainTeam();
			for (EventPlayer p : getPlayerList())
			{
				if (p.getMainTeam() != t)
				{
					return;
				}
			}
			
			endEvent();
			return;
			
		}
	}
	
	public void onSay(int type, EventPlayer player, String text)
	{
		return;
	}
	
	public boolean onTalkNpc(Integer npc, EventPlayer player)
	{
		return false;
	}
	
	public boolean onUseItem(EventPlayer player, Integer item)
	{
		if (Config.getInstance().getRestriction(0, "item").contains(item) || Config.getInstance().getRestriction(getId(), "item").contains(item))
		{
			return false;
		}
		
		if (Out.isPotion(item) && !Config.getInstance().getBoolean(getId(), "allowPotions"))
		{
			return false;
		}
		
		if (Out.isScroll(item))
		{
			return false;
		}
		
		return true;
	}
	
	public boolean onUseMagic(EventPlayer player, Integer skill)
	{
		if (Config.getInstance().getRestriction(0, "skill").contains(skill) || Config.getInstance().getRestriction(getId(), "skill").contains(skill))
		{
			return false;
		}
		
		if (Out.isRestrictedSkill(skill))
		{
			return false;
		}
		
		return true;
	}
	
	private void prepare(EventPlayer player)
	{
		if (player.isCastingNow())
		{
			player.abortCast();
		}
		
		player.setVisible();
		
		player.unsummonPet();
		
		if (Config.getInstance().getBoolean(getId(), "removeBuffs"))
		{
			player.stopAllEffects();
		}
		
		player.removeFromParty();
		
		int[] nameColor = getPlayersTeam(player).getTeamColor();
		player.setNameColor(nameColor[0], nameColor[1], nameColor[2]);
		
		if (Config.getInstance().getBoolean(0, "eventBufferEnabled"))
		{
			Buffer.getInstance().buffPlayer(player);
		}
		
		if (player.isDead())
		{
			player.doRevive();
		}
		
		player.healToMax();
		
		player.broadcastUserInfo();
	}
	
	public void preparePlayers()
	{
		for (EventPlayer player : players)
		{
			prepare(player);
		}
	}
	
	public boolean registerPlayer(Integer player)
	{
		EventPlayer pi = PlayerContainer.getInstance().getPlayer(player);
		if (pi != null)
		{
			pi.sendMessage("[Event Manager]: You are already registered on the event!");
			PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
			return false;
		}
		
		pi = PlayerContainer.getInstance().createInfo(player);
		
		if (Config.getInstance().getBoolean(0, "ipCheckOnRegister"))
		{
			for (EventPlayer p : PlayerContainer.getInstance().getPlayers())
			{
				if (p.getInetAddress().equals(pi.getInetAddress()) && (p.getOwner().getObjectId() == pi.getOwner().getObjectId()))
				{
					pi.sendMessage("[Event Manager]: This IP address is already registered on an event");
					return false;
				}
			}
		}
		
		if (getAbstractPhase() != AbstractPhase.REGISTER)
		{
			pi.sendMessage("[Event Manager]: You can't register now!");
			PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
			return false;
		}
		
		if (Config.getInstance().getBoolean(0, "eventBufferEnabled"))
		{
			if (!Buffer.getInstance().playerHaveTemplate(player))
			{
				pi.sendMessage("[Event Manager]: You have to set a buff template first!");
				Buffer.getInstance().showHtml(player);
				PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
				return false;
			}
		}
		if (canRegister(pi))
		{
			pi.sendMessage("[Event Manager]: You succesfully registered to the event!");
			pi.setEvent(this);
			pi.initOrigInfo();
			players.add(pi);
			return true;
		}
		pi.sendMessage("[Event Manager]: You failed on the registering to the event!");
		PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
		return false;
	}
	
	private void removePlayer(EventPlayer player)
	{
		players.remove(player);
	}
	
	protected void reset()
	{
		for (EventPlayer p : players)
		{
			PlayerContainer.getInstance().deleteInfo(p);
		}
		players.clear();
		Out.tpmPurge();
		winnerTeam = 0;
		
		for (Team team : teams.values())
		{
			team.setScore(0);
		}
	}
	
	protected abstract void schedule(int time);
	
	void setAbstractPhase(AbstractPhase p)
	{
		phase = p;
	}
	
	void showEscapeEffectOnAll()
	{
		for (EventPlayer player : PlayerContainer.getInstance().getPlayers())
		{
			player.showEscapeEffect();
		}
	}
	
	public abstract void start();
	
	void teleBackEveryone()
	{
		for (EventPlayer player : getPlayerList())
		{
			player.teleport(player.getOrigLoc(), 0, true, 0);
			player.restoreData();
			player.removeFromParty();
			
			player.broadcastUserInfo();
			if (player.isDead())
			{
				player.doRevive();
			}
		}
		
	}
	
	protected void teleportPlayer(EventPlayer player, int[] coordinates, int instance)
	{
		player.teleport(new PLoc(coordinates[0], coordinates[1], coordinates[2]), 0, true, instance);
	}
	
	public void teleportToTeamPos()
	{
		for (EventPlayer player : players)
		{
			teleportToTeamPos(player);
		}
		
	}
	
	protected void teleportToTeamPos(EventPlayer player)
	{
		int[] pos = Config.getInstance().getPosition(getId(), teams.get(player.getMainTeam()).getName(), 0);
		teleportPlayer(player, pos, instanceId);
	}
	
	private boolean unregisterPlayer(EventPlayer pi)
	{
		if (getAbstractPhase() != AbstractPhase.REGISTER)
		{
			pi.sendMessage("[Event Manager]: You can't unregister now!");
			return false;
		}
		pi.sendMessage("[Event Manager]: You have been succesfully unregistered from the event.");
		PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
		players.remove(pi);
		return true;
	}
	
	public boolean unregisterPlayer(Integer player)
	{
		EventPlayer pi = PlayerContainer.getInstance().getPlayer(player);
		if (pi == null)
		{
			Out.sendMessage(player, "[Event Manager]: You're not registered on the event!");
			return false;
		}
		
		return unregisterPlayer(pi);
	}
	
	public void useCapture(EventPlayer player, Integer base)
	{
	}
	
	public String getStarted()
	{
		return "" + (started.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + started.get(Calendar.HOUR_OF_DAY) : started.get(Calendar.HOUR_OF_DAY)) + ":" + (started.get(Calendar.MINUTE) < 10 ? "0" + started.get(Calendar.MINUTE) : started.get(Calendar.MINUTE));
	}
	
	public EventStatus getStatus()
	{
		return status;
	}
}