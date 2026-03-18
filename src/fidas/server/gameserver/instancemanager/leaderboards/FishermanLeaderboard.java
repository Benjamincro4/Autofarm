package fidas.server.gameserver.instancemanager.leaderboards;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javolution.util.FastMap;
import fidas.server.Config;
import fidas.server.gameserver.Announcements;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.SystemMessageId;
import fidas.server.gameserver.network.serverpackets.ItemList;
import fidas.server.gameserver.network.serverpackets.SystemMessage;
import fidas.server.util.Util;

public class FishermanLeaderboard
{
	private static FishermanLeaderboard	_instance;
	public Logger _log = Logger.getLogger(FishermanLeaderboard.class.getName());
	public Map<Integer, FishRank> _ranks = new FastMap<>();
	protected Future<?> _actionTask = null;
	protected int TASK_DELAY = Config.RANK_FISHERMAN_INTERVAL;
	protected Long nextTimeUpdateReward = 0L;

	public static FishermanLeaderboard getInstance()
	{
		if (_instance == null)
		{
			_instance = new FishermanLeaderboard();
		}

		return _instance;
	}
	
	public FishermanLeaderboard()
	{
		engineInit();
	}	

	public void onCatch(int owner, String name)
	{
		FishRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new FishRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}

		ar.cought();
		ar.name = name;
		_ranks.put(owner, ar);
	}

	public void onEscape(int owner, String name)
	{
		FishRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new FishRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}

		ar.escaped();
		ar.name = name;
		_ranks.put(owner, ar);
	}

	public void stopTask()
	{
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
		}

		_actionTask = null;
	}

	public class FishermanTask implements Runnable
	{
		@Override
		public void run()
		{
			_log.info("FishManager: Reiniciado.");
			formRank();
			nextTimeUpdateReward = System.currentTimeMillis() + TASK_DELAY * 60000;
		}
	}

	public void startTask()
	{
		if (_actionTask == null)
		{
			_actionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FishermanTask(), 1000, TASK_DELAY * 60000);
		}
	}

	public void formRank()
	{
		Map<Integer, Integer> scores = new FastMap<>();
		for (int obj : _ranks.keySet())
		{
			FishRank ar = _ranks.get(obj);
			scores.put(obj, ar.cought - ar.escaped);
		}

		int Top = -1;
		int idTop = 0;
		for (int id : scores.keySet())
		{
			if (scores.get(id) > Top)
			{
				idTop = id;
				Top = scores.get(id);
			}
		}
		
		FishRank arTop = _ranks.get(idTop);
		
		if (arTop == null)
		{
			Announcements.getInstance().announceToAll("Fisherman: Nao ha vencedores neste momento!");
			_ranks.clear();
			return;
		}

		L2PcInstance winner = (L2PcInstance) L2World.getInstance().findObject(idTop);

		Announcements.getInstance().announceToAll("Atencao pescadores: " + arTop.name + " foi o vencedor desta vez " + arTop.cought + "/" + arTop.escaped + ". Proximo calculo em " + Config.RANK_FISHERMAN_INTERVAL + " min(s).");
		if ((winner != null) && (Config.RANK_FISHERMAN_REWARD_ID > 0) && (Config.RANK_FISHERMAN_REWARD_COUNT > 0))
		{
			winner.getInventory().addItem("FishManager", Config.RANK_FISHERMAN_REWARD_ID, Config.RANK_FISHERMAN_REWARD_COUNT, winner, null);
			if (Config.RANK_FISHERMAN_REWARD_COUNT > 1) //You have earned $s1.
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(Config.RANK_FISHERMAN_REWARD_ID).addNumber( Config.RANK_FISHERMAN_REWARD_COUNT));
			}
			else
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(Config.RANK_FISHERMAN_REWARD_ID));
			}
			winner.sendPacket(new ItemList(winner, false));
		}
		_ranks.clear();
	}

	public String showHtm(int owner)
	{
		Map<Integer, Integer> scores = new FastMap<>();
		for (int obj : _ranks.keySet())
		{
			FishRank ar = _ranks.get(obj);
			scores.put(obj, ar.cought - ar.escaped);
		}

		scores = Util.sortMap(scores, false);

		int counter = 0, max = 20;
		String pt = "<html><body><center>" + "<font color=\"cc00ad\">TOP " + max + " pescadores</font><br>";

		pt += "<table width=260 border=0 cellspacing=0 cellpadding=0 bgcolor=333333>";
		pt += "<tr> <td align=center>No.</td> <td align=center>Nome</td> <td align=center>Pescado</td> <td align=center>Escapado</td> </tr>";
		pt += "<tr> <td align=center>&nbsp;</td> <td align=center>&nbsp;</td> <td align=center></td> <td align=center></td> </tr>";
		boolean inTop = false;
		for (int id : scores.keySet())
		{
			if (counter < max)
			{
				FishRank ar = _ranks.get(id);
				pt += tx(counter, ar.name, ar.cought, ar.escaped, id == owner);
				if (id == owner)
				{
					inTop = true;
				}

				counter++;
			}
			else
			{
				break;
			}
		}

		if (!inTop)
		{
			FishRank arMe = _ranks.get(owner);
			if (arMe != null)
			{
				pt += "<tr> <td align=center>...</td> <td align=center>...</td> <td align=center>...</td> <td align=center>...</td> </tr>";
				int placeMe = 0;
				for (int idMe : scores.keySet())
				{
					placeMe++;
					if (idMe == owner)
					{
						break;
					}
				}
				pt += tx(placeMe, arMe.name, arMe.cought, arMe.escaped, true);
			}
		}

		pt += "</table>";
		pt += "<br><br>";
		if ((Config.RANK_FISHERMAN_REWARD_ID > 0) && (Config.RANK_FISHERMAN_REWARD_COUNT > 0))
		{
			pt += "Proxima recmensa em <font color=\"LEVEL\">" + calcMinTo() + " min(s)</font><br1>";
			pt += "<font color=\"aadd77\">" + Config.RANK_FISHERMAN_REWARD_COUNT + " &#" + Config.RANK_FISHERMAN_REWARD_ID + ";</font>";
		}

		pt += "</center></body></html>";

		return pt;
	}

	private int calcMinTo()
	{
		return ((int) (nextTimeUpdateReward - System.currentTimeMillis())) / 60000;
	}

	private String tx(int counter, String name, int kills, int deaths, boolean mi)
	{
		String t = "";

		t += " <tr>" + "<td align=center>" + (mi ? "<font color=\"LEVEL\">" : "") + (counter + 1) + ".</td>" + "<td align=center>" + name + "</td>" + "<td align=center>" + kills + "</td>" + "<td align=center>" + deaths + "" + (mi ? "</font>" : "") + " </td>" + "</tr>";

		return t;
	}

	public void engineInit()
	{
		_ranks = new FastMap<>();
		startTask();
		_log.info(getClass().getSimpleName()+": Iniciado");
	}


	public class FishRank
	{
		public int		cought, escaped;
		public String	name;

		public FishRank()
		{
			cought = 0;
			escaped = 0;
		}

		public void cought()
		{
			cought++;
		}

		public void escaped()
		{
			escaped++;
		}
	}
}