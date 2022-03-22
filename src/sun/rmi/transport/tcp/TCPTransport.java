package sun.rmi.transport.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.rmi.server.LogStream;
import java.rmi.server.RMIFailureHandler;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UID;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import sun.rmi.runtime.Log;
import sun.rmi.runtime.NewThreadAction;
import sun.rmi.transport.Connection;
import sun.rmi.transport.DGCAckHandler;
import sun.rmi.transport.Endpoint;
import sun.rmi.transport.StreamRemoteCall;
import sun.rmi.transport.Target;
import sun.rmi.transport.Transport;
import sun.rmi.transport.proxy.HttpReceiveSocket;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetLongAction;
import sun.security.action.GetPropertyAction;

public class TCPTransport extends Transport
{
  static final Log tcpLog;
  private static final int maxConnectionThreads;
  private static final long threadKeepAliveTime;
  private static final ExecutorService connectionThreadPool;
  private static final AtomicInteger connectionCount;
  private static final ThreadLocal<ConnectionHandler> threadConnectionHandler;
  private final LinkedList<TCPEndpoint> epList;
  private int exportCount = 0;
  private ServerSocket server = null;
  private final Map<TCPEndpoint, Reference<TCPChannel>> channelTable = new WeakHashMap();
  static final RMISocketFactory defaultSocketFactory;
  private static final int connectionReadTimeout;

  TCPTransport(LinkedList<TCPEndpoint> paramLinkedList)
  {
    this.epList = paramLinkedList;
    if (tcpLog.isLoggable(Log.BRIEF))
      tcpLog.log(Log.BRIEF, "Version = 2, ep = " + getEndpoint());
  }

  public void shedConnectionCaches()
  {
    ArrayList localArrayList;
    Object localObject2;
    synchronized (this.channelTable)
    {
      localArrayList = new ArrayList(this.channelTable.values().size());
      localObject2 = this.channelTable.values().iterator();
      while (((Iterator)localObject2).hasNext())
      {
        Reference localReference = (Reference)((Iterator)localObject2).next();
        TCPChannel localTCPChannel = (TCPChannel)localReference.get();
        if (localTCPChannel != null)
          localArrayList.add(localTCPChannel);
      }
    }
    ??? = localArrayList.iterator();
    while (((Iterator)???).hasNext())
    {
      localObject2 = (TCPChannel)((Iterator)???).next();
      ((TCPChannel)localObject2).shedCache();
    }
  }

  public TCPChannel getChannel(Endpoint paramEndpoint)
  {
    TCPChannel localTCPChannel = null;
    if (paramEndpoint instanceof TCPEndpoint)
      synchronized (this.channelTable)
      {
        Reference localReference = (Reference)this.channelTable.get(paramEndpoint);
        if (localReference != null)
          localTCPChannel = (TCPChannel)localReference.get();
        if (localTCPChannel == null)
        {
          TCPEndpoint localTCPEndpoint = (TCPEndpoint)paramEndpoint;
          localTCPChannel = new TCPChannel(this, localTCPEndpoint);
          this.channelTable.put(localTCPEndpoint, new WeakReference(localTCPChannel));
        }
      }
    return localTCPChannel;
  }

  public void free(Endpoint paramEndpoint)
  {
    if (paramEndpoint instanceof TCPEndpoint)
      synchronized (this.channelTable)
      {
        Reference localReference = (Reference)this.channelTable.remove(paramEndpoint);
        if (localReference != null)
        {
          TCPChannel localTCPChannel = (TCPChannel)localReference.get();
          if (localTCPChannel != null)
            localTCPChannel.shedCache();
        }
      }
  }

  public void exportObject(Target paramTarget)
    throws RemoteException
  {
    synchronized (this)
    {
      listen();
      this.exportCount += 1;
    }
    int i = 0;
    try
    {
      super.exportObject(paramTarget);
      i = 1;
    }
    finally
    {
      if (i == 0)
        synchronized (this)
        {
          decrementExportCount();
        }
    }
  }

  protected synchronized void targetUnexported()
  {
    decrementExportCount();
  }

  private void decrementExportCount()
  {
    if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this))))
      throw new AssertionError();
    this.exportCount -= 1;
    if ((this.exportCount == 0) && (getEndpoint().getListenPort() != 0))
    {
      ServerSocket localServerSocket = this.server;
      this.server = null;
      try
      {
        localServerSocket.close();
      }
      catch (IOException localIOException)
      {
      }
    }
  }

  protected void checkAcceptPermission(AccessControlContext paramAccessControlContext)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager == null)
      return;
    ConnectionHandler localConnectionHandler = (ConnectionHandler)threadConnectionHandler.get();
    if (localConnectionHandler == null)
      throw new Error("checkAcceptPermission not in ConnectionHandler thread");
    localConnectionHandler.checkAcceptPermission(localSecurityManager, paramAccessControlContext);
  }

  private TCPEndpoint getEndpoint()
  {
    synchronized (this.epList)
    {
      return ((TCPEndpoint)this.epList.getLast());
    }
  }

  private void listen()
    throws RemoteException
  {
    if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this))))
      throw new AssertionError();
    TCPEndpoint localTCPEndpoint = getEndpoint();
    int i = localTCPEndpoint.getPort();
    if (this.server == null)
    {
      if (tcpLog.isLoggable(Log.BRIEF))
        tcpLog.log(Log.BRIEF, "(port " + i + ") create server socket");
      try
      {
        this.server = localTCPEndpoint.newServerSocket();
        Thread localThread = (Thread)AccessController.doPrivileged(new NewThreadAction(new AcceptLoop(this, this.server), "TCP Accept-" + i, true));
        localThread.start();
      }
      catch (BindException localBindException)
      {
        throw new ExportException("Port already in use: " + i, localBindException);
      }
      catch (IOException localIOException)
      {
        throw new ExportException("Listen failed on port: " + i, localIOException);
      }
    }
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkListen(i);
  }

  private static void closeSocket(Socket paramSocket)
  {
    try
    {
      paramSocket.close();
    }
    catch (IOException localIOException)
    {
    }
  }

  void handleMessages(Connection paramConnection, boolean paramBoolean)
  {
    int i = getEndpoint().getPort();
    try
    {
      int j;
      DataInputStream localDataInputStream = new DataInputStream(paramConnection.getInputStream());
      do
      {
        j = localDataInputStream.read();
        if (j == -1)
        {
          if (!(tcpLog.isLoggable(Log.BRIEF)))
            break;
          tcpLog.log(Log.BRIEF, "(port " + i + ") connection closed");
          break;
        }
        if (tcpLog.isLoggable(Log.BRIEF))
          tcpLog.log(Log.BRIEF, "(port " + i + ") op = " + j);
        switch (j)
        {
        case 80:
          StreamRemoteCall localStreamRemoteCall = new StreamRemoteCall(paramConnection);
          if (!(serviceCall(localStreamRemoteCall)))
            return;
        case 82:
          DataOutputStream localDataOutputStream = new DataOutputStream(paramConnection.getOutputStream());
          localDataOutputStream.writeByte(83);
          paramConnection.releaseOutputStream();
          break;
        case 84:
          DGCAckHandler.received(UID.read(localDataInputStream));
          break;
        case 81:
        case 83:
        default:
          throw new IOException("unknown transport op " + j);
        }
      }
      while (paramBoolean);
    }
    catch (IOException localIOException3)
    {
      if (tcpLog.isLoggable(Log.BRIEF))
        tcpLog.log(Log.BRIEF, "(port " + i + ") exception: ", localIOException2);
    }
    finally
    {
      try
      {
        paramConnection.close();
      }
      catch (IOException localIOException5)
      {
      }
    }
  }

  public static String getClientHost()
    throws ServerNotActiveException
  {
    ConnectionHandler localConnectionHandler = (ConnectionHandler)threadConnectionHandler.get();
    if (localConnectionHandler != null)
      return localConnectionHandler.getClientHost();
    throw new ServerNotActiveException("not in a remote call");
  }

  static
  {
    tcpLog = Log.getLog("sun.rmi.transport.tcp", "tcp", LogStream.parseLevel((String)AccessController.doPrivileged(new GetPropertyAction("sun.rmi.transport.tcp.logLevel"))));
    maxConnectionThreads = ((Integer)AccessController.doPrivileged(new GetIntegerAction("sun.rmi.transport.tcp.maxConnectionThreads", 2147483647))).intValue();
    threadKeepAliveTime = ((Long)AccessController.doPrivileged(new GetLongAction("sun.rmi.transport.tcp.threadKeepAliveTime", 60000L))).longValue();
    connectionThreadPool = new ThreadPoolExecutor(0, maxConnectionThreads, threadKeepAliveTime, TimeUnit.MILLISECONDS, new SynchronousQueue(), new ThreadFactory()
    {
      public Thread newThread(Runnable paramRunnable)
      {
        return ((Thread)AccessController.doPrivileged(new NewThreadAction(paramRunnable, "TCP Connection(idle)", true, true)));
      }
    });
    connectionCount = new AtomicInteger(0);
    threadConnectionHandler = new ThreadLocal();
    defaultSocketFactory = RMISocketFactory.getDefaultSocketFactory();
    connectionReadTimeout = ((Integer)AccessController.doPrivileged(new GetIntegerAction("sun.rmi.transport.tcp.readTimeout", 7200000))).intValue();
  }

  private class AcceptLoop
  implements Runnable
  {
    private final ServerSocket serverSocket;
    private long lastExceptionTime = 3412046294821109760L;
    private int recentExceptionCount;

    AcceptLoop(, ServerSocket paramServerSocket)
    {
      this.serverSocket = paramServerSocket;
    }

    public void run()
    {
      try
      {
        executeAcceptLoop();
      }
      finally
      {
        try
        {
          this.serverSocket.close();
        }
        catch (IOException localIOException2)
        {
        }
      }
    }

    private void executeAcceptLoop()
    {
      if (TCPTransport.tcpLog.isLoggable(Log.BRIEF))
        TCPTransport.tcpLog.log(Log.BRIEF, "listening on port " + TCPTransport.access$000(this.this$0).getPort());
      while (true)
      {
        Socket localSocket = null;
        try
        {
          localSocket = this.serverSocket.accept();
          InetAddress localInetAddress = localSocket.getInetAddress();
          String str = (localInetAddress != null) ? localInetAddress.getHostAddress() : "0.0.0.0";
          try
          {
            TCPTransport.access$100().execute(new TCPTransport.ConnectionHandler(this.this$0, localSocket, str));
          }
          catch (RejectedExecutionException localRejectedExecutionException)
          {
            TCPTransport.access$200(localSocket);
            label287: TCPTransport.tcpLog.log(Log.BRIEF, "rejected connection from " + str);
          }
        }
        catch (Throwable localThrowable1)
        {
          try
          {
            if (this.serverSocket.isClosed())
            {
              if (localSocket != null)
                TCPTransport.access$200(localSocket);
              return;
            }
            try
            {
              if (TCPTransport.tcpLog.isLoggable(Level.WARNING))
                TCPTransport.tcpLog.log(Level.WARNING, "accept loop for " + this.serverSocket + " throws", localThrowable1);
            }
            catch (Throwable localThrowable2)
            {
            }
          }
          finally
          {
            if (localSocket != null)
              TCPTransport.access$200(localSocket);
          }
          if (!(localThrowable1 instanceof SecurityException))
            try
            {
              TCPEndpoint.shedConnectionCaches();
            }
            catch (Throwable localThrowable3)
            {
            }
          if ((localThrowable1 instanceof Exception) || (localThrowable1 instanceof OutOfMemoryError) || (localThrowable1 instanceof NoClassDefFoundError))
          {
            if (continueAfterAcceptFailure(localThrowable1))
              break label287;
            return;
          }
          throw ((Error)localThrowable1);
        }
      }
    }

    private boolean continueAfterAcceptFailure()
    {
      RMIFailureHandler localRMIFailureHandler = RMISocketFactory.getFailureHandler();
      if (localRMIFailureHandler != null)
        return localRMIFailureHandler.failure(new InvocationTargetException(paramThrowable));
      throttleLoopOnException();
      return true;
    }

    private void throttleLoopOnException()
    {
      long l = System.currentTimeMillis();
      if ((this.lastExceptionTime == 3412047755109990400L) || (l - this.lastExceptionTime > 5000L))
      {
        this.lastExceptionTime = l;
        this.recentExceptionCount = 0;
      }
      else if (++this.recentExceptionCount >= 10)
      {
        try
        {
          Thread.sleep(10000L);
        }
        catch (InterruptedException localInterruptedException)
        {
        }
      }
    }
  }

  private class ConnectionHandler
  implements Runnable
  {
    private static final int POST = 1347375956;
    private AccessControlContext okContext;
    private Map<AccessControlContext, Reference<AccessControlContext>> authCache;
    private SecurityManager cacheSecurityManager = null;
    private Socket socket;
    private String remoteHost;

    ConnectionHandler(, Socket paramSocket, String paramString)
    {
      this.socket = paramSocket;
      this.remoteHost = paramString;
    }

    String getClientHost()
    {
      return this.remoteHost;
    }

    void checkAcceptPermission(, AccessControlContext paramAccessControlContext)
    {
      if (paramSecurityManager != this.cacheSecurityManager)
      {
        this.okContext = null;
        this.authCache = new WeakHashMap();
        this.cacheSecurityManager = paramSecurityManager;
      }
      if ((paramAccessControlContext.equals(this.okContext)) || (this.authCache.containsKey(paramAccessControlContext)))
        return;
      InetAddress localInetAddress = this.socket.getInetAddress();
      String str = (localInetAddress != null) ? localInetAddress.getHostAddress() : "*";
      paramSecurityManager.checkAccept(str, this.socket.getPort());
      this.authCache.put(paramAccessControlContext, new SoftReference(paramAccessControlContext));
      this.okContext = paramAccessControlContext;
    }

    public void run()
    {
      Thread localThread = Thread.currentThread();
      String str = localThread.getName();
      try
      {
        localThread.setName("RMI TCP Connection(" + TCPTransport.access$300().incrementAndGet() + ")-" + this.remoteHost);
        run0();
      }
      finally
      {
        localThread.setName(str);
      }
    }

    private void run0()
    {
      DataOutputStream localDataOutputStream;
      TCPEndpoint localTCPEndpoint1 = TCPTransport.access$000(this.this$0);
      int i = localTCPEndpoint1.getPort();
      TCPTransport.access$400().set(this);
      try
      {
        this.socket.setTcpNoDelay(true);
      }
      catch (Exception localException1)
      {
      }
      try
      {
        if (TCPTransport.access$500() > 0)
          this.socket.setSoTimeout(TCPTransport.access$500());
      }
      catch (Exception localException2)
      {
      }
      try
      {
        TCPEndpoint localTCPEndpoint2;
        TCPChannel localTCPChannel;
        TCPConnection localTCPConnection;
        InputStream localInputStream = this.socket.getInputStream();
        BufferedInputStream localBufferedInputStream = new BufferedInputStream(localInputStream);
        localBufferedInputStream.mark(4);
        DataInputStream localDataInputStream = new DataInputStream(localBufferedInputStream);
        int j = localDataInputStream.readInt();
        if (j == 1347375956)
        {
          TCPTransport.tcpLog.log(Log.BRIEF, "decoding HTTP-wrapped call");
          localBufferedInputStream.reset();
          try
          {
            this.socket = new HttpReceiveSocket(this.socket, localBufferedInputStream, null);
            this.remoteHost = "0.0.0.0";
            localInputStream = this.socket.getInputStream();
            localBufferedInputStream = new BufferedInputStream(localInputStream);
            localDataInputStream = new DataInputStream(localBufferedInputStream);
            j = localDataInputStream.readInt();
          }
          catch (IOException localIOException2)
          {
            throw new RemoteException("Error HTTP-unwrapping call", localIOException2);
          }
        }
        int k = localDataInputStream.readShort();
        if ((j != 1246907721) || (k != 2))
        {
          TCPTransport.access$200(this.socket);
          return;
        }
        OutputStream localOutputStream = this.socket.getOutputStream();
        BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(localOutputStream);
        localDataOutputStream = new DataOutputStream(localBufferedOutputStream);
        int l = this.socket.getPort();
        if (TCPTransport.tcpLog.isLoggable(Log.BRIEF))
          TCPTransport.tcpLog.log(Log.BRIEF, "accepted socket from [" + this.remoteHost + ":" + l + "]");
        int i1 = localDataInputStream.readByte();
        switch (i1)
        {
        case 76:
          localTCPEndpoint2 = new TCPEndpoint(this.remoteHost, this.socket.getLocalPort(), localTCPEndpoint1.getClientSocketFactory(), localTCPEndpoint1.getServerSocketFactory());
          localTCPChannel = new TCPChannel(this.this$0, localTCPEndpoint2);
          localTCPConnection = new TCPConnection(localTCPChannel, this.socket, localBufferedInputStream, localBufferedOutputStream);
          this.this$0.handleMessages(localTCPConnection, false);
          break;
        case 75:
          localDataOutputStream.writeByte(78);
          if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE))
            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + i + ") " + "suggesting " + this.remoteHost + ":" + l);
          localDataOutputStream.writeUTF(this.remoteHost);
          localDataOutputStream.writeInt(l);
          localDataOutputStream.flush();
          String str = localDataInputStream.readUTF();
          int i2 = localDataInputStream.readInt();
          if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE))
            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + i + ") client using " + str + ":" + i2);
          localTCPEndpoint2 = new TCPEndpoint(this.remoteHost, this.socket.getLocalPort(), localTCPEndpoint1.getClientSocketFactory(), localTCPEndpoint1.getServerSocketFactory());
          localTCPChannel = new TCPChannel(this.this$0, localTCPEndpoint2);
          localTCPConnection = new TCPConnection(localTCPChannel, this.socket, localBufferedInputStream, localBufferedOutputStream);
          this.this$0.handleMessages(localTCPConnection, true);
          break;
        case 77:
          ConnectionMultiplexer localConnectionMultiplexer;
          if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE))
            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + i + ") accepting multiplex protocol");
          localDataOutputStream.writeByte(78);
          if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE))
            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + i + ") suggesting " + this.remoteHost + ":" + l);
          localDataOutputStream.writeUTF(this.remoteHost);
          localDataOutputStream.writeInt(l);
          localDataOutputStream.flush();
          localTCPEndpoint2 = new TCPEndpoint(localDataInputStream.readUTF(), localDataInputStream.readInt(), localTCPEndpoint1.getClientSocketFactory(), localTCPEndpoint1.getServerSocketFactory());
          if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE))
            TCPTransport.tcpLog.log(Log.VERBOSE, "(port " + i + ") client using " + localTCPEndpoint2.getHost() + ":" + localTCPEndpoint2.getPort());
          synchronized (TCPTransport.access$600(this.this$0))
          {
            localTCPChannel = this.this$0.getChannel(localTCPEndpoint2);
            localConnectionMultiplexer = new ConnectionMultiplexer(localTCPChannel, localBufferedInputStream, localOutputStream, false);
            localTCPChannel.useMultiplexer(localConnectionMultiplexer);
          }
          localConnectionMultiplexer.run();
          break;
        default:
          localDataOutputStream.writeByte(79);
          localDataOutputStream.flush();
        }
      }
      catch (IOException localIOException1)
      {
        TCPTransport.tcpLog.log(Log.BRIEF, "terminated with exception:", localIOException1);
      }
      finally
      {
        TCPTransport.access$200(this.socket);
      }
    }
  }
}