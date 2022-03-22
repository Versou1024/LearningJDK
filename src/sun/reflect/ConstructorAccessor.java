package sun.reflect;

import java.lang.reflect.InvocationTargetException;

public abstract interface ConstructorAccessor
{
  public abstract Object newInstance(Object[] paramArrayOfObject)
    throws InstantiationException, IllegalArgumentException, InvocationTargetException;
}