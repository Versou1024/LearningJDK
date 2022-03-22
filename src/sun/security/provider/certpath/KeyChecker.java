package sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.PKIXExtensions;

class KeyChecker extends PKIXCertPathChecker
{
  private static final Debug debug = Debug.getInstance("certpath");
  private static final int keyCertSign = 5;
  private final int certPathLen;
  private CertSelector targetConstraints;
  private int remainingCerts;
  private static Set<String> supportedExts;

  KeyChecker(int paramInt, CertSelector paramCertSelector)
    throws CertPathValidatorException
  {
    this.certPathLen = paramInt;
    this.targetConstraints = paramCertSelector;
    init(false);
  }

  public void init(boolean paramBoolean)
    throws CertPathValidatorException
  {
    if (!(paramBoolean))
      this.remainingCerts = this.certPathLen;
    else
      throw new CertPathValidatorException("forward checking not supported");
  }

  public boolean isForwardCheckingSupported()
  {
    return false;
  }

  public Set<String> getSupportedExtensions()
  {
    if (supportedExts == null)
    {
      supportedExts = new HashSet();
      supportedExts.add(PKIXExtensions.KeyUsage_Id.toString());
      supportedExts.add(PKIXExtensions.ExtendedKeyUsage_Id.toString());
      supportedExts.add(PKIXExtensions.SubjectAlternativeName_Id.toString());
      supportedExts = Collections.unmodifiableSet(supportedExts);
    }
    return supportedExts;
  }

  public void check(Certificate paramCertificate, Collection<String> paramCollection)
    throws CertPathValidatorException
  {
    X509Certificate localX509Certificate = (X509Certificate)paramCertificate;
    this.remainingCerts -= 1;
    if (this.remainingCerts == 0)
    {
      if ((this.targetConstraints == null) || (this.targetConstraints.match(localX509Certificate)))
        break label56;
      throw new CertPathValidatorException("target certificate constraints check failed");
    }
    verifyCAKeyUsage(localX509Certificate);
    if ((paramCollection != null) && (!(paramCollection.isEmpty())))
    {
      label56: paramCollection.remove(PKIXExtensions.KeyUsage_Id.toString());
      paramCollection.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
      paramCollection.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
    }
  }

  static void verifyCAKeyUsage(X509Certificate paramX509Certificate)
    throws CertPathValidatorException
  {
    String str = "CA key usage";
    if (debug != null)
      debug.println("KeyChecker.verifyCAKeyUsage() ---checking " + str + "...");
    boolean[] arrayOfBoolean = paramX509Certificate.getKeyUsage();
    if (arrayOfBoolean == null)
      return;
    if (arrayOfBoolean[5] == 0)
      throw new CertPathValidatorException(str + " check failed: " + "keyCertSign bit is not set");
    if (debug != null)
      debug.println("KeyChecker.verifyCAKeyUsage() " + str + " verified.");
  }
}