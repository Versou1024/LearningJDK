package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.X509Factory;
import sun.security.util.Cache;
import sun.security.util.Cache.EqualByteArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.X509CertImpl;

public class X509CertificatePair
{
  private static final byte TAG_FORWARD = 0;
  private static final byte TAG_REVERSE = 1;
  private X509Certificate forward;
  private X509Certificate reverse;
  private byte[] encoded;
  private static final Cache cache = Cache.newSoftMemoryCache(750);

  public X509CertificatePair()
  {
  }

  public X509CertificatePair(X509Certificate paramX509Certificate1, X509Certificate paramX509Certificate2)
    throws CertificateException
  {
    if ((paramX509Certificate1 == null) && (paramX509Certificate2 == null))
      throw new CertificateException("at least one of certificate pair must be non-null");
    this.forward = paramX509Certificate1;
    this.reverse = paramX509Certificate2;
    checkPair();
  }

  private X509CertificatePair(byte[] paramArrayOfByte)
    throws CertificateException
  {
    try
    {
      parse(new DerValue(paramArrayOfByte));
      this.encoded = paramArrayOfByte;
    }
    catch (IOException localIOException)
    {
      throw new CertificateException(localIOException.toString());
    }
    checkPair();
  }

  public static synchronized void clearCache()
  {
    cache.clear();
  }

  public static synchronized X509CertificatePair generateCertificatePair(byte[] paramArrayOfByte)
    throws CertificateException
  {
    Cache.EqualByteArray localEqualByteArray = new Cache.EqualByteArray(paramArrayOfByte);
    X509CertificatePair localX509CertificatePair = (X509CertificatePair)cache.get(localEqualByteArray);
    if (localX509CertificatePair != null)
      return localX509CertificatePair;
    localX509CertificatePair = new X509CertificatePair(paramArrayOfByte);
    localEqualByteArray = new Cache.EqualByteArray(localX509CertificatePair.encoded);
    cache.put(localEqualByteArray, localX509CertificatePair);
    return localX509CertificatePair;
  }

  public void setForward(X509Certificate paramX509Certificate)
    throws CertificateException
  {
    checkPair();
    this.forward = paramX509Certificate;
  }

  public void setReverse(X509Certificate paramX509Certificate)
    throws CertificateException
  {
    checkPair();
    this.reverse = paramX509Certificate;
  }

  public X509Certificate getForward()
  {
    return this.forward;
  }

  public X509Certificate getReverse()
  {
    return this.reverse;
  }

  public byte[] getEncoded()
    throws CertificateEncodingException
  {
    try
    {
      if (this.encoded == null)
      {
        DerOutputStream localDerOutputStream = new DerOutputStream();
        emit(localDerOutputStream);
        this.encoded = localDerOutputStream.toByteArray();
      }
    }
    catch (IOException localIOException)
    {
      throw new CertificateEncodingException(localIOException.toString());
    }
    return this.encoded;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("X.509 Certificate Pair: [\n");
    if (this.forward != null)
      localStringBuffer.append("  Forward: " + this.forward + "\n");
    if (this.reverse != null)
      localStringBuffer.append("  Reverse: " + this.reverse + "\n");
    localStringBuffer.append("]");
    return localStringBuffer.toString();
  }

  private void parse(DerValue paramDerValue)
    throws IOException, CertificateException
  {
    if (paramDerValue.tag != 48)
      throw new IOException("Sequence tag missing for X509CertificatePair");
    while ((paramDerValue.data != null) && (paramDerValue.data.available() != 0))
    {
      DerValue localDerValue = paramDerValue.data.getDerValue();
      int i = (short)(byte)(localDerValue.tag & 0x1F);
      switch (i)
      {
      case 0:
        if ((localDerValue.isContextSpecific()) && (localDerValue.isConstructed()))
        {
          if (this.forward != null)
            throw new IOException("Duplicate forward certificate in X509CertificatePair");
          localDerValue = localDerValue.data.getDerValue();
          this.forward = X509Factory.intern(new X509CertImpl(localDerValue.toByteArray()));
        }
        break;
      case 1:
        if ((localDerValue.isContextSpecific()) && (localDerValue.isConstructed()))
        {
          if (this.reverse != null)
            throw new IOException("Duplicate reverse certificate in X509CertificatePair");
          localDerValue = localDerValue.data.getDerValue();
          this.reverse = X509Factory.intern(new X509CertImpl(localDerValue.toByteArray()));
        }
        break;
      default:
        throw new IOException("Invalid encoding of X509CertificatePair");
      }
    }
    if ((this.forward == null) && (this.reverse == null))
      throw new CertificateException("at least one of certificate pair must be non-null");
  }

  private void emit(DerOutputStream paramDerOutputStream)
    throws IOException, CertificateEncodingException
  {
    DerOutputStream localDerOutputStream2;
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    if (this.forward != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putDerValue(new DerValue(this.forward.getEncoded()));
      localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    }
    if (this.reverse != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putDerValue(new DerValue(this.reverse.getEncoded()));
      localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    }
    paramDerOutputStream.write(48, localDerOutputStream1);
  }

  private void checkPair()
    throws CertificateException
  {
    if ((this.forward == null) || (this.reverse == null))
      return;
    X500Principal localX500Principal1 = this.forward.getSubjectX500Principal();
    X500Principal localX500Principal2 = this.forward.getIssuerX500Principal();
    X500Principal localX500Principal3 = this.reverse.getSubjectX500Principal();
    X500Principal localX500Principal4 = this.reverse.getIssuerX500Principal();
    if ((!(localX500Principal2.equals(localX500Principal3))) || (!(localX500Principal4.equals(localX500Principal1))))
      throw new CertificateException("subject and issuer names in forward and reverse certificates do not match");
    try
    {
      PublicKey localPublicKey = this.reverse.getPublicKey();
      if ((!(localPublicKey instanceof DSAPublicKey)) || (((DSAPublicKey)localPublicKey).getParams() != null))
        this.forward.verify(localPublicKey);
      localPublicKey = this.forward.getPublicKey();
      if ((!(localPublicKey instanceof DSAPublicKey)) || (((DSAPublicKey)localPublicKey).getParams() != null))
        this.reverse.verify(localPublicKey);
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw new CertificateException("invalid signature: " + localGeneralSecurityException.getMessage());
    }
  }
}