package sun.text.normalizer;

public final class UCharacter
{
  public static final int MIN_VALUE = 0;
  public static final int MAX_VALUE = 1114111;
  public static final int SUPPLEMENTARY_MIN_VALUE = 65536;
  public static final double NO_NUMERIC_VALUE = -123456789.0D;
  private static final UCharacterProperty PROPERTY_;
  private static final char[] PROPERTY_TRIE_INDEX_;
  private static final char[] PROPERTY_TRIE_DATA_;
  private static final int[] PROPERTY_DATA_;
  private static final int PROPERTY_INITIAL_VALUE_;
  private static final int LAST_CHAR_MASK_ = 65535;
  private static final int SHIFT_24_ = 24;
  private static final int NUMERIC_TYPE_SHIFT_ = 12;
  private static final int NUMERIC_TYPE_MASK_ = 28672;
  private static final int BIDI_SHIFT_ = 6;
  private static final int BIDI_MASK_AFTER_SHIFT_ = 31;
  private static final int NUMERATOR_POWER_LIMIT_ = 2147483392;
  private static final int JOINING_TYPE_MASK_ = 14336;
  private static final int JOINING_TYPE_SHIFT_ = 11;
  private static final int JOINING_GROUP_MASK_ = 2016;
  private static final int JOINING_GROUP_SHIFT_ = 5;
  private static final int DECOMPOSITION_TYPE_MASK_ = 31;
  private static final int EAST_ASIAN_MASK_ = 229376;
  private static final int EAST_ASIAN_SHIFT_ = 15;
  private static final int LINE_BREAK_MASK_ = 8126464;
  private static final int LINE_BREAK_SHIFT_ = 18;
  private static final int BLOCK_MASK_ = 32640;
  private static final int BLOCK_SHIFT_ = 7;
  private static final int SCRIPT_MASK_ = 127;

  public static int digit(int paramInt1, int paramInt2)
  {
    int i = getProperty(paramInt1);
    if (getNumericType(i) != 1)
      return ((paramInt2 <= 10) ? -1 : getEuropeanDigit(paramInt1));
    if (isNotExceptionIndicator(i))
    {
      if (i < 0)
        break label78;
      return UCharacterProperty.getSignedValue(i);
    }
    int j = UCharacterProperty.getExceptionIndex(i);
    if (PROPERTY_.hasExceptionValue(j, 4))
    {
      int k = PROPERTY_.getException(j, 4);
      if (k >= 0)
        return k;
    }
    if (paramInt2 > 10)
    {
      label78: j = getEuropeanDigit(paramInt1);
      if ((j >= 0) && (j < paramInt2))
        return j;
    }
    return -1;
  }

  public static double getUnicodeNumericValue(int paramInt)
  {
    int i = PROPERTY_.getProperty(paramInt);
    int j = getNumericType(i);
    if ((j > 0) && (j < 4))
    {
      if (isNotExceptionIndicator(i))
        return UCharacterProperty.getSignedValue(i);
      int k = UCharacterProperty.getExceptionIndex(i);
      int l = 0;
      int i1 = 0;
      double d1 = 0D;
      if (PROPERTY_.hasExceptionValue(k, 4))
      {
        int i2 = PROPERTY_.getException(k, 4);
        if (i2 >= 2147483392)
        {
          i2 &= 255;
          d1 = Math.pow(10.0D, i2);
        }
        else
        {
          d1 = i2;
        }
        l = 1;
      }
      double d2 = 0D;
      if (PROPERTY_.hasExceptionValue(k, 5))
      {
        d2 = PROPERTY_.getException(k, 5);
        if (d1 != 0D)
          return (d1 / d2);
        i1 = 1;
      }
      if (l != 0)
      {
        if (i1 != 0)
          return (d1 / d2);
        return d1;
      }
      if (i1 != 0)
        return (1D / d2);
    }
    return -123456789.0D;
  }

  public static int getType(int paramInt)
  {
    return (getProperty(paramInt) & 0x1F);
  }

  public static int getCodePoint(char paramChar1, char paramChar2)
  {
    if ((paramChar1 >= 55296) && (paramChar1 <= 56319) && (paramChar2 >= 56320) && (paramChar2 <= 57343))
      return UCharacterProperty.getRawSupplementary(paramChar1, paramChar2);
    throw new IllegalArgumentException("Illegal surrogate characters");
  }

  public static int getDirection(int paramInt)
  {
    return (getProperty(paramInt) >> 6 & 0x1F);
  }

  public static String foldCase(String paramString, boolean paramBoolean)
  {
    int i = paramString.length();
    StringBuffer localStringBuffer = new StringBuffer(i);
    int j = 0;
    while (true)
    {
      int k;
      int i1;
      while (true)
      {
        do
          while (true)
          {
            while (true)
            {
              do
                while (true)
                {
                  while (true)
                  {
                    while (true)
                    {
                      if (j >= i)
                        break label289;
                      k = UTF16.charAt(paramString, j);
                      j += UTF16.getCharCount(k);
                      int l = PROPERTY_.getProperty(k);
                      if (isNotExceptionIndicator(l))
                      {
                        i1 = 0x1F & l;
                        if ((i1 == 1) || (i1 == 3))
                          k += UCharacterProperty.getSignedValue(l);
                        break label279:
                      }
                      i1 = UCharacterProperty.getExceptionIndex(l);
                      if (!(PROPERTY_.hasExceptionValue(i1, 8)))
                        break label256;
                      int i2 = PROPERTY_.getException(i1, 8);
                      if (i2 == 0)
                        break;
                      PROPERTY_.getFoldCase(i2 & 0xFFFF, i2 >> 24, localStringBuffer);
                    }
                    if ((k == 73) || (k == 304))
                      break;
                    UTF16.append(localStringBuffer, k);
                  }
                  if (!(paramBoolean))
                    break label220;
                  if (k != 73)
                    break;
                  localStringBuffer.append('i');
                }
              while (k != 304);
              localStringBuffer.append('i');
              localStringBuffer.append(775);
            }
            label220: if (k != 73)
              break;
            localStringBuffer.append(305);
          }
        while (k != 304);
        localStringBuffer.append('i');
      }
      if (PROPERTY_.hasExceptionValue(i1, 1))
        label256: k = PROPERTY_.getException(i1, 1);
      label279: UTF16.append(localStringBuffer, k);
    }
    label289: return localStringBuffer.toString();
  }

  public static VersionInfo getAge(int paramInt)
  {
    if ((paramInt < 0) || (paramInt > 1114111))
      throw new IllegalArgumentException("Codepoint out of bounds");
    return PROPERTY_.getAge(paramInt);
  }

  public static int getIntPropertyValue(int paramInt1, int paramInt2)
  {
    if (paramInt2 == 4107)
    {
      if (paramInt1 < 4352)
        break label128:
      if (paramInt1 <= 4607)
      {
        if (paramInt1 <= 4447)
        {
          if ((paramInt1 != 4447) && (paramInt1 > 4441) && (getType(paramInt1) != 5))
            break label128;
          return 1;
        }
        if (paramInt1 <= 4519)
        {
          if ((paramInt1 > 4514) && (getType(paramInt1) != 5))
            break label128;
          return 2;
        }
        if ((paramInt1 > 4601) && (getType(paramInt1) != 5))
          break label128;
        return 3;
      }
      paramInt1 -= 44032;
      if (paramInt1 < 0)
        break label128:
      if (paramInt1 < 11172)
        return ((paramInt1 % 28 == 0) ? 4 : 5);
    }
    label128: return 0;
  }

  private static int getEuropeanDigit(int paramInt)
  {
    if (((paramInt > 122) && (paramInt < 65313)) || (paramInt < 65) || ((paramInt > 90) && (paramInt < 97)) || (paramInt > 65370) || ((paramInt > 65329) && (paramInt < 65345)))
      return -1;
    if (paramInt <= 122)
      return (paramInt + 10 - ((paramInt <= 90) ? 65 : 97));
    if (paramInt <= 65338)
      return (paramInt + 10 - 65313);
    return (paramInt + 10 - 65345);
  }

  private static int getNumericType(int paramInt)
  {
    return ((paramInt & 0x7000) >> 12);
  }

  private static boolean isNotExceptionIndicator(int paramInt)
  {
    return ((paramInt & 0x20) == 0);
  }

  private static int getProperty(int paramInt)
  {
    if ((paramInt < 55296) || ((paramInt > 56319) && (paramInt < 65536)))
      try
      {
        return PROPERTY_DATA_[PROPERTY_TRIE_DATA_[((PROPERTY_TRIE_INDEX_[(paramInt >> 5)] << '\2') + (paramInt & 0x1F))]];
      }
      catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
      {
        return PROPERTY_INITIAL_VALUE_;
      }
    if (paramInt <= 56319)
      return PROPERTY_DATA_[PROPERTY_TRIE_DATA_[((PROPERTY_TRIE_INDEX_[(320 + (paramInt >> 5))] << '\2') + (paramInt & 0x1F))]];
    if (paramInt <= 1114111)
      return PROPERTY_DATA_[PROPERTY_.m_trie_.getSurrogateValue(UTF16.getLeadSurrogate(paramInt), (char)(paramInt & 0x3FF))];
    return PROPERTY_INITIAL_VALUE_;
  }

  static
  {
    try
    {
      PROPERTY_ = UCharacterProperty.getInstance();
      PROPERTY_TRIE_INDEX_ = PROPERTY_.m_trieIndex_;
      PROPERTY_TRIE_DATA_ = PROPERTY_.m_trieData_;
      PROPERTY_DATA_ = PROPERTY_.m_property_;
      PROPERTY_INITIAL_VALUE_ = PROPERTY_DATA_[PROPERTY_.m_trieInitialValue_];
    }
    catch (Exception localException)
    {
      throw new RuntimeException(localException.getMessage());
    }
  }

  /**
   * @deprecated
   */
  public static abstract interface ECharacterCategory
  {
    public static final int UPPERCASE_LETTER = 1;
    public static final int TITLECASE_LETTER = 3;
    public static final int OTHER_LETTER = 5;
  }

  public static abstract interface HangulSyllableType
  {
    public static final int NOT_APPLICABLE = 0;
    public static final int LEADING_JAMO = 1;
    public static final int VOWEL_JAMO = 2;
    public static final int TRAILING_JAMO = 3;
    public static final int LV_SYLLABLE = 4;
    public static final int LVT_SYLLABLE = 5;
    public static final int COUNT = 6;
  }

  public static abstract interface NumericType
  {
    public static final int NONE = 0;
    public static final int DECIMAL = 1;
    public static final int DIGIT = 2;
    public static final int NUMERIC = 3;
    public static final int COUNT = 4;
  }
}