/* This program is free software; you can redistribute it and/or modify
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
 * http://www.gnu.org/copyleft/gpl.html
 */
package fidas.server.gameserver.model.actor.instance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;

import fidas.server.gameserver.model.actor.templates.L2NpcTemplate;
import fidas.server.gameserver.network.clientpackets.Say2;
import fidas.server.gameserver.network.serverpackets.CreatureSay;

public class L2DonateInstance extends L2NpcInstance
{
	
	public L2DonateInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/donate/" + pom + ".htm";
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("sendDnt"))
		{
			try
			{
				String value = command.substring(7);
				StringTokenizer st = new StringTokenizer(value);
				int psfA = Integer.parseInt(st.nextToken());
				String psfP = st.nextToken();
				String accN = st.nextToken();
				String charN = st.nextToken();
				String dntMessage = st.nextToken();
				
				if (charN.length() > 16)
				{
					player.sendMessage("How the hell can a character have more than 16 name length !");
					return;
				}
				player.sendPacket(new CreatureSay(player.getObjectId(), Say2.TELL, "Donate Info", "Your donate information was sent successfully.Soon a gm will be informed ."));
				FileWriter save = null;
				try
				{
					File file = new File("data/donator.txt");
					
					boolean writeHead = false;
					if (!file.exists())
					{
						writeHead = true;
					}
					
					save = new FileWriter(file, true);
					
					if (writeHead)
					{
						String header = "----Donations----\n";
						save.write(header);
					}
					
					String out = "Paysafe Ammount: " + psfA + "\n" + "Paysafe Pin: " + psfP + "\n" + "Account Name: " + accN + "\n" + "Character Name: " + charN + "\n" + "Donate Message: " + dntMessage + "\n" + "---------\n";
					save.write(out);
				}
				catch (IOException e)
				{
					_log.log(Level.WARNING, "Something gone wrong with the donate npc ", e);
				}
				finally
				{
					try
					{
						save.close();
					}
					catch (Exception e)
					{
					}
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Be sure that you fill all fields !");
			}
			
		}
	}
}