package sun.reflect.generics.repository;

import java.lang.reflect.Type;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.ClassSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.TypeTree;
import sun.reflect.generics.visitor.Reifier;

public class ClassRepository extends GenericDeclRepository<ClassSignature>
{
  private Type superclass;
  private Type[] superInterfaces;

  private ClassRepository(String paramString, GenericsFactory paramGenericsFactory)
  {
    super(paramString, paramGenericsFactory);
  }

  protected ClassSignature parse(String paramString)
  {
    return SignatureParser.make().parseClassSig(paramString);
  }

  public static ClassRepository make(String paramString, GenericsFactory paramGenericsFactory)
  {
    return new ClassRepository(paramString, paramGenericsFactory);
  }

  public Type getSuperclass()
  {
    if (this.superclass == null)
    {
      Reifier localReifier = getReifier();
      ((ClassSignature)getTree()).getSuperclass().accept(localReifier);
      this.superclass = localReifier.getResult();
    }
    return this.superclass;
  }

  public Type[] getSuperInterfaces()
  {
    if (this.superInterfaces == null)
    {
      ClassTypeSignature[] arrayOfClassTypeSignature = ((ClassSignature)getTree()).getSuperInterfaces();
      Type[] arrayOfType = new Type[arrayOfClassTypeSignature.length];
      for (int i = 0; i < arrayOfClassTypeSignature.length; ++i)
      {
        Reifier localReifier = getReifier();
        arrayOfClassTypeSignature[i].accept(localReifier);
        arrayOfType[i] = localReifier.getResult();
      }
      this.superInterfaces = arrayOfType;
    }
    return ((Type[])this.superInterfaces.clone());
  }
}