package sun.security.krb5.internal.crypto.dk;

import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.internal.crypto.Confounder;
import sun.security.krb5.internal.crypto.KeyUsage;
import sun.security.provider.MD4;

public class ArcFourCrypto extends DkCrypto
{
  private static final boolean debug = 0;
  private static final int confounderSize = 8;
  private static final byte[] ZERO_IV = { 0, 0, 0, 0, 0, 0, 0, 0 };
  private static final int hashSize = 16;
  private final int keyLength;

  public ArcFourCrypto(int paramInt)
  {
    this.keyLength = paramInt;
  }

  protected int getKeySeedLength()
  {
    return this.keyLength;
  }

  protected byte[] randomToKey(byte[] paramArrayOfByte)
  {
    return paramArrayOfByte;
  }

  public byte[] stringToKey(char[] paramArrayOfChar)
    throws GeneralSecurityException
  {
    return stringToKey(paramArrayOfChar, null);
  }

  private byte[] stringToKey(char[] paramArrayOfChar, byte[] paramArrayOfByte)
    throws GeneralSecurityException
  {
    if ((paramArrayOfByte != null) && (paramArrayOfByte.length > 0))
      throw new RuntimeException("Invalid parameter to stringToKey");
    byte[] arrayOfByte1 = null;
    byte[] arrayOfByte2 = null;
    try
    {
      arrayOfByte1 = charToUtf16(paramArrayOfChar);
      MessageDigest localMessageDigest = MD4.getInstance();
      localMessageDigest.update(arrayOfByte1);
      arrayOfByte2 = localMessageDigest.digest();
    }
    catch (Exception localException)
    {
      Object localObject1 = null;
      return localObject1;
    }
    finally
    {
      if (arrayOfByte1 != null)
        Arrays.fill(arrayOfByte1, 0);
    }
    return arrayOfByte2;
  }

  protected Cipher getCipher(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt)
    throws GeneralSecurityException
  {
    if (paramArrayOfByte2 == null)
      paramArrayOfByte2 = ZERO_IV;
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(paramArrayOfByte1, "ARCFOUR");
    Cipher localCipher = Cipher.getInstance("ARCFOUR");
    IvParameterSpec localIvParameterSpec = new IvParameterSpec(paramArrayOfByte2, 0, paramArrayOfByte2.length);
    localCipher.init(paramInt, localSecretKeySpec, localIvParameterSpec);
    return localCipher;
  }

  public int getChecksumLength()
  {
    return 16;
  }

  protected byte[] getHmac(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws GeneralSecurityException
  {
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(paramArrayOfByte1, "HmacMD5");
    Mac localMac = Mac.getInstance("HmacMD5");
    localMac.init(localSecretKeySpec);
    byte[] arrayOfByte = localMac.doFinal(paramArrayOfByte2);
    return arrayOfByte;
  }

  public byte[] calculateChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2, int paramInt3)
    throws GeneralSecurityException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = null;
    try
    {
      byte[] arrayOfByte2 = "signaturekey".getBytes();
      localObject1 = new byte[arrayOfByte2.length + 1];
      System.arraycopy(arrayOfByte2, 0, localObject1, 0, arrayOfByte2.length);
      arrayOfByte1 = getHmac(paramArrayOfByte1, localObject1);
    }
    catch (Exception localException)
    {
      localObject1 = new GeneralSecurityException("Calculate Checkum Failed!");
      ((GeneralSecurityException)localObject1).initCause(localException);
      throw ((Throwable)localObject1);
    }
    byte[] arrayOfByte3 = getSalt(paramInt1);
    Object localObject1 = null;
    try
    {
      localObject1 = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      localObject2 = new GeneralSecurityException("Calculate Checkum Failed!");
      ((GeneralSecurityException)localObject2).initCause(localNoSuchAlgorithmException);
      throw ((Throwable)localObject2);
    }
    ((MessageDigest)localObject1).update(arrayOfByte3);
    ((MessageDigest)localObject1).update(paramArrayOfByte2, paramInt2, paramInt3);
    byte[] arrayOfByte4 = ((MessageDigest)localObject1).digest();
    Object localObject2 = getHmac(arrayOfByte1, arrayOfByte4);
    if (localObject2.length == getChecksumLength())
      return localObject2;
    if (localObject2.length > getChecksumLength())
    {
      byte[] arrayOfByte5 = new byte[getChecksumLength()];
      System.arraycopy(localObject2, 0, arrayOfByte5, 0, arrayOfByte5.length);
      return arrayOfByte5;
    }
    throw new GeneralSecurityException("checksum size too short: " + localObject2.length + "; expecting : " + getChecksumLength());
  }

  public byte[] encryptSeq(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException, KrbCryptoException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = new byte[4];
    byte[] arrayOfByte2 = getHmac(paramArrayOfByte1, arrayOfByte1);
    arrayOfByte2 = getHmac(arrayOfByte2, paramArrayOfByte2);
    Cipher localCipher = Cipher.getInstance("ARCFOUR");
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte2, "ARCFOUR");
    localCipher.init(1, localSecretKeySpec);
    byte[] arrayOfByte3 = localCipher.doFinal(paramArrayOfByte3, paramInt2, paramInt3);
    return arrayOfByte3;
  }

  public byte[] decryptSeq(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException, KrbCryptoException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = new byte[4];
    byte[] arrayOfByte2 = getHmac(paramArrayOfByte1, arrayOfByte1);
    arrayOfByte2 = getHmac(arrayOfByte2, paramArrayOfByte2);
    Cipher localCipher = Cipher.getInstance("ARCFOUR");
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte2, "ARCFOUR");
    localCipher.init(2, localSecretKeySpec);
    byte[] arrayOfByte3 = localCipher.doFinal(paramArrayOfByte3, paramInt2, paramInt3);
    return arrayOfByte3;
  }

  public byte[] encrypt(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4, int paramInt2, int paramInt3)
    throws GeneralSecurityException, KrbCryptoException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = Confounder.bytes(8);
    int i = roundup(arrayOfByte1.length + paramInt3, 1);
    byte[] arrayOfByte2 = new byte[i];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
    System.arraycopy(paramArrayOfByte4, paramInt2, arrayOfByte2, arrayOfByte1.length, paramInt3);
    byte[] arrayOfByte3 = new byte[paramArrayOfByte1.length];
    System.arraycopy(paramArrayOfByte1, 0, arrayOfByte3, 0, paramArrayOfByte1.length);
    byte[] arrayOfByte4 = getSalt(paramInt1);
    byte[] arrayOfByte5 = getHmac(arrayOfByte3, arrayOfByte4);
    byte[] arrayOfByte6 = getHmac(arrayOfByte5, arrayOfByte2);
    byte[] arrayOfByte7 = getHmac(arrayOfByte5, arrayOfByte6);
    Cipher localCipher = Cipher.getInstance("ARCFOUR");
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte7, "ARCFOUR");
    localCipher.init(1, localSecretKeySpec);
    byte[] arrayOfByte8 = localCipher.doFinal(arrayOfByte2, 0, arrayOfByte2.length);
    byte[] arrayOfByte9 = new byte[16 + arrayOfByte8.length];
    System.arraycopy(arrayOfByte6, 0, arrayOfByte9, 0, 16);
    System.arraycopy(arrayOfByte8, 0, arrayOfByte9, 16, arrayOfByte8.length);
    return arrayOfByte9;
  }

  public byte[] encryptRaw(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException, KrbCryptoException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = new byte[paramArrayOfByte1.length];
    for (int i = 0; i <= 15; ++i)
      arrayOfByte1[i] = (byte)(paramArrayOfByte1[i] ^ 0xF0);
    byte[] arrayOfByte2 = new byte[4];
    byte[] arrayOfByte3 = getHmac(arrayOfByte1, arrayOfByte2);
    arrayOfByte3 = getHmac(arrayOfByte3, paramArrayOfByte2);
    Cipher localCipher = Cipher.getInstance("ARCFOUR");
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte3, "ARCFOUR");
    localCipher.init(1, localSecretKeySpec);
    byte[] arrayOfByte4 = localCipher.doFinal(paramArrayOfByte3, paramInt2, paramInt3);
    return arrayOfByte4;
  }

  public byte[] decrypt(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = new byte[paramArrayOfByte1.length];
    System.arraycopy(paramArrayOfByte1, 0, arrayOfByte1, 0, paramArrayOfByte1.length);
    byte[] arrayOfByte2 = getSalt(paramInt1);
    byte[] arrayOfByte3 = getHmac(arrayOfByte1, arrayOfByte2);
    byte[] arrayOfByte4 = new byte[16];
    System.arraycopy(paramArrayOfByte3, paramInt2, arrayOfByte4, 0, 16);
    byte[] arrayOfByte5 = getHmac(arrayOfByte3, arrayOfByte4);
    Cipher localCipher = Cipher.getInstance("ARCFOUR");
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte5, "ARCFOUR");
    localCipher.init(2, localSecretKeySpec);
    byte[] arrayOfByte6 = localCipher.doFinal(paramArrayOfByte3, paramInt2 + 16, paramInt3 - 16);
    byte[] arrayOfByte7 = getHmac(arrayOfByte3, arrayOfByte6);
    int i = 0;
    if (arrayOfByte7.length >= 16)
      for (int j = 0; j < 16; ++j)
        if (arrayOfByte7[j] != paramArrayOfByte3[j])
        {
          i = 1;
          System.err.println("Checksum failed !");
          break;
        }
    if (i != 0)
      throw new GeneralSecurityException("Checksum failed");
    byte[] arrayOfByte8 = new byte[arrayOfByte6.length - 8];
    System.arraycopy(arrayOfByte6, 8, arrayOfByte8, 0, arrayOfByte8.length);
    return arrayOfByte8;
  }

  public byte[] decryptRaw(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3, byte[] paramArrayOfByte4)
    throws GeneralSecurityException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = new byte[paramArrayOfByte1.length];
    for (int i = 0; i <= 15; ++i)
      arrayOfByte1[i] = (byte)(paramArrayOfByte1[i] ^ 0xF0);
    byte[] arrayOfByte2 = new byte[4];
    byte[] arrayOfByte3 = getHmac(arrayOfByte1, arrayOfByte2);
    byte[] arrayOfByte4 = new byte[4];
    System.arraycopy(paramArrayOfByte4, 0, arrayOfByte4, 0, arrayOfByte4.length);
    arrayOfByte3 = getHmac(arrayOfByte3, arrayOfByte4);
    Cipher localCipher = Cipher.getInstance("ARCFOUR");
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte3, "ARCFOUR");
    localCipher.init(2, localSecretKeySpec);
    byte[] arrayOfByte5 = localCipher.doFinal(paramArrayOfByte3, paramInt2, paramInt3);
    return arrayOfByte5;
  }

  private byte[] getSalt(int paramInt)
  {
    int i = arcfour_translate_usage(paramInt);
    byte[] arrayOfByte = new byte[4];
    arrayOfByte[0] = (byte)(i & 0xFF);
    arrayOfByte[1] = (byte)(i >> 8 & 0xFF);
    arrayOfByte[2] = (byte)(i >> 16 & 0xFF);
    arrayOfByte[3] = (byte)(i >> 24 & 0xFF);
    return arrayOfByte;
  }

  private int arcfour_translate_usage(int paramInt)
  {
    switch (paramInt)
    {
    case 3:
      return 8;
    case 9:
      return 8;
    case 23:
      return 13;
    }
    return paramInt;
  }
}