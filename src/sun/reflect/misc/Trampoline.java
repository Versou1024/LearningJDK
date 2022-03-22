package sun.reflect.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Trampoline
{
  private static Object invoke(Method paramMethod, Object paramObject, Object[] paramArrayOfObject)
    throws InvocationTargetException, IllegalAccessException
  {
    return paramMethod.invoke(paramObject, paramArrayOfObject);
  }
}