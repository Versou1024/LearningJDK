package sun.security.util;

import B;
import java.math.BigInteger;

public final class BigInt
{
  private byte[] places;
  private static final String digits = "0123456789abcdef";

  public BigInt(byte[] paramArrayOfByte)
  {
    this.places = ((byte[])(byte[])paramArrayOfByte.clone());
  }

  public BigInt(BigInteger paramBigInteger)
  {
    byte[] arrayOfByte = paramBigInteger.toByteArray();
    if ((arrayOfByte[0] & 0x80) != 0)
      throw new IllegalArgumentException("negative BigInteger");
    if (arrayOfByte[0] != 0)
    {
      this.places = arrayOfByte;
    }
    else
    {
      this.places = new byte[arrayOfByte.length - 1];
      for (int i = 1; i < arrayOfByte.length; ++i)
        this.places[(i - 1)] = arrayOfByte[i];
    }
  }

  public BigInt(int paramInt)
  {
    if (paramInt < 256)
    {
      this.places = new byte[1];
      this.places[0] = (byte)paramInt;
    }
    else if (paramInt < 65536)
    {
      this.places = new byte[2];
      this.places[0] = (byte)(paramInt >> 8);
      this.places[1] = (byte)paramInt;
    }
    else if (paramInt < 16777216)
    {
      this.places = new byte[3];
      this.places[0] = (byte)(paramInt >> 16);
      this.places[1] = (byte)(paramInt >> 8);
      this.places[2] = (byte)paramInt;
    }
    else
    {
      this.places = new byte[4];
      this.places[0] = (byte)(paramInt >> 24);
      this.places[1] = (byte)(paramInt >> 16);
      this.places[2] = (byte)(paramInt >> 8);
      this.places[3] = (byte)paramInt;
    }
  }

  public int toInt()
  {
    if (this.places.length > 4)
      throw new NumberFormatException("BigInt.toLong, too big");
    int i = 0;
    for (int j = 0; j < this.places.length; ++j)
      i = (i << 8) + (this.places[j] & 0xFF);
    return i;
  }

  public String toString()
  {
    return hexify();
  }

  public BigInteger toBigInteger()
  {
    return new BigInteger(1, this.places);
  }

  public byte[] toByteArray()
  {
    return ((byte[])(byte[])this.places.clone());
  }

  private String hexify()
  {
    if (this.places.length == 0)
      return "  0  ";
    StringBuffer localStringBuffer = new StringBuffer(this.places.length * 2);
    localStringBuffer.append("    ");
    for (int i = 0; i < this.places.length; ++i)
    {
      localStringBuffer.append("0123456789abcdef".charAt(this.places[i] >> 4 & 0xF));
      localStringBuffer.append("0123456789abcdef".charAt(this.places[i] & 0xF));
      if ((i + 1) % 32 == 0)
        if (i + 1 != this.places.length)
          localStringBuffer.append("\n    ");
      else if ((i + 1) % 4 == 0)
        localStringBuffer.append(' ');
    }
    return localStringBuffer.toString();
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof BigInt)
      return equals((BigInt)paramObject);
    return false;
  }

  public boolean equals(BigInt paramBigInt)
  {
    if (this == paramBigInt)
      return true;
    byte[] arrayOfByte = paramBigInt.toByteArray();
    if (this.places.length != arrayOfByte.length)
      return false;
    for (int i = 0; i < this.places.length; ++i)
      if (this.places[i] != arrayOfByte[i])
        return false;
    return true;
  }

  public int hashCode()
  {
    return hexify().hashCode();
  }
}