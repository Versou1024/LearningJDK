package sun.reflect.generics.repository;

import java.lang.reflect.Type;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.TypeSignature;
import sun.reflect.generics.visitor.Reifier;

public class FieldRepository extends AbstractRepository<TypeSignature>
{
  private Type genericType;

  protected FieldRepository(String paramString, GenericsFactory paramGenericsFactory)
  {
    super(paramString, paramGenericsFactory);
  }

  protected TypeSignature parse(String paramString)
  {
    return SignatureParser.make().parseTypeSig(paramString);
  }

  public static FieldRepository make(String paramString, GenericsFactory paramGenericsFactory)
  {
    return new FieldRepository(paramString, paramGenericsFactory);
  }

  public Type getGenericType()
  {
    if (this.genericType == null)
    {
      Reifier localReifier = getReifier();
      ((TypeSignature)getTree()).accept(localReifier);
      this.genericType = localReifier.getResult();
    }
    return this.genericType;
  }
}