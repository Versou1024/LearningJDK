package sun.security.acl;

import java.security.acl.Permission;

public class AllPermissionsImpl extends PermissionImpl
{
  public AllPermissionsImpl(String paramString)
  {
    super(paramString);
  }

  public boolean equals(Permission paramPermission)
  {
    return true;
  }
}