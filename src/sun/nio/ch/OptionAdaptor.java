package sun.nio.ch;

import java.net.SocketException;

class OptionAdaptor
{
  private final SocketOpts.IP opts;

  OptionAdaptor(SocketChannelImpl paramSocketChannelImpl)
  {
    this.opts = ((SocketOpts.IP)paramSocketChannelImpl.options());
  }

  OptionAdaptor(ServerSocketChannelImpl paramServerSocketChannelImpl)
  {
    this.opts = ((SocketOpts.IP)paramServerSocketChannelImpl.options());
  }

  OptionAdaptor(DatagramChannelImpl paramDatagramChannelImpl)
  {
    this.opts = ((SocketOpts.IP)paramDatagramChannelImpl.options());
  }

  private SocketOpts.IP opts()
  {
    return this.opts;
  }

  private SocketOpts.IP.TCP tcpOpts()
  {
    return ((SocketOpts.IP.TCP)this.opts);
  }

  public void setTcpNoDelay(boolean paramBoolean)
    throws SocketException
  {
    try
    {
      tcpOpts().noDelay(paramBoolean);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public boolean getTcpNoDelay()
    throws SocketException
  {
    try
    {
      return tcpOpts().noDelay();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
    return false;
  }

  public void setSoLinger(boolean paramBoolean, int paramInt)
    throws SocketException
  {
    try
    {
      if (paramInt > 65535)
        paramInt = 65535;
      opts().linger((paramBoolean) ? paramInt : -1);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public int getSoLinger()
    throws SocketException
  {
    try
    {
      return opts().linger();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
    return 0;
  }

  public void setOOBInline(boolean paramBoolean)
    throws SocketException
  {
    try
    {
      opts().outOfBandInline(paramBoolean);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public boolean getOOBInline()
    throws SocketException
  {
    try
    {
      return opts().outOfBandInline();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
    return false;
  }

  public void setSendBufferSize(int paramInt)
    throws SocketException
  {
    try
    {
      opts().sendBufferSize(paramInt);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public int getSendBufferSize()
    throws SocketException
  {
    try
    {
      return opts().sendBufferSize();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
    return 0;
  }

  public void setReceiveBufferSize(int paramInt)
    throws SocketException
  {
    try
    {
      opts().receiveBufferSize(paramInt);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public int getReceiveBufferSize()
    throws SocketException
  {
    try
    {
      return opts().receiveBufferSize();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
    return 0;
  }

  public void setKeepAlive(boolean paramBoolean)
    throws SocketException
  {
    try
    {
      opts().keepAlive(paramBoolean);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public boolean getKeepAlive()
    throws SocketException
  {
    try
    {
      return opts().keepAlive();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
    return false;
  }

  public void setTrafficClass(int paramInt)
    throws SocketException
  {
    if ((paramInt < 0) || (paramInt > 255))
      throw new IllegalArgumentException("tc is not in range 0 -- 255");
    try
    {
      opts().typeOfService(paramInt);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public int getTrafficClass()
    throws SocketException
  {
    try
    {
      return opts().typeOfService();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
    return 0;
  }

  public void setReuseAddress(boolean paramBoolean)
    throws SocketException
  {
    try
    {
      opts().reuseAddress(paramBoolean);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public boolean getReuseAddress()
    throws SocketException
  {
    try
    {
      return opts().reuseAddress();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
    return false;
  }

  public void setBroadcast(boolean paramBoolean)
    throws SocketException
  {
    try
    {
      opts().broadcast(paramBoolean);
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
  }

  public boolean getBroadcast()
    throws SocketException
  {
    try
    {
      return opts().broadcast();
    }
    catch (Exception localException)
    {
      Net.translateToSocketException(localException);
    }
    return false;
  }
}