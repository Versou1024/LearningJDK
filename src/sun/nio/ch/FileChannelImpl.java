package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import sun.misc.Cleaner;
import sun.security.action.GetPropertyAction;

public class FileChannelImpl extends FileChannel
{
  private static NativeDispatcher nd;
  private static long allocationGranularity;
  private static Field isAMappedBufferField;
  private FileDescriptor fd;
  private boolean writable;
  private boolean readable;
  private boolean appending;
  private Object parent;
  private NativeThreadSet threads = new NativeThreadSet(2);
  private Object positionLock = new Object();
  private static volatile boolean transferSupported;
  private static volatile boolean pipeSupported;
  private static volatile boolean fileSupported;
  private static final int TRANSFER_SIZE = 8192;
  private static final int MAP_RO = 0;
  private static final int MAP_RW = 1;
  private static final int MAP_PV = 2;
  public static final int NO_LOCK = -1;
  public static final int LOCKED = 0;
  public static final int RET_EX_LOCK = 1;
  public static final int INTERRUPTED = 2;
  private volatile FileLockTable fileLockTable;
  private static boolean isSharedFileLockTable;
  private static volatile boolean propertyChecked;

  private FileChannelImpl(FileDescriptor paramFileDescriptor, boolean paramBoolean1, boolean paramBoolean2, Object paramObject, boolean paramBoolean3)
  {
    this.fd = paramFileDescriptor;
    this.readable = paramBoolean1;
    this.writable = paramBoolean2;
    this.parent = paramObject;
    this.appending = paramBoolean3;
  }

  public static FileChannel open(FileDescriptor paramFileDescriptor, boolean paramBoolean1, boolean paramBoolean2, Object paramObject)
  {
    return new FileChannelImpl(paramFileDescriptor, paramBoolean1, paramBoolean2, paramObject, false);
  }

  public static FileChannel open(FileDescriptor paramFileDescriptor, boolean paramBoolean1, boolean paramBoolean2, Object paramObject, boolean paramBoolean3)
  {
    return new FileChannelImpl(paramFileDescriptor, paramBoolean1, paramBoolean2, paramObject, paramBoolean3);
  }

  private void ensureOpen()
    throws IOException
  {
    if (!(isOpen()))
      throw new ClosedChannelException();
  }

  protected void implCloseChannel()
    throws IOException
  {
    nd.preClose(this.fd);
    this.threads.signal();
    if (this.fileLockTable != null)
      this.fileLockTable.removeAll(new FileLockTable.Releaser(this)
      {
        public void release()
          throws IOException
        {
          ((FileLockImpl)paramFileLock).invalidate();
          this.this$0.release0(FileChannelImpl.access$000(this.this$0), paramFileLock.position(), paramFileLock.size());
        }
      });
    if (this.parent != null)
    {
      if (this.parent instanceof FileInputStream)
      {
        ((FileInputStream)this.parent).close();
        return;
      }
      if (this.parent instanceof FileOutputStream)
      {
        ((FileOutputStream)this.parent).close();
        return;
      }
      if (this.parent instanceof RandomAccessFile)
      {
        ((RandomAccessFile)this.parent).close();
        return;
      }
      if ($assertionsDisabled)
        return;
      throw new AssertionError();
    }
    nd.close(this.fd);
  }

  // ERROR //
  public int read(ByteBuffer paramByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 468	sun/nio/ch/FileChannelImpl:ensureOpen	()V
    //   4: aload_0
    //   5: getfield 426	sun/nio/ch/FileChannelImpl:readable	Z
    //   8: ifne +11 -> 19
    //   11: new 235	java/nio/channels/NonReadableChannelException
    //   14: dup
    //   15: invokespecial 460	java/nio/channels/NonReadableChannelException:<init>	()V
    //   18: athrow
    //   19: aload_0
    //   20: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   23: dup
    //   24: astore_2
    //   25: monitorenter
    //   26: iconst_0
    //   27: istore_3
    //   28: iconst_m1
    //   29: istore 4
    //   31: aload_0
    //   32: invokevirtual 467	sun/nio/ch/FileChannelImpl:begin	()V
    //   35: aload_0
    //   36: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   39: ifne +14 -> 53
    //   42: iconst_0
    //   43: istore 5
    //   45: jsr +71 -> 116
    //   48: aload_2
    //   49: monitorexit
    //   50: iload 5
    //   52: ireturn
    //   53: aload_0
    //   54: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   57: invokevirtual 515	sun/nio/ch/NativeThreadSet:add	()I
    //   60: istore 4
    //   62: aload_0
    //   63: getfield 429	sun/nio/ch/FileChannelImpl:fd	Ljava/io/FileDescriptor;
    //   66: aload_1
    //   67: ldc2_w 205
    //   70: getstatic 434	sun/nio/ch/FileChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   73: aload_0
    //   74: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   77: invokestatic 511	sun/nio/ch/IOUtil:read	(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;JLsun/nio/ch/NativeDispatcher;Ljava/lang/Object;)I
    //   80: istore_3
    //   81: iload_3
    //   82: bipush 253
    //   84: if_icmpne +10 -> 94
    //   87: aload_0
    //   88: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   91: ifne -29 -> 62
    //   94: iload_3
    //   95: invokestatic 503	sun/nio/ch/IOStatus:normalize	(I)I
    //   98: istore 5
    //   100: jsr +16 -> 116
    //   103: aload_2
    //   104: monitorexit
    //   105: iload 5
    //   107: ireturn
    //   108: astore 6
    //   110: jsr +6 -> 116
    //   113: aload 6
    //   115: athrow
    //   116: astore 7
    //   118: aload_0
    //   119: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   122: iload 4
    //   124: invokevirtual 518	sun/nio/ch/NativeThreadSet:remove	(I)V
    //   127: aload_0
    //   128: iload_3
    //   129: ifle +7 -> 136
    //   132: iconst_1
    //   133: goto +4 -> 137
    //   136: iconst_0
    //   137: invokevirtual 474	sun/nio/ch/FileChannelImpl:end	(Z)V
    //   140: getstatic 420	sun/nio/ch/FileChannelImpl:$assertionsDisabled	Z
    //   143: ifne +18 -> 161
    //   146: iload_3
    //   147: invokestatic 504	sun/nio/ch/IOStatus:check	(I)Z
    //   150: ifne +11 -> 161
    //   153: new 217	java/lang/AssertionError
    //   156: dup
    //   157: invokespecial 440	java/lang/AssertionError:<init>	()V
    //   160: athrow
    //   161: ret 7
    //   163: astore 8
    //   165: aload_2
    //   166: monitorexit
    //   167: aload 8
    //   169: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   31	48	108	finally
    //   53	103	108	finally
    //   108	113	108	finally
    //   26	50	163	finally
    //   53	105	163	finally
    //   108	167	163	finally
  }

  // ERROR //
  private long read0(ByteBuffer[] paramArrayOfByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 468	sun/nio/ch/FileChannelImpl:ensureOpen	()V
    //   4: aload_0
    //   5: getfield 426	sun/nio/ch/FileChannelImpl:readable	Z
    //   8: ifne +11 -> 19
    //   11: new 235	java/nio/channels/NonReadableChannelException
    //   14: dup
    //   15: invokespecial 460	java/nio/channels/NonReadableChannelException:<init>	()V
    //   18: athrow
    //   19: aload_0
    //   20: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   23: dup
    //   24: astore_2
    //   25: monitorenter
    //   26: lconst_0
    //   27: lstore_3
    //   28: iconst_m1
    //   29: istore 5
    //   31: aload_0
    //   32: invokevirtual 467	sun/nio/ch/FileChannelImpl:begin	()V
    //   35: aload_0
    //   36: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   39: ifne +14 -> 53
    //   42: lconst_0
    //   43: lstore 6
    //   45: jsr +66 -> 111
    //   48: aload_2
    //   49: monitorexit
    //   50: lload 6
    //   52: lreturn
    //   53: aload_0
    //   54: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   57: invokevirtual 515	sun/nio/ch/NativeThreadSet:add	()I
    //   60: istore 5
    //   62: aload_0
    //   63: getfield 429	sun/nio/ch/FileChannelImpl:fd	Ljava/io/FileDescriptor;
    //   66: aload_1
    //   67: getstatic 434	sun/nio/ch/FileChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   70: invokestatic 509	sun/nio/ch/IOUtil:read	(Ljava/io/FileDescriptor;[Ljava/nio/ByteBuffer;Lsun/nio/ch/NativeDispatcher;)J
    //   73: lstore_3
    //   74: lload_3
    //   75: ldc2_w 203
    //   78: lcmp
    //   79: ifne +10 -> 89
    //   82: aload_0
    //   83: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   86: ifne -24 -> 62
    //   89: lload_3
    //   90: invokestatic 505	sun/nio/ch/IOStatus:normalize	(J)J
    //   93: lstore 6
    //   95: jsr +16 -> 111
    //   98: aload_2
    //   99: monitorexit
    //   100: lload 6
    //   102: lreturn
    //   103: astore 8
    //   105: jsr +6 -> 111
    //   108: aload 8
    //   110: athrow
    //   111: astore 9
    //   113: aload_0
    //   114: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   117: iload 5
    //   119: invokevirtual 518	sun/nio/ch/NativeThreadSet:remove	(I)V
    //   122: aload_0
    //   123: lload_3
    //   124: lconst_0
    //   125: lcmp
    //   126: ifle +7 -> 133
    //   129: iconst_1
    //   130: goto +4 -> 134
    //   133: iconst_0
    //   134: invokevirtual 474	sun/nio/ch/FileChannelImpl:end	(Z)V
    //   137: getstatic 420	sun/nio/ch/FileChannelImpl:$assertionsDisabled	Z
    //   140: ifne +18 -> 158
    //   143: lload_3
    //   144: invokestatic 506	sun/nio/ch/IOStatus:check	(J)Z
    //   147: ifne +11 -> 158
    //   150: new 217	java/lang/AssertionError
    //   153: dup
    //   154: invokespecial 440	java/lang/AssertionError:<init>	()V
    //   157: athrow
    //   158: ret 9
    //   160: astore 10
    //   162: aload_2
    //   163: monitorexit
    //   164: aload 10
    //   166: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   31	48	103	finally
    //   53	98	103	finally
    //   103	108	103	finally
    //   26	50	160	finally
    //   53	100	160	finally
    //   103	164	160	finally
  }

  public long read(ByteBuffer[] paramArrayOfByteBuffer, int paramInt1, int paramInt2)
    throws IOException
  {
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 > paramArrayOfByteBuffer.length - paramInt2))
      throw new IndexOutOfBoundsException();
    return read0(Util.subsequence(paramArrayOfByteBuffer, paramInt1, paramInt2));
  }

  // ERROR //
  public int write(ByteBuffer paramByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 468	sun/nio/ch/FileChannelImpl:ensureOpen	()V
    //   4: aload_0
    //   5: getfield 428	sun/nio/ch/FileChannelImpl:writable	Z
    //   8: ifne +11 -> 19
    //   11: new 236	java/nio/channels/NonWritableChannelException
    //   14: dup
    //   15: invokespecial 461	java/nio/channels/NonWritableChannelException:<init>	()V
    //   18: athrow
    //   19: aload_0
    //   20: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   23: dup
    //   24: astore_2
    //   25: monitorenter
    //   26: iconst_0
    //   27: istore_3
    //   28: iconst_m1
    //   29: istore 4
    //   31: aload_0
    //   32: invokevirtual 467	sun/nio/ch/FileChannelImpl:begin	()V
    //   35: aload_0
    //   36: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   39: ifne +14 -> 53
    //   42: iconst_0
    //   43: istore 5
    //   45: jsr +87 -> 132
    //   48: aload_2
    //   49: monitorexit
    //   50: iload 5
    //   52: ireturn
    //   53: aload_0
    //   54: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   57: invokevirtual 515	sun/nio/ch/NativeThreadSet:add	()I
    //   60: istore 4
    //   62: aload_0
    //   63: getfield 421	sun/nio/ch/FileChannelImpl:appending	Z
    //   66: ifeq +12 -> 78
    //   69: aload_0
    //   70: aload_0
    //   71: invokevirtual 466	sun/nio/ch/FileChannelImpl:size	()J
    //   74: invokevirtual 486	sun/nio/ch/FileChannelImpl:position	(J)Ljava/nio/channels/FileChannel;
    //   77: pop
    //   78: aload_0
    //   79: getfield 429	sun/nio/ch/FileChannelImpl:fd	Ljava/io/FileDescriptor;
    //   82: aload_1
    //   83: ldc2_w 205
    //   86: getstatic 434	sun/nio/ch/FileChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   89: aload_0
    //   90: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   93: invokestatic 512	sun/nio/ch/IOUtil:write	(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;JLsun/nio/ch/NativeDispatcher;Ljava/lang/Object;)I
    //   96: istore_3
    //   97: iload_3
    //   98: bipush 253
    //   100: if_icmpne +10 -> 110
    //   103: aload_0
    //   104: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   107: ifne -29 -> 78
    //   110: iload_3
    //   111: invokestatic 503	sun/nio/ch/IOStatus:normalize	(I)I
    //   114: istore 5
    //   116: jsr +16 -> 132
    //   119: aload_2
    //   120: monitorexit
    //   121: iload 5
    //   123: ireturn
    //   124: astore 6
    //   126: jsr +6 -> 132
    //   129: aload 6
    //   131: athrow
    //   132: astore 7
    //   134: aload_0
    //   135: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   138: iload 4
    //   140: invokevirtual 518	sun/nio/ch/NativeThreadSet:remove	(I)V
    //   143: aload_0
    //   144: iload_3
    //   145: ifle +7 -> 152
    //   148: iconst_1
    //   149: goto +4 -> 153
    //   152: iconst_0
    //   153: invokevirtual 474	sun/nio/ch/FileChannelImpl:end	(Z)V
    //   156: getstatic 420	sun/nio/ch/FileChannelImpl:$assertionsDisabled	Z
    //   159: ifne +18 -> 177
    //   162: iload_3
    //   163: invokestatic 504	sun/nio/ch/IOStatus:check	(I)Z
    //   166: ifne +11 -> 177
    //   169: new 217	java/lang/AssertionError
    //   172: dup
    //   173: invokespecial 440	java/lang/AssertionError:<init>	()V
    //   176: athrow
    //   177: ret 7
    //   179: astore 8
    //   181: aload_2
    //   182: monitorexit
    //   183: aload 8
    //   185: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   31	48	124	finally
    //   53	119	124	finally
    //   124	129	124	finally
    //   26	50	179	finally
    //   53	121	179	finally
    //   124	183	179	finally
  }

  // ERROR //
  private long write0(ByteBuffer[] paramArrayOfByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 468	sun/nio/ch/FileChannelImpl:ensureOpen	()V
    //   4: aload_0
    //   5: getfield 428	sun/nio/ch/FileChannelImpl:writable	Z
    //   8: ifne +11 -> 19
    //   11: new 236	java/nio/channels/NonWritableChannelException
    //   14: dup
    //   15: invokespecial 461	java/nio/channels/NonWritableChannelException:<init>	()V
    //   18: athrow
    //   19: aload_0
    //   20: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   23: dup
    //   24: astore_2
    //   25: monitorenter
    //   26: lconst_0
    //   27: lstore_3
    //   28: iconst_m1
    //   29: istore 5
    //   31: aload_0
    //   32: invokevirtual 467	sun/nio/ch/FileChannelImpl:begin	()V
    //   35: aload_0
    //   36: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   39: ifne +14 -> 53
    //   42: lconst_0
    //   43: lstore 6
    //   45: jsr +82 -> 127
    //   48: aload_2
    //   49: monitorexit
    //   50: lload 6
    //   52: lreturn
    //   53: aload_0
    //   54: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   57: invokevirtual 515	sun/nio/ch/NativeThreadSet:add	()I
    //   60: istore 5
    //   62: aload_0
    //   63: getfield 421	sun/nio/ch/FileChannelImpl:appending	Z
    //   66: ifeq +12 -> 78
    //   69: aload_0
    //   70: aload_0
    //   71: invokevirtual 466	sun/nio/ch/FileChannelImpl:size	()J
    //   74: invokevirtual 486	sun/nio/ch/FileChannelImpl:position	(J)Ljava/nio/channels/FileChannel;
    //   77: pop
    //   78: aload_0
    //   79: getfield 429	sun/nio/ch/FileChannelImpl:fd	Ljava/io/FileDescriptor;
    //   82: aload_1
    //   83: getstatic 434	sun/nio/ch/FileChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   86: invokestatic 510	sun/nio/ch/IOUtil:write	(Ljava/io/FileDescriptor;[Ljava/nio/ByteBuffer;Lsun/nio/ch/NativeDispatcher;)J
    //   89: lstore_3
    //   90: lload_3
    //   91: ldc2_w 203
    //   94: lcmp
    //   95: ifne +10 -> 105
    //   98: aload_0
    //   99: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   102: ifne -24 -> 78
    //   105: lload_3
    //   106: invokestatic 505	sun/nio/ch/IOStatus:normalize	(J)J
    //   109: lstore 6
    //   111: jsr +16 -> 127
    //   114: aload_2
    //   115: monitorexit
    //   116: lload 6
    //   118: lreturn
    //   119: astore 8
    //   121: jsr +6 -> 127
    //   124: aload 8
    //   126: athrow
    //   127: astore 9
    //   129: aload_0
    //   130: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   133: iload 5
    //   135: invokevirtual 518	sun/nio/ch/NativeThreadSet:remove	(I)V
    //   138: aload_0
    //   139: lload_3
    //   140: lconst_0
    //   141: lcmp
    //   142: ifle +7 -> 149
    //   145: iconst_1
    //   146: goto +4 -> 150
    //   149: iconst_0
    //   150: invokevirtual 474	sun/nio/ch/FileChannelImpl:end	(Z)V
    //   153: getstatic 420	sun/nio/ch/FileChannelImpl:$assertionsDisabled	Z
    //   156: ifne +18 -> 174
    //   159: lload_3
    //   160: invokestatic 506	sun/nio/ch/IOStatus:check	(J)Z
    //   163: ifne +11 -> 174
    //   166: new 217	java/lang/AssertionError
    //   169: dup
    //   170: invokespecial 440	java/lang/AssertionError:<init>	()V
    //   173: athrow
    //   174: ret 9
    //   176: astore 10
    //   178: aload_2
    //   179: monitorexit
    //   180: aload 10
    //   182: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   31	48	119	finally
    //   53	114	119	finally
    //   119	124	119	finally
    //   26	50	176	finally
    //   53	116	176	finally
    //   119	180	176	finally
  }

  public long write(ByteBuffer[] paramArrayOfByteBuffer, int paramInt1, int paramInt2)
    throws IOException
  {
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 > paramArrayOfByteBuffer.length - paramInt2))
      throw new IndexOutOfBoundsException();
    return write0(Util.subsequence(paramArrayOfByteBuffer, paramInt1, paramInt2));
  }

  // ERROR //
  public long position()
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 468	sun/nio/ch/FileChannelImpl:ensureOpen	()V
    //   4: aload_0
    //   5: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   8: dup
    //   9: astore_1
    //   10: monitorenter
    //   11: ldc2_w 205
    //   14: lstore_2
    //   15: iconst_m1
    //   16: istore 4
    //   18: aload_0
    //   19: invokevirtual 467	sun/nio/ch/FileChannelImpl:begin	()V
    //   22: aload_0
    //   23: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   26: ifne +14 -> 40
    //   29: lconst_0
    //   30: lstore 5
    //   32: jsr +66 -> 98
    //   35: aload_1
    //   36: monitorexit
    //   37: lload 5
    //   39: lreturn
    //   40: aload_0
    //   41: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   44: invokevirtual 515	sun/nio/ch/NativeThreadSet:add	()I
    //   47: istore 4
    //   49: aload_0
    //   50: aload_0
    //   51: getfield 429	sun/nio/ch/FileChannelImpl:fd	Ljava/io/FileDescriptor;
    //   54: ldc2_w 205
    //   57: invokespecial 477	sun/nio/ch/FileChannelImpl:position0	(Ljava/io/FileDescriptor;J)J
    //   60: lstore_2
    //   61: lload_2
    //   62: ldc2_w 203
    //   65: lcmp
    //   66: ifne +10 -> 76
    //   69: aload_0
    //   70: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   73: ifne -24 -> 49
    //   76: lload_2
    //   77: invokestatic 505	sun/nio/ch/IOStatus:normalize	(J)J
    //   80: lstore 5
    //   82: jsr +16 -> 98
    //   85: aload_1
    //   86: monitorexit
    //   87: lload 5
    //   89: lreturn
    //   90: astore 7
    //   92: jsr +6 -> 98
    //   95: aload 7
    //   97: athrow
    //   98: astore 8
    //   100: aload_0
    //   101: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   104: iload 4
    //   106: invokevirtual 518	sun/nio/ch/NativeThreadSet:remove	(I)V
    //   109: aload_0
    //   110: lload_2
    //   111: ldc2_w 205
    //   114: lcmp
    //   115: ifle +7 -> 122
    //   118: iconst_1
    //   119: goto +4 -> 123
    //   122: iconst_0
    //   123: invokevirtual 474	sun/nio/ch/FileChannelImpl:end	(Z)V
    //   126: getstatic 420	sun/nio/ch/FileChannelImpl:$assertionsDisabled	Z
    //   129: ifne +18 -> 147
    //   132: lload_2
    //   133: invokestatic 506	sun/nio/ch/IOStatus:check	(J)Z
    //   136: ifne +11 -> 147
    //   139: new 217	java/lang/AssertionError
    //   142: dup
    //   143: invokespecial 440	java/lang/AssertionError:<init>	()V
    //   146: athrow
    //   147: ret 8
    //   149: astore 9
    //   151: aload_1
    //   152: monitorexit
    //   153: aload 9
    //   155: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   18	35	90	finally
    //   40	85	90	finally
    //   90	95	90	finally
    //   11	37	149	finally
    //   40	87	149	finally
    //   90	153	149	finally
  }

  // ERROR //
  public FileChannel position(long paramLong)
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 468	sun/nio/ch/FileChannelImpl:ensureOpen	()V
    //   4: lload_1
    //   5: lconst_0
    //   6: lcmp
    //   7: ifge +11 -> 18
    //   10: new 219	java/lang/IllegalArgumentException
    //   13: dup
    //   14: invokespecial 442	java/lang/IllegalArgumentException:<init>	()V
    //   17: athrow
    //   18: aload_0
    //   19: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   22: dup
    //   23: astore_3
    //   24: monitorenter
    //   25: ldc2_w 205
    //   28: lstore 4
    //   30: iconst_m1
    //   31: istore 6
    //   33: aload_0
    //   34: invokevirtual 467	sun/nio/ch/FileChannelImpl:begin	()V
    //   37: aload_0
    //   38: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   41: ifne +14 -> 55
    //   44: aconst_null
    //   45: astore 7
    //   47: jsr +63 -> 110
    //   50: aload_3
    //   51: monitorexit
    //   52: aload 7
    //   54: areturn
    //   55: aload_0
    //   56: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   59: invokevirtual 515	sun/nio/ch/NativeThreadSet:add	()I
    //   62: istore 6
    //   64: aload_0
    //   65: aload_0
    //   66: getfield 429	sun/nio/ch/FileChannelImpl:fd	Ljava/io/FileDescriptor;
    //   69: lload_1
    //   70: invokespecial 477	sun/nio/ch/FileChannelImpl:position0	(Ljava/io/FileDescriptor;J)J
    //   73: lstore 4
    //   75: lload 4
    //   77: ldc2_w 203
    //   80: lcmp
    //   81: ifne +10 -> 91
    //   84: aload_0
    //   85: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   88: ifne -24 -> 64
    //   91: aload_0
    //   92: astore 7
    //   94: jsr +16 -> 110
    //   97: aload_3
    //   98: monitorexit
    //   99: aload 7
    //   101: areturn
    //   102: astore 8
    //   104: jsr +6 -> 110
    //   107: aload 8
    //   109: athrow
    //   110: astore 9
    //   112: aload_0
    //   113: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   116: iload 6
    //   118: invokevirtual 518	sun/nio/ch/NativeThreadSet:remove	(I)V
    //   121: aload_0
    //   122: lload 4
    //   124: ldc2_w 205
    //   127: lcmp
    //   128: ifle +7 -> 135
    //   131: iconst_1
    //   132: goto +4 -> 136
    //   135: iconst_0
    //   136: invokevirtual 474	sun/nio/ch/FileChannelImpl:end	(Z)V
    //   139: getstatic 420	sun/nio/ch/FileChannelImpl:$assertionsDisabled	Z
    //   142: ifne +19 -> 161
    //   145: lload 4
    //   147: invokestatic 506	sun/nio/ch/IOStatus:check	(J)Z
    //   150: ifne +11 -> 161
    //   153: new 217	java/lang/AssertionError
    //   156: dup
    //   157: invokespecial 440	java/lang/AssertionError:<init>	()V
    //   160: athrow
    //   161: ret 9
    //   163: astore 10
    //   165: aload_3
    //   166: monitorexit
    //   167: aload 10
    //   169: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   33	50	102	finally
    //   55	97	102	finally
    //   102	107	102	finally
    //   25	52	163	finally
    //   55	99	163	finally
    //   102	167	163	finally
  }

  // ERROR //
  public long size()
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 468	sun/nio/ch/FileChannelImpl:ensureOpen	()V
    //   4: aload_0
    //   5: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   8: dup
    //   9: astore_1
    //   10: monitorenter
    //   11: ldc2_w 205
    //   14: lstore_2
    //   15: iconst_m1
    //   16: istore 4
    //   18: aload_0
    //   19: invokevirtual 467	sun/nio/ch/FileChannelImpl:begin	()V
    //   22: aload_0
    //   23: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   26: ifne +16 -> 42
    //   29: ldc2_w 205
    //   32: lstore 5
    //   34: jsr +63 -> 97
    //   37: aload_1
    //   38: monitorexit
    //   39: lload 5
    //   41: lreturn
    //   42: aload_0
    //   43: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   46: invokevirtual 515	sun/nio/ch/NativeThreadSet:add	()I
    //   49: istore 4
    //   51: aload_0
    //   52: aload_0
    //   53: getfield 429	sun/nio/ch/FileChannelImpl:fd	Ljava/io/FileDescriptor;
    //   56: invokespecial 475	sun/nio/ch/FileChannelImpl:size0	(Ljava/io/FileDescriptor;)J
    //   59: lstore_2
    //   60: lload_2
    //   61: ldc2_w 203
    //   64: lcmp
    //   65: ifne +10 -> 75
    //   68: aload_0
    //   69: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   72: ifne -21 -> 51
    //   75: lload_2
    //   76: invokestatic 505	sun/nio/ch/IOStatus:normalize	(J)J
    //   79: lstore 5
    //   81: jsr +16 -> 97
    //   84: aload_1
    //   85: monitorexit
    //   86: lload 5
    //   88: lreturn
    //   89: astore 7
    //   91: jsr +6 -> 97
    //   94: aload 7
    //   96: athrow
    //   97: astore 8
    //   99: aload_0
    //   100: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   103: iload 4
    //   105: invokevirtual 518	sun/nio/ch/NativeThreadSet:remove	(I)V
    //   108: aload_0
    //   109: lload_2
    //   110: ldc2_w 205
    //   113: lcmp
    //   114: ifle +7 -> 121
    //   117: iconst_1
    //   118: goto +4 -> 122
    //   121: iconst_0
    //   122: invokevirtual 474	sun/nio/ch/FileChannelImpl:end	(Z)V
    //   125: getstatic 420	sun/nio/ch/FileChannelImpl:$assertionsDisabled	Z
    //   128: ifne +18 -> 146
    //   131: lload_2
    //   132: invokestatic 506	sun/nio/ch/IOStatus:check	(J)Z
    //   135: ifne +11 -> 146
    //   138: new 217	java/lang/AssertionError
    //   141: dup
    //   142: invokespecial 440	java/lang/AssertionError:<init>	()V
    //   145: athrow
    //   146: ret 8
    //   148: astore 9
    //   150: aload_1
    //   151: monitorexit
    //   152: aload 9
    //   154: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   18	37	89	finally
    //   42	84	89	finally
    //   89	94	89	finally
    //   11	39	148	finally
    //   42	86	148	finally
    //   89	152	148	finally
  }

  // ERROR //
  public FileChannel truncate(long paramLong)
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 468	sun/nio/ch/FileChannelImpl:ensureOpen	()V
    //   4: lload_1
    //   5: lconst_0
    //   6: lcmp
    //   7: ifge +11 -> 18
    //   10: new 219	java/lang/IllegalArgumentException
    //   13: dup
    //   14: invokespecial 442	java/lang/IllegalArgumentException:<init>	()V
    //   17: athrow
    //   18: lload_1
    //   19: aload_0
    //   20: invokevirtual 466	sun/nio/ch/FileChannelImpl:size	()J
    //   23: lcmp
    //   24: ifle +5 -> 29
    //   27: aload_0
    //   28: areturn
    //   29: aload_0
    //   30: getfield 428	sun/nio/ch/FileChannelImpl:writable	Z
    //   33: ifne +11 -> 44
    //   36: new 236	java/nio/channels/NonWritableChannelException
    //   39: dup
    //   40: invokespecial 461	java/nio/channels/NonWritableChannelException:<init>	()V
    //   43: athrow
    //   44: aload_0
    //   45: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   48: dup
    //   49: astore_3
    //   50: monitorenter
    //   51: iconst_m1
    //   52: istore 4
    //   54: iconst_m1
    //   55: istore 5
    //   57: aload_0
    //   58: invokevirtual 467	sun/nio/ch/FileChannelImpl:begin	()V
    //   61: aload_0
    //   62: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   65: ifne +14 -> 79
    //   68: aconst_null
    //   69: astore 6
    //   71: jsr +61 -> 132
    //   74: aload_3
    //   75: monitorexit
    //   76: aload 6
    //   78: areturn
    //   79: aload_0
    //   80: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   83: invokevirtual 515	sun/nio/ch/NativeThreadSet:add	()I
    //   86: istore 5
    //   88: aload_0
    //   89: aload_0
    //   90: getfield 429	sun/nio/ch/FileChannelImpl:fd	Ljava/io/FileDescriptor;
    //   93: lload_1
    //   94: invokespecial 476	sun/nio/ch/FileChannelImpl:truncate0	(Ljava/io/FileDescriptor;J)I
    //   97: istore 4
    //   99: iload 4
    //   101: bipush 253
    //   103: if_icmpne +10 -> 113
    //   106: aload_0
    //   107: invokevirtual 469	sun/nio/ch/FileChannelImpl:isOpen	()Z
    //   110: ifne -22 -> 88
    //   113: aload_0
    //   114: astore 6
    //   116: jsr +16 -> 132
    //   119: aload_3
    //   120: monitorexit
    //   121: aload 6
    //   123: areturn
    //   124: astore 7
    //   126: jsr +6 -> 132
    //   129: aload 7
    //   131: athrow
    //   132: astore 8
    //   134: aload_0
    //   135: getfield 435	sun/nio/ch/FileChannelImpl:threads	Lsun/nio/ch/NativeThreadSet;
    //   138: iload 5
    //   140: invokevirtual 518	sun/nio/ch/NativeThreadSet:remove	(I)V
    //   143: aload_0
    //   144: iload 4
    //   146: iconst_m1
    //   147: if_icmple +7 -> 154
    //   150: iconst_1
    //   151: goto +4 -> 155
    //   154: iconst_0
    //   155: invokevirtual 474	sun/nio/ch/FileChannelImpl:end	(Z)V
    //   158: getstatic 420	sun/nio/ch/FileChannelImpl:$assertionsDisabled	Z
    //   161: ifne +19 -> 180
    //   164: iload 4
    //   166: invokestatic 504	sun/nio/ch/IOStatus:check	(I)Z
    //   169: ifne +11 -> 180
    //   172: new 217	java/lang/AssertionError
    //   175: dup
    //   176: invokespecial 440	java/lang/AssertionError:<init>	()V
    //   179: athrow
    //   180: ret 8
    //   182: astore 9
    //   184: aload_3
    //   185: monitorexit
    //   186: aload 9
    //   188: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   57	74	124	finally
    //   79	119	124	finally
    //   124	129	124	finally
    //   51	76	182	finally
    //   79	121	182	finally
    //   124	186	182	finally
  }

  public void force(boolean paramBoolean)
    throws IOException
  {
    ensureOpen();
    int i = -1;
    int j = -1;
    try
    {
      begin();
      if (!(isOpen()))
      {
        jsr 49;
        return;
      }
      j = this.threads.add();
      do
      {
        i = force0(this.fd, paramBoolean);
        if (i != -3)
          break;
      }
      while (isOpen());
    }
    finally
    {
      this.threads.remove(j);
      end(i > -1);
      if ((!($assertionsDisabled)) && (!(IOStatus.check(i))))
        throw new AssertionError();
    }
  }

  private long transferToDirectly(long paramLong, int paramInt, WritableByteChannel paramWritableByteChannel)
    throws IOException
  {
    if (!(transferSupported))
      return -4L;
    FileDescriptor localFileDescriptor = null;
    if (paramWritableByteChannel instanceof FileChannelImpl)
    {
      if (!(fileSupported))
        return -6L;
      localFileDescriptor = ((FileChannelImpl)paramWritableByteChannel).fd;
    }
    else if (paramWritableByteChannel instanceof SelChImpl)
    {
      if ((paramWritableByteChannel instanceof SinkChannelImpl) && (!(pipeSupported)))
        return -6L;
      localFileDescriptor = ((SelChImpl)paramWritableByteChannel).getFD();
    }
    if (localFileDescriptor == null)
      return -4L;
    int i = IOUtil.fdVal(this.fd);
    int j = IOUtil.fdVal(localFileDescriptor);
    if (i == j)
      return -4L;
    long l1 = -1L;
    int k = -1;
    try
    {
      begin();
      if (!(isOpen()))
      {
        l2 = -1L;
        jsr 133;
        return l2;
      }
      k = this.threads.add();
      do
        l1 = transferTo0(i, paramLong, paramInt, j);
      while ((l1 == -3L) && (isOpen()));
      if (l1 == -6L)
      {
        if (paramWritableByteChannel instanceof SinkChannelImpl)
          pipeSupported = false;
        if (paramWritableByteChannel instanceof FileChannelImpl)
          fileSupported = false;
        l2 = -6L;
        jsr 51;
        return l2;
      }
      if (l1 == -4L)
      {
        transferSupported = false;
        l2 = -4L;
        jsr 27;
        return l2;
      }
      long l2 = IOStatus.normalize(l1);
      return l2;
    }
    finally
    {
      this.threads.remove(k);
      end(l1 > -1L);
    }
  }

  private long transferToTrustedChannel(long paramLong, int paramInt, WritableByteChannel paramWritableByteChannel)
    throws IOException
  {
    if ((!(paramWritableByteChannel instanceof FileChannelImpl)) && (!(paramWritableByteChannel instanceof SelChImpl)))
      return -4L;
    MappedByteBuffer localMappedByteBuffer = null;
    try
    {
      localMappedByteBuffer = map(FileChannel.MapMode.READ_ONLY, paramLong, paramInt);
      long l = paramWritableByteChannel.write(localMappedByteBuffer);
      return l;
    }
    finally
    {
      if (localMappedByteBuffer != null)
        unmap(localMappedByteBuffer);
    }
  }

  private long transferToArbitraryChannel(long paramLong, int paramInt, WritableByteChannel paramWritableByteChannel)
    throws IOException
  {
    int i = Math.min(paramInt, 8192);
    ByteBuffer localByteBuffer = Util.getTemporaryDirectBuffer(i);
    long l1 = 3412047291253522432L;
    long l2 = paramLong;
    try
    {
      Util.erase(localByteBuffer);
      while (l1 < paramInt)
      {
        localByteBuffer.limit(Math.min((int)(paramInt - l1), 8192));
        int j = read(localByteBuffer, l2);
        if (j <= 0)
          break;
        localByteBuffer.flip();
        int k = paramWritableByteChannel.write(localByteBuffer);
        l1 += k;
        if (k != j)
          break;
        l2 += k;
        localByteBuffer.clear();
      }
      long l3 = l1;
      return l3;
    }
    catch (IOException localIOException)
    {
      long l4;
      if (l1 > 3412047239713914880L)
      {
        l4 = l1;
        jsr 17;
      }
      throw localIOException;
    }
    finally
    {
      Util.releaseTemporaryDirectBuffer(localByteBuffer);
    }
  }

  public long transferTo(long paramLong1, long paramLong2, WritableByteChannel paramWritableByteChannel)
    throws IOException
  {
    long l2;
    ensureOpen();
    if (!(paramWritableByteChannel.isOpen()))
      throw new ClosedChannelException();
    if (!(this.readable))
      throw new NonReadableChannelException();
    if ((paramWritableByteChannel instanceof FileChannelImpl) && (!(((FileChannelImpl)paramWritableByteChannel).writable)))
      throw new NonWritableChannelException();
    if ((paramLong1 < 3412046964836007936L) || (paramLong2 < 3412046964836007936L))
      throw new IllegalArgumentException();
    long l1 = size();
    if (paramLong1 > l1)
      return 3412047463052214272L;
    int i = (int)Math.min(paramLong2, 2147483647L);
    if (l1 - paramLong1 < i)
      i = (int)(l1 - paramLong1);
    if ((l2 = transferToDirectly(paramLong1, i, paramWritableByteChannel)) >= 3412046810217185280L)
      return l2;
    if ((l2 = transferToTrustedChannel(paramLong1, i, paramWritableByteChannel)) >= 3412046810217185280L)
      return l2;
    return transferToArbitraryChannel(paramLong1, i, paramWritableByteChannel);
  }

  // ERROR //
  private long transferFromFileChannel(FileChannelImpl paramFileChannelImpl, long paramLong1, long paramLong2)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: getfield 431	sun/nio/ch/FileChannelImpl:positionLock	Ljava/lang/Object;
    //   4: dup
    //   5: astore 6
    //   7: monitorenter
    //   8: aload_1
    //   9: invokevirtual 465	sun/nio/ch/FileChannelImpl:position	()J
    //   12: lstore 7
    //   14: lload 4
    //   16: ldc2_w 211
    //   19: invokestatic 446	java/lang/Math:min	(JJ)J
    //   22: aload_1
    //   23: invokevirtual 466	sun/nio/ch/FileChannelImpl:size	()J
    //   26: lload 7
    //   28: lsub
    //   29: invokestatic 446	java/lang/Math:min	(JJ)J
    //   32: l2i
    //   33: istore 9
    //   35: aload_1
    //   36: getstatic 417	java/nio/channels/FileChannel$MapMode:READ_ONLY	Ljava/nio/channels/FileChannel$MapMode;
    //   39: lload 7
    //   41: iload 9
    //   43: i2l
    //   44: invokevirtual 494	sun/nio/ch/FileChannelImpl:map	(Ljava/nio/channels/FileChannel$MapMode;JJ)Ljava/nio/MappedByteBuffer;
    //   47: astore 10
    //   49: aload_0
    //   50: aload 10
    //   52: lload_2
    //   53: invokevirtual 482	sun/nio/ch/FileChannelImpl:write	(Ljava/nio/ByteBuffer;J)I
    //   56: i2l
    //   57: lstore 11
    //   59: aload_1
    //   60: lload 7
    //   62: lload 11
    //   64: ladd
    //   65: invokevirtual 486	sun/nio/ch/FileChannelImpl:position	(J)Ljava/nio/channels/FileChannel;
    //   68: pop
    //   69: lload 11
    //   71: lstore 13
    //   73: jsr +17 -> 90
    //   76: aload 6
    //   78: monitorexit
    //   79: lload 13
    //   81: lreturn
    //   82: astore 15
    //   84: jsr +6 -> 90
    //   87: aload 15
    //   89: athrow
    //   90: astore 16
    //   92: aload 10
    //   94: invokestatic 485	sun/nio/ch/FileChannelImpl:unmap	(Ljava/nio/MappedByteBuffer;)V
    //   97: ret 16
    //   99: astore 17
    //   101: aload 6
    //   103: monitorexit
    //   104: aload 17
    //   106: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   49	76	82	finally
    //   82	87	82	finally
    //   8	79	99	finally
    //   82	104	99	finally
  }

  private long transferFromArbitraryChannel(ReadableByteChannel paramReadableByteChannel, long paramLong1, long paramLong2)
    throws IOException
  {
    int i = (int)Math.min(paramLong2, 8192L);
    ByteBuffer localByteBuffer = Util.getTemporaryDirectBuffer(i);
    long l1 = 3412047291253522432L;
    long l2 = paramLong1;
    try
    {
      Util.erase(localByteBuffer);
      while (l1 < paramLong2)
      {
        localByteBuffer.limit((int)Math.min(paramLong2 - l1, 8192L));
        int j = paramReadableByteChannel.read(localByteBuffer);
        if (j <= 0)
          break;
        localByteBuffer.flip();
        int k = write(localByteBuffer, l2);
        l1 += k;
        if (k != j)
          break;
        l2 += k;
        localByteBuffer.clear();
      }
      long l3 = l1;
      return l3;
    }
    catch (IOException localIOException)
    {
      long l4;
      if (l1 > 3412047239713914880L)
      {
        l4 = l1;
        jsr 17;
      }
      throw localIOException;
    }
    finally
    {
      Util.releaseTemporaryDirectBuffer(localByteBuffer);
    }
  }

  public long transferFrom(ReadableByteChannel paramReadableByteChannel, long paramLong1, long paramLong2)
    throws IOException
  {
    ensureOpen();
    if (!(paramReadableByteChannel.isOpen()))
      throw new ClosedChannelException();
    if (!(this.writable))
      throw new NonWritableChannelException();
    if ((paramLong1 < 3412046964836007936L) || (paramLong2 < 3412046964836007936L))
      throw new IllegalArgumentException();
    if (paramLong1 > size())
      return 3412047463052214272L;
    if (paramReadableByteChannel instanceof FileChannelImpl)
      return transferFromFileChannel((FileChannelImpl)paramReadableByteChannel, paramLong1, paramLong2);
    return transferFromArbitraryChannel(paramReadableByteChannel, paramLong1, paramLong2);
  }

  public int read(ByteBuffer paramByteBuffer, long paramLong)
    throws IOException
  {
    if (paramByteBuffer == null)
      throw new NullPointerException();
    if (paramLong < 3412046810217185280L)
      throw new IllegalArgumentException("Negative position");
    if (!(this.readable))
      throw new NonReadableChannelException();
    ensureOpen();
    int i = 0;
    int j = -1;
    try
    {
      begin();
      if (!(isOpen()))
      {
        k = -1;
        jsr 68;
        return k;
      }
      j = this.threads.add();
      do
        i = IOUtil.read(this.fd, paramByteBuffer, paramLong, nd, this.positionLock);
      while ((i == -3) && (isOpen()));
      int k = IOStatus.normalize(i);
      return k;
    }
    finally
    {
      this.threads.remove(j);
      end(i > 0);
      if ((!($assertionsDisabled)) && (!(IOStatus.check(i))))
        throw new AssertionError();
    }
  }

  public int write(ByteBuffer paramByteBuffer, long paramLong)
    throws IOException
  {
    if (paramByteBuffer == null)
      throw new NullPointerException();
    if (paramLong < 3412046810217185280L)
      throw new IllegalArgumentException("Negative position");
    if (!(this.writable))
      throw new NonWritableChannelException();
    ensureOpen();
    int i = 0;
    int j = -1;
    try
    {
      begin();
      if (!(isOpen()))
      {
        k = -1;
        jsr 68;
        return k;
      }
      j = this.threads.add();
      do
        i = IOUtil.write(this.fd, paramByteBuffer, paramLong, nd, this.positionLock);
      while ((i == -3) && (isOpen()));
      int k = IOStatus.normalize(i);
      return k;
    }
    finally
    {
      this.threads.remove(j);
      end(i > 0);
      if ((!($assertionsDisabled)) && (!(IOStatus.check(i))))
        throw new AssertionError();
    }
  }

  private static void unmap(MappedByteBuffer paramMappedByteBuffer)
  {
    Cleaner localCleaner = ((DirectBuffer)paramMappedByteBuffer).cleaner();
    if (localCleaner != null)
      localCleaner.clean();
  }

  public MappedByteBuffer map(FileChannel.MapMode paramMapMode, long paramLong1, long paramLong2)
    throws IOException
  {
    ensureOpen();
    if (paramLong1 < 3412046810217185280L)
      throw new IllegalArgumentException("Negative position");
    if (paramLong2 < 3412046810217185280L)
      throw new IllegalArgumentException("Negative size");
    if (paramLong1 + paramLong2 < 3412046810217185280L)
      throw new IllegalArgumentException("Position + size overflow");
    if (paramLong2 > 2147483647L)
      throw new IllegalArgumentException("Size exceeds Integer.MAX_VALUE");
    int i = -1;
    if (paramMapMode == FileChannel.MapMode.READ_ONLY)
      i = 0;
    else if (paramMapMode == FileChannel.MapMode.READ_WRITE)
      i = 1;
    else if (paramMapMode == FileChannel.MapMode.PRIVATE)
      i = 2;
    if ((!($assertionsDisabled)) && (i < 0))
      throw new AssertionError();
    if ((paramMapMode != FileChannel.MapMode.READ_ONLY) && (!(this.writable)))
      throw new NonWritableChannelException();
    if (!(this.readable))
      throw new NonReadableChannelException();
    long l1 = -1L;
    int j = -1;
    try
    {
      begin();
      if (!(isOpen()))
      {
        Object localObject1 = null;
        jsr 326;
        return localObject1;
      }
      j = this.threads.add();
      if (size() < paramLong1 + paramLong2)
      {
        int k;
        do
          k = truncate0(this.fd, paramLong1 + paramLong2);
        while ((k == -3) && (isOpen()));
      }
      if (paramLong2 == 3412047239713914880L)
      {
        l1 = 3412048098707374080L;
        if ((!(this.writable)) || (i == 0))
        {
          localMappedByteBuffer1 = Util.newMappedByteBufferR(0, 3412040591104540672L, null);
          jsr 241;
          return localMappedByteBuffer1;
        }
        MappedByteBuffer localMappedByteBuffer1 = Util.newMappedByteBuffer(0, 3412040350586372096L, null);
        jsr 227;
        return localMappedByteBuffer1;
      }
      int l = (int)(paramLong1 % allocationGranularity);
      long l2 = paramLong1 - l;
      long l3 = paramLong2 + l;
      try
      {
        l1 = map0(i, l2, l3);
      }
      catch (OutOfMemoryError localOutOfMemoryError1)
      {
        System.gc();
        try
        {
          Thread.sleep(100L);
        }
        catch (InterruptedException localInterruptedException)
        {
          Thread.currentThread().interrupt();
        }
        try
        {
          l1 = map0(i, l2, l3);
        }
        catch (OutOfMemoryError localOutOfMemoryError2)
        {
          throw new IOException("Map failed", localOutOfMemoryError2);
        }
      }
      if ((!($assertionsDisabled)) && (!(IOStatus.checkAll(l1))))
        throw new AssertionError();
      if ((!($assertionsDisabled)) && (l1 % allocationGranularity != 3412047531771691008L))
        throw new AssertionError();
      int i1 = (int)paramLong2;
      Unmapper localUnmapper = new Unmapper(l1, paramLong2 + l, null);
      if ((!(this.writable)) || (i == 0))
      {
        localMappedByteBuffer2 = Util.newMappedByteBufferR(i1, l1 + l, localUnmapper);
        jsr 35;
        return localMappedByteBuffer2;
      }
      MappedByteBuffer localMappedByteBuffer2 = Util.newMappedByteBuffer(i1, l1 + l, localUnmapper);
      return localMappedByteBuffer2;
    }
    finally
    {
      this.threads.remove(j);
      end(IOStatus.checkAll(l1));
    }
  }

  private static boolean isSharedFileLockTable()
  {
    if (!(propertyChecked))
      synchronized (FileChannelImpl.class)
      {
        if (!(propertyChecked))
        {
          GetPropertyAction localGetPropertyAction = new GetPropertyAction("sun.nio.ch.disableSystemWideOverlappingFileLockCheck");
          String str = (String)AccessController.doPrivileged(localGetPropertyAction);
          isSharedFileLockTable = (str == null) || (str.equals("false"));
          propertyChecked = true;
        }
      }
    return isSharedFileLockTable;
  }

  private FileLockTable fileLockTable()
  {
    if (this.fileLockTable == null)
      synchronized (this)
      {
        if (this.fileLockTable == null)
          this.fileLockTable = new SimpleFileLockTable();
      }
    return this.fileLockTable;
  }

  public FileLock lock(long paramLong1, long paramLong2, boolean paramBoolean)
    throws IOException
  {
    ensureOpen();
    if ((paramBoolean) && (!(this.readable)))
      throw new NonReadableChannelException();
    if ((!(paramBoolean)) && (!(this.writable)))
      throw new NonWritableChannelException();
    FileLockImpl localFileLockImpl1 = new FileLockImpl(this, paramLong1, paramLong2, paramBoolean);
    FileLockTable localFileLockTable = fileLockTable();
    localFileLockTable.add(localFileLockImpl1);
    boolean bool = true;
    int i = -1;
    try
    {
      begin();
      if (!(isOpen()))
      {
        Object localObject1 = null;
        jsr 141;
        return localObject1;
      }
      i = this.threads.add();
      int j = lock0(this.fd, true, paramLong1, paramLong2, paramBoolean);
      if (j == 1)
      {
        if ((!($assertionsDisabled)) && (!(paramBoolean)))
          throw new AssertionError();
        FileLockImpl localFileLockImpl2 = new FileLockImpl(this, paramLong1, paramLong2, false);
        localFileLockTable.replace(localFileLockImpl1, localFileLockImpl2);
        FileLockImpl localFileLockImpl3 = localFileLockImpl2;
        jsr 58;
        return localFileLockImpl3;
      }
      if ((j == 2) || (j == -1))
      {
        localFileLockTable.remove(localFileLockImpl1);
        bool = false;
      }
    }
    catch (IOException localIOException)
    {
      throw localIOException;
    }
    finally
    {
      this.threads.remove(i);
      try
      {
        end(bool);
      }
      catch (ClosedByInterruptException localClosedByInterruptException)
      {
        throw new FileLockInterruptionException();
      }
    }
    return localFileLockImpl1;
  }

  public FileLock tryLock(long paramLong1, long paramLong2, boolean paramBoolean)
    throws IOException
  {
    ensureOpen();
    if ((paramBoolean) && (!(this.readable)))
      throw new NonReadableChannelException();
    if ((!(paramBoolean)) && (!(this.writable)))
      throw new NonWritableChannelException();
    FileLockImpl localFileLockImpl1 = new FileLockImpl(this, paramLong1, paramLong2, paramBoolean);
    FileLockTable localFileLockTable = fileLockTable();
    localFileLockTable.add(localFileLockImpl1);
    int i = lock0(this.fd, false, paramLong1, paramLong2, paramBoolean);
    if (i == -1)
    {
      localFileLockTable.remove(localFileLockImpl1);
      return null;
    }
    if (i == 1)
    {
      if ((!($assertionsDisabled)) && (!(paramBoolean)))
        throw new AssertionError();
      FileLockImpl localFileLockImpl2 = new FileLockImpl(this, paramLong1, paramLong2, false);
      localFileLockTable.replace(localFileLockImpl1, localFileLockImpl2);
      return localFileLockImpl2;
    }
    return localFileLockImpl1;
  }

  void release(FileLockImpl paramFileLockImpl)
    throws IOException
  {
    ensureOpen();
    release0(this.fd, paramFileLockImpl.position(), paramFileLockImpl.size());
    if ((!($assertionsDisabled)) && (this.fileLockTable == null))
      throw new AssertionError();
    this.fileLockTable.remove(paramFileLockImpl);
  }

  native int lock0(FileDescriptor paramFileDescriptor, boolean paramBoolean1, long paramLong1, long paramLong2, boolean paramBoolean2)
    throws IOException;

  native void release0(FileDescriptor paramFileDescriptor, long paramLong1, long paramLong2)
    throws IOException;

  private native long map0(int paramInt, long paramLong1, long paramLong2)
    throws IOException;

  private static native int unmap0(long paramLong1, long paramLong2);

  private native int force0(FileDescriptor paramFileDescriptor, boolean paramBoolean);

  private native int truncate0(FileDescriptor paramFileDescriptor, long paramLong);

  private native long transferTo0(int paramInt1, long paramLong1, long paramLong2, int paramInt2);

  private native long position0(FileDescriptor paramFileDescriptor, long paramLong);

  private native long size0(FileDescriptor paramFileDescriptor);

  private static native long initIDs();

  static
  {
    transferSupported = true;
    pipeSupported = true;
    fileSupported = true;
    Util.load();
    allocationGranularity = initIDs();
    nd = new FileDispatcher();
    isAMappedBufferField = Reflect.lookupField("java.nio.MappedByteBuffer", "isAMappedBuffer");
  }

  private static class FileLockReference extends WeakReference<FileLock>
  {
    private FileKey fileKey;

    FileLockReference(FileLock paramFileLock, ReferenceQueue paramReferenceQueue, FileKey paramFileKey)
    {
      super(paramFileLock, paramReferenceQueue);
      this.fileKey = paramFileKey;
    }

    private FileKey fileKey()
    {
      return this.fileKey;
    }
  }

  private static abstract interface FileLockTable
  {
    public abstract void add(FileLock paramFileLock)
      throws OverlappingFileLockException;

    public abstract void remove(FileLock paramFileLock);

    public abstract void removeAll(Releaser paramReleaser)
      throws IOException;

    public abstract void replace(FileLock paramFileLock1, FileLock paramFileLock2);

    public static abstract interface Releaser
    {
      public abstract void release(FileLock paramFileLock)
        throws IOException;
    }
  }

  private static class SharedFileLockTable
  implements FileChannelImpl.FileLockTable
  {
    private static ConcurrentHashMap<FileKey, ArrayList<FileChannelImpl.FileLockReference>> lockMap;
    private static ReferenceQueue queue;
    private FileChannelImpl fci;
    private FileKey fileKey;

    public SharedFileLockTable(FileChannelImpl paramFileChannelImpl)
    {
      this.fci = paramFileChannelImpl;
      this.fileKey = FileKey.create(FileChannelImpl.access$000(paramFileChannelImpl));
    }

    public void add(FileLock paramFileLock)
      throws OverlappingFileLockException
    {
      Object localObject1 = (ArrayList)lockMap.get(this.fileKey);
      while (true)
      {
        if (localObject1 == null)
        {
          localObject1 = new ArrayList(2);
          synchronized (localObject1)
          {
            ??? = (ArrayList)lockMap.putIfAbsent(this.fileKey, localObject1);
            if (??? != null)
              break label77;
            ((ArrayList)localObject1).add(new FileChannelImpl.FileLockReference(paramFileLock, queue, this.fileKey));
            label77: break label174:
          }
          localObject1 = ???;
        }
        synchronized (localObject1)
        {
          ??? = (ArrayList)lockMap.get(this.fileKey);
          if (localObject1 != ???)
            break label156;
          checkList((List)localObject1, paramFileLock.position(), paramFileLock.size());
          ((ArrayList)localObject1).add(new FileChannelImpl.FileLockReference(paramFileLock, queue, this.fileKey));
          break label174:
          label156: localObject1 = ???;
        }
      }
      label174: removeStaleEntries();
    }

    private void removeKeyIfEmpty(FileKey paramFileKey, ArrayList<FileChannelImpl.FileLockReference> paramArrayList)
    {
      if ((!($assertionsDisabled)) && (!(Thread.holdsLock(paramArrayList))))
        throw new AssertionError();
      if ((!($assertionsDisabled)) && (lockMap.get(paramFileKey) != paramArrayList))
        throw new AssertionError();
      if (paramArrayList.isEmpty())
        lockMap.remove(paramFileKey);
    }

    public void remove(FileLock paramFileLock)
    {
      if ((!($assertionsDisabled)) && (paramFileLock == null))
        throw new AssertionError();
      ArrayList localArrayList1 = (ArrayList)lockMap.get(this.fileKey);
      if ((!($assertionsDisabled)) && (localArrayList1 == null))
        throw new AssertionError();
      synchronized (localArrayList1)
      {
        for (int i = 0; i < localArrayList1.size(); ++i)
        {
          FileChannelImpl.FileLockReference localFileLockReference = (FileChannelImpl.FileLockReference)localArrayList1.get(i);
          FileLock localFileLock = (FileLock)localFileLockReference.get();
          if (localFileLock == paramFileLock)
          {
            if ((!($assertionsDisabled)) && (((localFileLock == null) || (localFileLock.channel() != this.fci))))
              throw new AssertionError();
            localFileLockReference.clear();
            localArrayList1.remove(i);
            break;
          }
        }
      }
    }

    public void removeAll(FileChannelImpl.FileLockTable.Releaser paramReleaser)
      throws IOException
    {
      ArrayList localArrayList1 = (ArrayList)lockMap.get(this.fileKey);
      if (localArrayList1 != null)
        synchronized (localArrayList1)
        {
          int i = 0;
          while (i < localArrayList1.size())
          {
            FileChannelImpl.FileLockReference localFileLockReference = (FileChannelImpl.FileLockReference)localArrayList1.get(i);
            FileLock localFileLock = (FileLock)localFileLockReference.get();
            if ((localFileLock != null) && (localFileLock.channel() == this.fci))
            {
              paramReleaser.release(localFileLock);
              localFileLockReference.clear();
              localArrayList1.remove(i);
            }
            else
            {
              ++i;
            }
          }
          removeKeyIfEmpty(this.fileKey, localArrayList1);
        }
    }

    public void replace(FileLock paramFileLock1, FileLock paramFileLock2)
    {
      ArrayList localArrayList1 = (ArrayList)lockMap.get(this.fileKey);
      if ((!($assertionsDisabled)) && (localArrayList1 == null))
        throw new AssertionError();
      synchronized (localArrayList1)
      {
        for (int i = 0; i < localArrayList1.size(); ++i)
        {
          FileChannelImpl.FileLockReference localFileLockReference = (FileChannelImpl.FileLockReference)localArrayList1.get(i);
          FileLock localFileLock = (FileLock)localFileLockReference.get();
          if (localFileLock == paramFileLock1)
          {
            localFileLockReference.clear();
            localArrayList1.set(i, new FileChannelImpl.FileLockReference(paramFileLock2, queue, this.fileKey));
            break;
          }
        }
      }
    }

    private void checkList(List<FileChannelImpl.FileLockReference> paramList, long paramLong1, long paramLong2)
      throws OverlappingFileLockException
    {
      if ((!($assertionsDisabled)) && (!(Thread.holdsLock(paramList))))
        throw new AssertionError();
      Iterator localIterator = paramList.iterator();
      while (localIterator.hasNext())
      {
        FileChannelImpl.FileLockReference localFileLockReference = (FileChannelImpl.FileLockReference)localIterator.next();
        FileLock localFileLock = (FileLock)localFileLockReference.get();
        if ((localFileLock != null) && (localFileLock.overlaps(paramLong1, paramLong2)))
          throw new OverlappingFileLockException();
      }
    }

    private void removeStaleEntries()
    {
      while ((localFileLockReference = (FileChannelImpl.FileLockReference)queue.poll()) != null)
      {
        FileChannelImpl.FileLockReference localFileLockReference;
        FileKey localFileKey = FileChannelImpl.FileLockReference.access$300(localFileLockReference);
        ArrayList localArrayList1 = (ArrayList)lockMap.get(localFileKey);
        if (localArrayList1 != null)
          synchronized (localArrayList1)
          {
            localArrayList1.remove(localFileLockReference);
            removeKeyIfEmpty(localFileKey, localArrayList1);
          }
      }
    }

    static
    {
      lockMap = new ConcurrentHashMap();
      queue = new ReferenceQueue();
    }
  }

  private static class SimpleFileLockTable
  implements FileChannelImpl.FileLockTable
  {
    private List<FileLock> lockList = new ArrayList(2);

    private void checkList(long paramLong1, long paramLong2)
      throws OverlappingFileLockException
    {
      if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this.lockList))))
        throw new AssertionError();
      Iterator localIterator = this.lockList.iterator();
      while (localIterator.hasNext())
      {
        FileLock localFileLock = (FileLock)localIterator.next();
        if (localFileLock.overlaps(paramLong1, paramLong2))
          throw new OverlappingFileLockException();
      }
    }

    public void add(FileLock paramFileLock)
      throws OverlappingFileLockException
    {
      synchronized (this.lockList)
      {
        checkList(paramFileLock.position(), paramFileLock.size());
        this.lockList.add(paramFileLock);
      }
    }

    public void remove(FileLock paramFileLock)
    {
      synchronized (this.lockList)
      {
        this.lockList.remove(paramFileLock);
      }
    }

    public void removeAll(FileChannelImpl.FileLockTable.Releaser paramReleaser)
      throws IOException
    {
      synchronized (this.lockList)
      {
        Iterator localIterator = this.lockList.iterator();
        while (localIterator.hasNext())
        {
          FileLock localFileLock = (FileLock)localIterator.next();
          paramReleaser.release(localFileLock);
          localIterator.remove();
        }
      }
    }

    public void replace(FileLock paramFileLock1, FileLock paramFileLock2)
    {
      synchronized (this.lockList)
      {
        this.lockList.remove(paramFileLock1);
        this.lockList.add(paramFileLock2);
      }
    }
  }

  private static class Unmapper
  implements Runnable
  {
    private long address;
    private long size;

    private Unmapper(long paramLong1, long paramLong2)
    {
      if ((!($assertionsDisabled)) && (paramLong1 == 3412047600491167744L))
        throw new AssertionError();
      this.address = paramLong1;
      this.size = paramLong2;
    }

    public void run()
    {
      if (this.address == 3412047308433391616L)
        return;
      FileChannelImpl.access$100(this.address, this.size);
      this.address = 3412047600491167744L;
    }
  }

  public static abstract interface Releaser
  {
    public abstract void release(FileLock paramFileLock)
      throws IOException;
  }
}