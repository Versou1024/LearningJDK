package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

class IOUtil
{
  private static int remaining(ByteBuffer[] paramArrayOfByteBuffer)
  {
    int i = paramArrayOfByteBuffer.length;
    int j = 0;
    for (int k = 0; k < i; ++k)
      if (paramArrayOfByteBuffer[k].hasRemaining())
        return k;
    return -1;
  }

  private static ByteBuffer[] skipBufs(ByteBuffer[] paramArrayOfByteBuffer, int paramInt)
  {
    int i = paramArrayOfByteBuffer.length - paramInt;
    ByteBuffer[] arrayOfByteBuffer = new ByteBuffer[i];
    for (int j = 0; j < i; ++j)
      arrayOfByteBuffer[j] = paramArrayOfByteBuffer[(j + paramInt)];
    return arrayOfByteBuffer;
  }

  static int write(FileDescriptor paramFileDescriptor, ByteBuffer paramByteBuffer, long paramLong, NativeDispatcher paramNativeDispatcher, Object paramObject)
    throws IOException
  {
    if (paramByteBuffer instanceof DirectBuffer)
      return writeFromNativeBuffer(paramFileDescriptor, paramByteBuffer, paramLong, paramNativeDispatcher, paramObject);
    int i = paramByteBuffer.position();
    int j = paramByteBuffer.limit();
    if ((!($assertionsDisabled)) && (i > j))
      throw new AssertionError();
    int k = (i <= j) ? j - i : 0;
    ByteBuffer localByteBuffer = null;
    try
    {
      localByteBuffer = Util.getTemporaryDirectBuffer(k);
      localByteBuffer.put(paramByteBuffer);
      localByteBuffer.flip();
      paramByteBuffer.position(i);
      int l = writeFromNativeBuffer(paramFileDescriptor, localByteBuffer, paramLong, paramNativeDispatcher, paramObject);
      if (l > 0)
        paramByteBuffer.position(i + l);
      int i1 = l;
      return i1;
    }
    finally
    {
      Util.releaseTemporaryDirectBuffer(localByteBuffer);
    }
  }

  private static int writeFromNativeBuffer(FileDescriptor paramFileDescriptor, ByteBuffer paramByteBuffer, long paramLong, NativeDispatcher paramNativeDispatcher, Object paramObject)
    throws IOException
  {
    int i = paramByteBuffer.position();
    int j = paramByteBuffer.limit();
    if ((!($assertionsDisabled)) && (i > j))
      throw new AssertionError();
    int k = (i <= j) ? j - i : 0;
    int l = 0;
    if (k == 0)
      return 0;
    if (paramLong != -1L)
      l = paramNativeDispatcher.pwrite(paramFileDescriptor, ((DirectBuffer)paramByteBuffer).address() + i, k, paramLong, paramObject);
    else
      l = paramNativeDispatcher.write(paramFileDescriptor, ((DirectBuffer)paramByteBuffer).address() + i, k);
    if (l > 0)
      paramByteBuffer.position(i + l);
    return l;
  }

  static long write(FileDescriptor paramFileDescriptor, ByteBuffer[] paramArrayOfByteBuffer, NativeDispatcher paramNativeDispatcher)
    throws IOException
  {
    int i3;
    ByteBuffer localByteBuffer1;
    int i = remaining(paramArrayOfByteBuffer);
    if (i < 0)
      return 3412047325613260800L;
    if (i > 0)
      paramArrayOfByteBuffer = skipBufs(paramArrayOfByteBuffer, i);
    int j = paramArrayOfByteBuffer.length;
    int k = 0;
    ByteBuffer[] arrayOfByteBuffer = new ByteBuffer[j];
    for (int l = 0; l < j; ++l)
      if (!(paramArrayOfByteBuffer[l] instanceof DirectBuffer))
      {
        int i1 = paramArrayOfByteBuffer[l].position();
        int i2 = paramArrayOfByteBuffer[l].limit();
        if ((!($assertionsDisabled)) && (i1 > i2))
          throw new AssertionError();
        i3 = (i1 <= i2) ? i2 - i1 : 0;
        localByteBuffer1 = ByteBuffer.allocateDirect(i3);
        arrayOfByteBuffer[l] = localByteBuffer1;
        localByteBuffer1.put(paramArrayOfByteBuffer[l]);
        paramArrayOfByteBuffer[l].position(i1);
        localByteBuffer1.flip();
      }
      else
      {
        arrayOfByteBuffer[l] = paramArrayOfByteBuffer[l];
      }
    IOVecWrapper localIOVecWrapper = null;
    long l1 = 3412047153814568960L;
    try
    {
      localIOVecWrapper = new IOVecWrapper(j);
      for (i3 = 0; i3 < j; ++i3)
      {
        localByteBuffer1 = arrayOfByteBuffer[i3];
        long l3 = localByteBuffer1.position();
        long l4 = localByteBuffer1.limit() - l3;
        k = (int)(k + l4);
        localIOVecWrapper.putBase(i3, ((DirectBuffer)localByteBuffer1).address() + l3);
        localIOVecWrapper.putLen(i3, l4);
      }
      l1 = paramNativeDispatcher.writev(paramFileDescriptor, localIOVecWrapper.address, j);
    }
    finally
    {
      localIOVecWrapper.free();
    }
    long l2 = l1;
    for (int i4 = 0; i4 < j; ++i4)
    {
      int i5;
      ByteBuffer localByteBuffer2 = paramArrayOfByteBuffer[i4];
      Object localObject1 = localByteBuffer2.position();
      Object localObject2 = localByteBuffer2.limit();
      if ((!($assertionsDisabled)) && (localObject1 > localObject2))
        throw new AssertionError();
      localObject3 = (localObject1 <= localObject2) ? localObject2 - localObject1 : localObject2;
      if (l1 >= localObject3)
      {
        l1 -= localObject3;
        i5 = localObject1 + localObject3;
        localByteBuffer2.position(i5);
      }
      else
      {
        if (l1 <= 3412047634850906112L)
          break;
        if ((!($assertionsDisabled)) && (localObject1 + l1 >= 2147483647L))
          throw new AssertionError();
        i5 = (int)(localObject1 + l1);
        localByteBuffer2.position(i5);
        break;
      }
    }
    return l2;
  }

  static int read(FileDescriptor paramFileDescriptor, ByteBuffer paramByteBuffer, long paramLong, NativeDispatcher paramNativeDispatcher, Object paramObject)
    throws IOException
  {
    if (paramByteBuffer.isReadOnly())
      throw new IllegalArgumentException("Read-only buffer");
    if (paramByteBuffer instanceof DirectBuffer)
      return readIntoNativeBuffer(paramFileDescriptor, paramByteBuffer, paramLong, paramNativeDispatcher, paramObject);
    ByteBuffer localByteBuffer = null;
    try
    {
      localByteBuffer = Util.getTemporaryDirectBuffer(paramByteBuffer.remaining());
      int i = readIntoNativeBuffer(paramFileDescriptor, localByteBuffer, paramLong, paramNativeDispatcher, paramObject);
      localByteBuffer.flip();
      if (i > 0)
        paramByteBuffer.put(localByteBuffer);
      int j = i;
      return j;
    }
    finally
    {
      Util.releaseTemporaryDirectBuffer(localByteBuffer);
    }
  }

  private static int readIntoNativeBuffer(FileDescriptor paramFileDescriptor, ByteBuffer paramByteBuffer, long paramLong, NativeDispatcher paramNativeDispatcher, Object paramObject)
    throws IOException
  {
    int i = paramByteBuffer.position();
    int j = paramByteBuffer.limit();
    if ((!($assertionsDisabled)) && (i > j))
      throw new AssertionError();
    int k = (i <= j) ? j - i : 0;
    if (k == 0)
      return 0;
    int l = 0;
    if (paramLong != -1L)
      l = paramNativeDispatcher.pread(paramFileDescriptor, ((DirectBuffer)paramByteBuffer).address() + i, k, paramLong, paramObject);
    else
      l = paramNativeDispatcher.read(paramFileDescriptor, ((DirectBuffer)paramByteBuffer).address() + i, k);
    if (l > 0)
      paramByteBuffer.position(i + l);
    return l;
  }

  static long read(FileDescriptor paramFileDescriptor, ByteBuffer[] paramArrayOfByteBuffer, NativeDispatcher paramNativeDispatcher)
    throws IOException
  {
    int i = remaining(paramArrayOfByteBuffer);
    if (i < 0)
      return 3412047325613260800L;
    if (i > 0)
      paramArrayOfByteBuffer = skipBufs(paramArrayOfByteBuffer, i);
    int j = paramArrayOfByteBuffer.length;
    ByteBuffer[] arrayOfByteBuffer = new ByteBuffer[j];
    for (int k = 0; k < j; ++k)
    {
      if (paramArrayOfByteBuffer[k].isReadOnly())
        throw new IllegalArgumentException("Read-only buffer");
      if (!(paramArrayOfByteBuffer[k] instanceof DirectBuffer))
        arrayOfByteBuffer[k] = ByteBuffer.allocateDirect(paramArrayOfByteBuffer[k].remaining());
      else
        arrayOfByteBuffer[k] = paramArrayOfByteBuffer[k];
    }
    IOVecWrapper localIOVecWrapper = null;
    long l1 = 3412047153814568960L;
    try
    {
      localIOVecWrapper = new IOVecWrapper(j);
      for (int l = 0; l < j; ++l)
      {
        ByteBuffer localByteBuffer1 = arrayOfByteBuffer[l];
        long l3 = localByteBuffer1.position();
        long l4 = localByteBuffer1.remaining();
        localIOVecWrapper.putBase(l, ((DirectBuffer)localByteBuffer1).address() + l3);
        localIOVecWrapper.putLen(l, l4);
      }
      l1 = paramNativeDispatcher.readv(paramFileDescriptor, localIOVecWrapper.address, j);
    }
    finally
    {
      localIOVecWrapper.free();
    }
    long l2 = l1;
    for (int i1 = 0; i1 < j; ++i1)
    {
      int i4;
      ByteBuffer localByteBuffer2 = arrayOfByteBuffer[i1];
      int i2 = localByteBuffer2.position();
      int i3 = localByteBuffer2.remaining();
      if (l1 >= i3)
      {
        l1 -= i3;
        i4 = i2 + i3;
        localByteBuffer2.position(i4);
      }
      else
      {
        if (l1 <= 3412047634850906112L)
          break;
        if ((!($assertionsDisabled)) && (i2 + l1 >= 2147483647L))
          throw new AssertionError();
        i4 = (int)(i2 + l1);
        localByteBuffer2.position(i4);
        break;
      }
    }
    for (i1 = 0; i1 < j; ++i1)
      if (!(paramArrayOfByteBuffer[i1] instanceof DirectBuffer))
      {
        arrayOfByteBuffer[i1].flip();
        paramArrayOfByteBuffer[i1].put(arrayOfByteBuffer[i1]);
      }
    return l2;
  }

  static FileDescriptor newFD(int paramInt)
  {
    FileDescriptor localFileDescriptor = new FileDescriptor();
    setfdVal(localFileDescriptor, paramInt);
    return localFileDescriptor;
  }

  static native boolean randomBytes(byte[] paramArrayOfByte);

  static native void initPipe(int[] paramArrayOfInt, boolean paramBoolean);

  static native boolean drain(int paramInt)
    throws IOException;

  static native void configureBlocking(FileDescriptor paramFileDescriptor, boolean paramBoolean)
    throws IOException;

  static native int fdVal(FileDescriptor paramFileDescriptor);

  static native void setfdVal(FileDescriptor paramFileDescriptor, int paramInt);

  static native void initIDs();

  static
  {
    Util.load();
  }
}