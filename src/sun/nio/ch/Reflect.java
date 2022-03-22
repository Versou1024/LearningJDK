package sun.nio.ch;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

class Reflect
{
  private static void setAccessible(AccessibleObject paramAccessibleObject)
  {
    AccessController.doPrivileged(new PrivilegedAction(paramAccessibleObject)
    {
      public Object run()
      {
        this.val$ao.setAccessible(true);
        return null;
      }
    });
  }

  static Constructor lookupConstructor(String paramString, Class[] paramArrayOfClass)
  {
    Class localClass;
    try
    {
      localClass = Class.forName(paramString);
      Constructor localConstructor = localClass.getDeclaredConstructor(paramArrayOfClass);
      setAccessible(localConstructor);
      return localConstructor;
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new ReflectionError(localClassNotFoundException);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new ReflectionError(localNoSuchMethodException);
    }
  }

  static Object invoke(Constructor paramConstructor, Object[] paramArrayOfObject)
  {
    try
    {
      return paramConstructor.newInstance(paramArrayOfObject);
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new ReflectionError(localInstantiationException);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new ReflectionError(localIllegalAccessException);
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      throw new ReflectionError(localInvocationTargetException);
    }
  }

  static Method lookupMethod(String paramString1, String paramString2, Class[] paramArrayOfClass)
  {
    Class localClass;
    try
    {
      localClass = Class.forName(paramString1);
      Method localMethod = localClass.getDeclaredMethod(paramString2, paramArrayOfClass);
      setAccessible(localMethod);
      return localMethod;
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new ReflectionError(localClassNotFoundException);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new ReflectionError(localNoSuchMethodException);
    }
  }

  static Object invoke(Method paramMethod, Object paramObject, Object[] paramArrayOfObject)
  {
    try
    {
      return paramMethod.invoke(paramObject, paramArrayOfObject);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new ReflectionError(localIllegalAccessException);
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      throw new ReflectionError(localInvocationTargetException);
    }
  }

  static Object invokeIO(Method paramMethod, Object paramObject, Object[] paramArrayOfObject)
    throws IOException
  {
    try
    {
      return paramMethod.invoke(paramObject, paramArrayOfObject);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new ReflectionError(localIllegalAccessException);
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      if (IOException.class.isInstance(localInvocationTargetException.getCause()))
        throw ((IOException)localInvocationTargetException.getCause());
      throw new ReflectionError(localInvocationTargetException);
    }
  }

  static Field lookupField(String paramString1, String paramString2)
  {
    Class localClass;
    try
    {
      localClass = Class.forName(paramString1);
      Field localField = localClass.getDeclaredField(paramString2);
      setAccessible(localField);
      return localField;
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new ReflectionError(localClassNotFoundException);
    }
    catch (NoSuchFieldException localNoSuchFieldException)
    {
      throw new ReflectionError(localNoSuchFieldException);
    }
  }

  static Object get(Object paramObject, Field paramField)
  {
    try
    {
      return paramField.get(paramObject);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new ReflectionError(localIllegalAccessException);
    }
  }

  static Object get(Field paramField)
  {
    return get(null, paramField);
  }

  static void set(Object paramObject1, Field paramField, Object paramObject2)
  {
    try
    {
      paramField.set(paramObject1, paramObject2);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new ReflectionError(localIllegalAccessException);
    }
  }

  static void setInt(Object paramObject, Field paramField, int paramInt)
  {
    try
    {
      paramField.setInt(paramObject, paramInt);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new ReflectionError(localIllegalAccessException);
    }
  }

  static void setBoolean(Object paramObject, Field paramField, boolean paramBoolean)
  {
    try
    {
      paramField.setBoolean(paramObject, paramBoolean);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new ReflectionError(localIllegalAccessException);
    }
  }

  private static class ReflectionError extends Error
  {
    ReflectionError(Throwable paramThrowable)
    {
      super(paramThrowable);
    }
  }
}