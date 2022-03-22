package sun.net.httpserver;

import java.io.IOException;
import java.io.InputStream;

class FixedLengthInputStream extends LeftOverInputStream
{
  private int remaining;

  FixedLengthInputStream(ExchangeImpl paramExchangeImpl, InputStream paramInputStream, int paramInt)
  {
    super(paramExchangeImpl, paramInputStream);
    this.remaining = paramInt;
  }

  protected int readImpl(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    this.eof = (this.remaining == 0);
    if (this.eof)
      return -1;
    if (paramInt2 > this.remaining)
      paramInt2 = this.remaining;
    int i = this.in.read(paramArrayOfByte, paramInt1, paramInt2);
    if (i > -1)
      this.remaining -= i;
    return i;
  }

  public int available()
    throws IOException
  {
    if (this.eof)
      return 0;
    int i = this.in.available();
    return ((i < this.remaining) ? i : this.remaining);
  }

  public boolean markSupported()
  {
    return false;
  }

  public void mark(int paramInt)
  {
  }

  public void reset()
    throws IOException
  {
    throw new IOException("mark/reset not supported");
  }
}