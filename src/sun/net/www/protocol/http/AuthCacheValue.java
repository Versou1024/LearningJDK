package sun.net.www.protocol.http;

import java.io.Serializable;
import java.net.PasswordAuthentication;

public abstract class AuthCacheValue
  implements Serializable
{
  protected static AuthCache cache = new AuthCacheImpl();

  public static void setAuthCache(AuthCache paramAuthCache)
  {
    cache = paramAuthCache;
  }

  abstract Type getAuthType();

  abstract String getHost();

  abstract int getPort();

  abstract String getRealm();

  abstract String getPath();

  abstract String getProtocolScheme();

  abstract PasswordAuthentication credentials();

  public static enum Type
  {
    Proxy, Server;
  }
}