package fidas.server.gameserver.instancemanager;

import java.util.Map;

import javolution.util.FastMap;
import fidas.server.Config;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.interfaces.IL2Procedure;
import fidas.server.gameserver.network.L2GameClient;
import fidas.server.util.L2FastMap;
import fidas.server.util.L2HashMap;

public class AntiFeedManager
{
	public static final int GAME_ID = 0;
	public static final int OLYMPIAD_ID = 1;
	public static final int TVT_ID = 2;
	public static final int L2EVENT_ID = 3;
	public static final int TVT_ROUND_ID = 4;

	private final Map<Integer, Long> _lastDeathTimes = new L2FastMap<>(true);
	private final L2HashMap<Integer, Map<Integer, Connections>> _eventIPs = new L2HashMap<>();
	
	protected AntiFeedManager()
	{
	
	}
	
	/**
	 * Set time of the last player's death to current
	 * @param objectId Player's objectId
	 */
	public final void setLastDeathTime(int objectId)
	{
		_lastDeathTimes.put(objectId, System.currentTimeMillis());
	}
	
	/**
	 * Check if current kill should be counted as non-feeded.
	 * @param attacker Attacker character
	 * @param target Target character
	 * @return True if kill is non-feeded.
	 */
	public final boolean check(L2Character attacker, L2Character target)
	{
		if (!Config.L2JMOD_ANTIFEED_ENABLE)
		{
			return true;
		}
		
		if (target == null)
		{
			return false;
		}
		
		final L2PcInstance targetPlayer = target.getActingPlayer();
		if (targetPlayer == null)
		{
			return false;
		}
		
		if ((Config.L2JMOD_ANTIFEED_INTERVAL > 0) && _lastDeathTimes.containsKey(targetPlayer.getObjectId()))
		{
			if ((System.currentTimeMillis() - _lastDeathTimes.get(targetPlayer.getObjectId())) < Config.L2JMOD_ANTIFEED_INTERVAL)
			{
				return false;
			}		
		}
		
		if (Config.L2JMOD_ANTIFEED_DUALBOX && (attacker != null))
		{
			final L2PcInstance attackerPlayer = attacker.getActingPlayer();
			if (attackerPlayer == null)
			{
				return false;
			}
			
			final L2GameClient targetClient = targetPlayer.getClient();
			final L2GameClient attackerClient = attackerPlayer.getClient();
			if ((targetClient == null) || (attackerClient == null) || targetClient.isDetached() || attackerClient.isDetached())
			{
				// unable to check ip address
				return !Config.L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX;
			}
			
			return !targetClient.getConnectionAddress().equals(attackerClient.getConnectionAddress());
		}
		
		return true;
	}
	
	/**
	 * Clears all timestamps
	 */
	public final void clear()
	{
		_lastDeathTimes.clear();
	}
	
	/**
	 * Register new event for dualbox check. Should be called only once.
	 * @param eventId
	 */
	public final void registerEvent(int eventId)
	{
		if (!_eventIPs.containsKey(eventId))
		{
			_eventIPs.put(eventId, new FastMap<Integer, Connections>());
		}
	}

	/**
	 * 
	 * @param eventId
	 * @param player
	 * @param max
	 * @return If number of all simultaneous connections from player's IP address lower than max then increment connection count and return true.<br>
	 *         False if number of all simultaneous connections from player's IP address higher than max.
	 */
	public final boolean tryAddPlayer(int eventId, L2PcInstance player, int max)
	{
		return tryAddClient(eventId, player.getClient(), max, false);
	}
	
	/**
	 * @param eventId
	 * @param client
	 * @param max
	 * @param isVip 
	 * @return If number of all simultaneous connections from player's IP address lower than max then increment connection count and return true.<br>
	 *         False if number of all simultaneous connections from player's IP address higher than max.
	 */
	public final boolean tryAddClient(int eventId, L2GameClient client, int max, boolean isVip)
	{
		if (client == null)
		{
			return false; // unable to determine IP address
		}

		final Map<Integer, Connections> event = _eventIPs.get(eventId);
		if (event == null)
		{
			return false; // no such event registered
		}

		final Integer addrHash = Integer.valueOf(client.getConnectionAddress().hashCode());

		// Whitelist semantics:
		// 0 or negative = unlimited connections for this IP.
		if (Config.L2JMOD_DUALBOX_CHECK_WHITELIST.containsKey(addrHash))
		{
			final int whitelistValue = Config.L2JMOD_DUALBOX_CHECK_WHITELIST.get(addrHash);

			if (whitelistValue <= 0)
			{
				return true;
			}

			max += whitelistValue;
		}

		Connections conns;
		synchronized (event)
		{
			conns = event.get(addrHash);
			if (conns == null)
			{
				conns = new Connections();
				event.put(addrHash, conns);
			}
		}

		return conns.testAndIncrement(max, isVip);
	}

	/**
	 * Decreasing number of active connection from player's IP address
	 * @param eventId
	 * @param player
	 * @return true if success and false if any problem detected.
	 */
	public final boolean removePlayer(int eventId, L2PcInstance player)
	{
		final L2GameClient client = player.getClient();
		if (client == null)
		{
			return false; // unable to determine IP address
		}

		final Map<Integer, Connections> event = _eventIPs.get(eventId);
		if (event == null)
		{
			return false; // no such event registered
		}

		final Integer addrHash = Integer.valueOf(client.getConnectionAddress().hashCode());
		Connections conns = event.get(addrHash);
		if (conns == null)
		{
			return false; // address not registered
		}

		synchronized (event)
		{
			if (conns.testAndDecrement())
			{
				event.remove(addrHash);
			}
		}

		return true;
	}

	/**
	 * Remove player connection IP address from all registered events lists.
	 * @param client
	 */
	public final void onDisconnect(L2GameClient client)
	{
		if (client == null)
		{
			return;
		}

		final Integer addrHash = Integer.valueOf(client.getConnectionAddress().hashCode());
		_eventIPs.executeForEachValue(new DisconnectProcedure(addrHash));
	}
	
	/**
	 * Clear all entries for this eventId.
	 * @param eventId
	 */
	public final void clear(int eventId)
	{
		final Map<Integer, Connections> event = _eventIPs.get(eventId);
		if (event != null)
		{
			event.clear();
		}
	}

	/**
	 * @param player
	 * @param max
	 * @return maximum number of allowed connections (whitelist + max)
	 */
	public final int getLimit(L2PcInstance player, int max)
	{
		return getLimit(player.getClient(), max);
	}

	/**
	 * @param client
	 * @param max
	 * @return maximum number of allowed connections (whitelist + max)
	 */
	public final int getLimit(L2GameClient client, int max)
	{
		if (client == null)
		{
			return max;
		}

		final Integer addrHash = Integer.valueOf(client.getConnectionAddress().hashCode());
		
		if (Config.L2JMOD_DUALBOX_CHECK_WHITELIST.containsKey(addrHash))
		{
			final int whitelistValue = Config.L2JMOD_DUALBOX_CHECK_WHITELIST.get(addrHash);

			// 0 or negative = unlimited
			if (whitelistValue <= 0)
			{
				return -1;
			}

			return max + whitelistValue;
		}

		return max;
	}
	
	protected static final class Connections
	{
		private int _num = 0;

		/**
		 * and false if maximum number is reached.
		 * @param max 
		 * @param isVip 
		 * @return true if successfully incremented number of connections
		 */
		public final synchronized boolean testAndIncrement(int max, boolean isVip)
		{
		    if (isVip)
		    {
		        _num++;
		        return true;
		    }

		    if (_num < max)
		    {
		        _num++;
		        return true;
		    }

		    return false;
		}

		/**
		 * @return true if all connections are removed
		 */
		public final synchronized boolean testAndDecrement()
		{
			if (_num > 0)
			{
				_num--;
			}
			
			return _num == 0;
		}
	}

	private static final class DisconnectProcedure implements IL2Procedure<Map<Integer, Connections>>
	{
		private final Integer _addrHash;

		public DisconnectProcedure(Integer addrHash)
		{
			_addrHash = addrHash;
			
		}

		@Override
		public final boolean execute(Map<Integer, Connections> event)
		{
			final Connections conns = event.get(_addrHash);
			if (conns != null)
			{
				synchronized (event)
				{
					if (conns.testAndDecrement())
					{
						event.remove(_addrHash);
					}
				}
			}
			return true;
		}
	}
 	
	public static final AntiFeedManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AntiFeedManager _instance = new AntiFeedManager();
	}
}