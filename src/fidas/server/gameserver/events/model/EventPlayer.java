package fidas.server.gameserver.events.model;

import java.net.InetAddress;
import java.util.List;

import javolution.util.FastList;

import fidas.server.gameserver.GameTimeController;
import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.datatables.ItemTable;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.events.AbstractEvent;
import fidas.server.gameserver.instancemanager.TransformationManager;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.L2Party;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.Location;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.L2Summon;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.actor.instance.L2PetInstance;
import fidas.server.gameserver.model.base.ClassType;
import fidas.server.gameserver.model.base.PlayerClass;
import fidas.server.gameserver.model.effects.AbnormalEffect;
import fidas.server.gameserver.model.effects.L2Effect;
import fidas.server.gameserver.model.entity.Duel;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.network.NpcStringId;
import fidas.server.gameserver.network.serverpackets.AbstractNpcInfo;
import fidas.server.gameserver.network.serverpackets.CreatureSay;
import fidas.server.gameserver.network.serverpackets.ExShowScreenMessage;
import fidas.server.gameserver.network.serverpackets.InventoryUpdate;
import fidas.server.gameserver.network.serverpackets.ItemList;
import fidas.server.gameserver.network.serverpackets.L2GameServerPacket;
import fidas.server.gameserver.network.serverpackets.MagicSkillUse;
import fidas.server.gameserver.network.serverpackets.SetupGauge;
import fidas.server.gameserver.network.serverpackets.SkillCoolTime;
import fidas.server.gameserver.util.Broadcast;

public class EventPlayer implements Comparable<EventPlayer>
{
	private final L2PcInstance _owner;
	private final int _playersId;
	private boolean _isInEvent;
	private boolean _isRegistered;
	private boolean _isInFFAEvent;
	private boolean _isSpectator;
	private boolean _canBuff;
	private int _status;
	private int _mainTeam;
	private int _score;
	private AbstractEvent event;
	
	private int _origNameColor;
	private PLoc _origLoc;
	private String _origTitle;
	
	private int _kills;
	private int _deaths;
	
	public EventPlayer(L2PcInstance owner)
	{
		_owner = owner;
		_playersId = owner == null ? -1 : owner.getObjectId();
		
		_isRegistered = false;
		_isInEvent = false;
		_isInFFAEvent = false;
		
		_kills = 0;
		_deaths = 0;
	}
	
	public void abortCast()
	{
		_owner.abortCast();
	}
	
	public void setEvent(AbstractEvent e)
	{
		event = e;
	}
	
	public AbstractEvent getEvent()
	{
		return event;
	}
	
	public void addItem(int id, int ammount, boolean show)
	{
		_owner.addItem("Event", id, ammount, _owner, show);
	}
	
	public void addSkill(int id, int lvl)
	{
		_owner.addSkill(SkillTable.getInstance().getInfo(id, lvl), false);
	}
	
	public void broadcastSkillUse(int skillId, int level)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
		
		if (skill != null)
		{
			_owner.broadcastPacket(new MagicSkillUse(_owner, _owner, skillId, level, skill.getHitTime(), skill.getReuseDelay()));
		}
	}
	
	public void broadcastTitleInfo()
	{
		_owner.broadcastTitleInfo();
	}
	
	public void broadcastUserInfo()
	{
		_owner.broadcastUserInfo();
	}
	
	public boolean canBuff()
	{
		return _canBuff;
	}
	
	public void clean()
	{
		_isRegistered = false;
		_isInEvent = false;
		_isInFFAEvent = false;
		
		_kills = 0;
		_deaths = 0;
	}
	
	public void doDie()
	{
		_owner.doDie(_owner);
	}
	
	public void doDieNpc(Integer npc)
	{
		_owner.doDie((L2Npc) L2World.getInstance().findObject(npc));
	}
	
	public void doRevive()
	{
		_owner.doRevive();
	}
	
	public void enableAllSkills()
	{
		for (L2Skill skill : _owner.getAllSkills())
		{
			if (skill.getReuseDelay() <= 900000)
			{
				_owner.enableSkill(skill);
			}
		}
		_owner.sendPacket(new SkillCoolTime(_owner));
	}
	
	public void equipNewItem(int itemId)
	{
		_owner.getInventory().equipItem(ItemTable.getInstance().createItem("", itemId, 1, _owner, null));
	}
	
	public int getClassIndex()
	{
		return _owner.getClassIndex();
	}
	
	@SuppressWarnings("deprecation")
	public String getClassName()
	{
		return _owner.getTemplate().getClassName();
	}
	
	public int getDeaths()
	{
		return _deaths;
	}
	
	public void getEffects(int skill, int lvl)
	{
		SkillTable.getInstance().getInfo(skill, lvl).getEffects(_owner, _owner);
	}
	
	public InetAddress getInetAddress()
	{
		try
		{
			return _owner.getClient().getConnectionAddress();
		}
		catch (NullPointerException e)
		{
			return null;
		}
		
	}
	
	public int getKarma()
	{
		return _owner.getKarma();
	}
	
	public int getKills()
	{
		return _kills;
	}
	
	public int getLevel()
	{
		if (_owner != null)
		{
			return _owner.getLevel();
		}
		return 0;
	}
	
	public int getMainTeam()
	{
		return _mainTeam;
	}
	
	public String getName()
	{
		return _owner.getName();
	}
	
	public PLoc getOrigLoc()
	{
		return _origLoc;
	}
	
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public PLoc getOwnerLoc()
	{
		return new PLoc(_owner.getX(), _owner.getY(), _owner.getZ());
	}
	
	@SuppressWarnings("deprecation")
	public int getPartyLeaderId()
	{
		return _owner.getParty().getPartyLeaderOID();
	}
	
	@SuppressWarnings("deprecation")
	public List<Integer> getPartyMembers()
	{
		List<Integer> pt = new FastList<>();
		for (L2PcInstance player : _owner.getParty().getPartyMembers())
		{
			if (player != null)
			{
				pt.add(player.getObjectId());
			}
		}
		return pt;
	}
	
	public int getPartyMembersCount()
	{
		return _owner.getParty().getMemberCount();
	}
	
	public int getPkKills()
	{
		return _owner.getPkKills();
	}
	
	public double getPlanDistanceSq(L2Object obj)
	{
		return _owner.getPlanDistanceSq(obj);
	}
	
	public int getPlayersId()
	{
		return _playersId;
	}
	
	public String getPlayersName()
	{
		if (_owner != null)
		{
			return _owner.getName();
		}
		return "";
	}
	
	public int getPvpKills()
	{
		return _owner.getPvpKills();
	}
	
	public int getScore()
	{
		return _score;
	}
	
	public int getStatus()
	{
		return _status;
	}
	
	public boolean hasParty()
	{
		return _owner.getParty() != null;
	}
	
	public void healToMax()
	{
		_owner.setCurrentCp(_owner.getMaxCp());
		_owner.setCurrentHp(_owner.getMaxHp());
		_owner.setCurrentMp(_owner.getMaxMp());
	}
	
	public void initOrigInfo()
	{
		_origNameColor = _owner.getAppearance().getNameColor();
		_origTitle = _owner.getTitle();
		_origLoc = new PLoc(_owner.getX(), _owner.getY(), _owner.getZ(), _owner.getHeading());
	}
	
	public boolean isCastingNow()
	{
		return _owner.isCastingNow();
	}
	
	public boolean isClassType(String type)
	{
		if (type.equals("Fighter"))
		{
			if (PlayerClass.values()[_owner.getActiveClass()].isOfType(ClassType.Fighter))
			{
				return true;
			}
		}
		else if (type.equals("Mystic"))
		{
			if (PlayerClass.values()[_owner.getActiveClass()].isOfType(ClassType.Mystic))
			{
				return true;
			}
		}
		else if (type.equals("Priest"))
		{
			if (PlayerClass.values()[_owner.getActiveClass()].isOfType(ClassType.Priest))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isCursedWeaponEquipped()
	{
		return _owner.isCursedWeaponEquipped();
	}
	
	public boolean isDead()
	{
		return _owner.isDead();
	}
	
	public boolean isGM()
	{
		return _owner.isGM();
	}
	
	public boolean isInDuel()
	{
		return _owner.isInDuel();
	}
	
	public boolean isInEvent()
	{
		return _isInEvent;
	}
	
	public boolean isInFFAEvent()
	{
		return _isInFFAEvent;
	}
	
	public boolean isInJail()
	{
		return _owner.isInJail();
	}
	
	public boolean isInOlympiadMode()
	{
		return _owner.isInOlympiadMode();
	}
	
	public boolean isInSiege()
	{
		return _owner.isInSiege();
	}
	
	public boolean isMageClass()
	{
		return _owner.isMageClass();
	}
	
	public boolean isOnline()
	{
		return (_owner != null) && _owner.isOnline();
	}
	
	public boolean isRegistered()
	{
		return _isRegistered;
	}
	
	public boolean isSpectator()
	{
		return _isSpectator;
	}
	
	public void joinParty(L2Party party)
	{
		if (_owner != null)
		{
			_owner.joinParty(party);
		}
	}
	
	public void leaveParty()
	{
		_owner.leaveParty();
	}
	
	public void onDamageReceive(L2Character attacker, int ammount, boolean isDOT)
	{
	}
	
	public void raiseDeaths(int count)
	{
		_deaths += count;
	}
	
	public void raiseKills(int count)
	{
		_kills += count;
	}
	
	public void removeFromParty()
	{
		if (_owner.getParty() != null)
		{
			L2Party party = _owner.getParty();
			party.removePartyMember(_owner, L2Party.messageType.Expelled);
		}
	}
	
	public void removeSkill(int id, int lvl)
	{
		_owner.removeSkill(SkillTable.getInstance().getInfo(id, lvl), false);
	}
	
	public void restoreData()
	{
		_owner.getAppearance().setNameColor(_origNameColor);
		_owner.broadcastUserInfo();
	}
	
	public void restoreTitle()
	{
		setTitle(_origTitle);
	}
	
	public void root()
	{
		_owner.setIsImmobilized(true);
		_owner.startAbnormalEffect(AbnormalEffect.STEALTH);
	}
	
	public void scorebarPacket(String text)
	{
		_owner.sendPacket(new ExShowScreenMessage(1, -1, 2, 0, 1, 0, 0, false, 2000, false, text, NpcStringId.EMPTY_STRING, null));
	}
	
	public void screenMessage(String message)
	{
		if (_owner != null)
		{
			_owner.sendPacket(new ExShowScreenMessage(message, 5000));
		}
	}
	
	public void screenMessage(String msg, int time)
	{
		if (_owner != null)
		{
			_owner.sendPacket(new ExShowScreenMessage(msg, time));
		}
	}
	
	public void sendAbstractNpcInfo(EventNpc npc)
	{
		_owner.sendPacket(new AbstractNpcInfo.NpcInfo(npc.getNpc(), _owner));
	}
	
	public void sendCreatureMessage(String message)
	{
		_owner.sendPacket(new CreatureSay(0, 18, "", message));
	}
	
	public void sendMessage(String msg)
	{
		if (_owner != null)
		{
			_owner.sendMessage(msg);
		}
	}
	
	public void sendPacket(L2GameServerPacket p)
	{
		if (_owner != null)
		{
			_owner.sendPacket(p);
		}
	}
	
	public void setCanBuff(boolean canBuff)
	{
		_canBuff = canBuff;
	}
	
	public void setIsInvul(boolean b)
	{
		_owner.setIsInvul(b);
	}
	
	public void setIsParalyzed(boolean b)
	{
		if ((b && !_owner.isParalyzed()) || (!b && _owner.isParalyzed()))
		{
			_owner.setIsParalyzed(b);
		}
	}
	
	public void setMainTeam(int mainTeam)
	{
		_mainTeam = mainTeam;
	}
	
	public void setNameColor(int color)
	{
		_owner.getAppearance().setNameColor(color);
		_owner.broadcastUserInfo();
	}
	
	public void setNameColor(int r, int g, int b)
	{
		_owner.getAppearance().setNameColor(r, g, b);
	}
	
	public void setScore(int _score)
	{
		this._score = _score;
	}
	
	public void setSitForced(boolean value)
	{
		_owner.eventSitForced = value;
	}
	
	public void setStatus(int status)
	{
		_status = status;
	}
	
	public void setTitle(String title)
	{
		if (_owner != null)
		{
			_owner.setTitle(title);
			_owner.broadcastTitleInfo();
		}
	}
	
	public void setVisible()
	{
		_owner.getAppearance().setVisible();
	}
	
	public void showEscapeEffect()
	{
		_owner.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		_owner.setTarget(_owner);
		_owner.disableAllSkills();
		
		MagicSkillUse msk = new MagicSkillUse(_owner, 1050, 1, 10000, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(_owner, msk, 810000/* 900 */);
		SetupGauge sg = new SetupGauge(0, 10000);
		_owner.sendPacket(sg);
		_owner.forceIsCasting(GameTimeController.getGameTicks() + (10000 / GameTimeController.MILLIS_IN_TICK));
	}
	
	public void simon(Integer npc, String text)
	{
		_owner.sendPacket(new CreatureSay(npc, 1, "Simon", text));
	}
	
	public void sitDown()
	{
		_owner.sitDown();
	}
	
	public void standUp()
	{
		_owner.standUp();
	}
	
	public void stopAllEffects()
	{
		_owner.stopAllEffects();
	}
	
	public void teleport(Location loc, int delay, boolean randomOffset, int instanceId)
	{
		teleport(new PLoc(loc.getX(), loc.getY(), loc.getZ()), delay, randomOffset, instanceId);
	}
	
	public void teleport(PLoc loc, int delay, boolean randomOffset, int instanceId)
	{
		if (_owner == null)
		{
			return;
		}
		
		L2Summon summon = _owner.getSummon();
		
		if (summon != null)
		{
			summon.unSummon(_owner);
		}
		
		if (_owner.isInDuel())
		{
			_owner.setDuelState(Duel.DUELSTATE_INTERRUPTED);
		}
		
		_owner.doRevive();
		
		for (L2Effect e : _owner.getAllEffects())
		{
			if ((e != null) && (e.getSkill() != null) && e.getSkill().isDebuff())
			{
				e.exit();
			}
		}
		
		if (_owner.isSitting())
		{
			_owner.standUp();
		}
		
		_owner.setTarget(null);
		
		_owner.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), randomOffset);
		
		if (instanceId != -1)
		{
			_owner.setInstanceId(instanceId);
		}
		
		healToMax();
		
		_owner.broadcastStatusUpdate();
		_owner.broadcastUserInfo();
	}
	
	public void transform(int id)
	{
		TransformationManager.getInstance().transformPlayer(id, _owner);
		_owner.broadcastUserInfo();
		removeSkill(619, 1);
	}
	
	public void unequipAndRemove(int itemId)
	{
		L2ItemInstance wpn = _owner.getInventory().getPaperdollItem(5);
		if (wpn != null)
		{
			L2ItemInstance[] unequiped = _owner.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			_owner.getInventory().destroyItemByItemId("", itemId, 1, _owner, null);
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			_owner.sendPacket(iu);
			_owner.sendPacket(new ItemList(_owner, true));
			_owner.broadcastUserInfo();
		}
	}
	
	public void unequipItemInSlot(int slot)
	{
		_owner.getInventory().unEquipItemInSlot(slot);
	}
	
	public void unequipWeapon()
	{
		L2ItemInstance wpn = _owner.getInventory().getPaperdollItem(5);
		if (wpn != null)
		{
			_owner.getInventory().unEquipItemInBodySlotAndRecord(128);
		}
	}
	
	public void unroot()
	{
		_owner.setIsImmobilized(false);
		_owner.stopAbnormalEffect(AbnormalEffect.STEALTH);
	}
	
	public void unrootIfNeeded()
	{
		if (getOwner().isImmobilized())
		{
			unroot();
		}
	}
	
	public void unsummonPet()
	{
		if (_owner.getSummon() != null)
		{
			L2Summon summon = _owner.getSummon();
			summon.stopAllEffects();
			if (summon instanceof L2PetInstance)
			{
				summon.unSummon(_owner);
			}
		}
	}
	
	public void untransform()
	{
		if (_owner.isTransformed())
		{
			_owner.untransform();
		}
	}
	
	public void increaseScore()
	{
		_score += 1;
		setTitle("Score: " + _score);
	}
	
	@Override
	public int compareTo(EventPlayer other)
	{
		if (getScore() > other.getScore())
		{
			return 1;
		}
		else if (getScore() < other.getScore())
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
}