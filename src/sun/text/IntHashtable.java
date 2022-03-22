package sun.text;

import I;
import java.io.PrintStream;

public final class IntHashtable
{
  private int defaultValue = 0;
  private int primeIndex;
  private static final float HIGH_WATER_FACTOR = 0.40000000596046448F;
  private int highWaterMark;
  private static final float LOW_WATER_FACTOR = 0.0F;
  private int lowWaterMark;
  private int count;
  private int[] values;
  private int[] keyList;
  private static final int EMPTY = -2147483648;
  private static final int DELETED = -2147483647;
  private static final int MAX_UNUSED = -2147483647;
  private static final int[] PRIMES = { 17, 37, 67, 131, 257, 521, 1031, 2053, 4099, 8209, 16411, 32771, 65537, 131101, 262147, 524309, 1048583, 2097169, 4194319, 8388617, 16777259, 33554467, 67108879, 134217757, 268435459, 536870923, 1073741827, 2147483647 };

  public IntHashtable()
  {
    initialize(3);
  }

  public IntHashtable(int paramInt)
  {
    initialize(leastGreaterPrimeIndex((int)(paramInt / 0.40000000596046448F)));
  }

  public int size()
  {
    return this.count;
  }

  public boolean isEmpty()
  {
    return (this.count == 0);
  }

  public void put(int paramInt1, int paramInt2)
  {
    if (this.count > this.highWaterMark)
      rehash();
    int i = find(paramInt1);
    if (this.keyList[i] <= -2147483647)
    {
      this.keyList[i] = paramInt1;
      this.count += 1;
    }
    this.values[i] = paramInt2;
  }

  public int get(int paramInt)
  {
    return this.values[find(paramInt)];
  }

  public void remove(int paramInt)
  {
    int i = find(paramInt);
    if (this.keyList[i] > -2147483647)
    {
      this.keyList[i] = -2147483647;
      this.values[i] = this.defaultValue;
      this.count -= 1;
      if (this.count < this.lowWaterMark)
        rehash();
    }
  }

  public int getDefaultValue()
  {
    return this.defaultValue;
  }

  public void setDefaultValue(int paramInt)
  {
    this.defaultValue = paramInt;
    rehash();
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject.getClass() != super.getClass())
      return false;
    IntHashtable localIntHashtable = (IntHashtable)paramObject;
    if ((localIntHashtable.size() != this.count) || (localIntHashtable.defaultValue != this.defaultValue))
      return false;
    for (int i = 0; i < this.keyList.length; ++i)
    {
      int j = this.keyList[i];
      if ((j > -2147483647) && (localIntHashtable.get(j) != this.values[i]))
        return false;
    }
    return true;
  }

  public int hashCode()
  {
    int i = 465;
    int j = 1362796821;
    for (int k = 0; k < this.keyList.length; ++k)
    {
      i = i * j + 1;
      i += this.keyList[k];
    }
    for (k = 0; k < this.values.length; ++k)
    {
      i = i * j + 1;
      i += this.values[k];
    }
    return i;
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    IntHashtable localIntHashtable = (IntHashtable)super.clone();
    this.values = ((int[])(int[])this.values.clone());
    this.keyList = ((int[])(int[])this.keyList.clone());
    return localIntHashtable;
  }

  private void initialize(int paramInt)
  {
    if (paramInt < 0)
    {
      paramInt = 0;
    }
    else if (paramInt >= PRIMES.length)
    {
      System.out.println("TOO BIG");
      paramInt = PRIMES.length - 1;
    }
    this.primeIndex = paramInt;
    int i = PRIMES[paramInt];
    this.values = new int[i];
    this.keyList = new int[i];
    for (int j = 0; j < i; ++j)
    {
      this.keyList[j] = -2147483648;
      this.values[j] = this.defaultValue;
    }
    this.count = 0;
    this.lowWaterMark = (int)(i * 0F);
    this.highWaterMark = (int)(i * 0.40000000596046448F);
  }

  private void rehash()
  {
    int[] arrayOfInt1 = this.values;
    int[] arrayOfInt2 = this.keyList;
    int i = this.primeIndex;
    if (this.count > this.highWaterMark)
      ++i;
    else if (this.count < this.lowWaterMark);
    initialize(i -= 2);
    for (int j = arrayOfInt1.length - 1; j >= 0; --j)
    {
      int k = arrayOfInt2[j];
      if (k > -2147483647)
        putInternal(k, arrayOfInt1[j]);
    }
  }

  public void putInternal(int paramInt1, int paramInt2)
  {
    int i = find(paramInt1);
    if (this.keyList[i] < -2147483647)
    {
      this.keyList[i] = paramInt1;
      this.count += 1;
    }
    this.values[i] = paramInt2;
  }

  private int find(int paramInt)
  {
    if (paramInt <= -2147483647)
      throw new IllegalArgumentException("key can't be less than 0xFFFFFFFE");
    int i = -1;
    int j = (paramInt ^ 0x4000000) % this.keyList.length;
    if (j < 0)
      j = -j;
    int k = 0;
    while (true)
    {
      int l = this.keyList[j];
      if (l == paramInt)
        return j;
      if (l > -2147483647)
        break label86:
      if (l == -2147483648)
      {
        if (i >= 0)
          j = i;
        return j;
      }
      if (i < 0)
        i = j;
      if (k == 0)
      {
        label86: k = paramInt % (this.keyList.length - 1);
        if (k < 0)
          k = -k;
      }
      j = (j + ++k) % this.keyList.length;
      if (j == i)
        return j;
    }
  }

  private static int leastGreaterPrimeIndex(int paramInt)
  {
    for (int i = 0; i < PRIMES.length; ++i)
      if (paramInt < PRIMES[i])
        break;
    return ((i == 0) ? 0 : i - 1);
  }
}