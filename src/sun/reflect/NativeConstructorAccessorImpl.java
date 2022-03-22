package sun.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class NativeConstructorAccessorImpl extends ConstructorAccessorImpl
{
  private Constructor c;
  private DelegatingConstructorAccessorImpl parent;
  private int numInvocations;

  NativeConstructorAccessorImpl(Constructor paramConstructor)
  {
    this.c = paramConstructor;
  }

  public Object newInstance(Object[] paramArrayOfObject)
    throws InstantiationException, IllegalArgumentException, InvocationTargetException
  {
    if (++this.numInvocations > ReflectionFactory.inflationThreshold())
    {
      ConstructorAccessorImpl localConstructorAccessorImpl = (ConstructorAccessorImpl)new MethodAccessorGenerator().generateConstructor(this.c.getDeclaringClass(), this.c.getParameterTypes(), this.c.getExceptionTypes(), this.c.getModifiers());
      this.parent.setDelegate(localConstructorAccessorImpl);
    }
    return newInstance0(this.c, paramArrayOfObject);
  }

  void setParent(DelegatingConstructorAccessorImpl paramDelegatingConstructorAccessorImpl)
  {
    this.parent = paramDelegatingConstructorAccessorImpl;
  }

  private static native Object newInstance0(Constructor paramConstructor, Object[] paramArrayOfObject)
    throws InstantiationException, IllegalArgumentException, InvocationTargetException;
}