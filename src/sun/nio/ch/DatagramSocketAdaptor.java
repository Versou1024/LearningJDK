package sun.nio.ch;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;

public class DatagramSocketAdaptor extends DatagramSocket
{
  private final DatagramChannelImpl dc;
  private volatile OptionAdaptor opts = null;
  private volatile int timeout = 0;
  private volatile int trafficClass = 0;
  private static final DatagramSocketImpl dummyDatagramSocket = new DatagramSocketImpl()
  {
    protected void create()
      throws SocketException
    {
    }

    protected void bind(int paramInt, InetAddress paramInetAddress)
      throws SocketException
    {
    }

    protected void send(DatagramPacket paramDatagramPacket)
      throws IOException
    {
    }

    protected int peek(InetAddress paramInetAddress)
      throws IOException
    {
      return 0;
    }

    protected int peekData(DatagramPacket paramDatagramPacket)
      throws IOException
    {
      return 0;
    }

    protected void receive(DatagramPacket paramDatagramPacket)
      throws IOException
    {
    }

    protected void setTTL(byte paramByte)
      throws IOException
    {
    }

    protected byte getTTL()
      throws IOException
    {
      return 0;
    }

    protected void setTimeToLive(int paramInt)
      throws IOException
    {
    }

    protected int getTimeToLive()
      throws IOException
    {
      return 0;
    }

    protected void join(InetAddress paramInetAddress)
      throws IOException
    {
    }

    protected void leave(InetAddress paramInetAddress)
      throws IOException
    {
    }

    protected void joinGroup(SocketAddress paramSocketAddress, NetworkInterface paramNetworkInterface)
      throws IOException
    {
    }

    protected void leaveGroup(SocketAddress paramSocketAddress, NetworkInterface paramNetworkInterface)
      throws IOException
    {
    }

    protected void close()
    {
    }

    public Object getOption(int paramInt)
      throws SocketException
    {
      return null;
    }

    public void setOption(int paramInt, Object paramObject)
      throws SocketException
    {
    }
  };

  private DatagramSocketAdaptor(DatagramChannelImpl paramDatagramChannelImpl)
    throws IOException
  {
    super(dummyDatagramSocket);
    this.dc = paramDatagramChannelImpl;
  }

  public static DatagramSocket create(DatagramChannelImpl paramDatagramChannelImpl)
  {
    try
    {
      return new DatagramSocketAdaptor(paramDatagramChannelImpl);
    }
    catch (IOException localIOException)
    {
      throw new Error(localIOException);
    }
  }

  private void connectInternal(SocketAddress paramSocketAddress)
    throws SocketException
  {
    InetSocketAddress localInetSocketAddress = Net.asInetSocketAddress(paramSocketAddress);
    int i = localInetSocketAddress.getPort();
    if ((i < 0) || (i > 65535))
      throw new IllegalArgumentException("connect: " + i);
    if (paramSocketAddress == null)
      throw new IllegalArgumentException("connect: null address");
    if (!(isClosed()))
      return;
    try
    {
      this.dc.connect(paramSocketAddress);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public void bind(SocketAddress paramSocketAddress)
    throws SocketException
  {
    try
    {
      if (paramSocketAddress == null)
        paramSocketAddress = new InetSocketAddress(0);
      this.dc.bind(paramSocketAddress);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public void connect(InetAddress paramInetAddress, int paramInt)
  {
    try
    {
      connectInternal(new InetSocketAddress(paramInetAddress, paramInt));
    }
    catch (SocketException localSocketException)
    {
    }
  }

  public void connect(SocketAddress paramSocketAddress)
    throws SocketException
  {
    if (paramSocketAddress == null)
      throw new IllegalArgumentException("Address can't be null");
    connectInternal(paramSocketAddress);
  }

  public void disconnect()
  {
    try
    {
      this.dc.disconnect();
    }
    catch (IOException localIOException)
    {
      throw new Error(localIOException);
    }
  }

  public boolean isBound()
  {
    return this.dc.isBound();
  }

  public boolean isConnected()
  {
    return this.dc.isConnected();
  }

  public InetAddress getInetAddress()
  {
    return ((isConnected()) ? Net.asInetSocketAddress(this.dc.remoteAddress()).getAddress() : null);
  }

  public int getPort()
  {
    return ((isConnected()) ? Net.asInetSocketAddress(this.dc.remoteAddress()).getPort() : -1);
  }

  public void send(DatagramPacket paramDatagramPacket)
    throws IOException
  {
    synchronized (this.dc.blockingLock())
    {
      if (!(this.dc.isBlocking()))
        throw new IllegalBlockingModeException();
      try
      {
        synchronized (paramDatagramPacket)
        {
          ByteBuffer localByteBuffer = ByteBuffer.wrap(paramDatagramPacket.getData(), paramDatagramPacket.getOffset(), paramDatagramPacket.getLength());
          if (this.dc.isConnected())
            if (paramDatagramPacket.getAddress() == null)
            {
              InetSocketAddress localInetSocketAddress = (InetSocketAddress)this.dc.remoteAddress;
              paramDatagramPacket.setPort(localInetSocketAddress.getPort());
              paramDatagramPacket.setAddress(localInetSocketAddress.getAddress());
              this.dc.write(localByteBuffer);
            }
            else
            {
              this.dc.send(localByteBuffer, paramDatagramPacket.getSocketAddress());
            }
          else
            this.dc.send(localByteBuffer, paramDatagramPacket.getSocketAddress());
        }
      }
      catch (IOException localIOException)
      {
        Net.translateException(localIOException);
      }
    }
  }

  // ERROR //
  private void receive(ByteBuffer paramByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 236	sun/nio/ch/DatagramSocketAdaptor:timeout	I
    //   4: ifne +13 -> 17
    //   7: aload_0
    //   8: getfield 239	sun/nio/ch/DatagramSocketAdaptor:dc	Lsun/nio/ch/DatagramChannelImpl;
    //   11: aload_1
    //   12: invokevirtual 282	sun/nio/ch/DatagramChannelImpl:receive	(Ljava/nio/ByteBuffer;)Ljava/net/SocketAddress;
    //   15: pop
    //   16: return
    //   17: aconst_null
    //   18: astore_2
    //   19: aconst_null
    //   20: astore_3
    //   21: aload_0
    //   22: getfield 239	sun/nio/ch/DatagramSocketAdaptor:dc	Lsun/nio/ch/DatagramChannelImpl;
    //   25: iconst_0
    //   26: invokevirtual 281	sun/nio/ch/DatagramChannelImpl:configureBlocking	(Z)Ljava/nio/channels/SelectableChannel;
    //   29: pop
    //   30: aload_0
    //   31: getfield 239	sun/nio/ch/DatagramSocketAdaptor:dc	Lsun/nio/ch/DatagramChannelImpl;
    //   34: aload_1
    //   35: invokevirtual 282	sun/nio/ch/DatagramChannelImpl:receive	(Ljava/nio/ByteBuffer;)Ljava/net/SocketAddress;
    //   38: ifnull +7 -> 45
    //   41: jsr +135 -> 176
    //   44: return
    //   45: aload_0
    //   46: getfield 239	sun/nio/ch/DatagramSocketAdaptor:dc	Lsun/nio/ch/DatagramChannelImpl;
    //   49: invokestatic 308	sun/nio/ch/Util:getTemporarySelector	(Ljava/nio/channels/SelectableChannel;)Ljava/nio/channels/Selector;
    //   52: astore_3
    //   53: aload_0
    //   54: getfield 239	sun/nio/ch/DatagramSocketAdaptor:dc	Lsun/nio/ch/DatagramChannelImpl;
    //   57: aload_3
    //   58: iconst_1
    //   59: invokevirtual 285	sun/nio/ch/DatagramChannelImpl:register	(Ljava/nio/channels/Selector;I)Ljava/nio/channels/SelectionKey;
    //   62: astore_2
    //   63: aload_0
    //   64: getfield 236	sun/nio/ch/DatagramSocketAdaptor:timeout	I
    //   67: i2l
    //   68: lstore 5
    //   70: aload_0
    //   71: getfield 239	sun/nio/ch/DatagramSocketAdaptor:dc	Lsun/nio/ch/DatagramChannelImpl;
    //   74: invokevirtual 274	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   77: ifne +11 -> 88
    //   80: new 125	java/nio/channels/ClosedChannelException
    //   83: dup
    //   84: invokespecial 264	java/nio/channels/ClosedChannelException:<init>	()V
    //   87: athrow
    //   88: invokestatic 247	java/lang/System:currentTimeMillis	()J
    //   91: lstore 7
    //   93: aload_3
    //   94: lload 5
    //   96: invokevirtual 268	java/nio/channels/Selector:select	(J)I
    //   99: istore 9
    //   101: iload 9
    //   103: ifle +25 -> 128
    //   106: aload_2
    //   107: invokevirtual 267	java/nio/channels/SelectionKey:isReadable	()Z
    //   110: ifeq +18 -> 128
    //   113: aload_0
    //   114: getfield 239	sun/nio/ch/DatagramSocketAdaptor:dc	Lsun/nio/ch/DatagramChannelImpl;
    //   117: aload_1
    //   118: invokevirtual 282	sun/nio/ch/DatagramChannelImpl:receive	(Ljava/nio/ByteBuffer;)Ljava/net/SocketAddress;
    //   121: ifnull +7 -> 128
    //   124: jsr +52 -> 176
    //   127: return
    //   128: aload_3
    //   129: invokevirtual 269	java/nio/channels/Selector:selectedKeys	()Ljava/util/Set;
    //   132: aload_2
    //   133: invokeinterface 309 2 0
    //   138: pop
    //   139: lload 5
    //   141: invokestatic 247	java/lang/System:currentTimeMillis	()J
    //   144: lload 7
    //   146: lsub
    //   147: lsub
    //   148: lstore 5
    //   150: lload 5
    //   152: lconst_0
    //   153: lcmp
    //   154: ifgt +11 -> 165
    //   157: new 123	java/net/SocketTimeoutException
    //   160: dup
    //   161: invokespecial 261	java/net/SocketTimeoutException:<init>	()V
    //   164: athrow
    //   165: goto -95 -> 70
    //   168: astore 10
    //   170: jsr +6 -> 176
    //   173: aload 10
    //   175: athrow
    //   176: astore 11
    //   178: aload_2
    //   179: ifnull +7 -> 186
    //   182: aload_2
    //   183: invokevirtual 266	java/nio/channels/SelectionKey:cancel	()V
    //   186: aload_0
    //   187: getfield 239	sun/nio/ch/DatagramSocketAdaptor:dc	Lsun/nio/ch/DatagramChannelImpl;
    //   190: invokevirtual 274	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   193: ifeq +12 -> 205
    //   196: aload_0
    //   197: getfield 239	sun/nio/ch/DatagramSocketAdaptor:dc	Lsun/nio/ch/DatagramChannelImpl;
    //   200: iconst_1
    //   201: invokevirtual 281	sun/nio/ch/DatagramChannelImpl:configureBlocking	(Z)Ljava/nio/channels/SelectableChannel;
    //   204: pop
    //   205: aload_3
    //   206: ifnull +7 -> 213
    //   209: aload_3
    //   210: invokestatic 307	sun/nio/ch/Util:releaseTemporarySelector	(Ljava/nio/channels/Selector;)V
    //   213: ret 11
    //
    // Exception table:
    //   from	to	target	type
    //   30	44	168	finally
    //   45	127	168	finally
    //   128	173	168	finally
  }

  public void receive(DatagramPacket paramDatagramPacket)
    throws IOException
  {
    synchronized (this.dc.blockingLock())
    {
      if (!(this.dc.isBlocking()))
        throw new IllegalBlockingModeException();
      try
      {
        synchronized (paramDatagramPacket)
        {
          ByteBuffer localByteBuffer = ByteBuffer.wrap(paramDatagramPacket.getData(), paramDatagramPacket.getOffset(), paramDatagramPacket.getLength());
          receive(localByteBuffer);
          paramDatagramPacket.setLength(localByteBuffer.position() - paramDatagramPacket.getOffset());
        }
      }
      catch (IOException localIOException)
      {
        Net.translateException(localIOException);
      }
    }
  }

  public InetAddress getLocalAddress()
  {
    if (isClosed())
      return null;
    try
    {
      return Net.asInetSocketAddress(this.dc.localAddress()).getAddress();
    }
    catch (Exception localException)
    {
    }
    return new InetSocketAddress(0).getAddress();
  }

  public int getLocalPort()
  {
    if (isClosed())
      return -1;
    try
    {
      return Net.asInetSocketAddress(this.dc.localAddress()).getPort();
    }
    catch (Exception localException)
    {
    }
    return 0;
  }

  public void setSoTimeout(int paramInt)
    throws SocketException
  {
    this.timeout = paramInt;
  }

  public int getSoTimeout()
    throws SocketException
  {
    return this.timeout;
  }

  private OptionAdaptor opts()
  {
    if (this.opts == null)
      this.opts = new OptionAdaptor(this.dc);
    return this.opts;
  }

  public void setSendBufferSize(int paramInt)
    throws SocketException
  {
    opts().setSendBufferSize(paramInt);
  }

  public int getSendBufferSize()
    throws SocketException
  {
    return opts().getSendBufferSize();
  }

  public void setReceiveBufferSize(int paramInt)
    throws SocketException
  {
    opts().setReceiveBufferSize(paramInt);
  }

  public int getReceiveBufferSize()
    throws SocketException
  {
    return opts().getReceiveBufferSize();
  }

  public void setReuseAddress(boolean paramBoolean)
    throws SocketException
  {
    opts().setReuseAddress(paramBoolean);
  }

  public boolean getReuseAddress()
    throws SocketException
  {
    return opts().getReuseAddress();
  }

  public void setBroadcast(boolean paramBoolean)
    throws SocketException
  {
    opts().setBroadcast(paramBoolean);
  }

  public boolean getBroadcast()
    throws SocketException
  {
    return opts().getBroadcast();
  }

  public void setTrafficClass(int paramInt)
    throws SocketException
  {
    opts().setTrafficClass(paramInt);
    this.trafficClass = paramInt;
  }

  public int getTrafficClass()
    throws SocketException
  {
    int i = opts().getTrafficClass();
    if (i < 0)
      i = this.trafficClass;
    return i;
  }

  public void close()
  {
    try
    {
      this.dc.close();
    }
    catch (IOException localIOException)
    {
      throw new Error(localIOException);
    }
  }

  public boolean isClosed()
  {
    return (!(this.dc.isOpen()));
  }

  public DatagramChannel getChannel()
  {
    return this.dc;
  }
}