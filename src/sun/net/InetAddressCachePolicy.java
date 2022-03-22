package sun.net;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import sun.security.action.GetIntegerAction;

public final class InetAddressCachePolicy
{
  private static final String cachePolicyProp = "networkaddress.cache.ttl";
  private static final String cachePolicyPropFallback = "sun.net.inetaddr.ttl";
  private static final String negativeCachePolicyProp = "networkaddress.cache.negative.ttl";
  private static final String negativeCachePolicyPropFallback = "sun.net.inetaddr.negative.ttl";
  public static final int FOREVER = -1;
  public static final int NEVER = 0;
  public static final int DEFAULT_POSITIVE = 30;
  private static int cachePolicy;
  private static int negativeCachePolicy;
  private static boolean set = false;
  private static boolean negativeSet = false;

  public static synchronized int get()
  {
    if ((!(set)) && (System.getSecurityManager() == null))
      return 30;
    return cachePolicy;
  }

  public static synchronized int getNegative()
  {
    return negativeCachePolicy;
  }

  public static synchronized void setIfNotSet(int paramInt)
  {
    if (!(set))
    {
      checkValue(paramInt, cachePolicy);
      cachePolicy = paramInt;
    }
  }

  public static synchronized void setNegativeIfNotSet(int paramInt)
  {
    if (!(negativeSet))
      negativeCachePolicy = paramInt;
  }

  private static void checkValue(int paramInt1, int paramInt2)
  {
    if (paramInt1 == -1)
      return;
    if ((paramInt2 == -1) || (paramInt1 < paramInt2) || (paramInt1 < -1))
      throw new SecurityException("can't make InetAddress cache more lax");
  }

  static
  {
    set = false;
    negativeSet = false;
    cachePolicy = -1;
    negativeCachePolicy = 0;
    Integer localInteger = null;
    try
    {
      localInteger = new Integer((String)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          return Security.getProperty("networkaddress.cache.ttl");
        }
      }));
    }
    catch (NumberFormatException localNumberFormatException1)
    {
    }
    if (localInteger != null)
    {
      cachePolicy = localInteger.intValue();
      if (cachePolicy < 0)
        cachePolicy = -1;
      set = true;
    }
    else
    {
      localInteger = (Integer)AccessController.doPrivileged(new GetIntegerAction("sun.net.inetaddr.ttl"));
      if (localInteger != null)
      {
        cachePolicy = localInteger.intValue();
        if (cachePolicy < 0)
          cachePolicy = -1;
        set = true;
      }
    }
    try
    {
      localInteger = new Integer((String)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          return Security.getProperty("networkaddress.cache.negative.ttl");
        }
      }));
    }
    catch (NumberFormatException localNumberFormatException2)
    {
    }
    if (localInteger != null)
    {
      negativeCachePolicy = localInteger.intValue();
      if (negativeCachePolicy < 0)
        negativeCachePolicy = -1;
      negativeSet = true;
    }
    else
    {
      localInteger = (Integer)AccessController.doPrivileged(new GetIntegerAction("sun.net.inetaddr.negative.ttl"));
      if (localInteger != null)
      {
        negativeCachePolicy = localInteger.intValue();
        if (negativeCachePolicy < 0)
          negativeCachePolicy = -1;
        negativeSet = true;
      }
    }
  }
}