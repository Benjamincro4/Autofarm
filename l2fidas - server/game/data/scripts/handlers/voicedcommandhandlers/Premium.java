/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import fidas.server.Config;
import fidas.server.gameserver.datatables.ItemTable;
import fidas.server.gameserver.datatables.PremiumTable;
import fidas.server.gameserver.instancemanager.ExpirableServicesManager;
import fidas.server.gameserver.instancemanager.ExpirableServicesManager.ServiceType;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.holders.ItemHolder;
import fidas.server.gameserver.network.DialogId;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.ConfirmDlg;
import fidas.server.gameserver.util.Util;

import java.util.Date;

public class Premium implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"mypremium",
		"addpremium_1d",
		"addpremium_1w"
	};
	
	/**
	 * 
	 * @see fidas.server.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, fidas.server.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("mypremium"))
   	{		
		  if (activeChar.hasPremium())
		  {
		    if (ExpirableServicesManager.getInstance().isServiceUnlimited(ServiceType.PREMIUM, activeChar))
		    {
		    	activeChar.sendMessage("Voce tem servico premium ilimitado");
				}
				else if (ExpirableServicesManager.getInstance().isServiceTemporary(ServiceType.PREMIUM, activeChar))
				{
					activeChar.sendMessage("Seu servico premium estara ativo ate o logout");
				}
				else
				{
					activeChar.sendMessage("O Servico Premium ficara ativo ate " + Util.formatDate(new Date(ExpirableServicesManager.getInstance().getExpirationDate(ServiceType.PREMIUM, activeChar)), "dd.MM.yyyy HH:mm"));
				}
		  }
		  else
		  {
		    activeChar.sendMessage("O Servico premium nao esta ativo");
		  }
    }
		
		else if (command.startsWith("addpremium"))
		{
			if (!Config.PREMIUM_ALLOW_VOICED)
			{
				activeChar.sendMessage("Command is forbidden");
			}
			else
			{
				String[] cmdSplit = command.split("_");
				if (cmdSplit.length != 2)
				{
					activeChar.sendMessage("Invalid parameter");
				}
				else
				{
					ItemHolder payItem = PremiumTable.getPrice(cmdSplit[1]);
					if (payItem == null)
					{
						activeChar.sendMessage("Invalid parameter");
					}
					else
					{
						if (payItem.getCount() > 0)
						{
							ConfirmDlg confirmation = new ConfirmDlg(SystemMessageId.S1.getId()).addString("Vai custar " + 
																																														Long.toString(payItem.getCount()) +  " pcs. " + 
																																														ItemTable.getInstance().getTemplate(payItem.getId()).getName() + 
																																														". Are you agreed?");
							activeChar.sendPacket(confirmation);
							activeChar.setDialogId(DialogId.PREMIUM);
							PremiumTable.getInstance().addRequest(activeChar, cmdSplit[1]);
						}
						else
						{
							PremiumTable.givePremium(activeChar, cmdSplit[1], activeChar);
						}
					}				
				}
			}


		} 	
		return true;
	}
	
	/**
	 * 
	 * @see fidas.server.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
