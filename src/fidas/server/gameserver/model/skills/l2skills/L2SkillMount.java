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
package fidas.server.gameserver.model.skills.l2skills;

import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.TvTEvent;
import fidas.server.gameserver.model.entity.TvTRoundEvent;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.network.SystemMessageId;

public class L2SkillMount extends L2Skill
{
	private final int _itemId;
	
	public L2SkillMount(StatsSet set)
	{
		super(set);
		_itemId = set.getInteger("itemId", 0);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (!caster.isPlayer())
		{
			return;
		}
		
		if (!TvTEvent.onItemSummon(caster.getObjectId()))
		{
			return;
		}

		if (!TvTRoundEvent.onItemSummon(caster.getObjectId()))
		{
			return;
		}
		
		L2PcInstance activePlayer = caster.getActingPlayer();
		
		if (!activePlayer.getFloodProtectors().getItemPetSummon().tryPerformAction("mount"))
		{
			return;
		}
		
		// Dismount Action
		if (getNpcId() == 0)
		{
			activePlayer.dismount();
			return;
		}
		
		if (activePlayer.isSitting())
		{
			activePlayer.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		
		if (activePlayer.inObserverMode())
		{
			return;
		}
		
		if (activePlayer.isInOlympiadMode())
		{
			activePlayer.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}
		
		if (activePlayer.hasSummon() || activePlayer.isMounted())
		{
			activePlayer.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}
		
		if (activePlayer.isAttackingNow())
		{
			activePlayer.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}
		
		if (activePlayer.isCursedWeaponEquipped())
		{
			return;
		}
		
		activePlayer.mount(getNpcId(), _itemId, false);
	}
}
