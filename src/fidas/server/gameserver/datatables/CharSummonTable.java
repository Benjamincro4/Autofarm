package fidas.server.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import fidas.server.Config;
import fidas.server.L2DatabaseFactory;
import fidas.server.gameserver.idfactory.IdFactory;
import fidas.server.gameserver.model.L2SummonItem;
import fidas.server.gameserver.model.actor.instance.L2MerchantSummonInstance;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.actor.instance.L2PetInstance;
import fidas.server.gameserver.model.actor.instance.L2ServitorInstance;
import fidas.server.gameserver.model.actor.instance.L2SiegeSummonInstance;
import fidas.server.gameserver.model.actor.templates.L2NpcTemplate;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.model.skills.l2skills.L2SkillSummon;
import fidas.server.gameserver.network.serverpackets.PetItemList;

import gnu.trove.map.hash.TIntIntHashMap;

public class CharSummonTable
{
	private static Logger _log = Logger.getLogger(CharSummonTable.class.getName());
	
	private static final String INIT_SUMMONS = "SELECT ownerId, summonSkillId FROM character_summons";
	private static final String INIT_PET = "SELECT ownerId, item_obj_id FROM pets WHERE restore = 'true'";
	
	private static final String SAVE_SUMMON = "REPLACE INTO character_summons (ownerId,summonSkillId,curHp,curMp,time) VALUES (?,?,?,?,?)";
	private static final String LOAD_SUMMON = "SELECT curHp, curMp, time FROM character_summons WHERE ownerId = ? AND summonSkillId = ?";
	private static final String REMOVE_SUMMON = "DELETE FROM character_summons WHERE ownerId = ?";
	
	private static final TIntIntHashMap _servitors = new TIntIntHashMap();
	private static final TIntIntHashMap _pets = new TIntIntHashMap();
	
	public static CharSummonTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void init()
	{
		if (Config.RESTORE_SERVITOR_ON_RECONNECT)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery(INIT_SUMMONS))
			{
				while (rs.next())
				{
					_servitors.put(rs.getInt("ownerId"), rs.getInt("summonSkillId"));
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error while loading saved summons", e);
			}
		}
		
		if (Config.RESTORE_PET_ON_RECONNECT)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery(INIT_PET))
			{
				while (rs.next())
				{
					_pets.put(rs.getInt("ownerId"), rs.getInt("item_obj_id"));
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error while loading saved summons", e);
			}
		}
	}
	
	public TIntIntHashMap getServitors()
	{
		return _servitors;
	}
	
	public TIntIntHashMap getPets()
	{
		return _pets;
	}
	
	public void saveSummon(L2ServitorInstance summon)
	{
		if ((summon == null) || (summon.getTimeRemaining() <= 0))
		{
			return;
		}
		_servitors.put(summon.getOwner().getObjectId(), summon.getReferenceSkill());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SAVE_SUMMON))
		{
			ps.setInt(1, summon.getOwner().getObjectId());
			ps.setInt(2, summon.getReferenceSkill());
			ps.setInt(3, (int) Math.round(summon.getCurrentHp()));
			ps.setInt(4, (int) Math.round(summon.getCurrentMp()));
			ps.setInt(5, summon.getTimeRemaining());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed to store summon [SummonId: " + summon.getNpcId() + "] from Char [CharId: " + summon.getOwner().getObjectId() + "] data", e);
		}
		
	}
	
	public void restoreServitor(L2PcInstance activeChar)
	{
		int skillId = _servitors.get(activeChar.getObjectId());
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_SUMMON))
		{
			ps.setInt(1, activeChar.getObjectId());
			ps.setInt(2, skillId);
			try (ResultSet rs = ps.executeQuery())
			{
				
				L2NpcTemplate summonTemplate;
				L2ServitorInstance summon;
				L2SkillSummon skill;
				
				while (rs.next())
				{
					int curHp = rs.getInt("curHp");
					int curMp = rs.getInt("curMp");
					int time = rs.getInt("time");
					
					skill = (L2SkillSummon) SkillTable.getInstance().getInfo(skillId, activeChar.getSkillLevel(skillId));
					if (skill == null)
					{
						removeServitor(activeChar);
						return;
					}
					
					summonTemplate = NpcTable.getInstance().getTemplate(skill.getNpcId());
					if (summonTemplate == null)
					{
						_log.warning(getClass().getSimpleName() + ": Summon attemp for nonexisting Skill ID:" + skillId);
						return;
					}
					
					final int id = IdFactory.getInstance().getNextId();
					if (summonTemplate.isType("L2SiegeSummon"))
					{
						summon = new L2SiegeSummonInstance(id, summonTemplate, activeChar, skill);
					}
					else if (summonTemplate.isType("L2MerchantSummon"))
					{
						// TODO: Confirm L2Merchant summon = new L2MerchantSummonInstance(id, summonTemplate, activeChar, skill);
						summon = new L2ServitorInstance(id, summonTemplate, activeChar, skill);
					}
					else
					{
						summon = new L2ServitorInstance(id, summonTemplate, activeChar, skill);
					}
					
					summon.setName(summonTemplate.getName());
					summon.setTitle(activeChar.getName());
					summon.setExpPenalty(skill.getExpPenalty());
					summon.setSharedElementals(skill.getInheritElementals());
					summon.setSharedElementalsValue(skill.getElementalSharePercent());
					
					if (summon.getLevel() >= ExperienceTable.getInstance().getMaxPetLevel())
					{
						summon.getStat().setExp(ExperienceTable.getInstance().getExpForLevel(ExperienceTable.getInstance().getMaxPetLevel() - 1));
						_log.warning(getClass().getSimpleName() + ": Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above " + ExperienceTable.getInstance().getMaxPetLevel() + ". Please rectify.");
					}
					else
					{
						summon.getStat().setExp(ExperienceTable.getInstance().getExpForLevel(summon.getLevel() % ExperienceTable.getInstance().getMaxPetLevel()));
					}
					summon.setCurrentHp(curHp);
					summon.setCurrentMp(curMp);
					summon.setHeading(activeChar.getHeading());
					summon.setRunning();
					if (!(summon instanceof L2MerchantSummonInstance))
					{
						activeChar.setPet(summon);
					}
					
					summon.setTimeRemaining(time);
					
					// L2World.getInstance().storeObject(summon);
					summon.spawnMe(activeChar.getX() + 20, activeChar.getY() + 20, activeChar.getZ());
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Summon cannot be restored: ", e);
		}
	}
	
	public void removeServitor(L2PcInstance activeChar)
	{
		_servitors.remove(activeChar.getObjectId());
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(REMOVE_SUMMON))
		{
			ps.setInt(1, activeChar.getObjectId());
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Summon cannot be removed: ", e);
		}
	}
	
	public void restorePet(L2PcInstance activeChar)
	{
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_pets.get(activeChar.getObjectId()));
		final L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(sitem.getNpcId());
		
		if (npcTemplate == null)
		{
			return;
		}
		
		final L2PetInstance pet = L2PetInstance.spawnPet(npcTemplate, activeChar, item);
		if (pet == null)
		{
			return;
		}
		
		pet.setShowSummonAnimation(true);
		pet.setTitle(activeChar.getName());
		
		if (!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp());
			pet.setCurrentMp(pet.getMaxMp());
			pet.getStat().setExp(pet.getExpForThisLevel());
			pet.setCurrentFed(pet.getMaxFed());
		}
		
		pet.setRunning();
		
		if (!pet.isRespawned())
		{
			pet.store();
		}
		
		activeChar.setPet(pet);
		
		pet.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
		pet.startFeed();
		item.setEnchantLevel(pet.getLevel());
		
		if (pet.getCurrentFed() <= 0)
		{
			pet.unSummon(activeChar);
		}
		else
		{
			pet.startFeed();
		}
		
		pet.setFollowStatus(true);
		
		pet.getOwner().sendPacket(new PetItemList(pet.getInventory().getItems()));
		pet.broadcastStatusUpdate();
	}
	
	private static class SingletonHolder
	{
		protected static final CharSummonTable _instance = new CharSummonTable();
	}
}