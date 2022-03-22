package sun.net.httpserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.IOException;
import java.net.InetSocketAddress;

public class DefaultHttpServerProvider extends HttpServerProvider
{
  public HttpServer createHttpServer(InetSocketAddress paramInetSocketAddress, int paramInt)
    throws IOException
  {
    return new HttpServerImpl(paramInetSocketAddress, paramInt);
  }

  public HttpsServer createHttpsServer(InetSocketAddress paramInetSocketAddress, int paramInt)
    throws IOException
  {
    return new HttpsServerImpl(paramInetSocketAddress, paramInt);
  }
}