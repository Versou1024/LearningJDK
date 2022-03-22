package sun.reflect.generics.repository;

import java.lang.reflect.TypeVariable;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.Signature;
import sun.reflect.generics.visitor.Reifier;

public abstract class GenericDeclRepository<S extends Signature> extends AbstractRepository<S>
{
  private TypeVariable[] typeParams;

  protected GenericDeclRepository(String paramString, GenericsFactory paramGenericsFactory)
  {
    super(paramString, paramGenericsFactory);
  }

  public TypeVariable[] getTypeParameters()
  {
    if (this.typeParams == null)
    {
      FormalTypeParameter[] arrayOfFormalTypeParameter = ((Signature)getTree()).getFormalTypeParameters();
      TypeVariable[] arrayOfTypeVariable = new TypeVariable[arrayOfFormalTypeParameter.length];
      for (int i = 0; i < arrayOfFormalTypeParameter.length; ++i)
      {
        Reifier localReifier = getReifier();
        arrayOfFormalTypeParameter[i].accept(localReifier);
        arrayOfTypeVariable[i] = ((TypeVariable)localReifier.getResult());
      }
      this.typeParams = arrayOfTypeVariable;
    }
    return ((TypeVariable[])this.typeParams.clone());
  }
}