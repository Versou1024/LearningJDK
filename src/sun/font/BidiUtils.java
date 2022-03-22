package sun.font;

import java.text.Bidi;

public final class BidiUtils
{
  static final char NUMLEVELS = 62;

  public static void getLevels(Bidi paramBidi, byte[] paramArrayOfByte, int paramInt)
  {
    int i = paramInt + paramBidi.getLength();
    if ((paramInt < 0) || (i > paramArrayOfByte.length))
      throw new IndexOutOfBoundsException("levels.length = " + paramArrayOfByte.length + " start: " + paramInt + " limit: " + i);
    int j = paramBidi.getRunCount();
    int k = paramInt;
    for (int l = 0; l < j; ++l)
    {
      int i1 = paramInt + paramBidi.getRunLimit(l);
      int i2 = (byte)paramBidi.getRunLevel(l);
      while (k < i1)
        paramArrayOfByte[(k++)] = i2;
    }
  }

  public static byte[] getLevels(Bidi paramBidi)
  {
    byte[] arrayOfByte = new byte[paramBidi.getLength()];
    getLevels(paramBidi, arrayOfByte, 0);
    return arrayOfByte;
  }

  public static int[] createVisualToLogicalMap(byte[] paramArrayOfByte)
  {
    int i1;
    int i = paramArrayOfByte.length;
    int[] arrayOfInt = new int[i];
    int j = 63;
    int k = 0;
    for (int l = 0; l < i; ++l)
    {
      arrayOfInt[l] = l;
      i1 = paramArrayOfByte[l];
      if (i1 > k)
        k = i1;
      if (((i1 & 0x1) != 0) && (i1 < j))
        j = i1;
    }
    while (k >= j)
    {
      l = 0;
      while (true)
      {
        while ((l < i) && (paramArrayOfByte[l] < k))
          ++l;
        i1 = l++;
        if (i1 == paramArrayOfByte.length)
          break;
        while ((l < i) && (paramArrayOfByte[l] >= k))
          ++l;
        for (int i2 = l - 1; i1 < i2; --i2)
        {
          int i3 = arrayOfInt[i1];
          arrayOfInt[i1] = arrayOfInt[i2];
          arrayOfInt[i2] = i3;
          ++i1;
        }
      }
      k = (byte)(k - 1);
    }
    return arrayOfInt;
  }

  public static int[] createInverseMap(int[] paramArrayOfInt)
  {
    if (paramArrayOfInt == null)
      return null;
    int[] arrayOfInt = new int[paramArrayOfInt.length];
    for (int i = 0; i < paramArrayOfInt.length; ++i)
      arrayOfInt[paramArrayOfInt[i]] = i;
    return arrayOfInt;
  }

  public static int[] createContiguousOrder(int[] paramArrayOfInt)
  {
    if (paramArrayOfInt != null)
      return computeContiguousOrder(paramArrayOfInt, 0, paramArrayOfInt.length);
    return null;
  }

  private static int[] computeContiguousOrder(int[] paramArrayOfInt, int paramInt1, int paramInt2)
  {
    int[] arrayOfInt = new int[paramInt2 - paramInt1];
    for (int i = 0; i < arrayOfInt.length; ++i)
      arrayOfInt[i] = (i + paramInt1);
    for (int j = 0; j < arrayOfInt.length - 1; ++j)
    {
      int k = j;
      int l = paramArrayOfInt[arrayOfInt[k]];
      for (int i1 = j; i1 < arrayOfInt.length; ++i1)
        if (paramArrayOfInt[arrayOfInt[i1]] < l)
        {
          k = i1;
          l = paramArrayOfInt[arrayOfInt[k]];
        }
      i1 = arrayOfInt[j];
      arrayOfInt[j] = arrayOfInt[k];
      arrayOfInt[k] = i1;
    }
    if (paramInt1 != 0)
      for (j = 0; j < arrayOfInt.length; ++j)
        arrayOfInt[j] -= paramInt1;
    for (j = 0; j < arrayOfInt.length; ++j)
      if (arrayOfInt[j] != j)
        break;
    if (j == arrayOfInt.length)
      return null;
    return createInverseMap(arrayOfInt);
  }

  public static int[] createNormalizedMap(int[] paramArrayOfInt, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (paramArrayOfInt != null)
    {
      if ((paramInt1 != 0) || (paramInt2 != paramArrayOfInt.length))
      {
        int i;
        int j;
        int k;
        if (paramArrayOfByte == null)
        {
          k = 0;
          i = 1;
          j = 1;
        }
        else if (paramArrayOfByte[paramInt1] == paramArrayOfByte[(paramInt2 - 1)])
        {
          k = paramArrayOfByte[paramInt1];
          j = ((k & 0x1) == 0) ? 1 : 0;
          for (int l = paramInt1; l < paramInt2; ++l)
          {
            if (paramArrayOfByte[l] < k)
              break;
            if (j != 0)
              j = (paramArrayOfByte[l] == k) ? 1 : 0;
          }
          i = (l == paramInt2) ? 1 : 0;
        }
        else
        {
          i = 0;
          k = 0;
          j = 0;
        }
        if (i != 0)
        {
          int i1;
          if (j != 0)
            return null;
          int[] arrayOfInt = new int[paramInt2 - paramInt1];
          if ((k & 0x1) != 0)
            i1 = paramArrayOfInt[(paramInt2 - 1)];
          else
            i1 = paramArrayOfInt[paramInt1];
          if (i1 == 0)
            System.arraycopy(paramArrayOfInt, paramInt1, arrayOfInt, 0, paramInt2 - paramInt1);
          else
            for (int i2 = 0; i2 < arrayOfInt.length; ++i2)
              arrayOfInt[i2] = (paramArrayOfInt[(i2 + paramInt1)] - i1);
          return arrayOfInt;
        }
        return computeContiguousOrder(paramArrayOfInt, paramInt1, paramInt2);
      }
      return paramArrayOfInt;
    }
    return null;
  }

  public static void reorderVisually(byte[] paramArrayOfByte, Object[] paramArrayOfObject)
  {
    int i1;
    int i = paramArrayOfByte.length;
    int j = 63;
    int k = 0;
    for (int l = 0; l < i; ++l)
    {
      i1 = paramArrayOfByte[l];
      if (i1 > k)
        k = i1;
      if (((i1 & 0x1) != 0) && (i1 < j))
        j = i1;
    }
    while (k >= j)
    {
      l = 0;
      while (true)
      {
        while ((l < i) && (paramArrayOfByte[l] < k))
          ++l;
        i1 = l++;
        if (i1 == paramArrayOfByte.length)
          break;
        while ((l < i) && (paramArrayOfByte[l] >= k))
          ++l;
        for (int i2 = l - 1; i1 < i2; --i2)
        {
          Object localObject = paramArrayOfObject[i1];
          paramArrayOfObject[i1] = paramArrayOfObject[i2];
          paramArrayOfObject[i2] = localObject;
          ++i1;
        }
      }
      k = (byte)(k - 1);
    }
  }
}