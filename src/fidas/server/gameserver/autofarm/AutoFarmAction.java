package fidas.server.gameserver.autofarm;

import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.skills.L2Skill;

/**
 * Represents one skill slot in the AutoFarm configuration.
 * Each slot stores the skill to use and the conditions under which to use it:
 *   – userHpPct  : use this skill only when MY HP% is ≤ this value  (default 100 = always)
 *   – targetHpPct: use this skill only when TARGET HP% is ≤ this value (default 100 = always)
 *   – reuseMs    : extra custom cooldown in milliseconds (0 = use skill's own reuse only)
 */
public class AutoFarmAction
{
	private final int    _skillId;
	private double       _userHpPct   = 100.0;   // 0–100 %
	private double       _targetHpPct = 100.0;   // 0–100 %
	private long         _reuseMs     = 0L;       // extra reuse in ms
	private long         _lastUse     = 0L;       // System.currentTimeMillis() at last cast

	public AutoFarmAction(final int skillId)
	{
		_skillId = skillId;
	}

	// -----------------------------------------------------------------------
	// Getters / setters
	// -----------------------------------------------------------------------

	public int getSkillId() { return _skillId; }

	public double getUserHpPct()  { return _userHpPct; }
	public double getTargetHpPct(){ return _targetHpPct; }

	public void setUserHpPct(final double v)   { _userHpPct   = Math.min(100.0, Math.max(0.0, v)); }
	public void setTargetHpPct(final double v) { _targetHpPct = Math.min(100.0, Math.max(0.0, v)); }

	public double getReuseSeconds() { return _reuseMs / 1000.0; }
	public void setReuseSeconds(final double seconds)
	{
		_reuseMs = Math.min((long)(seconds * 1000L), 300_000L); // max 5 min
	}

	// -----------------------------------------------------------------------
	// Custom reuse tracking
	// -----------------------------------------------------------------------

	/** @return true if the custom reuse timer has elapsed (or reuseMs == 0). */
	public boolean isCustomReuseReady()
	{
		return _reuseMs <= 0 || System.currentTimeMillis() >= _lastUse + _reuseMs;
	}

	public void initReuse()
	{
		_lastUse = System.currentTimeMillis();
	}

	// -----------------------------------------------------------------------
	// HP condition checks
	// -----------------------------------------------------------------------

	public boolean isUserHpOk(final L2PcInstance player)
	{
		final double pct = player.getCurrentHp() / player.getMaxHp() * 100.0;
		return pct <= _userHpPct;
	}

	public boolean isTargetHpOk(final L2Character target)
	{
		if (target == null) return true; // no target restriction
		final double pct = target.getCurrentHp() / target.getMaxHp() * 100.0;
		return pct <= _targetHpPct;
	}

	// -----------------------------------------------------------------------
	// Full readiness check (all conditions)
	// -----------------------------------------------------------------------

	/**
	 * @return true if all conditions pass:
	 *   1. custom reuse elapsed
	 *   2. player HP ≤ userHpPct
	 *   3. target HP ≤ targetHpPct
	 *   4. player has the skill (level ≥ 1)
	 *   5. skill is not disabled (server-side reuse / cast check)
	 */
	public boolean isReadyToUse(final L2PcInstance player, final L2Character target)
	{
		if (!isCustomReuseReady())        return false;
		if (!isUserHpOk(player))          return false;
		if (!isTargetHpOk(target))        return false;

		final int level = player.getSkillLevel(_skillId);
		if (level < 1) return false;

		final L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);
		if (skill == null)                return false;
		if (player.isSkillDisabled(skill)) return false;

		return true;
	}

	/**
	 * Returns the L2Skill for this action at the player's current level.
	 * Returns null if the player does not know the skill.
	 */
	public L2Skill getSkill(final L2PcInstance player)
	{
		final int level = player.getSkillLevel(_skillId);
		if (level < 1) return null;
		return SkillTable.getInstance().getInfo(_skillId, level);
	}

	/**
	 * Returns the icon path for display in the HTML.
	 * Uses the skill ID–based naming convention: "icon.skill{id}"
	 */
	public String getIcon()
	{
		return "icon.skill" + _skillId;
	}

	// -----------------------------------------------------------------------
	// Serialization helpers (for DB storage)
	// -----------------------------------------------------------------------

	/** Serializes as "skillId:userHp:targetHp:reuseMs" */
	public String serialize()
	{
		return _skillId + ":" + _userHpPct + ":" + _targetHpPct + ":" + _reuseMs;
	}

	/** Deserializes from "skillId:userHp:targetHp:reuseMs". Returns null on error. */
	public static AutoFarmAction deserialize(final String s)
	{
		if (s == null || s.isEmpty()) return null;
		try
		{
			final String[] p = s.split(":");
			final int skillId = Integer.parseInt(p[0]);
			if (skillId <= 0) return null;
			final AutoFarmAction a = new AutoFarmAction(skillId);
			if (p.length >= 2) a.setUserHpPct(Double.parseDouble(p[1]));
			if (p.length >= 3) a.setTargetHpPct(Double.parseDouble(p[2]));
			if (p.length >= 4) a.setReuseSeconds(Long.parseLong(p[3]) / 1000.0);
			return a;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
