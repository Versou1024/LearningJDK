package sun.security.tools;

class MBeanSvrPerm extends Perm
{
  public MBeanSvrPerm()
  {
    super("MBeanServerPermission", "javax.management.MBeanServerPermission", new String[] { "createMBeanServer", "findMBeanServer", "newMBeanServer", "releaseMBeanServer" }, null);
  }
}