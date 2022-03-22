package sun.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class NativeMethodAccessorImpl extends MethodAccessorImpl
{
  private Method method;
  private DelegatingMethodAccessorImpl parent;
  private int numInvocations;

  NativeMethodAccessorImpl(Method paramMethod)
  {
    this.method = paramMethod;
  }

  public Object invoke(Object paramObject, Object[] paramArrayOfObject)
    throws IllegalArgumentException, InvocationTargetException
  {
    if (++this.numInvocations > ReflectionFactory.inflationThreshold())
    {
      MethodAccessorImpl localMethodAccessorImpl = (MethodAccessorImpl)new MethodAccessorGenerator().generateMethod(this.method.getDeclaringClass(), this.method.getName(), this.method.getParameterTypes(), this.method.getReturnType(), this.method.getExceptionTypes(), this.method.getModifiers());
      this.parent.setDelegate(localMethodAccessorImpl);
    }
    return invoke0(this.method, paramObject, paramArrayOfObject);
  }

  void setParent(DelegatingMethodAccessorImpl paramDelegatingMethodAccessorImpl)
  {
    this.parent = paramDelegatingMethodAccessorImpl;
  }

  private static native Object invoke0(Method paramMethod, Object paramObject, Object[] paramArrayOfObject);
}