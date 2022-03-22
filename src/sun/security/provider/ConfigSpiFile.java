package sun.security.provider;

import com.sun.security.auth.login.ConfigFile;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.URIParameter;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration.Parameters;
import javax.security.auth.login.ConfigurationSpi;

public final class ConfigSpiFile extends ConfigurationSpi
{
  private ConfigFile cf;

  public ConfigSpiFile(Configuration.Parameters paramParameters)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedAction(this, paramParameters)
      {
        public Object run()
        {
          if (this.val$params == null)
          {
            ConfigSpiFile.access$002(this.this$0, new ConfigFile());
          }
          else
          {
            if (!(this.val$params instanceof URIParameter))
              throw new IllegalArgumentException("Unrecognized parameter: " + this.val$params);
            URIParameter localURIParameter = (URIParameter)this.val$params;
            ConfigSpiFile.access$002(this.this$0, new ConfigFile(localURIParameter.getURI()));
          }
          return null;
        }
      });
    }
    catch (SecurityException localSecurityException)
    {
      Throwable localThrowable = localSecurityException.getCause();
      if ((localThrowable != null) && (localThrowable instanceof IOException))
        throw ((IOException)localThrowable);
      throw localSecurityException;
    }
  }

  protected AppConfigurationEntry[] engineGetAppConfigurationEntry(String paramString)
  {
    return this.cf.getAppConfigurationEntry(paramString);
  }

  protected void engineRefresh()
  {
    this.cf.refresh();
  }
}