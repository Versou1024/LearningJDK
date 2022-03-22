package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.spi.SelectorProvider;

class DatagramChannelImpl extends DatagramChannel
  implements SelChImpl
{
  private static NativeDispatcher nd;
  FileDescriptor fd = null;
  int fdVal;
  private volatile long readerThread = 3412045521726996480L;
  private volatile long writerThread = 3412045521726996480L;
  private InetAddress cachedSenderInetAddress = null;
  private int cachedSenderPort = 0;
  private final Object readLock = new Object();
  private final Object writeLock = new Object();
  private final Object stateLock = new Object();
  private static final int ST_UNINITIALIZED = -1;
  private static int ST_UNCONNECTED;
  private static int ST_CONNECTED;
  private static final int ST_KILLED = 2;
  private int state = -1;
  private SocketAddress localAddress = null;
  SocketAddress remoteAddress = null;
  private SocketOpts.IP options = null;
  private DatagramSocket socket = null;
  private SocketAddress sender;

  public DatagramChannelImpl(SelectorProvider paramSelectorProvider)
    throws IOException
  {
    super(paramSelectorProvider);
    this.fd = Net.socket(false);
    this.fdVal = IOUtil.fdVal(this.fd);
    this.state = ST_UNCONNECTED;
  }

  public DatagramChannelImpl(SelectorProvider paramSelectorProvider, FileDescriptor paramFileDescriptor)
    throws IOException
  {
    super(paramSelectorProvider);
    this.fd = paramFileDescriptor;
    this.fdVal = IOUtil.fdVal(paramFileDescriptor);
    this.state = ST_UNCONNECTED;
  }

  public DatagramSocket socket()
  {
    synchronized (this.stateLock)
    {
      if (this.socket == null)
        this.socket = DatagramSocketAdaptor.create(this);
      return this.socket;
    }
  }

  private void ensureOpen()
    throws ClosedChannelException
  {
    if (!(isOpen()))
      throw new ClosedChannelException();
  }

  // ERROR //
  public SocketAddress receive(ByteBuffer paramByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: invokevirtual 406	java/nio/ByteBuffer:isReadOnly	()Z
    //   4: ifeq +13 -> 17
    //   7: new 178	java/lang/IllegalArgumentException
    //   10: dup
    //   11: ldc 3
    //   13: invokespecial 387	java/lang/IllegalArgumentException:<init>	(Ljava/lang/String;)V
    //   16: athrow
    //   17: aload_1
    //   18: ifnonnull +11 -> 29
    //   21: new 182	java/lang/NullPointerException
    //   24: dup
    //   25: invokespecial 391	java/lang/NullPointerException:<init>	()V
    //   28: athrow
    //   29: aload_0
    //   30: getfield 372	sun/nio/ch/DatagramChannelImpl:readLock	Ljava/lang/Object;
    //   33: dup
    //   34: astore_2
    //   35: monitorenter
    //   36: aload_0
    //   37: invokespecial 417	sun/nio/ch/DatagramChannelImpl:ensureOpen	()V
    //   40: aload_0
    //   41: invokevirtual 421	sun/nio/ch/DatagramChannelImpl:isBound	()Z
    //   44: ifne +7 -> 51
    //   47: aconst_null
    //   48: aload_2
    //   49: monitorexit
    //   50: areturn
    //   51: iconst_0
    //   52: istore_3
    //   53: aconst_null
    //   54: astore 4
    //   56: aload_0
    //   57: invokevirtual 415	sun/nio/ch/DatagramChannelImpl:begin	()V
    //   60: aload_0
    //   61: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   64: ifne +14 -> 78
    //   67: aconst_null
    //   68: astore 5
    //   70: jsr +203 -> 273
    //   73: aload_2
    //   74: monitorexit
    //   75: aload 5
    //   77: areturn
    //   78: invokestatic 398	java/lang/System:getSecurityManager	()Ljava/lang/SecurityManager;
    //   81: astore 5
    //   83: aload_0
    //   84: invokestatic 452	sun/nio/ch/NativeThread:current	()J
    //   87: putfield 368	sun/nio/ch/DatagramChannelImpl:readerThread	J
    //   90: aload_0
    //   91: invokevirtual 422	sun/nio/ch/DatagramChannelImpl:isConnected	()Z
    //   94: ifne +8 -> 102
    //   97: aload 5
    //   99: ifnonnull +43 -> 142
    //   102: aload_0
    //   103: aload_0
    //   104: getfield 371	sun/nio/ch/DatagramChannelImpl:fd	Ljava/io/FileDescriptor;
    //   107: aload_1
    //   108: invokespecial 433	sun/nio/ch/DatagramChannelImpl:receive	(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;)I
    //   111: istore_3
    //   112: iload_3
    //   113: bipush 253
    //   115: if_icmpne +10 -> 125
    //   118: aload_0
    //   119: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   122: ifne -20 -> 102
    //   125: iload_3
    //   126: bipush 254
    //   128: if_icmpne +123 -> 251
    //   131: aconst_null
    //   132: astore 6
    //   134: jsr +139 -> 273
    //   137: aload_2
    //   138: monitorexit
    //   139: aload 6
    //   141: areturn
    //   142: aload_1
    //   143: invokevirtual 405	java/nio/ByteBuffer:remaining	()I
    //   146: invokestatic 466	sun/nio/ch/Util:getTemporaryDirectBuffer	(I)Ljava/nio/ByteBuffer;
    //   149: astore 4
    //   151: aload_0
    //   152: aload_0
    //   153: getfield 371	sun/nio/ch/DatagramChannelImpl:fd	Ljava/io/FileDescriptor;
    //   156: aload 4
    //   158: invokespecial 433	sun/nio/ch/DatagramChannelImpl:receive	(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;)I
    //   161: istore_3
    //   162: iload_3
    //   163: bipush 253
    //   165: if_icmpne +10 -> 175
    //   168: aload_0
    //   169: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   172: ifne -21 -> 151
    //   175: iload_3
    //   176: bipush 254
    //   178: if_icmpne +14 -> 192
    //   181: aconst_null
    //   182: astore 6
    //   184: jsr +89 -> 273
    //   187: aload_2
    //   188: monitorexit
    //   189: aload 6
    //   191: areturn
    //   192: aload_0
    //   193: getfield 379	sun/nio/ch/DatagramChannelImpl:sender	Ljava/net/SocketAddress;
    //   196: checkcast 188	java/net/InetSocketAddress
    //   199: astore 6
    //   201: aload 5
    //   203: aload 6
    //   205: invokevirtual 402	java/net/InetSocketAddress:getAddress	()Ljava/net/InetAddress;
    //   208: invokevirtual 400	java/net/InetAddress:getHostAddress	()Ljava/lang/String;
    //   211: aload 6
    //   213: invokevirtual 401	java/net/InetSocketAddress:getPort	()I
    //   216: invokevirtual 395	java/lang/SecurityManager:checkAccept	(Ljava/lang/String;I)V
    //   219: goto +16 -> 235
    //   222: astore 7
    //   224: aload 4
    //   226: invokevirtual 407	java/nio/ByteBuffer:clear	()Ljava/nio/Buffer;
    //   229: pop
    //   230: iconst_0
    //   231: istore_3
    //   232: goto -81 -> 151
    //   235: aload 4
    //   237: invokevirtual 408	java/nio/ByteBuffer:flip	()Ljava/nio/Buffer;
    //   240: pop
    //   241: aload_1
    //   242: aload 4
    //   244: invokevirtual 410	java/nio/ByteBuffer:put	(Ljava/nio/ByteBuffer;)Ljava/nio/ByteBuffer;
    //   247: pop
    //   248: goto +3 -> 251
    //   251: aload_0
    //   252: getfield 379	sun/nio/ch/DatagramChannelImpl:sender	Ljava/net/SocketAddress;
    //   255: astore 6
    //   257: jsr +16 -> 273
    //   260: aload_2
    //   261: monitorexit
    //   262: aload 6
    //   264: areturn
    //   265: astore 8
    //   267: jsr +6 -> 273
    //   270: aload 8
    //   272: athrow
    //   273: astore 9
    //   275: aload 4
    //   277: ifnull +8 -> 285
    //   280: aload 4
    //   282: invokestatic 467	sun/nio/ch/Util:releaseTemporaryDirectBuffer	(Ljava/nio/ByteBuffer;)V
    //   285: aload_0
    //   286: lconst_0
    //   287: putfield 368	sun/nio/ch/DatagramChannelImpl:readerThread	J
    //   290: aload_0
    //   291: iload_3
    //   292: ifgt +9 -> 301
    //   295: iload_3
    //   296: bipush 254
    //   298: if_icmpne +7 -> 305
    //   301: iconst_1
    //   302: goto +4 -> 306
    //   305: iconst_0
    //   306: invokevirtual 425	sun/nio/ch/DatagramChannelImpl:end	(Z)V
    //   309: getstatic 370	sun/nio/ch/DatagramChannelImpl:$assertionsDisabled	Z
    //   312: ifne +18 -> 330
    //   315: iload_3
    //   316: invokestatic 441	sun/nio/ch/IOStatus:check	(I)Z
    //   319: ifne +11 -> 330
    //   322: new 175	java/lang/AssertionError
    //   325: dup
    //   326: invokespecial 384	java/lang/AssertionError:<init>	()V
    //   329: athrow
    //   330: ret 9
    //   332: astore 10
    //   334: aload_2
    //   335: monitorexit
    //   336: aload 10
    //   338: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   201	219	222	java/lang/SecurityException
    //   56	73	265	finally
    //   78	137	265	finally
    //   142	187	265	finally
    //   192	260	265	finally
    //   265	270	265	finally
    //   36	50	332	finally
    //   51	75	332	finally
    //   78	139	332	finally
    //   142	189	332	finally
    //   192	262	332	finally
    //   265	336	332	finally
  }

  private int receive(FileDescriptor paramFileDescriptor, ByteBuffer paramByteBuffer)
    throws IOException
  {
    int i = paramByteBuffer.position();
    int j = paramByteBuffer.limit();
    if ((!($assertionsDisabled)) && (i > j))
      throw new AssertionError();
    int k = (i <= j) ? j - i : 0;
    if ((paramByteBuffer instanceof DirectBuffer) && (k > 0))
      return receiveIntoNativeBuffer(paramFileDescriptor, paramByteBuffer, k, i);
    int l = Math.max(k, 1);
    ByteBuffer localByteBuffer = null;
    try
    {
      localByteBuffer = Util.getTemporaryDirectBuffer(l);
      int i1 = receiveIntoNativeBuffer(paramFileDescriptor, localByteBuffer, l, 0);
      localByteBuffer.flip();
      if ((i1 > 0) && (k > 0))
        paramByteBuffer.put(localByteBuffer);
      int i2 = i1;
      return i2;
    }
    finally
    {
      Util.releaseTemporaryDirectBuffer(localByteBuffer);
    }
  }

  private int receiveIntoNativeBuffer(FileDescriptor paramFileDescriptor, ByteBuffer paramByteBuffer, int paramInt1, int paramInt2)
    throws IOException
  {
    int i = receive0(paramFileDescriptor, ((DirectBuffer)paramByteBuffer).address() + paramInt2, paramInt1, isConnected());
    if (i > 0)
      paramByteBuffer.position(paramInt2 + i);
    return i;
  }

  // ERROR //
  public int send(ByteBuffer paramByteBuffer, SocketAddress paramSocketAddress)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +11 -> 12
    //   4: new 182	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 391	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_0
    //   13: getfield 374	sun/nio/ch/DatagramChannelImpl:writeLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore_3
    //   18: monitorenter
    //   19: aload_0
    //   20: invokespecial 417	sun/nio/ch/DatagramChannelImpl:ensureOpen	()V
    //   23: aload_2
    //   24: checkcast 188	java/net/InetSocketAddress
    //   27: astore 4
    //   29: aload 4
    //   31: invokevirtual 402	java/net/InetSocketAddress:getAddress	()Ljava/net/InetAddress;
    //   34: astore 5
    //   36: aload 5
    //   38: ifnonnull +13 -> 51
    //   41: new 174	IOException
    //   44: dup
    //   45: ldc 4
    //   47: invokespecial 383	IOException:<init>	(Ljava/lang/String;)V
    //   50: athrow
    //   51: aload_0
    //   52: getfield 373	sun/nio/ch/DatagramChannelImpl:stateLock	Ljava/lang/Object;
    //   55: dup
    //   56: astore 6
    //   58: monitorenter
    //   59: aload_0
    //   60: invokevirtual 422	sun/nio/ch/DatagramChannelImpl:isConnected	()Z
    //   63: ifne +67 -> 130
    //   66: aload_2
    //   67: ifnonnull +11 -> 78
    //   70: new 182	java/lang/NullPointerException
    //   73: dup
    //   74: invokespecial 391	java/lang/NullPointerException:<init>	()V
    //   77: athrow
    //   78: invokestatic 398	java/lang/System:getSecurityManager	()Ljava/lang/SecurityManager;
    //   81: astore 7
    //   83: aload 7
    //   85: ifnull +42 -> 127
    //   88: aload 5
    //   90: invokevirtual 399	java/net/InetAddress:isMulticastAddress	()Z
    //   93: ifeq +16 -> 109
    //   96: aload 7
    //   98: aload 4
    //   100: invokevirtual 402	java/net/InetSocketAddress:getAddress	()Ljava/net/InetAddress;
    //   103: invokevirtual 397	java/lang/SecurityManager:checkMulticast	(Ljava/net/InetAddress;)V
    //   106: goto +21 -> 127
    //   109: aload 7
    //   111: aload 4
    //   113: invokevirtual 402	java/net/InetSocketAddress:getAddress	()Ljava/net/InetAddress;
    //   116: invokevirtual 400	java/net/InetAddress:getHostAddress	()Ljava/lang/String;
    //   119: aload 4
    //   121: invokevirtual 401	java/net/InetSocketAddress:getPort	()I
    //   124: invokevirtual 396	java/lang/SecurityManager:checkConnect	(Ljava/lang/String;I)V
    //   127: goto +35 -> 162
    //   130: aload_2
    //   131: aload_0
    //   132: getfield 378	sun/nio/ch/DatagramChannelImpl:remoteAddress	Ljava/net/SocketAddress;
    //   135: invokevirtual 393	java/lang/Object:equals	(Ljava/lang/Object;)Z
    //   138: ifne +13 -> 151
    //   141: new 178	java/lang/IllegalArgumentException
    //   144: dup
    //   145: ldc 2
    //   147: invokespecial 387	java/lang/IllegalArgumentException:<init>	(Ljava/lang/String;)V
    //   150: athrow
    //   151: aload_0
    //   152: aload_1
    //   153: invokevirtual 428	sun/nio/ch/DatagramChannelImpl:write	(Ljava/nio/ByteBuffer;)I
    //   156: aload 6
    //   158: monitorexit
    //   159: aload_3
    //   160: monitorexit
    //   161: ireturn
    //   162: aload 6
    //   164: monitorexit
    //   165: goto +11 -> 176
    //   168: astore 8
    //   170: aload 6
    //   172: monitorexit
    //   173: aload 8
    //   175: athrow
    //   176: iconst_0
    //   177: istore 6
    //   179: aload_0
    //   180: invokevirtual 415	sun/nio/ch/DatagramChannelImpl:begin	()V
    //   183: aload_0
    //   184: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   187: ifne +14 -> 201
    //   190: iconst_0
    //   191: istore 7
    //   193: jsr +64 -> 257
    //   196: aload_3
    //   197: monitorexit
    //   198: iload 7
    //   200: ireturn
    //   201: aload_0
    //   202: invokestatic 452	sun/nio/ch/NativeThread:current	()J
    //   205: putfield 369	sun/nio/ch/DatagramChannelImpl:writerThread	J
    //   208: aload_0
    //   209: aload_0
    //   210: getfield 371	sun/nio/ch/DatagramChannelImpl:fd	Ljava/io/FileDescriptor;
    //   213: aload_1
    //   214: aload_2
    //   215: invokespecial 435	sun/nio/ch/DatagramChannelImpl:send	(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;Ljava/net/SocketAddress;)I
    //   218: istore 6
    //   220: iload 6
    //   222: bipush 253
    //   224: if_icmpne +10 -> 234
    //   227: aload_0
    //   228: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   231: ifne -23 -> 208
    //   234: iload 6
    //   236: invokestatic 440	sun/nio/ch/IOStatus:normalize	(I)I
    //   239: istore 7
    //   241: jsr +16 -> 257
    //   244: aload_3
    //   245: monitorexit
    //   246: iload 7
    //   248: ireturn
    //   249: astore 9
    //   251: jsr +6 -> 257
    //   254: aload 9
    //   256: athrow
    //   257: astore 10
    //   259: aload_0
    //   260: lconst_0
    //   261: putfield 369	sun/nio/ch/DatagramChannelImpl:writerThread	J
    //   264: aload_0
    //   265: iload 6
    //   267: ifgt +10 -> 277
    //   270: iload 6
    //   272: bipush 254
    //   274: if_icmpne +7 -> 281
    //   277: iconst_1
    //   278: goto +4 -> 282
    //   281: iconst_0
    //   282: invokevirtual 425	sun/nio/ch/DatagramChannelImpl:end	(Z)V
    //   285: getstatic 370	sun/nio/ch/DatagramChannelImpl:$assertionsDisabled	Z
    //   288: ifne +19 -> 307
    //   291: iload 6
    //   293: invokestatic 441	sun/nio/ch/IOStatus:check	(I)Z
    //   296: ifne +11 -> 307
    //   299: new 175	java/lang/AssertionError
    //   302: dup
    //   303: invokespecial 384	java/lang/AssertionError:<init>	()V
    //   306: athrow
    //   307: ret 10
    //   309: astore 11
    //   311: aload_3
    //   312: monitorexit
    //   313: aload 11
    //   315: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   59	159	168	finally
    //   162	165	168	finally
    //   168	173	168	finally
    //   179	196	249	finally
    //   201	244	249	finally
    //   249	254	249	finally
    //   19	161	309	finally
    //   162	198	309	finally
    //   201	246	309	finally
    //   249	313	309	finally
  }

  private int send(FileDescriptor paramFileDescriptor, ByteBuffer paramByteBuffer, SocketAddress paramSocketAddress)
    throws IOException
  {
    if (paramByteBuffer instanceof DirectBuffer)
      return sendFromNativeBuffer(paramFileDescriptor, paramByteBuffer, paramSocketAddress);
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
      int l = sendFromNativeBuffer(paramFileDescriptor, localByteBuffer, paramSocketAddress);
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

  private int sendFromNativeBuffer(FileDescriptor paramFileDescriptor, ByteBuffer paramByteBuffer, SocketAddress paramSocketAddress)
    throws IOException
  {
    int i = paramByteBuffer.position();
    int j = paramByteBuffer.limit();
    if ((!($assertionsDisabled)) && (i > j))
      throw new AssertionError();
    int k = (i <= j) ? j - i : 0;
    int l = send0(paramFileDescriptor, ((DirectBuffer)paramByteBuffer).address() + i, k, paramSocketAddress);
    if (l > 0)
      paramByteBuffer.position(i + l);
    return l;
  }

  // ERROR //
  public int read(ByteBuffer paramByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +11 -> 12
    //   4: new 182	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 391	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_0
    //   13: getfield 372	sun/nio/ch/DatagramChannelImpl:readLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore_2
    //   18: monitorenter
    //   19: aload_0
    //   20: getfield 373	sun/nio/ch/DatagramChannelImpl:stateLock	Ljava/lang/Object;
    //   23: dup
    //   24: astore_3
    //   25: monitorenter
    //   26: aload_0
    //   27: invokespecial 417	sun/nio/ch/DatagramChannelImpl:ensureOpen	()V
    //   30: aload_0
    //   31: invokevirtual 422	sun/nio/ch/DatagramChannelImpl:isConnected	()Z
    //   34: ifne +11 -> 45
    //   37: new 192	java/nio/channels/NotYetConnectedException
    //   40: dup
    //   41: invokespecial 413	java/nio/channels/NotYetConnectedException:<init>	()V
    //   44: athrow
    //   45: aload_3
    //   46: monitorexit
    //   47: goto +10 -> 57
    //   50: astore 4
    //   52: aload_3
    //   53: monitorexit
    //   54: aload 4
    //   56: athrow
    //   57: iconst_0
    //   58: istore_3
    //   59: aload_0
    //   60: invokevirtual 415	sun/nio/ch/DatagramChannelImpl:begin	()V
    //   63: aload_0
    //   64: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   67: ifne +14 -> 81
    //   70: iconst_0
    //   71: istore 4
    //   73: jsr +69 -> 142
    //   76: aload_2
    //   77: monitorexit
    //   78: iload 4
    //   80: ireturn
    //   81: aload_0
    //   82: invokestatic 452	sun/nio/ch/NativeThread:current	()J
    //   85: putfield 368	sun/nio/ch/DatagramChannelImpl:readerThread	J
    //   88: aload_0
    //   89: getfield 371	sun/nio/ch/DatagramChannelImpl:fd	Ljava/io/FileDescriptor;
    //   92: aload_1
    //   93: ldc2_w 172
    //   96: getstatic 380	sun/nio/ch/DatagramChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   99: aload_0
    //   100: getfield 372	sun/nio/ch/DatagramChannelImpl:readLock	Ljava/lang/Object;
    //   103: invokestatic 448	sun/nio/ch/IOUtil:read	(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;JLsun/nio/ch/NativeDispatcher;Ljava/lang/Object;)I
    //   106: istore_3
    //   107: iload_3
    //   108: bipush 253
    //   110: if_icmpne +10 -> 120
    //   113: aload_0
    //   114: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   117: ifne -29 -> 88
    //   120: iload_3
    //   121: invokestatic 440	sun/nio/ch/IOStatus:normalize	(I)I
    //   124: istore 4
    //   126: jsr +16 -> 142
    //   129: aload_2
    //   130: monitorexit
    //   131: iload 4
    //   133: ireturn
    //   134: astore 5
    //   136: jsr +6 -> 142
    //   139: aload 5
    //   141: athrow
    //   142: astore 6
    //   144: aload_0
    //   145: lconst_0
    //   146: putfield 368	sun/nio/ch/DatagramChannelImpl:readerThread	J
    //   149: aload_0
    //   150: iload_3
    //   151: ifgt +9 -> 160
    //   154: iload_3
    //   155: bipush 254
    //   157: if_icmpne +7 -> 164
    //   160: iconst_1
    //   161: goto +4 -> 165
    //   164: iconst_0
    //   165: invokevirtual 425	sun/nio/ch/DatagramChannelImpl:end	(Z)V
    //   168: getstatic 370	sun/nio/ch/DatagramChannelImpl:$assertionsDisabled	Z
    //   171: ifne +18 -> 189
    //   174: iload_3
    //   175: invokestatic 441	sun/nio/ch/IOStatus:check	(I)Z
    //   178: ifne +11 -> 189
    //   181: new 175	java/lang/AssertionError
    //   184: dup
    //   185: invokespecial 384	java/lang/AssertionError:<init>	()V
    //   188: athrow
    //   189: ret 6
    //   191: astore 7
    //   193: aload_2
    //   194: monitorexit
    //   195: aload 7
    //   197: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   26	47	50	finally
    //   50	54	50	finally
    //   59	76	134	finally
    //   81	129	134	finally
    //   134	139	134	finally
    //   19	78	191	finally
    //   81	131	191	finally
    //   134	195	191	finally
  }

  // ERROR //
  private long read0(ByteBuffer[] paramArrayOfByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +11 -> 12
    //   4: new 182	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 391	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_0
    //   13: getfield 372	sun/nio/ch/DatagramChannelImpl:readLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore_2
    //   18: monitorenter
    //   19: aload_0
    //   20: getfield 373	sun/nio/ch/DatagramChannelImpl:stateLock	Ljava/lang/Object;
    //   23: dup
    //   24: astore_3
    //   25: monitorenter
    //   26: aload_0
    //   27: invokespecial 417	sun/nio/ch/DatagramChannelImpl:ensureOpen	()V
    //   30: aload_0
    //   31: invokevirtual 422	sun/nio/ch/DatagramChannelImpl:isConnected	()Z
    //   34: ifne +11 -> 45
    //   37: new 192	java/nio/channels/NotYetConnectedException
    //   40: dup
    //   41: invokespecial 413	java/nio/channels/NotYetConnectedException:<init>	()V
    //   44: athrow
    //   45: aload_3
    //   46: monitorexit
    //   47: goto +10 -> 57
    //   50: astore 4
    //   52: aload_3
    //   53: monitorexit
    //   54: aload 4
    //   56: athrow
    //   57: lconst_0
    //   58: lstore_3
    //   59: aload_0
    //   60: invokevirtual 415	sun/nio/ch/DatagramChannelImpl:begin	()V
    //   63: aload_0
    //   64: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   67: ifne +14 -> 81
    //   70: lconst_0
    //   71: lstore 5
    //   73: jsr +64 -> 137
    //   76: aload_2
    //   77: monitorexit
    //   78: lload 5
    //   80: lreturn
    //   81: aload_0
    //   82: invokestatic 452	sun/nio/ch/NativeThread:current	()J
    //   85: putfield 368	sun/nio/ch/DatagramChannelImpl:readerThread	J
    //   88: aload_0
    //   89: getfield 371	sun/nio/ch/DatagramChannelImpl:fd	Ljava/io/FileDescriptor;
    //   92: aload_1
    //   93: getstatic 380	sun/nio/ch/DatagramChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   96: invokestatic 446	sun/nio/ch/IOUtil:read	(Ljava/io/FileDescriptor;[Ljava/nio/ByteBuffer;Lsun/nio/ch/NativeDispatcher;)J
    //   99: lstore_3
    //   100: lload_3
    //   101: ldc2_w 168
    //   104: lcmp
    //   105: ifne +10 -> 115
    //   108: aload_0
    //   109: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   112: ifne -24 -> 88
    //   115: lload_3
    //   116: invokestatic 442	sun/nio/ch/IOStatus:normalize	(J)J
    //   119: lstore 5
    //   121: jsr +16 -> 137
    //   124: aload_2
    //   125: monitorexit
    //   126: lload 5
    //   128: lreturn
    //   129: astore 7
    //   131: jsr +6 -> 137
    //   134: aload 7
    //   136: athrow
    //   137: astore 8
    //   139: aload_0
    //   140: lconst_0
    //   141: putfield 368	sun/nio/ch/DatagramChannelImpl:readerThread	J
    //   144: aload_0
    //   145: lload_3
    //   146: lconst_0
    //   147: lcmp
    //   148: ifgt +11 -> 159
    //   151: lload_3
    //   152: ldc2_w 170
    //   155: lcmp
    //   156: ifne +7 -> 163
    //   159: iconst_1
    //   160: goto +4 -> 164
    //   163: iconst_0
    //   164: invokevirtual 425	sun/nio/ch/DatagramChannelImpl:end	(Z)V
    //   167: getstatic 370	sun/nio/ch/DatagramChannelImpl:$assertionsDisabled	Z
    //   170: ifne +18 -> 188
    //   173: lload_3
    //   174: invokestatic 443	sun/nio/ch/IOStatus:check	(J)Z
    //   177: ifne +11 -> 188
    //   180: new 175	java/lang/AssertionError
    //   183: dup
    //   184: invokespecial 384	java/lang/AssertionError:<init>	()V
    //   187: athrow
    //   188: ret 8
    //   190: astore 9
    //   192: aload_2
    //   193: monitorexit
    //   194: aload 9
    //   196: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   26	47	50	finally
    //   50	54	50	finally
    //   59	76	129	finally
    //   81	124	129	finally
    //   129	134	129	finally
    //   19	78	190	finally
    //   81	126	190	finally
    //   129	194	190	finally
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
    //   4: new 182	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 391	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_0
    //   13: getfield 374	sun/nio/ch/DatagramChannelImpl:writeLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore_2
    //   18: monitorenter
    //   19: aload_0
    //   20: getfield 373	sun/nio/ch/DatagramChannelImpl:stateLock	Ljava/lang/Object;
    //   23: dup
    //   24: astore_3
    //   25: monitorenter
    //   26: aload_0
    //   27: invokespecial 417	sun/nio/ch/DatagramChannelImpl:ensureOpen	()V
    //   30: aload_0
    //   31: invokevirtual 422	sun/nio/ch/DatagramChannelImpl:isConnected	()Z
    //   34: ifne +11 -> 45
    //   37: new 192	java/nio/channels/NotYetConnectedException
    //   40: dup
    //   41: invokespecial 413	java/nio/channels/NotYetConnectedException:<init>	()V
    //   44: athrow
    //   45: aload_3
    //   46: monitorexit
    //   47: goto +10 -> 57
    //   50: astore 4
    //   52: aload_3
    //   53: monitorexit
    //   54: aload 4
    //   56: athrow
    //   57: iconst_0
    //   58: istore_3
    //   59: aload_0
    //   60: invokevirtual 415	sun/nio/ch/DatagramChannelImpl:begin	()V
    //   63: aload_0
    //   64: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   67: ifne +14 -> 81
    //   70: iconst_0
    //   71: istore 4
    //   73: jsr +69 -> 142
    //   76: aload_2
    //   77: monitorexit
    //   78: iload 4
    //   80: ireturn
    //   81: aload_0
    //   82: invokestatic 452	sun/nio/ch/NativeThread:current	()J
    //   85: putfield 369	sun/nio/ch/DatagramChannelImpl:writerThread	J
    //   88: aload_0
    //   89: getfield 371	sun/nio/ch/DatagramChannelImpl:fd	Ljava/io/FileDescriptor;
    //   92: aload_1
    //   93: ldc2_w 172
    //   96: getstatic 380	sun/nio/ch/DatagramChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   99: aload_0
    //   100: getfield 374	sun/nio/ch/DatagramChannelImpl:writeLock	Ljava/lang/Object;
    //   103: invokestatic 449	sun/nio/ch/IOUtil:write	(Ljava/io/FileDescriptor;Ljava/nio/ByteBuffer;JLsun/nio/ch/NativeDispatcher;Ljava/lang/Object;)I
    //   106: istore_3
    //   107: iload_3
    //   108: bipush 253
    //   110: if_icmpne +10 -> 120
    //   113: aload_0
    //   114: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   117: ifne -29 -> 88
    //   120: iload_3
    //   121: invokestatic 440	sun/nio/ch/IOStatus:normalize	(I)I
    //   124: istore 4
    //   126: jsr +16 -> 142
    //   129: aload_2
    //   130: monitorexit
    //   131: iload 4
    //   133: ireturn
    //   134: astore 5
    //   136: jsr +6 -> 142
    //   139: aload 5
    //   141: athrow
    //   142: astore 6
    //   144: aload_0
    //   145: lconst_0
    //   146: putfield 369	sun/nio/ch/DatagramChannelImpl:writerThread	J
    //   149: aload_0
    //   150: iload_3
    //   151: ifgt +9 -> 160
    //   154: iload_3
    //   155: bipush 254
    //   157: if_icmpne +7 -> 164
    //   160: iconst_1
    //   161: goto +4 -> 165
    //   164: iconst_0
    //   165: invokevirtual 425	sun/nio/ch/DatagramChannelImpl:end	(Z)V
    //   168: getstatic 370	sun/nio/ch/DatagramChannelImpl:$assertionsDisabled	Z
    //   171: ifne +18 -> 189
    //   174: iload_3
    //   175: invokestatic 441	sun/nio/ch/IOStatus:check	(I)Z
    //   178: ifne +11 -> 189
    //   181: new 175	java/lang/AssertionError
    //   184: dup
    //   185: invokespecial 384	java/lang/AssertionError:<init>	()V
    //   188: athrow
    //   189: ret 6
    //   191: astore 7
    //   193: aload_2
    //   194: monitorexit
    //   195: aload 7
    //   197: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   26	47	50	finally
    //   50	54	50	finally
    //   59	76	134	finally
    //   81	129	134	finally
    //   134	139	134	finally
    //   19	78	191	finally
    //   81	131	191	finally
    //   134	195	191	finally
  }

  // ERROR //
  private long write0(ByteBuffer[] paramArrayOfByteBuffer)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +11 -> 12
    //   4: new 182	java/lang/NullPointerException
    //   7: dup
    //   8: invokespecial 391	java/lang/NullPointerException:<init>	()V
    //   11: athrow
    //   12: aload_0
    //   13: getfield 374	sun/nio/ch/DatagramChannelImpl:writeLock	Ljava/lang/Object;
    //   16: dup
    //   17: astore_2
    //   18: monitorenter
    //   19: aload_0
    //   20: getfield 373	sun/nio/ch/DatagramChannelImpl:stateLock	Ljava/lang/Object;
    //   23: dup
    //   24: astore_3
    //   25: monitorenter
    //   26: aload_0
    //   27: invokespecial 417	sun/nio/ch/DatagramChannelImpl:ensureOpen	()V
    //   30: aload_0
    //   31: invokevirtual 422	sun/nio/ch/DatagramChannelImpl:isConnected	()Z
    //   34: ifne +11 -> 45
    //   37: new 192	java/nio/channels/NotYetConnectedException
    //   40: dup
    //   41: invokespecial 413	java/nio/channels/NotYetConnectedException:<init>	()V
    //   44: athrow
    //   45: aload_3
    //   46: monitorexit
    //   47: goto +10 -> 57
    //   50: astore 4
    //   52: aload_3
    //   53: monitorexit
    //   54: aload 4
    //   56: athrow
    //   57: lconst_0
    //   58: lstore_3
    //   59: aload_0
    //   60: invokevirtual 415	sun/nio/ch/DatagramChannelImpl:begin	()V
    //   63: aload_0
    //   64: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   67: ifne +14 -> 81
    //   70: lconst_0
    //   71: lstore 5
    //   73: jsr +64 -> 137
    //   76: aload_2
    //   77: monitorexit
    //   78: lload 5
    //   80: lreturn
    //   81: aload_0
    //   82: invokestatic 452	sun/nio/ch/NativeThread:current	()J
    //   85: putfield 369	sun/nio/ch/DatagramChannelImpl:writerThread	J
    //   88: aload_0
    //   89: getfield 371	sun/nio/ch/DatagramChannelImpl:fd	Ljava/io/FileDescriptor;
    //   92: aload_1
    //   93: getstatic 380	sun/nio/ch/DatagramChannelImpl:nd	Lsun/nio/ch/NativeDispatcher;
    //   96: invokestatic 447	sun/nio/ch/IOUtil:write	(Ljava/io/FileDescriptor;[Ljava/nio/ByteBuffer;Lsun/nio/ch/NativeDispatcher;)J
    //   99: lstore_3
    //   100: lload_3
    //   101: ldc2_w 168
    //   104: lcmp
    //   105: ifne +10 -> 115
    //   108: aload_0
    //   109: invokevirtual 423	sun/nio/ch/DatagramChannelImpl:isOpen	()Z
    //   112: ifne -24 -> 88
    //   115: lload_3
    //   116: invokestatic 442	sun/nio/ch/IOStatus:normalize	(J)J
    //   119: lstore 5
    //   121: jsr +16 -> 137
    //   124: aload_2
    //   125: monitorexit
    //   126: lload 5
    //   128: lreturn
    //   129: astore 7
    //   131: jsr +6 -> 137
    //   134: aload 7
    //   136: athrow
    //   137: astore 8
    //   139: aload_0
    //   140: lconst_0
    //   141: putfield 369	sun/nio/ch/DatagramChannelImpl:writerThread	J
    //   144: aload_0
    //   145: lload_3
    //   146: lconst_0
    //   147: lcmp
    //   148: ifgt +11 -> 159
    //   151: lload_3
    //   152: ldc2_w 170
    //   155: lcmp
    //   156: ifne +7 -> 163
    //   159: iconst_1
    //   160: goto +4 -> 164
    //   163: iconst_0
    //   164: invokevirtual 425	sun/nio/ch/DatagramChannelImpl:end	(Z)V
    //   167: getstatic 370	sun/nio/ch/DatagramChannelImpl:$assertionsDisabled	Z
    //   170: ifne +18 -> 188
    //   173: lload_3
    //   174: invokestatic 443	sun/nio/ch/IOStatus:check	(J)Z
    //   177: ifne +11 -> 188
    //   180: new 175	java/lang/AssertionError
    //   183: dup
    //   184: invokespecial 384	java/lang/AssertionError:<init>	()V
    //   187: athrow
    //   188: ret 8
    //   190: astore 9
    //   192: aload_2
    //   193: monitorexit
    //   194: aload 9
    //   196: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   26	47	50	finally
    //   50	54	50	finally
    //   59	76	129	finally
    //   81	124	129	finally
    //   129	134	129	finally
    //   19	78	190	finally
    //   81	126	190	finally
    //   129	194	190	finally
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
            return Net.getIntOption(this.this$0.fd, paramInt);
          }

          void setInt(, int paramInt2)
            throws IOException
          {
            Net.setIntOption(this.this$0.fd, paramInt1, paramInt2);
          }
        };
        this.options = new SocketOptsImpl.IP(local1);
      }
      return this.options;
    }
  }

  public boolean isBound()
  {
    return (Net.localPortNumber(this.fd) != 0);
  }

  public SocketAddress localAddress()
  {
    synchronized (this.stateLock)
    {
      if ((isConnected()) && (this.localAddress == null))
        this.localAddress = Net.localAddress(this.fd);
      SecurityManager localSecurityManager = System.getSecurityManager();
      if (localSecurityManager != null)
      {
        InetSocketAddress localInetSocketAddress = (InetSocketAddress)this.localAddress;
        localSecurityManager.checkConnect(localInetSocketAddress.getAddress().getHostAddress(), -1);
      }
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
          ensureOpen();
          if (isBound())
            throw new AlreadyBoundException();
          InetSocketAddress localInetSocketAddress = Net.checkAddress(paramSocketAddress);
          SecurityManager localSecurityManager = System.getSecurityManager();
          if (localSecurityManager != null)
            localSecurityManager.checkListen(localInetSocketAddress.getPort());
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
      return ((this.state == ST_CONNECTED) ? 1 : false);
    }
  }

  void ensureOpenAndUnconnected()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      if (!(isOpen()))
        throw new ClosedChannelException();
      if (this.state != ST_UNCONNECTED)
        throw new IllegalStateException("Connect already invoked");
    }
  }

  public DatagramChannel connect(SocketAddress paramSocketAddress)
    throws IOException
  {
    int i = 0;
    int j = 0;
    synchronized (this.readLock)
    {
      synchronized (this.writeLock)
      {
        synchronized (this.stateLock)
        {
          ensureOpenAndUnconnected();
          InetSocketAddress localInetSocketAddress = Net.checkAddress(paramSocketAddress);
          SecurityManager localSecurityManager = System.getSecurityManager();
          if (localSecurityManager != null)
            localSecurityManager.checkConnect(localInetSocketAddress.getAddress().getHostAddress(), localInetSocketAddress.getPort());
          int k = Net.connect(this.fd, localInetSocketAddress.getAddress(), localInetSocketAddress.getPort(), i);
          if (k <= 0)
            throw new Error();
          this.state = ST_CONNECTED;
          this.remoteAddress = paramSocketAddress;
          this.sender = localInetSocketAddress;
          this.cachedSenderInetAddress = localInetSocketAddress.getAddress();
          this.cachedSenderPort = localInetSocketAddress.getPort();
        }
      }
    }
    return this;
  }

  public DatagramChannel disconnect()
    throws IOException
  {
    synchronized (this.readLock)
    {
      synchronized (this.writeLock)
      {
        synchronized (this.stateLock)
        {
          if ((isConnected()) && (isOpen()))
            break label43;
          monitorexit;
          monitorexit;
          return this;
          label43: InetSocketAddress localInetSocketAddress = (InetSocketAddress)this.remoteAddress;
          SecurityManager localSecurityManager = System.getSecurityManager();
          if (localSecurityManager == null)
            break label80;
          localSecurityManager.checkConnect(localInetSocketAddress.getAddress().getHostAddress(), localInetSocketAddress.getPort());
          label80: disconnect0(this.fd);
          this.remoteAddress = null;
          this.state = ST_UNCONNECTED;
        }
      }
    }
    return this;
  }

  protected void implCloseSelectableChannel()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      long l;
      nd.preClose(this.fd);
      if ((l = this.readerThread) != 3412046930476269568L)
        NativeThread.signal(l);
      if ((l = this.writerThread) != 3412046930476269568L)
        NativeThread.signal(l);
      if (!(isRegistered()))
        kill();
    }
  }

  public void kill()
    throws IOException
  {
    synchronized (this.stateLock)
    {
      if (this.state != 2)
        break label18;
      return;
      label18: if (this.state != -1)
        break label34;
      this.state = 2;
      return;
      label34: if (($assertionsDisabled) || ((!(isOpen())) && (!(isRegistered()))))
        break label62;
      throw new AssertionError();
      label62: nd.close(this.fd);
      this.state = 2;
    }
  }

  protected void finalize()
    throws IOException
  {
    close();
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
      return ((k & (j ^ 0xFFFFFFFF)) != 0);
    }
    if (((paramInt1 & 0x1) != 0) && ((i & 0x1) != 0))
      k |= 1;
    if (((paramInt1 & 0x4) != 0) && ((i & 0x4) != 0))
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
      i |= 1;
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

  private static native void initIDs();

  private static native void disconnect0(FileDescriptor paramFileDescriptor)
    throws IOException;

  private native int receive0(FileDescriptor paramFileDescriptor, long paramLong, int paramInt, boolean paramBoolean)
    throws IOException;

  private native int send0(FileDescriptor paramFileDescriptor, long paramLong, int paramInt, SocketAddress paramSocketAddress)
    throws IOException;

  static
  {
    nd = new DatagramDispatcher();
    ST_UNCONNECTED = 0;
    ST_CONNECTED = 1;
    Util.load();
    initIDs();
  }
}