package sun.nio.cs;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

class UTF_8 extends Unicode
{
  public UTF_8()
  {
    super("UTF-8", StandardCharsets.aliases_UTF_8);
  }

  public String historicalName()
  {
    return "UTF8";
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
    private final Surrogate.Generator sgg = new Surrogate.Generator();

    private Decoder(Charset paramCharset)
    {
      super(paramCharset, 1F, 1F);
    }

    private boolean isContinuation(int paramInt)
    {
      return ((paramInt & 0xC0) == 128);
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
          int i1;
          int i2;
          int i3;
          int i4;
          int i5;
          int i7;
          int i8;
          CoderResult localCoderResult3;
          while (true)
          {
            CoderResult localCoderResult2;
            while (true)
            {
              while (true)
              {
                if (i >= j)
                  break label1753;
                i1 = arrayOfByte[i];
                switch (i1 >> 4 & 0xF)
                {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                  if (l - k < 1)
                  {
                    localCoderResult2 = CoderResult.OVERFLOW;
                    return localCoderResult2;
                  }
                  arrayOfChar[(k++)] = (char)(i1 & 0x7F);
                  ++i;
                case 12:
                case 13:
                case 14:
                case 15:
                case 8:
                case 9:
                case 10:
                case 11:
                }
              }
              if (j - i < 2)
              {
                localCoderResult2 = CoderResult.UNDERFLOW;
                return localCoderResult2;
              }
              if (l - k < 1)
              {
                localCoderResult2 = CoderResult.OVERFLOW;
                return localCoderResult2;
              }
              if (!(isContinuation(i2 = arrayOfByte[(i + 1)])))
              {
                localCoderResult2 = CoderResult.malformedForLength(1);
                return localCoderResult2;
              }
              arrayOfChar[(k++)] = (char)((i1 & 0x1F) << 6 | (i2 & 0x3F) << 0);
              i += 2;
            }
            if (j - i < 3)
            {
              localCoderResult2 = CoderResult.UNDERFLOW;
              return localCoderResult2;
            }
            if (l - k < 1)
            {
              localCoderResult2 = CoderResult.OVERFLOW;
              return localCoderResult2;
            }
            if (!(isContinuation(i2 = arrayOfByte[(i + 1)])))
            {
              localCoderResult2 = CoderResult.malformedForLength(1);
              return localCoderResult2;
            }
            if (!(isContinuation(i3 = arrayOfByte[(i + 2)])))
            {
              localCoderResult2 = CoderResult.malformedForLength(2);
              return localCoderResult2;
            }
            arrayOfChar[(k++)] = (char)((i1 & 0xF) << 12 | (i2 & 0x3F) << 6 | (i3 & 0x3F) << 0);
            i += 3;
          }
          switch (i1 & 0xF)
          {
          case 0:
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
            if (j - i < 4)
            {
              localCoderResult3 = CoderResult.UNDERFLOW;
              return localCoderResult3;
            }
            if (!(isContinuation(i2 = arrayOfByte[(i + 1)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(1);
              return localCoderResult3;
            }
            if (!(isContinuation(i3 = arrayOfByte[(i + 2)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(2);
              return localCoderResult3;
            }
            if (!(isContinuation(i4 = arrayOfByte[(i + 3)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(3);
              return localCoderResult3;
            }
            i7 = (i1 & 0x7) << 18 | (i2 & 0x3F) << 12 | (i3 & 0x3F) << 6 | (i4 & 0x3F) << 0;
            i8 = 4;
            break;
          case 8:
          case 9:
          case 10:
          case 11:
            if (j - i < 5)
            {
              localCoderResult3 = CoderResult.UNDERFLOW;
              return localCoderResult3;
            }
            if (!(isContinuation(i2 = arrayOfByte[(i + 1)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(1);
              return localCoderResult3;
            }
            if (!(isContinuation(i3 = arrayOfByte[(i + 2)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(2);
              return localCoderResult3;
            }
            if (!(isContinuation(i4 = arrayOfByte[(i + 3)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(3);
              return localCoderResult3;
            }
            if (!(isContinuation(i5 = arrayOfByte[(i + 4)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(4);
              return localCoderResult3;
            }
            i7 = (i1 & 0x3) << 24 | (i2 & 0x3F) << 18 | (i3 & 0x3F) << 12 | (i4 & 0x3F) << 6 | (i5 & 0x3F) << 0;
            i8 = 5;
            break;
          case 12:
          case 13:
            int i6;
            if (j - i < 6)
            {
              localCoderResult3 = CoderResult.UNDERFLOW;
              return localCoderResult3;
            }
            if (!(isContinuation(i2 = arrayOfByte[(i + 1)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(1);
              return localCoderResult3;
            }
            if (!(isContinuation(i3 = arrayOfByte[(i + 2)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(2);
              return localCoderResult3;
            }
            if (!(isContinuation(i4 = arrayOfByte[(i + 3)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(3);
              return localCoderResult3;
            }
            if (!(isContinuation(i5 = arrayOfByte[(i + 4)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(4);
              return localCoderResult3;
            }
            if (!(isContinuation(i6 = arrayOfByte[(i + 5)])))
            {
              localCoderResult3 = CoderResult.malformedForLength(5);
              return localCoderResult3;
            }
            i7 = (i1 & 0x1) << 30 | (i2 & 0x3F) << 24 | (i3 & 0x3F) << 18 | (i4 & 0x3F) << 12 | (i5 & 0x3F) << 6 | i6 & 0x3F;
            i8 = 6;
            break;
          default:
            localCoderResult3 = CoderResult.malformedForLength(1);
            return localCoderResult3;
          }
          int i9 = this.sgg.generate(i7, i8, arrayOfChar, k, l);
          if (i9 < 0)
          {
            localCoderResult4 = this.sgg.error();
            return localCoderResult4;
          }
          k += i9;
          i += i8;
        }
        CoderResult localCoderResult4 = CoderResult.malformedForLength(1);
        return localCoderResult4;
        label1753: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
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
          int j;
          int k;
          int l;
          int i1;
          int i2;
          int i4;
          int i5;
          while (true)
          {
            CoderResult localCoderResult2;
            while (true)
            {
              while (true)
              {
                if (!(paramByteBuffer.hasRemaining()))
                  break label1091;
                j = paramByteBuffer.get();
                switch (j >> 4 & 0xF)
                {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                  if (paramCharBuffer.remaining() < 1)
                  {
                    localCoderResult2 = CoderResult.OVERFLOW;
                    return localCoderResult2;
                  }
                  paramCharBuffer.put((char)j);
                  ++i;
                case 12:
                case 13:
                case 14:
                case 15:
                case 8:
                case 9:
                case 10:
                case 11:
                }
              }
              if (paramByteBuffer.remaining() < 1)
              {
                localCoderResult2 = CoderResult.UNDERFLOW;
                return localCoderResult2;
              }
              if (paramCharBuffer.remaining() < 1)
              {
                localCoderResult2 = CoderResult.OVERFLOW;
                return localCoderResult2;
              }
              if (!(isContinuation(k = paramByteBuffer.get())))
              {
                localCoderResult2 = CoderResult.malformedForLength(1);
                return localCoderResult2;
              }
              paramCharBuffer.put((char)((j & 0x1F) << 6 | (k & 0x3F) << 0));
              i += 2;
            }
            if (paramByteBuffer.remaining() < 2)
            {
              localCoderResult2 = CoderResult.UNDERFLOW;
              return localCoderResult2;
            }
            if (paramCharBuffer.remaining() < 1)
            {
              localCoderResult2 = CoderResult.OVERFLOW;
              return localCoderResult2;
            }
            if (!(isContinuation(k = paramByteBuffer.get())))
            {
              localCoderResult2 = CoderResult.malformedForLength(1);
              return localCoderResult2;
            }
            if (!(isContinuation(l = paramByteBuffer.get())))
            {
              localCoderResult2 = CoderResult.malformedForLength(2);
              return localCoderResult2;
            }
            paramCharBuffer.put((char)((j & 0xF) << 12 | (k & 0x3F) << 6 | (l & 0x3F) << 0));
            i += 3;
          }
          switch (j & 0xF)
          {
          case 0:
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
            if (paramByteBuffer.remaining() < 3)
            {
              localCoderResult3 = CoderResult.UNDERFLOW;
              return localCoderResult3;
            }
            if (!(isContinuation(k = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(1);
              return localCoderResult3;
            }
            if (!(isContinuation(l = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(2);
              return localCoderResult3;
            }
            if (!(isContinuation(i1 = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(3);
              return localCoderResult3;
            }
            i4 = (j & 0x7) << 18 | (k & 0x3F) << 12 | (l & 0x3F) << 6 | (i1 & 0x3F) << 0;
            i5 = 4;
            break;
          case 8:
          case 9:
          case 10:
          case 11:
            if (paramByteBuffer.remaining() < 4)
            {
              localCoderResult3 = CoderResult.UNDERFLOW;
              return localCoderResult3;
            }
            if (!(isContinuation(k = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(1);
              return localCoderResult3;
            }
            if (!(isContinuation(l = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(2);
              return localCoderResult3;
            }
            if (!(isContinuation(i1 = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(3);
              return localCoderResult3;
            }
            if (!(isContinuation(i2 = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(4);
              return localCoderResult3;
            }
            i4 = (j & 0x3) << 24 | (k & 0x3F) << 18 | (l & 0x3F) << 12 | (i1 & 0x3F) << 6 | (i2 & 0x3F) << 0;
            i5 = 5;
            break;
          case 12:
          case 13:
            int i3;
            if (paramByteBuffer.remaining() < 4)
            {
              localCoderResult3 = CoderResult.UNDERFLOW;
              return localCoderResult3;
            }
            if (!(isContinuation(k = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(1);
              return localCoderResult3;
            }
            if (!(isContinuation(l = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(2);
              return localCoderResult3;
            }
            if (!(isContinuation(i1 = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(3);
              return localCoderResult3;
            }
            if (!(isContinuation(i2 = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(4);
              return localCoderResult3;
            }
            if (!(isContinuation(i3 = paramByteBuffer.get())))
            {
              localCoderResult3 = CoderResult.malformedForLength(5);
              return localCoderResult3;
            }
            i4 = (j & 0x1) << 30 | (k & 0x3F) << 24 | (l & 0x3F) << 18 | (i1 & 0x3F) << 12 | (i2 & 0x3F) << 6 | i3 & 0x3F;
            i5 = 6;
            break;
          default:
            localCoderResult3 = CoderResult.malformedForLength(1);
            return localCoderResult3;
          }
          if (this.sgg.generate(i4, i5, paramCharBuffer) < 0)
          {
            localCoderResult3 = this.sgg.error();
            return localCoderResult3;
          }
          i += i5;
        }
        CoderResult localCoderResult3 = CoderResult.malformedForLength(1);
        return localCoderResult3;
        label1091: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
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
      super(paramCharset, 1.1000000238418579F, 4.0F);
    }

    public boolean canEncode(char paramChar)
    {
      return (!(Surrogate.is(paramChar)));
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
        while (true)
        {
          while (true)
          {
            int i1;
            CoderResult localCoderResult3;
            while (true)
            {
              CoderResult localCoderResult2;
              while (true)
              {
                while (true)
                {
                  if (i >= j)
                    break label637;
                  i1 = arrayOfChar[i];
                  if (i1 >= 128)
                    break;
                  if (k >= l)
                  {
                    localCoderResult2 = CoderResult.OVERFLOW;
                    return localCoderResult2;
                  }
                  arrayOfByte[(k++)] = (byte)i1;
                  ++i;
                }
                if (Surrogate.is(i1))
                  break label420;
                if (i1 >= 2048)
                  break;
                if (l - k < 2)
                {
                  localCoderResult2 = CoderResult.OVERFLOW;
                  return localCoderResult2;
                }
                arrayOfByte[(k++)] = (byte)(0xC0 | i1 >> 6);
                arrayOfByte[(k++)] = (byte)(0x80 | i1 >> 0 & 0x3F);
                ++i;
              }
              if (i1 > 65535)
                break;
              if (l - k < 3)
              {
                localCoderResult2 = CoderResult.OVERFLOW;
                return localCoderResult2;
              }
              arrayOfByte[(k++)] = (byte)(0xE0 | i1 >> 12);
              arrayOfByte[(k++)] = (byte)(0x80 | i1 >> 6 & 0x3F);
              arrayOfByte[(k++)] = (byte)(0x80 | i1 >> 0 & 0x3F);
              ++i;
            }
            label420: int i2 = this.sgp.parse(i1, arrayOfChar, i, j);
            if (i2 < 0)
            {
              localCoderResult3 = this.sgp.error();
              return localCoderResult3;
            }
            if (i2 >= 2097152)
              break;
            if (l - k < 4)
            {
              localCoderResult3 = CoderResult.OVERFLOW;
              return localCoderResult3;
            }
            arrayOfByte[(k++)] = (byte)(0xF0 | i2 >> 18);
            arrayOfByte[(k++)] = (byte)(0x80 | i2 >> 12 & 0x3F);
            arrayOfByte[(k++)] = (byte)(0x80 | i2 >> 6 & 0x3F);
            arrayOfByte[(k++)] = (byte)(0x80 | i2 >> 0 & 0x3F);
            i += this.sgp.increment();
          }
          if (!($assertionsDisabled))
            throw new AssertionError();
        }
        label637: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
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
        while (true)
        {
          while (true)
          {
            int j;
            CoderResult localCoderResult3;
            while (true)
            {
              CoderResult localCoderResult2;
              while (true)
              {
                while (true)
                {
                  if (!(paramCharBuffer.hasRemaining()))
                    break label384;
                  j = paramCharBuffer.get();
                  if (j >= 128)
                    break;
                  if (!(paramByteBuffer.hasRemaining()))
                  {
                    localCoderResult2 = CoderResult.OVERFLOW;
                    return localCoderResult2;
                  }
                  paramByteBuffer.put((byte)j);
                  ++i;
                }
                if (Surrogate.is(j))
                  break label222;
                if (j >= 2048)
                  break;
                if (paramByteBuffer.remaining() < 2)
                {
                  localCoderResult2 = CoderResult.OVERFLOW;
                  return localCoderResult2;
                }
                paramByteBuffer.put((byte)(0xC0 | j >> 6));
                paramByteBuffer.put((byte)(0x80 | j >> 0 & 0x3F));
                ++i;
              }
              if (j > 65535)
                break;
              if (paramByteBuffer.remaining() < 3)
              {
                localCoderResult2 = CoderResult.OVERFLOW;
                return localCoderResult2;
              }
              paramByteBuffer.put((byte)(0xE0 | j >> 12));
              paramByteBuffer.put((byte)(0x80 | j >> 6 & 0x3F));
              paramByteBuffer.put((byte)(0x80 | j >> 0 & 0x3F));
              ++i;
            }
            label222: int k = this.sgp.parse(j, paramCharBuffer);
            if (k < 0)
            {
              localCoderResult3 = this.sgp.error();
              return localCoderResult3;
            }
            if (k >= 2097152)
              break;
            if (paramByteBuffer.remaining() < 4)
            {
              localCoderResult3 = CoderResult.OVERFLOW;
              return localCoderResult3;
            }
            paramByteBuffer.put((byte)(0xF0 | k >> 18));
            paramByteBuffer.put((byte)(0x80 | k >> 12 & 0x3F));
            paramByteBuffer.put((byte)(0x80 | k >> 6 & 0x3F));
            paramByteBuffer.put((byte)(0x80 | k >> 0 & 0x3F));
            i += this.sgp.increment();
          }
          if (!($assertionsDisabled))
            throw new AssertionError();
        }
        label384: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
        return localCoderResult1;
      }
      finally
      {
        paramCharBuffer.position(i);
      }
    }

    protected final CoderResult encodeLoop(CharBuffer paramCharBuffer, ByteBuffer paramByteBuffer)
    {
      if ((paramCharBuffer.hasArray()) && (paramByteBuffer.hasArray()))
        return encodeArrayLoop(paramCharBuffer, paramByteBuffer);
      return encodeBufferLoop(paramCharBuffer, paramByteBuffer);
    }
  }
}