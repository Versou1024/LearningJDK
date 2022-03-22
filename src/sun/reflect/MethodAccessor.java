package sun.reflect;

import java.lang.reflect.InvocationTargetException;

public abstract interface MethodAccessor
{
  public abstract Object invoke(Object paramObject, Object[] paramArrayOfObject)
    throws IllegalArgumentException, InvocationTargetException;
}