package sun.net.httpserver;

import com.sun.net.httpserver.HttpsConfigurator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

class SSLStreams
{
  SSLContext sslctx;
  SocketChannel chan;
  TimeSource time;
  ServerImpl server;
  SSLEngine engine;
  EngineWrapper wrapper;
  OutputStream os;
  InputStream is;
  static long readTimeout;
  static long writeTimeout;
  Lock handshaking = new ReentrantLock();
  int app_buf_size;
  int packet_buf_size;

  SSLStreams(ServerImpl paramServerImpl, SSLContext paramSSLContext, SocketChannel paramSocketChannel)
    throws IOException
  {
    this.server = paramServerImpl;
    this.time = paramServerImpl;
    this.sslctx = paramSSLContext;
    this.chan = paramSocketChannel;
    InetSocketAddress localInetSocketAddress = (InetSocketAddress)paramSocketChannel.socket().getRemoteSocketAddress();
    this.engine = paramSSLContext.createSSLEngine(localInetSocketAddress.getHostName(), localInetSocketAddress.getPort());
    this.engine.setUseClientMode(false);
    HttpsConfigurator localHttpsConfigurator = paramServerImpl.getHttpsConfigurator();
    configureEngine(localHttpsConfigurator, localInetSocketAddress);
    this.wrapper = new EngineWrapper(this, paramSocketChannel, this.engine);
  }

  private void configureEngine(HttpsConfigurator paramHttpsConfigurator, InetSocketAddress paramInetSocketAddress)
  {
    if (paramHttpsConfigurator != null)
    {
      Parameters localParameters = new Parameters(this, paramHttpsConfigurator, paramInetSocketAddress);
      paramHttpsConfigurator.configure(localParameters);
      SSLParameters localSSLParameters = localParameters.getSSLParameters();
      if (localSSLParameters != null)
      {
        this.engine.setSSLParameters(localSSLParameters);
      }
      else
      {
        if (localParameters.getCipherSuites() != null)
          try
          {
            this.engine.setEnabledCipherSuites(localParameters.getCipherSuites());
          }
          catch (IllegalArgumentException localIllegalArgumentException1)
          {
          }
        this.engine.setNeedClientAuth(localParameters.getNeedClientAuth());
        this.engine.setWantClientAuth(localParameters.getWantClientAuth());
        if (localParameters.getProtocols() != null)
          try
          {
            this.engine.setEnabledProtocols(localParameters.getProtocols());
          }
          catch (IllegalArgumentException localIllegalArgumentException2)
          {
          }
      }
    }
  }

  void close()
    throws IOException
  {
    this.wrapper.close();
  }

  InputStream getInputStream()
    throws IOException
  {
    if (this.is == null)
      this.is = new InputStream(this);
    return this.is;
  }

  OutputStream getOutputStream()
    throws IOException
  {
    if (this.os == null)
      this.os = new OutputStream(this);
    return this.os;
  }

  SSLEngine getSSLEngine()
  {
    return this.engine;
  }

  void beginHandshake()
    throws SSLException
  {
    this.engine.beginHandshake();
  }

  private ByteBuffer allocate(BufType paramBufType)
  {
    return allocate(paramBufType, -1);
  }

  private ByteBuffer allocate(BufType paramBufType, int paramInt)
  {
    if ((!($assertionsDisabled)) && (this.engine == null))
      throw new AssertionError();
    synchronized (this)
    {
      int i;
      SSLSession localSSLSession;
      if (paramBufType == BufType.PACKET)
      {
        if (this.packet_buf_size == 0)
        {
          localSSLSession = this.engine.getSession();
          this.packet_buf_size = localSSLSession.getPacketBufferSize();
        }
        if (paramInt > this.packet_buf_size)
          this.packet_buf_size = paramInt;
        i = this.packet_buf_size;
      }
      else
      {
        if (this.app_buf_size == 0)
        {
          localSSLSession = this.engine.getSession();
          this.app_buf_size = localSSLSession.getApplicationBufferSize();
        }
        if (paramInt > this.app_buf_size)
          this.app_buf_size = paramInt;
        i = this.app_buf_size;
      }
      return ByteBuffer.allocate(i);
    }
  }

  private ByteBuffer realloc(ByteBuffer paramByteBuffer, boolean paramBoolean, BufType paramBufType)
  {
    synchronized (this)
    {
      int i = 2 * paramByteBuffer.capacity();
      ByteBuffer localByteBuffer = allocate(paramBufType, i);
      if (paramBoolean)
        paramByteBuffer.flip();
      localByteBuffer.put(paramByteBuffer);
      paramByteBuffer = localByteBuffer;
    }
    return paramByteBuffer;
  }

  public WrapperResult sendData(ByteBuffer paramByteBuffer)
    throws IOException
  {
    WrapperResult localWrapperResult = null;
    while (paramByteBuffer.remaining() > 0)
    {
      localWrapperResult = this.wrapper.wrapAndSend(paramByteBuffer);
      SSLEngineResult.Status localStatus = localWrapperResult.result.getStatus();
      if (localStatus == SSLEngineResult.Status.CLOSED)
      {
        doClosure();
        return localWrapperResult;
      }
      SSLEngineResult.HandshakeStatus localHandshakeStatus = localWrapperResult.result.getHandshakeStatus();
      if ((localHandshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED) && (localHandshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING))
        doHandshake(localHandshakeStatus);
    }
    return localWrapperResult;
  }

  public WrapperResult recvData(ByteBuffer paramByteBuffer)
    throws IOException
  {
    WrapperResult localWrapperResult = null;
    if ((!($assertionsDisabled)) && (paramByteBuffer.position() != 0))
      throw new AssertionError();
    while (paramByteBuffer.position() == 0)
    {
      localWrapperResult = this.wrapper.recvAndUnwrap(paramByteBuffer);
      paramByteBuffer = (localWrapperResult.buf != paramByteBuffer) ? localWrapperResult.buf : paramByteBuffer;
      SSLEngineResult.Status localStatus = localWrapperResult.result.getStatus();
      if (localStatus == SSLEngineResult.Status.CLOSED)
      {
        doClosure();
        return localWrapperResult;
      }
      SSLEngineResult.HandshakeStatus localHandshakeStatus = localWrapperResult.result.getHandshakeStatus();
      if ((localHandshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED) && (localHandshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING))
        doHandshake(localHandshakeStatus);
    }
    paramByteBuffer.flip();
    return localWrapperResult;
  }

  void doClosure()
    throws IOException
  {
    try
    {
      WrapperResult localWrapperResult;
      this.handshaking.lock();
      ByteBuffer localByteBuffer = allocate(BufType.APPLICATION);
      do
      {
        localByteBuffer.clear();
        localByteBuffer.flip();
        localWrapperResult = this.wrapper.wrapAndSendX(localByteBuffer, true);
      }
      while (localWrapperResult.result.getStatus() != SSLEngineResult.Status.CLOSED);
    }
    finally
    {
      this.handshaking.unlock();
    }
  }

  void doHandshake(SSLEngineResult.HandshakeStatus paramHandshakeStatus)
    throws IOException
  {
    try
    {
      this.handshaking.lock();
      ByteBuffer localByteBuffer = allocate(BufType.APPLICATION);
      while ((paramHandshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED) && (paramHandshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING))
      {
        WrapperResult localWrapperResult = null;
        switch (1.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[paramHandshakeStatus.ordinal()])
        {
        case 1:
          while ((localRunnable = this.engine.getDelegatedTask()) != null)
          {
            Runnable localRunnable;
            localRunnable.run();
          }
        case 2:
          localByteBuffer.clear();
          localByteBuffer.flip();
          localWrapperResult = this.wrapper.wrapAndSend(localByteBuffer);
          break;
        case 3:
          localByteBuffer.clear();
          localWrapperResult = this.wrapper.recvAndUnwrap(localByteBuffer);
          if (localWrapperResult.buf != localByteBuffer)
            localByteBuffer = localWrapperResult.buf;
          if ((!($assertionsDisabled)) && (localByteBuffer.position() != 0))
            throw new AssertionError();
        }
        paramHandshakeStatus = localWrapperResult.result.getHandshakeStatus();
      }
    }
    finally
    {
      this.handshaking.unlock();
    }
  }

  static
  {
    readTimeout = ServerConfig.getReadTimeout();
    writeTimeout = ServerConfig.getWriteTimeout();
  }

  static enum BufType
  {
    PACKET, APPLICATION;
  }

  class EngineWrapper
  {
    SocketChannel chan;
    SSLEngine engine;
    SelectorCache sc;
    Selector write_selector;
    Selector read_selector;
    SelectionKey wkey;
    SelectionKey rkey;
    Object wrapLock;
    Object unwrapLock;
    ByteBuffer unwrap_src;
    ByteBuffer wrap_dst;
    boolean closed = false;
    int u_remaining;

    EngineWrapper(, SocketChannel paramSocketChannel, SSLEngine paramSSLEngine)
      throws IOException
    {
      this.chan = paramSocketChannel;
      this.engine = paramSSLEngine;
      this.wrapLock = new Object();
      this.unwrapLock = new Object();
      this.unwrap_src = SSLStreams.access$000(paramSSLStreams, SSLStreams.BufType.PACKET);
      this.wrap_dst = SSLStreams.access$000(paramSSLStreams, SSLStreams.BufType.PACKET);
      this.sc = SelectorCache.getSelectorCache();
      this.write_selector = this.sc.getSelector();
      this.wkey = paramSocketChannel.register(this.write_selector, 4);
      this.read_selector = this.sc.getSelector();
      this.wkey = paramSocketChannel.register(this.read_selector, 1);
    }

    void close()
      throws IOException
    {
      this.sc.freeSelector(this.write_selector);
      this.sc.freeSelector(this.read_selector);
    }

    SSLStreams.WrapperResult wrapAndSend()
      throws IOException
    {
      return wrapAndSendX(paramByteBuffer, false);
    }

    SSLStreams.WrapperResult wrapAndSendX(, boolean paramBoolean)
      throws IOException
    {
      if ((this.closed) && (!(paramBoolean)))
        throw new IOException("Engine is closed");
      SSLStreams.WrapperResult localWrapperResult = new SSLStreams.WrapperResult(this.this$0);
      synchronized (this.wrapLock)
      {
        SSLEngineResult.Status localStatus;
        this.wrap_dst.clear();
        do
        {
          localWrapperResult.result = this.engine.wrap(paramByteBuffer, this.wrap_dst);
          localStatus = localWrapperResult.result.getStatus();
          if (localStatus == SSLEngineResult.Status.BUFFER_OVERFLOW)
            this.wrap_dst = SSLStreams.access$100(this.this$0, this.wrap_dst, true, SSLStreams.BufType.PACKET);
        }
        while (localStatus == SSLEngineResult.Status.BUFFER_OVERFLOW);
        if ((localStatus != SSLEngineResult.Status.CLOSED) || (paramBoolean))
          break label131;
        this.closed = true;
        return localWrapperResult;
        label131: if (localWrapperResult.result.bytesProduced() <= 0)
          break label287;
        this.wrap_dst.flip();
        int i = this.wrap_dst.remaining();
        if (($assertionsDisabled) || (i == localWrapperResult.result.bytesProduced()))
          break label186;
        throw new AssertionError();
        label186: long l1 = this.this$0.time.getTime();
        long l2 = l1 + SSLStreams.writeTimeout;
        while (true)
        {
          if (i <= 0)
            break label287;
          this.write_selector.select(SSLStreams.writeTimeout);
          l1 = this.this$0.time.getTime();
          if (l1 > l2)
            throw new SocketTimeoutException("write timed out");
          this.write_selector.selectedKeys().clear();
          label287: i -= this.chan.write(this.wrap_dst);
        }
      }
      return localWrapperResult;
    }

    SSLStreams.WrapperResult recvAndUnwrap()
      throws IOException
    {
      int i;
      SSLEngineResult.Status localStatus = SSLEngineResult.Status.OK;
      SSLStreams.WrapperResult localWrapperResult = new SSLStreams.WrapperResult(this.this$0);
      localWrapperResult.buf = paramByteBuffer;
      if (this.closed)
        throw new IOException("Engine is closed");
      if (this.u_remaining > 0)
      {
        this.unwrap_src.compact();
        this.unwrap_src.flip();
        i = 0;
      }
      else
      {
        this.unwrap_src.clear();
        i = 1;
      }
      synchronized (this.unwrapLock)
      {
        do
        {
          if (i != 0)
          {
            int k;
            long l1 = this.this$0.time.getTime();
            long l2 = l1 + SSLStreams.readTimeout;
            do
            {
              if (l1 > l2)
                throw new SocketTimeoutException("read timedout");
              k = this.read_selector.select(SSLStreams.readTimeout);
              l1 = this.this$0.time.getTime();
            }
            while (k != 1);
            this.read_selector.selectedKeys().clear();
            int j = this.chan.read(this.unwrap_src);
            if (j == -1)
              throw new IOException("connection closed for reading");
            this.unwrap_src.flip();
          }
          localWrapperResult.result = this.engine.unwrap(this.unwrap_src, localWrapperResult.buf);
          localStatus = localWrapperResult.result.getStatus();
          if (localStatus == SSLEngineResult.Status.BUFFER_UNDERFLOW)
          {
            if (this.unwrap_src.limit() == this.unwrap_src.capacity())
            {
              this.unwrap_src = SSLStreams.access$100(this.this$0, this.unwrap_src, false, SSLStreams.BufType.PACKET);
            }
            else
            {
              this.unwrap_src.position(this.unwrap_src.limit());
              this.unwrap_src.limit(this.unwrap_src.capacity());
            }
            i = 1;
          }
          else if (localStatus == SSLEngineResult.Status.BUFFER_OVERFLOW)
          {
            localWrapperResult.buf = SSLStreams.access$100(this.this$0, localWrapperResult.buf, true, SSLStreams.BufType.APPLICATION);
            i = 0;
          }
          else if (localStatus == SSLEngineResult.Status.CLOSED)
          {
            this.closed = true;
            localWrapperResult.buf.flip();
            return localWrapperResult;
          }
        }
        while (localStatus != SSLEngineResult.Status.OK);
      }
      this.u_remaining = this.unwrap_src.remaining();
      return localWrapperResult;
    }
  }

  class InputStream extends InputStream
  {
    ByteBuffer bbuf;
    boolean closed = false;
    boolean eof = false;
    boolean needData = true;
    byte[] single = new byte[1];

    InputStream()
    {
      this.bbuf = SSLStreams.access$000(paramSSLStreams, SSLStreams.BufType.APPLICATION);
    }

    public int read(, int paramInt1, int paramInt2)
      throws IOException
    {
      if (this.closed)
        throw new IOException("SSL stream is closed");
      if (this.eof)
        return 0;
      int i = 0;
      if (!(this.needData))
      {
        i = this.bbuf.remaining();
        this.needData = (i == 0);
      }
      if (this.needData)
      {
        this.bbuf.clear();
        SSLStreams.WrapperResult localWrapperResult = this.this$0.recvData(this.bbuf);
        this.bbuf = ((localWrapperResult.buf == this.bbuf) ? this.bbuf : localWrapperResult.buf);
        if ((i = this.bbuf.remaining()) == 0)
        {
          this.eof = true;
          return 0;
        }
        this.needData = false;
      }
      if (paramInt2 > i)
        paramInt2 = i;
      this.bbuf.get(paramArrayOfByte, paramInt1, paramInt2);
      return paramInt2;
    }

    public int available()
      throws IOException
    {
      return this.bbuf.remaining();
    }

    public boolean markSupported()
    {
      return false;
    }

    public void reset()
      throws IOException
    {
      throw new IOException("mark/reset not supported");
    }

    public long skip()
      throws IOException
    {
      int i = (int)paramLong;
      if (this.closed)
        throw new IOException("SSL stream is closed");
      if (this.eof)
        return 3412047961268420608L;
      int j = i;
      while (i > 0)
      {
        if (this.bbuf.remaining() >= i)
        {
          this.bbuf.position(this.bbuf.position() + i);
          return j;
        }
        i -= this.bbuf.remaining();
        this.bbuf.clear();
        SSLStreams.WrapperResult localWrapperResult = this.this$0.recvData(this.bbuf);
        this.bbuf = ((localWrapperResult.buf == this.bbuf) ? this.bbuf : localWrapperResult.buf);
      }
      return j;
    }

    public void close()
      throws IOException
    {
      this.eof = true;
      this.this$0.engine.closeInbound();
    }

    public int read()
      throws IOException
    {
      return read(paramArrayOfByte, 0, paramArrayOfByte.length);
    }

    public int read()
      throws IOException
    {
      int i = read(this.single, 0, 1);
      if (i == 0)
        return -1;
      return (this.single[0] & 0xFF);
    }
  }

  class OutputStream extends OutputStream
  {
    ByteBuffer buf;
    boolean closed = false;
    byte[] single = new byte[1];

    OutputStream()
    {
      this.buf = SSLStreams.access$000(paramSSLStreams, SSLStreams.BufType.APPLICATION);
    }

    public void write()
      throws IOException
    {
      this.single[0] = (byte)paramInt;
      write(this.single, 0, 1);
    }

    public void write()
      throws IOException
    {
      write(paramArrayOfByte, 0, paramArrayOfByte.length);
    }

    public void write(, int paramInt1, int paramInt2)
      throws IOException
    {
      if (this.closed)
        throw new IOException("output stream is closed");
      while (paramInt2 > 0)
      {
        int i = (paramInt2 > this.buf.capacity()) ? this.buf.capacity() : paramInt2;
        this.buf.clear();
        this.buf.put(paramArrayOfByte, paramInt1, i);
        paramInt2 -= i;
        paramInt1 += i;
        this.buf.flip();
        SSLStreams.WrapperResult localWrapperResult = this.this$0.sendData(this.buf);
        if (localWrapperResult.result.getStatus() == SSLEngineResult.Status.CLOSED)
        {
          this.closed = true;
          if (paramInt2 > 0)
            throw new IOException("output stream is closed");
        }
      }
    }

    public void flush()
      throws IOException
    {
    }

    public void close()
      throws IOException
    {
      SSLStreams.WrapperResult localWrapperResult = null;
      this.this$0.engine.closeOutbound();
      this.closed = true;
      SSLEngineResult.HandshakeStatus localHandshakeStatus = SSLEngineResult.HandshakeStatus.NEED_WRAP;
      this.buf.clear();
      while (localHandshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP)
      {
        localWrapperResult = this.this$0.wrapper.wrapAndSend(this.buf);
        localHandshakeStatus = localWrapperResult.result.getHandshakeStatus();
      }
      if ((!($assertionsDisabled)) && (localWrapperResult.result.getStatus() != SSLEngineResult.Status.CLOSED))
        throw new AssertionError();
    }
  }

  class Parameters extends com.sun.net.httpserver.HttpsParameters
  {
    InetSocketAddress addr;
    SSLParameters params;
    HttpsConfigurator cfg;

    Parameters(, HttpsConfigurator paramHttpsConfigurator, InetSocketAddress paramInetSocketAddress)
    {
      this.addr = paramInetSocketAddress;
      this.cfg = paramHttpsConfigurator;
    }

    public InetSocketAddress getClientAddress()
    {
      return this.addr;
    }

    public HttpsConfigurator getHttpsConfigurator()
    {
      return this.cfg;
    }

    public void setSSLParameters()
    {
      this.params = paramSSLParameters;
    }

    SSLParameters getSSLParameters()
    {
      return this.params;
    }
  }

  class WrapperResult
  {
    SSLEngineResult result;
    ByteBuffer buf;
  }
}