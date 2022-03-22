package sun.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Reflection
{
  private static Map fieldFilterMap = Collections.synchronizedMap(new HashMap());

  public static native Class getCallerClass(int paramInt);

  private static native int getClassAccessFlags(Class paramClass);

  public static boolean quickCheckMemberAccess(Class paramClass, int paramInt)
  {
    return Modifier.isPublic(getClassAccessFlags(paramClass) & paramInt);
  }

  public static void ensureMemberAccess(Class paramClass1, Class paramClass2, Object paramObject, int paramInt)
    throws IllegalAccessException
  {
    if ((paramClass1 == null) || (paramClass2 == null))
      throw new InternalError();
    if (!(verifyMemberAccess(paramClass1, paramClass2, paramObject, paramInt)))
      throw new IllegalAccessException("Class " + paramClass1.getName() + " can not access a member of class " + paramClass2.getName() + " with modifiers \"" + Modifier.toString(paramInt) + "\"");
  }

  public static boolean verifyMemberAccess(Class paramClass1, Class paramClass2, Object paramObject, int paramInt)
  {
    int i = 0;
    boolean bool = false;
    if (paramClass1 == paramClass2)
      return true;
    if (!(Modifier.isPublic(getClassAccessFlags(paramClass2))))
    {
      bool = isSameClassPackage(paramClass1, paramClass2);
      i = 1;
      if (!(bool))
        return false;
    }
    if (Modifier.isPublic(paramInt))
      return true;
    int j = 0;
    if ((Modifier.isProtected(paramInt)) && (isSubclassOf(paramClass1, paramClass2)))
      j = 1;
    if ((j == 0) && (!(Modifier.isPrivate(paramInt))))
    {
      if (i == 0)
      {
        bool = isSameClassPackage(paramClass1, paramClass2);
        i = 1;
      }
      if (bool)
        j = 1;
    }
    if (j == 0)
      return false;
    if (!(Modifier.isProtected(paramInt)))
      break label170;
    Class localClass = (paramObject == null) ? paramClass2 : paramObject.getClass();
    if (localClass == paramClass1)
      break label170;
    if (i == 0)
    {
      bool = isSameClassPackage(paramClass1, paramClass2);
      i = 1;
    }
    label170: return ((bool) || (isSubclassOf(localClass, paramClass1)));
  }

  private static boolean isSameClassPackage(Class paramClass1, Class paramClass2)
  {
    return isSameClassPackage(paramClass1.getClassLoader(), paramClass1.getName(), paramClass2.getClassLoader(), paramClass2.getName());
  }

  private static boolean isSameClassPackage(ClassLoader paramClassLoader1, String paramString1, ClassLoader paramClassLoader2, String paramString2)
  {
    if (paramClassLoader1 != paramClassLoader2)
      return false;
    int i = paramString1.lastIndexOf(46);
    int j = paramString2.lastIndexOf(46);
    if ((i == -1) || (j == -1))
      return (i == j);
    int k = 0;
    int l = 0;
    if (paramString1.charAt(k) == '[')
    {
      do;
      while (paramString1.charAt(++k) == '[');
      if (paramString1.charAt(k) != 'L')
        throw new InternalError("Illegal class name " + paramString1);
    }
    if (paramString2.charAt(l) == '[')
    {
      do;
      while (paramString2.charAt(++l) == '[');
      if (paramString2.charAt(l) != 'L')
        throw new InternalError("Illegal class name " + paramString2);
    }
    int i1 = i - k;
    int i2 = j - l;
    if (i1 != i2)
      return false;
    return paramString1.regionMatches(false, k, paramString2, l, i1);
  }

  static boolean isSubclassOf(Class paramClass1, Class paramClass2)
  {
    while (paramClass1 != null)
    {
      if (paramClass1 == paramClass2)
        return true;
      paramClass1 = paramClass1.getSuperclass();
    }
    return false;
  }

  public static void registerFieldsToFilter(Class paramClass, String[] paramArrayOfString)
  {
    fieldFilterMap.put(paramClass, paramArrayOfString);
  }

  public static Field[] filterFields(Class paramClass, Field[] paramArrayOfField)
  {
    int i1;
    if (fieldFilterMap == null)
      return paramArrayOfField;
    String[] arrayOfString = (String[])(String[])fieldFilterMap.get(paramClass);
    if (arrayOfString == null)
      return paramArrayOfField;
    int i = 0;
    for (int j = 0; j < paramArrayOfField.length; ++j)
    {
      k = 0;
      Field localField1 = paramArrayOfField[j];
      for (i1 = 0; i1 < arrayOfString.length; ++i1)
        if (localField1.getName() == arrayOfString[i1])
        {
          k = 1;
          break;
        }
      if (k == 0)
        ++i;
    }
    Field[] arrayOfField = new Field[i];
    int k = 0;
    for (int l = 0; l < paramArrayOfField.length; ++l)
    {
      i1 = 0;
      Field localField2 = paramArrayOfField[l];
      for (int i2 = 0; i2 < arrayOfString.length; ++i2)
        if (localField2.getName() == arrayOfString[i2])
        {
          i1 = 1;
          break;
        }
      if (i1 == 0)
        arrayOfField[(k++)] = localField2;
    }
    return arrayOfField;
  }
}