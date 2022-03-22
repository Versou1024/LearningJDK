package sun.reflect;

import java.lang.reflect.InvocationTargetException;

class DelegatingMethodAccessorImpl extends MethodAccessorImpl
{
  private MethodAccessorImpl delegate;

  DelegatingMethodAccessorImpl(MethodAccessorImpl paramMethodAccessorImpl)
  {
    setDelegate(paramMethodAccessorImpl);
  }

  public Object invoke(Object paramObject, Object[] paramArrayOfObject)
    throws IllegalArgumentException, InvocationTargetException
  {
    return this.delegate.invoke(paramObject, paramArrayOfObject);
  }

  void setDelegate(MethodAccessorImpl paramMethodAccessorImpl)
  {
    this.delegate = paramMethodAccessorImpl;
  }
}