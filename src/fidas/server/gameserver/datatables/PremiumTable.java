package fidas.server.gameserver.datatables;

import fidas.server.Config;
import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.instancemanager.ExpirableServicesManager;
import fidas.server.gameserver.instancemanager.ExpirableServicesManager.ServiceType;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.holders.ItemHolder;
import fidas.server.gameserver.network.serverpackets.ExBrPremiumState;
import fidas.server.gameserver.scripting.scriptengine.listeners.player.PlayerDespawnListener;
import fidas.server.gameserver.scripting.scriptengine.listeners.player.PlayerSpawnListener;
import fidas.server.gameserver.util.Util;
import fidas.server.util.TimeConstant;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PremiumTable
{
	private static Logger _log = Logger.getLogger(PremiumTable.class.getName());

	public static float PREMIUM_BONUS_EXP = 1;
	public static float PREMIUM_BONUS_SP = 1;
	
	private Map<L2PcInstance, String> _requesters;

	//SQL DEFINITIONS
	private static String LOAD_RECORD = "SELECT expiration FROM character_services WHERE charId = ? AND serviceName = ?";
	private static String INSERT_RECORD = "REPLACE INTO character_services(charId, serviceName, expiration) VALUES (?,?,?)";
	private static String REMOVE_RECORD = "DELETE FROM character_services WHERE charId = ? AND serviceName = ?";

	public PremiumTable()
	{
		if (Config.PREMIUM_SERVICE_ENABLED)
		{
			registerListeners();

			PREMIUM_BONUS_EXP = Math.max((Config.PREMIUM_RATE_XP / Config.RATE_XP), 1);
			PREMIUM_BONUS_SP = Math.max((Config.PREMIUM_RATE_SP / Config.RATE_SP), 1);
			_requesters = new HashMap<>();
		}
	}

	/** 
	 * Register listeners for player's enter world and logout
	 */
	private void registerListeners()
	{
		new PlayerSpawnListener()
		{
			/** 
		 	* Send Premium State packet, to player with enabled premium service; send notification, if premium service will expire soon
	 		* @param player player to send info
	 		*/
			@Override
			public void onSpawn(L2PcInstance player)
			{
				if (player.hasPremium())
				{
					if (Config.SHOW_PREMIUM_STATUS)
					{
						player.sendPacket(new ExBrPremiumState(player.getObjectId(), 1));
					}

					if (Config.NOTIFY_PREMIUM_EXPIRATION && !ExpirableServicesManager.getInstance().isServiceUnlimited(ServiceType.PREMIUM, player))
					{
						Date testDate = new Date(ExpirableServicesManager.getInstance().getExpirationDate(ServiceType.PREMIUM, player));
						if (Util.isToday(testDate))
						{
							player.sendMessage("Warning! Premium service will expire today");
						}
						else if (Util.isTomorrow(testDate))
						{
							player.sendMessage("Warning! Premium service will expire tomorrow");
						}
					}
				}		
			}		
		};

		/** 
	 	* Unregister player's premium service at logout
 		* @param player player to send info
 		*/
		new PlayerDespawnListener()
		{
			@Override
			public void onDespawn(L2PcInstance player)
			{
				ExpirableServicesManager.getInstance().expireService(ServiceType.PREMIUM, player);
			}
		};
	}

	/** 
	 * Load player's premium state from database
	 * @param player player to load info
	 */
	public static void loadState(L2PcInstance player)
	{
		if (!Config.PREMIUM_SERVICE_ENABLED || (player == null))
		{
			return;
		}

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(LOAD_RECORD);
			statement.setInt(1, player.getObjectId());
			statement.setString(2, ServiceType.PREMIUM.toString());
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				long expirationDate = rset.getLong("expiration");

				if ((expirationDate < 0) || (expirationDate > System.currentTimeMillis()))
				{
					ExpirableServicesManager.getInstance().registerService(ServiceType.PREMIUM, player, expirationDate);
				}
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(PremiumTable.class.getName() + ": Error while loading character premium data for character " + player.getName() + ": " + e);
		}	
	}

	/** 
	 * Extends premium period for given time
	 * @param player player to operate
	 * @param millisCount time in milliseconds
	 * @param unlimited {@code true} if service should be time-unlimited	 
	 * @param store {@code true} if premium state should be stored in database, {@code false} if it will expire at logout
	 * @return 
	 */
	private static boolean addTime(L2PcInstance player, long millisCount, boolean unlimited, boolean store)
	{
		if (!Config.PREMIUM_SERVICE_ENABLED || (player == null))
		{
			return false;
		}

		// Unlimited premium service will newer touched. There is also no need to touch parameter, if no-store is choosen for already enabled service
		if (ExpirableServicesManager.getInstance().isServiceUnlimited(ServiceType.PREMIUM, player) || (ExpirableServicesManager.getInstance().hasService(ServiceType.PREMIUM, player)) && !store)
		{
			return false;
		}

		long expirationDate;
		boolean success = !store;

		if (unlimited)
		{
			expirationDate = -1;
		}

		else if (!store)
		{
			expirationDate = 0;
		}

		else
		{
			expirationDate = Math.max(ExpirableServicesManager.getInstance().getExpirationDate(ServiceType.PREMIUM, player), System.currentTimeMillis()) + millisCount;
		}

		// Store info in database, if needed
		if (store)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement(INSERT_RECORD);
				statement.setInt(1, player.getObjectId());
				statement.setString(2, ServiceType.PREMIUM.toString());
				statement.setLong(3, expirationDate);
				statement.executeUpdate();
				statement.close();
				success = true;			
			}
			catch (Exception e)
			{
				_log.warning(PremiumTable.class.getName() + ":  Could not save data for character " + player.getName() + ": " + e);
			}
		}

		// if store was successfull, or store doesn't required
		if (success)
		{
			// register service
			ExpirableServicesManager.getInstance().registerService(ServiceType.PREMIUM, player, expirationDate);

			// Send Premium packet, if needed
			if (Config.SHOW_PREMIUM_STATUS)
			{
				player.sendPacket(new ExBrPremiumState(player.getObjectId(), 1));
			}

			// Send text message
			player.sendMessage("Premium Service is activated");			
		}

		return success;
	}

	/** 
	 * Public wparrer - extends premium period for given time, store state in database
	 * @param player player to operate
	 * @param millisCount time in milliseconds
	 * @return 
	 */
	public static boolean addTime (L2PcInstance player, long millisCount)
	{
		if (millisCount <= 0)
		{
			return false;
		}

		return addTime(player, millisCount, false, true);
	}

	/** 
	 * Public wparrer - extends premium period for given time, store state in database
	 * @param player player to operate
	 * @param val type of time period	 
	 * @param count number of given period
	 * @return 
	 */
	public static boolean addTime(L2PcInstance player, TimeConstant val, int count)
	{
		return addTime(player, val.getTimeInMillis() * count, false, true);
	}

	/** 
	 * Public wparrer - extends premium period for unlimited time, store state in database
	 * @param player player to operate
	 * @return 
	 */
	public static boolean setUnlimitedPremium(L2PcInstance player)
	{
		return addTime(player, 0, true, true);
	}

	/** 
	 * Public wparrer - give temporary premium, expiring at logout
	 * @param player player to operate
	 * @return 
	 */
	public static boolean setTemporaryPremium(L2PcInstance player)
	{
		return addTime(player, 0, false, false);
	}

	/** 
	 * Unregister premium service from given player ant remove it from database
	 * @param player player to operate
	 */
	public static void removeService(L2PcInstance player)
	{
		if ((player == null) || !player.hasPremium())
		{
			return;
		}

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(REMOVE_RECORD);
			statement.setInt(1, player.getObjectId());
			statement.setString(2, ServiceType.PREMIUM.toString());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(PremiumTable.class.getName() + ":  Could not remove data for character " + player.getName() + ": " + e);
		}

		ExpirableServicesManager.getInstance().expireService(ServiceType.PREMIUM, player);
	}

	public static ItemHolder getPrice(String period)
	{
		if (!Config.PREMIUM_PRICE.containsKey(period)) // There is no exact period in config
		{
			if (Config.PREMIUM_SMART_PRICING) // but calculation for non-existing time is allowed
			{
				String basePeriod = "1" + period.substring(period.length() - 1, period.length());
				if (Config.PREMIUM_PRICE.containsKey(basePeriod)) //and config contains price for base period 
				{
					ItemHolder baseItem = Config.PREMIUM_PRICE.get(basePeriod);
					int periodCount = Integer.parseInt(period.substring(0, period.length() - 1));
					return new ItemHolder(baseItem.getId(), baseItem.getCount() * periodCount);
				}
			}
		}
		else
		{
			return Config.PREMIUM_PRICE.get(period);
		}
		
		return null;
	}
	
	public static boolean givePremium(L2PcInstance player, String period, L2Character seller)
	{
		ItemHolder payItem = getPrice(period);
		if ((player == null) || !player.isOnline() || (payItem == null))
		{
			return false;
		}

		if ((payItem.getCount() > 0) && (player.getInventory().getInventoryItemCount(payItem.getId(), -1, false) < payItem.getCount()))
		{
			player.sendMessage("Not enough items to use");
		}

		if (addTime(player, Util.toMillis(period)))
		{
			player.destroyItemByItemId("Premium service", payItem.getId(), payItem.getCount(), seller, true);
			return true;
		}
		
		return false; 
	}

	public void addRequest(L2PcInstance player, String period)
	{
		_requesters.put(player, period);
	}
	
	public void completeRequest(L2PcInstance player, int answer)
	{
		if ((answer == 1) && _requesters.containsKey(player))
		{
			givePremium(player, _requesters.get(player), player);
		}

		if (_requesters.containsKey(player))
		{
			_requesters.remove(player);
		}
	}

	public static final PremiumTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final PremiumTable _instance = new PremiumTable();
	}
}