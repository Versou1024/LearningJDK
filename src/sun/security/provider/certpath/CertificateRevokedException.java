package sun.security.provider.certpath;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;

final class CertificateRevokedException extends CertPathValidatorException
{
  CertificateRevokedException(String paramString, CertPath paramCertPath, int paramInt)
  {
    super(paramString, null, paramCertPath, paramInt);
  }
}