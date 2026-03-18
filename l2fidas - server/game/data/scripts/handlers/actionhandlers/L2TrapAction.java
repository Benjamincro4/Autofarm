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
package handlers.actionhandlers;

import fidas.server.gameserver.handler.IActionHandler;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.L2Object.InstanceType;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.MyTargetSelected;

public class L2TrapAction implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		// Aggression target lock effect
		if (activeChar.isLockedTarget() && (activeChar.getLockedTarget() != target))
		{
			activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			return false;
		}
		
		activeChar.setTarget(target);
		MyTargetSelected my = new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((L2Character) target).getLevel());
		activeChar.sendPacket(my);
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2Trap;
	}
}