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

import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.network.NpcStringId;

public class ExSendUIEvent extends NpcStringContainer
{
	private final int _objectId;
	private final boolean _isHide;
	private final boolean _isIncrease;
	private final int _startTime;
	private final int _endTime;
	
	public ExSendUIEvent(L2Object player, boolean isHide, boolean isIncrease, int startTime, int endTime, String... params)
	{
		this(player, isHide, isIncrease, startTime, endTime, NpcStringId.NONE, params);
	}
	
	public ExSendUIEvent(L2Object player, boolean isHide, boolean isIncrease, int startTime, int endTime, NpcStringId npcString, String... params)
	{
		super(npcString, params);
		_objectId = player.getObjectId();
		_isHide = isHide;
		_isIncrease = isIncrease;
		_startTime = startTime;
		_endTime = endTime;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x8E);
		writeD(_objectId);
		writeD(_isHide ? 0x01 : 0x00); // 0: show timer, 1: hide timer
		writeD(0x00); // unknown
		writeD(0x00); // unknown
		writeS(_isIncrease ? "1" : "0"); // "0": count negative, "1": count positive
		writeS(String.valueOf(_startTime / 60)); // timer starting minute(s)
		writeS(String.valueOf(_startTime % 60)); // timer starting second(s)
		writeS(String.valueOf(_endTime / 60)); // timer length minute(s) (timer will disappear 10 seconds before it ends)
		writeS(String.valueOf(_endTime % 60)); // timer length second(s) (timer will disappear 10 seconds before it ends)
		writeElements();
	}
}