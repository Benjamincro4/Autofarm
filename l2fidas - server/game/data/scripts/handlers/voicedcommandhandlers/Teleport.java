package handlers.voicedcommandhandlers;

import fidas.server.Config;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;


public class Teleport implements IVoicedCommandHandler
{
 private static final String[] VOICED_COMMANDS =
 {
        "up10",
        "up20",
        "up30",
        "up40",
        "up50",
		"up60",
		"up70",
		"up80"
 };

 @Override
public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
 {
        {
        if (Config.ALLOW_TELEPORT_VOICECOMMAND)
        {
       
                        if (activeChar.isFestivalParticipant())
                        {
                        activeChar.sendMessage("Command blocked, you are in an Event!");
                        return false;
                        }
                        else if(activeChar.isInJail())
                        {
                        activeChar.sendMessage("Command blocked, you are in Jail");
                        return false;
                        }
                        else if(activeChar.isDead())
                        {
                        activeChar.sendMessage("Command blocked because you are Dead");
                        return false;
                        }
                        else if(activeChar.isInCombat())
                        {
                        activeChar.sendMessage("Command blocked in pvp or in Combat mode");
                        return false;
                        }
                        else if (activeChar.isInDuel())
                        {
                        activeChar.sendMessage("Command blocked, you are in a Duel");
                        return false;
                        }
                        else if (activeChar.isInOlympiadMode())
                        {
                        activeChar.sendMessage("Command blocked in olympiad");
                        return false;
                        }
                        else if (activeChar.getKarma() > 0)
                        {
                        activeChar.sendMessage("You cannot teleport with Karma");
                        return false;
                        }
                        else if (activeChar.inObserverMode())
                        {
                        activeChar.sendMessage("Command Blocked because you are in observer mode");
                        return false;
                        }
                        else if (!activeChar.inObserverMode() && !activeChar.isInOlympiadMode() && !activeChar.isInDuel() && !activeChar.isInCombat() && !activeChar.isDead() && !activeChar.isInJail())
                        {
                       
                        if(command.startsWith("up10"))
                        {
                        activeChar.teleToLocation(-18896, 122236, -3229);
                        activeChar.sendMessage("Level 10 to 20, Welcome to Gludio");
                        return false;
                        }
                        else if(command.startsWith("up20"))
                        {
                        activeChar.teleToLocation(13137, 137838, -3095);
                        activeChar.sendMessage("Level 20 to 30, Welcome to Dion");
                        return false;
                        }
                        else if(command.startsWith("up30"))
                        {
                        activeChar.teleToLocation(102656, 101463, -3571);
                        activeChar.sendMessage("Level 30 to 40, Welcome to Hardin's Private Academy");
                        return false;
                        }
                        else if(command.startsWith("up40"))
                        {
                        activeChar.teleToLocation(17722, 114358, -11673);
                        activeChar.sendMessage("Level 40 to 50, Welcome to Kruma Tower First Floor");
                        return false;
                        }              
                        else if(command.startsWith("up50"))
                        {
                        activeChar.teleToLocation(167312, 20289, -3330);
                        activeChar.sendMessage("Level 50 to 60, Welcome to The Cemetery");
                        return false;
                        }
						else if(command.startsWith("up60"))
                        {
                        activeChar.teleToLocation(165172 , -47741, -3577);
                        activeChar.sendMessage("Level 60 to 70, Welcome to Wall of Argos");
                        return false;
                        }
						else if(command.startsWith("up70"))
                        {
                        activeChar.teleToLocation(144516 , -69284, -3674);
                        activeChar.sendMessage("Level 70 to 80, Welcome to Ketra Orc Outpost");
                        return false;
                        }
						else if(command.startsWith("up80"))
                        {
                        activeChar.teleToLocation(133988 , 114443, -3725);
                        activeChar.sendMessage("Level above 80, Welcome to Antharas' Lair");
                        return false;
                        }
                        }
        }
        }
        return true;
 }

 @Override
public String[] getVoicedCommandList()
 {
        return VOICED_COMMANDS;
 }
}