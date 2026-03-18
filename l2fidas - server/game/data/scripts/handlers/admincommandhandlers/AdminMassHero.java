/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * [url]http://www.gnu.org/copyleft/gpl.html[/url]
 */
package handlers.admincommandhandlers;

import java.util.logging.Logger;

import fidas.server.gameserver.handler.IAdminCommandHandler;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.SocialAction;


public class AdminMassHero implements IAdminCommandHandler
{
 protected static final Logger _log = Logger.getLogger(AdminMassHero.class.getName());

 @Override
 public String[] getAdminCommandList()
 {
  return ADMIN_COMMANDS;
 }

 @SuppressWarnings("cast")
@Override
 public boolean useAdminCommand(String command, L2PcInstance activeChar)
 {
  if(activeChar == null)
   return false;

  if(command.startsWith("admin_masshero"))
  {
   for(L2PcInstance player : L2World.getInstance().getAllPlayers().valueCollection())
   {
    if(player instanceof L2PcInstance)
    {
     /* Check to see if the player already is Hero */
     if(!player.isHero())
     {
      player.setHero(true);
      player.sendMessage("Admin vai colocar todos online como hero.");
      player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
      player.broadcastUserInfo();
     }
     player = null;
    }
   }
  }
  return true;
 }

 private static String[] ADMIN_COMMANDS = { "admin_masshero" };
}