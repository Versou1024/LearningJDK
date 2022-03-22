package sun.management.jmxremote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Enumeration;

public final class LocalRMIServerSocketFactory
  implements RMIServerSocketFactory
{
  public ServerSocket createServerSocket(int paramInt)
    throws IOException
  {
    return new ServerSocket(this, paramInt)
    {
      public Socket accept()
        throws IOException
      {
        Enumeration localEnumeration1;
        Socket localSocket = super.accept();
        InetAddress localInetAddress1 = localSocket.getInetAddress();
        try
        {
          localEnumeration1 = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException localSocketException)
        {
          try
          {
            localSocket.close();
          }
          catch (IOException localIOException2)
          {
          }
          throw new IOException("The server sockets created using the LocalRMIServerSocketFactory only accept connections from clients running on the host where the RMI remote objects have been exported.", localSocketException);
        }
        while (localEnumeration1.hasMoreElements())
        {
          NetworkInterface localNetworkInterface = (NetworkInterface)localEnumeration1.nextElement();
          Enumeration localEnumeration2 = localNetworkInterface.getInetAddresses();
          while (localEnumeration2.hasMoreElements())
          {
            InetAddress localInetAddress2 = (InetAddress)localEnumeration2.nextElement();
            if (localInetAddress2.equals(localInetAddress1))
              return localSocket;
          }
        }
        try
        {
          localSocket.close();
        }
        catch (IOException localIOException1)
        {
        }
        throw new IOException("The server sockets created using the LocalRMIServerSocketFactory only accept connections from clients running on the host where the RMI remote objects have been exported.");
      }
    };
  }

  public boolean equals(Object paramObject)
  {
    return paramObject instanceof LocalRMIServerSocketFactory;
  }

  public int hashCode()
  {
    return super.getClass().hashCode();
  }
}