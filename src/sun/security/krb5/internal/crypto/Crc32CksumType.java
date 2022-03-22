package sun.security.krb5.internal.crypto;

public class Crc32CksumType extends CksumType
{
  public int confounderSize()
  {
    return 0;
  }

  public int cksumType()
  {
    return 1;
  }

  public boolean isSafe()
  {
    return false;
  }

  public int cksumSize()
  {
    return 4;
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
  {
    return crc32.byte2crc32sum_bytes(paramArrayOfByte, paramInt);
  }

  public byte[] calculateKeyedChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2)
  {
    return null;
  }

  public boolean verifyKeyedChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2)
  {
    return false;
  }

  public static byte[] int2quad(long paramLong)
  {
    byte[] arrayOfByte = new byte[4];
    for (int i = 0; i < 4; ++i)
      arrayOfByte[i] = (byte)(int)(paramLong >>> i * 8 & 0xFF);
    return arrayOfByte;
  }

  public static long bytes2long(byte[] paramArrayOfByte)
  {
    long l = 3412047153814568960L;
    l |= (paramArrayOfByte[0] & 0xFF) << 24;
    l |= (paramArrayOfByte[1] & 0xFF) << 16;
    l |= (paramArrayOfByte[2] & 0xFF) << 8;
    l |= paramArrayOfByte[3] & 0xFF;
    return l;
  }
}