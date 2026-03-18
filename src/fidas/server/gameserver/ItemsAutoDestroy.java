package fidas.server.gameserver;

import java.util.List;

import javolution.util.FastList;

import fidas.server.Config;
import fidas.server.gameserver.instancemanager.ItemsOnGroundManager;
import fidas.server.gameserver.model.L2World;
import fidas.server.gameserver.model.items.instance.L2ItemInstance;
import fidas.server.gameserver.model.items.type.L2EtcItemType;

public class ItemsAutoDestroy
{
	protected List<L2ItemInstance> _items = null;
	protected static long _sleep;
	
	protected ItemsAutoDestroy()
	{
		_items = new FastList<>();
		_sleep = Config.AUTODESTROY_ITEM_AFTER * 1000;
		if (_sleep == 0)
		{
			_sleep = 3600000;
		}
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckItemsForDestroy(), 5000, 5000);
	}
	
	public static ItemsAutoDestroy getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public synchronized void addItem(L2ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}
	
	public synchronized void removeItems()
	{
		if (_items.isEmpty())
		{
			return;
		}
		
		long curtime = System.currentTimeMillis();
		for (L2ItemInstance item : _items)
		{
			if ((item == null) || (item.getDropTime() == 0) || (item.getLocation() != L2ItemInstance.ItemLocation.VOID))
			{
				_items.remove(item);
			}
			else
			{
				if (item.getItem().getAutoDestroyTime() > 0)
				{
					if ((curtime - item.getDropTime()) > item.getItem().getAutoDestroyTime())
					{
						L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						L2World.getInstance().removeObject(item);
						_items.remove(item);
						if (Config.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}
				else if (item.getItemType() == L2EtcItemType.HERB)
				{
					if ((curtime - item.getDropTime()) > Config.HERB_AUTO_DESTROY_TIME)
					{
						L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						L2World.getInstance().removeObject(item);
						_items.remove(item);
						if (Config.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}
				else if ((curtime - item.getDropTime()) > _sleep)
				{
					L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
					L2World.getInstance().removeObject(item);
					_items.remove(item);
					if (Config.SAVE_DROPPED_ITEM)
					{
						ItemsOnGroundManager.getInstance().removeObject(item);
					}
				}
			}
		}
	}
	
	protected class CheckItemsForDestroy extends Thread
	{
		@Override
		public void run()
		{
			removeItems();
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsAutoDestroy _instance = new ItemsAutoDestroy();
	}
}