package sun.security.krb5.internal.crypto;

import java.security.GeneralSecurityException;
import sun.security.krb5.KrbCryptoException;

public class HmacSha1Aes256CksumType extends CksumType
{
  public int confounderSize()
  {
    return 16;
  }

  public int cksumType()
  {
    return 16;
  }

  public boolean isSafe()
  {
    return true;
  }

  public int cksumSize()
  {
    return 12;
  }

  public int keyType()
  {
    return 3;
  }

  public int keySize()
  {
    return 32;
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
      return Aes256.calculateChecksum(paramArrayOfByte2, paramInt2, paramArrayOfByte1, 0, paramInt1);
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
      arrayOfByte = Aes256.calculateChecksum(paramArrayOfByte2, paramInt2, paramArrayOfByte1, 0, paramInt1);
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