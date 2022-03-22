package sun.security.x509;

import B;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.Certificate;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Date;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

@Deprecated
public class X509Cert
  implements Certificate, Serializable
{
  static final long serialVersionUID = -52595524744692374L;
  protected transient AlgorithmId algid;
  private transient byte[] rawCert;
  private transient byte[] signature;
  private transient byte[] signedCert;
  private transient X500Name subject;
  private transient PublicKey pubkey;
  private transient Date notafter;
  private transient Date notbefore;
  private transient int version;
  private transient BigInteger serialnum;
  private transient X500Name issuer;
  private transient AlgorithmId issuerSigAlg;
  private transient boolean parsed = false;

  public X509Cert()
  {
  }

  public X509Cert(byte[] paramArrayOfByte)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramArrayOfByte);
    parse(localDerValue);
    if (localDerValue.data.available() != 0)
      throw new CertParseError("garbage at end");
    this.signedCert = paramArrayOfByte;
  }

  public X509Cert(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramArrayOfByte, paramInt1, paramInt2);
    parse(localDerValue);
    if (localDerValue.data.available() != 0)
      throw new CertParseError("garbage at end");
    this.signedCert = new byte[paramInt2];
    System.arraycopy(paramArrayOfByte, paramInt1, this.signedCert, 0, paramInt2);
  }

  public X509Cert(DerValue paramDerValue)
    throws IOException
  {
    parse(paramDerValue);
    if (paramDerValue.data.available() != 0)
      throw new CertParseError("garbage at end");
    this.signedCert = paramDerValue.toByteArray();
  }

  public X509Cert(X500Name paramX500Name, X509Key paramX509Key, Date paramDate1, Date paramDate2)
    throws sun.security.x509.CertException
  {
    this.subject = paramX500Name;
    if (!(paramX509Key instanceof PublicKey))
      throw new sun.security.x509.CertException(9, "Doesn't implement PublicKey interface");
    this.pubkey = paramX509Key;
    this.notbefore = paramDate1;
    this.notafter = paramDate2;
    this.version = 0;
  }

  public void decode(InputStream paramInputStream)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramInputStream);
    parse(localDerValue);
    this.signedCert = localDerValue.toByteArray();
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    paramOutputStream.write(getSignedCert());
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof X509Cert)
      return equals((X509Cert)paramObject);
    return false;
  }

  public boolean equals(X509Cert paramX509Cert)
  {
    if (this == paramX509Cert)
      return true;
    if ((this.signedCert == null) || (paramX509Cert.signedCert == null))
      return false;
    if (this.signedCert.length != paramX509Cert.signedCert.length)
      return false;
    for (int i = 0; i < this.signedCert.length; ++i)
      if (this.signedCert[i] != paramX509Cert.signedCert[i])
        return false;
    return true;
  }

  public String getFormat()
  {
    return "X.509";
  }

  public Principal getGuarantor()
  {
    return getIssuerName();
  }

  public Principal getPrincipal()
  {
    return getSubjectName();
  }

  public void verify(PublicKey paramPublicKey)
    throws sun.security.x509.CertException
  {
    Date localDate = new Date();
    if (localDate.before(this.notbefore))
      throw new sun.security.x509.CertException(3);
    if (localDate.after(this.notafter))
      throw new sun.security.x509.CertException(4);
    if (this.signedCert == null)
      throw new sun.security.x509.CertException(1, "?? certificate is not signed yet ??");
    String str = null;
    try
    {
      Signature localSignature = null;
      str = this.issuerSigAlg.getName();
      localSignature = Signature.getInstance(str);
      localSignature.initVerify(paramPublicKey);
      localSignature.update(this.rawCert, 0, this.rawCert.length);
      if (!(localSignature.verify(this.signature)))
        throw new sun.security.x509.CertException(1, "Signature ... by <" + this.issuer + "> for <" + this.subject + ">");
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new sun.security.x509.CertException(1, "Unsupported signature algorithm (" + str + ")");
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new sun.security.x509.CertException(9, "Algorithm (" + str + ") rejected public key");
    }
    catch (SignatureException localSignatureException)
    {
      throw new sun.security.x509.CertException(1, "Signature by <" + this.issuer + "> for <" + this.subject + ">");
    }
  }

  public byte[] encodeAndSign(BigInteger paramBigInteger, X500Signer paramX500Signer)
    throws IOException, SignatureException
  {
    this.rawCert = null;
    this.version = 0;
    this.serialnum = paramBigInteger;
    this.issuer = paramX500Signer.getSigner();
    this.issuerSigAlg = paramX500Signer.getAlgorithmId();
    if ((this.subject == null) || (this.pubkey == null) || (this.notbefore == null) || (this.notafter == null))
      throw new IOException("not enough cert parameters");
    this.rawCert = DERencode();
    this.signedCert = sign(paramX500Signer, this.rawCert);
    return this.signedCert;
  }

  public X500Signer getSigner(AlgorithmId paramAlgorithmId, PrivateKey paramPrivateKey)
    throws NoSuchAlgorithmException, InvalidKeyException
  {
    String str;
    if (paramPrivateKey instanceof Key)
    {
      PrivateKey localPrivateKey = paramPrivateKey;
      str = localPrivateKey.getAlgorithm();
    }
    else
    {
      throw new InvalidKeyException("private key not a key!");
    }
    Signature localSignature = Signature.getInstance(paramAlgorithmId.getName());
    if (!(this.pubkey.getAlgorithm().equals(str)))
      throw new InvalidKeyException("Private key algorithm " + str + " incompatible with certificate " + this.pubkey.getAlgorithm());
    localSignature.initSign(paramPrivateKey);
    return new X500Signer(localSignature, this.subject);
  }

  public Signature getVerifier(String paramString)
    throws NoSuchAlgorithmException, InvalidKeyException
  {
    Signature localSignature = Signature.getInstance(paramString);
    localSignature.initVerify(this.pubkey);
    return localSignature;
  }

  public byte[] getSignedCert()
  {
    return ((byte[])(byte[])this.signedCert.clone());
  }

  public BigInteger getSerialNumber()
  {
    return this.serialnum;
  }

  public X500Name getSubjectName()
  {
    return this.subject;
  }

  public X500Name getIssuerName()
  {
    return this.issuer;
  }

  public AlgorithmId getIssuerAlgorithmId()
  {
    return this.issuerSigAlg;
  }

  public Date getNotBefore()
  {
    return new Date(this.notbefore.getTime());
  }

  public Date getNotAfter()
  {
    return new Date(this.notafter.getTime());
  }

  public PublicKey getPublicKey()
  {
    return this.pubkey;
  }

  public int getVersion()
  {
    return this.version;
  }

  public int hashCode()
  {
    int i = 0;
    for (int j = 0; j < this.signedCert.length; ++j)
      i += this.signedCert[j] * j;
    return i;
  }

  public String toString()
  {
    if ((this.subject == null) || (this.pubkey == null) || (this.notbefore == null) || (this.notafter == null) || (this.issuer == null) || (this.issuerSigAlg == null) || (this.serialnum == null))
      throw new NullPointerException("X.509 cert is incomplete");
    String str = "  X.509v" + (this.version + 1) + " certificate,\n";
    str = str + "  Subject is " + this.subject + "\n";
    str = str + "  Key:  " + this.pubkey;
    str = str + "  Validity <" + this.notbefore + "> until <" + this.notafter + ">\n";
    str = str + "  Issuer is " + this.issuer + "\n";
    str = str + "  Issuer signature used " + this.issuerSigAlg.toString() + "\n";
    str = str + "  Serial number = " + Debug.toHexString(this.serialnum) + "\n";
    return "[\n" + str + "]";
  }

  public String toString(boolean paramBoolean)
  {
    return toString();
  }

  private void parse(DerValue paramDerValue)
    throws IOException
  {
    if (this.parsed == true)
      throw new IOException("Certificate already parsed");
    DerValue[] arrayOfDerValue = new DerValue[3];
    arrayOfDerValue[0] = paramDerValue.data.getDerValue();
    arrayOfDerValue[1] = paramDerValue.data.getDerValue();
    arrayOfDerValue[2] = paramDerValue.data.getDerValue();
    if (paramDerValue.data.available() != 0)
      throw new CertParseError("signed overrun, bytes = " + paramDerValue.data.available());
    if (arrayOfDerValue[0].tag != 48)
      throw new CertParseError("signed fields invalid");
    this.rawCert = arrayOfDerValue[0].toByteArray();
    this.issuerSigAlg = AlgorithmId.parse(arrayOfDerValue[1]);
    this.signature = arrayOfDerValue[2].getBitString();
    if (arrayOfDerValue[1].data.available() != 0)
      throw new CertParseError("algid field overrun");
    if (arrayOfDerValue[2].data.available() != 0)
      throw new CertParseError("signed fields overrun");
    DerInputStream localDerInputStream = arrayOfDerValue[0].data;
    this.version = 0;
    DerValue localDerValue = localDerInputStream.getDerValue();
    if ((localDerValue.isConstructed()) && (localDerValue.isContextSpecific()))
    {
      this.version = localDerValue.data.getInteger();
      if (localDerValue.data.available() != 0)
        throw new IOException("X.509 version, bad format");
      localDerValue = localDerInputStream.getDerValue();
    }
    this.serialnum = localDerValue.getBigInteger();
    localDerValue = localDerInputStream.getDerValue();
    AlgorithmId localAlgorithmId = AlgorithmId.parse(localDerValue);
    if (!(localAlgorithmId.equals(this.issuerSigAlg)))
      throw new CertParseError("CA Algorithm mismatch!");
    this.algid = localAlgorithmId;
    this.issuer = new X500Name(localDerInputStream);
    localDerValue = localDerInputStream.getDerValue();
    if (localDerValue.tag != 48)
      throw new CertParseError("corrupt validity field");
    this.notbefore = localDerValue.data.getUTCTime();
    this.notafter = localDerValue.data.getUTCTime();
    if (localDerValue.data.available() != 0)
      throw new CertParseError("excess validity data");
    this.subject = new X500Name(localDerInputStream);
    localDerValue = localDerInputStream.getDerValue();
    this.pubkey = X509Key.parse(localDerValue);
    if (localDerInputStream.available() != 0);
    this.parsed = true;
  }

  private byte[] DERencode()
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    encode(localDerOutputStream);
    return localDerOutputStream.toByteArray();
  }

  private void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(this.serialnum);
    this.issuerSigAlg.encode(localDerOutputStream1);
    this.issuer.encode(localDerOutputStream1);
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putUTCTime(this.notbefore);
    localDerOutputStream2.putUTCTime(this.notafter);
    localDerOutputStream1.write(48, localDerOutputStream2);
    this.subject.encode(localDerOutputStream1);
    localDerOutputStream1.write(this.pubkey.getEncoded());
    paramDerOutputStream.write(48, localDerOutputStream1);
  }

  private byte[] sign(X500Signer paramX500Signer, byte[] paramArrayOfByte)
    throws IOException, SignatureException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(paramArrayOfByte);
    paramX500Signer.getAlgorithmId().encode(localDerOutputStream2);
    paramX500Signer.update(paramArrayOfByte, 0, paramArrayOfByte.length);
    this.signature = paramX500Signer.sign();
    localDerOutputStream2.putBitString(this.signature);
    localDerOutputStream1.write(48, localDerOutputStream2);
    return localDerOutputStream1.toByteArray();
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    encode(paramObjectOutputStream);
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException
  {
    decode(paramObjectInputStream);
  }
}