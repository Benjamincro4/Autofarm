package handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import fidas.server.Config;
import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.GmListTable;
import fidas.server.gameserver.handler.IAdminCommandHandler;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.EtcStatusUpdate;
import fidas.server.gameserver.network.serverpackets.SystemMessage;

public class AdminVip implements IAdminCommandHandler
{
       private static String[] _adminCommands = { "admin_setvip", "admin_removevip" };
       private final static Logger _log = Logger.getLogger(AdminVip.class.getName());

       @Override
       public boolean useAdminCommand(String command, L2PcInstance activeChar)
       {
    	   if (command.startsWith("admin_setvip"))
    	   {
    		   StringTokenizer str = new StringTokenizer(command);
    		   L2Object target = activeChar.getTarget();
    		   
    		   L2PcInstance player = null;
    		   SystemMessage sm = new SystemMessage(SystemMessageId.S1);
    		   
    		   if (target != null && target instanceof L2PcInstance)
    			   player = (L2PcInstance)target;
    		   else
    			   player = activeChar;
    		   
    		   try
    		   {
    			   str.nextToken();
    			   String time = str.nextToken();
    			   if (str.hasMoreTokens())
    			   {
    				   String playername = time;
    				   time = str.nextToken();
    				   player = L2World.getInstance().getPlayer(playername);
    				   doVip(activeChar, player, playername, time);
    			   }
    			   else
    			   {
    				   String playername = player.getName();
    				   doVip(activeChar, player, playername, time);
    			   }
    			   if(!time.equals("0"))
    			   {
    				   sm.addString("You are now a Premium Member!");
    				   player.sendPacket(sm);
    			   }
    		   }
    		   catch(Exception e)
    		   {
    			   activeChar.sendMessage("Use: //setvip <char_name> [time](in days)");
    		   }
    		   
    		   player.broadcastUserInfo();
    		   if(player.isVip())
    			   return true;
    	   }
    	   else if(command.startsWith("admin_removevip"))
    	   {
    		   StringTokenizer str = new StringTokenizer(command);
    		   L2Object target = activeChar.getTarget();
    		   
    		   L2PcInstance player = null;

    		   if (target != null && target instanceof L2PcInstance)
    			   player = (L2PcInstance)target;
    		   else
    			   player = activeChar;
    		   
    		   try
    		   {
    			   str.nextToken();
    			   if (str.hasMoreTokens())
    			   {
    				   String playername = str.nextToken();
    				   player = L2World.getInstance().getPlayer(playername);
    				   removeVip(activeChar, player, playername);
    			   }
    			   else
    			   {
    				   String playername = player.getName();
    				   removeVip(activeChar, player, playername);
    			   }
    		   }
    		   catch(Exception e)
    		   {
    			   activeChar.sendMessage("Use: //removevip <char_name>");
    		   }
    		   player.broadcastUserInfo();
    		   if(!player.isVip())
    			   return true;
               }
               return false;
         	}

       public void doVip(L2PcInstance activeChar, L2PcInstance _player, String _playername, String _time)
       {
    	   int days = Integer.parseInt(_time);
    	   if (_player == null)
    	   {
    		   activeChar.sendMessage("Player not found:" + _playername);
    		   return;
               	}
    	   
    	   if(days > 0)
    	   {
    		   _player.setVip(true);
    		   _player.setEndTime("vip", days);
    		   
    		   try (Connection connection = L2DatabaseFactory.getInstance().getConnection())
    		   {
    			   PreparedStatement statement = connection.prepareStatement("UPDATE characters SET vip=1, vip_end=? WHERE CharId=?");
    			   statement.setLong(1, _player.getVipEndTime());
    			   statement.setInt(2, _player.getObjectId());
    			   statement.execute();
    			   statement.close();
    			   connection.close();
    			   
    			   if(Config.ALLOW_VIP_NCOLOR && activeChar.isVip())
    				   _player.getAppearance().setNameColor(Config.VIP_NCOLOR);
    			   
    			   if(Config.ALLOW_VIP_TCOLOR && activeChar.isVip())
    				   _player.getAppearance().setTitleColor(Config.VIP_TCOLOR);
    			   
    			   _player.rewardVipSkills();
    			   _player.broadcastUserInfo();
    			   _player.sendPacket(new EtcStatusUpdate(_player));
    			   _player.sendSkillList();
    			   GmListTable.broadcastMessageToGMs("GM "+ activeChar.getName()+ " give premium status to "+ _playername + " for " + _time + " day(s)");
    		   }
    		   catch (Exception e)
    		   {
    			   _log.log(Level.WARNING,"Not possible to give premium status to player:", e);
    		   }
    	   }
    	   else
    	   {
    		   removeVip(activeChar, _player, _playername);
    	   }
       }

       public void removeVip(L2PcInstance activeChar, L2PcInstance _player, String _playername)
       {
    	   _player.setVip(false);
    	   _player.setVipEndTime(0);
    	   
    	   try (Connection connection = L2DatabaseFactory.getInstance().getConnection())
    	   {
    		   PreparedStatement statement = connection.prepareStatement("UPDATE characters SET vip=0, vip_end=0 WHERE CharId=?");
    		   statement.setInt(1, _player.getObjectId());
    		   statement.execute();
    		   statement.close();
    		   connection.close();
    		   
    		   _player.getAppearance().setNameColor(0xFFFFFF);
    		   _player.getAppearance().setTitleColor(0xFFFFFF);
    		   _player.lostVipSkills();
    		   _player.broadcastUserInfo();
    		   _player.sendPacket(new EtcStatusUpdate(_player));
    		   _player.sendSkillList();
    		   GmListTable.broadcastMessageToGMs("GM "+activeChar.getName()+" remove premium status of player "+ _playername);
           	}
    	   catch (Exception e)
    	   {
    		   _log.log(Level.WARNING," cant remove premium status of player :", e);
    	   }
       }

       @Override
	public String[] getAdminCommandList()
       {
               return _adminCommands;
       }
}