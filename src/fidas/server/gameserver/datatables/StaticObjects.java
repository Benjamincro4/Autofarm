package fidas.server.gameserver.datatables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.idfactory.IdFactory;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.actor.instance.L2StaticObjectInstance;
import fidas.server.gameserver.model.actor.templates.L2CharTemplate;

public final class StaticObjects extends DocumentParser
{
	private static final Map<Integer, L2StaticObjectInstance> _staticObjects = new HashMap<>();
	
	/**
	 * Instantiates a new static objects.
	 */
	protected StaticObjects()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_staticObjects.clear();
		parseDatapackFile("data/staticObjects.xml");
		_log.info(getClass().getSimpleName() + ": " + _staticObjects.size() + " Templates.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node att;
		StatsSet set;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("object".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						addObject(set);
					}
				}
			}
		}
	}
	
	/**
	 * Initialize an static object based on the stats set and add it to the map.
	 * @param set the stats set to add.
	 */
	private void addObject(StatsSet set)
	{
		L2StaticObjectInstance obj = new L2StaticObjectInstance(IdFactory.getInstance().getNextId(), new L2CharTemplate(new StatsSet()), set.getInteger("id"));
		obj.setType(set.getInteger("type", 0));
		obj.setName(set.getString("name"));
		obj.setMap(set.getString("texture", "none"), set.getInteger("map_x", 0), set.getInteger("map_y", 0));
		obj.spawnMe(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"));
		_staticObjects.put(obj.getObjectId(), obj);
	}
	
	/**
	 * Gets the static objects.
	 * @return a collection of static objects.
	 */
	public Collection<L2StaticObjectInstance> getStaticObjects()
	{
		return _staticObjects.values();
	}
	
	/**
	 * Gets the single instance of StaticObjects.
	 * @return single instance of StaticObjects
	 */
	public static StaticObjects getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final StaticObjects _instance = new StaticObjects();
	}
}