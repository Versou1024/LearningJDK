package sun.reflect;

import java.lang.reflect.InvocationTargetException;

abstract class MethodAccessorImpl extends MagicAccessorImpl
  implements MethodAccessor
{
  public abstract Object invoke(Object paramObject, Object[] paramArrayOfObject)
    throws IllegalArgumentException, InvocationTargetException;
}