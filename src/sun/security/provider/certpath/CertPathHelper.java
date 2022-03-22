package sun.security.provider.certpath;

import java.security.cert.TrustAnchor;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.util.Collection;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.x509.GeneralNameInterface;

public abstract class CertPathHelper
{
  protected static CertPathHelper instance;

  protected abstract void implSetSubject(X509CertSelector paramX509CertSelector, X500Principal paramX500Principal);

  protected abstract X500Principal implGetSubject(X509CertSelector paramX509CertSelector);

  protected abstract void implSetIssuer(X509CertSelector paramX509CertSelector, X500Principal paramX500Principal);

  protected abstract X500Principal implGetIssuer(X509CertSelector paramX509CertSelector);

  protected abstract X500Principal implGetCA(TrustAnchor paramTrustAnchor);

  protected abstract void implSetPathToNames(X509CertSelector paramX509CertSelector, Set<GeneralNameInterface> paramSet);

  protected abstract void implAddIssuer(X509CRLSelector paramX509CRLSelector, X500Principal paramX500Principal);

  protected abstract Collection<X500Principal> implGetIssuers(X509CRLSelector paramX509CRLSelector);

  static void setSubject(X509CertSelector paramX509CertSelector, X500Principal paramX500Principal)
  {
    instance.implSetSubject(paramX509CertSelector, paramX500Principal);
  }

  static X500Principal getSubject(X509CertSelector paramX509CertSelector)
  {
    return instance.implGetSubject(paramX509CertSelector);
  }

  static void setIssuer(X509CertSelector paramX509CertSelector, X500Principal paramX500Principal)
  {
    instance.implSetIssuer(paramX509CertSelector, paramX500Principal);
  }

  static X500Principal getIssuer(X509CertSelector paramX509CertSelector)
  {
    return instance.implGetIssuer(paramX509CertSelector);
  }

  static X500Principal getCA(TrustAnchor paramTrustAnchor)
  {
    return instance.implGetCA(paramTrustAnchor);
  }

  static void setPathToNames(X509CertSelector paramX509CertSelector, Set<GeneralNameInterface> paramSet)
  {
    instance.implSetPathToNames(paramX509CertSelector, paramSet);
  }

  static void addIssuer(X509CRLSelector paramX509CRLSelector, X500Principal paramX500Principal)
  {
    instance.implAddIssuer(paramX509CRLSelector, paramX500Principal);
  }

  static Collection<X500Principal> getIssuers(X509CRLSelector paramX509CRLSelector)
  {
    return instance.implGetIssuers(paramX509CRLSelector);
  }
}