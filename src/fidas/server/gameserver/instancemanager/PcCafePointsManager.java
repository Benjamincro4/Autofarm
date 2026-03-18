package fidas.server.gameserver.instancemanager;

import fidas.server.Config;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.base.ClassId;
import fidas.server.gameserver.model.zone.ZoneId;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.ExPCCafePointInfo;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.util.Rnd;

public class PcCafePointsManager
{
	private static PcCafePointsManager _instance;
	
	public static PcCafePointsManager getInstance()
	{
		if (_instance == null)
			_instance = new PcCafePointsManager();
		return _instance;
	}
	
	public PcCafePointsManager()
	{
	}
	
	public void givePcCafePoint(final L2PcInstance player, final long givedexp)
	{
		if (!Config.PC_BANG_ENABLED)
			return;
		
		if (player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.PVP) || player.isInsideZone(ZoneId.SIEGE) || player.isOnlineInt() == 0 || player.isInJail())
			return;
		
		if (player.getPcBangPoints() >= Config.MAX_PC_BANG_POINTS)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED);
			player.sendPacket(sm);
			return;
		}
		int _points = (int) (givedexp * 0.0001 * Config.PC_BANG_POINT_RATE);
		if (player.getActiveClass() == ClassId.archmage.getId() || player.getActiveClass() == ClassId.soultaker.getId() || player.getActiveClass() == ClassId.stormScreamer.getId() || player.getActiveClass() == ClassId.mysticMuse.getId())
			_points /= 2;
		
		if (Config.RANDOM_PC_BANG_POINT)
			_points = Rnd.get(_points / 2, _points);
		
		boolean doublepoint = false;
		SystemMessage sm = null;
		if (_points > 0)
		{
			if (Config.ENABLE_DOUBLE_PC_BANG_POINTS
					&& Rnd.get(100) < Config.DOUBLE_PC_BANG_POINTS_CHANCE)
			{
				_points *= 2;
				sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE);
				doublepoint = true;
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
			}
			if (player.getPcBangPoints() + _points > Config.MAX_PC_BANG_POINTS)
				_points = Config.MAX_PC_BANG_POINTS-player.getPcBangPoints();
			sm.addNumber(_points);
			player.sendPacket(sm);
			player.setPcBangPoints(player.getPcBangPoints() + _points);
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), _points, 1));
		}
	}
}