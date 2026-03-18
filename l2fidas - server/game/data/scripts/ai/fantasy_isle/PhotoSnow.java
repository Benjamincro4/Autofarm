package ai.fantasy_isle;

import ai.npc.AbstractNpcAI;

import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

/**
 * Fantasy Isle Photo Npc
 */
public class PhotoSnow extends AbstractNpcAI
{
	private static final int FantasyIsle4317 = 4317;
	private static final int FantasyIsle4318 = 4318;
	private static final int FantasyIsle4319 = 4319;
	private static final int FantasyIsle4320 = 4320;
	private static final int FantasyIsle4321 = 4321;
	private static final int FantasyIsle4322 = 4322;
	private static final int FantasyIsle4323 = 4323;
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		switch (npc.getNpcId())
		{
			case FantasyIsle4317:
				player.teleToLocation(-57530, -54527, -1581);
				break;
			case FantasyIsle4318:
				player.teleToLocation(-57628, -53278, -1688);
				break;
			case FantasyIsle4319:
				int chance = getRandom(4);
				switch (chance)
				{
					case 0:
						player.teleToLocation(-55349, -56301, -1118);
						break;
					case 1:
						player.teleToLocation(-55540, -56310, -1266);
						break;
					case 2:
						player.teleToLocation(-55640, -56313, -1302);
						break;
					case 3:
						player.teleToLocation(-55739, -56319, -1338);
						break;
					default:
						player.teleToLocation(-55739, -56319, -1338);
						break;
				}
				break;
			case FantasyIsle4320:
				player.teleToLocation(-55256, -58839, -1690);
				break;
			case FantasyIsle4321:
				player.teleToLocation(-58955, -59573, -1468);
				break;
			case FantasyIsle4322:
				player.teleToLocation(-61937, -59448, -1713);
				break;
			case FantasyIsle4323:
				player.teleToLocation(-61338, -57686, -1385);
				break;
		}
		return null;
	}
	
	private PhotoSnow(String name, String descr)
	{
		super(name, descr);
		
		addStartNpc(FantasyIsle4317, FantasyIsle4318, FantasyIsle4319, FantasyIsle4320, FantasyIsle4321, FantasyIsle4322, FantasyIsle4323);
		addTalkId(FantasyIsle4317, FantasyIsle4318, FantasyIsle4319, FantasyIsle4320, FantasyIsle4321, FantasyIsle4322, FantasyIsle4323);
	}
	
	public static void main(String[] args)
	{
		new PhotoSnow("PhotoSnow", "fantasy_isle");
	}
}