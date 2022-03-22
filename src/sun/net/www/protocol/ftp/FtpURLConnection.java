package sun.net.www.protocol.ftp;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import sun.net.ProgressMonitor;
import sun.net.ProgressSource;
import sun.net.ftp.FtpClient;
import sun.net.www.MessageHeader;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;
import sun.net.www.URLConnection;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.security.action.GetPropertyAction;

public class FtpURLConnection extends URLConnection
{
  HttpURLConnection http;
  private Proxy instProxy;
  Proxy proxy;
  InputStream is;
  OutputStream os;
  FtpClient ftp;
  Permission permission;
  String password;
  String user;
  String host;
  String pathname;
  String filename;
  String fullpath;
  int port;
  static final int NONE = 0;
  static final int ASCII = 1;
  static final int BIN = 2;
  static final int DIR = 3;
  int type;
  private int connectTimeout;
  private int readTimeout;

  public FtpURLConnection(URL paramURL)
  {
    this(paramURL, null);
  }

  FtpURLConnection(URL paramURL, Proxy paramProxy)
  {
    super(paramURL);
    this.http = null;
    this.proxy = null;
    this.is = null;
    this.os = null;
    this.ftp = null;
    this.type = 0;
    this.connectTimeout = -1;
    this.readTimeout = -1;
    this.instProxy = paramProxy;
    this.host = paramURL.getHost();
    this.port = paramURL.getPort();
    String str = paramURL.getUserInfo();
    if (str != null)
    {
      int i = str.indexOf(58);
      if (i == -1)
      {
        this.user = ParseUtil.decode(str);
        this.password = null;
      }
      else
      {
        this.user = ParseUtil.decode(str.substring(0, i++));
        this.password = ParseUtil.decode(str.substring(i));
      }
    }
  }

  private void setTimeouts()
  {
    if (this.ftp != null)
    {
      if (this.connectTimeout >= 0)
        this.ftp.setConnectTimeout(this.connectTimeout);
      if (this.readTimeout >= 0)
        this.ftp.setReadTimeout(this.readTimeout);
    }
  }

  public synchronized void connect()
    throws IOException
  {
    Object localObject;
    URI localURI;
    InetSocketAddress localInetSocketAddress;
    if (this.connected)
      return;
    Proxy localProxy = null;
    if (this.instProxy == null)
    {
      localObject = (ProxySelector)AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          return ProxySelector.getDefault();
        }
      });
      if (localObject != null)
      {
        localURI = ParseUtil.toURI(this.url);
        Iterator localIterator = ((ProxySelector)localObject).select(localURI).iterator();
        while (true)
        {
          if (!(localIterator.hasNext()))
            break label235;
          localProxy = (Proxy)localIterator.next();
          if ((localProxy == null) || (localProxy == Proxy.NO_PROXY))
            break label235;
          if (localProxy.type() == Proxy.Type.SOCKS)
            break label235:
          if ((localProxy.type() == Proxy.Type.HTTP) && (localProxy.address() instanceof InetSocketAddress))
            break;
          ((ProxySelector)localObject).connectFailed(localURI, localProxy.address(), new IOException("Wrong proxy type"));
        }
        localInetSocketAddress = (InetSocketAddress)localProxy.address();
      }
    }
    try
    {
      this.http = new HttpURLConnection(this.url, localProxy);
      if (this.connectTimeout >= 0)
        this.http.setConnectTimeout(this.connectTimeout);
      if (this.readTimeout >= 0)
        this.http.setReadTimeout(this.readTimeout);
      this.http.connect();
      this.connected = true;
      label321: label235: return;
    }
    catch (IOException localIOException)
    {
      while (true)
      {
        ((ProxySelector)localObject).connectFailed(localURI, localInetSocketAddress, localIOException);
        this.http = null;
      }
      break label321:
      localProxy = this.instProxy;
      if (localProxy.type() == Proxy.Type.HTTP)
      {
        this.http = new HttpURLConnection(this.url, this.instProxy);
        if (this.connectTimeout >= 0)
          this.http.setConnectTimeout(this.connectTimeout);
        if (this.readTimeout >= 0)
          this.http.setReadTimeout(this.readTimeout);
        this.http.connect();
        this.connected = true;
        return;
      }
      if (this.user == null)
      {
        this.user = "anonymous";
        localObject = (String)AccessController.doPrivileged(new GetPropertyAction("java.version"));
        this.password = ((String)AccessController.doPrivileged(new GetPropertyAction("ftp.protocol.user", "Java" + ((String)localObject) + "@")));
      }
      try
      {
        if (localProxy != null)
          this.ftp = new FtpClient(localProxy);
        else
          this.ftp = new FtpClient();
        setTimeouts();
        if (this.port != -1)
          this.ftp.openServer(this.host, this.port);
        else
          this.ftp.openServer(this.host);
      }
      catch (UnknownHostException localUnknownHostException)
      {
        throw localUnknownHostException;
      }
      this.ftp.login(this.user, this.password);
      this.connected = true;
    }
  }

  private void decodePath(String paramString)
  {
    int i = paramString.indexOf(";type=");
    if (i >= 0)
    {
      String str = paramString.substring(i + 6, paramString.length());
      if ("i".equalsIgnoreCase(str))
        this.type = 2;
      if ("a".equalsIgnoreCase(str))
        this.type = 1;
      if ("d".equalsIgnoreCase(str))
        this.type = 3;
      paramString = paramString.substring(0, i);
    }
    if ((paramString != null) && (paramString.length() > 1) && (paramString.charAt(0) == '/'))
      paramString = paramString.substring(1);
    if ((paramString == null) || (paramString.length() == 0))
      paramString = "./";
    if (!(paramString.endsWith("/")))
    {
      i = paramString.lastIndexOf(47);
      if (i > 0)
      {
        this.filename = paramString.substring(i + 1, paramString.length());
        this.filename = ParseUtil.decode(this.filename);
        this.pathname = paramString.substring(0, i);
      }
      else
      {
        this.filename = ParseUtil.decode(paramString);
        this.pathname = null;
      }
    }
    else
    {
      this.pathname = paramString.substring(0, paramString.length() - 1);
      this.filename = null;
    }
    if (this.pathname != null)
      this.fullpath = this.pathname + "/" + ((this.filename != null) ? this.filename : "");
    else
      this.fullpath = this.filename;
  }

  private void cd(String paramString)
    throws IOException
  {
    if ((paramString == null) || ("".equals(paramString)))
      return;
    if (paramString.indexOf(47) == -1)
    {
      this.ftp.cd(ParseUtil.decode(paramString));
      return;
    }
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "/");
    while (localStringTokenizer.hasMoreTokens())
      this.ftp.cd(ParseUtil.decode(localStringTokenizer.nextToken()));
  }

  public InputStream getInputStream()
    throws IOException
  {
    if (!(this.connected))
      connect();
    if (this.http != null)
      return this.http.getInputStream();
    if (this.os != null)
      throw new IOException("Already opened for output");
    if (this.is != null)
      return this.is;
    MessageHeader localMessageHeader = new MessageHeader();
    try
    {
      decodePath(this.url.getPath());
      if ((this.filename == null) || (this.type == 3))
      {
        this.ftp.ascii();
        cd(this.pathname);
        if (this.filename == null)
          this.is = new FtpInputStream(this, this.ftp, this.ftp.list());
        else
          this.is = new FtpInputStream(this, this.ftp, this.ftp.nameList(this.filename));
      }
      else
      {
        if (this.type == 1)
          this.ftp.ascii();
        else
          this.ftp.binary();
        cd(this.pathname);
        this.is = new FtpInputStream(this, this.ftp, this.ftp.get(this.filename));
      }
      try
      {
        int i;
        String str1 = this.ftp.getResponseString();
        if ((i = str1.indexOf(" bytes)")) != -1)
        {
          int j = i;
          while (--j >= 0)
          {
            int k;
            if (((k = str1.charAt(j)) < '0') || (k > 57))
              break;
          }
          j = Integer.parseInt(str1.substring(j + 1, i));
          localMessageHeader.add("content-length", "" + j);
          if (j > 0)
          {
            boolean bool = ProgressMonitor.getDefault().shouldMeterInput(this.url, "GET");
            ProgressSource localProgressSource = null;
            if (bool)
            {
              localProgressSource = new ProgressSource(this.url, "GET", j);
              localProgressSource.beginTracking();
            }
            this.is = new MeteredStream(this.is, localProgressSource, j);
          }
        }
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
      String str2 = guessContentTypeFromName(this.fullpath);
      if ((str2 == null) && (this.is.markSupported()))
        str2 = guessContentTypeFromStream(this.is);
      if (str2 != null)
        localMessageHeader.add("content-type", str2);
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      try
      {
        cd(this.fullpath);
        this.ftp.ascii();
        this.is = new FtpInputStream(this, this.ftp, this.ftp.list());
        localMessageHeader.add("content-type", "text/plain");
      }
      catch (IOException localIOException)
      {
        throw new FileNotFoundException(this.fullpath);
      }
    }
    setProperties(localMessageHeader);
    return this.is;
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    if (!(this.connected))
      connect();
    if (this.http != null)
      return this.http.getOutputStream();
    if (this.is != null)
      throw new IOException("Already opened for input");
    if (this.os != null)
      return this.os;
    decodePath(this.url.getPath());
    if ((this.filename == null) || (this.filename.length() == 0))
      throw new IOException("illegal filename for a PUT");
    if (this.pathname != null)
      cd(this.pathname);
    if (this.type == 1)
      this.ftp.ascii();
    else
      this.ftp.binary();
    this.os = new FtpOutputStream(this, this.ftp, this.ftp.put(this.filename));
    return this.os;
  }

  String guessContentTypeFromFilename(String paramString)
  {
    return guessContentTypeFromName(paramString);
  }

  public Permission getPermission()
  {
    if (this.permission == null)
    {
      int i = this.url.getPort();
      i = (i < 0) ? 21 : i;
      String str = this.host + ":" + i;
      this.permission = new SocketPermission(str, "connect");
    }
    return this.permission;
  }

  public void setRequestProperty(String paramString1, String paramString2)
  {
    super.setRequestProperty(paramString1, paramString2);
    if ("type".equals(paramString1))
      if ("i".equalsIgnoreCase(paramString2))
        this.type = 2;
      else if ("a".equalsIgnoreCase(paramString2))
        this.type = 1;
      else if ("d".equalsIgnoreCase(paramString2))
        this.type = 3;
      else
        throw new IllegalArgumentException("Value of '" + paramString1 + "' request property was '" + paramString2 + "' when it must be either 'i', 'a' or 'd'");
  }

  public String getRequestProperty(String paramString)
  {
    String str = super.getRequestProperty(paramString);
    if ((str == null) && ("type".equals(paramString)))
      str = (this.type == 3) ? "d" : (this.type == 1) ? "a" : "i";
    return str;
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

  protected class FtpInputStream extends FilterInputStream
  {
    FtpClient ftp;

    FtpInputStream(, FtpClient paramFtpClient, InputStream paramInputStream)
    {
      super(new BufferedInputStream(paramInputStream));
      this.ftp = paramFtpClient;
    }

    public void close()
      throws IOException
    {
      super.close();
      try
      {
        if (this.ftp != null)
          this.ftp.closeServer();
      }
      catch (IOException localIOException)
      {
      }
    }
  }

  protected class FtpOutputStream extends FilterOutputStream
  {
    FtpClient ftp;

    FtpOutputStream(, FtpClient paramFtpClient, OutputStream paramOutputStream)
    {
      super(paramOutputStream);
      this.ftp = paramFtpClient;
    }

    public void close()
      throws IOException
    {
      super.close();
      try
      {
        if (this.ftp != null)
          this.ftp.closeServer();
      }
      catch (IOException localIOException)
      {
      }
    }
  }
}