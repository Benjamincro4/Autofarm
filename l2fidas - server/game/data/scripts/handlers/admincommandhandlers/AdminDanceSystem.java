package handlers.admincommandhandlers;

import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.handler.IAdminCommandHandler;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.effects.AbnormalEffect;
import fidas.server.gameserver.network.serverpackets.ExShowScreenMessage;
import fidas.server.gameserver.network.serverpackets.PlaySound;

/**
 * @author NeverMore
 */
public class AdminDanceSystem implements IAdminCommandHandler
{
	boolean _temp = false;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_dance",
		"admin_gangnam",
		"admin_sexi"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_dance"))
		{
			AdminHelpPage.showHelpPage(activeChar, "dancesystem.htm");
		}
		
		if (command.equals("admin_gangnam"))
		{
			if (_temp == true)
			{
				ExShowScreenMessage message1 = new ExShowScreenMessage("Ja existe um corredor evento de dança! Tente mais tarde!", 4000);
				activeChar.sendPacket(message1);
				return false;
			}
			_temp = true;
			ExShowScreenMessage message1 = new ExShowScreenMessage("Vamos ter um pouco de diversao! Em 30 seg evento de danca comeca!", 4000);
			activeChar.sendPacket(message1);
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
						{
							PlaySound _song = new PlaySound(1, "Gangnam", 0, 0, 0, 0, 0);
							player.sendPacket(_song);
							ExShowScreenMessage message1 = new ExShowScreenMessage("Mostre-me o que você tem, vamos sacudir !", 8000);
							player.sendPacket(message1);
							player.setIsParalyzed(true);
							player.setIsInvul(true);
							player.broadcastSocialAction(10);
							ThreadPoolManager.getInstance().scheduleGeneral(new MyTask(), 3500);
							ThreadPoolManager.getInstance().scheduleGeneral(new MyTask2(), 40000);
						}
					}
					catch (Exception e)
					{
					}
				}
			}, (30000));
		}
		
		if (command.equals("admin_sexi"))
		{
			if (_temp == true)
			{
				ExShowScreenMessage message1 = new ExShowScreenMessage("Ja existe um corredor evento de dança! Tente mais tarde!", 4000);
				activeChar.sendPacket(message1);
				return false;
			}
			_temp = true;
			ExShowScreenMessage message1 = new ExShowScreenMessage("Vamos ter um pouco de diversao! Em 30 seg evento de danca comeca!", 4000);
			activeChar.sendPacket(message1);
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
						{
							PlaySound _song = new PlaySound(1, "sexi", 0, 0, 0, 0, 0);
							player.sendPacket(_song);
							ExShowScreenMessage message1 = new ExShowScreenMessage("Mostre-me o que você tem, vamos sacudir !", 8000);
							player.sendPacket(message1);
							player.setIsParalyzed(true);
							player.setIsInvul(true);
							player.broadcastSocialAction(10);
							ThreadPoolManager.getInstance().scheduleGeneral(new MyTask(), 3500);
							ThreadPoolManager.getInstance().scheduleGeneral(new MyTask2(), 43000);
						}
					}
					catch (Exception e)
					{
					}
				}
			}, (30000));
		}
		
		return false;
	}
	
	class MyTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_temp == true)
			{
				for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
				{
					player.broadcastSocialAction(18);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new MyTask(), 18000);
			}
		}
	}
	
	class MyTask2 implements Runnable
	{
		
		@Override
		public void run()
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
			{
				_temp = false;
				player.setIsParalyzed(false);
				player.setIsInvul(false);
				player.broadcastSocialAction(10);
				player.broadcastSocialAction(11);
				player.stopAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}