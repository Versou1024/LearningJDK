package sun.security.provider;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.DSAKeyPairGenerator;
import java.security.interfaces.DSAParams;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import sun.security.jca.JCAUtil;

public class DSAKeyPairGenerator extends KeyPairGenerator
  implements DSAKeyPairGenerator
{
  private int modlen;
  private boolean forceNewParameters;
  private DSAParameterSpec params;
  private SecureRandom random;

  public DSAKeyPairGenerator()
  {
    super("DSA");
    initialize(1024, null);
  }

  private static void checkStrength(int paramInt)
  {
    if ((paramInt < 512) || (paramInt > 1024) || (paramInt % 64 != 0))
      throw new InvalidParameterException("Modulus size must range from 512 to 1024 and be a multiple of 64");
  }

  public void initialize(int paramInt, SecureRandom paramSecureRandom)
  {
    checkStrength(paramInt);
    this.random = paramSecureRandom;
    this.modlen = paramInt;
    this.params = null;
    this.forceNewParameters = false;
  }

  public void initialize(int paramInt, boolean paramBoolean, SecureRandom paramSecureRandom)
  {
    checkStrength(paramInt);
    if (paramBoolean)
    {
      this.params = null;
    }
    else
    {
      this.params = ParameterCache.getCachedDSAParameterSpec(paramInt);
      if (this.params == null)
        throw new InvalidParameterException("No precomputed parameters for requested modulus size available");
    }
    this.modlen = paramInt;
    this.random = paramSecureRandom;
    this.forceNewParameters = paramBoolean;
  }

  public void initialize(DSAParams paramDSAParams, SecureRandom paramSecureRandom)
  {
    if (paramDSAParams == null)
      throw new InvalidParameterException("Params must not be null");
    DSAParameterSpec localDSAParameterSpec = new DSAParameterSpec(paramDSAParams.getP(), paramDSAParams.getQ(), paramDSAParams.getG());
    initialize0(localDSAParameterSpec, paramSecureRandom);
  }

  public void initialize(AlgorithmParameterSpec paramAlgorithmParameterSpec, SecureRandom paramSecureRandom)
    throws InvalidAlgorithmParameterException
  {
    if (!(paramAlgorithmParameterSpec instanceof DSAParameterSpec))
      throw new InvalidAlgorithmParameterException("Inappropriate parameter");
    initialize0((DSAParameterSpec)paramAlgorithmParameterSpec, paramSecureRandom);
  }

  private void initialize0(DSAParameterSpec paramDSAParameterSpec, SecureRandom paramSecureRandom)
  {
    int i = paramDSAParameterSpec.getP().bitLength();
    checkStrength(i);
    this.modlen = i;
    this.params = paramDSAParameterSpec;
    this.random = paramSecureRandom;
    this.forceNewParameters = false;
  }

  public KeyPair generateKeyPair()
  {
    DSAParameterSpec localDSAParameterSpec;
    if (this.random == null)
      this.random = JCAUtil.getSecureRandom();
    try
    {
      if (this.forceNewParameters)
      {
        localDSAParameterSpec = ParameterCache.getNewDSAParameterSpec(this.modlen, this.random);
      }
      else
      {
        if (this.params == null)
          this.params = ParameterCache.getDSAParameterSpec(this.modlen, this.random);
        localDSAParameterSpec = this.params;
      }
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw new ProviderException(localGeneralSecurityException);
    }
    return generateKeyPair(localDSAParameterSpec.getP(), localDSAParameterSpec.getQ(), localDSAParameterSpec.getG(), this.random);
  }

  public KeyPair generateKeyPair(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3, SecureRandom paramSecureRandom)
  {
    BigInteger localBigInteger1 = generateX(paramSecureRandom, paramBigInteger2);
    BigInteger localBigInteger2 = generateY(localBigInteger1, paramBigInteger1, paramBigInteger3);
    try
    {
      Object localObject;
      if (DSAKeyFactory.SERIAL_INTEROP)
        localObject = new DSAPublicKey(localBigInteger2, paramBigInteger1, paramBigInteger2, paramBigInteger3);
      else
        localObject = new DSAPublicKeyImpl(localBigInteger2, paramBigInteger1, paramBigInteger2, paramBigInteger3);
      DSAPrivateKey localDSAPrivateKey = new DSAPrivateKey(localBigInteger1, paramBigInteger1, paramBigInteger2, paramBigInteger3);
      KeyPair localKeyPair = new KeyPair((PublicKey)localObject, localDSAPrivateKey);
      return localKeyPair;
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new ProviderException(localInvalidKeyException);
    }
  }

  private BigInteger generateX(SecureRandom paramSecureRandom, BigInteger paramBigInteger)
  {
    BigInteger localBigInteger = null;
    while (true)
    {
      int[] arrayOfInt = new int[5];
      for (int i = 0; i < 5; ++i)
        arrayOfInt[i] = paramSecureRandom.nextInt();
      localBigInteger = generateX(arrayOfInt, paramBigInteger);
      if ((localBigInteger.signum() > 0) && (localBigInteger.compareTo(paramBigInteger) < 0))
        break;
    }
    return localBigInteger;
  }

  BigInteger generateX(int[] paramArrayOfInt, BigInteger paramBigInteger)
  {
    int[] arrayOfInt1 = { 1732584193, -271733879, -1732584194, 271733878, -1009589776 };
    int[] arrayOfInt2 = DSA.SHA_7(paramArrayOfInt, arrayOfInt1);
    byte[] arrayOfByte = new byte[arrayOfInt2.length * 4];
    for (int i = 0; i < arrayOfInt2.length; ++i)
    {
      int j = arrayOfInt2[i];
      for (int k = 0; k < 4; ++k)
        arrayOfByte[(i * 4 + k)] = (byte)(j >>> 24 - k * 8);
    }
    BigInteger localBigInteger = new BigInteger(1, arrayOfByte).mod(paramBigInteger);
    return localBigInteger;
  }

  BigInteger generateY(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3)
  {
    BigInteger localBigInteger = paramBigInteger3.modPow(paramBigInteger1, paramBigInteger2);
    return localBigInteger;
  }
}