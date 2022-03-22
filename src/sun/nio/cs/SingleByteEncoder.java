package sun.nio.cs;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public abstract class SingleByteEncoder extends CharsetEncoder
{
  private final short[] index1;
  private final String index2;
  private final int mask1;
  private final int mask2;
  private final int shift;
  private final Surrogate.Parser sgp = new Surrogate.Parser();

  protected SingleByteEncoder(Charset paramCharset, short[] paramArrayOfShort, String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    super(paramCharset, 1F, 1F);
    this.index1 = paramArrayOfShort;
    this.index2 = paramString;
    this.mask1 = paramInt1;
    this.mask2 = paramInt2;
    this.shift = paramInt3;
  }

  public boolean canEncode(char paramChar)
  {
    int i = this.index2.charAt(this.index1[((paramChar & this.mask1) >> this.shift)] + (paramChar & this.mask2));
    return ((i != 0) || (paramChar == 0));
  }

  private CoderResult encodeArrayLoop(CharBuffer paramCharBuffer, ByteBuffer paramByteBuffer)
  {
    char[] arrayOfChar = paramCharBuffer.array();
    int i = paramCharBuffer.arrayOffset() + paramCharBuffer.position();
    int j = paramCharBuffer.arrayOffset() + paramCharBuffer.limit();
    if ((!($assertionsDisabled)) && (i > j))
      throw new AssertionError();
    i = (i <= j) ? i : j;
    byte[] arrayOfByte = paramByteBuffer.array();
    int k = paramByteBuffer.arrayOffset() + paramByteBuffer.position();
    int l = paramByteBuffer.arrayOffset() + paramByteBuffer.limit();
    if ((!($assertionsDisabled)) && (k > l))
      throw new AssertionError();
    k = (k <= l) ? k : l;
    try
    {
      while (i < j)
      {
        CoderResult localCoderResult2;
        int i1 = arrayOfChar[i];
        if (Surrogate.is(i1))
        {
          if (this.sgp.parse(i1, arrayOfChar, i, j) < 0)
          {
            localCoderResult2 = this.sgp.error();
            return localCoderResult2;
          }
          localCoderResult2 = this.sgp.unmappableResult();
          return localCoderResult2;
        }
        if (i1 >= 65534)
        {
          localCoderResult2 = CoderResult.unmappableForLength(1);
          return localCoderResult2;
        }
        if (l - k < 1)
        {
          localCoderResult2 = CoderResult.OVERFLOW;
          return localCoderResult2;
        }
        int i2 = this.index2.charAt(this.index1[((i1 & this.mask1) >> this.shift)] + (i1 & this.mask2));
        if ((i2 == 0) && (i1 != 0))
        {
          CoderResult localCoderResult3 = CoderResult.unmappableForLength(1);
          return localCoderResult3;
        }
        ++i;
        arrayOfByte[(k++)] = (byte)i2;
      }
      CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
      return localCoderResult1;
    }
    finally
    {
      paramCharBuffer.position(i - paramCharBuffer.arrayOffset());
      paramByteBuffer.position(k - paramByteBuffer.arrayOffset());
    }
  }

  private CoderResult encodeBufferLoop(CharBuffer paramCharBuffer, ByteBuffer paramByteBuffer)
  {
    int i = paramCharBuffer.position();
    try
    {
      while (paramCharBuffer.hasRemaining())
      {
        CoderResult localCoderResult2;
        int j = paramCharBuffer.get();
        if (Surrogate.is(j))
        {
          if (this.sgp.parse(j, paramCharBuffer) < 0)
          {
            localCoderResult2 = this.sgp.error();
            return localCoderResult2;
          }
          localCoderResult2 = this.sgp.unmappableResult();
          return localCoderResult2;
        }
        if (j >= 65534)
        {
          localCoderResult2 = CoderResult.unmappableForLength(1);
          return localCoderResult2;
        }
        if (!(paramByteBuffer.hasRemaining()))
        {
          localCoderResult2 = CoderResult.OVERFLOW;
          return localCoderResult2;
        }
        int k = this.index2.charAt(this.index1[((j & this.mask1) >> this.shift)] + (j & this.mask2));
        if ((k == 0) && (j != 0))
        {
          CoderResult localCoderResult3 = CoderResult.unmappableForLength(1);
          return localCoderResult3;
        }
        ++i;
        paramByteBuffer.put((byte)k);
      }
      CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
      return localCoderResult1;
    }
    finally
    {
      paramCharBuffer.position(i);
    }
  }

  protected CoderResult encodeLoop(CharBuffer paramCharBuffer, ByteBuffer paramByteBuffer)
  {
    if ((paramCharBuffer.hasArray()) && (paramByteBuffer.hasArray()))
      return encodeArrayLoop(paramCharBuffer, paramByteBuffer);
    return encodeBufferLoop(paramCharBuffer, paramByteBuffer);
  }

  public byte encode(char paramChar)
  {
    return (byte)this.index2.charAt(this.index1[((paramChar & this.mask1) >> this.shift)] + (paramChar & this.mask2));
  }
}