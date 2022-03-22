package sun.security.pkcs;

import B;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import sun.misc.BASE64Encoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;
import sun.security.x509.X500Signer;
import sun.security.x509.X509Key;

public class PKCS10
{
  private X500Name subject;
  private PublicKey subjectPublicKeyInfo;
  private PKCS10Attributes attributeSet;
  private byte[] encoded;

  public PKCS10(PublicKey paramPublicKey)
  {
    this.subjectPublicKeyInfo = paramPublicKey;
    this.attributeSet = new PKCS10Attributes();
  }

  public PKCS10(PublicKey paramPublicKey, PKCS10Attributes paramPKCS10Attributes)
  {
    this.subjectPublicKeyInfo = paramPublicKey;
    this.attributeSet = paramPKCS10Attributes;
  }

  public PKCS10(byte[] paramArrayOfByte)
    throws IOException, SignatureException, NoSuchAlgorithmException
  {
    this.encoded = paramArrayOfByte;
    DerInputStream localDerInputStream = new DerInputStream(paramArrayOfByte);
    DerValue[] arrayOfDerValue = localDerInputStream.getSequence(3);
    if (arrayOfDerValue.length != 3)
      throw new IllegalArgumentException("not a PKCS #10 request");
    paramArrayOfByte = arrayOfDerValue[0].toByteArray();
    AlgorithmId localAlgorithmId = AlgorithmId.parse(arrayOfDerValue[1]);
    byte[] arrayOfByte = arrayOfDerValue[2].getBitString();
    BigInteger localBigInteger = arrayOfDerValue[0].data.getBigInteger();
    if (!(localBigInteger.equals(BigInteger.ZERO)))
      throw new IllegalArgumentException("not PKCS #10 v1");
    this.subject = new X500Name(arrayOfDerValue[0].data);
    this.subjectPublicKeyInfo = X509Key.parse(arrayOfDerValue[0].data.getDerValue());
    if (arrayOfDerValue[0].data.available() != 0)
      this.attributeSet = new PKCS10Attributes(arrayOfDerValue[0].data);
    else
      this.attributeSet = new PKCS10Attributes();
    if (arrayOfDerValue[0].data.available() != 0)
      throw new IllegalArgumentException("illegal PKCS #10 data");
    try
    {
      Signature localSignature = Signature.getInstance(localAlgorithmId.getName());
      localSignature.initVerify(this.subjectPublicKeyInfo);
      localSignature.update(paramArrayOfByte);
      if (!(localSignature.verify(arrayOfByte)))
        throw new SignatureException("Invalid PKCS #10 signature");
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new SignatureException("invalid key");
    }
  }

  public void encodeAndSign(X500Signer paramX500Signer)
    throws CertificateException, IOException, SignatureException
  {
    if (this.encoded != null)
      throw new SignatureException("request is already signed");
    this.subject = paramX500Signer.getSigner();
    Object localObject = new DerOutputStream();
    ((DerOutputStream)localObject).putInteger(BigInteger.ZERO);
    this.subject.encode((DerOutputStream)localObject);
    ((DerOutputStream)localObject).write(this.subjectPublicKeyInfo.getEncoded());
    this.attributeSet.encode((OutputStream)localObject);
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.write(48, (DerOutputStream)localObject);
    byte[] arrayOfByte1 = localDerOutputStream.toByteArray();
    localObject = localDerOutputStream;
    paramX500Signer.update(arrayOfByte1, 0, arrayOfByte1.length);
    byte[] arrayOfByte2 = paramX500Signer.sign();
    paramX500Signer.getAlgorithmId().encode((DerOutputStream)localObject);
    ((DerOutputStream)localObject).putBitString(arrayOfByte2);
    localDerOutputStream = new DerOutputStream();
    localDerOutputStream.write(48, (DerOutputStream)localObject);
    this.encoded = localDerOutputStream.toByteArray();
  }

  public X500Name getSubjectName()
  {
    return this.subject;
  }

  public PublicKey getSubjectPublicKeyInfo()
  {
    return this.subjectPublicKeyInfo;
  }

  public PKCS10Attributes getAttributes()
  {
    return this.attributeSet;
  }

  public byte[] getEncoded()
  {
    if (this.encoded != null)
      return ((byte[])(byte[])this.encoded.clone());
    return null;
  }

  public void print(PrintStream paramPrintStream)
    throws IOException, SignatureException
  {
    if (this.encoded == null)
      throw new SignatureException("Cert request was not signed");
    BASE64Encoder localBASE64Encoder = new BASE64Encoder();
    paramPrintStream.println("-----BEGIN NEW CERTIFICATE REQUEST-----");
    localBASE64Encoder.encodeBuffer(this.encoded, paramPrintStream);
    paramPrintStream.println("-----END NEW CERTIFICATE REQUEST-----");
  }

  public String toString()
  {
    return "[PKCS #10 certificate request:\n" + this.subjectPublicKeyInfo.toString() + " subject: <" + this.subject + ">" + "\n" + " attributes: " + this.attributeSet.toString() + "\n]";
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof PKCS10))
      return false;
    if (this.encoded == null)
      return false;
    byte[] arrayOfByte = ((PKCS10)paramObject).getEncoded();
    if (arrayOfByte == null)
      return false;
    return Arrays.equals(this.encoded, arrayOfByte);
  }

  public int hashCode()
  {
    int i = 0;
    if (this.encoded != null)
      for (int j = 1; j < this.encoded.length; ++j)
        i += this.encoded[j] * j;
    return i;
  }
}