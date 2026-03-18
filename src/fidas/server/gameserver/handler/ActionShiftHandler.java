package fidas.server.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import fidas.server.gameserver.model.L2Object.InstanceType;

public class ActionShiftHandler
{
	private final Map<InstanceType, IActionHandler> _actionsShift;
	
	protected ActionShiftHandler()
	{
		_actionsShift = new HashMap<>();
	}
	
	public void registerHandler(IActionHandler handler)
	{
		_actionsShift.put(handler.getInstanceType(), handler);
	}
	
	public synchronized void removeHandler(IActionHandler handler)
	{
		_actionsShift.remove(handler.getInstanceType());
	}
	
	public IActionHandler getHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = _actionsShift.get(t);
			if (result != null)
			{
				break;
			}
		}
		return result;
	}
	
	public int size()
	{
		return _actionsShift.size();
	}
	
	public static ActionShiftHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ActionShiftHandler _instance = new ActionShiftHandler();
	}
}