package sun.security.acl;

import java.security.Principal;
import java.security.acl.AclEntry;
import java.security.acl.Group;
import java.security.acl.Permission;
import java.util.Enumeration;
import java.util.Vector;

public class AclEntryImpl
  implements AclEntry
{
  private Principal user = null;
  private Vector permissionSet = new Vector(10, 10);
  private boolean negative = false;

  public AclEntryImpl(Principal paramPrincipal)
  {
    this.user = paramPrincipal;
  }

  public AclEntryImpl()
  {
  }

  public boolean setPrincipal(Principal paramPrincipal)
  {
    if (this.user != null)
      return false;
    this.user = paramPrincipal;
    return true;
  }

  public void setNegativePermissions()
  {
    this.negative = true;
  }

  public boolean isNegative()
  {
    return this.negative;
  }

  public boolean addPermission(Permission paramPermission)
  {
    if (this.permissionSet.contains(paramPermission))
      return false;
    this.permissionSet.addElement(paramPermission);
    return true;
  }

  public boolean removePermission(Permission paramPermission)
  {
    return this.permissionSet.removeElement(paramPermission);
  }

  public boolean checkPermission(Permission paramPermission)
  {
    return this.permissionSet.contains(paramPermission);
  }

  public Enumeration permissions()
  {
    return this.permissionSet.elements();
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    if (this.negative)
      localStringBuffer.append("-");
    else
      localStringBuffer.append("+");
    if (this.user instanceof Group)
      localStringBuffer.append("Group.");
    else
      localStringBuffer.append("User.");
    localStringBuffer.append(this.user + "=");
    Enumeration localEnumeration = permissions();
    while (localEnumeration.hasMoreElements())
    {
      Permission localPermission = (Permission)localEnumeration.nextElement();
      localStringBuffer.append(localPermission);
      if (localEnumeration.hasMoreElements())
        localStringBuffer.append(",");
    }
    return new String(localStringBuffer);
  }

  public synchronized Object clone()
  {
    AclEntryImpl localAclEntryImpl = new AclEntryImpl(this.user);
    localAclEntryImpl.permissionSet = ((Vector)this.permissionSet.clone());
    localAclEntryImpl.negative = this.negative;
    return localAclEntryImpl;
  }

  public Principal getPrincipal()
  {
    return this.user;
  }
}