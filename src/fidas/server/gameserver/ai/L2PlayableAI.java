package fidas.server.gameserver.ai;

import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.L2Character.AIAccessor;
import fidas.server.gameserver.model.actor.L2Playable;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.model.zone.ZoneId;
import fidas.server.gameserver.network.SystemMessageId;

public abstract class L2PlayableAI extends L2CharacterAI
{
	/**
	 * @param accessor
	 */
	public L2PlayableAI(AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		if (target instanceof L2Playable)
		{
			if (target.getActingPlayer().getProtectionBlessing() && ((_actor.getActingPlayer().getLevel() - target.getActingPlayer().getLevel()) >= 10) && (_actor.getActingPlayer().getKarma() > 0) && !(target.isInsideZone(ZoneId.PVP)))
			{
				// If attacker have karma and have level >= 10 than his target and target have
				// Newbie Protection Buff,
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
			
			if (_actor.getActingPlayer().getProtectionBlessing() && ((target.getActingPlayer().getLevel() - _actor.getActingPlayer().getLevel()) >= 10) && (target.getActingPlayer().getKarma() > 0) && !(target.isInsideZone(ZoneId.PVP)))
			{
				// If target have karma and have level >= 10 than his target and actor have
				// Newbie Protection Buff,
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
			
			if (target.getActingPlayer().isCursedWeaponEquipped() && (_actor.getActingPlayer().getLevel() <= 20))
			{
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
			
			if (_actor.getActingPlayer().isCursedWeaponEquipped() && (target.getActingPlayer().getLevel() <= 20))
			{
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
		}
		
		super.onIntentionAttack(target);
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if ((target instanceof L2Playable) && skill.isOffensive())
		{
			if (target.getActingPlayer().getProtectionBlessing() && ((_actor.getActingPlayer().getLevel() - target.getActingPlayer().getLevel()) >= 10) && (_actor.getActingPlayer().getKarma() > 0) && !target.isInsideZone(ZoneId.PVP))
			{
				// If attacker have karma and have level >= 10 than his target and target have
				// Newbie Protection Buff,
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				_actor.setIsCastingNow(false);
				return;
			}
			
			if (_actor.getActingPlayer().getProtectionBlessing() && ((target.getActingPlayer().getLevel() - _actor.getActingPlayer().getLevel()) >= 10) && (target.getActingPlayer().getKarma() > 0) && !target.isInsideZone(ZoneId.PVP))
			{
				// If target have karma and have level >= 10 than his target and actor have
				// Newbie Protection Buff,
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				_actor.setIsCastingNow(false);
				return;
			}
			
			if (target.getActingPlayer().isCursedWeaponEquipped() && (_actor.getActingPlayer().getLevel() <= 20))
			{
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				_actor.setIsCastingNow(false);
				return;
			}
			
			if (_actor.getActingPlayer().isCursedWeaponEquipped() && (target.getActingPlayer().getLevel() <= 20))
			{
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				_actor.setIsCastingNow(false);
				return;
			}
		}
		
		super.onIntentionCast(skill, target);
	}	
}