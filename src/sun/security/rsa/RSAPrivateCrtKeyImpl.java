package sun.security.rsa;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public final class RSAPrivateCrtKeyImpl extends PKCS8Key
  implements RSAPrivateCrtKey
{
  private static final long serialVersionUID = -1326088454257084918L;
  private BigInteger n;
  private BigInteger e;
  private BigInteger d;
  private BigInteger p;
  private BigInteger q;
  private BigInteger pe;
  private BigInteger qe;
  private BigInteger coeff;
  static final AlgorithmId rsaId = new AlgorithmId(AlgorithmId.RSAEncryption_oid);

  public static RSAPrivateKey newKey(byte[] paramArrayOfByte)
    throws InvalidKeyException
  {
    RSAPrivateCrtKeyImpl localRSAPrivateCrtKeyImpl = new RSAPrivateCrtKeyImpl(paramArrayOfByte);
    if (localRSAPrivateCrtKeyImpl.getPublicExponent().signum() == 0)
      return new RSAPrivateKeyImpl(localRSAPrivateCrtKeyImpl.getModulus(), localRSAPrivateCrtKeyImpl.getPrivateExponent());
    return localRSAPrivateCrtKeyImpl;
  }

  RSAPrivateCrtKeyImpl(byte[] paramArrayOfByte)
    throws InvalidKeyException
  {
    decode(paramArrayOfByte);
    RSAKeyFactory.checkKeyLength(this.n);
  }

  RSAPrivateCrtKeyImpl(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3, BigInteger paramBigInteger4, BigInteger paramBigInteger5, BigInteger paramBigInteger6, BigInteger paramBigInteger7, BigInteger paramBigInteger8)
    throws InvalidKeyException
  {
    this.n = paramBigInteger1;
    this.e = paramBigInteger2;
    this.d = paramBigInteger3;
    this.p = paramBigInteger4;
    this.q = paramBigInteger5;
    this.pe = paramBigInteger6;
    this.qe = paramBigInteger7;
    this.coeff = paramBigInteger8;
    RSAKeyFactory.checkKeyLength(paramBigInteger1);
    this.algid = rsaId;
    try
    {
      DerOutputStream localDerOutputStream = new DerOutputStream();
      localDerOutputStream.putInteger(0);
      localDerOutputStream.putInteger(paramBigInteger1);
      localDerOutputStream.putInteger(paramBigInteger2);
      localDerOutputStream.putInteger(paramBigInteger3);
      localDerOutputStream.putInteger(paramBigInteger4);
      localDerOutputStream.putInteger(paramBigInteger5);
      localDerOutputStream.putInteger(paramBigInteger6);
      localDerOutputStream.putInteger(paramBigInteger7);
      localDerOutputStream.putInteger(paramBigInteger8);
      DerValue localDerValue = new DerValue(48, localDerOutputStream.toByteArray());
      this.key = localDerValue.toByteArray();
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException(localIOException);
    }
  }

  public String getAlgorithm()
  {
    return "RSA";
  }

  public BigInteger getModulus()
  {
    return this.n;
  }

  public BigInteger getPublicExponent()
  {
    return this.e;
  }

  public BigInteger getPrivateExponent()
  {
    return this.d;
  }

  public BigInteger getPrimeP()
  {
    return this.p;
  }

  public BigInteger getPrimeQ()
  {
    return this.q;
  }

  public BigInteger getPrimeExponentP()
  {
    return this.pe;
  }

  public BigInteger getPrimeExponentQ()
  {
    return this.qe;
  }

  public BigInteger getCrtCoefficient()
  {
    return this.coeff;
  }

  protected void parseKeyBits()
    throws InvalidKeyException
  {
    DerInputStream localDerInputStream1;
    try
    {
      localDerInputStream1 = new DerInputStream(this.key);
      DerValue localDerValue = localDerInputStream1.getDerValue();
      if (localDerValue.tag != 48)
        throw new IOException("Not a SEQUENCE");
      DerInputStream localDerInputStream2 = localDerValue.data;
      int i = localDerInputStream2.getInteger();
      if (i != 0)
        throw new IOException("Version must be 0");
      this.n = getBigInteger(localDerInputStream2);
      this.e = getBigInteger(localDerInputStream2);
      this.d = getBigInteger(localDerInputStream2);
      this.p = getBigInteger(localDerInputStream2);
      this.q = getBigInteger(localDerInputStream2);
      this.pe = getBigInteger(localDerInputStream2);
      this.qe = getBigInteger(localDerInputStream2);
      this.coeff = getBigInteger(localDerInputStream2);
      if (localDerValue.data.available() != 0)
        throw new IOException("Extra data available");
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException("Invalid RSA private key", localIOException);
    }
  }

  static BigInteger getBigInteger(DerInputStream paramDerInputStream)
    throws IOException
  {
    BigInteger localBigInteger = paramDerInputStream.getBigInteger();
    if (localBigInteger.signum() < 0)
      localBigInteger = new BigInteger(1, localBigInteger.toByteArray());
    return localBigInteger;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("Sun RSA private CRT key, ");
    localStringBuffer.append(this.n.bitLength());
    localStringBuffer.append(" bits\n  modulus:          ");
    localStringBuffer.append(this.n);
    localStringBuffer.append("\n  public exponent:  ");
    localStringBuffer.append(this.e);
    localStringBuffer.append("\n  private exponent: ");
    localStringBuffer.append(this.d);
    localStringBuffer.append("\n  prime p:          ");
    localStringBuffer.append(this.p);
    localStringBuffer.append("\n  prime q:          ");
    localStringBuffer.append(this.q);
    localStringBuffer.append("\n  prime exponent p: ");
    localStringBuffer.append(this.pe);
    localStringBuffer.append("\n  prime exponent q: ");
    localStringBuffer.append(this.qe);
    localStringBuffer.append("\n  crt coefficient:  ");
    localStringBuffer.append(this.coeff);
    return localStringBuffer.toString();
  }
}