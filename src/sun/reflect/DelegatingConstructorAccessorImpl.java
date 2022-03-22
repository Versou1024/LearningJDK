package sun.reflect;

import java.lang.reflect.InvocationTargetException;

class DelegatingConstructorAccessorImpl extends ConstructorAccessorImpl
{
  private ConstructorAccessorImpl delegate;

  DelegatingConstructorAccessorImpl(ConstructorAccessorImpl paramConstructorAccessorImpl)
  {
    setDelegate(paramConstructorAccessorImpl);
  }

  public Object newInstance(Object[] paramArrayOfObject)
    throws InstantiationException, IllegalArgumentException, InvocationTargetException
  {
    return this.delegate.newInstance(paramArrayOfObject);
  }

  void setDelegate(ConstructorAccessorImpl paramConstructorAccessorImpl)
  {
    this.delegate = paramConstructorAccessorImpl;
  }
}