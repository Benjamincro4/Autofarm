package handlers.bypasshandlers;

import fidas.server.gameserver.cache.HtmCache;
import fidas.server.gameserver.handler.IBypassHandler;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * With this bypass you can add custom links to html files.<br>
 * <a action="bypass -h CustomLinks custom.html">Custom Link</a><br>
 * Root directory data/html/
 * @version 1.0
 * @author Zoey76
 */
public class CustomLinks implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"CustomLinks"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		try
		{
			String path = command.substring(11).trim();
			if (path.indexOf("..") != -1)
				return false;
			String filename = "data/html/" + path;
	
			String content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), filename);
			
			if ((content == null) || content.isEmpty())
			{
				_log.warning("[" + getClass().getSimpleName() + "] " + filename + " content is null or empty.");
				return false;
			}
			
			activeChar.sendPacket(new NpcHtmlMessage(0, content));
			return true;
		}
		catch (Exception e)
		{
			_log.warning("[" + getClass().getSimpleName() + "] Exception: " + e);
		}
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}