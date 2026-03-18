
package handlers.voicedcommandhandlers;

import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.datatables.SkillTable;

public class Autofarm implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = {
        "autofarm",
        "enableautofarm",
        "disableautofarm",
        "toggleassist",
        "togglerandomskills",
        "togglekeeplocation",
        "setdistance",
        // Mode selection
        "setmode",
        // Combat skill slots
        "selectskill",
        "clearskill",
        // Healer skill slots
        "selecthealskill",
        "clearhealskill",
        "sethealthreshold",
        // Spoiler skill slots
        "setspoilskill",
        "setsweepskill",
        "clearspoilskill",
        "clearsweepskill",
        // Navigation
        "autofarm_combat",
        "autofarm_healer",
        "autofarm_spoiler"
    };

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance player, String params)
    {
        switch (command.toLowerCase())
        {
            case "autofarm":
                showMainHtml(player);
                break;

            case "enableautofarm":
                if (!player.isAutoFarmEnabled()) {
                    player.setAutoFarmEnabled(true);
                    player.sendMessage("[AutoFarm] Habilitado.");
                    player.saveAutofarmConfig();
                }
                showMainHtml(player);
                break;

            case "disableautofarm":
                if (player.isAutoFarmEnabled()) {
                    player.setAutoFarmEnabled(false);
                    player.sendMessage("[AutoFarm] Deshabilitado.");
                    player.saveAutofarmConfig();
                }
                showMainHtml(player);
                break;

            case "toggleassist":
                player.setAssistToPartyLeader(!player.isAssistToPartyLeader());
                player.saveAutofarmConfig();
                showMainHtml(player);
                break;

            case "togglerandomskills":
                player.setUseRandomSkills(!player.isUseRandomSkills());
                player.saveAutofarmConfig();
                showMainHtml(player);
                break;

            case "togglekeeplocation":
                player.setKeepStartingFarmLocation(!player.isKeepStartingFarmLocation());
                player.saveAutofarmConfig();
                showMainHtml(player);
                break;

            case "setdistance":
                handleSetDistance(player, params);
                showMainHtml(player);
                break;

            case "setmode":
                handleSetMode(player, params);
                break;

            // ----------------------------------------------------------------
            // Combat skill slots
            // ----------------------------------------------------------------
            case "autofarm_combat":
                showCombatHtml(player);
                break;

            case "selectskill":
                // params = "slot1" .. "slot4"  (clicking the slot icon opens skill selection)
                if (params != null && !params.trim().isEmpty()) {
                    player.setPendingAutoFarmSlot("combat_" + params.trim());
                    player.sendMessage("[AutoFarm] Ahora usa la habilidad que quieres asignar al slot " + params.trim() + ".");
                }
                showCombatHtml(player);
                break;

            case "clearskill":
                if (params != null && !params.trim().isEmpty()) {
                    player.getAutoFarmSkills().remove(params.trim());
                    player.saveAutofarmConfig();
                    player.sendMessage("[AutoFarm] Slot " + params.trim() + " limpiado.");
                }
                showCombatHtml(player);
                break;

            // ----------------------------------------------------------------
            // Healer skill slots
            // ----------------------------------------------------------------
            case "autofarm_healer":
                showHealerHtml(player);
                break;

            case "selecthealskill":
                // params = "heal1" .. "heal4"
                if (params != null && !params.trim().isEmpty()) {
                    player.setPendingAutoFarmSlot("healer_" + params.trim());
                    player.sendMessage("[AutoFarm] Ahora usa la habilidad de curación que quieres asignar al slot " + params.trim() + ".");
                }
                showHealerHtml(player);
                break;

            case "clearhealskill":
                if (params != null && !params.trim().isEmpty()) {
                    player.getHealerSkills().remove(params.trim());
                    player.getHealerHpThresholds().remove(params.trim());
                    player.saveAutofarmConfig();
                    player.sendMessage("[AutoFarm] Slot healer " + params.trim() + " limpiado.");
                }
                showHealerHtml(player);
                break;

            case "sethealthreshold":
                handleSetHealThreshold(player, params);
                showHealerHtml(player);
                break;

            // ----------------------------------------------------------------
            // Spoiler skill slots
            // ----------------------------------------------------------------
            case "autofarm_spoiler":
                showSpoilerHtml(player);
                break;

            case "setspoilskill":
                // params = skillId
                handleSetSpoilSkill(player, params);
                showSpoilerHtml(player);
                break;

            case "setsweepskill":
                handleSetSweepSkill(player, params);
                showSpoilerHtml(player);
                break;

            case "clearspoilskill":
                player.setAutoFarmSpoilSkillId(0);
                player.saveAutofarmConfig();
                player.sendMessage("[AutoFarm] Skill de Spoil eliminada.");
                showSpoilerHtml(player);
                break;

            case "clearsweepskill":
                player.setAutoFarmSweepSkillId(0);
                player.saveAutofarmConfig();
                player.sendMessage("[AutoFarm] Skill de Sweep eliminada.");
                showSpoilerHtml(player);
                break;
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Helper: set farm distance
    // -----------------------------------------------------------------------
    private void handleSetDistance(L2PcInstance player, String params)
    {
        if (params == null || params.trim().isEmpty()) {
            player.sendMessage("[AutoFarm] Introduce una distancia válida.");
            return;
        }
        try {
            int dist = Integer.parseInt(params.trim());
            if (dist <= 0 || dist > 10000) {
                player.sendMessage("[AutoFarm] El radio debe estar entre 1 y 10000.");
            } else {
                player.setAutoFarmRadius(dist);
                player.saveAutofarmConfig();
                player.sendMessage("[AutoFarm] Radio actualizado a: " + dist);
            }
        } catch (NumberFormatException e) {
            player.sendMessage("[AutoFarm] Entrada inválida. Introduce un número.");
        }
    }

    // -----------------------------------------------------------------------
    // Helper: set mode
    // -----------------------------------------------------------------------
    private void handleSetMode(L2PcInstance player, String params)
    {
        if (params == null) return;
        String mode = params.trim().toLowerCase();
        switch (mode) {
            case "combat":
            case "healer":
            case "spoiler":
                player.setAutoFarmMode(mode);
                player.saveAutofarmConfig();
                player.sendMessage("[AutoFarm] Modo cambiado a: " + mode);
                break;
            default:
                player.sendMessage("[AutoFarm] Modo desconocido: " + mode);
                return;
        }
        // Navigate to the mode-specific panel
        switch (mode) {
            case "combat":  showCombatHtml(player);  break;
            case "healer":  showHealerHtml(player);  break;
            case "spoiler": showSpoilerHtml(player); break;
        }
    }

    // -----------------------------------------------------------------------
    // Helper: set heal threshold   params = "heal1 70"
    // -----------------------------------------------------------------------
    private void handleSetHealThreshold(L2PcInstance player, String params)
    {
        if (params == null || params.trim().isEmpty()) return;
        String[] parts = params.trim().split(" ");
        if (parts.length != 2) {
            player.sendMessage("[AutoFarm] Uso: .sethealthreshold <slot> <porcentaje>  (ej: heal1 70)");
            return;
        }
        String slot = parts[0];
        try {
            int pct = Integer.parseInt(parts[1]);
            if (pct < 1 || pct > 100) {
                player.sendMessage("[AutoFarm] El porcentaje debe estar entre 1 y 100.");
                return;
            }
            player.getHealerHpThresholds().put(slot, pct);
            player.saveAutofarmConfig();
            player.sendMessage("[AutoFarm] Umbral HP para " + slot + " establecido en " + pct + "%.");
        } catch (NumberFormatException e) {
            player.sendMessage("[AutoFarm] Porcentaje inválido.");
        }
    }

    // -----------------------------------------------------------------------
    // Helper: set spoil skill   params = skillId
    // -----------------------------------------------------------------------
    private void handleSetSpoilSkill(L2PcInstance player, String params)
    {
        if (params == null || params.trim().isEmpty()) {
            player.sendMessage("[AutoFarm] Introduce el ID de la habilidad de Spoil.");
            return;
        }
        try {
            int skillId = Integer.parseInt(params.trim());
            int level = player.getSkillLevel(skillId);
            if (level < 1) {
                player.sendMessage("[AutoFarm] No tienes esa habilidad.");
                return;
            }
            player.setAutoFarmSpoilSkillId(skillId);
            player.saveAutofarmConfig();
            L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
            player.sendMessage("[AutoFarm] Habilidad de Spoil configurada: " + (skill != null ? skill.getName() : "ID " + skillId));
        } catch (NumberFormatException e) {
            player.sendMessage("[AutoFarm] ID inválido.");
        }
    }

    // -----------------------------------------------------------------------
    // Helper: set sweep skill   params = skillId
    // -----------------------------------------------------------------------
    private void handleSetSweepSkill(L2PcInstance player, String params)
    {
        if (params == null || params.trim().isEmpty()) {
            player.sendMessage("[AutoFarm] Introduce el ID de la habilidad de Sweep.");
            return;
        }
        try {
            int skillId = Integer.parseInt(params.trim());
            int level = player.getSkillLevel(skillId);
            if (level < 1) {
                player.sendMessage("[AutoFarm] No tienes esa habilidad.");
                return;
            }
            player.setAutoFarmSweepSkillId(skillId);
            player.saveAutofarmConfig();
            L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
            player.sendMessage("[AutoFarm] Habilidad de Sweep configurada: " + (skill != null ? skill.getName() : "ID " + skillId));
        } catch (NumberFormatException e) {
            player.sendMessage("[AutoFarm] ID inválido.");
        }
    }

    // -----------------------------------------------------------------------
    // HTML builders
    // -----------------------------------------------------------------------
    private void showMainHtml(L2PcInstance player)
    {
        String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm.htm");
        if (html == null) { player.sendMessage("HTML no encontrado: autofarm.htm"); return; }

        String status    = player.isAutoFarmEnabled() ? "<font color=\"00FF00\">ACTIVADO</font>" : "<font color=\"FF0000\">DESACTIVADO</font>";
        String mode      = player.getAutoFarmMode();
        String modeName  = mode.substring(0, 1).toUpperCase() + mode.substring(1);

        html = html.replace("%status%",         status);
        html = html.replace("%mode%",           modeName);
        html = html.replace("%mode_lower%",     mode);
        html = html.replace("%assist_check%",   player.isAssistToPartyLeader()      ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
        html = html.replace("%random_check%",   player.isUseRandomSkills()          ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
        html = html.replace("%location_check%", player.isKeepStartingFarmLocation() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
        html = html.replace("%farm_radius%",    String.valueOf(player.getAutoFarmRadius()));

        NpcHtmlMessage msg = new NpcHtmlMessage(5);
        msg.setHtml(html);
        player.sendPacket(msg);
    }

    private void showCombatHtml(L2PcInstance player)
    {
        String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm_combat.htm");
        if (html == null) { player.sendMessage("HTML no encontrado: autofarm_combat.htm"); return; }

        for (int i = 1; i <= 4; i++) {
            String slot = "slot" + i;
            Integer skillId = player.getAutoFarmSkills().get(slot);
            String icon = (skillId != null) ? "icon.skill" + skillId : "icon.skillnull";
            String name = "";
            if (skillId != null) {
                int lv = player.getSkillLevel(skillId);
                L2Skill sk = SkillTable.getInstance().getInfo(skillId, lv > 0 ? lv : 1);
                name = (sk != null) ? sk.getName() : "ID " + skillId;
            }
            html = html.replace("%slot" + i + "_icon%", icon);
            html = html.replace("%slot" + i + "_name%", name);
        }
        html = html.replace("%random_check%", player.isUseRandomSkills() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");

        NpcHtmlMessage msg = new NpcHtmlMessage(5);
        msg.setHtml(html);
        player.sendPacket(msg);
    }

    private void showHealerHtml(L2PcInstance player)
    {
        String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm_healer.htm");
        if (html == null) { player.sendMessage("HTML no encontrado: autofarm_healer.htm"); return; }

        for (int i = 1; i <= 4; i++) {
            String slot = "heal" + i;
            Integer skillId = player.getHealerSkills().get(slot);
            int threshold   = player.getHealerHpThresholds().getOrDefault(slot, 70);
            String icon = (skillId != null) ? "icon.skill" + skillId : "icon.skillnull";
            String name = "";
            if (skillId != null) {
                int lv = player.getSkillLevel(skillId);
                L2Skill sk = SkillTable.getInstance().getInfo(skillId, lv > 0 ? lv : 1);
                name = (sk != null) ? sk.getName() : "ID " + skillId;
            }
            html = html.replace("%heal" + i + "_icon%",      icon);
            html = html.replace("%heal" + i + "_name%",      name);
            html = html.replace("%heal" + i + "_threshold%", String.valueOf(threshold));
        }

        NpcHtmlMessage msg = new NpcHtmlMessage(5);
        msg.setHtml(html);
        player.sendPacket(msg);
    }

    private void showSpoilerHtml(L2PcInstance player)
    {
        String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm_spoiler.htm");
        if (html == null) { player.sendMessage("HTML no encontrado: autofarm_spoiler.htm"); return; }

        int spoilId = player.getAutoFarmSpoilSkillId();
        int sweepId = player.getAutoFarmSweepSkillId();

        String spoilIcon = (spoilId > 0) ? "icon.skill" + spoilId : "icon.skillnull";
        String sweepIcon = (sweepId > 0) ? "icon.skill" + sweepId : "icon.skillnull";

        String spoilName = "";
        String sweepName = "";
        if (spoilId > 0) {
            int lv = player.getSkillLevel(spoilId);
            L2Skill sk = SkillTable.getInstance().getInfo(spoilId, lv > 0 ? lv : 1);
            spoilName = (sk != null) ? sk.getName() : "ID " + spoilId;
        }
        if (sweepId > 0) {
            int lv = player.getSkillLevel(sweepId);
            L2Skill sk = SkillTable.getInstance().getInfo(sweepId, lv > 0 ? lv : 1);
            sweepName = (sk != null) ? sk.getName() : "ID " + sweepId;
        }

        html = html.replace("%spoil_icon%", spoilIcon);
        html = html.replace("%spoil_name%", spoilName);
        html = html.replace("%sweep_icon%", sweepIcon);
        html = html.replace("%sweep_name%", sweepName);

        NpcHtmlMessage msg = new NpcHtmlMessage(5);
        msg.setHtml(html);
        player.sendPacket(msg);
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}
