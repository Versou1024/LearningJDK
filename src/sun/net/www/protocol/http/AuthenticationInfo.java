package sun.net.www.protocol.http;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.AccessController;
import java.util.HashMap;
import sun.net.www.HeaderParser;
import sun.security.action.GetBooleanAction;

abstract class AuthenticationInfo extends AuthCacheValue
  implements Cloneable
{
  static final char SERVER_AUTHENTICATION = 115;
  static final char PROXY_AUTHENTICATION = 112;
  static boolean serializeAuth;
  protected transient PasswordAuthentication pw;
  private static HashMap<String, Thread> requests;
  char type;
  char authType;
  String protocol;
  String host;
  int port;
  String realm;
  String path;
  String s1;
  String s2;

  public PasswordAuthentication credentials()
  {
    return this.pw;
  }

  public AuthCacheValue.Type getAuthType()
  {
    return ((this.type == 's') ? AuthCacheValue.Type.Server : AuthCacheValue.Type.Proxy);
  }

  public String getHost()
  {
    return this.host;
  }

  public int getPort()
  {
    return this.port;
  }

  public String getRealm()
  {
    return this.realm;
  }

  public String getPath()
  {
    return this.path;
  }

  public String getProtocolScheme()
  {
    return this.protocol;
  }

  private static boolean requestIsInProgress(String paramString)
  {
    HashMap localHashMap;
    if (!(serializeAuth))
      return false;
    monitorenter;
    try
    {
      Thread localThread1;
      Thread localThread2 = Thread.currentThread();
      if ((localThread1 = (Thread)requests.get(paramString)) == null)
      {
        requests.put(paramString, localThread2);
        return false;
      }
      if (localThread1 == localThread2)
        monitorexit;
      label55: if (requests.containsKey(paramString));
    }
    finally
    {
      try
      {
        requests.wait();
      }
      catch (InterruptedException localInterruptedException)
      {
        break label55:
        monitorexit;
        break label91:
        localObject = finally;
        monitorexit;
        throw localObject;
      }
    }
    label91: return true;
  }

  private static void requestCompleted(String paramString)
  {
    synchronized (requests)
    {
      Thread localThread = (Thread)requests.get(paramString);
      if ((localThread != null) && (localThread == Thread.currentThread()))
      {
        int i = (requests.remove(paramString) != null) ? 1 : 0;
        if ((!($assertionsDisabled)) && (i == 0))
          throw new AssertionError();
      }
      requests.notifyAll();
    }
  }

  AuthenticationInfo(char paramChar1, char paramChar2, String paramString1, int paramInt, String paramString2)
  {
    this.type = paramChar1;
    this.authType = paramChar2;
    this.protocol = "";
    this.host = paramString1.toLowerCase();
    this.port = paramInt;
    this.realm = paramString2;
    this.path = null;
  }

  public Object clone()
  {
    try
    {
      return clone();
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
    }
    return null;
  }

  AuthenticationInfo(char paramChar1, char paramChar2, URL paramURL, String paramString)
  {
    this.type = paramChar1;
    this.authType = paramChar2;
    this.protocol = paramURL.getProtocol().toLowerCase();
    this.host = paramURL.getHost().toLowerCase();
    this.port = paramURL.getPort();
    if (this.port == -1)
      this.port = paramURL.getDefaultPort();
    this.realm = paramString;
    String str = paramURL.getPath();
    if (str.length() == 0)
      this.path = str;
    else
      this.path = reducePath(str);
  }

  static String reducePath(String paramString)
  {
    int i = paramString.lastIndexOf(47);
    int j = paramString.lastIndexOf(46);
    if (i != -1)
    {
      if (i < j)
        return paramString.substring(0, i + 1);
      return paramString;
    }
    return paramString;
  }

  static AuthenticationInfo getServerAuth(URL paramURL)
  {
    int i = paramURL.getPort();
    if (i == -1)
      i = paramURL.getDefaultPort();
    String str = "s:" + paramURL.getProtocol().toLowerCase() + ":" + paramURL.getHost().toLowerCase() + ":" + i;
    return getAuth(str, paramURL);
  }

  static String getServerAuthKey(URL paramURL, String paramString, char paramChar)
  {
    int i = paramURL.getPort();
    if (i == -1)
      i = paramURL.getDefaultPort();
    String str = "s:" + paramChar + ":" + paramURL.getProtocol().toLowerCase() + ":" + paramURL.getHost().toLowerCase() + ":" + i + ":" + paramString;
    return str;
  }

  static AuthenticationInfo getServerAuth(String paramString)
  {
    AuthenticationInfo localAuthenticationInfo = getAuth(paramString, null);
    if ((localAuthenticationInfo == null) && (requestIsInProgress(paramString)))
      localAuthenticationInfo = getAuth(paramString, null);
    return localAuthenticationInfo;
  }

  static AuthenticationInfo getAuth(String paramString, URL paramURL)
  {
    if (paramURL == null)
      return ((AuthenticationInfo)cache.get(paramString, null));
    return ((AuthenticationInfo)cache.get(paramString, paramURL.getPath()));
  }

  static AuthenticationInfo getProxyAuth(String paramString, int paramInt)
  {
    String str = "p::" + paramString.toLowerCase() + ":" + paramInt;
    AuthenticationInfo localAuthenticationInfo = (AuthenticationInfo)cache.get(str, null);
    return localAuthenticationInfo;
  }

  static String getProxyAuthKey(String paramString1, int paramInt, String paramString2, char paramChar)
  {
    String str = "p:" + paramChar + "::" + paramString1.toLowerCase() + ":" + paramInt + ":" + paramString2;
    return str;
  }

  static AuthenticationInfo getProxyAuth(String paramString)
  {
    AuthenticationInfo localAuthenticationInfo = (AuthenticationInfo)cache.get(paramString, null);
    if ((localAuthenticationInfo == null) && (requestIsInProgress(paramString)))
      localAuthenticationInfo = (AuthenticationInfo)cache.get(paramString, null);
    return localAuthenticationInfo;
  }

  void addToCache()
  {
    String str = cacheKey(true);
    cache.put(str, this);
    if (supportsPreemptiveAuthorization())
      cache.put(cacheKey(false), this);
    endAuthRequest(str);
  }

  static void endAuthRequest(String paramString)
  {
    if (!(serializeAuth))
      return;
    synchronized (requests)
    {
      requestCompleted(paramString);
    }
  }

  void removeFromCache()
  {
    cache.remove(cacheKey(true), this);
    if (supportsPreemptiveAuthorization())
      cache.remove(cacheKey(false), this);
  }

  abstract boolean supportsPreemptiveAuthorization();

  abstract String getHeaderName();

  abstract String getHeaderValue(URL paramURL, String paramString);

  abstract boolean setHeaders(HttpURLConnection paramHttpURLConnection, HeaderParser paramHeaderParser, String paramString);

  abstract boolean isAuthorizationStale(String paramString);

  abstract void checkResponse(String paramString1, String paramString2, URL paramURL)
    throws IOException;

  String cacheKey(boolean paramBoolean)
  {
    if (paramBoolean)
      return this.type + ":" + this.authType + ":" + this.protocol + ":" + this.host + ":" + this.port + ":" + this.realm;
    return this.type + ":" + this.protocol + ":" + this.host + ":" + this.port;
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    this.pw = new PasswordAuthentication(this.s1, this.s2.toCharArray());
    this.s1 = null;
    this.s2 = null;
  }

  private synchronized void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    this.s1 = this.pw.getUserName();
    this.s2 = new String(this.pw.getPassword());
    paramObjectOutputStream.defaultWriteObject();
  }

  static
  {
    serializeAuth = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("http.auth.serializeRequests"))).booleanValue();
    requests = new HashMap();
  }
}