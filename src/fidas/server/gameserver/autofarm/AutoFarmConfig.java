package fidas.server.gameserver.autofarm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.model.L2Party;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.L2Attackable;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.skills.L2Skill;

/**
 * Per-player AutoFarm configuration and main tick logic.
 *
 * <h3>Search types</h3>
 * <ul>
 *   <li><b>Off</b> – auto-targeting disabled</li>
 *   <li><b>Assist</b> – mirrors the assist player's target</li>
 *   <li><b>Close / Near / Far</b> – range-based search from the player or saved location</li>
 * </ul>
 *
 * <h3>Auto-attack modes</h3>
 * <ul>
 *   <li><b>Never</b> – never basic-attacks</li>
 *   <li><b>Always</b> – always basic-attacks the target</li>
 *   <li><b>Skills_Reuse</b> – basic-attacks only when all configured skills are on cooldown</li>
 * </ul>
 *
 * <h3>Movement modes</h3>
 * <ul>
 *   <li><b>Not_Set</b> – stays wherever the player is</li>
 *   <li><b>Follow_Target</b> – follows the assist player</li>
 *   <li><b>Saved_Location</b> – always searches from a previously saved (X,Y,Z)</li>
 * </ul>
 *
 * <h3>Skill slots (7 total)</h3>
 * Each slot is an {@link AutoFarmAction} with:
 *   skillId, userHpPct, targetHpPct, and a custom extra reuse in ms.
 */
public class AutoFarmConfig
{
	/** Total number of configurable skill slots. */
	public static final int SLOTS = 7;

	// -----------------------------------------------------------------------
	// State
	// -----------------------------------------------------------------------

	private final int          _ownerId;
	private boolean            _running      = false;
	private EAutoAttack        _autoAttack   = EAutoAttack.Never;
	private EMoveType          _moveType     = EMoveType.Not_Set;
	private ESearchType        _searchType   = ESearchType.Off;
	private int                _assistId     = 0;   // charId of party member to assist
	private int                _savedX, _savedY, _savedZ;
	private final AutoFarmAction[] _actions  = new AutoFarmAction[SLOTS];

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	public AutoFarmConfig(final int ownerId)
	{
		_ownerId = ownerId;
	}

	// -----------------------------------------------------------------------
	// Main tick – called every ~800ms by AutoFarmManager
	// -----------------------------------------------------------------------

	public void tick()
	{
		if (!_running) return;

		final L2PcInstance player = getPlayer();
		if (player == null || player.isDead() || player.isAlikeDead()) return;

		// If target is an already-dead character, clear it
		final L2Character oldTarget = (L2Character) player.getTarget();
		if (oldTarget != null && oldTarget.isAlikeDead())
		{
			// Leave the target; handle below
		}

		// ----------------------------------------------------------------
		// Assist follow / validate party membership
		// ----------------------------------------------------------------
		final L2PcInstance assistPlayer = getAssistPlayer();
		if (assistPlayer != null && !isSameParty(player, assistPlayer))
			_assistId = 0; // lost party – clear assist

		// Follow the assist player if set and movement allows
		if (assistPlayer != null && _moveType == EMoveType.Follow_Target && !player.isMoving())
		{
			if (!player.isInsideRadius(assistPlayer, 500, false, false))
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, assistPlayer);
		}

		// ----------------------------------------------------------------
		// Target search
		// ----------------------------------------------------------------
		if (_searchType != ESearchType.Off)
		{
			final L2Character currTarget = (L2Character) player.getTarget();

			if (currTarget != null && currTarget.isAlikeDead())
			{
				// Dead target – find a new one
				final L2Character newTarget = searchTarget(player);
				if (newTarget != null)
				{
					player.setTarget(newTarget);
					newTarget.onAction(player);
					return;
				}
			}
			else if (currTarget == null || currTarget == player || _searchType == ESearchType.Assist)
			{
				final L2Character newTarget = searchTarget(player);
				if (newTarget != null && newTarget != currTarget)
				{
					player.setTarget(newTarget);
					newTarget.onAction(player);
					return;
				}
			}
		}

		// ----------------------------------------------------------------
		// Use skills or basic-attack on the current target
		// ----------------------------------------------------------------
		final L2Character target = (L2Character) player.getTarget();
		if (target == null || target.isAlikeDead()) return;

		if (target.isAutoAttackable(player) && shouldForceAutoAttack(player))
		{
			doAutoAttack(player, target);
			return;
		}

		// Try skill slots in order
		for (final AutoFarmAction action : _actions)
		{
			if (action == null) continue;
			if (!action.isReadyToUse(player, target)) continue;

			final L2Skill skill = action.getSkill(player);
			if (skill == null) continue;

			// Offensive skills need an attackable target
			if (skill.isOffensive() && !target.isAutoAttackable(player))
				continue;

			if (player.useMagic(skill, false, false))
			{
				action.initReuse();
				return;
			}
		}

		// Fallback: auto-attack when Skills_Reuse mode is set
		if (_autoAttack == EAutoAttack.Skills_Reuse)
			doAutoAttack(player, target);
	}

	// -----------------------------------------------------------------------
	// Target search helpers
	// -----------------------------------------------------------------------

	private L2Character searchTarget(final L2PcInstance player)
	{
		if (_searchType == ESearchType.Assist)
		{
			final L2PcInstance assist = getAssistPlayer();
			return (assist != null) ? (L2Character) assist.getTarget() : null;
		}

		// Range-based: find the closest attackable mob within range
		final int range = _searchType.getRange();
		final int searchX = (_moveType == EMoveType.Saved_Location) ? _savedX : player.getX();
		final int searchY = (_moveType == EMoveType.Saved_Location) ? _savedY : player.getY();
		final int searchZ = (_moveType == EMoveType.Saved_Location) ? _savedZ : player.getZ();

		L2Character best = null;
		double bestDist   = Double.MAX_VALUE;

		final Collection<L2Character> known = player.getKnownList().getKnownCharacters();
		for (final L2Character obj : known)
		{
			if (!(obj instanceof L2Attackable)) continue;
			if (obj.isAlikeDead() || obj.isDead()) continue;
			if (!obj.isAutoAttackable(player)) continue;

			// Range check against search origin
			final double dx = obj.getX() - searchX;
			final double dy = obj.getY() - searchY;
			final double dz = obj.getZ() - searchZ;
			final double dist = dx * dx + dy * dy + dz * dz;
			final double rangeSq = (double) range * range;
			if (range > 0 && dist > rangeSq) continue;

			if (dist < bestDist)
			{
				bestDist = dist;
				best = obj;
			}
		}
		return best;
	}

	// -----------------------------------------------------------------------
	// Auto-attack helper
	// -----------------------------------------------------------------------

	private boolean shouldForceAutoAttack(final L2PcInstance player)
	{
		if (_autoAttack == EAutoAttack.Always) return true;
		if (_autoAttack == EAutoAttack.Skills_Reuse)
		{
			// Force auto-attack only if ALL configured skills are on cooldown
			for (final AutoFarmAction a : _actions)
			{
				if (a != null && a.isCustomReuseReady()) return false;
			}
			return true;
		}
		return false;
	}

	private void doAutoAttack(final L2PcInstance player, final L2Character target)
	{
		if (target instanceof L2Attackable && target.isAutoAttackable(player))
		{
			if (!player.isAttackingNow())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
	}

	// -----------------------------------------------------------------------
	// Actions (skill slots)
	// -----------------------------------------------------------------------

	public AutoFarmAction getAction(final int slot)
	{
		if (slot < 0 || slot >= SLOTS) return null;
		return _actions[slot];
	}

	public AutoFarmAction setAction(final int slot, final int skillId)
	{
		if (slot < 0 || slot >= SLOTS) return null;
		return _actions[slot] = new AutoFarmAction(skillId);
	}

	public boolean swapActions(final int slot0, final int slot1)
	{
		if (slot0 < 0 || slot0 >= SLOTS || slot1 < 0 || slot1 >= SLOTS) return false;
		final AutoFarmAction tmp = _actions[slot0];
		_actions[slot0] = _actions[slot1];
		_actions[slot1] = tmp;
		return true;
	}

	public void deleteAction(final int slot)
	{
		if (slot >= 0 && slot < SLOTS) _actions[slot] = null;
	}

	// -----------------------------------------------------------------------
	// Getters / setters
	// -----------------------------------------------------------------------

	public boolean isRunning() { return _running; }
	public void setRunning(final boolean v) { _running = v; }

	public EAutoAttack getAutoAttack() { return _autoAttack; }
	public void setAutoAttack(final EAutoAttack v) { _autoAttack = v; }

	public EMoveType getMoveType() { return _moveType; }
	public void setMoveType(final EMoveType v, final L2PcInstance player)
	{
		if (v == EMoveType.Current_Location)
		{
			// Snapshot current position
			_savedX = player.getX();
			_savedY = player.getY();
			_savedZ = player.getZ();
			_moveType = EMoveType.Saved_Location;
			player.sendMessage("[AutoFarm] Posición guardada: " + _savedX + ", " + _savedY + ", " + _savedZ);
		}
		else
		{
			_moveType = v;
		}
	}

	public ESearchType getSearchType() { return _searchType; }
	public void setSearchType(final ESearchType v) { _searchType = v; }

	public int getAssistId() { return _assistId; }
	public void setAssistId(final int id, final L2PcInstance player)
	{
		if (id == player.getObjectId()) { _assistId = 0; return; }
		_assistId = id;
	}

	public int getSavedX() { return _savedX; }
	public int getSavedY() { return _savedY; }
	public int getSavedZ() { return _savedZ; }

	public int getOwnerId() { return _ownerId; }

	// -----------------------------------------------------------------------
	// Utilities
	// -----------------------------------------------------------------------

	public L2PcInstance getPlayer()
	{
		return L2World.getInstance().getPlayer(_ownerId);
	}

	public L2PcInstance getAssistPlayer()
	{
		if (_assistId == 0) return null;
		return L2World.getInstance().getPlayer(_assistId);
	}

	private boolean isSameParty(final L2PcInstance a, final L2PcInstance b)
	{
		final L2Party pa = a.getParty();
		return pa != null && pa == b.getParty();
	}

	public void reset()
	{
		_running    = false;
		_autoAttack = EAutoAttack.Never;
		_moveType   = EMoveType.Not_Set;
		_searchType = ESearchType.Off;
		_assistId   = 0;
		_savedX = _savedY = _savedZ = 0;
		for (int i = 0; i < SLOTS; i++) _actions[i] = null;
	}

	// -----------------------------------------------------------------------
	// HTML rendering helpers
	// -----------------------------------------------------------------------

	/** Builds the %state% replacement: Start or Stop button + status label. */
	public String buildState()
	{
		if (_running)
			return "<td align=center><font name=hs12 color=\"63FF63\">Running</font></td>"
				 + "<td align=center><button value=\"Stop\" action=\"bypass autofarm_stop "
				 + _ownerId + "\" width=70 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
		else
			return "<td align=center><button value=\"Start\" action=\"bypass autofarm_start "
				 + _ownerId + "\" width=70 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>"
				 + "<td align=center><font name=hs12 color=\"FF6363\">Stopped</font></td>";
	}

	/** Builds a combobox list string for EAutoAttack (current first, others after ';'). */
	public String buildAutoAttack()
	{
		final StringBuilder sb = new StringBuilder(_autoAttack.toString());
		for (final EAutoAttack e : EAutoAttack.values())
			if (e != _autoAttack) sb.append(';').append(e);
		return sb.toString();
	}

	/** Builds a combobox list string for EMoveType. */
	public String buildMoveType()
	{
		final L2PcInstance player = getPlayer();
		final StringBuilder sb = new StringBuilder(_moveType.toString());
		for (final EMoveType e : EMoveType.values())
		{
			if (e == _moveType) continue;
			if (e == EMoveType.Saved_Location && _savedX == 0 && _savedY == 0 && _savedZ == 0) continue;
			if (e == EMoveType.Follow_Target && _assistId == 0) continue;
			sb.append(';').append(e);
		}
		// Always offer Current_Location to capture position
		if (_moveType != EMoveType.Current_Location) sb.append(';').append(EMoveType.Current_Location);

		// Replace "Follow Target" with actual player name if set
		final L2PcInstance assist = getAssistPlayer();
		String s = sb.toString().replace("Follow Target", assist != null ? assist.getName() : "Follow Target");
		return s;
	}

	/** Builds a combobox list of party members (for assist selection). */
	public String buildParty()
	{
		final L2PcInstance player = getPlayer();
		if (player == null) return "Not Set";

		final L2Party party = player.getParty();
		if (party == null) return "Not Set";

		final L2PcInstance assist = getAssistPlayer();
		final StringBuilder sb = new StringBuilder(assist != null ? assist.getName() : "Not Set");
		if (assist != null) sb.append(";Not Set");

		for (final L2PcInstance member : party.getPartyMembers())
		{
			if (member == player) continue;
			if (assist != null && member == assist) continue;
			sb.append(';').append(member.getName());
		}
		return sb.toString();
	}

	/** Builds the %search% row of colored buttons. */
	public String buildSearch()
	{
		final StringBuilder sb = new StringBuilder();
		final String btnFmt  = "<td align=center width=50><button value=\"%s\" action=\"bypass autofarm_search_type %d %s\" width=62 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
		final String lblFmt  = "<td align=center width=50><font name=hs12 color=\"%s\">%s</font></td>";
		for (final ESearchType e : ESearchType.values())
		{
			if (e == _searchType)
				sb.append(String.format(lblFmt, e.getColor(), e.toString()));
			else
				sb.append(String.format(btnFmt, e.toString(), _ownerId, e.name()));
		}
		return sb.toString();
	}

	/** Builds the %ask% skill slots row (7 slot icons). */
	public String buildActions()
	{
		final String slotFmt = "<td align=center width=50>"
			+ "<table height=34 cellspacing=0 cellpadding=0 background=%s>"
			+ "<tr><td><table cellspacing=0 cellpadding=0><tr><td>"
			+ "<button action=\"bypass autofarm_action_edit %d %d\" width=34 height=34 back=L2UI_CH3.menu_outline_Down fore=L2UI_CH3.menu_outline>"
			+ "</td></tr></table></td></tr></table></td>";

		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < SLOTS; i++)
		{
			final AutoFarmAction a = _actions[i];
			final String icon = (a != null) ? a.getIcon() : "L2UI_CT1.Inventory_DF_CloakSlot_Disable";
			sb.append(String.format(slotFmt, icon, _ownerId, i));
		}
		return sb.toString();
	}

	// -----------------------------------------------------------------------
	// Serialization (for DB storage)
	// -----------------------------------------------------------------------

	public String serialize()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(_autoAttack.name()).append(';');
		sb.append(_moveType.name()).append(';');
		sb.append(_searchType.name()).append(';');
		sb.append(_assistId).append(';');
		sb.append(_savedX).append(';').append(_savedY).append(';').append(_savedZ).append(';');
		for (int i = 0; i < SLOTS; i++)
		{
			final AutoFarmAction a = _actions[i];
			sb.append(a != null ? a.serialize() : "0:100:100:0");
			if (i < SLOTS - 1) sb.append('|');
		}
		return sb.toString();
	}

	public void deserialize(final String data)
	{
		if (data == null || data.isEmpty()) return;
		try
		{
			final String[] parts = data.split(";");
			int idx = 0;
			_autoAttack = Enum.valueOf(EAutoAttack.class, parts[idx++]);
			_moveType   = Enum.valueOf(EMoveType.class,   parts[idx++]);
			_searchType = Enum.valueOf(ESearchType.class, parts[idx++]);
			_assistId   = Integer.parseInt(parts[idx++]);
			_savedX     = Integer.parseInt(parts[idx++]);
			_savedY     = Integer.parseInt(parts[idx++]);
			_savedZ     = Integer.parseInt(parts[idx++]);
			if (idx < parts.length)
			{
				final String[] slots = parts[idx].split("\\|");
				for (int i = 0; i < SLOTS && i < slots.length; i++)
					_actions[i] = AutoFarmAction.deserialize(slots[i]);
			}
		}
		catch (Exception e)
		{
			// Ignore corrupted config – defaults remain
		}
	}
}
