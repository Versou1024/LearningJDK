package sun.rmi.transport.proxy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import sun.rmi.runtime.Log;

class HttpAwareServerSocket extends ServerSocket
{
  public HttpAwareServerSocket(int paramInt)
    throws IOException
  {
    super(paramInt);
  }

  public HttpAwareServerSocket(int paramInt1, int paramInt2)
    throws IOException
  {
    super(paramInt1, paramInt2);
  }

  public Socket accept()
    throws IOException
  {
    Socket localSocket = super.accept();
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(localSocket.getInputStream());
    RMIMasterSocketFactory.proxyLog.log(Log.BRIEF, "socket accepted (checking for POST)");
    localBufferedInputStream.mark(4);
    int i = ((localBufferedInputStream.read() == 80) && (localBufferedInputStream.read() == 79) && (localBufferedInputStream.read() == 83) && (localBufferedInputStream.read() == 84)) ? 1 : 0;
    localBufferedInputStream.reset();
    if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.BRIEF))
      RMIMasterSocketFactory.proxyLog.log(Log.BRIEF, (i != 0) ? "POST found, HTTP socket returned" : "POST not found, direct socket returned");
    if (i != 0)
      return new HttpReceiveSocket(localSocket, localBufferedInputStream, null);
    return new WrappedSocket(localSocket, localBufferedInputStream, null);
  }

  public String toString()
  {
    return "HttpAware" + super.toString();
  }
}