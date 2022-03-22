package sun.net;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TelnetInputStream extends FilterInputStream
{
  boolean stickyCRLF = false;
  boolean seenCR = false;
  public boolean binaryMode = false;

  public TelnetInputStream(InputStream paramInputStream, boolean paramBoolean)
  {
    super(paramInputStream);
    this.binaryMode = paramBoolean;
  }

  public void setStickyCRLF(boolean paramBoolean)
  {
    this.stickyCRLF = paramBoolean;
  }

  public int read()
    throws IOException
  {
    int i;
    if (this.binaryMode)
      return super.read();
    if (this.seenCR)
    {
      this.seenCR = false;
      return 10;
    }
    if ((i = super.read()) == 13)
    {
      switch (i = super.read())
      {
      case -1:
      default:
        throw new TelnetProtocolException("misplaced CR in input");
      case 0:
        return 13;
      case 10:
      }
      if (this.stickyCRLF)
      {
        this.seenCR = true;
        return 13;
      }
      return 10;
    }
    return i;
  }

  public int read(byte[] paramArrayOfByte)
    throws IOException
  {
    return read(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (this.binaryMode)
      return super.read(paramArrayOfByte, paramInt1, paramInt2);
    int j = paramInt1;
    while (--paramInt2 >= 0)
    {
      int i = read();
      if (i == -1)
        break;
      paramArrayOfByte[(paramInt1++)] = (byte)i;
    }
    return ((paramInt1 > j) ? paramInt1 - j : -1);
  }
}