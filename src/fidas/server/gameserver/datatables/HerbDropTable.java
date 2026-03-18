package fidas.server.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.model.L2DropCategory;
import fidas.server.gameserver.model.L2DropData;

public class HerbDropTable
{
	private static final Logger _log = Logger.getLogger(HerbDropTable.class.getName());
	
	private final Map<Integer, List<L2DropCategory>> _herbGroups = new HashMap<>();
	
	protected HerbDropTable()
	{
		restoreData();
	}
	
	private void restoreData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
			{
				"groupId",
				"itemId",
				"min",
				"max",
				"category",
				"chance"
			}) + " FROM herb_droplist_groups ORDER BY groupId, chance DESC");
			ResultSet dropData = statement.executeQuery())
		{
			L2DropData dropDat = null;
			while (dropData.next())
			{
				int groupId = dropData.getInt("groupId");
				List<L2DropCategory> category;
				if (_herbGroups.containsKey(groupId))
				{
					category = _herbGroups.get(groupId);
				}
				else
				{
					category = new ArrayList<>();
					_herbGroups.put(groupId, category);
				}
				
				dropDat = new L2DropData();
				
				dropDat.setItemId(dropData.getInt("itemId"));
				dropDat.setMinDrop(dropData.getInt("min"));
				dropDat.setMaxDrop(dropData.getInt("max"));
				dropDat.setChance(dropData.getInt("chance"));
				
				int categoryType = dropData.getInt("category");
				
				if (ItemTable.getInstance().getTemplate(dropDat.getItemId()) == null)
				{
					_log.warning(getClass().getSimpleName() + ": Data for undefined item template! GroupId: " + groupId + " itemId: " + dropDat.getItemId());
					continue;
				}
				
				boolean catExists = false;
				for (L2DropCategory cat : category)
				{
					// if the category exists, add the drop to this category.
					if (cat.getCategoryType() == categoryType)
					{
						cat.addDropData(dropDat, false);
						catExists = true;
						break;
					}
				}
				// if the category doesn't exit, create it and add the drop
				if (!catExists)
				{
					L2DropCategory cat = new L2DropCategory(categoryType);
					cat.addDropData(dropDat, false);
					category.add(cat);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading Herb dropdata. ", e);
		}
	}
	
	public List<L2DropCategory> getHerbDroplist(int groupId)
	{
		return _herbGroups.get(groupId);
	}
	
	public static HerbDropTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HerbDropTable _instance = new HerbDropTable();
	}
}