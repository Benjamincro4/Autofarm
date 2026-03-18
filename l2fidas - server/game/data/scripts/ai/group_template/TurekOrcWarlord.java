/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.group_template;

import ai.npc.AbstractNpcAI;

import fidas.server.gameserver.ai.CtrlIntention;
import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.util.Rnd;

public class TurekOrcWarlord extends AbstractNpcAI 
{
	private static final int TUREK_ORC_WARLORD = 20495;
	
	public TurekOrcWarlord(String name, String descr)
	{
		super(name, descr);
		addAttackId(TUREK_ORC_WARLORD);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getNpcId() == TUREK_ORC_WARLORD)
		{
			if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (Rnd.get(100) < 70)
					npc.broadcastNpcSay("The battle has just begun!");
			}
			else if (Rnd.get(100) < 10)
				npc.broadcastNpcSay("You wont take me down easily.");
		}
		
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new TurekOrcWarlord(TurekOrcWarlord.class.getSimpleName(), "ai");
	}
}