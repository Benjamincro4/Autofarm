package fidas.server.gameserver.instancemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fidas.server.gameserver.model.L2Transformation;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

public class TransformationManager
{
	private static final Logger _log = Logger.getLogger(TransformationManager.class.getName());
	
	private final Map<Integer, L2Transformation> _transformations = new HashMap<>();
	
	protected TransformationManager()
	{
	}
	
	public void report()
	{
		_log.info(getClass().getSimpleName() + ": " + _transformations.size() + " transformacoes.");
	}
	
	public boolean transformPlayer(int id, L2PcInstance player)
	{
		L2Transformation template = getTransformationById(id);
		if (template != null)
		{
			L2Transformation trans = template.createTransformationForPlayer(player);
			trans.start();
			return true;
		}
		return false;
	}
	
	public L2Transformation getTransformationById(int id)
	{
		return _transformations.get(id);
	}
	
	public L2Transformation registerTransformation(L2Transformation transformation)
	{
		return _transformations.put(transformation.getId(), transformation);
	}
	
	public static TransformationManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TransformationManager _instance = new TransformationManager();
	}
}