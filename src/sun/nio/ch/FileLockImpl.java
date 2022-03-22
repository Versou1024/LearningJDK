package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileLockImpl extends FileLock
{
  boolean valid = true;

  FileLockImpl(FileChannel paramFileChannel, long paramLong1, long paramLong2, boolean paramBoolean)
  {
    super(paramFileChannel, paramLong1, paramLong2, paramBoolean);
  }

  public synchronized boolean isValid()
  {
    return this.valid;
  }

  synchronized void invalidate()
  {
    this.valid = false;
  }

  public synchronized void release()
    throws IOException
  {
    if (!(channel().isOpen()))
      throw new ClosedChannelException();
    if (this.valid)
    {
      ((FileChannelImpl)channel()).release(this);
      this.valid = false;
    }
  }
}