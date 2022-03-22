package sun.security.tools;

class MBeanTrustPerm extends Perm
{
  public MBeanTrustPerm()
  {
    super("MBeanTrustPermission", "javax.management.MBeanTrustPermission", new String[] { "register" }, null);
  }
}