package sun.net.httpserver;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class ChunkedOutputStream extends FilterOutputStream
{
  private boolean closed = false;
  static final int CHUNK_SIZE = 4096;
  static final int OFFSET = 6;
  private int pos = 6;
  private int count = 0;
  private byte[] buf = new byte[4104];
  ExchangeImpl t;

  ChunkedOutputStream(ExchangeImpl paramExchangeImpl, OutputStream paramOutputStream)
  {
    super(paramOutputStream);
    this.t = paramExchangeImpl;
  }

  public void write(int paramInt)
    throws IOException
  {
    if (this.closed)
      throw new StreamClosedException();
    this.buf[(this.pos++)] = (byte)paramInt;
    this.count += 1;
    if (this.count == 4096)
      writeChunk();
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (this.closed)
      throw new StreamClosedException();
    int i = 4096 - this.count;
    if (paramInt2 > i)
    {
      System.arraycopy(paramArrayOfByte, paramInt1, this.buf, this.pos, i);
      this.count = 4096;
      writeChunk();
      paramInt2 -= i;
      paramInt1 += i;
      while (paramInt2 > 4096)
      {
        System.arraycopy(paramArrayOfByte, paramInt1, this.buf, 6, 4096);
        paramInt2 -= 4096;
        paramInt1 += 4096;
        this.count = 4096;
        writeChunk();
      }
      this.pos = 6;
    }
    if (paramInt2 > 0)
    {
      System.arraycopy(paramArrayOfByte, paramInt1, this.buf, this.pos, paramInt2);
      this.count += paramInt2;
      this.pos += paramInt2;
    }
  }

  private void writeChunk()
    throws IOException
  {
    char[] arrayOfChar = Integer.toHexString(this.count).toCharArray();
    int i = arrayOfChar.length;
    int j = 4 - i;
    for (int k = 0; k < i; ++k)
      this.buf[(j + k)] = (byte)arrayOfChar[k];
    this.buf[(j + k++)] = 13;
    this.buf[(j + k++)] = 10;
    this.buf[(j + k++ + this.count)] = 13;
    this.buf[(j + k++ + this.count)] = 10;
    this.out.write(this.buf, j, k + this.count);
    this.count = 0;
    this.pos = 6;
  }

  public void close()
    throws IOException
  {
    if (this.closed)
      return;
    flush();
    writeChunk();
    this.out.flush();
    this.closed = true;
    LeftOverInputStream localLeftOverInputStream = this.t.getOriginalInputStream();
    if (!(localLeftOverInputStream.isClosed()))
      try
      {
        localLeftOverInputStream.close();
      }
      catch (IOException localIOException)
      {
      }
    WriteFinishedEvent localWriteFinishedEvent = new WriteFinishedEvent(this.t);
    this.t.getHttpContext().getServerImpl().addEvent(localWriteFinishedEvent);
  }

  public void flush()
    throws IOException
  {
    if (this.closed)
      throw new StreamClosedException();
    if (this.count > 0)
      writeChunk();
    this.out.flush();
  }
}