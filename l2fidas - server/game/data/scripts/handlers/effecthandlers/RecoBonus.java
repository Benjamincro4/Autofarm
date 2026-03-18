package handlers.effecthandlers;

import fidas.server.gameserver.model.effects.L2Effect;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.stats.Env;
import fidas.server.gameserver.model.effects.EffectTemplate;
import fidas.server.gameserver.model.effects.L2EffectType;


public class RecoBonus extends L2Effect
{
    public RecoBonus(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    /**
     *
     * @see fidas.server.gameserver.model.effects.L2Effect#getEffectType()
     */
    @Override
    public L2EffectType getEffectType()
    {
        return L2EffectType.BUFF;
    }

    /**
     *
     * @see fidas.server.gameserver.model.effects.L2Effect#onStart()
     */
    @Override
    public boolean onStart()
    {
        if (!(getEffected() instanceof L2PcInstance))
        {
            return false;
        }

        ((L2PcInstance) getEffected()).setRecomBonusType(1).setRecoBonusActive(true);
        return true;
    }

    /**
     *
     * @see fidas.server.gameserver.model.effects.L2Effect#onExit()
     */
    @Override
    public void onExit()
    {
        ((L2PcInstance) getEffected()).setRecomBonusType(0).setRecoBonusActive(false);
    }

    @Override
    protected boolean effectCanBeStolen()
    {
        return false;
    }

    /**
     *
     * @see fidas.server.gameserver.model.effects.L2Effect#onActionTime()
     */
    @Override
    public boolean onActionTime()
    {
        return false;
    }
}