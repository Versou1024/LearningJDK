package sun.rmi.transport.proxy;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.AccessController;
import sun.rmi.runtime.Log;
import sun.security.action.GetPropertyAction;

class HttpSendSocket extends Socket
  implements RMISocketInfo
{
  protected String host;
  protected int port;
  protected URL url;
  protected URLConnection conn;
  protected InputStream in;
  protected OutputStream out;
  protected HttpSendInputStream inNotifier;
  protected HttpSendOutputStream outNotifier;
  private String lineSeparator;

  public HttpSendSocket(String paramString, int paramInt, URL paramURL)
    throws IOException
  {
    super((SocketImpl)null);
    this.conn = null;
    this.in = null;
    this.out = null;
    this.lineSeparator = ((String)AccessController.doPrivileged(new GetPropertyAction("line.separator")));
    if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE))
      RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE, "host = " + paramString + ", port = " + paramInt + ", url = " + paramURL);
    this.host = paramString;
    this.port = paramInt;
    this.url = paramURL;
    this.inNotifier = new HttpSendInputStream(null, this);
    this.outNotifier = new HttpSendOutputStream(writeNotify(), this);
  }

  public HttpSendSocket(String paramString, int paramInt)
    throws IOException
  {
    this(paramString, paramInt, new URL("http", paramString, paramInt, "/"));
  }

  public HttpSendSocket(InetAddress paramInetAddress, int paramInt)
    throws IOException
  {
    this(paramInetAddress.getHostName(), paramInt);
  }

  public boolean isReusable()
  {
    return false;
  }

  public synchronized OutputStream writeNotify()
    throws IOException
  {
    if (this.conn != null)
      throw new IOException("attempt to write on HttpSendSocket after request has been sent");
    this.conn = this.url.openConnection();
    this.conn.setDoOutput(true);
    this.conn.setUseCaches(false);
    this.conn.setRequestProperty("Content-type", "application/octet-stream");
    this.inNotifier.deactivate();
    this.in = null;
    return (this.out = this.conn.getOutputStream());
  }

  public synchronized InputStream readNotify()
    throws IOException
  {
    RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE, "sending request and activating input stream");
    this.outNotifier.deactivate();
    this.out.close();
    this.out = null;
    try
    {
      this.in = this.conn.getInputStream();
    }
    catch (IOException localIOException1)
    {
      RMIMasterSocketFactory.proxyLog.log(Log.BRIEF, "failed to get input stream, exception: ", localIOException1);
      throw new IOException("HTTP request failed");
    }
    String str1 = this.conn.getContentType();
    if ((str1 == null) || (!(this.conn.getContentType().equals("application/octet-stream"))))
    {
      if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.BRIEF))
      {
        if (str1 == null)
          str2 = "missing content type in response" + this.lineSeparator;
        else
          str2 = "invalid content type in response: " + str1 + this.lineSeparator;
        String str2 = str2 + "HttpSendSocket.readNotify: response body: ";
        try
        {
          DataInputStream localDataInputStream = new DataInputStream(this.in);
          while ((str3 = localDataInputStream.readLine()) != null)
          {
            String str3;
            str2 = str2 + str3 + this.lineSeparator;
          }
        }
        catch (IOException localIOException2)
        {
        }
        RMIMasterSocketFactory.proxyLog.log(Log.BRIEF, str2);
      }
      throw new IOException("HTTP request failed");
    }
    return this.in;
  }

  public InetAddress getInetAddress()
  {
    try
    {
      return InetAddress.getByName(this.host);
    }
    catch (UnknownHostException localUnknownHostException)
    {
    }
    return null;
  }

  public InetAddress getLocalAddress()
  {
    try
    {
      return InetAddress.getLocalHost();
    }
    catch (UnknownHostException localUnknownHostException)
    {
    }
    return null;
  }

  public int getPort()
  {
    return this.port;
  }

  public int getLocalPort()
  {
    return -1;
  }

  public InputStream getInputStream()
    throws IOException
  {
    return this.inNotifier;
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    return this.outNotifier;
  }

  public void setTcpNoDelay(boolean paramBoolean)
    throws SocketException
  {
  }

  public boolean getTcpNoDelay()
    throws SocketException
  {
    return false;
  }

  public void setSoLinger(boolean paramBoolean, int paramInt)
    throws SocketException
  {
  }

  public int getSoLinger()
    throws SocketException
  {
    return -1;
  }

  public synchronized void setSoTimeout(int paramInt)
    throws SocketException
  {
  }

  public synchronized int getSoTimeout()
    throws SocketException
  {
    return 0;
  }

  public synchronized void close()
    throws IOException
  {
    if (this.out != null)
      this.out.close();
  }

  public String toString()
  {
    return "HttpSendSocket[host=" + this.host + ",port=" + this.port + ",url=" + this.url + "]";
  }
}