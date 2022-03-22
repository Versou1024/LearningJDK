package sun.security.krb5.internal.ccache;

import java.io.File;
import java.io.IOException;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;

public abstract class MemoryCredentialsCache extends CredentialsCache
{
  private static CredentialsCache getCCacheInstance(PrincipalName paramPrincipalName)
  {
    return null;
  }

  private static CredentialsCache getCCacheInstance(PrincipalName paramPrincipalName, File paramFile)
  {
    return null;
  }

  public abstract boolean exists(String paramString);

  public abstract void update(Credentials paramCredentials);

  public abstract void save()
    throws IOException, KrbException;

  public abstract Credentials[] getCredsList();

  public abstract Credentials getCreds(PrincipalName paramPrincipalName, Realm paramRealm);

  public abstract PrincipalName getPrimaryPrincipal();
}