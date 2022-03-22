package sun.security.tools;

import java.util.ResourceBundle;

class SecurityPerm extends Perm
{
  public SecurityPerm()
  {
    super("SecurityPermission", "java.security.SecurityPermission", new String[] { "createAccessControlContext", "getDomainCombiner", "getPolicy", "setPolicy", "createPolicy.<" + PolicyTool.rb.getString("policy type") + ">", "getProperty.<" + PolicyTool.rb.getString("property name") + ">", "setProperty.<" + PolicyTool.rb.getString("property name") + ">", "insertProvider.<" + PolicyTool.rb.getString("provider name") + ">", "removeProvider.<" + PolicyTool.rb.getString("provider name") + ">", "clearProviderProperties.<" + PolicyTool.rb.getString("provider name") + ">", "putProviderProperty.<" + PolicyTool.rb.getString("provider name") + ">", "removeProviderProperty.<" + PolicyTool.rb.getString("provider name") + ">" }, null);
  }
}