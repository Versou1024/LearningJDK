package sun.corba;

import java.security.BasicPermission;

public final class BridgePermission extends BasicPermission
{
  public BridgePermission(String paramString)
  {
    super(paramString);
  }

  public BridgePermission(String paramString1, String paramString2)
  {
    super(paramString1, paramString2);
  }
}