package fidas.server.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.InstanceListManager;
import fidas.server.gameserver.model.L2Clan;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.entity.Fort;

public class FortManager implements InstanceListManager
{
	protected static final Logger _log = Logger.getLogger(FortManager.class.getName());
	
	private List<Fort> _forts;
	
	public final int findNearestFortIndex(L2Object obj)
	{
		return findNearestFortIndex(obj, Long.MAX_VALUE);
	}
	
	public final int findNearestFortIndex(L2Object obj, long maxDistance)
	{
		int index = getFortIndex(obj);
		if (index < 0)
		{
			double distance;
			Fort fort;
			for (int i = 0; i < getForts().size(); i++)
			{
				fort = getForts().get(i);
				if (fort == null)
				{
					continue;
				}
				distance = fort.getDistance(obj);
				if (maxDistance > distance)
				{
					maxDistance = (long) distance;
					index = i;
				}
			}
		}
		return index;
	}
	
	public final Fort getFortById(int fortId)
	{
		for (Fort f : getForts())
		{
			if (f.getFortId() == fortId)
			{
				return f;
			}
		}
		return null;
	}
	
	public final Fort getFortByOwner(L2Clan clan)
	{
		for (Fort f : getForts())
		{
			if (f.getOwnerClan() == clan)
			{
				return f;
			}
		}
		return null;
	}
	
	public final Fort getFort(String name)
	{
		for (Fort f : getForts())
		{
			if (f.getName().equalsIgnoreCase(name.trim()))
			{
				return f;
			}
		}
		return null;
	}
	
	public final Fort getFort(int x, int y, int z)
	{
		for (Fort f : getForts())
		{
			if (f.checkIfInZone(x, y, z))
			{
				return f;
			}
		}
		return null;
	}
	
	public final Fort getFort(L2Object activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getFortIndex(int fortId)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if ((fort != null) && (fort.getFortId() == fortId))
			{
				return i;
			}
		}
		return -1;
	}
	
	public final int getFortIndex(L2Object activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getFortIndex(int x, int y, int z)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if ((fort != null) && fort.checkIfInZone(x, y, z))
			{
				return i;
			}
		}
		return -1;
	}
	
	public final List<Fort> getForts()
	{
		if (_forts == null)
		{
			_forts = new FastList<>();
		}
		return _forts;
	}
	
	@Override
	public void loadInstances()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT id FROM fort ORDER BY id"))
		{
			while (rs.next())
			{
				getForts().add(new Fort(rs.getInt("id")));
			}
			
			_log.info(getClass().getSimpleName() + ": " + getForts().size() + " fortress");
			for (Fort fort : getForts())
			{
				fort.getSiege().getSiegeGuardManager().loadSiegeGuard();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: loadFortData(): " + e.getMessage(), e);
		}
	}
	
	@Override
	public void updateReferences()
	{
	}
	
	@Override
	public void activateInstances()
	{
		for (final Fort fort : _forts)
		{
			fort.activateInstance();
		}
	}
	
	public static final FortManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FortManager _instance = new FortManager();
	}
}