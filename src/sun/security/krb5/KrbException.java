package sun.security.krb5;

import sun.security.krb5.internal.KRBError;
import sun.security.krb5.internal.Krb5;

public class KrbException extends Exception
{
  private static final long serialVersionUID = -4993302876451928596L;
  private int returnCode;
  private KRBError error;

  public KrbException(String paramString)
  {
    super(paramString);
  }

  public KrbException(int paramInt)
  {
    this.returnCode = paramInt;
  }

  public KrbException(int paramInt, String paramString)
  {
    this(paramString);
    this.returnCode = paramInt;
  }

  public KrbException(KRBError paramKRBError)
  {
    this.returnCode = paramKRBError.getErrorCode();
    this.error = paramKRBError;
  }

  public KrbException(KRBError paramKRBError, String paramString)
  {
    this(paramString);
    this.returnCode = paramKRBError.getErrorCode();
    this.error = paramKRBError;
  }

  public KRBError getError()
  {
    return this.error;
  }

  public int returnCode()
  {
    return this.returnCode;
  }

  public String returnCodeSymbol()
  {
    return returnCodeSymbol(this.returnCode);
  }

  public static String returnCodeSymbol(int paramInt)
  {
    return "not yet implemented";
  }

  public String returnCodeMessage()
  {
    return Krb5.getErrorMessage(this.returnCode);
  }

  public static String errorMessage(int paramInt)
  {
    return Krb5.getErrorMessage(paramInt);
  }

  public String krbErrorMessage()
  {
    StringBuffer localStringBuffer = new StringBuffer("krb_error " + this.returnCode);
    String str = getMessage();
    if (str != null)
    {
      localStringBuffer.append(" ");
      localStringBuffer.append(str);
    }
    return localStringBuffer.toString();
  }

  public String getMessage()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = returnCode();
    if (i != 0)
    {
      localStringBuffer.append(returnCodeMessage());
      localStringBuffer.append(" (").append(returnCode()).append(')');
    }
    String str = super.getMessage();
    if ((str != null) && (str.length() != 0))
    {
      if (i != 0)
        localStringBuffer.append(" - ");
      localStringBuffer.append(str);
    }
    return localStringBuffer.toString();
  }

  public String toString()
  {
    return "KrbException: " + getMessage();
  }
}