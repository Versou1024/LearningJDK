package sun.org.mozilla.javascript.internal;

import java.math.BigInteger;

class DToA
{
  private static final int DTOBASESTR_BUFFER_SIZE = 1078;
  static final int DTOSTR_STANDARD = 0;
  static final int DTOSTR_STANDARD_EXPONENTIAL = 1;
  static final int DTOSTR_FIXED = 2;
  static final int DTOSTR_EXPONENTIAL = 3;
  static final int DTOSTR_PRECISION = 4;
  private static final int Frac_mask = 1048575;
  private static final int Exp_shift = 20;
  private static final int Exp_msk1 = 1048576;
  private static final long Frac_maskL = 4503599627370495L;
  private static final int Exp_shiftL = 52;
  private static final long Exp_msk1L = 4503599627370496L;
  private static final int Bias = 1023;
  private static final int P = 53;
  private static final int Exp_shift1 = 20;
  private static final int Exp_mask = 2146435072;
  private static final int Exp_mask_shifted = 2047;
  private static final int Bndry_mask = 1048575;
  private static final int Log2P = 1;
  private static final int Sign_bit = -2147483648;
  private static final int Exp_11 = 1072693248;
  private static final int Ten_pmax = 22;
  private static final int Quick_max = 14;
  private static final int Bletch = 16;
  private static final int Frac_mask1 = 1048575;
  private static final int Int_max = 14;
  private static final int n_bigtens = 5;
  private static final double[] tens = { 1D, 10.0D, 100.0D, 1000.0D, 10000.0D, 100000.0D, 1000000.0D, 10000000.0D, 100000000.0D, 1000000000.0D, 10000000000.0D, 100000000000.0D, 1000000000000.0D, 10000000000000.0D, 100000000000000.0D, 1000000000000000.0D, 10000000000000000.0D, 100000000000000000.0D, 1000000000000000000.0D, 10000000000000000000.0D, 100000000000000000000.0D, 1000000000000000000000.0D, 10000000000000000000000.0D };
  private static final double[] bigtens = { 10000000000000000.0D, 100000000000000010000000000000000.0D, 10000000000000000000000000000000000000000000000000000000000000000.0D, 100000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0D, 10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0D };
  private static final int[] dtoaModes = { 0, 0, 3, 2, 2 };

  private static char BASEDIGIT(int paramInt)
  {
    return (char)((paramInt >= 10) ? 87 + paramInt : 48 + paramInt);
  }

  private static int lo0bits(int paramInt)
  {
    int j = paramInt;
    if ((j & 0x7) != 0)
    {
      if ((j & 0x1) != 0)
        return 0;
      if ((j & 0x2) != 0)
        return 1;
      return 2;
    }
    int i = 0;
    if ((j & 0xFFFF) == 0)
    {
      i = 16;
      j >>>= 16;
    }
    if ((j & 0xFF) == 0)
    {
      i += 8;
      j >>>= 8;
    }
    if ((j & 0xF) == 0)
    {
      i += 4;
      j >>>= 4;
    }
    if ((j & 0x3) == 0)
    {
      i += 2;
      j >>>= 2;
    }
    if ((j & 0x1) == 0)
    {
      ++i;
      j >>>= 1;
      if ((j & 0x1) == 0)
        return 32;
    }
    return i;
  }

  private static int hi0bits(int paramInt)
  {
    int i = 0;
    if ((paramInt & 0xFFFF0000) == 0)
    {
      i = 16;
      paramInt <<= 16;
    }
    if ((paramInt & 0xFF000000) == 0)
    {
      i += 8;
      paramInt <<= 8;
    }
    if ((paramInt & 0xF0000000) == 0)
    {
      i += 4;
      paramInt <<= 4;
    }
    if ((paramInt & 0xC0000000) == 0)
    {
      i += 2;
      paramInt <<= 2;
    }
    if ((paramInt & 0x80000000) == 0)
    {
      ++i;
      if ((paramInt & 0x40000000) == 0)
        return 32;
    }
    return i;
  }

  private static void stuffBits(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    paramArrayOfByte[paramInt1] = (byte)(paramInt2 >> 24);
    paramArrayOfByte[(paramInt1 + 1)] = (byte)(paramInt2 >> 16);
    paramArrayOfByte[(paramInt1 + 2)] = (byte)(paramInt2 >> 8);
    paramArrayOfByte[(paramInt1 + 3)] = (byte)paramInt2;
  }

  private static BigInteger d2b(double paramDouble, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
  {
    byte[] arrayOfByte;
    int i;
    int j;
    int k;
    int i1;
    long l1 = Double.doubleToLongBits(paramDouble);
    int i2 = (int)(l1 >>> 32);
    int i3 = (int)l1;
    int l = i2 & 0xFFFFF;
    i2 &= 2147483647;
    if ((i1 = i2 >>> 20) != 0)
      l |= 1048576;
    if ((k = i3) != 0)
    {
      arrayOfByte = new byte[8];
      j = lo0bits(k);
      k >>>= j;
      if (j != 0)
      {
        stuffBits(arrayOfByte, 4, k | l << 32 - j);
        l >>= j;
      }
      else
      {
        stuffBits(arrayOfByte, 4, k);
      }
      stuffBits(arrayOfByte, 0, l);
      i = (l != 0) ? 2 : 1;
    }
    else
    {
      arrayOfByte = new byte[4];
      j = lo0bits(l);
      l >>>= j;
      stuffBits(arrayOfByte, 0, l);
      j += 32;
      i = 1;
    }
    if (i1 != 0)
    {
      paramArrayOfInt1[0] = (i1 - 1023 - 52 + j);
      paramArrayOfInt2[0] = (53 - j);
    }
    else
    {
      paramArrayOfInt1[0] = (i1 - 1023 - 52 + 1 + j);
      paramArrayOfInt2[0] = (32 * i - hi0bits(l));
    }
    return new BigInteger(arrayOfByte);
  }

  static String JS_dtobasestr(int paramInt, double paramDouble)
  {
    int i;
    String str;
    if ((2 > paramInt) || (paramInt > 36))
      throw new IllegalArgumentException("Bad base: " + paramInt);
    if (Double.isNaN(paramDouble))
      return "NaN";
    if (Double.isInfinite(paramDouble))
      return ((paramDouble > 0D) ? "Infinity" : "-Infinity");
    if (paramDouble == 0D)
      return "0";
    if (paramDouble >= 0D)
    {
      i = 0;
    }
    else
    {
      i = 1;
      paramDouble = -paramDouble;
    }
    double d1 = Math.floor(paramDouble);
    long l1 = ()d1;
    if (l1 == d1)
    {
      str = Long.toString((i != 0) ? -l1 : l1, paramInt);
    }
    else
    {
      long l3;
      long l2 = Double.doubleToLongBits(d1);
      int k = (int)(l2 >> 52) & 0x7FF;
      if (k == 0)
        l3 = (l2 & 0xFFFFFFFF) << 1;
      else
        l3 = l2 & 0xFFFFFFFF | 0x0;
      if (i != 0)
        l3 = -l3;
      k -= 1075;
      BigInteger localBigInteger1 = BigInteger.valueOf(l3);
      if (k > 0)
        localBigInteger1 = localBigInteger1.shiftLeft(k);
      else if (k < 0)
        localBigInteger1 = localBigInteger1.shiftRight(-k);
      str = localBigInteger1.toString(paramInt);
    }
    if (paramDouble == d1)
      return str;
    char[] arrayOfChar = new char[1078];
    int j = 0;
    double d2 = paramDouble - d1;
    long l4 = Double.doubleToLongBits(paramDouble);
    int i1 = (int)(l4 >> 32);
    int i2 = (int)l4;
    int[] arrayOfInt1 = new int[1];
    int[] arrayOfInt2 = new int[1];
    BigInteger localBigInteger2 = d2b(d2, arrayOfInt1, arrayOfInt2);
    int i3 = -(i1 >>> 20 & 0x7FF);
    if (i3 == 0)
      i3 = -1;
    i3 += 1076;
    BigInteger localBigInteger3 = BigInteger.valueOf(3412048081527504897L);
    BigInteger localBigInteger4 = localBigInteger3;
    if ((i2 == 0) && ((i1 & 0xFFFFF) == 0) && ((i1 & 0x7FE00000) != 0))
    {
      ++i3;
      localBigInteger4 = BigInteger.valueOf(2L);
    }
    localBigInteger2 = localBigInteger2.shiftLeft(arrayOfInt1[0] + i3);
    BigInteger localBigInteger5 = BigInteger.valueOf(3412048081527504897L);
    localBigInteger5 = localBigInteger5.shiftLeft(i3);
    BigInteger localBigInteger6 = BigInteger.valueOf(paramInt);
    int i4 = 0;
    do
    {
      localBigInteger2 = localBigInteger2.multiply(localBigInteger6);
      localObject = localBigInteger2.divideAndRemainder(localBigInteger5);
      localBigInteger2 = localObject[1];
      int l = (char)localObject[0].intValue();
      if (localBigInteger3 == localBigInteger4)
      {
        localBigInteger3 = localBigInteger4 = localBigInteger3.multiply(localBigInteger6);
      }
      else
      {
        localBigInteger3 = localBigInteger3.multiply(localBigInteger6);
        localBigInteger4 = localBigInteger4.multiply(localBigInteger6);
      }
      int i5 = localBigInteger2.compareTo(localBigInteger3);
      BigInteger localBigInteger7 = localBigInteger5.subtract(localBigInteger4);
      int i6 = (localBigInteger7.signum() <= 0) ? 1 : localBigInteger2.compareTo(localBigInteger7);
      if ((i6 == 0) && ((i2 & 0x1) == 0))
      {
        if (i5 > 0)
          ++l;
        i4 = 1;
      }
      else if ((i5 < 0) || ((i5 == 0) && ((i2 & 0x1) == 0)))
      {
        if (i6 > 0)
        {
          localBigInteger2 = localBigInteger2.shiftLeft(1);
          i6 = localBigInteger2.compareTo(localBigInteger5);
          if (i6 > 0)
            ++l;
        }
        i4 = 1;
      }
      else if (i6 > 0)
      {
        ++l;
        i4 = 1;
      }
      arrayOfChar[(j++)] = BASEDIGIT(l);
    }
    while (i4 == 0);
    Object localObject = new StringBuffer(str.length() + 1 + j);
    ((StringBuffer)localObject).append(str);
    ((StringBuffer)localObject).append('.');
    ((StringBuffer)localObject).append(arrayOfChar, 0, j);
    return ((String)((StringBuffer)localObject).toString());
  }

  static int word0(double paramDouble)
  {
    long l = Double.doubleToLongBits(paramDouble);
    return (int)(l >> 32);
  }

  static double setWord0(double paramDouble, int paramInt)
  {
    long l = Double.doubleToLongBits(paramDouble);
    l = paramInt << 32 | l & 0xFFFFFFFF;
    return Double.longBitsToDouble(l);
  }

  static int word1(double paramDouble)
  {
    long l = Double.doubleToLongBits(paramDouble);
    return (int)l;
  }

  static BigInteger pow5mult(BigInteger paramBigInteger, int paramInt)
  {
    return paramBigInteger.multiply(BigInteger.valueOf(5L).pow(paramInt));
  }

  static boolean roundOff(StringBuffer paramStringBuffer)
  {
    int i = paramStringBuffer.length();
    while (i != 0)
    {
      int j = paramStringBuffer.charAt(--i);
      if (j != 57)
      {
        paramStringBuffer.setCharAt(i, (char)(j + 1));
        paramStringBuffer.setLength(i + 1);
        return false;
      }
    }
    paramStringBuffer.setLength(0);
    return true;
  }

  static int JS_dtoa(double paramDouble, int paramInt1, boolean paramBoolean, int paramInt2, boolean[] paramArrayOfBoolean, StringBuffer paramStringBuffer)
  {
    int i;
    int j;
    int i3;
    int i5;
    int i10;
    int i11;
    char c;
    long l1;
    Object localObject2;
    double d1;
    int i13;
    int i18;
    BigInteger[] arrayOfBigInteger;
    int[] arrayOfInt1 = new int[1];
    int[] arrayOfInt2 = new int[1];
    if ((word0(paramDouble) & 0x80000000) != 0)
    {
      paramArrayOfBoolean[0] = true;
      paramDouble = setWord0(paramDouble, word0(paramDouble) & 0x7FFFFFFF);
    }
    else
    {
      paramArrayOfBoolean[0] = false;
    }
    if ((word0(paramDouble) & 0x7FF00000) == 2146435072)
    {
      paramStringBuffer.append(((word1(paramDouble) == 0) && ((word0(paramDouble) & 0xFFFFF) == 0)) ? "Infinity" : "NaN");
      return 9999;
    }
    if (paramDouble == 0D)
    {
      paramStringBuffer.setLength(0);
      paramStringBuffer.append('0');
      return 1;
    }
    Object localObject1 = d2b(paramDouble, arrayOfInt1, arrayOfInt2);
    if ((k = word0(paramDouble) >>> 20 & 0x7FF) != 0)
    {
      d1 = setWord0(paramDouble, word0(paramDouble) & 0xFFFFF | 0x3FF00000);
      k -= 1023;
      i13 = 0;
    }
    else
    {
      k = arrayOfInt2[0] + arrayOfInt1[0] + 1074;
      long l2 = word1(paramDouble) << 32 - k;
      d1 = setWord0(l2, word0(l2) - 32505856);
      k -= 1075;
      i13 = 1;
    }
    double d2 = (d1 - 1.5D) * 0.28952965460216801D + 0.1760912590558D + k * 0.30102999566398098D;
    int i6 = (int)d2;
    if ((d2 < 0D) && (d2 != i6))
      --i6;
    int i14 = 1;
    if ((i6 >= 0) && (i6 <= 22))
    {
      if (paramDouble < tens[i6])
        --i6;
      i14 = 0;
    }
    int i4 = arrayOfInt2[0] - k - 1;
    if (i4 >= 0)
    {
      i = 0;
      i10 = i4;
    }
    else
    {
      i = -i4;
      i10 = 0;
    }
    if (i6 >= 0)
    {
      j = 0;
      i11 = i6;
      i10 += i6;
    }
    else
    {
      i -= i6;
      j = -i6;
      i11 = 0;
    }
    if ((paramInt1 < 0) || (paramInt1 > 9))
      paramInt1 = 0;
    int i15 = 1;
    if (paramInt1 > 5)
    {
      paramInt1 -= 4;
      i15 = 0;
    }
    int i16 = 1;
    int i1 = i3 = 0;
    switch (paramInt1)
    {
    case 0:
    case 1:
      i1 = i3 = -1;
      k = 18;
      paramInt2 = 0;
      break;
    case 2:
      i16 = 0;
    case 4:
      if (paramInt2 <= 0)
        paramInt2 = 1;
      i1 = i3 = k = paramInt2;
      break;
    case 3:
      i16 = 0;
    case 5:
      k = paramInt2 + i6 + 1;
      i1 = k;
      i3 = k - 1;
      if (k <= 0)
        k = 1;
    }
    int i17 = 0;
    if ((i1 >= 0) && (i1 <= 14) && (i15 != 0))
    {
      k = 0;
      d1 = paramDouble;
      int i7 = i6;
      int i2 = i1;
      int l = 2;
      if (i6 > 0)
      {
        d2 = tens[(i6 & 0xF)];
        i4 = i6 >> 4;
        if ((i4 & 0x10) != 0)
        {
          i4 &= 15;
          paramDouble /= bigtens[4];
          ++l;
        }
        while (i4 != 0)
        {
          if ((i4 & 0x1) != 0)
          {
            ++l;
            d2 *= bigtens[k];
          }
          i4 >>= 1;
          ++k;
        }
        paramDouble /= d2;
      }
      else if ((i5 = -i6) != 0)
      {
        paramDouble *= tens[(i5 & 0xF)];
        i4 = i5 >> 4;
        while (i4 != 0)
        {
          if ((i4 & 0x1) != 0)
          {
            ++l;
            paramDouble *= bigtens[k];
          }
          i4 >>= 1;
          ++k;
        }
      }
      if ((i14 != 0) && (paramDouble < 1D) && (i1 > 0))
        if (i3 <= 0)
        {
          i17 = 1;
        }
        else
        {
          i1 = i3;
          --i6;
          paramDouble *= 10.0D;
          ++l;
        }
      double d3 = l * paramDouble + 7.0D;
      d3 = setWord0(d3, word0(d3) - 54525952);
      if (i1 == 0)
      {
        localBigInteger3 = localObject3 = null;
        paramDouble -= 5.0D;
        if (paramDouble > d3)
        {
          paramStringBuffer.append('1');
          return (++i6 + 1);
        }
        if (paramDouble < -d3)
        {
          paramStringBuffer.setLength(0);
          paramStringBuffer.append('0');
          return 1;
        }
        i17 = 1;
      }
      if (i17 == 0)
      {
        i17 = 1;
        if (i16 != 0)
        {
          d3 = 0.5D / tens[(i1 - 1)] - d3;
          k = 0;
          while (true)
          {
            l1 = ()paramDouble;
            paramDouble -= l1;
            paramStringBuffer.append((char)(int)(48L + l1));
            if (paramDouble < d3)
              return (i6 + 1);
            if (1D - paramDouble < d3)
            {
              do
              {
                i18 = paramStringBuffer.charAt(paramStringBuffer.length() - 1);
                paramStringBuffer.setLength(paramStringBuffer.length() - 1);
                if (i18 != 57)
                  break label1012:
              }
              while (paramStringBuffer.length() != 0);
              ++i6;
              i18 = 48;
              label1012: paramStringBuffer.append((char)(i18 + 1));
              return (i6 + 1);
            }
            if (++k >= i1)
              break label1246:
            d3 *= 10.0D;
            paramDouble *= 10.0D;
          }
        }
        d3 *= tens[(i1 - 1)];
        k = 1;
        while (true)
        {
          l1 = ()paramDouble;
          paramDouble -= l1;
          paramStringBuffer.append((char)(int)(48L + l1));
          if (k == i1)
          {
            if (paramDouble > 0.5D + d3)
            {
              do
              {
                i18 = paramStringBuffer.charAt(paramStringBuffer.length() - 1);
                paramStringBuffer.setLength(paramStringBuffer.length() - 1);
                if (i18 != 57)
                  break label1170:
              }
              while (paramStringBuffer.length() != 0);
              ++i6;
              i18 = 48;
              label1170: paramStringBuffer.append((char)(i18 + 1));
              return (i6 + 1);
            }
            if (paramDouble >= 0.5D - d3)
              break;
            while (paramStringBuffer.charAt(paramStringBuffer.length() - 1) == '0')
              paramStringBuffer.setLength(paramStringBuffer.length() - 1);
            return (i6 + 1);
          }
          ++k;
          paramDouble *= 10.0D;
        }
      }
      if (i17 != 0)
      {
        label1246: paramStringBuffer.setLength(0);
        paramDouble = d1;
        i6 = i7;
        i1 = i2;
      }
    }
    if ((arrayOfInt1[0] >= 0) && (i6 <= 14))
    {
      d2 = tens[i6];
      if ((paramInt2 < 0) && (i1 <= 0))
      {
        localBigInteger3 = localObject3 = null;
        if ((i1 < 0) || (paramDouble < 5.0D * d2) || ((!(paramBoolean)) && (paramDouble == 5.0D * d2)))
        {
          paramStringBuffer.setLength(0);
          paramStringBuffer.append('0');
          return 1;
        }
        paramStringBuffer.append('1');
        return (++i6 + 1);
      }
      k = 1;
      while (true)
      {
        l1 = ()(paramDouble / d2);
        paramDouble -= l1 * d2;
        paramStringBuffer.append((char)(int)(48L + l1));
        if (k == i1)
        {
          paramDouble += paramDouble;
          if ((paramDouble <= d2) && (((paramDouble != d2) || (((l1 & 3412040831622709249L) == 3412039972629250048L) && (!(paramBoolean))))))
            break;
          do
          {
            i18 = paramStringBuffer.charAt(paramStringBuffer.length() - 1);
            paramStringBuffer.setLength(paramStringBuffer.length() - 1);
            if (i18 != 57)
              break label1494:
          }
          while (paramStringBuffer.length() != 0);
          ++i6;
          i18 = 48;
          label1494: paramStringBuffer.append((char)(i18 + 1));
          break;
        }
        paramDouble *= 10.0D;
        if (paramDouble == 0D)
          break;
        ++k;
      }
      return (i6 + 1);
    }
    int i8 = i;
    int i9 = j;
    Object localObject3 = localObject2 = null;
    if (i16 != 0)
    {
      if (paramInt1 < 2)
      {
        k = (i13 != 0) ? arrayOfInt1[0] + 1075 : 54 - arrayOfInt2[0];
      }
      else
      {
        i4 = i1 - 1;
        if (i9 >= i4)
        {
          i9 -= i4;
        }
        else
        {
          i11 += i4 -= i9;
          j += i4;
          i9 = 0;
        }
        if ((k = i1) < 0)
        {
          i8 -= k;
          k = 0;
        }
      }
      i += k;
      i10 += k;
      localObject3 = BigInteger.valueOf(3412048459484626945L);
    }
    if ((i8 > 0) && (i10 > 0))
    {
      k = (i8 < i10) ? i8 : i10;
      i -= k;
      i8 -= k;
      i10 -= k;
    }
    if (j > 0)
      if (i16 != 0)
      {
        if (i9 > 0)
        {
          localObject3 = pow5mult((BigInteger)localObject3, i9);
          BigInteger localBigInteger1 = ((BigInteger)localObject3).multiply((BigInteger)localObject1);
          localObject1 = localBigInteger1;
        }
        if ((i4 = j - i9) != 0)
          localObject1 = pow5mult((BigInteger)localObject1, i4);
      }
      else
      {
        localObject1 = pow5mult((BigInteger)localObject1, j);
      }
    BigInteger localBigInteger3 = BigInteger.valueOf(3412048081527504897L);
    if (i11 > 0)
      localBigInteger3 = pow5mult(localBigInteger3, i11);
    int i12 = 0;
    if ((paramInt1 < 2) && (word1(paramDouble) == 0) && ((word0(paramDouble) & 0xFFFFF) == 0) && ((word0(paramDouble) & 0x7FE00000) != 0))
    {
      ++i;
      ++i10;
      i12 = 1;
    }
    byte[] arrayOfByte = localBigInteger3.toByteArray();
    int i19 = 0;
    for (int i20 = 0; i20 < 4; ++i20)
    {
      i19 <<= 8;
      if (i20 < arrayOfByte.length)
        i19 |= arrayOfByte[i20] & 0xFF;
    }
    if ((k = ((i11 != 0) ? 32 - hi0bits(i19) : 1) + i10 & 0x1F) != 0)
      k = 32 - k;
    if (k > 4)
    {
      i += (k -= 4);
      i8 += k;
      i10 += k;
    }
    else if (k < 4)
    {
      i += (k += 28);
      i8 += k;
      i10 += k;
    }
    if (i > 0)
      localObject1 = ((BigInteger)localObject1).shiftLeft(i);
    if (i10 > 0)
      localBigInteger3 = localBigInteger3.shiftLeft(i10);
    if ((i14 != 0) && (((BigInteger)localObject1).compareTo(localBigInteger3) < 0))
    {
      --i6;
      localObject1 = ((BigInteger)localObject1).multiply(BigInteger.valueOf(10L));
      if (i16 != 0)
        localObject3 = ((BigInteger)localObject3).multiply(BigInteger.valueOf(10L));
      i1 = i3;
    }
    if ((i1 <= 0) && (paramInt1 > 2))
    {
      if (i1 >= 0)
        if (((k = ((BigInteger)localObject1).compareTo(localBigInteger3 = localBigInteger3.multiply(BigInteger.valueOf(5L)))) >= 0) && (((k != 0) || (paramBoolean))))
          break label2149;
      paramStringBuffer.setLength(0);
      paramStringBuffer.append('0');
      return 1;
      label2149: paramStringBuffer.append('1');
      return (++i6 + 1);
    }
    if (i16 != 0)
    {
      if (i8 > 0)
        localObject3 = ((BigInteger)localObject3).shiftLeft(i8);
      localObject2 = localObject3;
      if (i12 != 0)
      {
        localObject3 = localObject2;
        localObject3 = ((BigInteger)localObject3).shiftLeft(1);
      }
      k = 1;
      while (true)
      {
        arrayOfBigInteger = ((BigInteger)localObject1).divideAndRemainder(localBigInteger3);
        localObject1 = arrayOfBigInteger[1];
        c = (char)(arrayOfBigInteger[0].intValue() + 48);
        i4 = ((BigInteger)localObject1).compareTo((BigInteger)localObject2);
        BigInteger localBigInteger2 = localBigInteger3.subtract((BigInteger)localObject3);
        i5 = (localBigInteger2.signum() <= 0) ? 1 : ((BigInteger)localObject1).compareTo(localBigInteger2);
        if ((i5 == 0) && (paramInt1 == 0) && ((word1(paramDouble) & 0x1) == 0))
        {
          if (c == '9')
          {
            paramStringBuffer.append('9');
            if (roundOff(paramStringBuffer))
            {
              ++i6;
              paramStringBuffer.append('1');
            }
            return (i6 + 1);
          }
          if (i4 > 0)
            c = (char)(c + '\1');
          paramStringBuffer.append(c);
          return (i6 + 1);
        }
        if ((i4 < 0) || ((i4 == 0) && (paramInt1 == 0) && ((word1(paramDouble) & 0x1) == 0)))
        {
          if (i5 > 0)
          {
            localObject1 = ((BigInteger)localObject1).shiftLeft(1);
            i5 = ((BigInteger)localObject1).compareTo(localBigInteger3);
            if ((i5 > 0) || ((i5 == 0) && ((((c & 0x1) == '\1') || (paramBoolean)))))
            {
              c = (char)(c + '\1');
              if (c == '9')
              {
                paramStringBuffer.append('9');
                if (roundOff(paramStringBuffer))
                {
                  ++i6;
                  paramStringBuffer.append('1');
                }
                return (i6 + 1);
              }
            }
          }
          paramStringBuffer.append(c);
          return (i6 + 1);
        }
        if (i5 > 0)
        {
          if (c == '9')
          {
            paramStringBuffer.append('9');
            if (roundOff(paramStringBuffer))
            {
              ++i6;
              paramStringBuffer.append('1');
            }
            return (i6 + 1);
          }
          paramStringBuffer.append((char)(c + '\1'));
          return (i6 + 1);
        }
        paramStringBuffer.append(c);
        if (k == i1)
          break label2700:
        localObject1 = ((BigInteger)localObject1).multiply(BigInteger.valueOf(10L));
        if (localObject2 == localObject3)
        {
          localObject2 = localObject3 = ((BigInteger)localObject3).multiply(BigInteger.valueOf(10L));
        }
        else
        {
          localObject2 = ((BigInteger)localObject2).multiply(BigInteger.valueOf(10L));
          localObject3 = ((BigInteger)localObject3).multiply(BigInteger.valueOf(10L));
        }
        ++k;
      }
    }
    int k = 1;
    while (true)
    {
      arrayOfBigInteger = ((BigInteger)localObject1).divideAndRemainder(localBigInteger3);
      localObject1 = arrayOfBigInteger[1];
      c = (char)(arrayOfBigInteger[0].intValue() + 48);
      paramStringBuffer.append(c);
      if (k >= i1)
        break;
      localObject1 = ((BigInteger)localObject1).multiply(BigInteger.valueOf(10L));
      ++k;
    }
    label2700: localObject1 = ((BigInteger)localObject1).shiftLeft(1);
    i4 = ((BigInteger)localObject1).compareTo(localBigInteger3);
    if ((i4 > 0) || ((i4 == 0) && ((((c & 0x1) == '\1') || (paramBoolean)))))
    {
      if (!(roundOff(paramStringBuffer)))
        break label2795;
      ++i6;
      paramStringBuffer.append('1');
      return (i6 + 1);
    }
    while (paramStringBuffer.charAt(paramStringBuffer.length() - 1) == '0')
      paramStringBuffer.setLength(paramStringBuffer.length() - 1);
    label2795: return (i6 + 1);
  }

  static void JS_dtostr(StringBuffer paramStringBuffer, int paramInt1, int paramInt2, double paramDouble)
  {
    boolean[] arrayOfBoolean = new boolean[1];
    if ((paramInt1 == 2) && (((paramDouble >= 1000000000000000000000.0D) || (paramDouble <= -1000000000000000000000.0D))))
      paramInt1 = 0;
    int i = JS_dtoa(paramDouble, dtoaModes[paramInt1], (paramInt1 >= 2) ? 1 : false, paramInt2, arrayOfBoolean, paramStringBuffer);
    int j = paramStringBuffer.length();
    if (i != 9999)
    {
      int k = 0;
      int l = 0;
      switch (paramInt1)
      {
      case 0:
        if ((i < -5) || (i > 21))
          k = 1;
        else
          l = i;
        break;
      case 2:
        if (paramInt2 >= 0)
          l = i + paramInt2;
        else
          l = i;
        break;
      case 3:
        l = paramInt2;
      case 1:
        k = 1;
        break;
      case 4:
        l = paramInt2;
        if ((i < -5) || (i > paramInt2))
          k = 1;
      }
      if (j < l)
      {
        int i1 = l;
        j = l;
        do
          paramStringBuffer.append('0');
        while (paramStringBuffer.length() != i1);
      }
      if (k != 0)
      {
        if (j != 1)
          paramStringBuffer.insert(1, '.');
        paramStringBuffer.append('e');
        if (i - 1 >= 0)
          paramStringBuffer.append('+');
        paramStringBuffer.append(i - 1);
      }
      else if (i != j)
      {
        if (i > 0)
        {
          paramStringBuffer.insert(i, '.');
        }
        else
        {
          for (int i2 = 0; i2 < 1 - i; ++i2)
            paramStringBuffer.insert(0, '0');
          paramStringBuffer.insert(1, '.');
        }
      }
    }
    if ((arrayOfBoolean[0] != 0) && (((word0(paramDouble) != -2147483648) || (word1(paramDouble) != 0))) && ((((word0(paramDouble) & 0x7FF00000) != 2146435072) || ((word1(paramDouble) == 0) && ((word0(paramDouble) & 0xFFFFF) == 0)))))
      paramStringBuffer.insert(0, '-');
  }
}