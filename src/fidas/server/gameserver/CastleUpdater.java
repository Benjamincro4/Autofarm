package fidas.server.gameserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import fidas.server.Config;
import fidas.server.gameserver.instancemanager.CastleManager;
import fidas.server.gameserver.model.L2Clan;
import fidas.server.gameserver.model.entity.Castle;
import fidas.server.gameserver.model.itemcontainer.ItemContainer;

public class CastleUpdater implements Runnable
{
	protected static final Logger _log = Logger.getLogger(CastleUpdater.class.getName());
	private final L2Clan _clan;
	private int _runCount = 0;
	
	public CastleUpdater(L2Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}
	
	@Override
	public void run()
	{
		try
		{
			// Move current castle treasury to clan warehouse every 2 hour
			ItemContainer warehouse = _clan.getWarehouse();
			if ((warehouse != null) && (_clan.getCastleId() > 0))
			{
				Castle castle = CastleManager.getInstance().getCastleById(_clan.getCastleId());
				if (!Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					if ((_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE) == 0)
					{
						castle.saveSeedData();
						castle.saveCropData();
						if (Config.DEBUG)
						{
							_log.info("Manor System: all data for " + castle.getName() + " saved");
						}
					}
				}
				CastleUpdater cu = new CastleUpdater(_clan, ++_runCount);
				ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
}