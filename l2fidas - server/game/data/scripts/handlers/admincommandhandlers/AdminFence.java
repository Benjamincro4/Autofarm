package handlers.admincommandhandlers;

import fidas.server.gameserver.fence.FenceBuilderManager;
import fidas.server.gameserver.handler.IAdminCommandHandler;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

public class AdminFence implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fence",
		"admin_fbuilder",
		"admin_delallspawned",
		"admin_dellastspawned"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_fence"))
		{
			String[] args = command.split(" ");
			if (args.length < 5)
			{
				activeChar.sendMessage("Not all arguments was set");
				return false;
			}
			FenceBuilderManager.getInstance().spawn_fence(activeChar, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
		}
		else if (command.equals("admin_delallspawned"))
		{
			FenceBuilderManager.getInstance().del_all(activeChar);
		}
		else if (command.equals("admin_dellastspawned"))
		{
			FenceBuilderManager.getInstance().del_last(activeChar);
		}
		else if (command.equals("admin_fbuilder"))
		{
			FenceBuilderManager.getInstance().main_fence(activeChar);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}