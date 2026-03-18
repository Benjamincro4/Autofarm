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
package fidas.server.gameserver.model.conditions;

import fidas.server.gameserver.datatables.PetDataTable;
import fidas.server.gameserver.model.actor.L2Summon;
import fidas.server.gameserver.model.actor.instance.L2PetInstance;
import fidas.server.gameserver.model.items.L2Item;
import fidas.server.gameserver.model.stats.Env;

/**
 * @author JIV
 */
public class ConditionPetType extends Condition
{
	private final int petType;
	
	public ConditionPetType(int petType)
	{
		this.petType = petType;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.getCharacter() instanceof L2PetInstance))
		{
			return false;
		}
		
		/*
		 * if ((petType & L2Item.ANY_PET) == L2Item.ANY_PET) return true;
		 */
		
		int npcid = ((L2Summon) env.getCharacter()).getNpcId();
		
		if (PetDataTable.isHatchling(npcid) && (petType == L2Item.HATCHLING))
		{
			return true;
		}
		else if (PetDataTable.isAllWolfGroup(npcid) && (petType == L2Item.ALL_WOLF_GROUP))
		{
			return true;
		}
		else if (PetDataTable.isEvolvedWolf(npcid) && (petType == L2Item.GROWN_WOLF))
		{
			return true;
		}
		else if (PetDataTable.isStrider(npcid) && (petType == L2Item.STRIDER))
		{
			return true;
		}
		else if (PetDataTable.isBaby(npcid) && (petType == L2Item.BABY))
		{
			return true;
		}
		else if (PetDataTable.isImprovedBaby(npcid) && (petType == L2Item.IMPROVED_BABY))
		{
			return true;
		}
		return false;
	}
	
}
