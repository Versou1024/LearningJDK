package sun.net.httpserver;

import com.sun.net.httpserver.Headers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

class Request
{
  static final int BUF_LEN = 2048;
  static final byte CR = 13;
  static final byte LF = 10;
  private String startLine;
  private SocketChannel chan = this.chan;
  private InputStream is;
  private OutputStream os;
  char[] buf = new char[2048];
  int pos;
  StringBuffer lineBuf;
  Headers hdrs = null;

  Request(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    this.is = paramInputStream;
    this.os = paramOutputStream;
    do
      this.startLine = readLine();
    while (this.startLine.equals(""));
  }

  public InputStream inputStream()
  {
    return this.is;
  }

  public OutputStream outputStream()
  {
    return this.os;
  }

  public String readLine()
    throws IOException
  {
    int i = 0;
    int j = 0;
    this.pos = 0;
    this.lineBuf = new StringBuffer();
    while (j == 0)
    {
      int k = this.is.read();
      if (k == -1)
        return null;
      if (i != 0)
        if (k == 10)
        {
          j = 1;
        }
        else
        {
          i = 0;
          consume(13);
          consume(k);
        }
      else if (k == 13)
        i = 1;
      else
        consume(k);
    }
    this.lineBuf.append(this.buf, 0, this.pos);
    return new String(this.lineBuf);
  }

  private void consume(int paramInt)
  {
    if (this.pos == 2048)
    {
      this.lineBuf.append(this.buf);
      this.pos = 0;
    }
    this.buf[(this.pos++)] = (char)paramInt;
  }

  public String requestLine()
  {
    return this.startLine;
  }

  Headers headers()
    throws IOException
  {
    if (this.hdrs != null)
      return this.hdrs;
    this.hdrs = new Headers();
    Object localObject1 = new char[10];
    int i = this.is.read();
    while ((i != 10) && (i != 13) && (i >= 0))
    {
      Object localObject2;
      String str;
      int j = 0;
      int k = -1;
      int i1 = (i > 32) ? 1 : 0;
      localObject1[(j++)] = (char)i;
      while ((l = this.is.read()) >= 0)
      {
        int l;
        switch (l)
        {
        case 58:
          if ((i1 != 0) && (j > 0))
            k = j;
          i1 = 0;
          break;
        case 9:
          l = 32;
        case 32:
          i1 = 0;
          break;
        case 10:
        case 13:
          i = this.is.read();
          if ((l == 13) && (i == 10))
          {
            i = this.is.read();
            if (i == 13)
              i = this.is.read();
          }
          if ((i == 10) || (i == 13))
            break label280;
          if (i > 32)
            break label280:
          l = 32;
        }
        if (j >= localObject1.length)
        {
          localObject2 = new char[localObject1.length * 2];
          System.arraycopy(localObject1, 0, localObject2, 0, j);
          localObject1 = localObject2;
        }
        localObject1[(j++)] = (char)l;
      }
      i = -1;
      while ((j > 0) && (localObject1[(j - 1)] <= ' '))
        label280: --j;
      if (k <= 0)
      {
        localObject2 = null;
        k = 0;
      }
      else
      {
        localObject2 = String.copyValueOf(localObject1, 0, k);
        if ((k < j) && (localObject1[k] == ':'))
          ++k;
        while ((k < j) && (localObject1[k] <= ' '))
          ++k;
      }
      if (k >= j)
        str = new String();
      else
        str = String.copyValueOf(localObject1, k, j - k);
      this.hdrs.add((String)localObject2, str);
    }
    return ((Headers)(Headers)this.hdrs);
  }

  static class ReadStream extends InputStream
  {
    SocketChannel channel;
    SelectorCache sc;
    Selector selector;
    ByteBuffer chanbuf;
    SelectionKey key;
    int available;
    byte[] one;
    boolean closed = false;
    boolean eof = false;
    ByteBuffer markBuf;
    boolean marked;
    boolean reset;
    int readlimit;
    static long readTimeout = ServerConfig.getReadTimeout();
    ServerImpl server;

    public ReadStream(ServerImpl paramServerImpl, SocketChannel paramSocketChannel)
      throws IOException
    {
      this.channel = paramSocketChannel;
      this.server = paramServerImpl;
      this.sc = SelectorCache.getSelectorCache();
      this.selector = this.sc.getSelector();
      this.chanbuf = ByteBuffer.allocate(8192);
      this.key = paramSocketChannel.register(this.selector, 1);
      this.available = 0;
      this.one = new byte[1];
      this.closed = (this.marked = this.reset = 0);
    }

    public synchronized int read(byte[] paramArrayOfByte)
      throws IOException
    {
      return read(paramArrayOfByte, 0, paramArrayOfByte.length);
    }

    public synchronized int read()
      throws IOException
    {
      int i = read(this.one, 0, 1);
      if (i == 1)
        return (this.one[0] & 0xFF);
      return -1;
    }

    public synchronized int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
      throws IOException
    {
      int i;
      int j;
      if (this.closed)
        throw new IOException("Stream closed");
      if (this.eof)
        return -1;
      if (this.reset)
      {
        i = this.markBuf.remaining();
        j = (i > paramInt2) ? paramInt2 : i;
        this.markBuf.get(paramArrayOfByte, paramInt1, j);
        if (i == j)
          this.reset = false;
      }
      else
      {
        for (i = available(); (i == 0) && (!(this.eof)); i = available())
          block();
        if (this.eof)
          return -1;
        j = (i > paramInt2) ? paramInt2 : i;
        this.chanbuf.get(paramArrayOfByte, paramInt1, j);
        this.available -= j;
        if (this.marked)
          try
          {
            this.markBuf.put(paramArrayOfByte, paramInt1, j);
          }
          catch (BufferOverflowException localBufferOverflowException)
          {
            this.marked = false;
          }
      }
      return j;
    }

    public synchronized int available()
      throws IOException
    {
      if (this.closed)
        throw new IOException("Stream is closed");
      if (this.eof)
        return -1;
      if (this.reset)
        return this.markBuf.remaining();
      if (this.available > 0)
        return this.available;
      this.chanbuf.clear();
      this.available = this.channel.read(this.chanbuf);
      if (this.available > 0)
      {
        this.chanbuf.flip();
      }
      else if (this.available == -1)
      {
        this.eof = true;
        this.available = 0;
      }
      return this.available;
    }

    private synchronized void block()
      throws IOException
    {
      long l1 = this.server.getTime();
      long l2 = l1 + readTimeout;
      while (l1 < l2)
      {
        if (this.selector.select(readTimeout) == 1)
        {
          this.selector.selectedKeys().clear();
          available();
          return;
        }
        l1 = this.server.getTime();
      }
      throw new SocketTimeoutException("no data received");
    }

    public void close()
      throws IOException
    {
      if (this.closed)
        return;
      this.channel.close();
      this.selector.selectNow();
      this.sc.freeSelector(this.selector);
      this.closed = true;
    }

    public synchronized void mark(int paramInt)
    {
      if (this.closed)
        return;
      this.readlimit = paramInt;
      this.markBuf = ByteBuffer.allocate(paramInt);
      this.marked = true;
      this.reset = false;
    }

    public synchronized void reset()
      throws IOException
    {
      if (this.closed)
        return;
      if (!(this.marked))
        throw new IOException("Stream not marked");
      this.marked = false;
      this.reset = true;
      this.markBuf.flip();
    }
  }

  static class WriteStream extends OutputStream
  {
    SocketChannel channel;
    ByteBuffer buf;
    SelectionKey key;
    SelectorCache sc;
    Selector selector;
    boolean closed;
    byte[] one;
    ServerImpl server;
    static long writeTimeout = ServerConfig.getWriteTimeout();

    public WriteStream(ServerImpl paramServerImpl, SocketChannel paramSocketChannel)
      throws IOException
    {
      this.channel = paramSocketChannel;
      this.server = paramServerImpl;
      this.sc = SelectorCache.getSelectorCache();
      this.selector = this.sc.getSelector();
      this.key = paramSocketChannel.register(this.selector, 4);
      this.closed = false;
      this.one = new byte[1];
      this.buf = ByteBuffer.allocate(4096);
    }

    public synchronized void write(int paramInt)
      throws IOException
    {
      this.one[0] = (byte)paramInt;
      write(this.one, 0, 1);
    }

    public synchronized void write(byte[] paramArrayOfByte)
      throws IOException
    {
      write(paramArrayOfByte, 0, paramArrayOfByte.length);
    }

    public synchronized void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
      throws IOException
    {
      int k;
      int i = paramInt2;
      if (this.closed)
        throw new IOException("stream is closed");
      int j = this.buf.capacity();
      if (j < paramInt2)
      {
        k = paramInt2 - j;
        this.buf = ByteBuffer.allocate(2 * (j + k));
      }
      this.buf.clear();
      this.buf.put(paramArrayOfByte, paramInt1, paramInt2);
      this.buf.flip();
      while ((k = this.channel.write(this.buf)) < i)
      {
        i -= k;
        if (i == 0)
          return;
        block();
      }
    }

    void block()
      throws IOException
    {
      long l1 = this.server.getTime();
      long l2 = l1 + writeTimeout;
      while (l1 < l2)
      {
        if (this.selector.select(writeTimeout) == 1)
        {
          this.selector.selectedKeys().clear();
          return;
        }
        l1 = this.server.getTime();
      }
      throw new SocketTimeoutException("write blocked too long");
    }

    public void close()
      throws IOException
    {
      if (this.closed)
        return;
      this.channel.close();
      this.selector.selectNow();
      this.sc.freeSelector(this.selector);
      this.closed = true;
    }
  }
}