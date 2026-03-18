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
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

public class VoiceExperience implements IVoicedCommandHandler
{
   private static final String[] VOICED_COMMANDS =
   {
       "expon",
       "expoff"
   };
   
   @Override
public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
   {
       if (!Config.Boost_EXP_COMMAND)
       {
           activeChar.sendMessage("Command Desativado");
               return false;
       }
      
       if (command.equalsIgnoreCase("expon"))
       {
           activeChar.setGainXp(true);
           activeChar.sendMessage("ganho de XP: Ativo");
           activeChar.sendMessage("ganho de SP: Ativo");
       }
       else if (command.equalsIgnoreCase("expoff"))
       {
           activeChar.setGainXp(false);
          activeChar.sendMessage("ganho de XP: Desativado");
           activeChar.sendMessage("ganho de SP: Desativado");
       }
       return true;
   }
   
   @Override
public String[] getVoicedCommandList()
   {
       return VOICED_COMMANDS;
   }
}