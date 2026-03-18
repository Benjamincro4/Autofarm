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


public class RefuseBuff implements IVoicedCommandHandler
{
	private String[] _voicedCommands =
	{
		"allowbuff",
		"refusebuff"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equals("allowbuff"))
		{
			CreatureSay nm = new CreatureSay(0, Say2.TELL,"Server","Refuse Buff Mode ON.");
			activeChar.sendPacket(nm);
			activeChar.setisRefusingBuff(false);
		}
		else if (command.equals("refusebuff"))
		{
			CreatureSay nm = new CreatureSay(0, Say2.TELL,"Server","Refuse Buff Mode OFF.");
			activeChar.sendPacket(nm);
			activeChar.setisRefusingBuff(true);
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
