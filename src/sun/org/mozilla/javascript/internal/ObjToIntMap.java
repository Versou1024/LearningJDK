package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjToIntMap
  implements Serializable
{
  static final long serialVersionUID = -1542220580748809402L;
  private static final int A = -1640531527;
  private static final Object DELETED = new Object();
  private transient Object[] keys;
  private transient int[] values;
  private int power;
  private int keyCount;
  private transient int occupiedCount;
  private static final boolean check = 0;

  public ObjToIntMap()
  {
    this(4);
  }

  public ObjToIntMap(int paramInt)
  {
    if (paramInt < 0)
      Kit.codeBug();
    int i = paramInt * 4 / 3;
    for (int j = 2; 1 << j < i; ++j);
    this.power = j;
  }

  public boolean isEmpty()
  {
    return (this.keyCount == 0);
  }

  public int size()
  {
    return this.keyCount;
  }

  public boolean has(Object paramObject)
  {
    if (paramObject == null)
      paramObject = UniqueTag.NULL_VALUE;
    return (0 <= findIndex(paramObject));
  }

  public int get(Object paramObject, int paramInt)
  {
    if (paramObject == null)
      paramObject = UniqueTag.NULL_VALUE;
    int i = findIndex(paramObject);
    if (0 <= i)
      return this.values[i];
    return paramInt;
  }

  public int getExisting(Object paramObject)
  {
    if (paramObject == null)
      paramObject = UniqueTag.NULL_VALUE;
    int i = findIndex(paramObject);
    if (0 <= i)
      return this.values[i];
    Kit.codeBug();
    return 0;
  }

  public void put(Object paramObject, int paramInt)
  {
    if (paramObject == null)
      paramObject = UniqueTag.NULL_VALUE;
    int i = ensureIndex(paramObject);
    this.values[i] = paramInt;
  }

  public Object intern(Object paramObject)
  {
    int i = 0;
    if (paramObject == null)
    {
      i = 1;
      paramObject = UniqueTag.NULL_VALUE;
    }
    int j = ensureIndex(paramObject);
    this.values[j] = 0;
    return ((i != 0) ? null : this.keys[j]);
  }

  public void remove(Object paramObject)
  {
    if (paramObject == null)
      paramObject = UniqueTag.NULL_VALUE;
    int i = findIndex(paramObject);
    if (0 <= i)
    {
      this.keys[i] = DELETED;
      this.keyCount -= 1;
    }
  }

  public void clear()
  {
    int i = this.keys.length;
    while (i != 0)
      this.keys[(--i)] = null;
    this.keyCount = 0;
    this.occupiedCount = 0;
  }

  public Iterator newIterator()
  {
    return new Iterator(this);
  }

  final void initIterator(Iterator paramIterator)
  {
    paramIterator.init(this.keys, this.values, this.keyCount);
  }

  public Object[] getKeys()
  {
    Object[] arrayOfObject = new Object[this.keyCount];
    getKeys(arrayOfObject, 0);
    return arrayOfObject;
  }

  public void getKeys(Object[] paramArrayOfObject, int paramInt)
  {
    int i = this.keyCount;
    for (int j = 0; i != 0; ++j)
    {
      Object localObject = this.keys[j];
      if ((localObject != null) && (localObject != DELETED))
      {
        if (localObject == UniqueTag.NULL_VALUE)
          localObject = null;
        paramArrayOfObject[paramInt] = localObject;
        ++paramInt;
        --i;
      }
    }
  }

  private static int tableLookupStep(int paramInt1, int paramInt2, int paramInt3)
  {
    int i = 32 - 2 * paramInt3;
    if (i >= 0)
      return (paramInt1 >>> i & paramInt2 | 0x1);
    return (paramInt1 & paramInt2 >>> -i | 0x1);
  }

  private int findIndex(Object paramObject)
  {
    if (this.keys != null)
    {
      int i = paramObject.hashCode();
      int j = i * -1640531527;
      int k = j >>> 32 - this.power;
      Object localObject = this.keys[k];
      if (localObject != null)
      {
        int l = 1 << this.power;
        if ((localObject == paramObject) || ((this.values[(l + k)] == i) && (localObject.equals(paramObject))))
          return k;
        int i1 = l - 1;
        int i2 = tableLookupStep(j, i1, this.power);
        int i3 = 0;
        do
        {
          k = k + i2 & i1;
          localObject = this.keys[k];
          if (localObject == null)
            break label162:
        }
        while ((localObject != paramObject) && (((this.values[(l + k)] != i) || (!(localObject.equals(paramObject))))));
        return k;
      }
    }
    label162: return -1;
  }

  private int insertNewKey(Object paramObject, int paramInt)
  {
    int i = paramInt * -1640531527;
    int j = i >>> 32 - this.power;
    int k = 1 << this.power;
    if (this.keys[j] != null)
    {
      int l = k - 1;
      int i1 = tableLookupStep(i, l, this.power);
      int i2 = j;
      do
        j = j + i1 & l;
      while (this.keys[j] != null);
    }
    this.keys[j] = paramObject;
    this.values[(k + j)] = paramInt;
    this.occupiedCount += 1;
    this.keyCount += 1;
    return j;
  }

  private void rehashTable()
  {
    int i;
    if (this.keys == null)
    {
      i = 1 << this.power;
      this.keys = new Object[i];
      this.values = new int[2 * i];
    }
    else
    {
      if (this.keyCount * 2 >= this.occupiedCount)
        this.power += 1;
      i = 1 << this.power;
      Object[] arrayOfObject = this.keys;
      int[] arrayOfInt = this.values;
      int j = arrayOfObject.length;
      this.keys = new Object[i];
      this.values = new int[2 * i];
      int k = this.keyCount;
      this.occupiedCount = (this.keyCount = 0);
      for (int l = 0; k != 0; ++l)
      {
        Object localObject = arrayOfObject[l];
        if ((localObject != null) && (localObject != DELETED))
        {
          int i1 = arrayOfInt[(j + l)];
          int i2 = insertNewKey(localObject, i1);
          this.values[i2] = arrayOfInt[l];
          --k;
        }
      }
    }
  }

  private int ensureIndex(Object paramObject)
  {
    int i = paramObject.hashCode();
    int j = -1;
    int k = -1;
    if (this.keys != null)
    {
      int l = i * -1640531527;
      j = l >>> 32 - this.power;
      Object localObject = this.keys[j];
      if (localObject != null)
      {
        int i1 = 1 << this.power;
        if ((localObject == paramObject) || ((this.values[(i1 + j)] == i) && (localObject.equals(paramObject))))
          return j;
        if (localObject == DELETED)
          k = j;
        int i2 = i1 - 1;
        int i3 = tableLookupStep(l, i2, this.power);
        int i4 = 0;
        while (true)
        {
          do
          {
            j = j + i3 & i2;
            localObject = this.keys[j];
            if (localObject == null)
              break label191:
            if ((localObject == paramObject) || ((this.values[(i1 + j)] == i) && (localObject.equals(paramObject))))
              return j;
          }
          while ((localObject != DELETED) || (k >= 0));
          k = j;
        }
      }
    }
    if (k >= 0)
    {
      label191: j = k;
    }
    else
    {
      if ((this.keys == null) || (this.occupiedCount * 4 >= (1 << this.power) * 3))
      {
        rehashTable();
        return insertNewKey(paramObject, i);
      }
      this.occupiedCount += 1;
    }
    this.keys[j] = paramObject;
    this.values[((1 << this.power) + j)] = i;
    this.keyCount += 1;
    return j;
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    paramObjectOutputStream.defaultWriteObject();
    int i = this.keyCount;
    for (int j = 0; i != 0; ++j)
    {
      Object localObject = this.keys[j];
      if ((localObject != null) && (localObject != DELETED))
      {
        --i;
        paramObjectOutputStream.writeObject(localObject);
        paramObjectOutputStream.writeInt(this.values[j]);
      }
    }
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    int i = this.keyCount;
    if (i != 0)
    {
      this.keyCount = 0;
      int j = 1 << this.power;
      this.keys = new Object[j];
      this.values = new int[2 * j];
      for (int k = 0; k != i; ++k)
      {
        Object localObject = paramObjectInputStream.readObject();
        int l = localObject.hashCode();
        int i1 = insertNewKey(localObject, l);
        this.values[i1] = paramObjectInputStream.readInt();
      }
    }
  }

  public static class Iterator
  {
    ObjToIntMap master;
    private int cursor;
    private int remaining;
    private Object[] keys;
    private int[] values;

    Iterator(ObjToIntMap paramObjToIntMap)
    {
      this.master = paramObjToIntMap;
    }

    final void init(Object[] paramArrayOfObject, int[] paramArrayOfInt, int paramInt)
    {
      this.keys = paramArrayOfObject;
      this.values = paramArrayOfInt;
      this.cursor = -1;
      this.remaining = paramInt;
    }

    public void start()
    {
      this.master.initIterator(this);
      next();
    }

    public boolean done()
    {
      return (this.remaining < 0);
    }

    public void next()
    {
      if (this.remaining == -1)
        Kit.codeBug();
      if (this.remaining == 0)
      {
        this.remaining = -1;
        this.cursor = -1;
      }
      else
      {
        this.cursor += 1;
        while (true)
        {
          Object localObject = this.keys[this.cursor];
          if ((localObject != null) && (localObject != ObjToIntMap.access$000()))
          {
            this.remaining -= 1;
            return;
          }
          this.cursor += 1;
        }
      }
    }

    public Object getKey()
    {
      Object localObject = this.keys[this.cursor];
      if (localObject == UniqueTag.NULL_VALUE)
        localObject = null;
      return localObject;
    }

    public int getValue()
    {
      return this.values[this.cursor];
    }

    public void setValue(int paramInt)
    {
      this.values[this.cursor] = paramInt;
    }
  }
}