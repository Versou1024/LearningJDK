package sun.security.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.Arrays;
import sun.security.jca.JCAUtil;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

abstract class DSA extends SignatureSpi
{
  private static final boolean debug = 0;
  private DSAParams params;
  private BigInteger presetP;
  private BigInteger presetQ;
  private BigInteger presetG;
  private BigInteger presetY;
  private BigInteger presetX;
  private int[] Kseed;
  private byte[] KseedAsByteArray;
  private int[] previousKseed;
  private SecureRandom signingRandom;
  private static final int round1_kt = 1518500249;
  private static final int round2_kt = 1859775393;
  private static final int round3_kt = -1894007588;
  private static final int round4_kt = -899497514;

  abstract byte[] getDigest()
    throws SignatureException;

  abstract void resetDigest();

  protected void engineInitSign(PrivateKey paramPrivateKey)
    throws InvalidKeyException
  {
    if (!(paramPrivateKey instanceof DSAPrivateKey))
      throw new InvalidKeyException("not a DSA private key: " + paramPrivateKey);
    DSAPrivateKey localDSAPrivateKey = (DSAPrivateKey)paramPrivateKey;
    this.presetX = localDSAPrivateKey.getX();
    this.presetY = null;
    initialize(localDSAPrivateKey.getParams());
  }

  protected void engineInitVerify(PublicKey paramPublicKey)
    throws InvalidKeyException
  {
    if (!(paramPublicKey instanceof DSAPublicKey))
      throw new InvalidKeyException("not a DSA public key: " + paramPublicKey);
    DSAPublicKey localDSAPublicKey = (DSAPublicKey)paramPublicKey;
    this.presetY = localDSAPublicKey.getY();
    this.presetX = null;
    initialize(localDSAPublicKey.getParams());
  }

  private void initialize(DSAParams paramDSAParams)
    throws InvalidKeyException
  {
    resetDigest();
    setParams(paramDSAParams);
  }

  protected byte[] engineSign()
    throws SignatureException
  {
    BigInteger localBigInteger1 = generateK(this.presetQ);
    BigInteger localBigInteger2 = generateR(this.presetP, this.presetQ, this.presetG, localBigInteger1);
    BigInteger localBigInteger3 = generateS(this.presetX, this.presetQ, localBigInteger2, localBigInteger1);
    try
    {
      DerOutputStream localDerOutputStream = new DerOutputStream(100);
      localDerOutputStream.putInteger(localBigInteger2);
      localDerOutputStream.putInteger(localBigInteger3);
      DerValue localDerValue = new DerValue(48, localDerOutputStream.toByteArray());
      return localDerValue.toByteArray();
    }
    catch (IOException localIOException)
    {
      throw new SignatureException("error encoding signature");
    }
  }

  protected boolean engineVerify(byte[] paramArrayOfByte)
    throws SignatureException
  {
    return engineVerify(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  protected boolean engineVerify(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws SignatureException
  {
    Object localObject;
    BigInteger localBigInteger1 = null;
    BigInteger localBigInteger2 = null;
    try
    {
      DerInputStream localDerInputStream = new DerInputStream(paramArrayOfByte, paramInt1, paramInt2);
      localObject = localDerInputStream.getSequence(2);
      localBigInteger1 = localObject[0].getBigInteger();
      localBigInteger2 = localObject[1].getBigInteger();
    }
    catch (IOException localIOException)
    {
      throw new SignatureException("invalid encoding for signature");
    }
    if (localBigInteger1.signum() < 0)
      localBigInteger1 = new BigInteger(1, localBigInteger1.toByteArray());
    if (localBigInteger2.signum() < 0)
      localBigInteger2 = new BigInteger(1, localBigInteger2.toByteArray());
    if ((localBigInteger1.compareTo(this.presetQ) == -1) && (localBigInteger2.compareTo(this.presetQ) == -1))
    {
      BigInteger localBigInteger3 = generateW(this.presetP, this.presetQ, this.presetG, localBigInteger2);
      localObject = generateV(this.presetY, this.presetP, this.presetQ, this.presetG, localBigInteger3, localBigInteger1);
      return ((BigInteger)localObject).equals(localBigInteger1);
    }
    throw new SignatureException("invalid signature: out of range values");
  }

  private BigInteger generateR(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3, BigInteger paramBigInteger4)
  {
    BigInteger localBigInteger = paramBigInteger3.modPow(paramBigInteger4, paramBigInteger1);
    return localBigInteger.remainder(paramBigInteger2);
  }

  private BigInteger generateS(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3, BigInteger paramBigInteger4)
    throws SignatureException
  {
    byte[] arrayOfByte = getDigest();
    BigInteger localBigInteger1 = new BigInteger(1, arrayOfByte);
    BigInteger localBigInteger2 = paramBigInteger4.modInverse(paramBigInteger2);
    BigInteger localBigInteger3 = paramBigInteger1.multiply(paramBigInteger3);
    localBigInteger3 = localBigInteger1.add(localBigInteger3);
    localBigInteger3 = localBigInteger2.multiply(localBigInteger3);
    return localBigInteger3.remainder(paramBigInteger2);
  }

  private BigInteger generateW(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3, BigInteger paramBigInteger4)
  {
    return paramBigInteger4.modInverse(paramBigInteger2);
  }

  private BigInteger generateV(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3, BigInteger paramBigInteger4, BigInteger paramBigInteger5, BigInteger paramBigInteger6)
    throws SignatureException
  {
    byte[] arrayOfByte = getDigest();
    BigInteger localBigInteger1 = new BigInteger(1, arrayOfByte);
    localBigInteger1 = localBigInteger1.multiply(paramBigInteger5);
    BigInteger localBigInteger2 = localBigInteger1.remainder(paramBigInteger3);
    BigInteger localBigInteger3 = paramBigInteger6.multiply(paramBigInteger5).remainder(paramBigInteger3);
    BigInteger localBigInteger4 = paramBigInteger4.modPow(localBigInteger2, paramBigInteger2);
    BigInteger localBigInteger5 = paramBigInteger1.modPow(localBigInteger3, paramBigInteger2);
    BigInteger localBigInteger6 = localBigInteger4.multiply(localBigInteger5);
    BigInteger localBigInteger7 = localBigInteger6.remainder(paramBigInteger2);
    return localBigInteger7.remainder(paramBigInteger3);
  }

  private BigInteger generateK(BigInteger paramBigInteger)
  {
    BigInteger localBigInteger = null;
    if ((this.Kseed != null) && (!(Arrays.equals(this.Kseed, this.previousKseed))))
    {
      localBigInteger = generateK(this.Kseed, paramBigInteger);
      if ((localBigInteger.signum() > 0) && (localBigInteger.compareTo(paramBigInteger) < 0))
      {
        this.previousKseed = new int[this.Kseed.length];
        System.arraycopy(this.Kseed, 0, this.previousKseed, 0, this.Kseed.length);
        return localBigInteger;
      }
    }
    SecureRandom localSecureRandom = getSigningRandom();
    while (true)
    {
      int[] arrayOfInt = new int[5];
      for (int i = 0; i < 5; ++i)
        arrayOfInt[i] = localSecureRandom.nextInt();
      localBigInteger = generateK(arrayOfInt, paramBigInteger);
      if ((localBigInteger.signum() > 0) && (localBigInteger.compareTo(paramBigInteger) < 0))
      {
        this.previousKseed = new int[arrayOfInt.length];
        System.arraycopy(arrayOfInt, 0, this.previousKseed, 0, arrayOfInt.length);
        return localBigInteger;
      }
    }
  }

  private SecureRandom getSigningRandom()
  {
    if (this.signingRandom == null)
      if (this.appRandom != null)
        this.signingRandom = this.appRandom;
      else
        this.signingRandom = JCAUtil.getSecureRandom();
    return this.signingRandom;
  }

  private BigInteger generateK(int[] paramArrayOfInt, BigInteger paramBigInteger)
  {
    int[] arrayOfInt1 = { -271733879, -1732584194, 271733878, -1009589776, 1732584193 };
    int[] arrayOfInt2 = SHA_7(paramArrayOfInt, arrayOfInt1);
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

  static int[] SHA_7(int[] paramArrayOfInt1, int[] paramArrayOfInt2)
  {
    int[] arrayOfInt1 = new int[80];
    System.arraycopy(paramArrayOfInt1, 0, arrayOfInt1, 0, paramArrayOfInt1.length);
    int i = 0;
    for (int j = 16; j <= 79; ++j)
    {
      i = arrayOfInt1[(j - 3)] ^ arrayOfInt1[(j - 8)] ^ arrayOfInt1[(j - 14)] ^ arrayOfInt1[(j - 16)];
      arrayOfInt1[j] = (i << 1 | i >>> 31);
    }
    j = paramArrayOfInt2[0];
    int k = paramArrayOfInt2[1];
    int l = paramArrayOfInt2[2];
    int i1 = paramArrayOfInt2[3];
    int i2 = paramArrayOfInt2[4];
    for (int i3 = 0; i3 < 20; ++i3)
    {
      i = (j << 5 | j >>> 27) + (k & l | (k ^ 0xFFFFFFFF) & i1) + i2 + arrayOfInt1[i3] + 1518500249;
      i2 = i1;
      i1 = l;
      l = k << 30 | k >>> 2;
      k = j;
      j = i;
    }
    for (i3 = 20; i3 < 40; ++i3)
    {
      i = (j << 5 | j >>> 27) + (k ^ l ^ i1) + i2 + arrayOfInt1[i3] + 1859775393;
      i2 = i1;
      i1 = l;
      l = k << 30 | k >>> 2;
      k = j;
      j = i;
    }
    for (i3 = 40; i3 < 60; ++i3)
    {
      i = (j << 5 | j >>> 27) + (k & l | k & i1 | l & i1) + i2 + arrayOfInt1[i3] + -1894007588;
      i2 = i1;
      i1 = l;
      l = k << 30 | k >>> 2;
      k = j;
      j = i;
    }
    for (i3 = 60; i3 < 80; ++i3)
    {
      i = (j << 5 | j >>> 27) + (k ^ l ^ i1) + i2 + arrayOfInt1[i3] + -899497514;
      i2 = i1;
      i1 = l;
      l = k << 30 | k >>> 2;
      k = j;
      j = i;
    }
    int[] arrayOfInt2 = new int[5];
    arrayOfInt2[0] = (paramArrayOfInt2[0] + j);
    arrayOfInt2[1] = (paramArrayOfInt2[1] + k);
    arrayOfInt2[2] = (paramArrayOfInt2[2] + l);
    arrayOfInt2[3] = (paramArrayOfInt2[3] + i1);
    arrayOfInt2[4] = (paramArrayOfInt2[4] + i2);
    return arrayOfInt2;
  }

  @Deprecated
  protected void engineSetParameter(String paramString, Object paramObject)
  {
    if (paramString.equals("KSEED"))
    {
      if (paramObject instanceof byte[])
      {
        this.Kseed = byteArray2IntArray((byte[])(byte[])paramObject);
        this.KseedAsByteArray = ((byte[])(byte[])paramObject);
        return;
      }
      debug("unrecognized param: " + paramString);
      throw new InvalidParameterException("Kseed not a byte array");
    }
    throw new InvalidParameterException("invalid parameter");
  }

  @Deprecated
  protected Object engineGetParameter(String paramString)
  {
    if (paramString.equals("KSEED"))
      return this.KseedAsByteArray;
    return null;
  }

  private void setParams(DSAParams paramDSAParams)
    throws InvalidKeyException
  {
    if (paramDSAParams == null)
      throw new InvalidKeyException("DSA public key lacks parameters");
    this.params = paramDSAParams;
    this.presetP = paramDSAParams.getP();
    this.presetQ = paramDSAParams.getQ();
    this.presetG = paramDSAParams.getG();
  }

  public String toString()
  {
    String str = "DSA Signature";
    if ((this.presetP != null) && (this.presetQ != null) && (this.presetG != null))
    {
      str = str + "\n\tp: " + Debug.toHexString(this.presetP);
      str = str + "\n\tq: " + Debug.toHexString(this.presetQ);
      str = str + "\n\tg: " + Debug.toHexString(this.presetG);
    }
    else
    {
      str = str + "\n\t P, Q or G not initialized.";
    }
    if (this.presetY != null)
      str = str + "\n\ty: " + Debug.toHexString(this.presetY);
    if ((this.presetY == null) && (this.presetX == null))
      str = str + "\n\tUNINIIALIZED";
    return str;
  }

  private int[] byteArray2IntArray(byte[] paramArrayOfByte)
  {
    byte[] arrayOfByte;
    int i = 0;
    int j = paramArrayOfByte.length % 4;
    switch (j)
    {
    case 3:
      arrayOfByte = new byte[paramArrayOfByte.length + 1];
      break;
    case 2:
      arrayOfByte = new byte[paramArrayOfByte.length + 2];
      break;
    case 1:
      arrayOfByte = new byte[paramArrayOfByte.length + 3];
      break;
    default:
      arrayOfByte = new byte[paramArrayOfByte.length + 0];
    }
    System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0, paramArrayOfByte.length);
    int[] arrayOfInt = new int[arrayOfByte.length / 4];
    for (int k = 0; k < arrayOfByte.length; k += 4)
    {
      arrayOfInt[i] = (arrayOfByte[(k + 3)] & 0xFF);
      arrayOfInt[i] |= arrayOfByte[(k + 2)] << 8 & 0xFF00;
      arrayOfInt[i] |= arrayOfByte[(k + 1)] << 16 & 0xFF0000;
      arrayOfInt[i] |= arrayOfByte[(k + 0)] << 24 & 0xFF000000;
      ++i;
    }
    return arrayOfInt;
  }

  private static void debug(Exception paramException)
  {
  }

  private static void debug(String paramString)
  {
  }

  public static final class RawDSA extends DSA
  {
    private static final int SHA1_LEN = 20;
    private final byte[] digestBuffer = new byte[20];
    private int ofs;

    protected void engineUpdate(byte paramByte)
    {
      if (this.ofs == 20)
      {
        this.ofs = 21;
        return;
      }
      this.digestBuffer[(this.ofs++)] = paramByte;
    }

    protected void engineUpdate(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    {
      if (this.ofs + paramInt2 > 20)
      {
        this.ofs = 21;
        return;
      }
      System.arraycopy(paramArrayOfByte, paramInt1, this.digestBuffer, this.ofs, paramInt2);
      this.ofs += paramInt2;
    }

    byte[] getDigest()
      throws SignatureException
    {
      if (this.ofs != 20)
        throw new SignatureException("Data for RawDSA must be exactly 20 bytes long");
      this.ofs = 0;
      return this.digestBuffer;
    }

    void resetDigest()
    {
      this.ofs = 0;
    }
  }

  public static final class SHA1withDSA extends DSA
  {
    private final MessageDigest dataSHA = MessageDigest.getInstance("SHA-1");

    protected void engineUpdate(byte paramByte)
    {
      this.dataSHA.update(paramByte);
    }

    protected void engineUpdate(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    {
      this.dataSHA.update(paramArrayOfByte, paramInt1, paramInt2);
    }

    protected void engineUpdate(ByteBuffer paramByteBuffer)
    {
      this.dataSHA.update(paramByteBuffer);
    }

    byte[] getDigest()
    {
      return this.dataSHA.digest();
    }

    void resetDigest()
    {
      this.dataSHA.reset();
    }
  }
}