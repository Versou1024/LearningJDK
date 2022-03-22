package sun.nio.ch;

import java.io.IOException;
import java.net.NetworkInterface;

class SocketOptsImpl
  implements SocketOpts
{
  private final Dispatcher d;

  SocketOptsImpl(Dispatcher paramDispatcher)
  {
    this.d = paramDispatcher;
  }

  protected boolean getBoolean(int paramInt)
    throws IOException
  {
    return (this.d.getInt(paramInt) > 0);
  }

  protected void setBoolean(int paramInt, boolean paramBoolean)
    throws IOException
  {
    this.d.setInt(paramInt, (paramBoolean) ? 1 : 0);
  }

  protected int getInt(int paramInt)
    throws IOException
  {
    return this.d.getInt(paramInt);
  }

  protected void setInt(int paramInt1, int paramInt2)
    throws IOException
  {
    this.d.setInt(paramInt1, paramInt2);
  }

  protected NetworkInterface getNetworkInterface(int paramInt)
    throws IOException
  {
    throw new UnsupportedOperationException("NYI");
  }

  protected void setNetworkInterface(int paramInt, NetworkInterface paramNetworkInterface)
    throws IOException
  {
    throw new UnsupportedOperationException("NYI");
  }

  protected void addToString(StringBuffer paramStringBuffer, String paramString)
  {
    int i = paramStringBuffer.charAt(paramStringBuffer.length() - 1);
    if ((i != 91) && (i != 61))
      paramStringBuffer.append(' ');
    paramStringBuffer.append(paramString);
  }

  protected void addToString(StringBuffer paramStringBuffer, int paramInt)
  {
    addToString(paramStringBuffer, Integer.toString(paramInt));
  }

  public boolean broadcast()
    throws IOException
  {
    return getBoolean(32);
  }

  public SocketOpts broadcast(boolean paramBoolean)
    throws IOException
  {
    setBoolean(32, paramBoolean);
    return this;
  }

  public boolean keepAlive()
    throws IOException
  {
    return getBoolean(8);
  }

  public SocketOpts keepAlive(boolean paramBoolean)
    throws IOException
  {
    setBoolean(8, paramBoolean);
    return this;
  }

  public int linger()
    throws IOException
  {
    return getInt(128);
  }

  public SocketOpts linger(int paramInt)
    throws IOException
  {
    setInt(128, paramInt);
    return this;
  }

  public boolean outOfBandInline()
    throws IOException
  {
    return getBoolean(4099);
  }

  public SocketOpts outOfBandInline(boolean paramBoolean)
    throws IOException
  {
    setBoolean(4099, paramBoolean);
    return this;
  }

  public int receiveBufferSize()
    throws IOException
  {
    return getInt(4098);
  }

  public SocketOpts receiveBufferSize(int paramInt)
    throws IOException
  {
    if (paramInt <= 0)
      throw new IllegalArgumentException("Invalid receive size");
    setInt(4098, paramInt);
    return this;
  }

  public int sendBufferSize()
    throws IOException
  {
    return getInt(4097);
  }

  public SocketOpts sendBufferSize(int paramInt)
    throws IOException
  {
    if (paramInt <= 0)
      throw new IllegalArgumentException("Invalid send size");
    setInt(4097, paramInt);
    return this;
  }

  public boolean reuseAddress()
    throws IOException
  {
    return getBoolean(4);
  }

  public SocketOpts reuseAddress(boolean paramBoolean)
    throws IOException
  {
    setBoolean(4, paramBoolean);
    return this;
  }

  protected void toString(StringBuffer paramStringBuffer)
    throws IOException
  {
    int i;
    if (broadcast())
      addToString(paramStringBuffer, "broadcast");
    if (keepAlive())
      addToString(paramStringBuffer, "keepalive");
    if ((i = linger()) > 0)
    {
      addToString(paramStringBuffer, "linger=");
      addToString(paramStringBuffer, i);
    }
    if (outOfBandInline())
      addToString(paramStringBuffer, "oobinline");
    if ((i = receiveBufferSize()) > 0)
    {
      addToString(paramStringBuffer, "rcvbuf=");
      addToString(paramStringBuffer, i);
    }
    if ((i = sendBufferSize()) > 0)
    {
      addToString(paramStringBuffer, "sndbuf=");
      addToString(paramStringBuffer, i);
    }
    if (reuseAddress())
      addToString(paramStringBuffer, "reuseaddr");
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append(super.getClass().getInterfaces()[0].getName());
    localStringBuffer.append('[');
    int i = localStringBuffer.length();
    try
    {
      toString(localStringBuffer);
    }
    catch (IOException localIOException)
    {
      localStringBuffer.setLength(i);
      localStringBuffer.append("closed");
    }
    localStringBuffer.append(']');
    return localStringBuffer.toString();
  }

  static abstract class Dispatcher
  {
    abstract int getInt(int paramInt)
      throws IOException;

    abstract void setInt(int paramInt1, int paramInt2)
      throws IOException;
  }

  static class IP extends SocketOptsImpl
  implements SocketOpts.IP
  {
    IP(SocketOptsImpl.Dispatcher paramDispatcher)
    {
      super(paramDispatcher);
    }

    public NetworkInterface multicastInterface()
      throws IOException
    {
      return getNetworkInterface(31);
    }

    public SocketOpts.IP multicastInterface(NetworkInterface paramNetworkInterface)
      throws IOException
    {
      setNetworkInterface(31, paramNetworkInterface);
      return this;
    }

    public boolean multicastLoop()
      throws IOException
    {
      return getBoolean(18);
    }

    public SocketOpts.IP multicastLoop(boolean paramBoolean)
      throws IOException
    {
      setBoolean(18, paramBoolean);
      return this;
    }

    public int typeOfService()
      throws IOException
    {
      return getInt(3);
    }

    public SocketOpts.IP typeOfService(int paramInt)
      throws IOException
    {
      setInt(3, paramInt);
      return this;
    }

    protected void toString(StringBuffer paramStringBuffer)
      throws IOException
    {
      int i;
      super.toString(paramStringBuffer);
      if ((i = typeOfService()) > 0)
      {
        addToString(paramStringBuffer, "tos=");
        addToString(paramStringBuffer, i);
      }
    }

    public static class TCP extends SocketOptsImpl.IP
  implements SocketOpts.IP.TCP
    {
      TCP(SocketOptsImpl.Dispatcher paramDispatcher)
      {
        super(paramDispatcher);
      }

      public boolean noDelay()
        throws IOException
      {
        return getBoolean(1);
      }

      public SocketOpts.IP.TCP noDelay(boolean paramBoolean)
        throws IOException
      {
        setBoolean(1, paramBoolean);
        return this;
      }

      protected void toString(StringBuffer paramStringBuffer)
        throws IOException
      {
        super.toString(paramStringBuffer);
        if (noDelay())
          addToString(paramStringBuffer, "nodelay");
      }
    }
  }
}