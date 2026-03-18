package fidas.server.gameserver.custom;

import java.util.Set;

import fidas.server.Config;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

public class PvPColorSystem
{
	public void updateNameColor(L2PcInstance player)
	{
		Set<Integer> pvpscolors = Config.PVPS_COLORS_LIST.keySet();
		for (Integer i : pvpscolors)
		{
			if (player.getPvpKills() >= i)
			{
				player.getAppearance().setNameColor(Config.PVPS_COLORS_LIST.get(i));
			}
		}
	}
	public void updateTitleColor(L2PcInstance player)
	{
		Set<Integer> pkscolors = Config.PKS_COLORS_LIST.keySet();
		for (Integer i : pkscolors)
		{
			if (player.getPkKills() >= i)
			{
				player.getAppearance().setTitleColor(Config.PKS_COLORS_LIST.get(i));
			}
		}
	}
}