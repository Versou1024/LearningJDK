package sun.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import sun.misc.Unsafe;

class BootstrapConstructorAccessorImpl extends ConstructorAccessorImpl
{
  private Constructor constructor;

  BootstrapConstructorAccessorImpl(Constructor paramConstructor)
  {
    this.constructor = paramConstructor;
  }

  public Object newInstance(Object[] paramArrayOfObject)
    throws IllegalArgumentException, InvocationTargetException
  {
    try
    {
      return UnsafeFieldAccessorImpl.unsafe.allocateInstance(this.constructor.getDeclaringClass());
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new InvocationTargetException(localInstantiationException);
    }
  }
}