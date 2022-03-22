package sun.net.www.protocol.file;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import sun.net.www.ParseUtil;

public class Handler extends URLStreamHandler
{
  private String getHost(URL paramURL)
  {
    String str = paramURL.getHost();
    if (str == null)
      str = "";
    return str;
  }

  protected void parseURL(URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    super.parseURL(paramURL, paramString.replace(File.separatorChar, '/'), paramInt1, paramInt2);
  }

  public synchronized URLConnection openConnection(URL paramURL)
    throws IOException
  {
    return openConnection(paramURL, null);
  }

  public synchronized URLConnection openConnection(URL paramURL, Proxy paramProxy)
    throws IOException
  {
    URLConnection localURLConnection;
    String str2 = paramURL.getFile();
    String str3 = paramURL.getHost();
    String str1 = ParseUtil.decode(str2);
    str1 = str1.replace('/', '\\');
    str1 = str1.replace('|', ':');
    if ((str3 == null) || (str3.equals("")) || (str3.equalsIgnoreCase("localhost")) || (str3.equals("~")))
      return createFileURLConnection(paramURL, new File(str1));
    str1 = "\\\\" + str3 + str1;
    File localFile = new File(str1);
    if (localFile.exists())
      return createFileURLConnection(paramURL, localFile);
    try
    {
      URL localURL = new URL("ftp", str3, str2 + new StringBuilder().append("#").append(paramURL.getRef()).toString());
      if (paramProxy != null)
        localURLConnection = localURL.openConnection(paramProxy);
      else
        localURLConnection = localURL.openConnection();
    }
    catch (IOException localIOException)
    {
      localURLConnection = null;
    }
    if (localURLConnection == null)
      throw new IOException("Unable to connect to: " + paramURL.toExternalForm());
    return localURLConnection;
  }

  protected URLConnection createFileURLConnection(URL paramURL, File paramFile)
  {
    return new FileURLConnection(paramURL, paramFile);
  }

  protected boolean hostsEqual(URL paramURL1, URL paramURL2)
  {
    String str1 = paramURL1.getHost();
    String str2 = paramURL2.getHost();
    if (("localhost".equalsIgnoreCase(str1)) && (((str2 == null) || ("".equals(str2)))))
      return true;
    if (("localhost".equalsIgnoreCase(str2)) && (((str1 == null) || ("".equals(str1)))))
      return true;
    return super.hostsEqual(paramURL1, paramURL2);
  }
}