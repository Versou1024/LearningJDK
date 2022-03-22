package sun.net.www.protocol.http;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.CookieHandler;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.ResponseCache;
import java.net.SecureCacheResponse;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.net.ProgressSource;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;
import sun.net.www.http.ChunkedInputStream;
import sun.net.www.http.ChunkedOutputStream;
import sun.net.www.http.HttpClient;
import sun.net.www.http.PosterOutputStream;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetPropertyAction;

public class HttpURLConnection extends HttpURLConnection
{
  private static Logger logger = Logger.getLogger("sun.net.www.protocol.http.HttpURLConnection");
  static final String version;
  public static final String userAgent;
  static final int defaultmaxRedirects = 20;
  static final int maxRedirects;
  static final boolean validateProxy;
  static final boolean validateServer;
  private StreamingOutputStream strOutputStream;
  private static final String RETRY_MSG1 = "cannot retry due to proxy authentication, in streaming mode";
  private static final String RETRY_MSG2 = "cannot retry due to server authentication, in streaming mode";
  private static final String RETRY_MSG3 = "cannot retry due to redirection, in streaming mode";
  private static boolean enableESBuffer = false;
  private static int timeout4ESBuffer = 0;
  private static int bufSize4ES = 0;
  static final String httpVersion = "HTTP/1.1";
  static final String acceptString = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
  private static final String[] EXCLUDE_HEADERS;
  protected HttpClient http;
  protected Handler handler;
  protected Proxy instProxy;
  private CookieHandler cookieHandler;
  private ResponseCache cacheHandler;
  protected CacheResponse cachedResponse;
  private MessageHeader cachedHeaders;
  private InputStream cachedInputStream;
  protected PrintStream ps;
  private InputStream errorStream;
  private boolean setUserCookies;
  private String userCookies;
  private static HttpAuthenticator defaultAuth;
  private MessageHeader requests;
  String domain;
  DigestAuthentication.Parameters digestparams;
  AuthenticationInfo currentProxyCredentials;
  AuthenticationInfo currentServerCredentials;
  boolean needToCheck;
  private boolean doingNTLM2ndStage;
  private boolean doingNTLMp2ndStage;
  private boolean tryTransparentNTLMServer;
  private boolean tryTransparentNTLMProxy;
  Object authObj;
  boolean isUserServerAuth;
  boolean isUserProxyAuth;
  String serverAuthKey;
  String proxyAuthKey;
  protected ProgressSource pi;
  private MessageHeader responses;
  private InputStream inputStream;
  private PosterOutputStream poster;
  private boolean setRequests;
  private boolean failedOnce;
  private Exception rememberedException;
  private HttpClient reuseClient;
  private int connectTimeout;
  private int readTimeout;
  byte[] cdata;

  private static PasswordAuthentication privilegedRequestPasswordAuthentication(String paramString1, java.net.InetAddress paramInetAddress, int paramInt, String paramString2, String paramString3, String paramString4, URL paramURL, Authenticator.RequestorType paramRequestorType)
  {
    return ((PasswordAuthentication)AccessController.doPrivileged(new PrivilegedAction(paramString1, paramInetAddress, paramInt, paramString2, paramString3, paramString4, paramURL, paramRequestorType)
    {
      public Object run()
      {
        return Authenticator.requestPasswordAuthentication(this.val$host, this.val$addr, this.val$port, this.val$protocol, this.val$prompt, this.val$scheme, this.val$url, this.val$authType);
      }
    }));
  }

  private void checkMessageHeader(String paramString1, String paramString2)
  {
    int i = 10;
    int j = paramString1.indexOf(i);
    if (j != -1)
      throw new IllegalArgumentException("Illegal character(s) in message header field: " + paramString1);
    if (paramString2 == null)
      return;
    j = paramString2.indexOf(i);
    while (true)
    {
      if (j == -1)
        return;
      if (++j >= paramString2.length())
        break;
      int k = paramString2.charAt(j);
      if ((k != 32) && (k != 9))
        break;
      j = paramString2.indexOf(i, j);
    }
    throw new IllegalArgumentException("Illegal character(s) in message header value: " + paramString2);
  }

  private void writeRequests()
    throws IOException
  {
    if (this.http.usingProxy)
      setPreemptiveProxyAuthentication(this.requests);
    if (!(this.setRequests))
    {
      if (!(this.failedOnce))
        this.requests.prepend(this.method + " " + this.http.getURLFile() + " " + "HTTP/1.1", null);
      if (!(getUseCaches()))
      {
        this.requests.setIfNotSet("Cache-Control", "no-cache");
        this.requests.setIfNotSet("Pragma", "no-cache");
      }
      this.requests.setIfNotSet("User-Agent", userAgent);
      int i = this.url.getPort();
      String str2 = this.url.getHost();
      if ((i != -1) && (i != this.url.getDefaultPort()))
        str2 = str2 + ":" + String.valueOf(i);
      this.requests.setIfNotSet("Host", str2);
      this.requests.setIfNotSet("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
      if ((!(this.failedOnce)) && (this.http.getHttpKeepAliveSet()))
        if (this.http.usingProxy)
          this.requests.setIfNotSet("Proxy-Connection", "keep-alive");
        else
          this.requests.setIfNotSet("Connection", "keep-alive");
      else
        this.requests.setIfNotSet("Connection", "close");
      long l = getIfModifiedSince();
      if (l != 3412047033555484672L)
      {
        localObject1 = new Date(l);
        ??? = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        ((SimpleDateFormat)???).setTimeZone(TimeZone.getTimeZone("GMT"));
        this.requests.setIfNotSet("If-Modified-Since", ((SimpleDateFormat)???).format((Date)localObject1));
      }
      Object localObject1 = AuthenticationInfo.getServerAuth(this.url);
      if ((localObject1 != null) && (((AuthenticationInfo)localObject1).supportsPreemptiveAuthorization()))
      {
        this.requests.setIfNotSet(((AuthenticationInfo)localObject1).getHeaderName(), ((AuthenticationInfo)localObject1).getHeaderValue(this.url, this.method));
        this.currentServerCredentials = ((AuthenticationInfo)localObject1);
      }
      if ((!(this.method.equals("PUT"))) && (((this.poster != null) || (streaming()))))
        this.requests.setIfNotSet("Content-type", "application/x-www-form-urlencoded");
      if (streaming())
        if (this.chunkLength != -1)
          this.requests.set("Transfer-Encoding", "chunked");
        else
          this.requests.set("Content-Length", String.valueOf(this.fixedContentLength));
      else if (this.poster != null)
        synchronized (this.poster)
        {
          this.poster.close();
          this.requests.set("Content-Length", String.valueOf(this.poster.size()));
        }
      setCookieHeader();
      this.setRequests = true;
    }
    if (logger.isLoggable(Level.FINEST))
      logger.fine(this.requests.toString());
    this.http.writeRequests(this.requests, this.poster);
    if (this.ps.checkError())
    {
      String str1 = this.http.getProxyHostUsed();
      int j = this.http.getProxyPortUsed();
      disconnectInternal();
      if (this.failedOnce)
        throw new IOException("Error writing to server");
      this.failedOnce = true;
      if (str1 != null)
        setProxiedClient(this.url, str1, j);
      else
        setNewClient(this.url);
      this.ps = ((PrintStream)this.http.getOutputStream());
      this.connected = true;
      this.responses = new MessageHeader();
      this.setRequests = false;
      writeRequests();
    }
  }

  protected void setNewClient(URL paramURL)
    throws IOException
  {
    setNewClient(paramURL, false);
  }

  protected void setNewClient(URL paramURL, boolean paramBoolean)
    throws IOException
  {
    this.http = HttpClient.New(paramURL, null, -1, paramBoolean, this.connectTimeout);
    this.http.setReadTimeout(this.readTimeout);
  }

  protected void setProxiedClient(URL paramURL, String paramString, int paramInt)
    throws IOException
  {
    setProxiedClient(paramURL, paramString, paramInt, false);
  }

  protected void setProxiedClient(URL paramURL, String paramString, int paramInt, boolean paramBoolean)
    throws IOException
  {
    proxiedConnect(paramURL, paramString, paramInt, paramBoolean);
  }

  protected void proxiedConnect(URL paramURL, String paramString, int paramInt, boolean paramBoolean)
    throws IOException
  {
    this.http = HttpClient.New(paramURL, paramString, paramInt, paramBoolean, this.connectTimeout);
    this.http.setReadTimeout(this.readTimeout);
  }

  protected HttpURLConnection(URL paramURL, Handler paramHandler)
    throws IOException
  {
    this(paramURL, null, paramHandler);
  }

  public HttpURLConnection(URL paramURL, String paramString, int paramInt)
  {
    this(paramURL, new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(paramString, paramInt)));
  }

  public HttpURLConnection(URL paramURL, Proxy paramProxy)
  {
    this(paramURL, paramProxy, new Handler());
  }

  protected HttpURLConnection(URL paramURL, Proxy paramProxy, Handler paramHandler)
  {
    super(paramURL);
    this.ps = null;
    this.errorStream = null;
    this.setUserCookies = true;
    this.userCookies = null;
    this.currentProxyCredentials = null;
    this.currentServerCredentials = null;
    this.needToCheck = true;
    this.doingNTLM2ndStage = false;
    this.doingNTLMp2ndStage = false;
    this.tryTransparentNTLMServer = NTLMAuthentication.supportsTransparentAuth();
    this.tryTransparentNTLMProxy = NTLMAuthentication.supportsTransparentAuth();
    this.inputStream = null;
    this.poster = null;
    this.setRequests = false;
    this.failedOnce = false;
    this.rememberedException = null;
    this.reuseClient = null;
    this.connectTimeout = -1;
    this.readTimeout = -1;
    this.cdata = new byte[128];
    this.requests = new MessageHeader();
    this.responses = new MessageHeader();
    this.handler = paramHandler;
    this.instProxy = paramProxy;
    this.cookieHandler = ((CookieHandler)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        return CookieHandler.getDefault();
      }
    }));
    this.cacheHandler = ((ResponseCache)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        return ResponseCache.getDefault();
      }
    }));
  }

  public static void setDefaultAuthenticator(HttpAuthenticator paramHttpAuthenticator)
  {
    defaultAuth = paramHttpAuthenticator;
  }

  public static InputStream openConnectionCheckRedirects(URLConnection paramURLConnection)
    throws IOException
  {
    int i;
    int j = 0;
    InputStream localInputStream = null;
    do
    {
      if (paramURLConnection instanceof HttpURLConnection)
        ((HttpURLConnection)paramURLConnection).setInstanceFollowRedirects(false);
      localInputStream = paramURLConnection.getInputStream();
      i = 0;
      if (paramURLConnection instanceof HttpURLConnection)
      {
        HttpURLConnection localHttpURLConnection = (HttpURLConnection)paramURLConnection;
        int k = localHttpURLConnection.getResponseCode();
        if ((k >= 300) && (k <= 307) && (k != 306) && (k != 304))
        {
          URL localURL1 = localHttpURLConnection.getURL();
          String str = localHttpURLConnection.getHeaderField("Location");
          URL localURL2 = null;
          if (str != null)
            localURL2 = new URL(localURL1, str);
          localHttpURLConnection.disconnect();
          if ((localURL2 == null) || (!(localURL1.getProtocol().equals(localURL2.getProtocol()))) || (localURL1.getPort() != localURL2.getPort()) || (!(hostsEqual(localURL1, localURL2))) || (j >= 5))
            throw new SecurityException("illegal URL redirect");
          i = 1;
          paramURLConnection = localURL2.openConnection();
          ++j;
        }
      }
    }
    while (i != 0);
    return localInputStream;
  }

  private static boolean hostsEqual(URL paramURL1, URL paramURL2)
  {
    String str1 = paramURL1.getHost();
    String str2 = paramURL2.getHost();
    if (str1 == null)
      return (str2 == null);
    if (str2 == null)
      return false;
    if (str1.equalsIgnoreCase(str2))
      return true;
    boolean[] arrayOfBoolean = { false };
    AccessController.doPrivileged(new PrivilegedAction(str1, str2, arrayOfBoolean)
    {
      public Object run()
      {
        java.net.InetAddress localInetAddress1;
        try
        {
          localInetAddress1 = java.net.InetAddress.getByName(this.val$h1);
          java.net.InetAddress localInetAddress2 = java.net.InetAddress.getByName(this.val$h2);
          this.val$result[0] = localInetAddress1.equals(localInetAddress2);
        }
        catch (UnknownHostException localUnknownHostException)
        {
        }
        catch (SecurityException localSecurityException)
        {
        }
        return null;
      }
    });
    return arrayOfBoolean[0];
  }

  public void connect()
    throws IOException
  {
    plainConnect();
  }

  private boolean checkReuseConnection()
  {
    if (this.connected)
      return true;
    if (this.reuseClient != null)
    {
      this.http = this.reuseClient;
      this.http.setReadTimeout(getReadTimeout());
      this.http.reuse = false;
      this.reuseClient = null;
      this.connected = true;
      return true;
    }
    return false;
  }

  protected void plainConnect()
    throws IOException
  {
    if (this.connected)
      return;
    if ((this.cacheHandler != null) && (getUseCaches()))
    {
      try
      {
        URI localURI1 = ParseUtil.toURI(this.url);
        if (localURI1 != null)
        {
          this.cachedResponse = this.cacheHandler.get(localURI1, getRequestMethod(), this.requests.getHeaders(EXCLUDE_HEADERS));
          if (("https".equalsIgnoreCase(localURI1.getScheme())) && (!(this.cachedResponse instanceof SecureCacheResponse)))
            this.cachedResponse = null;
          if (this.cachedResponse != null)
          {
            this.cachedHeaders = mapToMessageHeader(this.cachedResponse.getHeaders());
            this.cachedInputStream = this.cachedResponse.getBody();
          }
        }
      }
      catch (IOException localIOException1)
      {
      }
      if ((this.cachedHeaders != null) && (this.cachedInputStream != null))
      {
        this.connected = true;
        return;
      }
      this.cachedResponse = null;
    }
    try
    {
      if (this.instProxy == null)
      {
        ProxySelector localProxySelector = (ProxySelector)AccessController.doPrivileged(new PrivilegedAction(this)
        {
          public Object run()
          {
            return ProxySelector.getDefault();
          }
        });
        Proxy localProxy = null;
        if (localProxySelector != null)
        {
          URI localURI2 = ParseUtil.toURI(this.url);
          Iterator localIterator = localProxySelector.select(localURI2).iterator();
          while (localIterator.hasNext())
          {
            localProxy = (Proxy)localIterator.next();
            try
            {
              if (!(this.failedOnce))
              {
                this.http = getNewHttpClient(this.url, localProxy, this.connectTimeout);
                this.http.setReadTimeout(this.readTimeout);
              }
              else
              {
                this.http = getNewHttpClient(this.url, localProxy, this.connectTimeout, false);
                this.http.setReadTimeout(this.readTimeout);
              }
            }
            catch (IOException localIOException3)
            {
              if (localProxy != Proxy.NO_PROXY)
              {
                localProxySelector.connectFailed(localURI2, localProxy.address(), localIOException3);
                if (localIterator.hasNext())
                  break label353;
                this.http = getNewHttpClient(this.url, null, this.connectTimeout, false);
                this.http.setReadTimeout(this.readTimeout);
                break label356:
              }
              label353: label356: throw localIOException3;
            }
          }
        }
        else if (!(this.failedOnce))
        {
          this.http = getNewHttpClient(this.url, null, this.connectTimeout);
          this.http.setReadTimeout(this.readTimeout);
        }
        else
        {
          this.http = getNewHttpClient(this.url, null, this.connectTimeout, false);
          this.http.setReadTimeout(this.readTimeout);
        }
      }
      else if (!(this.failedOnce))
      {
        this.http = getNewHttpClient(this.url, this.instProxy, this.connectTimeout);
        this.http.setReadTimeout(this.readTimeout);
      }
      else
      {
        this.http = getNewHttpClient(this.url, this.instProxy, this.connectTimeout, false);
        this.http.setReadTimeout(this.readTimeout);
      }
      this.ps = ((PrintStream)this.http.getOutputStream());
    }
    catch (IOException localIOException2)
    {
      throw localIOException2;
    }
    this.connected = true;
  }

  protected HttpClient getNewHttpClient(URL paramURL, Proxy paramProxy, int paramInt)
    throws IOException
  {
    return HttpClient.New(paramURL, paramProxy, paramInt);
  }

  protected HttpClient getNewHttpClient(URL paramURL, Proxy paramProxy, int paramInt, boolean paramBoolean)
    throws IOException
  {
    return HttpClient.New(paramURL, paramProxy, paramInt, paramBoolean);
  }

  public synchronized OutputStream getOutputStream()
    throws IOException
  {
    try
    {
      if (!(this.doOutput))
        throw new ProtocolException("cannot write to a URLConnection if doOutput=false - call setDoOutput(true)");
      if (this.method.equals("GET"))
        this.method = "POST";
      if ((!("POST".equals(this.method))) && (!("PUT".equals(this.method))) && ("http".equals(this.url.getProtocol())))
        throw new ProtocolException("HTTP method " + this.method + " doesn't support output");
      if (this.inputStream != null)
        throw new ProtocolException("Cannot write output after reading input.");
      if (!(checkReuseConnection()))
        connect();
      if ((streaming()) && (this.strOutputStream == null))
        writeRequests();
      this.ps = ((PrintStream)this.http.getOutputStream());
      if (streaming())
      {
        if (this.fixedContentLength != -1)
          this.strOutputStream = new StreamingOutputStream(this, this.ps, this.fixedContentLength);
        else if (this.chunkLength != -1)
          this.strOutputStream = new StreamingOutputStream(this, new ChunkedOutputStream(this.ps, this.chunkLength), -1);
        return this.strOutputStream;
      }
      if (this.poster == null)
        this.poster = new PosterOutputStream();
      return this.poster;
    }
    catch (RuntimeException localRuntimeException)
    {
      disconnectInternal();
      throw localRuntimeException;
    }
    catch (IOException localIOException)
    {
      disconnectInternal();
      throw localIOException;
    }
  }

  private boolean streaming()
  {
    return ((this.fixedContentLength != -1) || (this.chunkLength != -1));
  }

  private void setCookieHeader()
    throws IOException
  {
    if (this.cookieHandler != null)
    {
      if (this.setUserCookies)
      {
        int i = this.requests.getKey("Cookie");
        if (i != -1)
          this.userCookies = this.requests.getValue(i);
        this.setUserCookies = false;
      }
      this.requests.remove("Cookie");
      URI localURI = ParseUtil.toURI(this.url);
      if (localURI != null)
      {
        Map localMap = this.cookieHandler.get(localURI, this.requests.getHeaders(EXCLUDE_HEADERS));
        if (!(localMap.isEmpty()))
        {
          Set localSet = localMap.entrySet();
          Iterator localIterator1 = localSet.iterator();
          while (true)
          {
            Map.Entry localEntry;
            String str1;
            while (true)
            {
              if (!(localIterator1.hasNext()))
                break label280;
              localEntry = (Map.Entry)localIterator1.next();
              str1 = (String)localEntry.getKey();
              if (("Cookie".equalsIgnoreCase(str1)) || ("Cookie2".equalsIgnoreCase(str1)))
                break;
            }
            List localList = (List)localEntry.getValue();
            if ((localList != null) && (!(localList.isEmpty())))
            {
              Iterator localIterator2 = localList.iterator();
              StringBuilder localStringBuilder = new StringBuilder();
              while (localIterator2.hasNext())
              {
                String str2 = (String)localIterator2.next();
                localStringBuilder.append(str2).append(';');
              }
              try
              {
                this.requests.add(str1, localStringBuilder.substring(0, localStringBuilder.length() - 1));
              }
              catch (StringIndexOutOfBoundsException localStringIndexOutOfBoundsException)
              {
              }
            }
          }
        }
      }
      if (this.userCookies != null)
      {
        label280: int j;
        if ((j = this.requests.getKey("Cookie")) != -1)
          this.requests.set("Cookie", this.requests.getValue(j) + ";" + this.userCookies);
        else
          this.requests.set("Cookie", this.userCookies);
      }
    }
  }

  // ERROR //
  public synchronized InputStream getInputStream()
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 1031	sun/net/www/protocol/http/HttpURLConnection:doInput	Z
    //   4: ifne +13 -> 17
    //   7: new 566	java/net/ProtocolException
    //   10: dup
    //   11: ldc 8
    //   13: invokespecial 1126	java/net/ProtocolException:<init>	(Ljava/lang/String;)V
    //   16: athrow
    //   17: aload_0
    //   18: getfield 1051	sun/net/www/protocol/http/HttpURLConnection:rememberedException	Ljava/lang/Exception;
    //   21: ifnull +37 -> 58
    //   24: aload_0
    //   25: getfield 1051	sun/net/www/protocol/http/HttpURLConnection:rememberedException	Ljava/lang/Exception;
    //   28: instanceof 548
    //   31: ifeq +15 -> 46
    //   34: new 548	java/lang/RuntimeException
    //   37: dup
    //   38: aload_0
    //   39: getfield 1051	sun/net/www/protocol/http/HttpURLConnection:rememberedException	Ljava/lang/Exception;
    //   42: invokespecial 1096	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
    //   45: athrow
    //   46: aload_0
    //   47: aload_0
    //   48: getfield 1051	sun/net/www/protocol/http/HttpURLConnection:rememberedException	Ljava/lang/Exception;
    //   51: checkcast 535	IOException
    //   54: invokespecial 1262	sun/net/www/protocol/http/HttpURLConnection:getChainedException	(Ljava/io/IOException;)Ljava/io/IOException;
    //   57: athrow
    //   58: aload_0
    //   59: getfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   62: ifnull +8 -> 70
    //   65: aload_0
    //   66: getfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   69: areturn
    //   70: aload_0
    //   71: invokespecial 1251	sun/net/www/protocol/http/HttpURLConnection:streaming	()Z
    //   74: ifeq +42 -> 116
    //   77: aload_0
    //   78: getfield 1079	sun/net/www/protocol/http/HttpURLConnection:strOutputStream	Lsun/net/www/protocol/http/HttpURLConnection$StreamingOutputStream;
    //   81: ifnonnull +8 -> 89
    //   84: aload_0
    //   85: invokevirtual 1255	sun/net/www/protocol/http/HttpURLConnection:getOutputStream	()Ljava/io/OutputStream;
    //   88: pop
    //   89: aload_0
    //   90: getfield 1079	sun/net/www/protocol/http/HttpURLConnection:strOutputStream	Lsun/net/www/protocol/http/HttpURLConnection$StreamingOutputStream;
    //   93: invokevirtual 1288	sun/net/www/protocol/http/HttpURLConnection$StreamingOutputStream:close	()V
    //   96: aload_0
    //   97: getfield 1079	sun/net/www/protocol/http/HttpURLConnection:strOutputStream	Lsun/net/www/protocol/http/HttpURLConnection$StreamingOutputStream;
    //   100: invokevirtual 1289	sun/net/www/protocol/http/HttpURLConnection$StreamingOutputStream:writtenOK	()Z
    //   103: ifne +13 -> 116
    //   106: new 535	IOException
    //   109: dup
    //   110: ldc 25
    //   112: invokespecial 1082	IOException:<init>	(Ljava/lang/String;)V
    //   115: athrow
    //   116: iconst_0
    //   117: istore_1
    //   118: iconst_0
    //   119: istore_2
    //   120: iconst_m1
    //   121: istore_3
    //   122: aconst_null
    //   123: astore 4
    //   125: aconst_null
    //   126: astore 5
    //   128: aconst_null
    //   129: astore 6
    //   131: aload_0
    //   132: aload_0
    //   133: getfield 1069	sun/net/www/protocol/http/HttpURLConnection:requests	Lsun/net/www/MessageHeader;
    //   136: ldc 6
    //   138: invokevirtual 1170	sun/net/www/MessageHeader:getKey	(Ljava/lang/String;)I
    //   141: iconst_m1
    //   142: if_icmpeq +7 -> 149
    //   145: iconst_1
    //   146: goto +4 -> 150
    //   149: iconst_0
    //   150: putfield 1038	sun/net/www/protocol/http/HttpURLConnection:isUserServerAuth	Z
    //   153: aload_0
    //   154: aload_0
    //   155: getfield 1069	sun/net/www/protocol/http/HttpURLConnection:requests	Lsun/net/www/MessageHeader;
    //   158: ldc 30
    //   160: invokevirtual 1170	sun/net/www/MessageHeader:getKey	(Ljava/lang/String;)I
    //   163: iconst_m1
    //   164: if_icmpeq +7 -> 171
    //   167: iconst_1
    //   168: goto +4 -> 172
    //   171: iconst_0
    //   172: putfield 1037	sun/net/www/protocol/http/HttpURLConnection:isUserProxyAuth	Z
    //   175: aload_0
    //   176: invokespecial 1247	sun/net/www/protocol/http/HttpURLConnection:checkReuseConnection	()Z
    //   179: ifne +7 -> 186
    //   182: aload_0
    //   183: invokevirtual 1239	sun/net/www/protocol/http/HttpURLConnection:connect	()V
    //   186: aload_0
    //   187: getfield 1047	sun/net/www/protocol/http/HttpURLConnection:cachedInputStream	Ljava/io/InputStream;
    //   190: ifnull +15 -> 205
    //   193: aload_0
    //   194: getfield 1047	sun/net/www/protocol/http/HttpURLConnection:cachedInputStream	Ljava/io/InputStream;
    //   197: astore 7
    //   199: jsr +1404 -> 1603
    //   202: aload 7
    //   204: areturn
    //   205: invokestatic 1159	sun/net/ProgressMonitor:getDefault	()Lsun/net/ProgressMonitor;
    //   208: aload_0
    //   209: getfield 1065	sun/net/www/protocol/http/HttpURLConnection:url	Ljava/net/URL;
    //   212: aload_0
    //   213: getfield 1054	sun/net/www/protocol/http/HttpURLConnection:method	Ljava/lang/String;
    //   216: invokevirtual 1160	sun/net/ProgressMonitor:shouldMeterInput	(Ljava/net/URL;Ljava/lang/String;)Z
    //   219: istore 7
    //   221: iload 7
    //   223: ifeq +29 -> 252
    //   226: aload_0
    //   227: new 591	sun/net/ProgressSource
    //   230: dup
    //   231: aload_0
    //   232: getfield 1065	sun/net/www/protocol/http/HttpURLConnection:url	Ljava/net/URL;
    //   235: aload_0
    //   236: getfield 1054	sun/net/www/protocol/http/HttpURLConnection:method	Ljava/lang/String;
    //   239: invokespecial 1163	sun/net/ProgressSource:<init>	(Ljava/net/URL;Ljava/lang/String;)V
    //   242: putfield 1067	sun/net/www/protocol/http/HttpURLConnection:pi	Lsun/net/ProgressSource;
    //   245: aload_0
    //   246: getfield 1067	sun/net/www/protocol/http/HttpURLConnection:pi	Lsun/net/ProgressSource;
    //   249: invokevirtual 1161	sun/net/ProgressSource:beginTracking	()V
    //   252: aload_0
    //   253: aload_0
    //   254: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   257: invokevirtual 1191	sun/net/www/http/HttpClient:getOutputStream	()Ljava/io/OutputStream;
    //   260: checkcast 537	java/io/PrintStream
    //   263: putfield 1050	sun/net/www/protocol/http/HttpURLConnection:ps	Ljava/io/PrintStream;
    //   266: aload_0
    //   267: invokespecial 1251	sun/net/www/protocol/http/HttpURLConnection:streaming	()Z
    //   270: ifne +7 -> 277
    //   273: aload_0
    //   274: invokespecial 1246	sun/net/www/protocol/http/HttpURLConnection:writeRequests	()V
    //   277: aload_0
    //   278: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   281: aload_0
    //   282: getfield 1070	sun/net/www/protocol/http/HttpURLConnection:responses	Lsun/net/www/MessageHeader;
    //   285: aload_0
    //   286: getfield 1067	sun/net/www/protocol/http/HttpURLConnection:pi	Lsun/net/ProgressSource;
    //   289: aload_0
    //   290: invokevirtual 1199	sun/net/www/http/HttpClient:parseHTTP	(Lsun/net/www/MessageHeader;Lsun/net/ProgressSource;Lsun/net/www/protocol/http/HttpURLConnection;)Z
    //   293: pop
    //   294: getstatic 1066	sun/net/www/protocol/http/HttpURLConnection:logger	Ljava/util/logging/Logger;
    //   297: getstatic 1014	java/util/logging/Level:FINEST	Ljava/util/logging/Level;
    //   300: invokevirtual 1157	java/util/logging/Logger:isLoggable	(Ljava/util/logging/Level;)Z
    //   303: ifeq +16 -> 319
    //   306: getstatic 1066	sun/net/www/protocol/http/HttpURLConnection:logger	Ljava/util/logging/Logger;
    //   309: aload_0
    //   310: getfield 1070	sun/net/www/protocol/http/HttpURLConnection:responses	Lsun/net/www/MessageHeader;
    //   313: invokevirtual 1167	sun/net/www/MessageHeader:toString	()Ljava/lang/String;
    //   316: invokevirtual 1156	java/util/logging/Logger:fine	(Ljava/lang/String;)V
    //   319: aload_0
    //   320: aload_0
    //   321: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   324: invokevirtual 1190	sun/net/www/http/HttpClient:getInputStream	()Ljava/io/InputStream;
    //   327: putfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   330: aload_0
    //   331: invokevirtual 1237	sun/net/www/protocol/http/HttpURLConnection:getResponseCode	()I
    //   334: istore_2
    //   335: iload_2
    //   336: sipush 407
    //   339: if_icmpne +184 -> 523
    //   342: aload_0
    //   343: invokespecial 1251	sun/net/www/protocol/http/HttpURLConnection:streaming	()Z
    //   346: ifeq +21 -> 367
    //   349: aload_0
    //   350: invokespecial 1241	sun/net/www/protocol/http/HttpURLConnection:disconnectInternal	()V
    //   353: new 560	java/net/HttpRetryException
    //   356: dup
    //   357: ldc_w 513
    //   360: sipush 407
    //   363: invokespecial 1121	java/net/HttpRetryException:<init>	(Ljava/lang/String;I)V
    //   366: athrow
    //   367: new 600	sun/net/www/protocol/http/AuthenticationHeader
    //   370: dup
    //   371: ldc_w 506
    //   374: aload_0
    //   375: getfield 1070	sun/net/www/protocol/http/HttpURLConnection:responses	Lsun/net/www/MessageHeader;
    //   378: aload_0
    //   379: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   382: invokevirtual 1192	sun/net/www/http/HttpClient:getProxyHostUsed	()Ljava/lang/String;
    //   385: invokespecial 1207	sun/net/www/protocol/http/AuthenticationHeader:<init>	(Ljava/lang/String;Lsun/net/www/MessageHeader;Ljava/lang/String;)V
    //   388: astore 8
    //   390: aload_0
    //   391: getfield 1034	sun/net/www/protocol/http/HttpURLConnection:doingNTLMp2ndStage	Z
    //   394: ifne +28 -> 422
    //   397: aload_0
    //   398: aload 5
    //   400: aload 8
    //   402: invokespecial 1275	sun/net/www/protocol/http/HttpURLConnection:resetProxyAuthentication	(Lsun/net/www/protocol/http/AuthenticationInfo;Lsun/net/www/protocol/http/AuthenticationHeader;)Lsun/net/www/protocol/http/AuthenticationInfo;
    //   405: astore 5
    //   407: aload 5
    //   409: ifnull +114 -> 523
    //   412: iinc 1 1
    //   415: aload_0
    //   416: invokespecial 1241	sun/net/www/protocol/http/HttpURLConnection:disconnectInternal	()V
    //   419: goto +1040 -> 1459
    //   422: aload_0
    //   423: getfield 1070	sun/net/www/protocol/http/HttpURLConnection:responses	Lsun/net/www/MessageHeader;
    //   426: ldc_w 506
    //   429: invokevirtual 1174	sun/net/www/MessageHeader:findValue	(Ljava/lang/String;)Ljava/lang/String;
    //   432: astore 9
    //   434: aload_0
    //   435: invokespecial 1243	sun/net/www/protocol/http/HttpURLConnection:reset	()V
    //   438: aload 5
    //   440: aload_0
    //   441: aload 8
    //   443: invokevirtual 1206	sun/net/www/protocol/http/AuthenticationHeader:headerParser	()Lsun/net/www/HeaderParser;
    //   446: aload 9
    //   448: invokevirtual 1223	sun/net/www/protocol/http/AuthenticationInfo:setHeaders	(Lsun/net/www/protocol/http/HttpURLConnection;Lsun/net/www/HeaderParser;Ljava/lang/String;)Z
    //   451: ifne +18 -> 469
    //   454: aload_0
    //   455: invokespecial 1241	sun/net/www/protocol/http/HttpURLConnection:disconnectInternal	()V
    //   458: new 535	IOException
    //   461: dup
    //   462: ldc_w 498
    //   465: invokespecial 1082	IOException:<init>	(Ljava/lang/String;)V
    //   468: athrow
    //   469: aload 4
    //   471: ifnull +39 -> 510
    //   474: aload 6
    //   476: ifnull +34 -> 510
    //   479: aload 4
    //   481: aload_0
    //   482: aload 6
    //   484: invokevirtual 1206	sun/net/www/protocol/http/AuthenticationHeader:headerParser	()Lsun/net/www/HeaderParser;
    //   487: aload 9
    //   489: invokevirtual 1223	sun/net/www/protocol/http/AuthenticationInfo:setHeaders	(Lsun/net/www/protocol/http/HttpURLConnection;Lsun/net/www/HeaderParser;Ljava/lang/String;)Z
    //   492: ifne +18 -> 510
    //   495: aload_0
    //   496: invokespecial 1241	sun/net/www/protocol/http/HttpURLConnection:disconnectInternal	()V
    //   499: new 535	IOException
    //   502: dup
    //   503: ldc_w 498
    //   506: invokespecial 1082	IOException:<init>	(Ljava/lang/String;)V
    //   509: athrow
    //   510: aload_0
    //   511: aconst_null
    //   512: putfield 1052	sun/net/www/protocol/http/HttpURLConnection:authObj	Ljava/lang/Object;
    //   515: aload_0
    //   516: iconst_0
    //   517: putfield 1034	sun/net/www/protocol/http/HttpURLConnection:doingNTLMp2ndStage	Z
    //   520: goto +939 -> 1459
    //   523: aload 5
    //   525: ifnull +8 -> 533
    //   528: aload 5
    //   530: invokevirtual 1208	sun/net/www/protocol/http/AuthenticationInfo:addToCache	()V
    //   533: iload_2
    //   534: sipush 401
    //   537: if_icmpne +222 -> 759
    //   540: aload_0
    //   541: invokespecial 1251	sun/net/www/protocol/http/HttpURLConnection:streaming	()Z
    //   544: ifeq +21 -> 565
    //   547: aload_0
    //   548: invokespecial 1241	sun/net/www/protocol/http/HttpURLConnection:disconnectInternal	()V
    //   551: new 560	java/net/HttpRetryException
    //   554: dup
    //   555: ldc_w 515
    //   558: sipush 401
    //   561: invokespecial 1121	java/net/HttpRetryException:<init>	(Ljava/lang/String;I)V
    //   564: athrow
    //   565: new 600	sun/net/www/protocol/http/AuthenticationHeader
    //   568: dup
    //   569: ldc_w 511
    //   572: aload_0
    //   573: getfield 1070	sun/net/www/protocol/http/HttpURLConnection:responses	Lsun/net/www/MessageHeader;
    //   576: aload_0
    //   577: getfield 1065	sun/net/www/protocol/http/HttpURLConnection:url	Ljava/net/URL;
    //   580: invokevirtual 1136	java/net/URL:getHost	()Ljava/lang/String;
    //   583: invokevirtual 1104	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   586: invokespecial 1207	sun/net/www/protocol/http/AuthenticationHeader:<init>	(Ljava/lang/String;Lsun/net/www/MessageHeader;Ljava/lang/String;)V
    //   589: astore 6
    //   591: aload 6
    //   593: invokevirtual 1204	sun/net/www/protocol/http/AuthenticationHeader:raw	()Ljava/lang/String;
    //   596: astore 8
    //   598: aload_0
    //   599: getfield 1033	sun/net/www/protocol/http/HttpURLConnection:doingNTLM2ndStage	Z
    //   602: ifne +109 -> 711
    //   605: aload 4
    //   607: ifnull +71 -> 678
    //   610: aload 4
    //   612: instanceof 619
    //   615: ifne +63 -> 678
    //   618: aload 4
    //   620: aload 8
    //   622: invokevirtual 1214	sun/net/www/protocol/http/AuthenticationInfo:isAuthorizationStale	(Ljava/lang/String;)Z
    //   625: ifeq +48 -> 673
    //   628: aload_0
    //   629: invokespecial 1241	sun/net/www/protocol/http/HttpURLConnection:disconnectInternal	()V
    //   632: iinc 1 1
    //   635: aload_0
    //   636: getfield 1069	sun/net/www/protocol/http/HttpURLConnection:requests	Lsun/net/www/MessageHeader;
    //   639: aload 4
    //   641: invokevirtual 1212	sun/net/www/protocol/http/AuthenticationInfo:getHeaderName	()Ljava/lang/String;
    //   644: aload 4
    //   646: aload_0
    //   647: getfield 1065	sun/net/www/protocol/http/HttpURLConnection:url	Ljava/net/URL;
    //   650: aload_0
    //   651: getfield 1054	sun/net/www/protocol/http/HttpURLConnection:method	Ljava/lang/String;
    //   654: invokevirtual 1221	sun/net/www/protocol/http/AuthenticationInfo:getHeaderValue	(Ljava/net/URL;Ljava/lang/String;)Ljava/lang/String;
    //   657: invokevirtual 1177	sun/net/www/MessageHeader:set	(Ljava/lang/String;Ljava/lang/String;)V
    //   660: aload_0
    //   661: aload 4
    //   663: putfield 1075	sun/net/www/protocol/http/HttpURLConnection:currentServerCredentials	Lsun/net/www/protocol/http/AuthenticationInfo;
    //   666: aload_0
    //   667: invokespecial 1245	sun/net/www/protocol/http/HttpURLConnection:setCookieHeader	()V
    //   670: goto +789 -> 1459
    //   673: aload 4
    //   675: invokevirtual 1209	sun/net/www/protocol/http/AuthenticationInfo:removeFromCache	()V
    //   678: aload_0
    //   679: aload 6
    //   681: invokespecial 1272	sun/net/www/protocol/http/HttpURLConnection:getServerAuthentication	(Lsun/net/www/protocol/http/AuthenticationHeader;)Lsun/net/www/protocol/http/AuthenticationInfo;
    //   684: astore 4
    //   686: aload_0
    //   687: aload 4
    //   689: putfield 1075	sun/net/www/protocol/http/HttpURLConnection:currentServerCredentials	Lsun/net/www/protocol/http/AuthenticationInfo;
    //   692: aload 4
    //   694: ifnull +65 -> 759
    //   697: aload_0
    //   698: invokespecial 1241	sun/net/www/protocol/http/HttpURLConnection:disconnectInternal	()V
    //   701: iinc 1 1
    //   704: aload_0
    //   705: invokespecial 1245	sun/net/www/protocol/http/HttpURLConnection:setCookieHeader	()V
    //   708: goto +751 -> 1459
    //   711: aload_0
    //   712: invokespecial 1243	sun/net/www/protocol/http/HttpURLConnection:reset	()V
    //   715: aload 4
    //   717: aload_0
    //   718: aconst_null
    //   719: aload 8
    //   721: invokevirtual 1223	sun/net/www/protocol/http/AuthenticationInfo:setHeaders	(Lsun/net/www/protocol/http/HttpURLConnection;Lsun/net/www/HeaderParser;Ljava/lang/String;)Z
    //   724: ifne +18 -> 742
    //   727: aload_0
    //   728: invokespecial 1241	sun/net/www/protocol/http/HttpURLConnection:disconnectInternal	()V
    //   731: new 535	IOException
    //   734: dup
    //   735: ldc_w 498
    //   738: invokespecial 1082	IOException:<init>	(Ljava/lang/String;)V
    //   741: athrow
    //   742: aload_0
    //   743: iconst_0
    //   744: putfield 1033	sun/net/www/protocol/http/HttpURLConnection:doingNTLM2ndStage	Z
    //   747: aload_0
    //   748: aconst_null
    //   749: putfield 1052	sun/net/www/protocol/http/HttpURLConnection:authObj	Ljava/lang/Object;
    //   752: aload_0
    //   753: invokespecial 1245	sun/net/www/protocol/http/HttpURLConnection:setCookieHeader	()V
    //   756: goto +703 -> 1459
    //   759: aload 4
    //   761: ifnull +222 -> 983
    //   764: aload 4
    //   766: instanceof 603
    //   769: ifeq +10 -> 779
    //   772: aload_0
    //   773: getfield 1053	sun/net/www/protocol/http/HttpURLConnection:domain	Ljava/lang/String;
    //   776: ifnonnull +96 -> 872
    //   779: aload 4
    //   781: instanceof 602
    //   784: ifeq +80 -> 864
    //   787: aload_0
    //   788: getfield 1065	sun/net/www/protocol/http/HttpURLConnection:url	Ljava/net/URL;
    //   791: invokevirtual 1137	java/net/URL:getPath	()Ljava/lang/String;
    //   794: invokestatic 1215	sun/net/www/protocol/http/AuthenticationInfo:reducePath	(Ljava/lang/String;)Ljava/lang/String;
    //   797: astore 8
    //   799: aload 4
    //   801: getfield 1017	sun/net/www/protocol/http/AuthenticationInfo:path	Ljava/lang/String;
    //   804: astore 9
    //   806: aload 9
    //   808: aload 8
    //   810: invokevirtual 1108	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   813: ifeq +16 -> 829
    //   816: aload 8
    //   818: invokevirtual 1099	java/lang/String:length	()I
    //   821: aload 9
    //   823: invokevirtual 1099	java/lang/String:length	()I
    //   826: if_icmplt +12 -> 838
    //   829: aload 9
    //   831: aload 8
    //   833: invokestatic 1225	sun/net/www/protocol/http/BasicAuthentication:getRootPath	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   836: astore 8
    //   838: aload 4
    //   840: invokevirtual 1211	sun/net/www/protocol/http/AuthenticationInfo:clone	()Ljava/lang/Object;
    //   843: checkcast 602	sun/net/www/protocol/http/BasicAuthentication
    //   846: astore 10
    //   848: aload 4
    //   850: invokevirtual 1209	sun/net/www/protocol/http/AuthenticationInfo:removeFromCache	()V
    //   853: aload 10
    //   855: aload 8
    //   857: putfield 1018	sun/net/www/protocol/http/BasicAuthentication:path	Ljava/lang/String;
    //   860: aload 10
    //   862: astore 4
    //   864: aload 4
    //   866: invokevirtual 1208	sun/net/www/protocol/http/AuthenticationInfo:addToCache	()V
    //   869: goto +114 -> 983
    //   872: aload 4
    //   874: checkcast 603	sun/net/www/protocol/http/DigestAuthentication
    //   877: astore 8
    //   879: new 586	java/util/StringTokenizer
    //   882: dup
    //   883: aload_0
    //   884: getfield 1053	sun/net/www/protocol/http/HttpURLConnection:domain	Ljava/lang/String;
    //   887: ldc 1
    //   889: invokespecial 1154	java/util/StringTokenizer:<init>	(Ljava/lang/String;Ljava/lang/String;)V
    //   892: astore 9
    //   894: aload 8
    //   896: getfield 1019	sun/net/www/protocol/http/DigestAuthentication:realm	Ljava/lang/String;
    //   899: astore 10
    //   901: aload 8
    //   903: getfield 1020	sun/net/www/protocol/http/DigestAuthentication:pw	Ljava/net/PasswordAuthentication;
    //   906: astore 11
    //   908: aload_0
    //   909: aload 8
    //   911: getfield 1021	sun/net/www/protocol/http/DigestAuthentication:params	Lsun/net/www/protocol/http/DigestAuthentication$Parameters;
    //   914: putfield 1076	sun/net/www/protocol/http/HttpURLConnection:digestparams	Lsun/net/www/protocol/http/DigestAuthentication$Parameters;
    //   917: aload 9
    //   919: invokevirtual 1151	java/util/StringTokenizer:hasMoreTokens	()Z
    //   922: ifeq +61 -> 983
    //   925: aload 9
    //   927: invokevirtual 1152	java/util/StringTokenizer:nextToken	()Ljava/lang/String;
    //   930: astore 12
    //   932: new 573	java/net/URL
    //   935: dup
    //   936: aload_0
    //   937: getfield 1065	sun/net/www/protocol/http/HttpURLConnection:url	Ljava/net/URL;
    //   940: aload 12
    //   942: invokespecial 1142	java/net/URL:<init>	(Ljava/net/URL;Ljava/lang/String;)V
    //   945: astore 13
    //   947: new 603	sun/net/www/protocol/http/DigestAuthentication
    //   950: dup
    //   951: iconst_0
    //   952: aload 13
    //   954: aload 10
    //   956: ldc_w 501
    //   959: aload 11
    //   961: aload_0
    //   962: getfield 1076	sun/net/www/protocol/http/HttpURLConnection:digestparams	Lsun/net/www/protocol/http/DigestAuthentication$Parameters;
    //   965: invokespecial 1232	sun/net/www/protocol/http/DigestAuthentication:<init>	(ZLjava/net/URL;Ljava/lang/String;Ljava/lang/String;Ljava/net/PasswordAuthentication;Lsun/net/www/protocol/http/DigestAuthentication$Parameters;)V
    //   968: astore 14
    //   970: aload 14
    //   972: invokevirtual 1230	sun/net/www/protocol/http/DigestAuthentication:addToCache	()V
    //   975: goto +5 -> 980
    //   978: astore 13
    //   980: goto -63 -> 917
    //   983: aload_0
    //   984: iconst_0
    //   985: putfield 1034	sun/net/www/protocol/http/HttpURLConnection:doingNTLMp2ndStage	Z
    //   988: aload_0
    //   989: iconst_0
    //   990: putfield 1033	sun/net/www/protocol/http/HttpURLConnection:doingNTLM2ndStage	Z
    //   993: aload_0
    //   994: getfield 1038	sun/net/www/protocol/http/HttpURLConnection:isUserServerAuth	Z
    //   997: ifne +12 -> 1009
    //   1000: aload_0
    //   1001: getfield 1069	sun/net/www/protocol/http/HttpURLConnection:requests	Lsun/net/www/MessageHeader;
    //   1004: ldc 6
    //   1006: invokevirtual 1171	sun/net/www/MessageHeader:remove	(Ljava/lang/String;)V
    //   1009: aload_0
    //   1010: getfield 1037	sun/net/www/protocol/http/HttpURLConnection:isUserProxyAuth	Z
    //   1013: ifne +12 -> 1025
    //   1016: aload_0
    //   1017: getfield 1069	sun/net/www/protocol/http/HttpURLConnection:requests	Lsun/net/www/MessageHeader;
    //   1020: ldc 30
    //   1022: invokevirtual 1171	sun/net/www/MessageHeader:remove	(Ljava/lang/String;)V
    //   1025: iload_2
    //   1026: sipush 200
    //   1029: if_icmpne +11 -> 1040
    //   1032: aload_0
    //   1033: iconst_0
    //   1034: invokespecial 1252	sun/net/www/protocol/http/HttpURLConnection:checkResponseCredentials	(Z)V
    //   1037: goto +8 -> 1045
    //   1040: aload_0
    //   1041: iconst_0
    //   1042: putfield 1039	sun/net/www/protocol/http/HttpURLConnection:needToCheck	Z
    //   1045: aload_0
    //   1046: iconst_1
    //   1047: putfield 1039	sun/net/www/protocol/http/HttpURLConnection:needToCheck	Z
    //   1050: aload_0
    //   1051: invokespecial 1248	sun/net/www/protocol/http/HttpURLConnection:followRedirect	()Z
    //   1054: ifeq +13 -> 1067
    //   1057: iinc 1 1
    //   1060: aload_0
    //   1061: invokespecial 1245	sun/net/www/protocol/http/HttpURLConnection:setCookieHeader	()V
    //   1064: goto +395 -> 1459
    //   1067: aload_0
    //   1068: getfield 1070	sun/net/www/protocol/http/HttpURLConnection:responses	Lsun/net/www/MessageHeader;
    //   1071: ldc_w 516
    //   1074: invokevirtual 1174	sun/net/www/MessageHeader:findValue	(Ljava/lang/String;)Ljava/lang/String;
    //   1077: invokestatic 1093	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1080: istore_3
    //   1081: goto +5 -> 1086
    //   1084: astore 8
    //   1086: aload_0
    //   1087: getfield 1054	sun/net/www/protocol/http/HttpURLConnection:method	Ljava/lang/String;
    //   1090: ldc_w 502
    //   1093: invokevirtual 1103	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1096: ifne +21 -> 1117
    //   1099: iload_3
    //   1100: ifeq +17 -> 1117
    //   1103: iload_2
    //   1104: sipush 304
    //   1107: if_icmpeq +10 -> 1117
    //   1110: iload_2
    //   1111: sipush 204
    //   1114: if_icmpne +50 -> 1164
    //   1117: aload_0
    //   1118: getfield 1067	sun/net/www/protocol/http/HttpURLConnection:pi	Lsun/net/ProgressSource;
    //   1121: ifnull +15 -> 1136
    //   1124: aload_0
    //   1125: getfield 1067	sun/net/www/protocol/http/HttpURLConnection:pi	Lsun/net/ProgressSource;
    //   1128: invokevirtual 1162	sun/net/ProgressSource:finishTracking	()V
    //   1131: aload_0
    //   1132: aconst_null
    //   1133: putfield 1067	sun/net/www/protocol/http/HttpURLConnection:pi	Lsun/net/ProgressSource;
    //   1136: aload_0
    //   1137: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   1140: invokevirtual 1185	sun/net/www/http/HttpClient:finished	()V
    //   1143: aload_0
    //   1144: aconst_null
    //   1145: putfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   1148: aload_0
    //   1149: new 605	sun/net/www/protocol/http/EmptyInputStream
    //   1152: dup
    //   1153: invokespecial 1234	sun/net/www/protocol/http/EmptyInputStream:<init>	()V
    //   1156: putfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   1159: aload_0
    //   1160: iconst_0
    //   1161: putfield 1030	sun/net/www/protocol/http/HttpURLConnection:connected	Z
    //   1164: iload_2
    //   1165: sipush 200
    //   1168: if_icmpeq +38 -> 1206
    //   1171: iload_2
    //   1172: sipush 203
    //   1175: if_icmpeq +31 -> 1206
    //   1178: iload_2
    //   1179: sipush 206
    //   1182: if_icmpeq +24 -> 1206
    //   1185: iload_2
    //   1186: sipush 300
    //   1189: if_icmpeq +17 -> 1206
    //   1192: iload_2
    //   1193: sipush 301
    //   1196: if_icmpeq +10 -> 1206
    //   1199: iload_2
    //   1200: sipush 410
    //   1203: if_icmpne +128 -> 1331
    //   1206: aload_0
    //   1207: getfield 1064	sun/net/www/protocol/http/HttpURLConnection:cacheHandler	Ljava/net/ResponseCache;
    //   1210: ifnull +121 -> 1331
    //   1213: aload_0
    //   1214: invokevirtual 1250	sun/net/www/protocol/http/HttpURLConnection:getUseCaches	()Z
    //   1217: ifeq +114 -> 1331
    //   1220: aload_0
    //   1221: getfield 1065	sun/net/www/protocol/http/HttpURLConnection:url	Ljava/net/URL;
    //   1224: invokestatic 1180	sun/net/www/ParseUtil:toURI	(Ljava/net/URL;)Ljava/net/URI;
    //   1227: astore 8
    //   1229: aload 8
    //   1231: ifnull +100 -> 1331
    //   1234: aload_0
    //   1235: astore 9
    //   1237: ldc 39
    //   1239: aload 8
    //   1241: invokevirtual 1133	java/net/URI:getScheme	()Ljava/lang/String;
    //   1244: invokevirtual 1107	java/lang/String:equalsIgnoreCase	(Ljava/lang/String;)Z
    //   1247: ifeq +32 -> 1279
    //   1250: aload_0
    //   1251: invokevirtual 1095	java/lang/Object:getClass	()Ljava/lang/Class;
    //   1254: ldc_w 524
    //   1257: invokevirtual 1089	java/lang/Class:getField	(Ljava/lang/String;)Ljava/lang/reflect/Field;
    //   1260: aload_0
    //   1261: invokevirtual 1117	java/lang/reflect/Field:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   1264: checkcast 574	java/net/URLConnection
    //   1267: astore 9
    //   1269: goto +10 -> 1279
    //   1272: astore 10
    //   1274: goto +5 -> 1279
    //   1277: astore 10
    //   1279: aload_0
    //   1280: getfield 1064	sun/net/www/protocol/http/HttpURLConnection:cacheHandler	Ljava/net/ResponseCache;
    //   1283: aload 8
    //   1285: aload 9
    //   1287: invokevirtual 1131	java/net/ResponseCache:put	(Ljava/net/URI;Ljava/net/URLConnection;)Ljava/net/CacheRequest;
    //   1290: astore 10
    //   1292: aload 10
    //   1294: ifnull +37 -> 1331
    //   1297: aload_0
    //   1298: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   1301: ifnull +30 -> 1331
    //   1304: aload_0
    //   1305: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   1308: aload 10
    //   1310: invokevirtual 1194	sun/net/www/http/HttpClient:setCacheRequest	(Ljava/net/CacheRequest;)V
    //   1313: aload_0
    //   1314: new 617	sun/net/www/protocol/http/HttpURLConnection$HttpInputStream
    //   1317: dup
    //   1318: aload_0
    //   1319: aload_0
    //   1320: getfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   1323: aload 10
    //   1325: invokespecial 1287	sun/net/www/protocol/http/HttpURLConnection$HttpInputStream:<init>	(Lsun/net/www/protocol/http/HttpURLConnection;Ljava/io/InputStream;Ljava/net/CacheRequest;)V
    //   1328: putfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   1331: aload_0
    //   1332: getfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   1335: instanceof 617
    //   1338: ifne +19 -> 1357
    //   1341: aload_0
    //   1342: new 617	sun/net/www/protocol/http/HttpURLConnection$HttpInputStream
    //   1345: dup
    //   1346: aload_0
    //   1347: aload_0
    //   1348: getfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   1351: invokespecial 1286	sun/net/www/protocol/http/HttpURLConnection$HttpInputStream:<init>	(Lsun/net/www/protocol/http/HttpURLConnection;Ljava/io/InputStream;)V
    //   1354: putfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   1357: iload_2
    //   1358: sipush 400
    //   1361: if_icmplt +76 -> 1437
    //   1364: iload_2
    //   1365: sipush 404
    //   1368: if_icmpeq +10 -> 1378
    //   1371: iload_2
    //   1372: sipush 410
    //   1375: if_icmpne +18 -> 1393
    //   1378: new 534	java/io/FileNotFoundException
    //   1381: dup
    //   1382: aload_0
    //   1383: getfield 1065	sun/net/www/protocol/http/HttpURLConnection:url	Ljava/net/URL;
    //   1386: invokevirtual 1139	java/net/URL:toString	()Ljava/lang/String;
    //   1389: invokespecial 1080	java/io/FileNotFoundException:<init>	(Ljava/lang/String;)V
    //   1392: athrow
    //   1393: new 535	IOException
    //   1396: dup
    //   1397: new 552	java/lang/StringBuilder
    //   1400: dup
    //   1401: invokespecial 1110	java/lang/StringBuilder:<init>	()V
    //   1404: ldc_w 509
    //   1407: invokevirtual 1115	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1410: iload_2
    //   1411: invokevirtual 1114	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   1414: ldc_w 493
    //   1417: invokevirtual 1115	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1420: aload_0
    //   1421: getfield 1065	sun/net/www/protocol/http/HttpURLConnection:url	Ljava/net/URL;
    //   1424: invokevirtual 1139	java/net/URL:toString	()Ljava/lang/String;
    //   1427: invokevirtual 1115	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1430: invokevirtual 1111	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1433: invokespecial 1082	IOException:<init>	(Ljava/lang/String;)V
    //   1436: athrow
    //   1437: aload_0
    //   1438: aconst_null
    //   1439: putfield 1073	sun/net/www/protocol/http/HttpURLConnection:poster	Lsun/net/www/http/PosterOutputStream;
    //   1442: aload_0
    //   1443: aconst_null
    //   1444: putfield 1079	sun/net/www/protocol/http/HttpURLConnection:strOutputStream	Lsun/net/www/protocol/http/HttpURLConnection$StreamingOutputStream;
    //   1447: aload_0
    //   1448: getfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   1451: astore 8
    //   1453: jsr +150 -> 1603
    //   1456: aload 8
    //   1458: areturn
    //   1459: iload_1
    //   1460: getstatic 1026	sun/net/www/protocol/http/HttpURLConnection:maxRedirects	I
    //   1463: if_icmplt -1288 -> 175
    //   1466: new 566	java/net/ProtocolException
    //   1469: dup
    //   1470: new 552	java/lang/StringBuilder
    //   1473: dup
    //   1474: invokespecial 1110	java/lang/StringBuilder:<init>	()V
    //   1477: ldc_w 508
    //   1480: invokevirtual 1115	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1483: iload_1
    //   1484: invokevirtual 1114	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   1487: ldc_w 495
    //   1490: invokevirtual 1115	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1493: invokevirtual 1111	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1496: invokespecial 1126	java/net/ProtocolException:<init>	(Ljava/lang/String;)V
    //   1499: athrow
    //   1500: astore 7
    //   1502: aload_0
    //   1503: invokespecial 1241	sun/net/www/protocol/http/HttpURLConnection:disconnectInternal	()V
    //   1506: aload_0
    //   1507: aload 7
    //   1509: putfield 1051	sun/net/www/protocol/http/HttpURLConnection:rememberedException	Ljava/lang/Exception;
    //   1512: aload 7
    //   1514: athrow
    //   1515: astore 7
    //   1517: aload_0
    //   1518: aload 7
    //   1520: putfield 1051	sun/net/www/protocol/http/HttpURLConnection:rememberedException	Ljava/lang/Exception;
    //   1523: aload_0
    //   1524: getfield 1070	sun/net/www/protocol/http/HttpURLConnection:responses	Lsun/net/www/MessageHeader;
    //   1527: ldc 32
    //   1529: invokevirtual 1174	sun/net/www/MessageHeader:findValue	(Ljava/lang/String;)Ljava/lang/String;
    //   1532: astore 8
    //   1534: aload_0
    //   1535: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   1538: ifnull +54 -> 1592
    //   1541: aload_0
    //   1542: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   1545: invokevirtual 1187	sun/net/www/http/HttpClient:isKeepingAlive	()Z
    //   1548: ifeq +44 -> 1592
    //   1551: getstatic 1035	sun/net/www/protocol/http/HttpURLConnection:enableESBuffer	Z
    //   1554: ifeq +38 -> 1592
    //   1557: iload_3
    //   1558: ifgt +18 -> 1576
    //   1561: aload 8
    //   1563: ifnull +29 -> 1592
    //   1566: aload 8
    //   1568: ldc 36
    //   1570: invokevirtual 1107	java/lang/String:equalsIgnoreCase	(Ljava/lang/String;)Z
    //   1573: ifeq +19 -> 1592
    //   1576: aload_0
    //   1577: aload_0
    //   1578: getfield 1049	sun/net/www/protocol/http/HttpURLConnection:inputStream	Ljava/io/InputStream;
    //   1581: iload_3
    //   1582: aload_0
    //   1583: getfield 1071	sun/net/www/protocol/http/HttpURLConnection:http	Lsun/net/www/http/HttpClient;
    //   1586: invokestatic 1285	sun/net/www/protocol/http/HttpURLConnection$ErrorStream:getErrorStream	(Ljava/io/InputStream;ILsun/net/www/http/HttpClient;)Ljava/io/InputStream;
    //   1589: putfield 1048	sun/net/www/protocol/http/HttpURLConnection:errorStream	Ljava/io/InputStream;
    //   1592: aload 7
    //   1594: athrow
    //   1595: astore 15
    //   1597: jsr +6 -> 1603
    //   1600: aload 15
    //   1602: athrow
    //   1603: astore 16
    //   1605: aload_0
    //   1606: getfield 1055	sun/net/www/protocol/http/HttpURLConnection:proxyAuthKey	Ljava/lang/String;
    //   1609: ifnull +13 -> 1622
    //   1612: aload_0
    //   1613: getfield 1055	sun/net/www/protocol/http/HttpURLConnection:proxyAuthKey	Ljava/lang/String;
    //   1616: invokestatic 1213	sun/net/www/protocol/http/AuthenticationInfo:endAuthRequest	(Ljava/lang/String;)V
    //   1619: goto +17 -> 1636
    //   1622: aload_0
    //   1623: getfield 1056	sun/net/www/protocol/http/HttpURLConnection:serverAuthKey	Ljava/lang/String;
    //   1626: ifnull +10 -> 1636
    //   1629: aload_0
    //   1630: getfield 1056	sun/net/www/protocol/http/HttpURLConnection:serverAuthKey	Ljava/lang/String;
    //   1633: invokestatic 1213	sun/net/www/protocol/http/AuthenticationInfo:endAuthRequest	(Ljava/lang/String;)V
    //   1636: ret 16
    //
    // Exception table:
    //   from	to	target	type
    //   932	975	978	java/lang/Exception
    //   1067	1081	1084	java/lang/Exception
    //   1250	1269	1272	java/lang/IllegalAccessException
    //   1250	1269	1277	java/lang/NoSuchFieldException
    //   175	202	1500	java/lang/RuntimeException
    //   205	1456	1500	java/lang/RuntimeException
    //   1459	1500	1500	java/lang/RuntimeException
    //   175	202	1515	IOException
    //   205	1456	1515	IOException
    //   1459	1500	1515	IOException
    //   175	202	1595	finally
    //   205	1456	1595	finally
    //   1459	1600	1595	finally
  }

  private IOException getChainedException(IOException paramIOException)
  {
    IOException localIOException1;
    try
    {
      localIOException1 = paramIOException;
      Class[] arrayOfClass = new Class[1];
      arrayOfClass[0] = String.class;
      String[] arrayOfString = new String[1];
      arrayOfString[0] = localIOException1.getMessage();
      IOException localIOException2 = (IOException)AccessController.doPrivileged(new PrivilegedExceptionAction(this, localIOException1, arrayOfClass, arrayOfString)
      {
        public Object run()
          throws Exception
        {
          Constructor localConstructor = this.val$originalException.getClass().getConstructor(this.val$cls);
          return ((IOException)localConstructor.newInstance((Object[])this.val$args));
        }
      });
      localIOException2.initCause(localIOException1);
      return localIOException2;
    }
    catch (Exception localException)
    {
    }
    return paramIOException;
  }

  public InputStream getErrorStream()
  {
    if ((this.connected) && (this.responseCode >= 400))
    {
      if (this.errorStream != null)
        return this.errorStream;
      if (this.inputStream != null)
        return this.inputStream;
    }
    return null;
  }

  private AuthenticationInfo resetProxyAuthentication(AuthenticationInfo paramAuthenticationInfo, AuthenticationHeader paramAuthenticationHeader)
  {
    if ((paramAuthenticationInfo != null) && (!(paramAuthenticationInfo instanceof NTLMAuthentication)))
    {
      String str = paramAuthenticationHeader.raw();
      if (paramAuthenticationInfo.isAuthorizationStale(str))
      {
        this.requests.set(paramAuthenticationInfo.getHeaderName(), paramAuthenticationInfo.getHeaderValue(this.url, this.method));
        this.currentProxyCredentials = paramAuthenticationInfo;
        return paramAuthenticationInfo;
      }
      paramAuthenticationInfo.removeFromCache();
    }
    paramAuthenticationInfo = getHttpProxyAuthentication(paramAuthenticationHeader);
    this.currentProxyCredentials = paramAuthenticationInfo;
    return paramAuthenticationInfo;
  }

  public synchronized void doTunneling()
    throws IOException
  {
    int i = 0;
    String str1 = "";
    int j = 0;
    AuthenticationInfo localAuthenticationInfo = null;
    String str2 = null;
    int k = -1;
    MessageHeader localMessageHeader = this.requests;
    this.requests = new MessageHeader();
    try
    {
      do
      {
        if (!(checkReuseConnection()))
          proxiedConnect(this.url, str2, k, false);
        sendCONNECTRequest();
        this.responses.reset();
        this.http.parseHTTP(this.responses, null, this);
        str1 = this.responses.getValue(0);
        StringTokenizer localStringTokenizer = new StringTokenizer(str1);
        localStringTokenizer.nextToken();
        j = Integer.parseInt(localStringTokenizer.nextToken().trim());
        if (j == 407)
        {
          AuthenticationHeader localAuthenticationHeader = new AuthenticationHeader("Proxy-Authenticate", this.responses, this.http.getProxyHostUsed());
          if (!(this.doingNTLMp2ndStage))
          {
            localAuthenticationInfo = resetProxyAuthentication(localAuthenticationInfo, localAuthenticationHeader);
            if (localAuthenticationInfo != null)
            {
              str2 = this.http.getProxyHostUsed();
              k = this.http.getProxyPortUsed();
              disconnectInternal();
              ++i;
            }
          }
          else
          {
            String str3 = this.responses.findValue("Proxy-Authenticate");
            reset();
            if (!(localAuthenticationInfo.setHeaders(this, localAuthenticationHeader.headerParser(), str3)))
            {
              str2 = this.http.getProxyHostUsed();
              k = this.http.getProxyPortUsed();
              disconnectInternal();
              throw new IOException("Authentication failure");
            }
            this.authObj = null;
            this.doingNTLMp2ndStage = false;
          }
        }
        else
        {
          if (localAuthenticationInfo != null)
            localAuthenticationInfo.addToCache();
          if (j == 200)
            break;
          disconnectInternal();
          break;
        }
      }
      while (i < maxRedirects);
      if ((i >= maxRedirects) || (j != 200))
        throw new IOException("Unable to tunnel through proxy. Proxy returns \"" + str1 + "\"");
    }
    finally
    {
      if (this.proxyAuthKey != null)
        AuthenticationInfo.endAuthRequest(this.proxyAuthKey);
    }
    this.requests = localMessageHeader;
    this.responses.reset();
  }

  private void sendCONNECTRequest()
    throws IOException
  {
    int i = this.url.getPort();
    if (this.setRequests)
      this.requests.set(0, null, null);
    this.requests.prepend("CONNECT " + this.url.getHost() + ":" + ((i != -1) ? i : this.url.getDefaultPort()) + " " + "HTTP/1.1", null);
    this.requests.setIfNotSet("User-Agent", userAgent);
    String str = this.url.getHost();
    if ((i != -1) && (i != this.url.getDefaultPort()))
      str = str + ":" + String.valueOf(i);
    this.requests.setIfNotSet("Host", str);
    this.requests.setIfNotSet("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
    setPreemptiveProxyAuthentication(this.requests);
    this.http.writeRequests(this.requests, null);
    this.requests.set(0, null, null);
  }

  private void setPreemptiveProxyAuthentication(MessageHeader paramMessageHeader)
  {
    AuthenticationInfo localAuthenticationInfo = AuthenticationInfo.getProxyAuth(this.http.getProxyHostUsed(), this.http.getProxyPortUsed());
    if ((localAuthenticationInfo != null) && (localAuthenticationInfo.supportsPreemptiveAuthorization()))
    {
      paramMessageHeader.set(localAuthenticationInfo.getHeaderName(), localAuthenticationInfo.getHeaderValue(this.url, this.method));
      this.currentProxyCredentials = localAuthenticationInfo;
    }
  }

  private AuthenticationInfo getHttpProxyAuthentication(AuthenticationHeader paramAuthenticationHeader)
  {
    Object localObject1 = null;
    String str1 = paramAuthenticationHeader.raw();
    String str2 = this.http.getProxyHostUsed();
    int i = this.http.getProxyPortUsed();
    if ((str2 != null) && (paramAuthenticationHeader.isPresent()))
    {
      char c;
      Object localObject2;
      Object localObject3;
      HeaderParser localHeaderParser = paramAuthenticationHeader.headerParser();
      String str3 = localHeaderParser.findValue("realm");
      String str4 = paramAuthenticationHeader.scheme();
      if ("basic".equalsIgnoreCase(str4))
      {
        c = 'B';
      }
      else if ("digest".equalsIgnoreCase(str4))
      {
        c = 'D';
      }
      else if ("ntlm".equalsIgnoreCase(str4))
      {
        c = 'N';
        this.doingNTLMp2ndStage = true;
      }
      else if ("Kerberos".equalsIgnoreCase(str4))
      {
        c = 'K';
        this.doingNTLMp2ndStage = true;
      }
      else if ("Negotiate".equalsIgnoreCase(str4))
      {
        c = 'S';
        this.doingNTLMp2ndStage = true;
      }
      else
      {
        c = ';
      }
      if (str3 == null)
        str3 = "";
      this.proxyAuthKey = AuthenticationInfo.getProxyAuthKey(str2, i, str3, c);
      localObject1 = AuthenticationInfo.getProxyAuth(this.proxyAuthKey);
      if (localObject1 == null)
        if (c == 'B')
        {
          localObject2 = null;
          try
          {
            String str5 = str2;
            localObject2 = (java.net.InetAddress)AccessController.doPrivileged(new PrivilegedExceptionAction(this, str5)
            {
              public Object run()
                throws UnknownHostException
              {
                return java.net.InetAddress.getByName(this.val$finalHost);
              }
            });
          }
          catch (PrivilegedActionException localPrivilegedActionException)
          {
          }
          localObject3 = privilegedRequestPasswordAuthentication(str2, (java.net.InetAddress)localObject2, i, "http", str3, str4, this.url, Authenticator.RequestorType.PROXY);
          if (localObject3 != null)
            localObject1 = new BasicAuthentication(true, str2, i, str3, (PasswordAuthentication)localObject3);
        }
        else if (c == 'D')
        {
          localObject2 = privilegedRequestPasswordAuthentication(str2, null, i, this.url.getProtocol(), str3, str4, this.url, Authenticator.RequestorType.PROXY);
          if (localObject2 != null)
          {
            localObject3 = new DigestAuthentication.Parameters();
            localObject1 = new DigestAuthentication(true, str2, i, str3, str4, (PasswordAuthentication)localObject2, (DigestAuthentication.Parameters)localObject3);
          }
        }
        else if (c == 'N')
        {
          localObject2 = null;
          if (!(this.tryTransparentNTLMProxy))
            localObject2 = privilegedRequestPasswordAuthentication(str2, null, i, this.url.getProtocol(), "", str4, this.url, Authenticator.RequestorType.PROXY);
          if ((this.tryTransparentNTLMProxy) || ((!(this.tryTransparentNTLMProxy)) && (localObject2 != null)))
            localObject1 = new NTLMAuthentication(true, str2, i, (PasswordAuthentication)localObject2);
          this.tryTransparentNTLMProxy = false;
        }
        else if (c == 'S')
        {
          localObject1 = new NegotiateAuthentication(true, str2, i, null, "Negotiate");
        }
        else if (c == 'K')
        {
          localObject1 = new NegotiateAuthentication(true, str2, i, null, "Kerberos");
        }
      if ((localObject1 == null) && (defaultAuth != null) && (defaultAuth.schemeSupported(str4)))
        try
        {
          localObject2 = new URL("http", str2, i, "/");
          localObject3 = defaultAuth.authString((URL)localObject2, str4, str3);
          if (localObject3 != null)
            localObject1 = new BasicAuthentication(true, str2, i, str3, (String)localObject3);
        }
        catch (MalformedURLException localMalformedURLException)
        {
        }
      if ((localObject1 != null) && (!(((AuthenticationInfo)localObject1).setHeaders(this, localHeaderParser, str1))))
        localObject1 = null;
    }
    return ((AuthenticationInfo)(AuthenticationInfo)(AuthenticationInfo)localObject1);
  }

  private AuthenticationInfo getServerAuthentication(AuthenticationHeader paramAuthenticationHeader)
  {
    Object localObject1 = null;
    String str1 = paramAuthenticationHeader.raw();
    if (paramAuthenticationHeader.isPresent())
    {
      char c;
      Object localObject2;
      HeaderParser localHeaderParser = paramAuthenticationHeader.headerParser();
      String str2 = localHeaderParser.findValue("realm");
      String str3 = paramAuthenticationHeader.scheme();
      if ("basic".equalsIgnoreCase(str3))
      {
        c = 'B';
      }
      else if ("digest".equalsIgnoreCase(str3))
      {
        c = 'D';
      }
      else if ("ntlm".equalsIgnoreCase(str3))
      {
        c = 'N';
        this.doingNTLM2ndStage = true;
      }
      else if ("Kerberos".equalsIgnoreCase(str3))
      {
        c = 'K';
        this.doingNTLM2ndStage = true;
      }
      else if ("Negotiate".equalsIgnoreCase(str3))
      {
        c = 'S';
        this.doingNTLM2ndStage = true;
      }
      else
      {
        c = ';
      }
      this.domain = localHeaderParser.findValue("domain");
      if (str2 == null)
        str2 = "";
      this.serverAuthKey = AuthenticationInfo.getServerAuthKey(this.url, str2, c);
      localObject1 = AuthenticationInfo.getServerAuth(this.serverAuthKey);
      java.net.InetAddress localInetAddress = null;
      if (localObject1 == null)
        try
        {
          localInetAddress = java.net.InetAddress.getByName(this.url.getHost());
        }
        catch (UnknownHostException localUnknownHostException)
        {
        }
      int i = this.url.getPort();
      if (i == -1)
        i = this.url.getDefaultPort();
      if (localObject1 == null)
      {
        if (c == 'K')
        {
          try
          {
            localObject2 = new URL(this.url, "/");
          }
          catch (Exception localException1)
          {
            localObject2 = this.url;
          }
          localObject1 = new NegotiateAuthentication(false, (URL)localObject2, null, "Kerberos");
        }
        if (c == 'S')
        {
          try
          {
            localObject2 = new URL(this.url, "/");
          }
          catch (Exception localException2)
          {
            localObject2 = this.url;
          }
          localObject1 = new NegotiateAuthentication(false, (URL)localObject2, null, "Negotiate");
        }
        if (c == 'B')
        {
          localObject2 = privilegedRequestPasswordAuthentication(this.url.getHost(), localInetAddress, i, this.url.getProtocol(), str2, str3, this.url, Authenticator.RequestorType.SERVER);
          if (localObject2 != null)
            localObject1 = new BasicAuthentication(false, this.url, str2, (PasswordAuthentication)localObject2);
        }
        if (c == 'D')
        {
          localObject2 = privilegedRequestPasswordAuthentication(this.url.getHost(), localInetAddress, i, this.url.getProtocol(), str2, str3, this.url, Authenticator.RequestorType.SERVER);
          if (localObject2 != null)
          {
            this.digestparams = new DigestAuthentication.Parameters();
            localObject1 = new DigestAuthentication(false, this.url, str2, str3, (PasswordAuthentication)localObject2, this.digestparams);
          }
        }
        if (c == 'N')
        {
          try
          {
            localObject2 = new URL(this.url, "/");
          }
          catch (Exception localException3)
          {
            localObject2 = this.url;
          }
          PasswordAuthentication localPasswordAuthentication = null;
          if (!(this.tryTransparentNTLMServer))
            localPasswordAuthentication = privilegedRequestPasswordAuthentication(this.url.getHost(), localInetAddress, i, this.url.getProtocol(), "", str3, this.url, Authenticator.RequestorType.SERVER);
          if ((this.tryTransparentNTLMServer) || ((!(this.tryTransparentNTLMServer)) && (localPasswordAuthentication != null)))
            localObject1 = new NTLMAuthentication(false, (URL)localObject2, localPasswordAuthentication);
          this.tryTransparentNTLMServer = false;
        }
      }
      if ((localObject1 == null) && (defaultAuth != null) && (defaultAuth.schemeSupported(str3)))
      {
        localObject2 = defaultAuth.authString(this.url, str3, str2);
        if (localObject2 != null)
          localObject1 = new BasicAuthentication(false, this.url, str2, (String)localObject2);
      }
      if ((localObject1 != null) && (!(((AuthenticationInfo)localObject1).setHeaders(this, localHeaderParser, str1))))
        localObject1 = null;
    }
    return ((AuthenticationInfo)(AuthenticationInfo)localObject1);
  }

  private void checkResponseCredentials(boolean paramBoolean)
    throws IOException
  {
    try
    {
      String str;
      if (!(this.needToCheck))
        return;
      if ((validateProxy) && (this.currentProxyCredentials != null))
      {
        str = this.responses.findValue("Proxy-Authentication-Info");
        if ((paramBoolean) || (str != null))
        {
          this.currentProxyCredentials.checkResponse(str, this.method, this.url);
          this.currentProxyCredentials = null;
        }
      }
      if ((validateServer) && (this.currentServerCredentials != null))
      {
        str = this.responses.findValue("Authentication-Info");
        if ((paramBoolean) || (str != null))
        {
          this.currentServerCredentials.checkResponse(str, this.method, this.url);
          this.currentServerCredentials = null;
        }
      }
      if ((this.currentServerCredentials == null) && (this.currentProxyCredentials == null))
        this.needToCheck = false;
    }
    catch (IOException localIOException)
    {
      disconnectInternal();
      this.connected = false;
      throw localIOException;
    }
  }

  private boolean followRedirect()
    throws IOException
  {
    URL localURL;
    if (!(getInstanceFollowRedirects()))
      return false;
    int i = getResponseCode();
    if ((i < 300) || (i > 307) || (i == 306) || (i == 304))
      return false;
    String str1 = getHeaderField("Location");
    if (str1 == null)
      return false;
    try
    {
      localURL = new URL(str1);
      if (!(this.url.getProtocol().equalsIgnoreCase(localURL.getProtocol())))
        return false;
    }
    catch (MalformedURLException localMalformedURLException)
    {
      localURL = new URL(this.url, str1);
    }
    disconnectInternal();
    if (streaming())
      throw new HttpRetryException("cannot retry due to redirection, in streaming mode", i, str1);
    this.responses = new MessageHeader();
    if (i == 305)
    {
      String str2 = localURL.getHost();
      int k = localURL.getPort();
      SecurityManager localSecurityManager = System.getSecurityManager();
      if (localSecurityManager != null)
        localSecurityManager.checkConnect(str2, k);
      setProxiedClient(this.url, str2, k);
      this.requests.set(0, this.method + " " + this.http.getURLFile() + " " + "HTTP/1.1", null);
      this.connected = true;
    }
    else
    {
      this.url = localURL;
      if ((this.method.equals("POST")) && (!(Boolean.getBoolean("http.strictPostRedirect"))) && (i != 307))
      {
        this.requests = new MessageHeader();
        this.setRequests = false;
        setRequestMethod("GET");
        this.poster = null;
        if (!(checkReuseConnection()))
          connect();
      }
      else
      {
        if (!(checkReuseConnection()))
          connect();
        if (this.http != null)
        {
          this.requests.set(0, this.method + " " + this.http.getURLFile() + " " + "HTTP/1.1", null);
          int j = this.url.getPort();
          String str3 = this.url.getHost();
          if ((j != -1) && (j != this.url.getDefaultPort()))
            str3 = str3 + ":" + String.valueOf(j);
          this.requests.set("Host", str3);
        }
      }
    }
    return true;
  }

  private void reset()
    throws IOException
  {
    this.http.reuse = true;
    this.reuseClient = this.http;
    InputStream localInputStream = this.http.getInputStream();
    if (!(this.method.equals("HEAD")))
    {
      try
      {
        if ((localInputStream instanceof ChunkedInputStream) || (localInputStream instanceof MeteredStream))
          while (true)
            if (localInputStream.read(this.cdata) <= 0)
              break label122;
        int i = 0;
        int j = 0;
        try
        {
          i = Integer.parseInt(this.responses.findValue("Content-Length"));
        }
        catch (Exception localException)
        {
        }
        int k = 0;
        while (k < i)
        {
          if ((j = localInputStream.read(this.cdata)) == -1)
            break;
          label122: k += j;
        }
      }
      catch (IOException localIOException1)
      {
        this.http.reuse = false;
        this.reuseClient = null;
        disconnectInternal();
        return;
      }
      try
      {
        if (localInputStream instanceof MeteredStream)
          localInputStream.close();
      }
      catch (IOException localIOException2)
      {
      }
    }
    this.responseCode = -1;
    this.responses = new MessageHeader();
    this.connected = false;
  }

  private void disconnectInternal()
  {
    this.responseCode = -1;
    if (this.pi != null)
    {
      this.pi.finishTracking();
      this.pi = null;
    }
    if (this.http != null)
    {
      this.http.closeServer();
      this.http = null;
      this.connected = false;
    }
  }

  public void disconnect()
  {
    this.responseCode = -1;
    if (this.pi != null)
    {
      this.pi.finishTracking();
      this.pi = null;
    }
    if (this.http != null)
    {
      if (this.inputStream != null)
      {
        HttpClient localHttpClient = this.http;
        boolean bool = localHttpClient.isKeepingAlive();
        try
        {
          this.inputStream.close();
        }
        catch (IOException localIOException)
        {
        }
        if (bool)
          localHttpClient.closeIdleConnection();
      }
      else
      {
        this.http.setDoNotRetry(true);
        this.http.closeServer();
      }
      this.http = null;
      this.connected = false;
    }
    this.cachedInputStream = null;
    if (this.cachedHeaders != null)
      this.cachedHeaders.reset();
  }

  public boolean usingProxy()
  {
    if (this.http != null)
      return (this.http.getProxyHostUsed() != null);
    return false;
  }

  public String getHeaderField(String paramString)
  {
    try
    {
      getInputStream();
    }
    catch (IOException localIOException)
    {
    }
    if (this.cachedHeaders != null)
      return this.cachedHeaders.findValue(paramString);
    return this.responses.findValue(paramString);
  }

  public Map getHeaderFields()
  {
    try
    {
      getInputStream();
    }
    catch (IOException localIOException)
    {
    }
    if (this.cachedHeaders != null)
      return this.cachedHeaders.getHeaders();
    return this.responses.getHeaders();
  }

  public String getHeaderField(int paramInt)
  {
    try
    {
      getInputStream();
    }
    catch (IOException localIOException)
    {
    }
    if (this.cachedHeaders != null)
      return this.cachedHeaders.getValue(paramInt);
    return this.responses.getValue(paramInt);
  }

  public String getHeaderFieldKey(int paramInt)
  {
    try
    {
      getInputStream();
    }
    catch (IOException localIOException)
    {
    }
    if (this.cachedHeaders != null)
      return this.cachedHeaders.getKey(paramInt);
    return this.responses.getKey(paramInt);
  }

  public void setRequestProperty(String paramString1, String paramString2)
  {
    if (this.connected)
      throw new IllegalStateException("Already connected");
    if (paramString1 == null)
      throw new NullPointerException("key is null");
    checkMessageHeader(paramString1, paramString2);
    this.requests.set(paramString1, paramString2);
  }

  public void addRequestProperty(String paramString1, String paramString2)
  {
    if (this.connected)
      throw new IllegalStateException("Already connected");
    if (paramString1 == null)
      throw new NullPointerException("key is null");
    checkMessageHeader(paramString1, paramString2);
    this.requests.add(paramString1, paramString2);
  }

  void setAuthenticationProperty(String paramString1, String paramString2)
  {
    checkMessageHeader(paramString1, paramString2);
    this.requests.set(paramString1, paramString2);
  }

  public String getRequestProperty(String paramString)
  {
    if (paramString != null)
      for (int i = 0; i < EXCLUDE_HEADERS.length; ++i)
        if (paramString.equalsIgnoreCase(EXCLUDE_HEADERS[i]))
          return null;
    return this.requests.findValue(paramString);
  }

  public Map getRequestProperties()
  {
    if (this.connected)
      throw new IllegalStateException("Already connected");
    return this.requests.getHeaders(EXCLUDE_HEADERS);
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

  protected void finalize()
  {
  }

  String getMethod()
  {
    return this.method;
  }

  private MessageHeader mapToMessageHeader(Map paramMap)
  {
    MessageHeader localMessageHeader = new MessageHeader();
    if ((paramMap == null) || (paramMap.isEmpty()))
      return localMessageHeader;
    Set localSet = paramMap.entrySet();
    Iterator localIterator1 = localSet.iterator();
    while (localIterator1.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator1.next();
      String str1 = (String)localEntry.getKey();
      List localList = (List)localEntry.getValue();
      Iterator localIterator2 = localList.iterator();
      while (localIterator2.hasNext())
      {
        String str2 = (String)localIterator2.next();
        if (str1 == null)
          localMessageHeader.prepend(str1, str2);
        else
          localMessageHeader.add(str1, str2);
      }
    }
    return localMessageHeader;
  }

  static
  {
    maxRedirects = ((Integer)AccessController.doPrivileged(new GetIntegerAction("http.maxRedirects", 20))).intValue();
    version = (String)AccessController.doPrivileged(new GetPropertyAction("java.version"));
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("http.agent"));
    if (str == null)
      str = "Java/" + version;
    else
      str = str + " Java/" + version;
    userAgent = str;
    validateProxy = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("http.auth.digest.validateProxy"))).booleanValue();
    validateServer = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("http.auth.digest.validateServer"))).booleanValue();
    enableESBuffer = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.net.http.errorstream.enableBuffering"))).booleanValue();
    timeout4ESBuffer = ((Integer)AccessController.doPrivileged(new GetIntegerAction("sun.net.http.errorstream.timeout", 300))).intValue();
    if (timeout4ESBuffer <= 0)
      timeout4ESBuffer = 300;
    bufSize4ES = ((Integer)AccessController.doPrivileged(new GetIntegerAction("sun.net.http.errorstream.bufferSize", 4096))).intValue();
    if (bufSize4ES <= 0)
      bufSize4ES = 4096;
    EXCLUDE_HEADERS = { "Proxy-Authorization", "Authorization" };
  }

  static class ErrorStream extends InputStream
  {
    ByteBuffer buffer;
    InputStream is;

    private ErrorStream(ByteBuffer paramByteBuffer)
    {
      this.buffer = paramByteBuffer;
      this.is = null;
    }

    private ErrorStream(ByteBuffer paramByteBuffer, InputStream paramInputStream)
    {
      this.buffer = paramByteBuffer;
      this.is = paramInputStream;
    }

    public static InputStream getErrorStream(InputStream paramInputStream, int paramInt, HttpClient paramHttpClient)
    {
      if (paramInt == 0)
        return null;
      try
      {
        int i = paramHttpClient.setTimeout(HttpURLConnection.access$100() / 5);
        int j = 0;
        int k = 0;
        if (paramInt < 0)
        {
          j = HttpURLConnection.access$200();
          k = 1;
        }
        else
        {
          j = paramInt;
        }
        if (j <= HttpURLConnection.access$200())
        {
          byte[] arrayOfByte = new byte[j];
          int l = 0;
          int i1 = 0;
          int i2 = 0;
          do
            try
            {
              i2 = paramInputStream.read(arrayOfByte, l, arrayOfByte.length - l);
              if (i2 < 0)
              {
                if (k != 0)
                  break label161:
                throw new IOException("the server closes before sending " + paramInt + " bytes of data");
              }
              l += i2;
            }
            catch (SocketTimeoutException localSocketTimeoutException)
            {
              i1 += HttpURLConnection.access$100() / 5;
            }
          while ((l < j) && (i1 < HttpURLConnection.access$100()));
          label161: paramHttpClient.setTimeout(i);
          if (l == 0)
            return null;
          if (((l == j) && (k == 0)) || ((k != 0) && (i2 < 0)))
          {
            paramInputStream.close();
            return new ErrorStream(ByteBuffer.wrap(arrayOfByte, 0, l));
          }
          return new ErrorStream(ByteBuffer.wrap(arrayOfByte, 0, l), paramInputStream);
        }
        return null;
      }
      catch (IOException localIOException)
      {
      }
      return null;
    }

    public int available()
      throws IOException
    {
      if (this.is == null)
        return this.buffer.remaining();
      return (this.buffer.remaining() + this.is.available());
    }

    public int read()
      throws IOException
    {
      byte[] arrayOfByte = new byte[1];
      int i = read(arrayOfByte);
      return ((i == -1) ? i : arrayOfByte[0] & 0xFF);
    }

    public int read(byte[] paramArrayOfByte)
      throws IOException
    {
      return read(paramArrayOfByte, 0, paramArrayOfByte.length);
    }

    public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
      throws IOException
    {
      int i = this.buffer.remaining();
      if (i > 0)
      {
        int j = (i < paramInt2) ? i : paramInt2;
        this.buffer.get(paramArrayOfByte, paramInt1, j);
        return j;
      }
      if (this.is == null)
        return -1;
      return this.is.read(paramArrayOfByte, paramInt1, paramInt2);
    }

    public void close()
      throws IOException
    {
      this.buffer = null;
      if (this.is != null)
        this.is.close();
    }
  }

  class HttpInputStream extends FilterInputStream
  {
    private CacheRequest cacheRequest;
    private OutputStream outputStream;
    private boolean marked = false;
    private int inCache = 0;
    private int markCount = 0;
    private byte[] skipBuffer;
    private static final int SKIP_BUFFER_SIZE = 8096;

    public HttpInputStream(, InputStream paramInputStream)
    {
      super(paramInputStream);
      this.cacheRequest = null;
      this.outputStream = null;
    }

    public HttpInputStream(, InputStream paramInputStream, CacheRequest paramCacheRequest)
    {
      super(paramInputStream);
      this.cacheRequest = paramCacheRequest;
      try
      {
        this.outputStream = paramCacheRequest.getBody();
      }
      catch (IOException localIOException)
      {
        this.cacheRequest.abort();
        this.cacheRequest = null;
        this.outputStream = null;
      }
    }

    public synchronized void mark()
    {
      super.mark(paramInt);
      if (this.cacheRequest != null)
      {
        this.marked = true;
        this.markCount = 0;
      }
    }

    public synchronized void reset()
      throws IOException
    {
      super.reset();
      if (this.cacheRequest != null)
      {
        this.marked = false;
        this.inCache += this.markCount;
      }
    }

    public int read()
      throws IOException
    {
      byte[] arrayOfByte;
      try
      {
        arrayOfByte = new byte[1];
        int i = read(arrayOfByte);
        return ((i == -1) ? i : arrayOfByte[0] & 0xFF);
      }
      catch (IOException localIOException)
      {
        if (this.cacheRequest != null)
          this.cacheRequest.abort();
        throw localIOException;
      }
    }

    public int read()
      throws IOException
    {
      return read(paramArrayOfByte, 0, paramArrayOfByte.length);
    }

    public int read(, int paramInt1, int paramInt2)
      throws IOException
    {
      int i;
      try
      {
        int j;
        i = super.read(paramArrayOfByte, paramInt1, paramInt2);
        if (this.inCache > 0)
          if (this.inCache >= i)
          {
            this.inCache -= i;
            j = 0;
          }
          else
          {
            j = i - this.inCache;
            this.inCache = 0;
          }
        else
          j = i;
        if ((j > 0) && (this.outputStream != null))
          this.outputStream.write(paramArrayOfByte, paramInt1 + i - j, j);
        if (this.marked)
          this.markCount += i;
        return i;
      }
      catch (IOException localIOException)
      {
        if (this.cacheRequest != null)
          this.cacheRequest.abort();
        throw localIOException;
      }
    }

    public long skip()
      throws IOException
    {
      long l = paramLong;
      if (this.skipBuffer == null)
        this.skipBuffer = new byte[8096];
      byte[] arrayOfByte = this.skipBuffer;
      if (paramLong <= 3412047170994438144L)
        return 3412047961268420608L;
      while (l > 3412047188174307328L)
      {
        int i = read(arrayOfByte, 0, (int)Math.min(8096L, l));
        if (i < 0)
          break;
        l -= i;
      }
      return (paramLong - l);
    }

    public void close()
      throws IOException
    {
      try
      {
        if (this.outputStream != null)
          if (read() != -1)
            this.cacheRequest.abort();
          else
            this.outputStream.close();
        super.close();
      }
      catch (IOException localIOException)
      {
        if (this.cacheRequest != null);
        throw localIOException;
      }
      finally
      {
        this.this$0.http = null;
        HttpURLConnection.access$000(this.this$0, true);
      }
    }
  }

  class StreamingOutputStream extends FilterOutputStream
  {
    int expected;
    int written;
    boolean closed;
    boolean error;
    IOException errorExcp;

    StreamingOutputStream(, OutputStream paramOutputStream, int paramInt)
    {
      super(paramOutputStream);
      this.expected = paramInt;
      this.written = 0;
      this.closed = false;
      this.error = false;
    }

    public void write()
      throws IOException
    {
      checkError();
      this.written += 1;
      if ((this.expected != -1) && (this.written > this.expected))
        throw new IOException("too many bytes written");
      this.out.write(paramInt);
    }

    public void write()
      throws IOException
    {
      write(paramArrayOfByte, 0, paramArrayOfByte.length);
    }

    public void write(, int paramInt1, int paramInt2)
      throws IOException
    {
      checkError();
      this.written += paramInt2;
      if ((this.expected != -1) && (this.written > this.expected))
      {
        this.out.close();
        throw new IOException("too many bytes written");
      }
      this.out.write(paramArrayOfByte, paramInt1, paramInt2);
    }

    void checkError()
      throws IOException
    {
      if (this.closed)
        throw new IOException("Stream is closed");
      if (this.error)
        throw this.errorExcp;
      if (((PrintStream)this.out).checkError())
        throw new IOException("Error writing request body to server");
    }

    boolean writtenOK()
    {
      return ((this.closed) && (!(this.error)));
    }

    public void close()
      throws IOException
    {
      if (this.closed)
        return;
      this.closed = true;
      if (this.expected != -1)
      {
        if (this.written != this.expected)
        {
          this.error = true;
          this.errorExcp = new IOException("insufficient data written");
          this.out.close();
          throw this.errorExcp;
        }
        super.flush();
      }
      else
      {
        super.close();
        OutputStream localOutputStream = this.this$0.http.getOutputStream();
        localOutputStream.write(13);
        localOutputStream.write(10);
        localOutputStream.flush();
      }
    }
  }
}