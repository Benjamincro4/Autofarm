package handlers.effecthandlers;

import fidas.server.gameserver.model.effects.EffectTemplate;
import fidas.server.gameserver.model.effects.L2Effect;
import fidas.server.gameserver.model.effects.L2EffectType;
import fidas.server.gameserver.model.stats.Env;

public class CancelAll extends L2Effect
{
	public CancelAll(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CANCEL_ALL;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().stopAllEffects();
		return false;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}