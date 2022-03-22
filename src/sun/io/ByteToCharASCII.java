package sun.io;

public class ByteToCharASCII extends ByteToCharConverter
{
  public String getCharacterEncoding()
  {
    return "ASCII";
  }

  public int flush(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    this.byteOff = (this.charOff = 0);
    return 0;
  }

  public int convert(byte[] paramArrayOfByte, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3, int paramInt4)
    throws sun.io.ConversionBufferFullException, sun.io.UnknownCharacterException
  {
    this.charOff = paramInt3;
    this.byteOff = paramInt1;
    while (true)
    {
      while (true)
      {
        if (this.byteOff >= paramInt2)
          break label115;
        if (this.charOff >= paramInt4)
          throw new sun.io.ConversionBufferFullException();
        int i = paramArrayOfByte[(this.byteOff++)];
        if (i < 0)
          break;
        paramArrayOfChar[(this.charOff++)] = (char)i;
      }
      if (!(this.subMode))
        break;
      paramArrayOfChar[(this.charOff++)] = 65533;
    }
    this.badInputLength = 1;
    throw new sun.io.UnknownCharacterException();
    label115: return (this.charOff - paramInt3);
  }

  public void reset()
  {
    this.byteOff = (this.charOff = 0);
  }
}