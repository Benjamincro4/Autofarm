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
package fidas.server.gameserver.handler;

import java.util.logging.Logger;

import fidas.server.Config;
import fidas.server.gameserver.datatables.OfflineTradersTable;
import fidas.server.gameserver.model.L2Party;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.L2GameClient;
import fidas.server.gameserver.network.serverpackets.ServerClose;

public class OfflineShop implements IVoicedCommandHandler
{
    private static final Logger _log = Logger.getLogger(OfflineShop.class.getName());
    private static final String[] VOICED_COMMANDS = { "offline" };

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
    {
        if (activeChar == null)
            return false;

        // 1️⃣ Verificar si el sistema de tiendas offline está activado
        if (!Config.OFFLINE_TRADE_ENABLE && !Config.OFFLINE_CRAFT_ENABLE)
        {
            activeChar.sendMessage("El sistema de tiendas offline está deshabilitado en este servidor.");
            return false;
        }

        // 2️⃣ Comprobar que el jugador tenga una tienda abierta (buy/sell/manufacture)
        if (activeChar.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
        {
            activeChar.sendMessage("Debes tener una tienda abierta para usar el modo offline.");
            return false;
        }

        // 3️⃣ Solo en zonas de paz (usando tu método correcto)
        if (Config.OFFLINE_MODE_IN_PEACE_ZONE && !activeChar.isInsidePeaceZone(activeChar))
        {
            activeChar.sendMessage("Solo puedes activar el modo offline dentro de una zona de paz.");
            return false;
        }

        // 4️⃣ Evitar combate, duelos, etc.
        if (activeChar.isInCombat() || activeChar.isInDuel() || activeChar.isDead())
        {
            activeChar.sendMessage("No puedes activar el modo offline durante combate o duelo.");
            return false;
        }

        // 5️⃣ Quitar de grupo si pertenece a uno
        if (activeChar.getParty() != null)
        {
            activeChar.getParty().removePartyMember(activeChar, L2Party.messageType.Disconnected);
        }

        // 6️⃣ Cambiar color de nombre si está configurado
        if (Config.OFFLINE_SET_NAME_COLOR)
        {
            activeChar.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
            activeChar.broadcastUserInfo();
        }

        // 7️⃣ Marcar como offline en el cliente
        final L2GameClient client = activeChar.getClient();
        if (client != null)
        {
            client.setDetached(true);
        }

        // 8️⃣ Guardar hora de inicio (por si hay límite de días)
        activeChar.setOfflineStartTime(System.currentTimeMillis());

        // 9️⃣ Cerrar el cliente
        if (client != null)
        {
            client.close(ServerClose.STATIC_PACKET);
        }

        // 🔟 Guardar manualmente en la tabla de offline traders
        try
        {
            OfflineTradersTable.getInstance().storeOffliners();
            _log.info("OfflineShop: personaje " + activeChar.getName() + " guardado como offliner.");
        }
        catch (Exception e)
        {
            _log.warning("Error guardando trader offline: " + e.getMessage());
        }

        activeChar.sendMessage("Tu tienda ha sido puesta en modo offline. Puedes cerrar el juego.");
        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}
