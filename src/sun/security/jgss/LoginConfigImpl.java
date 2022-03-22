package sun.security.jgss;

import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import org.ietf.jgss.Oid;
import sun.security.util.Debug;

public class LoginConfigImpl extends Configuration
{
  Configuration config;
  private final int caller;
  private final String mechName;
  private static final Debug debug = Debug.getInstance("gssloginconfig", "\t[GSS LoginConfigImpl]");

  public LoginConfigImpl(int paramInt, Oid paramOid)
  {
    this.caller = paramInt;
    if (paramOid.equals(GSSUtil.GSS_KRB5_MECH_OID))
      this.mechName = "krb5";
    else
      throw new IllegalArgumentException(paramOid.toString() + " not supported");
    this.config = ((Configuration)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Configuration run()
      {
        return Configuration.getConfiguration();
      }
    }));
  }

  public AppConfigurationEntry[] getAppConfigurationEntry(String paramString)
  {
    AppConfigurationEntry[] arrayOfAppConfigurationEntry = null;
    if ("OTHER".equalsIgnoreCase(paramString))
      return null;
    String[] arrayOfString1 = null;
    if ("krb5".equals(this.mechName));
    switch (this.caller)
    {
    case 1:
      arrayOfString1 = { "com.sun.security.jgss.krb5.initiate", "com.sun.security.jgss.initiate" };
      break;
    case 2:
      arrayOfString1 = { "com.sun.security.jgss.krb5.accept", "com.sun.security.jgss.accept" };
      break;
    case 3:
      arrayOfString1 = { "com.sun.security.jgss.krb5.initiate", "com.sun.net.ssl.client" };
      break;
    case 4:
      arrayOfString1 = { "com.sun.security.jgss.krb5.accept", "com.sun.net.ssl.server" };
      break;
    case 5:
      arrayOfString1 = { "com.sun.security.jgss.krb5.initiate" };
      break;
    case -1:
      throw new AssertionError("caller cannot be unknown");
    case 0:
    default:
      throw new AssertionError("caller not defined");
      throw new IllegalArgumentException(this.mechName + " not supported");
    }
    String[] arrayOfString2 = arrayOfString1;
    int i = arrayOfString2.length;
    for (int j = 0; j < i; ++j)
    {
      String str = arrayOfString2[j];
      if (debug != null)
        debug.println("Trying " + str);
      arrayOfAppConfigurationEntry = this.config.getAppConfigurationEntry(str);
      if (arrayOfAppConfigurationEntry != null)
        break;
    }
    return arrayOfAppConfigurationEntry;
  }
}