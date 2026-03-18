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
package custom.BossRespawn;


import javolution.text.TextBuilder;

import fidas.server.gameserver.instancemanager.GrandBossManager;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.datatables.NpcTable;

public class BossRespawn extends Quest
{
    private static final int NPC_ID = 36602;
    private static final int[] BOSSES =
    {
        29001,    // Queen Ant
        29006,    // Core
        29014,    // Orfen
        29019,    // Antharas
        29020,    // Baium
        29022,    // Zaken
        29028,    // Valakas
        29118,    // Beleth
        29187    // Freya
    };

    public BossRespawn(int questid, String name, String descr)
    {
        super(questid, name, descr);
        addFirstTalkId(NPC_ID);
    }

    @Override
	public String onFirstTalk(L2Npc npc, L2PcInstance pc)
    {
        if((npc == null) || (pc == null))
        {
            return null;
        }

        if(npc.getNpcId() == NPC_ID)
        {
            sendInfo(pc);
        }
        return null;
    }

    private void sendInfo(L2PcInstance activeChar)
    {
        TextBuilder tb = new TextBuilder();
        tb.append("<html><title>Grand Boss Info</title><body><br><center>");
        tb.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>");

        for(int boss : BOSSES)
        {
            String name = NpcTable.getInstance().getTemplate(boss).getName();
            long delay = GrandBossManager.getInstance().getStatsSet(boss).getLong("respawn_time");
            if (delay <= System.currentTimeMillis())
            {
                tb.append("<font color=\"00C3FF\">" + name + "</color>: " + "<font color=\"9CC300\">Is Alive</color>"+"<br1>");
            }
            else
            {
                int hours = (int) ((delay - System.currentTimeMillis()) / 1000 / 60 / 60);
                int mins = (int) (((delay - (hours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000 / 60);
                int seconts = (int) (((delay - ((hours * 60 * 60 * 1000) + (mins * 60 * 1000))) - System.currentTimeMillis()) / 1000);
                tb.append("<font color=\"00C3FF\">" + name + "</color>" + "<font color=\"FFFFFF\">" +" " + "Respawn in :</color>" + " " + " <font color=\"32C332\">" + hours + " : " + mins + " : " + seconts + "</color><br1>");
            }
        }

        tb.append("<br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
        tb.append("<font color=3293F3>L2jAvengers Team</font><br>");
        tb.append("</center></body></html>");

        NpcHtmlMessage msg = new NpcHtmlMessage(NPC_ID);
        msg.setHtml(tb.toString());
        activeChar.sendPacket(msg);
    }

    public static void main(String[] args)
    {
        new BossRespawn(-1, "BossRespawn", "custom");
    }
}