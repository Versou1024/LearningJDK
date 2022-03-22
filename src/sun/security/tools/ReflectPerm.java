package sun.security.tools;

class ReflectPerm extends Perm
{
  public ReflectPerm()
  {
    super("ReflectPermission", "java.lang.reflect.ReflectPermission", new String[] { "suppressAccessChecks" }, null);
  }
}