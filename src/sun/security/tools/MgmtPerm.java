package sun.security.tools;

class MgmtPerm extends Perm
{
  public MgmtPerm()
  {
    super("ManagementPermission", "java.lang.management.ManagementPermission", new String[] { "control", "monitor" }, null);
  }
}