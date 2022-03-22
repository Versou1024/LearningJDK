package sun.awt.image;

import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;

public class URLImageSource extends InputStreamImageSource
{
  URL url;
  URLConnection conn;
  String actualHost;
  int actualPort;

  public URLImageSource(URL paramURL)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      try
      {
        Permission localPermission = paramURL.openConnection().getPermission();
        if (localPermission != null)
          try
          {
            localSecurityManager.checkPermission(localPermission);
          }
          catch (SecurityException localSecurityException)
          {
            if ((localPermission instanceof FilePermission) && (localPermission.getActions().indexOf("read") != -1))
              localSecurityManager.checkRead(localPermission.getName());
            else if ((localPermission instanceof SocketPermission) && (localPermission.getActions().indexOf("connect") != -1))
              localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
            else
              throw localSecurityException;
          }
      }
      catch (IOException localIOException)
      {
        localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
      }
    this.url = paramURL;
  }

  public URLImageSource(String paramString)
    throws MalformedURLException
  {
    this(new URL(null, paramString));
  }

  public URLImageSource(URL paramURL, URLConnection paramURLConnection)
  {
    this(paramURL);
    this.conn = paramURLConnection;
  }

  public URLImageSource(URLConnection paramURLConnection)
  {
    this(paramURLConnection.getURL(), paramURLConnection);
  }

  final boolean checkSecurity(Object paramObject, boolean paramBoolean)
  {
    if (this.actualHost != null)
      try
      {
        SecurityManager localSecurityManager = System.getSecurityManager();
        if (localSecurityManager != null)
          localSecurityManager.checkConnect(this.actualHost, this.actualPort, paramObject);
      }
      catch (SecurityException localSecurityException)
      {
        if (!(paramBoolean))
          throw localSecurityException;
        return false;
      }
    return true;
  }

  private synchronized URLConnection getConnection()
    throws IOException
  {
    URLConnection localURLConnection;
    if (this.conn != null)
    {
      localURLConnection = this.conn;
      this.conn = null;
    }
    else
    {
      localURLConnection = this.url.openConnection();
    }
    return localURLConnection;
  }

  protected ImageDecoder getDecoder()
  {
    InputStream localInputStream = null;
    String str = null;
    URLConnection localURLConnection = null;
    try
    {
      localURLConnection = getConnection();
      localInputStream = localURLConnection.getInputStream();
      str = localURLConnection.getContentType();
      URL localURL = localURLConnection.getURL();
      if ((localURL != this.url) && (((!(localURL.getHost().equals(this.url.getHost()))) || (localURL.getPort() != this.url.getPort()))))
      {
        if ((this.actualHost != null) && (((!(this.actualHost.equals(localURL.getHost()))) || (this.actualPort != localURL.getPort()))))
          throw new SecurityException("image moved!");
        this.actualHost = localURL.getHost();
        this.actualPort = localURL.getPort();
      }
    }
    catch (IOException localIOException1)
    {
      if (localInputStream != null)
        try
        {
          localInputStream.close();
        }
        catch (IOException localIOException2)
        {
        }
      else if (localURLConnection instanceof HttpURLConnection)
        ((HttpURLConnection)localURLConnection).disconnect();
      return null;
    }
    ImageDecoder localImageDecoder = decoderForType(localInputStream, str);
    if (localImageDecoder == null)
      localImageDecoder = getDecoder(localInputStream);
    if (localImageDecoder == null)
      if (localInputStream != null)
        try
        {
          localInputStream.close();
        }
        catch (IOException localIOException3)
        {
        }
      else if (localURLConnection instanceof HttpURLConnection)
        ((HttpURLConnection)localURLConnection).disconnect();
    return localImageDecoder;
  }
}