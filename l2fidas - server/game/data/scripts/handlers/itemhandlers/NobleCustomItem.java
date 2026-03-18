package handlers.itemhandlers;

import fidas.server.Config;
import fidas.server.gameserver.handler.IItemHandler;
import fidas.server.gameserver.model.actor.L2Playable;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.network.serverpackets.SocialAction;

public class NobleCustomItem implements IItemHandler
{

	public NobleCustomItem()
	{
	//null
	}

	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(Config.NOBLE_CUSTOM_ITEMS)
		{
			if(!(playable instanceof L2PcInstance))
				return false;

			L2PcInstance activeChar = (L2PcInstance) playable;

			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("This Item Cannot Be Used On Olympiad Games.");
			}

			if(activeChar.isNoble())
			{
				activeChar.sendMessage("You Are Already A Noblesse!.");
			}
			else
			{
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				activeChar.setNoble(true);
				activeChar.sendMessage("You Are Now a Noble,You Are Granted With Noblesse Status , And Noblesse Skills.");
				activeChar.broadcastUserInfo();
				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.getInventory().addItem("Tiara", 7694, 1, activeChar, null);
			}
			activeChar = null;
		}
		return false;
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	private static final int ITEM_IDS[] =
	{
		Config.NOOBLE_CUSTOM_ITEM_ID
	};

}
