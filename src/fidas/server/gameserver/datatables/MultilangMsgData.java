package fidas.server.gameserver.datatables;

import fidas.server.Config;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MultilangMsgData
{
	private static Logger _log = Logger.getLogger(MultilangMsgData.class.getName());

	/** Common holder for language Maps of messages: <langCode, messageMap> **/
	private Map<String, HashMap<String, String>> _messages;

	public static MultilangMsgData getInstance()
	{
		return SingletonHolder._instance;
	}

	private MultilangMsgData()
	{
		_messages = new FastMap<>();
		load("en");

		if (Config.MULTILANG_MLM_ENABLE)
		{
			for (final String lang : Config.MULTILANG_MLM_ALLOWED)
			{
				load(lang);
			}		
		}
	}

	/**
	 * Loads all strings from XML file	
	 * @param lang 
	 */
	public void load(String lang)
	{
		File configFile = new File(Config.DATAPACK_ROOT, "/data/lang/" + lang + "/mlm/strings.xml");

		if (!configFile.isFile())
		{
			return;
		}

		// Check if language is already loaded
		if (_messages.containsKey(lang))
		{
			return;
		}

		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(configFile);
			if (!doc.getDocumentElement().getNodeName().equals("list"))
			{
				throw new NullPointerException("WARNING!!! Bad strings.xml file for language: '" + lang + "'");
			}

			HashMap<String, String> messages = new HashMap<>();

			for (Node d = doc.getDocumentElement().getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("string"))
				{
					try
					{
						String id = d.getAttributes().getNamedItem("id").getNodeValue();
						String text = d.getAttributes().getNamedItem("text").getNodeValue();
						messages.put(id, text);
					}
					catch (NullPointerException npe)
					{
						_log.warning(getClass().getSimpleName() + ": Error in strings.xml for lang code \"" + lang + "\"");
					}
				}
			}

			if (!messages.isEmpty())
			{
				_messages.put(lang, messages);
			}

			_log.info(getClass().getSimpleName() + ": " + messages.size() + " strings for lang \"" + lang + "\"");
		}
		catch (IOException e)
		{
			_log.warning(getClass().getSimpleName() + ": error reading " + configFile.getAbsolutePath() + " ! " + e.getMessage());
		}
		catch (Exception e) 
		{
    	e.printStackTrace();
  	}		
	}

	/**
	 * @param msgName name of message
	 * @param lang language code of message language
	 * @param msgText text string	 
	 * Adds msgText message with msgName on lang language into _messages 
	 */
	public void addMessage(String msgName, String lang, String msgText)
	{
		if (!_messages.containsKey(lang))
		{
			_messages.put(lang, new HashMap<String, String>());
		}

		_messages.get(lang).put(msgName, msgText);
	}

	/**
	 * @param player player to determine message language
	 * @param msgName name of message
	 * @return string message with given msgName for given player language,
	 * or string message with given msgName for "en" language, if not found for given language,
	 * or empty string, if not found message with given name	 
	 */
	public String getMessage(L2PcInstance player, String msgName)
	{
		String lang = player.getLang() == null ? "en" : player.getLang(); 

		// Try to find message in given language 
		if (_messages.containsKey(lang) && _messages.get(lang).containsKey(msgName))
		{
			return _messages.get(lang).get(msgName);
		}

		// Try to find message in "en", if not found in given language
		else if (!lang.equals("en") && _messages.containsKey("en") && _messages.get("en").containsKey(msgName))
		{
			return _messages.get("en").get(msgName);
		}

		return "";
	}

	/**
	 * @param msgName name of message
	 * @return Map<langCode, textString> of string messages with given msgName for all available languages,
	 */
	public Map<String, String> getMessageMap(String msgName)
	{
		Map<String, String> ret = new FastMap<>(); 

		if (_messages != null && !_messages.isEmpty())
		{
			for (String lang : _messages.keySet())
			{
				if (_messages.get(lang).containsKey(msgName))
				{
					ret.put(lang, _messages.get(lang).get(msgName));
				}
			}
		} 
		
		return ret.isEmpty() ? null : ret;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MultilangMsgData _instance = new MultilangMsgData();
	}
}