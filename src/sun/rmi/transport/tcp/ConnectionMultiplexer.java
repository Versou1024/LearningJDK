package sun.rmi.transport.tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.server.LogStream;
import java.security.AccessController;
import java.util.Enumeration;
import java.util.Hashtable;
import sun.rmi.runtime.Log;
import sun.security.action.GetPropertyAction;

final class ConnectionMultiplexer
{
  static int logLevel = LogStream.parseLevel(getLogLevel());
  static final Log multiplexLog = Log.getLog("sun.rmi.transport.tcp.multiplex", "multiplex", logLevel);
  private static final int OPEN = 225;
  private static final int CLOSE = 226;
  private static final int CLOSEACK = 227;
  private static final int REQUEST = 228;
  private static final int TRANSMIT = 229;
  private TCPChannel channel;
  private InputStream in;
  private OutputStream out;
  private boolean orig;
  private java.io.DataInputStream dataIn;
  private DataOutputStream dataOut;
  private Hashtable connectionTable = new Hashtable(7);
  private int numConnections = 0;
  private static final int maxConnections = 256;
  private int lastID = 4097;
  private boolean alive = true;

  private static String getLogLevel()
  {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("sun.rmi.transport.tcp.multiplex.logLevel")));
  }

  public ConnectionMultiplexer(TCPChannel paramTCPChannel, InputStream paramInputStream, OutputStream paramOutputStream, boolean paramBoolean)
  {
    this.channel = paramTCPChannel;
    this.in = paramInputStream;
    this.out = paramOutputStream;
    this.orig = paramBoolean;
    this.dataIn = new java.io.DataInputStream(paramInputStream);
    this.dataOut = new DataOutputStream(paramOutputStream);
  }

  // ERROR //
  public void run()
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 263	sun/rmi/transport/tcp/ConnectionMultiplexer:dataIn	Ljava/io/DataInputStream;
    //   4: invokevirtual 275	java/io/DataInputStream:readUnsignedByte	()I
    //   7: istore_1
    //   8: iload_1
    //   9: tableswitch	default:+811 -> 820, 225:+35->44, 226:+247->256, 227:+409->418, 228:+575->584, 229:+691->700
    //   45: getfield 263	sun/rmi/transport/tcp/ConnectionMultiplexer:dataIn	Ljava/io/DataInputStream;
    //   48: invokevirtual 276	java/io/DataInputStream:readUnsignedShort	()I
    //   51: istore_2
    //   52: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   55: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   58: invokevirtual 303	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   61: ifeq +31 -> 92
    //   64: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   67: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   70: new 151	java/lang/StringBuilder
    //   73: dup
    //   74: invokespecial 291	java/lang/StringBuilder:<init>	()V
    //   77: ldc 16
    //   79: invokevirtual 294	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   82: iload_2
    //   83: invokevirtual 293	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   86: invokevirtual 292	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   89: invokevirtual 304	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   92: new 148	java/lang/Integer
    //   95: dup
    //   96: iload_2
    //   97: invokespecial 287	java/lang/Integer:<init>	(I)V
    //   100: astore 4
    //   102: aload_0
    //   103: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   106: aload 4
    //   108: invokevirtual 300	java/util/Hashtable:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   111: checkcast 159	sun/rmi/transport/tcp/MultiplexConnectionInfo
    //   114: astore 5
    //   116: aload 5
    //   118: ifnull +13 -> 131
    //   121: new 145	IOException
    //   124: dup
    //   125: ldc 9
    //   127: invokespecial 284	IOException:<init>	(Ljava/lang/String;)V
    //   130: athrow
    //   131: new 159	sun/rmi/transport/tcp/MultiplexConnectionInfo
    //   134: dup
    //   135: iload_2
    //   136: invokespecial 310	sun/rmi/transport/tcp/MultiplexConnectionInfo:<init>	(I)V
    //   139: astore 5
    //   141: aload 5
    //   143: new 160	sun/rmi/transport/tcp/MultiplexInputStream
    //   146: dup
    //   147: aload_0
    //   148: aload 5
    //   150: sipush 2048
    //   153: invokespecial 313	sun/rmi/transport/tcp/MultiplexInputStream:<init>	(Lsun/rmi/transport/tcp/ConnectionMultiplexer;Lsun/rmi/transport/tcp/MultiplexConnectionInfo;I)V
    //   156: putfield 272	sun/rmi/transport/tcp/MultiplexConnectionInfo:in	Lsun/rmi/transport/tcp/MultiplexInputStream;
    //   159: aload 5
    //   161: new 161	sun/rmi/transport/tcp/MultiplexOutputStream
    //   164: dup
    //   165: aload_0
    //   166: aload 5
    //   168: sipush 2048
    //   171: invokespecial 316	sun/rmi/transport/tcp/MultiplexOutputStream:<init>	(Lsun/rmi/transport/tcp/ConnectionMultiplexer;Lsun/rmi/transport/tcp/MultiplexConnectionInfo;I)V
    //   174: putfield 273	sun/rmi/transport/tcp/MultiplexConnectionInfo:out	Lsun/rmi/transport/tcp/MultiplexOutputStream;
    //   177: aload_0
    //   178: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   181: dup
    //   182: astore 6
    //   184: monitorenter
    //   185: aload_0
    //   186: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   189: aload 4
    //   191: aload 5
    //   193: invokevirtual 302	java/util/Hashtable:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   196: pop
    //   197: aload_0
    //   198: dup
    //   199: getfield 260	sun/rmi/transport/tcp/ConnectionMultiplexer:numConnections	I
    //   202: iconst_1
    //   203: iadd
    //   204: putfield 260	sun/rmi/transport/tcp/ConnectionMultiplexer:numConnections	I
    //   207: aload 6
    //   209: monitorexit
    //   210: goto +11 -> 221
    //   213: astore 7
    //   215: aload 6
    //   217: monitorexit
    //   218: aload 7
    //   220: athrow
    //   221: new 163	sun/rmi/transport/tcp/TCPConnection
    //   224: dup
    //   225: aload_0
    //   226: getfield 269	sun/rmi/transport/tcp/ConnectionMultiplexer:channel	Lsun/rmi/transport/tcp/TCPChannel;
    //   229: aload 5
    //   231: getfield 272	sun/rmi/transport/tcp/MultiplexConnectionInfo:in	Lsun/rmi/transport/tcp/MultiplexInputStream;
    //   234: aload 5
    //   236: getfield 273	sun/rmi/transport/tcp/MultiplexConnectionInfo:out	Lsun/rmi/transport/tcp/MultiplexOutputStream;
    //   239: invokespecial 318	sun/rmi/transport/tcp/TCPConnection:<init>	(Lsun/rmi/transport/tcp/TCPChannel;Ljava/io/InputStream;Ljava/io/OutputStream;)V
    //   242: astore 6
    //   244: aload_0
    //   245: getfield 269	sun/rmi/transport/tcp/ConnectionMultiplexer:channel	Lsun/rmi/transport/tcp/TCPChannel;
    //   248: aload 6
    //   250: invokevirtual 317	sun/rmi/transport/tcp/TCPChannel:acceptMultiplexConnection	(Lsun/rmi/transport/Connection;)V
    //   253: goto +597 -> 850
    //   256: aload_0
    //   257: getfield 263	sun/rmi/transport/tcp/ConnectionMultiplexer:dataIn	Ljava/io/DataInputStream;
    //   260: invokevirtual 276	java/io/DataInputStream:readUnsignedShort	()I
    //   263: istore_2
    //   264: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   267: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   270: invokevirtual 303	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   273: ifeq +31 -> 304
    //   276: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   279: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   282: new 151	java/lang/StringBuilder
    //   285: dup
    //   286: invokespecial 291	java/lang/StringBuilder:<init>	()V
    //   289: ldc 14
    //   291: invokevirtual 294	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   294: iload_2
    //   295: invokevirtual 293	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   298: invokevirtual 292	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   301: invokevirtual 304	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   304: new 148	java/lang/Integer
    //   307: dup
    //   308: iload_2
    //   309: invokespecial 287	java/lang/Integer:<init>	(I)V
    //   312: astore 4
    //   314: aload_0
    //   315: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   318: aload 4
    //   320: invokevirtual 300	java/util/Hashtable:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   323: checkcast 159	sun/rmi/transport/tcp/MultiplexConnectionInfo
    //   326: astore 5
    //   328: aload 5
    //   330: ifnonnull +13 -> 343
    //   333: new 145	IOException
    //   336: dup
    //   337: ldc 3
    //   339: invokespecial 284	IOException:<init>	(Ljava/lang/String;)V
    //   342: athrow
    //   343: aload 5
    //   345: getfield 272	sun/rmi/transport/tcp/MultiplexConnectionInfo:in	Lsun/rmi/transport/tcp/MultiplexInputStream;
    //   348: invokevirtual 311	sun/rmi/transport/tcp/MultiplexInputStream:disconnect	()V
    //   351: aload 5
    //   353: getfield 273	sun/rmi/transport/tcp/MultiplexConnectionInfo:out	Lsun/rmi/transport/tcp/MultiplexOutputStream;
    //   356: invokevirtual 314	sun/rmi/transport/tcp/MultiplexOutputStream:disconnect	()V
    //   359: aload 5
    //   361: getfield 271	sun/rmi/transport/tcp/MultiplexConnectionInfo:closed	Z
    //   364: ifne +9 -> 373
    //   367: aload_0
    //   368: aload 5
    //   370: invokevirtual 309	sun/rmi/transport/tcp/ConnectionMultiplexer:sendCloseAck	(Lsun/rmi/transport/tcp/MultiplexConnectionInfo;)V
    //   373: aload_0
    //   374: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   377: dup
    //   378: astore 7
    //   380: monitorenter
    //   381: aload_0
    //   382: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   385: aload 4
    //   387: invokevirtual 301	java/util/Hashtable:remove	(Ljava/lang/Object;)Ljava/lang/Object;
    //   390: pop
    //   391: aload_0
    //   392: dup
    //   393: getfield 260	sun/rmi/transport/tcp/ConnectionMultiplexer:numConnections	I
    //   396: iconst_1
    //   397: isub
    //   398: putfield 260	sun/rmi/transport/tcp/ConnectionMultiplexer:numConnections	I
    //   401: aload 7
    //   403: monitorexit
    //   404: goto +11 -> 415
    //   407: astore 8
    //   409: aload 7
    //   411: monitorexit
    //   412: aload 8
    //   414: athrow
    //   415: goto +435 -> 850
    //   418: aload_0
    //   419: getfield 263	sun/rmi/transport/tcp/ConnectionMultiplexer:dataIn	Ljava/io/DataInputStream;
    //   422: invokevirtual 276	java/io/DataInputStream:readUnsignedShort	()I
    //   425: istore_2
    //   426: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   429: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   432: invokevirtual 303	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   435: ifeq +31 -> 466
    //   438: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   441: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   444: new 151	java/lang/StringBuilder
    //   447: dup
    //   448: invokespecial 291	java/lang/StringBuilder:<init>	()V
    //   451: ldc 15
    //   453: invokevirtual 294	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   456: iload_2
    //   457: invokevirtual 293	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   460: invokevirtual 292	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   463: invokevirtual 304	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   466: new 148	java/lang/Integer
    //   469: dup
    //   470: iload_2
    //   471: invokespecial 287	java/lang/Integer:<init>	(I)V
    //   474: astore 4
    //   476: aload_0
    //   477: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   480: aload 4
    //   482: invokevirtual 300	java/util/Hashtable:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   485: checkcast 159	sun/rmi/transport/tcp/MultiplexConnectionInfo
    //   488: astore 5
    //   490: aload 5
    //   492: ifnonnull +13 -> 505
    //   495: new 145	IOException
    //   498: dup
    //   499: ldc 5
    //   501: invokespecial 284	IOException:<init>	(Ljava/lang/String;)V
    //   504: athrow
    //   505: aload 5
    //   507: getfield 271	sun/rmi/transport/tcp/MultiplexConnectionInfo:closed	Z
    //   510: ifne +13 -> 523
    //   513: new 145	IOException
    //   516: dup
    //   517: ldc 4
    //   519: invokespecial 284	IOException:<init>	(Ljava/lang/String;)V
    //   522: athrow
    //   523: aload 5
    //   525: getfield 272	sun/rmi/transport/tcp/MultiplexConnectionInfo:in	Lsun/rmi/transport/tcp/MultiplexInputStream;
    //   528: invokevirtual 311	sun/rmi/transport/tcp/MultiplexInputStream:disconnect	()V
    //   531: aload 5
    //   533: getfield 273	sun/rmi/transport/tcp/MultiplexConnectionInfo:out	Lsun/rmi/transport/tcp/MultiplexOutputStream;
    //   536: invokevirtual 314	sun/rmi/transport/tcp/MultiplexOutputStream:disconnect	()V
    //   539: aload_0
    //   540: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   543: dup
    //   544: astore 7
    //   546: monitorenter
    //   547: aload_0
    //   548: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   551: aload 4
    //   553: invokevirtual 301	java/util/Hashtable:remove	(Ljava/lang/Object;)Ljava/lang/Object;
    //   556: pop
    //   557: aload_0
    //   558: dup
    //   559: getfield 260	sun/rmi/transport/tcp/ConnectionMultiplexer:numConnections	I
    //   562: iconst_1
    //   563: isub
    //   564: putfield 260	sun/rmi/transport/tcp/ConnectionMultiplexer:numConnections	I
    //   567: aload 7
    //   569: monitorexit
    //   570: goto +11 -> 581
    //   573: astore 9
    //   575: aload 7
    //   577: monitorexit
    //   578: aload 9
    //   580: athrow
    //   581: goto +269 -> 850
    //   584: aload_0
    //   585: getfield 263	sun/rmi/transport/tcp/ConnectionMultiplexer:dataIn	Ljava/io/DataInputStream;
    //   588: invokevirtual 276	java/io/DataInputStream:readUnsignedShort	()I
    //   591: istore_2
    //   592: new 148	java/lang/Integer
    //   595: dup
    //   596: iload_2
    //   597: invokespecial 287	java/lang/Integer:<init>	(I)V
    //   600: astore 4
    //   602: aload_0
    //   603: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   606: aload 4
    //   608: invokevirtual 300	java/util/Hashtable:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   611: checkcast 159	sun/rmi/transport/tcp/MultiplexConnectionInfo
    //   614: astore 5
    //   616: aload 5
    //   618: ifnonnull +13 -> 631
    //   621: new 145	IOException
    //   624: dup
    //   625: ldc 10
    //   627: invokespecial 284	IOException:<init>	(Ljava/lang/String;)V
    //   630: athrow
    //   631: aload_0
    //   632: getfield 263	sun/rmi/transport/tcp/ConnectionMultiplexer:dataIn	Ljava/io/DataInputStream;
    //   635: invokevirtual 274	java/io/DataInputStream:readInt	()I
    //   638: istore_3
    //   639: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   642: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   645: invokevirtual 303	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   648: ifeq +40 -> 688
    //   651: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   654: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   657: new 151	java/lang/StringBuilder
    //   660: dup
    //   661: invokespecial 291	java/lang/StringBuilder:<init>	()V
    //   664: ldc 17
    //   666: invokevirtual 294	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   669: iload_2
    //   670: invokevirtual 293	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   673: ldc 2
    //   675: invokevirtual 294	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   678: iload_3
    //   679: invokevirtual 293	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   682: invokevirtual 292	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   685: invokevirtual 304	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   688: aload 5
    //   690: getfield 273	sun/rmi/transport/tcp/MultiplexConnectionInfo:out	Lsun/rmi/transport/tcp/MultiplexOutputStream;
    //   693: iload_3
    //   694: invokevirtual 315	sun/rmi/transport/tcp/MultiplexOutputStream:request	(I)V
    //   697: goto +153 -> 850
    //   700: aload_0
    //   701: getfield 263	sun/rmi/transport/tcp/ConnectionMultiplexer:dataIn	Ljava/io/DataInputStream;
    //   704: invokevirtual 276	java/io/DataInputStream:readUnsignedShort	()I
    //   707: istore_2
    //   708: new 148	java/lang/Integer
    //   711: dup
    //   712: iload_2
    //   713: invokespecial 287	java/lang/Integer:<init>	(I)V
    //   716: astore 4
    //   718: aload_0
    //   719: getfield 267	sun/rmi/transport/tcp/ConnectionMultiplexer:connectionTable	Ljava/util/Hashtable;
    //   722: aload 4
    //   724: invokevirtual 300	java/util/Hashtable:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   727: checkcast 159	sun/rmi/transport/tcp/MultiplexConnectionInfo
    //   730: astore 5
    //   732: aload 5
    //   734: ifnonnull +13 -> 747
    //   737: new 145	IOException
    //   740: dup
    //   741: ldc 11
    //   743: invokespecial 284	IOException:<init>	(Ljava/lang/String;)V
    //   746: athrow
    //   747: aload_0
    //   748: getfield 263	sun/rmi/transport/tcp/ConnectionMultiplexer:dataIn	Ljava/io/DataInputStream;
    //   751: invokevirtual 274	java/io/DataInputStream:readInt	()I
    //   754: istore_3
    //   755: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   758: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   761: invokevirtual 303	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   764: ifeq +40 -> 804
    //   767: getstatic 268	sun/rmi/transport/tcp/ConnectionMultiplexer:multiplexLog	Lsun/rmi/runtime/Log;
    //   770: getstatic 257	sun/rmi/runtime/Log:VERBOSE	Ljava/util/logging/Level;
    //   773: new 151	java/lang/StringBuilder
    //   776: dup
    //   777: invokespecial 291	java/lang/StringBuilder:<init>	()V
    //   780: ldc 18
    //   782: invokevirtual 294	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   785: iload_2
    //   786: invokevirtual 293	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   789: ldc 2
    //   791: invokevirtual 294	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   794: iload_3
    //   795: invokevirtual 293	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   798: invokevirtual 292	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   801: invokevirtual 304	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   804: aload 5
    //   806: getfield 272	sun/rmi/transport/tcp/MultiplexConnectionInfo:in	Lsun/rmi/transport/tcp/MultiplexInputStream;
    //   809: iload_3
    //   810: aload_0
    //   811: getfield 263	sun/rmi/transport/tcp/ConnectionMultiplexer:dataIn	Ljava/io/DataInputStream;
    //   814: invokevirtual 312	sun/rmi/transport/tcp/MultiplexInputStream:receive	(ILjava/io/DataInputStream;)V
    //   817: goto +33 -> 850
    //   820: new 145	IOException
    //   823: dup
    //   824: new 151	java/lang/StringBuilder
    //   827: dup
    //   828: invokespecial 291	java/lang/StringBuilder:<init>	()V
    //   831: ldc 7
    //   833: invokevirtual 294	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   836: iload_1
    //   837: invokestatic 288	java/lang/Integer:toHexString	(I)Ljava/lang/String;
    //   840: invokevirtual 294	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   843: invokevirtual 292	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   846: invokespecial 284	IOException:<init>	(Ljava/lang/String;)V
    //   849: athrow
    //   850: goto -850 -> 0
    //   853: astore 10
    //   855: aload_0
    //   856: invokevirtual 307	sun/rmi/transport/tcp/ConnectionMultiplexer:shutDown	()V
    //   859: aload 10
    //   861: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   185	210	213	finally
    //   213	218	213	finally
    //   381	404	407	finally
    //   407	412	407	finally
    //   547	570	573	finally
    //   573	578	573	finally
    //   0	855	853	finally
  }

  public synchronized TCPConnection openConnection()
    throws IOException
  {
    int i;
    Integer localInteger;
    do
    {
      this.lastID = (++this.lastID & 0x7FFF);
      i = this.lastID;
      if (this.orig)
        i |= 32768;
      localInteger = new Integer(i);
    }
    while (this.connectionTable.get(localInteger) != null);
    MultiplexConnectionInfo localMultiplexConnectionInfo = new MultiplexConnectionInfo(i);
    localMultiplexConnectionInfo.in = new MultiplexInputStream(this, localMultiplexConnectionInfo, 2048);
    localMultiplexConnectionInfo.out = new MultiplexOutputStream(this, localMultiplexConnectionInfo, 2048);
    synchronized (this.connectionTable)
    {
      if (!(this.alive))
        throw new IOException("Multiplexer connection dead");
      if (this.numConnections >= 256)
        throw new IOException("Cannot exceed 256 simultaneous multiplexed connections");
      this.connectionTable.put(localInteger, localMultiplexConnectionInfo);
      this.numConnections += 1;
    }
    synchronized (this.dataOut)
    {
      try
      {
        this.dataOut.writeByte(225);
        this.dataOut.writeShort(i);
        this.dataOut.flush();
      }
      catch (IOException localIOException)
      {
        multiplexLog.log(Log.BRIEF, "exception: ", localIOException);
        shutDown();
        throw localIOException;
      }
    }
    return ((TCPConnection)new TCPConnection(this.channel, localMultiplexConnectionInfo.in, localMultiplexConnectionInfo.out));
  }

  public void shutDown()
  {
    synchronized (this.connectionTable)
    {
      if (this.alive)
        break label17;
      return;
      label17: this.alive = false;
      Enumeration localEnumeration = this.connectionTable.elements();
      while (localEnumeration.hasMoreElements())
      {
        MultiplexConnectionInfo localMultiplexConnectionInfo = (MultiplexConnectionInfo)localEnumeration.nextElement();
        localMultiplexConnectionInfo.in.disconnect();
        localMultiplexConnectionInfo.out.disconnect();
      }
      this.connectionTable.clear();
      this.numConnections = 0;
    }
    try
    {
      this.in.close();
    }
    catch (IOException localIOException1)
    {
    }
    try
    {
      this.out.close();
    }
    catch (IOException localIOException2)
    {
    }
  }

  void sendRequest(MultiplexConnectionInfo paramMultiplexConnectionInfo, int paramInt)
    throws IOException
  {
    synchronized (this.dataOut)
    {
      if ((this.alive) && (!(paramMultiplexConnectionInfo.closed)))
        try
        {
          this.dataOut.writeByte(228);
          this.dataOut.writeShort(paramMultiplexConnectionInfo.id);
          this.dataOut.writeInt(paramInt);
          this.dataOut.flush();
        }
        catch (IOException localIOException)
        {
          multiplexLog.log(Log.BRIEF, "exception: ", localIOException);
          shutDown();
          throw localIOException;
        }
    }
  }

  void sendTransmit(MultiplexConnectionInfo paramMultiplexConnectionInfo, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    synchronized (this.dataOut)
    {
      if ((this.alive) && (!(paramMultiplexConnectionInfo.closed)))
        try
        {
          this.dataOut.writeByte(229);
          this.dataOut.writeShort(paramMultiplexConnectionInfo.id);
          this.dataOut.writeInt(paramInt2);
          this.dataOut.write(paramArrayOfByte, paramInt1, paramInt2);
          this.dataOut.flush();
        }
        catch (IOException localIOException)
        {
          multiplexLog.log(Log.BRIEF, "exception: ", localIOException);
          shutDown();
          throw localIOException;
        }
    }
  }

  void sendClose(MultiplexConnectionInfo paramMultiplexConnectionInfo)
    throws IOException
  {
    paramMultiplexConnectionInfo.out.disconnect();
    synchronized (this.dataOut)
    {
      if ((this.alive) && (!(paramMultiplexConnectionInfo.closed)))
        try
        {
          this.dataOut.writeByte(226);
          this.dataOut.writeShort(paramMultiplexConnectionInfo.id);
          this.dataOut.flush();
          paramMultiplexConnectionInfo.closed = true;
        }
        catch (IOException localIOException)
        {
          multiplexLog.log(Log.BRIEF, "exception: ", localIOException);
          shutDown();
          throw localIOException;
        }
    }
  }

  void sendCloseAck(MultiplexConnectionInfo paramMultiplexConnectionInfo)
    throws IOException
  {
    synchronized (this.dataOut)
    {
      if ((this.alive) && (!(paramMultiplexConnectionInfo.closed)))
        try
        {
          this.dataOut.writeByte(227);
          this.dataOut.writeShort(paramMultiplexConnectionInfo.id);
          this.dataOut.flush();
          paramMultiplexConnectionInfo.closed = true;
        }
        catch (IOException localIOException)
        {
          multiplexLog.log(Log.BRIEF, "exception: ", localIOException);
          shutDown();
          throw localIOException;
        }
    }
  }

  protected void finalize()
    throws Throwable
  {
    super.finalize();
    shutDown();
  }
}