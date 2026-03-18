package fidas.server.gameserver.network.clientpackets;

import fidas.server.Config;
import fidas.server.gameserver.datatables.ItemTable;
import fidas.server.gameserver.datatables.ProductItemTable;
import fidas.server.gameserver.model.L2ProductItem;
import fidas.server.gameserver.model.L2ProductItemComponent;
import fidas.server.gameserver.model.actor.instance.L2PcInstance;
import fidas.server.gameserver.model.items.L2Item;
import fidas.server.gameserver.network.serverpackets.ExBrBuyProduct;
import fidas.server.gameserver.network.serverpackets.ExBrGamePoint;
import fidas.server.gameserver.network.serverpackets.StatusUpdate;

/**
 * Author: VISTALL
 * Company: J Develop Station
 * Date:  2:57:47/10.04.2010
 */
public class RequestBrBuyProduct  extends L2GameClientPacket
{
	private static final String TYPE = "[C] D0 8C RequestBrBuyProduct";

	private int _productId;
	private int _count;

	@Override
	protected void readImpl()
	{
		_productId = readD();
		_count = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_count > 99 || _count < 0)
		{
			return;
		}

		L2ProductItem product = ProductItemTable.getInstance().getProduct(_productId);
		if(product == null)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}

		long totalPoints = product.getPoints() * _count;

		if(totalPoints < 0)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}

		final long gamePointSize = Config.GAME_POINT_ITEM_ID == -1 ? player.getGamePoints() :  player.getInventory().getInventoryItemCount(Config.GAME_POINT_ITEM_ID, -1);

		if(totalPoints > gamePointSize)
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_NOT_ENOUGH_POINTS));
			return;
		}

		int totalWeight = 0;
		for(L2ProductItemComponent com : product.getComponents())
		{
			totalWeight += com.getWeight();
		}
		totalWeight *= _count; 

		int totalCount = 0;

		for(L2ProductItemComponent com : product.getComponents())
		{
			L2Item item = ItemTable.getInstance().getTemplate(com.getItemId());
			if(item == null)
			{
				player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_WRONG_PRODUCT));
				return; //what
			}
			totalCount += item.isStackable() ? 1 : com.getCount() * _count;
		}

		if(!player.getInventory().validateCapacity(totalCount) || !player.getInventory().validateWeight(totalWeight))
		{
			player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_INVENTORY_FULL));
			return;
		}

		for (L2ProductItemComponent $comp : product.getComponents())
		{
			player.getInventory().addItem("Buy Product" + _productId, $comp.getItemId(), $comp.getCount() * _count, player, null);
		}

		if(Config.GAME_POINT_ITEM_ID == -1)
		{
			player.setGamePoints(player.getGamePoints() - totalPoints);
		}
		else
		{
			player.getInventory().destroyItemByItemId("Buy Product" + _productId, Config.GAME_POINT_ITEM_ID, totalPoints, player, null);	
		}

		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		player.sendPacket(new ExBrGamePoint(player));
		player.sendPacket(new ExBrBuyProduct(ExBrBuyProduct.RESULT_OK));

		//player.sendPacket(new ItemList(player, true));
	}

	@Override
	public String getType()
	{
		return TYPE;
	}
}
