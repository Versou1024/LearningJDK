package sun.security.x509;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import sun.security.pkcs.PKCS10;

public final class CertAndKeyGen
{
  private SecureRandom prng;
  private String sigAlg;
  private KeyPairGenerator keyGen;
  private PublicKey publicKey;
  private PrivateKey privateKey;

  public CertAndKeyGen(String paramString1, String paramString2)
    throws NoSuchAlgorithmException
  {
    this.keyGen = KeyPairGenerator.getInstance(paramString1);
    this.sigAlg = paramString2;
  }

  public CertAndKeyGen(String paramString1, String paramString2, String paramString3)
    throws NoSuchAlgorithmException, NoSuchProviderException
  {
    if (paramString3 == null)
      this.keyGen = KeyPairGenerator.getInstance(paramString1);
    else
      try
      {
        this.keyGen = KeyPairGenerator.getInstance(paramString1, paramString3);
      }
      catch (Exception localException)
      {
        this.keyGen = KeyPairGenerator.getInstance(paramString1);
      }
    this.sigAlg = paramString2;
  }

  public void setRandom(SecureRandom paramSecureRandom)
  {
    this.prng = paramSecureRandom;
  }

  public void generate(int paramInt)
    throws InvalidKeyException
  {
    KeyPair localKeyPair;
    try
    {
      if (this.prng == null)
        this.prng = new SecureRandom();
      this.keyGen.initialize(paramInt, this.prng);
      localKeyPair = this.keyGen.generateKeyPair();
    }
    catch (Exception localException)
    {
      throw new IllegalArgumentException(localException.getMessage());
    }
    this.publicKey = localKeyPair.getPublic();
    this.privateKey = localKeyPair.getPrivate();
  }

  public X509Key getPublicKey()
  {
    if (!(this.publicKey instanceof X509Key))
      return null;
    return ((X509Key)this.publicKey);
  }

  public PrivateKey getPrivateKey()
  {
    return this.privateKey;
  }

  @Deprecated
  public X509Cert getSelfCert(X500Name paramX500Name, long paramLong)
    throws InvalidKeyException, SignatureException, NoSuchAlgorithmException
  {
    X509Certificate localX509Certificate;
    try
    {
      localX509Certificate = getSelfCertificate(paramX500Name, paramLong);
      return new X509Cert(localX509Certificate.getEncoded());
    }
    catch (CertificateException localCertificateException)
    {
      throw new SignatureException(localCertificateException.getMessage());
    }
    catch (NoSuchProviderException localNoSuchProviderException)
    {
      throw new NoSuchAlgorithmException(localNoSuchProviderException.getMessage());
    }
    catch (IOException localIOException)
    {
      throw new SignatureException(localIOException.getMessage());
    }
  }

  public X509Certificate getSelfCertificate(X500Name paramX500Name, Date paramDate, long paramLong)
    throws CertificateException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException
  {
    X500Signer localX500Signer;
    try
    {
      localX500Signer = getSigner(paramX500Name);
      Date localDate = new Date();
      localDate.setTime(paramDate.getTime() + paramLong * 1000L);
      CertificateValidity localCertificateValidity = new CertificateValidity(paramDate, localDate);
      X509CertInfo localX509CertInfo = new X509CertInfo();
      localX509CertInfo.set("version", new CertificateVersion(2));
      localX509CertInfo.set("serialNumber", new CertificateSerialNumber((int)(paramDate.getTime() / 1000L)));
      AlgorithmId localAlgorithmId = localX500Signer.getAlgorithmId();
      localX509CertInfo.set("algorithmID", new CertificateAlgorithmId(localAlgorithmId));
      localX509CertInfo.set("subject", new CertificateSubjectName(paramX500Name));
      localX509CertInfo.set("key", new CertificateX509Key(this.publicKey));
      localX509CertInfo.set("validity", localCertificateValidity);
      localX509CertInfo.set("issuer", new CertificateIssuerName(localX500Signer.getSigner()));
      if (System.getProperty("sun.security.internal.keytool.skid") != null)
      {
        CertificateExtensions localCertificateExtensions = new CertificateExtensions();
        localCertificateExtensions.set("SubjectKeyIdentifier", new SubjectKeyIdentifierExtension(new KeyIdentifier(this.publicKey).getIdentifier()));
        localX509CertInfo.set("extensions", localCertificateExtensions);
      }
      X509CertImpl localX509CertImpl = new X509CertImpl(localX509CertInfo);
      localX509CertImpl.sign(this.privateKey, this.sigAlg);
      return localX509CertImpl;
    }
    catch (IOException localIOException)
    {
      throw new CertificateEncodingException("getSelfCert: " + localIOException.getMessage());
    }
  }

  public X509Certificate getSelfCertificate(X500Name paramX500Name, long paramLong)
    throws CertificateException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException
  {
    return getSelfCertificate(paramX500Name, new Date(), paramLong);
  }

  public PKCS10 getCertRequest(X500Name paramX500Name)
    throws InvalidKeyException, SignatureException
  {
    PKCS10 localPKCS10 = new PKCS10(this.publicKey);
    try
    {
      localPKCS10.encodeAndSign(getSigner(paramX500Name));
    }
    catch (CertificateException localCertificateException)
    {
      throw new SignatureException(this.sigAlg + " CertificateException");
    }
    catch (IOException localIOException)
    {
      throw new SignatureException(this.sigAlg + " IOException");
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new SignatureException(this.sigAlg + " unavailable?");
    }
    return localPKCS10;
  }

  private X500Signer getSigner(X500Name paramX500Name)
    throws InvalidKeyException, NoSuchAlgorithmException
  {
    Signature localSignature = Signature.getInstance(this.sigAlg);
    localSignature.initSign(this.privateKey);
    return new X500Signer(localSignature, paramX500Name);
  }
}