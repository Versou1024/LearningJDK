package sun.nio.cs;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public abstract class SingleByteDecoder extends CharsetDecoder
{
  private final String byteToCharTable;

  protected SingleByteDecoder(Charset paramCharset, String paramString)
  {
    super(paramCharset, 1F, 1F);
    this.byteToCharTable = paramString;
  }

  private CoderResult decodeArrayLoop(ByteBuffer paramByteBuffer, CharBuffer paramCharBuffer)
  {
    byte[] arrayOfByte = paramByteBuffer.array();
    int i = paramByteBuffer.arrayOffset() + paramByteBuffer.position();
    int j = paramByteBuffer.arrayOffset() + paramByteBuffer.limit();
    if ((!($assertionsDisabled)) && (i > j))
      throw new AssertionError();
    i = (i <= j) ? i : j;
    char[] arrayOfChar = paramCharBuffer.array();
    int k = paramCharBuffer.arrayOffset() + paramCharBuffer.position();
    int l = paramCharBuffer.arrayOffset() + paramCharBuffer.limit();
    if ((!($assertionsDisabled)) && (k > l))
      throw new AssertionError();
    k = (k <= l) ? k : l;
    try
    {
      while (i < j)
      {
        CoderResult localCoderResult2;
        int i1 = arrayOfByte[i];
        int i2 = decode(i1);
        if (i2 == 65533)
        {
          localCoderResult2 = CoderResult.unmappableForLength(1);
          return localCoderResult2;
        }
        if (l - k < 1)
        {
          localCoderResult2 = CoderResult.OVERFLOW;
          return localCoderResult2;
        }
        arrayOfChar[(k++)] = i2;
        ++i;
      }
      CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
      return localCoderResult1;
    }
    finally
    {
      paramByteBuffer.position(i - paramByteBuffer.arrayOffset());
      paramCharBuffer.position(k - paramCharBuffer.arrayOffset());
    }
  }

  private CoderResult decodeBufferLoop(ByteBuffer paramByteBuffer, CharBuffer paramCharBuffer)
  {
    int i = paramByteBuffer.position();
    try
    {
      while (paramByteBuffer.hasRemaining())
      {
        CoderResult localCoderResult2;
        int j = paramByteBuffer.get();
        int k = decode(j);
        if (k == 65533)
        {
          localCoderResult2 = CoderResult.unmappableForLength(1);
          return localCoderResult2;
        }
        if (!(paramCharBuffer.hasRemaining()))
        {
          localCoderResult2 = CoderResult.OVERFLOW;
          return localCoderResult2;
        }
        ++i;
        paramCharBuffer.put(k);
      }
      CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
      return localCoderResult1;
    }
    finally
    {
      paramByteBuffer.position(i);
    }
  }

  protected CoderResult decodeLoop(ByteBuffer paramByteBuffer, CharBuffer paramCharBuffer)
  {
    if ((paramByteBuffer.hasArray()) && (paramCharBuffer.hasArray()))
      return decodeArrayLoop(paramByteBuffer, paramCharBuffer);
    return decodeBufferLoop(paramByteBuffer, paramCharBuffer);
  }

  public char decode(int paramInt)
  {
    int i = paramInt + 128;
    if ((i >= this.byteToCharTable.length()) || (i < 0))
      return 65533;
    return this.byteToCharTable.charAt(i);
  }
}