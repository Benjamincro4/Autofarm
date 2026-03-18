package fidas.server.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

public class TelnetHandler implements IHandler<ITelnetHandler, String>
{
	private final Map<String, ITelnetHandler> _telnetHandlers;
	
	protected TelnetHandler()
	{
		_telnetHandlers = new HashMap<>();
	}
	
	@Override
	public void registerHandler(ITelnetHandler handler)
	{
		for (String element : handler.getCommandList())
		{
			_telnetHandlers.put(element.toLowerCase(), handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(ITelnetHandler handler)
	{
		for (String element : handler.getCommandList())
		{
			_telnetHandlers.remove(element.toLowerCase());
		}
	}
	
	@Override
	public ITelnetHandler getHandler(String command)
	{
		if (command.contains(" "))
		{
			command = command.substring(0, command.indexOf(" "));
		}
		
		return _telnetHandlers.get(command.toLowerCase());
	}
	
	@Override
	public int size()
	{
		return _telnetHandlers.size();
	}
	
	public static TelnetHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TelnetHandler _instance = new TelnetHandler();
	}
}