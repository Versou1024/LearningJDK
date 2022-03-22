package sun.security.tools;

class DelegationPerm extends Perm
{
  public DelegationPerm()
  {
    super("DelegationPermission", "javax.security.auth.kerberos.DelegationPermission", new String[0], null);
  }
}