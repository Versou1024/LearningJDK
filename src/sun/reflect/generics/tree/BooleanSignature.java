package sun.reflect.generics.tree;

import sun.reflect.generics.visitor.TypeTreeVisitor;

public class BooleanSignature
  implements BaseType
{
  private static BooleanSignature singleton = new BooleanSignature();

  public static BooleanSignature make()
  {
    return singleton;
  }

  public void accept(TypeTreeVisitor<?> paramTypeTreeVisitor)
  {
    paramTypeTreeVisitor.visitBooleanSignature(this);
  }
}