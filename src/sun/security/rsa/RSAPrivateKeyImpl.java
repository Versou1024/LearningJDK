package sun.security.rsa;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPrivateKey;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class RSAPrivateKeyImpl extends PKCS8Key
  implements RSAPrivateKey
{
  private static final long serialVersionUID = -33106691987952810L;
  private final BigInteger n;
  private final BigInteger d;

  RSAPrivateKeyImpl(BigInteger paramBigInteger1, BigInteger paramBigInteger2)
    throws InvalidKeyException
  {
    this.n = paramBigInteger1;
    this.d = paramBigInteger2;
    RSAKeyFactory.checkKeyLength(paramBigInteger1);
    this.algid = RSAPrivateCrtKeyImpl.rsaId;
    try
    {
      DerOutputStream localDerOutputStream = new DerOutputStream();
      localDerOutputStream.putInteger(0);
      localDerOutputStream.putInteger(paramBigInteger1);
      localDerOutputStream.putInteger(0);
      localDerOutputStream.putInteger(paramBigInteger2);
      localDerOutputStream.putInteger(0);
      localDerOutputStream.putInteger(0);
      localDerOutputStream.putInteger(0);
      localDerOutputStream.putInteger(0);
      localDerOutputStream.putInteger(0);
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

  public BigInteger getPrivateExponent()
  {
    return this.d;
  }

  public String toString()
  {
    return "Sun RSA private key, " + this.n.bitLength() + " bits\n  modulus: " + this.n + "\n  private exponent: " + this.d;
  }
}