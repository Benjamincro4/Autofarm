package fidas.server.gameserver.network.clientpackets;

import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.network.serverpackets.ExBrProductInfo;

/**
 * Author: VISTALL
 * Company: J Develop Station
 * Date:  13:19:12/25.04.2010
 */
public class RequestBrProductInfo  extends L2GameClientPacket
{
	private static final String TYPE = "[C] D0:8B RequestBrProductInfo";

	private int _productId;

	@Override
	protected void readImpl()
	{
	 	_productId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player= getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		player.sendPacket(new ExBrProductInfo(_productId));
	}

	@Override
	public String getType()
	{
		return TYPE;
	}
}
