package sun.nio.ch;

import java.io.IOException;
import java.net.NetworkInterface;

public abstract interface SocketOpts
{
  public abstract boolean broadcast()
    throws IOException;

  public abstract SocketOpts broadcast(boolean paramBoolean)
    throws IOException;

  public abstract boolean keepAlive()
    throws IOException;

  public abstract SocketOpts keepAlive(boolean paramBoolean)
    throws IOException;

  public abstract int linger()
    throws IOException;

  public abstract SocketOpts linger(int paramInt)
    throws IOException;

  public abstract boolean outOfBandInline()
    throws IOException;

  public abstract SocketOpts outOfBandInline(boolean paramBoolean)
    throws IOException;

  public abstract int receiveBufferSize()
    throws IOException;

  public abstract SocketOpts receiveBufferSize(int paramInt)
    throws IOException;

  public abstract int sendBufferSize()
    throws IOException;

  public abstract SocketOpts sendBufferSize(int paramInt)
    throws IOException;

  public abstract boolean reuseAddress()
    throws IOException;

  public abstract SocketOpts reuseAddress(boolean paramBoolean)
    throws IOException;

  public static abstract interface IP extends SocketOpts
  {
    public static final int TOS_LOWDELAY = 16;
    public static final int TOS_THROUGHPUT = 8;
    public static final int TOS_RELIABILITY = 4;
    public static final int TOS_MINCOST = 2;

    public abstract NetworkInterface multicastInterface()
      throws IOException;

    public abstract IP multicastInterface(NetworkInterface paramNetworkInterface)
      throws IOException;

    public abstract boolean multicastLoop()
      throws IOException;

    public abstract IP multicastLoop(boolean paramBoolean)
      throws IOException;

    public abstract int typeOfService()
      throws IOException;

    public abstract IP typeOfService(int paramInt)
      throws IOException;

    public static abstract interface TCP extends SocketOpts.IP
    {
      public abstract boolean noDelay()
        throws IOException;

      public abstract TCP noDelay(boolean paramBoolean)
        throws IOException;
    }
  }
}