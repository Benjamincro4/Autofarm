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
package instances.Disciple;

import fidas.server.gameserver.instancemanager.InstanceManager;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.Location;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.instancezone.InstanceWorld;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Synerge & knoxville TODO: Lilith and Anakim Attack.
 */
public class Disciple extends Quest
{
	private class DiSWorld extends InstanceWorld
	{
		public long[] storeTime =
		{
			0,
			0
		};
		
		public DiSWorld()
		{
		}
	}
	
	private static final String qn = "Disciple";
	private static final int INSTANCEID = 112;
	
	private static final int PROMISE = 32585;
	private static final int LEON = 32587;
	private static final int DOOR = 17240111;
	private static final int GATEKEEPER = 32657;
	
	protected int enterInstance(L2PcInstance player, String template, Location loc)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof DiSWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleportPlayer(player, loc, world.getInstanceId());
			return world.getInstanceId();
		}
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new DiSWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(INSTANCEID);
		world.setStatus(0);
		((DiSWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("Disciple started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleportPlayer(player, loc, world.getInstanceId());
		world.addAllowed(player.getObjectId());
		return instanceId;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		switch (npc.getNpcId())
		{
			case PROMISE:
				enterInstance(player, "Disciple.xml", new Location(-89559, 216030, -7488));
				break;
			case LEON:
				InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
				world.removeAllowed(player.getObjectId());
				teleportPlayer(player, new Location(171782, -17612, -4901), 0);
				break;
			case GATEKEEPER:
				final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if (tmpworld instanceof DiSWorld)
				{
					openDoor(DOOR, tmpworld.getInstanceId());
					for (int objId : tmpworld.getAllowed())
					{
						final L2PcInstance pl = L2World.getInstance().getPlayer(objId);
						if (pl != null)
						{
							pl.showQuestMovie(12);
							// pl.showQuestMovie(ExStartScenePlayer.SSQ_SEALING_EMPEROR_1ST);
						}
					}
				}
				break;
		}
		
		return "";
	}
	
	public Disciple(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(PROMISE);
		addTalkId(PROMISE);
		addTalkId(LEON);
		addTalkId(GATEKEEPER);
	}
	
	public static void main(String[] args)
	{
		new Disciple(-1, qn, "instances");
	}
}
