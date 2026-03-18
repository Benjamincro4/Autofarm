package fidas.server.gameserver.datatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.base.ClassId;
import fidas.server.gameserver.model.items.L2Henna;

public final class HennaData extends DocumentParser
{
	private static final Map<Integer, L2Henna> _hennaList = new HashMap<>();
	
	/**
	 * Instantiates a new henna data.
	 */
	protected HennaData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_hennaList.clear();
		parseDatapackFile("data/stats/hennaList.xml");
		_log.info(getClass().getSimpleName() + ": " + _hennaList.size() + " Henna data.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("henna".equals(d.getNodeName()))
					{
						parseHenna(d);
					}
				}
			}
		}
	}
	
	/**
	 * Parses the henna.
	 * @param d the d
	 */
	private void parseHenna(Node d)
	{
		final StatsSet set = new StatsSet();
		final List<ClassId> wearClassIds = new ArrayList<>();
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		String name;
		for (int i = 0; i < attrs.getLength(); i++)
		{
			attr = attrs.item(i);
			set.set(attr.getNodeName(), attr.getNodeValue());
		}
		
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			name = c.getNodeName();
			attrs = c.getAttributes();
			switch (name)
			{
				case "stats":
				{
					for (int i = 0; i < attrs.getLength(); i++)
					{
						attr = attrs.item(i);
						set.set(attr.getNodeName(), attr.getNodeValue());
					}
					break;
				}
				case "wear":
				{
					attr = attrs.getNamedItem("count");
					set.set("wear_count", attr.getNodeValue());
					attr = attrs.getNamedItem("fee");
					set.set("wear_fee", attr.getNodeValue());
					break;
				}
				case "cancel":
				{
					attr = attrs.getNamedItem("count");
					set.set("cancel_count", attr.getNodeValue());
					attr = attrs.getNamedItem("fee");
					set.set("cancel_fee", attr.getNodeValue());
					break;
				}
				case "classId":
				{
					wearClassIds.add(ClassId.getClassId(Integer.parseInt(c.getTextContent())));
					break;
				}
			}
		}
		final L2Henna henna = new L2Henna(set);
		henna.setWearClassIds(wearClassIds);
		_hennaList.put(henna.getDyeId(), henna);
	}
	
	/**
	 * Gets the henna.
	 * @param id of the dye.
	 * @return the dye with that id.
	 */
	public L2Henna getHenna(int id)
	{
		return _hennaList.get(id);
	}
	
	/**
	 * Gets the henna list.
	 * @param classId the player's class Id.
	 * @return the list with all the allowed dyes.
	 */
	public List<L2Henna> getHennaList(ClassId classId)
	{
		final List<L2Henna> list = new ArrayList<>();
		for (L2Henna henna : _hennaList.values())
		{
			if (henna.isAllowedClass(classId))
			{
				list.add(henna);
			}
		}
		return list;
	}
	
	/**
	 * Gets the single instance of HennaData.
	 * @return single instance of HennaData
	 */
	public static HennaData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HennaData _instance = new HennaData();
	}
}