package sun.reflect.generics.repository;

import java.lang.reflect.Type;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.tree.MethodTypeSignature;
import sun.reflect.generics.tree.ReturnType;
import sun.reflect.generics.visitor.Reifier;

public class MethodRepository extends ConstructorRepository
{
  private Type returnType;

  private MethodRepository(String paramString, GenericsFactory paramGenericsFactory)
  {
    super(paramString, paramGenericsFactory);
  }

  public static MethodRepository make(String paramString, GenericsFactory paramGenericsFactory)
  {
    return new MethodRepository(paramString, paramGenericsFactory);
  }

  public Type getReturnType()
  {
    if (this.returnType == null)
    {
      Reifier localReifier = getReifier();
      ((MethodTypeSignature)getTree()).getReturnType().accept(localReifier);
      this.returnType = localReifier.getResult();
    }
    return this.returnType;
  }
}