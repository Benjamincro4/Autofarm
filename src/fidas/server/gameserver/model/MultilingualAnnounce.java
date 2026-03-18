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
 * This class represents announce on multiple languages
 * @author GKR
 */

import java.util.Map;

import javolution.util.FastMap;

import fidas.server.gameserver.datatables.MultilangMsgData;
import fidas.server.gameserver.model.MultilingualBroadcast;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.clientpackets.Say2;
import fidas.server.gameserver.network.serverpackets.CreatureSay;

public class MultilingualAnnounce extends MultilingualBroadcast
{
	private Map<String, CreatureSay> _packets;

	/**
	 * Class constructor with empty message map	
	 */
	public MultilingualAnnounce()
	{
		super();
	}

	/**
	 * Class constructor with given message map	
	 * @param msgMap text messages on different languages
	 */
	public MultilingualAnnounce(Map<String, String> msgMap)
	{
		super(msgMap);
	}

	/**
	 * @param msgName message name to create announce	
	 * @return Multilingual Announce for given message name
	 */	 	
	public static MultilingualAnnounce getAnnounce(String msgName)
	{
		Map<String, String> msgMap = MultilangMsgData.getInstance().getMessageMap(msgName);
		
		return (msgMap == null || msgMap.isEmpty()) ? null : new MultilingualAnnounce(msgMap);  
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
			_packets.put(lang, new CreatureSay(0, Say2.ANNOUNCEMENT, "", getMessages().get(lang)));
		}

		clearMessages();
	}

	/**
	 * @param player player to determine language	
	 * @return CreatureSay packet for given player language
	 */	 	
	@Override
	public CreatureSay getPacket(L2PcInstance player)
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

		return new CreatureSay(0, Say2.ANNOUNCEMENT, "", "");
	}
}