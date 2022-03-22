package sun.security.acl;

import java.security.Principal;
import java.security.acl.Group;
import java.security.acl.LastOwnerException;
import java.security.acl.NotOwnerException;
import java.security.acl.Owner;
import java.util.Enumeration;

public class OwnerImpl
  implements Owner
{
  private Group ownerGroup = new GroupImpl("AclOwners");

  public OwnerImpl(Principal paramPrincipal)
  {
    this.ownerGroup.addMember(paramPrincipal);
  }

  public synchronized boolean addOwner(Principal paramPrincipal1, Principal paramPrincipal2)
    throws NotOwnerException
  {
    if (!(isOwner(paramPrincipal1)))
      throw new NotOwnerException();
    this.ownerGroup.addMember(paramPrincipal2);
    return false;
  }

  public synchronized boolean deleteOwner(Principal paramPrincipal1, Principal paramPrincipal2)
    throws NotOwnerException, LastOwnerException
  {
    if (!(isOwner(paramPrincipal1)))
      throw new NotOwnerException();
    Enumeration localEnumeration = this.ownerGroup.members();
    Object localObject = localEnumeration.nextElement();
    if (localEnumeration.hasMoreElements())
      return this.ownerGroup.removeMember(paramPrincipal2);
    throw new LastOwnerException();
  }

  public synchronized boolean isOwner(Principal paramPrincipal)
  {
    return this.ownerGroup.isMember(paramPrincipal);
  }
}