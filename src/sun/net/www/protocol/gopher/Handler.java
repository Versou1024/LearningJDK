package sun.net.www.protocol.gopher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import sun.net.www.protocol.http.HttpURLConnection;

public class Handler extends URLStreamHandler
{
  protected int getDefaultPort()
  {
    return 70;
  }

  public URLConnection openConnection(URL paramURL)
    throws IOException
  {
    return openConnection(paramURL, null);
  }

  public URLConnection openConnection(URL paramURL, Proxy paramProxy)
    throws IOException
  {
    if ((paramProxy == null) && (GopherClient.getUseGopherProxy()))
    {
      String str = GopherClient.getGopherProxyHost();
      if (str != null)
      {
        InetSocketAddress localInetSocketAddress = InetSocketAddress.createUnresolved(str, GopherClient.getGopherProxyPort());
        paramProxy = new Proxy(Proxy.Type.HTTP, localInetSocketAddress);
      }
    }
    if (paramProxy != null)
      return new HttpURLConnection(paramURL, paramProxy);
    return new GopherURLConnection(paramURL);
  }
}