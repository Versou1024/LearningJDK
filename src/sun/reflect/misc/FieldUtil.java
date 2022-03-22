package sun.reflect.misc;

import java.lang.reflect.Field;

public final class FieldUtil
{
  public static Field getField(Class paramClass, String paramString)
    throws NoSuchFieldException
  {
    ReflectUtil.checkPackageAccess(paramClass);
    return paramClass.getField(paramString);
  }

  public static Field[] getFields(Class paramClass)
  {
    ReflectUtil.checkPackageAccess(paramClass);
    return paramClass.getFields();
  }

  public static Field[] getDeclaredFields(Class paramClass)
  {
    ReflectUtil.checkPackageAccess(paramClass);
    return paramClass.getDeclaredFields();
  }
}