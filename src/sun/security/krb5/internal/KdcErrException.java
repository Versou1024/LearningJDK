package sun.security.krb5.internal;

import sun.security.krb5.KrbException;

public class KdcErrException extends KrbException
{
  private static final long serialVersionUID = -8788186031117310306L;

  public KdcErrException(int paramInt)
  {
    super(paramInt);
  }

  public KdcErrException(int paramInt, String paramString)
  {
    super(paramInt, paramString);
  }
}