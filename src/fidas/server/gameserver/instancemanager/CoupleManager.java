package fidas.server.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Couple;

public class CoupleManager
{
	private static final Logger _log = Logger.getLogger(CoupleManager.class.getName());
	
	private FastList<Couple> _couples;
	
	protected CoupleManager()
	{
		load();
	}
	
	public void reload()
	{
		getCouples().clear();
		load();
	}
	
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement ps = con.createStatement();
			ResultSet rs = ps.executeQuery("SELECT id FROM mods_wedding ORDER BY id"))
		{
			while (rs.next())
			{
				getCouples().add(new Couple(rs.getInt("id")));
			}
			_log.info(getClass().getSimpleName() + ": " + getCouples().size() + " couples(s)");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: CoupleManager.load(): " + e.getMessage(), e);
		}
	}
	
	public final Couple getCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		if (index >= 0)
		{
			return getCouples().get(index);
		}
		return null;
	}
	
	public void createCouple(L2PcInstance player1, L2PcInstance player2)
	{
		if ((player1 != null) && (player2 != null))
		{
			if ((player1.getPartnerId() == 0) && (player2.getPartnerId() == 0))
			{
				int _player1id = player1.getObjectId();
				int _player2id = player2.getObjectId();
				
				Couple _new = new Couple(player1, player2);
				getCouples().add(_new);
				player1.setPartnerId(_player2id);
				player2.setPartnerId(_player1id);
				player1.setCoupleId(_new.getId());
				player2.setCoupleId(_new.getId());
			}
		}
	}
	
	public void deleteCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		Couple couple = getCouples().get(index);
		if (couple != null)
		{
			L2PcInstance player1 = L2World.getInstance().getPlayer(couple.getPlayer1Id());
			L2PcInstance player2 = L2World.getInstance().getPlayer(couple.getPlayer2Id());
			if (player1 != null)
			{
				player1.setPartnerId(0);
				player1.setMarried(false);
				player1.setCoupleId(0);
				
			}
			if (player2 != null)
			{
				player2.setPartnerId(0);
				player2.setMarried(false);
				player2.setCoupleId(0);
				
			}
			couple.divorce();
			getCouples().remove(index);
		}
	}
	
	public final int getCoupleIndex(int coupleId)
	{
		int i = 0;
		for (Couple temp : getCouples())
		{
			if ((temp != null) && (temp.getId() == coupleId))
			{
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public final FastList<Couple> getCouples()
	{
		if (_couples == null)
		{
			_couples = new FastList<>();
		}
		return _couples;
	}
	
	public static final CoupleManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CoupleManager _instance = new CoupleManager();
	}
}