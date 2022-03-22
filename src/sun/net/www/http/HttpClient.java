package sun.net.www.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.CacheRequest;
import java.net.CookieHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import sun.misc.RegexpPool;
import sun.net.NetworkClient;
import sun.net.ProgressSource;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;
import sun.net.www.URLConnection;
import sun.net.www.protocol.http.HttpURLConnection;

public class HttpClient extends NetworkClient
{
  protected boolean cachedHttpClient;
  private boolean inCache;
  protected CookieHandler cookieHandler;
  MessageHeader requests;
  PosterOutputStream poster;
  boolean failedOnce;
  private static RegexpPool nonProxyHostsPool;
  private static String nonProxyHostsSource;
  private static final int HTTP_CONTINUE = 100;
  static final int httpPortNumber = 80;

  /**
   * @deprecated
   */
  protected boolean proxyDisabled;
  public boolean usingProxy;
  protected String host;
  protected int port;
  protected static KeepAliveCache kac;
  private static boolean keepAliveProp;
  private static boolean retryPostProp;
  volatile boolean keepingAlive;
  int keepAliveConnections;
  int keepAliveTimeout;
  private CacheRequest cacheRequest;
  protected URL url;
  public boolean reuse;

  protected int getDefaultPort()
  {
    return 80;
  }

  private static int getDefaultPort(String paramString)
  {
    if ("http".equalsIgnoreCase(paramString))
      return 80;
    if ("https".equalsIgnoreCase(paramString))
      return 443;
    return -1;
  }

  @Deprecated
  public static synchronized void resetProperties()
  {
  }

  int getKeepAliveTimeout()
  {
    return this.keepAliveTimeout;
  }

  public boolean getHttpKeepAliveSet()
  {
    return keepAliveProp;
  }

  protected HttpClient()
  {
    this.cachedHttpClient = false;
    this.poster = null;
    this.failedOnce = false;
    this.usingProxy = false;
    this.keepingAlive = false;
    this.keepAliveConnections = -1;
    this.keepAliveTimeout = 0;
    this.cacheRequest = null;
    this.reuse = false;
  }

  private HttpClient(URL paramURL)
    throws IOException
  {
    this(paramURL, (String)null, -1, false);
  }

  protected HttpClient(URL paramURL, boolean paramBoolean)
    throws IOException
  {
    this(paramURL, null, -1, paramBoolean);
  }

  public HttpClient(URL paramURL, String paramString, int paramInt)
    throws IOException
  {
    this(paramURL, paramString, paramInt, false);
  }

  protected HttpClient(URL paramURL, Proxy paramProxy, int paramInt)
    throws IOException
  {
    this.cachedHttpClient = false;
    this.poster = null;
    this.failedOnce = false;
    this.usingProxy = false;
    this.keepingAlive = false;
    this.keepAliveConnections = -1;
    this.keepAliveTimeout = 0;
    this.cacheRequest = null;
    this.reuse = false;
    this.proxy = ((paramProxy == null) ? Proxy.NO_PROXY : paramProxy);
    this.host = paramURL.getHost();
    this.url = paramURL;
    this.port = paramURL.getPort();
    if (this.port == -1)
      this.port = getDefaultPort();
    setConnectTimeout(paramInt);
    this.cookieHandler = ((CookieHandler)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        return CookieHandler.getDefault();
      }
    }));
    openServer();
  }

  protected static Proxy newHttpProxy(String paramString1, int paramInt, String paramString2)
  {
    if ((paramString1 == null) || (paramString2 == null))
      return Proxy.NO_PROXY;
    int i = (paramInt < 0) ? getDefaultPort(paramString2) : paramInt;
    InetSocketAddress localInetSocketAddress = InetSocketAddress.createUnresolved(paramString1, i);
    return new Proxy(Proxy.Type.HTTP, localInetSocketAddress);
  }

  private HttpClient(URL paramURL, String paramString, int paramInt, boolean paramBoolean)
    throws IOException
  {
    this(paramURL, (paramBoolean) ? Proxy.NO_PROXY : newHttpProxy(paramString, paramInt, "http"), -1);
  }

  public HttpClient(URL paramURL, String paramString, int paramInt1, boolean paramBoolean, int paramInt2)
    throws IOException
  {
    this(paramURL, (paramBoolean) ? Proxy.NO_PROXY : newHttpProxy(paramString, paramInt1, "http"), paramInt2);
  }

  public static HttpClient New(URL paramURL)
    throws IOException
  {
    return New(paramURL, Proxy.NO_PROXY, -1, true);
  }

  public static HttpClient New(URL paramURL, boolean paramBoolean)
    throws IOException
  {
    return New(paramURL, Proxy.NO_PROXY, -1, paramBoolean);
  }

  public static HttpClient New(URL paramURL, Proxy paramProxy, int paramInt, boolean paramBoolean)
    throws IOException
  {
    if (paramProxy == null)
      paramProxy = Proxy.NO_PROXY;
    HttpClient localHttpClient = null;
    if (paramBoolean)
    {
      localHttpClient = (HttpClient)kac.get(paramURL, null);
      if ((localHttpClient != null) && ((((localHttpClient.proxy != null) && (localHttpClient.proxy.equals(paramProxy))) || ((localHttpClient.proxy == null) && (paramProxy == null)))))
        synchronized (localHttpClient)
        {
          localHttpClient.cachedHttpClient = true;
          if ((!($assertionsDisabled)) && (!(localHttpClient.inCache)))
            throw new AssertionError();
          localHttpClient.inCache = false;
        }
    }
    if (localHttpClient == null)
    {
      localHttpClient = new HttpClient(paramURL, paramProxy, paramInt);
    }
    else
    {
      ??? = System.getSecurityManager();
      if (??? != null)
        if ((localHttpClient.proxy == Proxy.NO_PROXY) || (localHttpClient.proxy == null))
          ((SecurityManager)???).checkConnect(InetAddress.getByName(paramURL.getHost()).getHostAddress(), paramURL.getPort());
        else
          ((SecurityManager)???).checkConnect(paramURL.getHost(), paramURL.getPort());
      localHttpClient.url = paramURL;
    }
    return ((HttpClient)localHttpClient);
  }

  public static HttpClient New(URL paramURL, Proxy paramProxy, int paramInt)
    throws IOException
  {
    return New(paramURL, paramProxy, paramInt, true);
  }

  public static HttpClient New(URL paramURL, String paramString, int paramInt, boolean paramBoolean)
    throws IOException
  {
    return New(paramURL, newHttpProxy(paramString, paramInt, "http"), -1, paramBoolean);
  }

  public static HttpClient New(URL paramURL, String paramString, int paramInt1, boolean paramBoolean, int paramInt2)
    throws IOException
  {
    return New(paramURL, newHttpProxy(paramString, paramInt1, "http"), paramInt2, paramBoolean);
  }

  public void finished()
  {
    if (this.reuse)
      return;
    this.keepAliveConnections -= 1;
    this.poster = null;
    if ((this.keepAliveConnections > 0) && (isKeepingAlive()) && (!(this.serverOutput.checkError())))
      putInKeepAliveCache();
    else
      closeServer();
  }

  protected synchronized void putInKeepAliveCache()
  {
    if (this.inCache)
    {
      if (!($assertionsDisabled))
        throw new AssertionError("Duplicate put to keep alive cache");
      return;
    }
    this.inCache = true;
    kac.put(this.url, null, this);
  }

  protected boolean isInKeepAliveCache()
  {
    return this.inCache;
  }

  public void closeIdleConnection()
  {
    HttpClient localHttpClient = (HttpClient)kac.get(this.url, null);
    if (localHttpClient != null)
      localHttpClient.closeServer();
  }

  public void openServer(String paramString, int paramInt)
    throws IOException
  {
    this.serverSocket = doConnect(paramString, paramInt);
    try
    {
      this.serverOutput = new PrintStream(new BufferedOutputStream(this.serverSocket.getOutputStream()), false, encoding);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new InternalError(encoding + " encoding not found");
    }
    this.serverSocket.setTcpNoDelay(true);
  }

  public boolean needsTunneling()
  {
    return false;
  }

  public boolean isCachedConnection()
  {
    return this.cachedHttpClient;
  }

  public void afterConnect()
    throws IOException, UnknownHostException
  {
  }

  private synchronized void privilegedOpenServer(InetSocketAddress paramInetSocketAddress)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(this, paramInetSocketAddress)
      {
        public Object run()
          throws IOException
        {
          String str;
          if (this.val$server.isUnresolved())
          {
            str = this.val$server.getHostName();
          }
          else
          {
            str = this.val$server.getAddress().toString();
            int i = str.indexOf(47);
            if (i == 0)
              str = str.substring(1);
            else
              str = str.substring(0, i);
          }
          this.this$0.openServer(str, this.val$server.getPort());
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }

  private void superOpenServer(String paramString, int paramInt)
    throws IOException, UnknownHostException
  {
    super.openServer(paramString, paramInt);
  }

  private synchronized void privilegedSuperOpenServer(String paramString, int paramInt)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(this, paramString, paramInt)
      {
        public Object run()
          throws IOException
        {
          HttpClient.access$000(this.this$0, this.val$proxyHost, this.val$proxyPort);
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }

  protected synchronized void openServer()
    throws IOException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (this.keepingAlive)
    {
      if (localSecurityManager != null)
        localSecurityManager.checkConnect(this.host, this.port);
      return;
    }
    String str = this.url.getHost().toLowerCase();
    if ((this.url.getProtocol().equals("http")) || (this.url.getProtocol().equals("https")))
    {
      if ((this.proxy != null) && (this.proxy.type() == Proxy.Type.HTTP))
      {
        URLConnection.setProxiedHost(this.host);
        if (localSecurityManager != null)
          localSecurityManager.checkConnect(this.host, this.port);
        privilegedOpenServer((InetSocketAddress)this.proxy.address());
        this.usingProxy = true;
        return;
      }
      if (localSecurityManager != null)
        localSecurityManager.checkConnect(this.host, this.port);
      openServer(this.host, this.port);
      this.usingProxy = false;
      return;
    }
    if ((this.proxy != null) && (this.proxy.type() == Proxy.Type.HTTP))
    {
      URLConnection.setProxiedHost(this.host);
      if (localSecurityManager != null)
        localSecurityManager.checkConnect(this.host, this.port);
      privilegedOpenServer((InetSocketAddress)this.proxy.address());
      this.usingProxy = true;
      return;
    }
    if (localSecurityManager != null)
      localSecurityManager.checkConnect(this.host, this.port);
    super.openServer(this.host, this.port);
    this.usingProxy = false;
  }

  public String getURLFile()
    throws IOException
  {
    String str = this.url.getFile();
    if ((str == null) || (str.length() == 0))
      str = "/";
    if ((this.usingProxy) && (!(this.proxyDisabled)))
    {
      StringBuffer localStringBuffer = new StringBuffer(128);
      localStringBuffer.append(this.url.getProtocol());
      localStringBuffer.append(":");
      if ((this.url.getAuthority() != null) && (this.url.getAuthority().length() > 0))
      {
        localStringBuffer.append("//");
        localStringBuffer.append(this.url.getAuthority());
      }
      if (this.url.getPath() != null)
        localStringBuffer.append(this.url.getPath());
      if (this.url.getQuery() != null)
      {
        localStringBuffer.append('?');
        localStringBuffer.append(this.url.getQuery());
      }
      str = localStringBuffer.toString();
    }
    if (str.indexOf(10) == -1)
      return str;
    throw new MalformedURLException("Illegal character in URL");
  }

  @Deprecated
  public void writeRequests(MessageHeader paramMessageHeader)
  {
    this.requests = paramMessageHeader;
    this.requests.print(this.serverOutput);
    this.serverOutput.flush();
  }

  public void writeRequests(MessageHeader paramMessageHeader, PosterOutputStream paramPosterOutputStream)
    throws IOException
  {
    this.requests = paramMessageHeader;
    this.requests.print(this.serverOutput);
    this.poster = paramPosterOutputStream;
    if (this.poster != null)
      this.poster.writeTo(this.serverOutput);
    this.serverOutput.flush();
  }

  public boolean parseHTTP(MessageHeader paramMessageHeader, ProgressSource paramProgressSource, HttpURLConnection paramHttpURLConnection)
    throws IOException
  {
    try
    {
      this.serverInput = this.serverSocket.getInputStream();
      this.serverInput = new BufferedInputStream(this.serverInput);
      label133: return parseHTTPHeader(paramMessageHeader, paramProgressSource, paramHttpURLConnection);
    }
    catch (SocketTimeoutException localSocketTimeoutException)
    {
      closeServer();
      throw localSocketTimeoutException;
    }
    catch (IOException localIOException)
    {
      closeServer();
      this.cachedHttpClient = false;
      if ((!(this.failedOnce)) && (this.requests != null))
      {
        if ((paramHttpURLConnection.getRequestMethod().equals("POST")) && (!(retryPostProp)))
          break label133:
        this.failedOnce = true;
        openServer();
        if (needsTunneling())
          paramHttpURLConnection.doTunneling();
        afterConnect();
        writeRequests(this.requests, this.poster);
        return parseHTTP(paramMessageHeader, paramProgressSource, paramHttpURLConnection);
      }
      throw localIOException;
    }
  }

  public int setTimeout(int paramInt)
    throws SocketException
  {
    int i = this.serverSocket.getSoTimeout();
    this.serverSocket.setSoTimeout(paramInt);
    return i;
  }

  private boolean parseHTTPHeader(MessageHeader paramMessageHeader, ProgressSource paramProgressSource, HttpURLConnection paramHttpURLConnection)
    throws IOException
  {
    String str1;
    this.keepAliveConnections = -1;
    this.keepAliveTimeout = 0;
    int i = 0;
    byte[] arrayOfByte = new byte[8];
    try
    {
      int j = 0;
      this.serverInput.mark(10);
      while (j < 8)
      {
        int l = this.serverInput.read(arrayOfByte, j, 8 - j);
        if (l < 0)
          break;
        j += l;
      }
      str1 = null;
      i = ((arrayOfByte[0] == 72) && (arrayOfByte[1] == 84) && (arrayOfByte[2] == 84) && (arrayOfByte[3] == 80) && (arrayOfByte[4] == 47) && (arrayOfByte[5] == 49) && (arrayOfByte[6] == 46)) ? 1 : 0;
      this.serverInput.reset();
      if (i != 0)
      {
        Object localObject;
        paramMessageHeader.parseHeader(this.serverInput);
        if (this.cookieHandler != null)
        {
          localObject = ParseUtil.toURI(this.url);
          if (localObject != null)
            this.cookieHandler.put((URI)localObject, paramMessageHeader.getHeaders());
        }
        if (this.usingProxy)
          str1 = paramMessageHeader.findValue("Proxy-Connection");
        if (str1 == null)
          str1 = paramMessageHeader.findValue("Connection");
        if ((str1 != null) && (str1.toLowerCase().equals("keep-alive")))
        {
          localObject = new HeaderParser(paramMessageHeader.findValue("Keep-Alive"));
          if (localObject != null)
          {
            this.keepAliveConnections = ((HeaderParser)localObject).findInt("max", (this.usingProxy) ? 50 : 5);
            label445: this.keepAliveTimeout = ((HeaderParser)localObject).findInt("timeout", (this.usingProxy) ? 60 : 5);
          }
        }
        else if (arrayOfByte[7] != 48)
        {
          if (str1 != null)
            this.keepAliveConnections = 1;
          else
            this.keepAliveConnections = 5;
        }
      }
      else
      {
        if (j != 8)
        {
          if ((!(this.failedOnce)) && (this.requests != null))
          {
            if ((paramHttpURLConnection.getRequestMethod().equals("POST")) && (!(retryPostProp)))
              break label445:
            this.failedOnce = true;
            closeServer();
            this.cachedHttpClient = false;
            openServer();
            if (needsTunneling())
              paramHttpURLConnection.doTunneling();
            afterConnect();
            writeRequests(this.requests, this.poster);
            return parseHTTP(paramMessageHeader, paramProgressSource, paramHttpURLConnection);
          }
          throw new SocketException("Unexpected end of file from server");
        }
        paramMessageHeader.set("Content-type", "unknown/unknown");
      }
    }
    catch (IOException localIOException)
    {
      throw localIOException;
    }
    int k = -1;
    try
    {
      str1 = paramMessageHeader.getValue(0);
      for (int i2 = str1.indexOf(32); str1.charAt(i2) == ' '; ++i2);
      k = Integer.parseInt(str1.substring(i2, i2 + 3));
    }
    catch (Exception localException1)
    {
    }
    if (k == 100)
    {
      paramMessageHeader.reset();
      return parseHTTPHeader(paramMessageHeader, paramProgressSource, paramHttpURLConnection);
    }
    int i1 = -1;
    String str2 = null;
    try
    {
      str2 = paramMessageHeader.findValue("Transfer-Encoding");
    }
    catch (Exception localException2)
    {
    }
    if ((str2 != null) && (str2.equalsIgnoreCase("chunked")))
    {
      this.serverInput = new ChunkedInputStream(this.serverInput, this, paramMessageHeader);
      if (this.keepAliveConnections <= 1)
      {
        this.keepAliveConnections = 1;
        this.keepingAlive = false;
      }
      else
      {
        this.keepingAlive = true;
      }
      this.failedOnce = false;
    }
    else
    {
      try
      {
        i1 = Integer.parseInt(paramMessageHeader.findValue("content-length"));
      }
      catch (Exception localException3)
      {
      }
      String str3 = this.requests.getKey(0);
      if (((str3 != null) && (str3.startsWith("HEAD"))) || (k == 304) || (k == 204))
        i1 = 0;
      if ((this.keepAliveConnections > 1) && (((i1 >= 0) || (k == 304) || (k == 204))))
      {
        this.keepingAlive = true;
        this.failedOnce = false;
      }
      else if (this.keepingAlive)
      {
        this.keepingAlive = false;
      }
    }
    if (i1 > 0)
    {
      if (paramProgressSource != null)
        paramProgressSource.setContentType(paramMessageHeader.findValue("content-type"));
      if (isKeepingAlive())
      {
        this.serverInput = new KeepAliveStream(this.serverInput, paramProgressSource, i1, this);
        this.failedOnce = false;
      }
      else
      {
        this.serverInput = new MeteredStream(this.serverInput, paramProgressSource, i1);
      }
    }
    else if (i1 == -1)
    {
      if (paramProgressSource != null)
      {
        paramProgressSource.setContentType(paramMessageHeader.findValue("content-type"));
        this.serverInput = new MeteredStream(this.serverInput, paramProgressSource, i1);
      }
    }
    else if (paramProgressSource != null)
    {
      paramProgressSource.finishTracking();
    }
    return i;
  }

  public synchronized InputStream getInputStream()
  {
    return this.serverInput;
  }

  public OutputStream getOutputStream()
  {
    return this.serverOutput;
  }

  public String toString()
  {
    return getClass().getName() + "(" + this.url + ")";
  }

  public final boolean isKeepingAlive()
  {
    return ((getHttpKeepAliveSet()) && (this.keepingAlive));
  }

  public void setCacheRequest(CacheRequest paramCacheRequest)
  {
    this.cacheRequest = paramCacheRequest;
  }

  CacheRequest getCacheRequest()
  {
    return this.cacheRequest;
  }

  protected void finalize()
    throws Throwable
  {
  }

  public void setDoNotRetry(boolean paramBoolean)
  {
    this.failedOnce = paramBoolean;
  }

  public void closeServer()
  {
    try
    {
      this.keepingAlive = false;
      this.serverSocket.close();
    }
    catch (Exception localException)
    {
    }
  }

  public String getProxyHostUsed()
  {
    if (!(this.usingProxy))
      return null;
    InetSocketAddress localInetSocketAddress = (InetSocketAddress)this.proxy.address();
    String str = null;
    if (localInetSocketAddress.isUnresolved())
    {
      str = localInetSocketAddress.getHostName();
    }
    else
    {
      str = localInetSocketAddress.getAddress().toString();
      int i = str.indexOf(47);
      if (i == 0)
        str = str.substring(1);
      else
        str = str.substring(0, i);
    }
    return str;
  }

  public int getProxyPortUsed()
  {
    if (this.usingProxy)
      return ((InetSocketAddress)this.proxy.address()).getPort();
    return -1;
  }

  static
  {
    nonProxyHostsPool = null;
    nonProxyHostsSource = null;
    kac = new KeepAliveCache();
    keepAliveProp = true;
    retryPostProp = true;
    String str1 = (String)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return System.getProperty("http.keepAlive");
      }
    });
    String str2 = (String)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return System.getProperty("sun.net.http.retryPost");
      }
    });
    if (str1 != null)
      keepAliveProp = Boolean.valueOf(str1).booleanValue();
    else
      keepAliveProp = true;
    if (str2 != null)
      retryPostProp = Boolean.valueOf(str2).booleanValue();
    else
      retryPostProp = true;
  }
}