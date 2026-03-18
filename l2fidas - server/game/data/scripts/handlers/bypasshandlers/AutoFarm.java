package handlers.bypasshandlers;

import java.util.StringTokenizer;

import fidas.server.gameserver.autofarm.AutoFarmAction;
import fidas.server.gameserver.autofarm.AutoFarmConfig;
import fidas.server.gameserver.autofarm.EActionPriority;
import fidas.server.gameserver.autofarm.EAutoAttack;
import fidas.server.gameserver.autofarm.EMoveType;
import fidas.server.gameserver.autofarm.ESearchType;
import fidas.server.gameserver.handler.IBypassHandler;
import fidas.server.gameserver.model.L2Party;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.util.AutoFarmManager;

/**
 * AutoFarm bypass handler – processes all "autofarm_*" commands from the UI.
 *
 * <h3>Command reference</h3>
 * <pre>
 *   autofarm_start     &lt;viewId&gt;
 *   autofarm_stop      &lt;viewId&gt;
 *   autofarm_reset     &lt;viewId&gt;
 *   autofarm_refresh   &lt;viewId&gt;
 *   autofarm_main      &lt;viewId&gt;
 *   autofarm_attack_type &lt;viewId&gt; &lt;EAutoAttack.name&gt;
 *   autofarm_move_type   &lt;viewId&gt; &lt;EMoveType.name&gt;
 *   autofarm_search_type &lt;viewId&gt; &lt;ESearchType.name&gt;
 *   autofarm_party_target &lt;viewId&gt; &lt;playerName | "Not Set"&gt;
 *   autofarm_save_location &lt;viewId&gt;
 *   autofarm_action_edit   &lt;viewId&gt; &lt;slot&gt; [&lt;page&gt;]
 *   autofarm_action_set    &lt;viewId&gt; &lt;slot&gt; &lt;skillId&gt;
 *   autofarm_reuse_set     &lt;viewId&gt; &lt;slot&gt; &lt;reuseSec&gt;
 *   autofarm_hpp_set       &lt;viewId&gt; &lt;slot&gt; &lt;hp%&gt;
 *   autofarm_tpp_set       &lt;viewId&gt; &lt;slot&gt; &lt;target_hp%&gt;
 *   autofarm_slot_set      &lt;viewId&gt; &lt;slot&gt; &lt;priority_ordinal&gt;
 * </pre>
 */
public class AutoFarm implements IBypassHandler
{
	private static final String[] BYPASSES = {
		"autofarm_start",
		"autofarm_stop",
		"autofarm_reset",
		"autofarm_refresh",
		"autofarm_main",
		"autofarm_attack_type",
		"autofarm_move_type",
		"autofarm_search_type",
		"autofarm_party_target",
		"autofarm_save_location",
		"autofarm_action_edit",
		"autofarm_action_set",
		"autofarm_reuse_set",
		"autofarm_hpp_set",
		"autofarm_tpp_set",
		"autofarm_slot_set",
	};

	@Override
	public boolean useBypass(final String command, final L2PcInstance activeChar, final L2Character target)
	{
		if (activeChar == null || command == null) return false;

		final StringTokenizer st = new StringTokenizer(command);
		final String cmd = st.nextToken();

		// viewId: which player's config to show (GM can view others)
		int viewId = activeChar.getObjectId();
		if (st.hasMoreTokens())
		{
			try { viewId = Integer.parseInt(st.nextToken()); }
			catch (NumberFormatException e) { viewId = activeChar.getObjectId(); }
		}

		final AutoFarmConfig cfg = getConfig(activeChar, viewId);
		if (cfg == null) return true;

		switch (cmd)
		{
			// ----------------------------------------------------------------
			// Start / Stop / Reset / Refresh
			// ----------------------------------------------------------------
			case "autofarm_start":
				cfg.setRunning(true);
				AutoFarmManager.getInstance().renderMain(activeChar);
				break;

			case "autofarm_stop":
				cfg.setRunning(false);
				AutoFarmManager.getInstance().saveConfig(cfg);
				AutoFarmManager.getInstance().renderMain(activeChar);
				break;

			case "autofarm_reset":
				cfg.reset();
				AutoFarmManager.getInstance().saveConfig(cfg);
				AutoFarmManager.getInstance().renderMain(activeChar);
				break;

			case "autofarm_refresh":
			case "autofarm_main":
				AutoFarmManager.getInstance().renderMain(activeChar);
				break;

			// ----------------------------------------------------------------
			// Auto-attack type
			// ----------------------------------------------------------------
			case "autofarm_attack_type":
				if (st.hasMoreTokens())
				{
					try
					{
						String name = st.nextToken();
						while (st.hasMoreTokens()) name += "_" + st.nextToken();
						cfg.setAutoAttack(Enum.valueOf(EAutoAttack.class, name));
						AutoFarmManager.getInstance().saveConfig(cfg);
						activeChar.sendMessage("[AutoFarm] Auto Attack → " + cfg.getAutoAttack());
					}
					catch (IllegalArgumentException e)
					{
						activeChar.sendMessage("[AutoFarm] Unknown attack type.");
					}
				}
				AutoFarmManager.getInstance().renderMain(activeChar);
				break;

			// ----------------------------------------------------------------
			// Movement type
			// ----------------------------------------------------------------
			case "autofarm_move_type":
				if (st.hasMoreTokens())
				{
					try
					{
						String name = st.nextToken();
						while (st.hasMoreTokens()) name += "_" + st.nextToken();
						// "Follow <PlayerName>" is stored as Follow_Target internally
						if (name.startsWith("Follow") && !name.equals("Follow_Target"))
							name = "Follow_Target";
						final EMoveType mt = Enum.valueOf(EMoveType.class, name);
						cfg.setMoveType(mt, activeChar);
						AutoFarmManager.getInstance().saveConfig(cfg);
						activeChar.sendMessage("[AutoFarm] Movement → " + cfg.getMoveType());
					}
					catch (IllegalArgumentException e)
					{
						activeChar.sendMessage("[AutoFarm] Unknown move type.");
					}
				}
				AutoFarmManager.getInstance().renderMain(activeChar);
				break;

			// ----------------------------------------------------------------
			// Search type (targeting range)
			// ----------------------------------------------------------------
			case "autofarm_search_type":
				if (st.hasMoreTokens())
				{
					try
					{
						final ESearchType st2 = Enum.valueOf(ESearchType.class, st.nextToken());
						cfg.setSearchType(st2);
						AutoFarmManager.getInstance().saveConfig(cfg);
						activeChar.sendMessage("[AutoFarm] Search → " + cfg.getSearchType());
					}
					catch (IllegalArgumentException e)
					{
						activeChar.sendMessage("[AutoFarm] Unknown search type.");
					}
				}
				AutoFarmManager.getInstance().renderMain(activeChar);
				break;

			// ----------------------------------------------------------------
			// Party target (assist)
			// ----------------------------------------------------------------
			case "autofarm_party_target":
				if (st.hasMoreTokens())
				{
					String name = st.nextToken();
					while (st.hasMoreTokens()) name += " " + st.nextToken();

					if (name.equalsIgnoreCase("Not Set") || name.equalsIgnoreCase("Not_Set"))
					{
						cfg.setAssistId(0, activeChar);
						activeChar.sendMessage("[AutoFarm] Party Target cleared.");
					}
					else
					{
						final L2PcInstance target2 = L2World.getInstance().getPlayer(name);
						if (target2 != null && target2 != activeChar)
						{
							final L2Party party = activeChar.getParty();
							if (party != null && party.getPartyMembers().contains(target2))
							{
								cfg.setAssistId(target2.getObjectId(), activeChar);
								activeChar.sendMessage("[AutoFarm] Party Target → " + target2.getName());
							}
							else
								activeChar.sendMessage("[AutoFarm] That player is not in your party.");
						}
						else
							activeChar.sendMessage("[AutoFarm] Player not found: " + name);
					}
					AutoFarmManager.getInstance().saveConfig(cfg);
				}
				AutoFarmManager.getInstance().renderMain(activeChar);
				break;

			// ----------------------------------------------------------------
			// Save current location
			// ----------------------------------------------------------------
			case "autofarm_save_location":
				cfg.setMoveType(EMoveType.Current_Location, activeChar);
				AutoFarmManager.getInstance().saveConfig(cfg);
				AutoFarmManager.getInstance().renderMain(activeChar);
				break;

			// ----------------------------------------------------------------
			// Open skill edit panel for a slot
			// ----------------------------------------------------------------
			case "autofarm_action_edit":
			{
				int slot = 0, page = 0;
				if (st.hasMoreTokens())
					try { slot = Integer.parseInt(st.nextToken()); } catch (Exception e) {}
				if (st.hasMoreTokens())
					try { page = Integer.parseInt(st.nextToken()); } catch (Exception e) {}
				AutoFarmManager.getInstance().renderActionEdit(activeChar, slot, page);
				break;
			}

			// ----------------------------------------------------------------
			// Assign a skill to a slot
			// ----------------------------------------------------------------
			case "autofarm_action_set":
			{
				int slot = 0, skillId = 0;
				if (st.hasMoreTokens()) try { slot    = Integer.parseInt(st.nextToken()); } catch (Exception e) {}
				if (st.hasMoreTokens()) try { skillId = Integer.parseInt(st.nextToken()); } catch (Exception e) {}

				if (skillId > 0 && activeChar.getSkillLevel(skillId) >= 1)
				{
					cfg.setAction(slot, skillId);
					AutoFarmManager.getInstance().saveConfig(cfg);
					activeChar.sendMessage("[AutoFarm] Skill " + skillId + " assigned to slot " + (slot + 1));
				}
				AutoFarmManager.getInstance().renderActionEdit(activeChar, slot, 0);
				break;
			}

			// ----------------------------------------------------------------
			// Set custom reuse for a slot
			// ----------------------------------------------------------------
			case "autofarm_reuse_set":
			{
				int slot = 0; double reuse = 0;
				if (st.hasMoreTokens()) try { slot  = Integer.parseInt(st.nextToken()); } catch (Exception e) {}
				if (st.hasMoreTokens()) try { reuse = Double.parseDouble(st.nextToken()); } catch (Exception e) {}

				final AutoFarmAction action = cfg.getAction(slot);
				if (action != null)
				{
					action.setReuseSeconds(reuse);
					AutoFarmManager.getInstance().saveConfig(cfg);
					activeChar.sendMessage("[AutoFarm] Reuse for slot " + (slot+1) + " → " + reuse + "s");
				}
				AutoFarmManager.getInstance().renderActionEdit(activeChar, slot, 0);
				break;
			}

			// ----------------------------------------------------------------
			// Set user HP% threshold for a slot
			// ----------------------------------------------------------------
			case "autofarm_hpp_set":
			{
				int slot = 0; double hpp = 100;
				if (st.hasMoreTokens()) try { slot = Integer.parseInt(st.nextToken()); } catch (Exception e) {}
				if (st.hasMoreTokens()) try { hpp  = Double.parseDouble(st.nextToken()); } catch (Exception e) {}

				final AutoFarmAction action = cfg.getAction(slot);
				if (action != null)
				{
					action.setUserHpPct(hpp);
					AutoFarmManager.getInstance().saveConfig(cfg);
					activeChar.sendMessage("[AutoFarm] User HP threshold for slot " + (slot+1) + " → " + hpp + "%");
				}
				AutoFarmManager.getInstance().renderActionEdit(activeChar, slot, 0);
				break;
			}

			// ----------------------------------------------------------------
			// Set target HP% threshold for a slot
			// ----------------------------------------------------------------
			case "autofarm_tpp_set":
			{
				int slot = 0; double tpp = 100;
				if (st.hasMoreTokens()) try { slot = Integer.parseInt(st.nextToken()); } catch (Exception e) {}
				if (st.hasMoreTokens()) try { tpp  = Double.parseDouble(st.nextToken()); } catch (Exception e) {}

				final AutoFarmAction action = cfg.getAction(slot);
				if (action != null)
				{
					action.setTargetHpPct(tpp);
					AutoFarmManager.getInstance().saveConfig(cfg);
					activeChar.sendMessage("[AutoFarm] Target HP threshold for slot " + (slot+1) + " → " + tpp + "%");
				}
				AutoFarmManager.getInstance().renderActionEdit(activeChar, slot, 0);
				break;
			}

			// ----------------------------------------------------------------
			// Reorder / remove a slot
			// ----------------------------------------------------------------
			case "autofarm_slot_set":
			{
				int slot = 0, priorityOrd = 0;
				if (st.hasMoreTokens()) try { slot        = Integer.parseInt(st.nextToken()); } catch (Exception e) {}
				if (st.hasMoreTokens()) try { priorityOrd = Integer.parseInt(st.nextToken()) - 1; } catch (Exception e) {}

				final EActionPriority[] priorities = EActionPriority.values();
				if (priorityOrd >= 0 && priorityOrd < priorities.length)
				{
					final EActionPriority newPriority = priorities[priorityOrd];
					if (newPriority == EActionPriority.Remove)
					{
						cfg.deleteAction(slot);
						AutoFarmManager.getInstance().saveConfig(cfg);
						AutoFarmManager.getInstance().renderMain(activeChar);
					}
					else
					{
						if (cfg.swapActions(slot, priorityOrd))
						{
							AutoFarmManager.getInstance().saveConfig(cfg);
							AutoFarmManager.getInstance().renderActionEdit(activeChar, priorityOrd, 0);
						}
					}
				}
				break;
			}
		}
		return true;
	}

	// -----------------------------------------------------------------------
	// Helper: resolve config for the given viewId
	// -----------------------------------------------------------------------
	private AutoFarmConfig getConfig(final L2PcInstance viewer, final int viewId)
	{
		if (viewId == viewer.getObjectId())
			return AutoFarmManager.getInstance().getOrCreate(viewer);

		// GMs can view/edit other players' configs
		if (viewer.isGM())
		{
			final L2PcInstance target = L2World.getInstance().getPlayer(viewId);
			if (target != null)
				return AutoFarmManager.getInstance().getOrCreate(target);
		}
		return AutoFarmManager.getInstance().getOrCreate(viewer);
	}

	@Override
	public String[] getBypassList()
	{
		return BYPASSES;
	}
}
