package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

abstract interface State extends Cloneable
{
  public abstract void updateState(X509Certificate paramX509Certificate)
    throws CertificateException, IOException, CertPathValidatorException;

  public abstract Object clone();

  public abstract boolean isInitial();

  public abstract boolean keyParamsNeeded();
}