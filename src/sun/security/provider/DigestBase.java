package sun.security.provider;

import B;
import java.security.DigestException;
import java.security.MessageDigestSpi;
import java.security.ProviderException;

abstract class DigestBase extends MessageDigestSpi
  implements Cloneable
{
  private byte[] oneByte;
  private final String algorithm;
  private final int digestLength;
  private final int blockSize;
  final byte[] buffer;
  private int bufOfs;
  long bytesProcessed;
  static final byte[] padding = new byte[136];

  DigestBase(String paramString, int paramInt1, int paramInt2)
  {
    this.algorithm = paramString;
    this.digestLength = paramInt1;
    this.blockSize = paramInt2;
    this.buffer = new byte[paramInt2];
  }

  DigestBase(DigestBase paramDigestBase)
  {
    this.algorithm = paramDigestBase.algorithm;
    this.digestLength = paramDigestBase.digestLength;
    this.blockSize = paramDigestBase.blockSize;
    this.buffer = ((byte[])(byte[])paramDigestBase.buffer.clone());
    this.bufOfs = paramDigestBase.bufOfs;
    this.bytesProcessed = paramDigestBase.bytesProcessed;
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

  public abstract Object clone();

  static
  {
    padding[0] = -128;
  }
}