package sun.security.krb5.internal.crypto;

import java.security.GeneralSecurityException;
import sun.security.krb5.KrbCryptoException;

public class HmacSha1Des3KdCksumType extends CksumType
{
  public int confounderSize()
  {
    return 8;
  }

  public int cksumType()
  {
    return 12;
  }

  public boolean isSafe()
  {
    return true;
  }

  public int cksumSize()
  {
    return 20;
  }

  public int keyType()
  {
    return 2;
  }

  public int keySize()
  {
    return 24;
  }

  public byte[] calculateChecksum(byte[] paramArrayOfByte, int paramInt)
  {
    return null;
  }

  public byte[] calculateKeyedChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2)
    throws KrbCryptoException
  {
    try
    {
      return Des3.calculateChecksum(paramArrayOfByte2, paramInt2, paramArrayOfByte1, 0, paramInt1);
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      KrbCryptoException localKrbCryptoException = new KrbCryptoException(localGeneralSecurityException.getMessage());
      localKrbCryptoException.initCause(localGeneralSecurityException);
      throw localKrbCryptoException;
    }
  }

  public boolean verifyKeyedChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2)
    throws KrbCryptoException
  {
    byte[] arrayOfByte;
    try
    {
      arrayOfByte = Des3.calculateChecksum(paramArrayOfByte2, paramInt2, paramArrayOfByte1, 0, paramInt1);
      return isChecksumEqual(paramArrayOfByte3, arrayOfByte);
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      KrbCryptoException localKrbCryptoException = new KrbCryptoException(localGeneralSecurityException.getMessage());
      localKrbCryptoException.initCause(localGeneralSecurityException);
      throw localKrbCryptoException;
    }
  }
}