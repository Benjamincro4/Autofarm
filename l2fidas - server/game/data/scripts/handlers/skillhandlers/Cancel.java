package handlers.skillhandlers;

import javolution.util.FastList;
import fidas.server.Config;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.handler.ISkillHandler;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.ShotType;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.effects.L2Effect;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.model.skills.L2SkillType;
import fidas.server.gameserver.model.stats.Formulas;
import fidas.server.gameserver.model.stats.Stats;
import fidas.server.util.Rnd;
import fidas.server.util.StringUtil;

public class Cancel implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CANCEL,
	};
	
	private final int seconds = Config.CANCEL_RETURNS_SECONDS;
	private boolean shouldbelanded = false;
	
	public void returnbuffs(L2PcInstance player, FastList<L2Effect> list)
	{
		for (L2Effect e : list)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(e.getSkill().getId(), e.getLevel());
			
			if (skill != null)
			{
				skill.getEffects(player, player);
			}
		}
		player.isoncanceltask = false;
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{	
		L2Effect effect;
		final int cancelLvl, minRate, maxRate;
		
		cancelLvl = skill.getMagicLevel();
		minRate = 20;
		maxRate = 80;
		
		for (L2Object obj : targets)
		{
			if (!(obj instanceof L2Character))
			{
				continue;
			}
			final L2Character target = (L2Character) obj;
			
			if (target.isDead())
			{
				continue;
			}
			
			int lastCanceledSkillId = 0;
			int count = skill.getMaxNegatedEffects();
			double rate = skill.getPower();
			final double vulnModifier = target.calcStat(Stats.CANCEL_VULN, 0, target, null);
			final double profModifier = activeChar.calcStat(Stats.CANCEL_PROF, 0, target, null);
			double res = vulnModifier + profModifier;
			double resMod = 1;
			
			if (Config.CANCEL_RETURNS_BUFFS && (target instanceof L2PcInstance) && (activeChar instanceof L2PcInstance))
			{
				shouldbelanded = true;
			}
			
			if (res != 0)
			{
				if (res < 0)
				{
					resMod = 1 - (0.075 * res);
					resMod = 1 / resMod;
				}
				else
				{
					resMod = 1 + (0.02 * res);
				}
				
				rate *= resMod;
			}
			
			if (activeChar.isDebug())
			{
				final StringBuilder stat = new StringBuilder(100);
				StringUtil.append(stat, skill.getName(), " power:", String.valueOf((int) skill.getPower()), " lvl:", String.valueOf(cancelLvl), " res:", String.format("%1.2f", resMod), "(", String.format("%1.2f", profModifier), "/", String.format("%1.2f", vulnModifier), ") total:", String.valueOf(rate));
				final String result = stat.toString();
				if (activeChar.isDebug())
				{
					activeChar.sendDebugMessage(result);
				}
				if (Config.DEVELOPER)
				{
					_log.info(result);
				}
			}
			
			final L2Effect[] effects = target.getAllEffects();
			
			if (Config.CANCEL_RETURNS_BUFFS && shouldbelanded)
			{
				((L2PcInstance) target).clearcancelbuffs();
			}
			
			if (skill.getNegateAbnormals() != null) // Cancel for abnormals
			{
				for (L2Effect eff : effects)
				{
					if (eff == null)
					{
						continue;
					}
					
					for (String negateAbnormalType : skill.getNegateAbnormals().keySet())
					{
						if (negateAbnormalType.equalsIgnoreCase(eff.getAbnormalType()) && (skill.getNegateAbnormals().get(negateAbnormalType) >= eff.getAbnormalLvl()))
						{
							if (calcCancelSuccess(eff, cancelLvl, (int) rate, minRate, maxRate))
							{
								eff.exit();
							}
						}
					}
				}
			}
			else
			{
				for (int i = effects.length; --i >= 0;)
				{
					effect = effects[i];
					if (effect == null)
					{
						continue;
					}
					
					if (!effect.canBeStolen())
					{
						effects[i] = null;
						continue;
					}
					
					// first pass - dances/songs only
					if (!effect.getSkill().isDance())
					{
						continue;
					}
					
					if (effect.getSkill().getId() == lastCanceledSkillId)
					{
						effect.exit(); // this skill already canceled
						continue;
					}
					
					if (!calcCancelSuccess(effect, cancelLvl, (int) rate, minRate, maxRate))
					{
						continue;
					}
					
					if (Config.CANCEL_RETURNS_BUFFS && shouldbelanded)
					{
						((L2PcInstance) target).addcancelbuffs(effect);
					}
					
					lastCanceledSkillId = effect.getSkill().getId();
					effect.exit();
					count--;
					
					if (count == 0)
					{
						break;
					}
				}
				
				if (count != 0)
				{
					lastCanceledSkillId = 0;
					for (int i = effects.length; --i >= 0;)
					{
						effect = effects[i];
						if (effect == null)
						{
							continue;
						}
						
						// second pass - all except dances/songs
						if (effect.getSkill().isDance())
						{
							continue;
						}
						
						if (effect.getSkill().getId() == lastCanceledSkillId)
						{
							effect.exit(); // this skill already canceled
							continue;
						}
						
						if (!calcCancelSuccess(effect, cancelLvl, (int) rate, minRate, maxRate))
						{
							continue;
						}
						
						if (Config.CANCEL_RETURNS_BUFFS && shouldbelanded)
						{
							((L2PcInstance) target).addcancelbuffs(effect);
						}
						
						lastCanceledSkillId = effect.getSkill().getId();
						effect.exit();
						count--;
						
						if (count == 0)
						{
							break;
						}
					}
				}
			}
			
			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
			
			if (Config.CANCEL_RETURNS_BUFFS && shouldbelanded && (((L2PcInstance) target).getcancelbuffs() != null) && !((L2PcInstance) target).isoncanceltask)
			{
				((L2PcInstance) target).sendMessage("Seus buffs retornam em " + seconds + " segundos.");
				((L2PcInstance) target).isoncanceltask = true;
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						returnbuffs(((L2PcInstance) target), ((L2PcInstance) target).getcancelbuffs());
					}
				}, seconds * 1000);
			}
		}
		
		// Applying self-effects
		if (skill.hasSelfEffects())
		{
			effect = activeChar.getFirstEffect(skill.getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				// Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
		
		
		activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
	}
	
	private boolean calcCancelSuccess(L2Effect effect, int cancelLvl, int baseRate, int minRate, int maxRate)
	{
		int rate = (2 * (cancelLvl - effect.getSkill().getMagicLevel()));
		rate += (effect.getAbnormalTime() / 120);
		rate += baseRate;
		
		if (rate < minRate)
		{
			rate = minRate;
		}
		else if (rate > maxRate)
		{
			rate = maxRate;
		}
		
		return Rnd.get(100) < rate;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}