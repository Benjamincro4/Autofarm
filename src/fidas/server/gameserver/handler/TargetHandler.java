package fidas.server.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import fidas.server.gameserver.model.skills.targets.L2TargetType;

public class TargetHandler implements IHandler<ITargetTypeHandler, Enum<L2TargetType>>
{
	private final Map<Enum<L2TargetType>, ITargetTypeHandler> _datatable;
	
	protected TargetHandler()
	{
		_datatable = new HashMap<>();
	}
	
	@Override
	public void registerHandler(ITargetTypeHandler handler)
	{
		_datatable.put(handler.getTargetType(), handler);
	}
	
	@Override
	public synchronized void removeHandler(ITargetTypeHandler handler)
	{
		_datatable.remove(handler.getTargetType());
	}
	
	@Override
	public ITargetTypeHandler getHandler(Enum<L2TargetType> targetType)
	{
		return _datatable.get(targetType);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static TargetHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TargetHandler _instance = new TargetHandler();
	}
}