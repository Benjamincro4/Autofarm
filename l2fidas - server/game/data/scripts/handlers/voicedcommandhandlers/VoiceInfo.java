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
 * this program. If not, see http://www.gnu.org/licenses/
 */
package handlers.voicedcommandhandlers;

import fidas.server.Config;
import fidas.server.gameserver.GameServer;
import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Michiru
 *
 */
public class VoiceInfo implements IVoicedCommandHandler
{
	private static String[]	VOICED_COMMANDS	=
											{ "info" };


	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		String htmFile = "data/html/ServerInfo.htm";
		String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmFile);
		if (htmContent != null)
		{
			NpcHtmlMessage infoHtml = new NpcHtmlMessage(1);
			infoHtml.setHtml(htmContent);
			infoHtml.replace("%server_restarted%", String.valueOf(GameServer.dateTimeServerStarted.getTime()));
			infoHtml.replace("%server_core_version%", String.valueOf(Config.SERVER_VERSION));
			infoHtml.replace("%server_os%", String.valueOf(System.getProperty("os.name")));
			infoHtml.replace("%server_free_mem%", String.valueOf((Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory()) / 1048576));
			infoHtml.replace("%server_total_mem%", String.valueOf(Runtime.getRuntime().totalMemory() / 1048576));
			infoHtml.replace("%rate_xp%", String.valueOf(Config.RATE_XP));
			infoHtml.replace("%rate_sp%", String.valueOf(Config.RATE_SP));
			infoHtml.replace("%rate_party_xp%", String.valueOf(Config.RATE_PARTY_XP));
			infoHtml.replace("%rate_party_sp%", String.valueOf(Config.RATE_PARTY_SP));
			infoHtml.replace("%rate_adena%", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(57)));
			infoHtml.replace("%rate_items%", String.valueOf(Config.RATE_DROP_ITEMS));
			infoHtml.replace("%rate_spoil%", String.valueOf(Config.RATE_DROP_SPOIL));
			infoHtml.replace("%rate_drop_manor%", String.valueOf(Config.RATE_DROP_MANOR));
			infoHtml.replace("%rate_quest_reward%", String.valueOf(Config.RATE_QUEST_REWARD));
			infoHtml.replace("%rate_drop_quest%", String.valueOf(Config.RATE_QUEST_DROP));
			infoHtml.replace("%pet_rate_xp%", String.valueOf(Config.PET_XP_RATE));
			infoHtml.replace("%sineater_rate_xp%", String.valueOf(Config.SINEATER_XP_RATE));
			infoHtml.replace("%pet_food_rate%", String.valueOf(Config.PET_FOOD_RATE));
			activeChar.sendPacket(infoHtml);
		}
		else
		{
			activeChar.sendMessage("omg lame error! where is " + htmFile + " ! blame the Server Admin");
		}
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}