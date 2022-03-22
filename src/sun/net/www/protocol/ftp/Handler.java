package sun.net.www.protocol.ftp;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler
{
  protected int getDefaultPort()
  {
    return 21;
  }

  protected boolean equals(URL paramURL1, URL paramURL2)
  {
    String str1 = paramURL1.getUserInfo();
    String str2 = paramURL2.getUserInfo();
    if (super.equals(paramURL1, paramURL2))
      if (str1 == null)
        if (str2 != null)
          break label45;
    label45: return (str1.equals(str2));
  }

  protected URLConnection openConnection(URL paramURL)
    throws IOException
  {
    return openConnection(paramURL, null);
  }

  protected URLConnection openConnection(URL paramURL, Proxy paramProxy)
    throws IOException
  {
    return new FtpURLConnection(paramURL, paramProxy);
  }
}