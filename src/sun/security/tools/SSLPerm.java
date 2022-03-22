package sun.security.tools;

class SSLPerm extends Perm
{
  public SSLPerm()
  {
    super("SSLPermission", "javax.net.ssl.SSLPermission", new String[] { "setHostnameVerifier", "getSSLSessionContext" }, null);
  }
}