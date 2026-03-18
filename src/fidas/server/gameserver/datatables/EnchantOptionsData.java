package fidas.server.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Node;

import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.EnchantOptions;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.util.Util;

public class EnchantOptionsData extends DocumentParser
{
	private final Map<Integer, Map<Integer, EnchantOptions>> _data = new HashMap<>();
	
	protected EnchantOptionsData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		parseDatapackFile("data/enchantOptions.xml");
	}
	
	@Override
	protected void parseDocument()
	{
		Node att = null;
		int counter = 0;
		EnchantOptions op = null;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						int itemId = parseInt(d.getAttributes(), "id");
						if (!_data.containsKey(itemId))
						{
							_data.put(itemId, new HashMap<Integer, EnchantOptions>());
						}
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("options".equalsIgnoreCase(cd.getNodeName()))
							{
								op = new EnchantOptions(parseInt(cd.getAttributes(), "level"));
								_data.get(itemId).put(op.getLevel(), op);
								
								for (byte i = 0; i < 3; i++)
								{
									att = cd.getAttributes().getNamedItem("option" + (i + 1));
									if ((att != null) && Util.isDigit(att.getNodeValue()))
									{
										op.setOption(i, parseInt(att));
									}
								}
								counter++;
							}
						}
					}
				}
			}
		}
		_log.log(Level.INFO, getClass().getSimpleName() + ": " + _data.size() + " Items e " + counter + " Opcoes.");
	}
	
	/**
	 * @param itemId
	 * @param enchantLevel
	 * @return enchant effects information.
	 */
	public EnchantOptions getOptions(int itemId, int enchantLevel)
	{
		if (!_data.containsKey(itemId) || !_data.get(itemId).containsKey(enchantLevel))
		{
			return null;
		}
		return _data.get(itemId).get(enchantLevel);
	}
	
	/**
	 * @param item
	 * @return enchant effects information.
	 */
	public EnchantOptions getOptions(L2ItemInstance item)
	{
		return item != null ? getOptions(item.getItemId(), item.getEnchantLevel()) : null;
	}
	
	/**
	 * Gets the single instance of EnchantOptionsData.
	 * @return single instance of EnchantOptionsData
	 */
	public static final EnchantOptionsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantOptionsData _instance = new EnchantOptionsData();
	}
}