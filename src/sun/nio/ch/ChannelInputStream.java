package sun.nio.ch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;

public class ChannelInputStream extends InputStream
{
  protected final ReadableByteChannel ch;
  private ByteBuffer bb = null;
  private byte[] bs = null;
  private byte[] b1 = null;

  public static int read(ReadableByteChannel paramReadableByteChannel, ByteBuffer paramByteBuffer, boolean paramBoolean)
    throws IOException
  {
    if (paramReadableByteChannel instanceof SelectableChannel)
    {
      SelectableChannel localSelectableChannel = (SelectableChannel)paramReadableByteChannel;
      synchronized (localSelectableChannel.blockingLock())
      {
        boolean bool = localSelectableChannel.isBlocking();
        if (!(bool))
          throw new IllegalBlockingModeException();
        if (bool != paramBoolean)
          localSelectableChannel.configureBlocking(paramBoolean);
        int i = paramReadableByteChannel.read(paramByteBuffer);
        if (bool != paramBoolean)
          localSelectableChannel.configureBlocking(bool);
        return i;
      }
    }
    return paramReadableByteChannel.read(paramByteBuffer);
  }

  public ChannelInputStream(ReadableByteChannel paramReadableByteChannel)
  {
    this.ch = paramReadableByteChannel;
  }

  public synchronized int read()
    throws IOException
  {
    if (this.b1 == null)
      this.b1 = new byte[1];
    int i = read(this.b1);
    if (i == 1)
      return (this.b1[0] & 0xFF);
    return -1;
  }

  public synchronized int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if ((paramInt1 < 0) || (paramInt1 > paramArrayOfByte.length) || (paramInt2 < 0) || (paramInt1 + paramInt2 > paramArrayOfByte.length) || (paramInt1 + paramInt2 < 0))
      throw new IndexOutOfBoundsException();
    if (paramInt2 == 0)
      return 0;
    ByteBuffer localByteBuffer = (this.bs == paramArrayOfByte) ? this.bb : ByteBuffer.wrap(paramArrayOfByte);
    localByteBuffer.position(paramInt1);
    localByteBuffer.limit(Math.min(paramInt1 + paramInt2, localByteBuffer.capacity()));
    this.bb = localByteBuffer;
    this.bs = paramArrayOfByte;
    return read(localByteBuffer);
  }

  protected int read(ByteBuffer paramByteBuffer)
    throws IOException
  {
    return read(this.ch, paramByteBuffer, true);
  }

  public void close()
    throws IOException
  {
    this.ch.close();
  }
}