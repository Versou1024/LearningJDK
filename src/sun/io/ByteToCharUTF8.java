package sun.io;

public class ByteToCharUTF8 extends ByteToCharConverter
{
  private int savedSize = 0;
  private byte[] savedBytes = new byte[5];

  public int flush(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws sun.io.MalformedInputException
  {
    if (this.savedSize != 0)
    {
      this.savedSize = 0;
      this.badInputLength = 0;
      throw new sun.io.MalformedInputException();
    }
    this.byteOff = (this.charOff = 0);
    return 0;
  }

  public int convert(byte[] paramArrayOfByte, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3, int paramInt4)
    throws sun.io.MalformedInputException, sun.io.ConversionBufferFullException
  {
    int i4;
    char[] arrayOfChar = new char[2];
    int i2 = 0;
    if (this.savedSize != 0)
    {
      byte[] arrayOfByte = new byte[paramInt2 - paramInt1 + this.savedSize];
      for (i4 = 0; i4 < this.savedSize; ++i4)
        arrayOfByte[i4] = this.savedBytes[i4];
      System.arraycopy(paramArrayOfByte, paramInt1, arrayOfByte, this.savedSize, paramInt2 - paramInt1);
      paramArrayOfByte = arrayOfByte;
      paramInt1 = 0;
      paramInt2 = arrayOfByte.length;
      i2 = -this.savedSize;
      this.savedSize = 0;
    }
    this.charOff = paramInt3;
    this.byteOff = paramInt1;
    while (this.byteOff < paramInt2)
    {
      int i1;
      int i3 = this.byteOff;
      int i = paramArrayOfByte[(this.byteOff++)] & 0xFF;
      if ((i & 0x80) == 0)
      {
        arrayOfChar[0] = (char)i;
        i1 = 1;
      }
      else
      {
        int j;
        if ((i & 0xE0) == 192)
        {
          if (this.byteOff >= paramInt2)
          {
            this.savedSize = 1;
            this.savedBytes[0] = (byte)i;
            break;
          }
          j = paramArrayOfByte[(this.byteOff++)] & 0xFF;
          if ((j & 0xC0) != 128)
          {
            this.badInputLength = 2;
            this.byteOff += i2;
            throw new sun.io.MalformedInputException();
          }
          arrayOfChar[0] = (char)((i & 0x1F) << 6 | j & 0x3F);
          i1 = 1;
        }
        else
        {
          int k;
          if ((i & 0xF0) == 224)
          {
            if (this.byteOff + 1 >= paramInt2)
            {
              this.savedBytes[0] = (byte)i;
              if (this.byteOff >= paramInt2)
              {
                this.savedSize = 1;
                break;
              }
              this.savedSize = 2;
              this.savedBytes[1] = paramArrayOfByte[(this.byteOff++)];
              break;
            }
            j = paramArrayOfByte[(this.byteOff++)] & 0xFF;
            k = paramArrayOfByte[(this.byteOff++)] & 0xFF;
            if (((j & 0xC0) != 128) || ((k & 0xC0) != 128))
            {
              this.badInputLength = 3;
              this.byteOff += i2;
              throw new sun.io.MalformedInputException();
            }
            arrayOfChar[0] = (char)((i & 0xF) << 12 | (j & 0x3F) << 6 | k & 0x3F);
            i1 = 1;
          }
          else if ((i & 0xF8) == 240)
          {
            if (this.byteOff + 2 >= paramInt2)
            {
              this.savedBytes[0] = (byte)i;
              if (this.byteOff >= paramInt2)
              {
                this.savedSize = 1;
                break;
              }
              if (this.byteOff + 1 >= paramInt2)
              {
                this.savedSize = 2;
                this.savedBytes[1] = paramArrayOfByte[(this.byteOff++)];
                break;
              }
              this.savedSize = 3;
              this.savedBytes[1] = paramArrayOfByte[(this.byteOff++)];
              this.savedBytes[2] = paramArrayOfByte[(this.byteOff++)];
              break;
            }
            j = paramArrayOfByte[(this.byteOff++)] & 0xFF;
            k = paramArrayOfByte[(this.byteOff++)] & 0xFF;
            int l = paramArrayOfByte[(this.byteOff++)] & 0xFF;
            if (((j & 0xC0) != 128) || ((k & 0xC0) != 128) || ((l & 0xC0) != 128))
            {
              this.badInputLength = 4;
              this.byteOff += i2;
              throw new sun.io.MalformedInputException();
            }
            i4 = (0x7 & i) << 18 | (0x3F & j) << 12 | (0x3F & k) << 6 | 0x3F & l;
            arrayOfChar[0] = (char)((i4 - 65536) / 1024 + 55296);
            arrayOfChar[1] = (char)((i4 - 65536) % 1024 + 56320);
            i1 = 2;
          }
          else
          {
            this.badInputLength = 1;
            this.byteOff += i2;
            throw new sun.io.MalformedInputException();
          }
        }
      }
      if (this.charOff + i1 > paramInt4)
      {
        this.byteOff = i3;
        this.byteOff += i2;
        throw new sun.io.ConversionBufferFullException();
      }
      for (i4 = 0; i4 < i1; ++i4)
        paramArrayOfChar[(this.charOff + i4)] = arrayOfChar[i4];
      this.charOff += i1;
    }
    this.byteOff += i2;
    return (this.charOff - paramInt3);
  }

  public String getCharacterEncoding()
  {
    return "UTF8";
  }

  public void reset()
  {
    this.byteOff = (this.charOff = 0);
    this.savedSize = 0;
  }
}