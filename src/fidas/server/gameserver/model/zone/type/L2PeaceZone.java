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
package fidas.server.gameserver.model.zone.type;

import fidas.server.Config;
import fidas.server.gameserver.instancemanager.TerritoryWarManager;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.zone.L2ZoneType;
import fidas.server.gameserver.model.zone.ZoneId;

/**
 * A Peace Zone
 * @author durgus
 */
public class L2PeaceZone extends L2ZoneType
{
	public L2PeaceZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
	    L2PcInstance player = character.getActingPlayer();
		if (character.isPlayer())
		{
			if (player.isCombatFlagEquipped() && TerritoryWarManager.getInstance().isTWInProgress())
			{
				TerritoryWarManager.getInstance().dropCombatFlag(player, false, true);
			}
			
			// PVP possible during siege, now for siege participants only
			// Could also check if this town is in siege, or if any siege is going on
			if ((player.getSiegeState() != 0) && (Config.PEACE_ZONE_MODE == 1))
			{
				return;
			}
		}
		
		if (Config.PEACE_ZONE_MODE != 2)
		{
			character.setInsideZone(ZoneId.PEACE, true);
         
            if (player != null)
            {
                player.pauseAdventTask();
            }
		}
		
		if (!getAllowStore())
		{
			character.setInsideZone(ZoneId.NO_STORE, true);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (Config.PEACE_ZONE_MODE != 2)
		{
			character.setInsideZone(ZoneId.PEACE, false);
		}
		
		if (!getAllowStore())
		{
			character.setInsideZone(ZoneId.NO_STORE, false);
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
}
