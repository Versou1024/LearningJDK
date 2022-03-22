package sun.text.normalizer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class UCharacterProperty
  implements Trie.DataManipulate
{
  public CharTrie m_trie_;
  public char[] m_trieIndex_;
  public char[] m_trieData_;
  public int m_trieInitialValue_;
  public int[] m_property_;
  public VersionInfo m_unicodeVersion_;
  public static final int EXC_UPPERCASE_ = 0;
  public static final int EXC_LOWERCASE_ = 1;
  public static final int EXC_TITLECASE_ = 2;
  public static final int EXC_UNUSED_ = 3;
  public static final int EXC_NUMERIC_VALUE_ = 4;
  public static final int EXC_DENOMINATOR_VALUE_ = 5;
  public static final int EXC_MIRROR_MAPPING_ = 6;
  public static final int EXC_SPECIAL_CASING_ = 7;
  public static final int EXC_CASE_FOLDING_ = 8;
  public static final int EXC_COMBINING_CLASS_ = 9;
  public static final char LATIN_SMALL_LETTER_I_ = 105;
  public static final int TYPE_MASK = 31;
  public static final int EXCEPTION_MASK = 32;
  char[] m_case_;
  int[] m_exception_;
  CharTrie m_additionalTrie_;
  int[] m_additionalVectors_;
  int m_additionalColumnsCount_;
  int m_maxBlockScriptValue_;
  int m_maxJTGValue_;
  private static UCharacterProperty INSTANCE_ = null;
  private static final String DATA_FILE_NAME_ = "/sun/text/resources/uprops.icu";
  private static final int DATA_BUFFER_SIZE_ = 25000;
  private static final int EXC_GROUP_ = 8;
  private static final int EXC_GROUP_MASK_ = 255;
  private static final int EXC_DIGIT_MASK_ = 65535;
  private static final byte[] FLAGS_OFFSET_ = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8 };
  private static final int VALUE_SHIFT_ = 20;
  private static final int UNSIGNED_VALUE_MASK_AFTER_SHIFT_ = 2047;
  private static final int NUMERIC_TYPE_SHIFT = 12;
  private static final int SUPPLEMENTARY_FOLD_INDICATOR_MASK_ = 32768;
  private static final int SUPPLEMENTARY_FOLD_OFFSET_MASK_ = 32767;
  private static final int LEAD_SURROGATE_SHIFT_ = 10;
  private static final int SURROGATE_OFFSET_ = -56613888;
  private static final int LAST_CHAR_MASK_ = 65535;
  private static final int FIRST_NIBBLE_SHIFT_ = 4;
  private static final int LAST_NIBBLE_MASK_ = 15;
  private static final int AGE_SHIFT_ = 24;
  private static final int TAB = 9;
  private static final int LF = 10;
  private static final int FF = 12;
  private static final int CR = 13;
  private static final int U_A = 65;
  private static final int U_Z = 90;
  private static final int U_a = 97;
  private static final int U_z = 122;
  private static final int DEL = 127;
  private static final int NL = 133;
  private static final int NBSP = 160;
  private static final int CGJ = 847;
  private static final int FIGURESP = 8199;
  private static final int HAIRSP = 8202;
  private static final int ZWNJ = 8204;
  private static final int ZWJ = 8205;
  private static final int RLM = 8207;
  private static final int NNBSP = 8239;
  private static final int WJ = 8288;
  private static final int INHSWAP = 8298;
  private static final int NOMDIG = 8303;
  private static final int ZWNBSP = 65279;

  public void setIndexData(CharTrie.FriendAgent paramFriendAgent)
  {
    this.m_trieIndex_ = paramFriendAgent.getPrivateIndex();
    this.m_trieData_ = paramFriendAgent.getPrivateData();
    this.m_trieInitialValue_ = paramFriendAgent.getPrivateInitialValue();
  }

  public int getFoldingOffset(int paramInt)
  {
    if ((paramInt & 0x8000) != 0)
      return (paramInt & 0x7FFF);
    return 0;
  }

  public int getProperty(int paramInt)
  {
    if ((paramInt < 55296) || ((paramInt > 56319) && (paramInt < 65536)))
      try
      {
        return this.m_property_[this.m_trieData_[((this.m_trieIndex_[(paramInt >> 5)] << '\2') + (paramInt & 0x1F))]];
      }
      catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
      {
        return this.m_property_[this.m_trieInitialValue_];
      }
    if (paramInt <= 56319)
      return this.m_property_[this.m_trieData_[((this.m_trieIndex_[(320 + (paramInt >> 5))] << '\2') + (paramInt & 0x1F))]];
    if (paramInt <= 1114111)
      return this.m_property_[this.m_trie_.getSurrogateValue(UTF16.getLeadSurrogate(paramInt), (char)(paramInt & 0x3FF))];
    return this.m_property_[this.m_trieInitialValue_];
  }

  public static int getSignedValue(int paramInt)
  {
    return (paramInt >> 20);
  }

  public static int getExceptionIndex(int paramInt)
  {
    return (paramInt >> 20 & 0x7FF);
  }

  public boolean hasExceptionValue(int paramInt1, int paramInt2)
  {
    return ((this.m_exception_[paramInt1] & 1 << paramInt2) != 0);
  }

  public int getException(int paramInt1, int paramInt2)
  {
    if (paramInt2 == 9)
      return this.m_exception_[paramInt1];
    paramInt1 = addExceptionOffset(this.m_exception_[paramInt1], paramInt2, ++paramInt1);
    return this.m_exception_[paramInt1];
  }

  public void getFoldCase(int paramInt1, int paramInt2, StringBuffer paramStringBuffer)
  {
    paramInt1 += 2;
    while (paramInt2 > 0)
    {
      paramStringBuffer.append(this.m_case_[paramInt1]);
      ++paramInt1;
      --paramInt2;
    }
  }

  public int getAdditional(int paramInt)
  {
    return this.m_additionalVectors_[this.m_additionalTrie_.getCodePointValue(paramInt)];
  }

  public VersionInfo getAge(int paramInt)
  {
    int i = getAdditional(paramInt) >> 24;
    return VersionInfo.getInstance(i >> 4 & 0xF, i & 0xF, 0, 0);
  }

  public static int getRawSupplementary(char paramChar1, char paramChar2)
  {
    return ((paramChar1 << '\n') + paramChar2 + -56613888);
  }

  public static UCharacterProperty getInstance()
    throws RuntimeException
  {
    if (INSTANCE_ == null)
      try
      {
        INSTANCE_ = new UCharacterProperty();
      }
      catch (Exception localException)
      {
        throw new RuntimeException(localException.getMessage());
      }
    return INSTANCE_;
  }

  public static boolean isRuleWhiteSpace(int paramInt)
  {
    return ((paramInt >= 9) && (paramInt <= 8233) && (((paramInt <= 13) || (paramInt == 32) || (paramInt == 133) || (paramInt == 8206) || (paramInt == 8207) || (paramInt >= 8232))));
  }

  private UCharacterProperty()
    throws IOException
  {
    InputStream localInputStream = ICUData.getRequiredStream("/sun/text/resources/uprops.icu");
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(localInputStream, 25000);
    UCharacterPropertyReader localUCharacterPropertyReader = new UCharacterPropertyReader(localBufferedInputStream);
    localUCharacterPropertyReader.read(this);
    localBufferedInputStream.close();
    this.m_trie_.putIndexData(this);
  }

  private int addExceptionOffset(int paramInt1, int paramInt2, int paramInt3)
  {
    int i = paramInt3;
    if (paramInt2 >= 8)
    {
      i += FLAGS_OFFSET_[(paramInt1 & 0xFF)];
      paramInt1 >>= 8;
    }
    int j = (1 << (paramInt2 -= 8)) - 1;
    i += FLAGS_OFFSET_[(paramInt1 & j)];
    return i;
  }

  public UnicodeSet addPropertyStarts(UnicodeSet paramUnicodeSet)
  {
    int k;
    TrieIterator localTrieIterator1 = new TrieIterator(this.m_trie_);
    RangeValueIterator.Element localElement1 = new RangeValueIterator.Element();
    while (localTrieIterator1.next(localElement1))
      paramUnicodeSet.add(localElement1.start);
    TrieIterator localTrieIterator2 = new TrieIterator(this.m_additionalTrie_);
    RangeValueIterator.Element localElement2 = new RangeValueIterator.Element();
    while (localTrieIterator2.next(localElement2))
      paramUnicodeSet.add(localElement2.start);
    paramUnicodeSet.add(9);
    paramUnicodeSet.add(14);
    paramUnicodeSet.add(28);
    paramUnicodeSet.add(32);
    paramUnicodeSet.add(133);
    paramUnicodeSet.add(134);
    paramUnicodeSet.add(127);
    paramUnicodeSet.add(8202);
    paramUnicodeSet.add(8208);
    paramUnicodeSet.add(8298);
    paramUnicodeSet.add(8304);
    paramUnicodeSet.add(65279);
    paramUnicodeSet.add(65280);
    paramUnicodeSet.add(160);
    paramUnicodeSet.add(161);
    paramUnicodeSet.add(8199);
    paramUnicodeSet.add(8200);
    paramUnicodeSet.add(8239);
    paramUnicodeSet.add(8240);
    paramUnicodeSet.add(12295);
    paramUnicodeSet.add(12296);
    paramUnicodeSet.add(19968);
    paramUnicodeSet.add(19969);
    paramUnicodeSet.add(20108);
    paramUnicodeSet.add(20109);
    paramUnicodeSet.add(19977);
    paramUnicodeSet.add(19978);
    paramUnicodeSet.add(22235);
    paramUnicodeSet.add(22236);
    paramUnicodeSet.add(20116);
    paramUnicodeSet.add(20117);
    paramUnicodeSet.add(20845);
    paramUnicodeSet.add(20846);
    paramUnicodeSet.add(19971);
    paramUnicodeSet.add(19972);
    paramUnicodeSet.add(20843);
    paramUnicodeSet.add(20844);
    paramUnicodeSet.add(20061);
    paramUnicodeSet.add(20062);
    paramUnicodeSet.add(97);
    paramUnicodeSet.add(123);
    paramUnicodeSet.add(65);
    paramUnicodeSet.add(91);
    paramUnicodeSet.add(8288);
    paramUnicodeSet.add(65520);
    paramUnicodeSet.add(65532);
    paramUnicodeSet.add(917504);
    paramUnicodeSet.add(921600);
    paramUnicodeSet.add(847);
    paramUnicodeSet.add(848);
    paramUnicodeSet.add(8204);
    paramUnicodeSet.add(8206);
    paramUnicodeSet.add(4352);
    int j = 1;
    for (int i = 4442; i <= 4447; ++i)
    {
      k = UCharacter.getIntPropertyValue(i, 4107);
      if (j != k)
      {
        j = k;
        paramUnicodeSet.add(i);
      }
    }
    paramUnicodeSet.add(4448);
    j = 2;
    for (i = 4515; i <= 4519; ++i)
    {
      k = UCharacter.getIntPropertyValue(i, 4107);
      if (j != k)
      {
        j = k;
        paramUnicodeSet.add(i);
      }
    }
    paramUnicodeSet.add(4520);
    j = 3;
    for (i = 4602; i <= 4607; ++i)
    {
      k = UCharacter.getIntPropertyValue(i, 4107);
      if (j != k)
      {
        j = k;
        paramUnicodeSet.add(i);
      }
    }
    return paramUnicodeSet;
  }

  public UnicodeSet getInclusions()
  {
    UnicodeSet localUnicodeSet = new UnicodeSet();
    NormalizerImpl.addPropertyStarts(localUnicodeSet);
    addPropertyStarts(localUnicodeSet);
    return localUnicodeSet;
  }
}