package sun.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedFloatingDecimal
{
  boolean isExceptional;
  boolean isNegative;
  int decExponent;
  int decExponentRounded;
  char[] digits;
  int nDigits;
  int bigIntExp;
  int bigIntNBits;
  boolean mustSetRoundDir;
  boolean fromHex;
  int roundDir;
  int precision;
  private Form form;
  static final long signMask = -9223372036854775808L;
  static final long expMask = 9218868437227405312L;
  static final long fractMask = 4503599627370495L;
  static final int expShift = 52;
  static final int expBias = 1023;
  static final long fractHOB = 4503599627370496L;
  static final long expOne = 4607182418800017408L;
  static final int maxSmallBinExp = 62;
  static final int minSmallBinExp = -21;
  static final int maxDecimalDigits = 15;
  static final int maxDecimalExponent = 308;
  static final int minDecimalExponent = -324;
  static final int bigDecimalExponent = 324;
  static final long highbyte = -72057594037927936L;
  static final long highbit = -9223372036854775808L;
  static final long lowbytes = 72057594037927935L;
  static final int singleSignMask = -2147483648;
  static final int singleExpMask = 2139095040;
  static final int singleFractMask = 8388607;
  static final int singleExpShift = 23;
  static final int singleFractHOB = 8388608;
  static final int singleExpBias = 127;
  static final int singleMaxDecimalDigits = 7;
  static final int singleMaxDecimalExponent = 38;
  static final int singleMinDecimalExponent = -45;
  static final int intDecimalDigits = 9;
  private static FDBigInt[] b5p;
  private static ThreadLocal perThreadBuffer;
  private static final double[] small10pow;
  private static final float[] singleSmall10pow;
  private static final double[] big10pow;
  private static final double[] tiny10pow;
  private static final int maxSmallTen;
  private static final int singleMaxSmallTen;
  private static final int[] small5pow;
  private static final long[] long5pow;
  private static final int[] n5bits;
  private static final char[] infinity;
  private static final char[] notANumber;
  private static final char[] zero;
  private static Pattern hexFloatPattern;

  private FormattedFloatingDecimal(boolean paramBoolean1, int paramInt1, char[] paramArrayOfChar, int paramInt2, boolean paramBoolean2, int paramInt3, Form paramForm)
  {
    this.mustSetRoundDir = false;
    this.fromHex = false;
    this.roundDir = 0;
    this.isNegative = paramBoolean1;
    this.isExceptional = paramBoolean2;
    this.decExponent = paramInt1;
    this.digits = paramArrayOfChar;
    this.nDigits = paramInt2;
    this.precision = paramInt3;
    this.form = paramForm;
  }

  private static int countBits(long paramLong)
  {
    if (paramLong == 3412046810217185280L)
      return 0;
    while ((paramLong & 0x0) == 3412046689958100992L)
      paramLong <<= 8;
    while (paramLong > 3412046689958100992L)
      paramLong <<= 1;
    for (int i = 0; (paramLong & 0xFFFFFFFF) != 3412046810217185280L; i += 8)
      paramLong <<= 8;
    while (paramLong != 3412046689958100992L)
    {
      paramLong <<= 1;
      ++i;
    }
    return i;
  }

  private static synchronized FDBigInt big5pow(int paramInt)
  {
    if ((!($assertionsDisabled)) && (paramInt < 0))
      throw new AssertionError(paramInt);
    if (b5p == null)
    {
      b5p = new FDBigInt[paramInt + 1];
    }
    else if (b5p.length <= paramInt)
    {
      FDBigInt[] arrayOfFDBigInt = new FDBigInt[paramInt + 1];
      System.arraycopy(b5p, 0, arrayOfFDBigInt, 0, b5p.length);
      b5p = arrayOfFDBigInt;
    }
    if (b5p[paramInt] != null)
      return b5p[paramInt];
    if (paramInt < small5pow.length)
      return (b5p[paramInt] =  = new FDBigInt(small5pow[paramInt]));
    if (paramInt < long5pow.length)
      return (b5p[paramInt] =  = new FDBigInt(long5pow[paramInt]));
    int i = paramInt >> 1;
    int j = paramInt - i;
    FDBigInt localFDBigInt1 = b5p[i];
    if (localFDBigInt1 == null)
      localFDBigInt1 = big5pow(i);
    if (j < small5pow.length)
      return (b5p[paramInt] =  = localFDBigInt1.mult(small5pow[j]));
    FDBigInt localFDBigInt2 = b5p[j];
    if (localFDBigInt2 == null)
      localFDBigInt2 = big5pow(j);
    return (b5p[paramInt] =  = localFDBigInt1.mult(localFDBigInt2));
  }

  private static FDBigInt multPow52(FDBigInt paramFDBigInt, int paramInt1, int paramInt2)
  {
    if (paramInt1 != 0)
      if (paramInt1 < small5pow.length)
        paramFDBigInt = paramFDBigInt.mult(small5pow[paramInt1]);
      else
        paramFDBigInt = paramFDBigInt.mult(big5pow(paramInt1));
    if (paramInt2 != 0)
      paramFDBigInt.lshiftMe(paramInt2);
    return paramFDBigInt;
  }

  private static FDBigInt constructPow52(int paramInt1, int paramInt2)
  {
    FDBigInt localFDBigInt = new FDBigInt(big5pow(paramInt1));
    if (paramInt2 != 0)
      localFDBigInt.lshiftMe(paramInt2);
    return localFDBigInt;
  }

  private FDBigInt doubleToBigInt(double paramDouble)
  {
    long l = Double.doubleToLongBits(paramDouble) & 0xFFFFFFFF;
    int i = (int)(l >>> 52);
    l &= 4503599627370495L;
    if (i > 0)
    {
      l |= 4503599627370496L;
    }
    else
    {
      if ((!($assertionsDisabled)) && (l == 3412047617671036928L))
        throw new AssertionError(l);
      ++i;
      while ((l & 0x0) == 3412047342793129984L)
      {
        l <<= 1;
        --i;
      }
    }
    i -= 1023;
    int j = countBits(l);
    int k = 53 - j;
    l >>>= k;
    this.bigIntExp = (i + 1 - j);
    this.bigIntNBits = j;
    return new FDBigInt(l);
  }

  private static double ulp(double paramDouble, boolean paramBoolean)
  {
    double d;
    long l = Double.doubleToLongBits(paramDouble) & 0xFFFFFFFF;
    int i = (int)(l >>> 52);
    if ((paramBoolean) && (i >= 52) && ((l & 0xFFFFFFFF) == 3412046964836007936L))
      --i;
    if (i > 52)
      d = Double.longBitsToDouble(i - 52 << 52);
    else if (i == 0)
      d = 4.9e-324D;
    else
      d = Double.longBitsToDouble(3412040299046764545L << i - 1);
    if (paramBoolean)
      d = -d;
    return d;
  }

  float stickyRound(double paramDouble)
  {
    long l1 = Double.doubleToLongBits(paramDouble);
    long l2 = l1 & 0x0;
    if ((l2 == 3412046964836007936L) || (l2 == 9218868437227405312L))
      return (float)paramDouble;
    l1 += this.roundDir;
    return (float)Double.longBitsToDouble(l1);
  }

  private void developLongDigits(int paramInt, long paramLong1, long paramLong2)
  {
    char[] arrayOfChar1;
    int i;
    int j;
    int k;
    for (int l = 0; paramLong2 >= 10L; ++l)
      paramLong2 /= 10L;
    if (l != 0)
    {
      long l1 = long5pow[l] << l;
      long l2 = paramLong1 % l1;
      paramLong1 /= l1;
      paramInt += l;
      if (l2 >= l1 >> 1)
        paramLong1 += 3412048184606720001L;
    }
    if (paramLong1 <= 2147483647L)
    {
      if ((!($assertionsDisabled)) && (paramLong1 <= 3412047617671036928L))
        throw new AssertionError(paramLong1);
      int i1 = (int)paramLong1;
      i = 10;
      arrayOfChar1 = (char[])(char[])perThreadBuffer.get();
      j = i - 1;
      k = i1 % 10;
      i1 /= 10;
      while (k == 0)
      {
        ++paramInt;
        k = i1 % 10;
        i1 /= 10;
      }
      while (i1 != 0)
      {
        arrayOfChar1[(j--)] = (char)(k + 48);
        ++paramInt;
        k = i1 % 10;
        i1 /= 10;
      }
      arrayOfChar1[j] = (char)(k + 48);
    }
    else
    {
      i = 20;
      arrayOfChar1 = (char[])(char[])perThreadBuffer.get();
      j = i - 1;
      k = (int)(paramLong1 % 10L);
      paramLong1 /= 10L;
      while (k == 0)
      {
        ++paramInt;
        k = (int)(paramLong1 % 10L);
        paramLong1 /= 10L;
      }
      while (paramLong1 != 3412047342793129984L)
      {
        arrayOfChar1[(j--)] = (char)(k + 48);
        ++paramInt;
        k = (int)(paramLong1 % 10L);
        paramLong1 /= 10L;
      }
      arrayOfChar1[j] = (char)(k + 48);
    }
    i -= j;
    char[] arrayOfChar2 = new char[i];
    System.arraycopy(arrayOfChar1, j, arrayOfChar2, 0, i);
    this.digits = arrayOfChar2;
    this.decExponent = (paramInt + 1);
    this.nDigits = i;
  }

  private void roundup()
  {
    int i;
    int j = this.digits[(--this.nDigits)];
    if (j == 57)
    {
      while ((j == 57) && (i > 0))
      {
        this.digits[i] = '0';
        j = this.digits[(--i)];
      }
      if (j == 57)
      {
        this.decExponent += 1;
        this.digits[0] = '1';
        return;
      }
    }
    this.digits[i] = (char)(j + 1);
  }

  private int checkExponent(int paramInt)
  {
    if ((paramInt >= this.nDigits) || (paramInt < 0))
      return this.decExponent;
    for (int i = 0; i < paramInt; ++i)
      if (this.digits[i] != '9')
        return this.decExponent;
    return (this.decExponent + ((this.digits[paramInt] >= '5') ? 1 : 0));
  }

  private char[] applyPrecision(int paramInt)
  {
    char[] arrayOfChar = new char[this.nDigits];
    for (int i = 0; i < arrayOfChar.length; ++i)
      arrayOfChar[i] = '0';
    if ((paramInt >= this.nDigits) || (paramInt < 0))
    {
      System.arraycopy(this.digits, 0, arrayOfChar, 0, this.nDigits);
      return arrayOfChar;
    }
    if (paramInt == 0)
    {
      if (this.digits[0] >= '5')
        arrayOfChar[0] = '1';
      return arrayOfChar;
    }
    i = paramInt;
    int j = this.digits[i];
    if ((j >= 53) && (i > 0))
    {
      j = this.digits[(--i)];
      if (j == 57)
      {
        while ((j == 57) && (i > 0))
          j = this.digits[(--i)];
        if (j == 57)
        {
          arrayOfChar[0] = '1';
          return arrayOfChar;
        }
      }
      arrayOfChar[i] = (char)(j + 1);
    }
    while (--i >= 0)
      arrayOfChar[i] = this.digits[i];
    return arrayOfChar;
  }

  public FormattedFloatingDecimal(double paramDouble)
  {
    this(paramDouble, 2147483647, Form.COMPATIBLE);
  }

  public FormattedFloatingDecimal(double paramDouble, int paramInt, Form paramForm)
  {
    this.mustSetRoundDir = false;
    this.fromHex = false;
    this.roundDir = 0;
    long l1 = Double.doubleToLongBits(paramDouble);
    this.precision = paramInt;
    this.form = paramForm;
    if ((l1 & 0x0) != 3412046827397054464L)
    {
      this.isNegative = true;
      l1 ^= -9223372036854775808L;
    }
    else
    {
      this.isNegative = false;
    }
    int i = (int)((l1 & 0x0) >> 52);
    long l2 = l1 & 0xFFFFFFFF;
    if (i == 2047)
    {
      this.isExceptional = true;
      if (l2 == 3412047325613260800L)
      {
        this.digits = infinity;
      }
      else
      {
        this.digits = notANumber;
        this.isNegative = false;
      }
      this.nDigits = this.digits.length;
      return;
    }
    this.isExceptional = false;
    if (i == 0)
    {
      if (l2 == 3412047325613260800L)
      {
        this.decExponent = 0;
        this.digits = zero;
        this.nDigits = 1;
        return;
      }
      while ((l2 & 0x0) == 3412047342793129984L)
      {
        l2 <<= 1;
        --i;
      }
      j = 52 + i + 1;
      ++i;
    }
    else
    {
      l2 |= 4503599627370496L;
      j = 53;
    }
    dtoa(i -= 1023, l2, j);
  }

  public FormattedFloatingDecimal(float paramFloat)
  {
    this(paramFloat, 2147483647, Form.COMPATIBLE);
  }

  public FormattedFloatingDecimal(float paramFloat, int paramInt, Form paramForm)
  {
    this.mustSetRoundDir = false;
    this.fromHex = false;
    this.roundDir = 0;
    int i = Float.floatToIntBits(paramFloat);
    this.precision = paramInt;
    this.form = paramForm;
    if ((i & 0x80000000) != 0)
    {
      this.isNegative = true;
      i ^= -2147483648;
    }
    else
    {
      this.isNegative = false;
    }
    int k = (i & 0x7F800000) >> 23;
    int j = i & 0x7FFFFF;
    if (k == 255)
    {
      this.isExceptional = true;
      if (j == 3412047325613260800L)
      {
        this.digits = infinity;
      }
      else
      {
        this.digits = notANumber;
        this.isNegative = false;
      }
      this.nDigits = this.digits.length;
      return;
    }
    this.isExceptional = false;
    if (k == 0)
    {
      if (j == 0)
      {
        this.decExponent = 0;
        this.digits = zero;
        this.nDigits = 1;
        return;
      }
      while ((j & 0x800000) == 0)
      {
        j <<= 1;
        --k;
      }
      l = 23 + k + 1;
      ++k;
    }
    else
    {
      j |= 8388608;
      l = 24;
    }
    dtoa(k -= 127, j << 29, l);
  }

  private void dtoa(int paramInt1, long paramLong, int paramInt2)
  {
    int i5;
    int i11;
    int i12;
    long l2;
    int i13;
    int i15;
    int i = countBits(paramLong);
    int j = Math.max(0, i - paramInt1 - 1);
    if ((paramInt1 <= 62) && (paramInt1 >= -21) && (j < long5pow.length) && (i + n5bits[j] < 64) && (j == 0))
    {
      long l1;
      if (paramInt1 > paramInt2)
        l1 = 3412040006988988417L << paramInt1 - paramInt2 - 1;
      else
        l1 = 3412048184606720000L;
      if (paramInt1 >= 52)
        paramLong <<= paramInt1 - 52;
      else
        paramLong >>>= 52 - paramInt1;
      developLongDigits(0, paramLong, l1);
      return;
    }
    double d = Double.longBitsToDouble(0x0 | paramLong & 0xFFFFFFFF);
    int k = (int)Math.floor((d - 1.5D) * 0.28952965400000003D + 0.176091259D + paramInt1 * 0.30102999566398098D);
    int i1 = Math.max(0, -k);
    int l = i1 + j + paramInt1;
    int i3 = Math.max(0, k);
    int i2 = i3 + j;
    int i6 = i1;
    int i4 = l - paramInt2;
    paramLong >>>= 53 - i;
    l -= i - 1;
    int i9 = Math.min(l, i2);
    l -= i9;
    i2 -= i9;
    i4 -= i9;
    if (i == 1)
      --i4;
    if (i4 < 0)
    {
      l -= i4;
      i2 -= i4;
      i5 = 0;
    }
    char[] arrayOfChar = this.digits = new char[18];
    int i10 = 0;
    int i7 = i + l + ((i1 < n5bits.length) ? n5bits[i1] : i1 * 3);
    int i8 = i2 + 1 + ((i3 + 1 < n5bits.length) ? n5bits[(i3 + 1)] : (i3 + 1) * 3);
    if ((i7 < 64) && (i8 < 64))
    {
      if ((i7 < 32) && (i8 < 32))
      {
        int i14 = (int)paramLong * small5pow[i1] << l;
        i15 = small5pow[i3] << i2;
        int i16 = small5pow[i6] << i5;
        int i17 = i15 * 10;
        i10 = 0;
        i13 = i14 / i15;
        i14 = 10 * i14 % i15;
        i16 *= 10;
        i11 = (i14 < i16) ? 1 : 0;
        i12 = (i14 + i16 > i17) ? 1 : 0;
        if ((!($assertionsDisabled)) && (i13 >= 10))
          throw new AssertionError(i13);
        if ((i13 == 0) && (i12 == 0))
          --k;
        else
          arrayOfChar[(i10++)] = (char)(48 + i13);
        if ((this.form != Form.COMPATIBLE) || (-3 >= k) || (k >= 8))
          i12 = i11 = 0;
        while ((i11 == 0) && (i12 == 0))
        {
          i13 = i14 / i15;
          i14 = 10 * i14 % i15;
          i16 *= 10;
          if ((!($assertionsDisabled)) && (i13 >= 10))
            throw new AssertionError(i13);
          if (i16 > 3412039714931212288L)
          {
            i11 = (i14 < i16) ? 1 : 0;
            i12 = (i14 + i16 > i17) ? 1 : 0;
          }
          else
          {
            i11 = 1;
            i12 = 1;
          }
          arrayOfChar[(i10++)] = (char)(48 + i13);
        }
        l2 = (i14 << 1) - i17;
      }
      else
      {
        long l3 = paramLong * long5pow[i1] << l;
        long l4 = long5pow[i3] << i2;
        long l5 = long5pow[i6] << i5;
        long l6 = l4 * 10L;
        i10 = 0;
        i13 = (int)(l3 / l4);
        l3 = 10L * l3 % l4;
        l5 *= 10L;
        i11 = (l3 < l5) ? 1 : 0;
        i12 = (l3 + l5 > l6) ? 1 : 0;
        if ((!($assertionsDisabled)) && (i13 >= 10))
          throw new AssertionError(i13);
        if ((i13 == 0) && (i12 == 0))
          --k;
        else
          arrayOfChar[(i10++)] = (char)(48 + i13);
        if ((this.form != Form.COMPATIBLE) || (-3 >= k) || (k >= 8))
          i12 = i11 = 0;
        while ((i11 == 0) && (i12 == 0))
        {
          i13 = (int)(l3 / l4);
          l3 = 10L * l3 % l4;
          l5 *= 10L;
          if ((!($assertionsDisabled)) && (i13 >= 10))
            throw new AssertionError(i13);
          if (l5 > 3412039714931212288L)
          {
            i11 = (l3 < l5) ? 1 : 0;
            i12 = (l3 + l5 > l6) ? 1 : 0;
          }
          else
          {
            i11 = 1;
            i12 = 1;
          }
          arrayOfChar[(i10++)] = (char)(48 + i13);
        }
        l2 = (l3 << 1) - l6;
      }
    }
    else
    {
      FDBigInt localFDBigInt2 = multPow52(new FDBigInt(paramLong), i1, l);
      FDBigInt localFDBigInt1 = constructPow52(i3, i2);
      FDBigInt localFDBigInt3 = constructPow52(i6, i5);
      localFDBigInt2.lshiftMe(i15 = localFDBigInt1.normalizeMe());
      localFDBigInt3.lshiftMe(i15);
      FDBigInt localFDBigInt4 = localFDBigInt1.mult(10);
      i10 = 0;
      i13 = localFDBigInt2.quoRemIteration(localFDBigInt1);
      localFDBigInt3 = localFDBigInt3.mult(10);
      i11 = (localFDBigInt2.cmp(localFDBigInt3) < 0) ? 1 : 0;
      i12 = (localFDBigInt2.add(localFDBigInt3).cmp(localFDBigInt4) > 0) ? 1 : 0;
      if ((!($assertionsDisabled)) && (i13 >= 10))
        throw new AssertionError(i13);
      if ((i13 == 0) && (i12 == 0))
        --k;
      else
        arrayOfChar[(i10++)] = (char)(48 + i13);
      if ((this.form != Form.COMPATIBLE) || (-3 >= k) || (k >= 8))
        i12 = i11 = 0;
      while ((i11 == 0) && (i12 == 0))
      {
        i13 = localFDBigInt2.quoRemIteration(localFDBigInt1);
        localFDBigInt3 = localFDBigInt3.mult(10);
        if ((!($assertionsDisabled)) && (i13 >= 10))
          throw new AssertionError(i13);
        i11 = (localFDBigInt2.cmp(localFDBigInt3) < 0) ? 1 : 0;
        i12 = (localFDBigInt2.add(localFDBigInt3).cmp(localFDBigInt4) > 0) ? 1 : 0;
        arrayOfChar[(i10++)] = (char)(48 + i13);
      }
      if ((i12 != 0) && (i11 != 0))
      {
        localFDBigInt2.lshiftMe(1);
        l2 = localFDBigInt2.cmp(localFDBigInt4);
      }
      else
      {
        l2 = 3412048201786589184L;
      }
    }
    this.decExponent = (k + 1);
    this.digits = arrayOfChar;
    this.nDigits = i10;
    if (i12 != 0)
      if (i11 != 0)
        if (l2 == 3412047978448289792L)
          if ((arrayOfChar[(this.nDigits - 1)] & 0x1) != 0)
            roundup();
        else if (l2 > 3412048098707374080L)
          roundup();
      else
        roundup();
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer(this.nDigits + 8);
    if (this.isNegative)
      localStringBuffer.append('-');
    if (this.isExceptional)
    {
      localStringBuffer.append(this.digits, 0, this.nDigits);
    }
    else
    {
      localStringBuffer.append("0.");
      localStringBuffer.append(this.digits, 0, this.nDigits);
      localStringBuffer.append('e');
      localStringBuffer.append(this.decExponent);
    }
    return new String(localStringBuffer);
  }

  public String toJavaFormatString()
  {
    char[] arrayOfChar = (char[])(char[])perThreadBuffer.get();
    int i = getChars(arrayOfChar);
    return new String(arrayOfChar, 0, i);
  }

  public int getExponent()
  {
    return (this.decExponent - 1);
  }

  public int getExponentRounded()
  {
    return (this.decExponentRounded - 1);
  }

  public int getChars(char[] paramArrayOfChar)
  {
    if ((!($assertionsDisabled)) && (this.nDigits > 19))
      throw new AssertionError(this.nDigits);
    int i = 0;
    if (this.isNegative)
    {
      paramArrayOfChar[0] = '-';
      i = 1;
    }
    if (this.isExceptional)
    {
      System.arraycopy(this.digits, 0, paramArrayOfChar, i, this.nDigits);
      i += this.nDigits;
    }
    else
    {
      int k;
      int l;
      char[] arrayOfChar = this.digits;
      int j = this.decExponent;
      switch (2.$SwitchMap$sun$misc$FormattedFloatingDecimal$Form[this.form.ordinal()])
      {
      case 1:
        break;
      case 2:
        j = checkExponent(this.decExponent + this.precision);
        arrayOfChar = applyPrecision(this.decExponent + this.precision);
        break;
      case 3:
        j = checkExponent(this.precision + 1);
        arrayOfChar = applyPrecision(this.precision + 1);
        break;
      case 4:
        j = checkExponent(this.precision);
        arrayOfChar = applyPrecision(this.precision);
        if ((j - 1 < -4) || (j - 1 >= this.precision))
        {
          this.form = Form.SCIENTIFIC;
          this.precision -= 1;
        }
        else
        {
          this.form = Form.DECIMAL_FLOAT;
          this.precision -= j;
        }
        break;
      default:
        if (!($assertionsDisabled))
          throw new AssertionError();
      }
      this.decExponentRounded = j;
      if ((j > 0) && ((((this.form == Form.COMPATIBLE) && (j < 8)) || (this.form == Form.DECIMAL_FLOAT))))
      {
        k = Math.min(this.nDigits, j);
        System.arraycopy(arrayOfChar, 0, paramArrayOfChar, i, k);
        i += k;
        if (k < j)
        {
          k = j - k;
          for (l = 0; l < k; ++l)
            paramArrayOfChar[(i++)] = '0';
          if (this.form == Form.COMPATIBLE)
          {
            paramArrayOfChar[(i++)] = '.';
            paramArrayOfChar[(i++)] = '0';
          }
        }
        else if (this.form == Form.COMPATIBLE)
        {
          paramArrayOfChar[(i++)] = '.';
          if (k < this.nDigits)
          {
            l = Math.min(this.nDigits - k, this.precision);
            System.arraycopy(arrayOfChar, k, paramArrayOfChar, i, l);
            i += l;
          }
          else
          {
            paramArrayOfChar[(i++)] = '0';
          }
        }
        else
        {
          l = Math.min(this.nDigits - k, this.precision);
          if (l > 0)
          {
            paramArrayOfChar[(i++)] = '.';
            System.arraycopy(arrayOfChar, k, paramArrayOfChar, i, l);
            i += l;
          }
        }
      }
      else if ((j <= 0) && ((((this.form == Form.COMPATIBLE) && (j > -3)) || (this.form == Form.DECIMAL_FLOAT))))
      {
        paramArrayOfChar[(i++)] = '0';
        if (j != 0)
        {
          k = Math.min(-j, this.precision);
          if (k > 0)
          {
            paramArrayOfChar[(i++)] = '.';
            for (l = 0; l < k; ++l)
              paramArrayOfChar[(i++)] = '0';
          }
        }
        k = Math.min(arrayOfChar.length, this.precision + j);
        if (k > 0)
        {
          if (i == 1)
            paramArrayOfChar[(i++)] = '.';
          System.arraycopy(arrayOfChar, 0, paramArrayOfChar, i, k);
          i += k;
        }
      }
      else
      {
        paramArrayOfChar[(i++)] = arrayOfChar[0];
        if (this.form == Form.COMPATIBLE)
        {
          paramArrayOfChar[(i++)] = '.';
          if (this.nDigits > 1)
          {
            System.arraycopy(arrayOfChar, 1, paramArrayOfChar, i, this.nDigits - 1);
            i += this.nDigits - 1;
          }
          else
          {
            paramArrayOfChar[(i++)] = '0';
          }
          paramArrayOfChar[(i++)] = 'E';
        }
        else
        {
          if (this.nDigits > 1)
          {
            k = Math.min(this.nDigits - 1, this.precision);
            if (k > 0)
            {
              paramArrayOfChar[(i++)] = '.';
              System.arraycopy(arrayOfChar, 1, paramArrayOfChar, i, k);
              i += k;
            }
          }
          paramArrayOfChar[(i++)] = 'e';
        }
        if (j <= 0)
        {
          paramArrayOfChar[(i++)] = '-';
          k = -j + 1;
        }
        else
        {
          if (this.form != Form.COMPATIBLE)
            paramArrayOfChar[(i++)] = '+';
          k = j - 1;
        }
        if (k <= 9)
        {
          if (this.form != Form.COMPATIBLE)
            paramArrayOfChar[(i++)] = '0';
          paramArrayOfChar[(i++)] = (char)(k + 48);
        }
        else if (k <= 99)
        {
          paramArrayOfChar[(i++)] = (char)(k / 10 + 48);
          paramArrayOfChar[(i++)] = (char)(k % 10 + 48);
        }
        else
        {
          paramArrayOfChar[(i++)] = (char)(k / 100 + 48);
          k %= 100;
          paramArrayOfChar[(i++)] = (char)(k / 10 + 48);
          paramArrayOfChar[(i++)] = (char)(k % 10 + 48);
        }
      }
    }
    return i;
  }

  public void appendTo(Appendable paramAppendable)
  {
    char[] arrayOfChar = (char[])(char[])perThreadBuffer.get();
    int i = getChars(arrayOfChar);
    if (paramAppendable instanceof StringBuilder)
      ((StringBuilder)paramAppendable).append(arrayOfChar, 0, i);
    else if (paramAppendable instanceof StringBuffer)
      ((StringBuffer)paramAppendable).append(arrayOfChar, 0, i);
    else if (!($assertionsDisabled))
      throw new AssertionError();
  }

  public static FormattedFloatingDecimal readJavaFormatString(String paramString)
    throws NumberFormatException
  {
    boolean bool = false;
    int i = 0;
    try
    {
      label170: int j;
      int i2;
      paramString = paramString.trim();
      int l = paramString.length();
      if (l == 0)
        throw new NumberFormatException("empty String");
      int i1 = 0;
      switch (k = paramString.charAt(i1))
      {
      case '-':
        bool = true;
      case '+':
        ++i1;
        i = 1;
      }
      int k = paramString.charAt(i1);
      if ((k == 78) || (k == 73))
      {
        i2 = 0;
        char[] arrayOfChar2 = null;
        if (k == 78)
        {
          arrayOfChar2 = notANumber;
          i2 = 1;
        }
        else
        {
          arrayOfChar2 = infinity;
        }
        i4 = 0;
        while (true)
        {
          if ((i1 >= l) || (i4 >= arrayOfChar2.length))
            break label170;
          if (paramString.charAt(i1) != arrayOfChar2[i4])
            break;
          ++i1;
          ++i4;
        }
        break label829:
        if ((i4 == arrayOfChar2.length) && (i1 == l))
          return new FormattedFloatingDecimal((1.0D / 0.0D));
        break label829:
      }
      if ((k == 48) && (l > i1 + 1))
      {
        i2 = paramString.charAt(i1 + 1);
        if ((i2 == 120) || (i2 == 88))
          return parseHexString(paramString);
      }
      char[] arrayOfChar1 = new char[l];
      int i3 = 0;
      int i4 = 0;
      int i5 = 0;
      int i6 = 0;
      int i7 = 0;
      while (i1 < l)
      {
        switch (k = paramString.charAt(i1))
        {
        case '0':
          if (i3 > 0)
            ++i7;
          else
            ++i6;
          break;
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          while (i7 > 0)
          {
            arrayOfChar1[(i3++)] = '0';
            --i7;
          }
          arrayOfChar1[(i3++)] = k;
          break;
        case '.':
          if (i4 != 0)
            throw new NumberFormatException("multiple points");
          i5 = i1;
          if (i != 0)
            --i5;
          i4 = 1;
          break;
        case '/':
        default:
          break;
        }
        ++i1;
      }
      if (i3 == 0)
      {
        arrayOfChar1 = zero;
        i3 = 1;
        if (i6 == 0)
          break label829:
      }
      if (i4 != 0)
        j = i5 - i6;
      else
        j = i3 + i7;
      if (i1 < l)
        if (((k = paramString.charAt(i1)) == 'e') || (k == 69))
        {
          int i8 = 1;
          int i9 = 0;
          int i10 = 214748364;
          int i11 = 0;
          switch (paramString.charAt(++i1))
          {
          case '-':
            i8 = -1;
          case '+':
          }
          int i12 = ++i1;
          while (true)
          {
            if (i1 >= l)
              break label694;
            if (i9 >= i10)
              i11 = 1;
            switch (k = paramString.charAt(i1++))
            {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              i9 = i9 * 10 + k - 48;
            }
          }
          --i1;
          label694: int i13 = 324 + i3 + i7;
          if ((i11 != 0) || (i9 > i13))
            j = i8 * i13;
          else
            j += i8 * i9;
          if (i1 == i12)
            break label829:
        }
      if ((i1 < l) && (((i1 != l - 1) || ((paramString.charAt(i1) != 'f') && (paramString.charAt(i1) != 'F') && (paramString.charAt(i1) != 'd') && (paramString.charAt(i1) != 'D')))))
        break label829:
      label829: return new FormattedFloatingDecimal(bool, j, arrayOfChar1, i3, false, 2147483647, Form.COMPATIBLE);
    }
    catch (StringIndexOutOfBoundsException localStringIndexOutOfBoundsException)
    {
      throw new NumberFormatException("For input string: \"" + paramString + "\"");
    }
  }

  public double doubleValue()
  {
    int i2;
    double d4;
    int i = Math.min(this.nDigits, 16);
    if ((this.digits == infinity) || (this.digits == notANumber))
    {
      if (this.digits == notANumber)
        return (0.0D / 0.0D);
      return ((this.isNegative) ? (-1.0D / 0.0D) : (1.0D / 0.0D));
    }
    if (this.mustSetRoundDir)
      this.roundDir = 0;
    int j = this.digits[0] - '0';
    int k = Math.min(i, 9);
    for (int i1 = 1; i1 < k; ++i1)
      j = j * 10 + this.digits[i1] - 48;
    long l = j;
    for (i1 = k; i1 < i; ++i1)
      l = l * 10L + this.digits[i1] - '0';
    double d1 = l;
    i1 = this.decExponent - i;
    if (this.nDigits <= 15)
    {
      double d2;
      double d3;
      if ((i1 == 0) || (d1 == 0D))
        return ((this.isNegative) ? -d1 : d1);
      if (i1 >= 0)
      {
        if (i1 <= maxSmallTen)
        {
          d2 = d1 * small10pow[i1];
          if (this.mustSetRoundDir)
          {
            d3 = d2 / small10pow[i1];
            this.roundDir = ((d3 < d1) ? 1 : (d3 == d1) ? 0 : -1);
          }
          return ((this.isNegative) ? -d2 : d2);
        }
        i2 = 15 - i;
        if (i1 <= maxSmallTen + i2)
        {
          d1 *= small10pow[i2];
          d2 = d1 * small10pow[(i1 - i2)];
          if (this.mustSetRoundDir)
          {
            d3 = d2 / small10pow[(i1 - i2)];
            this.roundDir = ((d3 < d1) ? 1 : (d3 == d1) ? 0 : -1);
          }
          return ((this.isNegative) ? -d2 : d2);
        }
      }
      else if (i1 >= -maxSmallTen)
      {
        d2 = d1 / small10pow[(-i1)];
        d3 = d2 * small10pow[(-i1)];
        if (this.mustSetRoundDir)
          this.roundDir = ((d3 < d1) ? 1 : (d3 == d1) ? 0 : -1);
        return ((this.isNegative) ? -d2 : d2);
      }
    }
    if (i1 > 0)
    {
      if (this.decExponent > 309)
        return ((this.isNegative) ? (-1.0D / 0.0D) : (1.0D / 0.0D));
      if ((i1 & 0xF) != 0)
        d1 *= small10pow[(i1 & 0xF)];
      if (i1 >>= 4 != 0)
      {
        i2 = 0;
        while (i1 > 1)
        {
          if ((i1 & 0x1) != 0)
            d1 *= big10pow[i2];
          ++i2;
          i1 >>= 1;
        }
        d4 = d1 * big10pow[i2];
        if (Double.isInfinite(d4))
        {
          d4 = d1 / 2.0D;
          d4 *= big10pow[i2];
          if (Double.isInfinite(d4))
            return ((this.isNegative) ? (-1.0D / 0.0D) : (1.0D / 0.0D));
          d4 = 1.7976931348623157e+308D;
        }
        d1 = d4;
      }
    }
    else if (i1 < 0)
    {
      i1 = -i1;
      if (this.decExponent < -325)
        return ((this.isNegative) ? -0.0D : 0D);
      if ((i1 & 0xF) != 0)
        d1 /= small10pow[(i1 & 0xF)];
      if (i1 >>= 4 != 0)
      {
        i2 = 0;
        while (i1 > 1)
        {
          if ((i1 & 0x1) != 0)
            d1 *= tiny10pow[i2];
          ++i2;
          i1 >>= 1;
        }
        d4 = d1 * tiny10pow[i2];
        if (d4 == 0D)
        {
          d4 = d1 * 2.0D;
          d4 *= tiny10pow[i2];
          if (d4 == 0D)
            return ((this.isNegative) ? -0.0D : 0D);
          d4 = 4.9e-324D;
        }
        d1 = d4;
      }
    }
    FDBigInt localFDBigInt1 = new FDBigInt(l, this.digits, i, this.nDigits);
    i1 = this.decExponent - this.nDigits;
    do
    {
      int i3;
      int i4;
      int i5;
      int i6;
      int i8;
      FDBigInt localFDBigInt4;
      int i10;
      boolean bool;
      FDBigInt localFDBigInt2 = doubleToBigInt(d1);
      if (i1 >= 0)
      {
        i3 = i4 = 0;
        i5 = i6 = i1;
      }
      else
      {
        i3 = i4 = -i1;
        i5 = i6 = 0;
      }
      if (this.bigIntExp >= 0)
        i3 += this.bigIntExp;
      else
        i5 -= this.bigIntExp;
      int i7 = i3;
      if (this.bigIntExp + this.bigIntNBits <= -1022)
        i8 = this.bigIntExp + 1023 + 52;
      else
        i8 = 54 - this.bigIntNBits;
      i3 += i8;
      i5 += i8;
      int i9 = Math.min(i3, Math.min(i5, i7));
      i3 -= i9;
      i5 -= i9;
      i7 -= i9;
      localFDBigInt2 = multPow52(localFDBigInt2, i4, i3);
      FDBigInt localFDBigInt3 = multPow52(new FDBigInt(localFDBigInt1), i6, i5);
      if ((i10 = localFDBigInt2.cmp(localFDBigInt3)) > 0)
      {
        bool = true;
        localFDBigInt4 = localFDBigInt2.sub(localFDBigInt3);
        if ((this.bigIntNBits == 1) && (this.bigIntExp > -1023) && (--i7 < 0))
        {
          i7 = 0;
          localFDBigInt4.lshiftMe(1);
        }
      }
      else
      {
        if (i10 >= 0)
          break;
        bool = false;
        localFDBigInt4 = localFDBigInt3.sub(localFDBigInt2);
      }
      FDBigInt localFDBigInt5 = constructPow52(i4, i7);
      if ((i10 = localFDBigInt4.cmp(localFDBigInt5)) < 0)
      {
        if (!(this.mustSetRoundDir))
          break;
        this.roundDir = ((bool) ? -1 : 1);
        break;
      }
      if (i10 == 0)
      {
        d1 += 0.5D * ulp(d1, bool);
        if (!(this.mustSetRoundDir))
          break;
        this.roundDir = ((bool) ? -1 : 1);
        break;
      }
      d1 += ulp(d1, bool);
      if (d1 == 0D)
        break;
    }
    while (d1 != (1.0D / 0.0D));
    return ((this.isNegative) ? -d1 : d1);
  }

  public float floatValue()
  {
    int i = Math.min(this.nDigits, 8);
    if ((this.digits == infinity) || (this.digits == notANumber))
    {
      if (this.digits == notANumber)
        return (0.0F / 0.0F);
      return ((this.isNegative) ? (1.0F / -1.0F) : (1.0F / 1.0F));
    }
    int j = this.digits[0] - '0';
    for (int k = 1; k < i; ++k)
      j = j * 10 + this.digits[k] - 48;
    float f = j;
    k = this.decExponent - i;
    if (this.nDigits <= 7)
    {
      if ((k == 0) || (f == 0F))
        return ((this.isNegative) ? -f : f);
      if (k >= 0)
      {
        if (k <= singleMaxSmallTen)
        {
          f *= singleSmall10pow[k];
          return ((this.isNegative) ? -f : f);
        }
        int l = 7 - i;
        if (k <= singleMaxSmallTen + l)
        {
          f *= singleSmall10pow[l];
          f *= singleSmall10pow[(k - l)];
          return ((this.isNegative) ? -f : f);
        }
        break label380:
      }
      if (k < -singleMaxSmallTen)
        break label380;
      f /= singleSmall10pow[(-k)];
      return ((this.isNegative) ? -f : f);
    }
    if ((this.decExponent >= this.nDigits) && (this.nDigits + this.decExponent <= 15))
    {
      long l1 = j;
      for (int i1 = i; i1 < this.nDigits; ++i1)
        l1 = l1 * 10L + this.digits[i1] - '0';
      double d2 = l1;
      k = this.decExponent - this.nDigits;
      d2 *= small10pow[k];
      f = (float)d2;
      return ((this.isNegative) ? -f : f);
    }
    if (this.decExponent > 39)
      label380: return ((this.isNegative) ? (1.0F / -1.0F) : (1.0F / 1.0F));
    if (this.decExponent < -46)
      return ((this.isNegative) ? -0.0F : 0F);
    this.mustSetRoundDir = (!(this.fromHex));
    double d1 = doubleValue();
    return stickyRound(d1);
  }

  static FormattedFloatingDecimal parseHexString(String paramString)
  {
    String str4;
    long l1;
    long l6;
    Matcher localMatcher = hexFloatPattern.matcher(paramString);
    boolean bool = localMatcher.matches();
    if (!(bool))
      throw new NumberFormatException("For input string: \"" + paramString + "\"");
    String str1 = localMatcher.group(1);
    double d = ((str1 == null) || (str1.equals("+"))) ? 1D : -1.0D;
    String str2 = null;
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    if ((str4 = localMatcher.group(4)) != null)
    {
      str2 = stripLeadingZeros(str4);
      k = str2.length();
    }
    else
    {
      String str5 = stripLeadingZeros(localMatcher.group(6));
      k = str5.length();
      String str6 = localMatcher.group(7);
      l = str6.length();
      str2 = ((str5 == null) ? "" : str5) + str6;
    }
    str2 = stripLeadingZeros(str2);
    i = str2.length();
    if (k >= 1)
      j = 4 * (k - 1);
    else
      j = -4 * (l - i + 1);
    if (i == 0)
      return new FormattedFloatingDecimal(d * 0D);
    String str3 = localMatcher.group(8);
    l = ((str3 == null) || (str3.equals("+"))) ? 1 : 0;
    try
    {
      l1 = Integer.parseInt(localMatcher.group(9));
    }
    catch (NumberFormatException localNumberFormatException)
    {
      return new FormattedFloatingDecimal(d * 0D);
    }
    long l2 = ((l != 0) ? 3412048356405411841L : -1L) * l1;
    long l3 = l2 + j;
    int i1 = 0;
    int i2 = 0;
    int i3 = 0;
    int i4 = 0;
    long l4 = 3412047291253522432L;
    long l5 = getHexDigit(str2, 0);
    if (l5 == 3412046827397054465L)
    {
      l4 |= l5 << 52;
      i4 = 48;
    }
    else if (l5 <= 3L)
    {
      l4 |= l5 << 51;
      i4 = 47;
      l3 += 3412047841009336321L;
    }
    else if (l5 <= 7L)
    {
      l4 |= l5 << 50;
      i4 = 46;
      l3 += 2L;
    }
    else if (l5 <= 15L)
    {
      l4 |= l5 << 49;
      i4 = 45;
      l3 += 3L;
    }
    else
    {
      throw new AssertionError("Result from digit converstion too large!");
    }
    int i5 = 0;
    for (i5 = 1; (i5 < i) && (i4 >= 0); ++i5)
    {
      l6 = getHexDigit(str2, i5);
      l4 |= l6 << i4;
      i4 -= 4;
    }
    if (i5 < i)
    {
      l6 = getHexDigit(str2, i5);
      switch (i4)
      {
      case -1:
        l4 |= (l6 & 0xE) >> 1;
        i1 = ((l6 & 3412040900342185985L) != 3412040041348726784L) ? 1 : 0;
        break;
      case -2:
        l4 |= (l6 & 0xC) >> 2;
        i1 = ((l6 & 0x2) != 3412040041348726784L) ? 1 : 0;
        i2 = ((l6 & 3412040900342185985L) != 3412040041348726784L) ? 1 : 0;
        break;
      case -3:
        l4 |= (l6 & 0x8) >> 3;
        i1 = ((l6 & 0x4) != 3412040041348726784L) ? 1 : 0;
        i2 = ((l6 & 0x3) != 3412040041348726784L) ? 1 : 0;
        break;
      case -4:
        i1 = ((l6 & 0x8) != 3412040041348726784L) ? 1 : 0;
        i2 = ((l6 & 0x7) != 3412040041348726784L) ? 1 : 0;
        break;
      default:
        throw new AssertionError("Unexpected shift distance remainder.");
      }
      ++i5;
      while ((i5 < i) && (i2 == 0))
      {
        l6 = getHexDigit(str2, i5);
        i2 = ((i2 != 0) || (l6 != 3412040024168857600L)) ? 1 : 0;
        ++i5;
      }
    }
    if (l3 > 1023L)
      return new FormattedFloatingDecimal(d * (1.0D / 0.0D));
    if ((l3 <= 1023L) && (l3 >= -1022L))
    {
      l4 = l3 + 1023L << 52 & 0x0 | 0xFFFFFFFF & l4;
    }
    else
    {
      if (l3 < -1075L)
        return new FormattedFloatingDecimal(d * 0D);
      i2 = ((i2 != 0) || (i1 != 0)) ? 1 : 0;
      i1 = 0;
      i6 = 53 - (int)l3 - -1074 + 1;
      if ((!($assertionsDisabled)) && (((i6 < 1) || (i6 > 53))))
        throw new AssertionError();
      i1 = ((l4 & 3412040848802578433L << i6 - 1) != 3412048304865804288L) ? 1 : 0;
      if (i6 > 1)
      {
        long l7 = -1L << i6 - 1 ^ 0xFFFFFFFF;
        i2 = ((i2 != 0) || ((l4 & l7) != 3412040161607811072L)) ? 1 : 0;
      }
      l4 >>= i6;
      l4 = 3412048407945019392L | 0xFFFFFFFF & l4;
    }
    int i6 = 0;
    int i7 = ((l4 & 3412039835190296577L) == 3412047909728813056L) ? 1 : 0;
    if (((i7 != 0) && (i1 != 0) && (i2 != 0)) || ((i7 == 0) && (i1 != 0)))
    {
      i6 = 1;
      l4 += 3412047686390513665L;
    }
    FormattedFloatingDecimal localFormattedFloatingDecimal = new FormattedFloatingDecimal(FpUtils.rawCopySign(Double.longBitsToDouble(l4), d));
    if ((l3 >= -150L) && (l3 <= 127L) && ((l4 & 0xFFFFFFF) == 3412046964836007936L) && (((i1 != 0) || (i2 != 0))))
      if (i7 != 0)
        if ((i1 ^ i2) != 0)
          localFormattedFloatingDecimal.roundDir = 1;
      else if (i1 != 0)
        localFormattedFloatingDecimal.roundDir = -1;
    localFormattedFloatingDecimal.fromHex = true;
    return localFormattedFloatingDecimal;
  }

  static String stripLeadingZeros(String paramString)
  {
    return paramString.replaceFirst("^0+", "");
  }

  static int getHexDigit(String paramString, int paramInt)
  {
    int i = Character.digit(paramString.charAt(paramInt), 16);
    if ((i <= -1) || (i >= 16))
      throw new AssertionError("Unxpected failure of digit converstion of " + paramString.charAt(paramInt));
    return i;
  }

  static
  {
    perThreadBuffer = new ThreadLocal()
    {
      protected synchronized Object initialValue()
      {
        return new char[26];
      }
    };
    small10pow = { 1D, 10.0D, 100.0D, 1000.0D, 10000.0D, 100000.0D, 1000000.0D, 10000000.0D, 100000000.0D, 1000000000.0D, 10000000000.0D, 100000000000.0D, 1000000000000.0D, 10000000000000.0D, 100000000000000.0D, 1000000000000000.0D, 10000000000000000.0D, 100000000000000000.0D, 1000000000000000000.0D, 10000000000000000000.0D, 100000000000000000000.0D, 1000000000000000000000.0D, 10000000000000000000000.0D };
    singleSmall10pow = { 1F, 10.0F, 100.0F, 1000.0F, 10000.0F, 100000.0F, 1000000.0F, 10000000.0F, 100000000.0F, 1000000000.0F, 10000000000.0F };
    big10pow = { 10000000000000000.0D, 100000000000000010000000000000000.0D, 10000000000000000000000000000000000000000000000000000000000000000.0D, 100000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0D, 10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0D };
    tiny10pow = { 0.000000000000000099999999999999998D, 0.000000000000000000000000000000010000000000000001D, 0.0D, 0.0D, 0.0D };
    maxSmallTen = small10pow.length - 1;
    singleMaxSmallTen = singleSmall10pow.length - 1;
    small5pow = { 1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125 };
    long5pow = { 3412047961268420609L, 5L, 25L, 125L, 625L, 3125L, 15625L, 78125L, 390625L, 1953125L, 9765625L, 48828125L, 244140625L, 1220703125L, 6103515625L, 30517578125L, 152587890625L, 762939453125L, 3814697265625L, 19073486328125L, 95367431640625L, 476837158203125L, 2384185791015625L, 11920928955078125L, 59604644775390625L, 298023223876953125L, 1490116119384765625L };
    n5bits = { 0, 3, 5, 7, 10, 12, 14, 17, 19, 21, 24, 26, 28, 31, 33, 35, 38, 40, 42, 45, 47, 49, 52, 54, 56, 59, 61 };
    infinity = { 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y' };
    notANumber = { 'N', 'a', 'N' };
    zero = { '0', '0', '0', '0', '0', '0', '0', '0' };
    hexFloatPattern = Pattern.compile("([-+])?0[xX](((\\p{XDigit}+)\\.?)|((\\p{XDigit}*)\\.(\\p{XDigit}+)))[pP]([-+])?(\\p{Digit}+)[fFdD]?");
  }

  public static enum Form
  {
    SCIENTIFIC, COMPATIBLE, DECIMAL_FLOAT, GENERAL;
  }
}