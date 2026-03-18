package fidas.server.gameserver.handler;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public interface ITelnetHandler
{
	public static Logger _log = Logger.getLogger(ITelnetHandler.class.getName());
	
	/**
	 * this is the worker method that is called when someone uses an bypass command
	 * @param command
	 * @param _print
	 * @param _cSocket
	 * @param __uptime
	 * @return success
	 */
	public boolean useCommand(String command, PrintWriter _print, Socket _cSocket, int __uptime);
	
	/**
	 * this method is called at initialization to register all bypasses automatically
	 * @return all known bypasses
	 */
	public String[] getCommandList();
}