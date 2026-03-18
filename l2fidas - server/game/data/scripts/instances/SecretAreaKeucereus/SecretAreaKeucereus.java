/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package instances.SecretAreaKeucereus;

import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.instancemanager.InstanceManager;
import fidas.server.gameserver.instancemanager.QuestManager;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.instancezone.InstanceWorld;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.State;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.SystemMessage;

public class SecretAreaKeucereus extends Quest
{
	private static final String qn = "SecretAreaKeucereus";
	
	private class KSAWorld extends InstanceWorld
	{
		public KSAWorld()
		{
			//InstanceManager.getInstance().super();
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, "SecretAreaKeucereus.xml");
		}
		else if (event.equalsIgnoreCase("enter_118"))
		{
			enterInstance118(player, "SecretAreaKeucereus.xml");
		}
		else if (event.equalsIgnoreCase("exit"))
		{
			player.teleToLocation(-184997, 242818, 1578);
			player.setInstanceId(0);
		}
		return "";
	}
	
	public SecretAreaKeucereus(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addTalkId(32566);
		addTalkId(32567);
	}
	
	private void enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof KSAWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player,(KSAWorld)world);
			return;
		}
		//New instance
		if (!checkCond(player))
			return;
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new KSAWorld();
		
		world.setInstanceId(instanceId);
		world.setTemplateId(117);
		world.setStatus(0);
		
		InstanceManager.getInstance().addWorld(world);
		_log.info("SecretAreaKeucereus started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleportPlayer(player, (KSAWorld)world);
	}
	
	private void enterInstance118(L2PcInstance player, String template)
	{
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof KSAWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player,(KSAWorld)world);
			return;
		}
		//New instance
		if (!checkCond118(player))
			return;
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new KSAWorld();
		
		world.setInstanceId(instanceId);
		world.setTemplateId(118);
		world.setStatus(0);
		
		InstanceManager.getInstance().addWorld(world);
		_log.info("SecretAreaKeucereus started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleportPlayer(player, (KSAWorld)world);
	}
	
	private void teleportPlayer(L2PcInstance player, KSAWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.teleToLocation(-23530, -8963, -5413);
		player.setInstanceId(world.getInstanceId());
		if(player.getSummon() != null)
		{
			player.getSummon().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getSummon().setInstanceId(world.getInstanceId());
			player.getSummon().teleToLocation(-23530, -8963, -5413);
		}
	}
	
	private boolean checkCond(L2PcInstance player)
	{
		if (QuestManager.getInstance().getQuest(10270) != null)
		{
			if (player.getQuestState(QuestManager.getInstance().getQuest(10270).getName()).getState() == State.STARTED
					&& player.getQuestState(QuestManager.getInstance().getQuest(10270).getName()).getInt("cond") == 4)
				return true;
		}
		return false;
	}
	
	private boolean checkCond118(L2PcInstance player)
	{
		if (QuestManager.getInstance().getQuest(10272) != null)
		{
			if (player.getQuestState(QuestManager.getInstance().getQuest(10272).getName()).getState() == State.STARTED
					&& player.getQuestState(QuestManager.getInstance().getQuest(10272).getName()).getInt("cond") == 3)
				return true;
		}
		return false;
	}

	public static void main(String[] args)
	{
		new SecretAreaKeucereus(-1, qn, "instances");
	}
}