/*
 * Custom quest based on L2J style (QuestState, State, cond, main()).
 */
package quests.Q90001_DailyHunt;

import java.util.Calendar;

import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.quest.Quest;
import fidas.server.gameserver.model.quest.QuestState;
import fidas.server.gameserver.model.quest.State;

public class Q90001_DailyHunt extends Quest
{
	// ===== CONFIGURA ESTO =====
	private static final int QUEST_GIVER = 13104; // <-- cambia por el NPC que quieras (ID)
	private static final int MIN_LEVEL = 20;      // <-- nivel mínimo
	
	private static final int KILL_GOAL = 80;      // <-- nº de kills requeridas
	
	private static final int[] MOBS = new int[]
	{
		20120, 20121, 20122 // <-- cambia por mobs reales (IDs)
	};

	// Recompensa (por defecto: adena moderada para x5, ajusta a tu economía)
	private static final int REWARD_ITEM_ID = 57;      // Adena
	private static final int REWARD_ITEM_COUNT = 50000;

	// Vars de quest
	private static final String VAR_KILLS = "kills";
	private static final String VAR_DAYKEY = "dayKey"; // YYYY*1000 + DAY_OF_YEAR (reset diario)

	public Q90001_DailyHunt(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(QUEST_GIVER);
		addTalkId(QUEST_GIVER);

		for (int mobId : MOBS)
			addKillId(mobId);

		questItemIds = new int[]
		{
			// No items obligatorios en inventario para esta quest
		};
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		// Si ya completó hoy, bloquear
		if (isDoneToday(st))
			return npc.getNpcId() + "-5.htm"; // cooldown

		if (event.equalsIgnoreCase("accept"))
		{
			if (st.getInt("cond") == 0)
			{
				if (player.getLevel() >= MIN_LEVEL)
				{
					st.setState(State.STARTED);
					st.playSound("ItemSound.quest_accept");
					st.set("cond", "1");
					st.set(VAR_KILLS, "0");
					htmltext = npc.getNpcId() + "-2.htm"; // accepted
				}
				else
				{
					htmltext = npc.getNpcId() + "-6.htm"; // low level
					st.exitQuest(true);
				}
			}
			return htmltext;
		}
		else if (event.equalsIgnoreCase("reward"))
		{
			// Solo si está en cond 2 (objetivo cumplido)
			if (st.getInt("cond") == 2)
			{
				st.giveItems(REWARD_ITEM_ID, REWARD_ITEM_COUNT);
				st.playSound("ItemSound.quest_finish");

				// marcar completada hoy (reset diario)
				st.set(VAR_DAYKEY, String.valueOf(getTodayKey()));

				// exitQuest(true) = quest repetible / vuelve a CREATED
				st.exitQuest(true);
				htmltext = npc.getNpcId() + "-4.htm"; // reward ok
			}
			else
			{
				htmltext = npc.getNpcId() + "-3.htm"; // not yet
			}
			return htmltext;
		}

		return htmltext;
	}

	@Override
public String onTalk(L2Npc npc, L2PcInstance player)
{
    String htmltext = getNoQuestMsg(player);
    QuestState st = player.getQuestState(getName());

    if (st == null)
        return htmltext;

    // OJO: no resetees cond si ya está STARTED
    if (st.getState() == State.CREATED)
        st.set("cond", "0");

    int cond = st.getInt("cond");

    if (cond == 0)
        return npc.getNpcId() + "-1.htm";   // start (con botón Aceptar)
    else if (cond == 1)
        return npc.getNpcId() + "-3.htm";   // en progreso (sin botón aceptar)
    else if (cond == 2)
        return npc.getNpcId() + "-7.htm";   // listo para recompensa
    else
        return htmltext;
}


	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;

		if (isDoneToday(st))
			return null;

		int cond = st.getInt("cond");
		if (cond != 1)
			return null;

		int kills = st.getInt(VAR_KILLS);
		kills++;
		st.set(VAR_KILLS, String.valueOf(kills));

		// Si llega al objetivo, cambiar cond a 2
		if (kills >= KILL_GOAL)
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}

		return null;
	}

	private boolean isDoneToday(QuestState st)
	{
		final String val = st.get(VAR_DAYKEY);
		if (val == null)
			return false;

		try
		{
			return Integer.parseInt(val) == getTodayKey();
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}

	private int getTodayKey()
	{
		final Calendar c = Calendar.getInstance();
		return (c.get(Calendar.YEAR) * 1000) + c.get(Calendar.DAY_OF_YEAR);
	}

	public static void main(String[] args)
	{
		new Q90001_DailyHunt(90001, Q90001_DailyHunt.class.getSimpleName(), "Daily Hunt");
	}
}
