package sun.org.mozilla.javascript.internal.jdk13;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.ContextFactory;
import sun.org.mozilla.javascript.internal.InterfaceAdapter;
import sun.org.mozilla.javascript.internal.Kit;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.VMBridge;

public class VMBridge_jdk13 extends VMBridge
{
  private ThreadLocal contextLocal = new ThreadLocal();

  protected Object getThreadContextHelper()
  {
    Object[] arrayOfObject = (Object[])(Object[])this.contextLocal.get();
    if (arrayOfObject == null)
    {
      arrayOfObject = new Object[1];
      this.contextLocal.set(arrayOfObject);
    }
    return arrayOfObject;
  }

  protected Context getContext(Object paramObject)
  {
    Object[] arrayOfObject = (Object[])(Object[])paramObject;
    return ((Context)arrayOfObject[0]);
  }

  protected void setContext(Object paramObject, Context paramContext)
  {
    Object[] arrayOfObject = (Object[])(Object[])paramObject;
    arrayOfObject[0] = paramContext;
  }

  protected ClassLoader getCurrentThreadClassLoader()
  {
    return Thread.currentThread().getContextClassLoader();
  }

  protected boolean tryToMakeAccessible(Object paramObject)
  {
    if (!(paramObject instanceof AccessibleObject))
      return false;
    AccessibleObject localAccessibleObject = (AccessibleObject)paramObject;
    if (localAccessibleObject.isAccessible())
      return true;
    try
    {
      localAccessibleObject.setAccessible(true);
    }
    catch (Exception localException)
    {
    }
    return localAccessibleObject.isAccessible();
  }

  protected Object getInterfaceProxyHelper(ContextFactory paramContextFactory, Class[] paramArrayOfClass)
  {
    Constructor localConstructor;
    ClassLoader localClassLoader = paramArrayOfClass[0].getClassLoader();
    Class localClass = Proxy.getProxyClass(localClassLoader, paramArrayOfClass);
    try
    {
      localConstructor = localClass.getConstructor(new Class[] { InvocationHandler.class });
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw Kit.initCause(new IllegalStateException(), localNoSuchMethodException);
    }
    return localConstructor;
  }

  protected Object newInterfaceProxy(Object paramObject1, ContextFactory paramContextFactory, InterfaceAdapter paramInterfaceAdapter, Object paramObject2, Scriptable paramScriptable)
  {
    Object localObject;
    Constructor localConstructor = (Constructor)paramObject1;
    1 local1 = new InvocationHandler(this, paramInterfaceAdapter, paramContextFactory, paramObject2, paramScriptable)
    {
      public Object invoke(, Method paramMethod, Object[] paramArrayOfObject)
      {
        return this.val$adapter.invoke(this.val$cf, this.val$target, this.val$topScope, paramMethod, paramArrayOfObject);
      }
    };
    try
    {
      localObject = localConstructor.newInstance(new Object[] { local1 });
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      throw Context.throwAsScriptRuntimeEx(localInvocationTargetException);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw Kit.initCause(new IllegalStateException(), localIllegalAccessException);
    }
    catch (InstantiationException localInstantiationException)
    {
      throw Kit.initCause(new IllegalStateException(), localInstantiationException);
    }
    return localObject;
  }
}