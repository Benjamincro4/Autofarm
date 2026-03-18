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
package handlers.admincommandhandlers;

import fidas.server.gameserver.handler.IAdminCommandHandler;
import fidas.server.gameserver.datatables.PremiumTable;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.util.Util;

import java.util.StringTokenizer;
/**
 * @author GKR
 */

public class AdminPremium implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_premium",
		"admin_remove_premium"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String cmd = st.nextToken();
		L2PcInstance player = null;
		L2Object targetObj = activeChar.getTarget();

		if (targetObj instanceof L2PcInstance)
		{
			player = targetObj.getActingPlayer();
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}

		if (cmd.startsWith("admin_add_premium"))
		{
			if (st.hasMoreTokens())
			{
				String val;
				val = st.nextToken();

				if (val.equalsIgnoreCase("unlim"))
				{
					if (PremiumTable.setUnlimitedPremium(player))
					{
						activeChar.sendMessage("Unlimited Premium Service for " + player.getName() + " was activated");
					}
				}
				else
				{
					long millis = Util.toMillis(val);
					if (millis > 0)
					{
						if (PremiumTable.addTime(player, millis))
						{
							activeChar.sendMessage("Premium Service for " + player.getName() + " was activated");
						}
					}
					else
					{
						activeChar.sendMessage("Error in parameters");
					}
				}
			}
			else
			{
				PremiumTable.setTemporaryPremium(player);
				activeChar.sendMessage("Premium Service for " + player.getName() + " was activated. It stays until player's logout");
			}
		}
		else if (cmd.startsWith("admin_remove_premium"))
		{
			PremiumTable.removeService(player);
			activeChar.sendMessage("Premium Service for " + player.getName() + " was cancelled");
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}