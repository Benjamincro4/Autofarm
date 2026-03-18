package fidas.server.gameserver.scripts.zone;

import java.util.HashMap;
import java.util.Map;

import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.gameserver.datatables.SkillTable;
import fidas.server.gameserver.instancemanager.ZoneManager;
import fidas.server.gameserver.model.L2Party;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.base.ClassId;
import fidas.server.gameserver.model.skills.L2Skill;
import fidas.server.gameserver.model.zone.type.L2ScriptZone;
import fidas.server.gameserver.scripts.data.AbstractNpcAI1;

public class DragonValleyZone extends AbstractNpcAI1
{
    public static final Map<ClassId, Double> weight = new HashMap<>();
    public static final L2ScriptZone zone = ZoneManager.getInstance().getZoneById(80005, L2ScriptZone.class);

    static
    {
        weight.put(ClassId.duelist, 0.2);
        weight.put(ClassId.dreadnought, 0.7);
        weight.put(ClassId.phoenixKnight, 0.5);
        weight.put(ClassId.hellKnight, 0.5);
        weight.put(ClassId.sagittarius, 0.3);
        weight.put(ClassId.adventurer, 0.4);
        weight.put(ClassId.archmage, 0.3);
        weight.put(ClassId.soultaker, 0.3);
        weight.put(ClassId.arcanaLord, 1.);
        weight.put(ClassId.cardinal, -0.6);
        weight.put(ClassId.hierophant, 0.);
        weight.put(ClassId.evaTemplar, 0.8);
        weight.put(ClassId.swordMuse, 0.5);
        weight.put(ClassId.windRider, 0.4);
        weight.put(ClassId.moonlightSentinel, 0.3);
        weight.put(ClassId.mysticMuse, 0.3);
        weight.put(ClassId.elementalMaster, 1.);
        weight.put(ClassId.evaSaint, -0.6);
        weight.put(ClassId.shillienTemplar, 0.8);
        weight.put(ClassId.spectralDancer, 0.5);
        weight.put(ClassId.ghostHunter, 0.4);
        weight.put(ClassId.ghostSentinel, 0.3);
        weight.put(ClassId.stormScreamer, 0.3);
        weight.put(ClassId.spectralMaster, 1.);
        weight.put(ClassId.shillienSaint, -0.6);
        weight.put(ClassId.titan, 0.3);
        weight.put(ClassId.dominator, 0.1);
        weight.put(ClassId.grandKhavatari, 0.2);
        weight.put(ClassId.doomcryer, 0.1);
        weight.put(ClassId.fortuneSeeker, 0.9);
        weight.put(ClassId.maestro, 0.7);
        weight.put(ClassId.doombringer, 0.2);
        weight.put(ClassId.trickster, 0.5);
        weight.put(ClassId.judicator, 0.1);
        weight.put(ClassId.maleSoulhound, 0.3);
        weight.put(ClassId.femaleSoulhound, 0.3);
    }

    public class BuffTask implements Runnable
    {
        @Override
        public void run()
        {
            for (L2PcInstance pc : zone.getPlayersInside())
            {
                if (getBuffLevel(pc) > 0)
                {
                    L2Skill skill = SkillTable.getInstance().getInfo(6885, getBuffLevel(pc));
                    //skill.getEffects(pc, pc); desativado para teste
                }
            }
        }
    }
    public int getBuffLevel(L2PcInstance player)
    {
        if (!player.isInParty())
        {
            return 0;
        }

        L2Party party = player.getParty();
        if (party.getMemberCount() < 5)
        {
            return 0;
        }

        for (L2PcInstance p : party.getMembers())
        {
            if (p.getLevel() < 80)
            {
                return 0;
            }
        }

        double points = 0;
        int count = party.getMemberCount();

        for (L2PcInstance p : party.getMembers())
        {
            points += weight.get(p.getClassId());
        }

        return (int) Math.max(0, Math.min(3, Math.round(points * getCoefficient(count)))); // Brutally custom
    }

    private double getCoefficient(int count)
    {
        double cf;

        switch (count)
        {
            case 4:
                cf = 0.7;
                break;
            case 5:
                cf = 0.75;
                break;
            case 6:
                cf = 0.8;
                break;
            case 7:
                cf = 0.85;
                break;
            case 8:
                cf = 0.9;
                break;
            case 9:
                cf = 0.95;
                break;
            default:
                cf = 1;
        }
        return cf;
    }

    public DragonValleyZone(int questId, String name, String descr)
    {
        super(descr, descr);

        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BuffTask(), 1000, 10000);
    }

    public static void main(String[] args)
    {
        new DragonValleyZone(-1, DragonValleyZone.class.getSimpleName(), "ai");
    }
}