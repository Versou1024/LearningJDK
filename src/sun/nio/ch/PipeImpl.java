package sun.nio.ch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Random;

class PipeImpl extends Pipe
{
  private Pipe.SourceChannel source;
  private Pipe.SinkChannel sink;
  private static final Random rnd;

  PipeImpl(SelectorProvider paramSelectorProvider)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new Initializer(this, paramSelectorProvider, null));
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getCause());
    }
  }

  public Pipe.SourceChannel source()
  {
    return this.source;
  }

  public Pipe.SinkChannel sink()
  {
    return this.sink;
  }

  static
  {
    Util.load();
    byte[] arrayOfByte = new byte[8];
    boolean bool = IOUtil.randomBytes(arrayOfByte);
    if (bool)
      rnd = new Random(ByteBuffer.wrap(arrayOfByte).getLong());
    else
      rnd = new Random();
  }

  private class Initializer
  implements PrivilegedExceptionAction
  {
    private final SelectorProvider sp;

    private Initializer(, SelectorProvider paramSelectorProvider)
    {
      this.sp = paramSelectorProvider;
    }

    public Object run()
      throws IOException
    {
      ServerSocketChannel localServerSocketChannel = null;
      SocketChannel localSocketChannel1 = null;
      SocketChannel localSocketChannel2 = null;
      try
      {
        InetAddress localInetAddress = InetAddress.getByName("127.0.0.1");
        if ((!($assertionsDisabled)) && (!(localInetAddress.isLoopbackAddress())))
          throw new AssertionError();
        localServerSocketChannel = ServerSocketChannel.open();
        localServerSocketChannel.socket().bind(new InetSocketAddress(localInetAddress, 0));
        InetSocketAddress localInetSocketAddress = new InetSocketAddress(localInetAddress, localServerSocketChannel.socket().getLocalPort());
        localSocketChannel1 = SocketChannel.open(localInetSocketAddress);
        ByteBuffer localByteBuffer = ByteBuffer.allocate(8);
        long l = PipeImpl.access$000().nextLong();
        localByteBuffer.putLong(l).flip();
        localSocketChannel1.write(localByteBuffer);
        while (true)
        {
          localSocketChannel2 = localServerSocketChannel.accept();
          localByteBuffer.clear();
          localSocketChannel2.read(localByteBuffer);
          localByteBuffer.rewind();
          if (localByteBuffer.getLong() == l)
            break;
          localSocketChannel2.close();
        }
        PipeImpl.access$102(this.this$0, new SourceChannelImpl(this.sp, localSocketChannel1));
        PipeImpl.access$202(this.this$0, new SinkChannelImpl(this.sp, localSocketChannel2));
      }
      catch (IOException localIOException1)
      {
        try
        {
          if (localSocketChannel1 != null)
            localSocketChannel1.close();
          if (localSocketChannel2 != null)
            localSocketChannel2.close();
        }
        catch (IOException localIOException2)
        {
        }
        IOException localIOException3 = new IOException("Unable to establish loopback connection");
        throw localIOException3;
      }
      finally
      {
        try
        {
          if (localServerSocketChannel != null)
            localServerSocketChannel.close();
        }
        catch (IOException localIOException4)
        {
        }
      }
      return null;
    }
  }
}