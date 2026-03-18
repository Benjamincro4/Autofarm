package fidas.server.gameserver.dressme;

import java.util.ArrayList;
import java.util.List;

import fidas.server.Config;
import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;

public class DressMeHtml
{
	public static void showMain(L2PcInstance player)
	{
		String htm = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/merchant/36605.htm");
		if (htm == null)
		{
			player.sendMessage("File not found: data/html/merchant/36605.htm");
			return;
		}

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(htm);
		player.sendPacket(msg);
	}

	public static void showArmors(L2PcInstance player)
	{
		String htm = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/merchant/36605-1.htm");
		if (htm == null)
		{
			player.sendMessage("File not found: data/html/merchant/36605-1.htm");
			return;
		}

		DressMeData.getInstance().ensureLoaded();
		List<DressMeArmorSet> sets = DressMeData.getInstance().getArmorSets();

		StringBuilder list = new StringBuilder();

		if (sets.isEmpty())
		{
			list.append("No armor sets configured in dressme.xml");
		}
		else
		{
			for (DressMeArmorSet s : sets)
			{
				boolean owned = DressMeService.ownsArmorSet(player, s.getId());
				boolean equipped = DressMeService.isEquippedArmorSet(player, s.getId());

				list.append("<table width=280 border=0 cellspacing=0 cellpadding=0 bgcolor=\"000000\">");
				list.append("<tr>");
				list.append("<td width=150>").append(s.getName()).append("</td>");

				list.append("<td width=55 align=center>");
				if (!owned)
				{
					list.append("<button value=\"Try\" action=\"bypass -h dressme trySet ")
						.append(s.getId())
						.append("\" width=50 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				}
				else
				{
					list.append("");
				}
				list.append("</td>");

				list.append("<td width=75 align=center>");
				if (owned)
				{
					if (equipped)
					{
						list.append("<button value=\"Unequip\" action=\"bypass -h dressme unequip\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
					else
					{
						list.append("<button value=\"Equip\" action=\"bypass -h dressme equip ")
							.append(s.getId())
							.append("\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
				}
				else
				{
					list.append("<button value=\"Buy\" action=\"bypass -h dressme buyConfirm ")
						.append(s.getId())
						.append("\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				}
				list.append("</td>");
				list.append("</tr>");
				list.append("</table>");
				list.append("<img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
			}
		}

		htm = htm.replace("%armor_list%", list.toString());

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(htm);
		player.sendPacket(msg);
	}

	public static void showBuyConfirm(L2PcInstance player, int setId)
	{
		DressMeData.getInstance().ensureLoaded();
		DressMeArmorSet set = DressMeData.getInstance().getSet(setId);

		if (set == null)
		{
			player.sendMessage("Armor set not found: " + setId);
			return;
		}

		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append("<br><br>");
		html.append("<font name=\"hs12\">Confirm Purchase</font><br><br>");
		html.append("You are about to buy the armor skin:<br>");
		html.append("<font color=\"LEVEL\">").append(set.getName()).append("</font><br><br>");
		html.append("Price: <font color=\"LEVEL\">").append(Config.DRESSME_ARMOR_PRICE).append(" L2Fidas Coin</font><br><br>");
		html.append("This unlock is shared for the entire account.<br><br>");
		html.append("<button value=\"Confirm\" action=\"bypass -h dressme buy ")
			.append(setId)
			.append("\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("&nbsp;");
		html.append("<button value=\"Back\" action=\"bypass -h dressme armors\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("</center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}

	public static void showNameColors(L2PcInstance player)
	{
		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append("<br>");
		html.append("<font name=\"hs12\">Change Name Color</font><br><br>");
		html.append("Choose a color for your character name.<br>");
		html.append("Click on the preview to continue.<br><br>");

		appendColorList(html, player.getName(), "nameColorConfirm");

		html.append("<br>");
		html.append("<button value=\"Back\" action=\"bypass -h dressme main\" width=120 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("</center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}

	public static void showTitleColors(L2PcInstance player)
	{
		String title = player.getTitle();
		if ((title == null) || title.trim().isEmpty())
		{
			title = "L2Fidas";
		}

		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append("<br>");
		html.append("<font name=\"hs12\">Change Title Color</font><br><br>");
		html.append("Choose a color for your character title.<br>");
		html.append("Click on the preview to continue.<br><br>");

		appendColorList(html, title, "titleColorConfirm");

		html.append("<br>");
		html.append("<button value=\"Back\" action=\"bypass -h dressme main\" width=120 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("</center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}

	public static void showNameColorConfirm(L2PcInstance player, String colorHex)
	{
		ColorEntry color = findColor(colorHex);
		if (color == null)
		{
			player.sendMessage("Invalid color.");
			showNameColors(player);
			return;
		}

		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append("<br><br>");
		html.append("<font name=\"hs12\">Confirm Name Color</font><br><br>");
		html.append("Selected preview:<br><br>");
		html.append(buildPreviewText(player.getName(), color));
		html.append("<br><br>");

		if (color.free)
		{
			html.append("This color change is free.<br>");
			html.append("Do you want to apply it?<br><br>");
		}
		else
		{
			html.append("Price: <font color=\"LEVEL\">").append(Config.DRESSME_NAME_COLOR_PRICE).append(" L2Fidas Coin</font><br><br>");
		}

		html.append("<button value=\"Confirm\" action=\"bypass -h dressme buyNameColor ")
			.append(color.hex)
			.append("\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("<br><br>");
		html.append("<button value=\"Back\" action=\"bypass -h dressme nameColors\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("</center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}

	public static void showTitleColorConfirm(L2PcInstance player, String colorHex)
	{
		ColorEntry color = findColor(colorHex);
		if (color == null)
		{
			player.sendMessage("Invalid color.");
			showTitleColors(player);
			return;
		}

		String title = player.getTitle();
		if ((title == null) || title.trim().isEmpty())
		{
			title = "L2Fidas";
		}

		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append("<br><br>");
		html.append("<font name=\"hs12\">Confirm Title Color</font><br><br>");
		html.append("Selected preview:<br><br>");
		html.append(buildPreviewText(title, color));
		html.append("<br><br>");

		if (color.free)
		{
			html.append("This color change is free.<br>");
			html.append("Do you want to apply it?<br><br>");
		}
		else
		{
			html.append("Price: <font color=\"LEVEL\">").append(Config.DRESSME_TITLE_COLOR_PRICE).append(" L2Fidas Coin</font><br><br>");
		}

		html.append("<button value=\"Confirm\" action=\"bypass -h dressme buyTitleColor ")
			.append(color.hex)
			.append("\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("<br><br>");
		html.append("<button value=\"Back\" action=\"bypass -h dressme titleColors\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("</center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}

	private static void appendColorList(StringBuilder html, String previewText, String bypassPrefix)
	{
		List<ColorEntry> colors = getAllColors();

		if (colors.isEmpty())
		{
			html.append("No colors configured in L2Fidas.properties");
			return;
		}

		html.append("<table width=260 border=0 cellspacing=2 cellpadding=2>");

		for (int i = 0; i < colors.size(); i += 2)
		{
			html.append("<tr>");

			appendColorCell(html, previewText, bypassPrefix, colors.get(i));

			if ((i + 1) < colors.size())
			{
				appendColorCell(html, previewText, bypassPrefix, colors.get(i + 1));
			}
			else
			{
				html.append("<td width=130 align=center></td>");
			}

			html.append("</tr>");
		}

		html.append("</table>");
	}
	
	private static void appendColorCell(StringBuilder html, String previewText, String bypassPrefix, ColorEntry color)
	{
		html.append("<td width=130 align=center>");
		html.append("<a action=\"bypass -h dressme ")
			.append(bypassPrefix)
			.append(" ")
			.append(color.hex)
			.append("\">");
		html.append(buildPreviewText(previewText, color));
		html.append("</a>");
		html.append("<br></td>");
	}

	private static String buildPreviewText(String text, ColorEntry color)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<font color=\"").append(color.hex).append("\">");
		sb.append(text);
		if (color.free)
		{
			sb.append(" (free)");
		}
		sb.append("</font>");
		return sb.toString();
	}

	private static ColorEntry findColor(String colorHex)
	{
		if (colorHex == null)
		{
			return null;
		}

		for (ColorEntry color : getAllColors())
		{
			if (color.hex.equalsIgnoreCase(colorHex))
			{
				return color;
			}
		}
		return null;
	}

	private static List<ColorEntry> getAllColors()
	{
		List<ColorEntry> result = new ArrayList<ColorEntry>();
		addColors(result, Config.DRESSME_FREE_COLORS, true);
		addColors(result, Config.DRESSME_COLORS, false);
		return result;
	}

	private static void addColors(List<ColorEntry> list, String configValue, boolean free)
	{
		if ((configValue == null) || configValue.trim().isEmpty())
		{
			return;
		}

		String[] entries = configValue.split(";");
		for (String entry : entries)
		{
			if ((entry == null) || entry.trim().isEmpty())
			{
				continue;
			}

			String hex = entry.trim().toUpperCase();

			if (!hex.matches("[0-9A-F]{6}"))
			{
				continue;
			}

			list.add(new ColorEntry(hex, free));
		}
	}

	private static class ColorEntry
	{
		private final String hex;
		private final boolean free;

		private ColorEntry(String hex, boolean free)
		{
			this.hex = hex;
			this.free = free;
		}
	}
	
	public static void showSexConfirm(L2PcInstance player)
	{
		String targetSex = player.getAppearance().getSex() ? "Male" : "Female";

		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append("<br><br>");
		html.append("<font name=\"hs12\">Confirm Sex Change</font><br><br>");
		html.append("Your character will be changed to:<br>");
		html.append("<font color=\"LEVEL\">").append(targetSex).append("</font><br><br>");
		html.append("Price: <font color=\"LEVEL\">").append(Config.DRESSME_SEX_PRICE).append(" L2Fidas Coin</font><br><br>");
		html.append("This change affects only the current character.<br><br>");
		html.append("<button value=\"Confirm\" action=\"bypass -h dressme buySexChange\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("<br><br>");
		html.append("<button value=\"Back\" action=\"bypass -h dressme main\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("</center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}

	public static void showNameChangeInput(L2PcInstance player)
	{
		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append("<br><br>");
		html.append("<font name=\"hs12\">Change Character Name</font><br><br>");
		html.append("Current name:<br>");
		html.append("<font color=\"LEVEL\">").append(player.getName()).append("</font><br><br>");
		html.append("Enter your new character name below.<br><br>");
		html.append("<edit var=\"newName\" width=120 height=15 length=16><br><br>");
		html.append("<button value=\"Accept\" action=\"bypass -h dressme nameCheck $newName\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("<br><br>");
		html.append("<button value=\"Back\" action=\"bypass -h dressme main\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("</center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}

	public static void showNameChangeConfirm(L2PcInstance player, String newName)
	{
		String validationError = DressMeService.validateNewName(player, newName);
		if (validationError != null)
		{
			player.sendMessage(validationError);
			showNameChangeInput(player);
			return;
		}

		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append("<br><br>");
		html.append("<font name=\"hs12\">Confirm Name Change</font><br><br>");
		html.append("Current name:<br>");
		html.append("<font color=\"LEVEL\">").append(player.getName()).append("</font><br><br>");
		html.append("New name:<br>");
		html.append("<font color=\"LEVEL\">").append(newName).append("</font><br><br>");
		html.append("Price: <font color=\"LEVEL\">").append(Config.DRESSME_NAME_CHANGE_PRICE).append(" L2Fidas Coin</font><br><br>");
		html.append("<button value=\"Confirm\" action=\"bypass -h dressme buyNameChange ").append(newName).append("\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("<br><br>");
		html.append("<button value=\"Back\" action=\"bypass -h dressme changeName\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("</center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}
}