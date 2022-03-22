package sun.rmi.transport.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

final class MultiplexInputStream extends InputStream
{
  private ConnectionMultiplexer manager;
  private MultiplexConnectionInfo info;
  private byte[] buffer;
  private int present = 0;
  private int pos = 0;
  private int requested = 0;
  private boolean disconnected = false;
  private Object lock = new Object();
  private int waterMark;
  private byte[] temp = new byte[1];

  MultiplexInputStream(ConnectionMultiplexer paramConnectionMultiplexer, MultiplexConnectionInfo paramMultiplexConnectionInfo, int paramInt)
  {
    this.manager = paramConnectionMultiplexer;
    this.info = paramMultiplexConnectionInfo;
    this.buffer = new byte[paramInt];
    this.waterMark = (paramInt / 2);
  }

  public synchronized int read()
    throws IOException
  {
    int i = read(this.temp, 0, 1);
    if (i != 1)
      return -1;
    return (this.temp[0] & 0xFF);
  }

  // ERROR //
  public synchronized int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    // Byte code:
    //   0: iload_3
    //   1: ifgt +5 -> 6
    //   4: iconst_0
    //   5: ireturn
    //   6: aload_0
    //   7: getfield 90	sun/rmi/transport/tcp/MultiplexInputStream:lock	Ljava/lang/Object;
    //   10: dup
    //   11: astore 5
    //   13: monitorenter
    //   14: aload_0
    //   15: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   18: aload_0
    //   19: getfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   22: if_icmplt +16 -> 38
    //   25: aload_0
    //   26: aload_0
    //   27: iconst_0
    //   28: dup_x1
    //   29: putfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   32: putfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   35: goto +57 -> 92
    //   38: aload_0
    //   39: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   42: aload_0
    //   43: getfield 86	sun/rmi/transport/tcp/MultiplexInputStream:waterMark	I
    //   46: if_icmplt +46 -> 92
    //   49: aload_0
    //   50: getfield 88	sun/rmi/transport/tcp/MultiplexInputStream:buffer	[B
    //   53: aload_0
    //   54: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   57: aload_0
    //   58: getfield 88	sun/rmi/transport/tcp/MultiplexInputStream:buffer	[B
    //   61: iconst_0
    //   62: aload_0
    //   63: getfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   66: aload_0
    //   67: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   70: isub
    //   71: invokestatic 100	java/lang/System:arraycopy	(Ljava/lang/Object;ILjava/lang/Object;II)V
    //   74: aload_0
    //   75: dup
    //   76: getfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   79: aload_0
    //   80: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   83: isub
    //   84: putfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   87: aload_0
    //   88: iconst_0
    //   89: putfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   92: aload_0
    //   93: getfield 88	sun/rmi/transport/tcp/MultiplexInputStream:buffer	[B
    //   96: arraylength
    //   97: aload_0
    //   98: getfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   101: isub
    //   102: istore 6
    //   104: iload 6
    //   106: aload_0
    //   107: getfield 85	sun/rmi/transport/tcp/MultiplexInputStream:requested	I
    //   110: isub
    //   111: iconst_0
    //   112: invokestatic 96	java/lang/Math:max	(II)I
    //   115: istore 4
    //   117: aload 5
    //   119: monitorexit
    //   120: goto +11 -> 131
    //   123: astore 7
    //   125: aload 5
    //   127: monitorexit
    //   128: aload 7
    //   130: athrow
    //   131: iload 4
    //   133: ifle +16 -> 149
    //   136: aload_0
    //   137: getfield 91	sun/rmi/transport/tcp/MultiplexInputStream:manager	Lsun/rmi/transport/tcp/ConnectionMultiplexer;
    //   140: aload_0
    //   141: getfield 92	sun/rmi/transport/tcp/MultiplexInputStream:info	Lsun/rmi/transport/tcp/MultiplexConnectionInfo;
    //   144: iload 4
    //   146: invokevirtual 102	sun/rmi/transport/tcp/ConnectionMultiplexer:sendRequest	(Lsun/rmi/transport/tcp/MultiplexConnectionInfo;I)V
    //   149: aload_0
    //   150: getfield 90	sun/rmi/transport/tcp/MultiplexInputStream:lock	Ljava/lang/Object;
    //   153: dup
    //   154: astore 5
    //   156: monitorenter
    //   157: aload_0
    //   158: dup
    //   159: getfield 85	sun/rmi/transport/tcp/MultiplexInputStream:requested	I
    //   162: iload 4
    //   164: iadd
    //   165: putfield 85	sun/rmi/transport/tcp/MultiplexInputStream:requested	I
    //   168: aload_0
    //   169: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   172: aload_0
    //   173: getfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   176: if_icmplt +25 -> 201
    //   179: aload_0
    //   180: getfield 87	sun/rmi/transport/tcp/MultiplexInputStream:disconnected	Z
    //   183: ifne +18 -> 201
    //   186: aload_0
    //   187: getfield 90	sun/rmi/transport/tcp/MultiplexInputStream:lock	Ljava/lang/Object;
    //   190: invokevirtual 99	java/lang/Object:wait	()V
    //   193: goto -25 -> 168
    //   196: astore 6
    //   198: goto -30 -> 168
    //   201: aload_0
    //   202: getfield 87	sun/rmi/transport/tcp/MultiplexInputStream:disconnected	Z
    //   205: ifeq +19 -> 224
    //   208: aload_0
    //   209: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   212: aload_0
    //   213: getfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   216: if_icmplt +8 -> 224
    //   219: iconst_m1
    //   220: aload 5
    //   222: monitorexit
    //   223: ireturn
    //   224: aload_0
    //   225: getfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   228: aload_0
    //   229: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   232: isub
    //   233: istore 6
    //   235: iload_3
    //   236: iload 6
    //   238: if_icmpge +32 -> 270
    //   241: aload_0
    //   242: getfield 88	sun/rmi/transport/tcp/MultiplexInputStream:buffer	[B
    //   245: aload_0
    //   246: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   249: aload_1
    //   250: iload_2
    //   251: iload_3
    //   252: invokestatic 100	java/lang/System:arraycopy	(Ljava/lang/Object;ILjava/lang/Object;II)V
    //   255: aload_0
    //   256: dup
    //   257: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   260: iload_3
    //   261: iadd
    //   262: putfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   265: iload_3
    //   266: aload 5
    //   268: monitorexit
    //   269: ireturn
    //   270: aload_0
    //   271: getfield 88	sun/rmi/transport/tcp/MultiplexInputStream:buffer	[B
    //   274: aload_0
    //   275: getfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   278: aload_1
    //   279: iload_2
    //   280: iload 6
    //   282: invokestatic 100	java/lang/System:arraycopy	(Ljava/lang/Object;ILjava/lang/Object;II)V
    //   285: aload_0
    //   286: aload_0
    //   287: iconst_0
    //   288: dup_x1
    //   289: putfield 84	sun/rmi/transport/tcp/MultiplexInputStream:present	I
    //   292: putfield 83	sun/rmi/transport/tcp/MultiplexInputStream:pos	I
    //   295: iload 6
    //   297: aload 5
    //   299: monitorexit
    //   300: ireturn
    //   301: astore 8
    //   303: aload 5
    //   305: monitorexit
    //   306: aload 8
    //   308: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   14	120	123	finally
    //   123	128	123	finally
    //   186	193	196	java/lang/InterruptedException
    //   157	223	301	finally
    //   224	269	301	finally
    //   270	300	301	finally
    //   301	306	301	finally
  }

  public int available()
    throws IOException
  {
    synchronized (this.lock)
    {
      return (this.present - this.pos);
    }
  }

  public void close()
    throws IOException
  {
    this.manager.sendClose(this.info);
  }

  void receive(int paramInt, DataInputStream paramDataInputStream)
    throws IOException
  {
    synchronized (this.lock)
    {
      if ((this.pos > 0) && (this.buffer.length - this.present < paramInt))
      {
        System.arraycopy(this.buffer, this.pos, this.buffer, 0, this.present - this.pos);
        this.present -= this.pos;
        this.pos = 0;
      }
      if (this.buffer.length - this.present < paramInt)
        throw new IOException("Receive buffer overflow");
      paramDataInputStream.readFully(this.buffer, this.present, paramInt);
      this.present += paramInt;
      this.requested -= paramInt;
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
}