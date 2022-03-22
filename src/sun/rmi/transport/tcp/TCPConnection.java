package sun.rmi.transport.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import sun.rmi.runtime.Log;
import sun.rmi.transport.Channel;
import sun.rmi.transport.Connection;
import sun.rmi.transport.proxy.RMISocketInfo;

public class TCPConnection
  implements Connection
{
  private Socket socket;
  private Channel channel;
  private InputStream in;
  private OutputStream out;
  private long expiration;
  private long lastuse;
  private long roundtrip;

  TCPConnection(TCPChannel paramTCPChannel, Socket paramSocket, InputStream paramInputStream, OutputStream paramOutputStream)
  {
    this.in = null;
    this.out = null;
    this.expiration = 9223372036854775807L;
    this.lastuse = -9223372036854775808L;
    this.roundtrip = 5L;
    this.socket = paramSocket;
    this.channel = paramTCPChannel;
    this.in = paramInputStream;
    this.out = paramOutputStream;
  }

  TCPConnection(TCPChannel paramTCPChannel, InputStream paramInputStream, OutputStream paramOutputStream)
  {
    this(paramTCPChannel, null, paramInputStream, paramOutputStream);
  }

  TCPConnection(TCPChannel paramTCPChannel, Socket paramSocket)
  {
    this(paramTCPChannel, paramSocket, null, null);
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    if (this.out == null)
      this.out = new BufferedOutputStream(this.socket.getOutputStream());
    return this.out;
  }

  public void releaseOutputStream()
    throws IOException
  {
    if (this.out != null)
      this.out.flush();
  }

  public InputStream getInputStream()
    throws IOException
  {
    if (this.in == null)
      this.in = new BufferedInputStream(this.socket.getInputStream());
    return this.in;
  }

  public void releaseInputStream()
  {
  }

  public boolean isReusable()
  {
    if ((this.socket != null) && (this.socket instanceof RMISocketInfo))
      return ((RMISocketInfo)this.socket).isReusable();
    return true;
  }

  void setExpiration(long paramLong)
  {
    this.expiration = paramLong;
  }

  void setLastUseTime(long paramLong)
  {
    this.lastuse = paramLong;
  }

  boolean expired(long paramLong)
  {
    return (this.expiration <= paramLong);
  }

  public boolean isDead()
  {
    InputStream localInputStream;
    OutputStream localOutputStream;
    long l = System.currentTimeMillis();
    if ((this.roundtrip > 3412046827397054464L) && (l < this.lastuse + this.roundtrip))
      return false;
    try
    {
      localInputStream = getInputStream();
      localOutputStream = getOutputStream();
    }
    catch (IOException localIOException1)
    {
      return true;
    }
    int i = 0;
    try
    {
      localOutputStream.write(82);
      localOutputStream.flush();
      i = localInputStream.read();
    }
    catch (IOException localIOException2)
    {
      TCPTransport.tcpLog.log(Log.VERBOSE, "exception: ", localIOException2);
      TCPTransport.tcpLog.log(Log.BRIEF, "server ping failed");
      return true;
    }
    if (i == 83)
    {
      this.roundtrip = ((System.currentTimeMillis() - l) * 2L);
      return false;
    }
    if (TCPTransport.tcpLog.isLoggable(Log.BRIEF))
      TCPTransport.tcpLog.log(Log.BRIEF, "server protocol error: ping response = " + i);
    return true;
  }

  public void close()
    throws IOException
  {
    TCPTransport.tcpLog.log(Log.BRIEF, "close connection");
    if (this.socket != null)
    {
      this.socket.close();
    }
    else
    {
      this.in.close();
      this.out.close();
    }
  }

  public Channel getChannel()
  {
    return this.channel;
  }
}