package sun.reflect.generics.scope;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;

public abstract class AbstractScope<D extends GenericDeclaration>
  implements Scope
{
  private D recvr;
  private Scope enclosingScope;

  protected AbstractScope(D paramD)
  {
    this.recvr = paramD;
  }

  protected D getRecvr()
  {
    return this.recvr;
  }

  protected abstract Scope computeEnclosingScope();

  protected Scope getEnclosingScope()
  {
    if (this.enclosingScope == null)
      this.enclosingScope = computeEnclosingScope();
    return this.enclosingScope;
  }

  public TypeVariable<?> lookup(String paramString)
  {
    TypeVariable[] arrayOfTypeVariable1 = getRecvr().getTypeParameters();
    TypeVariable[] arrayOfTypeVariable2 = arrayOfTypeVariable1;
    int i = arrayOfTypeVariable2.length;
    for (int j = 0; j < i; ++j)
    {
      TypeVariable localTypeVariable = arrayOfTypeVariable2[j];
      if (localTypeVariable.getName().equals(paramString))
        return localTypeVariable;
    }
    return getEnclosingScope().lookup(paramString);
  }
}