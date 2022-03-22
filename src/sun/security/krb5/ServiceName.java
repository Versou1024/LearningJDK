package sun.security.krb5;

public class ServiceName extends PrincipalName
{
  public ServiceName(String paramString, int paramInt)
    throws sun.security.krb5.RealmException
  {
    super(paramString, paramInt);
  }

  public ServiceName(String paramString)
    throws sun.security.krb5.RealmException
  {
    this(paramString, 0);
  }

  public ServiceName(String paramString1, String paramString2)
    throws sun.security.krb5.RealmException
  {
    this(paramString1, 0);
    setRealm(paramString2);
  }

  public ServiceName(String paramString1, String paramString2, String paramString3)
    throws sun.security.krb5.KrbException
  {
    super(paramString1, paramString2, paramString3, 2);
  }
}