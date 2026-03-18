package fidas.server.gameserver.model;

import fidas.server.gameserver.datatables.ItemTable;
import fidas.server.gameserver.model.items.L2Item;

/**
 * Author: VISTALL
 * Company: ihgap
 * Date:  11:53:22/25.04.2010
 */
public class L2ProductItemComponent
{
	private final int _itemId;
	private final int _count;

	private final int _weight;
	private final boolean _dropable;

	public L2ProductItemComponent(int item_id, int count)
	{
	 	_itemId = item_id;
		_count = count;

		L2Item item = ItemTable.getInstance().getTemplate(item_id);
		if(item != null)
		{
			_weight = item.getWeight();
			_dropable = item.isDropable();
		}
		else
		{
			//FIX ME what the mother facker???
			_weight = 0;
			_dropable = true;
		}
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getCount()
	{
		return _count;
	}

	public int getWeight()
	{
		return _weight;
	}

	public boolean isDropable()
	{
		return _dropable;
	}
}
