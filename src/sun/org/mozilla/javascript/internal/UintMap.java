package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class UintMap
  implements Serializable
{
  static final long serialVersionUID = 4242698212885848444L;
  private static final int A = -1640531527;
  private static final int EMPTY = -1;
  private static final int DELETED = -2;
  private transient int[] keys;
  private transient Object[] values;
  private int power;
  private int keyCount;
  private transient int occupiedCount;
  private transient int ivaluesShift;
  private static final boolean check = 0;

  public UintMap()
  {
    this(4);
  }

  public UintMap(int paramInt)
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

  public boolean has(int paramInt)
  {
    if (paramInt < 0)
      Kit.codeBug();
    return (0 <= findIndex(paramInt));
  }

  public Object getObject(int paramInt)
  {
    if (paramInt < 0)
      Kit.codeBug();
    if (this.values != null)
    {
      int i = findIndex(paramInt);
      if (0 <= i)
        return this.values[i];
    }
    return null;
  }

  public int getInt(int paramInt1, int paramInt2)
  {
    if (paramInt1 < 0)
      Kit.codeBug();
    int i = findIndex(paramInt1);
    if (0 <= i)
    {
      if (this.ivaluesShift != 0)
        return this.keys[(this.ivaluesShift + i)];
      return 0;
    }
    return paramInt2;
  }

  public int getExistingInt(int paramInt)
  {
    if (paramInt < 0)
      Kit.codeBug();
    int i = findIndex(paramInt);
    if (0 <= i)
    {
      if (this.ivaluesShift != 0)
        return this.keys[(this.ivaluesShift + i)];
      return 0;
    }
    Kit.codeBug();
    return 0;
  }

  public void put(int paramInt, Object paramObject)
  {
    if (paramInt < 0)
      Kit.codeBug();
    int i = ensureIndex(paramInt, false);
    if (this.values == null)
      this.values = new Object[1 << this.power];
    this.values[i] = paramObject;
  }

  public void put(int paramInt1, int paramInt2)
  {
    if (paramInt1 < 0)
      Kit.codeBug();
    int i = ensureIndex(paramInt1, true);
    if (this.ivaluesShift == 0)
    {
      int j = 1 << this.power;
      if (this.keys.length != j * 2)
      {
        int[] arrayOfInt = new int[j * 2];
        System.arraycopy(this.keys, 0, arrayOfInt, 0, j);
        this.keys = arrayOfInt;
      }
      this.ivaluesShift = j;
    }
    this.keys[(this.ivaluesShift + i)] = paramInt2;
  }

  public void remove(int paramInt)
  {
    if (paramInt < 0)
      Kit.codeBug();
    int i = findIndex(paramInt);
    if (0 <= i)
    {
      this.keys[i] = -2;
      this.keyCount -= 1;
      if (this.values != null)
        this.values[i] = null;
      if (this.ivaluesShift != 0)
        this.keys[(this.ivaluesShift + i)] = 0;
    }
  }

  public void clear()
  {
    int i = 1 << this.power;
    if (this.keys != null)
    {
      for (int j = 0; j != i; ++j)
        this.keys[j] = -1;
      if (this.values != null)
        for (j = 0; j != i; ++j)
          this.values[j] = null;
    }
    this.ivaluesShift = 0;
    this.keyCount = 0;
    this.occupiedCount = 0;
  }

  public int[] getKeys()
  {
    int[] arrayOfInt1 = this.keys;
    int i = this.keyCount;
    int[] arrayOfInt2 = new int[i];
    for (int j = 0; i != 0; ++j)
    {
      int k = arrayOfInt1[j];
      if ((k != -1) && (k != -2))
        arrayOfInt2[(--i)] = k;
    }
    return arrayOfInt2;
  }

  private static int tableLookupStep(int paramInt1, int paramInt2, int paramInt3)
  {
    int i = 32 - 2 * paramInt3;
    if (i >= 0)
      return (paramInt1 >>> i & paramInt2 | 0x1);
    return (paramInt1 & paramInt2 >>> -i | 0x1);
  }

  private int findIndex(int paramInt)
  {
    int[] arrayOfInt = this.keys;
    if (arrayOfInt != null)
    {
      int i = paramInt * -1640531527;
      int j = i >>> 32 - this.power;
      int k = arrayOfInt[j];
      if (k == paramInt)
        return j;
      if (k != -1)
      {
        int l = (1 << this.power) - 1;
        int i1 = tableLookupStep(i, l, this.power);
        int i2 = 0;
        do
        {
          j = j + i1 & l;
          k = arrayOfInt[j];
          if (k == paramInt)
            return j;
        }
        while (k != -1);
      }
    }
    return -1;
  }

  private int insertNewKey(int paramInt)
  {
    int[] arrayOfInt = this.keys;
    int i = paramInt * -1640531527;
    int j = i >>> 32 - this.power;
    if (arrayOfInt[j] != -1)
    {
      int k = (1 << this.power) - 1;
      int l = tableLookupStep(i, k, this.power);
      int i1 = j;
      do
        j = j + l & k;
      while (arrayOfInt[j] != -1);
    }
    arrayOfInt[j] = paramInt;
    this.occupiedCount += 1;
    this.keyCount += 1;
    return j;
  }

  private void rehashTable(boolean paramBoolean)
  {
    if ((this.keys != null) && (this.keyCount * 2 >= this.occupiedCount))
      this.power += 1;
    int i = 1 << this.power;
    int[] arrayOfInt = this.keys;
    int j = this.ivaluesShift;
    if ((j == 0) && (!(paramBoolean)))
    {
      this.keys = new int[i];
    }
    else
    {
      this.ivaluesShift = i;
      this.keys = new int[i * 2];
    }
    for (int k = 0; k != i; ++k)
      this.keys[k] = -1;
    Object[] arrayOfObject = this.values;
    if (arrayOfObject != null)
      this.values = new Object[i];
    int l = this.keyCount;
    this.occupiedCount = 0;
    if (l != 0)
    {
      this.keyCount = 0;
      int i1 = 0;
      int i2 = l;
      while (i2 != 0)
      {
        int i3 = arrayOfInt[i1];
        if ((i3 != -1) && (i3 != -2))
        {
          int i4 = insertNewKey(i3);
          if (arrayOfObject != null)
            this.values[i4] = arrayOfObject[i1];
          if (j != 0)
            this.keys[(this.ivaluesShift + i4)] = arrayOfInt[(j + i1)];
          --i2;
        }
        ++i1;
      }
    }
  }

  private int ensureIndex(int paramInt, boolean paramBoolean)
  {
    int i = -1;
    int j = -1;
    int[] arrayOfInt = this.keys;
    if (arrayOfInt != null)
    {
      int k = paramInt * -1640531527;
      i = k >>> 32 - this.power;
      int l = arrayOfInt[i];
      if (l == paramInt)
        return i;
      if (l != -1)
      {
        if (l == -2)
          j = i;
        int i1 = (1 << this.power) - 1;
        int i2 = tableLookupStep(k, i1, this.power);
        int i3 = 0;
        do
        {
          i = i + i2 & i1;
          l = arrayOfInt[i];
          if (l == paramInt)
            return i;
          if ((l == -2) && (j < 0))
            j = i;
        }
        while (l != -1);
      }
    }
    if (j >= 0)
    {
      i = j;
    }
    else
    {
      if ((arrayOfInt == null) || (this.occupiedCount * 4 >= (1 << this.power) * 3))
      {
        rehashTable(paramBoolean);
        arrayOfInt = this.keys;
        return insertNewKey(paramInt);
      }
      this.occupiedCount += 1;
    }
    arrayOfInt[i] = paramInt;
    this.keyCount += 1;
    return i;
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    paramObjectOutputStream.defaultWriteObject();
    int i = this.keyCount;
    if (i != 0)
    {
      boolean bool1 = this.ivaluesShift != 0;
      boolean bool2 = this.values != null;
      paramObjectOutputStream.writeBoolean(bool1);
      paramObjectOutputStream.writeBoolean(bool2);
      for (int j = 0; i != 0; ++j)
      {
        int k = this.keys[j];
        if ((k != -1) && (k != -2))
        {
          --i;
          paramObjectOutputStream.writeInt(k);
          if (bool1)
            paramObjectOutputStream.writeInt(this.keys[(this.ivaluesShift + j)]);
          if (bool2)
            paramObjectOutputStream.writeObject(this.values[j]);
        }
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
      boolean bool1 = paramObjectInputStream.readBoolean();
      boolean bool2 = paramObjectInputStream.readBoolean();
      int j = 1 << this.power;
      if (bool1)
      {
        this.keys = new int[2 * j];
        this.ivaluesShift = j;
      }
      else
      {
        this.keys = new int[j];
      }
      for (int k = 0; k != j; ++k)
        this.keys[k] = -1;
      if (bool2)
        this.values = new Object[j];
      for (k = 0; k != i; ++k)
      {
        int l = paramObjectInputStream.readInt();
        int i1 = insertNewKey(l);
        if (bool1)
        {
          int i2 = paramObjectInputStream.readInt();
          this.keys[(this.ivaluesShift + i1)] = i2;
        }
        if (bool2)
          this.values[i1] = paramObjectInputStream.readObject();
      }
    }
  }
}