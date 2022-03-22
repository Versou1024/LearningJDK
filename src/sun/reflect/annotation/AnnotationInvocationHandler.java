package sun.reflect.annotation;

import B;
import C;
import D;
import F;
import I;
import J;
import S;
import Z;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class AnnotationInvocationHandler
  implements InvocationHandler, Serializable
{
  private final Class type;
  private final Map<String, Object> memberValues;
  private volatile transient Method[] memberMethods = null;

  AnnotationInvocationHandler(Class paramClass, Map<String, Object> paramMap)
  {
    this.type = paramClass;
    this.memberValues = paramMap;
  }

  public Object invoke(Object paramObject, Method paramMethod, Object[] paramArrayOfObject)
  {
    String str = paramMethod.getName();
    Class[] arrayOfClass = paramMethod.getParameterTypes();
    if ((str.equals("equals")) && (arrayOfClass.length == 1) && (arrayOfClass[0] == Object.class))
      return equalsImpl(paramArrayOfObject[0]);
    if ((!($assertionsDisabled)) && (arrayOfClass.length != 0))
      throw new AssertionError();
    if (str.equals("toString"))
      return toStringImpl();
    if (str.equals("hashCode"))
      return Integer.valueOf(hashCodeImpl());
    if (str.equals("annotationType"))
      return this.type;
    Object localObject = this.memberValues.get(str);
    if (localObject == null)
      throw new IncompleteAnnotationException(this.type, str);
    if (localObject instanceof ExceptionProxy)
      throw ((ExceptionProxy)localObject).generateException();
    if ((localObject.getClass().isArray()) && (Array.getLength(localObject) != 0))
      localObject = cloneArray(localObject);
    return localObject;
  }

  private Object cloneArray(Object paramObject)
  {
    Class localClass = paramObject.getClass();
    if (localClass == [B.class)
    {
      localObject = (byte[])(byte[])paramObject;
      return ((byte[])localObject).clone();
    }
    if (localClass == [C.class)
    {
      localObject = (char[])(char[])paramObject;
      return ((char[])localObject).clone();
    }
    if (localClass == [D.class)
    {
      localObject = (double[])(double[])paramObject;
      return ((double[])localObject).clone();
    }
    if (localClass == [F.class)
    {
      localObject = (float[])(float[])paramObject;
      return ((float[])localObject).clone();
    }
    if (localClass == [I.class)
    {
      localObject = (int[])(int[])paramObject;
      return ((int[])localObject).clone();
    }
    if (localClass == [J.class)
    {
      localObject = (long[])(long[])paramObject;
      return ((long[])localObject).clone();
    }
    if (localClass == [S.class)
    {
      localObject = (short[])(short[])paramObject;
      return ((short[])localObject).clone();
    }
    if (localClass == [Z.class)
    {
      localObject = (boolean[])(boolean[])paramObject;
      return ((boolean[])localObject).clone();
    }
    Object localObject = (Object[])(Object[])paramObject;
    return ((Object[])localObject).clone();
  }

  private String toStringImpl()
  {
    StringBuffer localStringBuffer = new StringBuffer(128);
    localStringBuffer.append('@');
    localStringBuffer.append(this.type.getName());
    localStringBuffer.append('(');
    int i = 1;
    Iterator localIterator = this.memberValues.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      if (i != 0)
        i = 0;
      else
        localStringBuffer.append(", ");
      localStringBuffer.append((String)localEntry.getKey());
      localStringBuffer.append('=');
      localStringBuffer.append(memberValueToString(localEntry.getValue()));
    }
    localStringBuffer.append(')');
    return localStringBuffer.toString();
  }

  private static String memberValueToString(Object paramObject)
  {
    Class localClass = paramObject.getClass();
    if (!(localClass.isArray()))
      return paramObject.toString();
    if (localClass == [B.class)
      return Arrays.toString((byte[])(byte[])paramObject);
    if (localClass == [C.class)
      return Arrays.toString((char[])(char[])paramObject);
    if (localClass == [D.class)
      return Arrays.toString((double[])(double[])paramObject);
    if (localClass == [F.class)
      return Arrays.toString((float[])(float[])paramObject);
    if (localClass == [I.class)
      return Arrays.toString((int[])(int[])paramObject);
    if (localClass == [J.class)
      return Arrays.toString((long[])(long[])paramObject);
    if (localClass == [S.class)
      return Arrays.toString((short[])(short[])paramObject);
    if (localClass == [Z.class)
      return Arrays.toString((boolean[])(boolean[])paramObject);
    return Arrays.toString((Object[])(Object[])paramObject);
  }

  private Boolean equalsImpl(Object paramObject)
  {
    if (paramObject == this)
      return Boolean.valueOf(true);
    if (!(this.type.isInstance(paramObject)))
      return Boolean.valueOf(false);
    Method[] arrayOfMethod = getMemberMethods();
    int i = arrayOfMethod.length;
    for (int j = 0; j < i; ++j)
    {
      Method localMethod = arrayOfMethod[j];
      String str = localMethod.getName();
      Object localObject1 = this.memberValues.get(str);
      Object localObject2 = null;
      AnnotationInvocationHandler localAnnotationInvocationHandler = asOneOfUs(paramObject);
      if (localAnnotationInvocationHandler != null)
        localObject2 = localAnnotationInvocationHandler.memberValues.get(str);
      else
        try
        {
          localObject2 = localMethod.invoke(paramObject, new Object[0]);
        }
        catch (InvocationTargetException localInvocationTargetException)
        {
          return Boolean.valueOf(false);
        }
        catch (IllegalAccessException localIllegalAccessException)
        {
          throw new AssertionError(localIllegalAccessException);
        }
      if (!(memberValueEquals(localObject1, localObject2)))
        return Boolean.valueOf(false);
    }
    return Boolean.valueOf(true);
  }

  private AnnotationInvocationHandler asOneOfUs(Object paramObject)
  {
    if (Proxy.isProxyClass(paramObject.getClass()))
    {
      InvocationHandler localInvocationHandler = Proxy.getInvocationHandler(paramObject);
      if (localInvocationHandler instanceof AnnotationInvocationHandler)
        return ((AnnotationInvocationHandler)localInvocationHandler);
    }
    return null;
  }

  private static boolean memberValueEquals(Object paramObject1, Object paramObject2)
  {
    Class localClass = paramObject1.getClass();
    if (!(localClass.isArray()))
      return paramObject1.equals(paramObject2);
    if ((paramObject1 instanceof Object[]) && (paramObject2 instanceof Object[]))
      return Arrays.equals((Object[])(Object[])paramObject1, (Object[])(Object[])paramObject2);
    if (paramObject2.getClass() != localClass)
      return false;
    if (localClass == [B.class)
      return Arrays.equals((byte[])(byte[])paramObject1, (byte[])(byte[])paramObject2);
    if (localClass == [C.class)
      return Arrays.equals((char[])(char[])paramObject1, (char[])(char[])paramObject2);
    if (localClass == [D.class)
      return Arrays.equals((double[])(double[])paramObject1, (double[])(double[])paramObject2);
    if (localClass == [F.class)
      return Arrays.equals((float[])(float[])paramObject1, (float[])(float[])paramObject2);
    if (localClass == [I.class)
      return Arrays.equals((int[])(int[])paramObject1, (int[])(int[])paramObject2);
    if (localClass == [J.class)
      return Arrays.equals((long[])(long[])paramObject1, (long[])(long[])paramObject2);
    if (localClass == [S.class)
      return Arrays.equals((short[])(short[])paramObject1, (short[])(short[])paramObject2);
    if ((!($assertionsDisabled)) && (localClass != [Z.class))
      throw new AssertionError();
    return Arrays.equals((boolean[])(boolean[])paramObject1, (boolean[])(boolean[])paramObject2);
  }

  private Method[] getMemberMethods()
  {
    if (this.memberMethods == null)
    {
      Method[] arrayOfMethod = this.type.getDeclaredMethods();
      AccessController.doPrivileged(new PrivilegedAction(this, arrayOfMethod)
      {
        public Object run()
        {
          AccessibleObject.setAccessible(this.val$mm, true);
          return null;
        }
      });
      this.memberMethods = arrayOfMethod;
    }
    return this.memberMethods;
  }

  private int hashCodeImpl()
  {
    int i = 0;
    Iterator localIterator = this.memberValues.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      i += (127 * ((String)localEntry.getKey()).hashCode() ^ memberValueHashCode(localEntry.getValue()));
    }
    return i;
  }

  private static int memberValueHashCode(Object paramObject)
  {
    Class localClass = paramObject.getClass();
    if (!(localClass.isArray()))
      return paramObject.hashCode();
    if (localClass == [B.class)
      return Arrays.hashCode((byte[])(byte[])paramObject);
    if (localClass == [C.class)
      return Arrays.hashCode((char[])(char[])paramObject);
    if (localClass == [D.class)
      return Arrays.hashCode((double[])(double[])paramObject);
    if (localClass == [F.class)
      return Arrays.hashCode((float[])(float[])paramObject);
    if (localClass == [I.class)
      return Arrays.hashCode((int[])(int[])paramObject);
    if (localClass == [J.class)
      return Arrays.hashCode((long[])(long[])paramObject);
    if (localClass == [S.class)
      return Arrays.hashCode((short[])(short[])paramObject);
    if (localClass == [Z.class)
      return Arrays.hashCode((boolean[])(boolean[])paramObject);
    return Arrays.hashCode((Object[])(Object[])paramObject);
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    AnnotationType localAnnotationType = null;
    try
    {
      localAnnotationType = AnnotationType.getInstance(this.type);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      return;
    }
    Map localMap = localAnnotationType.memberTypes();
    Iterator localIterator = this.memberValues.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      String str = (String)localEntry.getKey();
      Class localClass = (Class)localMap.get(str);
      if (localClass != null)
      {
        Object localObject = localEntry.getValue();
        if ((!(localClass.isInstance(localObject))) && (!(localObject instanceof ExceptionProxy)))
          localEntry.setValue(new AnnotationTypeMismatchExceptionProxy(localObject.getClass() + "[" + localObject + "]").setMember((Method)localAnnotationType.members().get(str)));
      }
    }
  }
}