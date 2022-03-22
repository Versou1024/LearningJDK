package sun.reflect.generics.tree;

import sun.reflect.generics.visitor.TypeTreeVisitor;

public class FloatSignature
  implements BaseType
{
  private static FloatSignature singleton = new FloatSignature();

  public static FloatSignature make()
  {
    return singleton;
  }

  public void accept(TypeTreeVisitor<?> paramTypeTreeVisitor)
  {
    paramTypeTreeVisitor.visitFloatSignature(this);
  }
}