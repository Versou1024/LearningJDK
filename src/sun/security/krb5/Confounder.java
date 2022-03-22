package sun.security.krb5;

public final class Confounder
{
  public static byte[] bytes(int paramInt)
    throws sun.security.krb5.KrbCryptoException
  {
    return sun.security.krb5.internal.crypto.Confounder.bytes(paramInt);
  }

  public static int intValue()
    throws sun.security.krb5.KrbCryptoException
  {
    return sun.security.krb5.internal.crypto.Confounder.intValue();
  }

  public static long longValue()
    throws sun.security.krb5.KrbCryptoException
  {
    return sun.security.krb5.internal.crypto.Confounder.longValue();
  }
}