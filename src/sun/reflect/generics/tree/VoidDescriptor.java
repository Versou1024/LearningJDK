package sun.reflect.generics.tree;

import sun.reflect.generics.visitor.TypeTreeVisitor;

public class VoidDescriptor
  implements ReturnType
{
  private static VoidDescriptor singleton = new VoidDescriptor();

  public static VoidDescriptor make()
  {
    return singleton;
  }

  public void accept(TypeTreeVisitor<?> paramTypeTreeVisitor)
  {
    paramTypeTreeVisitor.visitVoidDescriptor(this);
  }
}