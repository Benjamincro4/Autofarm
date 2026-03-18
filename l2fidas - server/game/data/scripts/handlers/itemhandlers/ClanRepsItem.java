package handlers.itemhandlers;

import fidas.server.Config;
import fidas.server.gameserver.handler.IItemHandler;
import fidas.server.gameserver.model.actor.L2Playable;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.MagicSkillUse;

public class ClanRepsItem implements IItemHandler
{
    private static final int ITEM_IDS[] = 
    { 
    	Config.CR_ITEM_REPS_ITEM_ID
    };

	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}

            L2PcInstance activeChar = (L2PcInstance)playable;

            if (!activeChar.isClanLeader())
            {
                activeChar.sendMessage("This can be used only by Clan Leaders!");
                return false;
            }
        	 
            else if (!(activeChar.getClan().getLevel() >= Config.CR_ITEM_MIN_CLAN_LVL))
            {
            	activeChar.sendMessage("Your Clan Level is not big enough to use this item!");
            	return false;
            }
            else
            {
            	//activeChar.getClan().setReputationScore(activeChar.getClan().getReputationScore()+Config.CR_ITEM_REPS_TO_BE_AWARDED, true);
            	activeChar.getClan().addReputationScore(Config.CR_ITEM_REPS_TO_BE_AWARDED, true);
            	activeChar.sendMessage("Your clan has earned "+ Config.CR_ITEM_REPS_TO_BE_AWARDED +" rep points!");
            	MagicSkillUse  MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
            	activeChar.broadcastPacket(MSU);
              playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
            }
			return false;
        }

    public int[] getItemIds()
    {
        return ITEM_IDS;
   }
}