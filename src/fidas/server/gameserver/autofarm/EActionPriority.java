package fidas.server.gameserver.autofarm;

public enum EActionPriority
{
	Highest,
	Very_High,
	High,
	Medium,
	Low,
	Very_Low,
	Lowest,
	Remove;

	@Override
	public String toString()
	{
		return (ordinal() + 1) + " " + name().replace('_', ' ');
	}
}
