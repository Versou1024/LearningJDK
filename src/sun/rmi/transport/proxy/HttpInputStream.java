package sun.rmi.transport.proxy;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import sun.rmi.runtime.Log;

class HttpInputStream extends FilterInputStream
{
  protected int bytesLeft;
  protected int bytesLeftAtMark;

  public HttpInputStream(InputStream paramInputStream)
    throws IOException
  {
    super(paramInputStream);
    if (paramInputStream.markSupported())
      paramInputStream.mark(0);
    DataInputStream localDataInputStream = new DataInputStream(paramInputStream);
    String str1 = "Content-length:".toLowerCase();
    int i = 0;
    do
    {
      str2 = localDataInputStream.readLine();
      if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE))
        RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE, "received header line: \"" + str2 + "\"");
      if (str2 == null)
        throw new EOFException();
      if (str2.toLowerCase().startsWith(str1))
      {
        if (i != 0);
        this.bytesLeft = Integer.parseInt(str2.substring(str1.length()).trim());
        i = 1;
      }
    }
    while ((str2.length() != 0) && (str2.charAt(0) != '\r') && (str2.charAt(0) != '\n'));
    if ((i == 0) || (this.bytesLeft < 0))
      this.bytesLeft = 2147483647;
    this.bytesLeftAtMark = this.bytesLeft;
    if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE))
      RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE, "content length: " + this.bytesLeft);
  }

  public int available()
    throws IOException
  {
    int i = this.in.available();
    if (i > this.bytesLeft)
      i = this.bytesLeft;
    return i;
  }

  public int read()
    throws IOException
  {
    if (this.bytesLeft > 0)
    {
      int i = this.in.read();
      if (i != -1)
        this.bytesLeft -= 1;
      if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE))
        RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE, "received byte: '" + (((i & 0x7F) < 32) ? " " : String.valueOf((char)i)) + "' " + i);
      return i;
    }
    RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE, "read past content length");
    return -1;
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if ((this.bytesLeft == 0) && (paramInt2 > 0))
    {
      RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE, "read past content length");
      return -1;
    }
    if (paramInt2 > this.bytesLeft)
      paramInt2 = this.bytesLeft;
    int i = this.in.read(paramArrayOfByte, paramInt1, paramInt2);
    this.bytesLeft -= i;
    if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE))
      RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE, "read " + i + " bytes, " + this.bytesLeft + " remaining");
    return i;
  }

  public void mark(int paramInt)
  {
    this.in.mark(paramInt);
    if (this.in.markSupported())
      this.bytesLeftAtMark = this.bytesLeft;
  }

  public void reset()
    throws IOException
  {
    this.in.reset();
    this.bytesLeft = this.bytesLeftAtMark;
  }

  public long skip(long paramLong)
    throws IOException
  {
    if (paramLong > this.bytesLeft)
      paramLong = this.bytesLeft;
    long l = this.in.skip(paramLong);
    HttpInputStream tmp26_25 = this;
    tmp26_25.bytesLeft = (int)(tmp26_25.bytesLeft - tmp26_25);
    return tmp26_25;
  }
}