package fidas.server.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.actor.L2Npc;

public class NpcPersonalAIData extends DocumentParser
{
	private Map<String, Map<String, Integer>> _AIData;

	/**
	 * Instantiates a new table.
	 */
	protected NpcPersonalAIData()
	{
		_AIData = new HashMap<>();
  	load();
	}

	@Override
	public void load()
	{
		_AIData.clear();
  	parseDatapackFile("data/stats/npc/PersonalAIData.xml");
		_log.info(getClass().getSimpleName() + ": Ai pessoal para " + _AIData.size() + " NPC('s).");
	}

	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		for (Node a = getCurrentDocument().getFirstChild(); a != null; a = a.getNextSibling())
		{
			if ("list".equalsIgnoreCase(a.getNodeName()))
			{
				for (Node b = a.getFirstChild(); b != null; b = b.getNextSibling())
				{
					if ("spawn".equalsIgnoreCase(b.getNodeName()))
					{
						attrs = b.getAttributes();
						Map<String, Integer> map = new HashMap<>();
						String name = attrs.getNamedItem("name").getNodeValue();
						for (Node c = b.getFirstChild(); c != null; c = c.getNextSibling())
						{
							// Skip odd nodes
							if (c.getNodeName().equals("#text"))
							{
								continue;
							}
							int val;
							switch(c.getNodeName())
							{
								case "disableRandomAnimation":
								case "disableRandomWalk":
									val = Boolean.parseBoolean(c.getTextContent()) ? 1 : 0;
									break;
								default:
									val = Integer.parseInt(c.getTextContent());
							}
							map.put(c.getNodeName(), val);
						}

						if (!map.isEmpty())
						{
							_AIData.put(name, map);
						}
					}
				}
			}
		}
	}

	/**
	 * @param spawnName spawn name to check
	 * @param paramName parameter to check	 
	 * @return value of given parameter for given spawn name
	 */
	public int getAIValue(String spawnName, String paramName)
	{
		return hasAIValue(spawnName, paramName) ? _AIData.get(spawnName).get(paramName) : -1;
	}

	/**
	 * @param spawnName spawn name to check
	 * @param paramName parameter name to check
	 * @return {@code true} if parameter paramName is set for spawn spawnName, {@code false} otherwise
	 */
	public boolean hasAIValue(String spawnName, String paramName)
	{
		return (spawnName != null) && _AIData.containsKey(spawnName) && _AIData.get(spawnName).containsKey(paramName);
	}

	/**
	 * Initializes npc parameters by specified values.
	 * @param npc NPC to process
	 * @param spawnName 
	 */
	public void initializeNpcParameters(L2Npc npc, String spawnName)
	{
		if (_AIData.containsKey(spawnName))
		{
			Map<String, Integer> map = _AIData.get(spawnName);

			try
			{
				for (String key : map.keySet())
				{
					switch(key)
					{
						case "disableRandomAnimation":
							npc.setRandomAnimationEnabled((map.get(key) == 0));
							break;
						case "disableRandomWalk":
							npc.setIsNoRndWalk((map.get(key) == 1));
							break;
					}
				}
			}
			catch(Exception e)
			{
				// Do nothing
			}
		}
	}

	/**
	 * Gets the single instance of NpcTable.
	 * @return single instance of NpcTable
	 */
	public static NpcPersonalAIData getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final NpcPersonalAIData _instance = new NpcPersonalAIData();
	}
}