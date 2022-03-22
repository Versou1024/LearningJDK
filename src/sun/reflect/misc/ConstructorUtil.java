package sun.reflect.misc;

import java.lang.reflect.Constructor;

public final class ConstructorUtil
{
  public static Constructor getConstructor(Class paramClass, Class[] paramArrayOfClass)
    throws NoSuchMethodException
  {
    ReflectUtil.checkPackageAccess(paramClass);
    return paramClass.getConstructor(paramArrayOfClass);
  }

  public static Constructor[] getConstructors(Class paramClass)
  {
    ReflectUtil.checkPackageAccess(paramClass);
    return paramClass.getConstructors();
  }
}