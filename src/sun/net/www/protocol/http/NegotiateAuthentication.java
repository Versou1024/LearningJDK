package sun.net.www.protocol.http;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.net.www.HeaderParser;

class NegotiateAuthentication extends AuthenticationInfo
{
  private static final long serialVersionUID = 100L;
  private String scheme = null;
  static final char NEGOTIATE_AUTH = 83;
  static final char KERBEROS_AUTH = 75;
  static HashMap<String, Boolean> supported = null;
  static HashMap<String, Negotiator> cache = null;
  private Negotiator negotiator = null;

  public NegotiateAuthentication(boolean paramBoolean, URL paramURL, PasswordAuthentication paramPasswordAuthentication, String paramString)
  {
    super((paramBoolean) ? 112 : 's', 'S', paramURL, "");
    this.scheme = paramString;
  }

  public NegotiateAuthentication(boolean paramBoolean, String paramString1, int paramInt, PasswordAuthentication paramPasswordAuthentication, String paramString2)
  {
    super((paramBoolean) ? 112 : 's', 'S', paramString1, paramInt, "");
    this.scheme = paramString2;
  }

  boolean supportsPreemptiveAuthorization()
  {
    return false;
  }

  public static synchronized boolean isSupported(String paramString1, String paramString2)
  {
    if (supported == null)
    {
      supported = new HashMap();
      cache = new HashMap();
    }
    paramString1 = paramString1.toLowerCase();
    if (supported.containsKey(paramString1))
      return ((Boolean)supported.get(paramString1)).booleanValue();
    try
    {
      Negotiator localNegotiator = Negotiator.getSupported(paramString1, paramString2);
      supported.put(paramString1, Boolean.valueOf(true));
      cache.put(paramString1, localNegotiator);
      return true;
    }
    catch (Exception localException)
    {
      supported.put(paramString1, Boolean.valueOf(false));
    }
    return false;
  }

  String getHeaderName()
  {
    if (this.type == 's')
      return "Authorization";
    return "Proxy-Authorization";
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
    byte[] arrayOfByte;
    try
    {
      arrayOfByte = null;
      String[] arrayOfString = paramString.split("\\s+");
      if (arrayOfString.length > 1)
        arrayOfByte = new BASE64Decoder().decodeBuffer(arrayOfString[1]);
      String str = this.scheme + " " + new B64Encoder(this).encode((arrayOfByte == null) ? firstToken() : nextToken(arrayOfByte));
      paramHttpURLConnection.setAuthenticationProperty(getHeaderName(), str);
      return true;
    }
    catch (IOException localIOException)
    {
    }
    return false;
  }

  private byte[] firstToken()
    throws IOException
  {
    IOException localIOException;
    if (cache != null)
      synchronized (cache)
      {
        this.negotiator = ((Negotiator)cache.get(getHost()));
        if (this.negotiator != null)
          cache.remove(getHost());
      }
    if (this.negotiator == null)
      try
      {
        this.negotiator = Negotiator.getSupported(getHost(), this.scheme);
      }
      catch (Exception localException1)
      {
        localIOException = new IOException("Cannot initialize Negotiator");
        localIOException.initCause(localException1);
        throw localIOException;
      }
    try
    {
      return this.negotiator.firstToken();
    }
    catch (Exception localException2)
    {
      localIOException = new IOException("firstToken fails");
      localIOException.initCause(localException2);
      throw localIOException;
    }
  }

  private byte[] nextToken(byte[] paramArrayOfByte)
    throws IOException
  {
    try
    {
      return this.negotiator.nextToken(paramArrayOfByte);
    }
    catch (Exception localException)
    {
      IOException localIOException = new IOException("nextToken fails");
      localIOException.initCause(localException);
      throw localIOException;
    }
  }

  public void checkResponse(String paramString1, String paramString2, URL paramURL)
    throws IOException
  {
  }

  class B64Encoder extends BASE64Encoder
  {
    protected int bytesPerLine()
    {
      return 100000;
    }
  }
}