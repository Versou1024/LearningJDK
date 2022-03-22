package sun.rmi.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.rmi.server.RMIClassLoader;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permission;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import sun.security.action.GetBooleanAction;
import sun.security.action.LoadLibraryAction;

public class MarshalInputStream extends ObjectInputStream
{
  private static final boolean useCodebaseOnlyProperty = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("java.rmi.server.useCodebaseOnly"))).booleanValue();
  protected static Map permittedSunClasses = new HashMap(3);
  private boolean skipDefaultResolveClass = false;
  private final Map doneCallbacks = new HashMap(3);
  private boolean useCodebaseOnly = useCodebaseOnlyProperty;

  public MarshalInputStream(InputStream paramInputStream)
    throws IOException, StreamCorruptedException
  {
    super(paramInputStream);
  }

  public Runnable getDoneCallback(Object paramObject)
  {
    return ((Runnable)this.doneCallbacks.get(paramObject));
  }

  public void setDoneCallback(Object paramObject, Runnable paramRunnable)
  {
    this.doneCallbacks.put(paramObject, paramRunnable);
  }

  public void done()
  {
    Iterator localIterator = this.doneCallbacks.values().iterator();
    while (localIterator.hasNext())
    {
      Runnable localRunnable = (Runnable)localIterator.next();
      localRunnable.run();
    }
    this.doneCallbacks.clear();
  }

  public void close()
    throws IOException
  {
    done();
    super.close();
  }

  protected Class resolveClass(ObjectStreamClass paramObjectStreamClass)
    throws IOException, ClassNotFoundException
  {
    Object localObject = readLocation();
    String str1 = paramObjectStreamClass.getName();
    ClassLoader localClassLoader = (this.skipDefaultResolveClass) ? null : latestUserDefinedLoader();
    String str2 = null;
    if ((!(this.useCodebaseOnly)) && (localObject instanceof String))
      str2 = (String)localObject;
    try
    {
      return RMIClassLoader.loadClass(str2, str1, localClassLoader);
    }
    catch (AccessControlException localAccessControlException)
    {
      return checkSunClass(str1, localAccessControlException);
    }
    catch (ClassNotFoundException localClassNotFoundException1)
    {
      try
      {
        if ((Character.isLowerCase(str1.charAt(0))) && (str1.indexOf(46) == -1))
          return super.resolveClass(paramObjectStreamClass);
      }
      catch (ClassNotFoundException localClassNotFoundException2)
      {
      }
      throw localClassNotFoundException1;
    }
  }

  protected Class resolveProxyClass(String[] paramArrayOfString)
    throws IOException, ClassNotFoundException
  {
    Object localObject = readLocation();
    ClassLoader localClassLoader = (this.skipDefaultResolveClass) ? null : latestUserDefinedLoader();
    String str = null;
    if ((!(this.useCodebaseOnly)) && (localObject instanceof String))
      str = (String)localObject;
    return RMIClassLoader.loadProxyClass(str, paramArrayOfString, localClassLoader);
  }

  private static native ClassLoader latestUserDefinedLoader();

  private Class checkSunClass(String paramString, AccessControlException paramAccessControlException)
    throws AccessControlException
  {
    Permission localPermission = paramAccessControlException.getPermission();
    String str = null;
    if (localPermission != null)
      str = localPermission.getName();
    Class localClass = (Class)permittedSunClasses.get(paramString);
    if ((str == null) || (localClass == null) || ((!(str.equals("accessClassInPackage.sun.rmi.server"))) && (!(str.equals("accessClassInPackage.sun.rmi.registry")))))
      throw paramAccessControlException;
    return localClass;
  }

  protected Object readLocation()
    throws IOException, ClassNotFoundException
  {
    return readObject();
  }

  void skipDefaultResolveClass()
  {
    this.skipDefaultResolveClass = true;
  }

  void useCodebaseOnly()
  {
    this.useCodebaseOnly = true;
  }

  static
  {
    try
    {
      String str1 = "sun.rmi.server.Activation$ActivationSystemImpl_Stub";
      String str2 = "sun.rmi.registry.RegistryImpl_Stub";
      permittedSunClasses.put(str1, Class.forName(str1));
      permittedSunClasses.put(str2, Class.forName(str2));
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new NoClassDefFoundError("Missing system class: " + localClassNotFoundException.getMessage());
    }
    AccessController.doPrivileged(new LoadLibraryAction("rmi"));
  }
}