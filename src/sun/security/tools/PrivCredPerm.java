package sun.security.tools;

class PrivCredPerm extends Perm
{
  public PrivCredPerm()
  {
    super("PrivateCredentialPermission", "javax.security.auth.PrivateCredentialPermission", new String[0], new String[] { "read" });
  }
}