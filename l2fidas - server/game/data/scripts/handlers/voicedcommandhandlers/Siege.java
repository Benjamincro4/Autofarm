/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.instancemanager.CastleManager;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Castle;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.network.serverpackets.SiegeInfo;

public class Siege implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"siege",
		"siege_gludio",
		"siege_dion",
		"siege_giran",
		"siege_oren",
		"siege_aden",
		"siege_innadril",
		"siege_goddard",
		"siege_rune",
		"siege_schuttgart"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("siege"))
		{
			sendHtml(activeChar);
		}
		
		if (command.startsWith("siege_"))
		{
			if ((activeChar.getClan() != null) && !activeChar.isClanLeader())
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return false;
			}
			
			int castleId = 0;
			
			if (command.startsWith("siege_gludio"))
			{
				castleId = 1;
			}
			else if (command.startsWith("siege_dion"))
			{
				castleId = 2;
			}
			else if (command.startsWith("siege_giran"))
			{
				castleId = 3;
			}
			else if (command.startsWith("siege_oren"))
			{
				castleId = 4;
			}
			else if (command.startsWith("siege_aden"))
			{
				castleId = 5;
			}
			else if (command.startsWith("siege_innadril"))
			{
				castleId = 6;
			}
			else if (command.startsWith("siege_goddard"))
			{
				castleId = 7;
			}
			else if (command.startsWith("siege_rune"))
			{
				castleId = 8;
			}
			else if (command.startsWith("siege_schuttgart"))
			{
				castleId = 9;
			}
			
			Castle castle = CastleManager.getInstance().getCastleById(castleId);
			if ((castle != null) && (castleId != 0))
			{
				activeChar.sendPacket(new SiegeInfo(castle));
			}
		}
		return true;
	}
	
	private void sendHtml(L2PcInstance activeChar)
	{
		String htmFile = "data/html/custom/CastleManager.htm";
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(activeChar.getHtmlPrefix(), htmFile);
		activeChar.sendPacket(msg);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}