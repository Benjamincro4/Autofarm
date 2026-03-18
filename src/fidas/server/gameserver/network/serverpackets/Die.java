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
package fidas.server.gameserver.network.serverpackets;

import fidas.server.gameserver.datatables.AdminTable;
import fidas.server.gameserver.events.EventsInterface;
import fidas.server.gameserver.instancemanager.CHSiegeManager;
import fidas.server.gameserver.instancemanager.CastleManager;
import fidas.server.gameserver.instancemanager.FortManager;
import fidas.server.gameserver.instancemanager.TerritoryWarManager;
import fidas.server.gameserver.model.L2AccessLevel;
import fidas.server.gameserver.model.L2Clan;
import fidas.server.gameserver.model.L2SiegeClan;
import fidas.server.gameserver.model.actor.L2Attackable;
import fidas.server.gameserver.model.actor.L2Character;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.entity.Castle;
import fidas.server.gameserver.model.entity.Fort;
import fidas.server.gameserver.model.entity.TvTEvent;
import fidas.server.gameserver.model.entity.TvTRoundEvent;
import fidas.server.gameserver.model.entity.clanhall.SiegableHall;

public class Die extends L2GameServerPacket
{
	private final int _charObjId;
	private boolean _canTeleport;
	private boolean _sweepable;
	private L2AccessLevel _access = AdminTable.getInstance().getAccessLevel(0);
	private L2Clan _clan;
	private final L2Character _activeChar;
	private boolean _isJailed;
	
	/**
	 * @param cha
	 */
	public Die(L2Character cha)
	{
		_activeChar = cha;
		if (cha.isPlayer())
		{
			L2PcInstance player = (L2PcInstance) cha;
			_access = player.getAccessLevel();
			_clan = player.getClan();
			_isJailed = player.isInJail();
			
		}
		_charObjId = cha.getObjectId();
		_canTeleport = !((cha.isPlayer() && ((TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(_charObjId)) || (TvTRoundEvent.isStarted() && TvTRoundEvent.isPlayerParticipant(_charObjId)))) || cha.isPendingRevive());
		
		if (cha instanceof L2PcInstance)
		{
			if (EventsInterface.isParticipating(cha.getObjectId()))
			{
				_canTeleport = false;
			}
		}
		
		if (cha.isL2Attackable())
		{
			_sweepable = ((L2Attackable) cha).isSweepActive();
		}
		
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x00);
		writeD(_charObjId);
		writeD(_canTeleport ? 0x01 : 0);
		if (_canTeleport && (_clan != null) && !_isJailed)
		{
			boolean isInCastleDefense = false;
			boolean isInFortDefense = false;
			
			L2SiegeClan siegeClan = null;
			Castle castle = CastleManager.getInstance().getCastle(_activeChar);
			Fort fort = FortManager.getInstance().getFort(_activeChar);
			SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(_activeChar);
			if ((castle != null) && castle.getSiege().getIsInProgress())
			{
				// siege in progress
				siegeClan = castle.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && castle.getSiege().checkIsDefender(_clan))
				{
					isInCastleDefense = true;
				}
			}
			else if ((fort != null) && fort.getSiege().getIsInProgress())
			{
				// siege in progress
				siegeClan = fort.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && fort.getSiege().checkIsDefender(_clan))
				{
					isInFortDefense = true;
				}
			}
			
			writeD(_clan.getHideoutId() > 0 ? 0x01 : 0x00); // 6d 01 00 00 00 - to hide away
			writeD((_clan.getCastleId() > 0) || isInCastleDefense ? 0x01 : 0x00); // 6d 02 00 00 00 - to castle
			writeD((TerritoryWarManager.getInstance().getFlagForClan(_clan) != null) || ((siegeClan != null) && !isInCastleDefense && !isInFortDefense && !siegeClan.getFlag().isEmpty()) || ((hall != null) && hall.getSiege().checkIsAttacker(_clan)) ? 0x01 : 0x00); // 6d 03 00 00 00 - to siege HQ
			writeD(_sweepable ? 0x01 : 0x00); // sweepable (blue glow)
			writeD(_access.allowFixedRes() ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
			writeD((_clan.getFortId() > 0) || isInFortDefense ? 0x01 : 0x00); // 6d 05 00 00 00 - to fortress
		}
		else
		{
			writeD(0x00); // 6d 01 00 00 00 - to hide away
			writeD(0x00); // 6d 02 00 00 00 - to castle
			writeD(0x00); // 6d 03 00 00 00 - to siege HQ
			writeD(_sweepable ? 0x01 : 0x00); // sweepable (blue glow)
			writeD(_access.allowFixedRes() ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
			writeD(0x00); // 6d 05 00 00 00 - to fortress
		}
		// TODO: protocol 152
		//@formatter:off
		/*
		 * writeC(0); //show die animation 
		 * writeD(0); //agathion ress button 
		 * writeD(0); //additional free space
		 */
		//@formatter:on
	}
}
