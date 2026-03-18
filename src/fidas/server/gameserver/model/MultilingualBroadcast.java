/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package fidas.server.gameserver.model;

/**
 * Base text holder for multilingual broadcast classes
 * @author GKR
 */

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import fidas.server.gameserver.datatables.MultilangMsgData;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.clientpackets.Say2;
import fidas.server.gameserver.network.serverpackets.CreatureSay;
import fidas.server.gameserver.network.serverpackets.L2GameServerPacket;

public class MultilingualBroadcast
{
	private final Logger _log = Logger.getLogger(getClass().getName());

	private Map<String, String> _messages;

	private boolean _compiled;

	/**
	 * Class constructor with empty message map	
	 */
	public MultilingualBroadcast()
	{
		_messages = new FastMap<String, String>();
	}

	/**
	 * Class constructor with given message map	
	 * @param msgMap text messages on different languages
	 */
	public MultilingualBroadcast(Map<String, String> msgMap)
	{
		_messages = msgMap;
	}

	/**
	 * Adds message on given languauge to text map
	 * @param lang message languauge
	 * @param text message text
	 */	 	
	public void addEntry(String lang, String text)
	{
		if (_compiled)
		{
			return;
		}

		_messages.put(lang, text);
	}

	/**
	 * Replaces first found substitute fragment (for all languages) with given text
	 * @param text replacement text
	 */	 	
	public void addCommonStringParameter(String text)
	{
		if (_compiled)
		{
			return;
		}

		for (String key : _messages.keySet())
		{
			String val = _messages.get(key).replaceFirst("%.*?%", text);
			_messages.put(key, val);
		}
	}
	
	/**
	 * Replaces first found substitute fragment with given text on appropriate language
	 * @param textMap message text map with replacement text on available languages
	 */	 	
	public void addUniqueStringParameter(Map<String, String> textMap)
	{
		if (_compiled || textMap == null)
		{
			return;
		}

		for (String key : _messages.keySet())
		{
			String val;

			if (textMap.containsKey(key))
			{
				val = _messages.get(key).replaceFirst("%.*?%", textMap.get(key));
			}
			else if (textMap.containsKey("en"))
			{
				val = _messages.get(key).replaceFirst("%.*?%", textMap.get("en"));
			}
			else
			{
				val = _messages.get(key).replaceFirst("%.*?%", "");
			}

			_messages.put(key, val);
		}
	}

	/**
	 * "Compiles" objects: creates map of packets for all languages
	 */	 	
	public void compile()
	{
		// No default implementation, overrides in children
	}

	/**
	 * @return true if object is "compiled": map of packets for all languages is created
	 */	 	
	protected final boolean isCompiled()
	{
		return _compiled;
	}

	/**
	 * Sets compiled state to object	
	 */	 	
	protected final void setCompiled()
	{
		_compiled = true;
	}

	/**
	 * @return true if object contains not text strings
	 */	 	
	public boolean isEmpty()
	{
		return _messages.isEmpty();
	}

	/**
	 * @param lang language to check	
	 * @return true if object contains string on given language
	 */	 	
	public boolean hasLang(String lang)
	{
		return _messages.containsKey(lang);
	}

	/**
	 * Clears message map	
	 */	 	
	protected void clearMessages()
	{
		_messages.clear();
	}

	/**
	 * @return message map
	 */	 	
	protected final Map<String, String> getMessages()
	{
		return _messages;
	}

	/**
	 * @param player player to determine language	
	 * @return CreatureSay packet for given player language
	 */	 	
	public L2GameServerPacket getPacket(L2PcInstance player)
	{
		return null; // No default implementation, overrides in children
	}
}