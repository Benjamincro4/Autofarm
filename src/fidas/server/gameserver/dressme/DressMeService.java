package fidas.server.gameserver.dressme;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import fidas.server.Config;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.itemcontainer.Inventory;
import fidas.server.gameserver.network.serverpackets.UserInfo;
import java.util.regex.Pattern;
import fidas.server.gameserver.datatables.CharNameTable;

public class DressMeService
{
	private static final int DRESSME_COIN_ID = Config.DRESSME_ARMOR_COIN_ID;
	private static final long DRESSME_SKIN_PRICE = Config.DRESSME_ARMOR_PRICE;

	private static final Map<Integer, ActiveSkin> _active = new ConcurrentHashMap<Integer, ActiveSkin>();
	private static final Map<Integer, ScheduledFuture<?>> _tryTasks = new ConcurrentHashMap<Integer, ScheduledFuture<?>>();

	public static boolean ownsArmorSet(L2PcInstance player, int setId)
	{
		if (player == null)
		{
			return false;
		}

		return DressMeDAO.getInstance().ownsArmorSet(player.getAccountName(), setId);
	}

	public static boolean isEquippedArmorSet(L2PcInstance player, int setId)
	{
		if (player == null)
		{
			return false;
		}

		return DressMeDAO.getInstance().loadEquippedArmorSet(player.getObjectId()) == setId;
	}

	public static void unlockArmorSet(L2PcInstance player, int setId)
	{
		if (player == null)
		{
			return;
		}

		DressMeData.getInstance().ensureLoaded();
		DressMeArmorSet set = DressMeData.getInstance().getSet(setId);
		if (set == null)
		{
			player.sendMessage("Armor set not found: " + setId);
			return;
		}

		if (ownsArmorSet(player, setId))
		{
			player.sendMessage("You already own this armor set.");
			return;
		}

		if (player.getInventory().getInventoryItemCount(DRESSME_COIN_ID, 0) < DRESSME_SKIN_PRICE)
		{
			player.sendMessage("You do not have enough L2Fidas Coin.");
			return;
		}

		if (!player.destroyItemByItemId("DressMeUnlock", DRESSME_COIN_ID, DRESSME_SKIN_PRICE, player, true))
		{
			player.sendMessage("Payment failed.");
			return;
		}

		DressMeDAO.getInstance().addOwnedArmorSet(player.getAccountName(), setId);
		player.sendMessage("Armor set unlocked: " + set.getName());
	}

	public static void applyArmorSet(L2PcInstance player, int setId)
	{
		equipArmorSet(player, setId);
	}

	public static void equipArmorSet(L2PcInstance player, int setId)
	{
		if (player == null)
		{
			return;
		}

		cancelTryTask(player);

		DressMeData.getInstance().ensureLoaded();
		DressMeArmorSet set = DressMeData.getInstance().getSet(setId);

		if (set == null)
		{
			player.sendMessage("Armor set not found: " + setId);
			return;
		}

		if (!ownsArmorSet(player, setId))
		{
			player.sendMessage("You do not own this armor set.");
			return;
		}

		_active.put(player.getObjectId(), new ActiveSkin(set.getChest(), set.getLegs(), set.getGloves(), set.getBoots()));
		DressMeDAO.getInstance().saveEquippedArmorSet(player.getObjectId(), setId);
		player.sendMessage("Armor skin applied: " + set.getName());
	}

	public static void tryArmorSet(final L2PcInstance player, final int setId)
	{
		if (player == null)
		{
			return;
		}

		cancelTryTask(player);

		DressMeData.getInstance().ensureLoaded();
		final DressMeArmorSet set = DressMeData.getInstance().getSet(setId);

		if (set == null)
		{
			player.sendMessage("Armor set not found: " + setId);
			return;
		}

		_active.put(player.getObjectId(), new ActiveSkin(set.getChest(), set.getLegs(), set.getGloves(), set.getBoots()));
		player.sendPacket(new UserInfo(player));
		player.broadcastUserInfo();
		player.sendMessage("Preview applied for 10 seconds: " + set.getName());

		ScheduledFuture<?> task = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					int equippedSetId = DressMeDAO.getInstance().loadEquippedArmorSet(player.getObjectId());

					if (equippedSetId > 0)
					{
						restoreEquippedArmorSet(player);
					}
					else
					{
						_active.remove(player.getObjectId());
					}

					player.sendPacket(new UserInfo(player));
					player.broadcastUserInfo();
					player.sendMessage("Preview ended.");
				}
				finally
				{
					_tryTasks.remove(player.getObjectId());
				}
			}
		}, 10000);

		_tryTasks.put(player.getObjectId(), task);
	}

	public static void clearArmorSkin(L2PcInstance player)
	{
		unequipArmorSet(player);
	}

	public static void unequipArmorSet(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		cancelTryTask(player);
		_active.remove(player.getObjectId());
		DressMeDAO.getInstance().saveEquippedArmorSet(player.getObjectId(), 0);
		player.sendMessage("Armor skin removed.");
	}

	public static void restoreEquippedArmorSet(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		DressMeData.getInstance().ensureLoaded();

		int setId = DressMeDAO.getInstance().loadEquippedArmorSet(player.getObjectId());
		if (setId <= 0)
		{
			_active.remove(player.getObjectId());
			return;
		}

		DressMeArmorSet set = DressMeData.getInstance().getSet(setId);
		if (set == null)
		{
			_active.remove(player.getObjectId());
			return;
		}

		_active.put(player.getObjectId(), new ActiveSkin(set.getChest(), set.getLegs(), set.getGloves(), set.getBoots()));
	}

	public static void restoreColorData(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		int storedNameColor = DressMeDAO.getInstance().loadNameColor(player.getObjectId());
		int storedTitleColor = DressMeDAO.getInstance().loadTitleColor(player.getObjectId());

		if (storedNameColor > 0)
		{
			player.getAppearance().setNameColor(storedNameColor);
		}

		if (storedTitleColor > 0)
		{
			player.getAppearance().setTitleColor(storedTitleColor);
		}
	}

	public static void buyNameColor(L2PcInstance player, String colorHex)
	{
		if (player == null)
		{
			return;
		}

		String normalizedHex = normalizeHex(colorHex);
		if (normalizedHex == null)
		{
			player.sendMessage("Invalid color.");
			return;
		}

		if (isFreeColor(normalizedHex))
		{
			applyNameColor(player, normalizedHex);
			player.sendMessage("Your name color has been changed for free.");
			return;
		}

		if (!isPaidColor(normalizedHex))
		{
			player.sendMessage("Invalid color.");
			return;
		}

		if (player.getInventory().getInventoryItemCount(Config.DRESSME_COLOR_COIN_ID, 0) < Config.DRESSME_NAME_COLOR_PRICE)
		{
			player.sendMessage("You do not have enough L2Fidas Coin.");
			return;
		}

		if (!player.destroyItemByItemId("DressMeNameColor", Config.DRESSME_COLOR_COIN_ID, Config.DRESSME_NAME_COLOR_PRICE, player, true))
		{
			player.sendMessage("Payment failed.");
			return;
		}

		applyNameColor(player, normalizedHex);
		player.sendMessage("Your name color has been changed.");
	}

	public static void buyTitleColor(L2PcInstance player, String colorHex)
	{
		if (player == null)
		{
			return;
		}

		String normalizedHex = normalizeHex(colorHex);
		if (normalizedHex == null)
		{
			player.sendMessage("Invalid color.");
			return;
		}

		if (isFreeColor(normalizedHex))
		{
			applyTitleColor(player, normalizedHex);
			player.sendMessage("Your title color has been changed for free.");
			return;
		}

		if (!isPaidColor(normalizedHex))
		{
			player.sendMessage("Invalid color.");
			return;
		}

		if (player.getInventory().getInventoryItemCount(Config.DRESSME_COLOR_COIN_ID, 0) < Config.DRESSME_TITLE_COLOR_PRICE)
		{
			player.sendMessage("You do not have enough L2Fidas Coin.");
			return;
		}

		if (!player.destroyItemByItemId("DressMeTitleColor", Config.DRESSME_COLOR_COIN_ID, Config.DRESSME_TITLE_COLOR_PRICE, player, true))
		{
			player.sendMessage("Payment failed.");
			return;
		}

		applyTitleColor(player, normalizedHex);
		player.sendMessage("Your title color has been changed.");
	}

	private static void applyNameColor(L2PcInstance player, String colorHex)
	{
		int color = toGameColor(colorHex);
		player.getAppearance().setNameColor(color);
		DressMeDAO.getInstance().saveNameColor(player.getObjectId(), color);
		player.sendPacket(new UserInfo(player));
		player.broadcastUserInfo();
	}

	private static void applyTitleColor(L2PcInstance player, String colorHex)
	{
		int color = toGameColor(colorHex);
		player.getAppearance().setTitleColor(color);
		DressMeDAO.getInstance().saveTitleColor(player.getObjectId(), color);
		player.sendPacket(new UserInfo(player));
		player.broadcastUserInfo();
	}

	private static int toGameColor(String colorHex)
	{
		String hex = normalizeHex(colorHex);
		if (hex == null)
		{
			return 0xFFFFFF;
		}

		int rgb = Integer.parseInt(hex, 16);

		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;

		return (b << 16) | (g << 8) | r;
	}

	private static boolean isFreeColor(String colorHex)
	{
		return containsHex(Config.DRESSME_FREE_COLORS, colorHex);
	}

	private static boolean isPaidColor(String colorHex)
	{
		return containsHex(Config.DRESSME_COLORS, colorHex);
	}

	private static boolean containsHex(String configValue, String colorHex)
	{
		if ((configValue == null) || configValue.trim().isEmpty() || (colorHex == null))
		{
			return false;
		}

		String[] entries = configValue.split(";");
		for (String entry : entries)
		{
			String hex = normalizeHex(entry);
			if ((hex != null) && hex.equalsIgnoreCase(colorHex))
			{
				return true;
			}
		}

		return false;
	}

	private static String normalizeHex(String value)
	{
		if (value == null)
		{
			return null;
		}

		String hex = value.trim().toUpperCase();
		if (!hex.matches("[0-9A-F]{6}"))
		{
			return null;
		}

		return hex;
	}

	public static int visual(L2PcInstance player, int paperdollSlot, int realItemId)
	{
		if (player == null)
		{
			return realItemId;
		}

		ActiveSkin s = _active.get(player.getObjectId());
		if (s == null)
		{
			return realItemId;
		}

		int v = 0;
		switch (paperdollSlot)
		{
			case Inventory.PAPERDOLL_CHEST:
				v = s.chest;
				break;
			case Inventory.PAPERDOLL_LEGS:
				v = s.legs;
				break;
			case Inventory.PAPERDOLL_GLOVES:
				v = s.gloves;
				break;
			case Inventory.PAPERDOLL_FEET:
				v = s.feet;
				break;
			default:
				v = 0;
				break;
		}

		return (v > 0) ? v : realItemId;
	}

	private static void cancelTryTask(L2PcInstance player)
	{
		ScheduledFuture<?> task = _tryTasks.remove(player.getObjectId());
		if (task != null)
		{
			task.cancel(false);
		}
	}

	private static class ActiveSkin
	{
		final int chest;
		final int legs;
		final int gloves;
		final int feet;

		ActiveSkin(int chest, int legs, int gloves, int feet)
		{
			this.chest = chest;
			this.legs = legs;
			this.gloves = gloves;
			this.feet = feet;
		}
	}
	
	public static void changeSex(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		if (player.getInventory().getInventoryItemCount(Config.DRESSME_SEX_COIN_ID, 0) < Config.DRESSME_SEX_PRICE)
		{
			player.sendMessage("You do not have enough L2Fidas Coin.");
			return;
		}

		if (!player.destroyItemByItemId("DressMeSexChange", Config.DRESSME_SEX_COIN_ID, Config.DRESSME_SEX_PRICE, player, true))
		{
			player.sendMessage("Payment failed.");
			return;
		}

		final boolean newSex = !player.getAppearance().getSex();
		player.getAppearance().setSex(newSex);
		player.store();

		player.sendPacket(new UserInfo(player));
		player.broadcastUserInfo();

		player.sendMessage("Your sex has been changed successfully.");
	}

	public static String validateNewName(L2PcInstance player, String newName)
	{
		if (player == null)
		{
			return "Invalid character.";
		}

		if (newName == null)
		{
			return "The name cannot be empty.";
		}

		newName = newName.trim();

		if (newName.isEmpty())
		{
			return "The name cannot be empty.";
		}

		if (newName.equalsIgnoreCase(player.getName()))
		{
			return "You must enter a different name.";
		}

		if ((newName.length() < 3) || (newName.length() > 16))
		{
			return "The name length must be between 3 and 16 characters.";
		}

		// No spaces, tabs or any whitespace characters.
		for (int i = 0; i < newName.length(); i++)
		{
			if (Character.isWhitespace(newName.charAt(i)))
			{
				return "Spaces are not allowed in character names.";
			}
		}

		// Safe character restriction to avoid problematic DB/client/server chars.
		// Allows only letters and digits.
		if (!newName.matches("^[A-Za-z0-9]+$"))
		{
			return "Only letters and numbers are allowed in character names.";
		}

		// Keep core rule too, in case your server already defines a stricter template.
		if ((Config.CNAME_TEMPLATE != null) && !Config.CNAME_TEMPLATE.isEmpty())
		{
			if (!Pattern.matches(Config.CNAME_TEMPLATE, newName))
			{
				return "That name does not match server name rules.";
			}
		}

		if ((Config.FORBIDDEN_NAMES != null) && (Config.FORBIDDEN_NAMES.length > 0))
		{
			for (String forbidden : Config.FORBIDDEN_NAMES)
			{
				if ((forbidden != null) && !forbidden.trim().isEmpty() && forbidden.equalsIgnoreCase(newName))
				{
					return "That name is not allowed.";
				}
			}
		}

		if (CharNameTable.getInstance().doesCharNameExist(newName))
		{
			return "That name is already in use.";
		}

		return null;
	}

	public static void changeName(L2PcInstance player, String newName)
	{
		if (player == null)
		{
			return;
		}

		final String validationError = validateNewName(player, newName);
		if (validationError != null)
		{
			player.sendMessage(validationError);
			return;
		}

		if (player.getInventory().getInventoryItemCount(Config.DRESSME_NAME_CHANGE_COIN_ID, 0) < Config.DRESSME_NAME_CHANGE_PRICE)
		{
			player.sendMessage("You do not have enough L2Fidas Coin.");
			return;
		}

		if (!player.destroyItemByItemId("DressMeNameChange", Config.DRESSME_NAME_CHANGE_COIN_ID, Config.DRESSME_NAME_CHANGE_PRICE, player, true))
		{
			player.sendMessage("Payment failed.");
			return;
		}

		final int objectId = player.getObjectId();
		final String oldName = player.getName();

		CharNameTable.getInstance().removeName(objectId);

		player.setName(newName.trim());
		player.store();

		CharNameTable.getInstance().addName(player);

		player.sendPacket(new UserInfo(player));
		player.broadcastUserInfo();

		player.sendMessage("Your character name has been changed from " + oldName + " to " + player.getName() + ".");
	}
}