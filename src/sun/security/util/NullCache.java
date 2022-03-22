package sun.security.util;

class NullCache extends Cache
{
  static final Cache INSTANCE = new NullCache();

  public int size()
  {
    return 0;
  }

  public void clear()
  {
  }

  public void put(Object paramObject1, Object paramObject2)
  {
  }

  public Object get(Object paramObject)
  {
    return null;
  }

  public void remove(Object paramObject)
  {
  }
}