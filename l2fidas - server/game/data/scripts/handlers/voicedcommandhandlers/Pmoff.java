package handlers.voicedcommandhandlers;

import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
/**
 * @author KingHanker
 *
 */
public class Pmoff implements IVoicedCommandHandler
{
    private static final String[] _voicedCommands =
    {
        "pmoff",
        "pmon"
    };

    @Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
            if ((command.startsWith("pmon")))
            {
                    activeChar.setMessageRefusal(false);
                    activeChar.sendMessage("Liberado para rebecer pm");
            }
            else if ((command.startsWith("pmoff")))
            {
                activeChar.setMessageRefusal(true);
                activeChar.sendMessage("Bloqueado para rebecer pm");
            }
        else
        {
            activeChar.sendMessage("Comando Desabilitado pelo Admin");
        }
        return true;
    }

    @Override
	public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }
}