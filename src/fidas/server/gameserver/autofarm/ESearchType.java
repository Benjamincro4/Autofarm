package fidas.server.gameserver.autofarm;

public enum ESearchType
{
	Off    ("FF6363"),
	Assist ("LEVEL"),
	Close  ("63FF63"),  // 400u
	Near   ("63FF63"),  // 2000u
	Far    ("63FF63");  // 3000u

	private final String _color;

	ESearchType(final String color)
	{
		_color = color;
	}

	public String getColor()
	{
		return _color;
	}

	/** Search radius in world units. Returns -1 if disabled, 0 for Assist (no range filter). */
	public int getRange()
	{
		switch (this)
		{
			case Off:    return -1;
			case Assist: return 0;
			case Close:  return 400;
			case Near:   return 2000;
			case Far:    return 3000;
		}
		return 0;
	}
}
