package sun.security.rsa;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import sun.security.jca.JCAUtil;

public final class RSAKeyPairGenerator extends KeyPairGeneratorSpi
{
  private BigInteger publicExponent;
  private int keySize;
  private SecureRandom random;

  public RSAKeyPairGenerator()
  {
    initialize(1024, null);
  }

  public void initialize(int paramInt, SecureRandom paramSecureRandom)
  {
    if (paramInt < 512)
      throw new InvalidParameterException("Key size must be at least 512 bits");
    if (paramInt > 65536)
      throw new InvalidParameterException("Key size must be 65536 bits or less");
    this.keySize = paramInt;
    this.random = paramSecureRandom;
    this.publicExponent = RSAKeyGenParameterSpec.F4;
  }

  public void initialize(AlgorithmParameterSpec paramAlgorithmParameterSpec, SecureRandom paramSecureRandom)
    throws InvalidAlgorithmParameterException
  {
    if (!(paramAlgorithmParameterSpec instanceof RSAKeyGenParameterSpec))
      throw new InvalidAlgorithmParameterException("Params must be instance of RSAKeyGenParameterSpec");
    RSAKeyGenParameterSpec localRSAKeyGenParameterSpec = (RSAKeyGenParameterSpec)paramAlgorithmParameterSpec;
    this.keySize = localRSAKeyGenParameterSpec.getKeysize();
    this.publicExponent = localRSAKeyGenParameterSpec.getPublicExponent();
    this.random = paramSecureRandom;
    if (this.keySize < 512)
      throw new InvalidAlgorithmParameterException("Key size must be at least 512 bits");
    if (this.keySize > 65536)
      throw new InvalidAlgorithmParameterException("Key size must be 65536 bits or less");
    if (this.publicExponent == null)
    {
      this.publicExponent = RSAKeyGenParameterSpec.F4;
    }
    else
    {
      if (this.publicExponent.compareTo(RSAKeyGenParameterSpec.F0) < 0)
        throw new InvalidAlgorithmParameterException("Public exponent must be 3 or larger");
      if (this.publicExponent.bitLength() > this.keySize)
        throw new InvalidAlgorithmParameterException("Public exponent must be smaller than key size");
    }
  }

  public KeyPair generateKeyPair()
  {
    Object localObject1;
    Object localObject2;
    BigInteger localBigInteger2;
    Object localObject3;
    BigInteger localBigInteger3;
    BigInteger localBigInteger4;
    int i = this.keySize + 1 >> 1;
    int j = this.keySize - i;
    if (this.random == null)
      this.random = JCAUtil.getSecureRandom();
    BigInteger localBigInteger1 = this.publicExponent;
    while (true)
    {
      localObject1 = BigInteger.probablePrime(i, this.random);
      do
      {
        localObject2 = BigInteger.probablePrime(j, this.random);
        if (((BigInteger)localObject1).compareTo((BigInteger)localObject2) < 0)
        {
          localObject3 = localObject1;
          localObject1 = localObject2;
          localObject2 = localObject3;
        }
        localBigInteger2 = ((BigInteger)localObject1).multiply((BigInteger)localObject2);
      }
      while (localBigInteger2.bitLength() < this.keySize);
      localObject3 = ((BigInteger)localObject1).subtract(BigInteger.ONE);
      localBigInteger3 = ((BigInteger)localObject2).subtract(BigInteger.ONE);
      localBigInteger4 = ((BigInteger)localObject3).multiply(localBigInteger3);
      if (localBigInteger1.gcd(localBigInteger4).equals(BigInteger.ONE))
        break;
    }
    BigInteger localBigInteger5 = localBigInteger1.modInverse(localBigInteger4);
    BigInteger localBigInteger6 = localBigInteger5.mod((BigInteger)localObject3);
    BigInteger localBigInteger7 = localBigInteger5.mod(localBigInteger3);
    BigInteger localBigInteger8 = ((BigInteger)localObject2).modInverse((BigInteger)localObject1);
    try
    {
      RSAPublicKeyImpl localRSAPublicKeyImpl = new RSAPublicKeyImpl(localBigInteger2, localBigInteger1);
      RSAPrivateCrtKeyImpl localRSAPrivateCrtKeyImpl = new RSAPrivateCrtKeyImpl(localBigInteger2, localBigInteger1, localBigInteger5, (BigInteger)localObject1, (BigInteger)localObject2, localBigInteger6, localBigInteger7, localBigInteger8);
      return new KeyPair(localRSAPublicKeyImpl, localRSAPrivateCrtKeyImpl);
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new RuntimeException(localInvalidKeyException);
    }
  }
}