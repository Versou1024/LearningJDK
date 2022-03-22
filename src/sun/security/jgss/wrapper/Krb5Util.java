package sun.security.jgss.wrapper;

import javax.security.auth.kerberos.ServicePermission;
import org.ietf.jgss.GSSException;

class Krb5Util
{
  static String getTGSName(GSSNameElement paramGSSNameElement)
    throws GSSException
  {
    String str1 = paramGSSNameElement.getKrbName();
    int i = str1.indexOf("@");
    String str2 = str1.substring(i + 1);
    StringBuffer localStringBuffer = new StringBuffer("krbtgt/");
    localStringBuffer.append(str2).append('@').append(str2);
    return localStringBuffer.toString();
  }

  static void checkServicePermission(String paramString1, String paramString2)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      SunNativeProvider.debug("Checking ServicePermission(" + paramString1 + ", " + paramString2 + ")");
      ServicePermission localServicePermission = new ServicePermission(paramString1, paramString2);
      localSecurityManager.checkPermission(localServicePermission);
    }
  }
}