package sun.jkernel;

import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;

public abstract class StandaloneMessageDigest
{
  public static final boolean debug = 0;
  private String algorithm;
  private static final int INITIAL = 0;
  private static final int IN_PROGRESS = 1;
  private int state = 0;
  private byte[] oneByte;
  private final int digestLength;
  private final int blockSize;
  final byte[] buffer;
  private int bufOfs;
  long bytesProcessed;
  static final byte[] padding = new byte[136];

  private StandaloneMessageDigest()
  {
    this.digestLength = 0;
    this.blockSize = 0;
    this.algorithm = null;
    this.buffer = null;
  }

  public static StandaloneMessageDigest getInstance(String paramString)
    throws NoSuchAlgorithmException
  {
    if (!(paramString.equals("SHA-1")))
      throw new NoSuchAlgorithmException(paramString + " not found");
    return new StandaloneSHA();
  }

  public void update(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (paramArrayOfByte == null)
      throw new IllegalArgumentException("No input buffer given");
    if (paramArrayOfByte.length - paramInt1 < paramInt2)
      throw new IllegalArgumentException("Input buffer too short");
    engineUpdate(paramArrayOfByte, paramInt1, paramInt2);
    this.state = 1;
  }

  public byte[] digest()
  {
    byte[] arrayOfByte = engineDigest();
    this.state = 0;
    return arrayOfByte;
  }

  public static boolean isEqual(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    if (paramArrayOfByte1.length != paramArrayOfByte2.length)
      return false;
    for (int i = 0; i < paramArrayOfByte1.length; ++i)
      if (paramArrayOfByte1[i] != paramArrayOfByte2[i])
        return false;
    return true;
  }

  public void reset()
  {
    engineReset();
    this.state = 0;
  }

  public final String getAlgorithm()
  {
    return this.algorithm;
  }

  public final int getDigestLength()
  {
    return engineGetDigestLength();
  }

  StandaloneMessageDigest(String paramString, int paramInt1, int paramInt2)
  {
    this.algorithm = paramString;
    this.digestLength = paramInt1;
    this.blockSize = paramInt2;
    this.buffer = new byte[paramInt2];
  }

  protected final int engineGetDigestLength()
  {
    return this.digestLength;
  }

  protected final void engineUpdate(byte paramByte)
  {
    if (this.oneByte == null)
      this.oneByte = new byte[1];
    this.oneByte[0] = paramByte;
    engineUpdate(this.oneByte, 0, 1);
  }

  protected final void engineUpdate(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (paramInt2 == 0)
      return;
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 > paramArrayOfByte.length - paramInt2))
      throw new ArrayIndexOutOfBoundsException();
    if (this.bytesProcessed < 3412046672778231808L)
      engineReset();
    this.bytesProcessed += paramInt2;
    if (this.bufOfs != 0)
    {
      int i = Math.min(paramInt2, this.blockSize - this.bufOfs);
      System.arraycopy(paramArrayOfByte, paramInt1, this.buffer, this.bufOfs, i);
      this.bufOfs += i;
      paramInt1 += i;
      paramInt2 -= i;
      if (this.bufOfs >= this.blockSize)
      {
        implCompress(this.buffer, 0);
        this.bufOfs = 0;
      }
    }
    while (paramInt2 >= this.blockSize)
    {
      implCompress(paramArrayOfByte, paramInt1);
      paramInt2 -= this.blockSize;
      paramInt1 += this.blockSize;
    }
    if (paramInt2 > 0)
    {
      System.arraycopy(paramArrayOfByte, paramInt1, this.buffer, 0, paramInt2);
      this.bufOfs = paramInt2;
    }
  }

  protected final void engineReset()
  {
    if (this.bytesProcessed == 3412046672778231808L)
      return;
    implReset();
    this.bufOfs = 0;
    this.bytesProcessed = 3412046827397054464L;
  }

  protected final byte[] engineDigest()
    throws ProviderException
  {
    byte[] arrayOfByte = new byte[this.digestLength];
    try
    {
      engineDigest(arrayOfByte, 0, arrayOfByte.length);
    }
    catch (DigestException localDigestException)
    {
      throw ((ProviderException)new ProviderException("Internal error").initCause(localDigestException));
    }
    return arrayOfByte;
  }

  protected final int engineDigest(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws DigestException
  {
    if (paramInt2 < this.digestLength)
      throw new DigestException("Length must be at least " + this.digestLength + " for " + this.algorithm + "digests");
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 > paramArrayOfByte.length - paramInt2))
      throw new DigestException("Buffer too short to store digest");
    if (this.bytesProcessed < 3412046672778231808L)
      engineReset();
    implDigest(paramArrayOfByte, paramInt1);
    this.bytesProcessed = -1L;
    return this.digestLength;
  }

  abstract void implCompress(byte[] paramArrayOfByte, int paramInt);

  abstract void implDigest(byte[] paramArrayOfByte, int paramInt);

  abstract void implReset();

  static
  {
    padding[0] = -128;
  }
}