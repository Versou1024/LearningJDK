package sun.net.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

class HttpConnection
{
  HttpContextImpl context;
  SSLEngine engine;
  SSLContext sslContext;
  SSLStreams sslStreams;
  InputStream i;
  InputStream raw;
  OutputStream rawout;
  SocketChannel chan;
  SelectionKey selectionKey;
  String protocol;
  long time;
  int remaining;
  boolean closed = false;
  Logger logger;

  public String toString()
  {
    String str = null;
    if (this.chan != null)
      str = this.chan.toString();
    return str;
  }

  void setChannel(SocketChannel paramSocketChannel)
  {
    this.chan = paramSocketChannel;
  }

  void setContext(HttpContextImpl paramHttpContextImpl)
  {
    this.context = paramHttpContextImpl;
  }

  void setParameters(InputStream paramInputStream1, OutputStream paramOutputStream, SocketChannel paramSocketChannel, SSLEngine paramSSLEngine, SSLStreams paramSSLStreams, SSLContext paramSSLContext, String paramString, HttpContextImpl paramHttpContextImpl, InputStream paramInputStream2)
  {
    this.context = paramHttpContextImpl;
    this.i = paramInputStream1;
    this.rawout = paramOutputStream;
    this.raw = paramInputStream2;
    this.protocol = paramString;
    this.engine = paramSSLEngine;
    this.chan = paramSocketChannel;
    this.sslContext = paramSSLContext;
    this.sslStreams = paramSSLStreams;
    this.logger = paramHttpContextImpl.getLogger();
  }

  SocketChannel getChannel()
  {
    return this.chan;
  }

  synchronized void close()
  {
    if (this.closed)
      return;
    this.closed = true;
    if ((this.logger != null) && (this.chan != null))
      this.logger.finest("Closing connection: " + this.chan.toString());
    if (!(this.chan.isOpen()))
    {
      ServerImpl.dprint("Channel already closed");
      return;
    }
    try
    {
      if (this.raw != null)
        this.raw.close();
    }
    catch (IOException localIOException1)
    {
      ServerImpl.dprint(localIOException1);
    }
    try
    {
      if (this.rawout != null)
        this.rawout.close();
    }
    catch (IOException localIOException2)
    {
      ServerImpl.dprint(localIOException2);
    }
    try
    {
      if (this.sslStreams != null)
        this.sslStreams.close();
    }
    catch (IOException localIOException3)
    {
      ServerImpl.dprint(localIOException3);
    }
    try
    {
      this.chan.close();
    }
    catch (IOException localIOException4)
    {
      ServerImpl.dprint(localIOException4);
    }
  }

  void setRemaining(int paramInt)
  {
    this.remaining = paramInt;
  }

  int getRemaining()
  {
    return this.remaining;
  }

  SelectionKey getSelectionKey()
  {
    return this.selectionKey;
  }

  InputStream getInputStream()
  {
    return this.i;
  }

  OutputStream getRawOutputStream()
  {
    return this.rawout;
  }

  String getProtocol()
  {
    return this.protocol;
  }

  SSLEngine getSSLEngine()
  {
    return this.engine;
  }

  SSLContext getSSLContext()
  {
    return this.sslContext;
  }

  HttpContextImpl getHttpContext()
  {
    return this.context;
  }
}