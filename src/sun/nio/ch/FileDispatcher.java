package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;

class FileDispatcher extends NativeDispatcher
{
  int read(FileDescriptor paramFileDescriptor, long paramLong, int paramInt)
    throws IOException
  {
    return read0(paramFileDescriptor, paramLong, paramInt);
  }

  int pread(FileDescriptor paramFileDescriptor, long paramLong1, int paramInt, long paramLong2, Object paramObject)
    throws IOException
  {
    synchronized (paramObject)
    {
      return pread0(paramFileDescriptor, paramLong1, paramInt, paramLong2);
    }
  }

  long readv(FileDescriptor paramFileDescriptor, long paramLong, int paramInt)
    throws IOException
  {
    return readv0(paramFileDescriptor, paramLong, paramInt);
  }

  int write(FileDescriptor paramFileDescriptor, long paramLong, int paramInt)
    throws IOException
  {
    return write0(paramFileDescriptor, paramLong, paramInt);
  }

  int pwrite(FileDescriptor paramFileDescriptor, long paramLong1, int paramInt, long paramLong2, Object paramObject)
    throws IOException
  {
    synchronized (paramObject)
    {
      return pwrite0(paramFileDescriptor, paramLong1, paramInt, paramLong2);
    }
  }

  long writev(FileDescriptor paramFileDescriptor, long paramLong, int paramInt)
    throws IOException
  {
    return writev0(paramFileDescriptor, paramLong, paramInt);
  }

  void close(FileDescriptor paramFileDescriptor)
    throws IOException
  {
    close0(paramFileDescriptor);
  }

  static native int read0(FileDescriptor paramFileDescriptor, long paramLong, int paramInt)
    throws IOException;

  static native int pread0(FileDescriptor paramFileDescriptor, long paramLong1, int paramInt, long paramLong2)
    throws IOException;

  static native long readv0(FileDescriptor paramFileDescriptor, long paramLong, int paramInt)
    throws IOException;

  static native int write0(FileDescriptor paramFileDescriptor, long paramLong, int paramInt)
    throws IOException;

  static native int pwrite0(FileDescriptor paramFileDescriptor, long paramLong1, int paramInt, long paramLong2)
    throws IOException;

  static native long writev0(FileDescriptor paramFileDescriptor, long paramLong, int paramInt)
    throws IOException;

  static native void close0(FileDescriptor paramFileDescriptor)
    throws IOException;

  static native void closeByHandle(long paramLong)
    throws IOException;

  static
  {
    Util.load();
  }
}