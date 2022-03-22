package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.Gregorian;

class DerInputBuffer extends ByteArrayInputStream
  implements Cloneable
{
  DerInputBuffer(byte[] paramArrayOfByte)
  {
    super(paramArrayOfByte);
  }

  DerInputBuffer(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    super(paramArrayOfByte, paramInt1, paramInt2);
  }

  DerInputBuffer dup()
  {
    DerInputBuffer localDerInputBuffer;
    try
    {
      localDerInputBuffer = (DerInputBuffer)clone();
      localDerInputBuffer.mark(2147483647);
      return localDerInputBuffer;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      throw new IllegalArgumentException(localCloneNotSupportedException.toString());
    }
  }

  byte[] toByteArray()
  {
    int i = available();
    if (i <= 0)
      return null;
    byte[] arrayOfByte = new byte[i];
    System.arraycopy(this.buf, this.pos, arrayOfByte, 0, i);
    return arrayOfByte;
  }

  int peek()
    throws IOException
  {
    if (this.pos >= this.count)
      throw new IOException("out of data");
    return this.buf[this.pos];
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof DerInputBuffer)
      return equals((DerInputBuffer)paramObject);
    return false;
  }

  boolean equals(DerInputBuffer paramDerInputBuffer)
  {
    if (this == paramDerInputBuffer)
      return true;
    int i = available();
    if (paramDerInputBuffer.available() != i)
      return false;
    for (int j = 0; j < i; ++j)
      if (this.buf[(this.pos + j)] != paramDerInputBuffer.buf[(paramDerInputBuffer.pos + j)])
        return false;
    return true;
  }

  public int hashCode()
  {
    int i = 0;
    int j = available();
    int k = this.pos;
    for (int l = 0; l < j; ++l)
      i += this.buf[(k + l)] * l;
    return i;
  }

  void truncate(int paramInt)
    throws IOException
  {
    if (paramInt > available())
      throw new IOException("insufficient data");
    this.count = (this.pos + paramInt);
  }

  BigInteger getBigInteger(int paramInt, boolean paramBoolean)
    throws IOException
  {
    if (paramInt > available())
      throw new IOException("short read of integer");
    if (paramInt == 0)
      throw new IOException("Invalid encoding: zero length Int value");
    byte[] arrayOfByte = new byte[paramInt];
    System.arraycopy(this.buf, this.pos, arrayOfByte, 0, paramInt);
    skip(paramInt);
    if (paramBoolean)
      return new BigInteger(1, arrayOfByte);
    return new BigInteger(arrayOfByte);
  }

  public int getInteger(int paramInt)
    throws IOException
  {
    BigInteger localBigInteger = getBigInteger(paramInt, false);
    if (localBigInteger.compareTo(BigInteger.valueOf(-2147483648L)) < 0)
      throw new IOException("Integer below minimum valid value");
    if (localBigInteger.compareTo(BigInteger.valueOf(2147483647L)) > 0)
      throw new IOException("Integer exceeds maximum valid value");
    return localBigInteger.intValue();
  }

  public byte[] getBitString(int paramInt)
    throws IOException
  {
    if (paramInt > available())
      throw new IOException("short read of bit string");
    if (paramInt == 0)
      throw new IOException("Invalid encoding: zero length bit string");
    int i = this.buf[this.pos];
    if ((i < 0) || (i > 7))
      throw new IOException("Invalid number of padding bits");
    byte[] arrayOfByte = new byte[paramInt - 1];
    System.arraycopy(this.buf, this.pos + 1, arrayOfByte, 0, paramInt - 1);
    if (i != 0)
    {
      int tmp94_93 = (paramInt - 2);
      byte[] tmp94_90 = arrayOfByte;
      tmp94_90[tmp94_93] = (byte)(tmp94_90[tmp94_93] & 255 << i);
    }
    skip(paramInt);
    return arrayOfByte;
  }

  byte[] getBitString()
    throws IOException
  {
    return getBitString(available());
  }

  BitArray getUnalignedBitString()
    throws IOException
  {
    if (this.pos >= this.count)
      return null;
    int i = available();
    int j = this.buf[this.pos] & 0xFF;
    if (j > 7)
      throw new IOException("Invalid value for unused bits: " + j);
    byte[] arrayOfByte = new byte[i - 1];
    int k = (arrayOfByte.length == 0) ? 0 : arrayOfByte.length * 8 - j;
    System.arraycopy(this.buf, this.pos + 1, arrayOfByte, 0, i - 1);
    BitArray localBitArray = new BitArray(k, arrayOfByte);
    this.pos = this.count;
    return localBitArray;
  }

  public Date getUTCTime(int paramInt)
    throws IOException
  {
    if (paramInt > available())
      throw new IOException("short read of DER UTC Time");
    if ((paramInt < 11) || (paramInt > 17))
      throw new IOException("DER UTC Time length error");
    return getTime(paramInt, false);
  }

  public Date getGeneralizedTime(int paramInt)
    throws IOException
  {
    if (paramInt > available())
      throw new IOException("short read of DER Generalized Time");
    if ((paramInt < 13) || (paramInt > 23))
      throw new IOException("DER Generalized Time length error");
    return getTime(paramInt, true);
  }

  private Date getTime(int paramInt, boolean paramBoolean)
    throws IOException
  {
    int i;
    int i2;
    int i6;
    int i7;
    String str = null;
    if (paramBoolean)
    {
      str = "Generalized";
      i = 1000 * Character.digit((char)this.buf[(this.pos++)], 10);
      i += 100 * Character.digit((char)this.buf[(this.pos++)], 10);
      i += 10 * Character.digit((char)this.buf[(this.pos++)], 10);
      i += Character.digit((char)this.buf[(this.pos++)], 10);
      paramInt -= 2;
    }
    else
    {
      str = "UTC";
      i = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
      i += Character.digit((char)this.buf[(this.pos++)], 10);
      if (i < 50)
        i += 2000;
      else
        i += 1900;
    }
    int j = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
    j += Character.digit((char)this.buf[(this.pos++)], 10);
    int k = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
    k += Character.digit((char)this.buf[(this.pos++)], 10);
    int l = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
    l += Character.digit((char)this.buf[(this.pos++)], 10);
    int i1 = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
    i1 += Character.digit((char)this.buf[(this.pos++)], 10);
    paramInt -= 10;
    int i3 = 0;
    if ((paramInt > 2) && (paramInt < 12))
    {
      i2 = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
      i2 += Character.digit((char)this.buf[(this.pos++)], 10);
      paramInt -= 2;
      if ((this.buf[this.pos] == 46) || (this.buf[this.pos] == 44))
      {
        --paramInt;
        this.pos += 1;
        int i4 = 0;
        int i5 = this.pos;
        while ((this.buf[i5] != 90) && (this.buf[i5] != 43) && (this.buf[i5] != 45))
        {
          ++i5;
          ++i4;
        }
        switch (i4)
        {
        case 3:
          i3 += 100 * Character.digit((char)this.buf[(this.pos++)], 10);
          i3 += 10 * Character.digit((char)this.buf[(this.pos++)], 10);
          i3 += Character.digit((char)this.buf[(this.pos++)], 10);
          break;
        case 2:
          i3 += 100 * Character.digit((char)this.buf[(this.pos++)], 10);
          i3 += 10 * Character.digit((char)this.buf[(this.pos++)], 10);
          break;
        case 1:
          i3 += 100 * Character.digit((char)this.buf[(this.pos++)], 10);
          break;
        default:
          throw new IOException("Parse " + str + " time, unsupported precision for seconds value");
        }
        paramInt -= i4;
      }
    }
    else
    {
      i2 = 0;
    }
    if ((j == 0) || (k == 0) || (j > 12) || (k > 31) || (l >= 24) || (i1 >= 60) || (i2 >= 60))
      throw new IOException("Parse " + str + " time, invalid format");
    Gregorian localGregorian = CalendarSystem.getGregorianCalendar();
    CalendarDate localCalendarDate = localGregorian.newCalendarDate(null);
    localCalendarDate.setDate(i, j, k);
    localCalendarDate.setTimeOfDay(l, i1, i2, i3);
    long l1 = localGregorian.getTime(localCalendarDate);
    if ((paramInt != 1) && (paramInt != 5))
      throw new IOException("Parse " + str + " time, invalid offset");
    switch (this.buf[(this.pos++)])
    {
    case 43:
      i6 = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
      i6 += Character.digit((char)this.buf[(this.pos++)], 10);
      i7 = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
      i7 += Character.digit((char)this.buf[(this.pos++)], 10);
      if ((i6 >= 24) || (i7 >= 60))
        throw new IOException("Parse " + str + " time, +hhmm");
      l1 -= (i6 * 60 + i7) * 60 * 1000;
      break;
    case 45:
      i6 = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
      i6 += Character.digit((char)this.buf[(this.pos++)], 10);
      i7 = 10 * Character.digit((char)this.buf[(this.pos++)], 10);
      i7 += Character.digit((char)this.buf[(this.pos++)], 10);
      if ((i6 >= 24) || (i7 >= 60))
        throw new IOException("Parse " + str + " time, -hhmm");
      l1 += (i6 * 60 + i7) * 60 * 1000;
      break;
    case 90:
      break;
    default:
      throw new IOException("Parse " + str + " time, garbage offset");
    }
    return new Date(l1);
  }
}