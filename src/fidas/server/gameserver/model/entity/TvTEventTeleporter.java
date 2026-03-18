package fidas.server.gameserver.model.entity;

import fidas.server.Config;
import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.model.actor.L2Summon;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.util.Rnd;

public class TvTEventTeleporter implements Runnable
{
	/** The instance of the player to teleport */
	private L2PcInstance _playerInstance = null;
	/** Coordinates of the spot to teleport to */
	private int[] _coordinates = new int[3];
	/** Admin removed this player from event */
	private boolean _adminRemove = false;
	
	/**
	 * Initialize the teleporter and start the delayed task.
	 * @param playerInstance
	 * @param coordinates
	 * @param fastSchedule
	 * @param adminRemove
	 */
	public TvTEventTeleporter(L2PcInstance playerInstance, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;
		_adminRemove = adminRemove;
		
		long delay = (TvTEvent.isStarted() ? Config.TVT_EVENT_RESPAWN_TELEPORT_DELAY : Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;
		
		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}
	
	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one<br>
	 * 2. Remove all effects<br>
	 * 3. Revive and full heal the player<br>
	 * 4. Teleport the player<br>
	 * 5. Broadcast status and user info
	 */
	@Override
	public void run()
	{
		if (_playerInstance == null)
		{
			return;
		}
		
		L2Summon summon = _playerInstance.getSummon();
		
		if (summon != null)
		{
			summon.unSummon(_playerInstance);
		}
		
		if ((Config.TVT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TVT_EVENT_EFFECTS_REMOVAL == 1) && ((_playerInstance.getTeam() == 0) || (_playerInstance.isInDuel() && (_playerInstance.getDuelState() != Duel.DUELSTATE_INTERRUPTED)))))
		{
			_playerInstance.stopAllEffectsExceptThoseThatLastThroughDeath();
			if (_playerInstance.getSummon() != null)
			{
				L2Summon pet = _playerInstance.getSummon();
				pet.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
		}
		
		if (_playerInstance.isInDuel())
		{
			_playerInstance.setDuelState(Duel.DUELSTATE_INTERRUPTED);
		}
		
		int TvTInstance = TvTEvent.getTvTEventInstance();
		if (TvTInstance != 0)
		{
			if (TvTEvent.isStarted() && !_adminRemove)
			{
				_playerInstance.setInstanceId(TvTInstance);
			}
			else
			{
				_playerInstance.setInstanceId(0);
			}
		}
		else
		{
			_playerInstance.setInstanceId(0);
		}
		
		_playerInstance.doRevive();
		
		_playerInstance.teleToLocation((_coordinates[0] + Rnd.get(101)) - 50, (_coordinates[1] + Rnd.get(101)) - 50, _coordinates[2], false);
		
		if (TvTEvent.isStarted() && !_adminRemove)
		{
			_playerInstance.setTeam(TvTEvent.getParticipantTeamId(_playerInstance.getObjectId()) + 1);
		}
		else
		{
			_playerInstance.setTeam(0);
		}
		
		_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
		_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
		_playerInstance.setCurrentMp(_playerInstance.getMaxMp());
		
		_playerInstance.broadcastStatusUpdate();
		_playerInstance.broadcastUserInfo();
	}
}