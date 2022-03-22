package sun.reflect.generics.visitor;

import sun.reflect.generics.tree.ClassSignature;
import sun.reflect.generics.tree.MethodTypeSignature;

public abstract interface Visitor<T> extends TypeTreeVisitor<T>
{
  public abstract void visitClassSignature(ClassSignature paramClassSignature);

  public abstract void visitMethodTypeSignature(MethodTypeSignature paramMethodTypeSignature);
}