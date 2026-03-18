package fidas.server.gameserver.handler;

import java.util.logging.Logger;

import fidas.server.gameserver.model.actor.instance.L2PcInstance;

public interface IUserCommandHandler
{
	public static Logger _log = Logger.getLogger(IUserCommandHandler.class.getName());
	
	/**
	 * this is the worker method that is called when someone uses an admin command.
	 * @param id
	 * @param activeChar
	 * @return command success
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar);
	
	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public int[] getUserCommandList();
}