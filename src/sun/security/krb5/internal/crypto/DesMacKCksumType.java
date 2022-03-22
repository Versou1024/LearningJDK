package sun.security.krb5.internal.crypto;

import java.security.InvalidKeyException;
import javax.crypto.spec.DESKeySpec;
import sun.security.krb5.KrbCryptoException;

public class DesMacKCksumType extends CksumType
{
  public int confounderSize()
  {
    return 0;
  }

  public int cksumType()
  {
    return 5;
  }

  public boolean isSafe()
  {
    return true;
  }

  public int cksumSize()
  {
    return 16;
  }

  public int keyType()
  {
    return 1;
  }

  public int keySize()
  {
    return 8;
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
      if (DESKeySpec.isWeak(paramArrayOfByte2, 0))
        paramArrayOfByte2[7] = (byte)(paramArrayOfByte2[7] ^ 0xF0);
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
    }
    byte[] arrayOfByte1 = new byte[paramArrayOfByte2.length];
    System.arraycopy(paramArrayOfByte2, 0, arrayOfByte1, 0, paramArrayOfByte2.length);
    byte[] arrayOfByte2 = Des.des_cksum(arrayOfByte1, paramArrayOfByte1, paramArrayOfByte2);
    return arrayOfByte2;
  }

  public boolean verifyKeyedChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2)
    throws KrbCryptoException
  {
    byte[] arrayOfByte = calculateKeyedChecksum(paramArrayOfByte1, paramArrayOfByte1.length, paramArrayOfByte2, paramInt2);
    return isChecksumEqual(paramArrayOfByte3, arrayOfByte);
  }
}