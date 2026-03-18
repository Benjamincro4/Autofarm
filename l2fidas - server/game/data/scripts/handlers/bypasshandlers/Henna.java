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
package handlers.bypasshandlers;

import fidas.server.gameserver.handler.IBypassHandler;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.actor.instance.L2SymbolMakerInstance;
import fidas.server.gameserver.model.items.L2Henna;
import fidas.server.gameserver.network.serverpackets.HennaEquipList;
import fidas.server.gameserver.network.serverpackets.HennaRemoveList;

/**
 * @author Zoey76
 */
public class Henna implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Draw",
		"RemoveList"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2SymbolMakerInstance))
		{
			return false;
		}
		
		if (command.equals("Draw"))
		{
			activeChar.sendPacket(new HennaEquipList(activeChar));
		}
		else if (command.equals("RemoveList"))
		{
			for (L2Henna henna : activeChar.getHennaList())
			{
				if (henna != null)
				{
					activeChar.sendPacket(new HennaRemoveList(activeChar));
					break;
				}
			}
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
