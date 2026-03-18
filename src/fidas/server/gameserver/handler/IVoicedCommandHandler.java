package fidas.server.gameserver.handler;

import java.util.logging.Logger;

import fidas.server.gameserver.model.actor.instance.L2PcInstance;

public interface IVoicedCommandHandler
{
	public static Logger _log = Logger.getLogger(IVoicedCommandHandler.class.getName());
	
	/**
	 * this is the worker method that is called when someone uses an admin command.
	 * @param activeChar
	 * @param command
	 * @param params
	 * @return command success
	 */
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params);
	
	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public String[] getVoicedCommandList();
}