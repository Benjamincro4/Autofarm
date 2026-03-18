package fidas.server.gameserver.dressme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

import fidas.server.L2DatabaseFactory;

public class DressMeDAO
{
	private static final DressMeDAO INSTANCE = new DressMeDAO();

	private static final String SELECT_ACCOUNT_SETS =
		"SELECT dressme_armor_sets FROM accounts WHERE login=?";
	private static final String UPDATE_ACCOUNT_SETS =
		"UPDATE accounts SET dressme_armor_sets=? WHERE login=?";

	private static final String SELECT_EQUIPPED_SET =
		"SELECT dressme_armor_set FROM characters WHERE charId=?";
	private static final String UPDATE_EQUIPPED_SET =
		"UPDATE characters SET dressme_armor_set=? WHERE charId=?";

	private static final String SELECT_NAME_COLOR =
		"SELECT char_name_color FROM characters WHERE charId=?";
	private static final String UPDATE_NAME_COLOR =
		"UPDATE characters SET char_name_color=? WHERE charId=?";

	private static final String SELECT_TITLE_COLOR =
		"SELECT title_color FROM characters WHERE charId=?";
	private static final String UPDATE_TITLE_COLOR =
		"UPDATE characters SET title_color=? WHERE charId=?";

	public static DressMeDAO getInstance()
	{
		return INSTANCE;
	}

	public boolean ownsArmorSet(String accountName, int setId)
	{
		Set<Integer> owned = loadOwnedArmorSets(accountName);
		return owned.contains(setId);
	}

	public Set<Integer> loadOwnedArmorSets(String accountName)
	{
		Set<Integer> result = new LinkedHashSet<Integer>();

		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_ACCOUNT_SETS))
		{
			ps.setString(1, accountName);

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					String raw = rs.getString("dressme_armor_sets");
					if (raw != null && !raw.trim().isEmpty())
					{
						String[] tokens = raw.split(";");
						for (String token : tokens)
						{
							if (token == null)
							{
								continue;
							}

							token = token.trim();
							if (token.isEmpty())
							{
								continue;
							}

							try
							{
								result.add(Integer.parseInt(token));
							}
							catch (NumberFormatException e)
							{
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public void addOwnedArmorSet(String accountName, int setId)
	{
		Set<Integer> owned = loadOwnedArmorSets(accountName);
		if (owned.contains(setId))
		{
			return;
		}

		owned.add(setId);
		String serialized = serialize(owned);

		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_ACCOUNT_SETS))
		{
			ps.setString(1, serialized);
			ps.setString(2, accountName);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public int loadEquippedArmorSet(int charId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_EQUIPPED_SET))
		{
			ps.setInt(1, charId);

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return rs.getInt("dressme_armor_set");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	public void saveEquippedArmorSet(int charId, int setId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_EQUIPPED_SET))
		{
			ps.setInt(1, setId);
			ps.setInt(2, charId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public int loadNameColor(int charId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_NAME_COLOR))
		{
			ps.setInt(1, charId);

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return rs.getInt("char_name_color");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	public void saveNameColor(int charId, int color)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_NAME_COLOR))
		{
			ps.setInt(1, color);
			ps.setInt(2, charId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public int loadTitleColor(int charId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_TITLE_COLOR))
		{
			ps.setInt(1, charId);

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return rs.getInt("title_color");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	public void saveTitleColor(int charId, int color)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_TITLE_COLOR))
		{
			ps.setInt(1, color);
			ps.setInt(2, charId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private String serialize(Set<Integer> owned)
	{
		StringBuilder sb = new StringBuilder();

		for (Integer id : owned)
		{
			if (id == null)
			{
				continue;
			}

			if (sb.length() > 0)
			{
				sb.append(';');
			}

			sb.append(id.intValue());
		}

		return sb.toString();
	}
}