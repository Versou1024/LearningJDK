package sun.security.util;

import java.util.Arrays;

public abstract class Cache
{
  public abstract int size();

  public abstract void clear();

  public abstract void put(Object paramObject1, Object paramObject2);

  public abstract Object get(Object paramObject);

  public abstract void remove(Object paramObject);

  public static Cache newSoftMemoryCache(int paramInt)
  {
    return new MemoryCache(true, paramInt);
  }

  public static Cache newSoftMemoryCache(int paramInt1, int paramInt2)
  {
    return new MemoryCache(true, paramInt1, paramInt2);
  }

  public static Cache newHardMemoryCache(int paramInt)
  {
    return new MemoryCache(false, paramInt);
  }

  public static Cache newNullCache()
  {
    return NullCache.INSTANCE;
  }

  public static Cache newHardMemoryCache(int paramInt1, int paramInt2)
  {
    return new MemoryCache(false, paramInt1, paramInt2);
  }

  public static class EqualByteArray
  {
    private final byte[] b;
    private volatile int hash;

    public EqualByteArray(byte[] paramArrayOfByte)
    {
      this.b = paramArrayOfByte;
    }

    public int hashCode()
    {
      int i = this.hash;
      if (i == 0)
      {
        i = this.b.length + 1;
        for (int j = 0; j < this.b.length; ++j)
          i += (this.b[j] & 0xFF) * 37;
        this.hash = i;
      }
      return i;
    }

    public boolean equals(Object paramObject)
    {
      if (this == paramObject)
        return true;
      if (!(paramObject instanceof EqualByteArray))
        return false;
      EqualByteArray localEqualByteArray = (EqualByteArray)paramObject;
      return Arrays.equals(this.b, localEqualByteArray.b);
    }
  }
}