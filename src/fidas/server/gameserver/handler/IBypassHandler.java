package fidas.server.gameserver.handler;

import java.util.logging.Logger;

import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

public interface IBypassHandler
{
	public static Logger _log = Logger.getLogger(IBypassHandler.class.getName());
	
	/**
	 * This is the worker method that is called when someone uses an bypass command.
	 * @param command
	 * @param activeChar
	 * @param target
	 * @return success
	 */
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target);
	
	/**
	 * This method is called at initialization to register all bypasses automatically.
	 * @return all known bypasses
	 */
	public String[] getBypassList();
}