package sun.security.provider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandomSpi;

public final class SecureRandom extends SecureRandomSpi
  implements Serializable
{
  private static final long serialVersionUID = 3581829991155417889L;
  private static SecureRandom seeder;
  private static final int DIGEST_SIZE = 20;
  private transient MessageDigest digest;
  private byte[] state;
  private byte[] remainder;
  private int remCount;

  public SecureRandom()
  {
    init(null);
  }

  private SecureRandom(byte[] paramArrayOfByte)
  {
    init(paramArrayOfByte);
  }

  private void init(byte[] paramArrayOfByte)
  {
    try
    {
      this.digest = MessageDigest.getInstance("SHA");
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new InternalError("internal error: SHA-1 not available.");
    }
    if (paramArrayOfByte != null)
      engineSetSeed(paramArrayOfByte);
  }

  public byte[] engineGenerateSeed(int paramInt)
  {
    byte[] arrayOfByte = new byte[paramInt];
    SeedGenerator.generateSeed(arrayOfByte);
    return arrayOfByte;
  }

  public synchronized void engineSetSeed(byte[] paramArrayOfByte)
  {
    if (this.state != null)
    {
      this.digest.update(this.state);
      for (int i = 0; i < this.state.length; ++i)
        this.state[i] = 0;
    }
    this.state = this.digest.digest(paramArrayOfByte);
  }

  private static void updateState(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    int i = 1;
    int j = 0;
    int k = 0;
    int l = 0;
    for (int i1 = 0; i1 < paramArrayOfByte1.length; ++i1)
    {
      j = paramArrayOfByte1[i1] + paramArrayOfByte2[i1] + i;
      k = (byte)j;
      l |= ((paramArrayOfByte1[i1] != k) ? 1 : 0);
      paramArrayOfByte1[i1] = k;
      i = j >> 8;
    }
    if (l == 0)
    {
      int tmp79_78 = 0;
      byte[] tmp79_77 = paramArrayOfByte1;
      tmp79_77[tmp79_78] = (byte)(tmp79_77[tmp79_78] + 1);
    }
  }

  public synchronized void engineNextBytes(byte[] paramArrayOfByte)
  {
    int j;
    int l;
    int i = 0;
    byte[] arrayOfByte1 = this.remainder;
    if (this.state == null)
    {
      if (seeder == null)
      {
        seeder = new SecureRandom(SeedGenerator.getSystemEntropy());
        seeder.engineSetSeed(engineGenerateSeed(20));
      }
      byte[] arrayOfByte2 = new byte[20];
      seeder.engineNextBytes(arrayOfByte2);
      this.state = this.digest.digest(arrayOfByte2);
    }
    int k = this.remCount;
    if (k > 0)
    {
      j = (paramArrayOfByte.length - i < 20 - k) ? paramArrayOfByte.length - i : 20 - k;
      for (l = 0; l < j; ++l)
      {
        paramArrayOfByte[l] = arrayOfByte1[k];
        arrayOfByte1[(k++)] = 0;
      }
      this.remCount += j;
      i += j;
    }
    while (i < paramArrayOfByte.length)
    {
      this.digest.update(this.state);
      arrayOfByte1 = this.digest.digest();
      updateState(this.state, arrayOfByte1);
      j = (paramArrayOfByte.length - i > 20) ? 20 : paramArrayOfByte.length - i;
      for (l = 0; l < j; ++l)
      {
        paramArrayOfByte[(i++)] = arrayOfByte1[l];
        arrayOfByte1[l] = 0;
      }
      this.remCount += j;
    }
    this.remainder = arrayOfByte1;
    this.remCount %= 20;
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    try
    {
      this.digest = MessageDigest.getInstance("SHA");
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new InternalError("internal error: SHA-1 not available.");
    }
  }
}