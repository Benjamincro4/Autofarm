package fidas.server.gameserver.network.clientpackets;

/**
 * @author Ragnarok
 */

public class RequestBrRecentProductList extends L2GameClientPacket
{
    public RequestBrRecentProductList()
    {
    }

    @Override
   public void readImpl()
    {
    }

    @Override
   public void runImpl()
    {
    }

    @Override
   public String getType()
    {
        return "[C] d0:8D BrRecentProductList";
    }
}
