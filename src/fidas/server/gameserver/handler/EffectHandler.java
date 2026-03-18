package fidas.server.gameserver.handler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import fidas.server.gameserver.model.effects.L2Effect;
import fidas.server.gameserver.scripting.L2ScriptEngineManager;

public final class EffectHandler implements IHandler<Class<? extends L2Effect>, String>
{
	private final Map<String, Class<? extends L2Effect>> _handlers;
	
	protected EffectHandler()
	{
		_handlers = new HashMap<>();
	}
	
	@Override
	public void registerHandler(Class<? extends L2Effect> handler)
	{
		_handlers.put(handler.getSimpleName(), handler);
	}
	
	@Override
	public synchronized void removeHandler(Class<? extends L2Effect> handler)
	{
		_handlers.remove(handler.getSimpleName());
	}
	
	@Override
	public final Class<? extends L2Effect> getHandler(String name)
	{
		return _handlers.get(name);
	}
	
	@Override
	public int size()
	{
		return _handlers.size();
	}
	
	public void executeScript()
	{
		try
		{
			File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, "handlers/EffectMasterHandler.java");
			L2ScriptEngineManager.getInstance().executeScript(file);
		}
		catch (Exception e)
		{
			throw new Error("Problems while running EffectMansterHandler", e);
		}
	}
	
	private static final class SingletonHolder
	{
		protected static final EffectHandler _instance = new EffectHandler();
	}
	
	public static EffectHandler getInstance()
	{
		return SingletonHolder._instance;
	}
}