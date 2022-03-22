package sun.security.provider.certpath;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.x509.X500Name;

class BasicChecker extends PKIXCertPathChecker
{
  private static final Debug debug = Debug.getInstance("certpath");
  private final PublicKey trustedPubKey;
  private final X500Principal caName;
  private final Date testDate;
  private final String sigProvider;
  private final boolean sigOnly;
  private X500Principal prevSubject;
  private PublicKey prevPubKey;

  BasicChecker(PublicKey paramPublicKey, X500Principal paramX500Principal, Date paramDate, String paramString, boolean paramBoolean)
    throws CertPathValidatorException
  {
    this.trustedPubKey = paramPublicKey;
    this.caName = paramX500Principal;
    this.testDate = paramDate;
    this.sigProvider = paramString;
    this.sigOnly = paramBoolean;
    init(false);
  }

  public void init(boolean paramBoolean)
    throws CertPathValidatorException
  {
    if (!(paramBoolean))
    {
      this.prevPubKey = this.trustedPubKey;
      this.prevSubject = this.caName;
    }
    else
    {
      throw new CertPathValidatorException("forward checking not supported");
    }
  }

  public boolean isForwardCheckingSupported()
  {
    return false;
  }

  public Set<String> getSupportedExtensions()
  {
    return null;
  }

  public void check(Certificate paramCertificate, Collection<String> paramCollection)
    throws CertPathValidatorException
  {
    X509Certificate localX509Certificate = (X509Certificate)paramCertificate;
    if (!(this.sigOnly))
    {
      verifyTimestamp(localX509Certificate, this.testDate);
      verifyNameChaining(localX509Certificate, this.prevSubject);
    }
    verifySignature(localX509Certificate, this.prevPubKey, this.sigProvider);
    updateState(localX509Certificate);
  }

  private void verifySignature(X509Certificate paramX509Certificate, PublicKey paramPublicKey, String paramString)
    throws CertPathValidatorException
  {
    String str = "signature";
    if (debug != null)
      debug.println("---checking " + str + "...");
    try
    {
      paramX509Certificate.verify(paramPublicKey, paramString);
    }
    catch (Exception localException)
    {
      if (debug != null)
      {
        debug.println(localException.getMessage());
        localException.printStackTrace();
      }
      throw new CertPathValidatorException(str + " check failed", localException);
    }
    if (debug != null)
      debug.println(str + " verified.");
  }

  private void verifyTimestamp(X509Certificate paramX509Certificate, Date paramDate)
    throws CertPathValidatorException
  {
    String str = "timestamp";
    if (debug != null)
      debug.println("---checking " + str + ":" + paramDate.toString() + "...");
    try
    {
      paramX509Certificate.checkValidity(paramDate);
    }
    catch (Exception localException)
    {
      if (debug != null)
      {
        debug.println(localException.getMessage());
        localException.printStackTrace();
      }
      throw new CertPathValidatorException(str + " check failed", localException);
    }
    if (debug != null)
      debug.println(str + " verified.");
  }

  private void verifyNameChaining(X509Certificate paramX509Certificate, X500Principal paramX500Principal)
    throws CertPathValidatorException
  {
    if (paramX500Principal != null)
    {
      String str = "subject/issuer name chaining";
      if (debug != null)
        debug.println("---checking " + str + "...");
      X500Principal localX500Principal = paramX509Certificate.getIssuerX500Principal();
      if (X500Name.asX500Name(localX500Principal).isEmpty())
        throw new CertPathValidatorException(str + " check failed: " + "empty/null issuer DN in certificate is invalid");
      if (!(localX500Principal.equals(paramX500Principal)))
        throw new CertPathValidatorException(str + " check failed");
      if (debug != null)
        debug.println(str + " verified.");
    }
  }

  private void updateState(X509Certificate paramX509Certificate)
    throws CertPathValidatorException
  {
    PublicKey localPublicKey = paramX509Certificate.getPublicKey();
    if (debug != null)
      debug.println("BasicChecker.updateState issuer: " + paramX509Certificate.getIssuerX500Principal().toString() + "; subject: " + paramX509Certificate.getSubjectX500Principal() + "; serial#: " + paramX509Certificate.getSerialNumber().toString());
    if ((localPublicKey instanceof DSAPublicKey) && (((DSAPublicKey)localPublicKey).getParams() == null))
    {
      localPublicKey = makeInheritedParamsKey(localPublicKey, this.prevPubKey);
      if (debug != null)
        debug.println("BasicChecker.updateState Made key with inherited params");
    }
    this.prevPubKey = localPublicKey;
    this.prevSubject = paramX509Certificate.getSubjectX500Principal();
  }

  static PublicKey makeInheritedParamsKey(PublicKey paramPublicKey1, PublicKey paramPublicKey2)
    throws CertPathValidatorException
  {
    PublicKey localPublicKey;
    if ((!(paramPublicKey1 instanceof DSAPublicKey)) || (!(paramPublicKey2 instanceof DSAPublicKey)))
      throw new CertPathValidatorException("Input key is not appropriate type for inheriting parameters");
    DSAParams localDSAParams = ((DSAPublicKey)paramPublicKey2).getParams();
    if (localDSAParams == null)
      throw new CertPathValidatorException("Key parameters missing");
    try
    {
      BigInteger localBigInteger = ((DSAPublicKey)paramPublicKey1).getY();
      KeyFactory localKeyFactory = KeyFactory.getInstance("DSA");
      DSAPublicKeySpec localDSAPublicKeySpec = new DSAPublicKeySpec(localBigInteger, localDSAParams.getP(), localDSAParams.getQ(), localDSAParams.getG());
      localPublicKey = localKeyFactory.generatePublic(localDSAPublicKeySpec);
    }
    catch (Exception localException)
    {
      throw new CertPathValidatorException("Unable to generate key with inherited parameters: " + localException.getMessage(), localException);
    }
    return localPublicKey;
  }

  PublicKey getPublicKey()
  {
    return this.prevPubKey;
  }
}