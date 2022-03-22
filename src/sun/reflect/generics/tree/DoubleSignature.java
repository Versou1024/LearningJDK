package sun.reflect.generics.tree;

import sun.reflect.generics.visitor.TypeTreeVisitor;

public class DoubleSignature
  implements BaseType
{
  private static DoubleSignature singleton = new DoubleSignature();

  public static DoubleSignature make()
  {
    return singleton;
  }

  public void accept(TypeTreeVisitor<?> paramTypeTreeVisitor)
  {
    paramTypeTreeVisitor.visitDoubleSignature(this);
  }
}