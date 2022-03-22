package sun.security.action;

import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.Map;

public class PutAllAction
  implements PrivilegedAction
{
  private final Provider provider;
  private final Map map;

  public PutAllAction(Provider paramProvider, Map paramMap)
  {
    this.provider = paramProvider;
    this.map = paramMap;
  }

  public Object run()
  {
    this.provider.putAll(this.map);
    return null;
  }
}