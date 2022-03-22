package sun.io;

public class ByteToCharISO8859_1 extends ByteToCharConverter
{
  public String getCharacterEncoding()
  {
    return "ISO8859_1";
  }

  public int flush(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    this.byteOff = (this.charOff = 0);
    return 0;
  }

  public int convert(byte[] paramArrayOfByte, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3, int paramInt4)
    throws sun.io.ConversionBufferFullException
  {
    int i = paramInt1 + paramInt4 - paramInt3;
    if (i >= paramInt2)
      i = paramInt2;
    int j = paramInt2 - paramInt1;
    try
    {
      while (paramInt1 < i)
        paramArrayOfChar[(paramInt3++)] = (char)(0xFF & paramArrayOfByte[(paramInt1++)]);
    }
    finally
    {
      this.charOff = paramInt3;
      this.byteOff = paramInt1;
    }
    if (i < paramInt2)
      throw new sun.io.ConversionBufferFullException();
    return j;
  }

  public void reset()
  {
    this.byteOff = (this.charOff = 0);
  }
}