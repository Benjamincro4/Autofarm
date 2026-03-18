package fidas.server.gameserver.model.entity;

import fidas.server.gameserver.model.actor.instance.L2PcInstance;

public class PlayerToAssasinate
{
	private int _objectId;
	private int _clientId;
	private String _name;
	private int _bounty;
	private boolean _online;
	private boolean _pendingDelete;
	
	public PlayerToAssasinate(L2PcInstance target, int clientId, int bounty)
	{
		_objectId = target.getObjectId();
		_clientId = clientId;
		_name = target.getName();
		_bounty = bounty;
		_online = target.isOnline();
	}
	
	public PlayerToAssasinate(int objectId, int clientId, int bounty, String name)
	{
		_objectId = objectId;
		_clientId = clientId;
		_name = name;
		_bounty = bounty;
		_online = false;
	}
	
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public void setName(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}

	public void setBounty(int vol)
	{
		_bounty = vol;
	}
	
	public void incBountyBy(int vol)
	{
		_bounty += vol;
	}

	public void decBountyBy(int vol)
	{
		_bounty -= vol;
	}

	public int getBounty()
	{
		return _bounty;
	}

	public void setOnline(boolean online)
	{
		_online = online;
	}

	public boolean isOnline()
	{
		return _online;
	}

	public void setClientId(int clientId)
	{
		_clientId = clientId;
	}

	public int getClientId()
	{
		return _clientId;
	}

	public void setPendingDelete(boolean pendingDelete)
	{
		_pendingDelete = pendingDelete;
	}

	public boolean isPendingDelete()
	{
		return _pendingDelete;
	}
}