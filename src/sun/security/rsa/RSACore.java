package sun.security.rsa;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.WeakHashMap;
import javax.crypto.BadPaddingException;
import sun.security.jca.JCAUtil;

public final class RSACore
{
  private static final boolean ENABLE_BLINDING = 1;
  private static final int BLINDING_MAX_REUSE = 50;
  private static final Map blindingCache;

  public static int getByteLength(BigInteger paramBigInteger)
  {
    int i = paramBigInteger.bitLength();
    return (i + 7 >> 3);
  }

  public static int getByteLength(RSAKey paramRSAKey)
  {
    return getByteLength(paramRSAKey.getModulus());
  }

  public static byte[] convert(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if ((paramInt1 == 0) && (paramInt2 == paramArrayOfByte.length))
      return paramArrayOfByte;
    byte[] arrayOfByte = new byte[paramInt2];
    System.arraycopy(paramArrayOfByte, paramInt1, arrayOfByte, 0, paramInt2);
    return arrayOfByte;
  }

  public static byte[] rsa(byte[] paramArrayOfByte, RSAPublicKey paramRSAPublicKey)
    throws BadPaddingException
  {
    return crypt(paramArrayOfByte, paramRSAPublicKey.getModulus(), paramRSAPublicKey.getPublicExponent());
  }

  public static byte[] rsa(byte[] paramArrayOfByte, RSAPrivateKey paramRSAPrivateKey)
    throws BadPaddingException
  {
    if (paramRSAPrivateKey instanceof RSAPrivateCrtKey)
      return crtCrypt(paramArrayOfByte, (RSAPrivateCrtKey)paramRSAPrivateKey);
    return crypt(paramArrayOfByte, paramRSAPrivateKey.getModulus(), paramRSAPrivateKey.getPrivateExponent());
  }

  private static byte[] crypt(byte[] paramArrayOfByte, BigInteger paramBigInteger1, BigInteger paramBigInteger2)
    throws BadPaddingException
  {
    BigInteger localBigInteger1 = parseMsg(paramArrayOfByte, paramBigInteger1);
    BigInteger localBigInteger2 = localBigInteger1.modPow(paramBigInteger2, paramBigInteger1);
    return toByteArray(localBigInteger2, getByteLength(paramBigInteger1));
  }

  private static byte[] crtCrypt(byte[] paramArrayOfByte, RSAPrivateCrtKey paramRSAPrivateCrtKey)
    throws BadPaddingException
  {
    BigInteger localBigInteger1 = paramRSAPrivateCrtKey.getModulus();
    BigInteger localBigInteger2 = parseMsg(paramArrayOfByte, localBigInteger1);
    BigInteger localBigInteger3 = paramRSAPrivateCrtKey.getPrimeP();
    BigInteger localBigInteger4 = paramRSAPrivateCrtKey.getPrimeQ();
    BigInteger localBigInteger5 = paramRSAPrivateCrtKey.getPrimeExponentP();
    BigInteger localBigInteger6 = paramRSAPrivateCrtKey.getPrimeExponentQ();
    BigInteger localBigInteger7 = paramRSAPrivateCrtKey.getCrtCoefficient();
    BlindingParameters localBlindingParameters = getBlindingParameters(paramRSAPrivateCrtKey);
    localBigInteger2 = localBigInteger2.multiply(localBlindingParameters.re).mod(localBigInteger1);
    BigInteger localBigInteger8 = localBigInteger2.modPow(localBigInteger5, localBigInteger3);
    BigInteger localBigInteger9 = localBigInteger2.modPow(localBigInteger6, localBigInteger4);
    BigInteger localBigInteger10 = localBigInteger8.subtract(localBigInteger9);
    if (localBigInteger10.signum() < 0)
      localBigInteger10 = localBigInteger10.add(localBigInteger3);
    BigInteger localBigInteger11 = localBigInteger10.multiply(localBigInteger7).mod(localBigInteger3);
    BigInteger localBigInteger12 = localBigInteger11.multiply(localBigInteger4).add(localBigInteger9);
    if (localBlindingParameters != null)
      localBigInteger12 = localBigInteger12.multiply(localBlindingParameters.rInv).mod(localBigInteger1);
    return toByteArray(localBigInteger12, getByteLength(localBigInteger1));
  }

  private static BigInteger parseMsg(byte[] paramArrayOfByte, BigInteger paramBigInteger)
    throws BadPaddingException
  {
    BigInteger localBigInteger = new BigInteger(1, paramArrayOfByte);
    if (localBigInteger.compareTo(paramBigInteger) >= 0)
      throw new BadPaddingException("Message is larger than modulus");
    return localBigInteger;
  }

  private static byte[] toByteArray(BigInteger paramBigInteger, int paramInt)
  {
    byte[] arrayOfByte1 = paramBigInteger.toByteArray();
    int i = arrayOfByte1.length;
    if (i == paramInt)
      return arrayOfByte1;
    if ((i == paramInt + 1) && (arrayOfByte1[0] == 0))
    {
      arrayOfByte2 = new byte[paramInt];
      System.arraycopy(arrayOfByte1, 1, arrayOfByte2, 0, paramInt);
      return arrayOfByte2;
    }
    if ((!($assertionsDisabled)) && (i >= paramInt))
      throw new AssertionError();
    byte[] arrayOfByte2 = new byte[paramInt];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, paramInt - i, i);
    return arrayOfByte2;
  }

  private static BlindingParameters getBlindingParameters(RSAPrivateCrtKey paramRSAPrivateCrtKey)
  {
    BigInteger localBigInteger1 = paramRSAPrivateCrtKey.getModulus();
    BigInteger localBigInteger2 = paramRSAPrivateCrtKey.getPublicExponent();
    synchronized (blindingCache)
    {
      localBlindingParameters = (BlindingParameters)blindingCache.get(localBigInteger1);
    }
    if ((localBlindingParameters != null) && (localBlindingParameters.valid(localBigInteger2)))
      return localBlindingParameters;
    int i = localBigInteger1.bitLength();
    SecureRandom localSecureRandom = JCAUtil.getSecureRandom();
    BigInteger localBigInteger3 = new BigInteger(i, localSecureRandom).mod(localBigInteger1);
    BigInteger localBigInteger4 = localBigInteger3.modPow(localBigInteger2, localBigInteger1);
    BigInteger localBigInteger5 = localBigInteger3.modInverse(localBigInteger1);
    BlindingParameters localBlindingParameters = new BlindingParameters(localBigInteger2, localBigInteger4, localBigInteger5);
    synchronized (blindingCache)
    {
      blindingCache.put(localBigInteger1, localBlindingParameters);
    }
    return localBlindingParameters;
  }

  static
  {
    blindingCache = new WeakHashMap();
  }

  private static final class BlindingParameters
  {
    final BigInteger e;
    final BigInteger re;
    final BigInteger rInv;
    private volatile int remainingUses;

    BlindingParameters(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3)
    {
      this.e = paramBigInteger1;
      this.re = paramBigInteger2;
      this.rInv = paramBigInteger3;
      this.remainingUses = 49;
    }

    boolean valid(BigInteger paramBigInteger)
    {
      int i = this.remainingUses--;
      return ((i > 0) && (this.e.equals(paramBigInteger)));
    }
  }
}