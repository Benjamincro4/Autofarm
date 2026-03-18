package fidas.server.gameserver.dressme;

public class DressMeArmorSet
{
    private final int _id;
    private final String _name;
    private final int _chest, _legs, _gloves, _boots, _head;
    private final boolean _premium;

    public DressMeArmorSet(int id, String name, int chest, int legs, int gloves, int boots, int head, boolean premium)
    {
        _id = id;
        _name = name;
        _chest = chest;
        _legs = legs;
        _gloves = gloves;
        _boots = boots;
        _head = head;
        _premium = premium;
    }

    public int getId() { return _id; }
    public String getName() { return _name; }

    public int getChest() { return _chest; }
    public int getLegs() { return _legs; }
    public int getGloves() { return _gloves; }
    public int getBoots() { return _boots; }
    public int getHead() { return _head; }

    public boolean isPremium() { return _premium; }
}
