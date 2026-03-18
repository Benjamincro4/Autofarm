package ai.npc.LairOfAntharas;

import ai.npc.AbstractNpcAI;

import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.model.actor.L2Attackable;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.NpcStringId;
import fidas.server.gameserver.network.serverpackets.NpcSay;
import fidas.server.util.Rnd;
/**
 * 
 * @author Fixed by Hl4p3x
 *
 */
public class DragonKnight extends AbstractNpcAI
{
	private static final int DRAGON_KNIGHT_1 = 22844;
	private static final int DRAGON_KNIGHT_2 = 22845;
	private static final int ELITE_DRAGON_KNIGHT = 22846;

	public DragonKnight(String name, String descr)
	{
		super(name, descr);

		addKillId(DRAGON_KNIGHT_1);
		addKillId(DRAGON_KNIGHT_2);
		addKillId(ELITE_DRAGON_KNIGHT);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == DRAGON_KNIGHT_1 && Rnd.get(100) < 30)
		{
				L2Attackable warrior = (L2Attackable) addSpawn(DRAGON_KNIGHT_2, npc.getX() + Rnd.get(10, 50), npc.getY() + Rnd.get(10, 50), npc.getZ(), 0, false, 240000, true);
                                warrior.broadcastPacket(new NpcSay(warrior.getObjectId(), 0, warrior.getNpcId(), NpcStringId.THOSE_WHO_SET_FOOT_IN_THIS_PLACE_SHALL_NOT_LEAVE_ALIVE));
				warrior.setRunning();
				warrior.addDamageHate(killer, 0, 999);
				warrior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		if (npc.getNpcId() == DRAGON_KNIGHT_2 && Rnd.get(100) < 30)
		{
				L2Attackable knight = (L2Attackable) addSpawn(ELITE_DRAGON_KNIGHT, npc.getX() + Rnd.get(10, 50), npc.getY() + Rnd.get(10, 50), npc.getZ(), 0, false, 240000, true);
                                knight.broadcastPacket(new NpcSay(knight.getObjectId(), 0, knight.getNpcId(), NpcStringId.IF_YOU_WISH_TO_SEE_HELL_I_WILL_GRANT_YOU_YOUR_WISH));
				knight.setRunning();
				knight.addDamageHate(killer, 0, 999);
				knight.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args)
	{
		new DragonKnight(DragonKnight.class.getSimpleName(), "ai/npc");
	}
}
