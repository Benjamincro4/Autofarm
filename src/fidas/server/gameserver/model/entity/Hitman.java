package fidas.server.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import javolution.util.FastMap;
import fidas.server.Config;
import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.datatables.CharNameTable;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

public class Hitman
{
	private static Hitman _instance;
	public FastMap <Integer, PlayerToAssasinate> _targets;
	public Logger _log = Logger.getLogger(Hitman.class.getName());
	
	// Data Strings
	private static String   SQL_SELECT  = "select targetId,clientId,target_name,bounty,pending_delete from hitman_list";
	private static String   SQL_DELETE  = "delete from hitman_list where targetId=?";
	private static String   SQL_SAVEING = "replace into `hitman_list` VALUES (?, ?, ?, ?, ?)";
	private static String[] SQL_OFFLINE = { "select * from characters where char_name=?", "select * from characters where charId=?" };

	// Clean every 15 mins ^^
	private int MIN_MAX_CLEAN_RATE = (15 * 60000);
	
	// Fancy lookin
	public static boolean start()
	{
		if(Config.ALLOW_HITMAN_GDE)
		{
			getInstance();
		}
		
		return _instance != null;
	}
	
	public static Hitman getInstance()
	{
		if(_instance == null)
		{
			_instance = new Hitman();
		}
		
		return _instance;
	}
	
	public Hitman()
	{
		_targets = load();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new AISystem(), MIN_MAX_CLEAN_RATE, MIN_MAX_CLEAN_RATE);
	}
	
	private FastMap<Integer, PlayerToAssasinate> load()
	{
		FastMap<Integer, PlayerToAssasinate> map = new FastMap<>();
		
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(SQL_SELECT);
			ResultSet rs = st.executeQuery();
			
			while(rs.next())
			{
				int targetId = rs.getInt("targetId");
				int clientId = rs.getInt("clientId");
				String target_name = rs.getString("target_name");
				int bounty = rs.getInt("bounty");
				boolean pending = rs.getInt("pending_delete") == 1;

				if(pending)
				{
					removeTarget(targetId, false);
				}
				else
				{
					map.put(targetId, new PlayerToAssasinate(targetId, clientId, bounty, target_name));
				}
			}
			_log.info("Hitman: "+map.size()+" Target Murder(s)");
			rs.close();
			st.close();
			con.close();
		}
		catch(Exception e)
		{
			_log.warning("Hitman: "+e.getCause());
			return new FastMap<>();
		}
		
		return map;
	}
	
	public void onDeath(L2PcInstance assassin, L2PcInstance target)
	{
		if(_targets.containsKey(target.getObjectId()))
		{
			PlayerToAssasinate pta = _targets.get(target.getObjectId());
			String name= getOfflineData(null, pta.getClientId())[1];
			L2PcInstance client = L2World.getInstance().getPlayer(name);
			
			target.sendMessage("You were murdered. Your reward is 0.");
			
			if(client != null)
			{
				client.sendMessage("Your assassination request to kill the player "+target.getName()+" was fulfilled.");
				client.setHitmanTarget(0);
			}
			
			assassin.sendMessage("You assassinated "+target.getName()+", your reward will be converted into Adena!");
			assassin.addAdena("Hitman", pta.getBounty(), target, true);
			removeTarget(pta.getObjectId(), true);
		}
	}
	
	public void onEnterWorld(L2PcInstance activeChar)
	{
		if(_targets.containsKey(activeChar.getObjectId()))
		{
			PlayerToAssasinate pta = _targets.get(activeChar.getObjectId());
			activeChar.sendMessage("There is a request to assassinate you. Reward worth " + pta.getBounty() + " Adena(s).");
		}
		
		if(activeChar.getHitmanTarget() > 0)
		{
			if(!_targets.containsKey(activeChar.getHitmanTarget()))
			{
				activeChar.sendMessage("Your target has been eliminated. Have a good day.");
				activeChar.setHitmanTarget(0);
			}
			else
			{
				activeChar.sendMessage("Your target is still on the run.");
			}
		}
	}
	
	public void save()
	{
		try
		{
			for(PlayerToAssasinate pta : _targets.values())
			{
				Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement st = con.prepareStatement(SQL_SAVEING);
				st.setInt(1, pta.getObjectId());
				st.setInt(2, pta.getClientId());
				st.setString(3, pta.getName());
				st.setInt(4, pta.getBounty());
				st.setInt(5, pta.isPendingDelete() ? 1 : 0);
				st.executeQuery();
				st.close();
				con.close();
			}
		}
		catch(Exception e)
		{
			_log.warning("Hitman: "+e);
		}
		System.out.println("Hitman: Saved list.");
	}
	
	public void putHitOn(L2PcInstance client, String playerName, int bounty)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(playerName);
		
		if(client.getHitmanTarget() > 0)
		{
			client.sendMessage("You have already made an assassination request, you can only place one request at a time.");
			return;
		}
		else if(client.getAdena() < bounty)
		{
			client.sendMessage("You don't have enough adena.");
			return;
		}
		else if((player == null) && CharNameTable.getInstance().doesCharNameExist(playerName))
		{
			Integer targetId = Integer.parseInt(getOfflineData(playerName, 0)[0]);

			if(_targets.containsKey(targetId))
			{
				client.sendMessage("There is already a hit on that player.");
				return;
			}
			_targets.put(targetId, new PlayerToAssasinate(targetId, client.getObjectId(), bounty, playerName));
			client.reduceAdena("Hitman", bounty, client, true);
			client.setHitmanTarget(targetId);
		}
		else if((player != null) && CharNameTable.getInstance().doesCharNameExist(playerName))
		{
			if(_targets.containsKey(player.getObjectId()))
			{
				client.sendMessage("There is already a hit on that player.");
				return;
			}
			player.sendMessage("There is a request to assassinate you. Reward of " + bounty + " Adena(s).");
			_targets.put(player.getObjectId(), new PlayerToAssasinate(player, client.getObjectId(), bounty));
			client.reduceAdena("Hitman", bounty, client, true);
			client.setHitmanTarget(player.getObjectId());
		}
		else
		{
			client.sendMessage("Invalid player name. The user does not exist.");
		}
	}
	
	public class AISystem implements Runnable
	{
		@Override
		public void run()
		{
			if(Config.DEBUG)
			{
				_log.info("Cleaning sequance initiated.");
			}
			
			for(PlayerToAssasinate target : _targets.values())
			{
				if(target.isPendingDelete())
				{
					removeTarget(target.getObjectId(), true);
				}
			}
			save();
		}
	}
	
	public void removeTarget(int obId, boolean live)
	{
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(SQL_DELETE);
			st.setInt(1, obId);
			st.execute();
			st.close();
			con.close();

			if(live)
			{
				_targets.remove(obId);
			}
		}
		catch(Exception e)
		{
			_log.warning("Hitman: "+e);
		}
	}
	
	public void cancelAssasination(String name, L2PcInstance client)
	{
		L2PcInstance target = L2World.getInstance().getPlayer(name);
		
		if(client.getHitmanTarget() <= 0)
		{
			client.sendMessage("You don't have a murder claim.");
			return;
		}
		else if((target == null) && CharNameTable.getInstance().doesCharNameExist(name))
		{
			PlayerToAssasinate pta = _targets.get(client.getHitmanTarget());
			
			if(!_targets.containsKey(pta.getObjectId()))
			{
				client.sendMessage("There is no agreement about this player.");
			}
			else if(pta.getClientId() == client.getObjectId())
			{
				removeTarget(pta.getObjectId(), true);
				client.sendMessage("The assassination order was cancelled.");
				client.setHitmanTarget(0);
			}
			else
			{
				client.sendMessage("You do not own this target!");
			}
		}
		else if((target != null) && CharNameTable.getInstance().doesCharNameExist(name))
		{
			PlayerToAssasinate pta = _targets.get(target.getObjectId());

			if(!_targets.containsKey(pta.getObjectId()))
			{
				client.sendMessage("There is no agreement about this player.");
			}
			else if(pta.getClientId() == client.getObjectId())
			{
				removeTarget(pta.getObjectId(), true);
				client.sendMessage("The assassination order was cancelled.");
				target.sendMessage("The order to assassinate you has been cancelled.");
				client.setHitmanTarget(0);
			}
			else
			{
				client.sendMessage("You do not own this target!");
			}
		}
		else
		{
			client.sendMessage("Invalid player name, the user does not exist.");
		}
	}
	
	/**
	 * Its useing a array in case in a future update more values will be added
	 * @param name
	 * @param objId 
	 * @return 
	 */
	public String[] getOfflineData(String name, int objId)
	{
		String[] set = new String[2];
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(objId > 0 ? SQL_OFFLINE[1] : SQL_OFFLINE[0]);

			if(objId > 0)
			{
				st.setInt(1, objId);
			}
			else
			{
				st.setString(1, name);
			}
			
			ResultSet rs = st.executeQuery();

			while(rs.next())
			{
				set[0] = String.valueOf(rs.getInt("charId"));
				set[1] = rs.getString("char_name");
			}

			rs.close();
			st.close();
			con.close();
		}
		catch(Exception e){
			_log.warning("Hitman: "+e);
		}
		
		return set;
	}

	public boolean exists(int objId)
	{
		return _targets.containsKey(objId);
	}
	
	public PlayerToAssasinate getTarget(int objId)
	{
		return _targets.get(objId);
	}
	
	/**
	 * @return the _targets
	 */
	public FastMap<Integer, PlayerToAssasinate> getTargets()
	{
		return _targets;
	}

	/**
	 * @param targets the _targets to set
	 */
	public void set_targets(FastMap<Integer, PlayerToAssasinate> targets)
	{
		_targets = targets;
	}
}