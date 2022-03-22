package sun.security.tools;

class LogPerm extends Perm
{
  public LogPerm()
  {
    super("LoggingPermission", "java.util.logging.LoggingPermission", new String[] { "control" }, null);
  }
}