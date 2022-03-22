package sun.io;

public class CharToByteUTF8 extends CharToByteConverter
{
  private char highHalfZoneCode;

  public int flush(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws sun.io.MalformedInputException
  {
    if (this.highHalfZoneCode != 0)
    {
      this.highHalfZoneCode = ';
      this.badInputLength = 0;
      throw new sun.io.MalformedInputException();
    }
    this.byteOff = (this.charOff = 0);
    return 0;
  }

  public int convert(char[] paramArrayOfChar, int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3, int paramInt4)
    throws sun.io.ConversionBufferFullException, sun.io.MalformedInputException
  {
    int i;
    int l;
    byte[] arrayOfByte = new byte[6];
    this.charOff = paramInt1;
    this.byteOff = paramInt3;
    if (this.highHalfZoneCode != 0)
    {
      i = this.highHalfZoneCode;
      this.highHalfZoneCode = ';
      if ((paramArrayOfChar[paramInt1] >= 56320) && (paramArrayOfChar[paramInt1] <= 57343))
      {
        l = (this.highHalfZoneCode - 55296) * 1024 + paramArrayOfChar[paramInt1] - 56320 + 65536;
        paramArrayOfByte[0] = (byte)(0xF0 | l >> 18 & 0x7);
        paramArrayOfByte[1] = (byte)(0x80 | l >> 12 & 0x3F);
        paramArrayOfByte[2] = (byte)(0x80 | l >> 6 & 0x3F);
        paramArrayOfByte[3] = (byte)(0x80 | l & 0x3F);
        this.charOff += 1;
        this.highHalfZoneCode = ';
      }
      else
      {
        this.badInputLength = 0;
        throw new sun.io.MalformedInputException();
      }
    }
    while (this.charOff < paramInt2)
    {
      int j;
      int k;
      i = paramArrayOfChar[this.charOff];
      if (i < 128)
      {
        arrayOfByte[0] = (byte)i;
        j = 1;
        k = 1;
      }
      else if (i < 2048)
      {
        arrayOfByte[0] = (byte)(0xC0 | i >> 6 & 0x1F);
        arrayOfByte[1] = (byte)(0x80 | i & 0x3F);
        j = 1;
        k = 2;
      }
      else if ((i >= 55296) && (i <= 56319))
      {
        if (this.charOff + 1 >= paramInt2)
        {
          this.highHalfZoneCode = i;
          break;
        }
        l = paramArrayOfChar[(this.charOff + 1)];
        if ((l < 56320) || (l > 57343))
        {
          this.badInputLength = 1;
          throw new sun.io.MalformedInputException();
        }
        int i1 = (i - 55296) * 1024 + l - 56320 + 65536;
        arrayOfByte[0] = (byte)(0xF0 | i1 >> 18 & 0x7);
        arrayOfByte[1] = (byte)(0x80 | i1 >> 12 & 0x3F);
        arrayOfByte[2] = (byte)(0x80 | i1 >> 6 & 0x3F);
        arrayOfByte[3] = (byte)(0x80 | i1 & 0x3F);
        k = 4;
        j = 2;
      }
      else
      {
        arrayOfByte[0] = (byte)(0xE0 | i >> 12 & 0xF);
        arrayOfByte[1] = (byte)(0x80 | i >> 6 & 0x3F);
        arrayOfByte[2] = (byte)(0x80 | i & 0x3F);
        j = 1;
        k = 3;
      }
      if (this.byteOff + k > paramInt4)
        throw new sun.io.ConversionBufferFullException();
      for (l = 0; l < k; ++l)
        paramArrayOfByte[(this.byteOff++)] = arrayOfByte[l];
      this.charOff += j;
    }
    return (this.byteOff - paramInt3);
  }

  public boolean canConvert(char paramChar)
  {
    return true;
  }

  public int getMaxBytesPerChar()
  {
    return 3;
  }

  public void reset()
  {
    this.byteOff = (this.charOff = 0);
    this.highHalfZoneCode = ';
  }

  public String getCharacterEncoding()
  {
    return "UTF8";
  }
}