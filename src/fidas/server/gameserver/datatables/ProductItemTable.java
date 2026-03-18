package fidas.server.gameserver.datatables;

import java.sql.*;
import java.util.*;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.model.L2ProductItem;
import fidas.server.gameserver.model.L2ProductItemComponent;
import fidas.server.gameserver.model.actor.instance.L2PetInstance;

public class ProductItemTable
{
	private static java.util.logging.Logger _log = java.util.logging.Logger.getLogger(L2PetInstance.class.getName());
	private Map<Integer, L2ProductItem> _itemsList = new TreeMap<>();

	public static final ProductItemTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private ProductItemTable()
	{
		load();

		_log.info(String.format("ProductItemTable: %d product item", _itemsList.size()));
	}

	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM product_items ORDER BY product_id");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int productId = rset.getInt("product_id");
				int category = rset.getInt("category");
				int points = rset.getInt("points");

				L2ProductItem pr = new L2ProductItem(productId,category, points);
				pr.setComponents(loadComponents(productId));

				_itemsList.put(productId, pr);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Exception: error while loading product items "  + e);
		}
	}

	private ArrayList<L2ProductItemComponent> loadComponents(int product_id)
	{
		ArrayList<L2ProductItemComponent> a = new ArrayList<>();

			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM product_item_components WHERE product_id=" + product_id);
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int item_id = rset.getInt("item_id");
				int count = rset.getInt("count");

				L2ProductItemComponent component = new L2ProductItemComponent(item_id, count);
				a.add(component);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Exception: error while loading product item components for product: " + product_id  + e);
		}

		return a;
	}


	public Collection<L2ProductItem> getAllItems()
	{
		return _itemsList.values();
	}

	public L2ProductItem getProduct(int id)
	{
		return _itemsList.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ProductItemTable _instance = new ProductItemTable();
	}
}