/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General private License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General private License for more
 * details.
 *
 * You should have received a copy of the GNU General private License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fidas.server.gameserver.events.io;

import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.datatables.ItemTable;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.events.Config;
import fidas.server.gameserver.events.ManagerNpc;
import fidas.server.gameserver.events.container.EventContainer;
import fidas.server.gameserver.events.container.PlayerContainer;
import fidas.server.gameserver.events.functions.Vote;
import fidas.server.gameserver.events.model.EventPlayer;
import fidas.server.gameserver.handler.AdminCommandHandler;
import fidas.server.gameserver.handler.IAdminCommandHandler;
import fidas.server.gameserver.handler.ISkillHandler;
import fidas.server.gameserver.handler.IVoicedCommandHandler;
import fidas.server.gameserver.handler.SkillHandler;
import fidas.server.gameserver.handler.VoicedCommandHandler;
import fidas.server.gameserver.instancemanager.InstanceManager;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.L2Party;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2NpcInstance;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.effects.AbnormalEffect;
import fidas.server.gameserver.model.items.type.L2EtcItemType;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.model.skills.L2SkillType;
import fidas.server.gameserver.network.serverpackets.CreatureSay;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;
import fidas.server.gameserver.util.Broadcast;
import fidas.server.util.Rnd;

/**
 * @author Rizel
 */
public class Out
{
	
	private static class BombHandler implements ISkillHandler
	{
		private final L2SkillType[] SKILL_IDS =
		{
			L2SkillType.BOMB
		};
		
		@Override
		public L2SkillType[] getSkillIds()
		{
			return SKILL_IDS;
		}
		
		@Override
		public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
		{
			if (!(activeChar instanceof L2PcInstance))
			{
				return;
			}
			
			PlayerContainer.getInstance().getPlayer(activeChar.getObjectId()).getEvent().dropBomb(PlayerContainer.getInstance().getPlayer(((L2PcInstance) activeChar).getObjectId()));
			
		}
	}
	
	private static class CaptureHandler implements ISkillHandler
	{
		private final L2SkillType[] SKILL_IDS =
		{
			L2SkillType.CAPTURE
		};
		
		@Override
		public L2SkillType[] getSkillIds()
		{
			return SKILL_IDS;
		}
		
		@Override
		public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
		{
			if (!(activeChar instanceof L2PcInstance))
			{
				return;
			}
			
			if (!(targets[0] instanceof L2NpcInstance))
			{
				return;
			}
			
			L2PcInstance player = (L2PcInstance) activeChar;
			L2NpcInstance target = (L2NpcInstance) targets[0];
			PlayerContainer.getInstance().getPlayer(activeChar.getObjectId()).getEvent().useCapture(PlayerContainer.getInstance().getPlayer(player.getObjectId()), target.getObjectId());
		}
	}
	
	private static class ReloadHandler implements IAdminCommandHandler
	{
		private final String[] ADMIN_COMMANDS =
		{
			"admin_reload_event_config"
		};
		
		@Override
		public String[] getAdminCommandList()
		{
			return ADMIN_COMMANDS;
		}
		
		@Override
		public boolean useAdminCommand(String command, L2PcInstance activeChar)
		{
			if (command.startsWith("admin_reload_event_config"))
			{
				Config.getInstance().load();
			}
			return true;
		}
	}
	
	private static class KickHandler implements IAdminCommandHandler
	{
		private final String[] ADMIN_COMMANDS =
		{
			"admin_eventkick"
		};
		
		@Override
		public String[] getAdminCommandList()
		{
			return ADMIN_COMMANDS;
		}
		
		@Override
		public boolean useAdminCommand(String command, L2PcInstance activeChar)
		{
			if (command.startsWith("admin_eventkick "))
			{
				EventPlayer p = PlayerContainer.getInstance().getPlayerByName(command.substring(16));
				if (p != null)
				{
					p.getEvent().onLogout(p);
				}
			}
			return true;
		}
	}
	
	private static class CreateEventHandler implements IAdminCommandHandler
	{
		private final String[] ADMIN_COMMANDS =
		{
			"admin_create_event"
		};
		
		@Override
		public String[] getAdminCommandList()
		{
			return ADMIN_COMMANDS;
		}
		
		@Override
		public boolean useAdminCommand(String command, L2PcInstance activeChar)
		{
			if (command.startsWith("admin_create_event "))
			{
				EventContainer.getInstance().createEvent(Integer.parseInt(command.substring(18)));
			}
			return true;
		}
	}
	
	private static class VoicedHandler implements IVoicedCommandHandler
	{
		private static final String[] _voicedCommands =
		{
			"event",
			"popup"
		};
		
		@Override
		public String[] getVoicedCommandList()
		{
			return _voicedCommands;
		}
		
		@Override
		public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
		{
			if (command.equalsIgnoreCase("event"))
			{
				ManagerNpc.getInstance().showMain(activeChar.getObjectId());
			}
			if (command.equalsIgnoreCase("popup"))
			{
				if (Config.getInstance().getBoolean(0, "voteEnabled"))
				{
					Vote.getInstance().switchPopup(activeChar.getObjectId());
				}
			}
			return true;
		}
	}
	
	public static void broadcastCreatureSay(String message)
	{
		Broadcast.toAllOnlinePlayers(new CreatureSay(0, 18, "", message));
	}
	
	@SuppressWarnings("deprecation")
	public static void closeConnection(Connection con)
	{
		L2DatabaseFactory.close(con);
	}
	
	public static void createInstance(int id)
	{
		InstanceManager.getInstance().createInstance(id);
	}
	
	public static void createParty2(FastList<EventPlayer> players)
	{
		L2Party party = null;
		party = new L2Party(players.get(0).getOwner(), 1);
		
		for (EventPlayer player : players.subList(1, players.size()))
		{
			player.joinParty(party);
		}
	}
	
	public static int getClassIndex(int player)
	{
		return getPlayerById(player).getClassIndex();
	}
	
	public static Connection getConnection()
	{
		try
		{
			return L2DatabaseFactory.getInstance().getConnection();
		}
		catch (Exception e)
		{
			System.out.println("getconnection error");
			return null;
		}
	}
	
	public static L2PcInstance getPlayerById(int id)
	{
		return L2World.getInstance().getPlayer(id);
	}
	
	public static String getSkillName(int skill)
	{
		return SkillTable.getInstance().getInfo(skill, 1).getName();
	}
	
	public static void html(Integer player, String html)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html);
		getPlayerById(player).sendPacket(msg);
	}
	
	public static boolean isPotion(int item)
	{
		if (ItemTable.getInstance().getTemplate(item).getItemType() == L2EtcItemType.POTION)
		{
			return true;
		}
		return false;
	}
	
	public static boolean isRestrictedSkill(int skill)
	{
		if (SkillTable.getInstance().getInfo(skill, 1).getSkillType() == L2SkillType.RESURRECT)
		{
			return true;
		}
		
		if (SkillTable.getInstance().getInfo(skill, 1).getSkillType() == L2SkillType.RECALL)
		{
			return true;
		}
		
		if (SkillTable.getInstance().getInfo(skill, 1).getSkillType() == L2SkillType.SUMMON_FRIEND)
		{
			return true;
		}
		
		if (SkillTable.getInstance().getInfo(skill, 1).getSkillType() == L2SkillType.FAKE_DEATH)
		{
			return true;
		}
		
		return false;
	}
	
	public static boolean isScroll(int item)
	{
		if (ItemTable.getInstance().getTemplate(item).getItemType() == L2EtcItemType.SCROLL)
		{
			return true;
		}
		return false;
	}
	
	public static int random(int max)
	{
		return Rnd.get(max);
	}
	
	@SuppressWarnings("synthetic-access")
	public static void registerHandlers()
	{
		SkillHandler.getInstance().registerHandler(new BombHandler());
		SkillHandler.getInstance().registerHandler(new CaptureHandler());
		
		AdminCommandHandler.getInstance().registerHandler(new ReloadHandler());
		AdminCommandHandler.getInstance().registerHandler(new KickHandler());
		AdminCommandHandler.getInstance().registerHandler(new CreateEventHandler());
		VoicedCommandHandler.getInstance().registerHandler(new VoicedHandler());
	}
	
	public static void sendMessage(int player, String message)
	{
		getPlayerById(player).sendMessage(message);
	}
	
	public static void setPvPInstance(int id)
	{
		InstanceManager.getInstance().getInstance(id).setPvPInstance(true);
	}
	
	public static void startFlameEffect(Integer npc)
	{
		((L2Npc) L2World.getInstance().findObject(npc)).startAbnormalEffect(AbnormalEffect.FLAME);
	}
	
	public static void tpmPurge()
	{
		ThreadPoolManager.getInstance().purge();
	}
	
	public static ScheduledFuture<?> tpmScheduleGeneral(Runnable task, int time)
	{
		return ThreadPoolManager.getInstance().scheduleGeneral(task, time);
	}
	
	public static void tpmScheduleGeneralAtFixedRate(Runnable task, int first, int delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(task, first, delay);
	}
	
	public static Collection<Integer> getEveryPlayer()
	{
		List<Integer> l = new LinkedList<>();
		for (Integer p : L2World.getInstance().getAllPlayers().keys())
		{
			l.add(p);
		}
		return l;
	}
}
