package sun.management.jmxremote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class SSLContextRMIServerSocketFactory extends SslRMIServerSocketFactory
{
  private SSLContext context;

  public SSLContextRMIServerSocketFactory(SSLContext paramSSLContext)
  {
    this(paramSSLContext, null, null, false);
  }

  public SSLContextRMIServerSocketFactory(SSLContext paramSSLContext, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean)
    throws IllegalArgumentException
  {
    super(paramArrayOfString1, paramArrayOfString2, paramBoolean);
    this.context = paramSSLContext;
  }

  public ServerSocket createServerSocket(int paramInt)
    throws IOException
  {
    if (this.context == null)
      return super.createServerSocket(paramInt);
    SSLSocketFactory localSSLSocketFactory = this.context.getSocketFactory();
    return new ServerSocket(this, paramInt, localSSLSocketFactory)
    {
      public Socket accept()
        throws IOException
      {
        Socket localSocket = super.accept();
        SSLSocket localSSLSocket = (SSLSocket)this.val$sslSocketFactory.createSocket(localSocket, localSocket.getInetAddress().getHostName(), localSocket.getPort(), true);
        localSSLSocket.setUseClientMode(false);
        if (this.this$0.getEnabledCipherSuites() != null)
          localSSLSocket.setEnabledCipherSuites(this.this$0.getEnabledCipherSuites());
        if (this.this$0.getEnabledProtocols() != null)
          localSSLSocket.setEnabledProtocols(this.this$0.getEnabledProtocols());
        localSSLSocket.setNeedClientAuth(this.this$0.getNeedClientAuth());
        return localSSLSocket;
      }
    };
  }

  public boolean equals(Object paramObject)
  {
    if (!(super.equals(paramObject)))
      return false;
    SSLContextRMIServerSocketFactory localSSLContextRMIServerSocketFactory = (SSLContextRMIServerSocketFactory)paramObject;
    return ((this.context == null) ? false : (localSSLContextRMIServerSocketFactory.context == null) ? true : this.context.equals(localSSLContextRMIServerSocketFactory.context));
  }

  public int hashCode()
  {
    return (super.hashCode() + ((this.context == null) ? 0 : this.context.hashCode()));
  }
}