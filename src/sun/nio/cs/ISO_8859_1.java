package sun.nio.cs;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

class ISO_8859_1 extends Charset
  implements HistoricallyNamedCharset
{
  public ISO_8859_1()
  {
    super("ISO-8859-1", StandardCharsets.aliases_ISO_8859_1);
  }

  public String historicalName()
  {
    return "ISO8859_1";
  }

  public boolean contains(Charset paramCharset)
  {
    return ((paramCharset instanceof US_ASCII) || (paramCharset instanceof ISO_8859_1));
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
        while (i < j)
        {
          int i1 = arrayOfByte[i];
          if (k >= l)
          {
            CoderResult localCoderResult2 = CoderResult.OVERFLOW;
            return localCoderResult2;
          }
          arrayOfChar[(k++)] = (char)(i1 & 0xFF);
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
          int j = paramByteBuffer.get();
          if (!(paramCharBuffer.hasRemaining()))
          {
            CoderResult localCoderResult2 = CoderResult.OVERFLOW;
            return localCoderResult2;
          }
          paramCharBuffer.put((char)(j & 0xFF));
          ++i;
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
      return (paramChar <= 255);
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
            break label295;
          c = arrayOfChar[i];
          if (c > 255)
            break;
          if (k >= l)
          {
            localCoderResult2 = CoderResult.OVERFLOW;
            return localCoderResult2;
          }
          arrayOfByte[(k++)] = (byte)c;
          ++i;
        }
        if (this.sgp.parse(c, arrayOfChar, i, j) < 0)
        {
          localCoderResult2 = this.sgp.error();
          return localCoderResult2;
        }
        CoderResult localCoderResult2 = this.sgp.unmappableResult();
        return localCoderResult2;
        label295: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
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
            break label110;
          c = paramCharBuffer.get();
          if (c > 255)
            break;
          if (!(paramByteBuffer.hasRemaining()))
          {
            localCoderResult2 = CoderResult.OVERFLOW;
            return localCoderResult2;
          }
          paramByteBuffer.put((byte)c);
          ++i;
        }
        if (this.sgp.parse(c, paramCharBuffer) < 0)
        {
          localCoderResult2 = this.sgp.error();
          return localCoderResult2;
        }
        CoderResult localCoderResult2 = this.sgp.unmappableResult();
        return localCoderResult2;
        label110: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
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