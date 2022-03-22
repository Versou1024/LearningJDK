package sun.reflect.generics.tree;

import sun.reflect.generics.visitor.TypeTreeVisitor;

public class IntSignature
  implements BaseType
{
  private static IntSignature singleton = new IntSignature();

  public static IntSignature make()
  {
    return singleton;
  }

  public void accept(TypeTreeVisitor<?> paramTypeTreeVisitor)
  {
    paramTypeTreeVisitor.visitIntSignature(this);
  }
}