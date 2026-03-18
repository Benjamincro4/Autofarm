package handlers.voicedcommandhandlers;

import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.util.AutoFarmManager;

/**
 * Voiced command handler for AutoFarm.
 * Opens the main AutoFarm panel via .autofarm
 */
public class Autofarm implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = { "autofarm" };

    @Override
    public boolean useVoicedCommand(final String command, final L2PcInstance player, final String params)
    {
        if (player == null) return false;
        AutoFarmManager.getInstance().renderMain(player);
        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}
