package sun.security.acl;

import java.security.Principal;
import java.security.acl.Acl;
import java.security.acl.AclEntry;
import java.security.acl.Group;
import java.security.acl.NotOwnerException;
import java.security.acl.Permission;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class AclImpl extends OwnerImpl
  implements Acl
{
  private Hashtable allowedUsersTable = new Hashtable(23);
  private Hashtable allowedGroupsTable = new Hashtable(23);
  private Hashtable deniedUsersTable = new Hashtable(23);
  private Hashtable deniedGroupsTable = new Hashtable(23);
  private String aclName = null;
  private Vector zeroSet = new Vector(1, 1);

  public AclImpl(Principal paramPrincipal, String paramString)
  {
    super(paramPrincipal);
    try
    {
      setName(paramPrincipal, paramString);
    }
    catch (Exception localException)
    {
    }
  }

  public void setName(Principal paramPrincipal, String paramString)
    throws NotOwnerException
  {
    if (!(isOwner(paramPrincipal)))
      throw new NotOwnerException();
    this.aclName = paramString;
  }

  public String getName()
  {
    return this.aclName;
  }

  public synchronized boolean addEntry(Principal paramPrincipal, AclEntry paramAclEntry)
    throws NotOwnerException
  {
    if (!(isOwner(paramPrincipal)))
      throw new NotOwnerException();
    Hashtable localHashtable = findTable(paramAclEntry);
    Principal localPrincipal = paramAclEntry.getPrincipal();
    if (localHashtable.get(localPrincipal) != null)
      return false;
    localHashtable.put(localPrincipal, paramAclEntry);
    return true;
  }

  public synchronized boolean removeEntry(Principal paramPrincipal, AclEntry paramAclEntry)
    throws NotOwnerException
  {
    if (!(isOwner(paramPrincipal)))
      throw new NotOwnerException();
    Hashtable localHashtable = findTable(paramAclEntry);
    Principal localPrincipal = paramAclEntry.getPrincipal();
    Object localObject = localHashtable.remove(localPrincipal);
    return (localObject != null);
  }

  public synchronized Enumeration getPermissions(Principal paramPrincipal)
  {
    Enumeration localEnumeration3 = subtract(getGroupPositive(paramPrincipal), getGroupNegative(paramPrincipal));
    Enumeration localEnumeration4 = subtract(getGroupNegative(paramPrincipal), getGroupPositive(paramPrincipal));
    Enumeration localEnumeration1 = subtract(getIndividualPositive(paramPrincipal), getIndividualNegative(paramPrincipal));
    Enumeration localEnumeration2 = subtract(getIndividualNegative(paramPrincipal), getIndividualPositive(paramPrincipal));
    Enumeration localEnumeration5 = subtract(localEnumeration3, localEnumeration2);
    Enumeration localEnumeration6 = union(localEnumeration1, localEnumeration5);
    localEnumeration1 = subtract(getIndividualPositive(paramPrincipal), getIndividualNegative(paramPrincipal));
    localEnumeration2 = subtract(getIndividualNegative(paramPrincipal), getIndividualPositive(paramPrincipal));
    localEnumeration5 = subtract(localEnumeration4, localEnumeration1);
    Enumeration localEnumeration7 = union(localEnumeration2, localEnumeration5);
    return subtract(localEnumeration6, localEnumeration7);
  }

  public boolean checkPermission(Principal paramPrincipal, Permission paramPermission)
  {
    Enumeration localEnumeration = getPermissions(paramPrincipal);
    while (localEnumeration.hasMoreElements())
    {
      Permission localPermission = (Permission)localEnumeration.nextElement();
      if (localPermission.equals(paramPermission))
        return true;
    }
    return false;
  }

  public synchronized Enumeration entries()
  {
    return new AclEnumerator(this, this.allowedUsersTable, this.allowedGroupsTable, this.deniedUsersTable, this.deniedGroupsTable);
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    Enumeration localEnumeration = entries();
    while (localEnumeration.hasMoreElements())
    {
      AclEntry localAclEntry = (AclEntry)localEnumeration.nextElement();
      localStringBuffer.append(localAclEntry.toString().trim());
      localStringBuffer.append("\n");
    }
    return localStringBuffer.toString();
  }

  private Hashtable findTable(AclEntry paramAclEntry)
  {
    Hashtable localHashtable = null;
    Principal localPrincipal = paramAclEntry.getPrincipal();
    if (localPrincipal instanceof Group)
      if (paramAclEntry.isNegative())
        localHashtable = this.deniedGroupsTable;
      else
        localHashtable = this.allowedGroupsTable;
    else if (paramAclEntry.isNegative())
      localHashtable = this.deniedUsersTable;
    else
      localHashtable = this.allowedUsersTable;
    return localHashtable;
  }

  private static Enumeration union(Enumeration paramEnumeration1, Enumeration paramEnumeration2)
  {
    Vector localVector = new Vector(20, 20);
    while (paramEnumeration1.hasMoreElements())
      localVector.addElement(paramEnumeration1.nextElement());
    while (paramEnumeration2.hasMoreElements())
    {
      Object localObject = paramEnumeration2.nextElement();
      if (!(localVector.contains(localObject)))
        localVector.addElement(localObject);
    }
    return localVector.elements();
  }

  private Enumeration subtract(Enumeration paramEnumeration1, Enumeration paramEnumeration2)
  {
    Vector localVector = new Vector(20, 20);
    while (paramEnumeration1.hasMoreElements())
      localVector.addElement(paramEnumeration1.nextElement());
    while (paramEnumeration2.hasMoreElements())
    {
      Object localObject = paramEnumeration2.nextElement();
      if (localVector.contains(localObject))
        localVector.removeElement(localObject);
    }
    return localVector.elements();
  }

  private Enumeration getGroupPositive(Principal paramPrincipal)
  {
    Enumeration localEnumeration1 = this.zeroSet.elements();
    Enumeration localEnumeration2 = this.allowedGroupsTable.keys();
    while (localEnumeration2.hasMoreElements())
    {
      Group localGroup = (Group)localEnumeration2.nextElement();
      if (localGroup.isMember(paramPrincipal))
      {
        AclEntry localAclEntry = (AclEntry)this.allowedGroupsTable.get(localGroup);
        localEnumeration1 = union(localAclEntry.permissions(), localEnumeration1);
      }
    }
    return localEnumeration1;
  }

  private Enumeration getGroupNegative(Principal paramPrincipal)
  {
    Enumeration localEnumeration1 = this.zeroSet.elements();
    Enumeration localEnumeration2 = this.deniedGroupsTable.keys();
    while (localEnumeration2.hasMoreElements())
    {
      Group localGroup = (Group)localEnumeration2.nextElement();
      if (localGroup.isMember(paramPrincipal))
      {
        AclEntry localAclEntry = (AclEntry)this.deniedGroupsTable.get(localGroup);
        localEnumeration1 = union(localAclEntry.permissions(), localEnumeration1);
      }
    }
    return localEnumeration1;
  }

  private Enumeration getIndividualPositive(Principal paramPrincipal)
  {
    Enumeration localEnumeration = this.zeroSet.elements();
    AclEntry localAclEntry = (AclEntry)this.allowedUsersTable.get(paramPrincipal);
    if (localAclEntry != null)
      localEnumeration = localAclEntry.permissions();
    return localEnumeration;
  }

  private Enumeration getIndividualNegative(Principal paramPrincipal)
  {
    Enumeration localEnumeration = this.zeroSet.elements();
    AclEntry localAclEntry = (AclEntry)this.deniedUsersTable.get(paramPrincipal);
    if (localAclEntry != null)
      localEnumeration = localAclEntry.permissions();
    return localEnumeration;
  }
}