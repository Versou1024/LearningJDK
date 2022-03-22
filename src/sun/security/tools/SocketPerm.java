package sun.security.tools;

class SocketPerm extends Perm
{
  public SocketPerm()
  {
    super("SocketPermission", "java.net.SocketPermission", new String[0], new String[] { "accept", "connect", "listen", "resolve" });
  }
}