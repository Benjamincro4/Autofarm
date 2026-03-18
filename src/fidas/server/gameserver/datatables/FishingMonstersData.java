package fidas.server.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.fishing.L2FishingMonster;

public final class FishingMonstersData extends DocumentParser
{
	private static final Map<Integer, L2FishingMonster> _fishingMonstersData = new HashMap<>();
	
	/**
	 * Instantiates a new fishing monsters data.
	 */
	protected FishingMonstersData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_fishingMonstersData.clear();
		parseDatapackFile("data/stats/fishing/fishingMonsters.xml");
		_log.info(getClass().getSimpleName() + ": " + _fishingMonstersData.size() + " Monstros de pesca.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node att;
		L2FishingMonster fishingMonster;
		StatsSet set;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("fishingMonster".equalsIgnoreCase(d.getNodeName()))
					{
						
						attrs = d.getAttributes();
						
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						fishingMonster = new L2FishingMonster(set);
						_fishingMonstersData.put(fishingMonster.getFishingMonsterId(), fishingMonster);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the fishing monster.
	 * @param lvl the fisherman level
	 * @return a fishing monster given the fisherman level
	 */
	public L2FishingMonster getFishingMonster(int lvl)
	{
		for (L2FishingMonster fishingMonster : _fishingMonstersData.values())
		{
			if ((lvl >= fishingMonster.getUserMinLevel()) && (lvl <= fishingMonster.getUserMaxLevel()))
			{
				return fishingMonster;
			}
		}
		return null;
	}
	
	/**
	 * Gets the fishing monster by Id.
	 * @param id the fishing monster Id
	 * @return the fishing monster by Id
	 */
	public L2FishingMonster getFishingMonsterById(int id)
	{
		if (_fishingMonstersData.containsKey(id))
		{
			return _fishingMonstersData.get(id);
		}
		return null;
	}
	
	/**
	 * Gets the single instance of FishingMonsterData.
	 * @return single instance of FishingMonsterData
	 */
	public static FishingMonstersData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FishingMonstersData _instance = new FishingMonstersData();
	}
}