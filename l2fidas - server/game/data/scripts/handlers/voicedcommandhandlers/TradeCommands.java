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

import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.clientpackets.Say2;
import fidas.server.gameserver.network.serverpackets.CreatureSay;


public class TradeCommands implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"tradeon",
		"tradeoff"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if ((command.startsWith("tradeon")))
		{
			CreatureSay nx = new CreatureSay(0, Say2.TELL,"Trade","Recusar Comercio Desativado.");
			activeChar.sendPacket(nx);
			activeChar.setTradeRefusal(false);
		}
		else if ((command.startsWith("tradeoff")))
		{
			CreatureSay nj = new CreatureSay(0, Say2.TELL,"Trade","Recusar Comercio Ativado.");
			activeChar.sendPacket(nj);
			activeChar.setTradeRefusal(true);
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
