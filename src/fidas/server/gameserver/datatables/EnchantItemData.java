package fidas.server.gameserver.datatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.EnchantItem;
import fidas.server.gameserver.model.EnchantScroll;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;

public class EnchantItemData extends DocumentParser
{
	public static final Map<Integer, EnchantScroll> _scrolls = new HashMap<>();
	public static final Map<Integer, EnchantItem> _supports = new HashMap<>();
	
	/**
	 * Instantiates a new enchant item data.
	 */
	public EnchantItemData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_scrolls.clear();
		_supports.clear();
		parseDatapackFile("data/enchantData.xml");
		_log.info(getClass().getSimpleName() + ": " + _scrolls.size() + " Enchant Scrolls.");
		_log.info(getClass().getSimpleName() + ": " + _supports.size() + " Support Items.");
	}
	
	@Override
	protected void parseDocument()
	{
		StatsSet set;
		Node att;
		Map<Integer, Double> enchantSteps;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("enchant".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						set = new StatsSet();
						enchantSteps = new HashMap<>();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						List<Integer> items = new ArrayList<>();
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("item".equalsIgnoreCase(cd.getNodeName()))
							{
								items.add(parseInteger(cd.getAttributes(), "id"));
							}
							else if ("step".equalsIgnoreCase(cd.getNodeName()))
							{
								enchantSteps.put(parseInt(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "successRate"));
							}
						}
						EnchantScroll item = new EnchantScroll(set, items, enchantSteps);
						_scrolls.put(item.getScrollId(), item);
					}
					else if ("support".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						List<Integer> items = new ArrayList<>();
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("item".equalsIgnoreCase(cd.getNodeName()))
							{
								items.add(parseInteger(cd.getAttributes(), "id"));
							}
						}
						EnchantItem item = new EnchantItem(set, items);
						_supports.put(item.getScrollId(), item);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the enchant scroll.
	 * @param scroll the scroll
	 * @return enchant template for scroll
	 */
	public final EnchantScroll getEnchantScroll(L2ItemInstance scroll)
	{
		return _scrolls.get(scroll.getItemId());
	}
	
	/**
	 * Gets the support item.
	 * @param item the item
	 * @return enchant template for support item
	 */
	public final EnchantItem getSupportItem(L2ItemInstance item)
	{
		return _supports.get(item.getItemId());
	}
	
	/**
	 * Gets the single instance of EnchantItemData.
	 * @return single instance of EnchantItemData
	 */
	public static final EnchantItemData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemData _instance = new EnchantItemData();
	}
}