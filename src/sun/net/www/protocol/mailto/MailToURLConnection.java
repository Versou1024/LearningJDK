package sun.net.www.protocol.mailto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.SocketPermission;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.Permission;
import sun.net.smtp.SmtpClient;
import sun.net.www.MessageHeader;
import sun.net.www.ParseUtil;
import sun.net.www.URLConnection;

public class MailToURLConnection extends URLConnection
{
  InputStream is = null;
  OutputStream os = null;
  SmtpClient client;
  Permission permission;
  private int connectTimeout = -1;
  private int readTimeout = -1;

  MailToURLConnection(URL paramURL)
  {
    super(paramURL);
    MessageHeader localMessageHeader = new MessageHeader();
    localMessageHeader.add("content-type", "text/html");
    setProperties(localMessageHeader);
  }

  String getFromAddress()
  {
    String str1 = System.getProperty("user.fromaddr");
    if (str1 == null)
    {
      str1 = System.getProperty("user.name");
      if (str1 != null)
      {
        String str2 = System.getProperty("mail.host");
        if (str2 == null)
          try
          {
            str2 = InetAddress.getLocalHost().getHostName();
          }
          catch (UnknownHostException localUnknownHostException)
          {
          }
        str1 = str1 + "@" + str2;
      }
      else
      {
        str1 = "";
      }
    }
    return str1;
  }

  public void connect()
    throws IOException
  {
    System.err.println("connect. Timeout = " + this.connectTimeout);
    this.client = new SmtpClient(this.connectTimeout);
    this.client.setReadTimeout(this.readTimeout);
  }

  public synchronized OutputStream getOutputStream()
    throws IOException
  {
    if (this.os != null)
      return this.os;
    if (this.is != null)
      throw new IOException("Cannot write output after reading input.");
    connect();
    String str = ParseUtil.decode(this.url.getPath());
    this.client.from(getFromAddress());
    this.client.to(str);
    this.os = this.client.startMessage();
    return this.os;
  }

  public Permission getPermission()
    throws IOException
  {
    if (this.permission == null)
    {
      connect();
      String str = this.client.getMailHost() + ":" + 25;
      this.permission = new SocketPermission(str, "connect");
    }
    return this.permission;
  }

  public void setConnectTimeout(int paramInt)
  {
    if (paramInt < 0)
      throw new IllegalArgumentException("timeouts can't be negative");
    this.connectTimeout = paramInt;
  }

  public int getConnectTimeout()
  {
    return ((this.connectTimeout < 0) ? 0 : this.connectTimeout);
  }

  public void setReadTimeout(int paramInt)
  {
    if (paramInt < 0)
      throw new IllegalArgumentException("timeouts can't be negative");
    this.readTimeout = paramInt;
  }

  public int getReadTimeout()
  {
    return ((this.readTimeout < 0) ? 0 : this.readTimeout);
  }
}