
package fidas.server.gameserver.util;

import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.model.L2ShortCut;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.network.serverpackets.ShortCutRegister;

/**
 * AutoFarmManager – handles skill slot assignment for all AutoFarm modes
 * (combat, healer, spoiler) and refreshes the appropriate HTML panel.
 *
 * Slot key format passed through _pendingAutoFarmSlot:
 *   "combat_slot1"  .. "combat_slot4"   → combat skill slots
 *   "healer_heal1"  .. "healer_heal4"   → healer skill slots
 */
public class AutoFarmManager
{
    /**
     * Called from RequestMagicSkillUse when a pending slot is set.
     * Dispatches to the correct assignment method based on the prefix.
     */
    public static void assignSkillToSlot(L2PcInstance player, String rawSlot, int skillId)
    {
        if (rawSlot == null) return;

        if (rawSlot.startsWith("combat_")) {
            String slot = rawSlot.substring("combat_".length()); // e.g. "slot1"
            assignCombatSkill(player, slot, skillId);
        } else if (rawSlot.startsWith("healer_")) {
            String slot = rawSlot.substring("healer_".length()); // e.g. "heal1"
            assignHealerSkill(player, slot, skillId);
        } else {
            // Legacy format without prefix
            assignCombatSkill(player, rawSlot, skillId);
        }

        player.saveAutofarmConfig();
    }

    // -----------------------------------------------------------------------
    // Combat skill assignment
    // -----------------------------------------------------------------------
    private static void assignCombatSkill(L2PcInstance player, String slot, int skillId)
    {
        player.getAutoFarmSkills().put(slot, skillId);

        // Also register to quick-bar page 9 (bar 10) for visual feedback
        int pos = getCombatSlotPosition(slot);
        if (pos >= 0) {
            L2ShortCut sc = new L2ShortCut(pos, 9, L2ShortCut.TYPE_SKILL, skillId, 1, 1);
            player.registerShortCut(sc);
            player.sendPacket(new ShortCutRegister(sc));
        }

        L2Skill skill = getSkill(player, skillId);
        player.sendMessage("[AutoFarm] Habilidad de combate asignada al " + slot + ": "
            + (skill != null ? skill.getName() : "ID " + skillId));
    }

    private static int getCombatSlotPosition(String slot)
    {
        switch (slot) {
            case "slot1": return 0;
            case "slot2": return 1;
            case "slot3": return 2;
            case "slot4": return 3;
            default:      return -1;
        }
    }

    // -----------------------------------------------------------------------
    // Healer skill assignment
    // -----------------------------------------------------------------------
    private static void assignHealerSkill(L2PcInstance player, String slot, int skillId)
    {
        player.getHealerSkills().put(slot, skillId);
        // Keep existing threshold or default to 70%
        if (!player.getHealerHpThresholds().containsKey(slot))
            player.getHealerHpThresholds().put(slot, 70);

        L2Skill skill = getSkill(player, skillId);
        player.sendMessage("[AutoFarm] Habilidad de curación asignada al " + slot + ": "
            + (skill != null ? skill.getName() : "ID " + skillId));
    }

    // -----------------------------------------------------------------------
    // Refresh HTML after assignment
    // -----------------------------------------------------------------------
    public static void refreshAutoFarmHtml(L2PcInstance player)
    {
        // Determine which panel to refresh based on current mode
        String mode = player.getAutoFarmMode();
        switch (mode) {
            case "healer":  refreshHealerHtml(player);  break;
            case "spoiler": refreshSpoilerHtml(player); break;
            default:        refreshCombatHtml(player);  break;
        }
    }

    private static void refreshCombatHtml(L2PcInstance player)
    {
        String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm_combat.htm");
        if (html == null) return;

        for (int i = 1; i <= 4; i++) {
            String slot = "slot" + i;
            Integer sid = player.getAutoFarmSkills().get(slot);
            String icon = (sid != null) ? "icon.skill" + sid : "icon.skillnull";
            String name = "";
            if (sid != null) {
                L2Skill sk = getSkill(player, sid);
                name = (sk != null) ? sk.getName() : "ID " + sid;
            }
            html = html.replace("%slot" + i + "_icon%", icon);
            html = html.replace("%slot" + i + "_name%", name);
        }
        html = html.replace("%random_check%", player.isUseRandomSkills() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");

        sendHtml(player, html);
    }

    private static void refreshHealerHtml(L2PcInstance player)
    {
        String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm_healer.htm");
        if (html == null) return;

        for (int i = 1; i <= 4; i++) {
            String slot = "heal" + i;
            Integer sid = player.getHealerSkills().get(slot);
            int thr = player.getHealerHpThresholds().getOrDefault(slot, 70);
            String icon = (sid != null) ? "icon.skill" + sid : "icon.skillnull";
            String name = "";
            if (sid != null) {
                L2Skill sk = getSkill(player, sid);
                name = (sk != null) ? sk.getName() : "ID " + sid;
            }
            html = html.replace("%heal" + i + "_icon%",      icon);
            html = html.replace("%heal" + i + "_name%",      name);
            html = html.replace("%heal" + i + "_threshold%", String.valueOf(thr));
        }

        sendHtml(player, html);
    }

    private static void refreshSpoilerHtml(L2PcInstance player)
    {
        String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm_spoiler.htm");
        if (html == null) return;

        int spoilId = player.getAutoFarmSpoilSkillId();
        int sweepId = player.getAutoFarmSweepSkillId();

        L2Skill spoilSkill = (spoilId > 0) ? getSkill(player, spoilId) : null;
        L2Skill sweepSkill = (sweepId > 0) ? getSkill(player, sweepId) : null;

        html = html.replace("%spoil_icon%", spoilId > 0 ? "icon.skill" + spoilId : "icon.skillnull");
        html = html.replace("%spoil_name%", spoilSkill != null ? spoilSkill.getName() : (spoilId > 0 ? "ID " + spoilId : ""));
        html = html.replace("%sweep_icon%", sweepId > 0 ? "icon.skill" + sweepId : "icon.skillnull");
        html = html.replace("%sweep_name%", sweepSkill != null ? sweepSkill.getName() : (sweepId > 0 ? "ID " + sweepId : ""));

        sendHtml(player, html);
    }

    // -----------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------
    private static L2Skill getSkill(L2PcInstance player, int skillId)
    {
        int level = player.getSkillLevel(skillId);
        if (level < 1) level = 1;
        return SkillTable.getInstance().getInfo(skillId, level);
    }

    private static void sendHtml(L2PcInstance player, String html)
    {
        NpcHtmlMessage msg = new NpcHtmlMessage(5);
        msg.setHtml(html);
        player.sendPacket(msg);
    }
}
