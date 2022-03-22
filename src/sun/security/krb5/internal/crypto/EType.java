package sun.security.krb5.internal.crypto;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import sun.security.krb5.Config;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.KrbException;
import sun.security.krb5.internal.KdcErrException;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.KrbApErrException;

public abstract class EType
{
  private static final boolean DEBUG = Krb5.DEBUG;
  private static final int[] BUILTIN_ETYPES = { 3, 1, 23, 16, 17, 18 };
  private static final int[] BUILTIN_ETYPES_NOAES256 = { 3, 1, 23, 16, 17 };

  public static EType getInstance(int paramInt)
    throws KdcErrException
  {
    Object localObject = null;
    String str1 = null;
    switch (paramInt)
    {
    case 0:
      localObject = new NullEType();
      str1 = "sun.security.krb5.internal.crypto.NullEType";
      break;
    case 1:
      localObject = new DesCbcCrcEType();
      str1 = "sun.security.krb5.internal.crypto.DesCbcCrcEType";
      break;
    case 3:
      localObject = new DesCbcMd5EType();
      str1 = "sun.security.krb5.internal.crypto.DesCbcMd5EType";
      break;
    case 16:
      localObject = new Des3CbcHmacSha1KdEType();
      str1 = "sun.security.krb5.internal.crypto.Des3CbcHmacSha1KdEType";
      break;
    case 17:
      localObject = new Aes128CtsHmacSha1EType();
      str1 = "sun.security.krb5.internal.crypto.Aes128CtsHmacSha1EType";
      break;
    case 18:
      localObject = new Aes256CtsHmacSha1EType();
      str1 = "sun.security.krb5.internal.crypto.Aes256CtsHmacSha1EType";
      break;
    case 23:
      localObject = new ArcFourHmacEType();
      str1 = "sun.security.krb5.internal.crypto.ArcFourHmacEType";
      break;
    case 2:
    case 4:
    case 5:
    case 6:
    case 7:
    case 8:
    case 9:
    case 10:
    case 11:
    case 12:
    case 13:
    case 14:
    case 15:
    case 19:
    case 20:
    case 21:
    case 22:
    default:
      String str2 = "encryption type = " + toString(paramInt) + " (" + paramInt + ")";
      throw new KdcErrException(14, str2);
    }
    if (DEBUG)
      System.out.println(">>> EType: " + str1);
    return ((EType)localObject);
  }

  public abstract int eType();

  public abstract int minimumPadSize();

  public abstract int confounderSize();

  public abstract int checksumType();

  public abstract int checksumSize();

  public abstract int blockSize();

  public abstract int keyType();

  public abstract int keySize();

  public abstract byte[] encrypt(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt)
    throws KrbCryptoException;

  public abstract byte[] encrypt(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt)
    throws KrbCryptoException;

  public abstract byte[] decrypt(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt)
    throws KrbApErrException, KrbCryptoException;

  public abstract byte[] decrypt(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt)
    throws KrbApErrException, KrbCryptoException;

  public int dataSize(byte[] paramArrayOfByte)
  {
    return (paramArrayOfByte.length - startOfData());
  }

  public int padSize(byte[] paramArrayOfByte)
  {
    return (paramArrayOfByte.length - confounderSize() - checksumSize() - dataSize(paramArrayOfByte));
  }

  public int startOfChecksum()
  {
    return confounderSize();
  }

  public int startOfData()
  {
    return (confounderSize() + checksumSize());
  }

  public int startOfPad(byte[] paramArrayOfByte)
  {
    return (confounderSize() + checksumSize() + dataSize(paramArrayOfByte));
  }

  public byte[] decryptedData(byte[] paramArrayOfByte)
  {
    int i = dataSize(paramArrayOfByte);
    byte[] arrayOfByte = new byte[i];
    System.arraycopy(paramArrayOfByte, startOfData(), arrayOfByte, 0, i);
    return arrayOfByte;
  }

  public static int[] getBuiltInDefaults()
  {
    int i = 0;
    try
    {
      i = Cipher.getMaxAllowedKeyLength("AES");
    }
    catch (Exception localException)
    {
    }
    if (i < 256)
      return BUILTIN_ETYPES_NOAES256;
    return BUILTIN_ETYPES;
  }

  public static int[] getDefaults(String paramString)
  {
    try
    {
      return Config.getInstance().defaultEtype(paramString);
    }
    catch (KrbException localKrbException)
    {
      if (DEBUG)
      {
        System.out.println("Exception while getting " + paramString + localKrbException.getMessage());
        System.out.println("Using defaults des-cbc-md5, des-cbc-crc, des3-cbc-sha1, aes128cts, aes256cts, rc4-hmac");
      }
    }
    return getBuiltInDefaults();
  }

  public static int[] getDefaults(String paramString, EncryptionKey[] paramArrayOfEncryptionKey)
    throws KrbException
  {
    int[] arrayOfInt = getDefaults(paramString);
    if (arrayOfInt == null)
      throw new KrbException("No supported encryption types listed in " + paramString);
    ArrayList localArrayList = new ArrayList(arrayOfInt.length);
    for (int i = 0; i < arrayOfInt.length; ++i)
      if (EncryptionKey.findKey(arrayOfInt[i], paramArrayOfEncryptionKey) != null)
        localArrayList.add(new Integer(arrayOfInt[i]));
    i = localArrayList.size();
    if (i <= 0)
    {
      StringBuffer localStringBuffer = new StringBuffer();
      for (int k = 0; k < paramArrayOfEncryptionKey.length; ++k)
      {
        localStringBuffer.append(toString(paramArrayOfEncryptionKey[k].getEType()));
        localStringBuffer.append(" ");
      }
      throw new KrbException("Do not have keys of types listed in " + paramString + " available; only have keys of following type: " + localStringBuffer.toString());
    }
    arrayOfInt = new int[i];
    for (int j = 0; j < i; ++j)
      arrayOfInt[j] = ((Integer)(Integer)localArrayList.get(j)).intValue();
    return arrayOfInt;
  }

  public static boolean isSupported(int paramInt, int[] paramArrayOfInt)
  {
    for (int i = 0; i < paramArrayOfInt.length; ++i)
      if (paramInt == paramArrayOfInt[i])
        return true;
    return false;
  }

  public static boolean isSupported(int paramInt)
  {
    int[] arrayOfInt = getBuiltInDefaults();
    return isSupported(paramInt, arrayOfInt);
  }

  public static String toString(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return "NULL";
    case 1:
      return "DES CBC mode with CRC-32";
    case 2:
      return "DES CBC mode with MD4";
    case 3:
      return "DES CBC mode with MD5";
    case 4:
      return "reserved";
    case 5:
      return "DES3 CBC mode with MD5";
    case 6:
      return "reserved";
    case 7:
      return "DES3 CBC mode with SHA1";
    case 9:
      return "DSA with SHA1- Cms0ID";
    case 10:
      return "MD5 with RSA encryption - Cms0ID";
    case 11:
      return "SHA1 with RSA encryption - Cms0ID";
    case 12:
      return "RC2 CBC mode with Env0ID";
    case 13:
      return "RSA encryption with Env0ID";
    case 14:
      return "RSAES-0AEP-ENV-0ID";
    case 15:
      return "DES-EDE3-CBC-ENV-0ID";
    case 16:
      return "DES3 CBC mode with SHA1-KD";
    case 17:
      return "AES128 CTS mode with HMAC SHA1-96";
    case 18:
      return "AES256 CTS mode with HMAC SHA1-96";
    case 23:
      return "RC4 with HMAC";
    case 24:
      return "RC4 with HMAC EXP";
    case 8:
    case 19:
    case 20:
    case 21:
    case 22:
    }
    return "Unknown (" + paramInt + ")";
  }
}