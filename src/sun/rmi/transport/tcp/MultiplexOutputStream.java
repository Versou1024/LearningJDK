package sun.rmi.transport.tcp;

import java.io.IOException;
import java.io.OutputStream;

final class MultiplexOutputStream extends OutputStream
{
  private ConnectionMultiplexer manager;
  private MultiplexConnectionInfo info;
  private byte[] buffer;
  private int pos = 0;
  private int requested = 0;
  private boolean disconnected = false;
  private Object lock = new Object();

  MultiplexOutputStream(ConnectionMultiplexer paramConnectionMultiplexer, MultiplexConnectionInfo paramMultiplexConnectionInfo, int paramInt)
  {
    this.manager = paramConnectionMultiplexer;
    this.info = paramMultiplexConnectionInfo;
    this.buffer = new byte[paramInt];
    this.pos = 0;
  }

  public synchronized void write(int paramInt)
    throws IOException
  {
    while (this.pos >= this.buffer.length)
      push();
    this.buffer[(this.pos++)] = (byte)paramInt;
  }

  // ERROR //
  public synchronized void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    // Byte code:
    //   0: iload_3
    //   1: ifgt +4 -> 5
    //   4: return
    //   5: aload_0
    //   6: getfield 71	sun/rmi/transport/tcp/MultiplexOutputStream:buffer	[B
    //   9: arraylength
    //   10: aload_0
    //   11: getfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   14: isub
    //   15: istore 4
    //   17: iload_3
    //   18: iload 4
    //   20: if_icmpgt +28 -> 48
    //   23: aload_1
    //   24: iload_2
    //   25: aload_0
    //   26: getfield 71	sun/rmi/transport/tcp/MultiplexOutputStream:buffer	[B
    //   29: aload_0
    //   30: getfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   33: iload_3
    //   34: invokestatic 80	java/lang/System:arraycopy	(Ljava/lang/Object;ILjava/lang/Object;II)V
    //   37: aload_0
    //   38: dup
    //   39: getfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   42: iload_3
    //   43: iadd
    //   44: putfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   47: return
    //   48: aload_0
    //   49: invokevirtual 83	sun/rmi/transport/tcp/MultiplexOutputStream:flush	()V
    //   52: aload_0
    //   53: getfield 72	sun/rmi/transport/tcp/MultiplexOutputStream:lock	Ljava/lang/Object;
    //   56: dup
    //   57: astore 6
    //   59: monitorenter
    //   60: aload_0
    //   61: getfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   64: dup
    //   65: istore 5
    //   67: iconst_1
    //   68: if_icmpge +25 -> 93
    //   71: aload_0
    //   72: getfield 70	sun/rmi/transport/tcp/MultiplexOutputStream:disconnected	Z
    //   75: ifne +18 -> 93
    //   78: aload_0
    //   79: getfield 72	sun/rmi/transport/tcp/MultiplexOutputStream:lock	Ljava/lang/Object;
    //   82: invokevirtual 79	java/lang/Object:wait	()V
    //   85: goto -25 -> 60
    //   88: astore 7
    //   90: goto -30 -> 60
    //   93: aload_0
    //   94: getfield 70	sun/rmi/transport/tcp/MultiplexOutputStream:disconnected	Z
    //   97: ifeq +13 -> 110
    //   100: new 39	IOException
    //   103: dup
    //   104: ldc 1
    //   106: invokespecial 75	IOException:<init>	(Ljava/lang/String;)V
    //   109: athrow
    //   110: aload 6
    //   112: monitorexit
    //   113: goto +11 -> 124
    //   116: astore 8
    //   118: aload 6
    //   120: monitorexit
    //   121: aload 8
    //   123: athrow
    //   124: iload 5
    //   126: iload_3
    //   127: if_icmpge +64 -> 191
    //   130: aload_0
    //   131: getfield 73	sun/rmi/transport/tcp/MultiplexOutputStream:manager	Lsun/rmi/transport/tcp/ConnectionMultiplexer;
    //   134: aload_0
    //   135: getfield 74	sun/rmi/transport/tcp/MultiplexOutputStream:info	Lsun/rmi/transport/tcp/MultiplexConnectionInfo;
    //   138: aload_1
    //   139: iload_2
    //   140: iload 5
    //   142: invokevirtual 82	sun/rmi/transport/tcp/ConnectionMultiplexer:sendTransmit	(Lsun/rmi/transport/tcp/MultiplexConnectionInfo;[BII)V
    //   145: iload_2
    //   146: iload 5
    //   148: iadd
    //   149: istore_2
    //   150: iload_3
    //   151: iload 5
    //   153: isub
    //   154: istore_3
    //   155: aload_0
    //   156: getfield 72	sun/rmi/transport/tcp/MultiplexOutputStream:lock	Ljava/lang/Object;
    //   159: dup
    //   160: astore 6
    //   162: monitorenter
    //   163: aload_0
    //   164: dup
    //   165: getfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   168: iload 5
    //   170: isub
    //   171: putfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   174: aload 6
    //   176: monitorexit
    //   177: goto +11 -> 188
    //   180: astore 9
    //   182: aload 6
    //   184: monitorexit
    //   185: aload 9
    //   187: athrow
    //   188: goto -136 -> 52
    //   191: aload_0
    //   192: getfield 73	sun/rmi/transport/tcp/MultiplexOutputStream:manager	Lsun/rmi/transport/tcp/ConnectionMultiplexer;
    //   195: aload_0
    //   196: getfield 74	sun/rmi/transport/tcp/MultiplexOutputStream:info	Lsun/rmi/transport/tcp/MultiplexConnectionInfo;
    //   199: aload_1
    //   200: iload_2
    //   201: iload_3
    //   202: invokevirtual 82	sun/rmi/transport/tcp/ConnectionMultiplexer:sendTransmit	(Lsun/rmi/transport/tcp/MultiplexConnectionInfo;[BII)V
    //   205: aload_0
    //   206: getfield 72	sun/rmi/transport/tcp/MultiplexOutputStream:lock	Ljava/lang/Object;
    //   209: dup
    //   210: astore 6
    //   212: monitorenter
    //   213: aload_0
    //   214: dup
    //   215: getfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   218: iload_3
    //   219: isub
    //   220: putfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   223: aload 6
    //   225: monitorexit
    //   226: goto +11 -> 237
    //   229: astore 10
    //   231: aload 6
    //   233: monitorexit
    //   234: aload 10
    //   236: athrow
    //   237: goto +3 -> 240
    //   240: return
    //
    // Exception table:
    //   from	to	target	type
    //   78	85	88	java/lang/InterruptedException
    //   60	113	116	finally
    //   116	121	116	finally
    //   163	177	180	finally
    //   180	185	180	finally
    //   213	226	229	finally
    //   229	234	229	finally
  }

  public synchronized void flush()
    throws IOException
  {
    while (this.pos > 0)
      push();
  }

  public void close()
    throws IOException
  {
    this.manager.sendClose(this.info);
  }

  void request(int paramInt)
  {
    synchronized (this.lock)
    {
      this.requested += paramInt;
      this.lock.notifyAll();
    }
  }

  void disconnect()
  {
    synchronized (this.lock)
    {
      this.disconnected = true;
      this.lock.notifyAll();
    }
  }

  // ERROR //
  private void push()
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 72	sun/rmi/transport/tcp/MultiplexOutputStream:lock	Ljava/lang/Object;
    //   4: dup
    //   5: astore_2
    //   6: monitorenter
    //   7: aload_0
    //   8: getfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   11: dup
    //   12: istore_1
    //   13: iconst_1
    //   14: if_icmpge +24 -> 38
    //   17: aload_0
    //   18: getfield 70	sun/rmi/transport/tcp/MultiplexOutputStream:disconnected	Z
    //   21: ifne +17 -> 38
    //   24: aload_0
    //   25: getfield 72	sun/rmi/transport/tcp/MultiplexOutputStream:lock	Ljava/lang/Object;
    //   28: invokevirtual 79	java/lang/Object:wait	()V
    //   31: goto -24 -> 7
    //   34: astore_3
    //   35: goto -28 -> 7
    //   38: aload_0
    //   39: getfield 70	sun/rmi/transport/tcp/MultiplexOutputStream:disconnected	Z
    //   42: ifeq +13 -> 55
    //   45: new 39	IOException
    //   48: dup
    //   49: ldc 1
    //   51: invokespecial 75	IOException:<init>	(Ljava/lang/String;)V
    //   54: athrow
    //   55: aload_2
    //   56: monitorexit
    //   57: goto +10 -> 67
    //   60: astore 4
    //   62: aload_2
    //   63: monitorexit
    //   64: aload 4
    //   66: athrow
    //   67: iload_1
    //   68: aload_0
    //   69: getfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   72: if_icmpge +81 -> 153
    //   75: aload_0
    //   76: getfield 73	sun/rmi/transport/tcp/MultiplexOutputStream:manager	Lsun/rmi/transport/tcp/ConnectionMultiplexer;
    //   79: aload_0
    //   80: getfield 74	sun/rmi/transport/tcp/MultiplexOutputStream:info	Lsun/rmi/transport/tcp/MultiplexConnectionInfo;
    //   83: aload_0
    //   84: getfield 71	sun/rmi/transport/tcp/MultiplexOutputStream:buffer	[B
    //   87: iconst_0
    //   88: iload_1
    //   89: invokevirtual 82	sun/rmi/transport/tcp/ConnectionMultiplexer:sendTransmit	(Lsun/rmi/transport/tcp/MultiplexConnectionInfo;[BII)V
    //   92: aload_0
    //   93: getfield 71	sun/rmi/transport/tcp/MultiplexOutputStream:buffer	[B
    //   96: iload_1
    //   97: aload_0
    //   98: getfield 71	sun/rmi/transport/tcp/MultiplexOutputStream:buffer	[B
    //   101: iconst_0
    //   102: aload_0
    //   103: getfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   106: iload_1
    //   107: isub
    //   108: invokestatic 80	java/lang/System:arraycopy	(Ljava/lang/Object;ILjava/lang/Object;II)V
    //   111: aload_0
    //   112: dup
    //   113: getfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   116: iload_1
    //   117: isub
    //   118: putfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   121: aload_0
    //   122: getfield 72	sun/rmi/transport/tcp/MultiplexOutputStream:lock	Ljava/lang/Object;
    //   125: dup
    //   126: astore_2
    //   127: monitorenter
    //   128: aload_0
    //   129: dup
    //   130: getfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   133: iload_1
    //   134: isub
    //   135: putfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   138: aload_2
    //   139: monitorexit
    //   140: goto +10 -> 150
    //   143: astore 5
    //   145: aload_2
    //   146: monitorexit
    //   147: aload 5
    //   149: athrow
    //   150: goto +60 -> 210
    //   153: aload_0
    //   154: getfield 73	sun/rmi/transport/tcp/MultiplexOutputStream:manager	Lsun/rmi/transport/tcp/ConnectionMultiplexer;
    //   157: aload_0
    //   158: getfield 74	sun/rmi/transport/tcp/MultiplexOutputStream:info	Lsun/rmi/transport/tcp/MultiplexConnectionInfo;
    //   161: aload_0
    //   162: getfield 71	sun/rmi/transport/tcp/MultiplexOutputStream:buffer	[B
    //   165: iconst_0
    //   166: aload_0
    //   167: getfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   170: invokevirtual 82	sun/rmi/transport/tcp/ConnectionMultiplexer:sendTransmit	(Lsun/rmi/transport/tcp/MultiplexConnectionInfo;[BII)V
    //   173: aload_0
    //   174: getfield 72	sun/rmi/transport/tcp/MultiplexOutputStream:lock	Ljava/lang/Object;
    //   177: dup
    //   178: astore_2
    //   179: monitorenter
    //   180: aload_0
    //   181: dup
    //   182: getfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   185: aload_0
    //   186: getfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   189: isub
    //   190: putfield 69	sun/rmi/transport/tcp/MultiplexOutputStream:requested	I
    //   193: aload_2
    //   194: monitorexit
    //   195: goto +10 -> 205
    //   198: astore 6
    //   200: aload_2
    //   201: monitorexit
    //   202: aload 6
    //   204: athrow
    //   205: aload_0
    //   206: iconst_0
    //   207: putfield 68	sun/rmi/transport/tcp/MultiplexOutputStream:pos	I
    //   210: return
    //
    // Exception table:
    //   from	to	target	type
    //   24	31	34	java/lang/InterruptedException
    //   7	57	60	finally
    //   60	64	60	finally
    //   128	140	143	finally
    //   143	147	143	finally
    //   180	195	198	finally
    //   198	202	198	finally
  }
}