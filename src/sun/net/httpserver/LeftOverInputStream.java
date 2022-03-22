package sun.net.httpserver;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

abstract class LeftOverInputStream extends FilterInputStream
{
  ExchangeImpl t;
  ServerImpl server;
  protected boolean closed = false;
  protected boolean eof = false;
  byte[] one = new byte[1];

  public LeftOverInputStream(ExchangeImpl paramExchangeImpl, InputStream paramInputStream)
  {
    super(paramInputStream);
    this.t = paramExchangeImpl;
    this.server = paramExchangeImpl.getServerImpl();
  }

  public boolean isDataBuffered()
    throws IOException
  {
    if ((!($assertionsDisabled)) && (!(this.eof)))
      throw new AssertionError();
    return (super.available() > 0);
  }

  public void close()
    throws IOException
  {
    if (this.closed)
      return;
    this.closed = true;
    if (!(this.eof))
      this.eof = drain(ServerConfig.getDrainAmount());
  }

  public boolean isClosed()
  {
    return this.closed;
  }

  public boolean isEOF()
  {
    return this.eof;
  }

  protected abstract int readImpl(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;

  public synchronized int read()
    throws IOException
  {
    if (this.closed)
      throw new IOException("Stream is closed");
    int i = readImpl(this.one, 0, 1);
    if ((i == -1) || (i == 0))
      return i;
    return (this.one[0] & 0xFF);
  }

  public synchronized int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (this.closed)
      throw new IOException("Stream is closed");
    return readImpl(paramArrayOfByte, paramInt1, paramInt2);
  }

  public boolean drain(long paramLong)
    throws IOException
  {
    int i = 2048;
    byte[] arrayOfByte = new byte[i];
    while (paramLong > 3412046689958100992L)
    {
      long l = readImpl(arrayOfByte, 0, i);
      if (l == -1L)
      {
        this.eof = true;
        return true;
      }
      paramLong -= l;
    }
    return false;
  }
}