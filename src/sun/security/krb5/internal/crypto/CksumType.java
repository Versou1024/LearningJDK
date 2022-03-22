package sun.security.krb5.internal.crypto;

import java.io.PrintStream;
import sun.security.krb5.Config;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.KrbException;
import sun.security.krb5.internal.KdcErrException;
import sun.security.krb5.internal.Krb5;

public abstract class CksumType
{
  private static boolean DEBUG = Krb5.DEBUG;

  public static CksumType getInstance(int paramInt)
    throws KdcErrException
  {
    Object localObject = null;
    String str = null;
    switch (paramInt)
    {
    case 1:
      localObject = new Crc32CksumType();
      str = "sun.security.krb5.internal.crypto.Crc32CksumType";
      break;
    case 4:
      localObject = new DesMacCksumType();
      str = "sun.security.krb5.internal.crypto.DesMacCksumType";
      break;
    case 5:
      localObject = new DesMacKCksumType();
      str = "sun.security.krb5.internal.crypto.DesMacKCksumType";
      break;
    case 7:
      localObject = new RsaMd5CksumType();
      str = "sun.security.krb5.internal.crypto.RsaMd5CksumType";
      break;
    case 8:
      localObject = new RsaMd5DesCksumType();
      str = "sun.security.krb5.internal.crypto.RsaMd5DesCksumType";
      break;
    case 12:
      localObject = new HmacSha1Des3KdCksumType();
      str = "sun.security.krb5.internal.crypto.HmacSha1Des3KdCksumType";
      break;
    case 15:
      localObject = new HmacSha1Aes128CksumType();
      str = "sun.security.krb5.internal.crypto.HmacSha1Aes128CksumType";
      break;
    case 16:
      localObject = new HmacSha1Aes256CksumType();
      str = "sun.security.krb5.internal.crypto.HmacSha1Aes256CksumType";
      break;
    case -138:
      localObject = new HmacMd5ArcFourCksumType();
      str = "sun.security.krb5.internal.crypto.HmacMd5ArcFourCksumType";
      break;
    case 2:
    case 3:
    case 6:
    default:
      throw new KdcErrException(15);
    }
    if (DEBUG)
      System.out.println(">>> CksumType: " + str);
    return ((CksumType)localObject);
  }

  public static CksumType getInstance()
    throws KdcErrException
  {
    int i = 7;
    try
    {
      Config localConfig = Config.getInstance();
      if ((i = localConfig.getType(localConfig.getDefault("ap_req_checksum_type", "libdefaults"))) == -1)
        if ((i = localConfig.getType(localConfig.getDefault("checksum_type", "libdefaults"))) == -1)
          i = 7;
    }
    catch (KrbException localKrbException)
    {
    }
    return getInstance(i);
  }

  public abstract int confounderSize();

  public abstract int cksumType();

  public abstract boolean isSafe();

  public abstract int cksumSize();

  public abstract int keyType();

  public abstract int keySize();

  public abstract byte[] calculateChecksum(byte[] paramArrayOfByte, int paramInt)
    throws KrbCryptoException;

  public abstract byte[] calculateKeyedChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2)
    throws KrbCryptoException;

  public abstract boolean verifyKeyedChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2)
    throws KrbCryptoException;

  public static boolean isChecksumEqual(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    if (paramArrayOfByte1 == paramArrayOfByte2)
      return true;
    if (((paramArrayOfByte1 == null) && (paramArrayOfByte2 != null)) || ((paramArrayOfByte1 != null) && (paramArrayOfByte2 == null)))
      return false;
    if (paramArrayOfByte1.length != paramArrayOfByte2.length)
      return false;
    for (int i = 0; i < paramArrayOfByte1.length; ++i)
      if (paramArrayOfByte1[i] != paramArrayOfByte2[i])
        return false;
    return true;
  }
}