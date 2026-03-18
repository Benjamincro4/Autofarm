package handlers.voicedcommandhandlers;

import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.instancemanager.TerritoryWarManager;
import fidas.server.gameserver.model.TerritoryWard;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Phear3d
 * @version 1.0 (TWGm.java)
 * .tw voiced command for GMs to locate Ward(s) & Player(s) holding Ward(s) during TW.
 */
public class TWGm implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = { "tw", "tw_goto_ward" };
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String target)
	{
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage( 0 );
		if (command.equalsIgnoreCase("tw_goto_ward") && target != null && activeChar.isGM())
		{
			final StringTokenizer st = new StringTokenizer(target);
			try
			{
				String str_x = null;String str_y = null;String str_z = null;
				int ward_x = 0;int ward_y = 0;int ward_z = 0;
				
				//_log.log(Level.INFO, "[TWGm] .tw_goto_ward " + target);
				
				if (st.hasMoreTokens())
				{
					str_x = st.nextToken();
					ward_x = Integer.parseInt(str_x);
				}
				if (st.hasMoreTokens())
				{
					str_y = st.nextToken();
					ward_y = Integer.parseInt(str_y);
				}
				if (st.hasMoreTokens())
				{
					str_z = st.nextToken();
					ward_z = Integer.parseInt(str_z);
				}
				
				if (ward_x != 0 && ward_y != 0 && ward_z != 0)
				{
					activeChar.teleToLocation(ward_x, ward_y, ward_z, false);
					activeChar.sendMessage("[TWGm] TeleTo Ward location: " + ward_x + " " + ward_y + " " + ward_z);
				}
				return true;
			}
	    	catch (Exception e)
	    	{
				_log.log(Level.WARNING, "TWGm.java .voicedhandler: exception: " + e.getMessage(), e);
				return false;
	    	}
		}
		else if (command.equalsIgnoreCase("tw") && activeChar.isGM())
		{
			try
			{
				//build beginning of html page
				StringBuilder sb = new StringBuilder();
				sb.append("<html><title>TW Wards GM Panel</title><body><br><center>-<font color=\"0066FF\">Current TW Ward List</font>-</center>");

				//get & build current Wardlist
				if (TerritoryWarManager.getInstance().isTWInProgress())
				{
					List<TerritoryWard> territoryWardList = TerritoryWarManager.getInstance().getAllTerritoryWards();
					for(TerritoryWard ward : territoryWardList)
					{
						//ward.getTerritoryId();
						if (ward.getNpc() != null)
						{
							sb.append("<table width=270><tr>");
							sb.append("<td width=135 ALIGN=\"LEFT\">" + ward.getNpc().getName() + "</td>");
							sb.append("<td width=135 ALIGN=\"RIGHT\"><button value=\"TeleTo\" action=\"bypass -h voice .tw_goto_ward " + ward.getNpc().getX() + " " + ward.getNpc().getY() +  " " + ward.getNpc().getZ() + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
							sb.append("</tr></table>");
						}
						else if (ward.getPlayer() != null)
						{
							sb.append("<table width=270><tr>");
							sb.append("<td width=135 ALIGN=\"LEFT\">" + getWardName(ward.getTerritoryId()) + " - " + ward.getPlayer().getName() + "</td>");
							sb.append("<td width=135 ALIGN=\"RIGHT\"><button value=\"TeleTo\" action=\"bypass -h voice .tw_goto_ward " + ward.getPlayer().getX() + " " + ward.getPlayer().getY() +  " " + ward.getPlayer().getZ() + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
							sb.append("</tr></table>");
						}
					}
			    	sb.append("</body></html>");
					npcHtmlMessage.setHtml(sb.toString());
			    	activeChar.sendPacket(npcHtmlMessage);
				}
				else
				{
					sb.append("<br><br><center>The Ward List is empty!<br></center></body></html>");
			    	npcHtmlMessage.setHtml(sb.toString());
			    	activeChar.sendPacket(npcHtmlMessage);
			    	return true;
				}
			}
	    	catch (Exception e)
	    	{
				_log.log(Level.WARNING, "TWGm.java .voicedhandler: exception: " + e.getMessage(), e);
				return false;
	    	}
		}
		else
		{
			return false;
		}
		return true;
	}
	
	/**
	 * gets the Wards Name from its territoryID
	 * @param _terrID Integer:
	 * @return 
	 */
	private String getWardName(int _terrID)
	{
		String _theName = null;
		switch(_terrID)
		{
			case 81:
				_theName = "Gludio Ward";
				break;
			case 82:
				_theName = "Dion Ward";
				break;
			case 83:
				_theName = "Giran Ward";
				break;
			case 84:
				_theName = "Oren Ward";
				break;
			case 85:
				_theName = "Aden Ward";
				break;
			case 86:
				_theName = "Innadril Ward";
				break;
			case 87:
				_theName = "Goddard Ward";
				break;
			case 88:
				_theName = "Rune Ward";
				break;
			case 89:
				_theName = "Schuttgart Ward";
				break;
			default:
				_theName = "null";
				break;
		}
		return _theName;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
