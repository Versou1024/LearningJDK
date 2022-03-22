package sun.swing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class AccessibleMethod
{
  private final Method method;

  public AccessibleMethod(Class paramClass, String paramString, Class[] paramArrayOfClass)
    throws NoSuchMethodException
  {
    try
    {
      this.method = ((Method)AccessController.doPrivileged(new AccessMethodAction(paramClass, paramString, paramArrayOfClass)));
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((NoSuchMethodException)localPrivilegedActionException.getCause());
    }
  }

  public Object invoke(Object paramObject, Object[] paramArrayOfObject)
    throws IllegalArgumentException, InvocationTargetException
  {
    try
    {
      return this.method.invoke(paramObject, paramArrayOfObject);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new AssertionError("accessible method inaccessible");
    }
  }

  public Object invokeNoChecked(Object paramObject, Object[] paramArrayOfObject)
  {
    try
    {
      return invoke(paramObject, paramArrayOfObject);
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      if (localInvocationTargetException.getCause() instanceof RuntimeException)
        throw ((RuntimeException)localInvocationTargetException.getCause());
      throw new RuntimeException(localInvocationTargetException.getCause());
    }
  }

  private static class AccessMethodAction
  implements PrivilegedExceptionAction<Method>
  {
    private final Class klass;
    private final String methodName;
    private final Class[] paramTypes;

    public AccessMethodAction(Class paramClass, String paramString, Class[] paramArrayOfClass)
    {
      this.klass = paramClass;
      this.methodName = paramString;
      this.paramTypes = paramArrayOfClass;
    }

    public Method run()
      throws NoSuchMethodException
    {
      Method localMethod = this.klass.getDeclaredMethod(this.methodName, this.paramTypes);
      localMethod.setAccessible(true);
      return localMethod;
    }
  }
}