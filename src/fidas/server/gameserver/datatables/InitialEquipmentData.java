package fidas.server.gameserver.datatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.Config;
import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.base.ClassId;
import fidas.server.gameserver.model.items.PcItemTemplate;

public final class InitialEquipmentData extends DocumentParser
{
	private static final String NORMAL = "data/stats/initialEquipment.xml";
	private static final String EVENT = "data/stats/initialEquipmentEvent.xml";
	private final Map<ClassId, List<PcItemTemplate>> _initialEquipmentList = new HashMap<>();
	
	/**
	 * Instantiates a new initial equipment data.
	 */
	protected InitialEquipmentData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_initialEquipmentList.clear();
		parseDatapackFile(Config.INITIAL_EQUIPMENT_EVENT ? EVENT : NORMAL);
		_log.info(getClass().getSimpleName() + ": " + _initialEquipmentList.size() + " Initial Equipment data.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("equipment".equalsIgnoreCase(d.getNodeName()))
					{
						parseEquipment(d);
					}
				}
			}
		}
	}
	
	/**
	 * Parses the equipment.
	 * @param d parse an initial equipment and add it to {@link #_initialEquipmentList}
	 */
	private void parseEquipment(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_initialEquipmentList.put(classId, equipList);
	}
	
	/**
	 * Gets the equipment list.
	 * @param cId the class Id for the required initial equipment.
	 * @return the initial equipment for the given class Id.
	 */
	public List<PcItemTemplate> getEquipmentList(ClassId cId)
	{
		return _initialEquipmentList.get(cId);
	}
	
	/**
	 * Gets the equipment list.
	 * @param cId the class Id for the required initial equipment.
	 * @return the initial equipment for the given class Id.
	 */
	public List<PcItemTemplate> getEquipmentList(int cId)
	{
		return _initialEquipmentList.get(ClassId.getClassId(cId));
	}
	
	/**
	 * Gets the single instance of InitialEquipmentData.
	 * @return single instance of InitialEquipmentData
	 */
	public static InitialEquipmentData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final InitialEquipmentData _instance = new InitialEquipmentData();
	}
}