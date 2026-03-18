package fidas.server.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fidas.server.Config;
import fidas.server.gameserver.engines.DocumentParser;
import fidas.server.gameserver.model.L2EnchantSkillGroup;
import fidas.server.gameserver.model.L2EnchantSkillGroup.EnchantSkillHolder;
import fidas.server.gameserver.model.L2EnchantSkillLearn;
import fidas.server.gameserver.model.StatsSet;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.skills.L2Skill;

public class EnchantGroupsData extends DocumentParser
{
	public static final int NORMAL_ENCHANT_COST_MULTIPLIER = Config.NORMAL_ENCHANT_COST_MULTIPLIER;
	public static final int SAFE_ENCHANT_COST_MULTIPLIER = Config.SAFE_ENCHANT_COST_MULTIPLIER;
	
	public static final int NORMAL_ENCHANT_BOOK = 6622;
	public static final int SAFE_ENCHANT_BOOK = 9627;
	public static final int CHANGE_ENCHANT_BOOK = 9626;
	public static final int UNTRAIN_ENCHANT_BOOK = 9625;
	
	private final Map<Integer, L2EnchantSkillGroup> _enchantSkillGroups = new HashMap<>();
	private final Map<Integer, L2EnchantSkillLearn> _enchantSkillTrees = new HashMap<>();
	
	/**
	 * Instantiates a new enchant groups table.
	 */
	protected EnchantGroupsData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_enchantSkillGroups.clear();
		_enchantSkillTrees.clear();
		parseDatapackFile("data/enchantSkillGroups.xml");
		int routes = 0;
		for (L2EnchantSkillGroup group : _enchantSkillGroups.values())
		{
			routes += group.getEnchantGroupDetails().size();
		}
		_log.info(getClass().getSimpleName() + ": " + _enchantSkillGroups.size() + " groups and " + routes + " routes.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		StatsSet set;
		Node att;
		int id = 0;
		L2EnchantSkillGroup group;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("group".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						id = parseInt(attrs, "id");
						
						group = _enchantSkillGroups.get(id);
						if (group == null)
						{
							group = new L2EnchantSkillGroup(id);
							_enchantSkillGroups.put(id, group);
						}
						
						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							if ("enchant".equalsIgnoreCase(b.getNodeName()))
							{
								attrs = b.getAttributes();
								set = new StatsSet();
								
								for (int i = 0; i < attrs.getLength(); i++)
								{
									att = attrs.item(i);
									set.set(att.getNodeName(), att.getNodeValue());
								}
								group.addEnchantDetail(new EnchantSkillHolder(set));
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Adds the new route for skill.
	 * @param skillId the skill id
	 * @param maxLvL the max lvl
	 * @param route the route
	 * @param group the group
	 * @return the int
	 */
	public int addNewRouteForSkill(int skillId, int maxLvL, int route, int group)
	{
		L2EnchantSkillLearn enchantableSkill = _enchantSkillTrees.get(skillId);
		if (enchantableSkill == null)
		{
			enchantableSkill = new L2EnchantSkillLearn(skillId, maxLvL);
			_enchantSkillTrees.put(skillId, enchantableSkill);
		}
		if (_enchantSkillGroups.containsKey(group))
		{
			enchantableSkill.addNewEnchantRoute(route, group);
			
			return _enchantSkillGroups.get(group).getEnchantGroupDetails().size();
		}
		_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error while loading generating enchant skill id: " + skillId + "; route: " + route + "; missing group: " + group);
		return 0;
	}
	
	/**
	 * Gets the skill enchantment for skill.
	 * @param skill the skill
	 * @return the skill enchantment for skill
	 */
	public L2EnchantSkillLearn getSkillEnchantmentForSkill(L2Skill skill)
	{
		// there is enchantment for this skill and we have the required level of it
		final L2EnchantSkillLearn esl = getSkillEnchantmentBySkillId(skill.getId());
		if ((esl != null) && (skill.getLevel() >= esl.getBaseLevel()))
		{
			return esl;
		}
		return null;
	}
	
	/**
	 * Gets the skill enchantment by skill id.
	 * @param skillId the skill id
	 * @return the skill enchantment by skill id
	 */
	public L2EnchantSkillLearn getSkillEnchantmentBySkillId(int skillId)
	{
		return _enchantSkillTrees.get(skillId);
	}
	
	/**
	 * Gets the enchant skill group by id.
	 * @param id the id
	 * @return the enchant skill group by id
	 */
	public L2EnchantSkillGroup getEnchantSkillGroupById(int id)
	{
		return _enchantSkillGroups.get(id);
	}
	
	/**
	 * Gets the enchant skill sp cost.
	 * @param skill the skill
	 * @return the enchant skill sp cost
	 */
	public int getEnchantSkillSpCost(L2Skill skill)
	{
		final L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null)
		{
			final EnchantSkillHolder esh = enchantSkillLearn.getEnchantSkillHolder(skill.getLevel());
			if (esh != null)
			{
				return esh.getSpCost();
			}
		}
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Gets the enchant skill Adena cost.
	 * @param skill the skill
	 * @return the enchant skill Adena cost
	 */
	public int getEnchantSkillAdenaCost(L2Skill skill)
	{
		final L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null)
		{
			final EnchantSkillHolder esh = enchantSkillLearn.getEnchantSkillHolder(skill.getLevel());
			if (esh != null)
			{
				return esh.getAdenaCost();
			}
		}
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Gets the enchant skill rate.
	 * @param player the player
	 * @param skill the skill
	 * @return the enchant skill rate
	 */
	public byte getEnchantSkillRate(L2PcInstance player, L2Skill skill)
	{
		final L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null)
		{
			final EnchantSkillHolder esh = enchantSkillLearn.getEnchantSkillHolder(skill.getLevel());
			if (esh != null)
			{
				return esh.getRate(player);
			}
		}
		return 0;
	}
	
	/**
	 * Gets the single instance of EnchantGroupsData.
	 * @return single instance of EnchantGroupsData
	 */
	public static EnchantGroupsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantGroupsData _instance = new EnchantGroupsData();
	}
}