package fidas.server.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.fishing.L2FishingRod;

public final class FishingRodsData extends DocumentParser
{
	private static final Map<Integer, L2FishingRod> _fishingRods = new HashMap<>();
	
	/**
	 * Instantiates a new fishing rods data.
	 */
	protected FishingRodsData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_fishingRods.clear();
		parseDatapackFile("data/stats/fishing/fishingRods.xml");
		_log.info(getClass().getSimpleName() + ": " + _fishingRods.size() + " Varas de pescar.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node att;
		L2FishingRod fishingRod;
		StatsSet set;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("fishingRod".equalsIgnoreCase(d.getNodeName()))
					{
						
						attrs = d.getAttributes();
						
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						fishingRod = new L2FishingRod(set);
						_fishingRods.put(fishingRod.getFishingRodItemId(), fishingRod);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the fishing rod.
	 * @param itemId the item id
	 * @return A fishing Rod by Item Id
	 */
	public L2FishingRod getFishingRod(int itemId)
	{
		return _fishingRods.get(itemId);
	}
	
	/**
	 * Gets the single instance of FishingRodsData.
	 * @return single instance of FishingRodsData
	 */
	public static FishingRodsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FishingRodsData _instance = new FishingRodsData();
	}
}