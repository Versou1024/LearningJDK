package sun.security.provider;

import java.net.MalformedURLException;
import java.net.URI;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy.Parameters;
import java.security.PolicySpi;
import java.security.ProtectionDomain;
import java.security.URIParameter;

public final class PolicySpiFile extends PolicySpi
{
  private PolicyFile pf;

  public PolicySpiFile(Policy.Parameters paramParameters)
  {
    if (paramParameters == null)
    {
      this.pf = new PolicyFile();
    }
    else
    {
      if (!(paramParameters instanceof URIParameter))
        throw new IllegalArgumentException("Unrecognized policy parameter: " + paramParameters);
      URIParameter localURIParameter = (URIParameter)paramParameters;
      try
      {
        this.pf = new PolicyFile(localURIParameter.getURI().toURL());
      }
      catch (MalformedURLException localMalformedURLException)
      {
        throw new IllegalArgumentException("Invalid URIParameter", localMalformedURLException);
      }
    }
  }

  protected PermissionCollection engineGetPermissions(CodeSource paramCodeSource)
  {
    return this.pf.getPermissions(paramCodeSource);
  }

  protected PermissionCollection engineGetPermissions(ProtectionDomain paramProtectionDomain)
  {
    return this.pf.getPermissions(paramProtectionDomain);
  }

  protected boolean engineImplies(ProtectionDomain paramProtectionDomain, Permission paramPermission)
  {
    return this.pf.implies(paramProtectionDomain, paramPermission);
  }

  protected void engineRefresh()
  {
    this.pf.refresh();
  }
}