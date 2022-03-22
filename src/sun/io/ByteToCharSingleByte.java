package sun.io;

public abstract class ByteToCharSingleByte extends ByteToCharConverter
{
  protected String byteToCharTable;

  public String getByteToCharTable()
  {
    return this.byteToCharTable;
  }

  public int flush(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    this.byteOff = (this.charOff = 0);
    return 0;
  }

  public int convert(byte[] paramArrayOfByte, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3, int paramInt4)
    throws sun.io.UnknownCharacterException, sun.io.MalformedInputException, sun.io.ConversionBufferFullException
  {
    this.charOff = paramInt3;
    this.byteOff = paramInt1;
    while (this.byteOff < paramInt2)
    {
      int j = paramArrayOfByte[this.byteOff];
      int i = getUnicode(j);
      if (i == 65533)
        if (this.subMode)
        {
          i = this.subChars[0];
        }
        else
        {
          this.badInputLength = 1;
          throw new sun.io.UnknownCharacterException();
        }
      if (this.charOff >= paramInt4)
        throw new sun.io.ConversionBufferFullException();
      paramArrayOfChar[this.charOff] = i;
      this.charOff += 1;
      this.byteOff += 1;
    }
    return (this.charOff - paramInt3);
  }

  protected char getUnicode(int paramInt)
  {
    int i = paramInt + 128;
    if ((i >= this.byteToCharTable.length()) || (i < 0))
      return 65533;
    return this.byteToCharTable.charAt(i);
  }

  public void reset()
  {
    this.byteOff = (this.charOff = 0);
  }
}