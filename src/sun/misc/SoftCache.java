package sun.misc;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

public class SoftCache extends AbstractMap
  implements Map
{
  private Map hash;
  private ReferenceQueue queue = new ReferenceQueue();
  private Set entrySet = null;

  private void processQueue()
  {
    while (true)
    {
      while (true)
      {
        ValueCell localValueCell;
        if ((localValueCell = (ValueCell)this.queue.poll()) == null)
          return;
        if (!(ValueCell.access$000(localValueCell)))
          break;
        this.hash.remove(ValueCell.access$100(localValueCell));
      }
      ValueCell.access$210();
    }
  }

  public SoftCache(int paramInt, float paramFloat)
  {
    this.hash = new HashMap(paramInt, paramFloat);
  }

  public SoftCache(int paramInt)
  {
    this.hash = new HashMap(paramInt);
  }

  public SoftCache()
  {
    this.hash = new HashMap();
  }

  public int size()
  {
    return entrySet().size();
  }

  public boolean isEmpty()
  {
    return entrySet().isEmpty();
  }

  public boolean containsKey(Object paramObject)
  {
    return (ValueCell.access$300(this.hash.get(paramObject), false) != null);
  }

  protected Object fill(Object paramObject)
  {
    return null;
  }

  public Object get(Object paramObject)
  {
    processQueue();
    Object localObject = this.hash.get(paramObject);
    if (localObject == null)
    {
      localObject = fill(paramObject);
      if (localObject != null)
      {
        this.hash.put(paramObject, ValueCell.access$400(paramObject, localObject, this.queue));
        return localObject;
      }
    }
    return ValueCell.access$300(localObject, false);
  }

  public Object put(Object paramObject1, Object paramObject2)
  {
    processQueue();
    ValueCell localValueCell = ValueCell.access$400(paramObject1, paramObject2, this.queue);
    return ValueCell.access$300(this.hash.put(paramObject1, localValueCell), true);
  }

  public Object remove(Object paramObject)
  {
    processQueue();
    return ValueCell.access$300(this.hash.remove(paramObject), true);
  }

  public void clear()
  {
    processQueue();
    this.hash.clear();
  }

  private static boolean valEquals(Object paramObject1, Object paramObject2)
  {
    return ((paramObject1 == null) ? false : (paramObject2 == null) ? true : paramObject1.equals(paramObject2));
  }

  public Set entrySet()
  {
    if (this.entrySet == null)
      this.entrySet = new EntrySet(this, null);
    return this.entrySet;
  }

  private class Entry
  implements Map.Entry
  {
    private Map.Entry ent;
    private Object value;

    Entry(, Map.Entry paramEntry, Object paramObject)
    {
      this.ent = paramEntry;
      this.value = paramObject;
    }

    public Object getKey()
    {
      return this.ent.getKey();
    }

    public Object getValue()
    {
      return this.value;
    }

    public Object setValue()
    {
      return this.ent.setValue(SoftCache.ValueCell.access$400(this.ent.getKey(), paramObject, SoftCache.access$500(this.this$0)));
    }

    public boolean equals()
    {
      if (!(paramObject instanceof Map.Entry))
        return false;
      Map.Entry localEntry = (Map.Entry)paramObject;
      return ((SoftCache.access$600(this.ent.getKey(), localEntry.getKey())) && (SoftCache.access$600(this.value, localEntry.getValue())));
    }

    public int hashCode()
    {
      Object localObject;
      return ((((localObject = getKey()) == null) ? 0 : localObject.hashCode()) ^ ((this.value == null) ? 0 : this.value.hashCode()));
    }
  }

  private class EntrySet extends AbstractSet
  {
    Set hashEntries = SoftCache.access$700(this.this$0).entrySet();

    public Iterator iterator()
    {
      return new Iterator(this)
      {
        Iterator hashIterator = this.this$1.hashEntries.iterator();
        SoftCache.Entry next = null;

        public boolean hasNext()
        {
          Map.Entry localEntry;
          Object localObject;
          while (true)
          {
            if (!(this.hashIterator.hasNext()))
              break label75;
            localEntry = (Map.Entry)this.hashIterator.next();
            SoftCache.ValueCell localValueCell = (SoftCache.ValueCell)localEntry.getValue();
            localObject = null;
            if (localValueCell == null)
              break;
            if ((localObject = localValueCell.get()) != null)
              break;
          }
          this.next = new SoftCache.Entry(this.this$1.this$0, localEntry, localObject);
          return true;
          label75: return false;
        }

        public Object next()
        {
          if ((this.next == null) && (!(hasNext())))
            throw new NoSuchElementException();
          SoftCache.Entry localEntry = this.next;
          this.next = null;
          return localEntry;
        }

        public void remove()
        {
          this.hashIterator.remove();
        }
      };
    }

    public boolean isEmpty()
    {
      return (!(iterator().hasNext()));
    }

    public int size()
    {
      int i = 0;
      Iterator localIterator = iterator();
      while (localIterator.hasNext())
      {
        ++i;
        localIterator.next();
      }
      return i;
    }

    public boolean remove()
    {
      SoftCache.access$800(this.this$0);
      if (paramObject instanceof SoftCache.Entry)
        return this.hashEntries.remove(SoftCache.Entry.access$900((SoftCache.Entry)paramObject));
      return false;
    }
  }

  private static class ValueCell extends SoftReference
  {
    private static Object INVALID_KEY = new Object();
    private static int dropped = 0;
    private Object key;

    private ValueCell(Object paramObject1, Object paramObject2, ReferenceQueue paramReferenceQueue)
    {
      super(paramObject2, paramReferenceQueue);
      this.key = paramObject1;
    }

    private static ValueCell create(Object paramObject1, Object paramObject2, ReferenceQueue paramReferenceQueue)
    {
      if (paramObject2 == null)
        return null;
      return new ValueCell(paramObject1, paramObject2, paramReferenceQueue);
    }

    private static Object strip(Object paramObject, boolean paramBoolean)
    {
      if (paramObject == null)
        return null;
      ValueCell localValueCell = (ValueCell)paramObject;
      Object localObject = localValueCell.get();
      if (paramBoolean)
        localValueCell.drop();
      return localObject;
    }

    private boolean isValid()
    {
      return (this.key != INVALID_KEY);
    }

    private void drop()
    {
      super.clear();
      this.key = INVALID_KEY;
      dropped += 1;
    }
  }
}