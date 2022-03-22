package sun.security.jgss;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

public class GSSExceptionImpl extends GSSException
{
  private static final long serialVersionUID = 4251197939069005575L;
  private String majorMessage;

  GSSExceptionImpl(int paramInt, Oid paramOid)
  {
    super(paramInt);
    this.majorMessage = super.getMajorString() + ": " + paramOid;
  }

  public GSSExceptionImpl(int paramInt, String paramString)
  {
    super(paramInt);
    this.majorMessage = paramString;
  }

  public GSSExceptionImpl(int paramInt, Exception paramException)
  {
    super(paramInt);
    initCause(paramException);
  }

  public GSSExceptionImpl(int paramInt, String paramString, Exception paramException)
  {
    this(paramInt, paramString);
    initCause(paramException);
  }

  public String getMessage()
  {
    if (this.majorMessage != null)
      return this.majorMessage;
    return super.getMessage();
  }
}