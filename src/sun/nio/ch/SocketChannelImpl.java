package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

class SocketChannelImpl extends SocketChannel
  implements SelChImpl
{
  private static NativeDispatcher nd;
  private final FileDescriptor fd;
  private final int fdVal;
  private volatile long readerThread = 3412045659165949952L;
  private volatile long writerThread = 3412045659165949952L;
  private final Object readLock = new Object();
  private final Object writeLock = new Object();
  private final Object stateLock = new Object();
  private static final int ST_UNINITIALIZED = -1;
  private static final int ST_UNCONNECTED = 0;
  private static final int ST_PENDING = 1;
  private static final int ST_CONNECTED = 2;
  private static final int ST_KILLPENDING = 3;
  private static final int ST_KILLED = 4;
  private int state = -1;
  private SocketAddress localAddress = null;
  private SocketAddress remoteAddress = null;
  private boolean isInputOpen = true;
  private boolean isOutputOpen = true;
  private boolean readyToConnect = false;
  private SocketOpts.IP.TCP options = null;
  private Socket socket = null;
  public static final int SHUT_RD = 0;
  public static final int SHUT_WR = 1;
  public static final int SHUT_RDWR = 2;

  SocketChannelImpl(SelectorProvider paramSelectorProvider)
    throws IOException
  {
    super(paramSelectorProvider);
    this.fd = Net.socket(true);
    this.fdVal = IOUtil.fdVal(this.fd);
    this.state = 0;
  }

  SocketChannelImpl(SelectorProvider paramSelectorProvider, FileDescriptor paramFileDescriptor, InetSocketAddress paramInetSocketAddress)
    throws IOException
  {
    super(paramSelectorProvider);
    this.fd = paramFileDescriptor;
    this.fdVal = IOUtil.fdVal(paramFileDescriptor);
    this.state = 2;
    this.remoteAddress = paramInetSocketAddress;
  }

  public Socket socket()
  {
    synchronized (this.stateLock)
    {
      if (this.socket == null)
        this.socket = SocketAdaptor.create(this);
      return this.socket;
    }
  }

  private boolean ensureReadOpen()
    throws ClosedChannelException
  {
    synchronized (this.stateLock)
    {
      if (!(isOpen()))
        throw new ClosedChannelException();
      if (!(isConnected()))
        throw new NotYetConnectedException();
      return (this.isInputOpen);
    }
  }

  private void ensureWriteOpen()
    throws ClosedChannelException
  {
    synchronized (this.stateLock)
    {
      if (!(isOpen()))
        throw new ClosedChannelException();
      if (!(this.isOutputOpen))
        throw new ClosedChannelException();
      if (!(isConnected()))
        throw new NotYetConnectedException();
    }
  }

  private void readerCleanup()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      this.readerThread = 3412047359972999168L;
      if (this.state == 3)
        kill();
    }
  }

  private void writerCleanup()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      this.writerThread = 3412047359972999168L;
      if (this.state == 3)
        kill();
    }
  }

  // ERROR //
  public int read(ByteBuffer paramByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +11 -> 12
    //   4: new 184	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 369	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_0
    //   13: getfield 356	sun/nio/ch/SocketChannelImpl:readLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore_2
    //   18: monitorenter
    //   19: aload_0
    //   20: invokespecial 423	sun/nio/ch/SocketChannelImpl:ensureReadOpen	()Z
    //   23: ifne +7 -> 30
    //   26: iconst_m1
    //   27: aload_2
    //   28: monitorexit
    //   29: ireturn
    //   30: iconst_0
    //   31: istore_3
    //   32: aload_0
    //   33: invokevirtual 416	sun/nio/ch/SocketChannelImpl:begin	()V
    //   36: aload_0
    //   37: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   40: dup
    //   41: astore 4
    //   43: monitorenter
    //   44: aload_0
    //   45: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   48: ifne +17 -> 65
    //   51: iconst_0
    //   52: istore 5
    //   54: aload 4
    //   56: monitorexit
    //   57: jsr +86 -> 143
    //   60: aload_2
    //   61: monitorexit
    //   62: iload 5
    //   64: ireturn
    //   65: aload_0
    //   66: invokestatic 404	sun/nio/ch/NativeThread:current	()J
    //   69: putfield 349	sun/nio/ch/SocketChannelImpl:readerThread	J
    //   72: aload 4
    //   74: monitorexit
    //   75: goto +11 -> 86
    //   78: astore 6
    //   80: aload 4
    //   82: monitorexit
    //   83: aload 6
    //   85: athrow
    //   86: aload_0
    //   87: getfield 355	sun/nio/ch/SocketChannelImpl:fd	Ljava/io/FileDescriptor;
    //   90: aload_1
    //   91: ldc2_w 178
    //   94: getstatic 362	sun/nio/ch/SocketChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   97: aload_0
    //   98: getfield 356	sun/nio/ch/SocketChannelImpl:readLock	Ljava/lang/Object;
    //   101: invokestatic 400	sun/nio/ch/IOUtil:read	(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;JLsun/nio/ch/NativeDispatcher;Ljava/lang/Object;)I
    //   104: istore_3
    //   105: iload_3
    //   106: bipush 253
    //   108: if_icmpne +13 -> 121
    //   111: aload_0
    //   112: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   115: ifeq +6 -> 121
    //   118: goto -32 -> 86
    //   121: iload_3
    //   122: invokestatic 392	sun/nio/ch/IOStatus:normalize	(I)I
    //   125: istore 4
    //   127: jsr +16 -> 143
    //   130: aload_2
    //   131: monitorexit
    //   132: iload 4
    //   134: ireturn
    //   135: astore 7
    //   137: jsr +6 -> 143
    //   140: aload 7
    //   142: athrow
    //   143: astore 8
    //   145: aload_0
    //   146: invokespecial 421	sun/nio/ch/SocketChannelImpl:readerCleanup	()V
    //   149: aload_0
    //   150: iload_3
    //   151: ifgt +9 -> 160
    //   154: iload_3
    //   155: bipush 254
    //   157: if_icmpne +7 -> 164
    //   160: iconst_1
    //   161: goto +4 -> 165
    //   164: iconst_0
    //   165: invokevirtual 428	sun/nio/ch/SocketChannelImpl:end	(Z)V
    //   168: aload_0
    //   169: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   172: dup
    //   173: astore 9
    //   175: monitorenter
    //   176: iload_3
    //   177: ifgt +17 -> 194
    //   180: aload_0
    //   181: getfield 352	sun/nio/ch/SocketChannelImpl:isInputOpen	Z
    //   184: ifne +10 -> 194
    //   187: iconst_m1
    //   188: aload 9
    //   190: monitorexit
    //   191: aload_2
    //   192: monitorexit
    //   193: ireturn
    //   194: aload 9
    //   196: monitorexit
    //   197: goto +11 -> 208
    //   200: astore 10
    //   202: aload 9
    //   204: monitorexit
    //   205: aload 10
    //   207: athrow
    //   208: getstatic 351	sun/nio/ch/SocketChannelImpl:$assertionsDisabled	Z
    //   211: ifne +18 -> 229
    //   214: iload_3
    //   215: invokestatic 393	sun/nio/ch/IOStatus:check	(I)Z
    //   218: ifne +11 -> 229
    //   221: new 181	java/lang/AssertionError
    //   224: dup
    //   225: invokespecial 364	java/lang/AssertionError:<init>	()V
    //   228: athrow
    //   229: ret 8
    //   231: astore 11
    //   233: aload_2
    //   234: monitorexit
    //   235: aload 11
    //   237: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   44	57	78	finally
    //   65	75	78	finally
    //   78	83	78	finally
    //   32	60	135	finally
    //   65	130	135	finally
    //   135	140	135	finally
    //   176	191	200	finally
    //   194	197	200	finally
    //   200	205	200	finally
    //   19	29	231	finally
    //   30	62	231	finally
    //   65	132	231	finally
    //   135	193	231	finally
    //   194	235	231	finally
  }

  // ERROR //
  private long read0(ByteBuffer[] paramArrayOfByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +11 -> 12
    //   4: new 184	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 369	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_0
    //   13: getfield 356	sun/nio/ch/SocketChannelImpl:readLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore_2
    //   18: monitorenter
    //   19: aload_0
    //   20: invokespecial 423	sun/nio/ch/SocketChannelImpl:ensureReadOpen	()Z
    //   23: ifne +9 -> 32
    //   26: ldc2_w 178
    //   29: aload_2
    //   30: monitorexit
    //   31: lreturn
    //   32: lconst_0
    //   33: lstore_3
    //   34: aload_0
    //   35: invokevirtual 416	sun/nio/ch/SocketChannelImpl:begin	()V
    //   38: aload_0
    //   39: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   42: dup
    //   43: astore 5
    //   45: monitorenter
    //   46: aload_0
    //   47: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   50: ifne +17 -> 67
    //   53: lconst_0
    //   54: lstore 6
    //   56: aload 5
    //   58: monitorexit
    //   59: jsr +81 -> 140
    //   62: aload_2
    //   63: monitorexit
    //   64: lload 6
    //   66: lreturn
    //   67: aload_0
    //   68: invokestatic 404	sun/nio/ch/NativeThread:current	()J
    //   71: putfield 349	sun/nio/ch/SocketChannelImpl:readerThread	J
    //   74: aload 5
    //   76: monitorexit
    //   77: goto +11 -> 88
    //   80: astore 8
    //   82: aload 5
    //   84: monitorexit
    //   85: aload 8
    //   87: athrow
    //   88: aload_0
    //   89: getfield 355	sun/nio/ch/SocketChannelImpl:fd	Ljava/io/FileDescriptor;
    //   92: aload_1
    //   93: getstatic 362	sun/nio/ch/SocketChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   96: invokestatic 398	sun/nio/ch/IOUtil:read	(Ljava/io/FileDescriptor;[Ljava/nio/ByteBuffer;Lsun/nio/ch/NativeDispatcher;)J
    //   99: lstore_3
    //   100: lload_3
    //   101: ldc2_w 174
    //   104: lcmp
    //   105: ifne +13 -> 118
    //   108: aload_0
    //   109: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   112: ifeq +6 -> 118
    //   115: goto -27 -> 88
    //   118: lload_3
    //   119: invokestatic 394	sun/nio/ch/IOStatus:normalize	(J)J
    //   122: lstore 5
    //   124: jsr +16 -> 140
    //   127: aload_2
    //   128: monitorexit
    //   129: lload 5
    //   131: lreturn
    //   132: astore 9
    //   134: jsr +6 -> 140
    //   137: aload 9
    //   139: athrow
    //   140: astore 10
    //   142: aload_0
    //   143: invokespecial 421	sun/nio/ch/SocketChannelImpl:readerCleanup	()V
    //   146: aload_0
    //   147: lload_3
    //   148: lconst_0
    //   149: lcmp
    //   150: ifgt +11 -> 161
    //   153: lload_3
    //   154: ldc2_w 176
    //   157: lcmp
    //   158: ifne +7 -> 165
    //   161: iconst_1
    //   162: goto +4 -> 166
    //   165: iconst_0
    //   166: invokevirtual 428	sun/nio/ch/SocketChannelImpl:end	(Z)V
    //   169: aload_0
    //   170: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   173: dup
    //   174: astore 11
    //   176: monitorenter
    //   177: lload_3
    //   178: lconst_0
    //   179: lcmp
    //   180: ifgt +19 -> 199
    //   183: aload_0
    //   184: getfield 352	sun/nio/ch/SocketChannelImpl:isInputOpen	Z
    //   187: ifne +12 -> 199
    //   190: ldc2_w 178
    //   193: aload 11
    //   195: monitorexit
    //   196: aload_2
    //   197: monitorexit
    //   198: lreturn
    //   199: aload 11
    //   201: monitorexit
    //   202: goto +11 -> 213
    //   205: astore 12
    //   207: aload 11
    //   209: monitorexit
    //   210: aload 12
    //   212: athrow
    //   213: getstatic 351	sun/nio/ch/SocketChannelImpl:$assertionsDisabled	Z
    //   216: ifne +18 -> 234
    //   219: lload_3
    //   220: invokestatic 395	sun/nio/ch/IOStatus:check	(J)Z
    //   223: ifne +11 -> 234
    //   226: new 181	java/lang/AssertionError
    //   229: dup
    //   230: invokespecial 364	java/lang/AssertionError:<init>	()V
    //   233: athrow
    //   234: ret 10
    //   236: astore 13
    //   238: aload_2
    //   239: monitorexit
    //   240: aload 13
    //   242: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   46	59	80	finally
    //   67	77	80	finally
    //   80	85	80	finally
    //   34	62	132	finally
    //   67	127	132	finally
    //   132	137	132	finally
    //   177	196	205	finally
    //   199	202	205	finally
    //   205	210	205	finally
    //   19	31	236	finally
    //   32	64	236	finally
    //   67	129	236	finally
    //   132	198	236	finally
    //   199	240	236	finally
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
    //   0: aload_1
    //   1: ifnonnull +11 -> 12
    //   4: new 184	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 369	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_0
    //   13: getfield 358	sun/nio/ch/SocketChannelImpl:writeLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore_2
    //   18: monitorenter
    //   19: aload_0
    //   20: invokespecial 419	sun/nio/ch/SocketChannelImpl:ensureWriteOpen	()V
    //   23: iconst_0
    //   24: istore_3
    //   25: aload_0
    //   26: invokevirtual 416	sun/nio/ch/SocketChannelImpl:begin	()V
    //   29: aload_0
    //   30: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   33: dup
    //   34: astore 4
    //   36: monitorenter
    //   37: aload_0
    //   38: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   41: ifne +17 -> 58
    //   44: iconst_0
    //   45: istore 5
    //   47: aload 4
    //   49: monitorexit
    //   50: jsr +86 -> 136
    //   53: aload_2
    //   54: monitorexit
    //   55: iload 5
    //   57: ireturn
    //   58: aload_0
    //   59: invokestatic 404	sun/nio/ch/NativeThread:current	()J
    //   62: putfield 350	sun/nio/ch/SocketChannelImpl:writerThread	J
    //   65: aload 4
    //   67: monitorexit
    //   68: goto +11 -> 79
    //   71: astore 6
    //   73: aload 4
    //   75: monitorexit
    //   76: aload 6
    //   78: athrow
    //   79: aload_0
    //   80: getfield 355	sun/nio/ch/SocketChannelImpl:fd	Ljava/io/FileDescriptor;
    //   83: aload_1
    //   84: ldc2_w 178
    //   87: getstatic 362	sun/nio/ch/SocketChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   90: aload_0
    //   91: getfield 358	sun/nio/ch/SocketChannelImpl:writeLock	Ljava/lang/Object;
    //   94: invokestatic 401	sun/nio/ch/IOUtil:write	(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;JLsun/nio/ch/NativeDispatcher;Ljava/lang/Object;)I
    //   97: istore_3
    //   98: iload_3
    //   99: bipush 253
    //   101: if_icmpne +13 -> 114
    //   104: aload_0
    //   105: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   108: ifeq +6 -> 114
    //   111: goto -32 -> 79
    //   114: iload_3
    //   115: invokestatic 392	sun/nio/ch/IOStatus:normalize	(I)I
    //   118: istore 4
    //   120: jsr +16 -> 136
    //   123: aload_2
    //   124: monitorexit
    //   125: iload 4
    //   127: ireturn
    //   128: astore 7
    //   130: jsr +6 -> 136
    //   133: aload 7
    //   135: athrow
    //   136: astore 8
    //   138: aload_0
    //   139: invokespecial 422	sun/nio/ch/SocketChannelImpl:writerCleanup	()V
    //   142: aload_0
    //   143: iload_3
    //   144: ifgt +9 -> 153
    //   147: iload_3
    //   148: bipush 254
    //   150: if_icmpne +7 -> 157
    //   153: iconst_1
    //   154: goto +4 -> 158
    //   157: iconst_0
    //   158: invokevirtual 428	sun/nio/ch/SocketChannelImpl:end	(Z)V
    //   161: aload_0
    //   162: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   165: dup
    //   166: astore 9
    //   168: monitorenter
    //   169: iload_3
    //   170: ifgt +18 -> 188
    //   173: aload_0
    //   174: getfield 353	sun/nio/ch/SocketChannelImpl:isOutputOpen	Z
    //   177: ifne +11 -> 188
    //   180: new 192	java/nio/channels/AsynchronousCloseException
    //   183: dup
    //   184: invokespecial 385	java/nio/channels/AsynchronousCloseException:<init>	()V
    //   187: athrow
    //   188: aload 9
    //   190: monitorexit
    //   191: goto +11 -> 202
    //   194: astore 10
    //   196: aload 9
    //   198: monitorexit
    //   199: aload 10
    //   201: athrow
    //   202: getstatic 351	sun/nio/ch/SocketChannelImpl:$assertionsDisabled	Z
    //   205: ifne +18 -> 223
    //   208: iload_3
    //   209: invokestatic 393	sun/nio/ch/IOStatus:check	(I)Z
    //   212: ifne +11 -> 223
    //   215: new 181	java/lang/AssertionError
    //   218: dup
    //   219: invokespecial 364	java/lang/AssertionError:<init>	()V
    //   222: athrow
    //   223: ret 8
    //   225: astore 11
    //   227: aload_2
    //   228: monitorexit
    //   229: aload 11
    //   231: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   37	50	71	finally
    //   58	68	71	finally
    //   71	76	71	finally
    //   25	53	128	finally
    //   58	123	128	finally
    //   128	133	128	finally
    //   169	191	194	finally
    //   194	199	194	finally
    //   19	55	225	finally
    //   58	125	225	finally
    //   128	229	225	finally
  }

  // ERROR //
  public long write0(ByteBuffer[] paramArrayOfByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +11 -> 12
    //   4: new 184	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 369	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_0
    //   13: getfield 358	sun/nio/ch/SocketChannelImpl:writeLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore_2
    //   18: monitorenter
    //   19: aload_0
    //   20: invokespecial 419	sun/nio/ch/SocketChannelImpl:ensureWriteOpen	()V
    //   23: lconst_0
    //   24: lstore_3
    //   25: aload_0
    //   26: invokevirtual 416	sun/nio/ch/SocketChannelImpl:begin	()V
    //   29: aload_0
    //   30: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   33: dup
    //   34: astore 5
    //   36: monitorenter
    //   37: aload_0
    //   38: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   41: ifne +17 -> 58
    //   44: lconst_0
    //   45: lstore 6
    //   47: aload 5
    //   49: monitorexit
    //   50: jsr +81 -> 131
    //   53: aload_2
    //   54: monitorexit
    //   55: lload 6
    //   57: lreturn
    //   58: aload_0
    //   59: invokestatic 404	sun/nio/ch/NativeThread:current	()J
    //   62: putfield 350	sun/nio/ch/SocketChannelImpl:writerThread	J
    //   65: aload 5
    //   67: monitorexit
    //   68: goto +11 -> 79
    //   71: astore 8
    //   73: aload 5
    //   75: monitorexit
    //   76: aload 8
    //   78: athrow
    //   79: aload_0
    //   80: getfield 355	sun/nio/ch/SocketChannelImpl:fd	Ljava/io/FileDescriptor;
    //   83: aload_1
    //   84: getstatic 362	sun/nio/ch/SocketChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   87: invokestatic 399	sun/nio/ch/IOUtil:write	(Ljava/io/FileDescriptor;[Ljava/nio/ByteBuffer;Lsun/nio/ch/NativeDispatcher;)J
    //   90: lstore_3
    //   91: lload_3
    //   92: ldc2_w 174
    //   95: lcmp
    //   96: ifne +13 -> 109
    //   99: aload_0
    //   100: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   103: ifeq +6 -> 109
    //   106: goto -27 -> 79
    //   109: lload_3
    //   110: invokestatic 394	sun/nio/ch/IOStatus:normalize	(J)J
    //   113: lstore 5
    //   115: jsr +16 -> 131
    //   118: aload_2
    //   119: monitorexit
    //   120: lload 5
    //   122: lreturn
    //   123: astore 9
    //   125: jsr +6 -> 131
    //   128: aload 9
    //   130: athrow
    //   131: astore 10
    //   133: aload_0
    //   134: invokespecial 422	sun/nio/ch/SocketChannelImpl:writerCleanup	()V
    //   137: aload_0
    //   138: lload_3
    //   139: lconst_0
    //   140: lcmp
    //   141: ifgt +11 -> 152
    //   144: lload_3
    //   145: ldc2_w 176
    //   148: lcmp
    //   149: ifne +7 -> 156
    //   152: iconst_1
    //   153: goto +4 -> 157
    //   156: iconst_0
    //   157: invokevirtual 428	sun/nio/ch/SocketChannelImpl:end	(Z)V
    //   160: aload_0
    //   161: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   164: dup
    //   165: astore 11
    //   167: monitorenter
    //   168: lload_3
    //   169: lconst_0
    //   170: lcmp
    //   171: ifgt +18 -> 189
    //   174: aload_0
    //   175: getfield 353	sun/nio/ch/SocketChannelImpl:isOutputOpen	Z
    //   178: ifne +11 -> 189
    //   181: new 192	java/nio/channels/AsynchronousCloseException
    //   184: dup
    //   185: invokespecial 385	java/nio/channels/AsynchronousCloseException:<init>	()V
    //   188: athrow
    //   189: aload 11
    //   191: monitorexit
    //   192: goto +11 -> 203
    //   195: astore 12
    //   197: aload 11
    //   199: monitorexit
    //   200: aload 12
    //   202: athrow
    //   203: getstatic 351	sun/nio/ch/SocketChannelImpl:$assertionsDisabled	Z
    //   206: ifne +18 -> 224
    //   209: lload_3
    //   210: invokestatic 395	sun/nio/ch/IOStatus:check	(J)Z
    //   213: ifne +11 -> 224
    //   216: new 181	java/lang/AssertionError
    //   219: dup
    //   220: invokespecial 364	java/lang/AssertionError:<init>	()V
    //   223: athrow
    //   224: ret 10
    //   226: astore 13
    //   228: aload_2
    //   229: monitorexit
    //   230: aload 13
    //   232: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   37	50	71	finally
    //   58	68	71	finally
    //   71	76	71	finally
    //   25	53	123	finally
    //   58	118	123	finally
    //   123	128	123	finally
    //   168	192	195	finally
    //   195	200	195	finally
    //   19	55	226	finally
    //   58	120	226	finally
    //   123	230	226	finally
  }

  public long write(ByteBuffer[] paramArrayOfByteBuffer, int paramInt1, int paramInt2)
    throws IOException
  {
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 > paramArrayOfByteBuffer.length - paramInt2))
      throw new IndexOutOfBoundsException();
    return write0(Util.subsequence(paramArrayOfByteBuffer, paramInt1, paramInt2));
  }

  protected void implConfigureBlocking(boolean paramBoolean)
    throws IOException
  {
    IOUtil.configureBlocking(this.fd, paramBoolean);
  }

  public SocketOpts options()
  {
    synchronized (this.stateLock)
    {
      if (this.options == null)
      {
        1 local1 = new SocketOptsImpl.Dispatcher(this)
        {
          int getInt()
            throws IOException
          {
            return Net.getIntOption(SocketChannelImpl.access$000(this.this$0), paramInt);
          }

          void setInt(, int paramInt2)
            throws IOException
          {
            Net.setIntOption(SocketChannelImpl.access$000(this.this$0), paramInt1, paramInt2);
          }
        };
        this.options = new SocketOptsImpl.IP.TCP(local1);
      }
      return this.options;
    }
  }

  public boolean isBound()
  {
    synchronized (this.stateLock)
    {
      if (this.state != 2)
        break label19;
      return true;
      label19: return ((this.localAddress != null) ? 1 : false);
    }
  }

  public SocketAddress localAddress()
  {
    synchronized (this.stateLock)
    {
      if ((this.state == 2) && (((this.localAddress == null) || (((InetSocketAddress)this.localAddress).getAddress().isAnyLocalAddress()))))
        this.localAddress = Net.localAddress(this.fd);
      return this.localAddress;
    }
  }

  public SocketAddress remoteAddress()
  {
    synchronized (this.stateLock)
    {
      return this.remoteAddress;
    }
  }

  public void bind(SocketAddress paramSocketAddress)
    throws IOException
  {
    synchronized (this.readLock)
    {
      synchronized (this.writeLock)
      {
        synchronized (this.stateLock)
        {
          ensureOpenAndUnconnected();
          if (this.localAddress != null)
            throw new AlreadyBoundException();
          InetSocketAddress localInetSocketAddress = Net.checkAddress(paramSocketAddress);
          Net.bind(this.fd, localInetSocketAddress.getAddress(), localInetSocketAddress.getPort());
          this.localAddress = Net.localAddress(this.fd);
        }
      }
    }
  }

  public boolean isConnected()
  {
    synchronized (this.stateLock)
    {
      return ((this.state == 2) ? 1 : false);
    }
  }

  public boolean isConnectionPending()
  {
    synchronized (this.stateLock)
    {
      return ((this.state == 1) ? 1 : false);
    }
  }

  void ensureOpenAndUnconnected()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      if (!(isOpen()))
        throw new ClosedChannelException();
      if (this.state == 2)
        throw new AlreadyConnectedException();
      if (this.state == 1)
        throw new ConnectionPendingException();
    }
  }

  // ERROR //
  public boolean connect(SocketAddress paramSocketAddress)
    throws IOException
  {
    // Byte code:
    //   0: iconst_0
    //   1: istore_2
    //   2: iconst_0
    //   3: istore_3
    //   4: aload_0
    //   5: getfield 356	sun/nio/ch/SocketChannelImpl:readLock	Ljava/lang/Object;
    //   8: dup
    //   9: astore 4
    //   11: monitorenter
    //   12: aload_0
    //   13: getfield 358	sun/nio/ch/SocketChannelImpl:writeLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore 5
    //   19: monitorenter
    //   20: aload_0
    //   21: invokevirtual 418	sun/nio/ch/SocketChannelImpl:ensureOpenAndUnconnected	()V
    //   24: aload_1
    //   25: invokestatic 410	sun/nio/ch/Net:checkAddress	(Ljava/net/SocketAddress;)Ljava/net/InetSocketAddress;
    //   28: astore 6
    //   30: invokestatic 378	java/lang/System:getSecurityManager	()Ljava/lang/SecurityManager;
    //   33: astore 7
    //   35: aload 7
    //   37: ifnull +21 -> 58
    //   40: aload 7
    //   42: aload 6
    //   44: invokevirtual 383	java/net/InetSocketAddress:getAddress	()Ljava/net/InetAddress;
    //   47: invokevirtual 380	java/net/InetAddress:getHostAddress	()Ljava/lang/String;
    //   50: aload 6
    //   52: invokevirtual 382	java/net/InetSocketAddress:getPort	()I
    //   55: invokevirtual 373	java/lang/SecurityManager:checkConnect	(Ljava/lang/String;I)V
    //   58: aload_0
    //   59: invokevirtual 431	sun/nio/ch/SocketChannelImpl:blockingLock	()Ljava/lang/Object;
    //   62: dup
    //   63: astore 8
    //   65: monitorenter
    //   66: iconst_0
    //   67: istore 9
    //   69: aload_0
    //   70: invokevirtual 416	sun/nio/ch/SocketChannelImpl:begin	()V
    //   73: aload_0
    //   74: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   77: dup
    //   78: astore 10
    //   80: monitorenter
    //   81: aload_0
    //   82: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   85: ifne +24 -> 109
    //   88: iconst_0
    //   89: istore 11
    //   91: aload 10
    //   93: monitorexit
    //   94: jsr +104 -> 198
    //   97: aload 8
    //   99: monitorexit
    //   100: aload 5
    //   102: monitorexit
    //   103: aload 4
    //   105: monitorexit
    //   106: iload 11
    //   108: ireturn
    //   109: aload_0
    //   110: invokestatic 404	sun/nio/ch/NativeThread:current	()J
    //   113: putfield 349	sun/nio/ch/SocketChannelImpl:readerThread	J
    //   116: aload 10
    //   118: monitorexit
    //   119: goto +11 -> 130
    //   122: astore 12
    //   124: aload 10
    //   126: monitorexit
    //   127: aload 12
    //   129: athrow
    //   130: aload 6
    //   132: invokevirtual 383	java/net/InetSocketAddress:getAddress	()Ljava/net/InetAddress;
    //   135: astore 10
    //   137: aload 10
    //   139: invokevirtual 379	java/net/InetAddress:isAnyLocalAddress	()Z
    //   142: ifeq +8 -> 150
    //   145: invokestatic 381	java/net/InetAddress:getLocalHost	()Ljava/net/InetAddress;
    //   148: astore 10
    //   150: aload_0
    //   151: getfield 355	sun/nio/ch/SocketChannelImpl:fd	Ljava/io/FileDescriptor;
    //   154: aload 10
    //   156: aload 6
    //   158: invokevirtual 382	java/net/InetSocketAddress:getPort	()I
    //   161: iload_2
    //   162: invokestatic 408	sun/nio/ch/Net:connect	(Ljava/io/FileDescriptor;Ljava/net/InetAddress;II)I
    //   165: istore 9
    //   167: iload 9
    //   169: bipush 253
    //   171: if_icmpne +13 -> 184
    //   174: aload_0
    //   175: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   178: ifeq +6 -> 184
    //   181: goto -51 -> 130
    //   184: jsr +14 -> 198
    //   187: goto +62 -> 249
    //   190: astore 13
    //   192: jsr +6 -> 198
    //   195: aload 13
    //   197: athrow
    //   198: astore 14
    //   200: aload_0
    //   201: invokespecial 421	sun/nio/ch/SocketChannelImpl:readerCleanup	()V
    //   204: aload_0
    //   205: iload 9
    //   207: ifgt +10 -> 217
    //   210: iload 9
    //   212: bipush 254
    //   214: if_icmpne +7 -> 221
    //   217: iconst_1
    //   218: goto +4 -> 222
    //   221: iconst_0
    //   222: invokevirtual 428	sun/nio/ch/SocketChannelImpl:end	(Z)V
    //   225: getstatic 351	sun/nio/ch/SocketChannelImpl:$assertionsDisabled	Z
    //   228: ifne +19 -> 247
    //   231: iload 9
    //   233: invokestatic 393	sun/nio/ch/IOStatus:check	(I)Z
    //   236: ifne +11 -> 247
    //   239: new 181	java/lang/AssertionError
    //   242: dup
    //   243: invokespecial 364	java/lang/AssertionError:<init>	()V
    //   246: athrow
    //   247: ret 14
    //   249: goto +12 -> 261
    //   252: astore 10
    //   254: aload_0
    //   255: invokevirtual 417	sun/nio/ch/SocketChannelImpl:close	()V
    //   258: aload 10
    //   260: athrow
    //   261: aload_0
    //   262: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   265: dup
    //   266: astore 10
    //   268: monitorenter
    //   269: aload_0
    //   270: aload 6
    //   272: putfield 361	sun/nio/ch/SocketChannelImpl:remoteAddress	Ljava/net/SocketAddress;
    //   275: iload 9
    //   277: ifle +22 -> 299
    //   280: aload_0
    //   281: iconst_2
    //   282: putfield 348	sun/nio/ch/SocketChannelImpl:state	I
    //   285: iconst_1
    //   286: aload 10
    //   288: monitorexit
    //   289: aload 8
    //   291: monitorexit
    //   292: aload 5
    //   294: monitorexit
    //   295: aload 4
    //   297: monitorexit
    //   298: ireturn
    //   299: aload_0
    //   300: invokevirtual 424	sun/nio/ch/SocketChannelImpl:isBlocking	()Z
    //   303: ifne +11 -> 314
    //   306: aload_0
    //   307: iconst_1
    //   308: putfield 348	sun/nio/ch/SocketChannelImpl:state	I
    //   311: goto +17 -> 328
    //   314: getstatic 351	sun/nio/ch/SocketChannelImpl:$assertionsDisabled	Z
    //   317: ifne +11 -> 328
    //   320: new 181	java/lang/AssertionError
    //   323: dup
    //   324: invokespecial 364	java/lang/AssertionError:<init>	()V
    //   327: athrow
    //   328: aload 10
    //   330: monitorexit
    //   331: goto +11 -> 342
    //   334: astore 15
    //   336: aload 10
    //   338: monitorexit
    //   339: aload 15
    //   341: athrow
    //   342: aload 8
    //   344: monitorexit
    //   345: goto +11 -> 356
    //   348: astore 16
    //   350: aload 8
    //   352: monitorexit
    //   353: aload 16
    //   355: athrow
    //   356: iconst_0
    //   357: aload 5
    //   359: monitorexit
    //   360: aload 4
    //   362: monitorexit
    //   363: ireturn
    //   364: astore 17
    //   366: aload 5
    //   368: monitorexit
    //   369: aload 17
    //   371: athrow
    //   372: astore 18
    //   374: aload 4
    //   376: monitorexit
    //   377: aload 18
    //   379: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   81	94	122	finally
    //   109	119	122	finally
    //   122	127	122	finally
    //   69	97	190	finally
    //   109	187	190	finally
    //   190	195	190	finally
    //   69	97	252	IOException
    //   109	249	252	IOException
    //   269	289	334	finally
    //   299	331	334	finally
    //   334	339	334	finally
    //   66	100	348	finally
    //   109	292	348	finally
    //   299	345	348	finally
    //   348	353	348	finally
    //   20	103	364	finally
    //   109	295	364	finally
    //   299	360	364	finally
    //   364	369	364	finally
    //   12	106	372	finally
    //   109	298	372	finally
    //   299	363	372	finally
    //   364	377	372	finally
  }

  // ERROR //
  public boolean finishConnect()
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 356	sun/nio/ch/SocketChannelImpl:readLock	Ljava/lang/Object;
    //   4: dup
    //   5: astore_1
    //   6: monitorenter
    //   7: aload_0
    //   8: getfield 358	sun/nio/ch/SocketChannelImpl:writeLock	Ljava/lang/Object;
    //   11: dup
    //   12: astore_2
    //   13: monitorenter
    //   14: aload_0
    //   15: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   18: dup
    //   19: astore_3
    //   20: monitorenter
    //   21: aload_0
    //   22: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   25: ifne +11 -> 36
    //   28: new 193	ClosedChannelException
    //   31: dup
    //   32: invokespecial 386	ClosedChannelException:<init>	()V
    //   35: athrow
    //   36: aload_0
    //   37: getfield 348	sun/nio/ch/SocketChannelImpl:state	I
    //   40: iconst_2
    //   41: if_icmpne +11 -> 52
    //   44: iconst_1
    //   45: aload_3
    //   46: monitorexit
    //   47: aload_2
    //   48: monitorexit
    //   49: aload_1
    //   50: monitorexit
    //   51: ireturn
    //   52: aload_0
    //   53: getfield 348	sun/nio/ch/SocketChannelImpl:state	I
    //   56: iconst_1
    //   57: if_icmpeq +11 -> 68
    //   60: new 195	java/nio/channels/NoConnectionPendingException
    //   63: dup
    //   64: invokespecial 388	java/nio/channels/NoConnectionPendingException:<init>	()V
    //   67: athrow
    //   68: aload_3
    //   69: monitorexit
    //   70: goto +10 -> 80
    //   73: astore 4
    //   75: aload_3
    //   76: monitorexit
    //   77: aload 4
    //   79: athrow
    //   80: iconst_0
    //   81: istore_3
    //   82: aload_0
    //   83: invokevirtual 416	sun/nio/ch/SocketChannelImpl:begin	()V
    //   86: aload_0
    //   87: invokevirtual 431	sun/nio/ch/SocketChannelImpl:blockingLock	()Ljava/lang/Object;
    //   90: dup
    //   91: astore 4
    //   93: monitorenter
    //   94: aload_0
    //   95: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   98: dup
    //   99: astore 5
    //   101: monitorenter
    //   102: aload_0
    //   103: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   106: ifne +22 -> 128
    //   109: iconst_0
    //   110: istore 6
    //   112: aload 5
    //   114: monitorexit
    //   115: aload 4
    //   117: monitorexit
    //   118: jsr +131 -> 249
    //   121: aload_2
    //   122: monitorexit
    //   123: aload_1
    //   124: monitorexit
    //   125: iload 6
    //   127: ireturn
    //   128: aload_0
    //   129: invokestatic 404	sun/nio/ch/NativeThread:current	()J
    //   132: putfield 349	sun/nio/ch/SocketChannelImpl:readerThread	J
    //   135: aload 5
    //   137: monitorexit
    //   138: goto +11 -> 149
    //   141: astore 7
    //   143: aload 5
    //   145: monitorexit
    //   146: aload 7
    //   148: athrow
    //   149: aload_0
    //   150: invokevirtual 424	sun/nio/ch/SocketChannelImpl:isBlocking	()Z
    //   153: ifne +32 -> 185
    //   156: aload_0
    //   157: getfield 355	sun/nio/ch/SocketChannelImpl:fd	Ljava/io/FileDescriptor;
    //   160: iconst_0
    //   161: aload_0
    //   162: getfield 354	sun/nio/ch/SocketChannelImpl:readyToConnect	Z
    //   165: invokestatic 430	sun/nio/ch/SocketChannelImpl:checkConnect	(Ljava/io/FileDescriptor;ZZ)I
    //   168: istore_3
    //   169: iload_3
    //   170: bipush 253
    //   172: if_icmpne +49 -> 221
    //   175: aload_0
    //   176: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   179: ifeq +42 -> 221
    //   182: goto -26 -> 156
    //   185: aload_0
    //   186: getfield 355	sun/nio/ch/SocketChannelImpl:fd	Ljava/io/FileDescriptor;
    //   189: iconst_1
    //   190: aload_0
    //   191: getfield 354	sun/nio/ch/SocketChannelImpl:readyToConnect	Z
    //   194: invokestatic 430	sun/nio/ch/SocketChannelImpl:checkConnect	(Ljava/io/FileDescriptor;ZZ)I
    //   197: istore_3
    //   198: iload_3
    //   199: ifne +6 -> 205
    //   202: goto -17 -> 185
    //   205: iload_3
    //   206: bipush 253
    //   208: if_icmpne +13 -> 221
    //   211: aload_0
    //   212: invokevirtual 426	sun/nio/ch/SocketChannelImpl:isOpen	()Z
    //   215: ifeq +6 -> 221
    //   218: goto -33 -> 185
    //   221: aload 4
    //   223: monitorexit
    //   224: goto +11 -> 235
    //   227: astore 8
    //   229: aload 4
    //   231: monitorexit
    //   232: aload 8
    //   234: athrow
    //   235: jsr +14 -> 249
    //   238: goto +96 -> 334
    //   241: astore 9
    //   243: jsr +6 -> 249
    //   246: aload 9
    //   248: athrow
    //   249: astore 10
    //   251: aload_0
    //   252: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   255: dup
    //   256: astore 11
    //   258: monitorenter
    //   259: aload_0
    //   260: lconst_0
    //   261: putfield 349	sun/nio/ch/SocketChannelImpl:readerThread	J
    //   264: aload_0
    //   265: getfield 348	sun/nio/ch/SocketChannelImpl:state	I
    //   268: iconst_3
    //   269: if_icmpne +9 -> 278
    //   272: aload_0
    //   273: invokevirtual 420	sun/nio/ch/SocketChannelImpl:kill	()V
    //   276: iconst_0
    //   277: istore_3
    //   278: aload 11
    //   280: monitorexit
    //   281: goto +11 -> 292
    //   284: astore 12
    //   286: aload 11
    //   288: monitorexit
    //   289: aload 12
    //   291: athrow
    //   292: aload_0
    //   293: iload_3
    //   294: ifgt +9 -> 303
    //   297: iload_3
    //   298: bipush 254
    //   300: if_icmpne +7 -> 307
    //   303: iconst_1
    //   304: goto +4 -> 308
    //   307: iconst_0
    //   308: invokevirtual 428	sun/nio/ch/SocketChannelImpl:end	(Z)V
    //   311: getstatic 351	sun/nio/ch/SocketChannelImpl:$assertionsDisabled	Z
    //   314: ifne +18 -> 332
    //   317: iload_3
    //   318: invokestatic 393	sun/nio/ch/IOStatus:check	(I)Z
    //   321: ifne +11 -> 332
    //   324: new 181	java/lang/AssertionError
    //   327: dup
    //   328: invokespecial 364	java/lang/AssertionError:<init>	()V
    //   331: athrow
    //   332: ret 10
    //   334: goto +12 -> 346
    //   337: astore 4
    //   339: aload_0
    //   340: invokevirtual 417	sun/nio/ch/SocketChannelImpl:close	()V
    //   343: aload 4
    //   345: athrow
    //   346: iload_3
    //   347: ifle +36 -> 383
    //   350: aload_0
    //   351: getfield 357	sun/nio/ch/SocketChannelImpl:stateLock	Ljava/lang/Object;
    //   354: dup
    //   355: astore 4
    //   357: monitorenter
    //   358: aload_0
    //   359: iconst_2
    //   360: putfield 348	sun/nio/ch/SocketChannelImpl:state	I
    //   363: aload 4
    //   365: monitorexit
    //   366: goto +11 -> 377
    //   369: astore 13
    //   371: aload 4
    //   373: monitorexit
    //   374: aload 13
    //   376: athrow
    //   377: iconst_1
    //   378: aload_2
    //   379: monitorexit
    //   380: aload_1
    //   381: monitorexit
    //   382: ireturn
    //   383: iconst_0
    //   384: aload_2
    //   385: monitorexit
    //   386: aload_1
    //   387: monitorexit
    //   388: ireturn
    //   389: astore 14
    //   391: aload_2
    //   392: monitorexit
    //   393: aload 14
    //   395: athrow
    //   396: astore 15
    //   398: aload_1
    //   399: monitorexit
    //   400: aload 15
    //   402: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   21	47	73	finally
    //   52	70	73	finally
    //   73	77	73	finally
    //   102	115	141	finally
    //   128	138	141	finally
    //   141	146	141	finally
    //   94	118	227	finally
    //   128	224	227	finally
    //   227	232	227	finally
    //   82	121	241	finally
    //   128	238	241	finally
    //   241	246	241	finally
    //   259	281	284	finally
    //   284	289	284	finally
    //   82	121	337	IOException
    //   128	334	337	IOException
    //   358	366	369	finally
    //   369	374	369	finally
    //   14	49	389	finally
    //   52	123	389	finally
    //   128	380	389	finally
    //   383	386	389	finally
    //   389	393	389	finally
    //   7	51	396	finally
    //   52	125	396	finally
    //   128	382	396	finally
    //   383	388	396	finally
    //   389	400	396	finally
  }

  public void shutdownInput()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      if (!(isOpen()))
        throw new ClosedChannelException();
      this.isInputOpen = false;
      shutdown(this.fd, 0);
      if (this.readerThread != 3412047067915223040L)
        NativeThread.signal(this.readerThread);
    }
  }

  public void shutdownOutput()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      if (!(isOpen()))
        throw new ClosedChannelException();
      this.isOutputOpen = false;
      shutdown(this.fd, 1);
      if (this.writerThread != 3412047067915223040L)
        NativeThread.signal(this.writerThread);
    }
  }

  public boolean isInputOpen()
  {
    synchronized (this.stateLock)
    {
      return this.isInputOpen;
    }
  }

  public boolean isOutputOpen()
  {
    synchronized (this.stateLock)
    {
      return this.isOutputOpen;
    }
  }

  protected void implCloseSelectableChannel()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      this.isInputOpen = false;
      this.isOutputOpen = false;
      nd.preClose(this.fd);
      if (this.readerThread != 3412047067915223040L)
        NativeThread.signal(this.readerThread);
      if (this.writerThread != 3412047067915223040L)
        NativeThread.signal(this.writerThread);
      if (!(isRegistered()))
        kill();
    }
  }

  public void kill()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      if (this.state != 4)
        break label18;
      return;
      label18: if (this.state != -1)
        break label34;
      this.state = 4;
      return;
      label34: if (($assertionsDisabled) || ((!(isOpen())) && (!(isRegistered()))))
        break label62;
      throw new AssertionError();
      label62: if ((this.readerThread != 3412047772289859584L) || (this.writerThread != 3412047772289859584L))
        break label98;
      nd.close(this.fd);
      this.state = 4;
      break label103:
      label98: label103: this.state = 3;
    }
  }

  public boolean translateReadyOps(int paramInt1, int paramInt2, SelectionKeyImpl paramSelectionKeyImpl)
  {
    int i = paramSelectionKeyImpl.nioInterestOps();
    int j = paramSelectionKeyImpl.nioReadyOps();
    int k = paramInt2;
    if ((paramInt1 & 0x20) != 0)
      return false;
    if ((paramInt1 & 0x18) != 0)
    {
      k = i;
      paramSelectionKeyImpl.nioReadyOps(k);
      this.readyToConnect = true;
      return ((k & (j ^ 0xFFFFFFFF)) != 0);
    }
    if (((paramInt1 & 0x1) != 0) && ((i & 0x1) != 0) && (this.state == 2))
      k |= 1;
    if (((paramInt1 & 0x2) != 0) && ((i & 0x8) != 0) && (((this.state == 0) || (this.state == 1))))
    {
      k |= 8;
      this.readyToConnect = true;
    }
    if (((paramInt1 & 0x4) != 0) && ((i & 0x4) != 0) && (this.state == 2))
      k |= 4;
    paramSelectionKeyImpl.nioReadyOps(k);
    return ((k & (j ^ 0xFFFFFFFF)) != 0);
  }

  public boolean translateAndUpdateReadyOps(int paramInt, SelectionKeyImpl paramSelectionKeyImpl)
  {
    return translateReadyOps(paramInt, paramSelectionKeyImpl.nioReadyOps(), paramSelectionKeyImpl);
  }

  public boolean translateAndSetReadyOps(int paramInt, SelectionKeyImpl paramSelectionKeyImpl)
  {
    return translateReadyOps(paramInt, 0, paramSelectionKeyImpl);
  }

  public void translateAndSetInterestOps(int paramInt, SelectionKeyImpl paramSelectionKeyImpl)
  {
    int i = 0;
    if ((paramInt & 0x1) != 0)
      i |= 1;
    if ((paramInt & 0x4) != 0)
      i |= 4;
    if ((paramInt & 0x8) != 0)
      i |= 2;
    paramSelectionKeyImpl.selector.putEventOps(paramSelectionKeyImpl, i);
  }

  public FileDescriptor getFD()
  {
    return this.fd;
  }

  public int getFDVal()
  {
    return this.fdVal;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append(getClass().getSuperclass().getName());
    localStringBuffer.append('[');
    if (!(isOpen()))
      localStringBuffer.append("closed");
    else
      synchronized (this.stateLock)
      {
        switch (this.state)
        {
        case 0:
          localStringBuffer.append("unconnected");
          break;
        case 1:
          localStringBuffer.append("connection-pending");
          break;
        case 2:
          localStringBuffer.append("connected");
          if (!(this.isInputOpen))
            localStringBuffer.append(" ishut");
          if (!(this.isOutputOpen))
            localStringBuffer.append(" oshut");
        }
        if (localAddress() != null)
        {
          localStringBuffer.append(" local=");
          localStringBuffer.append(localAddress().toString());
        }
        if (remoteAddress() != null)
        {
          localStringBuffer.append(" remote=");
          localStringBuffer.append(remoteAddress().toString());
        }
      }
    localStringBuffer.append(']');
    return localStringBuffer.toString();
  }

  private static native int checkConnect(FileDescriptor paramFileDescriptor, boolean paramBoolean1, boolean paramBoolean2)
    throws IOException;

  private static native void shutdown(FileDescriptor paramFileDescriptor, int paramInt)
    throws IOException;

  static
  {
    Util.load();
    nd = new SocketDispatcher();
  }
}