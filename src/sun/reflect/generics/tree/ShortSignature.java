package sun.reflect.generics.tree;

import sun.reflect.generics.visitor.TypeTreeVisitor;

public class ShortSignature
  implements BaseType
{
  private static ShortSignature singleton = new ShortSignature();

  public static ShortSignature make()
  {
    return singleton;
  }

  public void accept(TypeTreeVisitor<?> paramTypeTreeVisitor)
  {
    paramTypeTreeVisitor.visitShortSignature(this);
  }
}