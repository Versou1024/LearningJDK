package sun.nio.cs;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

class UTF_32Coder
{
  protected static final int BOM_BIG = 65279;
  protected static final int BOM_LITTLE = -131072;
  protected static final int NONE = 0;
  protected static final int BIG = 1;
  protected static final int LITTLE = 2;

  protected static class Decoder extends CharsetDecoder
  {
    private int currentBO;
    private int expectedBO;

    protected Decoder(Charset paramCharset, int paramInt)
    {
      super(paramCharset, 0.25F, 1F);
      this.expectedBO = paramInt;
      this.currentBO = 0;
    }

    private int getCP(ByteBuffer paramByteBuffer)
    {
      return ((this.currentBO == 1) ? (paramByteBuffer.get() & 0xFF) << 24 | (paramByteBuffer.get() & 0xFF) << 16 | (paramByteBuffer.get() & 0xFF) << 8 | paramByteBuffer.get() & 0xFF : paramByteBuffer.get() & 0xFF | (paramByteBuffer.get() & 0xFF) << 8 | (paramByteBuffer.get() & 0xFF) << 16 | (paramByteBuffer.get() & 0xFF) << 24);
    }

    protected CoderResult decodeLoop(ByteBuffer paramByteBuffer, CharBuffer paramCharBuffer)
    {
      if (paramByteBuffer.remaining() < 4)
        return CoderResult.UNDERFLOW;
      int i = paramByteBuffer.position();
      try
      {
        int j;
        if (this.currentBO == 0)
        {
          j = (paramByteBuffer.get() & 0xFF) << 24 | (paramByteBuffer.get() & 0xFF) << 16 | (paramByteBuffer.get() & 0xFF) << 8 | paramByteBuffer.get() & 0xFF;
          if ((j == 65279) && (this.expectedBO != 2))
          {
            this.currentBO = 1;
            i += 4;
          }
          else if ((j == -131072) && (this.expectedBO != 1))
          {
            this.currentBO = 2;
            i += 4;
          }
          else
          {
            if (this.expectedBO == 0)
              this.currentBO = 1;
            else
              this.currentBO = this.expectedBO;
            paramByteBuffer.position(i);
          }
        }
        while (true)
        {
          while (true)
          {
            if (paramByteBuffer.remaining() <= 3)
              break label283;
            j = getCP(paramByteBuffer);
            if ((j < 0) || (j > 1114111))
            {
              localCoderResult = CoderResult.malformedForLength(4);
              return localCoderResult;
            }
            if (j >= 65536)
              break;
            if (!(paramCharBuffer.hasRemaining()))
            {
              localCoderResult = CoderResult.OVERFLOW;
              return localCoderResult;
            }
            i += 4;
            paramCharBuffer.put((char)j);
          }
          if (paramCharBuffer.remaining() < 2)
          {
            localCoderResult = CoderResult.OVERFLOW;
            return localCoderResult;
          }
          i += 4;
          paramCharBuffer.put(Surrogate.high(j));
          paramCharBuffer.put(Surrogate.low(j));
        }
        label283: CoderResult localCoderResult = CoderResult.UNDERFLOW;
        return localCoderResult;
      }
      finally
      {
        paramByteBuffer.position(i);
      }
    }

    protected void implReset()
    {
      this.currentBO = 0;
    }
  }

  protected static class Encoder extends CharsetEncoder
  {
    private boolean doBOM = false;
    private boolean doneBOM = true;
    private int byteOrder;

    protected void put(int paramInt, ByteBuffer paramByteBuffer)
    {
      if (this.byteOrder == 1)
      {
        paramByteBuffer.put((byte)(paramInt >> 24));
        paramByteBuffer.put((byte)(paramInt >> 16));
        paramByteBuffer.put((byte)(paramInt >> 8));
        paramByteBuffer.put((byte)paramInt);
      }
      else
      {
        paramByteBuffer.put((byte)paramInt);
        paramByteBuffer.put((byte)(paramInt >> 8));
        paramByteBuffer.put((byte)(paramInt >> 16));
        paramByteBuffer.put((byte)(paramInt >> 24));
      }
    }

    protected Encoder(Charset paramCharset, int paramInt, boolean paramBoolean)
    {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: ldc 2
      //   4: iload_3
      //   5: ifeq +8 -> 13
      //   8: ldc 3
      //   10: goto +5 -> 15
      //   13: ldc 2
      //   15: iload_2
      //   16: iconst_1
      //   17: if_icmpne +26 -> 43
      //   20: iconst_4
      //   21: newarray byte
      //   23: dup
      //   24: iconst_0
      //   25: iconst_0
      //   26: bastore
      //   27: dup
      //   28: iconst_1
      //   29: iconst_0
      //   30: bastore
      //   31: dup
      //   32: iconst_2
      //   33: iconst_m1
      //   34: bastore
      //   35: dup
      //   36: iconst_3
      //   37: bipush 253
      //   39: bastore
      //   40: goto +23 -> 63
      //   43: iconst_4
      //   44: newarray byte
      //   46: dup
      //   47: iconst_0
      //   48: bipush 253
      //   50: bastore
      //   51: dup
      //   52: iconst_1
      //   53: iconst_m1
      //   54: bastore
      //   55: dup
      //   56: iconst_2
      //   57: iconst_0
      //   58: bastore
      //   59: dup
      //   60: iconst_3
      //   61: iconst_0
      //   62: bastore
      //   63: invokespecial 81	java/nio/charset/CharsetEncoder:<init>	(Ljava/nio/charset/Charset;FF[B)V
      //   66: aload_0
      //   67: iconst_0
      //   68: putfield 73	sun/nio/cs/UTF_32Coder$Encoder:doBOM	Z
      //   71: aload_0
      //   72: iconst_1
      //   73: putfield 74	sun/nio/cs/UTF_32Coder$Encoder:doneBOM	Z
      //   76: aload_0
      //   77: iload_2
      //   78: putfield 72	sun/nio/cs/UTF_32Coder$Encoder:byteOrder	I
      //   81: aload_0
      //   82: iload_3
      //   83: putfield 73	sun/nio/cs/UTF_32Coder$Encoder:doBOM	Z
      //   86: aload_0
      //   87: iload_3
      //   88: ifne +7 -> 95
      //   91: iconst_1
      //   92: goto +4 -> 96
      //   95: iconst_0
      //   96: putfield 74	sun/nio/cs/UTF_32Coder$Encoder:doneBOM	Z
      //   99: return
    }

    protected CoderResult encodeLoop(CharBuffer paramCharBuffer, ByteBuffer paramByteBuffer)
    {
      int i = paramCharBuffer.position();
      if (!(this.doneBOM))
      {
        if (paramByteBuffer.remaining() < 4)
          return CoderResult.OVERFLOW;
        put(65279, paramByteBuffer);
        this.doneBOM = true;
      }
      try
      {
        while (paramCharBuffer.hasRemaining())
        {
          char c1 = paramCharBuffer.get();
          if (Surrogate.isHigh(c1))
          {
            CoderResult localCoderResult4;
            if (!(paramCharBuffer.hasRemaining()))
            {
              CoderResult localCoderResult2 = CoderResult.UNDERFLOW;
              return localCoderResult2;
            }
            char c2 = paramCharBuffer.get();
            if (Surrogate.isLow(c2))
            {
              if (paramByteBuffer.remaining() < 4)
              {
                localCoderResult4 = CoderResult.OVERFLOW;
                return localCoderResult4;
              }
              i += 2;
              put(Surrogate.toUCS4(c1, c2), paramByteBuffer);
            }
            else
            {
              localCoderResult4 = CoderResult.malformedForLength(1);
              return localCoderResult4;
            }
          }
          else
          {
            CoderResult localCoderResult3;
            if (Surrogate.isLow(c1))
            {
              localCoderResult3 = CoderResult.malformedForLength(1);
              return localCoderResult3;
            }
            if (paramByteBuffer.remaining() < 4)
            {
              localCoderResult3 = CoderResult.OVERFLOW;
              return localCoderResult3;
            }
            ++i;
            put(c1, paramByteBuffer);
          }
        }
        CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
        return localCoderResult1;
      }
      finally
      {
        paramCharBuffer.position(i);
      }
    }

    protected void implReset()
    {
      this.doneBOM = (!(this.doBOM));
    }
  }
}