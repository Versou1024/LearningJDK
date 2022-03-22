package sun.net.www.protocol.http;

import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import sun.misc.BASE64Encoder;
import sun.net.www.HeaderParser;

class BasicAuthentication extends AuthenticationInfo
{
  private static final long serialVersionUID = 100L;
  static final char BASIC_AUTH = 66;
  String auth;

  public BasicAuthentication(boolean paramBoolean, String paramString1, int paramInt, String paramString2, PasswordAuthentication paramPasswordAuthentication)
  {
    super((paramBoolean) ? 112 : 's', 'B', paramString1, paramInt, paramString2);
    String str = paramPasswordAuthentication.getUserName() + ":";
    byte[] arrayOfByte1 = null;
    try
    {
      arrayOfByte1 = str.getBytes("ISO-8859-1");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    char[] arrayOfChar = paramPasswordAuthentication.getPassword();
    byte[] arrayOfByte2 = new byte[arrayOfChar.length];
    for (int i = 0; i < arrayOfChar.length; ++i)
      arrayOfByte2[i] = (byte)arrayOfChar[i];
    byte[] arrayOfByte3 = new byte[arrayOfByte1.length + arrayOfByte2.length];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte3, 0, arrayOfByte1.length);
    System.arraycopy(arrayOfByte2, 0, arrayOfByte3, arrayOfByte1.length, arrayOfByte2.length);
    this.auth = "Basic " + new BASE64Encoder().encode(arrayOfByte3);
    this.pw = paramPasswordAuthentication;
  }

  public BasicAuthentication(boolean paramBoolean, String paramString1, int paramInt, String paramString2, String paramString3)
  {
    super((paramBoolean) ? 112 : 's', 'B', paramString1, paramInt, paramString2);
    this.auth = "Basic " + paramString3;
  }

  public BasicAuthentication(boolean paramBoolean, URL paramURL, String paramString, PasswordAuthentication paramPasswordAuthentication)
  {
    super((paramBoolean) ? 112 : 's', 'B', paramURL, paramString);
    String str = paramPasswordAuthentication.getUserName() + ":";
    byte[] arrayOfByte1 = null;
    try
    {
      arrayOfByte1 = str.getBytes("ISO-8859-1");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    char[] arrayOfChar = paramPasswordAuthentication.getPassword();
    byte[] arrayOfByte2 = new byte[arrayOfChar.length];
    for (int i = 0; i < arrayOfChar.length; ++i)
      arrayOfByte2[i] = (byte)arrayOfChar[i];
    byte[] arrayOfByte3 = new byte[arrayOfByte1.length + arrayOfByte2.length];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte3, 0, arrayOfByte1.length);
    System.arraycopy(arrayOfByte2, 0, arrayOfByte3, arrayOfByte1.length, arrayOfByte2.length);
    this.auth = "Basic " + new BASE64Encoder().encode(arrayOfByte3);
    this.pw = paramPasswordAuthentication;
  }

  public BasicAuthentication(boolean paramBoolean, URL paramURL, String paramString1, String paramString2)
  {
    super((paramBoolean) ? 112 : 's', 'B', paramURL, paramString1);
    this.auth = "Basic " + paramString2;
  }

  boolean supportsPreemptiveAuthorization()
  {
    return true;
  }

  String getHeaderName()
  {
    if (this.type == 's')
      return "Authorization";
    return "Proxy-authorization";
  }

  boolean setHeaders(HttpURLConnection paramHttpURLConnection, HeaderParser paramHeaderParser, String paramString)
  {
    paramHttpURLConnection.setAuthenticationProperty(getHeaderName(), getHeaderValue(null, null));
    return true;
  }

  String getHeaderValue(URL paramURL, String paramString)
  {
    return this.auth;
  }

  boolean isAuthorizationStale(String paramString)
  {
    return false;
  }

  void checkResponse(String paramString1, String paramString2, URL paramURL)
  {
  }

  static String getRootPath(String paramString1, String paramString2)
  {
    int i = 0;
    try
    {
      paramString1 = new URI(paramString1).normalize().getPath();
      paramString2 = new URI(paramString2).normalize().getPath();
    }
    catch (URISyntaxException localURISyntaxException)
    {
    }
    while (true)
    {
      if (i >= paramString2.length())
        break label87;
      int j = paramString2.indexOf(47, i + 1);
      if ((j == -1) || (!(paramString2.regionMatches(0, paramString1, 0, j + 1))))
        break;
      i = j;
    }
    return paramString2.substring(0, i + 1);
    label87: return paramString1;
  }
}