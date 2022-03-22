package sun.nio.cs;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

abstract class UnicodeDecoder extends CharsetDecoder
{
  protected static final char BYTE_ORDER_MARK = 65279;
  protected static final char REVERSED_MARK = 65534;
  protected static final int NONE = 0;
  protected static final int BIG = 1;
  protected static final int LITTLE = 2;
  private final int expectedByteOrder;
  private int currentByteOrder;
  private int defaultByteOrder;

  public UnicodeDecoder(Charset paramCharset, int paramInt)
  {
    super(paramCharset, 0.5F, 1F);
    this.defaultByteOrder = 1;
    this.expectedByteOrder = (this.currentByteOrder = paramInt);
  }

  public UnicodeDecoder(Charset paramCharset, int paramInt1, int paramInt2)
  {
    this(paramCharset, paramInt1);
    this.defaultByteOrder = paramInt2;
  }

  private char decode(int paramInt1, int paramInt2)
  {
    if (this.currentByteOrder == 1)
      return (char)(paramInt1 << 8 | paramInt2);
    return (char)(paramInt2 << 8 | paramInt1);
  }

  protected CoderResult decodeLoop(ByteBuffer paramByteBuffer, CharBuffer paramCharBuffer)
  {
    int i = paramByteBuffer.position();
    try
    {
      while (true)
      {
        int l;
        while (true)
        {
          int j;
          int k;
          label95: CoderResult localCoderResult2;
          CoderResult localCoderResult4;
          while (true)
          {
            while (true)
            {
              if (paramByteBuffer.remaining() <= 1)
                break label301;
              j = paramByteBuffer.get() & 0xFF;
              k = paramByteBuffer.get() & 0xFF;
              if (this.currentByteOrder != 0)
                break label95;
              l = (char)(j << 8 | k);
              if (l != 65279)
                break;
              this.currentByteOrder = 1;
              i += 2;
            }
            if (l != 65534)
              break;
            this.currentByteOrder = 2;
            i += 2;
          }
          this.currentByteOrder = this.defaultByteOrder;
          l = decode(j, k);
          if (l == 65534)
          {
            localCoderResult2 = CoderResult.malformedForLength(2);
            return localCoderResult2;
          }
          if (!(Surrogate.is(l)))
            break label267;
          if (!(Surrogate.isHigh(l)))
            break;
          if (paramByteBuffer.remaining() < 2)
          {
            localCoderResult2 = CoderResult.UNDERFLOW;
            return localCoderResult2;
          }
          char c = decode(paramByteBuffer.get() & 0xFF, paramByteBuffer.get() & 0xFF);
          if (!(Surrogate.isLow(c)))
          {
            localCoderResult4 = CoderResult.malformedForLength(4);
            return localCoderResult4;
          }
          if (paramCharBuffer.remaining() < 2)
          {
            localCoderResult4 = CoderResult.OVERFLOW;
            return localCoderResult4;
          }
          i += 4;
          paramCharBuffer.put(l);
          paramCharBuffer.put(c);
        }
        CoderResult localCoderResult3 = CoderResult.malformedForLength(2);
        return localCoderResult3;
        if (!(paramCharBuffer.hasRemaining()))
        {
          label267: localCoderResult3 = CoderResult.OVERFLOW;
          return localCoderResult3;
        }
        i += 2;
        paramCharBuffer.put(l);
      }
      label301: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
      return localCoderResult1;
    }
    finally
    {
      paramByteBuffer.position(i);
    }
  }

  protected void implReset()
  {
    this.currentByteOrder = this.expectedByteOrder;
  }
}