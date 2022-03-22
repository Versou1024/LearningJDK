package sun.security.util;

import B;
import java.io.ByteArrayOutputStream;

public class BitArray
{
  private byte[] repn;
  private int length;
  private static final int BITS_PER_UNIT = 8;
  private static final byte[][] NYBBLE = { { 48, 48, 48, 48 }, { 48, 48, 48, 49 }, { 48, 48, 49, 48 }, { 48, 48, 49, 49 }, { 48, 49, 48, 48 }, { 48, 49, 48, 49 }, { 48, 49, 49, 48 }, { 48, 49, 49, 49 }, { 49, 48, 48, 48 }, { 49, 48, 48, 49 }, { 49, 48, 49, 48 }, { 49, 48, 49, 49 }, { 49, 49, 48, 48 }, { 49, 49, 48, 49 }, { 49, 49, 49, 48 }, { 49, 49, 49, 49 } };
  private static final int BYTES_PER_LINE = 8;

  private static int subscript(int paramInt)
  {
    return (paramInt / 8);
  }

  private static int position(int paramInt)
  {
    return (1 << 7 - paramInt % 8);
  }

  public BitArray(int paramInt)
    throws IllegalArgumentException
  {
    if (paramInt < 0)
      throw new IllegalArgumentException("Negative length for BitArray");
    this.length = paramInt;
    this.repn = new byte[(paramInt + 8 - 1) / 8];
  }

  public BitArray(int paramInt, byte[] paramArrayOfByte)
    throws IllegalArgumentException
  {
    if (paramInt < 0)
      throw new IllegalArgumentException("Negative length for BitArray");
    if (paramArrayOfByte.length * 8 < paramInt)
      throw new IllegalArgumentException("Byte array too short to represent bit array of given length");
    this.length = paramInt;
    int i = (paramInt + 8 - 1) / 8;
    int j = i * 8 - paramInt;
    int k = (byte)(255 << j);
    this.repn = new byte[i];
    System.arraycopy(paramArrayOfByte, 0, this.repn, 0, i);
    if (i > 0)
    {
      int tmp98_97 = (i - 1);
      byte[] tmp98_92 = this.repn;
      tmp98_92[tmp98_97] = (byte)(tmp98_92[tmp98_97] & k);
    }
  }

  public BitArray(boolean[] paramArrayOfBoolean)
  {
    this.length = paramArrayOfBoolean.length;
    this.repn = new byte[(this.length + 7) / 8];
    for (int i = 0; i < this.length; ++i)
      set(i, paramArrayOfBoolean[i]);
  }

  private BitArray(BitArray paramBitArray)
  {
    this.length = paramBitArray.length;
    this.repn = ((byte[])(byte[])paramBitArray.repn.clone());
  }

  public boolean get(int paramInt)
    throws ArrayIndexOutOfBoundsException
  {
    if ((paramInt < 0) || (paramInt >= this.length))
      throw new ArrayIndexOutOfBoundsException(Integer.toString(paramInt));
    return ((this.repn[subscript(paramInt)] & position(paramInt)) != 0);
  }

  public void set(int paramInt, boolean paramBoolean)
    throws ArrayIndexOutOfBoundsException
  {
    if ((paramInt < 0) || (paramInt >= this.length))
      throw new ArrayIndexOutOfBoundsException(Integer.toString(paramInt));
    int i = subscript(paramInt);
    int j = position(paramInt);
    if (paramBoolean)
    {
      int tmp44_43 = i;
      byte[] tmp44_40 = this.repn;
      tmp44_40[tmp44_43] = (byte)(tmp44_40[tmp44_43] | j);
    }
    else
    {
      int tmp59_58 = i;
      byte[] tmp59_55 = this.repn;
      tmp59_55[tmp59_58] = (byte)(tmp59_55[tmp59_58] & (j ^ 0xFFFFFFFF));
    }
  }

  public int length()
  {
    return this.length;
  }

  public byte[] toByteArray()
  {
    return ((byte[])(byte[])this.repn.clone());
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject == this)
      return true;
    if ((paramObject == null) || (!(paramObject instanceof BitArray)))
      return false;
    BitArray localBitArray = (BitArray)paramObject;
    if (localBitArray.length != this.length)
      return false;
    for (int i = 0; i < this.repn.length; ++i)
      if (this.repn[i] != localBitArray.repn[i])
        return false;
    return true;
  }

  public boolean[] toBooleanArray()
  {
    boolean[] arrayOfBoolean = new boolean[this.length];
    for (int i = 0; i < this.length; ++i)
      arrayOfBoolean[i] = get(i);
    return arrayOfBoolean;
  }

  public int hashCode()
  {
    int i = 0;
    for (int j = 0; j < this.repn.length; ++j)
      i = 31 * i + this.repn[j];
    return (i ^ this.length);
  }

  public Object clone()
  {
    return new BitArray(this);
  }

  public String toString()
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    for (int i = 0; i < this.repn.length - 1; ++i)
    {
      localByteArrayOutputStream.write(NYBBLE[(this.repn[i] >> 4 & 0xF)], 0, 4);
      localByteArrayOutputStream.write(NYBBLE[(this.repn[i] & 0xF)], 0, 4);
      if (i % 8 == 7)
        localByteArrayOutputStream.write(10);
      else
        localByteArrayOutputStream.write(32);
    }
    for (i = 8 * (this.repn.length - 1); i < this.length; ++i)
      localByteArrayOutputStream.write((get(i)) ? 49 : 48);
    return new String(localByteArrayOutputStream.toByteArray());
  }
}