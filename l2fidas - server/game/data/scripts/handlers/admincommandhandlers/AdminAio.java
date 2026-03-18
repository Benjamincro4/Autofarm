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
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.EtcStatusUpdate;

/**
 * Give / Take Status Aio to Player Changes name color and title color if enabled Uses: setaio [<player_name>] [<time_duration in days>] removeaio [<player_name>] If <player_name> is not specified, the current target player is used.
 * @author KhayrusS
 */
public class AdminAio implements IAdminCommandHandler
{
	private final static Logger _log = Logger.getLogger(AdminAio.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_setaio",
		"admin_removeaio"
	};
	
	private enum CommandEnum
	{
		admin_setaio,
		admin_removeaio
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		/*
		 * if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){ return false; } if(Config.GMAUDIT) { Logger _logAudit = Logger.getLogger("gmaudit"); LogRecord record = new LogRecord(Level.INFO, command); record.setParameters(new Object[] { "GM: " +
		 * activeChar.getName(), " to target [" + activeChar.getTarget() + "] " }); _logAudit.log(record); }
		 */
		
		StringTokenizer st = new StringTokenizer(command);
		
		CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_setaio:
			{
				
				boolean no_token = false;
				
				if (st.hasMoreTokens())
				{ // char_name not specified
				
					String char_name = st.nextToken();
				
					L2PcInstance player = L2World.getInstance().getPlayer(char_name);
					
					if (player != null)
					{
						
						if (st.hasMoreTokens()) // time
						{
							String time = st.nextToken();
							
							try
							{
								int value = Integer.parseInt(time);
								
								if (value > 0)
								{
									
									doAio(activeChar, player, char_name, time);
									
									if (player.isAio())
									{
										return true;
									}
									
								}
								else
								{
									activeChar.sendMessage("O tempo deve ser maior que 0!");
									return false;
								}
								
							}
							catch (NumberFormatException e)
							{
								activeChar.sendMessage("O tempo deve ser um numero!");
								return false;
							}
							
						}
						else
						{
							no_token = true;
						}
						
					}
					else
					{
						activeChar.sendMessage("O jogador deve estar online para definir o status AIO");
						no_token = true;
					}
					
				}
				else
				{
					
					no_token = true;
					
				}
				
				if (no_token)
				{
					activeChar.sendMessage("Use o comando: //setaio <char_name> [time](em dias)");
					return false;
				}
				
			}
			case admin_removeaio:
			{
				
				boolean no_token = false;
				
				if (st.hasMoreTokens())
				{ // char_name
				
					String char_name = st.nextToken();
					
					L2PcInstance player = L2World.getInstance().getPlayer(char_name);
					
					if (player != null)
					{
						
						removeAio(activeChar, player, char_name);
						
						if (!player.isAio())
						{
							return true;
						}
						
					}
					else
					{
						
						activeChar.sendMessage("O jogador deve estar online para remover o status AIO");
						no_token = true;
					}
					
				}
				else
				{
					no_token = true;
				}
				
				if (no_token)
				{
					activeChar.sendMessage("Use o comando: //removeaio <char_name>");
					return false;
				}
				
			}
		}
		
		return true;
	}
	public void doAio(L2PcInstance activeChar, L2PcInstance _player, String _playername, String _time)
	{
		int days = Integer.parseInt(_time);
		if (_player == null)
		{
			activeChar.sendMessage("not found char" + _playername);
			return;
		}
		
		if (days > 0)
		{
			_player.setAio(true);
			_player.setEndTime("aio", days);
			_player.getStat().addExp(_player.getStat().getExpForLevel(81));
			
	    	   try (Connection connection = L2DatabaseFactory.getInstance().getConnection())
	    	   {
				PreparedStatement statement = connection.prepareStatement("UPDATE characters SET aio=1, aio_end=? WHERE CharId=?");
				statement.setLong(1, _player.getAioEndTime());
				statement.setInt(2, _player.getObjectId());
				statement.execute();
				statement.close();
				connection.close();
				
				if (Config.ALLOW_AIO_NCOLOR && activeChar.isAio())
				{
					_player.getAppearance().setNameColor(Config.AIO_NCOLOR);
				}
				
				if (Config.ALLOW_AIO_TCOLOR && activeChar.isAio())
				{
					_player.getAppearance().setTitleColor(Config.AIO_TCOLOR);
				}
				
				if (Config.ALLOW_AIO_ITEM && activeChar.isAio())
				{
					_player.getInventory().addItem("", Config.AIO_ITEMID, 1, _player, null);
				}
				
				_player.rewardAioSkills();
				
				_player.broadcastUserInfo();
				_player.sendPacket(new EtcStatusUpdate(_player));
				_player.sendSkillList();
				GmListTable.broadcastMessageToGMs("[Admin] " + activeChar.getName() + " colocar como AIO o jogador " + _playername + " por " + _time + " dia(s)");
				_player.sendMessage("Voce agora e um Aio, Parabens!");
				_player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
				
				_log.log(Level.WARNING, "nao foi possivel colocar o char como AIO:", e);
			}
		}
		else
		{
			removeAio(activeChar, _player, _playername);
		}
	}
	
	public void removeAio(L2PcInstance activeChar, L2PcInstance _player, String _playername)
	{
		_player.setAio(false);
		_player.setAioEndTime(0);
		
 	   try (Connection connection = L2DatabaseFactory.getInstance().getConnection())
 	   {
			PreparedStatement statement = connection.prepareStatement("UPDATE characters SET aio=0, aio_end=0 WHERE CharId=?");
			statement.setInt(1, _player.getObjectId());
			statement.execute();
			statement.close();
			connection.close();
			
			if (Config.ALLOW_AIO_ITEM && (_player.isAio() == false))
			{
				_player.getInventory().destroyItemByItemId("", Config.AIO_ITEMID, 1, _player, null);
			}
			_player.getWarehouse().destroyItemByItemId("", Config.AIO_ITEMID, 1, _player, null);
			
			_player.lostAioSkills();
			
			_player.getAppearance().setNameColor(0xFFFFFF);
			_player.getAppearance().setTitleColor(0xFFFFFF);
			_player.broadcastUserInfo();
			_player.sendPacket(new EtcStatusUpdate(_player));
			_player.sendSkillList();
			GmListTable.broadcastMessageToGMs("[Admin] " + activeChar.getName() + " remover o status de AIO do jogador " + _playername);
			_player.sendMessage("Agora voce nao e um Aio..");
			_player.broadcastUserInfo();
		}
		catch (Exception e)
		{
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
			
			_log.log(Level.WARNING, "nao pode remover status de AIO do jogador:", e);
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}