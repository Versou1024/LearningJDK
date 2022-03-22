package sun.io;

public class CharToByteASCII extends CharToByteConverter
{
  private char highHalfZoneCode;

  public String getCharacterEncoding()
  {
    return "ASCII";
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
    byte[] arrayOfByte2 = new byte[1];
    this.charOff = paramInt1;
    this.byteOff = paramInt3;
    if (this.highHalfZoneCode != 0)
    {
      i = this.highHalfZoneCode;
      this.highHalfZoneCode = ';
      if ((paramArrayOfChar[paramInt1] >= 56320) && (paramArrayOfChar[paramInt1] <= 57343))
      {
        this.badInputLength = 1;
        throw new sun.io.UnknownCharacterException();
      }
      this.badInputLength = 0;
      throw new sun.io.MalformedInputException("Previous converted string ends with <High Half Zone Code> of UTF16 , but this string is not begin with <Low Half Zone>");
    }
    while (this.charOff < paramInt2)
    {
      byte[] arrayOfByte1 = arrayOfByte2;
      i = paramArrayOfChar[this.charOff];
      int k = 1;
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
            break label293:
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
      if (i <= 127)
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
        label293: throw new sun.io.ConversionBufferFullException();
      for (int l = 0; l < k; ++l)
        paramArrayOfByte[(this.byteOff++)] = arrayOfByte1[l];
      this.charOff += j;
    }
    return (this.byteOff - paramInt3);
  }

  public boolean canConvert(char paramChar)
  {
    return (paramChar <= '');
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