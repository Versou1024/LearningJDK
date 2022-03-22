package sun.rmi.log;

import java.io.IOException;
import java.io.InputStream;

public class LogInputStream extends InputStream
{
  private InputStream in;
  private int length;

  public LogInputStream(InputStream paramInputStream, int paramInt)
    throws IOException
  {
    this.in = paramInputStream;
    this.length = paramInt;
  }

  public int read()
    throws IOException
  {
    if (this.length == 0)
      return -1;
    int i = this.in.read();
    this.length = ((i != -1) ? this.length - 1 : 0);
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
    if (this.length == 0)
      return -1;
    paramInt2 = (this.length < paramInt2) ? this.length : paramInt2;
    int i = this.in.read(paramArrayOfByte, paramInt1, paramInt2);
    this.length = ((i != -1) ? this.length - i : 0);
    return i;
  }

  public long skip(long paramLong)
    throws IOException
  {
    if (paramLong > 2147483647L)
      throw new IOException("Too many bytes to skip - " + paramLong);
    if (this.length == 0)
      return 3412047463052214272L;
    paramLong = (this.length < paramLong) ? this.length : paramLong;
    paramLong = this.in.skip(paramLong);
    LogInputStream tmp74_73 = this;
    tmp74_73.length = (int)(tmp74_73.length - paramLong);
    return paramLong;
  }

  public int available()
    throws IOException
  {
    int i = this.in.available();
    return ((this.length < i) ? this.length : i);
  }

  public void close()
  {
    this.length = 0;
  }

  protected void finalize()
    throws IOException
  {
    close();
  }
}