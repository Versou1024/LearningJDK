package sun.misc;

import java.util.Dictionary;
import java.util.Enumeration;

public class Cache extends Dictionary
{
  private CacheEntry[] table;
  private int count;
  private int threshold;
  private float loadFactor;

  private void init(int paramInt, float paramFloat)
  {
    if ((paramInt <= 0) || (paramFloat <= 0D))
      throw new IllegalArgumentException();
    this.loadFactor = paramFloat;
    this.table = new CacheEntry[paramInt];
    this.threshold = (int)(paramInt * paramFloat);
  }

  public Cache(int paramInt, float paramFloat)
  {
    init(paramInt, paramFloat);
  }

  public Cache(int paramInt)
  {
    init(paramInt, 0.75F);
  }

  public Cache()
  {
    try
    {
      init(101, 0.75F);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw new Error("panic");
    }
  }

  public int size()
  {
    return this.count;
  }

  public boolean isEmpty()
  {
    return (this.count == 0);
  }

  public synchronized Enumeration keys()
  {
    return new CacheEnumerator(this.table, true);
  }

  public synchronized Enumeration elements()
  {
    return new CacheEnumerator(this.table, false);
  }

  public synchronized Object get(Object paramObject)
  {
    CacheEntry[] arrayOfCacheEntry = this.table;
    int i = paramObject.hashCode();
    int j = (i & 0x7FFFFFFF) % arrayOfCacheEntry.length;
    for (CacheEntry localCacheEntry = arrayOfCacheEntry[j]; localCacheEntry != null; localCacheEntry = localCacheEntry.next)
      if ((localCacheEntry.hash == i) && (localCacheEntry.key.equals(paramObject)))
        return localCacheEntry.check();
    return null;
  }

  protected void rehash()
  {
    int i = this.table.length;
    CacheEntry[] arrayOfCacheEntry1 = this.table;
    int j = i * 2 + 1;
    CacheEntry[] arrayOfCacheEntry2 = new CacheEntry[j];
    this.threshold = (int)(j * this.loadFactor);
    this.table = arrayOfCacheEntry2;
    int k = i;
    while (k-- > 0)
    {
      CacheEntry localCacheEntry1 = arrayOfCacheEntry1[k];
      while (localCacheEntry1 != null)
      {
        CacheEntry localCacheEntry2 = localCacheEntry1;
        localCacheEntry1 = localCacheEntry1.next;
        if (localCacheEntry2.check() != null)
        {
          int l = (localCacheEntry2.hash & 0x7FFFFFFF) % j;
          localCacheEntry2.next = arrayOfCacheEntry2[l];
          arrayOfCacheEntry2[l] = localCacheEntry2;
        }
        else
        {
          this.count -= 1;
        }
      }
    }
  }

  public synchronized Object put(Object paramObject1, Object paramObject2)
  {
    if (paramObject2 == null)
      throw new NullPointerException();
    CacheEntry[] arrayOfCacheEntry = this.table;
    int i = paramObject1.hashCode();
    int j = (i & 0x7FFFFFFF) % arrayOfCacheEntry.length;
    Object localObject1 = null;
    for (CacheEntry localCacheEntry = arrayOfCacheEntry[j]; localCacheEntry != null; localCacheEntry = localCacheEntry.next)
    {
      if ((localCacheEntry.hash == i) && (localCacheEntry.key.equals(paramObject1)))
      {
        Object localObject2 = localCacheEntry.check();
        localCacheEntry.setThing(paramObject2);
        return localObject2;
      }
      if (localCacheEntry.check() == null)
        localObject1 = localCacheEntry;
    }
    if (this.count >= this.threshold)
    {
      rehash();
      return put(paramObject1, paramObject2);
    }
    if (localObject1 == null)
    {
      localObject1 = new CacheEntry();
      ((CacheEntry)localObject1).next = arrayOfCacheEntry[j];
      arrayOfCacheEntry[j] = localObject1;
      this.count += 1;
    }
    ((CacheEntry)localObject1).hash = i;
    ((CacheEntry)localObject1).key = paramObject1;
    ((CacheEntry)localObject1).setThing(paramObject2);
    return null;
  }

  public synchronized Object remove(Object paramObject)
  {
    CacheEntry[] arrayOfCacheEntry = this.table;
    int i = paramObject.hashCode();
    int j = (i & 0x7FFFFFFF) % arrayOfCacheEntry.length;
    CacheEntry localCacheEntry1 = arrayOfCacheEntry[j];
    CacheEntry localCacheEntry2 = null;
    while (localCacheEntry1 != null)
    {
      if ((localCacheEntry1.hash == i) && (localCacheEntry1.key.equals(paramObject)))
      {
        if (localCacheEntry2 != null)
          localCacheEntry2.next = localCacheEntry1.next;
        else
          arrayOfCacheEntry[j] = localCacheEntry1.next;
        this.count -= 1;
        return localCacheEntry1.check();
      }
      localCacheEntry2 = localCacheEntry1;
      localCacheEntry1 = localCacheEntry1.next;
    }
    return null;
  }
}