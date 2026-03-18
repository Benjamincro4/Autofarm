package handlers.chathandlers;

import fidas.server.Config;
import fidas.server.gameserver.handler.IChatHandler;
import fidas.server.gameserver.model.BlockList;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.PcCondOverride;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.CreatureSay;
import fidas.server.gameserver.util.Util;

/**
 * Hero chat handler.
 * @author durgus
 */
public class ChatHeroVoice implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		17
	};
	
	@Override
	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		if (activeChar.isHero() || activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
		{
			if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type))
			{
				activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
				return;
			}
			
			CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
			
			for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
			{
				if ((player != null) && !BlockList.isBlocked(player, activeChar))
				{
					player.sendPacket(cs);
				}
			}
		}
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}