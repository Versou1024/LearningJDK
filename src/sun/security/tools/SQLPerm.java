package sun.security.tools;

class SQLPerm extends Perm
{
  public SQLPerm()
  {
    super("SQLPermission", "java.sql.SQLPermission", new String[] { "setLog" }, null);
  }
}