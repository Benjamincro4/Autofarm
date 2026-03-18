package fidas.server.gameserver.handler;

import java.util.logging.Logger;

import fidas.server.gameserver.model.actor.L2Playable;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;

public interface IItemHandler
{
	public static Logger _log = Logger.getLogger(IItemHandler.class.getName());
	
	/**
	 * Launch task associated to the item.
	 * @param playable the non-NPC character using the item
	 * @param item L2ItemInstance designating the item to use
	 * @param forceUse ctrl hold on item use
	 * @return {@code true} if the item all conditions are met and the item is used, {@code false} otherwise.
	 */
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse);
}