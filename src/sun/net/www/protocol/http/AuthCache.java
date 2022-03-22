package sun.net.www.protocol.http;

public abstract interface AuthCache
{
  public abstract void put(String paramString, AuthCacheValue paramAuthCacheValue);

  public abstract AuthCacheValue get(String paramString1, String paramString2);

  public abstract void remove(String paramString, AuthCacheValue paramAuthCacheValue);
}