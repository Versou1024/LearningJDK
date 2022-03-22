package sun.security.krb5.internal.rcache;

import java.io.PrintStream;
import java.util.Hashtable;
import sun.security.krb5.internal.Krb5;

public class CacheTable extends Hashtable
{
  private static final long serialVersionUID = -4695501354546664910L;
  private boolean DEBUG = Krb5.DEBUG;

  public synchronized void put(String paramString, AuthTime paramAuthTime, long paramLong)
  {
    ReplayCache localReplayCache = (ReplayCache)super.get(paramString);
    if (localReplayCache == null)
    {
      if (this.DEBUG)
        System.out.println("replay cache for " + paramString + " is null.");
      localReplayCache = new ReplayCache(paramString, this);
      localReplayCache.put(paramAuthTime, paramLong);
      super.put(paramString, localReplayCache);
    }
    else
    {
      localReplayCache.put(paramAuthTime, paramLong);
      super.put(paramString, localReplayCache);
      if (this.DEBUG)
        System.out.println("replay cache found.");
    }
  }

  public Object get(AuthTime paramAuthTime, String paramString)
  {
    ReplayCache localReplayCache = (ReplayCache)super.get(paramString);
    if ((localReplayCache != null) && (localReplayCache.contains(paramAuthTime)))
      return paramAuthTime;
    return null;
  }
}