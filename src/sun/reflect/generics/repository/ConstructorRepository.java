package sun.reflect.generics.repository;

import java.lang.reflect.Type;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.tree.MethodTypeSignature;
import sun.reflect.generics.tree.TypeSignature;
import sun.reflect.generics.visitor.Reifier;

public class ConstructorRepository extends GenericDeclRepository<MethodTypeSignature>
{
  private Type[] paramTypes;
  private Type[] exceptionTypes;

  protected ConstructorRepository(String paramString, GenericsFactory paramGenericsFactory)
  {
    super(paramString, paramGenericsFactory);
  }

  protected MethodTypeSignature parse(String paramString)
  {
    return SignatureParser.make().parseMethodSig(paramString);
  }

  public static ConstructorRepository make(String paramString, GenericsFactory paramGenericsFactory)
  {
    return new ConstructorRepository(paramString, paramGenericsFactory);
  }

  public Type[] getParameterTypes()
  {
    if (this.paramTypes == null)
    {
      TypeSignature[] arrayOfTypeSignature = ((MethodTypeSignature)getTree()).getParameterTypes();
      Type[] arrayOfType = new Type[arrayOfTypeSignature.length];
      for (int i = 0; i < arrayOfTypeSignature.length; ++i)
      {
        Reifier localReifier = getReifier();
        arrayOfTypeSignature[i].accept(localReifier);
        arrayOfType[i] = localReifier.getResult();
      }
      this.paramTypes = arrayOfType;
    }
    return ((Type[])this.paramTypes.clone());
  }

  public Type[] getExceptionTypes()
  {
    if (this.exceptionTypes == null)
    {
      FieldTypeSignature[] arrayOfFieldTypeSignature = ((MethodTypeSignature)getTree()).getExceptionTypes();
      Type[] arrayOfType = new Type[arrayOfFieldTypeSignature.length];
      for (int i = 0; i < arrayOfFieldTypeSignature.length; ++i)
      {
        Reifier localReifier = getReifier();
        arrayOfFieldTypeSignature[i].accept(localReifier);
        arrayOfType[i] = localReifier.getResult();
      }
      this.exceptionTypes = arrayOfType;
    }
    return ((Type[])this.exceptionTypes.clone());
  }
}