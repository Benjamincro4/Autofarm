package fidas.server.gameserver.network.serverpackets;

/**
 * Author: VISTALL
 * Company: J Develop Station
 * Date:  2:02:35/10.04.2010
 * thx l2jfree
 */
public class ExBrBuyProduct extends L2GameServerPacket
{
	private static final String TYPE = "[S] FE:D8 ExBrBuyProduct";

	public static final int RESULT_OK 					= 1; // ok
	public static final int RESULT_NOT_ENOUGH_POINTS 	= -1;
	public static final int RESULT_WRONG_PRODUCT 		= -2; // also -5
	public static final int RESULT_INVENTORY_FULL 		= -4;
	public static final int RESULT_SALE_PERIOD_ENDED 	= -7; // also -8
	public static final int RESULT_WRONG_USER_STATE 	= -9; // also -11
	public static final int RESULT_WRONG_PRODUCT_ITEM 	= -10;

	private final int _result;

	public ExBrBuyProduct(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD8);
		writeD(_result);
	}

	public String getType()
	{
		return TYPE;
	}
}
