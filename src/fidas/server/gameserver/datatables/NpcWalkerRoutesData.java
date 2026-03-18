package fidas.server.gameserver.datatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.Config;
import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.L2NpcWalkerNode;
import fidas.server.gameserver.network.NpcStringId;

public class NpcWalkerRoutesData extends DocumentParser
{
	private static final Map<Integer, List<L2NpcWalkerNode>> _routes = new HashMap<>();
	
	protected NpcWalkerRoutesData()
	{
		if (Config.ALLOW_NPC_WALKERS)
		{
			load();
		}
	}
	
	@Override
	public void load()
	{
		_routes.clear();
		parseDatapackFile("data/WalkerRoutes.xml");
		_log.info(getClass().getSimpleName() + ": " + _routes.size() + " Npc Walker Routes.");
	}
	
	@Override
	protected void parseDocument()
	{
		final Node n = getCurrentDocument().getFirstChild();
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if (d.getNodeName().equals("walker"))
			{
				List<L2NpcWalkerNode> list = new ArrayList<>(5);
				final Integer npcId = parseInteger(d.getAttributes(), "npcId");
				for (Node r = d.getFirstChild(); r != null; r = r.getNextSibling())
				{
					if (r.getNodeName().equals("route"))
					{
						NamedNodeMap attrs = r.getAttributes();
						int id = parseInt(attrs, "id");
						int x = parseInt(attrs, "X");
						int y = parseInt(attrs, "Y");
						int z = parseInt(attrs, "Z");
						int delay = parseInt(attrs, "delay");
						String chatString = null;
						NpcStringId npcString = null;
						Node node = attrs.getNamedItem("string");
						if (node != null)
						{
							chatString = node.getNodeValue();
						}
						else
						{
							node = attrs.getNamedItem("npcString");
							if (node != null)
							{
								npcString = NpcStringId.getNpcStringId(node.getNodeValue());
								if (npcString == null)
								{
									_log.log(Level.WARNING, getClass().getSimpleName() + ": Unknown npcstring '" + node.getNodeValue() + ".");
									continue;
								}
							}
							else
							{
								node = attrs.getNamedItem("npcStringId");
								if (node != null)
								{
									npcString = NpcStringId.getNpcStringId(parseInt(node));
									if (npcString == null)
									{
										_log.log(Level.WARNING, getClass().getSimpleName() + ": Unknown npcstring '" + node.getNodeValue() + ".");
										continue;
									}
								}
							}
						}
						
						list.add(new L2NpcWalkerNode(id, npcString, chatString, x, y, z, delay, parseBoolean(attrs, "run")));
					}
				}
				_routes.put(npcId, list);
			}
		}
	}
	
	public List<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		return _routes.get(id);
	}
	
	public static NpcWalkerRoutesData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcWalkerRoutesData _instance = new NpcWalkerRoutesData();
	}
}