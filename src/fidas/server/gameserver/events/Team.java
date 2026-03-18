package fidas.server.gameserver.events;

public class Team implements Comparable<Team>
{
	private int score;
	
	private final String name;
	
	private final int[] nameColor;
	
	private final int[] startPos;
	
	private final int id;
	
	public Team(int id, String name, int[] color, int[] startPos)
	{
		this.id = id;
		score = 0;
		this.name = name;
		nameColor = color;
		this.startPos = startPos;
	}
	
	@Override
	public int compareTo(Team second)
	{
		
		if (getScore() > second.getScore())
		{
			return 1;
		}
		if (getScore() < second.getScore())
		{
			return -1;
		}
		if (getScore() == second.getScore())
		{
			return 0;
		}
		
		return 0;
		
	}
	
	public String getHexaColor()
	{
		String hexa;
		Integer i1 = nameColor[0];
		Integer i2 = nameColor[1];
		Integer i3 = nameColor[2];
		hexa = "" + (i1 > 15 ? Integer.toHexString(i1) : "0" + Integer.toHexString(i1)) + (i2 > 15 ? Integer.toHexString(i2) : "0" + Integer.toHexString(i2)) + (i3 > 15 ? Integer.toHexString(i3) : "0" + Integer.toHexString(i3));
		return hexa;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getScore()
	{
		return score;
	}
	
	public int[] getTeamColor()
	{
		return nameColor;
	}
	
	public int[] getTeamPos()
	{
		return startPos;
	}
	
	public void increaseScore()
	{
		score++;
	}
	
	public void increaseScore(int ammount)
	{
		score += ammount;
	}
	
	public void setScore(int ammount)
	{
		score = ammount;
	}
}