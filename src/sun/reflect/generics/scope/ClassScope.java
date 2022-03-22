package sun.reflect.generics.scope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ClassScope extends AbstractScope<Class<?>>
  implements Scope
{
  private ClassScope(Class<?> paramClass)
  {
    super(paramClass);
  }

  protected Scope computeEnclosingScope()
  {
    Class localClass1 = (Class)getRecvr();
    Method localMethod = localClass1.getEnclosingMethod();
    if (localMethod != null)
      return MethodScope.make(localMethod);
    Constructor localConstructor = localClass1.getEnclosingConstructor();
    if (localConstructor != null)
      return ConstructorScope.make(localConstructor);
    Class localClass2 = localClass1.getEnclosingClass();
    if (localClass2 != null)
      return make(localClass2);
    return DummyScope.make();
  }

  public static ClassScope make(Class<?> paramClass)
  {
    return new ClassScope(paramClass);
  }
}