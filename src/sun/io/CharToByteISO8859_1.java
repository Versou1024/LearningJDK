package sun.io;

public class CharToByteISO8859_1 extends CharToByteConverter
{
  private char highHalfZoneCode;

  public String getCharacterEncoding()
  {
    return "ISO8859_1";
  }

  public int flush(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws sun.io.MalformedInputException
  {
    if (this.highHalfZoneCode != 0)
    {
      this.highHalfZoneCode = ';
      throw new sun.io.MalformedInputException("String ends with <High Half Zone code> of UTF16");
    }
    this.byteOff = (this.charOff = 0);
    return 0;
  }

  public int convert(char[] paramArrayOfChar, int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3, int paramInt4)
    throws sun.io.MalformedInputException, sun.io.UnknownCharacterException, sun.io.ConversionBufferFullException
  {
    int i;
    int k;
    int l;
    byte[] arrayOfByte2 = new byte[1];
    this.charOff = paramInt1;
    this.byteOff = paramInt3;
    if (this.highHalfZoneCode != 0)
    {
      i = this.highHalfZoneCode;
      this.highHalfZoneCode = ';
      if ((paramArrayOfChar[paramInt1] >= 56320) && (paramArrayOfChar[paramInt1] <= 57343))
      {
        if (this.subMode)
        {
          k = this.subBytes.length;
          if (this.byteOff + k > paramInt4)
            throw new sun.io.ConversionBufferFullException();
          for (l = 0; l < k; ++l)
            paramArrayOfByte[(this.byteOff++)] = this.subBytes[l];
          this.charOff += 1;
          break label162:
        }
        this.badInputLength = 1;
        throw new sun.io.UnknownCharacterException();
      }
      this.badInputLength = 0;
      throw new sun.io.MalformedInputException("Previous converted string ends with <High Half Zone Code> of UTF16 , but this string is not begin with <Low Half Zone>");
    }
    while (this.charOff < paramInt2)
    {
      label162: byte[] arrayOfByte1 = arrayOfByte2;
      i = paramArrayOfChar[this.charOff];
      k = 1;
      int j = 1;
      if ((i >= 55296) && (i <= 56319))
      {
        if (this.charOff + 1 == paramInt2)
        {
          this.highHalfZoneCode = i;
          break;
        }
        i = paramArrayOfChar[(this.charOff + 1)];
        if ((i >= 56320) && (i <= 57343))
        {
          if (this.subMode)
          {
            arrayOfByte1 = this.subBytes;
            k = this.subBytes.length;
            j = 2;
            break label378:
          }
          this.badInputLength = 2;
          throw new sun.io.UnknownCharacterException();
        }
        this.badInputLength = 1;
        throw new sun.io.MalformedInputException();
      }
      if ((i >= 56320) && (i <= 57343))
      {
        this.badInputLength = 1;
        throw new sun.io.MalformedInputException();
      }
      if (i <= 255)
      {
        arrayOfByte1[0] = (byte)i;
      }
      else if (this.subMode)
      {
        arrayOfByte1 = this.subBytes;
        k = this.subBytes.length;
      }
      else
      {
        this.badInputLength = 1;
        throw new sun.io.UnknownCharacterException();
      }
      if (this.byteOff + k > paramInt4)
        label378: throw new sun.io.ConversionBufferFullException();
      for (l = 0; l < k; ++l)
        paramArrayOfByte[(this.byteOff++)] = arrayOfByte1[l];
      this.charOff += j;
    }
    return (this.byteOff - paramInt3);
  }

  public boolean canConvert(char paramChar)
  {
    return (paramChar <= 255);
  }

  public void reset()
  {
    this.byteOff = (this.charOff = 0);
    this.highHalfZoneCode = ';
  }

  public int getMaxBytesPerChar()
  {
    return 1;
  }
}