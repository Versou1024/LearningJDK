package sun.nio.cs;

import java.nio.CharBuffer;
import java.nio.charset.CoderResult;

public class Surrogate
{
  public static final char MIN_HIGH = 55296;
  public static final char MAX_HIGH = 56319;
  public static final char MIN_LOW = 56320;
  public static final char MAX_LOW = 57343;
  public static final char MIN = 55296;
  public static final char MAX = 57343;
  public static final int UCS4_MIN = 65536;
  public static final int UCS4_MAX = 1114111;

  public static boolean isHigh(int paramInt)
  {
    return ((55296 <= paramInt) && (paramInt <= 56319));
  }

  public static boolean isLow(int paramInt)
  {
    return ((56320 <= paramInt) && (paramInt <= 57343));
  }

  public static boolean is(int paramInt)
  {
    return ((55296 <= paramInt) && (paramInt <= 57343));
  }

  public static boolean neededFor(int paramInt)
  {
    return ((paramInt >= 65536) && (paramInt <= 1114111));
  }

  public static char high(int paramInt)
  {
    if ((!($assertionsDisabled)) && (!(neededFor(paramInt))))
      throw new AssertionError();
    return (char)(0xD800 | paramInt - 65536 >> 10 & 0x3FF);
  }

  public static char low(int paramInt)
  {
    if ((!($assertionsDisabled)) && (!(neededFor(paramInt))))
      throw new AssertionError();
    return (char)(0xDC00 | paramInt - 65536 & 0x3FF);
  }

  public static int toUCS4(char paramChar1, char paramChar2)
  {
    if ((!($assertionsDisabled)) && (((!(isHigh(paramChar1))) || (!(isLow(paramChar2))))))
      throw new AssertionError();
    return (((paramChar1 & 0x3FF) << '\n' | paramChar2 & 0x3FF) + 65536);
  }

  public static class Generator
  {
    private CoderResult error = CoderResult.OVERFLOW;

    public CoderResult error()
    {
      if ((!($assertionsDisabled)) && (this.error == null))
        throw new AssertionError();
      return this.error;
    }

    public int generate(int paramInt1, int paramInt2, CharBuffer paramCharBuffer)
    {
      if (paramInt1 <= 65535)
      {
        if (Surrogate.is(paramInt1))
        {
          this.error = CoderResult.malformedForLength(paramInt2);
          return -1;
        }
        if (paramCharBuffer.remaining() < 1)
        {
          this.error = CoderResult.OVERFLOW;
          return -1;
        }
        paramCharBuffer.put((char)paramInt1);
        this.error = null;
        return 1;
      }
      if (paramInt1 < 65536)
      {
        this.error = CoderResult.malformedForLength(paramInt2);
        return -1;
      }
      if (paramInt1 <= 1114111)
      {
        if (paramCharBuffer.remaining() < 2)
        {
          this.error = CoderResult.OVERFLOW;
          return -1;
        }
        paramCharBuffer.put(Surrogate.high(paramInt1));
        paramCharBuffer.put(Surrogate.low(paramInt1));
        this.error = null;
        return 2;
      }
      this.error = CoderResult.unmappableForLength(paramInt2);
      return -1;
    }

    public int generate(int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3, int paramInt4)
    {
      if (paramInt1 <= 65535)
      {
        if (Surrogate.is(paramInt1))
        {
          this.error = CoderResult.malformedForLength(paramInt2);
          return -1;
        }
        if (paramInt4 - paramInt3 < 1)
        {
          this.error = CoderResult.OVERFLOW;
          return -1;
        }
        paramArrayOfChar[paramInt3] = (char)paramInt1;
        this.error = null;
        return 1;
      }
      if (paramInt1 < 65536)
      {
        this.error = CoderResult.malformedForLength(paramInt2);
        return -1;
      }
      if (paramInt1 <= 1114111)
      {
        if (paramInt4 - paramInt3 < 2)
        {
          this.error = CoderResult.OVERFLOW;
          return -1;
        }
        paramArrayOfChar[paramInt3] = Surrogate.high(paramInt1);
        paramArrayOfChar[(paramInt3 + 1)] = Surrogate.low(paramInt1);
        this.error = null;
        return 2;
      }
      this.error = CoderResult.unmappableForLength(paramInt2);
      return -1;
    }
  }

  public static class Parser
  {
    private int character;
    private CoderResult error = CoderResult.UNDERFLOW;
    private boolean isPair;

    public int character()
    {
      if ((!($assertionsDisabled)) && (this.error != null))
        throw new AssertionError();
      return this.character;
    }

    public boolean isPair()
    {
      if ((!($assertionsDisabled)) && (this.error != null))
        throw new AssertionError();
      return this.isPair;
    }

    public int increment()
    {
      if ((!($assertionsDisabled)) && (this.error != null))
        throw new AssertionError();
      return ((this.isPair) ? 2 : 1);
    }

    public CoderResult error()
    {
      if ((!($assertionsDisabled)) && (this.error == null))
        throw new AssertionError();
      return this.error;
    }

    public CoderResult unmappableResult()
    {
      if ((!($assertionsDisabled)) && (this.error != null))
        throw new AssertionError();
      return CoderResult.unmappableForLength((this.isPair) ? 2 : 1);
    }

    public int parse(char paramChar, CharBuffer paramCharBuffer)
    {
      if (Surrogate.isHigh(paramChar))
      {
        if (!(paramCharBuffer.hasRemaining()))
        {
          this.error = CoderResult.UNDERFLOW;
          return -1;
        }
        char c = paramCharBuffer.get();
        if (Surrogate.isLow(c))
        {
          this.character = Surrogate.toUCS4(paramChar, c);
          this.isPair = true;
          this.error = null;
          return this.character;
        }
        this.error = CoderResult.malformedForLength(1);
        return -1;
      }
      if (Surrogate.isLow(paramChar))
      {
        this.error = CoderResult.malformedForLength(1);
        return -1;
      }
      this.character = paramChar;
      this.isPair = false;
      this.error = null;
      return this.character;
    }

    public int parse(char paramChar, char[] paramArrayOfChar, int paramInt1, int paramInt2)
    {
      if ((!($assertionsDisabled)) && (paramArrayOfChar[paramInt1] != paramChar))
        throw new AssertionError();
      if (Surrogate.isHigh(paramChar))
      {
        if (paramInt2 - paramInt1 < 2)
        {
          this.error = CoderResult.UNDERFLOW;
          return -1;
        }
        char c = paramArrayOfChar[(paramInt1 + 1)];
        if (Surrogate.isLow(c))
        {
          this.character = Surrogate.toUCS4(paramChar, c);
          this.isPair = true;
          this.error = null;
          return this.character;
        }
        this.error = CoderResult.malformedForLength(1);
        return -1;
      }
      if (Surrogate.isLow(paramChar))
      {
        this.error = CoderResult.malformedForLength(1);
        return -1;
      }
      this.character = paramChar;
      this.isPair = false;
      this.error = null;
      return this.character;
    }
  }
}