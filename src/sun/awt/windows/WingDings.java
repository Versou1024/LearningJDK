package sun.awt.windows;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class WingDings extends Charset
{
  public WingDings()
  {
    super("WingDings", null);
  }

  public CharsetEncoder newEncoder()
  {
    return new Encoder(this);
  }

  public CharsetDecoder newDecoder()
  {
    throw new Error("Decoder isn't implemented for WingDings Charset");
  }

  public boolean contains(Charset paramCharset)
  {
    return paramCharset instanceof WingDings;
  }

  private static class Encoder extends CharsetEncoder
  {
    private static byte[] table;

    public Encoder(Charset paramCharset)
    {
      super(paramCharset, 1F, 1F);
    }

    public boolean canEncode(char paramChar)
    {
      if ((paramChar >= 9985) && (paramChar <= 10174))
        return (table[(paramChar - 9984)] != 0);
      return false;
    }

    protected CoderResult encodeLoop(CharBuffer paramCharBuffer, ByteBuffer paramByteBuffer)
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
          if (l - k < 1)
          {
            localCoderResult2 = CoderResult.OVERFLOW;
            return localCoderResult2;
          }
          if (!(canEncode(i1)))
          {
            localCoderResult2 = CoderResult.unmappableForLength(1);
            return localCoderResult2;
          }
          ++i;
          arrayOfByte[(k++)] = table[(i1 - 9984)];
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

    public boolean isLegalReplacement(byte[] paramArrayOfByte)
    {
      return true;
    }

    static
    {
      table = { 0, 35, 34, 0, 0, 0, 41, 62, 81, 42, 0, 0, 65, 63, 0, 0, 0, 0, 0, -4, 0, 0, 0, -5, 0, 0, 0, 0, 0, 0, 86, 0, 88, 89, 0, 0, 0, 0, 0, 0, 0, 0, -75, 0, 0, 0, 0, 0, -74, 0, 0, 0, -83, -81, -84, 0, 0, 0, 0, 0, 0, 0, 0, 124, 123, 0, 0, 0, 84, 0, 0, 0, 0, 0, 0, 0, 0, -90, 0, 0, 0, 113, 114, 0, 0, 0, 117, 0, 0, 0, 0, 0, 0, 125, 126, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -116, -115, -114, -113, -112, -111, -110, -109, -108, -107, -127, -126, -125, -124, -123, -122, -121, -120, -119, -118, -116, -115, -114, -113, -112, -111, -110, -109, -108, -107, -24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -24, -40, 0, 0, -60, -58, 0, 0, -16, 0, 0, 0, 0, 0, 0, 0, 0, 0, -36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    }
  }
}