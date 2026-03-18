package events;

import fidas.server.gameserver.model.quest.Quest;

public class EventsSpawn extends Quest
{
	private static final String qn = "EventsSpawn";
	
	/**
	 * Holly Cow
	 */
	private static final boolean HC_SPAWN = EventsConfig.HC_STARTED;
	private static final int Farmer = 13183;
	private static final int HC_SpawnLocs[][] =
	{
		{
			83000,
			147529,
			-3477,
			15689
		}, // Giran
		{
			147931,
			26598,
			-2200,
			19424
		}, // Aden
		{
			147341,
			-56846,
			-2783,
			1722
		}, // Goddard
		{
			44160,
			-48746,
			-799,
			30212
		}
	// Rune
	};
	/**
	 * L2 Day
	 */
	private static final boolean L2DAY_SPAWN = EventsConfig.L2DAY_STARTED;
	private static final int L2DAY_CAT = 31228;
	private static final int L2DAY_SpawnLocs[][] =
	{
		{
			82700,
			147529,
			-3477,
			15689
		}, // Giran
		{
			147880,
			26598,
			-2213,
			16383
		}, // Aden
		{
			147322,
			-56916,
			-2788,
			0
		}, // Goddard
		{
			44163,
			-48668,
			-805,
			29654
		}
	// Rune
	};
	/**
	 * Ninja Adventures
	 */
	private static final boolean NA_SPAWN = EventsConfig.NA_STARTED;
	private static final int Ninja_Master = 7102;
	private static final int NA_SpawnLocs[][] =
	{
		{
			82900,
			147529,
			-3477,
			15689
		}, // Giran
		{
			148548,
			26781,
			-2207,
			32767
		}, // Aden
		{
			148128,
			-57014,
			-2783,
			34490
		}, // Goddard
		{
			44623,
			-48385,
			-792,
			18476
		}
	// Rune
	};
	/**
	 * Trick or Transmutation
	 */
	private static final boolean TOT_SPAWN = EventsConfig.TOT_STARTED;
	private static final int SPECIAL_CHEST = 13036;
	private static final int TOT_SpawnLocs[][] =
	{
		{
			82800,
			147529,
			-3477,
			15689
		}, // Giran
		{
			148004,
			26604,
			-2200,
			16152
		}, // Aden
		{
			147374,
			-56785,
			-2783,
			63255
		}, // Goddard
		{
			44160,
			-48823,
			-799,
			29412
		}
	// Rune
	};
	/**
	 * HallowedYou
	 */
	private static final boolean HY_SPAWN = EventsConfig.HY_STARTED;
	private static final int HALLOWEEN_MANAGER = 7104;
	private static final int HY_SpawnLocs[][] =
	{
		{
			82887,
			149729,
			-3464,
			49648
		}, // Giran
		{
			148078,
			26597,
			-2200,
			16152
		}, // Aden
		{
			148117,
			-56895,
			-2776,
			32767
		}, // Goddard
		{
			44740,
			-48358,
			-792,
			17216
		}
	// Rune
	};
	/**
	 * ChristmasIsHere
	 */
	private static final boolean CH_SPAWN = EventsConfig.CH_STARTED;
	private static final int CHRISTMAS_SANTA = 7106;
	private static final int CH_SpawnLocs[][] =
	{
		{
			82817,
			149727,
			-3464,
			49648
		}, // Giran
		{
			148546,
			26660,
			-2200,
			32420
		}, // Aden
		{
			147400,
			-56730,
			-2776,
			63255
		}, // Goddard
		{
			44795,
			-48353,
			-792,
			17216
		}
	// Rune
	};
	/**
	 * Squash Event
	 */
	private static final boolean SQUASH_SPAWN = EventsConfig.SQUASH_STARTED;
	private static final int SQUASH_NPC = 31860;
	private static final int[][] SQUASH_SpawnLocs =
	{
		{
			82756,
			149727,
			-3477,
			48909
		}, // Giran
		{
			148126,
			26602,
			-2213,
			16152
		}, // Aden
		{
			148171,
			-57173,
			-2776,
			32767
		}, // Goddard
		{
			44368,
			-48386,
			-805,
			17216
		}, // Rune
	};
	
	/**
	 * School Day
	 */
	private static final boolean SD_SPAWN = EventsConfig.SD_STARTED;
	private static final int SD_NPC = 13182;
	private static final int SD_SpawnLocs[][] =
	{
		{
			82697,
			149727,
			-3464,
			46596
		}, // Giran
		{
			148543,
			26598,
			-2213,
			28799
		}, // Aden
		{
			147291,
			-57055,
			-2776,
			61312
		}, // Goddard
		{
			44212,
			-48951,
			-805,
			32461
		}
	// Rune
	};
	/**
	 * April Fools Day
	 */
	private static final boolean AP_SPAWN = EventsConfig.AP_STARTED;
	private static final int AP_NPC = 32639;
	private static final int AP_SpawnLocs[][] =
	{
		{
			82631,
			149722,
			-3477,
			46596
		}, // Giran
		{
			148178,
			29596,
			-2213,
			16152
		}, // Aden
		{
			148086,
			-56803,
			-2776,
			32767
		}, // Goddard
		{
			44482,
			-48399,
			-792,
			17216
		}
	// Rune
	};
	/**
	 * Super Star Event
	 */
	public static final boolean SS_SPAWN = EventsConfig.SS_STARTED;
	public static final int SS_NPC = 7107;
	public static final int SS_SpawnLocs[][] =
	{
		{
			83067,
			149724,
			-3464,
			46596
		}, // Giran
		{
			148552,
			27169,
			-2213,
			28799
		}, // Aden
		{
			148082,
			-56761,
			-2789,
			32767
		}, // Goddard
		{
			44433,
			-48396,
			-805,
			17216
		}
	// Rune
	};
	
	public EventsSpawn(int questId, String name, String descr)
	{
		super(questId, name, descr);
		if (EventsConfig.EVENTS_ENABLED)
		{
			checkEvent(HC_SPAWN, Farmer, HC_SpawnLocs);
			checkEvent(L2DAY_SPAWN, L2DAY_CAT, L2DAY_SpawnLocs);
			checkEvent(NA_SPAWN, Ninja_Master, NA_SpawnLocs);
			checkEvent(TOT_SPAWN, SPECIAL_CHEST, TOT_SpawnLocs);
			checkEvent(HY_SPAWN, HALLOWEEN_MANAGER, HY_SpawnLocs);
			checkEvent(CH_SPAWN, CHRISTMAS_SANTA, CH_SpawnLocs);
			checkEvent(SQUASH_SPAWN, SQUASH_NPC, SQUASH_SpawnLocs);
			checkEvent(SD_SPAWN, SD_NPC, SD_SpawnLocs);
			checkEvent(AP_SPAWN, AP_NPC, AP_SpawnLocs);
			checkEvent(SS_SPAWN, SS_NPC, SS_SpawnLocs);
		}
	}
	
	private void checkEvent(boolean event, int npcId, int[][] loc)
	{
		if (event)
		{
			try
			{
				SpawnEventNpc(npcId, loc);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	private void SpawnEventNpc(int npcId, int[][] loc)
	{
		for (int[] element : loc)
		{
			try
			{
				addSpawn(npcId, element[0], element[1], element[2], element[3], false, 0, false);
			}
			catch (Exception e)
			{
				_log.warning("EventNpcSpawn: Spawns could not be initialized: " + e);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new EventsSpawn(-1, qn, "events");
		_log.warning("Events System: Spawn Events Npcs");
	}
}