/*
 * Copyright (C) 2004-2013 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fidas.server.gameserver.network.serverpackets;

import fidas.server.gameserver.instancemanager.GraciaSeedsManager;
import fidas.server.gameserver.instancemanager.SoIManager;
import fidas.server.gameserver.model.Location;

public class ExShowSeedMapInfo extends L2GameServerPacket
{
	public static final ExShowSeedMapInfo STATIC_PACKET = new ExShowSeedMapInfo();
	
	private static final Location[] ENTRANCES =
	{
		new Location(-246857, 251960, 4331, 1),
		new Location(-213770, 210760, 4400, 2),
	};
	
	private ExShowSeedMapInfo()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xA1);
		
		writeD(ENTRANCES.length);
		for (Location loc : ENTRANCES)
		{
			writeD(loc._x);
			writeD(loc._y);
			writeD(loc._z);
			switch (loc._heading)
			{
				case 1:
					writeD(2770 + GraciaSeedsManager.getInstance().getSoDState());
					break;
				case 2:
					writeD(SoIManager.getCurrentStage() + 2765);
					break;
			}
		}
	}
}