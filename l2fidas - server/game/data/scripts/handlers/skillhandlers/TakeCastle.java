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
import fidas.server.gameserver.instancemanager.CastleManager;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Castle;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.model.skills.L2SkillType;

/**
 * @author _drunk_
 */
public class TakeCastle implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.TAKECASTLE
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!activeChar.isPlayer() || (targets.length == 0))
		{
			return;
		}
		
		L2PcInstance player = activeChar.getActingPlayer();
		
		if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId()))
		{
			return;
		}
		
		Castle castle = CastleManager.getInstance().getCastle(player);
		if ((castle == null) || !player.checkIfOkToCastSealOfRule(castle, true, skill, targets[0]))
		{
			return;
		}
		
		try
		{
			castle.engrave(player.getClan(), targets[0]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
	public static void main(String[] args)
	{
		new TakeCastle();
	}
}
