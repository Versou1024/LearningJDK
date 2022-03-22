package sun.net.www.protocol.gopher;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import sun.net.NetworkClient;

class GopherInputStream extends FilterInputStream
{
  NetworkClient parent;

  GopherInputStream(NetworkClient paramNetworkClient, InputStream paramInputStream)
  {
    super(paramInputStream);
    this.parent = paramNetworkClient;
  }

  public void close()
  {
    try
    {
      this.parent.closeServer();
      super.close();
    }
    catch (IOException localIOException)
    {
    }
  }
}