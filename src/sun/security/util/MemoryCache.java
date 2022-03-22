package sun.security.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class MemoryCache extends Cache
{
  private static final float LOAD_FACTOR = 0.75F;
  private static final boolean DEBUG = 0;
  private final Map cacheMap;
  private final int maxSize;
  private final int lifetime;
  private final ReferenceQueue queue;

  public MemoryCache(boolean paramBoolean, int paramInt)
  {
    this(paramBoolean, paramInt, 0);
  }

  public MemoryCache(boolean paramBoolean, int paramInt1, int paramInt2)
  {
    this.maxSize = paramInt1;
    this.lifetime = (paramInt2 * 1000);
    this.queue = ((paramBoolean) ? new ReferenceQueue() : null);
    int i = (int)(paramInt1 / 0.75F) + 1;
    this.cacheMap = new LinkedHashMap(i, 0.75F, true);
  }

  private void emptyQueue()
  {
    if (this.queue == null)
      return;
    int i = this.cacheMap.size();
    while (true)
    {
      CacheEntry localCacheEntry1;
      Object localObject;
      while (true)
      {
        localCacheEntry1 = (CacheEntry)this.queue.poll();
        if (localCacheEntry1 == null)
          return;
        localObject = localCacheEntry1.getKey();
        if (localObject != null)
          break;
      }
      CacheEntry localCacheEntry2 = (CacheEntry)this.cacheMap.remove(localObject);
      if ((localCacheEntry2 != null) && (localCacheEntry1 != localCacheEntry2))
        this.cacheMap.put(localObject, localCacheEntry2);
    }
  }

  private void expungeExpiredEntries()
  {
    emptyQueue();
    if (this.lifetime == 0)
      return;
    int i = 0;
    long l = System.currentTimeMillis();
    Iterator localIterator = this.cacheMap.values().iterator();
    while (localIterator.hasNext())
    {
      CacheEntry localCacheEntry = (CacheEntry)localIterator.next();
      if (!(localCacheEntry.isValid(l)))
      {
        localIterator.remove();
        ++i;
      }
    }
  }

  public synchronized int size()
  {
    expungeExpiredEntries();
    return this.cacheMap.size();
  }

  public synchronized void clear()
  {
    if (this.queue != null)
    {
      Iterator localIterator = this.cacheMap.values().iterator();
      while (localIterator.hasNext())
      {
        CacheEntry localCacheEntry = (CacheEntry)localIterator.next();
        localCacheEntry.invalidate();
      }
      while (this.queue.poll() != null);
    }
    this.cacheMap.clear();
  }

  public synchronized void put(Object paramObject1, Object paramObject2)
  {
    emptyQueue();
    long l = (this.lifetime == 0) ? 3412047737930121216L : System.currentTimeMillis() + this.lifetime;
    CacheEntry localCacheEntry1 = newEntry(paramObject1, paramObject2, l, this.queue);
    CacheEntry localCacheEntry2 = (CacheEntry)this.cacheMap.put(paramObject1, localCacheEntry1);
    if (localCacheEntry2 != null)
    {
      localCacheEntry2.invalidate();
      return;
    }
    if (this.cacheMap.size() > this.maxSize)
    {
      expungeExpiredEntries();
      if (this.cacheMap.size() > this.maxSize)
      {
        Iterator localIterator = this.cacheMap.values().iterator();
        CacheEntry localCacheEntry3 = (CacheEntry)localIterator.next();
        localIterator.remove();
        localCacheEntry3.invalidate();
      }
    }
  }

  public synchronized Object get(Object paramObject)
  {
    emptyQueue();
    CacheEntry localCacheEntry = (CacheEntry)this.cacheMap.get(paramObject);
    if (localCacheEntry == null)
      return null;
    long l = (this.lifetime == 0) ? 3412047737930121216L : System.currentTimeMillis();
    if (!(localCacheEntry.isValid(l)))
    {
      this.cacheMap.remove(paramObject);
      return null;
    }
    return localCacheEntry.getValue();
  }

  public synchronized void remove(Object paramObject)
  {
    emptyQueue();
    CacheEntry localCacheEntry = (CacheEntry)this.cacheMap.remove(paramObject);
    if (localCacheEntry != null)
      localCacheEntry.invalidate();
  }

  protected CacheEntry newEntry(Object paramObject1, Object paramObject2, long paramLong, ReferenceQueue paramReferenceQueue)
  {
    if (paramReferenceQueue != null)
      return new SoftCacheEntry(paramObject1, paramObject2, paramLong, paramReferenceQueue);
    return new HardCacheEntry(paramObject1, paramObject2, paramLong);
  }

  private static abstract interface CacheEntry
  {
    public abstract boolean isValid(long paramLong);

    public abstract void invalidate();

    public abstract Object getKey();

    public abstract Object getValue();
  }

  private static class HardCacheEntry
  implements MemoryCache.CacheEntry
  {
    private Object key;
    private Object value;
    private long expirationTime;

    HardCacheEntry(Object paramObject1, Object paramObject2, long paramLong)
    {
      this.key = paramObject1;
      this.value = paramObject2;
      this.expirationTime = paramLong;
    }

    public Object getKey()
    {
      return this.key;
    }

    public Object getValue()
    {
      return this.value;
    }

    public boolean isValid(long paramLong)
    {
      int i = (paramLong <= this.expirationTime) ? 1 : 0;
      if (i == 0)
        invalidate();
      return i;
    }

    public void invalidate()
    {
      this.key = null;
      this.value = null;
      this.expirationTime = -1L;
    }
  }

  private static class SoftCacheEntry extends SoftReference
  implements MemoryCache.CacheEntry
  {
    private Object key;
    private long expirationTime;

    SoftCacheEntry(Object paramObject1, Object paramObject2, long paramLong, ReferenceQueue paramReferenceQueue)
    {
      super(paramObject2, paramReferenceQueue);
      this.key = paramObject1;
      this.expirationTime = paramLong;
    }

    public Object getKey()
    {
      return this.key;
    }

    public Object getValue()
    {
      return get();
    }

    public boolean isValid(long paramLong)
    {
      int i = ((paramLong <= this.expirationTime) && (get() != null)) ? 1 : 0;
      if (i == 0)
        invalidate();
      return i;
    }

    public void invalidate()
    {
      clear();
      this.key = null;
      this.expirationTime = -1L;
    }
  }
}