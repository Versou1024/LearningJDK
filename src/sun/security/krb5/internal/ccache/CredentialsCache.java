package sun.security.krb5.internal.ccache;

import java.io.IOException;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.LoginOptions;

public abstract class CredentialsCache
{
  static CredentialsCache singleton = null;
  static String cacheName;
  private static boolean DEBUG = Krb5.DEBUG;

  public static CredentialsCache getInstance(PrincipalName paramPrincipalName)
  {
    return FileCredentialsCache.acquireInstance(paramPrincipalName, null);
  }

  public static CredentialsCache getInstance(String paramString)
  {
    if ((paramString.length() >= 5) && (paramString.substring(0, 5).equalsIgnoreCase("FILE:")))
      return FileCredentialsCache.acquireInstance(null, paramString.substring(5));
    return FileCredentialsCache.acquireInstance(null, paramString);
  }

  public static CredentialsCache getInstance(PrincipalName paramPrincipalName, String paramString)
  {
    if ((paramString != null) && (paramString.length() >= 5) && (paramString.regionMatches(true, 0, "FILE:", 0, 5)))
      return FileCredentialsCache.acquireInstance(paramPrincipalName, paramString.substring(5));
    return FileCredentialsCache.acquireInstance(paramPrincipalName, paramString);
  }

  public static CredentialsCache getInstance()
  {
    return FileCredentialsCache.acquireInstance();
  }

  public static CredentialsCache create(PrincipalName paramPrincipalName, String paramString)
  {
    if (paramString == null)
      throw new RuntimeException("cache name error");
    if ((paramString.length() >= 5) && (paramString.regionMatches(true, 0, "FILE:", 0, 5)))
    {
      paramString = paramString.substring(5);
      return FileCredentialsCache.New(paramPrincipalName, paramString);
    }
    return FileCredentialsCache.New(paramPrincipalName, paramString);
  }

  public static CredentialsCache create(PrincipalName paramPrincipalName)
  {
    return FileCredentialsCache.New(paramPrincipalName);
  }

  public static String cacheName()
  {
    return cacheName;
  }

  public abstract PrincipalName getPrimaryPrincipal();

  public abstract void update(Credentials paramCredentials);

  public abstract void save()
    throws IOException, KrbException;

  public abstract Credentials[] getCredsList();

  public abstract Credentials getDefaultCreds();

  public abstract Credentials getCreds(PrincipalName paramPrincipalName, Realm paramRealm);

  public abstract Credentials getCreds(LoginOptions paramLoginOptions, PrincipalName paramPrincipalName, Realm paramRealm);
}