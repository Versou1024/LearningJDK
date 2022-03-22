package sun.rmi.transport.proxy;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class HttpReceiveSocket extends WrappedSocket
  implements RMISocketInfo
{
  private boolean headerSent = false;

  public HttpReceiveSocket(Socket paramSocket, InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    super(paramSocket, paramInputStream, paramOutputStream);
    this.in = new HttpInputStream(paramSocket.getInputStream());
    this.out = ((paramOutputStream != null) ? paramOutputStream : paramSocket.getOutputStream());
  }

  public boolean isReusable()
  {
    return false;
  }

  public InetAddress getInetAddress()
  {
    return null;
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    if (!(this.headerSent))
    {
      DataOutputStream localDataOutputStream = new DataOutputStream(this.out);
      localDataOutputStream.writeBytes("HTTP/1.0 200 OK\r\n");
      localDataOutputStream.flush();
      this.headerSent = true;
      this.out = new HttpOutputStream(this.out);
    }
    return this.out;
  }

  public synchronized void close()
    throws IOException
  {
    getOutputStream().close();
    this.socket.close();
  }

  public String toString()
  {
    return "HttpReceive" + this.socket.toString();
  }
}