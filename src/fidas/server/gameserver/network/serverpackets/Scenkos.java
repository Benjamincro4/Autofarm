package fidas.server.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import fidas.server.Config;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.clientpackets.Say2;

public final class Scenkos
{
	private static Logger _log = Logger.getLogger(Scenkos.class.getName());
	
	public static void toPlayersTargettingMyself(L2Character character, L2GameServerPacket mov)
	{
		if (Config.DEBUG)
		{
			_log.fine("players to notify:" + character.getKnownList().getKnownPlayers().size());
		}
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		// synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (player.getTarget() != character)
				{
					continue;
				}
				
				player.sendPacket(mov);
			}
		}
	}
	
	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (Config.DEBUG)
		{
			_log.fine("players to notify:" + character.getKnownList().getKnownPlayers().size());
		}
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		// synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (player == null)
				{
					continue;
				}
				try
				{
					player.sendPacket(mov);
					if ((mov instanceof CharInfo) && (character instanceof L2PcInstance))
					{
						int relation = ((L2PcInstance) character).getRelation(player);
						Integer oldrelation = character.getKnownList().getKnownRelations().get(player.getObjectId());
						if ((oldrelation != null) && (oldrelation != relation))
						{
							player.sendPacket(new RelationChanged((L2PcInstance) character, relation, character.isAutoAttackable(player)));
							if (((L2PcInstance) character).getSummon() != null)
							{
								player.sendPacket(new RelationChanged(((L2PcInstance) character).getSummon(), relation, character.isAutoAttackable(player)));
							}
						}
					}
				}
				catch (NullPointerException e)
				{
					_log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
	}
	
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 1500;
		}
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		// synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (character.isInsideRadius(player, radius, false, false))
				{
					player.sendPacket(mov);
				}
			}
		}
	}
	
	public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}
		
		toKnownPlayers(character, mov);
	}
	
	// To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, long radiusSq)
	{
		if (radiusSq < 0)
		{
			radiusSq = 360000;
		}
		
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		// synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if ((player != null) && (character.getDistanceSq(player) <= radiusSq))
				{
					player.sendPacket(mov);
				}
			}
		}
	}
	
	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		if (Config.DEBUG)
		{
			_log.fine("Players to notify: " + L2World.getInstance().getAllPlayersCount());
		}
		
		L2PcInstance[] pls = L2World.getInstance().getAllPlayersArray();
		// synchronized (L2World.getInstance().getAllPlayers())
		{
			for (L2PcInstance onlinePlayer : pls)
			{
				if ((onlinePlayer != null) && onlinePlayer.isOnline())
				{
					onlinePlayer.sendPacket(mov);
				}
			}
		}
	}
	
	public static void announceToOnlinePlayers(String text)
	{
		CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		toAllOnlinePlayers(cs);
	}
	
	public static void toPlayersInInstance(L2GameServerPacket mov, int instanceId)
	{
		L2PcInstance[] pls = L2World.getInstance().getAllPlayersArray();
		// synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance onlinePlayer : pls)
			{
				if ((onlinePlayer != null) && onlinePlayer.isOnline() && (onlinePlayer.getInstanceId() == instanceId))
				{
					onlinePlayer.sendPacket(mov);
				}
			}
		}
	}
}
