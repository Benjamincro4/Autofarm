
package fidas.server.gameserver.util;

import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.model.L2ShortCut;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.network.serverpackets.ShortCutRegister;

public class AutoFarmManager {
    public static void assignSkillToSlot(L2PcInstance player, String slot, int skillId) {
        // Guarda la habilidad seleccionada en AutoFarm Skills
        player.getAutoFarmSkills().put(slot, skillId);
        player.sendMessage("[AutoFarm] Skill ID " + skillId + " assigned to slot: " + slot);

        // Mapea el slot del HTML al slot de la barra de acceso rápido 10 (página 9)
        int shortcutSlot = getSlotPosition(slot);

        if (shortcutSlot != -1) {
            L2ShortCut shortCut = new L2ShortCut(shortcutSlot, 9, L2ShortCut.TYPE_SKILL, skillId, 1, 1);
            player.registerShortCut(shortCut); // Registra la habilidad en la barra
            player.sendPacket(new ShortCutRegister(shortCut)); // Actualiza el cliente

            player.sendMessage("[AutoFarm] Skill added to Quick Bar 10 at position " + shortcutSlot + ".");
        }
    }

    private static int getSlotPosition(String slot) {
        // Mapea slot1, slot2, etc., a posiciones de la barra de acceso rápido
        switch (slot) {
            case "slot1": return 0; // Posición 1 en la barra 10
            case "slot2": return 1; // Posición 2
            case "slot3": return 2; // Posición 3
            case "slot4": return 3; // Posición 4
            default: return -1; // Slot inválido
        }
    }

    public static void refreshAutoFarmHtml(L2PcInstance player) {
        String htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm.htm");
        if (htmContent == null) {
            player.sendMessage("No se encontró el archivo HTML.");
            return;
        }

        // Actualiza las imágenes de los slots
        htmContent = htmContent.replace("%slot1_icon%", getSkillIcon(player, "slot1"));
        htmContent = htmContent.replace("%slot2_icon%", getSkillIcon(player, "slot2"));
        htmContent = htmContent.replace("%slot3_icon%", getSkillIcon(player, "slot3"));
        htmContent = htmContent.replace("%slot4_icon%", getSkillIcon(player, "slot4"));

        NpcHtmlMessage html = new NpcHtmlMessage(5);
        html.setHtml(htmContent);
        player.sendPacket(html);
    }

    private static String getSkillIcon(L2PcInstance player, String slot) {
        Integer skillId = player.getAutoFarmSkills().get(slot);
        return (skillId != null) ? "icon.skill" + skillId : "icon.skillnull";
    }
}
