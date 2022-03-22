package sun.security.krb5.internal.crypto;

import java.security.MessageDigest;
import java.util.Random;
import sun.security.krb5.KrbCryptoException;

public final class Confounder
{
  private static long count = 3412045521726996480L;
  private static long index = 3412045521726996480L;
  private static long lastKeyTime = 3412045521726996480L;
  private static Random rand = new Random();
  private static byte[] buffer = new byte[16];

  private static synchronized void reinitialize()
    throws KrbCryptoException
  {
    long l = System.currentTimeMillis();
    rand.setSeed(lastKeyTime + l + count + index);
    count += 3412047050735353857L;
    for (int i = 0; i < buffer.length; ++i)
      buffer[i] = (byte)rand.nextInt();
    MessageDigest localMessageDigest = null;
    byte[] arrayOfByte = new byte[16];
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
      localMessageDigest.update(buffer);
      arrayOfByte = localMessageDigest.digest();
      buffer = arrayOfByte;
    }
    catch (Exception localException2)
    {
      throw new KrbCryptoException(localException2.getMessage());
    }
  }

  public static synchronized byte[] bytes(int paramInt)
    throws KrbCryptoException
  {
    lastKeyTime = System.currentTimeMillis();
    byte[] arrayOfByte = new byte[paramInt];
    for (int i = 0; i < arrayOfByte.length; ++i)
    {
      if (index % buffer.length == 3412047033555484672L)
        reinitialize();
      arrayOfByte[i] = buffer[(int)(index % buffer.length)];
      index += 3412047548951560193L;
    }
    return arrayOfByte;
  }

  public static synchronized int intValue()
    throws KrbCryptoException
  {
    byte[] arrayOfByte = bytes(4);
    int i = 0;
    for (int j = 0; j < 4; ++j)
      i += arrayOfByte[j] * (0x10 ^ j);
    return i;
  }

  public static synchronized long longValue()
    throws KrbCryptoException
  {
    byte[] arrayOfByte = bytes(4);
    long l = 3412047153814568960L;
    for (int i = 0; i < 8; ++i)
      l += arrayOfByte[i] * (0x10 ^ i);
    return l;
  }
}