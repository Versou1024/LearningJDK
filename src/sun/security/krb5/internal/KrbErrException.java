package sun.security.krb5.internal;

import sun.security.krb5.KrbException;

public class KrbErrException extends KrbException
{
  private static final long serialVersionUID = 2186533836785448317L;

  public KrbErrException(int paramInt)
  {
    super(paramInt);
  }

  public KrbErrException(int paramInt, String paramString)
  {
    super(paramInt, paramString);
  }
}