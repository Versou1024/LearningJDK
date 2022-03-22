package sun.text.normalizer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class NormalizerImpl
{
  static final NormalizerImpl IMPL;
  static final int UNSIGNED_BYTE_MASK = 255;
  static final long UNSIGNED_INT_MASK = 4294967295L;
  private static final String DATA_FILE_NAME = "/sun/text/resources/unorm.icu";
  public static final int QC_NFC = 17;
  public static final int QC_NFKC = 34;
  public static final int QC_NFD = 4;
  public static final int QC_NFKD = 8;
  public static final int QC_ANY_NO = 15;
  public static final int QC_MAYBE = 16;
  public static final int QC_ANY_MAYBE = 48;
  public static final int QC_MASK = 63;
  private static final int COMBINES_FWD = 64;
  private static final int COMBINES_BACK = 128;
  public static final int COMBINES_ANY = 192;
  private static final int CC_SHIFT = 8;
  public static final int CC_MASK = 65280;
  private static final int EXTRA_SHIFT = 16;
  private static final long MIN_SPECIAL = 4227858432L;
  private static final long SURROGATES_TOP = 4293918720L;
  private static final long MIN_HANGUL = 4293918720L;
  private static final long MIN_JAMO_V = 4294049792L;
  private static final long JAMO_V_TOP = 4294115328L;
  static final int INDEX_TRIE_SIZE = 0;
  static final int INDEX_CHAR_COUNT = 1;
  static final int INDEX_COMBINE_DATA_COUNT = 2;
  public static final int INDEX_MIN_NFC_NO_MAYBE = 6;
  public static final int INDEX_MIN_NFKC_NO_MAYBE = 7;
  public static final int INDEX_MIN_NFD_NO_MAYBE = 8;
  public static final int INDEX_MIN_NFKD_NO_MAYBE = 9;
  static final int INDEX_FCD_TRIE_SIZE = 10;
  static final int INDEX_AUX_TRIE_SIZE = 11;
  static final int INDEX_TOP = 32;
  private static final int AUX_UNSAFE_SHIFT = 11;
  private static final int AUX_COMP_EX_SHIFT = 10;
  private static final int AUX_NFC_SKIPPABLE_F_SHIFT = 12;
  private static final int AUX_MAX_FNC = 1024;
  private static final int AUX_UNSAFE_MASK = 2048;
  private static final int AUX_FNC_MASK = 1023;
  private static final int AUX_COMP_EX_MASK = 1024;
  private static final long AUX_NFC_SKIP_F_MASK = 4096L;
  private static final int MAX_BUFFER_SIZE = 20;
  private static FCDTrieImpl fcdTrieImpl;
  private static NormTrieImpl normTrieImpl;
  private static AuxTrieImpl auxTrieImpl;
  private static int[] indexes;
  private static char[] combiningTable;
  private static char[] extraData;
  private static boolean isDataLoaded;
  private static boolean isFormatVersion_2_1;
  private static boolean isFormatVersion_2_2;
  private static byte[] unicodeVersion;
  private static final int DATA_BUFFER_SIZE = 25000;
  public static final int MIN_WITH_LEAD_CC = 768;
  private static final int DECOMP_FLAG_LENGTH_HAS_CC = 128;
  private static final int DECOMP_LENGTH_MASK = 127;
  private static final int BMP_INDEX_LENGTH = 2048;
  private static final int SURROGATE_BLOCK_BITS = 5;
  public static final int JAMO_L_BASE = 4352;
  public static final int JAMO_V_BASE = 4449;
  public static final int JAMO_T_BASE = 4519;
  public static final int HANGUL_BASE = 44032;
  public static final int JAMO_L_COUNT = 19;
  public static final int JAMO_V_COUNT = 21;
  public static final int JAMO_T_COUNT = 28;
  public static final int HANGUL_COUNT = 11172;
  private static final int OPTIONS_NX_MASK = 31;
  private static final int OPTIONS_UNICODE_MASK = 224;
  public static final int OPTIONS_SETS_MASK = 255;
  private static final int OPTIONS_UNICODE_SHIFT = 5;
  private static final UnicodeSet[] nxCache;
  private static final int NX_HANGUL = 1;
  private static final int NX_CJK_COMPAT = 2;
  public static final int BEFORE_PRI_29 = 256;
  public static final int OPTIONS_COMPAT = 4096;
  public static final int OPTIONS_COMPOSE_CONTIGUOUS = 8192;
  public static final int WITHOUT_CORRIGENDUM4_CORRECTIONS = 262144;
  private static final char[][] corrigendum4MappingTable;

  public static int getFromIndexesArr(int paramInt)
  {
    return indexes[paramInt];
  }

  private NormalizerImpl()
    throws IOException
  {
    if (!(isDataLoaded))
    {
      InputStream localInputStream = ICUData.getRequiredStream("/sun/text/resources/unorm.icu");
      BufferedInputStream localBufferedInputStream = new BufferedInputStream(localInputStream, 25000);
      NormalizerDataReader localNormalizerDataReader = new NormalizerDataReader(localBufferedInputStream);
      indexes = localNormalizerDataReader.readIndexes(32);
      byte[] arrayOfByte1 = new byte[indexes[0]];
      int i = indexes[2];
      combiningTable = new char[i];
      int j = indexes[1];
      extraData = new char[j];
      byte[] arrayOfByte2 = new byte[indexes[10]];
      byte[] arrayOfByte3 = new byte[indexes[11]];
      fcdTrieImpl = new FCDTrieImpl();
      normTrieImpl = new NormTrieImpl();
      auxTrieImpl = new AuxTrieImpl();
      localNormalizerDataReader.read(arrayOfByte1, arrayOfByte2, arrayOfByte3, extraData, combiningTable);
      NormTrieImpl.normTrie = new IntTrie(new ByteArrayInputStream(arrayOfByte1), normTrieImpl);
      FCDTrieImpl.fcdTrie = new CharTrie(new ByteArrayInputStream(arrayOfByte2), fcdTrieImpl);
      AuxTrieImpl.auxTrie = new CharTrie(new ByteArrayInputStream(arrayOfByte3), auxTrieImpl);
      isDataLoaded = true;
      byte[] arrayOfByte4 = localNormalizerDataReader.getDataFormatVersion();
      isFormatVersion_2_1 = (arrayOfByte4[0] > 2) || ((arrayOfByte4[0] == 2) && (arrayOfByte4[1] >= 1));
      isFormatVersion_2_2 = (arrayOfByte4[0] > 2) || ((arrayOfByte4[0] == 2) && (arrayOfByte4[1] >= 2));
      unicodeVersion = localNormalizerDataReader.getUnicodeVersion();
      localBufferedInputStream.close();
    }
  }

  private static boolean isHangulWithoutJamoT(char paramChar)
  {
    paramChar = (char)(paramChar - 44032);
    return ((paramChar < 11172) && (paramChar % '\28' == 0));
  }

  private static boolean isNorm32Regular(long paramLong)
  {
    return (paramLong < 4227858432L);
  }

  private static boolean isNorm32LeadSurrogate(long paramLong)
  {
    return ((4227858432L <= paramLong) && (paramLong < 4293918720L));
  }

  private static boolean isNorm32HangulOrJamo(long paramLong)
  {
    return (paramLong >= 4293918720L);
  }

  private static boolean isJamoVTNorm32JamoV(long paramLong)
  {
    return (paramLong < 4294115328L);
  }

  public static long getNorm32(char paramChar)
  {
    return (0xFFFFFFFF & NormTrieImpl.normTrie.getLeadValue(paramChar));
  }

  public static long getNorm32FromSurrogatePair(long paramLong, char paramChar)
  {
    return (0xFFFFFFFF & NormTrieImpl.normTrie.getTrailValue((int)paramLong, paramChar));
  }

  private static long getNorm32(int paramInt)
  {
    return (0xFFFFFFFF & NormTrieImpl.normTrie.getCodePointValue(paramInt));
  }

  private static long getNorm32(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    long l = getNorm32(paramArrayOfChar[paramInt1]);
    if (((l & paramInt2) > 3412046964836007936L) && (isNorm32LeadSurrogate(l)))
      l = getNorm32FromSurrogatePair(l, paramArrayOfChar[(paramInt1 + 1)]);
    return l;
  }

  public static VersionInfo getUnicodeVersion()
  {
    return VersionInfo.getInstance(unicodeVersion[0], unicodeVersion[1], unicodeVersion[2], unicodeVersion[3]);
  }

  public static char getFCD16(char paramChar)
  {
    return FCDTrieImpl.fcdTrie.getLeadValue(paramChar);
  }

  public static char getFCD16FromSurrogatePair(char paramChar1, char paramChar2)
  {
    return FCDTrieImpl.fcdTrie.getTrailValue(paramChar1, paramChar2);
  }

  public static int getFCD16(int paramInt)
  {
    return FCDTrieImpl.fcdTrie.getCodePointValue(paramInt);
  }

  private static int getExtraDataIndex(long paramLong)
  {
    return (int)(paramLong >> 16);
  }

  private static int decompose(long paramLong, int paramInt, DecomposeArgs paramDecomposeArgs)
  {
    int i = getExtraDataIndex(paramLong);
    paramDecomposeArgs.length = extraData[(i++)];
    if (((paramLong & paramInt & 0x8) != 3412046964836007936L) && (paramDecomposeArgs.length >= 256))
    {
      i += (paramDecomposeArgs.length >> 7 & 0x1) + (paramDecomposeArgs.length & 0x7F);
      paramDecomposeArgs.length >>= 8;
    }
    if ((paramDecomposeArgs.length & 0x80) > 0)
    {
      int j = extraData[(i++)];
      paramDecomposeArgs.cc = (0xFF & j >> 8);
      paramDecomposeArgs.trailCC = (0xFF & j);
    }
    else
    {
      paramDecomposeArgs.cc = (paramDecomposeArgs.trailCC = 0);
    }
    paramDecomposeArgs.length &= 127;
    return i;
  }

  private static int decompose(long paramLong, DecomposeArgs paramDecomposeArgs)
  {
    int i = getExtraDataIndex(paramLong);
    paramDecomposeArgs.length = extraData[(i++)];
    if ((paramDecomposeArgs.length & 0x80) > 0)
    {
      int j = extraData[(i++)];
      paramDecomposeArgs.cc = (0xFF & j >> 8);
      paramDecomposeArgs.trailCC = (0xFF & j);
    }
    else
    {
      paramDecomposeArgs.cc = (paramDecomposeArgs.trailCC = 0);
    }
    paramDecomposeArgs.length &= 127;
    return i;
  }

  private static int getNextCC(NextCCArgs paramNextCCArgs)
  {
    paramNextCCArgs.c = paramNextCCArgs.source[(paramNextCCArgs.next++)];
    long l = getNorm32(paramNextCCArgs.c);
    if ((l & 0xFF00) == 3412046810217185280L)
    {
      paramNextCCArgs.c2 = ';
      return 0;
    }
    if (!(isNorm32LeadSurrogate(l)))
    {
      paramNextCCArgs.c2 = ';
    }
    else if (paramNextCCArgs.next != paramNextCCArgs.limit)
    {
      if (UTF16.isTrailSurrogate(paramNextCCArgs.c2 = paramNextCCArgs.source[paramNextCCArgs.next]))
      {
        paramNextCCArgs.next += 1;
        l = getNorm32FromSurrogatePair(l, paramNextCCArgs.c2);
      }
    }
    else
    {
      paramNextCCArgs.c2 = ';
      return 0;
    }
    return (int)(0xFF & l >> 8);
  }

  private static long getPrevNorm32(PrevArgs paramPrevArgs, int paramInt1, int paramInt2)
  {
    paramPrevArgs.c = paramPrevArgs.src[(--paramPrevArgs.current)];
    paramPrevArgs.c2 = ';
    if (paramPrevArgs.c < paramInt1)
      return 3412047463052214272L;
    if (!(UTF16.isSurrogate(paramPrevArgs.c)))
      return getNorm32(paramPrevArgs.c);
    if (UTF16.isLeadSurrogate(paramPrevArgs.c))
      return 3412047463052214272L;
    if (paramPrevArgs.current != paramPrevArgs.start)
      if (UTF16.isLeadSurrogate(paramPrevArgs.c2 = paramPrevArgs.src[(paramPrevArgs.current - 1)]))
      {
        paramPrevArgs.current -= 1;
        long l = getNorm32(paramPrevArgs.c2);
        if ((l & paramInt2) == 3412047669210644480L)
          return 3412048459484626944L;
        return getNorm32FromSurrogatePair(l, paramPrevArgs.c);
      }
    paramPrevArgs.c2 = ';
    return 3412046964836007936L;
  }

  private static int getPrevCC(PrevArgs paramPrevArgs)
  {
    return (int)(0xFF & getPrevNorm32(paramPrevArgs, 768, 65280) >> 8);
  }

  public static boolean isNFDSafe(long paramLong, int paramInt1, int paramInt2)
  {
    if ((paramLong & paramInt1) == 3412046810217185280L)
      return true;
    if ((isNorm32Regular(paramLong)) && ((paramLong & paramInt2) != 3412046964836007936L))
    {
      DecomposeArgs localDecomposeArgs = new DecomposeArgs(null);
      decompose(paramLong, paramInt2, localDecomposeArgs);
      return (localDecomposeArgs.cc == 0);
    }
    return ((paramLong & 0xFF00) == 3412047102274961408L);
  }

  public static boolean isTrueStarter(long paramLong, int paramInt1, int paramInt2)
  {
    if ((paramLong & paramInt1) == 3412046810217185280L)
      return true;
    if ((paramLong & paramInt2) == 3412046793037316096L)
      break label74;
    DecomposeArgs localDecomposeArgs = new DecomposeArgs(null);
    int i = decompose(paramLong, paramInt2, localDecomposeArgs);
    if (localDecomposeArgs.cc != 0)
      break label74;
    int j = paramInt1 & 0x3F;
    label74: return ((getNorm32(extraData, i, j) & j) == 3412047531771691008L);
  }

  private static int insertOrdered(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, char paramChar1, char paramChar2, int paramInt4)
  {
    int i1 = paramInt4;
    if ((paramInt1 < paramInt2) && (paramInt4 != 0))
    {
      int i;
      int j = i = paramInt2;
      PrevArgs localPrevArgs = new PrevArgs(null);
      localPrevArgs.current = paramInt2;
      localPrevArgs.start = paramInt1;
      localPrevArgs.src = paramArrayOfChar;
      int l = getPrevCC(localPrevArgs);
      j = localPrevArgs.current;
      if (paramInt4 < l)
      {
        i1 = l;
        for (i = j; paramInt1 < j; i = j)
        {
          l = getPrevCC(localPrevArgs);
          j = localPrevArgs.current;
          if (paramInt4 >= l)
            break;
        }
        int k = paramInt3;
        do
          paramArrayOfChar[(--k)] = paramArrayOfChar[(--paramInt2)];
        while (i != paramInt2);
      }
    }
    paramArrayOfChar[paramInt2] = paramChar1;
    if (paramChar2 != 0)
      paramArrayOfChar[(paramInt2 + 1)] = paramChar2;
    return i1;
  }

  private static int mergeOrdered(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, boolean paramBoolean)
  {
    int k = 0;
    int l = (paramInt2 == paramInt3) ? 1 : 0;
    NextCCArgs localNextCCArgs = new NextCCArgs(null);
    localNextCCArgs.source = paramArrayOfChar2;
    localNextCCArgs.next = paramInt3;
    localNextCCArgs.limit = paramInt4;
    while (true)
    {
      int j;
      while (true)
      {
        if (((paramInt1 == paramInt2) && (paramBoolean)) || (localNextCCArgs.next >= localNextCCArgs.limit))
          break label186;
        j = getNextCC(localNextCCArgs);
        if (j != 0)
          break;
        k = 0;
        if (l != 0)
        {
          paramInt2 = localNextCCArgs.next;
        }
        else
        {
          paramArrayOfChar2[(paramInt2++)] = localNextCCArgs.c;
          if (localNextCCArgs.c2 != 0)
            paramArrayOfChar2[(paramInt2++)] = localNextCCArgs.c2;
        }
        if (paramBoolean)
          break label186:
        paramInt1 = paramInt2;
      }
      int i = paramInt2 + ((localNextCCArgs.c2 == 0) ? 1 : 2);
      k = insertOrdered(paramArrayOfChar1, paramInt1, paramInt2, i, localNextCCArgs.c, localNextCCArgs.c2, j);
      paramInt2 = i;
    }
    if (localNextCCArgs.next == localNextCCArgs.limit)
      label186: return k;
    if (l == 0)
    {
      do
        paramArrayOfChar1[(paramInt2++)] = paramArrayOfChar2[(localNextCCArgs.next++)];
      while (localNextCCArgs.next != localNextCCArgs.limit);
      localNextCCArgs.limit = paramInt2;
    }
    PrevArgs localPrevArgs = new PrevArgs(null);
    localPrevArgs.src = paramArrayOfChar2;
    localPrevArgs.start = paramInt1;
    localPrevArgs.current = localNextCCArgs.limit;
    return getPrevCC(localPrevArgs);
  }

  private static int mergeOrdered(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4)
  {
    return mergeOrdered(paramArrayOfChar1, paramInt1, paramInt2, paramArrayOfChar2, paramInt3, paramInt4, true);
  }

  public static NormalizerBase.QuickCheckResult quickCheck(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, boolean paramBoolean, UnicodeSet paramUnicodeSet)
  {
    ComposePartArgs localComposePartArgs = new ComposePartArgs(null);
    int l = paramInt1;
    if (!(isDataLoaded))
      return NormalizerBase.MAYBE;
    int i = 0xFF00 | paramInt4;
    NormalizerBase.QuickCheckResult localQuickCheckResult = NormalizerBase.YES;
    int k = 0;
    while (true)
    {
      while (true)
      {
        long l2;
        do
        {
          long l1;
          char c1;
          char c2;
          while (true)
          {
            if (paramInt1 == paramInt2)
              return localQuickCheckResult;
            if ((c1 = paramArrayOfChar[(paramInt1++)]) >= paramInt3)
              if (((l1 = getNorm32(c1)) & i) != 3412040110068203520L)
                break;
            k = 0;
          }
          if (isNorm32LeadSurrogate(l1))
            if (paramInt1 != paramInt2)
            {
              if (UTF16.isTrailSurrogate(c2 = paramArrayOfChar[paramInt1]))
              {
                ++paramInt1;
                l1 = getNorm32FromSurrogatePair(l1, c2);
              }
            }
            else
            {
              l1 = 3412040728543494144L;
              c2 = ';
            }
          else
            c2 = ';
          if (nx_contains(paramUnicodeSet, c1, c2))
            l1 = 3412040058528595968L;
          int j = (char)(int)(l1 >> 8 & 0xFF);
          if ((j != 0) && (j < k))
            return NormalizerBase.NO;
          k = j;
          l2 = l1 & paramInt4;
          if ((l2 & 0xF) >= 3412047995628158977L)
          {
            localQuickCheckResult = NormalizerBase.NO;
            break label345:
          }
        }
        while (l2 == 3412047634850906112L);
        if (!(paramBoolean))
          break;
        localQuickCheckResult = NormalizerBase.MAYBE;
      }
      int i2 = paramInt4 << 2 & 0xF;
      int i1 = paramInt1 - 1;
      if (UTF16.isTrailSurrogate(paramArrayOfChar[i1]));
      i1 = findPreviousStarter(paramArrayOfChar, ???, --i1, i, i2, (char)paramInt3);
      paramInt1 = findNextStarter(paramArrayOfChar, paramInt1, paramInt2, paramInt4, i2, (char)paramInt3);
      localComposePartArgs.prevCC = k;
      char[] arrayOfChar = composePart(localComposePartArgs, i1, paramArrayOfChar, paramInt1, paramInt2, paramInt5, paramUnicodeSet);
      if (0 != strCompare(arrayOfChar, 0, localComposePartArgs.length, paramArrayOfChar, i1, paramInt1 - i1, false))
      {
        localQuickCheckResult = NormalizerBase.NO;
        break;
      }
    }
    label345: return localQuickCheckResult;
  }

  public static int decompose(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, boolean paramBoolean, int[] paramArrayOfInt, UnicodeSet paramUnicodeSet)
  {
    int k;
    int i6;
    int i9;
    char[] arrayOfChar1 = new char[3];
    int i11 = paramInt3;
    int i12 = paramInt1;
    if (!(paramBoolean))
    {
      i6 = (char)indexes[8];
      k = 4;
    }
    else
    {
      i6 = (char)indexes[9];
      k = 8;
    }
    int j = 0xFF00 | k;
    int i1 = 0;
    int i8 = 0;
    long l = 3412047291253522432L;
    int i4 = 0;
    int i10 = 0;
    int i7 = i9 = -1;
    while (true)
    {
      do
      {
        int i3;
        char c1;
        int i5;
        char c2;
        char[] arrayOfChar2;
        int i = i12;
        while (i12 != paramInt2)
        {
          if ((i4 = paramArrayOfChar1[i12]) >= i6)
            if (((l = getNorm32(i4)) & j) != 3412048407945019392L)
              break;
          i8 = 0;
          ++i12;
        }
        if (i12 != i)
        {
          int i2 = i12 - i;
          if (i11 + i2 <= paramInt4)
            System.arraycopy(paramArrayOfChar1, i, paramArrayOfChar2, i11, i2);
          i11 += i2;
          i1 = i11;
        }
        if (i12 == paramInt2)
          break label668:
        ++i12;
        if (isNorm32HangulOrJamo(l))
        {
          if (nx_contains(paramUnicodeSet, i4))
          {
            i5 = 0;
            arrayOfChar2 = null;
            i3 = 1;
          }
          else
          {
            arrayOfChar2 = arrayOfChar1;
            i10 = 0;
            i7 = i9 = 0;
            c1 = (char)(i4 - 44032);
            i5 = (char)(c1 % '\28');
            c1 = (char)(c1 / '\28');
            if (i5 > 0)
            {
              arrayOfChar1[2] = (char)(4519 + i5);
              i3 = 3;
            }
            else
            {
              i3 = 2;
            }
            arrayOfChar1[1] = (char)(4449 + c1 % '\21');
            arrayOfChar1[0] = (char)(4352 + c1 / '\21');
          }
        }
        else
        {
          if (isNorm32Regular(l))
          {
            i5 = 0;
            i3 = 1;
          }
          else if (i12 != paramInt2)
          {
            if (UTF16.isTrailSurrogate(i5 = paramArrayOfChar1[i12]))
            {
              ++i12;
              i3 = 2;
              l = getNorm32FromSurrogatePair(l, i5);
            }
          }
          else
          {
            c2 = ';
            i3 = 1;
            l = 3412040316226633728L;
          }
          if (nx_contains(paramUnicodeSet, c1, c2))
          {
            i7 = i9 = 0;
            arrayOfChar2 = null;
          }
          else if ((l & k) == 3412048390765150208L)
          {
            i7 = i9 = (int)(0xFF & l >> 8);
            arrayOfChar2 = null;
            i10 = -1;
          }
          else
          {
            DecomposeArgs localDecomposeArgs = new DecomposeArgs(null);
            i10 = decompose(l, k, localDecomposeArgs);
            arrayOfChar2 = extraData;
            i3 = localDecomposeArgs.length;
            i7 = localDecomposeArgs.cc;
            i9 = localDecomposeArgs.trailCC;
            if (i3 == 1)
            {
              c1 = arrayOfChar2[i10];
              c2 = ';
              arrayOfChar2 = null;
              i10 = -1;
            }
          }
        }
        if (i11 + i3 <= paramInt4)
        {
          int i13 = i11;
          if (arrayOfChar2 == null)
          {
            if ((i7 != 0) && (i7 < i8))
            {
              i11 += i3;
              i9 = insertOrdered(paramArrayOfChar2, i1, i13, i11, c1, c2, i7);
            }
            else
            {
              paramArrayOfChar2[(i11++)] = c1;
              if (c2 != 0)
                paramArrayOfChar2[(i11++)] = c2;
            }
          }
          else if ((i7 != 0) && (i7 < i8))
          {
            i11 += i3;
            i9 = mergeOrdered(paramArrayOfChar2, i1, i13, arrayOfChar2, i10, i10 + i3);
          }
          else
          {
            do
              paramArrayOfChar2[(i11++)] = arrayOfChar2[(i10++)];
            while (--i3 > 0);
          }
        }
        else
        {
          i11 += i3;
        }
        i8 = i9;
      }
      while (i8 != 0);
      i1 = i11;
    }
    label668: paramArrayOfInt[0] = i8;
    return (i11 - paramInt3);
  }

  private static int getNextCombining(NextCombiningArgs paramNextCombiningArgs, int paramInt, UnicodeSet paramUnicodeSet)
  {
    paramNextCombiningArgs.c = paramNextCombiningArgs.source[(paramNextCombiningArgs.start++)];
    long l = getNorm32(paramNextCombiningArgs.c);
    paramNextCombiningArgs.c2 = ';
    paramNextCombiningArgs.combiningIndex = 0;
    paramNextCombiningArgs.cc = ';
    if ((l & 0xFFC0) == 3412046810217185280L)
      return 0;
    if (isNorm32Regular(l))
      break label153:
    if (isNorm32HangulOrJamo(l))
    {
      paramNextCombiningArgs.combiningIndex = (int)(0xFFFFFFFF & (0xFFF0 | l >> 16));
      return (int)(l & 0xC0);
    }
    if (paramNextCombiningArgs.start != paramInt)
    {
      if (UTF16.isTrailSurrogate(paramNextCombiningArgs.c2 = paramNextCombiningArgs.source[paramNextCombiningArgs.start]))
      {
        paramNextCombiningArgs.start += 1;
        l = getNorm32FromSurrogatePair(l, paramNextCombiningArgs.c2);
      }
    }
    else
    {
      paramNextCombiningArgs.c2 = ';
      return 0;
    }
    if (nx_contains(paramUnicodeSet, paramNextCombiningArgs.c, paramNextCombiningArgs.c2))
      label153: return 0;
    paramNextCombiningArgs.cc = (char)(int)(l >> 8 & 0xFF);
    int i = (int)(l & 0xC0);
    if (i != 0)
    {
      int j = getExtraDataIndex(l);
      paramNextCombiningArgs.combiningIndex = ((j > 0) ? extraData[(j - 1)] : 0);
    }
    return i;
  }

  private static int getCombiningIndexFromStarter(char paramChar1, char paramChar2)
  {
    long l = getNorm32(paramChar1);
    if (paramChar2 != 0)
      l = getNorm32FromSurrogatePair(l, paramChar2);
    return extraData[(getExtraDataIndex(l) - 1)];
  }

  private static int combine(char[] paramArrayOfChar, int paramInt1, int paramInt2, int[] paramArrayOfInt)
  {
    int i;
    if (paramArrayOfInt.length < 2)
      throw new IllegalArgumentException();
    while (true)
    {
      i = paramArrayOfChar[(paramInt1++)];
      if (i >= paramInt2)
        break;
      paramInt1 += (((paramArrayOfChar[paramInt1] & 0x8000) != 0) ? 2 : 1);
    }
    if ((i & 0x7FFF) == paramInt2)
    {
      int k;
      int j = paramArrayOfChar[paramInt1];
      i = (int)(0xFFFFFFFF & (j & 0x2000) + 1);
      if ((j & 0x8000) != 0)
      {
        if ((j & 0x4000) != 0)
        {
          j = (int)(0xFFFFFFFF & (j & 0x3FF | 0xD800));
          k = paramArrayOfChar[(paramInt1 + 1)];
        }
        else
        {
          j = paramArrayOfChar[(paramInt1 + 1)];
          k = 0;
        }
      }
      else
      {
        j &= 8191;
        k = 0;
      }
      paramArrayOfInt[0] = j;
      paramArrayOfInt[1] = k;
      return i;
    }
    return 0;
  }

  private static char recompose(RecomposeArgs paramRecomposeArgs, int paramInt, UnicodeSet paramUnicodeSet)
  {
    int i4 = 0;
    int i5 = 0;
    int[] arrayOfInt = new int[2];
    int i8 = -1;
    int i1 = 0;
    int i7 = 0;
    int i6 = 0;
    NextCombiningArgs localNextCombiningArgs = new NextCombiningArgs(null);
    localNextCombiningArgs.source = paramRecomposeArgs.source;
    localNextCombiningArgs.cc = ';
    localNextCombiningArgs.c2 = ';
    while (true)
    {
      label940: 
      do
        while (true)
        {
          while (true)
          {
            int l;
            int i2;
            while (true)
            {
              while (true)
              {
                int j;
                int k;
                int i3;
                while (true)
                {
                  localNextCombiningArgs.start = paramRecomposeArgs.start;
                  l = getNextCombining(localNextCombiningArgs, paramRecomposeArgs.limit, paramUnicodeSet);
                  i2 = localNextCombiningArgs.combiningIndex;
                  paramRecomposeArgs.start = localNextCombiningArgs.start;
                  if (((l & 0x80) == 0) || (i8 == -1))
                    break label856;
                  if ((i2 & 0x8000) == 0)
                    break;
                  if (((paramInt & 0x100) == 0) && (i6 != 0))
                    break label856;
                  i = -1;
                  l = 0;
                  localNextCombiningArgs.c2 = paramRecomposeArgs.source[i8];
                  if (i2 == 65522)
                  {
                    localNextCombiningArgs.c2 = (char)(localNextCombiningArgs.c2 - 4352);
                    if (localNextCombiningArgs.c2 < '\19')
                    {
                      i = paramRecomposeArgs.start - 1;
                      localNextCombiningArgs.c = (char)(44032 + (localNextCombiningArgs.c2 * '\21' + localNextCombiningArgs.c - 4449) * 28);
                      if (paramRecomposeArgs.start != paramRecomposeArgs.limit)
                        if ((localNextCombiningArgs.c2 = (char)(paramRecomposeArgs.source[paramRecomposeArgs.start] - 4519)) < '\28')
                        {
                          paramRecomposeArgs.start += 1;
                          tmp261_259 = localNextCombiningArgs;
                          tmp261_259.c = (char)(tmp261_259.c + localNextCombiningArgs.c2);
                        }
                      else
                        l = 64;
                      if (!(nx_contains(paramUnicodeSet, localNextCombiningArgs.c)))
                      {
                        paramRecomposeArgs.source[i8] = localNextCombiningArgs.c;
                      }
                      else
                      {
                        if (!(isHangulWithoutJamoT(localNextCombiningArgs.c)))
                          paramRecomposeArgs.start -= 1;
                        i = paramRecomposeArgs.start;
                      }
                    }
                  }
                  else if (isHangulWithoutJamoT(localNextCombiningArgs.c2))
                  {
                    tmp351_349 = localNextCombiningArgs;
                    tmp351_349.c2 = (char)(tmp351_349.c2 + localNextCombiningArgs.c - 4519);
                    if (!(nx_contains(paramUnicodeSet, localNextCombiningArgs.c2)))
                    {
                      i = paramRecomposeArgs.start - 1;
                      paramRecomposeArgs.source[i8] = localNextCombiningArgs.c2;
                    }
                  }
                  if (i != -1)
                  {
                    j = i;
                    k = paramRecomposeArgs.start;
                    while (k < paramRecomposeArgs.limit)
                      paramRecomposeArgs.source[(j++)] = paramRecomposeArgs.source[(k++)];
                    paramRecomposeArgs.start = i;
                    paramRecomposeArgs.limit = j;
                  }
                  localNextCombiningArgs.c2 = ';
                  if (l == 0)
                    break label856;
                  if (paramRecomposeArgs.start == paramRecomposeArgs.limit)
                    return (char)i6;
                  i1 = 65520;
                }
                if ((i1 & 0x8000) != 0)
                  break label856;
                if ((paramInt & 0x100) != 0)
                  if (i6 == localNextCombiningArgs.cc)
                    if (i6 != 0)
                      break label856;
                else
                  if ((i6 >= localNextCombiningArgs.cc) && (i6 != 0))
                    break label856;
                if ((0 == (i3 = combine(combiningTable, i1, i2, arrayOfInt))) || (nx_contains(paramUnicodeSet, (char)i4, (char)i5)))
                  break label856;
                i4 = arrayOfInt[0];
                i5 = arrayOfInt[1];
                int i = (localNextCombiningArgs.c2 == 0) ? paramRecomposeArgs.start - 1 : paramRecomposeArgs.start - 2;
                paramRecomposeArgs.source[i8] = (char)i4;
                if (i7 != 0)
                {
                  if (i5 != 0)
                  {
                    paramRecomposeArgs.source[(i8 + 1)] = (char)i5;
                  }
                  else
                  {
                    i7 = 0;
                    j = i8 + 1;
                    k = j + 1;
                    while (k < i)
                      paramRecomposeArgs.source[(j++)] = paramRecomposeArgs.source[(k++)];
                    --i;
                  }
                }
                else if (i5 != 0)
                {
                  i7 = 1;
                  ++i8;
                  j = i;
                  k = ++i;
                  while (i8 < j)
                    paramRecomposeArgs.source[(--k)] = paramRecomposeArgs.source[(--j)];
                  paramRecomposeArgs.source[i8] = (char)i5;
                  --i8;
                }
                if (i < paramRecomposeArgs.start)
                {
                  j = i;
                  k = paramRecomposeArgs.start;
                  while (k < paramRecomposeArgs.limit)
                    paramRecomposeArgs.source[(j++)] = paramRecomposeArgs.source[(k++)];
                  paramRecomposeArgs.start = i;
                  paramRecomposeArgs.limit = j;
                }
                if (paramRecomposeArgs.start == paramRecomposeArgs.limit)
                  return (char)i6;
                if (i3 <= 1)
                  break;
                i1 = getCombiningIndexFromStarter((char)i4, (char)i5);
              }
              i8 = -1;
            }
            label856: i6 = localNextCombiningArgs.cc;
            if (paramRecomposeArgs.start == paramRecomposeArgs.limit)
              return (char)i6;
            if (localNextCombiningArgs.cc != 0)
              break label940;
            if ((l & 0x40) == 0)
              break;
            if (localNextCombiningArgs.c2 == 0)
            {
              i7 = 0;
              i8 = paramRecomposeArgs.start - 1;
            }
            else
            {
              i7 = 0;
              i8 = paramRecomposeArgs.start - 2;
            }
            i1 = i2;
          }
          i8 = -1;
        }
      while ((paramInt & 0x2000) == 0);
      i8 = -1;
    }
  }

  private static int findPreviousStarter(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4, char paramChar)
  {
    long l;
    PrevArgs localPrevArgs = new PrevArgs(null);
    localPrevArgs.src = paramArrayOfChar;
    localPrevArgs.start = paramInt1;
    localPrevArgs.current = paramInt2;
    do
    {
      if (localPrevArgs.start >= localPrevArgs.current)
        break;
      l = getPrevNorm32(localPrevArgs, paramChar, paramInt3 | paramInt4);
    }
    while (!(isTrueStarter(l, paramInt3, paramInt4)));
    return localPrevArgs.current;
  }

  private static int findNextStarter(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4, char paramChar)
  {
    int j = 0xFF00 | paramInt3;
    DecomposeArgs localDecomposeArgs = new DecomposeArgs(null);
    while (true)
    {
      if (paramInt1 == paramInt2)
        break;
      char c1 = paramArrayOfChar[paramInt1];
      if (c1 < paramChar)
        break;
      long l = getNorm32(c1);
      if ((l & j) == 3412047222534045696L)
        break;
      if (isNorm32LeadSurrogate(l))
      {
        if (paramInt1 + 1 == paramInt2)
          break;
        if (!(UTF16.isTrailSurrogate(c2 = paramArrayOfChar[(paramInt1 + 1)])))
          break;
        l = getNorm32FromSurrogatePair(l, c2);
        if ((l & j) != 3412047703570382848L)
          break label118;
        break;
      }
      char c2 = ';
      if ((l & paramInt4) != 3412047222534045696L)
      {
        label118: int i = decompose(l, paramInt4, localDecomposeArgs);
        if ((localDecomposeArgs.cc == 0) && ((getNorm32(extraData, i, paramInt3) & paramInt3) == 3412047875369074688L))
          break;
      }
      paramInt1 += ((c2 == 0) ? 1 : 2);
    }
    return paramInt1;
  }

  private static char[] composePart(ComposePartArgs paramComposePartArgs, int paramInt1, char[] paramArrayOfChar, int paramInt2, int paramInt3, int paramInt4, UnicodeSet paramUnicodeSet)
  {
    boolean bool = (paramInt4 & 0x1000) != 0;
    int[] arrayOfInt = new int[1];
    char[] arrayOfChar = new char[(paramInt3 - paramInt1) * 20];
    while (true)
    {
      paramComposePartArgs.length = decompose(paramArrayOfChar, paramInt1, paramInt2, arrayOfChar, 0, arrayOfChar.length, bool, arrayOfInt, paramUnicodeSet);
      if (paramComposePartArgs.length <= arrayOfChar.length)
        break;
      arrayOfChar = new char[paramComposePartArgs.length];
    }
    int i = paramComposePartArgs.length;
    if (paramComposePartArgs.length >= 2)
    {
      RecomposeArgs localRecomposeArgs = new RecomposeArgs(null);
      localRecomposeArgs.source = arrayOfChar;
      localRecomposeArgs.start = 0;
      localRecomposeArgs.limit = i;
      paramComposePartArgs.prevCC = recompose(localRecomposeArgs, paramInt4, paramUnicodeSet);
      i = localRecomposeArgs.limit;
    }
    paramComposePartArgs.length = i;
    return arrayOfChar;
  }

  private static boolean composeHangul(char paramChar1, char paramChar2, long paramLong, char[] paramArrayOfChar1, int[] paramArrayOfInt, int paramInt1, boolean paramBoolean, char[] paramArrayOfChar2, int paramInt2, UnicodeSet paramUnicodeSet)
  {
    int i = paramArrayOfInt[0];
    if (isJamoVTNorm32JamoV(paramLong))
    {
      paramChar1 = (char)(paramChar1 - 4352);
      if (paramChar1 >= '\19')
        break label246;
      paramChar2 = (char)(44032 + (paramChar1 * '\21' + paramChar2 - 4449) * 28);
      if (i != paramInt1)
      {
        char c;
        int j = paramArrayOfChar1[i];
        if ((c = (char)(j - 4519)) < '\28')
        {
          ++i;
          paramChar2 = (char)(paramChar2 + c);
        }
        else if (paramBoolean)
        {
          paramLong = getNorm32(j);
          if ((isNorm32Regular(paramLong)) && ((paramLong & 0x8) != 3412039938269511680L))
          {
            DecomposeArgs localDecomposeArgs = new DecomposeArgs(null);
            int k = decompose(paramLong, 8, localDecomposeArgs);
            if (localDecomposeArgs.length == 1)
              if ((c = (char)(extraData[k] - 4519)) < '\28')
              {
                ++i;
                paramChar2 = (char)(paramChar2 + c);
              }
          }
        }
      }
      if (nx_contains(paramUnicodeSet, paramChar2))
      {
        if (!(isHangulWithoutJamoT(paramChar2)))
          --i;
        return false;
      }
      paramArrayOfChar2[paramInt2] = paramChar2;
      paramArrayOfInt[0] = i;
      return true;
    }
    if (isHangulWithoutJamoT(paramChar1))
    {
      paramChar2 = (char)(paramChar1 + paramChar2 - 4519);
      if (nx_contains(paramUnicodeSet, paramChar2))
        return false;
      paramArrayOfChar2[paramInt2] = paramChar2;
      paramArrayOfInt[0] = i;
      return true;
    }
    label246: return false;
  }

  public static int compose(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, int paramInt5, UnicodeSet paramUnicodeSet)
  {
    int i1;
    char c3;
    int[] arrayOfInt = new int[1];
    int i7 = paramInt3;
    int i8 = paramInt1;
    if ((paramInt5 & 0x1000) != 0)
    {
      c3 = (char)indexes[7];
      i1 = 34;
    }
    else
    {
      c3 = (char)indexes[6];
      i1 = 17;
    }
    int j = i8;
    int k = 0xFF00 | i1;
    int i2 = 0;
    int i6 = 0;
    long l = 3412047291253522432L;
    char c1 = ';
    while (true)
    {
      int i4;
      int i5;
      while (true)
      {
        char c2;
        while (true)
        {
          while (true)
          {
            int i;
            while (true)
            {
              i = i8;
              while (i8 != paramInt2)
              {
                if ((c1 = paramArrayOfChar1[i8]) >= c3)
                  if (((l = getNorm32(c1)) & k) != 3412040900342185984L)
                    break;
                i6 = 0;
                ++i8;
              }
              if (i8 != i)
              {
                int i3 = i8 - i;
                if (i7 + i3 <= paramInt4)
                  System.arraycopy(paramArrayOfChar1, i, paramArrayOfChar2, i7, i3);
                i7 += i3;
                i2 = i7;
                j = i8 - 1;
                if ((UTF16.isTrailSurrogate(paramArrayOfChar1[j])) && (i < j) && (UTF16.isLeadSurrogate(paramArrayOfChar1[(j - 1)])))
                  --j;
                i = i8;
              }
              if (i8 == paramInt2)
                break label722:
              ++i8;
              if (!(isNorm32HangulOrJamo(l)))
                break label334;
              i6 = i5 = 0;
              i2 = i7;
              arrayOfInt[0] = i8;
              if (i7 <= 0)
                break;
              if (!(composeHangul(paramArrayOfChar1[(i - 1)], c1, l, paramArrayOfChar1, arrayOfInt, paramInt2, ((paramInt5 & 0x1000) != 0) ? 1 : false, paramArrayOfChar2, (i7 <= paramInt4) ? i7 - 1 : 0, paramUnicodeSet)))
                break;
              i8 = arrayOfInt[0];
              j = i8;
            }
            i8 = arrayOfInt[0];
            c2 = ';
            i4 = 1;
            j = i;
            break;
            if (isNorm32Regular(l))
            {
              label334: c2 = ';
              i4 = 1;
            }
            else if (i8 != paramInt2)
            {
              if (UTF16.isTrailSurrogate(c2 = paramArrayOfChar1[i8]))
              {
                ++i8;
                i4 = 2;
                l = getNorm32FromSurrogatePair(l, c2);
              }
            }
            else
            {
              c2 = ';
              i4 = 1;
              l = 3412040539564933120L;
            }
            ComposePartArgs localComposePartArgs = new ComposePartArgs(null);
            if (nx_contains(paramUnicodeSet, c1, c2))
            {
              i5 = 0;
              break;
            }
            if ((l & i1) == 3412048459484626944L)
            {
              i5 = (int)(0xFF & l >> 8);
              break;
            }
            int i10 = i1 << 2 & 0xF;
            if (isTrueStarter(l, 0xFF00 | i1, i10))
              j = i;
            else
              i7 -= i - j;
            i8 = findNextStarter(paramArrayOfChar1, i8, paramInt2, i1, i10, c3);
            localComposePartArgs.prevCC = i6;
            localComposePartArgs.length = i4;
            char[] arrayOfChar = composePart(localComposePartArgs, j, paramArrayOfChar1, i8, paramInt2, paramInt5, paramUnicodeSet);
            if (arrayOfChar == null)
              break label722:
            i6 = localComposePartArgs.prevCC;
            i4 = localComposePartArgs.length;
            if (i7 + localComposePartArgs.length <= paramInt4)
            {
              int i11 = 0;
              while (i11 < localComposePartArgs.length)
              {
                paramArrayOfChar2[(i7++)] = arrayOfChar[(i11++)];
                --i4;
              }
            }
            else
            {
              i7 += i4;
            }
            j = i8;
          }
          if (i7 + i4 > paramInt4)
            break label708;
          if ((i5 == 0) || (i5 >= i6))
            break;
          int i9 = i7;
          i7 += i4;
          i6 = insertOrdered(paramArrayOfChar2, i2, i9, i7, c1, c2, i5);
        }
        paramArrayOfChar2[(i7++)] = c1;
        if (c2 != 0)
          paramArrayOfChar2[(i7++)] = c2;
        i6 = i5;
      }
      label708: i7 += i4;
      i6 = i5;
    }
    label722: return (i7 - paramInt3);
  }

  public static int getCombiningClass(int paramInt)
  {
    long l = getNorm32(paramInt);
    return (char)(int)(l >> 8 & 0xFF);
  }

  public static boolean isFullCompositionExclusion(int paramInt)
  {
    if (isFormatVersion_2_1)
    {
      int i = AuxTrieImpl.auxTrie.getCodePointValue(paramInt);
      return ((i & 0x400) != 0);
    }
    return false;
  }

  public static boolean isCanonSafeStart(int paramInt)
  {
    if (isFormatVersion_2_1)
    {
      int i = AuxTrieImpl.auxTrie.getCodePointValue(paramInt);
      return ((i & 0x800) == 0);
    }
    return false;
  }

  public static boolean isNFSkippable(int paramInt, NormalizerBase.Mode paramMode, long paramLong)
  {
    paramLong &= 4294967295L;
    long l = getNorm32(paramInt);
    if ((l & paramLong) != 3412046810217185280L)
      return false;
    if ((paramMode == NormalizerBase.NFD) || (paramMode == NormalizerBase.NFKD) || (paramMode == NormalizerBase.NONE))
      return true;
    if ((l & 0x4) == 3412046810217185280L)
      return true;
    if (isNorm32HangulOrJamo(l))
      return (!(isHangulWithoutJamoT((char)paramInt)));
    if (!(isFormatVersion_2_2))
      return false;
    int i = AuxTrieImpl.auxTrie.getCodePointValue(paramInt);
    return ((i & 0x1000) == 3412047102274961408L);
  }

  public static UnicodeSet addPropertyStarts(UnicodeSet paramUnicodeSet)
  {
    TrieIterator localTrieIterator1 = new TrieIterator(NormTrieImpl.normTrie);
    RangeValueIterator.Element localElement1 = new RangeValueIterator.Element();
    while (localTrieIterator1.next(localElement1))
      paramUnicodeSet.add(localElement1.start);
    TrieIterator localTrieIterator2 = new TrieIterator(FCDTrieImpl.fcdTrie);
    RangeValueIterator.Element localElement2 = new RangeValueIterator.Element();
    while (localTrieIterator2.next(localElement2))
      paramUnicodeSet.add(localElement2.start);
    if (isFormatVersion_2_1)
    {
      TrieIterator localTrieIterator3 = new TrieIterator(AuxTrieImpl.auxTrie);
      RangeValueIterator.Element localElement3 = new RangeValueIterator.Element();
      while (localTrieIterator3.next(localElement3))
        paramUnicodeSet.add(localElement3.start);
    }
    for (int i = 44032; i < 55204; i += 28)
    {
      paramUnicodeSet.add(i);
      paramUnicodeSet.add(i + 1);
    }
    paramUnicodeSet.add(55204);
    return paramUnicodeSet;
  }

  public static final int quickCheck(int paramInt1, int paramInt2)
  {
    int[] arrayOfInt = { 0, 0, 4, 8, 17, 34 };
    int i = (int)getNorm32(paramInt1) & arrayOfInt[paramInt2];
    if (i == 0)
      return 1;
    if ((i & 0xF) != 0)
      return 0;
    return 2;
  }

  private static int strCompare(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, boolean paramBoolean)
  {
    int i1;
    int i2;
    int i3;
    int i4;
    int i7;
    int i = paramInt1;
    int j = paramInt3;
    int i5 = paramInt2 - paramInt1;
    int i6 = paramInt4 - paramInt3;
    if (i5 < i6)
    {
      i7 = -1;
      k = i + i5;
    }
    else if (i5 == i6)
    {
      i7 = 0;
      k = i + i5;
    }
    else
    {
      i7 = 1;
      k = i + i6;
    }
    if (paramArrayOfChar1 == paramArrayOfChar2)
      return i7;
    while (true)
    {
      if (paramInt1 == k)
        return i7;
      i1 = paramArrayOfChar1[paramInt1];
      i3 = paramArrayOfChar2[paramInt3];
      if (i1 != i3)
        break;
      ++paramInt1;
      ++paramInt3;
    }
    int k = i + i5;
    int l = j + i6;
    if ((i1 >= 55296) && (i3 >= 55296) && (paramBoolean))
    {
      if ((i1 > 56319) || (paramInt1 + 1 == k) || (!(UTF16.isTrailSurrogate(paramArrayOfChar1[(paramInt1 + 1)]))))
      {
        if ((UTF16.isTrailSurrogate(i1)) && (i != paramInt1) && (UTF16.isLeadSurrogate(paramArrayOfChar1[(paramInt1 - 1)])))
          break label212:
        i2 = (char)(i1 - 10240);
      }
      if ((i3 > 56319) || (paramInt3 + 1 == l) || (!(UTF16.isTrailSurrogate(paramArrayOfChar2[(paramInt3 + 1)]))))
      {
        if ((UTF16.isTrailSurrogate(i3)) && (j != paramInt3) && (UTF16.isLeadSurrogate(paramArrayOfChar2[(paramInt3 - 1)])))
          label212: break label279:
        i4 = (char)(i3 - 10240);
      }
    }
    label279: return (i2 - i4);
  }

  private static final synchronized UnicodeSet internalGetNXHangul()
  {
    if (nxCache[1] == null)
      nxCache[1] = new UnicodeSet(44032, 55203);
    return nxCache[1];
  }

  private static final synchronized UnicodeSet internalGetNXCJKCompat()
  {
    if (nxCache[2] == null)
    {
      UnicodeSet localUnicodeSet1 = new UnicodeSet("[:Ideographic:]");
      UnicodeSet localUnicodeSet2 = new UnicodeSet();
      UnicodeSetIterator localUnicodeSetIterator = new UnicodeSetIterator(localUnicodeSet1);
      if ((localUnicodeSetIterator.nextRange()) && (localUnicodeSetIterator.codepoint != UnicodeSetIterator.IS_STRING))
      {
        int i = localUnicodeSetIterator.codepoint;
        int j = localUnicodeSetIterator.codepointEnd;
        while (true)
        {
          if (i > j);
          long l = getNorm32(i);
          if ((l & 0x4) > 3412048081527504896L)
            localUnicodeSet2.add(i);
          ++i;
        }
      }
      nxCache[2] = localUnicodeSet2;
    }
    return nxCache[2];
  }

  private static final synchronized UnicodeSet internalGetNXUnicode(int paramInt)
  {
    paramInt &= 224;
    if (paramInt == 0)
      return null;
    if (nxCache[paramInt] == null)
    {
      UnicodeSet localUnicodeSet = new UnicodeSet();
      switch (paramInt)
      {
      case 32:
        localUnicodeSet.applyPattern("[:^Age=3.2:]");
        break;
      default:
        return null;
      }
      nxCache[paramInt] = localUnicodeSet;
    }
    return nxCache[paramInt];
  }

  private static final synchronized UnicodeSet internalGetNX(int paramInt)
  {
    paramInt &= 255;
    if (nxCache[paramInt] == null)
    {
      UnicodeSet localUnicodeSet2;
      if (paramInt == 1)
        return internalGetNXHangul();
      if (paramInt == 2)
        return internalGetNXCJKCompat();
      if (((paramInt & 0xE0) != 0) && ((paramInt & 0x1F) == 0))
        return internalGetNXUnicode(paramInt);
      UnicodeSet localUnicodeSet1 = new UnicodeSet();
      if ((paramInt & 0x1) != 0)
        if (null != (localUnicodeSet2 = internalGetNXHangul()))
          localUnicodeSet1.addAll(localUnicodeSet2);
      if ((paramInt & 0x2) != 0)
        if (null != (localUnicodeSet2 = internalGetNXCJKCompat()))
          localUnicodeSet1.addAll(localUnicodeSet2);
      if ((paramInt & 0xE0) != 0)
        if (null != (localUnicodeSet2 = internalGetNXUnicode(paramInt)))
          localUnicodeSet1.addAll(localUnicodeSet2);
      nxCache[paramInt] = localUnicodeSet1;
    }
    return nxCache[paramInt];
  }

  public static final UnicodeSet getNX(int paramInt)
  {
    if ((paramInt &= 255) == 0)
      return null;
    return internalGetNX(paramInt);
  }

  private static final boolean nx_contains(UnicodeSet paramUnicodeSet, int paramInt)
  {
    return ((paramUnicodeSet != null) && (paramUnicodeSet.contains(paramInt)));
  }

  private static final boolean nx_contains(UnicodeSet paramUnicodeSet, char paramChar1, char paramChar2)
  {
    if (paramUnicodeSet != null);
    return (paramUnicodeSet.contains((paramChar2 == 0) ? paramChar1 : UCharacterProperty.getRawSupplementary(paramChar1, paramChar2)));
  }

  public static int getDecompose(int[] paramArrayOfInt, String[] paramArrayOfString)
  {
    DecomposeArgs localDecomposeArgs = new DecomposeArgs(null);
    int i = 0;
    long l = 3412047291253522432L;
    int j = -1;
    int k = 0;
    int i1 = 0;
    while (true)
    {
      do
      {
        if (++j >= 195102)
          break label138;
        if (j == 12543)
          j = 63744;
        else if (j == 65536)
          j = 119134;
        else if (j == 119233)
          j = 194560;
        l = getNorm32(j);
      }
      while (((l & 0x4) == 3412047377152868352L) || (i1 >= paramArrayOfInt.length));
      paramArrayOfInt[i1] = j;
      k = decompose(l, localDecomposeArgs);
      paramArrayOfString[(i1++)] = new String(extraData, k, localDecomposeArgs.length);
    }
    label138: return i1;
  }

  private static boolean needSingleQuotation(char paramChar)
  {
    return (((paramChar >= '\t') && (paramChar <= '\r')) || ((paramChar >= ' ') && (paramChar <= '/')) || ((paramChar >= ':') && (paramChar <= '@')) || ((paramChar >= '[') && (paramChar <= '`')) || ((paramChar >= '{') && (paramChar <= '~')));
  }

  public static String canonicalDecomposeWithSingleQuotation(String paramString)
  {
    int i9;
    char[] arrayOfChar1 = paramString.toCharArray();
    int i = 0;
    int j = arrayOfChar1.length;
    Object localObject1 = new char[arrayOfChar1.length * 3];
    Object localObject2 = 0;
    int k = localObject1.length;
    char[] arrayOfChar2 = new char[3];
    int i2 = 4;
    int i6 = (char)indexes[8];
    int i1 = 0xFF00 | i2;
    int i3 = 0;
    int i8 = 0;
    long l1 = 3412047291253522432L;
    int i5 = 0;
    int i10 = 0;
    int i7 = i9 = -1;
    while (true)
    {
      do
      {
        int i4;
        char c1;
        char c2;
        char[] arrayOfChar3;
        int l = i;
        while (i != j)
        {
          if ((i5 = arrayOfChar1[i]) >= i6)
            if ((((l1 = getNorm32(i5)) & i1) != 3412039903909773312L) && (((i5 < 44032) || (i5 > 55203))))
              break;
          i8 = 0;
          ++i;
        }
        if (i != l)
        {
          i4 = i - l;
          if (localObject2 + i4 <= k)
            System.arraycopy(arrayOfChar1, l, localObject1, localObject2, i4);
          localObject2 += i4;
          i3 = localObject2;
        }
        if (i == j)
          break label672:
        ++i;
        if (isNorm32Regular(l1))
        {
          c2 = ';
          i4 = 1;
        }
        else if (i != j)
        {
          if (Character.isLowSurrogate(c2 = arrayOfChar1[i]))
          {
            ++i;
            i4 = 2;
            l1 = getNorm32FromSurrogatePair(l1, c2);
          }
        }
        else
        {
          c2 = ';
          i4 = 1;
          l1 = 3412039800830558208L;
        }
        if ((l1 & i2) == 3412047737930121216L)
        {
          i7 = i9 = (int)(0xFF & l1 >> 8);
          arrayOfChar3 = null;
          i10 = -1;
        }
        else
        {
          localObject3 = new DecomposeArgs(null);
          i10 = decompose(l1, i2, (DecomposeArgs)localObject3);
          arrayOfChar3 = extraData;
          i4 = ((DecomposeArgs)localObject3).length;
          i7 = ((DecomposeArgs)localObject3).cc;
          i9 = ((DecomposeArgs)localObject3).trailCC;
          if (i4 == 1)
          {
            c1 = arrayOfChar3[i10];
            c2 = ';
            arrayOfChar3 = null;
            i10 = -1;
          }
        }
        if (localObject2 + i4 * 3 >= k)
        {
          localObject3 = new char[k * 2];
          System.arraycopy(localObject1, 0, localObject3, 0, localObject2);
          localObject1 = localObject3;
          k = localObject1.length;
        }
        Object localObject3 = localObject2;
        if (arrayOfChar3 == null)
        {
          if (needSingleQuotation(c1))
          {
            localObject1[(localObject2++)] = 39;
            localObject1[(localObject2++)] = c1;
            localObject1[(localObject2++)] = 39;
            i9 = 0;
          }
          else if ((i7 != 0) && (i7 < i8))
          {
            localObject2 += i4;
            i9 = insertOrdered(localObject1, i3, localObject3, localObject2, c1, c2, i7);
          }
          else
          {
            localObject1[(localObject2++)] = c1;
            if (c2 != 0)
              localObject1[(localObject2++)] = c2;
          }
        }
        else if (needSingleQuotation(arrayOfChar3[i10]))
        {
          localObject1[(localObject2++)] = 39;
          localObject1[(localObject2++)] = arrayOfChar3[(i10++)];
          localObject1[(localObject2++)] = 39;
          --i4;
          do
            localObject1[(localObject2++)] = arrayOfChar3[(i10++)];
          while (--i4 > 0);
        }
        else if ((i7 != 0) && (i7 < i8))
        {
          localObject2 += i4;
          i9 = mergeOrdered(localObject1, i3, localObject3, arrayOfChar3, i10, i10 + i4);
        }
        else
        {
          do
            localObject1[(localObject2++)] = arrayOfChar3[(i10++)];
          while (--i4 > 0);
        }
        i8 = i9;
      }
      while (i8 != 0);
      i3 = localObject2;
    }
    label672: return ((String)(String)(String)new String(localObject1, 0, localObject2));
  }

  public static String convert(String paramString)
  {
    if (paramString == null)
      return null;
    int i = -1;
    StringBuffer localStringBuffer = new StringBuffer();
    UCharacterIterator localUCharacterIterator = UCharacterIterator.getInstance(paramString);
    while (true)
    {
      while (true)
      {
        while (true)
        {
          while (true)
          {
            while (true)
            {
              while (true)
              {
                if ((i = localUCharacterIterator.nextCodePoint()) == -1)
                  break label158;
                switch (i)
                {
                case 194664:
                  localStringBuffer.append(corrigendum4MappingTable[0]);
                case 194676:
                case 194847:
                case 194911:
                case 195007:
                }
              }
              localStringBuffer.append(corrigendum4MappingTable[1]);
            }
            localStringBuffer.append(corrigendum4MappingTable[2]);
          }
          localStringBuffer.append(corrigendum4MappingTable[3]);
        }
        localStringBuffer.append(corrigendum4MappingTable[4]);
      }
      UTF16.append(localStringBuffer, i);
    }
    label158: return localStringBuffer.toString();
  }

  static
  {
    try
    {
      IMPL = new NormalizerImpl();
    }
    catch (Exception localException)
    {
      throw new RuntimeException(localException.getMessage());
    }
    nxCache = new UnicodeSet[256];
    corrigendum4MappingTable = { { 55364, 57194 }, { 24371 }, { 17323 }, { 31406 }, { 19799 } };
  }

  static final class AuxTrieImpl
  implements Trie.DataManipulate
  {
    static CharTrie auxTrie = null;

    public int getFoldingOffset(int paramInt)
    {
      return ((paramInt & 0x3FF) << 5);
    }
  }

  private static final class ComposePartArgs
  {
    int prevCC;
    int length;
  }

  private static final class DecomposeArgs
  {
    int cc;
    int trailCC;
    int length;
  }

  static final class FCDTrieImpl
  implements Trie.DataManipulate
  {
    static CharTrie fcdTrie = null;

    public int getFoldingOffset(int paramInt)
    {
      return paramInt;
    }
  }

  private static final class NextCCArgs
  {
    char[] source;
    int next;
    int limit;
    char c;
    char c2;
  }

  private static final class NextCombiningArgs
  {
    char[] source;
    int start;
    char c;
    char c2;
    int combiningIndex;
    char cc;
  }

  static final class NormTrieImpl
  implements Trie.DataManipulate
  {
    static IntTrie normTrie = null;

    public int getFoldingOffset(int paramInt)
    {
      return (2048 + (paramInt >> 11 & 0x7FE0));
    }
  }

  private static final class PrevArgs
  {
    char[] src;
    int start;
    int current;
    char c;
    char c2;
  }

  private static final class RecomposeArgs
  {
    char[] source;
    int start;
    int limit;
  }
}