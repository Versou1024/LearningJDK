package sun.security.krb5.internal.crypto;

import java.security.MessageDigest;
import sun.security.krb5.KrbCryptoException;

public final class RsaMd5CksumType extends CksumType
{
  public int confounderSize()
  {
    return 0;
  }

  public int cksumType()
  {
    return 7;
  }

  public boolean isSafe()
  {
    return false;
  }

  public int cksumSize()
  {
    return 16;
  }

  public int keyType()
  {
    return 0;
  }

  public int keySize()
  {
    return 0;
  }

  public byte[] calculateChecksum(byte[] paramArrayOfByte, int paramInt)
    throws KrbCryptoException
  {
    MessageDigest localMessageDigest;
    byte[] arrayOfByte = null;
    try
    {
      localMessageDigest = MessageDigest.getInstance("MD5");
    }
    catch (Exception localException1)
    {
      throw new KrbCryptoException("JCE provider may not be installed. " + localException1.getMessage());
    }
    try
    {
      localMessageDigest.update(paramArrayOfByte);
      arrayOfByte = localMessageDigest.digest();
    }
    catch (Exception localException2)
    {
      throw new KrbCryptoException(localException2.getMessage());
    }
    return arrayOfByte;
  }

  public byte[] calculateKeyedChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2)
    throws KrbCryptoException
  {
    return null;
  }

  public boolean verifyKeyedChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2)
    throws KrbCryptoException
  {
    return false;
  }
}