package handlers.bypasshandlers;

import java.util.StringTokenizer;

import fidas.server.gameserver.dressme.DressMeHtml;
import fidas.server.gameserver.dressme.DressMeService;
import fidas.server.gameserver.handler.IBypassHandler;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.UserInfo;

public class DressMeBypassHandler implements IBypassHandler
{
	private static final String[] BYPASSES =
	{
		"dressme"
	};

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if ((activeChar == null) || (command == null))
		{
			return false;
		}

		try
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			if (!st.hasMoreTokens())
			{
				return false;
			}

			st.nextToken();
			String action = st.hasMoreTokens() ? st.nextToken() : "main";

			if ("main".equalsIgnoreCase(action))
			{
				DressMeHtml.showMain(activeChar);
				return true;
			}
			else if ("armors".equalsIgnoreCase(action))
			{
				DressMeHtml.showArmors(activeChar);
				return true;
			}
			else if ("trySet".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					return false;
				}

				int setId = Integer.parseInt(st.nextToken());
				DressMeService.tryArmorSet(activeChar, setId);

				activeChar.sendPacket(new UserInfo(activeChar));
				activeChar.broadcastUserInfo();

				DressMeHtml.showArmors(activeChar);
				return true;
			}
			else if ("equip".equalsIgnoreCase(action) || "applySet".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					return false;
				}

				int setId = Integer.parseInt(st.nextToken());
				DressMeService.equipArmorSet(activeChar, setId);

				activeChar.sendPacket(new UserInfo(activeChar));
				activeChar.broadcastUserInfo();

				DressMeHtml.showArmors(activeChar);
				return true;
			}
			else if ("unequip".equalsIgnoreCase(action) || "clear".equalsIgnoreCase(action))
			{
				DressMeService.unequipArmorSet(activeChar);

				activeChar.sendPacket(new UserInfo(activeChar));
				activeChar.broadcastUserInfo();

				DressMeHtml.showArmors(activeChar);
				return true;
			}
			else if ("buyConfirm".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					return false;
				}

				int setId = Integer.parseInt(st.nextToken());
				DressMeHtml.showBuyConfirm(activeChar, setId);
				return true;
			}
			else if ("buy".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					return false;
				}

				int setId = Integer.parseInt(st.nextToken());
				DressMeService.unlockArmorSet(activeChar, setId);

				activeChar.sendPacket(new UserInfo(activeChar));
				activeChar.broadcastUserInfo();

				DressMeHtml.showArmors(activeChar);
				return true;
			}
			else if ("nameColors".equalsIgnoreCase(action))
			{
				DressMeHtml.showNameColors(activeChar);
				return true;
			}
			else if ("titleColors".equalsIgnoreCase(action))
			{
				DressMeHtml.showTitleColors(activeChar);
				return true;
			}
			else if ("nameColorConfirm".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					return false;
				}

				String colorHex = st.nextToken();
				DressMeHtml.showNameColorConfirm(activeChar, colorHex);
				return true;
			}
			else if ("titleColorConfirm".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					return false;
				}

				String colorHex = st.nextToken();
				DressMeHtml.showTitleColorConfirm(activeChar, colorHex);
				return true;
			}
			else if ("buyNameColor".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					return false;
				}

				String colorHex = st.nextToken();
				DressMeService.buyNameColor(activeChar, colorHex);
				DressMeHtml.showNameColors(activeChar);
				return true;
			}
			else if ("buyTitleColor".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					return false;
				}

				String colorHex = st.nextToken();
				DressMeService.buyTitleColor(activeChar, colorHex);
				DressMeHtml.showTitleColors(activeChar);
				return true;
			}
			else if ("sexConfirm".equalsIgnoreCase(action))
			{
				DressMeHtml.showSexConfirm(activeChar);
				return true;
			}
			else if ("buySexChange".equalsIgnoreCase(action))
			{
				DressMeService.changeSex(activeChar);
				DressMeHtml.showMain(activeChar);
				return true;
			}
			else if ("changeName".equalsIgnoreCase(action))
			{
				DressMeHtml.showNameChangeInput(activeChar);
				return true;
			}
			else if ("nameCheck".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					DressMeHtml.showNameChangeInput(activeChar);
					return true;
				}

				String newName = st.nextToken("").trim();
				DressMeHtml.showNameChangeConfirm(activeChar, newName);
				return true;
			}
			else if ("buyNameChange".equalsIgnoreCase(action))
			{
				if (!st.hasMoreTokens())
				{
					DressMeHtml.showNameChangeInput(activeChar);
					return true;
				}

				String newName = st.nextToken("").trim();
				DressMeService.changeName(activeChar, newName);
				DressMeHtml.showMain(activeChar);
				return true;
			}

			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String[] getBypassList()
	{
		return BYPASSES;
	}
}