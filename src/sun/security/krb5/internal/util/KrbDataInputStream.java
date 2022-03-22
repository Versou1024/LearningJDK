package sun.security.krb5.internal.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class KrbDataInputStream extends BufferedInputStream
{
  private boolean bigEndian = true;

  public void setNativeByteOrder()
  {
    if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN))
      this.bigEndian = true;
    else
      this.bigEndian = false;
  }

  public KrbDataInputStream(InputStream paramInputStream)
  {
    super(paramInputStream);
  }

  public int read(int paramInt)
    throws IOException
  {
    byte[] arrayOfByte = new byte[paramInt];
    read(arrayOfByte, 0, paramInt);
    int i = 0;
    for (int j = 0; j < paramInt; ++j)
      if (this.bigEndian)
        i |= (arrayOfByte[j] & 0xFF) << (paramInt - j - 1) * 8;
      else
        i |= (arrayOfByte[j] & 0xFF) << j * 8;
    return i;
  }

  public int readVersion()
    throws IOException
  {
    int i = (read() & 0xFF) << 8;
    return (i | read() & 0xFF);
  }
}