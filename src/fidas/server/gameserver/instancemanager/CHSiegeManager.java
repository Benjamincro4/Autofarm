package fidas.server.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import fidas.server.Config;
import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.model.L2Clan;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import fidas.server.gameserver.model.entity.clanhall.SiegableHall;
import fidas.server.gameserver.model.zone.type.L2ClanHallZone;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.SystemMessage;

public final class CHSiegeManager
{
	private static final Logger _log = Logger.getLogger(CHSiegeManager.class.getName());
	private static final String SQL_LOAD_HALLS = "SELECT * FROM siegable_clanhall";
	
	private final FastMap<Integer, SiegableHall> _siegableHalls = new FastMap<>();
	
	protected CHSiegeManager()
	{
		loadClanHalls();
	}
	
	private final void loadClanHalls()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(SQL_LOAD_HALLS))
		{
			_siegableHalls.clear();
			
			while (rs.next())
			{
				final int id = rs.getInt("clanHallId");
				
				StatsSet set = new StatsSet();
				
				set.set("id", id);
				set.set("name", rs.getString("name"));
				set.set("ownerId", rs.getInt("ownerId"));
				set.set("desc", rs.getString("desc"));
				set.set("location", rs.getString("location"));
				set.set("nextSiege", rs.getLong("nextSiege"));
				set.set("siegeLenght", rs.getLong("siegeLenght"));
				set.set("scheduleConfig", rs.getString("schedule_config"));
				SiegableHall hall = new SiegableHall(set);
				_siegableHalls.put(id, hall);
				ClanHallManager.addClanHall(hall);
			}
			_log.info(getClass().getSimpleName() + ": " + _siegableHalls.size() + " conquerable clan halls.");
		}
		catch (Exception e)
		{
			_log.warning("CHSiegeManager: Could not load siegable clan halls!:");
		}
	}
	
	public FastMap<Integer, SiegableHall> getConquerableHalls()
	{
		return _siegableHalls;
	}
	
	public SiegableHall getSiegableHall(int clanHall)
	{
		return getConquerableHalls().get(clanHall);
	}
	
	public final SiegableHall getNearbyClanHall(L2Character activeChar)
	{
		return getNearbyClanHall(activeChar.getX(), activeChar.getY(), 10000);
	}
	
	public final SiegableHall getNearbyClanHall(int x, int y, int maxDist)
	{
		L2ClanHallZone zone = null;
		
		for (Map.Entry<Integer, SiegableHall> ch : _siegableHalls.entrySet())
		{
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
			{
				return ch.getValue();
			}
		}
		return null;
	}
	
	public final ClanHallSiegeEngine getSiege(L2Character character)
	{
		SiegableHall hall = getNearbyClanHall(character);
		if (hall == null)
		{
			return null;
		}
		return hall.getSiege();
	}
	
	public final void registerClan(L2Clan clan, SiegableHall hall, L2PcInstance player)
	{
		if (clan.getLevel() < Config.CHS_CLAN_MINLEVEL)
		{
			player.sendMessage("Only clans of level " + Config.CHS_CLAN_MINLEVEL + " or higher may register for a castle siege");
		}
		else if (hall.isWaitingBattle())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED);
			sm.addString(hall.getName());
			player.sendPacket(sm);
		}
		else if (hall.isInSiege())
		{
			player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
		}
		else if (hall.getOwnerId() == clan.getClanId())
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		}
		else if ((clan.getCastleId() != 0) || (clan.getHideoutId() != 0))
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
		}
		else if (hall.getSiege().checkIsAttacker(clan))
		{
			player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
		}
		else if (isClanParticipating(clan))
		{
			player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
		}
		else if (hall.getSiege().getAttackers().size() >= Config.CHS_MAX_ATTACKERS)
		{
			player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
		}
		else
		{
			hall.addAttacker(clan);
		}
	}
	
	public final void unRegisterClan(L2Clan clan, SiegableHall hall)
	{
		if (!hall.isRegistering())
		{
			return;
		}
		hall.removeAttacker(clan);
	}
	
	public final boolean isClanParticipating(L2Clan clan)
	{
		for (SiegableHall hall : getConquerableHalls().values())
		{
			if ((hall.getSiege() != null) && hall.getSiege().checkIsAttacker(clan))
			{
				return true;
			}
		}
		return false;
	}
	
	public final void onServerShutDown()
	{
		for (SiegableHall hall : getConquerableHalls().values())
		{
			// Rainbow springs has his own attackers table
			if ((hall.getId() == 62) || (hall.getSiege() == null))
			{
				continue;
			}
			
			hall.getSiege().saveAttackers();
		}
	}
	
	public static CHSiegeManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static final class SingletonHolder
	{
		protected static final CHSiegeManager _instance = new CHSiegeManager();
	}
}