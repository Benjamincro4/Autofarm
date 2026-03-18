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
package fidas.server.gameserver.scripting.scriptengine.listeners.player;

import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.clientpackets.EnterWorld;
import fidas.server.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * Gets triggered when a L2PcInstance is spawned in the world
 * @author TheOne
 */
public abstract class PlayerSpawnListener extends L2JListener
{
	public PlayerSpawnListener()
	{
		register();
	}
	
	/**
	 * Triggered when a player is spawned
	 * @param player
	 */
	public abstract void onSpawn(L2PcInstance player);
	
	@Override
	public void register()
	{
		EnterWorld.addSpawnListener(this);
		
	}
	
	@Override
	public void unregister()
	{
		EnterWorld.removeSpawnListener(this);
	}
}
