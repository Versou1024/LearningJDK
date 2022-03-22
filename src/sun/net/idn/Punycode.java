package sun.net.idn;

import java.text.ParseException;
import sun.text.normalizer.UCharacter;
import sun.text.normalizer.UTF16;

public final class Punycode
{
  private static final int BASE = 36;
  private static final int TMIN = 1;
  private static final int TMAX = 26;
  private static final int SKEW = 38;
  private static final int DAMP = 700;
  private static final int INITIAL_BIAS = 72;
  private static final int INITIAL_N = 128;
  private static final int HYPHEN = 45;
  private static final int DELIMITER = 45;
  private static final int ZERO = 48;
  private static final int NINE = 57;
  private static final int SMALL_A = 97;
  private static final int SMALL_Z = 122;
  private static final int CAPITAL_A = 65;
  private static final int CAPITAL_Z = 90;
  private static final int MAX_CP_COUNT = 256;
  private static final int UINT_MAGIC = -2147483648;
  private static final long ULONG_MAGIC = -9223372036854775808L;
  static final int[] basicToDigit = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

  private static int adaptBias(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    if (paramBoolean)
      paramInt1 /= 700;
    else
      paramInt1 /= 2;
    paramInt1 += paramInt1 / paramInt2;
    for (int i = 0; paramInt1 > 455; i += 36)
      paramInt1 /= 35;
    return (i + 36 * paramInt1 / (paramInt1 + 38));
  }

  private static char asciiCaseMap(char paramChar, boolean paramBoolean)
  {
    if (paramBoolean)
      if (('a' <= paramChar) && (paramChar <= 'z'))
        paramChar = (char)(paramChar - ' ');
    else if (('A' <= paramChar) && (paramChar <= 'Z'))
      paramChar = (char)(paramChar + ' ');
    return paramChar;
  }

  private static char digitToBasic(int paramInt, boolean paramBoolean)
  {
    if (paramInt < 26)
    {
      if (paramBoolean)
        return (char)(65 + paramInt);
      return (char)(97 + paramInt);
    }
    return (char)(22 + paramInt);
  }

  public static StringBuffer encode(StringBuffer paramStringBuffer, boolean[] paramArrayOfBoolean)
    throws ParseException
  {
    int i2;
    int[] arrayOfInt = new int[256];
    int i11 = paramStringBuffer.length();
    int i12 = 256;
    char[] arrayOfChar = new char[i12];
    StringBuffer localStringBuffer = new StringBuffer();
    int i9 = i2 = 0;
    for (int i4 = 0; i4 < i11; ++i4)
    {
      if (i9 == 256)
        throw new IndexOutOfBoundsException();
      int i10 = paramStringBuffer.charAt(i4);
      if (isBasic(i10))
      {
        if (i2 < i12)
        {
          arrayOfInt[(i9++)] = 0;
          arrayOfChar[i2] = ((paramArrayOfBoolean != null) ? asciiCaseMap(i10, paramArrayOfBoolean[i4]) : i10);
        }
        ++i2;
      }
      else
      {
        int i = (((paramArrayOfBoolean != null) && (paramArrayOfBoolean[i4] != 0)) ? 1 : 0) << 31;
        if (!(UTF16.isSurrogate(i10)))
        {
          i |= i10;
        }
        else if ((UTF16.isLeadSurrogate(i10)) && (i4 + 1 < i11))
        {
          char c;
          if (UTF16.isTrailSurrogate(c = paramStringBuffer.charAt(i4 + 1)))
          {
            ++i4;
            i |= UCharacter.getCodePoint(i10, c);
          }
        }
        else
        {
          throw new ParseException("Illegal char found", -1);
        }
        arrayOfInt[(i9++)] = j;
      }
    }
    int i1 = i2;
    if (i1 > 0)
    {
      if (i2 < i12)
        arrayOfChar[i2] = '-';
      ++i2;
    }
    int j = 128;
    int k = 0;
    int i3 = 72;
    int l = i1;
    while (l < i9)
    {
      int i6;
      int i5 = 2147483647;
      for (i4 = 0; i4 < i9; ++i4)
      {
        i6 = arrayOfInt[i4] & 0x7FFFFFFF;
        if ((j <= i6) && (i6 < i5))
          i5 = i6;
      }
      if (i5 - j > (2147483391 - k) / (l + 1))
        throw new RuntimeException("Internal program error");
      k += (i5 - j) * (l + 1);
      j = i5;
      for (i4 = 0; i4 < i9; ++i4)
      {
        i6 = arrayOfInt[i4] & 0x7FFFFFFF;
        if (i6 < j)
        {
          ++k;
        }
        else if (i6 == j)
        {
          i6 = k;
          int i7 = 36;
          while (true)
          {
            int i8 = i7 - i3;
            if (i8 < 1)
              i8 = 1;
            else if (i7 >= i3 + 26)
              i8 = 26;
            if (i6 < i8)
              break;
            if (i2 < i12)
              arrayOfChar[(i2++)] = digitToBasic(i8 + (i6 - i8) % (36 - i8), false);
            i6 = (i6 - i8) / (36 - i8);
            i7 += 36;
          }
          if (i2 < i12)
            arrayOfChar[(i2++)] = digitToBasic(i6, arrayOfInt[i4] < 0);
          i3 = adaptBias(k, l + 1, l == i1);
          k = 0;
          ++l;
        }
      }
      ++k;
      ++j;
    }
    return localStringBuffer.append(arrayOfChar, 0, i2);
  }

  private static boolean isBasic(int paramInt)
  {
    return (paramInt < 128);
  }

  private static boolean isBasicUpperCase(int paramInt)
  {
    return ((65 <= paramInt) && (paramInt <= 90));
  }

  private static boolean isSurrogate(int paramInt)
  {
    return ((paramInt & 0xFFFFF800) == 55296);
  }

  public static StringBuffer decode(StringBuffer paramStringBuffer, boolean[] paramArrayOfBoolean)
    throws ParseException
  {
    int i2;
    int i10;
    int i = paramStringBuffer.length();
    StringBuffer localStringBuffer = new StringBuffer();
    int i14 = 256;
    char[] arrayOfChar = new char[i14];
    int i3 = i;
    do
      if (i3 <= 0)
        break;
    while (paramStringBuffer.charAt(--i3) != '-');
    int k = i2 = i10 = i3;
    while (true)
    {
      int i13;
      do
      {
        do
        {
          if (i3 <= 0)
            break label124;
          i13 = paramStringBuffer.charAt(--i3);
          if (!(isBasic(i13)))
            throw new ParseException("Illegal char found", -1);
        }
        while (i3 >= i14);
        arrayOfChar[i3] = i13;
      }
      while (paramArrayOfBoolean == null);
      paramArrayOfBoolean[i3] = isBasicUpperCase(i13);
    }
    label124: int j = 128;
    int l = 0;
    int i1 = 72;
    int i11 = 1000000000;
    int i4 = (i2 > 0) ? i2 + 1 : 0;
    while (i4 < i)
    {
      int i5 = l;
      int i6 = 1;
      int i7 = 36;
      while (true)
      {
        if (i4 >= i)
          throw new ParseException("Illegal char found", -1);
        int i8 = basicToDigit[(byte)paramStringBuffer.charAt(i4++)];
        if (i8 < 0)
          throw new ParseException("Invalid char found", -1);
        if (i8 > (2147483647 - l) / i6)
          throw new ParseException("Illegal char found", -1);
        l += i8 * i6;
        int i9 = i7 - i1;
        if (i9 < 1)
          i9 = 1;
        else if (i7 >= i1 + 26)
          i9 = 26;
        if (i8 < i9)
          break;
        if (i6 > 2147483647 / (36 - i9))
          throw new ParseException("Illegal char found", -1);
        i6 *= (36 - i9);
        i7 += 36;
      }
      i1 = adaptBias(l - i5, ++i10, i5 == 0);
      if (l / i10 > 2147483647 - j)
        throw new ParseException("Illegal char found", -1);
      j += l / i10;
      l %= i10;
      if ((j > 1114111) || (isSurrogate(j)))
        throw new ParseException("Illegal char found", -1);
      int i12 = UTF16.getCharCount(j);
      if (k + i12 < i14)
      {
        int i15;
        if (l <= i11)
        {
          i15 = l;
          if (i12 > 1)
            i11 = i15;
          else
            ++i11;
        }
        else
        {
          i15 = i11;
          i15 = UTF16.moveCodePointOffset(arrayOfChar, 0, k, i15, l - i15);
        }
        if (i15 < k)
        {
          System.arraycopy(arrayOfChar, i15, arrayOfChar, i15 + i12, k - i15);
          if (paramArrayOfBoolean != null)
            System.arraycopy(paramArrayOfBoolean, i15, paramArrayOfBoolean, i15 + i12, k - i15);
        }
        if (i12 == 1)
        {
          arrayOfChar[i15] = (char)j;
        }
        else
        {
          arrayOfChar[i15] = UTF16.getLeadSurrogate(j);
          arrayOfChar[(i15 + 1)] = UTF16.getTrailSurrogate(j);
        }
        if (paramArrayOfBoolean != null)
        {
          paramArrayOfBoolean[i15] = isBasicUpperCase(paramStringBuffer.charAt(i4 - 1));
          if (i12 == 2)
            paramArrayOfBoolean[(i15 + 1)] = false;
        }
      }
      k += i12;
      ++l;
    }
    localStringBuffer.append(arrayOfChar, 0, k);
    return localStringBuffer;
  }
}