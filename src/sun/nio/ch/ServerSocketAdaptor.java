package sun.nio.ch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

public class ServerSocketAdaptor extends ServerSocket
{
  private final ServerSocketChannelImpl ssc;
  private volatile OptionAdaptor opts = null;
  private volatile int timeout = 0;

  public static ServerSocket create(ServerSocketChannelImpl paramServerSocketChannelImpl)
  {
    try
    {
      return new ServerSocketAdaptor(paramServerSocketChannelImpl);
    }
    catch (IOException localIOException)
    {
      throw new Error(localIOException);
    }
  }

  private ServerSocketAdaptor(ServerSocketChannelImpl paramServerSocketChannelImpl)
    throws IOException
  {
    this.ssc = paramServerSocketChannelImpl;
  }

  public void bind(SocketAddress paramSocketAddress)
    throws IOException
  {
    bind(paramSocketAddress, 50);
  }

  public void bind(SocketAddress paramSocketAddress, int paramInt)
    throws IOException
  {
    if (paramSocketAddress == null)
      paramSocketAddress = new InetSocketAddress(0);
    try
    {
      this.ssc.bind(paramSocketAddress, paramInt);
    }
    catch (Exception localException)
    {
      Net.translateException(localException);
    }
  }

  public InetAddress getInetAddress()
  {
    if (!(this.ssc.isBound()))
      return null;
    return Net.asInetSocketAddress(this.ssc.localAddress()).getAddress();
  }

  public int getLocalPort()
  {
    if (!(this.ssc.isBound()))
      return -1;
    return Net.asInetSocketAddress(this.ssc.localAddress()).getPort();
  }

  // ERROR //
  public java.net.Socket accept()
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   4: invokevirtual 218	sun/nio/ch/ServerSocketChannelImpl:blockingLock	()Ljava/lang/Object;
    //   7: dup
    //   8: astore_1
    //   9: monitorenter
    //   10: aload_0
    //   11: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   14: invokevirtual 216	sun/nio/ch/ServerSocketChannelImpl:isBound	()Z
    //   17: ifne +11 -> 28
    //   20: new 100	java/nio/channels/IllegalBlockingModeException
    //   23: dup
    //   24: invokespecial 195	java/nio/channels/IllegalBlockingModeException:<init>	()V
    //   27: athrow
    //   28: aload_0
    //   29: getfield 176	sun/nio/ch/ServerSocketAdaptor:timeout	I
    //   32: ifne +40 -> 72
    //   35: aload_0
    //   36: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   39: invokevirtual 222	sun/nio/ch/ServerSocketChannelImpl:accept	()Ljava/nio/channels/SocketChannel;
    //   42: astore_2
    //   43: aload_2
    //   44: ifnonnull +21 -> 65
    //   47: aload_0
    //   48: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   51: invokevirtual 215	sun/nio/ch/ServerSocketChannelImpl:isBlocking	()Z
    //   54: ifne +11 -> 65
    //   57: new 100	java/nio/channels/IllegalBlockingModeException
    //   60: dup
    //   61: invokespecial 195	java/nio/channels/IllegalBlockingModeException:<init>	()V
    //   64: athrow
    //   65: aload_2
    //   66: invokevirtual 200	java/nio/channels/SocketChannel:socket	()Ljava/net/Socket;
    //   69: aload_1
    //   70: monitorexit
    //   71: areturn
    //   72: aconst_null
    //   73: astore_2
    //   74: aconst_null
    //   75: astore_3
    //   76: aload_0
    //   77: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   80: iconst_0
    //   81: invokevirtual 221	sun/nio/ch/ServerSocketChannelImpl:configureBlocking	(Z)Ljava/nio/channels/SelectableChannel;
    //   84: pop
    //   85: aload_0
    //   86: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   89: invokevirtual 222	sun/nio/ch/ServerSocketChannelImpl:accept	()Ljava/nio/channels/SocketChannel;
    //   92: dup
    //   93: astore 4
    //   95: ifnull +18 -> 113
    //   98: aload 4
    //   100: invokevirtual 200	java/nio/channels/SocketChannel:socket	()Ljava/net/Socket;
    //   103: astore 5
    //   105: jsr +153 -> 258
    //   108: aload_1
    //   109: monitorexit
    //   110: aload 5
    //   112: areturn
    //   113: aload_0
    //   114: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   117: invokestatic 225	sun/nio/ch/Util:getTemporarySelector	(Ljava/nio/channels/SelectableChannel;)Ljava/nio/channels/Selector;
    //   120: astore_3
    //   121: aload_0
    //   122: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   125: aload_3
    //   126: bipush 16
    //   128: invokevirtual 223	sun/nio/ch/ServerSocketChannelImpl:register	(Ljava/nio/channels/Selector;I)Ljava/nio/channels/SelectionKey;
    //   131: astore_2
    //   132: aload_0
    //   133: getfield 176	sun/nio/ch/ServerSocketAdaptor:timeout	I
    //   136: i2l
    //   137: lstore 5
    //   139: aload_0
    //   140: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   143: invokevirtual 217	sun/nio/ch/ServerSocketChannelImpl:isOpen	()Z
    //   146: ifne +11 -> 157
    //   149: new 99	java/nio/channels/ClosedChannelException
    //   152: dup
    //   153: invokespecial 194	java/nio/channels/ClosedChannelException:<init>	()V
    //   156: athrow
    //   157: invokestatic 188	java/lang/System:currentTimeMillis	()J
    //   160: lstore 7
    //   162: aload_3
    //   163: lload 5
    //   165: invokevirtual 198	java/nio/channels/Selector:select	(J)I
    //   168: istore 9
    //   170: iload 9
    //   172: ifle +38 -> 210
    //   175: aload_2
    //   176: invokevirtual 197	java/nio/channels/SelectionKey:isAcceptable	()Z
    //   179: ifeq +31 -> 210
    //   182: aload_0
    //   183: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   186: invokevirtual 222	sun/nio/ch/ServerSocketChannelImpl:accept	()Ljava/nio/channels/SocketChannel;
    //   189: dup
    //   190: astore 4
    //   192: ifnull +18 -> 210
    //   195: aload 4
    //   197: invokevirtual 200	java/nio/channels/SocketChannel:socket	()Ljava/net/Socket;
    //   200: astore 10
    //   202: jsr +56 -> 258
    //   205: aload_1
    //   206: monitorexit
    //   207: aload 10
    //   209: areturn
    //   210: aload_3
    //   211: invokevirtual 199	java/nio/channels/Selector:selectedKeys	()Ljava/util/Set;
    //   214: aload_2
    //   215: invokeinterface 226 2 0
    //   220: pop
    //   221: lload 5
    //   223: invokestatic 188	java/lang/System:currentTimeMillis	()J
    //   226: lload 7
    //   228: lsub
    //   229: lsub
    //   230: lstore 5
    //   232: lload 5
    //   234: lconst_0
    //   235: lcmp
    //   236: ifgt +11 -> 247
    //   239: new 98	java/net/SocketTimeoutException
    //   242: dup
    //   243: invokespecial 193	java/net/SocketTimeoutException:<init>	()V
    //   246: athrow
    //   247: goto -108 -> 139
    //   250: astore 11
    //   252: jsr +6 -> 258
    //   255: aload 11
    //   257: athrow
    //   258: astore 12
    //   260: aload_2
    //   261: ifnull +7 -> 268
    //   264: aload_2
    //   265: invokevirtual 196	java/nio/channels/SelectionKey:cancel	()V
    //   268: aload_0
    //   269: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   272: invokevirtual 217	sun/nio/ch/ServerSocketChannelImpl:isOpen	()Z
    //   275: ifeq +12 -> 287
    //   278: aload_0
    //   279: getfield 179	sun/nio/ch/ServerSocketAdaptor:ssc	Lsun/nio/ch/ServerSocketChannelImpl;
    //   282: iconst_1
    //   283: invokevirtual 221	sun/nio/ch/ServerSocketChannelImpl:configureBlocking	(Z)Ljava/nio/channels/SelectableChannel;
    //   286: pop
    //   287: aload_3
    //   288: ifnull +7 -> 295
    //   291: aload_3
    //   292: invokestatic 224	sun/nio/ch/Util:releaseTemporarySelector	(Ljava/nio/channels/Selector;)V
    //   295: ret 12
    //   297: astore_2
    //   298: aload_2
    //   299: invokestatic 201	sun/nio/ch/Net:translateException	(Ljava/lang/Exception;)V
    //   302: getstatic 177	sun/nio/ch/ServerSocketAdaptor:$assertionsDisabled	Z
    //   305: ifne +11 -> 316
    //   308: new 89	java/lang/AssertionError
    //   311: dup
    //   312: invokespecial 180	java/lang/AssertionError:<init>	()V
    //   315: athrow
    //   316: aconst_null
    //   317: aload_1
    //   318: monitorexit
    //   319: areturn
    //   320: astore 13
    //   322: aload_1
    //   323: monitorexit
    //   324: aload 13
    //   326: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   85	108	250	finally
    //   113	205	250	finally
    //   210	255	250	finally
    //   28	69	297	java/lang/Exception
    //   72	108	297	java/lang/Exception
    //   113	205	297	java/lang/Exception
    //   210	297	297	java/lang/Exception
    //   10	71	320	finally
    //   72	110	320	finally
    //   113	207	320	finally
    //   210	319	320	finally
    //   320	324	320	finally
  }

  public void close()
    throws IOException
  {
    try
    {
      this.ssc.close();
    }
    catch (Exception localException)
    {
      Net.translateException(localException);
    }
  }

  public ServerSocketChannel getChannel()
  {
    return this.ssc;
  }

  public boolean isBound()
  {
    return this.ssc.isBound();
  }

  public boolean isClosed()
  {
    return (!(this.ssc.isOpen()));
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
      this.opts = new OptionAdaptor(this.ssc);
    return this.opts;
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

  public String toString()
  {
    if (!(isBound()))
      return "ServerSocket[unbound]";
    return "ServerSocket[addr=" + getInetAddress() + ",localport=" + getLocalPort() + "]";
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
}