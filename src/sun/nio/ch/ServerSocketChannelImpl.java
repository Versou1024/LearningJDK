package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

class ServerSocketChannelImpl extends ServerSocketChannel
  implements SelChImpl
{
  private static NativeDispatcher nd;
  private final FileDescriptor fd;
  private int fdVal;
  private volatile long thread = 3412045659165949952L;
  private final Object lock = new Object();
  private final Object stateLock = new Object();
  private static final int ST_UNINITIALIZED = -1;
  private static final int ST_INUSE = 0;
  private static final int ST_KILLED = 1;
  private int state = -1;
  private SocketAddress localAddress = null;
  private SocketOpts.IP.TCP options = null;
  ServerSocket socket;

  public ServerSocketChannelImpl(SelectorProvider paramSelectorProvider)
    throws IOException
  {
    super(paramSelectorProvider);
    this.fd = Net.serverSocket(true);
    this.fdVal = IOUtil.fdVal(this.fd);
    this.state = 0;
  }

  public ServerSocketChannelImpl(SelectorProvider paramSelectorProvider, FileDescriptor paramFileDescriptor)
    throws IOException
  {
    super(paramSelectorProvider);
    this.fd = paramFileDescriptor;
    this.fdVal = IOUtil.fdVal(paramFileDescriptor);
    this.state = 0;
    this.localAddress = Net.localAddress(paramFileDescriptor);
  }

  public ServerSocket socket()
  {
    synchronized (this.stateLock)
    {
      if (this.socket == null)
        this.socket = ServerSocketAdaptor.create(this);
      return this.socket;
    }
  }

  public boolean isBound()
  {
    synchronized (this.stateLock)
    {
      return ((this.localAddress != null) ? 1 : false);
    }
  }

  public SocketAddress localAddress()
  {
    synchronized (this.stateLock)
    {
      return this.localAddress;
    }
  }

  public void bind(SocketAddress paramSocketAddress, int paramInt)
    throws IOException
  {
    synchronized (this.lock)
    {
      if (!(isOpen()))
        throw new ClosedChannelException();
      if (isBound())
        throw new AlreadyBoundException();
      InetSocketAddress localInetSocketAddress = Net.checkAddress(paramSocketAddress);
      SecurityManager localSecurityManager = System.getSecurityManager();
      if (localSecurityManager != null)
        localSecurityManager.checkListen(localInetSocketAddress.getPort());
      Net.bind(this.fd, localInetSocketAddress.getAddress(), localInetSocketAddress.getPort());
      listen(this.fd, (paramInt < 1) ? 50 : paramInt);
      synchronized (this.stateLock)
      {
        this.localAddress = Net.localAddress(this.fd);
      }
    }
  }

  public SocketChannel accept()
    throws IOException
  {
    synchronized (this.lock)
    {
      if (!(isOpen()))
        throw new ClosedChannelException();
      if (!(isBound()))
        throw new NotYetBoundException();
      SocketChannelImpl localSocketChannelImpl = null;
      int i = 0;
      FileDescriptor localFileDescriptor = new FileDescriptor();
      InetSocketAddress[] arrayOfInetSocketAddress = new InetSocketAddress[1];
      try
      {
        begin();
        if (!(isOpen()))
        {
          localInetSocketAddress = null;
          jsr 58;
          return localInetSocketAddress;
        }
        this.thread = NativeThread.current();
        while (true)
        {
          i = accept0(this.fd, localFileDescriptor, arrayOfInetSocketAddress);
          if ((i != -3) || (!(isOpen())))
            break;
        }
      }
      finally
      {
        jsr 6;
      }
      localObject3 = returnAddress;
      this.thread = 3412047359972999168L;
      end(i > 0);
      if ((!($assertionsDisabled)) && (!(IOStatus.check(i))))
        throw new AssertionError();
      ret;
      if (i >= 1)
        break label180;
      return null;
      label180: IOUtil.configureBlocking(localFileDescriptor, true);
      InetSocketAddress localInetSocketAddress = arrayOfInetSocketAddress[0];
      localSocketChannelImpl = new SocketChannelImpl(provider(), localFileDescriptor, localInetSocketAddress);
      SecurityManager localSecurityManager = System.getSecurityManager();
      if (localSecurityManager == null)
        break label248;
      try
      {
        localSecurityManager.checkAccept(localInetSocketAddress.getAddress().getHostAddress(), localInetSocketAddress.getPort());
      }
      catch (SecurityException localSecurityException)
      {
        localSocketChannelImpl.close();
        throw localSecurityException;
      }
      label248: return localSocketChannelImpl;
    }
  }

  protected void implConfigureBlocking(boolean paramBoolean)
    throws IOException
  {
    IOUtil.configureBlocking(this.fd, paramBoolean);
  }

  public SocketOpts options()
  {
    synchronized (this.stateLock)
    {
      if (this.options == null)
      {
        1 local1 = new SocketOptsImpl.Dispatcher(this)
        {
          int getInt()
            throws IOException
          {
            return Net.getIntOption(ServerSocketChannelImpl.access$000(this.this$0), paramInt);
          }

          void setInt(, int paramInt2)
            throws IOException
          {
            Net.setIntOption(ServerSocketChannelImpl.access$000(this.this$0), paramInt1, paramInt2);
          }
        };
        this.options = new SocketOptsImpl.IP.TCP(local1);
      }
      return this.options;
    }
  }

  protected void implCloseSelectableChannel()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      nd.preClose(this.fd);
      long l = this.thread;
      if (l != 3412047067915223040L)
        NativeThread.signal(l);
      if (!(isRegistered()))
        kill();
    }
  }

  public void kill()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      if (this.state != 1)
        break label18;
      return;
      label18: if (this.state != -1)
        break label34;
      this.state = 1;
      return;
      label34: if (($assertionsDisabled) || ((!(isOpen())) && (!(isRegistered()))))
        break label62;
      throw new AssertionError();
      label62: nd.close(this.fd);
      this.state = 1;
    }
  }

  public boolean translateReadyOps(int paramInt1, int paramInt2, SelectionKeyImpl paramSelectionKeyImpl)
  {
    int i = paramSelectionKeyImpl.nioInterestOps();
    int j = paramSelectionKeyImpl.nioReadyOps();
    int k = paramInt2;
    if ((paramInt1 & 0x20) != 0)
      return false;
    if ((paramInt1 & 0x18) != 0)
    {
      k = i;
      paramSelectionKeyImpl.nioReadyOps(k);
      return ((k & (j ^ 0xFFFFFFFF)) != 0);
    }
    if (((paramInt1 & 0x1) != 0) && ((i & 0x10) != 0))
      k |= 16;
    paramSelectionKeyImpl.nioReadyOps(k);
    return ((k & (j ^ 0xFFFFFFFF)) != 0);
  }

  public boolean translateAndUpdateReadyOps(int paramInt, SelectionKeyImpl paramSelectionKeyImpl)
  {
    return translateReadyOps(paramInt, paramSelectionKeyImpl.nioReadyOps(), paramSelectionKeyImpl);
  }

  public boolean translateAndSetReadyOps(int paramInt, SelectionKeyImpl paramSelectionKeyImpl)
  {
    return translateReadyOps(paramInt, 0, paramSelectionKeyImpl);
  }

  public void translateAndSetInterestOps(int paramInt, SelectionKeyImpl paramSelectionKeyImpl)
  {
    int i = 0;
    if ((paramInt & 0x10) != 0)
      i |= 1;
    paramSelectionKeyImpl.selector.putEventOps(paramSelectionKeyImpl, i);
  }

  public FileDescriptor getFD()
  {
    return this.fd;
  }

  public int getFDVal()
  {
    return this.fdVal;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append(getClass().getName());
    localStringBuffer.append('[');
    if (!(isOpen()))
      localStringBuffer.append("closed");
    else
      synchronized (this.stateLock)
      {
        if (localAddress() == null)
          localStringBuffer.append("unbound");
        else
          localStringBuffer.append(localAddress().toString());
      }
    localStringBuffer.append(']');
    return localStringBuffer.toString();
  }

  private static native void listen(FileDescriptor paramFileDescriptor, int paramInt)
    throws IOException;

  private native int accept0(FileDescriptor paramFileDescriptor1, FileDescriptor paramFileDescriptor2, InetSocketAddress[] paramArrayOfInetSocketAddress)
    throws IOException;

  private static native void initIDs();

  static
  {
    Util.load();
    initIDs();
    nd = new SocketDispatcher();
  }
}