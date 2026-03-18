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
package handlers.skillhandlers;

import fidas.server.gameserver.handler.ISkillHandler;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.model.skills.L2SkillType;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.gameserver.network.serverpackets.UserInfo;

public class GiveVitality implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.GIVE_VITALITY
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for (L2Object target : targets)
		{
			if (target.isPlayer())
			{
				if (skill.hasEffects())
				{
					target.getActingPlayer().stopSkillEffects(skill.getId());
					skill.getEffects(activeChar, target.getActingPlayer());
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(skill);
					target.sendPacket(sm);
				}
				target.getActingPlayer().updateVitalityPoints((float) skill.getPower(), false, false);
				target.getActingPlayer().sendPacket(new UserInfo(target.getActingPlayer()));
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
