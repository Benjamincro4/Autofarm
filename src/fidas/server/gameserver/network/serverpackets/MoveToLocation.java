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

import fidas.server.Config;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.util.BotPunish;

public final class MoveToLocation extends L2GameServerPacket
{
	private final int _charObjId, _x, _y, _z, _xDst, _yDst, _zDst;
	private L2Character _cha;
	public MoveToLocation(L2Character cha)
	{
	    _cha = cha;
		_charObjId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_xDst = cha.getXdestination();
		_yDst = cha.getYdestination();
		_zDst = cha.getZdestination();
	}
	
	@Override
	protected final void writeImpl()
	{
		// Bot punishment restriction
	if(_cha instanceof L2PcInstance && Config.ENABLE_BOTREPORT)
		{
			L2PcInstance actor = (L2PcInstance) _cha;
			if(actor.isBeingPunished())
			{
				if(actor.getPlayerPunish().canWalk() && actor.getPlayerPunish().getBotPunishType() == BotPunish.Punish.MOVEBAN)
				{
					actor.endPunishment();
				}
				else
				{
					actor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REPORTED_120_MINS_WITHOUT_MOVE));
					return;
				}
			}
		}
		
		writeC(0x2f);
		
		writeD(_charObjId);
		
		writeD(_xDst);
		writeD(_yDst);
		writeD(_zDst);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
