package fidas.server.gameserver.autofarm;

public enum EMoveType
{
	Not_Set,
	Follow_Target,
	Current_Location,   // transient – immediately converts to Saved_Location
	Saved_Location;

	@Override
	public String toString()
	{
		return name().replace('_', ' ');
	}
}
