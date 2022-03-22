package sun.reflect.misc;

import java.lang.reflect.Modifier;
import sun.reflect.Reflection;

public final class ReflectUtil
{
  public static Class forName(String paramString)
    throws ClassNotFoundException
  {
    checkPackageAccess(paramString);
    return Class.forName(paramString);
  }

  public static Object newInstance(Class paramClass)
    throws InstantiationException, IllegalAccessException
  {
    checkPackageAccess(paramClass);
    return paramClass.newInstance();
  }

  public static void ensureMemberAccess(Class paramClass1, Class paramClass2, Object paramObject, int paramInt)
    throws IllegalAccessException
  {
    if ((paramObject == null) && (Modifier.isProtected(paramInt)))
    {
      int i = paramInt;
      i &= -5;
      i |= 1;
      Reflection.ensureMemberAccess(paramClass1, paramClass2, paramObject, i);
      try
      {
        i &= -2;
        Reflection.ensureMemberAccess(paramClass1, paramClass2, paramObject, i);
        return;
      }
      catch (IllegalAccessException localIllegalAccessException)
      {
        if (isSubclassOf(paramClass1, paramClass2))
          return;
        throw localIllegalAccessException;
      }
    }
    Reflection.ensureMemberAccess(paramClass1, paramClass2, paramObject, paramInt);
  }

  private static boolean isSubclassOf(Class paramClass1, Class paramClass2)
  {
    while (paramClass1 != null)
    {
      if (paramClass1 == paramClass2)
        return true;
      paramClass1 = paramClass1.getSuperclass();
    }
    return false;
  }

  public static void checkPackageAccess(Class paramClass)
  {
    checkPackageAccess(paramClass.getName());
  }

  public static void checkPackageAccess(String paramString)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      String str = paramString.replace('/', '.');
      if (str.startsWith("["))
      {
        i = str.lastIndexOf(91) + 2;
        if ((i > 1) && (i < str.length()))
          str = str.substring(i);
      }
      int i = str.lastIndexOf(46);
      if (i != -1)
        localSecurityManager.checkPackageAccess(str.substring(0, i));
    }
  }

  public static boolean isPackageAccessible(Class paramClass)
  {
    try
    {
      checkPackageAccess(paramClass);
    }
    catch (SecurityException localSecurityException)
    {
      return false;
    }
    return true;
  }
}