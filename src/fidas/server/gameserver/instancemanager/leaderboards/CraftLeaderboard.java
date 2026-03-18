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

public class CraftLeaderboard
{
	private static CraftLeaderboard	_instance;
	public Logger _log = Logger.getLogger(CraftLeaderboard.class.getName());
	public Map<Integer, CraftRank> _ranks = new FastMap<>();
	protected Future<?> _actionTask = null;
	protected int TASK_DELAY = Config.RANK_CRAFT_INTERVAL;
	protected Long nextTimeUpdateReward = 0L;

	public static CraftLeaderboard getInstance()
	{
		if (_instance == null)
		{
			_instance = new CraftLeaderboard();
		}

		return _instance;
	}
	
	public CraftLeaderboard()
	{
		engineInit();
	}	

	public void onSucess(int owner, String name)
	{
		CraftRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new CraftRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}

		ar.sucess();
		ar.name = name;
		_ranks.put(owner, ar);
	}

	public void onFail(int owner, String name)
	{
		CraftRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new CraftRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}

		ar.fail();
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

	public class CraftTask implements Runnable
	{
		@Override
		public void run()
		{
			_log.info("CraftManager: Reiniciado.");
			formRank();
			nextTimeUpdateReward = System.currentTimeMillis() + TASK_DELAY * 60000;
		}
	}

	public void startTask()
	{
		if (_actionTask == null)
		{
			_actionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CraftTask(), 1000, TASK_DELAY * 60000);
		}
	}

	public void formRank()
	{
		Map<Integer, Integer> scores = new FastMap<>();
		for (int obj : _ranks.keySet())
		{
			CraftRank ar = _ranks.get(obj);
			scores.put(obj, ar.sucess - ar.fail);
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
		
		CraftRank arTop = _ranks.get(idTop);
		
		if (arTop == null)
		{
			Announcements.getInstance().announceToAll("CraftMaster: Nao ha vencedores neste momento!");
			_ranks.clear();
			return;
		}

		L2PcInstance winner = (L2PcInstance) L2World.getInstance().findObject(idTop);

		Announcements.getInstance().announceToAll("Atencao jogadores: " + arTop.name + " foi o vencedor desta vez com " + arTop.sucess + "/" + arTop.fail + ". Proximo calxculo em " + Config.RANK_CRAFT_INTERVAL + " min(s).");
		if ((winner != null) && (Config.RANK_CRAFT_REWARD_ID > 0) && (Config.RANK_CRAFT_REWARD_COUNT > 0))
		{
			winner.getInventory().addItem("CraftManager", Config.RANK_CRAFT_REWARD_ID, Config.RANK_CRAFT_REWARD_COUNT, winner, null);
			if (Config.RANK_CRAFT_REWARD_COUNT > 1) //You have earned $s1.
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(Config.RANK_CRAFT_REWARD_ID).addNumber( Config.RANK_CRAFT_REWARD_COUNT));
			}
			else
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(Config.RANK_CRAFT_REWARD_ID));
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
			CraftRank ar = _ranks.get(obj);
			scores.put(obj, ar.sucess - ar.fail);
		}

		scores = Util.sortMap(scores, false);

		int counter = 0, max = 20;
		String pt = "<html><body><center>" + "<font color=\"cc00ad\">TOP " + max + " Crafters</font><br>";

		pt += "<table width=260 border=0 cellspacing=0 cellpadding=0 bgcolor=333333>";
		pt += "<tr> <td align=center>No.</td> <td align=center>Nome</td> <td align=center>Sucesso</td> <td align=center>Falho</td> </tr>";
		pt += "<tr> <td align=center>&nbsp;</td> <td align=center>&nbsp;</td> <td align=center></td> <td align=center></td> </tr>";
		boolean inTop = false;
		for (int id : scores.keySet())
		{
			if (counter < max)
			{
				CraftRank ar = _ranks.get(id);
				pt += tx(counter, ar.name, ar.sucess, ar.fail, id == owner);
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
			CraftRank arMe = _ranks.get(owner);
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
				pt += tx(placeMe, arMe.name, arMe.sucess, arMe.fail, true);
			}
		}

		pt += "</table>";
		pt += "<br><br>";
		if ((Config.RANK_CRAFT_REWARD_ID > 0) && (Config.RANK_CRAFT_REWARD_COUNT > 0))
		{
			pt += "Proxima recompensa em <font color=\"LEVEL\">" + calcMinTo() + " min(s)</font><br1>";
			pt += "<font color=\"aadd77\">" + Config.RANK_CRAFT_REWARD_COUNT + " &#" + Config.RANK_CRAFT_REWARD_ID + ";</font>";
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


	public class CraftRank
	{
		public int		sucess, fail;
		public String	name;

		public CraftRank()
		{
			sucess = 0;
			fail = 0;
		}

		public void sucess()
		{
			sucess++;
		}

		public void fail()
		{
			fail++;
		}
	}
}