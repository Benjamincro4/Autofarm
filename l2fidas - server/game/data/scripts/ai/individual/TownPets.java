package ai.individual;

import fidas.server.gameserver.model.actor.L2Npc;
import fidas.server.gameserver.model.quest.Quest;

public class TownPets extends Quest
{
  private static final int[][] TOWN_PET_TABLE = { 
    { 30731, 31202 }, 
    { 30827, 31203 }, 
    { 30828, 31204 }, 
    { 30829, 31205 }, 
    { 30830, 31206 }, 
    { 30831, 31207 }, 
    { 30869, 31208 }, 
    { 31067, 31209 }, 
    { 31265, 31758 }, 
    { 31309, 31266 }, 
    { 31592, 31593 }, 
    { 31605, 31606 }, 
    { 31608, 31609 }, 
    { 31614, 31630 }, 
    { 31624, 31625 }, 
    { 31701, 31703 }, 
    { 31702, 31704 }, 
    { 31954, 31955 } };

  @Override
public String onSpawn(L2Npc npc)
  {
    for (int[] dat : TOWN_PET_TABLE)
    {
      if (dat[0] == npc.getNpcId())
        addSpawn(dat[1], npc.getX() + 10, npc.getY() + 10, npc.getZ(), npc.getHeading(), false, 0L);
    }
    return super.onSpawn(npc);
  }

  public TownPets(int questId, String name, String descr)
  {
    super(questId, name, descr);

    for (int[] npc : TOWN_PET_TABLE)
      addSpawnId(npc[0]);
  }

  public static void main(String[] args)
  {
    new TownPets(-1, "TownPets", "npc");
  }
}