package sun.net.www.http;

import java.io.PrintStream;

public class ChunkedOutputStream extends PrintStream
{
  static final int DEFAULT_CHUNK_SIZE = 4096;
  private byte[] buf;
  private int count;
  private PrintStream out;
  private int preferredChunkSize;
  static final int MAX_BUF_SIZE = 10240;

  private int headerSize(int paramInt)
  {
    return (2 + Integer.toHexString(paramInt).length());
  }

  public ChunkedOutputStream(PrintStream paramPrintStream)
  {
    this(paramPrintStream, 4096);
  }

  public ChunkedOutputStream(PrintStream paramPrintStream, int paramInt)
  {
    super(paramPrintStream);
    this.out = paramPrintStream;
    if (paramInt <= 0)
      paramInt = 4096;
    if (paramInt > 0)
    {
      int i = paramInt - headerSize(paramInt);
      if (i + headerSize(i) < paramInt)
        ++i;
      paramInt = i;
    }
    if (paramInt > 0)
      this.preferredChunkSize = paramInt;
    else
      this.preferredChunkSize = (4096 - headerSize(4096));
    this.buf = new byte[this.preferredChunkSize + 32];
  }

  private void flush(byte[] paramArrayOfByte, boolean paramBoolean)
  {
    flush(paramArrayOfByte, paramBoolean, 0);
  }

  private void flush(byte[] paramArrayOfByte, boolean paramBoolean, int paramInt)
  {
    do
    {
      int i;
      if (this.count < this.preferredChunkSize)
      {
        if (!(paramBoolean))
          break;
        i = this.count;
      }
      else
      {
        i = this.preferredChunkSize;
      }
      byte[] arrayOfByte = Integer.toHexString(i).getBytes();
      this.out.write(arrayOfByte, 0, arrayOfByte.length);
      this.out.write(13);
      this.out.write(10);
      if (i > 0)
      {
        this.out.write(paramArrayOfByte, paramInt, i);
        this.out.write(13);
        this.out.write(10);
      }
      this.out.flush();
      if (checkError())
        break;
      if (i > 0)
      {
        this.count -= i;
        paramInt += i;
      }
    }
    while (this.count > 0);
    if ((!(checkError())) && (this.count > 0))
      System.arraycopy(paramArrayOfByte, paramInt, this.buf, 0, this.count);
  }

  public boolean checkError()
  {
    return this.out.checkError();
  }

  private void checkFlush()
  {
    if (this.count >= this.preferredChunkSize)
      flush(this.buf, false);
  }

  private void ensureOpen()
  {
    if (this.out == null)
      setError();
  }

  public synchronized void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    ensureOpen();
    if ((paramInt1 < 0) || (paramInt1 > paramArrayOfByte.length) || (paramInt2 < 0) || (paramInt1 + paramInt2 > paramArrayOfByte.length) || (paramInt1 + paramInt2 < 0))
      throw new IndexOutOfBoundsException();
    if (paramInt2 == 0)
      return;
    int i = this.preferredChunkSize - this.count;
    if ((paramInt2 > 10240) && (paramInt2 > i))
    {
      if (this.count == 0)
      {
        this.count = paramInt2;
        flush(paramArrayOfByte, false, paramInt1);
        return;
      }
      if (i > 0)
      {
        System.arraycopy(paramArrayOfByte, paramInt1, this.buf, this.count, i);
        this.count = this.preferredChunkSize;
        flush(this.buf, false);
      }
      this.count = (paramInt2 - i);
      flush(paramArrayOfByte, false, i + paramInt1);
    }
    else
    {
      int j = this.count + paramInt2;
      if (j > this.buf.length)
      {
        byte[] arrayOfByte = new byte[Math.max(this.buf.length << 1, j)];
        System.arraycopy(this.buf, 0, arrayOfByte, 0, this.count);
        this.buf = arrayOfByte;
      }
      System.arraycopy(paramArrayOfByte, paramInt1, this.buf, this.count, paramInt2);
      this.count = j;
      checkFlush();
    }
  }

  public synchronized void write(int paramInt)
  {
    ensureOpen();
    int i = this.count + 1;
    if (i > this.buf.length)
    {
      byte[] arrayOfByte = new byte[Math.max(this.buf.length << 1, i)];
      System.arraycopy(this.buf, 0, arrayOfByte, 0, this.count);
      this.buf = arrayOfByte;
    }
    this.buf[this.count] = (byte)paramInt;
    this.count = i;
    checkFlush();
  }

  public synchronized void reset()
  {
    this.count = 0;
  }

  public int size()
  {
    return this.count;
  }

  public synchronized void close()
  {
    ensureOpen();
    if (this.count > 0)
      flush(this.buf, true);
    flush(this.buf, true);
    this.out = null;
  }

  public synchronized void flush()
  {
    ensureOpen();
    if (this.count > 0)
      flush(this.buf, true);
  }
}