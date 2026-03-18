package fidas.server.gameserver.events.container;

import javolution.util.FastMap;
import fidas.server.gameserver.events.model.EventNpc;

public class NpcContainer
{
	private static class SingletonHolder
	{
		protected static final NpcContainer _instance = new NpcContainer();
	}
	
	public static final NpcContainer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final FastMap<Integer, EventNpc> npcs;
	
	public NpcContainer()
	{
		npcs = new FastMap<>();
	}
	
	public EventNpc createNpc(int x, int y, int z, int npcId, int instance)
	{
		EventNpc npci = new EventNpc(x, y, z, npcId, instance);
		npcs.put(npci.getId(), npci);
		return npci;
	}
	
	public void deleteNpc(EventNpc npc)
	{
		npcs.remove(npc.getId());
	}
	
	public EventNpc getNpc(Integer id)
	{
		return npcs.get(id);
	}
}