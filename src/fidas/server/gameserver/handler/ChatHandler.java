package fidas.server.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

public class ChatHandler implements IHandler<IChatHandler, Integer>
{
	private final Map<Integer, IChatHandler> _datatable;
	
	/**
	 * Singleton constructor
	 */
	protected ChatHandler()
	{
		_datatable = new HashMap<>();
	}
	
	/**
	 * Register a new chat handler
	 * @param handler
	 */
	@Override
	public void registerHandler(IChatHandler handler)
	{
		int[] ids = handler.getChatTypeList();
		for (int id : ids)
		{
			_datatable.put(id, handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(IChatHandler handler)
	{
		int[] ids = handler.getChatTypeList();
		for (int id : ids)
		{
			_datatable.remove(id);
		}
	}
	
	/**
	 * Get the chat handler for the given chat type
	 * @param chatType
	 * @return
	 */
	@Override
	public IChatHandler getHandler(Integer chatType)
	{
		return _datatable.get(chatType);
	}
	
	/**
	 * Returns the size
	 * @return
	 */
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static ChatHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ChatHandler _instance = new ChatHandler();
	}
}