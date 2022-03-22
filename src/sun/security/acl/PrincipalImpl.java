package sun.security.acl;

import java.security.Principal;

public class PrincipalImpl
  implements Principal
{
  private String user;

  public PrincipalImpl(String paramString)
  {
    this.user = paramString;
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof PrincipalImpl)
    {
      PrincipalImpl localPrincipalImpl = (PrincipalImpl)paramObject;
      return this.user.equals(localPrincipalImpl.toString());
    }
    return false;
  }

  public String toString()
  {
    return this.user;
  }

  public int hashCode()
  {
    return this.user.hashCode();
  }

  public String getName()
  {
    return this.user;
  }
}