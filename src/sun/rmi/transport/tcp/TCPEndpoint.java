package sun.rmi.transport.tcp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ConnectIOException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.security.AccessController;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import sun.rmi.runtime.Log;
import sun.rmi.runtime.NewThreadAction;
import sun.rmi.transport.Channel;
import sun.rmi.transport.Endpoint;
import sun.rmi.transport.Target;
import sun.rmi.transport.Transport;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetPropertyAction;

public class TCPEndpoint
  implements Endpoint
{
  private String host;
  private int port;
  private final RMIClientSocketFactory csf;
  private final RMIServerSocketFactory ssf;
  private int listenPort;
  private TCPTransport transport;
  private static String localHost;
  private static boolean localHostKnown = true;
  private static final Map<TCPEndpoint, LinkedList<TCPEndpoint>> localEndpoints;
  private static final int FORMAT_HOST_PORT = 0;
  private static final int FORMAT_HOST_PORT_FACTORY = 1;

  private static int getInt(String paramString, int paramInt)
  {
    return ((Integer)AccessController.doPrivileged(new GetIntegerAction(paramString, paramInt))).intValue();
  }

  private static boolean getBoolean(String paramString)
  {
    return ((Boolean)AccessController.doPrivileged(new GetBooleanAction(paramString))).booleanValue();
  }

  private static String getHostnameProperty()
  {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("java.rmi.server.hostname")));
  }

  public TCPEndpoint(String paramString, int paramInt)
  {
    this(paramString, paramInt, null, null);
  }

  public TCPEndpoint(String paramString, int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory)
  {
    this.listenPort = -1;
    this.transport = null;
    if (paramString == null)
      paramString = "";
    this.host = paramString;
    this.port = paramInt;
    this.csf = paramRMIClientSocketFactory;
    this.ssf = paramRMIServerSocketFactory;
  }

  public static TCPEndpoint getLocalEndpoint(int paramInt)
  {
    return getLocalEndpoint(paramInt, null, null);
  }

  public static TCPEndpoint getLocalEndpoint(int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory)
  {
    TCPEndpoint localTCPEndpoint1 = null;
    synchronized (localEndpoints)
    {
      TCPEndpoint localTCPEndpoint2 = new TCPEndpoint(null, paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
      LinkedList localLinkedList1 = (LinkedList)localEndpoints.get(localTCPEndpoint2);
      String str1 = resampleLocalHost();
      if (localLinkedList1 == null)
      {
        localTCPEndpoint1 = new TCPEndpoint(str1, paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
        localLinkedList1 = new LinkedList();
        localLinkedList1.add(localTCPEndpoint1);
        localTCPEndpoint1.listenPort = paramInt;
        localTCPEndpoint1.transport = new TCPTransport(localLinkedList1);
        localEndpoints.put(localTCPEndpoint2, localLinkedList1);
        if (TCPTransport.tcpLog.isLoggable(Log.BRIEF))
          TCPTransport.tcpLog.log(Log.BRIEF, "created local endpoint for socket factory " + paramRMIServerSocketFactory + " on port " + paramInt);
      }
      else
      {
        synchronized (localLinkedList1)
        {
          localTCPEndpoint1 = (TCPEndpoint)localLinkedList1.getLast();
          String str2 = localTCPEndpoint1.host;
          int i = localTCPEndpoint1.port;
          TCPTransport localTCPTransport = localTCPEndpoint1.transport;
          if ((str1 != null) && (!(str1.equals(str2))))
          {
            if (i != 0)
              localLinkedList1.clear();
            localTCPEndpoint1 = new TCPEndpoint(str1, i, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
            localTCPEndpoint1.listenPort = paramInt;
            localTCPEndpoint1.transport = localTCPTransport;
            localLinkedList1.add(localTCPEndpoint1);
          }
        }
      }
    }
    return localTCPEndpoint1;
  }

  private static String resampleLocalHost()
  {
    String str = getHostnameProperty();
    synchronized (localEndpoints)
    {
      if (str != null)
        if (!(localHostKnown))
        {
          setLocalHost(str);
        }
        else if (!(str.equals(localHost)))
        {
          localHost = str;
          if (TCPTransport.tcpLog.isLoggable(Log.BRIEF))
            TCPTransport.tcpLog.log(Log.BRIEF, "updated local hostname to: " + localHost);
        }
      return localHost;
    }
  }

  static void setLocalHost(String paramString)
  {
    synchronized (localEndpoints)
    {
      if (!(localHostKnown))
      {
        localHost = paramString;
        localHostKnown = true;
        if (TCPTransport.tcpLog.isLoggable(Log.BRIEF))
          TCPTransport.tcpLog.log(Log.BRIEF, "local host set to " + paramString);
        Iterator localIterator1 = localEndpoints.values().iterator();
        while (localIterator1.hasNext())
        {
          LinkedList localLinkedList1 = (LinkedList)localIterator1.next();
          synchronized (localLinkedList1)
          {
            Iterator localIterator2 = localLinkedList1.iterator();
            while (localIterator2.hasNext())
            {
              TCPEndpoint localTCPEndpoint = (TCPEndpoint)localIterator2.next();
              localTCPEndpoint.host = paramString;
            }
          }
        }
      }
    }
  }

  static void setDefaultPort(int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory)
  {
    TCPEndpoint localTCPEndpoint1 = new TCPEndpoint(null, 0, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
    synchronized (localEndpoints)
    {
      LinkedList localLinkedList = (LinkedList)localEndpoints.get(localTCPEndpoint1);
      synchronized (localLinkedList)
      {
        int i = localLinkedList.size();
        TCPEndpoint localTCPEndpoint2 = (TCPEndpoint)localLinkedList.getLast();
        Iterator localIterator = localLinkedList.iterator();
        while (localIterator.hasNext())
        {
          TCPEndpoint localTCPEndpoint3 = (TCPEndpoint)localIterator.next();
          localTCPEndpoint3.port = paramInt;
        }
        if (i > 1)
        {
          localLinkedList.clear();
          localLinkedList.add(localTCPEndpoint2);
        }
      }
      ??? = new TCPEndpoint(null, paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
      localEndpoints.put(???, localLinkedList);
      if (TCPTransport.tcpLog.isLoggable(Log.BRIEF))
        TCPTransport.tcpLog.log(Log.BRIEF, "default port for server socket factory " + paramRMIServerSocketFactory + " and client socket factory " + paramRMIClientSocketFactory + " set to " + paramInt);
    }
  }

  public Transport getOutboundTransport()
  {
    TCPEndpoint localTCPEndpoint = getLocalEndpoint(0, null, null);
    return localTCPEndpoint.transport;
  }

  private static Collection<TCPTransport> allKnownTransports()
  {
    HashSet localHashSet;
    synchronized (localEndpoints)
    {
      localHashSet = new HashSet(localEndpoints.size());
      Iterator localIterator = localEndpoints.values().iterator();
      while (localIterator.hasNext())
      {
        LinkedList localLinkedList = (LinkedList)localIterator.next();
        TCPEndpoint localTCPEndpoint = (TCPEndpoint)localLinkedList.getFirst();
        localHashSet.add(localTCPEndpoint.transport);
      }
    }
    return localHashSet;
  }

  public static void shedConnectionCaches()
  {
    Iterator localIterator = allKnownTransports().iterator();
    while (localIterator.hasNext())
    {
      TCPTransport localTCPTransport = (TCPTransport)localIterator.next();
      localTCPTransport.shedConnectionCaches();
    }
  }

  public void exportObject(Target paramTarget)
    throws RemoteException
  {
    this.transport.exportObject(paramTarget);
  }

  public Channel getChannel()
  {
    return getOutboundTransport().getChannel(this);
  }

  public String getHost()
  {
    return this.host;
  }

  public int getPort()
  {
    return this.port;
  }

  public int getListenPort()
  {
    return this.listenPort;
  }

  public Transport getInboundTransport()
  {
    return this.transport;
  }

  public RMIClientSocketFactory getClientSocketFactory()
  {
    return this.csf;
  }

  public RMIServerSocketFactory getServerSocketFactory()
  {
    return this.ssf;
  }

  public String toString()
  {
    return "[" + this.host + ":" + this.port + ((this.ssf != null) ? "," + this.ssf : "") + ((this.csf != null) ? "," + this.csf : "") + "]";
  }

  public int hashCode()
  {
    return this.port;
  }

  public boolean equals(Object paramObject)
  {
    if ((paramObject != null) && (paramObject instanceof TCPEndpoint))
    {
      TCPEndpoint localTCPEndpoint = (TCPEndpoint)paramObject;
      if ((this.port != localTCPEndpoint.port) || (!(this.host.equals(localTCPEndpoint.host))))
        return false;
      if ((((this.csf == null) ? 1 : 0) ^ ((localTCPEndpoint.csf == null) ? 1 : 0)) == 0)
        if ((((this.ssf == null) ? 1 : 0) ^ ((localTCPEndpoint.ssf == null) ? 1 : 0)) == 0)
          break label101;
      return false;
      if ((this.csf != null) && (((this.csf.getClass() != localTCPEndpoint.csf.getClass()) || (!(this.csf.equals(localTCPEndpoint.csf))))))
        label101: return false;
      return ((this.ssf == null) || ((this.ssf.getClass() == localTCPEndpoint.ssf.getClass()) && (this.ssf.equals(localTCPEndpoint.ssf))));
    }
    return false;
  }

  public void write(ObjectOutput paramObjectOutput)
    throws IOException
  {
    if (this.csf == null)
    {
      paramObjectOutput.writeByte(0);
      paramObjectOutput.writeUTF(this.host);
      paramObjectOutput.writeInt(this.port);
    }
    else
    {
      paramObjectOutput.writeByte(1);
      paramObjectOutput.writeUTF(this.host);
      paramObjectOutput.writeInt(this.port);
      paramObjectOutput.writeObject(this.csf);
    }
  }

  public static TCPEndpoint read(ObjectInput paramObjectInput)
    throws IOException, ClassNotFoundException
  {
    String str;
    int i;
    RMIClientSocketFactory localRMIClientSocketFactory = null;
    int j = paramObjectInput.readByte();
    switch (j)
    {
    case 0:
      str = paramObjectInput.readUTF();
      i = paramObjectInput.readInt();
      break;
    case 1:
      str = paramObjectInput.readUTF();
      i = paramObjectInput.readInt();
      localRMIClientSocketFactory = (RMIClientSocketFactory)paramObjectInput.readObject();
      break;
    default:
      throw new IOException("invalid endpoint format");
    }
    return new TCPEndpoint(str, i, localRMIClientSocketFactory, null);
  }

  public void writeHostPortFormat(DataOutput paramDataOutput)
    throws IOException
  {
    if (this.csf != null)
      throw new InternalError("TCPEndpoint.writeHostPortFormat: called for endpoint with non-null socket factory");
    paramDataOutput.writeUTF(this.host);
    paramDataOutput.writeInt(this.port);
  }

  public static TCPEndpoint readHostPortFormat(DataInput paramDataInput)
    throws IOException
  {
    String str = paramDataInput.readUTF();
    int i = paramDataInput.readInt();
    return new TCPEndpoint(str, i);
  }

  private static RMISocketFactory chooseFactory()
  {
    RMISocketFactory localRMISocketFactory = RMISocketFactory.getSocketFactory();
    if (localRMISocketFactory == null)
      localRMISocketFactory = TCPTransport.defaultSocketFactory;
    return localRMISocketFactory;
  }

  Socket newSocket()
    throws RemoteException
  {
    Socket localSocket;
    if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE))
      TCPTransport.tcpLog.log(Log.VERBOSE, "opening socket to " + this);
    try
    {
      Object localObject = this.csf;
      if (localObject == null)
        localObject = chooseFactory();
      localSocket = ((RMIClientSocketFactory)localObject).createSocket(this.host, this.port);
    }
    catch (java.net.UnknownHostException localUnknownHostException)
    {
      throw new java.rmi.UnknownHostException("Unknown host: " + this.host, localUnknownHostException);
    }
    catch (java.net.ConnectException localConnectException)
    {
      throw new java.rmi.ConnectException("Connection refused to host: " + this.host, localConnectException);
    }
    catch (IOException localIOException)
    {
      try
      {
        shedConnectionCaches();
      }
      catch (OutOfMemoryError localOutOfMemoryError)
      {
      }
      catch (Exception localException3)
      {
      }
      throw new ConnectIOException("Exception creating connection to: " + this.host, localIOException);
    }
    try
    {
      localSocket.setTcpNoDelay(true);
    }
    catch (Exception localException1)
    {
    }
    try
    {
      localSocket.setKeepAlive(true);
    }
    catch (Exception localException2)
    {
    }
    return ((Socket)localSocket);
  }

  ServerSocket newServerSocket()
    throws IOException
  {
    if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE))
      TCPTransport.tcpLog.log(Log.VERBOSE, "creating server socket on " + this);
    Object localObject = this.ssf;
    if (localObject == null)
      localObject = chooseFactory();
    ServerSocket localServerSocket = ((RMIServerSocketFactory)localObject).createServerSocket(this.listenPort);
    if (this.listenPort == 0)
      setDefaultPort(localServerSocket.getLocalPort(), this.csf, this.ssf);
    return ((ServerSocket)localServerSocket);
  }

  static
  {
    localHost = getHostnameProperty();
    if (localHost == null)
      try
      {
        InetAddress localInetAddress = InetAddress.getLocalHost();
        byte[] arrayOfByte = localInetAddress.getAddress();
        if ((arrayOfByte[0] == 127) && (arrayOfByte[1] == 0) && (arrayOfByte[2] == 0) && (arrayOfByte[3] == 1))
          localHostKnown = false;
        if (getBoolean("java.rmi.server.useLocalHostName"))
          localHost = FQDN.attemptFQDN(localInetAddress);
        else
          localHost = localInetAddress.getHostAddress();
      }
      catch (Exception localException)
      {
        localHostKnown = false;
        localHost = null;
      }
    if (TCPTransport.tcpLog.isLoggable(Log.BRIEF))
      TCPTransport.tcpLog.log(Log.BRIEF, "localHostKnown = " + localHostKnown + ", localHost = " + localHost);
    localEndpoints = new HashMap();
  }

  private static class FQDN
  implements Runnable
  {
    private String reverseLookup;
    private String hostAddress;

    private FQDN(String paramString)
    {
      this.hostAddress = paramString;
    }

    static String attemptFQDN(InetAddress paramInetAddress)
      throws java.net.UnknownHostException
    {
      Object localObject1 = paramInetAddress.getHostName();
      if (((String)localObject1).indexOf(46) < 0)
      {
        String str = paramInetAddress.getHostAddress();
        FQDN localFQDN1 = new FQDN(str);
        int i = TCPEndpoint.access$000("sun.rmi.transport.tcp.localHostNameTimeOut", 10000);
        try
        {
          synchronized (localFQDN1)
          {
            localFQDN1.getFQDN();
            localFQDN1.wait(i);
          }
        }
        catch (InterruptedException localInterruptedException)
        {
          Thread.currentThread().interrupt();
        }
        localObject1 = localFQDN1.getHost();
        if ((localObject1 == null) || (((String)localObject1).equals("")) || (((String)localObject1).indexOf(46) < 0))
          localObject1 = str;
      }
      return ((String)localObject1);
    }

    private void getFQDN()
    {
      Thread localThread = (Thread)AccessController.doPrivileged(new NewThreadAction(this, "FQDN Finder", true));
      localThread.start();
    }

    private synchronized String getHost()
    {
      return this.reverseLookup;
    }

    public void run()
    {
      String str = null;
      try
      {
        str = InetAddress.getByName(this.hostAddress).getHostName();
      }
      catch (java.net.UnknownHostException localUnknownHostException)
      {
      }
      finally
      {
        synchronized (this)
        {
          this.reverseLookup = str;
          super.notify();
        }
      }
    }
  }
}