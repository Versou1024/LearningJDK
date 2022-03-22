package sun.nio.ch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Set;

public class SocketAdaptor extends Socket
{
  private final SocketChannelImpl sc;
  private volatile OptionAdaptor opts = null;
  private volatile int timeout = 0;
  private volatile int trafficClass = 0;
  private InputStream socketInputStream = null;

  private SocketAdaptor(SocketChannelImpl paramSocketChannelImpl)
  {
    this.sc = paramSocketChannelImpl;
  }

  public static Socket create(SocketChannelImpl paramSocketChannelImpl)
  {
    return new SocketAdaptor(paramSocketChannelImpl);
  }

  public SocketChannel getChannel()
  {
    return this.sc;
  }

  public void connect(SocketAddress paramSocketAddress)
    throws IOException
  {
    connect(paramSocketAddress, 0);
  }

  public void connect(SocketAddress paramSocketAddress, int paramInt)
    throws IOException
  {
    if (paramSocketAddress == null)
      throw new IllegalArgumentException("connect: The address can't be null");
    if (paramInt < 0)
      throw new IllegalArgumentException("connect: timeout can't be negative");
    synchronized (this.sc.blockingLock())
    {
      if (!(this.sc.isBlocking()))
        throw new IllegalBlockingModeException();
      try
      {
        if (paramInt == 0)
        {
          this.sc.connect(paramSocketAddress);
          monitorexit;
          return;
        }
        SelectionKey localSelectionKey = null;
        Selector localSelector = null;
        this.sc.configureBlocking(false);
        try
        {
          if (this.sc.connect(paramSocketAddress))
          {
            jsr 158;
            return;
          }
          localSelector = Util.getTemporarySelector(this.sc);
          localSelectionKey = this.sc.register(localSelector, 8);
          long l1 = paramInt;
          while (true)
          {
            if (!(this.sc.isOpen()))
              throw new ClosedChannelException();
            long l2 = System.currentTimeMillis();
            int i = localSelector.select(l1);
            if ((i > 0) && (localSelectionKey.isConnectable()) && (this.sc.finishConnect()))
              break;
            localSelector.selectedKeys().remove(localSelectionKey);
            l1 -= System.currentTimeMillis() - l2;
            if (l1 <= 3412039680571473920L)
            {
              try
              {
                this.sc.close();
              }
              catch (IOException localIOException)
              {
              }
              throw new SocketTimeoutException();
            }
          }
        }
        finally
        {
          jsr 6;
        }
        localObject3 = returnAddress;
        if (localSelectionKey != null)
          localSelectionKey.cancel();
        if (this.sc.isOpen())
          this.sc.configureBlocking(true);
        if (localSelector != null)
          Util.releaseTemporarySelector(localSelector);
        ret;
      }
      catch (Exception localException)
      {
        Net.translateException(localException, true);
      }
    }
  }

  public void bind(SocketAddress paramSocketAddress)
    throws IOException
  {
    try
    {
      if (paramSocketAddress == null)
        paramSocketAddress = new InetSocketAddress(0);
      this.sc.bind(paramSocketAddress);
    }
    catch (Exception localException)
    {
      Net.translateException(localException);
    }
  }

  public InetAddress getInetAddress()
  {
    if (!(this.sc.isConnected()))
      return null;
    return Net.asInetSocketAddress(this.sc.remoteAddress()).getAddress();
  }

  public InetAddress getLocalAddress()
  {
    if (!(this.sc.isBound()))
      return new InetSocketAddress(0).getAddress();
    return Net.asInetSocketAddress(this.sc.localAddress()).getAddress();
  }

  public int getPort()
  {
    if (!(this.sc.isConnected()))
      return 0;
    return Net.asInetSocketAddress(this.sc.remoteAddress()).getPort();
  }

  public int getLocalPort()
  {
    if (!(this.sc.isBound()))
      return 0;
    return Net.asInetSocketAddress(this.sc.localAddress()).getPort();
  }

  public InputStream getInputStream()
    throws IOException
  {
    if (!(this.sc.isOpen()))
      throw new SocketException("Socket is closed");
    if (!(this.sc.isConnected()))
      throw new SocketException("Socket is not connected");
    if (!(this.sc.isInputOpen()))
      throw new SocketException("Socket input is shutdown");
    if (this.socketInputStream == null)
      try
      {
        this.socketInputStream = ((InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(this)
        {
          public Object run()
            throws IOException
          {
            return new SocketAdaptor.SocketInputStream(this.this$0, null);
          }
        }));
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        throw ((IOException)localPrivilegedActionException.getException());
      }
    return this.socketInputStream;
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    if (!(this.sc.isOpen()))
      throw new SocketException("Socket is closed");
    if (!(this.sc.isConnected()))
      throw new SocketException("Socket is not connected");
    if (!(this.sc.isOutputOpen()))
      throw new SocketException("Socket output is shutdown");
    OutputStream localOutputStream = null;
    try
    {
      localOutputStream = (OutputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(this)
      {
        public Object run()
          throws IOException
        {
          return Channels.newOutputStream(SocketAdaptor.access$000(this.this$0));
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
    return localOutputStream;
  }

  private OptionAdaptor opts()
  {
    if (this.opts == null)
      this.opts = new OptionAdaptor(this.sc);
    return this.opts;
  }

  public void setTcpNoDelay(boolean paramBoolean)
    throws SocketException
  {
    opts().setTcpNoDelay(paramBoolean);
  }

  public boolean getTcpNoDelay()
    throws SocketException
  {
    return opts().getTcpNoDelay();
  }

  public void setSoLinger(boolean paramBoolean, int paramInt)
    throws SocketException
  {
    opts().setSoLinger(paramBoolean, paramInt);
  }

  public int getSoLinger()
    throws SocketException
  {
    return opts().getSoLinger();
  }

  public void sendUrgentData(int paramInt)
    throws IOException
  {
    throw new SocketException("Urgent data not supported");
  }

  public void setOOBInline(boolean paramBoolean)
    throws SocketException
  {
    opts().setOOBInline(paramBoolean);
  }

  public boolean getOOBInline()
    throws SocketException
  {
    return opts().getOOBInline();
  }

  public void setSoTimeout(int paramInt)
    throws SocketException
  {
    if (paramInt < 0)
      throw new IllegalArgumentException("timeout can't be negative");
    this.timeout = paramInt;
  }

  public int getSoTimeout()
    throws SocketException
  {
    return this.timeout;
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

  public void setKeepAlive(boolean paramBoolean)
    throws SocketException
  {
    opts().setKeepAlive(paramBoolean);
  }

  public boolean getKeepAlive()
    throws SocketException
  {
    return opts().getKeepAlive();
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

  public void close()
    throws IOException
  {
    try
    {
      this.sc.close();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public void shutdownInput()
    throws IOException
  {
    try
    {
      this.sc.shutdownInput();
    }
    catch (Exception localException)
    {
      Net.translateException(localException);
    }
  }

  public void shutdownOutput()
    throws IOException
  {
    try
    {
      this.sc.shutdownOutput();
    }
    catch (Exception localException)
    {
      Net.translateException(localException);
    }
  }

  public String toString()
  {
    if (this.sc.isConnected())
      return "Socket[addr=" + getInetAddress() + ",port=" + getPort() + ",localport=" + getLocalPort() + "]";
    return "Socket[unconnected]";
  }

  public boolean isConnected()
  {
    return this.sc.isConnected();
  }

  public boolean isBound()
  {
    return this.sc.isBound();
  }

  public boolean isClosed()
  {
    return (!(this.sc.isOpen()));
  }

  public boolean isInputShutdown()
  {
    return (!(this.sc.isInputOpen()));
  }

  public boolean isOutputShutdown()
  {
    return (!(this.sc.isOutputOpen()));
  }

  private class SocketInputStream extends ChannelInputStream
  {
    private SocketInputStream()
    {
      super(SocketAdaptor.access$000(paramSocketAdaptor));
    }

    // ERROR //
    protected int read()
      throws IOException
    {
      // Byte code:
      //   0: aload_0
      //   1: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   4: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   7: invokevirtual 103	sun/nio/ch/SocketChannelImpl:blockingLock	()Ljava/lang/Object;
      //   10: dup
      //   11: astore_2
      //   12: monitorenter
      //   13: aload_0
      //   14: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   17: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   20: invokevirtual 101	sun/nio/ch/SocketChannelImpl:isBlocking	()Z
      //   23: ifne +11 -> 34
      //   26: new 45	java/nio/channels/IllegalBlockingModeException
      //   29: dup
      //   30: invokespecial 92	java/nio/channels/IllegalBlockingModeException:<init>	()V
      //   33: athrow
      //   34: aload_0
      //   35: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   38: invokestatic 98	sun/nio/ch/SocketAdaptor:access$100	(Lsun/nio/ch/SocketAdaptor;)I
      //   41: ifne +17 -> 58
      //   44: aload_0
      //   45: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   48: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   51: aload_1
      //   52: invokevirtual 104	sun/nio/ch/SocketChannelImpl:read	(Ljava/nio/ByteBuffer;)I
      //   55: aload_2
      //   56: monitorexit
      //   57: ireturn
      //   58: aconst_null
      //   59: astore_3
      //   60: aconst_null
      //   61: astore 4
      //   63: aload_0
      //   64: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   67: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   70: iconst_0
      //   71: invokevirtual 105	sun/nio/ch/SocketChannelImpl:configureBlocking	(Z)Ljava/nio/channels/SelectableChannel;
      //   74: pop
      //   75: aload_0
      //   76: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   79: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   82: aload_1
      //   83: invokevirtual 104	sun/nio/ch/SocketChannelImpl:read	(Ljava/nio/ByteBuffer;)I
      //   86: dup
      //   87: istore 5
      //   89: ifeq +15 -> 104
      //   92: iload 5
      //   94: istore 6
      //   96: jsr +169 -> 265
      //   99: aload_2
      //   100: monitorexit
      //   101: iload 6
      //   103: ireturn
      //   104: aload_0
      //   105: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   108: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   111: invokestatic 108	sun/nio/ch/Util:getTemporarySelector	(Ljava/nio/channels/SelectableChannel;)Ljava/nio/channels/Selector;
      //   114: astore 4
      //   116: aload_0
      //   117: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   120: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   123: aload 4
      //   125: iconst_1
      //   126: invokevirtual 106	sun/nio/ch/SocketChannelImpl:register	(Ljava/nio/channels/Selector;I)Ljava/nio/channels/SelectionKey;
      //   129: astore_3
      //   130: aload_0
      //   131: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   134: invokestatic 98	sun/nio/ch/SocketAdaptor:access$100	(Lsun/nio/ch/SocketAdaptor;)I
      //   137: i2l
      //   138: lstore 6
      //   140: aload_0
      //   141: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   144: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   147: invokevirtual 102	sun/nio/ch/SocketChannelImpl:isOpen	()Z
      //   150: ifne +11 -> 161
      //   153: new 44	java/nio/channels/ClosedChannelException
      //   156: dup
      //   157: invokespecial 91	java/nio/channels/ClosedChannelException:<init>	()V
      //   160: athrow
      //   161: invokestatic 89	java/lang/System:currentTimeMillis	()J
      //   164: lstore 8
      //   166: aload 4
      //   168: lload 6
      //   170: invokevirtual 95	java/nio/channels/Selector:select	(J)I
      //   173: istore 10
      //   175: iload 10
      //   177: ifle +39 -> 216
      //   180: aload_3
      //   181: invokevirtual 94	java/nio/channels/SelectionKey:isReadable	()Z
      //   184: ifeq +32 -> 216
      //   187: aload_0
      //   188: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   191: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   194: aload_1
      //   195: invokevirtual 104	sun/nio/ch/SocketChannelImpl:read	(Ljava/nio/ByteBuffer;)I
      //   198: dup
      //   199: istore 5
      //   201: ifeq +15 -> 216
      //   204: iload 5
      //   206: istore 11
      //   208: jsr +57 -> 265
      //   211: aload_2
      //   212: monitorexit
      //   213: iload 11
      //   215: ireturn
      //   216: aload 4
      //   218: invokevirtual 96	java/nio/channels/Selector:selectedKeys	()Ljava/util/Set;
      //   221: aload_3
      //   222: invokeinterface 109 2 0
      //   227: pop
      //   228: lload 6
      //   230: invokestatic 89	java/lang/System:currentTimeMillis	()J
      //   233: lload 8
      //   235: lsub
      //   236: lsub
      //   237: lstore 6
      //   239: lload 6
      //   241: lconst_0
      //   242: lcmp
      //   243: ifgt +11 -> 254
      //   246: new 43	java/net/SocketTimeoutException
      //   249: dup
      //   250: invokespecial 90	java/net/SocketTimeoutException:<init>	()V
      //   253: athrow
      //   254: goto -114 -> 140
      //   257: astore 12
      //   259: jsr +6 -> 265
      //   262: aload 12
      //   264: athrow
      //   265: astore 13
      //   267: aload_3
      //   268: ifnull +7 -> 275
      //   271: aload_3
      //   272: invokevirtual 93	java/nio/channels/SelectionKey:cancel	()V
      //   275: aload_0
      //   276: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   279: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   282: invokevirtual 102	sun/nio/ch/SocketChannelImpl:isOpen	()Z
      //   285: ifeq +15 -> 300
      //   288: aload_0
      //   289: getfield 88	sun/nio/ch/SocketAdaptor$SocketInputStream:this$0	Lsun/nio/ch/SocketAdaptor;
      //   292: invokestatic 99	sun/nio/ch/SocketAdaptor:access$000	(Lsun/nio/ch/SocketAdaptor;)Lsun/nio/ch/SocketChannelImpl;
      //   295: iconst_1
      //   296: invokevirtual 105	sun/nio/ch/SocketChannelImpl:configureBlocking	(Z)Ljava/nio/channels/SelectableChannel;
      //   299: pop
      //   300: aload 4
      //   302: ifnull +8 -> 310
      //   305: aload 4
      //   307: invokestatic 107	sun/nio/ch/Util:releaseTemporarySelector	(Ljava/nio/channels/Selector;)V
      //   310: ret 13
      //   312: astore 14
      //   314: aload_2
      //   315: monitorexit
      //   316: aload 14
      //   318: athrow
      //
      // Exception table:
      //   from	to	target	type
      //   75	99	257	finally
      //   104	211	257	finally
      //   216	262	257	finally
      //   13	57	312	finally
      //   58	101	312	finally
      //   104	213	312	finally
      //   216	316	312	finally
    }
  }
}