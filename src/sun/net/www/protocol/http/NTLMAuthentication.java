package sun.net.www.protocol.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.net.www.HeaderParser;

class NTLMAuthentication extends AuthenticationInfo
{
  private static final long serialVersionUID = 100L;
  static final char NTLM_AUTH = 78;
  private String hostname;
  private static String defaultDomain = (String)AccessController.doPrivileged(new PrivilegedAction()
  {
    public Object run()
    {
      String str = System.getProperty("http.auth.ntlm.domain");
      if (str == null)
        return "domain";
      return str;
    }
  });
  String username;
  String ntdomain;
  String password;

  private void init0()
  {
    this.hostname = ((String)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        String str;
        try
        {
          str = InetAddress.getLocalHost().getHostName().toUpperCase();
        }
        catch (UnknownHostException localUnknownHostException)
        {
          str = "localhost";
        }
        return str;
      }
    }));
    int i = this.hostname.indexOf(46);
    if (i != -1)
      this.hostname = this.hostname.substring(0, i);
  }

  public NTLMAuthentication(boolean paramBoolean, URL paramURL, PasswordAuthentication paramPasswordAuthentication)
  {
    super((paramBoolean) ? 112 : 's', 'N', paramURL, "");
    init(paramPasswordAuthentication);
  }

  private void init(PasswordAuthentication paramPasswordAuthentication)
  {
    this.pw = paramPasswordAuthentication;
    if (paramPasswordAuthentication != null)
    {
      String str = paramPasswordAuthentication.getUserName();
      int i = str.indexOf(92);
      if (i == -1)
      {
        this.username = str;
        this.ntdomain = defaultDomain;
      }
      else
      {
        this.ntdomain = str.substring(0, i).toUpperCase();
        this.username = str.substring(i + 1);
      }
      this.password = new String(paramPasswordAuthentication.getPassword());
    }
    else
    {
      this.username = null;
      this.ntdomain = null;
      this.password = null;
    }
    init0();
  }

  public NTLMAuthentication(boolean paramBoolean, String paramString, int paramInt, PasswordAuthentication paramPasswordAuthentication)
  {
    super((paramBoolean) ? 112 : 's', 'N', paramString, paramInt, "");
    init(paramPasswordAuthentication);
  }

  boolean supportsPreemptiveAuthorization()
  {
    return false;
  }

  static boolean supportsTransparentAuth()
  {
    return true;
  }

  String getHeaderName()
  {
    if (this.type == 's')
      return "Authorization";
    return "Proxy-authorization";
  }

  String getHeaderValue(URL paramURL, String paramString)
  {
    throw new RuntimeException("getHeaderValue not supported");
  }

  boolean isAuthorizationStale(String paramString)
  {
    return false;
  }

  synchronized boolean setHeaders(HttpURLConnection paramHttpURLConnection, HeaderParser paramHeaderParser, String paramString)
  {
    NTLMAuthSequence localNTLMAuthSequence;
    try
    {
      localNTLMAuthSequence = (NTLMAuthSequence)paramHttpURLConnection.authObj;
      if (localNTLMAuthSequence == null)
      {
        localNTLMAuthSequence = new NTLMAuthSequence(this.username, this.password, this.ntdomain);
        paramHttpURLConnection.authObj = localNTLMAuthSequence;
      }
      String str = "NTLM " + localNTLMAuthSequence.getAuthHeader((paramString.length() > 6) ? paramString.substring(5) : null);
      paramHttpURLConnection.setAuthenticationProperty(getHeaderName(), str);
      return true;
    }
    catch (IOException localIOException)
    {
    }
    return false;
  }

  public void checkResponse(String paramString1, String paramString2, URL paramURL)
    throws IOException
  {
  }
}