package sun.nio.cs;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class US_ASCII extends Charset
  implements HistoricallyNamedCharset
{
  public US_ASCII()
  {
    super("US-ASCII", StandardCharsets.aliases_US_ASCII);
  }

  public String historicalName()
  {
    return "ASCII";
  }

  public boolean contains(Charset paramCharset)
  {
    return paramCharset instanceof US_ASCII;
  }

  public CharsetDecoder newDecoder()
  {
    return new Decoder(this, null);
  }

  public CharsetEncoder newEncoder()
  {
    return new Encoder(this, null);
  }

  private static class Decoder extends CharsetDecoder
  {
    private Decoder(Charset paramCharset)
    {
      super(paramCharset, 1F, 1F);
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
        while (true)
        {
          if (i >= j)
            break label194;
          int i1 = arrayOfByte[i];
          if (i1 < 0)
            break;
          if (k >= l)
          {
            localCoderResult2 = CoderResult.OVERFLOW;
            jsr 54;
            return localCoderResult2;
          }
          arrayOfChar[(k++)] = (char)i1;
          ++i;
        }
        CoderResult localCoderResult2 = CoderResult.malformedForLength(1);
        jsr 25;
        return localCoderResult2;
        label194: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
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
        while (true)
        {
          if (!(paramByteBuffer.hasRemaining()))
            break label67;
          int j = paramByteBuffer.get();
          if (j < 0)
            break;
          if (!(paramCharBuffer.hasRemaining()))
          {
            localCoderResult2 = CoderResult.OVERFLOW;
            jsr 51;
            return localCoderResult2;
          }
          paramCharBuffer.put((char)j);
          ++i;
        }
        CoderResult localCoderResult2 = CoderResult.malformedForLength(1);
        jsr 25;
        return localCoderResult2;
        label67: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
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
  }

  private static class Encoder extends CharsetEncoder
  {
    private final Surrogate.Parser sgp = new Surrogate.Parser();

    private Encoder(Charset paramCharset)
    {
      super(paramCharset, 1F, 1F);
    }

    public boolean canEncode(char paramChar)
    {
      return (paramChar < 128);
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
        char c;
        while (true)
        {
          if (i >= j)
            break label232;
          c = arrayOfChar[i];
          if (c >= 128)
            break;
          if (k >= l)
          {
            localCoderResult2 = CoderResult.OVERFLOW;
            jsr 89;
            return localCoderResult2;
          }
          arrayOfByte[k] = (byte)c;
          ++i;
          ++k;
        }
        if (this.sgp.parse(c, arrayOfChar, i, j) < 0)
        {
          localCoderResult2 = this.sgp.error();
          jsr 40;
          return localCoderResult2;
        }
        CoderResult localCoderResult2 = this.sgp.unmappableResult();
        jsr 25;
        return localCoderResult2;
        label232: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
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
        char c;
        while (true)
        {
          if (!(paramCharBuffer.hasRemaining()))
            break label101;
          c = paramCharBuffer.get();
          if (c >= 128)
            break;
          if (!(paramByteBuffer.hasRemaining()))
          {
            localCoderResult2 = CoderResult.OVERFLOW;
            jsr 82;
            return localCoderResult2;
          }
          paramByteBuffer.put((byte)c);
          ++i;
        }
        if (this.sgp.parse(c, paramCharBuffer) < 0)
        {
          localCoderResult2 = this.sgp.error();
          jsr 40;
          return localCoderResult2;
        }
        CoderResult localCoderResult2 = this.sgp.unmappableResult();
        jsr 25;
        return localCoderResult2;
        label101: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
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
  }
}