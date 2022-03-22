package sun.security.acl;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Vector;

public class GroupImpl
  implements Group
{
  private Vector groupMembers = new Vector(50, 100);
  private String group;

  public GroupImpl(String paramString)
  {
    this.group = paramString;
  }

  public boolean addMember(Principal paramPrincipal)
  {
    if (this.groupMembers.contains(paramPrincipal))
      return false;
    if (this.group.equals(paramPrincipal.toString()))
      throw new IllegalArgumentException();
    this.groupMembers.addElement(paramPrincipal);
    return true;
  }

  public boolean removeMember(Principal paramPrincipal)
  {
    return this.groupMembers.removeElement(paramPrincipal);
  }

  public Enumeration members()
  {
    return this.groupMembers.elements();
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof Group))
      return false;
    Group localGroup = (Group)paramObject;
    return this.group.equals(localGroup.toString());
  }

  public boolean equals(Group paramGroup)
  {
    return equals(paramGroup);
  }

  public String toString()
  {
    return this.group;
  }

  public int hashCode()
  {
    return this.group.hashCode();
  }

  public boolean isMember(Principal paramPrincipal)
  {
    if (this.groupMembers.contains(paramPrincipal))
      return true;
    Vector localVector = new Vector(10);
    return isMemberRecurse(paramPrincipal, localVector);
  }

  public String getName()
  {
    return this.group;
  }

  boolean isMemberRecurse(Principal paramPrincipal, Vector paramVector)
  {
    Enumeration localEnumeration = members();
    while (localEnumeration.hasMoreElements())
    {
      Object localObject;
      boolean bool = false;
      Principal localPrincipal = (Principal)localEnumeration.nextElement();
      if (localPrincipal.equals(paramPrincipal))
        return true;
      if (localPrincipal instanceof GroupImpl)
      {
        localObject = (GroupImpl)localPrincipal;
        paramVector.addElement(this);
        if (!(paramVector.contains(localObject)))
          bool = ((GroupImpl)localObject).isMemberRecurse(paramPrincipal, paramVector);
      }
      else if (localPrincipal instanceof Group)
      {
        localObject = (Group)localPrincipal;
        if (!(paramVector.contains(localObject)))
          bool = ((Group)localObject).isMember(paramPrincipal);
      }
      if (bool)
        return bool;
    }
    return false;
  }
}