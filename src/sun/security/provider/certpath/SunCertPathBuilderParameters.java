package sun.security.provider.certpath;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertSelector;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.util.Set;

public class SunCertPathBuilderParameters extends PKIXBuilderParameters
{
  private boolean buildForward = true;

  public SunCertPathBuilderParameters(Set<TrustAnchor> paramSet, CertSelector paramCertSelector)
    throws InvalidAlgorithmParameterException
  {
    super(paramSet, paramCertSelector);
    setBuildForward(true);
  }

  public SunCertPathBuilderParameters(KeyStore paramKeyStore, CertSelector paramCertSelector)
    throws KeyStoreException, InvalidAlgorithmParameterException
  {
    super(paramKeyStore, paramCertSelector);
    setBuildForward(true);
  }

  public boolean getBuildForward()
  {
    return this.buildForward;
  }

  public void setBuildForward(boolean paramBoolean)
  {
    this.buildForward = paramBoolean;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("[\n");
    localStringBuffer.append(super.toString());
    localStringBuffer.append("  Build Forward Flag: " + String.valueOf(this.buildForward) + "\n");
    localStringBuffer.append("]\n");
    return localStringBuffer.toString();
  }
}