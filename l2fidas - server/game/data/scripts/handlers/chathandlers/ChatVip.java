package handlers.chathandlers;

import fidas.server.gameserver.handler.IChatHandler;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.clientpackets.Say2;
import fidas.server.gameserver.network.serverpackets.CreatureSay;

public class ChatVip implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		Say2.VIP_CHAT
	};

	@Override
	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), Say2.VIP_CHAT, activeChar.getName(), text);

		for (Object obj : L2World.getInstance().getAllPlayers().values())
		{
			if (obj instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) obj;

				if (player.isOnline())
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