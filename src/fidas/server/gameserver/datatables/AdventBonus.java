package fidas.server.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import fidas.server.L2DatabaseFactory;

public class AdventBonus
{
	private static Logger _log = Logger.getLogger(AdventBonus.class.getName());
	private static String SQL_RESTORE = "SELECT charId,advent_time,advent_points FROM adventurer_bonus";
	private static String SQL_REPLACE = "REPLACE INTO adventurer_bonus (charId,advent_time,advent_points) VALUES (?,?,?)";
	
	private static class HuntingState
	{
		private int adventTime;
		private int adventPoints;
		
		public HuntingState(int adventTime, int adventPoints)
		{
			super();
			this.adventTime = adventTime;
			this.adventPoints = adventPoints;
		}
		
		public int getAdventTime()
		{
			return adventTime;
		}
		
		public int getAdventPoints()
		{
			return adventPoints;
		}
		
		public void setAdventTime(int adventTime)
		{
			this.adventTime = adventTime;
		}
		
		public void setAdventPoints(int adventPoints)
		{
			this.adventPoints = adventPoints;
		}
	}
	
	private final FastMap<Integer, HuntingState> hunting = new FastMap<>();
	
	public AdventBonus()
	{
		load();
	}
	
	private void load()
	{
		reload();
	}
	
	public void reload()
	{
		hunting.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			PreparedStatement statement = con.prepareStatement(SQL_RESTORE);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int charId = rset.getInt("charId");
				int atime = rset.getInt("advent_time");
				int apoints = rset.getInt("advent_points");
				hunting.put(charId, new HuntingState(atime, apoints));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "[AdventTable]: Could not restore Adventurer Bonus.", e);
		}
	}
	
	public int getAdventTime(int charId)
	{
		HuntingState rec = hunting.get(charId);
		if (rec != null)
		{
			return rec.getAdventTime();
		}
		return -1;
	}
	
	public int getAdventPoints(int charId)
	{
		HuntingState rec = hunting.get(charId);
		if (rec != null)
		{
			return rec.getAdventPoints();
		}
		return 0;
	}
	
	public void setAdventTime(int charId, int time, boolean store)
	{
		HuntingState rec = hunting.get(charId);
		if (rec != null)
		{
			rec.setAdventTime(time);
		}
		else
		{
			rec = new HuntingState(time, 0);
			hunting.put(charId, rec);
		}
		if (store)
		{
			store(rec, charId);
		}
	}
	
	public void setAdventPoints(int charId, int value, boolean store)
	{
		HuntingState rec = hunting.get(charId);
		if (rec != null)
		{
			rec.setAdventPoints(value);
		}
		else
		{
			rec = new HuntingState(0, value);
			hunting.put(charId, rec);
		}
		if (store)
		{
			store(rec, charId);
		}
	}
	
	public static AdventBonus getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private void store(HuntingState rec, int charId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			PreparedStatement statement = con.prepareStatement(SQL_REPLACE);
			storeExecute(rec, charId, statement);
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "[AdventTable]: Could not update Adventurer Bonus for player: " + charId + ".", e);
		}
	}
	
	private void storeExecute(HuntingState rec, int charId, PreparedStatement statement) throws SQLException
	{
		statement.setInt(1, charId);
		statement.setInt(2, rec.getAdventTime());
		statement.setInt(3, rec.getAdventPoints());
		statement.executeUpdate();
	}
	
	public void execRecTask()
	{
		for (HuntingState rec : hunting.values())
		{
			rec.setAdventTime(0);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			PreparedStatement statement = con.prepareStatement(SQL_REPLACE);
			for (Entry<Integer, HuntingState> e : hunting.entrySet())
			{
				HuntingState rec = e.getValue();
				int charId = e.getKey();
				storeExecute(rec, charId, statement);
				statement.clearParameters();
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "[AdventTable]: Could not update Adventurer Bonus tasks.", e);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final AdventBonus _instance = new AdventBonus();
	}
}