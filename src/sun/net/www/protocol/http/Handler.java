package sun.net.www.protocol.http;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler
{
  protected String proxy;
  protected int proxyPort;

  protected int getDefaultPort()
  {
    return 80;
  }

  public Handler()
  {
    this.proxy = null;
    this.proxyPort = -1;
  }

  public Handler(String paramString, int paramInt)
  {
    this.proxy = paramString;
    this.proxyPort = paramInt;
  }

  protected URLConnection openConnection(URL paramURL)
    throws IOException
  {
    return openConnection(paramURL, (Proxy)null);
  }

  protected URLConnection openConnection(URL paramURL, Proxy paramProxy)
    throws IOException
  {
    return new HttpURLConnection(paramURL, paramProxy, this);
  }
}