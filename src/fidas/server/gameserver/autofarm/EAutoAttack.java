package fidas.server.gameserver.autofarm;

public enum EAutoAttack
{
	Never,
	Always,
	Skills_Reuse;

	@Override
	public String toString()
	{
		return name().replace('_', ' ');
	}
}
