package sun.net.www;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import sun.net.ProgressSource;
import sun.net.www.http.ChunkedInputStream;

public class MeteredStream extends FilterInputStream
{
  protected boolean closed = false;
  protected int expected;
  protected int count = 0;
  protected int markedCount = 0;
  protected int markLimit = -1;
  protected ProgressSource pi;

  public MeteredStream(InputStream paramInputStream, ProgressSource paramProgressSource, int paramInt)
  {
    super(paramInputStream);
    this.pi = paramProgressSource;
    this.expected = paramInt;
    if (paramProgressSource != null)
      paramProgressSource.updateProgress(0, paramInt);
  }

  private final void justRead(int paramInt)
    throws IOException
  {
    if (paramInt == -1)
    {
      if (!(isMarked()))
        close();
      return;
    }
    this.count += paramInt;
    if (this.count - this.markedCount > this.markLimit)
      this.markLimit = -1;
    if (this.pi != null)
      this.pi.updateProgress(this.count, this.expected);
    if (isMarked())
      return;
    if ((this.expected > 0) && (this.count >= this.expected))
      close();
  }

  private boolean isMarked()
  {
    if (this.markLimit < 0)
      return false;
    return (this.count - this.markedCount <= this.markLimit);
  }

  public synchronized int read()
    throws IOException
  {
    if (this.closed)
      return -1;
    int i = this.in.read();
    if (i != -1)
      justRead(1);
    else
      justRead(i);
    return i;
  }

  public synchronized int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (this.closed)
      return -1;
    int i = this.in.read(paramArrayOfByte, paramInt1, paramInt2);
    justRead(i);
    return i;
  }

  public synchronized long skip(long paramLong)
    throws IOException
  {
    if (this.closed)
      return 3412047463052214272L;
    if (this.in instanceof ChunkedInputStream)
    {
      paramLong = this.in.skip(paramLong);
    }
    else
    {
      int i = (paramLong > this.expected - this.count) ? this.expected - this.count : (int)paramLong;
      paramLong = this.in.skip(i);
    }
    justRead((int)paramLong);
    return paramLong;
  }

  public void close()
    throws IOException
  {
    if (this.closed)
      return;
    if (this.pi != null)
      this.pi.finishTracking();
    this.closed = true;
    this.in.close();
  }

  public synchronized int available()
    throws IOException
  {
    return ((this.closed) ? 0 : this.in.available());
  }

  public synchronized void mark(int paramInt)
  {
    if (this.closed)
      return;
    super.mark(paramInt);
    this.markedCount = this.count;
    this.markLimit = paramInt;
  }

  public synchronized void reset()
    throws IOException
  {
    if (this.closed)
      return;
    if (!(isMarked()))
      throw new IOException("Resetting to an invalid mark");
    this.count = this.markedCount;
    super.reset();
  }

  public boolean markSupported()
  {
    if (this.closed)
      return false;
    return super.markSupported();
  }

  protected void finalize()
    throws Throwable
  {
    try
    {
      close();
      if (this.pi != null)
        this.pi.close();
    }
    finally
    {
      finalize();
    }
  }
}