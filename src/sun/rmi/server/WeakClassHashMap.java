package sun.rmi.server;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class WeakClassHashMap<V>
{
  private Map<Class<?>, ValueCell<V>> internalMap = new WeakHashMap();

  public V get(Class<?> paramClass)
  {
    ValueCell localValueCell;
    synchronized (this.internalMap)
    {
      localValueCell = (ValueCell)this.internalMap.get(paramClass);
      if (localValueCell == null)
      {
        localValueCell = new ValueCell();
        this.internalMap.put(paramClass, localValueCell);
      }
    }
    synchronized (localValueCell)
    {
      Object localObject3 = null;
      if (localValueCell.ref != null)
        localObject3 = localValueCell.ref.get();
      if (localObject3 == null)
      {
        localObject3 = computeValue(paramClass);
        localValueCell.ref = new SoftReference(localObject3);
      }
      return localObject3;
    }
  }

  protected abstract V computeValue(Class<?> paramClass);

  private static class ValueCell<T>
  {
    Reference<T> ref = null;
  }
}