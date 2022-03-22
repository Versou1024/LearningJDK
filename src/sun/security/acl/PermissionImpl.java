package sun.security.acl;

import java.security.acl.Permission;

public class PermissionImpl
  implements Permission
{
  private String permission;

  public PermissionImpl(String paramString)
  {
    this.permission = paramString;
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof Permission)
    {
      Permission localPermission = (Permission)paramObject;
      return this.permission.equals(localPermission.toString());
    }
    return false;
  }

  public String toString()
  {
    return this.permission;
  }

  public int hashCode()
  {
    return toString().hashCode();
  }
}