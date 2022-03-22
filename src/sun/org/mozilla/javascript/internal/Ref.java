package sun.org.mozilla.javascript.internal;

import java.io.Serializable;

public abstract class Ref
  implements Serializable
{
  public boolean has(Context paramContext)
  {
    return true;
  }

  public abstract Object get(Context paramContext);

  public abstract Object set(Context paramContext, Object paramObject);

  public boolean delete(Context paramContext)
  {
    return false;
  }
}