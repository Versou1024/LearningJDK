package sun.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class PreHashedMap<V> extends AbstractMap<String, V>
{
  private final int rows;
  private final int size;
  private final int shift;
  private final int mask;
  private final Object[] ht;

  protected PreHashedMap(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.rows = paramInt1;
    this.size = paramInt2;
    this.shift = paramInt3;
    this.mask = paramInt4;
    this.ht = new Object[paramInt1];
    init(this.ht);
  }

  protected abstract void init(Object[] paramArrayOfObject);

  private V toV(Object paramObject)
  {
    return paramObject;
  }

  public V get(Object paramObject)
  {
    int i = paramObject.hashCode() >> this.shift & this.mask;
    Object[] arrayOfObject = (Object[])(Object[])this.ht[i];
    if (arrayOfObject == null)
      return null;
    while (true)
    {
      if (arrayOfObject[0].equals(paramObject))
        return toV(arrayOfObject[1]);
      if (arrayOfObject.length < 3)
        return null;
      arrayOfObject = (Object[])(Object[])arrayOfObject[2];
    }
  }

  public V put(String paramString, V paramV)
  {
    int i = paramString.hashCode() >> this.shift & this.mask;
    Object[] arrayOfObject = (Object[])(Object[])this.ht[i];
    if (arrayOfObject == null)
      throw new UnsupportedOperationException(paramString);
    while (true)
    {
      if (arrayOfObject[0].equals(paramString))
      {
        Object localObject = toV(arrayOfObject[1]);
        arrayOfObject[1] = paramV;
        return localObject;
      }
      if (arrayOfObject.length < 3)
        throw new UnsupportedOperationException(paramString);
      arrayOfObject = (Object[])(Object[])arrayOfObject[2];
    }
  }

  public Set<String> keySet()
  {
    return new AbstractSet(this)
    {
      public int size()
      {
        return PreHashedMap.access$000(this.this$0);
      }

      public Iterator<String> iterator()
      {
        return new Iterator(this)
        {
          private int i = -1;
          Object[] a = null;
          String cur = null;

          private boolean findNext()
          {
            if (this.a != null)
            {
              if (this.a.length == 3)
              {
                this.a = ((Object[])(Object[])this.a[2]);
                this.cur = ((String)this.a[0]);
                return true;
              }
              this.i += 1;
              this.a = null;
            }
            this.cur = null;
            if (this.i >= PreHashedMap.access$100(this.this$1.this$0))
              return false;
            if ((this.i < 0) || (PreHashedMap.access$200(this.this$1.this$0)[this.i] == null))
              do
                if (++this.i >= PreHashedMap.access$100(this.this$1.this$0))
                  return false;
              while (PreHashedMap.access$200(this.this$1.this$0)[this.i] == null);
            this.a = ((Object[])(Object[])PreHashedMap.access$200(this.this$1.this$0)[this.i]);
            this.cur = ((String)this.a[0]);
            return true;
          }

          public boolean hasNext()
          {
            if (this.cur != null)
              return true;
            return findNext();
          }

          public String next()
          {
            if ((this.cur == null) && (!(findNext())))
              throw new NoSuchElementException();
            String str = this.cur;
            this.cur = null;
            return str;
          }

          public void remove()
          {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public Set<Map.Entry<String, V>> entrySet()
  {
    return new AbstractSet(this)
    {
      public int size()
      {
        return PreHashedMap.access$000(this.this$0);
      }

      public Iterator<Map.Entry<String, V>> iterator()
      {
        return new Iterator(this)
        {
          final Iterator<String> i = this.this$1.this$0.keySet().iterator();

          public boolean hasNext()
          {
            return this.i.hasNext();
          }

          public Map.Entry<String, V> next()
          {
            return new Map.Entry(this)
            {
              String k = (String)this.this$2.i.next();

              public String getKey()
              {
                return this.k;
              }

              public V getValue()
              {
                return this.this$2.this$1.this$0.get(this.k);
              }

              public int hashCode()
              {
                Object localObject = this.this$2.this$1.this$0.get(this.k);
                return (this.k.hashCode() + ((localObject == null) ? 0 : localObject.hashCode()));
              }

              public boolean equals()
              {
                if (paramObject == this)
                  return true;
                if (!(paramObject instanceof Map.Entry))
                  return false;
                Map.Entry localEntry = (Map.Entry)paramObject;
                if (getKey() == null)
                  if (localEntry.getKey() != null)
                    break label95;
                else
                  if (!(getKey().equals(localEntry.getKey())))
                    break label95;
                if (getValue() == null)
                  if (localEntry.getValue() != null)
                    break label95;
                label95: return (getValue().equals(localEntry.getValue()));
              }

              public V setValue()
              {
                throw new UnsupportedOperationException();
              }
            };
          }

          public void remove()
          {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}