package fidas.server.gameserver.instancemanager;

import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Castle;
import fidas.server.gameserver.model.zone.L2ZoneRespawn;
import fidas.server.gameserver.model.zone.L2ZoneType;
import fidas.server.gameserver.model.zone.type.L2TownZone;

public class TownManager
{
	public static final int getTownCastle(int townId)
	{
		switch (townId)
		{
			case 912:
				return 1;
			case 916:
				return 2;
			case 918:
				return 3;
			case 922:
				return 4;
			case 924:
				return 5;
			case 926:
				return 6;
			case 1538:
				return 7;
			case 1537:
				return 8;
			case 1714:
				return 9;
			default:
				return 0;
		}
	}
	
	public static final boolean townHasCastleInSiege(int townId)
	{
		int castleIndex = getTownCastle(townId);
		
		if (castleIndex > 0)
		{
			Castle castle = CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
			if (castle != null)
			{
				return castle.getSiege().getIsInProgress();
			}
		}
		return false;
	}
	
	public static final boolean townHasCastleInSiege(int x, int y)
	{
		return townHasCastleInSiege(MapRegionManager.getInstance().getMapRegionLocId(x, y));
	}
	
	public static final L2TownZone getTown(int townId)
	{
		for (L2TownZone temp : ZoneManager.getInstance().getAllZones(L2TownZone.class))
		{
			if (temp.getTownId() == townId)
			{
				return temp;
			}
		}
		return null;
	}
	
	/**
	 * Returns the town at that position (if any)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static final L2TownZone getTown(int x, int y, int z)
	{
		for (L2ZoneType temp : ZoneManager.getInstance().getZones(x, y, z))
		{
			if (temp instanceof L2TownZone)
			{
				return (L2TownZone) temp;
			}
		}
		return null;
	}

    /**
     * @param player
     * @return
     */
    public static L2ZoneRespawn getClosestTownNumber(L2PcInstance player)
    {
        // TODO Auto-generated method stub
        return null;
    }
}