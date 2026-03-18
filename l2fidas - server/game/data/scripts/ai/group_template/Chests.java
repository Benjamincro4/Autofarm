/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.group_template;

import java.util.List;

import ai.npc.AbstractNpcAI;

import fidas.server.gameserver.datatables.NpcTable;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.model.L2Object;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2ChestInstance;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.actor.templates.L2NpcTemplate;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.network.serverpackets.PlaySound;
import fidas.server.gameserver.util.Util;
import fidas.server.util.Rnd;

/**
 * Chest AI implementation.
 * @author Fulminus
 * @author FBIagent
 */
public class Chests extends AbstractNpcAI
{
	private static final int _SKILL_UNLOCK_ID = 27;
	private static final int _SKILL_MAESTRO_KEY_ID = 22271;
	// TARGET_AURA skill with suicide(Treasure Bomb)
	private static final int _SKILL_SUICIDE_ID = 4143;
	
	private static void makeSelfDestruction(L2ChestInstance chest)
	{
		int skillLevel = chest.getLevel() / 10;
		chest.doCast(SkillTable.getInstance().getInfo(_SKILL_SUICIDE_ID, skillLevel));
	}
	
	private static void makeDieReward(L2ChestInstance chest, L2PcInstance attacker)
	{
		chest.setIsInvul(false);
		chest.setSpecialDrop();
		chest.reduceCurrentHp(chest.getMaxHp(), attacker, null);
	}
	
	private static void handleChanceFailure(L2ChestInstance chest, L2PcInstance attacker)
	{
		attacker.sendPacket(new PlaySound("ItemSound2.broken_key"));
		chest.deleteMe();
	}
	
	public static void main(String[] args)
	{
		new Chests("chests", "ai");
	}
	
	private Chests(String name, String descr)
	{
		super(name, descr);
		List<L2NpcTemplate> chestTemplates = NpcTable.getInstance().getAllNpcOfClassType("L2Chest");
		for (L2NpcTemplate chestTemplate : chestTemplates)
		{
			int chestNpcId = chestTemplate.getNpcId();
			addSpawnId(chestNpcId);
			addAttackId(chestNpcId);
			addSkillSeeId(chestNpcId);
			addSpellFinishedId(chestNpcId);
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		// this is set to avoid killing the chest when a player is too strong with an attack
		npc.setIsInvul(true);
		// this is set to avoid the chests to attack back
		npc.disableCoreAI(true);
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		L2ChestInstance chest = (L2ChestInstance) npc;
		if (chest.isInteracted())
		{
			return null;
		}
		
		chest.setInteracted();
		makeSelfDestruction(chest);
		return null;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		L2ChestInstance chest = (L2ChestInstance) npc;
		if (!Util.contains(targets, npc) || chest.isInteracted())
		{
			return null;
		}
		
		chest.setInteracted();
		
		int rewardChance = 0;
		
		switch (skill.getId())
		{
			case _SKILL_UNLOCK_ID:
			{
				int maxChance = 0;
				
				switch (skill.getLevel())
				{
					case 1:
						maxChance = 20;
						break;
					case 2:
						maxChance = 30;
						break;
					case 3:
						maxChance = 40;
						break;
					case 4:
						maxChance = 50;
						break;
					case 5:
						maxChance = 60;
						break;
					case 6:
						maxChance = 63;
						break;
					case 7:
						maxChance = 70;
						break;
					case 8:
						maxChance = 72;
						break;
					case 9:
						maxChance = 75;
						break;
					case 10:
						maxChance = 80;
						break;
					case 11:
						maxChance = 85;
						break;
					case 12:
					case 13:
					case 14:
					case 15:
						maxChance = 90;
						break;
					default:
						break;
				}
				
				rewardChance = maxChance - ((chest.getLevel() - (skill.getLevel() * 4) - 16) * 6);
				if (rewardChance > maxChance)
				{
					rewardChance = maxChance;
				}
			}
			case _SKILL_MAESTRO_KEY_ID:
				if (((caster.getLevel() <= 77) && (Math.abs(chest.getLevel() - caster.getLevel()) > 6)) || ((caster.getLevel() >= 78) && (Math.abs(chest.getLevel() - caster.getLevel()) > 5)))
				{
					rewardChance = 0;
				}
				else
				{
					rewardChance = 80;
				}
				
				break;
			default:
				makeSelfDestruction(chest);
				return null;
		}
		
		if (Rnd.get(100) < rewardChance)
		{
			makeDieReward(chest, caster);
		}
		else
		{
			handleChanceFailure(chest, caster);
		}
		
		return null;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		npc.deleteMe();
		return null;
	}
}
