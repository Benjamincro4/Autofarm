package fidas.server.gameserver.events.model;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import fidas.server.gameserver.events.Config;

public class ManagerNpcHtml
{
	TextBuilder sb = new TextBuilder();
	
	public ManagerNpcHtml(String content)
	{
		
		FastList<String> buttons = new FastList<>();
		
		if (Config.getInstance().getBoolean(0, "voteEnabled"))
		{
			buttons.add("<button value=\"Vote\" action=\"bypass -h eventmanager showvotelist\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (Config.getInstance().getBoolean(0, "eventBufferEnabled"))
		{
			buttons.add("<button value=\"Buffer\" action=\"bypass -h eventmanager buffershow\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (Config.getInstance().getBoolean(0, "schedulerEnabled"))
		{
			buttons.add("<button value=\"Scheduler\" action=\"bypass -h eventmanager scheduler\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		buttons.add("<button value=\"Running\" action=\"bypass -h eventmanager running\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		
		sb.append("<html><title>Event Manager - Main Events</title><body>");
		
		sb.append("<table width=270 border=0 bgcolor=666666><tr>");
		
		int c = 0;
		
		for (String button : buttons)
		{
			c++;
			if (c == 4)
			{
				sb.append("</tr><tr>");
			}
			
			sb.append("<td width=90>" + button + "</td>");
		}
		
		switch (c)
		{
			case 1:
				sb.append("<td width=90></td><td width=90></td>");
				break;
			case 2:
				sb.append("<td width=90></td>");
				break;
			case 4:
				sb.append("<td width=90></td><td width=90></td>");
				break;
			case 5:
				sb.append("<td width=90></td>");
				break;
		}
		
		sb.append("</tr></table>");
		
		sb.append("<br>");
		
		sb.append(content);
		
		sb.append("</body></html>");
	}
	
	public String string()
	{
		return sb.toString();
	}
}