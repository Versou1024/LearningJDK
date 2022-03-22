package sun.reflect.generics.tree;

import sun.reflect.generics.visitor.TypeTreeVisitor;

public class BottomSignature
  implements FieldTypeSignature
{
  private static BottomSignature singleton = new BottomSignature();

  public static BottomSignature make()
  {
    return singleton;
  }

  public void accept(TypeTreeVisitor<?> paramTypeTreeVisitor)
  {
    paramTypeTreeVisitor.visitBottomSignature(this);
  }
}