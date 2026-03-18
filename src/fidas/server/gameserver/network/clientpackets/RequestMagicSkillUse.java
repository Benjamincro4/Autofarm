/*
 * Copyright (C) 2004-2013 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fidas.server.gameserver.network.clientpackets;

import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.skills.L2Skill;

public final class RequestMagicSkillUse extends L2GameClientPacket
{
	private static final String _C__39_REQUESTMAGICSKILLUSE = "[C] 39 RequestMagicSkillUse";

	private int _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		_magicId = readD(); // Identifier of the used skill
		_ctrlPressed = readD() != 0; // True if it's a ForceAttack : Ctrl pressed
		_shiftPressed = readC() != 0; // True if Shift pressed
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final int level = activeChar.getSkillLevel(_magicId);
		if (level <= 0)
			return;

		final L2Skill skill = SkillTable.getInstance().getInfo(_magicId, level);
		if (skill != null)
			activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
	}

	@Override
	public String getType()
	{
		return _C__39_REQUESTMAGICSKILLUSE;
	}
}
