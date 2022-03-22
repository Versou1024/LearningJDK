package sun.reflect;

import java.lang.reflect.InvocationTargetException;

class InstantiationExceptionConstructorAccessorImpl extends ConstructorAccessorImpl
{
  private String message;

  InstantiationExceptionConstructorAccessorImpl(String paramString)
  {
    this.message = paramString;
  }

  public Object newInstance(Object[] paramArrayOfObject)
    throws InstantiationException, IllegalArgumentException, InvocationTargetException
  {
    if (this.message == null)
      throw new InstantiationException();
    throw new InstantiationException(this.message);
  }
}