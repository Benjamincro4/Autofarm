package handlers.voicedcommandhandlers;

import fidas.server.Config;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.TvTEvent;
import fidas.server.gameserver.model.entity.TvTRoundEvent;
import fidas.server.gameserver.model.zone.ZoneId;
import fidas.server.gameserver.network.serverpackets.LeaveWorld;

public class Offline implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS =
    {
        "offline"
    };

    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
    {
        if (activeChar == null)
        {
            return false;
        }

        if (!Config.OFFLINE_TRADE_ENABLE)
        {
            activeChar.sendMessage("El offline trade está desactivado.");
            return false;
        }

        if ((activeChar.getClient() == null) || activeChar.getClient().isDetached())
        {
            return false;
        }

        final int storeType = activeChar.getPrivateStoreType();
        final boolean validStore =
                (storeType == L2PcInstance.STORE_PRIVATE_SELL) ||
                (storeType == L2PcInstance.STORE_PRIVATE_BUY) ||
                (storeType == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL);

        if (!validStore)
        {
            activeChar.sendMessage("Debes tener abierta una tienda privada de compra o venta.");
            return false;
        }

        if (!activeChar.isSitting())
        {
            activeChar.sendMessage("Debes estar sentado para usar .offline.");
            return false;
        }

        // mismas restricciones del core
        if (activeChar.isInOlympiadMode()
                || activeChar.isFestivalParticipant()
                || TvTEvent.isPlayerParticipant(activeChar.getObjectId())
                || TvTRoundEvent.isPlayerParticipant(activeChar.getObjectId())
                || activeChar.isInJail()
                || (activeChar.getVehicle() != null))
        {
            activeChar.sendMessage("No puedes usar .offline en tu estado actual.");
            return false;
        }

        if (Config.OFFLINE_MODE_IN_PEACE_ZONE && !activeChar.isInsideZone(ZoneId.PEACE))
        {
            activeChar.sendMessage("Solo puedes usar .offline en zona de paz.");
            return false;
        }

        activeChar.sendMessage("Entrando en modo offline trade...");

        // IMPORTANTE:
        // NO usar closeNow().
        // Esto debe provocar la desconexión normal para que entre en DisconnectTask.
        activeChar.getClient().close(LeaveWorld.STATIC_PACKET);
        return true;
    }
}