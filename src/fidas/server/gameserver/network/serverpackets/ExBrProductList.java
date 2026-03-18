package fidas.server.gameserver.network.serverpackets;

import fidas.server.gameserver.datatables.ProductItemTable;
import fidas.server.gameserver.model.L2ProductItem;

import java.util.Collection;

/**
 * Author: VISTALL
 * Company: J Develop Station
 * Date:  1:50:38/10.04.2010
 */
public class ExBrProductList extends L2GameServerPacket
{
	private static final String TYPE = "[S] FE:D6 ExBrProductList";

	@Override
	protected void writeImpl()
	{
   		writeC(0xFE);
		writeH(0xD6);
		Collection<L2ProductItem> items = ProductItemTable.getInstance().getAllItems();
		writeD(items.size());

		for (L2ProductItem template : items)
		{
            writeD(template.getProductId());
            writeH(template.getCategory());
            writeH(template.getPoints());
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeC(0);
            writeH(0);
        }
    }

    public String getType()
    {
		return TYPE;
    }
}
