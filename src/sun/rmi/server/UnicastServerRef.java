package sun.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;
import java.rmi.server.ExportException;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.Skeleton;
import java.rmi.server.SkeletonNotFoundException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map<Ljava.lang.Long;Ljava.lang.reflect.Method;>;
import java.util.WeakHashMap;
import sun.rmi.runtime.Log;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.Target;
import sun.rmi.transport.tcp.TCPTransport;
import sun.security.action.GetBooleanAction;

public class UnicastServerRef extends UnicastRef
  implements java.rmi.server.ServerRef, Dispatcher
{
  public static final boolean logCalls = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("java.rmi.server.logCalls"))).booleanValue();
  public static final Log callLog = Log.getLog("sun.rmi.server.call", "RMI", logCalls);
  private static final long serialVersionUID = -7384275867073752268L;
  private static final boolean wantExceptionLog = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.rmi.server.exceptionTrace"))).booleanValue();
  private boolean forceStubUse;
  private static final boolean suppressStackTraces = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.rmi.server.suppressStackTraces"))).booleanValue();
  private transient Skeleton skel;
  private transient Map<Long, Method> hashToMethod_Map;
  private static final WeakClassHashMap<Map<Long, Method>> hashToMethod_Maps = new HashToMethod_Maps();
  private static final Map<Class<?>, ?> withoutSkeletons = Collections.synchronizedMap(new WeakHashMap());

  public UnicastServerRef()
  {
    this.forceStubUse = false;
    this.hashToMethod_Map = null;
  }

  public UnicastServerRef(LiveRef paramLiveRef)
  {
    super(paramLiveRef);
    this.forceStubUse = false;
    this.hashToMethod_Map = null;
  }

  public UnicastServerRef(int paramInt)
  {
    super(new LiveRef(paramInt));
    this.forceStubUse = false;
    this.hashToMethod_Map = null;
  }

  public UnicastServerRef(boolean paramBoolean)
  {
    this(0);
    this.forceStubUse = paramBoolean;
  }

  public RemoteStub exportObject(Remote paramRemote, Object paramObject)
    throws RemoteException
  {
    this.forceStubUse = true;
    return ((RemoteStub)exportObject(paramRemote, paramObject, false));
  }

  public Remote exportObject(Remote paramRemote, Object paramObject, boolean paramBoolean)
    throws RemoteException
  {
    Remote localRemote;
    Class localClass = paramRemote.getClass();
    try
    {
      localRemote = Util.createProxy(localClass, getClientRef(), this.forceStubUse);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw new ExportException("remote object implements illegal remote interface", localIllegalArgumentException);
    }
    if (localRemote instanceof RemoteStub)
      setSkeleton(paramRemote);
    Target localTarget = new Target(paramRemote, this, localRemote, this.ref.getObjID(), paramBoolean);
    this.ref.exportObject(localTarget);
    this.hashToMethod_Map = ((Map)hashToMethod_Maps.get(localClass));
    return localRemote;
  }

  public String getClientHost()
    throws ServerNotActiveException
  {
    return TCPTransport.getClientHost();
  }

  public void setSkeleton(Remote paramRemote)
    throws RemoteException
  {
    if (!(withoutSkeletons.containsKey(paramRemote.getClass())))
      try
      {
        this.skel = Util.createSkeleton(paramRemote);
      }
      catch (SkeletonNotFoundException localSkeletonNotFoundException)
      {
        withoutSkeletons.put(paramRemote.getClass(), null);
      }
  }

  // ERROR //
  public void dispatch(Remote paramRemote, RemoteCall paramRemoteCall)
    throws IOException
  {
    // Byte code:
    //   0: aload_2
    //   1: invokeinterface 443 1 0
    //   6: astore 6
    //   8: aload 6
    //   10: invokeinterface 438 1 0
    //   15: istore_3
    //   16: iload_3
    //   17: iflt +40 -> 57
    //   20: aload_0
    //   21: getfield 373	sun/rmi/server/UnicastServerRef:skel	Ljava/rmi/server/Skeleton;
    //   24: ifnull +23 -> 47
    //   27: aload_0
    //   28: aload_1
    //   29: aload_2
    //   30: iload_3
    //   31: invokevirtual 424	sun/rmi/server/UnicastServerRef:oldDispatch	(Ljava/rmi/Remote;Ljava/rmi/server/RemoteCall;I)V
    //   34: aload_2
    //   35: invokeinterface 441 1 0
    //   40: aload_2
    //   41: invokeinterface 442 1 0
    //   46: return
    //   47: new 214	java/rmi/UnmarshalException
    //   50: dup
    //   51: ldc 21
    //   53: invokespecial 401	java/rmi/UnmarshalException:<init>	(Ljava/lang/String;)V
    //   56: athrow
    //   57: aload 6
    //   59: invokeinterface 439 1 0
    //   64: lstore 4
    //   66: goto +17 -> 83
    //   69: astore 7
    //   71: new 214	java/rmi/UnmarshalException
    //   74: dup
    //   75: ldc 17
    //   77: aload 7
    //   79: invokespecial 402	java/rmi/UnmarshalException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   82: athrow
    //   83: aload 6
    //   85: checkcast 230	sun/rmi/server/MarshalInputStream
    //   88: astore 7
    //   90: aload 7
    //   92: invokevirtual 413	sun/rmi/server/MarshalInputStream:skipDefaultResolveClass	()V
    //   95: aload_0
    //   96: getfield 374	sun/rmi/server/UnicastServerRef:hashToMethod_Map	Ljava/util/Map;
    //   99: lload 4
    //   101: invokestatic 383	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   104: invokeinterface 448 2 0
    //   109: checkcast 209	java/lang/reflect/Method
    //   112: astore 8
    //   114: aload 8
    //   116: ifnonnull +13 -> 129
    //   119: new 214	java/rmi/UnmarshalException
    //   122: dup
    //   123: ldc 25
    //   125: invokespecial 401	java/rmi/UnmarshalException:<init>	(Ljava/lang/String;)V
    //   128: athrow
    //   129: aload_0
    //   130: aload_1
    //   131: aload 8
    //   133: invokespecial 423	sun/rmi/server/UnicastServerRef:logCall	(Ljava/rmi/Remote;Ljava/lang/Object;)V
    //   136: aload 8
    //   138: invokevirtual 396	java/lang/reflect/Method:getParameterTypes	()[Ljava/lang/Class;
    //   141: astore 9
    //   143: aload 9
    //   145: arraylength
    //   146: anewarray 201	java/lang/Object
    //   149: astore 10
    //   151: aload_0
    //   152: aload 6
    //   154: invokevirtual 417	sun/rmi/server/UnicastServerRef:unmarshalCustomCallData	(Ljava/io/ObjectInput;)V
    //   157: iconst_0
    //   158: istore 11
    //   160: iload 11
    //   162: aload 9
    //   164: arraylength
    //   165: if_icmpge +24 -> 189
    //   168: aload 10
    //   170: iload 11
    //   172: aload 9
    //   174: iload 11
    //   176: aaload
    //   177: aload 6
    //   179: invokestatic 426	sun/rmi/server/UnicastServerRef:unmarshalValue	(Ljava/lang/Class;Ljava/io/ObjectInput;)Ljava/lang/Object;
    //   182: aastore
    //   183: iinc 11 1
    //   186: goto -26 -> 160
    //   189: aload_2
    //   190: invokeinterface 441 1 0
    //   195: goto +42 -> 237
    //   198: astore 11
    //   200: new 214	java/rmi/UnmarshalException
    //   203: dup
    //   204: ldc 16
    //   206: aload 11
    //   208: invokespecial 402	java/rmi/UnmarshalException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   211: athrow
    //   212: astore 11
    //   214: new 214	java/rmi/UnmarshalException
    //   217: dup
    //   218: ldc 16
    //   220: aload 11
    //   222: invokespecial 402	java/rmi/UnmarshalException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   225: athrow
    //   226: astore 12
    //   228: aload_2
    //   229: invokeinterface 441 1 0
    //   234: aload 12
    //   236: athrow
    //   237: aload 8
    //   239: aload_1
    //   240: aload 10
    //   242: invokevirtual 397	java/lang/reflect/Method:invoke	(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    //   245: astore 11
    //   247: goto +11 -> 258
    //   250: astore 12
    //   252: aload 12
    //   254: invokevirtual 394	java/lang/reflect/InvocationTargetException:getTargetException	()Ljava/lang/Throwable;
    //   257: athrow
    //   258: aload_2
    //   259: iconst_1
    //   260: invokeinterface 444 2 0
    //   265: astore 12
    //   267: aload 8
    //   269: invokevirtual 395	java/lang/reflect/Method:getReturnType	()Ljava/lang/Class;
    //   272: astore 13
    //   274: aload 13
    //   276: getstatic 366	java/lang/Void:TYPE	Ljava/lang/Class;
    //   279: if_acmpeq +12 -> 291
    //   282: aload 13
    //   284: aload 11
    //   286: aload 12
    //   288: invokestatic 425	sun/rmi/server/UnicastServerRef:marshalValue	(Ljava/lang/Class;Ljava/lang/Object;Ljava/io/ObjectOutput;)V
    //   291: goto +17 -> 308
    //   294: astore 12
    //   296: new 210	java/rmi/MarshalException
    //   299: dup
    //   300: ldc 15
    //   302: aload 12
    //   304: invokespecial 398	java/rmi/MarshalException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   307: athrow
    //   308: aload_2
    //   309: invokeinterface 441 1 0
    //   314: aload_2
    //   315: invokeinterface 442 1 0
    //   320: goto +123 -> 443
    //   323: astore 6
    //   325: aload_0
    //   326: aload 6
    //   328: invokespecial 420	sun/rmi/server/UnicastServerRef:logCallException	(Ljava/lang/Throwable;)V
    //   331: aload_2
    //   332: iconst_0
    //   333: invokeinterface 444 2 0
    //   338: astore 7
    //   340: aload 6
    //   342: instanceof 197
    //   345: ifeq +22 -> 367
    //   348: new 212	java/rmi/ServerError
    //   351: dup
    //   352: ldc 7
    //   354: aload 6
    //   356: checkcast 197	java/lang/Error
    //   359: invokespecial 399	java/rmi/ServerError:<init>	(Ljava/lang/String;Ljava/lang/Error;)V
    //   362: astore 6
    //   364: goto +27 -> 391
    //   367: aload 6
    //   369: instanceof 211
    //   372: ifeq +19 -> 391
    //   375: new 213	java/rmi/ServerException
    //   378: dup
    //   379: ldc 10
    //   381: aload 6
    //   383: checkcast 198	java/lang/Exception
    //   386: invokespecial 400	java/rmi/ServerException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   389: astore 6
    //   391: getstatic 371	sun/rmi/server/UnicastServerRef:suppressStackTraces	Z
    //   394: ifeq +8 -> 402
    //   397: aload 6
    //   399: invokestatic 419	sun/rmi/server/UnicastServerRef:clearStackTraces	(Ljava/lang/Throwable;)V
    //   402: aload 7
    //   404: aload 6
    //   406: invokeinterface 440 2 0
    //   411: aload_2
    //   412: invokeinterface 441 1 0
    //   417: aload_2
    //   418: invokeinterface 442 1 0
    //   423: goto +20 -> 443
    //   426: astore 14
    //   428: aload_2
    //   429: invokeinterface 441 1 0
    //   434: aload_2
    //   435: invokeinterface 442 1 0
    //   440: aload 14
    //   442: athrow
    //   443: return
    //
    // Exception table:
    //   from	to	target	type
    //   0	34	69	java/lang/Exception
    //   47	66	69	java/lang/Exception
    //   151	189	198	IOException
    //   151	189	212	java/lang/ClassNotFoundException
    //   151	189	226	finally
    //   198	228	226	finally
    //   237	247	250	java/lang/reflect/InvocationTargetException
    //   258	291	294	IOException
    //   0	34	323	java/lang/Throwable
    //   47	308	323	java/lang/Throwable
    //   0	34	426	finally
    //   47	308	426	finally
    //   323	411	426	finally
    //   426	428	426	finally
  }

  protected void unmarshalCustomCallData(ObjectInput paramObjectInput)
    throws IOException, ClassNotFoundException
  {
  }

  public void oldDispatch(Remote paramRemote, RemoteCall paramRemoteCall, int paramInt)
    throws IOException
  {
    long l;
    ObjectInput localObjectInput;
    try
    {
      try
      {
        localObjectInput = paramRemoteCall.getInputStream();
        l = localObjectInput.readLong();
      }
      catch (Exception localException)
      {
        throw new UnmarshalException("error unmarshalling call header", localException);
      }
      logCall(paramRemote, this.skel.getOperations()[paramInt]);
      unmarshalCustomCallData(localObjectInput);
      this.skel.dispatch(paramRemote, paramRemoteCall, paramInt, l);
    }
    catch (Throwable localObject1)
    {
      Object localObject1;
      logCallException(localThrowable);
      ObjectOutput localObjectOutput = paramRemoteCall.getResultStream(false);
      if (localThrowable instanceof Error)
        localObject1 = new ServerError("Error occurred in server thread", (Error)localThrowable);
      else if (localObject1 instanceof RemoteException)
        localObject1 = new ServerException("RemoteException occurred in server thread", (Exception)localObject1);
      if (suppressStackTraces)
        clearStackTraces((Throwable)localObject1);
      localObjectOutput.writeObject(localObject1);
    }
    finally
    {
      paramRemoteCall.releaseInputStream();
      paramRemoteCall.releaseOutputStream();
    }
  }

  public static void clearStackTraces(Throwable paramThrowable)
  {
    StackTraceElement[] arrayOfStackTraceElement = new StackTraceElement[0];
    while (paramThrowable != null)
    {
      paramThrowable.setStackTrace(arrayOfStackTraceElement);
      paramThrowable = paramThrowable.getCause();
    }
  }

  private void logCall(Remote paramRemote, Object paramObject)
  {
    if (callLog.isLoggable(Log.VERBOSE))
    {
      String str;
      try
      {
        str = getClientHost();
      }
      catch (ServerNotActiveException localServerNotActiveException)
      {
        str = "(local)";
      }
      callLog.log(Log.VERBOSE, "[" + str + ": " + paramRemote.getClass().getName() + this.ref.getObjID().toString() + ": " + paramObject + "]");
    }
  }

  private void logCallException(Throwable paramThrowable)
  {
    Object localObject1;
    if (callLog.isLoggable(Log.BRIEF))
    {
      localObject1 = "";
      try
      {
        localObject1 = "[" + getClientHost() + "] ";
      }
      catch (ServerNotActiveException localServerNotActiveException)
      {
      }
      callLog.log(Log.BRIEF, ((String)localObject1) + "exception: ", paramThrowable);
    }
    if (wantExceptionLog)
    {
      localObject1 = System.err;
      synchronized (localObject1)
      {
        ((PrintStream)localObject1).println();
        ((PrintStream)localObject1).println("Exception dispatching call to " + this.ref.getObjID() + " in thread \"" + Thread.currentThread().getName() + "\" at " + new Date() + ":");
        paramThrowable.printStackTrace((PrintStream)localObject1);
      }
    }
  }

  public String getRefClass(ObjectOutput paramObjectOutput)
  {
    return "UnicastServerRef";
  }

  protected RemoteRef getClientRef()
  {
    return new UnicastRef(this.ref);
  }

  public void writeExternal(ObjectOutput paramObjectOutput)
    throws IOException
  {
  }

  public void readExternal(ObjectInput paramObjectInput)
    throws IOException, ClassNotFoundException
  {
    this.ref = null;
    this.skel = null;
  }

  private static class HashToMethod_Maps extends WeakClassHashMap<Map<Long, Method>>
  {
    protected Map<Long, Method> computeValue(Class<?> paramClass)
    {
      HashMap localHashMap = new HashMap();
      for (Object localObject = paramClass; localObject != null; localObject = ((Class)localObject).getSuperclass())
      {
        Class[] arrayOfClass = ((Class)localObject).getInterfaces();
        int i = arrayOfClass.length;
        for (int j = 0; j < i; ++j)
        {
          Class localClass = arrayOfClass[j];
          if (Remote.class.isAssignableFrom(localClass))
          {
            Method[] arrayOfMethod = localClass.getMethods();
            int k = arrayOfMethod.length;
            for (int l = 0; l < k; ++l)
            {
              Method localMethod1 = arrayOfMethod[l];
              Method localMethod2 = localMethod1;
              AccessController.doPrivileged(new PrivilegedAction(this, localMethod2)
              {
                public Void run()
                {
                  this.val$m.setAccessible(true);
                  return null;
                }
              });
              localHashMap.put(Long.valueOf(Util.computeMethodHash(localMethod2)), localMethod2);
            }
          }
        }
      }
      return ((Map<Long, Method>)localHashMap);
    }
  }
}