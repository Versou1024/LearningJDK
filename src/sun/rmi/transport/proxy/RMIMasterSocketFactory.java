package sun.rmi.transport.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.LogStream;
import java.rmi.server.RMISocketFactory;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Hashtable;
import java.util.Vector;
import sun.rmi.runtime.Log;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetLongAction;
import sun.security.action.GetPropertyAction;

public class RMIMasterSocketFactory extends RMISocketFactory
{
  static int logLevel = LogStream.parseLevel(getLogLevel());
  static final Log proxyLog = Log.getLog("sun.rmi.transport.tcp.proxy", "transport", logLevel);
  private static long connectTimeout = getConnectTimeout();
  private static final boolean eagerHttpFallback = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.rmi.transport.proxy.eagerHttpFallback"))).booleanValue();
  private Hashtable successTable = new Hashtable();
  private static final int MaxRememberedHosts = 64;
  private Vector hostList = new Vector(64);
  protected RMISocketFactory initialFactory = new RMIDirectSocketFactory();
  protected Vector altFactoryList = new Vector(2);

  private static String getLogLevel()
  {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("sun.rmi.transport.proxy.logLevel")));
  }

  private static long getConnectTimeout()
  {
    return ((Long)AccessController.doPrivileged(new GetLongAction("sun.rmi.transport.proxy.connectTimeout", 15000L))).longValue();
  }

  public RMIMasterSocketFactory()
  {
    int i = 0;
    try
    {
      String str = (String)AccessController.doPrivileged(new GetPropertyAction("http.proxyHost"));
      if (str == null)
        str = (String)AccessController.doPrivileged(new GetPropertyAction("proxyHost"));
      Boolean localBoolean = (Boolean)AccessController.doPrivileged(new GetBooleanAction("java.rmi.server.disableHttp"));
      if ((!(localBoolean.booleanValue())) && (str != null) && (str.length() > 0))
        i = 1;
    }
    catch (Exception localException)
    {
      i = 1;
    }
    if (i != 0)
    {
      this.altFactoryList.addElement(new RMIHttpToPortSocketFactory());
      this.altFactoryList.addElement(new RMIHttpToCGISocketFactory());
    }
  }

  // ERROR //
  public Socket createSocket(String paramString, int paramInt)
    throws IOException
  {
    // Byte code:
    //   0: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   3: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   6: invokevirtual 295	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   9: ifeq +40 -> 49
    //   12: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   15: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   18: new 150	java/lang/StringBuilder
    //   21: dup
    //   22: invokespecial 270	java/lang/StringBuilder:<init>	()V
    //   25: ldc 8
    //   27: invokevirtual 274	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   30: aload_1
    //   31: invokevirtual 274	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   34: ldc 1
    //   36: invokevirtual 274	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   39: iload_2
    //   40: invokevirtual 272	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   43: invokevirtual 271	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   46: invokevirtual 296	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   49: aload_0
    //   50: getfield 258	sun/rmi/transport/proxy/RMIMasterSocketFactory:altFactoryList	Ljava/util/Vector;
    //   53: invokevirtual 290	java/util/Vector:size	()I
    //   56: ifne +13 -> 69
    //   59: aload_0
    //   60: getfield 256	sun/rmi/transport/proxy/RMIMasterSocketFactory:initialFactory	Ljava/rmi/server/RMISocketFactory;
    //   63: aload_1
    //   64: iload_2
    //   65: invokevirtual 283	java/rmi/server/RMISocketFactory:createSocket	(Ljava/lang/String;I)Ljava/net/Socket;
    //   68: areturn
    //   69: aload_0
    //   70: getfield 257	sun/rmi/transport/proxy/RMIMasterSocketFactory:successTable	Ljava/util/Hashtable;
    //   73: aload_1
    //   74: invokevirtual 287	java/util/Hashtable:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   77: checkcast 158	java/rmi/server/RMISocketFactory
    //   80: astore_3
    //   81: aload_3
    //   82: ifnull +50 -> 132
    //   85: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   88: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   91: invokevirtual 295	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   94: ifeq +31 -> 125
    //   97: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   100: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   103: new 150	java/lang/StringBuilder
    //   106: dup
    //   107: invokespecial 270	java/lang/StringBuilder:<init>	()V
    //   110: ldc 13
    //   112: invokevirtual 274	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   115: aload_3
    //   116: invokevirtual 273	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   119: invokevirtual 271	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   122: invokevirtual 296	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   125: aload_3
    //   126: aload_1
    //   127: iload_2
    //   128: invokevirtual 283	java/rmi/server/RMISocketFactory:createSocket	(Ljava/lang/String;I)Ljava/net/Socket;
    //   131: areturn
    //   132: aconst_null
    //   133: astore 4
    //   135: aconst_null
    //   136: astore 5
    //   138: new 168	sun/rmi/transport/proxy/RMIMasterSocketFactory$AsyncConnector
    //   141: dup
    //   142: aload_0
    //   143: aload_0
    //   144: getfield 256	sun/rmi/transport/proxy/RMIMasterSocketFactory:initialFactory	Ljava/rmi/server/RMISocketFactory;
    //   147: aload_1
    //   148: iload_2
    //   149: invokestatic 284	java/security/AccessController:getContext	()Ljava/security/AccessControlContext;
    //   152: invokespecial 310	sun/rmi/transport/proxy/RMIMasterSocketFactory$AsyncConnector:<init>	(Lsun/rmi/transport/proxy/RMIMasterSocketFactory;Ljava/rmi/server/RMISocketFactory;Ljava/lang/String;ILjava/security/AccessControlContext;)V
    //   155: astore 6
    //   157: aconst_null
    //   158: astore 7
    //   160: aload 6
    //   162: dup
    //   163: astore 8
    //   165: monitorenter
    //   166: new 163	sun/rmi/runtime/NewThreadAction
    //   169: dup
    //   170: aload 6
    //   172: ldc 2
    //   174: iconst_1
    //   175: invokespecial 299	sun/rmi/runtime/NewThreadAction:<init>	(Ljava/lang/Runnable;Ljava/lang/String;Z)V
    //   178: invokestatic 285	java/security/AccessController:doPrivileged	(Ljava/security/PrivilegedAction;)Ljava/lang/Object;
    //   181: checkcast 152	java/lang/Thread
    //   184: astore 9
    //   186: aload 9
    //   188: invokevirtual 276	java/lang/Thread:start	()V
    //   191: invokestatic 275	java/lang/System:currentTimeMillis	()J
    //   194: lstore 10
    //   196: lload 10
    //   198: getstatic 254	sun/rmi/transport/proxy/RMIMasterSocketFactory:connectTimeout	J
    //   201: ladd
    //   202: lstore 12
    //   204: aload 6
    //   206: lload 12
    //   208: lload 10
    //   210: lsub
    //   211: invokevirtual 268	java/lang/Object:wait	(J)V
    //   214: aload_0
    //   215: aload 6
    //   217: invokevirtual 305	sun/rmi/transport/proxy/RMIMasterSocketFactory:checkConnector	(Lsun/rmi/transport/proxy/RMIMasterSocketFactory$AsyncConnector;)Ljava/net/Socket;
    //   220: astore 4
    //   222: aload 4
    //   224: ifnull +6 -> 230
    //   227: goto +16 -> 243
    //   230: invokestatic 275	java/lang/System:currentTimeMillis	()J
    //   233: lstore 10
    //   235: lload 10
    //   237: lload 12
    //   239: lcmp
    //   240: iflt -36 -> 204
    //   243: goto +15 -> 258
    //   246: astore 10
    //   248: new 141	java/io/InterruptedIOException
    //   251: dup
    //   252: ldc 11
    //   254: invokespecial 262	java/io/InterruptedIOException:<init>	(Ljava/lang/String;)V
    //   257: athrow
    //   258: aload 8
    //   260: monitorexit
    //   261: goto +11 -> 272
    //   264: astore 14
    //   266: aload 8
    //   268: monitorexit
    //   269: aload 14
    //   271: athrow
    //   272: aload 4
    //   274: ifnonnull +30 -> 304
    //   277: new 153	java/net/NoRouteToHostException
    //   280: dup
    //   281: new 150	java/lang/StringBuilder
    //   284: dup
    //   285: invokespecial 270	java/lang/StringBuilder:<init>	()V
    //   288: ldc 3
    //   290: invokevirtual 274	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   293: aload_1
    //   294: invokevirtual 274	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   297: invokevirtual 271	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   300: invokespecial 277	java/net/NoRouteToHostException:<init>	(Ljava/lang/String;)V
    //   303: athrow
    //   304: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   307: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   310: ldc 5
    //   312: invokevirtual 296	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   315: aload 4
    //   317: astore 8
    //   319: jsr +62 -> 381
    //   322: aload 8
    //   324: areturn
    //   325: astore 8
    //   327: aload 8
    //   329: astore 7
    //   331: jsr +50 -> 381
    //   334: goto +242 -> 576
    //   337: astore 8
    //   339: aload 8
    //   341: astore 7
    //   343: jsr +38 -> 381
    //   346: goto +230 -> 576
    //   349: astore 8
    //   351: getstatic 255	sun/rmi/transport/proxy/RMIMasterSocketFactory:eagerHttpFallback	Z
    //   354: ifeq +10 -> 364
    //   357: aload 8
    //   359: astore 7
    //   361: goto +6 -> 367
    //   364: aload 8
    //   366: athrow
    //   367: jsr +14 -> 381
    //   370: goto +206 -> 576
    //   373: astore 15
    //   375: jsr +6 -> 381
    //   378: aload 15
    //   380: athrow
    //   381: astore 16
    //   383: aload 7
    //   385: ifnull +189 -> 574
    //   388: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   391: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   394: invokevirtual 295	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   397: ifeq +16 -> 413
    //   400: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   403: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   406: ldc 4
    //   408: aload 7
    //   410: invokevirtual 297	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   413: iconst_0
    //   414: istore 17
    //   416: iload 17
    //   418: aload_0
    //   419: getfield 258	sun/rmi/transport/proxy/RMIMasterSocketFactory:altFactoryList	Ljava/util/Vector;
    //   422: invokevirtual 290	java/util/Vector:size	()I
    //   425: if_icmpge +149 -> 574
    //   428: aload_0
    //   429: getfield 258	sun/rmi/transport/proxy/RMIMasterSocketFactory:altFactoryList	Ljava/util/Vector;
    //   432: iload 17
    //   434: invokevirtual 293	java/util/Vector:elementAt	(I)Ljava/lang/Object;
    //   437: checkcast 158	java/rmi/server/RMISocketFactory
    //   440: astore_3
    //   441: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   444: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   447: invokevirtual 295	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   450: ifeq +31 -> 481
    //   453: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   456: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   459: new 150	java/lang/StringBuilder
    //   462: dup
    //   463: invokespecial 270	java/lang/StringBuilder:<init>	()V
    //   466: ldc 20
    //   468: invokevirtual 274	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   471: aload_3
    //   472: invokevirtual 273	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   475: invokevirtual 271	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   478: invokevirtual 296	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   481: aload_3
    //   482: aload_1
    //   483: iload_2
    //   484: invokevirtual 283	java/rmi/server/RMISocketFactory:createSocket	(Ljava/lang/String;I)Ljava/net/Socket;
    //   487: astore 18
    //   489: aload 18
    //   491: invokevirtual 279	java/net/Socket:getInputStream	()Ljava/io/InputStream;
    //   494: astore 19
    //   496: aload 19
    //   498: invokevirtual 261	java/io/InputStream:read	()I
    //   501: istore 20
    //   503: aload 18
    //   505: invokevirtual 278	java/net/Socket:close	()V
    //   508: goto +33 -> 541
    //   511: astore 18
    //   513: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   516: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   519: invokevirtual 295	sun/rmi/runtime/Log:isLoggable	(Ljava/util/logging/Level;)Z
    //   522: ifeq +16 -> 538
    //   525: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   528: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   531: ldc 6
    //   533: aload 18
    //   535: invokevirtual 297	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   538: goto +30 -> 568
    //   541: getstatic 260	sun/rmi/transport/proxy/RMIMasterSocketFactory:proxyLog	Lsun/rmi/runtime/Log;
    //   544: getstatic 252	sun/rmi/runtime/Log:BRIEF	Ljava/util/logging/Level;
    //   547: ldc 7
    //   549: invokevirtual 296	sun/rmi/runtime/Log:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   552: aload_3
    //   553: aload_1
    //   554: iload_2
    //   555: invokevirtual 283	java/rmi/server/RMISocketFactory:createSocket	(Ljava/lang/String;I)Ljava/net/Socket;
    //   558: astore 5
    //   560: goto +14 -> 574
    //   563: astore 18
    //   565: goto +9 -> 574
    //   568: iinc 17 1
    //   571: goto -155 -> 416
    //   574: ret 16
    //   576: aload_0
    //   577: getfield 257	sun/rmi/transport/proxy/RMIMasterSocketFactory:successTable	Ljava/util/Hashtable;
    //   580: dup
    //   581: astore 8
    //   583: monitorenter
    //   584: aload 6
    //   586: dup
    //   587: astore 9
    //   589: monitorenter
    //   590: aload_0
    //   591: aload 6
    //   593: invokevirtual 305	sun/rmi/transport/proxy/RMIMasterSocketFactory:checkConnector	(Lsun/rmi/transport/proxy/RMIMasterSocketFactory$AsyncConnector;)Ljava/net/Socket;
    //   596: astore 4
    //   598: aload 9
    //   600: monitorexit
    //   601: goto +11 -> 612
    //   604: astore 21
    //   606: aload 9
    //   608: monitorexit
    //   609: aload 21
    //   611: athrow
    //   612: aload 4
    //   614: ifnull +19 -> 633
    //   617: aload 5
    //   619: ifnull +8 -> 627
    //   622: aload 5
    //   624: invokevirtual 278	java/net/Socket:close	()V
    //   627: aload 4
    //   629: aload 8
    //   631: monitorexit
    //   632: areturn
    //   633: aload 6
    //   635: invokevirtual 307	sun/rmi/transport/proxy/RMIMasterSocketFactory$AsyncConnector:notUsed	()V
    //   638: goto +39 -> 677
    //   641: astore 9
    //   643: aload 9
    //   645: astore 7
    //   647: goto +30 -> 677
    //   650: astore 9
    //   652: aload 9
    //   654: astore 7
    //   656: goto +21 -> 677
    //   659: astore 9
    //   661: getstatic 255	sun/rmi/transport/proxy/RMIMasterSocketFactory:eagerHttpFallback	Z
    //   664: ifeq +10 -> 674
    //   667: aload 9
    //   669: astore 7
    //   671: goto +6 -> 677
    //   674: aload 9
    //   676: athrow
    //   677: aload 5
    //   679: ifnull +15 -> 694
    //   682: aload_0
    //   683: aload_1
    //   684: aload_3
    //   685: invokevirtual 306	sun/rmi/transport/proxy/RMIMasterSocketFactory:rememberFactory	(Ljava/lang/String;Ljava/rmi/server/RMISocketFactory;)V
    //   688: aload 5
    //   690: aload 8
    //   692: monitorexit
    //   693: areturn
    //   694: aload 7
    //   696: athrow
    //   697: astore 22
    //   699: aload 8
    //   701: monitorexit
    //   702: aload 22
    //   704: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   191	243	246	java/lang/InterruptedException
    //   166	261	264	finally
    //   264	269	264	finally
    //   160	322	325	java/net/UnknownHostException
    //   160	322	337	java/net/NoRouteToHostException
    //   160	322	349	java/net/SocketException
    //   160	322	373	finally
    //   325	334	373	finally
    //   337	346	373	finally
    //   349	370	373	finally
    //   373	378	373	finally
    //   441	508	511	IOException
    //   552	560	563	IOException
    //   590	601	604	finally
    //   604	609	604	finally
    //   584	629	641	java/net/UnknownHostException
    //   633	638	641	java/net/UnknownHostException
    //   584	629	650	java/net/NoRouteToHostException
    //   633	638	650	java/net/NoRouteToHostException
    //   584	629	659	java/net/SocketException
    //   633	638	659	java/net/SocketException
    //   584	632	697	finally
    //   633	693	697	finally
    //   694	702	697	finally
  }

  void rememberFactory(String paramString, RMISocketFactory paramRMISocketFactory)
  {
    synchronized (this.successTable)
    {
      while (this.hostList.size() >= 64)
      {
        this.successTable.remove(this.hostList.elementAt(0));
        this.hostList.removeElementAt(0);
      }
      this.hostList.addElement(paramString);
      this.successTable.put(paramString, paramRMISocketFactory);
    }
  }

  Socket checkConnector(AsyncConnector paramAsyncConnector)
    throws IOException
  {
    Exception localException = AsyncConnector.access$000(paramAsyncConnector);
    if (localException != null)
    {
      localException.fillInStackTrace();
      if (localException instanceof IOException)
        throw ((IOException)localException);
      if (localException instanceof RuntimeException)
        throw ((RuntimeException)localException);
      throw new Error("internal error: unexpected checked exception: " + localException.toString());
    }
    return AsyncConnector.access$100(paramAsyncConnector);
  }

  public ServerSocket createServerSocket(int paramInt)
    throws IOException
  {
    return this.initialFactory.createServerSocket(paramInt);
  }

  private class AsyncConnector
  implements Runnable
  {
    private RMISocketFactory factory;
    private String host;
    private int port;
    private AccessControlContext acc;
    private Exception exception = null;
    private Socket socket = null;
    private boolean cleanUp = false;

    AsyncConnector(, RMISocketFactory paramRMISocketFactory, String paramString, int paramInt, AccessControlContext paramAccessControlContext)
    {
      this.factory = paramRMISocketFactory;
      this.host = paramString;
      this.port = paramInt;
      this.acc = paramAccessControlContext;
      SecurityManager localSecurityManager = System.getSecurityManager();
      if (localSecurityManager != null)
        localSecurityManager.checkConnect(paramString, paramInt);
    }

    public void run()
    {
      Socket localSocket;
      try
      {
        try
        {
          localSocket = this.factory.createSocket(this.host, this.port);
          synchronized (this)
          {
            this.socket = localSocket;
            super.notify();
          }
          this.this$0.rememberFactory(this.host, this.factory);
          synchronized (this)
          {
            if (this.cleanUp)
              try
              {
                this.socket.close();
              }
              catch (IOException localIOException)
              {
              }
          }
        }
        catch (Exception localException)
        {
          synchronized (this)
          {
            this.exception = localException;
            super.notify();
          }
        }
      }
    }

    private synchronized Exception getException()
    {
      return this.exception;
    }

    private synchronized Socket getSocket()
    {
      return this.socket;
    }

    synchronized void notUsed()
    {
      if (this.socket != null)
        try
        {
          this.socket.close();
        }
        catch (IOException localIOException)
        {
        }
      this.cleanUp = true;
    }
  }
}