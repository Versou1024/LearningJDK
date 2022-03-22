package sun.text.normalizer;

public final class Utility
{
  private static final char[] UNESCAPE_MAP = { 'a', '\7', 'b', '\b', 'e', '\27', 'f', '\f', 'n', '\n', 'r', '\r', 't', '\t', 'v', '\11' };
  static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

  public static final String escape(String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 0;
    while (i < paramString.length())
    {
      int j = UTF16.charAt(paramString, i);
      i += UTF16.getCharCount(j);
      if ((j >= 32) && (j <= 127))
      {
        if (j == 92)
          localStringBuffer.append("\\\\");
        else
          localStringBuffer.append((char)j);
      }
      else
      {
        int k = (j <= 65535) ? 1 : 0;
        localStringBuffer.append((k != 0) ? "\\u" : "\\U");
        hex(j, (k != 0) ? 4 : 8, localStringBuffer);
      }
    }
    return localStringBuffer.toString();
  }

  public static int unescapeAt(String paramString, int[] paramArrayOfInt)
  {
    int i3;
    int j = 0;
    int k = 0;
    int l = 0;
    int i1 = 0;
    int i2 = 4;
    int i5 = 0;
    int i6 = paramArrayOfInt[0];
    int i7 = paramString.length();
    if ((i6 < 0) || (i6 >= i7))
      return -1;
    int i = UTF16.charAt(paramString, i6);
    i6 += UTF16.getCharCount(i);
    switch (i)
    {
    case 117:
      l = i1 = 4;
      break;
    case 85:
      l = i1 = 8;
      break;
    case 120:
      l = 1;
      if ((i6 < i7) && (UTF16.charAt(paramString, i6) == 123))
      {
        ++i6;
        i5 = 1;
        i1 = 8;
      }
      else
      {
        i1 = 2;
      }
      break;
    default:
      i3 = UCharacter.digit(i, 8);
      if (i3 >= 0)
      {
        l = 1;
        i1 = 3;
        k = 1;
        i2 = 3;
        j = i3;
      }
    }
    if (l != 0)
    {
      while ((i6 < i7) && (k < i1))
      {
        i = UTF16.charAt(paramString, i6);
        i3 = UCharacter.digit(i, (i2 == 3) ? 8 : 16);
        if (i3 < 0)
          break;
        j = j << i2 | i3;
        i6 += UTF16.getCharCount(i);
        ++k;
      }
      if (k < l)
        return -1;
      if (i5 != 0)
      {
        if (i != 125)
          return -1;
        ++i6;
      }
      if ((j < 0) || (j >= 1114112))
        return -1;
      if ((i6 < i7) && (UTF16.isLeadSurrogate((char)j)))
      {
        int i8 = i6 + 1;
        i = paramString.charAt(i6);
        if ((i == 92) && (i8 < i7))
        {
          int[] arrayOfInt = { i8 };
          i = unescapeAt(paramString, arrayOfInt);
          i8 = arrayOfInt[0];
        }
        if (UTF16.isTrailSurrogate((char)i))
        {
          i6 = i8;
          j = UCharacterProperty.getRawSupplementary((char)j, (char)i);
        }
      }
      paramArrayOfInt[0] = i6;
      return j;
    }
    for (int i4 = 0; i4 < UNESCAPE_MAP.length; i4 += 2)
    {
      if (i == UNESCAPE_MAP[i4])
      {
        paramArrayOfInt[0] = i6;
        return UNESCAPE_MAP[(i4 + 1)];
      }
      if (i < UNESCAPE_MAP[i4])
        break;
    }
    if ((i == 99) && (i6 < i7))
    {
      i = UTF16.charAt(paramString, i6);
      paramArrayOfInt[0] = (i6 + UTF16.getCharCount(i));
      return (0x1F & i);
    }
    paramArrayOfInt[0] = i6;
    return i;
  }

  public static StringBuffer hex(int paramInt1, int paramInt2, StringBuffer paramStringBuffer)
  {
    return appendNumber(paramStringBuffer, paramInt1, 16, paramInt2);
  }

  public static String hex(int paramInt1, int paramInt2)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    return appendNumber(localStringBuffer, paramInt1, 16, paramInt2).toString();
  }

  public static int skipWhitespace(String paramString, int paramInt)
  {
    while (paramInt < paramString.length())
    {
      int i = UTF16.charAt(paramString, paramInt);
      if (!(UCharacterProperty.isRuleWhiteSpace(i)))
        break;
      paramInt += UTF16.getCharCount(i);
    }
    return paramInt;
  }

  private static void recursiveAppendNumber(StringBuffer paramStringBuffer, int paramInt1, int paramInt2, int paramInt3)
  {
    int i = paramInt1 % paramInt2;
    if ((paramInt1 >= paramInt2) || (paramInt3 > 1))
      recursiveAppendNumber(paramStringBuffer, paramInt1 / paramInt2, paramInt2, paramInt3 - 1);
    paramStringBuffer.append(DIGITS[i]);
  }

  public static StringBuffer appendNumber(StringBuffer paramStringBuffer, int paramInt1, int paramInt2, int paramInt3)
    throws IllegalArgumentException
  {
    if ((paramInt2 < 2) || (paramInt2 > 36))
      throw new IllegalArgumentException("Illegal radix " + paramInt2);
    int i = paramInt1;
    if (paramInt1 < 0)
    {
      i = -paramInt1;
      paramStringBuffer.append("-");
    }
    recursiveAppendNumber(paramStringBuffer, i, paramInt2, paramInt3);
    return paramStringBuffer;
  }

  public static boolean isUnprintable(int paramInt)
  {
    return ((paramInt < 32) || (paramInt > 126));
  }

  public static boolean escapeUnprintable(StringBuffer paramStringBuffer, int paramInt)
  {
    if (isUnprintable(paramInt))
    {
      paramStringBuffer.append('\\');
      if ((paramInt & 0xFFFF0000) != 0)
      {
        paramStringBuffer.append('U');
        paramStringBuffer.append(DIGITS[(0xF & paramInt >> 28)]);
        paramStringBuffer.append(DIGITS[(0xF & paramInt >> 24)]);
        paramStringBuffer.append(DIGITS[(0xF & paramInt >> 20)]);
        paramStringBuffer.append(DIGITS[(0xF & paramInt >> 16)]);
      }
      else
      {
        paramStringBuffer.append('u');
      }
      paramStringBuffer.append(DIGITS[(0xF & paramInt >> 12)]);
      paramStringBuffer.append(DIGITS[(0xF & paramInt >> 8)]);
      paramStringBuffer.append(DIGITS[(0xF & paramInt >> 4)]);
      paramStringBuffer.append(DIGITS[(0xF & paramInt)]);
      return true;
    }
    return false;
  }

  public static void getChars(StringBuffer paramStringBuffer, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3)
  {
    if (paramInt1 == paramInt2)
      return;
    paramStringBuffer.getChars(paramInt1, paramInt2, paramArrayOfChar, paramInt3);
  }

  public static final boolean arrayRegionMatches(char[] paramArrayOfChar1, int paramInt1, char[] paramArrayOfChar2, int paramInt2, int paramInt3)
  {
    int i = paramInt1 + paramInt3;
    int j = paramInt2 - paramInt1;
    for (int k = paramInt1; k < i; ++k)
      if (paramArrayOfChar1[k] != paramArrayOfChar2[(k + j)])
        return false;
    return true;
  }
}