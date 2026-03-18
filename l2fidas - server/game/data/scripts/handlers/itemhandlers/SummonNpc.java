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
package handlers.itemhandlers;

import fidas.server.gameserver.datatables.NpcTable;
import fidas.server.gameserver.handler.IItemHandler;
import fidas.server.gameserver.model.L2Spawn;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.L2Playable;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.actor.templates.L2NpcTemplate;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.SystemMessage;

/**
 * Custom item handler to allow items to summon NPCs.
 * @author Zoey76
 */
public final class SummonNpc implements IItemHandler
{
	private static final int[][] _itemIdNpcIdNpcLifeTimes = 
	{
		//Item Id, Npc Id, Npc life Time in seconds.
		{ 3470, 13033, 60 } //Gold Bar, Huge Pig, 60 seconds. 
	};
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (playable == null || item == null || !(playable instanceof L2PcInstance))
		{
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		
		if (!activeChar.getFloodProtectors().getItemPetSummon().tryPerformAction("summon items"))
		{
			return false;
		}
		
		if(activeChar.getBlockCheckerArena() != -1)
		{
			return false;
		}
		
		if (activeChar.inObserverMode())
		{
			return false;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}
		
		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
		{
			return false;
		}
		
		L2NpcTemplate npcTemplate = null;
		int lifeTime = 0;
		final int itemId = item.getItemId();
		
		for (int[] data : _itemIdNpcIdNpcLifeTimes)
		{
			if (data[0] == itemId)
			{
				npcTemplate = NpcTable.getInstance().getTemplate(data[1]);
				lifeTime = data[2];
				break;
			}
		}
		
		activeChar.stopMove(null, false);
		if ((npcTemplate != null) && (lifeTime > 0))
		{
			activeChar.stopMove(null, false);
			
			try
			{
				final L2Spawn spawn = new L2Spawn(npcTemplate);
				
				if (activeChar.destroyItem("Consume", item.getObjectId(), 1, null, true))
				{
					spawn.setLocx(activeChar.getX());
					spawn.setLocy(activeChar.getY());
					spawn.setLocz(activeChar.getZ());
					spawn.setHeading(activeChar.getHeading());
					
					final L2Npc npc = spawn.doSpawn(true);
					npc.setTitle(activeChar.getName());
					npc.setIsRunning(false); //Broadcast info
					npc.scheduleDespawn(lifeTime * 1000L);
					
					activeChar.sendMessage("Summoned " + npcTemplate.getName() + ".");
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
}
