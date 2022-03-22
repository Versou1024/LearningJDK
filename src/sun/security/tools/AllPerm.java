package sun.security.tools;

class AllPerm extends Perm
{
  public AllPerm()
  {
    super("AllPermission", "java.security.AllPermission", null, null);
  }
}