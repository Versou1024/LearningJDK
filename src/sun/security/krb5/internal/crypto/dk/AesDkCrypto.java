package sun.security.krb5.internal.crypto.dk;

import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.internal.crypto.Confounder;
import sun.security.krb5.internal.crypto.KeyUsage;

public class AesDkCrypto extends DkCrypto
{
  private static final boolean debug = 0;
  private static final int BLOCK_SIZE = 16;
  private static final int DEFAULT_ITERATION_COUNT = 4096;
  private static final byte[] ZERO_IV = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
  private static final int hashSize = 12;
  private final int keyLength;

  public AesDkCrypto(int paramInt)
  {
    this.keyLength = paramInt;
  }

  protected int getKeySeedLength()
  {
    return this.keyLength;
  }

  public byte[] stringToKey(char[] paramArrayOfChar, String paramString, byte[] paramArrayOfByte)
    throws GeneralSecurityException
  {
    byte[] arrayOfByte1 = null;
    try
    {
      arrayOfByte1 = paramString.getBytes("UTF-8");
      byte[] arrayOfByte2 = stringToKey(paramArrayOfChar, arrayOfByte1, paramArrayOfByte);
      return arrayOfByte2;
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
  }

  private byte[] stringToKey(char[] paramArrayOfChar, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws GeneralSecurityException
  {
    int i = 4096;
    if (paramArrayOfByte2 != null)
    {
      if (paramArrayOfByte2.length != 4)
        throw new RuntimeException("Invalid parameter to stringToKey");
      i = readBigEndian(paramArrayOfByte2, 0, 4);
    }
    byte[] arrayOfByte1 = randomToKey(PBKDF2(paramArrayOfChar, paramArrayOfByte1, i, getKeySeedLength()));
    byte[] arrayOfByte2 = dk(arrayOfByte1, KERBEROS_CONSTANT);
    return arrayOfByte2;
  }

  protected byte[] randomToKey(byte[] paramArrayOfByte)
  {
    return paramArrayOfByte;
  }

  protected Cipher getCipher(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt)
    throws GeneralSecurityException
  {
    if (paramArrayOfByte2 == null)
      paramArrayOfByte2 = ZERO_IV;
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(paramArrayOfByte1, "AES");
    Cipher localCipher = Cipher.getInstance("AES/CBC/NoPadding");
    IvParameterSpec localIvParameterSpec = new IvParameterSpec(paramArrayOfByte2, 0, paramArrayOfByte2.length);
    localCipher.init(paramInt, localSecretKeySpec, localIvParameterSpec);
    return localCipher;
  }

  public int getChecksumLength()
  {
    return 12;
  }

  protected byte[] getHmac(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws GeneralSecurityException
  {
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(paramArrayOfByte1, "HMAC");
    Mac localMac = Mac.getInstance("HmacSHA1");
    localMac.init(localSecretKeySpec);
    byte[] arrayOfByte1 = localMac.doFinal(paramArrayOfByte2);
    byte[] arrayOfByte2 = new byte[12];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, 12);
    return arrayOfByte2;
  }

  public byte[] calculateChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2, int paramInt3)
    throws GeneralSecurityException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = new byte[5];
    arrayOfByte1[0] = (byte)(paramInt1 >> 24 & 0xFF);
    arrayOfByte1[1] = (byte)(paramInt1 >> 16 & 0xFF);
    arrayOfByte1[2] = (byte)(paramInt1 >> 8 & 0xFF);
    arrayOfByte1[3] = (byte)(paramInt1 & 0xFF);
    arrayOfByte1[4] = -103;
    byte[] arrayOfByte2 = dk(paramArrayOfByte1, arrayOfByte1);
    try
    {
      byte[] arrayOfByte4;
      byte[] arrayOfByte5;
      byte[] arrayOfByte3 = getHmac(arrayOfByte2, paramArrayOfByte2);
      if (arrayOfByte3.length == getChecksumLength())
      {
        arrayOfByte4 = arrayOfByte3;
        return arrayOfByte4;
      }
      if (arrayOfByte3.length > getChecksumLength())
      {
        arrayOfByte4 = new byte[getChecksumLength()];
        System.arraycopy(arrayOfByte3, 0, arrayOfByte4, 0, arrayOfByte4.length);
        arrayOfByte5 = arrayOfByte4;
        Arrays.fill(arrayOfByte2, 0, arrayOfByte2.length, 0);
      }
      throw new GeneralSecurityException("checksum size too short: " + arrayOfByte3.length + "; expecting : " + getChecksumLength());
    }
    finally
    {
      Arrays.fill(arrayOfByte2, 0, arrayOfByte2.length, 0);
    }
  }

  public byte[] encrypt(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4, int paramInt2, int paramInt3)
    throws GeneralSecurityException, KrbCryptoException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte = encryptCTS(paramArrayOfByte1, paramInt1, paramArrayOfByte2, paramArrayOfByte3, paramArrayOfByte4, paramInt2, paramInt3, true);
    return arrayOfByte;
  }

  public byte[] encryptRaw(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException, KrbCryptoException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte = encryptCTS(paramArrayOfByte1, paramInt1, paramArrayOfByte2, null, paramArrayOfByte3, paramInt2, paramInt3, false);
    return arrayOfByte;
  }

  public byte[] decrypt(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte = decryptCTS(paramArrayOfByte1, paramInt1, paramArrayOfByte2, paramArrayOfByte3, paramInt2, paramInt3, true);
    return arrayOfByte;
  }

  public byte[] decryptRaw(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte = decryptCTS(paramArrayOfByte1, paramInt1, paramArrayOfByte2, paramArrayOfByte3, paramInt2, paramInt3, false);
    return arrayOfByte;
  }

  private byte[] encryptCTS(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4, int paramInt2, int paramInt3, boolean paramBoolean)
    throws GeneralSecurityException, KrbCryptoException
  {
    byte[] arrayOfByte1 = null;
    byte[] arrayOfByte2 = null;
    try
    {
      byte[] arrayOfByte3 = new byte[5];
      arrayOfByte3[0] = (byte)(paramInt1 >> 24 & 0xFF);
      arrayOfByte3[1] = (byte)(paramInt1 >> 16 & 0xFF);
      arrayOfByte3[2] = (byte)(paramInt1 >> 8 & 0xFF);
      arrayOfByte3[3] = (byte)(paramInt1 & 0xFF);
      arrayOfByte3[4] = -86;
      arrayOfByte1 = dk(paramArrayOfByte1, arrayOfByte3);
      byte[] arrayOfByte4 = null;
      if (paramBoolean)
      {
        arrayOfByte5 = Confounder.bytes(16);
        arrayOfByte4 = new byte[arrayOfByte5.length + paramInt3];
        System.arraycopy(arrayOfByte5, 0, arrayOfByte4, 0, arrayOfByte5.length);
        System.arraycopy(paramArrayOfByte4, paramInt2, arrayOfByte4, arrayOfByte5.length, paramInt3);
      }
      else
      {
        arrayOfByte4 = new byte[paramInt3];
        System.arraycopy(paramArrayOfByte4, paramInt2, arrayOfByte4, 0, paramInt3);
      }
      byte[] arrayOfByte5 = new byte[arrayOfByte4.length + 12];
      Cipher localCipher = Cipher.getInstance("AES/CTS/NoPadding");
      SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte1, "AES");
      IvParameterSpec localIvParameterSpec = new IvParameterSpec(paramArrayOfByte2, 0, paramArrayOfByte2.length);
      localCipher.init(1, localSecretKeySpec, localIvParameterSpec);
      localCipher.doFinal(arrayOfByte4, 0, arrayOfByte4.length, arrayOfByte5);
      arrayOfByte3[4] = 85;
      arrayOfByte2 = dk(paramArrayOfByte1, arrayOfByte3);
      byte[] arrayOfByte6 = getHmac(arrayOfByte2, arrayOfByte4);
      System.arraycopy(arrayOfByte6, 0, arrayOfByte5, arrayOfByte4.length, arrayOfByte6.length);
      byte[] arrayOfByte7 = arrayOfByte5;
      return arrayOfByte7;
    }
    finally
    {
      if (arrayOfByte1 != null)
        Arrays.fill(arrayOfByte1, 0, arrayOfByte1.length, 0);
      if (arrayOfByte2 != null)
        Arrays.fill(arrayOfByte2, 0, arrayOfByte2.length, 0);
    }
  }

  private byte[] decryptCTS(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3, boolean paramBoolean)
    throws GeneralSecurityException
  {
    byte[] arrayOfByte1 = null;
    byte[] arrayOfByte2 = null;
    try
    {
      byte[] arrayOfByte3 = new byte[5];
      arrayOfByte3[0] = (byte)(paramInt1 >> 24 & 0xFF);
      arrayOfByte3[1] = (byte)(paramInt1 >> 16 & 0xFF);
      arrayOfByte3[2] = (byte)(paramInt1 >> 8 & 0xFF);
      arrayOfByte3[3] = (byte)(paramInt1 & 0xFF);
      arrayOfByte3[4] = -86;
      arrayOfByte1 = dk(paramArrayOfByte1, arrayOfByte3);
      Cipher localCipher = Cipher.getInstance("AES/CTS/NoPadding");
      SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte1, "AES");
      IvParameterSpec localIvParameterSpec = new IvParameterSpec(paramArrayOfByte2, 0, paramArrayOfByte2.length);
      localCipher.init(2, localSecretKeySpec, localIvParameterSpec);
      byte[] arrayOfByte4 = localCipher.doFinal(paramArrayOfByte3, paramInt2, paramInt3 - 12);
      arrayOfByte3[4] = 85;
      arrayOfByte2 = dk(paramArrayOfByte1, arrayOfByte3);
      byte[] arrayOfByte5 = getHmac(arrayOfByte2, arrayOfByte4);
      int i = paramInt2 + paramInt3 - 12;
      int j = 0;
      if (arrayOfByte5.length >= 12)
        for (int k = 0; k < 12; ++k)
          if (arrayOfByte5[k] != paramArrayOfByte3[(i + k)])
          {
            j = 1;
            System.err.println("Checksum failed !");
            break;
          }
      if (j != 0)
        throw new GeneralSecurityException("Checksum failed");
      if (paramBoolean)
      {
        arrayOfByte6 = new byte[arrayOfByte4.length - 16];
        System.arraycopy(arrayOfByte4, 16, arrayOfByte6, 0, arrayOfByte6.length);
        byte[] arrayOfByte7 = arrayOfByte6;
        return arrayOfByte7;
      }
      byte[] arrayOfByte6 = arrayOfByte4;
      return arrayOfByte6;
    }
    finally
    {
      if (arrayOfByte1 != null)
        Arrays.fill(arrayOfByte1, 0, arrayOfByte1.length, 0);
      if (arrayOfByte2 != null)
        Arrays.fill(arrayOfByte2, 0, arrayOfByte2.length, 0);
    }
  }

  private static byte[] PBKDF2(char[] paramArrayOfChar, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws GeneralSecurityException
  {
    PBEKeySpec localPBEKeySpec = new PBEKeySpec(paramArrayOfChar, paramArrayOfByte, paramInt1, paramInt2);
    SecretKeyFactory localSecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    SecretKey localSecretKey = localSecretKeyFactory.generateSecret(localPBEKeySpec);
    byte[] arrayOfByte = localSecretKey.getEncoded();
    return arrayOfByte;
  }

  public static final int readBigEndian(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    int i = 0;
    int j = (paramInt2 - 1) * 8;
    while (paramInt2 > 0)
    {
      i += ((paramArrayOfByte[paramInt1] & 0xFF) << j);
      j -= 8;
      ++paramInt1;
      --paramInt2;
    }
    return i;
  }
}