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
package handlers.itemhandlers;

import java.util.logging.Level;

import fidas.server.Config;
import fidas.server.gameserver.handler.IItemHandler;
import fidas.server.gameserver.model.ShotType;
import fidas.server.gameserver.model.actor.L2Playable;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.holders.SkillHolder;
import fidas.server.gameserver.model.items.L2Weapon;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.model.items.type.L2ActionType;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.MagicSkillUse;
import fidas.server.gameserver.util.Broadcast;

public class SpiritShot implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		final SkillHolder[] skills = item.getItem().getSkills();
		
		int itemId = item.getItemId();
		
		if (skills == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": is missing skills!");
			return false;
		}
		
		// Check if Spirit shot can be used
		if ((weaponInst == null) || (weaponItem.getSpiritShotCount() == 0))
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
			}
			return false;
		}
		
		// Check if Spirit shot is already active
		if (activeChar.isChargedShot(ShotType.SPIRITSHOTS))
		{
			return false;
		}
		
		boolean gradeCheck = item.isEtcItem() && (item.getEtcItem().getDefaultAction() == L2ActionType.spiritshot) && (weaponInst.getItem().getItemGradeSPlus() == item.getItem().getItemGradeSPlus());
		
		if (!gradeCheck)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
			}
			
			return false;
		}
		
		// Consume Spirit shot if player has enough of them
		if (!Config.INFINITE_SPIRIT_SHOT && !activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
		{
			if (!activeChar.disableAutoShot(itemId))
			{
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
			}
			return false;
		}
		
		// Charge Spirit shot
		activeChar.setChargedShot(ShotType.SPIRITSHOTS, true);
		
		// Send message to client
		activeChar.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skills[0].getSkillId(), skills[0].getSkillLvl(), 0, 0), 600);
		return true;
	}
}
