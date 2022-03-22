package sun.security.acl;

import java.security.Principal;

public class WorldGroupImpl extends GroupImpl
{
  public WorldGroupImpl(String paramString)
  {
    super(paramString);
  }

  public boolean isMember(Principal paramPrincipal)
  {
    return true;
  }
}