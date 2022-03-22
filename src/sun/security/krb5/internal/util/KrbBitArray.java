package sun.security.krb5.internal.util;

import sun.security.util.BitArray;

public class KrbBitArray
{
  BitArray bits;

  public KrbBitArray(int paramInt)
    throws IllegalArgumentException
  {
    this.bits = new BitArray(paramInt);
  }

  public KrbBitArray(int paramInt, byte[] paramArrayOfByte)
    throws IllegalArgumentException
  {
    this.bits = new BitArray(paramInt, paramArrayOfByte);
  }

  public KrbBitArray(boolean[] paramArrayOfBoolean)
  {
    this.bits = new BitArray(paramArrayOfBoolean);
  }

  public void set(int paramInt, boolean paramBoolean)
  {
    this.bits.set(paramInt, paramBoolean);
  }

  public boolean get(int paramInt)
  {
    return this.bits.get(paramInt);
  }

  public boolean[] toBooleanArray()
  {
    return this.bits.toBooleanArray();
  }

  public String toString()
  {
    return this.bits.toString();
  }
}