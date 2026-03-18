package fidas.server.gameserver.datatables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.actor.templates.L2PcTemplate;
import fidas.server.gameserver.model.base.ClassId;

public final class CharTemplateTable
{
	private static final Logger _log = Logger.getLogger(CharTemplateTable.class.getName());
	
	private static final Map<ClassId, L2PcTemplate> _charTemplates = new HashMap<>();
	
	protected CharTemplateTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rset = s.executeQuery("SELECT * FROM char_templates, lvlupgain WHERE char_templates.classId = lvlupgain.classId ORDER BY char_templates.ClassId"))
		{
			StatsSet set;
			int cId;
			while (rset.next())
			{
				set = new StatsSet();
				cId = rset.getInt("ClassId");
				set.set("classId", cId);
				set.set("className", rset.getString("className"));
				set.set("raceId", rset.getInt("raceId"));
				set.set("baseSTR", rset.getInt("STR"));
				set.set("baseCON", rset.getInt("CON"));
				set.set("baseDEX", rset.getInt("DEX"));
				set.set("baseINT", rset.getInt("_INT"));
				set.set("baseWIT", rset.getInt("WIT"));
				set.set("baseMEN", rset.getInt("MEN"));
				set.set("baseHpMax", rset.getFloat("defaultHpBase"));
				set.set("lvlHpAdd", rset.getFloat("defaultHpAdd"));
				set.set("lvlHpMod", rset.getFloat("defaultHpMod"));
				set.set("baseMpMax", rset.getFloat("defaultMpBase"));
				set.set("baseCpMax", rset.getFloat("defaultCpBase"));
				set.set("lvlCpAdd", rset.getFloat("defaultCpAdd"));
				set.set("lvlCpMod", rset.getFloat("defaultCpMod"));
				set.set("lvlMpAdd", rset.getFloat("defaultMpAdd"));
				set.set("lvlMpMod", rset.getFloat("defaultMpMod"));
				set.set("baseHpReg", 2);
				set.set("baseMpReg", 0.9);
				set.set("basePAtk", rset.getInt("p_atk"));
				set.set("basePDef", rset.getInt("p_def"));
				set.set("baseMAtk", rset.getInt("m_atk"));
				set.set("baseMDef", rset.getInt("m_def"));
				set.set("classBaseLevel", rset.getInt("class_lvl"));
				set.set("baseRunSpd", rset.getInt("move_spd"));
				set.set("baseWalkSpd", 0);
				set.set("baseShldDef", 0);
				set.set("baseShldRate", 0);
				set.set("baseAtkRange", 40);
				
				set.set("spawnX", rset.getInt("x"));
				set.set("spawnY", rset.getInt("y"));
				set.set("spawnZ", rset.getInt("z"));
				
				set.set("collision_radius", rset.getDouble("m_col_r"));
				set.set("collision_height", rset.getDouble("m_col_h"));
				set.set("collision_radius_female", rset.getDouble("f_col_r"));
				set.set("collision_height_female", rset.getDouble("f_col_h"));
				
				final L2PcTemplate ct = new L2PcTemplate(set);
				_charTemplates.put(ClassId.getClassId(cId), ct);
			}
			_log.info(getClass().getSimpleName() + ": " + _charTemplates.size() + " Characters.");
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed loading char templates", e);
		}
	}
	
	public L2PcTemplate getTemplate(final ClassId classId)
	{
		return _charTemplates.get(classId);
	}
	
	public L2PcTemplate getTemplate(final int classId)
	{
		return _charTemplates.get(ClassId.getClassId(classId));
	}
	
	public static final CharTemplateTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CharTemplateTable _instance = new CharTemplateTable();
	}
}