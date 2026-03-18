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
 * This class represents screen message on multiple languages
 * @author GKR
 */

import java.util.Map;

import javolution.util.FastMap;

import fidas.server.gameserver.datatables.MultilangMsgData;
import fidas.server.gameserver.model.MultilingualBroadcast;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.ExShowScreenMessage;

public class MultilingualScreenMsg extends MultilingualBroadcast
{
	private Map<String, ExShowScreenMessage> _packets;
	private int _showTime;

	/**
	 * Class constructor with empty message map and given show time
	 * @param showTime time to show messages, ms	 	
	 */
	public MultilingualScreenMsg(int showTime)
	{
		super();
		_showTime = showTime;
	}

	/**
	 * Class constructor with given message map and given show time	
	 * @param msgMap text messages on different languages
	 * @param showTime time to show messages, ms	 
	 */
	public MultilingualScreenMsg(Map<String, String> msgMap, int showTime)
	{
		super(msgMap);
		_showTime = showTime;
	}

	/**
	 * @param msgName message name to create announce
	 * @param showTime time to show messages, ms	 	
	 * @return Multilingual Announce for given message name
	 */	 	
	public static MultilingualScreenMsg getScreenMsg(String msgName, int showTime)
	{
		Map<String, String> msgMap = MultilangMsgData.getInstance().getMessageMap(msgName);

		return (msgMap == null || msgMap.isEmpty()) ? null : new MultilingualScreenMsg(msgMap, showTime);  
	}

	/**
	 * Just short wrapper for default time	
	 * @param msgName message name to create announce	
	 * @return Multilingual Announce for given message name
	 */	 	
	public static MultilingualScreenMsg getScreenMsg(String msgName)
	{
		return getScreenMsg(msgName, 5000);
	}

	/**
	 * "Compiles" objects: creates map of packets for all languages
	 */	 	
	@Override
	public void compile()
	{
		if (isCompiled())
		{
			return;
		}

		setCompiled();
		_packets = new FastMap<>();
		for (String lang : getMessages().keySet())
		{
			_packets.put(lang, new ExShowScreenMessage(getMessages().get(lang), _showTime));
		}

		clearMessages();
	}

	/**
	 * @param player player to determine language	
	 * @return ExShowScreenMessage packet for given player language
	 */	 	
	@Override
	public ExShowScreenMessage getPacket(L2PcInstance player)
	{
		String lang = player.getLang() == null ? "en" : player.getLang();

		if (_packets.containsKey(lang))
		{
			return _packets.get(lang);
		} 
		else if (_packets.containsKey("en"))
		{
			return _packets.get("en");
		}

		return new ExShowScreenMessage("", 0);
	}
}