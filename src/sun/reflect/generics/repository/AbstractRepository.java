package sun.reflect.generics.repository;

import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.tree.Tree;
import sun.reflect.generics.visitor.Reifier;

public abstract class AbstractRepository<T extends Tree>
{
  private GenericsFactory factory;
  private T tree = parse(paramString);

  private GenericsFactory getFactory()
  {
    return this.factory;
  }

  protected T getTree()
  {
    return this.tree;
  }

  protected Reifier getReifier()
  {
    return Reifier.make(getFactory());
  }

  protected AbstractRepository(String paramString, GenericsFactory paramGenericsFactory)
  {
    this.factory = paramGenericsFactory;
  }

  protected abstract T parse(String paramString);
}