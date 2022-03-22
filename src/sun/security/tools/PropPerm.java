package sun.security.tools;

class PropPerm extends Perm
{
  public PropPerm()
  {
    super("PropertyPermission", "java.util.PropertyPermission", new String[0], new String[] { "read", "write" });
  }
}