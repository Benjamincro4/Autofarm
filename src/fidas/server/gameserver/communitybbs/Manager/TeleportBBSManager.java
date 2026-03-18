package fidas.server.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.zone.ZoneId;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.ShowBoard;
import fidas.server.gameserver.network.serverpackets.SystemMessage;

public class TeleportBBSManager extends BaseBBSManager
{
	private static final int DEFAULT_PRICE = 10000;

	private static final Category[] CATEGORIES = new Category[]
	{
		new Category("starter", "Starter Regions", new Teleport[]
		{
			new Teleport("Talking Island Village", -84318, 244579, -3730, DEFAULT_PRICE),
			new Teleport("Cedric's Training Hall", -72061, 257534, -3116, DEFAULT_PRICE),
			new Teleport("Elven Ruins", 49315, 248452, -5960, DEFAULT_PRICE),
			new Teleport("Elven Village", 46934, 51467, -2977, DEFAULT_PRICE),
			new Teleport("Dark Elven Village", 9745, 15606, -4574, DEFAULT_PRICE),
			new Teleport("Orc Village", -44836, -112524, -235, DEFAULT_PRICE),
			new Teleport("Dwarven Village", 115113, -178212, -901, DEFAULT_PRICE),
			new Teleport("Kamael Village", -117251, 46771, 360, DEFAULT_PRICE)
		}),
		new Category("towns", "Towns & Villages", new Teleport[]
		{
			new Teleport("Town of Gludin", -80826, 149775, -3043, DEFAULT_PRICE),
			new Teleport("Town of Gludio", -12672, 122776, -3116, DEFAULT_PRICE),
			new Teleport("Town of Dion", 15670, 142983, -2705, DEFAULT_PRICE),
			new Teleport("Town of Giran", 83400, 147943, -3404, DEFAULT_PRICE),
			new Teleport("Town of Oren", 82956, 53162, -1495, DEFAULT_PRICE),
			new Teleport("Town of Aden", 147476, 26925, -2204, DEFAULT_PRICE),
			new Teleport("Town of Goddard", 147928, -55273, -2734, DEFAULT_PRICE),
			new Teleport("Town of Rune", 43799, -47727, -798, DEFAULT_PRICE),
			new Teleport("Town of Schuttgart", 87386, -143246, -1293, DEFAULT_PRICE),
			new Teleport("Hunter Village", 116589, 76268, -2734, DEFAULT_PRICE),
			new Teleport("Floran Village", 17430, 170103, -3496, DEFAULT_PRICE)
		}),
		new Category("gludio", "Gludio Region", new Teleport[]
		{
			new Teleport("Town of Gludio", -12672, 122776, -3116, DEFAULT_PRICE),
			new Teleport("Evil Manor", -6989, 109503, -3040, DEFAULT_PRICE),
			new Teleport("Ruins of Agony", -42504, 120046, -3519, DEFAULT_PRICE),
			new Teleport("Ruins of Despair", -20057, 137618, -3897, DEFAULT_PRICE),
			new Teleport("Maille Lizardman Barracks", -31776, 115187, -2694, DEFAULT_PRICE),
			new Teleport("Forgotten Temple", -52841, 190730, -3518, DEFAULT_PRICE),
			new Teleport("Wastelands", -15545, 208817, -3665, DEFAULT_PRICE),
			new Teleport("Ant Nest", -9993, 176457, -4182, DEFAULT_PRICE),
			new Teleport("Windawood Manor", -23789, 169683, -3424, DEFAULT_PRICE)
		}),
		new Category("dion", "Dion Region", new Teleport[]
		{
			new Teleport("Town of Dion", 15670, 142983, -2705, DEFAULT_PRICE),
			new Teleport("Beehive Fortress", 20505, 189036, -3344, DEFAULT_PRICE),
			new Teleport("Cruma Marshlands", 5941, 125455, -3640, DEFAULT_PRICE),
			new Teleport("Cruma Tower", 17192, 114178, -3439, DEFAULT_PRICE),
			new Teleport("Dion Hills", 26176, 141702, -2914, DEFAULT_PRICE),
			new Teleport("Execution Grounds", 51055, 141959, -2869, DEFAULT_PRICE),
			new Teleport("Floran Village", 17430, 170103, -3496, DEFAULT_PRICE),
			new Teleport("Fortress of Resistance", 46467, 126885, -3720, DEFAULT_PRICE),
			new Teleport("Dion Plains", 9980, 173167, -3734, DEFAULT_PRICE)
		}),
		new Category("giran", "Giran Region", new Teleport[]
		{
			new Teleport("Town of Giran", 83400, 147943, -3404, DEFAULT_PRICE),
			new Teleport("Giran Harbor", 47942, 186764, -3485, DEFAULT_PRICE),
			new Teleport("Breka Stronghold", 79798, 130624, -3677, DEFAULT_PRICE),
			new Teleport("Death Pass", 70000, 126636, -3804, DEFAULT_PRICE),
			new Teleport("Dragon Valley East", 73029, 118504, -3683, DEFAULT_PRICE),
			new Teleport("Dragon Valley West", 122824, 110836, -3720, DEFAULT_PRICE),
			new Teleport("Hardin's Academy", 105918, 109759, -3207, DEFAULT_PRICE),
			new Teleport("Garden of Gorgon", 113553, 134813, -3640, DEFAULT_PRICE),
			new Teleport("Tanor Canyon", 58316, 163851, -2816, DEFAULT_PRICE),
			new Teleport("Devil's Island", 43408, 206881, -3752, DEFAULT_PRICE),
			new Teleport("Antharas Lair", 132828, 114421, -3725, DEFAULT_PRICE)
		}),
		new Category("oren", "Oren & Hunter Region", new Teleport[]
		{
			new Teleport("Town of Oren", 82956, 53162, -1495, DEFAULT_PRICE),
			new Teleport("Hunter Village", 116589, 76268, -2734, DEFAULT_PRICE),
			new Teleport("Ivory Tower", 85348, 16142, -3699, DEFAULT_PRICE),
			new Teleport("Sea of Spores", 64328, 26803, -3768, DEFAULT_PRICE),
			new Teleport("Plains of the Lizardmen", 87252, 85514, -3056, DEFAULT_PRICE),
			new Teleport("Timak Outpost", 67097, 68815, -3648, DEFAULT_PRICE),
			new Teleport("Bandit Stronghold", 88396, -20821, -2038, DEFAULT_PRICE),
			new Teleport("Forest of Mirrors", 142065, 81300, -3000, DEFAULT_PRICE),
			new Teleport("Enchanted Valley North", 104635, 34561, -4001, DEFAULT_PRICE),
			new Teleport("Enchanted Valley South", 124904, 61992, -3920, DEFAULT_PRICE),
			new Teleport("Angel Waterfall", 166182, 91560, -3168, DEFAULT_PRICE)
		}),
		new Category("aden", "Aden Region", new Teleport[]
		{
			new Teleport("Town of Aden", 147476, 26925, -2204, DEFAULT_PRICE),
			new Teleport("Ancient Battleground", 127739, -6998, -3869, DEFAULT_PRICE),
			new Teleport("Blazing Swamp", 159455, -12931, -2872, DEFAULT_PRICE),
			new Teleport("Devastated Castle", 178271, -13326, -2268, DEFAULT_PRICE),
			new Teleport("Field of Massacre", 179718, -7843, -3517, DEFAULT_PRICE),
			new Teleport("Giant's Cave", 174528, 52683, -4364, DEFAULT_PRICE),
			new Teleport("Plains of Glory", 135756, 19557, -3424, DEFAULT_PRICE),
			new Teleport("Forsaken Plains", 177318, 48447, -3835, DEFAULT_PRICE),
			new Teleport("Cemetery", 172136, 20325, -3321, DEFAULT_PRICE),
			new Teleport("Seal of Shilen", 188611, 20588, -3696, DEFAULT_PRICE),
			new Teleport("Fury Plains", 156898, 11217, -4032, DEFAULT_PRICE)
		}),
		new Category("goddard", "Goddard Region", new Teleport[]
		{
			new Teleport("Town of Goddard", 147928, -55273, -2734, DEFAULT_PRICE),
			new Teleport("Forge of the Gods", 169018, -116303, -2432, DEFAULT_PRICE),
			new Teleport("Hot Springs", 149594, -112698, -2065, DEFAULT_PRICE),
			new Teleport("Imperial Tomb", 186699, -75915, -2824, DEFAULT_PRICE),
			new Teleport("Monastery of Silence", 106414, -87799, -2920, DEFAULT_PRICE),
			new Teleport("Ketra Orc Outpost", 146954, -67390, -3660, DEFAULT_PRICE),
			new Teleport("Varka Silenos Outpost", 125543, -40953, -3724, DEFAULT_PRICE),
			new Teleport("Garden of Beasts", 132997, -60608, -2960, DEFAULT_PRICE),
			new Teleport("Border Outpost", 114172, -18034, -1875, DEFAULT_PRICE),
			new Teleport("Rainbow Springs Chateau", 141039, -123217, -1915, DEFAULT_PRICE)
		}),
		new Category("rune", "Rune Region", new Teleport[]
		{
			new Teleport("Town of Rune", 43799, -47727, -798, DEFAULT_PRICE),
			new Teleport("Rune Harbor", 38025, -38359, -3608, DEFAULT_PRICE),
			new Teleport("Forest of the Dead", 52112, -53939, -3159, DEFAULT_PRICE),
			new Teleport("Cursed Village", 57670, -41672, -3144, DEFAULT_PRICE),
			new Teleport("Valley of Saints", 67992, -72012, -3748, DEFAULT_PRICE),
			new Teleport("Stakato Nest", 88969, -45307, -2104, DEFAULT_PRICE),
			new Teleport("Wild Beast Farm", 57059, -82976, -2847, DEFAULT_PRICE),
			new Teleport("Wild Beast Pastures", 56376, -92568, -1357, DEFAULT_PRICE),
			new Teleport("Swamp of Screams", 70006, -49902, -3251, DEFAULT_PRICE),
			new Teleport("Windtail Waterfall", 40723, -92245, -3747, DEFAULT_PRICE)
		}),
		new Category("schuttgart", "Schuttgart Region", new Teleport[]
		{
			new Teleport("Town of Schuttgart", 87386, -143246, -1293, DEFAULT_PRICE),
			new Teleport("Pavel Ruins", 88288, -125692, -3816, DEFAULT_PRICE),
			new Teleport("Crypts of Disgrace", 56095, -118952, -3290, DEFAULT_PRICE),
			new Teleport("Den of Evil", 76860, -125169, -3414, DEFAULT_PRICE),
			new Teleport("Frost Lake", 107577, -122392, -3632, DEFAULT_PRICE),
			new Teleport("Ice Merchant Cabin", 124877, -116103, -2585, DEFAULT_PRICE),
			new Teleport("Archaic Laboratory", 91496, -112114, -3321, DEFAULT_PRICE),
			new Teleport("Brigands Stronghold", 127898, -160595, -1237, DEFAULT_PRICE),
			new Teleport("Caron's Dungeon", 68680, -110483, -1925, DEFAULT_PRICE),
			new Teleport("Plunderous Plains", 109024, -159223, -1778, DEFAULT_PRICE)
		}),
		new Category("gracia", "Gracia", new Teleport[]
		{
			new Teleport("Airship Field", -149365, 255309, -86, DEFAULT_PRICE),
			new Teleport("Keucereus Base", -186742, 244167, 2670, DEFAULT_PRICE),
			new Teleport("Seed of Infinity", -213678, 210670, 4408, DEFAULT_PRICE),
			new Teleport("Seed of Destruction", -247012, 251804, 4340, DEFAULT_PRICE),
			new Teleport("Seed of Annihilation", -175520, 154505, 2712, DEFAULT_PRICE),
			new Teleport("Infinity Gate", -183443, 205834, -12902, DEFAULT_PRICE),
			new Teleport("Destruction Gate", -241752, 219983, -9985, DEFAULT_PRICE),
			new Teleport("Annihilation Gate", -180218, 185923, -10576, DEFAULT_PRICE),
			new Teleport("Sel Mahum Training Grounds", 76883, 63814, -3655, DEFAULT_PRICE)
		}),
		new Category("toi", "Tower of Insolence", new Teleport[]
		{
			new Teleport("1st Floor", 115168, 16022, -5100, DEFAULT_PRICE),
			new Teleport("2nd Floor", 114649, 18587, -3609, DEFAULT_PRICE),
			new Teleport("3rd Floor", 117918, 16039, -2127, DEFAULT_PRICE),
			new Teleport("4th Floor", 114622, 12946, -645, DEFAULT_PRICE),
			new Teleport("5th Floor", 112209, 16078, 928, DEFAULT_PRICE),
			new Teleport("6th Floor", 112376, 16099, 1947, DEFAULT_PRICE),
			new Teleport("7th Floor", 116827, 16025, 2956, DEFAULT_PRICE),
			new Teleport("8th Floor", 111063, 16118, 3967, DEFAULT_PRICE),
			new Teleport("9th Floor", 117147, 18415, 4977, DEFAULT_PRICE),
			new Teleport("10th Floor", 118374, 15973, 5987, DEFAULT_PRICE),
			new Teleport("11th Floor", 112856, 16078, 7028, DEFAULT_PRICE),
			new Teleport("12th Floor", 114809, 18711, 7996, DEFAULT_PRICE)
		}),
		new Category("catacombs", "Catacombs", new Teleport[]
		{
			new Teleport("Catacomb of the Heretic", 42514, 143917, -5385, DEFAULT_PRICE),
			new Teleport("Catacomb of the Branded", 45770, 170299, -4985, DEFAULT_PRICE),
			new Teleport("Catacomb of the Apostate", 77225, 78362, -5119, DEFAULT_PRICE),
			new Teleport("Catacomb of the Witch", 139965, 79678, -5433, DEFAULT_PRICE),
			new Teleport("Catacomb of Dark Omens", -19931, 13502, -4905, DEFAULT_PRICE),
			new Teleport("Catacomb of the Forbidden Path", 113429, 84540, -6545, DEFAULT_PRICE)
		}),
		new Category("necro", "Necropolises", new Teleport[]
		{
			new Teleport("Necropolis of Sacrifice", -41567, 209292, -5091, DEFAULT_PRICE),
			new Teleport("Pilgrim's Necropolis", 45250, 124366, -5417, DEFAULT_PRICE),
			new Teleport("Necropolis of Worship", 110818, 174010, -5443, DEFAULT_PRICE),
			new Teleport("Patriot's Necropolis", -22197, 77369, -5177, DEFAULT_PRICE),
			new Teleport("Necropolis of Devotion", -52716, 79106, -4745, DEFAULT_PRICE),
			new Teleport("Necropolis of Martyrdom", 117793, 132810, -4835, DEFAULT_PRICE),
			new Teleport("Saint's Necropolis", 82608, 209225, -5443, DEFAULT_PRICE),
			new Teleport("Disciples Necropolis", 171902, -17595, -4905, DEFAULT_PRICE)
		}),
		new Category("primeval", "Primeval Isle", new Teleport[]
		{
			new Teleport("Primeval Isle Wharf", 6229, -2924, -2965, DEFAULT_PRICE),
			new Teleport("Primeval Harbor", 10468, -24569, -3650, DEFAULT_PRICE),
			new Teleport("Research Facility", 8264, -14431, -3696, DEFAULT_PRICE),
			new Teleport("Lost Nest", 26174, -17134, -2747, DEFAULT_PRICE)
		}),
		new Category("hellbound", "Hellbound", new Teleport[]
		{
			new Teleport("Parnassus", 135890, 165149, -1911, DEFAULT_PRICE),
			new Teleport("Hellbound Entrance", -18856, 237466, -2868, DEFAULT_PRICE),
			new Teleport("Ancient Temple Remains", -27286, 253422, -2195, DEFAULT_PRICE),
			new Teleport("Battered Lands", 401, 234949, -3268, DEFAULT_PRICE),
			new Teleport("Caravan Encampment", -4617, 255617, -3140, DEFAULT_PRICE),
			new Teleport("Enchanted Megaliths", -22247, 243132, -3070, DEFAULT_PRICE),
			new Teleport("Hellbound Quarry", -8003, 242048, -1907, DEFAULT_PRICE),
			new Teleport("Hidden Oasis", -20465, 250282, -3236, DEFAULT_PRICE),
			new Teleport("Sand Swept Dunes", -14065, 254387, -3444, DEFAULT_PRICE)
		}),
		new Category("other", "Other & Events", new Teleport[]
		{
			new Teleport("Coliseum", 146440, 46723, -3432, DEFAULT_PRICE),
			new Teleport("Giran Arena", 73890, 142656, -3778, DEFAULT_PRICE),
			new Teleport("Gludin Arena", -86979, 142402, -3643, DEFAULT_PRICE),
			new Teleport("Race Track", 12312, 182752, -3558, DEFAULT_PRICE),
			new Teleport("Fantasy Island", -59703, -56061, -2031, DEFAULT_PRICE),
			new Teleport("Fantasy Falls", -75755, -51107, -1725, DEFAULT_PRICE)
		})
	};

	private static TeleportBBSManager _instance;

	public static TeleportBBSManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new TeleportBBSManager();
		}
		return _instance;
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsteleport;") || command.equals("_bbsteleport;main"))
		{
			showMain(activeChar);
			return;
		}
		if (command.startsWith("_bbsteleport;cat;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			if (st.hasMoreTokens())
			{
				showCategory(activeChar, st.nextToken());
				return;
			}
		}
		if (command.startsWith("_bbsteleport;teleport;"))
		{
			StringTokenizer stGoTp = new StringTokenizer(command, " ");
			stGoTp.nextToken();
			int xTp = Integer.parseInt(stGoTp.nextToken());
			int yTp = Integer.parseInt(stGoTp.nextToken());
			int zTp = Integer.parseInt(stGoTp.nextToken());
			int priceTp = Integer.parseInt(stGoTp.nextToken());
			goTp(activeChar, xTp, yTp, zTp, priceTp);
			showMain(activeChar);
			return;
		}

		ShowBoard sb = new ShowBoard("<html><body><br><br><center>The command: " + command + " is not implemented yet.</center><br><br></body></html>", "101");
		activeChar.sendPacket(sb);
		activeChar.sendPacket(new ShowBoard(null, "102"));
		activeChar.sendPacket(new ShowBoard(null, "103"));
	}

	private void goTp(L2PcInstance activeChar, int xTp, int yTp, int zTp, int priceTp)
	{
		if (activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isInJail() || activeChar.isFlying() || (activeChar.getKarma() > 0) || activeChar.isInDuel())
		{
			activeChar.sendMessage("Teleportation is not possible right now.");
			return;
		}

		if (activeChar.isInsideZone(ZoneId.PVP))
		{
			activeChar.sendMessage("You cannot use the Global GK in a PvP zone.");
			return;
		}

		if ((priceTp > 0) && (activeChar.getAdena() < priceTp))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		else if (priceTp > 0)
		{
			activeChar.reduceAdena("CommunityTeleport", priceTp, activeChar, true);
		}
		activeChar.teleToLocation(xTp, yTp, zTp);
	}

	private void showMain(L2PcInstance activeChar)
	{
		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append(buildHeader("GLOBAL GATEKEEPER"));
		html.append("<table width=740 border=0 cellspacing=6 cellpadding=4>");

		int col = 0;
		for (int i = 0; i < CATEGORIES.length; i++)
		{
			if (col == 0)
			{
				html.append("<tr>");
			}
			html.append("<td align=center width=240>");
			html.append("<button value=\"").append(CATEGORIES[i].title).append("\" action=\"bypass -h _bbsteleport;cat;").append(CATEGORIES[i].key).append("\" width=220 height=28 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			html.append("</td>");
			col++;
			if (col == 3)
			{
				html.append("</tr>");
				col = 0;
			}
		}
		if (col != 0)
		{
			while (col < 3)
			{
				html.append("<td width=240></td>");
				col++;
			}
			html.append("</tr>");
		}
		html.append("</table>");
		html.append("<br><font color=B09878>All teleports currently cost ").append(DEFAULT_PRICE).append(" Adena.</font>");
		html.append(buildFooter());
		html.append("</center></body></html>");
		separateAndSend(html.toString(), activeChar);
	}

	private void showCategory(L2PcInstance activeChar, String key)
	{
		Category category = getCategory(key);
		if (category == null)
		{
			showMain(activeChar);
			return;
		}

		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append(buildHeader(category.title));
		html.append("<table width=740 border=0 cellspacing=6 cellpadding=4>");

		int col = 0;
		for (int i = 0; i < category.teleports.length; i++)
		{
			Teleport tp = category.teleports[i];
			if (col == 0)
			{
				html.append("<tr>");
			}
			html.append("<td align=center width=240>");
			html.append("<button value=\"").append(tp.name).append(" - ").append(tp.price).append(" Adena\" action=\"bypass -h _bbsteleport;teleport; ").append(tp.x).append(" ").append(tp.y).append(" ").append(tp.z).append(" ").append(tp.price).append("\" width=220 height=28 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			html.append("</td>");
			col++;
			if (col == 3)
			{
				html.append("</tr>");
				col = 0;
			}
		}
		if (col != 0)
		{
			while (col < 3)
			{
				html.append("<td width=240></td>");
				col++;
			}
			html.append("</tr>");
		}
		
		html.append("</table>");
		html.append("<br><button value=\"Back to GK\" action=\"bypass -h _bbsteleport;\" width=160 height=26 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		html.append(buildFooter());
		html.append("</center></body></html>");
		separateAndSend(html.toString(), activeChar);
	}

	private Category getCategory(String key)
	{
		for (int i = 0; i < CATEGORIES.length; i++)
		{
			if (CATEGORIES[i].key.equalsIgnoreCase(key))
			{
				return CATEGORIES[i];
			}
		}
		return null;
	}

	private String buildHeader(String title)
	{
		StringBuilder html = new StringBuilder();
		html.append("<br><br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0><tr><td width=80><img src=l2ui.bbs_lineage2 height=16 width=80></td></tr></table>");
		html.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1>");
		html.append("<table><tr>");
		html.append(menuButton("HOME", "_bbstop"));
		html.append(menuButton("GLOBAL GK", "_bbsteleport;"));
		html.append(menuButton("TOP PVP", "_bbstop;pvp"));
		html.append(menuButton("TOP PK", "_bbstop;pk"));
		html.append(menuButton("DONATE", "_bbstop;donate"));
		html.append(menuButton("INFO", "_bbstop;info"));
		html.append("</tr></table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1>");
		html.append("<br><br><font color=LEVEL>").append(title).append("</font><br><br>");
		return html.toString();
	}

	private String buildFooter()
	{
		return "<br><img src=\"L2UI.SquareGray\" width=770 height=1><br><font color=LEVEL>L2Fidas Community Board</font>";
	}

	private String menuButton(String label, String bypass)
	{
		return "<td><button value=\"" + label + "\" action=\"bypass -h " + bypass + "\" width=110 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>";
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}

	private static class Category
	{
		private final String key;
		private final String title;
		private final Teleport[] teleports;

		private Category(String key, String title, Teleport[] teleports)
		{
			this.key = key;
			this.title = title;
			this.teleports = teleports;
		}
	}

	private static class Teleport
	{
		private final String name;
		private final int x;
		private final int y;
		private final int z;
		private final int price;

		private Teleport(String name, int x, int y, int z, int price)
		{
			this.name = name;
			this.x = x;
			this.y = y;
			this.z = z;
			this.price = price;
		}
	}
}
