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

import fidas.server.gameserver.datatables.NpcTable;
import fidas.server.gameserver.idfactory.IdFactory;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2DecoyInstance;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.actor.templates.L2NpcTemplate;
import fidas.server.gameserver.model.skills.L2Skill;

public class L2SkillDecoy extends L2Skill
{
	private final int _summonTotalLifeTime;
	
	public L2SkillDecoy(StatsSet set)
	{
		super(set);
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 20000);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead() || !caster.isPlayer())
		{
			return;
		}
		
		if (getNpcId() == 0)
		{
			return;
		}
		
		final L2PcInstance activeChar = caster.getActingPlayer();
		if (activeChar.inObserverMode())
		{
			return;
		}
		
		if (activeChar.hasSummon() || activeChar.isMounted())
		{
			return;
		}
		
		final L2NpcTemplate DecoyTemplate = NpcTable.getInstance().getTemplate(getNpcId());
		final L2DecoyInstance decoy = new L2DecoyInstance(IdFactory.getInstance().getNextId(), DecoyTemplate, activeChar, this);
		decoy.setCurrentHp(decoy.getMaxHp());
		decoy.setCurrentMp(decoy.getMaxMp());
		decoy.setHeading(activeChar.getHeading());
		activeChar.setDecoy(decoy);
		decoy.setInstanceId(activeChar.getInstanceId());
		decoy.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
}