package fidas.server.gameserver.idfactory;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

import fidas.server.gameserver.ThreadPoolManager;
import fidas.server.util.PrimeFinder;

public class BitSetIDFactory extends IdFactory
{
	private BitSet _freeIds;
	private AtomicInteger _freeIdCount;
	private AtomicInteger _nextFreeId;
	
	protected class BitSetCapacityCheck implements Runnable
	{
		@Override
		public void run()
		{
			synchronized (BitSetIDFactory.this)
			{
				if (reachingBitSetCapacity())
				{
					increaseBitSetCapacity();
				}
			}
		}
	}
	
	protected BitSetIDFactory()
	{
		super();
		
		synchronized (BitSetIDFactory.class)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BitSetCapacityCheck(), 30000, 30000);
			initialize();
		}
		_log.info(getClass().getSimpleName() + ": " + _freeIds.size() + " id's available.");
	}
	
	public void initialize()
	{
		try
		{
			_freeIds = new BitSet(PrimeFinder.nextPrime(100000));
			_freeIds.clear();
			_freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);
			
			for (int usedObjectId : extractUsedObjectIDTable())
			{
				int objectID = usedObjectId - FIRST_OID;
				if (objectID < 0)
				{
					_log.warning(getClass().getSimpleName() + ": Object ID " + usedObjectId + " in DB is less than minimum ID of " + FIRST_OID);
					continue;
				}
				_freeIds.set(usedObjectId - FIRST_OID);
				_freeIdCount.decrementAndGet();
			}
			
			_nextFreeId = new AtomicInteger(_freeIds.nextClearBit(0));
			_initialized = true;
		}
		catch (Exception e)
		{
			_initialized = false;
			_log.severe(getClass().getSimpleName() + ": Could not be initialized properly: " + e.getMessage());
		}
	}
	
	@Override
	public synchronized void releaseId(int objectID)
	{
		if ((objectID - FIRST_OID) > -1)
		{
			_freeIds.clear(objectID - FIRST_OID);
			_freeIdCount.incrementAndGet();
		}
		else
		{
			_log.warning(getClass().getSimpleName() + ": Release objectID " + objectID + " failed (< " + FIRST_OID + ")");
		}
	}
	
	@Override
	public synchronized int getNextId()
	{
		int newID = _nextFreeId.get();
		_freeIds.set(newID);
		_freeIdCount.decrementAndGet();
		
		int nextFree = _freeIds.nextClearBit(newID);
		
		if (nextFree < 0)
		{
			nextFree = _freeIds.nextClearBit(0);
		}
		if (nextFree < 0)
		{
			if (_freeIds.size() < FREE_OBJECT_ID_SIZE)
			{
				increaseBitSetCapacity();
			}
			else
			{
				throw new NullPointerException("Ran out of valid Id's.");
			}
		}
		
		_nextFreeId.set(nextFree);
		
		return newID + FIRST_OID;
	}
	
	@Override
	public synchronized int size()
	{
		return _freeIdCount.get();
	}
	
	/**
	 * @return
	 */
	protected synchronized int usedIdCount()
	{
		return (size() - FIRST_OID);
	}
	
	/**
	 * @return
	 */
	protected synchronized boolean reachingBitSetCapacity()
	{
		return PrimeFinder.nextPrime((usedIdCount() * 11) / 10) > _freeIds.size();
	}
	
	protected synchronized void increaseBitSetCapacity()
	{
		BitSet newBitSet = new BitSet(PrimeFinder.nextPrime((usedIdCount() * 11) / 10));
		newBitSet.or(_freeIds);
		_freeIds = newBitSet;
	}
}