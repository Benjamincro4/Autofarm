package ai.npc.LairOfAntharas;

import ai.npc.AbstractNpcAI;

import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.model.actor.L2Attackable;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2MonsterInstance;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
/**
 * 
 * @author Fixed by Hl4p3x
 *
 */
public class DragonGuard extends AbstractNpcAI
{
	private static final int DRAGON_GUARD = 22852;
	private static final int DRAGON_MAGE = 22853;
	private static final int[] MONSTERS = { DRAGON_GUARD,DRAGON_MAGE };
	
	public DragonGuard(String name, String descr)
	{
		super(name, descr);
		
		for (int mobId : MONSTERS)
		{
			addSpawnId(mobId);
			addAggroRangeEnterId(mobId);
			addAttackId(mobId);
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if ((npc instanceof L2MonsterInstance))
		{
			for (int mobId : MONSTERS)
			{
				if (mobId != npc.getNpcId())
					continue;
				L2MonsterInstance monster = (L2MonsterInstance) npc;
				monster.setIsImmobilized(true);
				break;
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if ((!npc.isCastingNow()) && (!npc.isAttackingNow()) && (!npc.isInCombat()) && (!player.isDead()))
		{
			npc.setIsImmobilized(false);
			npc.setRunning();
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			((L2Attackable) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if ((npc instanceof L2MonsterInstance))
		{
			for (int mobId : MONSTERS)
			{
				if (mobId != npc.getNpcId())
					continue;
				L2MonsterInstance monster = (L2MonsterInstance) npc;
				monster.setIsImmobilized(false);
				monster.setRunning();
				break;
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new DragonGuard(DragonGuard.class.getSimpleName(), "ai/npc");
	}
}
