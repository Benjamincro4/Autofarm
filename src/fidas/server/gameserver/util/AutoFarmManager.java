package fidas.server.gameserver.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.autofarm.AutoFarmConfig;
import fidas.server.gameserver.autofarm.AutoFarmAction;
import fidas.server.gameserver.autofarm.EActionPriority;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;

/**
 * AutoFarmManager – singleton controller for the AutoFarm system.
 *
 * <ul>
 *   <li>Holds one {@link AutoFarmConfig} per online player (lazy-created).</li>
 *   <li>Runs a dedicated 800ms tick loop that calls {@code config.tick()} for every
 *       running config in parallel.</li>
 *   <li>Persists config to the {@code character_autofarm} DB table on save/logout.</li>
 * </ul>
 */
public class AutoFarmManager
{
	private static final Logger _log = Logger.getLogger(AutoFarmManager.class.getName());

	/** Tick interval in milliseconds. */
	public static final long TICK_MS = 800L;

	/** Map from charId → AutoFarmConfig. */
	private final ConcurrentHashMap<Integer, AutoFarmConfig> _configs = new ConcurrentHashMap<>();

	private final ScheduledExecutorService _scheduler =
		Executors.newSingleThreadScheduledExecutor(r -> {
			final Thread t = new Thread(r, "AutoFarm-Tick");
			t.setDaemon(true);
			return t;
		});

	// -----------------------------------------------------------------------
	// Singleton
	// -----------------------------------------------------------------------

	private AutoFarmManager()
	{
		load();
		_scheduler.scheduleAtFixedRate(this::tickAll, TICK_MS, TICK_MS, TimeUnit.MILLISECONDS);
	}

	public static AutoFarmManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		private static final AutoFarmManager _instance = new AutoFarmManager();
	}

	// -----------------------------------------------------------------------
	// Tick
	// -----------------------------------------------------------------------

	private void tickAll()
	{
		for (final AutoFarmConfig cfg : _configs.values())
		{
			if (!cfg.isRunning()) continue;
			try
			{
				cfg.tick();
			}
			catch (Exception e)
			{
				_log.warning("[AutoFarm] Exception in tick for charId=" + cfg.getOwnerId() + ": " + e.getMessage());
			}
		}
	}

	// -----------------------------------------------------------------------
	// Config access
	// -----------------------------------------------------------------------

	/**
	 * Returns the existing config for the player, or creates and loads a new one.
	 */
	public AutoFarmConfig getOrCreate(final L2PcInstance player)
	{
		return _configs.computeIfAbsent(player.getObjectId(), id ->
		{
			final AutoFarmConfig cfg = new AutoFarmConfig(id);
			loadConfig(cfg);
			return cfg;
		});
	}

	/** Returns the config if it exists, null otherwise. */
	public AutoFarmConfig get(final int charId)
	{
		return _configs.get(charId);
	}

	// -----------------------------------------------------------------------
	// Persistence
	// -----------------------------------------------------------------------

	/** Save all configs to the DB (typically called on server shutdown). */
	public void saveAll()
	{
		for (final AutoFarmConfig cfg : _configs.values())
			saveConfig(cfg);
	}

	/** Save a single player's config. */
	public void saveConfig(final AutoFarmConfig cfg)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		     PreparedStatement ps = con.prepareStatement(
		         "INSERT INTO character_autofarm (char_id, config) VALUES (?, ?) "
		         + "ON DUPLICATE KEY UPDATE config=VALUES(config)"))
		{
			ps.setInt(1, cfg.getOwnerId());
			ps.setString(2, cfg.serialize());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warning("[AutoFarm] Failed to save config for charId=" + cfg.getOwnerId() + ": " + e.getMessage());
		}
	}

	/** Called when a player logs in – loads their saved config. */
	private void loadConfig(final AutoFarmConfig cfg)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		     PreparedStatement ps = con.prepareStatement(
		         "SELECT config FROM character_autofarm WHERE char_id=?"))
		{
			ps.setInt(1, cfg.getOwnerId());
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
					cfg.deserialize(rs.getString("config"));
			}
		}
		catch (Exception e)
		{
			_log.warning("[AutoFarm] Failed to load config for charId=" + cfg.getOwnerId() + ": " + e.getMessage());
		}
	}

	/** Pre-loads all saved configs at server startup. */
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		     PreparedStatement ps = con.prepareStatement("SELECT char_id, config FROM character_autofarm");
		     ResultSet rs = ps.executeQuery())
		{
			int count = 0;
			while (rs.next())
			{
				final int charId = rs.getInt("char_id");
				final AutoFarmConfig cfg = new AutoFarmConfig(charId);
				cfg.deserialize(rs.getString("config"));
				_configs.put(charId, cfg);
				count++;
			}
			_log.info("[AutoFarm] Loaded " + count + " saved configs.");
		}
		catch (Exception e)
		{
			_log.warning("[AutoFarm] Failed to load configs at startup: " + e.getMessage());
		}
	}

	/** Called when a player logs out – stops their config and saves. */
	public void onLogout(final L2PcInstance player)
	{
		final AutoFarmConfig cfg = _configs.get(player.getObjectId());
		if (cfg != null)
		{
			cfg.setRunning(false);
			saveConfig(cfg);
		}
	}

	// -----------------------------------------------------------------------
	// HTML rendering (main panel)
	// -----------------------------------------------------------------------

	/** Sends the main AutoFarm panel to the player. */
	public void renderMain(final L2PcInstance player)
	{
		final AutoFarmConfig cfg = getOrCreate(player);

		final fidas.server.gameserver.cache.HtmCache htmCache = fidas.server.gameserver.cache.HtmCache.getInstance();
		String html = htmCache.getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm.htm");
		if (html == null) { player.sendMessage("[AutoFarm] HTML not found."); return; }

		html = html.replace("%state%",  cfg.buildState());
		html = html.replace("%attack%", cfg.buildAutoAttack());
		html = html.replace("%move%",   cfg.buildMoveType());
		html = html.replace("%party%",  cfg.buildParty());
		html = html.replace("%search%", cfg.buildSearch());
		html = html.replace("%ask%",    cfg.buildActions());
		html = html.replace("%id%",     String.valueOf(player.getObjectId()));
		html = html.replace("%name%",   player.getName());

		sendHtml(player, html);
	}

	/** Sends the skill-edit panel for a specific slot. */
	public void renderActionEdit(final L2PcInstance player, final int slot, final int page)
	{
		final AutoFarmConfig cfg = getOrCreate(player);

		final int SKILLS_PER_PAGE = 9;
		final fidas.server.gameserver.cache.HtmCache htmCache = fidas.server.gameserver.cache.HtmCache.getInstance();
		String html = htmCache.getHtm(player.getHtmlPrefix(), "data/html/mods/autofarm/autofarm_skill.htm");
		if (html == null) { player.sendMessage("[AutoFarm] Skill HTML not found."); return; }

		html = html.replace("%tit%",      "AutoFarm Skill Slot " + (slot + 1));
		html = html.replace("%priority%", String.valueOf(slot + 1));
		html = html.replace("%slot%",     String.valueOf(slot));
		html = html.replace("%id%",       String.valueOf(player.getObjectId()));

		// Populate available skills (active, non-toggle)
		final List<fidas.server.gameserver.model.skills.L2Skill> availSkills = new ArrayList<>();
		for (final fidas.server.gameserver.model.skills.L2Skill sk : player.getAllSkills())
		{
			if (sk.isActive() && !sk.isToggle())
				availSkills.add(sk);
		}
		final int total = availSkills.size();
		final String slotFmt = "<td align=center width=50>"
			+ "<table height=34 cellspacing=0 cellpadding=0 background=icon.skill%d>"
			+ "<tr><td><table cellspacing=0 cellpadding=0><tr><td>"
			+ "<button action=\"bypass autofarm_action_set %d %d %d\" width=34 height=34 "
			+ "back=L2UI_CH3.menu_outline_Down fore=L2UI_CH3.menu_outline>"
			+ "</td></tr></table></td></tr></table></td>";
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < SKILLS_PER_PAGE; i++)
		{
			final int idx = SKILLS_PER_PAGE * page + i;
			if (idx < total)
			{
				final fidas.server.gameserver.model.skills.L2Skill sk = availSkills.get(idx);
				sb.append(String.format(slotFmt, sk.getId(), player.getObjectId(), slot, sk.getId()));
			}
		}
		html = html.replace("%ask%", sb.toString());

		// Page links
		final int pages = total < SKILLS_PER_PAGE ? 1 : total / SKILLS_PER_PAGE + ((total % SKILLS_PER_PAGE) > 0 ? 1 : 0);
		sb.setLength(0);
		for (int i = 0; i < pages; i++)
		{
			if (page == i)
				sb.append(String.format("<td align=center>Page %d</td>", i + 1));
			else
				sb.append(String.format("<td align=center><a action=\"bypass autofarm_action_edit %d %d %d\">Page %d</a></td>",
				                       player.getObjectId(), slot, i, i + 1));
		}
		html = html.replace("%pages1%", sb.toString());

		// Current slot info
		final AutoFarmAction action = cfg.getAction(slot);
		if (action != null)
		{
			html = html.replace("%sic%", action.getIcon());
			final fidas.server.gameserver.model.skills.L2Skill sk = action.getSkill(player);
			html = html.replace("%sna%", sk != null ? sk.getName() : "ID " + action.getSkillId());
			html = html.replace("%reu%", String.format("%.2fs", action.getReuseSeconds()));
			html = html.replace("%hpp%", String.format("%05.1f%%", action.getUserHpPct()));
			html = html.replace("%tpp%", String.format("%05.1f%%", action.getTargetHpPct()));

			final EActionPriority[] pris = EActionPriority.values();
			final EActionPriority   cur  = pris[Math.min(slot, pris.length - 1)];
			final StringBuilder prs = new StringBuilder(cur.toString());
			for (final EActionPriority p : pris)
				if (p != cur) prs.append(';').append(p);
			html = html.replace("%pr%", prs.toString());
		}
		else
		{
			html = html.replace("%sic%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
			html = html.replace("%sna%", "Empty");
			html = html.replace("%reu%", "0.00s");
			html = html.replace("%hpp%", "100.0%");
			html = html.replace("%tpp%", "100.0%");
			html = html.replace("%pr%",  "");
		}

		sendHtml(player, html);
	}

	// -----------------------------------------------------------------------
	// Utility
	// -----------------------------------------------------------------------

	private static void sendHtml(final L2PcInstance player, final String html)
	{
		final fidas.server.gameserver.network.serverpackets.NpcHtmlMessage msg =
			new fidas.server.gameserver.network.serverpackets.NpcHtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
	}
}
