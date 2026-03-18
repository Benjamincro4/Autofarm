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
package handlers.voicedcommandhandlers;

import fidas.server.Config;
import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.TvTEvent;
import fidas.server.gameserver.network.serverpackets.ActionFailed;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Tvt info.
 * 
 * @author denser
 */
public class TvTVoiced implements IVoicedCommandHandler
{
	private static final boolean USE_STATIC_HTML = true;
	private static final String HTML = HtmCache.getInstance().getHtm(null, "data/html/mods/TvTEvent/Status.htm");
	
	private static final String[] VOICED_COMMANDS = 
	{
		"tvt", 
		"tvtjoin", 
		"tvtleave"
	};
	
	/**
	 * Set this to false and recompile script if you dont want to use string cache.
	 * This will decrease performance but will be more consistent against possible html editions during runtime
	 * Recompiling the script will get the new html would be enough too [DrHouse]
	 */
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("tvt"))
		{
			if (TvTEvent.isStarting() || TvTEvent.isStarted())
			{
				String htmContent = (USE_STATIC_HTML && !HTML.isEmpty()) ? HTML : 
					HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/TvTEvent/Status.htm");
				
				try
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					
					npcHtmlMessage.setHtml(htmContent);
					// npcHtmlMessage.replace("%objectId%",
					// String.valueOf(getObjectId()));
					npcHtmlMessage.replace("%team1name%", Config.TVT_EVENT_TEAM_1_NAME);
					npcHtmlMessage.replace("%team1playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[0]));
					npcHtmlMessage.replace("%team1points%", String.valueOf(TvTEvent.getTeamsPoints()[0]));
					npcHtmlMessage.replace("%team2name%", Config.TVT_EVENT_TEAM_2_NAME);
					npcHtmlMessage.replace("%team2playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[1]));
					npcHtmlMessage.replace("%team2points%", String.valueOf(TvTEvent.getTeamsPoints()[1]));
					activeChar.sendPacket(npcHtmlMessage);
				}
				catch (Exception e)
				{
					_log.warning("Wrong TvT voiced: " + e);
				}
			}
			else
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
		else if(command.equalsIgnoreCase("tvtjoin"))
		{
			if(Config.TVT_ALLOW_REGISTER_VOICED_COMMAND)
				TvTEvent.onBypass("tvt_event_participation", activeChar);
			else
				activeChar.sendMessage("Command disabled.");
		}
		else if(command.equalsIgnoreCase("tvtleave"))
		{
			if(Config.TVT_ALLOW_REGISTER_VOICED_COMMAND)
				TvTEvent.onBypass("tvt_event_remove_participation", activeChar);
			else
				activeChar.sendMessage("Command disabled.");
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}