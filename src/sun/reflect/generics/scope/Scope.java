package sun.reflect.generics.scope;

import java.lang.reflect.TypeVariable;

public abstract interface Scope
{
  public abstract TypeVariable<?> lookup(String paramString);
}