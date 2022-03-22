package sun.rmi.registry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.UnicastServerRef2;
import sun.rmi.transport.LiveRef;

public class RegistryImpl extends RemoteServer
  implements Registry
{
  private static final long serialVersionUID = 4666870661827494597L;
  private Hashtable bindings = new Hashtable(101);
  private static Hashtable allowedAccessCache = new Hashtable(3);
  private static RegistryImpl registry;
  private static ObjID id = new ObjID(0);
  private static ResourceBundle resources = null;

  public RegistryImpl(int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory)
    throws RemoteException
  {
    LiveRef localLiveRef = new LiveRef(id, paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
    setup(new UnicastServerRef2(localLiveRef));
  }

  public RegistryImpl(int paramInt)
    throws RemoteException
  {
    LiveRef localLiveRef = new LiveRef(id, paramInt);
    setup(new UnicastServerRef(localLiveRef));
  }

  private void setup(UnicastServerRef paramUnicastServerRef)
    throws RemoteException
  {
    this.ref = paramUnicastServerRef;
    paramUnicastServerRef.exportObject(this, null, true);
  }

  public Remote lookup(String paramString)
    throws RemoteException, NotBoundException
  {
    synchronized (this.bindings)
    {
      Remote localRemote = (Remote)this.bindings.get(paramString);
      if (localRemote == null)
        throw new NotBoundException(paramString);
      return localRemote;
    }
  }

  public void bind(String paramString, Remote paramRemote)
    throws RemoteException, AlreadyBoundException, AccessException
  {
    checkAccess("Registry.bind");
    synchronized (this.bindings)
    {
      Remote localRemote = (Remote)this.bindings.get(paramString);
      if (localRemote != null)
        throw new AlreadyBoundException(paramString);
      this.bindings.put(paramString, paramRemote);
    }
  }

  public void unbind(String paramString)
    throws RemoteException, NotBoundException, AccessException
  {
    checkAccess("Registry.unbind");
    synchronized (this.bindings)
    {
      Remote localRemote = (Remote)this.bindings.get(paramString);
      if (localRemote == null)
        throw new NotBoundException(paramString);
      this.bindings.remove(paramString);
    }
  }

  public void rebind(String paramString, Remote paramRemote)
    throws RemoteException, AccessException
  {
    checkAccess("Registry.rebind");
    this.bindings.put(paramString, paramRemote);
  }

  public String[] list()
    throws RemoteException
  {
    String[] arrayOfString;
    synchronized (this.bindings)
    {
      int i = this.bindings.size();
      arrayOfString = new String[i];
      Enumeration localEnumeration = this.bindings.keys();
      while (--i >= 0)
        arrayOfString[i] = ((String)localEnumeration.nextElement());
    }
    return arrayOfString;
  }

  public static void checkAccess(String paramString)
    throws AccessException
  {
    String str;
    try
    {
      InetAddress localInetAddress1;
      str = getClientHost();
      try
      {
        localInetAddress1 = (InetAddress)AccessController.doPrivileged(new PrivilegedExceptionAction(str)
        {
          public Object run()
            throws UnknownHostException
          {
            return InetAddress.getByName(this.val$clientHostName);
          }
        });
      }
      catch (PrivilegedActionException localPrivilegedActionException1)
      {
        throw ((UnknownHostException)localPrivilegedActionException1.getException());
      }
      if (allowedAccessCache.get(localInetAddress1) == null)
      {
        if (localInetAddress1.isAnyLocalAddress())
          throw new AccessException("Registry." + paramString + " disallowed; origin unknown");
        try
        {
          InetAddress localInetAddress2 = localInetAddress1;
          AccessController.doPrivileged(new PrivilegedExceptionAction(localInetAddress2)
          {
            public Object run()
              throws IOException
            {
              new ServerSocket(0, 10, this.val$finalClientHost).close();
              RegistryImpl.access$000().put(this.val$finalClientHost, this.val$finalClientHost);
              return null;
            }
          });
        }
        catch (PrivilegedActionException localPrivilegedActionException2)
        {
          throw new AccessException("Registry." + paramString + " disallowed; origin " + localInetAddress1 + " is non-local host");
        }
      }
    }
    catch (ServerNotActiveException localServerNotActiveException)
    {
    }
    catch (UnknownHostException localUnknownHostException)
    {
      throw new AccessException("Registry." + paramString + " disallowed; origin is unknown host");
    }
  }

  public static ObjID getID()
  {
    return id;
  }

  private static String getTextResource(String paramString)
  {
    if (resources == null)
    {
      try
      {
        resources = ResourceBundle.getBundle("sun.rmi.registry.resources.rmiregistry");
      }
      catch (MissingResourceException localMissingResourceException1)
      {
      }
      if (resources == null)
        return "[missing resource file: " + paramString + "]";
    }
    String str = null;
    try
    {
      str = resources.getString(paramString);
    }
    catch (MissingResourceException localMissingResourceException2)
    {
    }
    if (str == null)
      return "[missing resource: " + paramString + "]";
    return str;
  }

  // ERROR //
  public static void main(String[] paramArrayOfString)
  {
    // Byte code:
    //   0: invokestatic 275	java/lang/System:getSecurityManager	()Ljava/lang/SecurityManager;
    //   3: ifnonnull +13 -> 16
    //   6: new 157	java/rmi/RMISecurityManager
    //   9: dup
    //   10: invokespecial 286	java/rmi/RMISecurityManager:<init>	()V
    //   13: invokestatic 276	java/lang/System:setSecurityManager	(Ljava/lang/SecurityManager;)V
    //   16: ldc 13
    //   18: invokestatic 277	java/lang/System:getProperty	(Ljava/lang/String;)Ljava/lang/String;
    //   21: astore_1
    //   22: aload_1
    //   23: ifnonnull +6 -> 29
    //   26: ldc 5
    //   28: astore_1
    //   29: aload_1
    //   30: invokestatic 300	sun/misc/URLClassPath:pathToURLs	(Ljava/lang/String;)[Ljava/net/URL;
    //   33: astore_2
    //   34: new 152	java/net/URLClassLoader
    //   37: dup
    //   38: aload_2
    //   39: invokespecial 282	java/net/URLClassLoader:<init>	([Ljava/net/URL;)V
    //   42: astore_3
    //   43: aload_3
    //   44: invokestatic 308	sun/rmi/server/LoaderHandler:registerCodebaseLoader	(Ljava/lang/ClassLoader;)V
    //   47: invokestatic 280	java/lang/Thread:currentThread	()Ljava/lang/Thread;
    //   50: aload_3
    //   51: invokevirtual 279	java/lang/Thread:setContextClassLoader	(Ljava/lang/ClassLoader;)V
    //   54: sipush 1099
    //   57: istore 4
    //   59: aload_0
    //   60: arraylength
    //   61: iconst_1
    //   62: if_icmplt +11 -> 73
    //   65: aload_0
    //   66: iconst_0
    //   67: aaload
    //   68: invokestatic 269	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   71: istore 4
    //   73: new 172	sun/rmi/registry/RegistryImpl
    //   76: dup
    //   77: iload 4
    //   79: invokespecial 301	sun/rmi/registry/RegistryImpl:<init>	(I)V
    //   82: putstatic 266	sun/rmi/registry/RegistryImpl:registry	Lsun/rmi/registry/RegistryImpl;
    //   85: ldc2_w 139
    //   88: invokestatic 278	java/lang/Thread:sleep	(J)V
    //   91: goto -6 -> 85
    //   94: astore 5
    //   96: goto -11 -> 85
    //   99: astore_1
    //   100: getstatic 260	java/lang/System:err	Ljava/io/PrintStream;
    //   103: ldc 15
    //   105: invokestatic 305	sun/rmi/registry/RegistryImpl:getTextResource	(Ljava/lang/String;)Ljava/lang/String;
    //   108: iconst_1
    //   109: anewarray 146	java/lang/Object
    //   112: dup
    //   113: iconst_0
    //   114: aload_0
    //   115: iconst_0
    //   116: aaload
    //   117: aastore
    //   118: invokestatic 291	java/text/MessageFormat:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   121: invokevirtual 267	java/io/PrintStream:println	(Ljava/lang/String;)V
    //   124: getstatic 260	java/lang/System:err	Ljava/io/PrintStream;
    //   127: ldc 16
    //   129: invokestatic 305	sun/rmi/registry/RegistryImpl:getTextResource	(Ljava/lang/String;)Ljava/lang/String;
    //   132: iconst_1
    //   133: anewarray 146	java/lang/Object
    //   136: dup
    //   137: iconst_0
    //   138: ldc 14
    //   140: aastore
    //   141: invokestatic 291	java/text/MessageFormat:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   144: invokevirtual 267	java/io/PrintStream:println	(Ljava/lang/String;)V
    //   147: goto +8 -> 155
    //   150: astore_1
    //   151: aload_1
    //   152: invokevirtual 268	java/lang/Exception:printStackTrace	()V
    //   155: iconst_1
    //   156: invokestatic 274	java/lang/System:exit	(I)V
    //   159: return
    //
    // Exception table:
    //   from	to	target	type
    //   85	91	94	java/lang/InterruptedException
    //   16	99	99	java/lang/NumberFormatException
    //   16	99	150	java/lang/Exception
  }
}