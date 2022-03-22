package sun.security.rsa;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyRep;
import java.security.KeyRep.Type;
import java.security.interfaces.RSAPublicKey;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.X509Key;

public final class RSAPublicKeyImpl extends X509Key
  implements RSAPublicKey
{
  private static final long serialVersionUID = 2644735423591199609L;
  private BigInteger n;
  private BigInteger e;

  public RSAPublicKeyImpl(BigInteger paramBigInteger1, BigInteger paramBigInteger2)
    throws InvalidKeyException
  {
    this.n = paramBigInteger1;
    this.e = paramBigInteger2;
    RSAKeyFactory.checkKeyLength(paramBigInteger1);
    this.algid = RSAPrivateCrtKeyImpl.rsaId;
    try
    {
      DerOutputStream localDerOutputStream = new DerOutputStream();
      localDerOutputStream.putInteger(paramBigInteger1);
      localDerOutputStream.putInteger(paramBigInteger2);
      DerValue localDerValue = new DerValue(48, localDerOutputStream.toByteArray());
      this.key = localDerValue.toByteArray();
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException(localIOException);
    }
  }

  public RSAPublicKeyImpl(byte[] paramArrayOfByte)
    throws InvalidKeyException
  {
    decode(paramArrayOfByte);
    RSAKeyFactory.checkKeyLength(this.n);
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
      this.n = RSAPrivateCrtKeyImpl.getBigInteger(localDerInputStream2);
      this.e = RSAPrivateCrtKeyImpl.getBigInteger(localDerInputStream2);
      if (localDerValue.data.available() != 0)
        throw new IOException("Extra data available");
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException("Invalid RSA public key", localIOException);
    }
  }

  public String toString()
  {
    return "Sun RSA public key, " + this.n.bitLength() + " bits\n  modulus: " + this.n + "\n  public exponent: " + this.e;
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return new KeyRep(KeyRep.Type.PUBLIC, getAlgorithm(), getFormat(), getEncoded());
  }
}