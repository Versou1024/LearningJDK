package sun.security.provider;

import B;
import java.math.BigInteger;
import java.security.AlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;

public class DSAParameterGenerator extends AlgorithmParameterGeneratorSpi
{
  private int modLen = 1024;
  private SecureRandom random;
  private static final BigInteger ZERO = BigInteger.valueOf(3412046586878885888L);
  private static final BigInteger ONE = BigInteger.valueOf(3412046586878885889L);
  private static final BigInteger TWO = BigInteger.valueOf(2L);
  private SHA sha = new SHA();

  protected void engineInit(int paramInt, SecureRandom paramSecureRandom)
  {
    if ((paramInt < 512) || (paramInt > 1024) || (paramInt % 64 != 0))
      throw new InvalidParameterException("Prime size must range from 512 to 1024 and be a multiple of 64");
    this.modLen = paramInt;
    this.random = paramSecureRandom;
  }

  protected void engineInit(AlgorithmParameterSpec paramAlgorithmParameterSpec, SecureRandom paramSecureRandom)
    throws InvalidAlgorithmParameterException
  {
    throw new InvalidAlgorithmParameterException("Invalid parameter");
  }

  protected AlgorithmParameters engineGenerateParameters()
  {
    AlgorithmParameters localAlgorithmParameters = null;
    try
    {
      if (this.random == null)
        this.random = new SecureRandom();
      BigInteger[] arrayOfBigInteger = generatePandQ(this.random, this.modLen);
      BigInteger localBigInteger1 = arrayOfBigInteger[0];
      BigInteger localBigInteger2 = arrayOfBigInteger[1];
      BigInteger localBigInteger3 = generateG(localBigInteger1, localBigInteger2);
      DSAParameterSpec localDSAParameterSpec = new DSAParameterSpec(localBigInteger1, localBigInteger2, localBigInteger3);
      localAlgorithmParameters = AlgorithmParameters.getInstance("DSA", "SUN");
      localAlgorithmParameters.init(localDSAParameterSpec);
    }
    catch (InvalidParameterSpecException localInvalidParameterSpecException)
    {
      throw new RuntimeException(localInvalidParameterSpecException.getMessage());
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new RuntimeException(localNoSuchAlgorithmException.getMessage());
    }
    catch (NoSuchProviderException localNoSuchProviderException)
    {
      throw new RuntimeException(localNoSuchProviderException.getMessage());
    }
    return localAlgorithmParameters;
  }

  BigInteger[] generatePandQ(SecureRandom paramSecureRandom, int paramInt)
  {
    BigInteger[] arrayOfBigInteger = null;
    byte[] arrayOfByte = new byte[20];
    while (arrayOfBigInteger == null)
    {
      for (int i = 0; i < 20; ++i)
        arrayOfByte[i] = (byte)paramSecureRandom.nextInt();
      arrayOfBigInteger = generatePandQ(arrayOfByte, paramInt);
    }
    return arrayOfBigInteger;
  }

  BigInteger[] generatePandQ(byte[] paramArrayOfByte, int paramInt)
  {
    int i = paramArrayOfByte.length * 8;
    int j = (paramInt - 1) / 160;
    int k = (paramInt - 1) % 160;
    BigInteger localBigInteger1 = new BigInteger(1, paramArrayOfByte);
    BigInteger localBigInteger2 = TWO.pow(2 * i);
    byte[] arrayOfByte1 = SHA(paramArrayOfByte);
    byte[] arrayOfByte2 = SHA(toByteArray(localBigInteger1.add(ONE).mod(localBigInteger2)));
    xor(arrayOfByte1, arrayOfByte2);
    byte[] arrayOfByte3 = arrayOfByte1;
    int tmp91_90 = 0;
    byte[] tmp91_88 = arrayOfByte3;
    tmp91_88[tmp91_90] = (byte)(tmp91_88[tmp91_90] | 0x80);
    byte[] tmp103_99 = arrayOfByte3;
    tmp103_99[19] = (byte)(tmp103_99[19] | 0x1);
    BigInteger localBigInteger3 = new BigInteger(1, arrayOfByte3);
    if (!(localBigInteger3.isProbablePrime(80)))
      return null;
    BigInteger[] arrayOfBigInteger1 = new BigInteger[j + 1];
    BigInteger localBigInteger4 = TWO;
    for (int l = 0; l < 4096; ++l)
    {
      for (int i1 = 0; i1 <= j; ++i1)
      {
        BigInteger localBigInteger6 = BigInteger.valueOf(i1);
        localBigInteger8 = localBigInteger1.add(localBigInteger4).add(localBigInteger6).mod(localBigInteger2);
        arrayOfBigInteger1[i1] = new BigInteger(1, SHA(toByteArray(localBigInteger8)));
      }
      BigInteger localBigInteger5 = arrayOfBigInteger1[0];
      for (int i2 = 1; i2 < j; ++i2)
        localBigInteger5 = localBigInteger5.add(arrayOfBigInteger1[i2].multiply(TWO.pow(i2 * 160)));
      localBigInteger5 = localBigInteger5.add(arrayOfBigInteger1[j].mod(TWO.pow(k)).multiply(TWO.pow(j * 160)));
      BigInteger localBigInteger7 = TWO.pow(paramInt - 1);
      BigInteger localBigInteger8 = localBigInteger5.add(localBigInteger7);
      BigInteger localBigInteger9 = localBigInteger8.mod(localBigInteger3.multiply(TWO));
      BigInteger localBigInteger10 = localBigInteger8.subtract(localBigInteger9.subtract(ONE));
      if ((localBigInteger10.compareTo(localBigInteger7) > -1) && (localBigInteger10.isProbablePrime(80)))
      {
        BigInteger[] arrayOfBigInteger2 = { localBigInteger10, localBigInteger3, localBigInteger1, BigInteger.valueOf(l) };
        return arrayOfBigInteger2;
      }
      localBigInteger4 = localBigInteger4.add(BigInteger.valueOf(j)).add(ONE);
    }
    return null;
  }

  BigInteger generateG(BigInteger paramBigInteger1, BigInteger paramBigInteger2)
  {
    BigInteger localBigInteger1 = ONE;
    BigInteger localBigInteger2 = paramBigInteger1.subtract(ONE).divide(paramBigInteger2);
    BigInteger localBigInteger3 = ONE;
    while (localBigInteger3.compareTo(TWO) < 0)
    {
      localBigInteger3 = localBigInteger1.modPow(localBigInteger2, paramBigInteger1);
      localBigInteger1 = localBigInteger1.add(ONE);
    }
    return localBigInteger3;
  }

  private byte[] SHA(byte[] paramArrayOfByte)
  {
    this.sha.engineReset();
    this.sha.engineUpdate(paramArrayOfByte, 0, paramArrayOfByte.length);
    return this.sha.engineDigest();
  }

  private byte[] toByteArray(BigInteger paramBigInteger)
  {
    Object localObject = paramBigInteger.toByteArray();
    if (localObject[0] == 0)
    {
      byte[] arrayOfByte = new byte[localObject.length - 1];
      System.arraycopy(localObject, 1, arrayOfByte, 0, arrayOfByte.length);
      localObject = arrayOfByte;
    }
    return ((B)localObject);
  }

  private void xor(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    for (int i = 0; i < paramArrayOfByte1.length; ++i)
    {
      int tmp10_9 = i;
      byte[] tmp10_8 = paramArrayOfByte1;
      tmp10_8[tmp10_9] = (byte)(tmp10_8[tmp10_9] ^ paramArrayOfByte2[i]);
    }
  }
}