package fidas.server.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fidas.server.Config;
import fidas.server.L2DatabaseFactory;

public class PetNameTable
{
	private static Logger _log = Logger.getLogger(PetNameTable.class.getName());
	
	public static PetNameTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public boolean doesPetNameExist(String name, int petNpcId)
	{
		boolean result = true;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id IN (?)"))
		{
			ps.setString(1, name);
			StringBuilder cond = new StringBuilder();
			for (int it : PetDataTable.getPetItemsByNpc(petNpcId))
			{
				if (!cond.toString().isEmpty())
				{
					cond.append(", ");
				}
				cond.append(it);
			}
			ps.setString(2, cond.toString());
			try (ResultSet rs = ps.executeQuery())
			{
				result = rs.next();
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not check existing petname:" + e.getMessage(), e);
		}
		return result;
	}
	
	public boolean isValidPetName(String name)
	{
		boolean result = true;
		
		if (!isAlphaNumeric(name))
		{
			return result;
		}
		
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.PET_NAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			_log.warning(getClass().getSimpleName() + ": Pet name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(name);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	private boolean isAlphaNumeric(String text)
	{
		boolean result = true;
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			if (!Character.isLetterOrDigit(chars[i]))
			{
				result = false;
				break;
			}
		}
		return result;
	}
	
	private static class SingletonHolder
	{
		protected static final PetNameTable _instance = new PetNameTable();
	}
}