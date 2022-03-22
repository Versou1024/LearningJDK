package sun.security.tools;

import java.util.ResourceBundle;

class AuthPerm extends Perm
{
  public AuthPerm()
  {
    super("AuthPermission", "javax.security.auth.AuthPermission", new String[] { "doAs", "doAsPrivileged", "getSubject", "getSubjectFromDomainCombiner", "setReadOnly", "modifyPrincipals", "modifyPublicCredentials", "modifyPrivateCredentials", "refreshCredential", "destroyCredential", "createLoginContext.<" + PolicyTool.rb.getString("name") + ">", "getLoginConfiguration", "setLoginConfiguration", "createLoginConfiguration.<" + PolicyTool.rb.getString("configuration type") + ">", "refreshLoginConfiguration" }, null);
  }
}