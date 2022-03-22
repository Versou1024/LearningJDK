package sun.reflect.generics.scope;

import java.lang.reflect.Constructor;

public class ConstructorScope extends AbstractScope<Constructor>
{
  private ConstructorScope(Constructor paramConstructor)
  {
    super(paramConstructor);
  }

  private Class<?> getEnclosingClass()
  {
    return ((Constructor)getRecvr()).getDeclaringClass();
  }

  protected Scope computeEnclosingScope()
  {
    return ClassScope.make(getEnclosingClass());
  }

  public static ConstructorScope make(Constructor paramConstructor)
  {
    return new ConstructorScope(paramConstructor);
  }
}