package sun.nio.cs;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public abstract class UnicodeEncoder extends CharsetEncoder
{
  protected static final char BYTE_ORDER_MARK = 65279;
  protected static final char REVERSED_MARK = 65534;
  protected static final int BIG = 0;
  protected static final int LITTLE = 1;
  private int byteOrder;
  private boolean usesMark;
  private boolean needsMark;
  private final Surrogate.Parser sgp = new Surrogate.Parser();

  protected UnicodeEncoder(Charset paramCharset, int paramInt, boolean paramBoolean)
  {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: fconst_2
    //   3: iload_3
    //   4: ifeq +8 -> 12
    //   7: ldc 2
    //   9: goto +4 -> 13
    //   12: fconst_2
    //   13: iload_2
    //   14: ifne +18 -> 32
    //   17: iconst_2
    //   18: newarray byte
    //   20: dup
    //   21: iconst_0
    //   22: iconst_m1
    //   23: bastore
    //   24: dup
    //   25: iconst_1
    //   26: bipush 253
    //   28: bastore
    //   29: goto +15 -> 44
    //   32: iconst_2
    //   33: newarray byte
    //   35: dup
    //   36: iconst_0
    //   37: bipush 253
    //   39: bastore
    //   40: dup
    //   41: iconst_1
    //   42: iconst_m1
    //   43: bastore
    //   44: invokespecial 99	java/nio/charset/CharsetEncoder:<init>	(Ljava/nio/charset/Charset;FF[B)V
    //   47: aload_0
    //   48: new 55	sun/nio/cs/Surrogate$Parser
    //   51: dup
    //   52: invokespecial 103	sun/nio/cs/Surrogate$Parser:<init>	()V
    //   55: putfield 92	sun/nio/cs/UnicodeEncoder:sgp	Lsun/nio/cs/Surrogate$Parser;
    //   58: aload_0
    //   59: aload_0
    //   60: iload_3
    //   61: dup_x1
    //   62: putfield 90	sun/nio/cs/UnicodeEncoder:needsMark	Z
    //   65: putfield 91	sun/nio/cs/UnicodeEncoder:usesMark	Z
    //   68: aload_0
    //   69: iload_2
    //   70: putfield 89	sun/nio/cs/UnicodeEncoder:byteOrder	I
    //   73: return
  }

  private void put(char paramChar, ByteBuffer paramByteBuffer)
  {
    if (this.byteOrder == 0)
    {
      paramByteBuffer.put((byte)(paramChar >> '\b'));
      paramByteBuffer.put((byte)(paramChar & 0xFF));
    }
    else
    {
      paramByteBuffer.put((byte)(paramChar & 0xFF));
      paramByteBuffer.put((byte)(paramChar >> '\b'));
    }
  }

  protected CoderResult encodeLoop(CharBuffer paramCharBuffer, ByteBuffer paramByteBuffer)
  {
    int i = paramCharBuffer.position();
    if (this.needsMark)
    {
      if (paramByteBuffer.remaining() < 2)
        return CoderResult.OVERFLOW;
      put(65279, paramByteBuffer);
      this.needsMark = false;
    }
    try
    {
      while (true)
      {
        char c;
        CoderResult localCoderResult3;
        while (true)
        {
          if (!(paramCharBuffer.hasRemaining()))
            break label175;
          c = paramCharBuffer.get();
          if (Surrogate.is(c))
            break;
          if (paramByteBuffer.remaining() < 2)
          {
            CoderResult localCoderResult2 = CoderResult.OVERFLOW;
            return localCoderResult2;
          }
          ++i;
          put(c, paramByteBuffer);
        }
        int j = this.sgp.parse(c, paramCharBuffer);
        if (j < 0)
        {
          localCoderResult3 = this.sgp.error();
          return localCoderResult3;
        }
        if (paramByteBuffer.remaining() < 4)
        {
          localCoderResult3 = CoderResult.OVERFLOW;
          return localCoderResult3;
        }
        i += 2;
        put(Surrogate.high(j), paramByteBuffer);
        put(Surrogate.low(j), paramByteBuffer);
      }
      label175: CoderResult localCoderResult1 = CoderResult.UNDERFLOW;
      return localCoderResult1;
    }
    finally
    {
      paramCharBuffer.position(i);
    }
  }

  protected void implReset()
  {
    this.needsMark = this.usesMark;
  }

  public boolean canEncode(char paramChar)
  {
    return (!(Surrogate.is(paramChar)));
  }
}