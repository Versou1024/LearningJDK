package sun.reflect;

import java.lang.reflect.InvocationTargetException;

abstract class ConstructorAccessorImpl extends MagicAccessorImpl
  implements ConstructorAccessor
{
  public abstract Object newInstance(Object[] paramArrayOfObject)
    throws InstantiationException, IllegalArgumentException, InvocationTargetException;
}