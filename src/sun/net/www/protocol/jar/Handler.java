package sun.net.www.protocol.jar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import sun.net.www.ParseUtil;

public class Handler extends URLStreamHandler
{
  private static final String separator = "!/";

  protected URLConnection openConnection(URL paramURL)
    throws IOException
  {
    return new JarURLConnection(paramURL, this);
  }

  private int indexOfBangSlash(String paramString)
  {
    for (int i = paramString.length(); (i = paramString.lastIndexOf(33, i)) != -1; --i)
      if ((i != paramString.length() - 1) && (paramString.charAt(i + 1) == '/'))
        return (i + 1);
    return -1;
  }

  protected void parseURL(URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    String str1 = null;
    String str2 = null;
    int i = paramString.indexOf(35, paramInt2);
    int j = (i == paramInt1) ? 1 : 0;
    if (i > -1)
    {
      str2 = paramString.substring(i + 1, paramString.length());
      if (j != 0)
        str1 = paramURL.getFile();
    }
    boolean bool = false;
    if (paramString.length() >= 4)
      bool = paramString.substring(0, 4).equalsIgnoreCase("jar:");
    paramString = paramString.substring(paramInt1, paramInt2);
    if (bool)
    {
      str1 = parseAbsoluteSpec(paramString);
    }
    else if (j == 0)
    {
      str1 = parseContextSpec(paramURL, paramString);
      int k = indexOfBangSlash(str1);
      String str3 = str1.substring(0, k);
      String str4 = str1.substring(k);
      ParseUtil localParseUtil = new ParseUtil();
      str4 = localParseUtil.canonizeString(str4);
      str1 = str3 + str4;
    }
    setURL(paramURL, "jar", "", -1, str1, str2);
  }

  private String parseAbsoluteSpec(String paramString)
  {
    URL localURL = null;
    int i = -1;
    if ((i = indexOfBangSlash(paramString)) == -1)
      throw new NullPointerException("no !/ in spec");
    try
    {
      String str = paramString.substring(0, i - 1);
      localURL = new URL(str);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      throw new NullPointerException("invalid url: " + paramString + " (" + localMalformedURLException + ")");
    }
    return paramString;
  }

  private String parseContextSpec(URL paramURL, String paramString)
  {
    int i;
    String str = paramURL.getFile();
    if (paramString.startsWith("/"))
    {
      i = indexOfBangSlash(str);
      if (i == -1)
        throw new NullPointerException("malformed context url:" + paramURL + ": no !/");
      str = str.substring(0, i);
    }
    if ((!(str.endsWith("/"))) && (!(paramString.startsWith("/"))))
    {
      i = str.lastIndexOf(47);
      if (i == -1)
        throw new NullPointerException("malformed context url:" + paramURL);
      str = str.substring(0, i + 1);
    }
    return str + paramString;
  }
}