
package handlers.voicedcommandhandlers;

import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.network.serverpackets.SkillList;
import fidas.server.gameserver.model.skills.L2Skill;
import java.util.Map;

public class Autofarm implements IVoicedCommandHandler {
    private static final String[] _voicedCommands = {
        "autofarm",
        "disableautofarm",
        "enableautofarm",
        "toggleassist",
        "togglerandomskills",
        "togglekeeplocation",
        "setdistance",
        "selectskill",
        "assignskill"
    };

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params) {
        updateHtml(activeChar);

        if (command.equalsIgnoreCase("autofarm")) {
            showStatusHtml(activeChar, activeChar.isAutoFarmEnabled() ? "enabled" : "disabled");
        } 
        else if (command.equalsIgnoreCase("enableautofarm")) {
            if (!activeChar.isAutoFarmEnabled()) {
                activeChar.setAutoFarmEnabled(true);
                activeChar.sendMessage("AutoFarm habilitado.");
                activeChar.saveAutofarmConfig();
            } else {
                activeChar.sendMessage("El AutoFarm ya está habilitado.");
            }
            showStatusHtml(activeChar, "enabled");
        } 
        else if (command.equalsIgnoreCase("disableautofarm")) {
            if (activeChar.isAutoFarmEnabled()) {
                activeChar.setAutoFarmEnabled(false);
                activeChar.sendMessage("AutoFarm deshabilitado.");
            } else {
                activeChar.sendMessage("El AutoFarm ya está deshabilitado.");
            }
            showStatusHtml(activeChar, "disabled");
        } 
        else if (command.equalsIgnoreCase("toggleassist")) {
            activeChar.setAssistToPartyLeader(!activeChar.isAssistToPartyLeader());
            activeChar.sendMessage("[AutoFarm] Assist to Party Leader: " + (activeChar.isAssistToPartyLeader() ? "Enabled" : "Disabled"));
        } 
        else if (command.equalsIgnoreCase("togglerandomskills")) {
            activeChar.setUseRandomSkills(!activeChar.isUseRandomSkills());
            activeChar.sendMessage("[AutoFarm] Use Random Skills: " + (activeChar.isUseRandomSkills() ? "Enabled" : "Disabled"));
        } 
        else if (command.equalsIgnoreCase("togglekeeplocation")) {
            activeChar.setKeepStartingFarmLocation(!activeChar.isKeepStartingFarmLocation());
            activeChar.sendMessage("[AutoFarm] Keep Starting Farm Location: " + (activeChar.isKeepStartingFarmLocation() ? "Enabled" : "Disabled"));
        } 
        else if (command.equalsIgnoreCase("setdistance")) {
            setDistance(activeChar, params);
        } 
        else if (command.equalsIgnoreCase("selectskill")) {
            handleSelectSkill(activeChar, params);
        }
        return true;
    }

    private void setDistance(L2PcInstance player, String params) {
        if (params == null || params.trim().isEmpty()) {
            player.sendMessage("[AutoFarm] Please enter a valid distance.");
            return;
        }
        try {
            int distance = Integer.parseInt(params);
            if (distance <= 0 || distance > 10000) {
                player.sendMessage("[AutoFarm] Radius must be between 1 and 10000.");
            } else {
                player.setAutoFarmRadius(distance);
                player.sendMessage("[AutoFarm] Radius updated to: " + distance);
            }
        } catch (NumberFormatException e) {
            player.sendMessage("[AutoFarm] Invalid input. Please enter a number.");
        }
    }

    private void handleSelectSkill(L2PcInstance player, String params) {
        if (params == null || params.trim().isEmpty()) {
            player.sendMessage("[AutoFarm] No slot specified.");
            return;
        }

        // Guardar el slot pendiente
        player.setPendingAutoFarmSlot(params.trim());

        // Crear y llenar el paquete SkillList
        SkillList skillList = new SkillList();
        for (L2Skill skill : player.getAllSkills()) {
            skillList.addSkill(skill.getId(), skill.getLevel(), skill.isPassive(), false, false);
        }

        // Enviar el paquete SkillList al cliente
        player.sendPacket(skillList);

        player.sendMessage("[AutoFarm] Please select a skill for the slot: " + params);
    }



    private void updateHtml(L2PcInstance player) {
        String htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm.htm");
        if (htmContent == null) {
            player.sendMessage("No se encontró el archivo HTML.");
            return;
        }

        // Checkboxes
        htmContent = htmContent.replace("%assist_check%", player.isAssistToPartyLeader() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
        htmContent = htmContent.replace("%random_skills_check%", player.isUseRandomSkills() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
        htmContent = htmContent.replace("%keep_location_check%", player.isKeepStartingFarmLocation() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");

        // Skill slots
        htmContent = htmContent.replace("%slot1_icon%", getSkillIcon(player, "slot1"));
        htmContent = htmContent.replace("%slot2_icon%", getSkillIcon(player, "slot2"));
        htmContent = htmContent.replace("%slot3_icon%", getSkillIcon(player, "slot3"));
        htmContent = htmContent.replace("%slot4_icon%", getSkillIcon(player, "slot4"));

        NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
        npcHtmlMessage.setHtml(htmContent);
        player.sendPacket(npcHtmlMessage);
    }

    private String getSkillIcon(L2PcInstance player, String slot) {
        Map<String, Integer> skills = player.getAutoFarmSkills();
        return skills.containsKey(slot) ? "icon.skill" + skills.get(slot) : "icon.skillnull";
    }

    private void showStatusHtml(L2PcInstance player, String status) {
        String htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm.htm");
        if (htmContent == null) {
            player.sendMessage("No se encontró el archivo HTML.");
            return;
        }
        htmContent = htmContent.replace("%auto_farm_status%", status);

        NpcHtmlMessage html = new NpcHtmlMessage(5);
        html.setHtml(htmContent);
        player.sendPacket(html);
    }

    @Override
    public String[] getVoicedCommandList() {
        return _voicedCommands;
    }
}
