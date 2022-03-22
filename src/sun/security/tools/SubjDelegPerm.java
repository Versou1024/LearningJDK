package sun.security.tools;

class SubjDelegPerm extends Perm
{
  public SubjDelegPerm()
  {
    super("SubjectDelegationPermission", "javax.management.remote.SubjectDelegationPermission", new String[0], null);
  }
}