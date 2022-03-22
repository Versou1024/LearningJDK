package sun.rmi.transport.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.rmi.ConnectIOException;
import java.rmi.RemoteException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import sun.rmi.runtime.Log;
import sun.rmi.runtime.RuntimeUtil;
import sun.rmi.runtime.RuntimeUtil.GetInstanceAction;
import sun.rmi.transport.Channel;
import sun.rmi.transport.Connection;
import sun.rmi.transport.Endpoint;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetLongAction;

public class TCPChannel
  implements Channel
{
  private final TCPEndpoint ep;
  private final TCPTransport tr;
  private final List<TCPConnection> freeList = new ArrayList();
  private Future<?> reaper = null;
  private boolean usingMultiplexer = false;
  private ConnectionMultiplexer multiplexer = null;
  private ConnectionAcceptor acceptor;
  private AccessControlContext okContext;
  private WeakHashMap<AccessControlContext, Reference<AccessControlContext>> authcache;
  private SecurityManager cacheSecurityManager = null;
  private static final long idleTimeout = ((Long)AccessController.doPrivileged(new GetLongAction("sun.rmi.transport.connectionTimeout", 15000L))).longValue();
  private static final int handshakeTimeout = ((Integer)AccessController.doPrivileged(new GetIntegerAction("sun.rmi.transport.tcp.handshakeTimeout", 60000))).intValue();
  private static final int responseTimeout = ((Integer)AccessController.doPrivileged(new GetIntegerAction("sun.rmi.transport.tcp.responseTimeout", 0))).intValue();
  private static final ScheduledExecutorService scheduler = ((RuntimeUtil)AccessController.doPrivileged(new RuntimeUtil.GetInstanceAction())).getScheduler();

  TCPChannel(TCPTransport paramTCPTransport, TCPEndpoint paramTCPEndpoint)
  {
    this.tr = paramTCPTransport;
    this.ep = paramTCPEndpoint;
  }

  public Endpoint getEndpoint()
  {
    return this.ep;
  }

  private void checkConnectPermission()
    throws SecurityException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager == null)
      return;
    if (localSecurityManager != this.cacheSecurityManager)
    {
      this.okContext = null;
      this.authcache = new WeakHashMap();
      this.cacheSecurityManager = localSecurityManager;
    }
    AccessControlContext localAccessControlContext = AccessController.getContext();
    if ((this.okContext == null) || ((!(this.okContext.equals(localAccessControlContext))) && (!(this.authcache.containsKey(localAccessControlContext)))))
    {
      localSecurityManager.checkConnect(this.ep.getHost(), this.ep.getPort());
      this.authcache.put(localAccessControlContext, new SoftReference(localAccessControlContext));
    }
    this.okContext = localAccessControlContext;
  }

  public Connection newConnection()
    throws RemoteException
  {
    TCPConnection localTCPConnection;
    do
    {
      localTCPConnection = null;
      synchronized (this.freeList)
      {
        int i = this.freeList.size() - 1;
        if (i >= 0)
        {
          checkConnectPermission();
          localTCPConnection = (TCPConnection)this.freeList.get(i);
          this.freeList.remove(i);
        }
      }
      if (localTCPConnection != null)
      {
        if (!(localTCPConnection.isDead()))
        {
          TCPTransport.tcpLog.log(Log.BRIEF, "reuse connection");
          return localTCPConnection;
        }
        free(localTCPConnection, false);
      }
    }
    while (localTCPConnection != null);
    return createConnection();
  }

  private Connection createConnection()
    throws RemoteException
  {
    TCPConnection localTCPConnection;
    TCPTransport.tcpLog.log(Log.BRIEF, "create connection");
    if (!(this.usingMultiplexer))
    {
      Socket localSocket = this.ep.newSocket();
      localTCPConnection = new TCPConnection(this, localSocket);
      try
      {
        DataOutputStream localDataOutputStream = new DataOutputStream(localTCPConnection.getOutputStream());
        writeTransportHeader(localDataOutputStream);
        if (!(localTCPConnection.isReusable()))
        {
          localDataOutputStream.writeByte(76);
        }
        else
        {
          localDataOutputStream.writeByte(75);
          localDataOutputStream.flush();
          int i = 0;
          try
          {
            i = localSocket.getSoTimeout();
            localSocket.setSoTimeout(handshakeTimeout);
          }
          catch (Exception localException1)
          {
          }
          DataInputStream localDataInputStream = new DataInputStream(localTCPConnection.getInputStream());
          int j = localDataInputStream.readByte();
          if (j != 78)
            throw new ConnectIOException("non-JRMP server at remote endpoint");
          String str = localDataInputStream.readUTF();
          int k = localDataInputStream.readInt();
          if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE))
            TCPTransport.tcpLog.log(Log.VERBOSE, "server suggested " + str + ":" + k);
          TCPEndpoint.setLocalHost(str);
          TCPEndpoint localTCPEndpoint = TCPEndpoint.getLocalEndpoint(0, null, null);
          localDataOutputStream.writeUTF(localTCPEndpoint.getHost());
          localDataOutputStream.writeInt(localTCPEndpoint.getPort());
          if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE))
            TCPTransport.tcpLog.log(Log.VERBOSE, "using " + localTCPEndpoint.getHost() + ":" + localTCPEndpoint.getPort());
          try
          {
            localSocket.setSoTimeout((i != 0) ? i : responseTimeout);
          }
          catch (Exception localException2)
          {
          }
          localDataOutputStream.flush();
        }
      }
      catch (IOException localIOException2)
      {
        if (localIOException2 instanceof RemoteException)
          throw ((RemoteException)localIOException2);
        throw new ConnectIOException("error during JRMP connection establishment", localIOException2);
      }
    }
    else
    {
      try
      {
        localTCPConnection = this.multiplexer.openConnection();
      }
      catch (IOException localIOException1)
      {
        synchronized (this)
        {
          this.usingMultiplexer = false;
          this.multiplexer = null;
        }
        throw new ConnectIOException("error opening virtual connection over multiplexed connection", localIOException1);
      }
    }
    return localTCPConnection;
  }

  public void free(Connection paramConnection, boolean paramBoolean)
  {
    if (paramConnection == null)
      return;
    if ((paramBoolean) && (paramConnection.isReusable()))
    {
      long l = System.currentTimeMillis();
      TCPConnection localTCPConnection = (TCPConnection)paramConnection;
      TCPTransport.tcpLog.log(Log.BRIEF, "reuse connection");
      synchronized (this.freeList)
      {
        this.freeList.add(localTCPConnection);
        if (this.reaper == null)
        {
          TCPTransport.tcpLog.log(Log.BRIEF, "create reaper");
          this.reaper = scheduler.scheduleWithFixedDelay(new java.lang.Runnable(this)
          {
            public void run()
            {
              TCPTransport.tcpLog.log(Log.VERBOSE, "wake up");
              TCPChannel.access$000(this.this$0);
            }
          }
          , idleTimeout, idleTimeout, TimeUnit.MILLISECONDS);
        }
      }
      localTCPConnection.setLastUseTime(l);
      localTCPConnection.setExpiration(l + idleTimeout);
    }
    else
    {
      TCPTransport.tcpLog.log(Log.BRIEF, "close connection");
      try
      {
        paramConnection.close();
      }
      catch (IOException localIOException)
      {
      }
    }
  }

  private void writeTransportHeader(DataOutputStream paramDataOutputStream)
    throws RemoteException
  {
    DataOutputStream localDataOutputStream;
    try
    {
      localDataOutputStream = new DataOutputStream(paramDataOutputStream);
      localDataOutputStream.writeInt(1246907721);
      localDataOutputStream.writeShort(2);
    }
    catch (IOException localIOException)
    {
      throw new ConnectIOException("error writing JRMP transport header", localIOException);
    }
  }

  synchronized void useMultiplexer(ConnectionMultiplexer paramConnectionMultiplexer)
  {
    this.multiplexer = paramConnectionMultiplexer;
    this.usingMultiplexer = true;
  }

  void acceptMultiplexConnection(Connection paramConnection)
  {
    if (this.acceptor == null)
    {
      this.acceptor = new ConnectionAcceptor(this.tr);
      this.acceptor.startNewAcceptor();
    }
    this.acceptor.accept(paramConnection);
  }

  public void shedCache()
  {
    Connection[] arrayOfConnection;
    synchronized (this.freeList)
    {
      arrayOfConnection = (Connection[])this.freeList.toArray(new Connection[this.freeList.size()]);
      this.freeList.clear();
    }
    int i = arrayOfConnection.length;
    while (--i >= 0)
    {
      Connection localConnection = arrayOfConnection[i];
      arrayOfConnection[i] = null;
      try
      {
        localConnection.close();
      }
      catch (IOException localIOException)
      {
      }
    }
  }

  private void freeCachedConnections()
  {
    synchronized (this.freeList)
    {
      int i = this.freeList.size();
      if (i > 0)
      {
        long l = System.currentTimeMillis();
        ListIterator localListIterator = this.freeList.listIterator(i);
        while (localListIterator.hasPrevious())
        {
          TCPConnection localTCPConnection = (TCPConnection)localListIterator.previous();
          if (localTCPConnection.expired(l))
          {
            TCPTransport.tcpLog.log(Log.VERBOSE, "connection timeout expired");
            try
            {
              localTCPConnection.close();
            }
            catch (IOException localIOException)
            {
            }
            localListIterator.remove();
          }
        }
      }
      if (this.freeList.isEmpty())
      {
        this.reaper.cancel(false);
        this.reaper = null;
      }
    }
  }
}