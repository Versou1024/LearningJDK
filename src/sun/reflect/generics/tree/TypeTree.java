package sun.reflect.generics.tree;

import sun.reflect.generics.visitor.TypeTreeVisitor;

public abstract interface TypeTree extends Tree
{
  public abstract void accept(TypeTreeVisitor<?> paramTypeTreeVisitor);
}