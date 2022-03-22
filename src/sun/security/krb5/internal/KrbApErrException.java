package sun.security.krb5.internal;

import sun.security.krb5.KrbException;

public class KrbApErrException extends KrbException
{
  private static final long serialVersionUID = 7545264413323118315L;

  public KrbApErrException(int paramInt)
  {
    super(paramInt);
  }

  public KrbApErrException(int paramInt, String paramString)
  {
    super(paramInt, paramString);
  }
}