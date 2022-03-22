package sun.rmi.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.StubNotFoundException;
import java.rmi.server.LogStream;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;
import java.rmi.server.Skeleton;
import java.rmi.server.SkeletonNotFoundException;
import java.security.AccessController;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import sun.rmi.runtime.Log;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;

public final class Util
{
  static final int logLevel = LogStream.parseLevel((String)AccessController.doPrivileged(new GetPropertyAction("sun.rmi.server.logLevel")));
  public static final Log serverRefLog = Log.getLog("sun.rmi.server.ref", "transport", logLevel);
  private static final boolean ignoreStubClasses = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("java.rmi.server.ignoreStubClasses"))).booleanValue();
  private static final Map withoutStubs = Collections.synchronizedMap(new WeakHashMap(11));
  private static final Class[] stubConsParamTypes = { RemoteRef.class };

  public static Remote createProxy(Class paramClass, RemoteRef paramRemoteRef, boolean paramBoolean)
    throws StubNotFoundException
  {
    Class localClass;
    try
    {
      localClass = getRemoteClass(paramClass);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new StubNotFoundException("object does not implement a remote interface: " + paramClass.getName());
    }
    if ((paramBoolean) || ((!(ignoreStubClasses)) && (stubClassExists(localClass))))
      return createStub(localClass, paramRemoteRef);
    ClassLoader localClassLoader = paramClass.getClassLoader();
    Class[] arrayOfClass = getRemoteInterfaces(paramClass);
    RemoteObjectInvocationHandler localRemoteObjectInvocationHandler = new RemoteObjectInvocationHandler(paramRemoteRef);
    try
    {
      return ((Remote)Proxy.newProxyInstance(localClassLoader, arrayOfClass, localRemoteObjectInvocationHandler));
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw new StubNotFoundException("unable to create proxy", localIllegalArgumentException);
    }
  }

  private static boolean stubClassExists(Class paramClass)
  {
    if (!(withoutStubs.containsKey(paramClass)))
      try
      {
        Class.forName(paramClass.getName() + "_Stub", false, paramClass.getClassLoader());
        return true;
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        withoutStubs.put(paramClass, null);
      }
    return false;
  }

  private static Class getRemoteClass(Class paramClass)
    throws ClassNotFoundException
  {
    while (paramClass != null)
    {
      Class[] arrayOfClass = paramClass.getInterfaces();
      for (int i = arrayOfClass.length - 1; i >= 0; --i)
        if (Remote.class.isAssignableFrom(arrayOfClass[i]))
          return paramClass;
      paramClass = paramClass.getSuperclass();
    }
    throw new ClassNotFoundException("class does not implement java.rmi.Remote");
  }

  private static Class[] getRemoteInterfaces(Class paramClass)
  {
    ArrayList localArrayList = new ArrayList();
    getRemoteInterfaces(localArrayList, paramClass);
    return ((Class[])(Class[])localArrayList.toArray(new Class[localArrayList.size()]));
  }

  private static void getRemoteInterfaces(ArrayList paramArrayList, Class paramClass)
  {
    Class localClass1 = paramClass.getSuperclass();
    if (localClass1 != null)
      getRemoteInterfaces(paramArrayList, localClass1);
    Class[] arrayOfClass = paramClass.getInterfaces();
    for (int i = 0; i < arrayOfClass.length; ++i)
    {
      Class localClass2 = arrayOfClass[i];
      if ((Remote.class.isAssignableFrom(localClass2)) && (!(paramArrayList.contains(localClass2))))
      {
        Method[] arrayOfMethod = localClass2.getMethods();
        for (int j = 0; j < arrayOfMethod.length; ++j)
          checkMethod(arrayOfMethod[j]);
        paramArrayList.add(localClass2);
      }
    }
  }

  private static void checkMethod(Method paramMethod)
  {
    Class[] arrayOfClass = paramMethod.getExceptionTypes();
    for (int i = 0; i < arrayOfClass.length; ++i)
      if (arrayOfClass[i].isAssignableFrom(RemoteException.class))
        return;
    throw new IllegalArgumentException("illegal remote method encountered: " + paramMethod);
  }

  private static RemoteStub createStub(Class paramClass, RemoteRef paramRemoteRef)
    throws StubNotFoundException
  {
    String str = paramClass.getName() + "_Stub";
    try
    {
      Class localClass = Class.forName(str, false, paramClass.getClassLoader());
      Constructor localConstructor = localClass.getConstructor(stubConsParamTypes);
      return ((RemoteStub)localConstructor.newInstance(new Object[] { paramRemoteRef }));
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new StubNotFoundException("Stub class not found: " + str, localClassNotFoundException);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new StubNotFoundException("Stub class missing constructor: " + str, localNoSuchMethodException);
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new StubNotFoundException("Can't create instance of stub class: " + str, localInstantiationException);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new StubNotFoundException("Stub class constructor not public: " + str, localIllegalAccessException);
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      throw new StubNotFoundException("Exception creating instance of stub class: " + str, localInvocationTargetException);
    }
    catch (ClassCastException localClassCastException)
    {
      throw new StubNotFoundException("Stub class not instance of RemoteStub: " + str, localClassCastException);
    }
  }

  static Skeleton createSkeleton(Remote paramRemote)
    throws SkeletonNotFoundException
  {
    Class localClass1;
    try
    {
      localClass1 = getRemoteClass(paramRemote.getClass());
    }
    catch (ClassNotFoundException localClassNotFoundException1)
    {
      throw new SkeletonNotFoundException("object does not implement a remote interface: " + paramRemote.getClass().getName());
    }
    String str = localClass1.getName() + "_Skel";
    try
    {
      Class localClass2 = Class.forName(str, false, localClass1.getClassLoader());
      return ((Skeleton)localClass2.newInstance());
    }
    catch (ClassNotFoundException localClassNotFoundException2)
    {
      throw new SkeletonNotFoundException("Skeleton class not found: " + str, localClassNotFoundException2);
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new SkeletonNotFoundException("Can't create skeleton: " + str, localInstantiationException);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new SkeletonNotFoundException("No public constructor: " + str, localIllegalAccessException);
    }
    catch (ClassCastException localClassCastException)
    {
      throw new SkeletonNotFoundException("Skeleton not of correct class: " + str, localClassCastException);
    }
  }

  public static long computeMethodHash(Method paramMethod)
  {
    long l = 3412047291253522432L;
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(127);
    try
    {
      MessageDigest localMessageDigest = MessageDigest.getInstance("SHA");
      DataOutputStream localDataOutputStream = new DataOutputStream(new DigestOutputStream(localByteArrayOutputStream, localMessageDigest));
      String str = getMethodNameAndDescriptor(paramMethod);
      if (serverRefLog.isLoggable(Log.VERBOSE))
        serverRefLog.log(Log.VERBOSE, "string used for method hash: \"" + str + "\"");
      localDataOutputStream.writeUTF(str);
      localDataOutputStream.flush();
      byte[] arrayOfByte = localMessageDigest.digest();
      for (int i = 0; i < Math.min(8, arrayOfByte.length); ++i)
        l += ((arrayOfByte[i] & 0xFF) << i * 8);
    }
    catch (IOException localIOException)
    {
      l = -1L;
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new SecurityException(localNoSuchAlgorithmException.getMessage());
    }
    return l;
  }

  private static String getMethodNameAndDescriptor(Method paramMethod)
  {
    StringBuffer localStringBuffer = new StringBuffer(paramMethod.getName());
    localStringBuffer.append('(');
    Class[] arrayOfClass = paramMethod.getParameterTypes();
    for (int i = 0; i < arrayOfClass.length; ++i)
      localStringBuffer.append(getTypeDescriptor(arrayOfClass[i]));
    localStringBuffer.append(')');
    Class localClass = paramMethod.getReturnType();
    if (localClass == Void.TYPE)
      localStringBuffer.append('V');
    else
      localStringBuffer.append(getTypeDescriptor(localClass));
    return localStringBuffer.toString();
  }

  private static String getTypeDescriptor(Class paramClass)
  {
    if (paramClass.isPrimitive())
    {
      if (paramClass == Integer.TYPE)
        return "I";
      if (paramClass == Boolean.TYPE)
        return "Z";
      if (paramClass == Byte.TYPE)
        return "B";
      if (paramClass == Character.TYPE)
        return "C";
      if (paramClass == Short.TYPE)
        return "S";
      if (paramClass == Long.TYPE)
        return "J";
      if (paramClass == Float.TYPE)
        return "F";
      if (paramClass == Double.TYPE)
        return "D";
      if (paramClass == Void.TYPE)
        return "V";
      throw new Error("unrecognized primitive type: " + paramClass);
    }
    if (paramClass.isArray())
      return paramClass.getName().replace('.', '/');
    return "L" + paramClass.getName().replace('.', '/') + ";";
  }

  public static String getUnqualifiedName(Class paramClass)
  {
    String str = paramClass.getName();
    return str.substring(str.lastIndexOf(46) + 1);
  }
}