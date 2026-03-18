package fidas.server.gameserver.datatables;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.Config;
import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.L2DropData;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.actor.instance.L2MonsterInstance;
import fidas.server.gameserver.model.stats.Stats;
import fidas.server.util.Rnd;

public final class ChampionData extends DocumentParser
{
	private List<StatsSet> _championData;
	private List<Integer> _championChances;
	private List<List<L2DropData>> _championRewards;
	
	protected ChampionData()
	{
		if (Config.L2JMOD_CHAMPION_ENABLE)
		{
			_championData = new ArrayList<>();
			_championChances = new ArrayList<>();
			_championRewards = new ArrayList<>();
			load();
		}
	}
	
	/**
	 * Loads configuration data.
	 */
	@Override
	public void load()
	{
		_championData.clear();
		_championChances.clear();
		_championRewards.clear();
		parseDirectory(new File(Config.DATAPACK_ROOT, "data/stats/champions"));
		_log.info(getClass().getSimpleName() + ": " + _championData.size() + " champion templates.");
	}
	
	/**
	 * Parses configuration files.
	 */
	@Override
	protected void parseDocument()
	{
		final Node list = getCurrentDocument().getFirstChild();
		
		NamedNodeMap attrs;
		int sumChance = 0;
		for (Node n = list.getFirstChild(); n != null; n = n.getNextSibling())
		{
			StatsSet dataHolder = new StatsSet();
			List<L2DropData> rewards = new ArrayList<>();
			if (n.getNodeName().equals("champion"))
			{
				if (sumChance <= 1000000)
				{
					for (Node championData = n.getFirstChild(); championData != null; championData = championData.getNextSibling())
					{
						if (championData.getNodeName().equals("chance"))
						{
							int chance = (int) (Float.parseFloat(championData.getTextContent()) * 10000);
							sumChance += chance;
							if (sumChance <= 1000000)
							{
								_championChances.add(chance);
							}
							else
							{
								_log.warning(getClass().getSimpleName() + ": sum of chances is greater, than 100% - champion definition ignored");
								break;
							}
						}
						else if (championData.getNodeName().equals("reward"))
						{
							attrs = championData.getAttributes();
							int itemId = parseInteger(attrs, "id");
							int min = parseInteger(attrs, "min");
							int max = parseInteger(attrs, "max");
							if (ItemTable.getInstance().getTemplate(itemId) != null)
							{
								rewards.add(new L2DropData(itemId, min, max, 0));
							}
							else
							{
								_log.warning(getClass().getSimpleName() + ": invalid item id: " + itemId);
							}
						}
						else
						{
							dataHolder.set(championData.getNodeName(), championData.getTextContent());
						}
					}
					_championData.add(dataHolder);
					_championRewards.add(rewards);
				}
				else
				{
					_log.warning(getClass().getSimpleName() + ": sum of chances is greater, than 100% - champion definition ignored");
				}
			}
		}
	}
	
	/**
	 * @param championType type of champion.
	 * @param name String name of multiplier.
	 * @return value of multiplier for given champion type.
	 */
	public float getMultiplier(int championType, String name)
	{
		float mul;
		if ((championType >= 0) && (championType < _championData.size()))
		{
			mul = _championData.get(championType).getFloat(name, 1);
		}
		else
		{
			mul = 1;
		}
		
		return mul;
	}
	
	/**
	 * @param championType type of champion.
	 * @param stat stat to calculate multiplier.
	 * @return value of multiplier for given champion type.
	 */
	public float getMultiplier(int championType, Stats stat)
	{
		return getMultiplier(championType, stat.getValue());
	}
	
	/**
	 * @param championType type of champion
	 * @param monster monster to check
	 * @return {@code true} if given monster meeets level requirements for given type of champion
	 */
	private boolean checkLevel(int championType, L2MonsterInstance monster)
	{
		boolean result;
		if ((championType >= 0) && (championType < _championData.size()))
		{
			int minLvl = _championData.get(championType).getInteger("minLvl", 1);
			int maxLvl = _championData.get(championType).getInteger("maxLvl", 1000);
			
			result = (monster.getLevel() >= minLvl) && (monster.getLevel() <= maxLvl);
		}
		else
		{
			result = false;
		}
		
		return result;
	}
	
	/**
	 * @param monster monster to check
	 * @return random type of champion for given monster, based on config rules
	 */
	public int calculateChampionType(L2MonsterInstance monster)
	{
		int championType = -1;
		
		// Check, if monster meets all conditions
		if (!monster.getTemplate().isQuestMonster() && !monster.isRaid() && !monster.isRaidMinion() && (Config.L2JMOD_CHAMPION_ENABLE_IN_INSTANCES || (monster.getInstanceId() == 0)))
		{
			int totalProb = 0;
			for (int i = 0; i < _championChances.size(); i++)
			{
				if (!checkLevel(i, monster))
				{
					continue;
				}
				
				int fortune = Rnd.get(1000000);
				if ((fortune >= totalProb) && (fortune < (totalProb + _championChances.get(i))))
				{
					championType = i;
					break;
				}
				totalProb += _championChances.get(i);
			}
		}
		
		return championType;
	}
	
	/**
	 * @param championType type of champion.
	 * @return glow type for given champion type.
	 */
	public int getGlow(int championType)
	{
		int teamId;
		if ((championType >= 0) && (championType < _championData.size()))
		{
			teamId = _championData.get(championType).getInteger("glow", 0);
		}
		else
		{
			teamId = 0;
		}
		
		return teamId;
	}
	
	/**
	 * @param championType type of champion.
	 * @return title for given champion type.
	 */
	public String getTitle(int championType)
	{
		String title;
		if ((championType >= 0) && (championType < _championData.size()))
		{
			title = _championData.get(championType).getString("title", "");
		}
		else
		{
			title = "";
		}
		
		return title;
	}
	
	/**
	 * @param championType type of champion.
	 * @return glow List of specific rewards for given champion type.
	 */
	public List<L2DropData> getReward(int championType)
	{
		if ((championType >= 0) && (championType < _championData.size()))
		{
			return _championRewards.get(championType);
		}
		
		return null;
	}
	
	/**
	 * @param championType type of champion
	 * @return {@code true} if given type of champion has personal reward.
	 */
	public boolean hasPersonalReward(int championType)
	{
		return ((getReward(championType) != null) && !getReward(championType).isEmpty());
	}
	
	public static ChampionData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ChampionData _instance = new ChampionData();
	}
}