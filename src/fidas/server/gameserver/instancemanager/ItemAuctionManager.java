package fidas.server.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.Config;
import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.model.itemauction.ItemAuctionInstance;

import gnu.trove.map.hash.TIntObjectHashMap;

public final class ItemAuctionManager
{
	private static final Logger _log = Logger.getLogger(ItemAuctionManager.class.getName());
	
	private final TIntObjectHashMap<ItemAuctionInstance> _managerInstances;
	private final AtomicInteger _auctionIds;
	
	protected ItemAuctionManager()
	{
		_managerInstances = new TIntObjectHashMap<>();
		_auctionIds = new AtomicInteger(1);
		
		if (!Config.ALT_ITEM_AUCTION_ENABLED)
		{
			_log.log(Level.INFO, getClass().getSimpleName() + ": Desativado.");
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement();
			ResultSet rset = statement.executeQuery("SELECT auctionId FROM item_auction ORDER BY auctionId DESC LIMIT 0, 1"))
		{
			if (rset.next())
			{
				_auctionIds.set(rset.getInt(1) + 1);
			}
		}
		catch (final SQLException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed loading auctions.", e);
		}
		
		final File file = new File(Config.DATAPACK_ROOT + "/data/ItemAuctions.xml");
		if (!file.exists())
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Missing ItemAuctions.xml!");
			return;
		}
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		try
		{
			final Document doc = factory.newDocumentBuilder().parse(file);
			for (Node na = doc.getFirstChild(); na != null; na = na.getNextSibling())
			{
				if ("list".equalsIgnoreCase(na.getNodeName()))
				{
					for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if ("instance".equalsIgnoreCase(nb.getNodeName()))
						{
							final NamedNodeMap nab = nb.getAttributes();
							final int instanceId = Integer.parseInt(nab.getNamedItem("id").getNodeValue());
							
							if (_managerInstances.containsKey(instanceId))
							{
								throw new Exception("Dublicated instanceId " + instanceId);
							}
							
							final ItemAuctionInstance instance = new ItemAuctionInstance(instanceId, _auctionIds, nb);
							_managerInstances.put(instanceId, instance);
						}
					}
				}
			}
			_log.log(Level.INFO, getClass().getSimpleName() + ": " + _managerInstances.size() + " instance(s).");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed loading auctions from xml.", e);
		}
	}
	
	public final void shutdown()
	{
		final ItemAuctionInstance[] instances = _managerInstances.values(new ItemAuctionInstance[0]);
		for (final ItemAuctionInstance instance : instances)
		{
			instance.shutdown();
		}
	}
	
	public final ItemAuctionInstance getManagerInstance(final int instanceId)
	{
		return _managerInstances.get(instanceId);
	}
	
	public final int getNextAuctionId()
	{
		return _auctionIds.getAndIncrement();
	}
	
	public static final void deleteAuction(final int auctionId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?"))
			{
				statement.setInt(1, auctionId);
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?"))
			{
				statement.setInt(1, auctionId);
				statement.execute();
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "L2ItemAuctionManagerInstance: Failed deleting auction: " + auctionId, e);
		}
	}
	
	public static final ItemAuctionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemAuctionManager _instance = new ItemAuctionManager();
	}
}