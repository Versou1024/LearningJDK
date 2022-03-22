package sun.net.www.protocol.gopher;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketPermission;
import java.net.URL;
import java.security.Permission;
import sun.net.www.URLConnection;

class GopherURLConnection extends URLConnection
{
  Permission permission;

  GopherURLConnection(URL paramURL)
  {
    super(paramURL);
  }

  public void connect()
    throws IOException
  {
  }

  public InputStream getInputStream()
    throws IOException
  {
    return new GopherClient(this).openStream(this.url);
  }

  public Permission getPermission()
  {
    if (this.permission == null)
    {
      int i = this.url.getPort();
      i = (i < 0) ? 70 : i;
      String str = this.url.getHost() + ":" + this.url.getPort();
      this.permission = new SocketPermission(str, "connect");
    }
    return this.permission;
  }
}