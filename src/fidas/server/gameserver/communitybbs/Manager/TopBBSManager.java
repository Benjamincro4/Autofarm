package fidas.server.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.ShowBoard;

public class TopBBSManager extends BaseBBSManager
{
	private static final int RANK_LIMIT = 20;
	private static final String HTML_PATH = "data/html/CommunityBoard/";

	private TopBBSManager()
	{
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbstop") || command.equals("_bbshome"))
		{
			sendHtm(activeChar, HTML_PATH + "index.htm");
			return;
		}

		if (command.startsWith("_bbstop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();

			if (!st.hasMoreTokens())
			{
				sendHtm(activeChar, HTML_PATH + "index.htm");
				return;
			}

			String page = st.nextToken();
			if ("donate".equalsIgnoreCase(page))
			{
				sendHtm(activeChar, HTML_PATH + "donate.htm");
				return;
			}
			else if ("info".equalsIgnoreCase(page))
			{
				sendHtm(activeChar, HTML_PATH + "info.htm");
				return;
			}
			else if ("pvp".equalsIgnoreCase(page))
			{
				showTopPvp(activeChar);
				return;
			}
			else if ("pk".equalsIgnoreCase(page))
			{
				showTopPk(activeChar);
				return;
			}
			else if (isNumeric(page))
			{
				sendHtm(activeChar, HTML_PATH + page + ".htm");
				return;
			}
		}

		showNotImplemented(command, activeChar);
	}

	private void showTopPvp(L2PcInstance activeChar)
	{
		showRanking(activeChar, "TOP PVP", "pvpkills", true);
	}

	private void showTopPk(L2PcInstance activeChar)
	{
		showRanking(activeChar, "TOP PK", "pkkills", false);
	}

	private void showRanking(L2PcInstance activeChar, String title, String field, boolean showPvp)
	{
		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		html.append(buildHeader(title));
		html.append("<table width=760 border=0 cellspacing=0 cellpadding=4>");
		html.append("<tr>");
		html.append("<td width=40 align=center><font color=LEVEL>#</font></td>");
		html.append("<td width=270 align=center><font color=LEVEL>Character</font></td>");
		html.append("<td width=120 align=center><font color=LEVEL>Level</font></td>");
		html.append("<td width=120 align=center><font color=LEVEL>").append(showPvp ? "PvP" : "PK").append("</font></td>");
		html.append("<td width=120 align=center><font color=LEVEL>Status</font></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=760 height=1>");
		html.append("<table width=760 border=0 cellspacing=0 cellpadding=3>");

		boolean hasRows = false;
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT char_name, level, pvpkills, pkkills, online FROM characters WHERE " + field + " > 0 ORDER BY " + field + " DESC LIMIT ?");
			st.setInt(1, RANK_LIMIT);
			rs = st.executeQuery();

			int pos = 1;
			while (rs.next())
			{
				hasRows = true;
				html.append("<tr>");
				html.append("<td width=40 align=center>").append(pos++).append("</td>");
				html.append("<td width=270 align=center>").append(safe(rs.getString("char_name"))).append("</td>");
				html.append("<td width=120 align=center>").append(rs.getInt("level")).append("</td>");
				html.append("<td width=120 align=center>").append(rs.getInt(field)).append("</td>");
				html.append("<td width=120 align=center>").append(rs.getInt("online") > 0 ? "<font color=00FF00>Online</font>" : "Offline").append("</td>");
				html.append("</tr>");
			}
		}
		catch (Exception e)
		{
			html.append("<tr><td width=760 align=center><font color=FF5555>Error loading ranking.</font></td></tr>");
		}
		finally
		{
			closeQuietly(rs);
			closeQuietly(st);
			closeQuietly(con);
		}

		if (!hasRows)
		{
			html.append("<tr><td width=760 align=center>No characters found.</td></tr>");
		}
		html.append("</table>");
		html.append("<br><button value=\"Back\" action=\"bypass -h _bbstop\" width=160 height=26 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		html.append(buildFooter());
		html.append("</center></body></html>");
		separateAndSend(html.toString(), activeChar);
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

	private boolean sendHtm(L2PcInstance player, String path)
	{
		String oriPath = path;
		if ((player.getLang() != null) && !player.getLang().equalsIgnoreCase("en") && path.indexOf("html/") >= 0)
		{
			path = path.replace("html/", "html-" + player.getLang() + "/");
		}

		String content = HtmCache.getInstance().getHtm(path);
		if ((content == null) && !oriPath.equals(path))
		{
			content = HtmCache.getInstance().getHtm(oriPath);
		}
		if (content == null)
		{
			showNotImplemented(path, player);
			return false;
		}

		separateAndSend(content, player);
		return true;
	}

	private boolean isNumeric(String str)
	{
		try
		{
			Integer.parseInt(str);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}

	private String safe(String value)
	{
		return value == null ? "" : value;
	}

	private void closeQuietly(ResultSet rs)
	{
		if (rs != null)
		{
			try
			{
				rs.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private void closeQuietly(PreparedStatement st)
	{
		if (st != null)
		{
			try
			{
				st.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private void closeQuietly(Connection con)
	{
		if (con != null)
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private void showNotImplemented(String command, L2PcInstance activeChar)
	{
		ShowBoard sb = new ShowBoard("<html><body><br><br><center>The command: " + command + " is not implemented yet.</center><br><br></body></html>", "101");
		activeChar.sendPacket(sb);
		activeChar.sendPacket(new ShowBoard(null, "102"));
		activeChar.sendPacket(new ShowBoard(null, "103"));
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}

	public static TopBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final TopBBSManager _instance = new TopBBSManager();
	}
}
