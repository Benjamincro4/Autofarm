package fidas.server.gameserver.events.container;

import java.util.Random;

import javolution.util.FastList;
import javolution.util.FastMap;
import fidas.server.gameserver.events.AbstractEvent;
import fidas.server.gameserver.events.Config;
import fidas.server.gameserver.events.events.Battlefield;
import fidas.server.gameserver.events.events.Bomb;
import fidas.server.gameserver.events.events.CTF;
import fidas.server.gameserver.events.events.DM;
import fidas.server.gameserver.events.events.Domination;
import fidas.server.gameserver.events.events.DoubleDomination;
import fidas.server.gameserver.events.events.LMS;
import fidas.server.gameserver.events.events.Lucky;
import fidas.server.gameserver.events.events.Mutant;
import fidas.server.gameserver.events.events.Russian;
import fidas.server.gameserver.events.events.Simon;
import fidas.server.gameserver.events.events.TvT;
import fidas.server.gameserver.events.events.VIPTvT;
import fidas.server.gameserver.events.events.Zombie;

public class EventContainer
{
	
	private static class SingletonHolder
	{
		private static final EventContainer _instance = new EventContainer();
	}
	
	@SuppressWarnings("synthetic-access")
	public static EventContainer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected Random rnd = new Random();
	
	private FastMap<Integer, AbstractEvent> events;
	
	public FastList<Integer> eventIds;
	
	public EventContainer()
	{
		eventIds = new FastList<>();
		events = new FastMap<>();
		
		if (DM.enabled && Config.getInstance().getBoolean(0, "eventEnabled_1"))
			eventIds.add(1);
		if (Domination.enabled && Config.getInstance().getBoolean(0, "eventEnabled_2"))
			eventIds.add(2);
		if (DoubleDomination.enabled && Config.getInstance().getBoolean(0, "eventEnabled_3"))
			eventIds.add(3);
		if (LMS.enabled && Config.getInstance().getBoolean(0, "eventEnabled_4"))
			eventIds.add(4);
		if (Lucky.enabled && Config.getInstance().getBoolean(0, "eventEnabled_5"))
			eventIds.add(5);
		if (Simon.enabled && Config.getInstance().getBoolean(0, "eventEnabled_6"))
			eventIds.add(6);
		if (TvT.enabled && Config.getInstance().getBoolean(0, "eventEnabled_7"))
			eventIds.add(7);
		if (VIPTvT.enabled && Config.getInstance().getBoolean(0, "eventEnabled_8"))
			eventIds.add(8);
		if (Zombie.enabled && Config.getInstance().getBoolean(0, "eventEnabled_9"))
			eventIds.add(9);
		if (CTF.enabled && Config.getInstance().getBoolean(0, "eventEnabled_10"))
			eventIds.add(10);
		if (Russian.enabled && Config.getInstance().getBoolean(0, "eventEnabled_11"))
			eventIds.add(11);
		if (Bomb.enabled && Config.getInstance().getBoolean(0, "eventEnabled_12"))
			eventIds.add(12);
		if (Mutant.enabled && Config.getInstance().getBoolean(0, "eventEnabled_13"))
			eventIds.add(13);
		if (Battlefield.enabled && Config.getInstance().getBoolean(0, "eventEnabled_14"))
			eventIds.add(14);
		
	}
	
	public AbstractEvent createEvent(int id)
	{
		if (!eventIds.contains(id))
			return null;
		
		for (AbstractEvent event : events.values())
			if (event.eventId == id)
				return null;
		
		switch (id)
		{
			case 1:
				events.put(events.size() + 1, new DM(events.size() + 1));
				break;
			case 2:
				events.put(events.size() + 1, new Domination(events.size() + 1));
				break;
			case 3:
				events.put(events.size() + 1, new DoubleDomination(events.size() + 1));
				break;
			case 4:
				events.put(events.size() + 1, new LMS(events.size() + 1));
				break;
			case 5:
				events.put(events.size() + 1, new Lucky(events.size() + 1));
				break;
			case 6:
				events.put(events.size() + 1, new Simon(events.size() + 1));
				break;
			case 7:
				events.put(events.size() + 1, new TvT(events.size() + 1));
				break;
			case 8:
				events.put(events.size() + 1, new VIPTvT(events.size() + 1));
				break;
			case 9:
				events.put(events.size() + 1, new Zombie(events.size() + 1));
				break;
			case 10:
				events.put(events.size() + 1, new CTF(events.size() + 1));
				break;
			case 11:
				events.put(events.size() + 1, new Russian(events.size() + 1));
				break;
			case 12:
				events.put(events.size() + 1, new Bomb(events.size() + 1));
				break;
			case 13:
				events.put(events.size() + 1, new Mutant(events.size() + 1));
				break;
			case 14:
				events.put(events.size() + 1, new Battlefield(events.size() + 1));
				break;
		}
		
		events.get(events.size()).createStatus();
		
		return events.get(events.size());
	}
	
	public AbstractEvent createRandomEvent()
	{
		return createEvent(eventIds.get(rnd.nextInt(eventIds.size())));
	}
	
	public AbstractEvent getEvent(Integer id)
	{
		return events.get(id);
	}
	
	public FastMap<Integer, AbstractEvent> getEventMap()
	{
		return events;
	}
	
	protected FastList<String> getEventNames()
	{
		FastList<String> map = new FastList<>();
		for (AbstractEvent event : events.values())
		{
			map.add(Config.getInstance().getString(event.getId(), "eventName"));
		}
		return map;
	}
	
	protected int numberOfEvents()
	{
		return events.size();
	}
	
	public void removeEvent(Integer id)
	{
		events.remove(id);
	}
	
}