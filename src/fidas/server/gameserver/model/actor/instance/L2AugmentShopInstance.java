
package fidas.server.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.datatables.ItemTable;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.model.L2Augmentation;
import fidas.server.gameserver.model.actor.templates.L2NpcTemplate;
import fidas.server.gameserver.model.itemcontainer.Inventory;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.network.serverpackets.ActionFailed;
import fidas.server.gameserver.network.serverpackets.ExShowScreenMessage;
import fidas.server.gameserver.network.serverpackets.ExVariationCancelResult;
import fidas.server.gameserver.network.serverpackets.InventoryUpdate;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.network.serverpackets.StatusUpdate;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

import javolution.text.TextBuilder;

public class L2AugmentShopInstance extends L2NpcInstance
{
        protected static final Logger _log = Logger.getLogger(L2AugmentShopInstance.class.getName());

        public L2AugmentShopInstance(int objectId, L2NpcTemplate template)
        {
                super(objectId, template);
        }
        
        @Override
		public String getHtmlPath(int npcId, int val)
        {
                String pom = "";
                if (val == 0) {
                        pom = "augmentshop";
                } else {
                        pom = "augmentshop-" + val;
                }
                
                return "data/html/custom/" + pom + ".htm";
        }
        
        private void sendHTML(L2PcInstance activeChar, String headtext, TextBuilder text)
        {
                if(headtext == null || headtext.length() == 0)
                        headtext = "Augmentation Shop";
                
                TextBuilder replyMSG = new TextBuilder();
                replyMSG.append("<html><body>");
                replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
                replyMSG.append("<font color=\"LEVEL\">" + headtext + "</font></center><br>");
                replyMSG.append(text);
                replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
                replyMSG.append("<center><font color=\"A2A0A2\">Augmentation Shop</font> (c) <font color=\"B09878\">Mentor</font></center><br>");
                replyMSG.append("</body></html>");
                NpcHtmlMessage html = new NpcHtmlMessage(5);
        html.setHtml(replyMSG.toString());
        activeChar.sendPacket(html);
        }
        
        public void ViewAugStats(L2PcInstance player, String type, String Stype, int page, int augId)
        {
                //int AugId = augId;
                //int EffectId = AugId*65536+1;
                String AugDesc = "none";
                String SkillDesc = "none";
                String SkillId = "1";
                int SkillLevel = 1;
                String color = "none";
                int PriceId = 1;
                long PriceCount = 1;
                String SkillName = "none";
                String ItemName = "none";
                
        		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
        		{
                        PreparedStatement statement = con.prepareStatement("SELECT * FROM augmentation_skillmap WHERE augId='" + augId + "';");
                        ResultSet result = statement.executeQuery();
                        while (result.next())
                        {
                                AugDesc = result.getString("augDesc");
                                SkillDesc = result.getString("skillDesc");
                                SkillId = result.getString("skillId");
                                SkillLevel = result.getInt("skillLevel");
                                color = result.getString("color");
                                PriceId = result.getInt("priceId");
                                PriceCount = result.getLong("priceCount");
                        }
                        result.close();
                        statement.close();
                }
                catch (Exception e)
                {
                        _log.log(Level.WARNING, "[L2AugmentShopIntance] AddAug() : " + e.getMessage(), e);
                }

                if(color.equalsIgnoreCase("red"))
                        color = "FF0000";
                else if(color.equalsIgnoreCase("blue"))
                        color = "0000FF";
                else if(color.equalsIgnoreCase("purple"))
                        color = "CC00CC";
                
                if(Integer.parseInt(SkillId)<100)
                        SkillId = "00"+SkillId;
                if(Integer.parseInt(SkillId)<1000)
                        SkillId = "0"+SkillId;
                
                SkillName = SkillTable.getInstance().getInfo(Integer.parseInt(SkillId), SkillLevel).getName();
                ItemName = ItemTable.getInstance().getTemplate(PriceId).getName();
                
                TextBuilder replyMSG = new TextBuilder();
                replyMSG.append("<center>");
                replyMSG.append("<table width=\"270\" border=\"0\">");
                replyMSG.append("<tr>");
                replyMSG.append("       <td valign=\"top\">");
                replyMSG.append("       <font color=\"A2A0A2\">Augmentation information:</font><br1> <font color=\"B09878\">" + AugDesc + "</font><br1>");
                replyMSG.append("       <font color=\"A2A0A2\">Skill information:</font><br1> <font color=\"" + color + "\">" + SkillDesc + "</font><br1>");
                replyMSG.append("       <font color=\"A2A0A2\">Skill Name:</font><br1> <font color=\"" + color + "\">" + SkillName + "</font><br1>");
                replyMSG.append("       <font color=\"A2A0A2\">Skill level:</font> <font color=\"FF0000\">" + SkillLevel + "</font><br1>");
                if(PriceCount>1) {
                        replyMSG.append("       <font color=\"A2A0A2\">Price Item:</font> <font color=\"LEVEL\">" + ItemName + "</font><br1>");
                        replyMSG.append("       <font color=\"A2A0A2\">Price Count:</font> <font color=\"LEVEL\">" + PriceCount + "</font><br1>");
                } else {
                        replyMSG.append("       <font color=\"A2A0A2\">Price:</font> <font color=\"LEVEL\">Free</font><br1>");
                }
                replyMSG.append("       </td>");
                replyMSG.append("</tr>");
                replyMSG.append("<tr>");
                replyMSG.append("       <td><center>");
                replyMSG.append("               <table width=\"170\" border=\"0\">");
                replyMSG.append("               <tr>");
                replyMSG.append("               <td><button value=\"Cancel\" action=\"bypass -h npc_" + getObjectId() + "_ListByColor " + type + " " + Stype + " " + page + " \" width=80 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
                replyMSG.append("               <td><button value=\"Buy\" action=\"bypass -h npc_" + getObjectId() + "_CreateAug " + type + " " + Stype + " " + page + " " + augId + "\" width=80 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
                replyMSG.append("               <tr>");
                replyMSG.append("               </table>");
                replyMSG.append("       </center></td>");
                replyMSG.append("</tr>");
                replyMSG.append("</table>");
                replyMSG.append("</center>");
                sendHTML(player, "Augmentation Stats", replyMSG);
        }
        
        public void ListAugs(L2PcInstance player, String type, String Stype, int page)
        {
                int AugCount            = 0; // Default value
                int AugCountPage        = 8; // Default value
                int PageCount           = 0; // Default value
                int StartCount          = 1; // Default value
                //int FinishCount               = AugCountPage; // Default value
                
                ArrayList<Integer> augId = new ArrayList<>();
                HashMap<Integer, String> augDesc = new HashMap<>();
                HashMap<Integer, String> PriceName = new HashMap<>();
                HashMap<Integer, Long> PriceCount = new HashMap<>();
                
                PreparedStatement statement = null;
                ResultSet result = null;
        		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
        		{
                        
                        /** Augmentek megszamolasa az adott kategoriaban. **/
                        statement = con.prepareStatement("SELECT COUNT(*) AS rowcount FROM augmentation_skillmap WHERE augType LIKE '%" + Stype + "%' and color LIKE '%" + type + "%' and enable='true';");
                        result = statement.executeQuery();
                        result.next();
                        AugCount = result.getInt("rowcount");
                        result.close();
                        /** Augmentek megszamolasa az adott kategoriaban. **/
                        
                        PageCount = AugCount/AugCountPage;
                        if(AugCount>(AugCountPage*PageCount))
                                PageCount++;
                        
                        if((PageCount<page) || (page<1))
                                page = 1;
                        
                        if(page>1)
                        {
                                StartCount = AugCountPage*(page-1);
                        }
                        
                        /** Augmentek betoltese **/
                        statement = con.prepareStatement("SELECT augId,augDesc,priceId,priceCount FROM augmentation_skillmap WHERE augType LIKE '%" + Stype + "%' and color LIKE '%" + type + "%' and enable='true' ORDER by augId LIMIT " + StartCount + ", " + AugCountPage + ";");
                        result = statement.executeQuery();
                        while(result.next())
                        {
                                augId.add(result.getInt("augId"));
                                augDesc.put(result.getInt("augId"), result.getString("augDesc"));
                                PriceName.put(result.getInt("augId"), ItemTable.getInstance().getTemplate(result.getInt("priceId")).getName());
                                PriceCount.put(result.getInt("augId"), result.getLong("priceCount"));
                        }
                        result.close();
                        /** Augmentek betoltese **/
                }
                catch (Exception e)
                {
                        _log.log(Level.WARNING, "[L2AugmentShopIntance] ListAugs() : " + e.getMessage(), e);
                }

                String TableColor = "";
                TextBuilder replyMSG = new TextBuilder();
                if(AugCount>=1)
                {
                        for (Integer AugId : augId)  
                        {
                                if(TableColor.equalsIgnoreCase(""))
                                        TableColor = " bgcolor=\"452822\"";
                                else
                                        TableColor = "";
                                
                                replyMSG.append("<table width=\"290\" border=\"0\"" + TableColor + ">");
                                replyMSG.append("<tr>");
                                replyMSG.append("       <td>");
                                replyMSG.append("       <font color=\"B09878\">" + augDesc.get(AugId) + "</font><br1>");
                                if(PriceCount.get(AugId)>1)
                                        replyMSG.append("       <font color=\"FFFFFF\">Price:</font> <font color=\"LEVEL\">" + PriceCount.get(AugId) + " (" + PriceName.get(AugId) + ")</font>");
                                else
                                        replyMSG.append("       <font color=\"FFFFFF\">Price:</font> <font color=\"LEVEL\">Free</font>");
                                replyMSG.append("       ");
                                replyMSG.append("       </td>");
                                replyMSG.append("</tr>");
                                replyMSG.append("<tr>");
                                replyMSG.append("       <td><button value=\"View augmentation\" action=\"bypass -h npc_" + getObjectId() + "_ViewAug " + type + " " + Stype + " " + page + " " + AugId + "\" width=130 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
                                replyMSG.append("</tr>");
                                replyMSG.append("</table>");
                        }
                        replyMSG.append("<br>");
                        replyMSG.append("<table width=\"290\" border=\"0\"" + TableColor + ">");
                        replyMSG.append("<tr>");
                        
                        if(page>1)
                                replyMSG.append("       <td width=\"100\"><button value=\"Prev page\" action=\"bypass -h npc_" + getObjectId() + "_ListByColor " + type + " " + Stype + " " + (page-1) + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
                        else
                                replyMSG.append("       <td width=\"100\"><center>-</center></td>");
                        
                        replyMSG.append("       <td><center>" + page + " / " + PageCount + "</center></td>");
                        
                        if((PageCount>1) && (page<PageCount))
                                replyMSG.append("       <td width=\"100\"><button value=\"Next page\" action=\"bypass -h npc_" + getObjectId() + "_ListByColor " + type + " " + Stype + " " + (page+1) + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
                        else
                                replyMSG.append("       <td width=\"100\"><center>-</center></td>");
                        
                        replyMSG.append("</tr>");
                        replyMSG.append("</table>");
                }
                else
                {
                        replyMSG.append("<center>No augmentation can be bought in this category.</center>");
                }
                sendHTML(player, "Augmentation List (<font color=\"FF0000\">" + type + "</font>-<font color=\"FF0000\">" + Stype + "</font>)", replyMSG);
        }
        
        public void CreateAugmentation(L2PcInstance player, String type, String Stype, int page, int augId)
        {
                int AugId = augId;
                int EffectId = AugId*65536+1;
                int SkillId = 1;
                int SkillLevel = 1;
                int PriceId = 1;
                long PriceCount = 1;
                
                String list = "red";
                if(type.equalsIgnoreCase("_RedList"))
                        list = "FF0000";
                else if(type.equalsIgnoreCase("_BlueList"))
                        list = "0000FF";
                else if(type.equalsIgnoreCase("_PurpleList"))
                        list = "CC00CC";
                
                
        		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
        		{
                        PreparedStatement statement = con.prepareStatement("SELECT * FROM augmentation_skillmap WHERE augId='" + AugId + "';");
                        ResultSet result = statement.executeQuery();
                        while (result.next())
                        {
                                SkillId = result.getInt("skillId");
                                SkillLevel = result.getInt("skillLevel");
                                PriceId = result.getInt("priceId");
                                PriceCount = result.getLong("priceCount");
                        }
                        result.close();
                        statement.close();
                }
                catch (Exception e)
                {
                        _log.log(Level.WARNING, "[L2AugmentShopIntance] AddAug() : " + e.getMessage(), e);
                }

                if(player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null)
                {
                        if ((player.getInventory().getInventoryItemCount(PriceId, 0)<PriceCount) && (PriceCount>1))
                        {
                                sendMsg(player, "Not enough item!");
                                ViewAugStats(player, list, Stype, page, augId);
                                return;
                        }
                        
                        L2ItemInstance item = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
                        if(!item.isAugmented())
                        {
                                if(PriceCount>1)
                                {
                                        // Remove price
                                        player.destroyItemByItemId("AugmentationShop", PriceId, PriceCount, player, true);
                                        player.getInventory().updateDatabase();
                                }
                                
                                L2Augmentation augmentation = new L2Augmentation(EffectId, SkillTable.getInstance().getInfo(SkillId, SkillLevel));
                                item.setAugmentation(augmentation);
                                UpdatePlayer(player, item);
                                SkillDebug(player, item);
                                sendMsg(player, "Augmentation added to weapon.");
                        }
                        else
                        {
                                sendMsg(player, "You weapon is have augmentation.");
                                ViewAugStats(player, list, Stype, page, augId);
                                return;
                        }
                }
                else
                {
                        sendMsg(player, "Weapon not found!");
                        ViewAugStats(player, list, Stype, page, augId);
                        return;
                }
        }
        
        public void SkillDebug(L2PcInstance player, L2ItemInstance item)
        {
                player.disarmWeapons();
                InventoryUpdate iu = new InventoryUpdate();
                player.getInventory().equipItemAndRecord(item);
                iu.addModifiedItem(item);
                player.sendPacket(iu);
        }
        
        public void sendMsg(L2PcInstance player, String msg)
        {
                player.sendPacket(new ExShowScreenMessage(msg, 3500));
                player.sendMessage(msg);
        }
        
        public void RemoveAug(L2PcInstance player)
        {
                if(player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null)
                {
                        L2ItemInstance item = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
                        if(item.isAugmented())
                        {
                                player.disarmWeapons();
                                item.removeAugmentation();
                                UpdatePlayer(player, item);
                                player.sendPacket(new ExVariationCancelResult(1));
                                SkillDebug(player, item);
                                showHtmlMenu(player, "data/html/custom/augmentshop.htm");
                                sendMsg(player, "Augmentation deleted from weapon.");
                        }
                        else
                        {
                                showHtmlMenu(player, "data/html/custom/augmentshop.htm");
                                sendMsg(player, "You weapon is havent augmentation.");
                                return;
                        }
                }
                else
                {
                        showHtmlMenu(player, "data/html/custom/augmentshop.htm");
                        sendMsg(player, "Weapon not found!");
                        return;
                }
        }
        
        public void UpdatePlayer(L2PcInstance player, L2ItemInstance targetItem)
        {
                InventoryUpdate iu = new InventoryUpdate();
                iu.addModifiedItem(targetItem);
                player.sendPacket(iu);
                
                StatusUpdate su = new StatusUpdate(player);
                su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
                player.sendPacket(su);
        }
        
        private final void showHtmlMenu(L2PcInstance player, String htm)
        {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile(player.getHtmlPrefix(), htm);
                html.replace("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);
        }
        
        @Override
        public void onBypassFeedback(L2PcInstance player, String command)
        {
                StringTokenizer st = new StringTokenizer(command, " ");
                String Command = st.nextToken();
                int page = 1;
                
                if (Command.equalsIgnoreCase("BlueList"))
                {
                        String SType = st.nextToken();
                        if(st.hasMoreTokens())
                                page = Integer.parseInt(st.nextToken());
                        ListAugs(player, "blue", SType, page);
                }
                else if (Command.equalsIgnoreCase("PurpleList"))
                {
                        String SType = st.nextToken();
                        if(st.hasMoreTokens())
                                page = Integer.parseInt(st.nextToken());
                        ListAugs(player, "purple", SType, page);
                }
                else if (Command.equalsIgnoreCase("RedList"))
                {
                        String SType = st.nextToken();
                        if(st.hasMoreTokens())
                                page = Integer.parseInt(st.nextToken());
                        ListAugs(player, "red", SType, page);
                }
                else if (Command.equalsIgnoreCase("ListByColor"))
                {
                        String type = st.nextToken();
                        String SType = st.nextToken();
                        page = Integer.parseInt(st.nextToken());
                        ListAugs(player, type, SType, page);
                }
                else if (Command.equalsIgnoreCase("ViewAug"))
                {
                        ViewAugStats(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
                }
                else if (Command.equalsIgnoreCase("CreateAug"))
                {
                        CreateAugmentation(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
                }
                else if (Command.equalsIgnoreCase("RemoveAug"))
                {
                        RemoveAug(player);
                }
        }
}