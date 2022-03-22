package sun.reflect.generics.scope;

import java.lang.reflect.TypeVariable;

public class DummyScope
  implements Scope
{
  private static DummyScope singleton = new DummyScope();

  public static DummyScope make()
  {
    return singleton;
  }

  public TypeVariable<?> lookup(String paramString)
  {
    return null;
  }
}