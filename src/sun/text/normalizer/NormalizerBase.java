package sun.text.normalizer;

import java.text.CharacterIterator;
import java.text.Normalizer.Form;

public final class NormalizerBase
  implements Cloneable
{
  private char[] buffer;
  private int bufferStart;
  private int bufferPos;
  private int bufferLimit;
  private UCharacterIterator text;
  private Mode mode;
  private int options;
  private int currentIndex;
  private int nextIndex;
  public static final int UNICODE_3_2 = 32;
  public static final int DONE = -1;
  public static final Mode NONE = new Mode(1, null);
  public static final Mode NFD = new NFDMode(2, null);
  public static final Mode NFKD = new NFKDMode(3, null);
  public static final Mode NFC = new NFCMode(4, null);
  public static final Mode NFKC = new NFKCMode(5, null);
  public static final QuickCheckResult NO = new QuickCheckResult(0, null);
  public static final QuickCheckResult YES = new QuickCheckResult(1, null);
  public static final QuickCheckResult MAYBE = new QuickCheckResult(2, null);
  private static final int MAX_BUF_SIZE_COMPOSE = 2;
  private static final int MAX_BUF_SIZE_DECOMPOSE = 3;
  public static final int UNICODE_3_2_0_ORIGINAL = 262432;
  public static final int UNICODE_LATEST = 0;

  public NormalizerBase(String paramString, Mode paramMode, int paramInt)
  {
    this.buffer = new char[100];
    this.bufferStart = 0;
    this.bufferPos = 0;
    this.bufferLimit = 0;
    this.mode = NFC;
    this.options = 0;
    this.text = UCharacterIterator.getInstance(paramString);
    this.mode = paramMode;
    this.options = paramInt;
  }

  public NormalizerBase(CharacterIterator paramCharacterIterator, Mode paramMode)
  {
    this(paramCharacterIterator, paramMode, 0);
  }

  public NormalizerBase(CharacterIterator paramCharacterIterator, Mode paramMode, int paramInt)
  {
    this.buffer = new char[100];
    this.bufferStart = 0;
    this.bufferPos = 0;
    this.bufferLimit = 0;
    this.mode = NFC;
    this.options = 0;
    this.text = UCharacterIterator.getInstance((CharacterIterator)paramCharacterIterator.clone());
    this.mode = paramMode;
    this.options = paramInt;
  }

  public Object clone()
  {
    NormalizerBase localNormalizerBase;
    try
    {
      localNormalizerBase = (NormalizerBase)super.clone();
      localNormalizerBase.text = ((UCharacterIterator)this.text.clone());
      if (this.buffer != null)
      {
        localNormalizerBase.buffer = new char[this.buffer.length];
        System.arraycopy(this.buffer, 0, localNormalizerBase.buffer, 0, this.buffer.length);
      }
      return localNormalizerBase;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      throw new InternalError(localCloneNotSupportedException.toString());
    }
  }

  public static String compose(String paramString, boolean paramBoolean, int paramInt)
  {
    char[] arrayOfChar1;
    char[] arrayOfChar2;
    if (paramInt == 262432)
    {
      String str = NormalizerImpl.convert(paramString);
      arrayOfChar1 = new char[str.length() * 2];
      arrayOfChar2 = str.toCharArray();
    }
    else
    {
      arrayOfChar1 = new char[paramString.length() * 2];
      arrayOfChar2 = paramString.toCharArray();
    }
    int i = 0;
    UnicodeSet localUnicodeSet = NormalizerImpl.getNX(paramInt);
    paramInt &= -12544;
    if (paramBoolean)
      paramInt |= 4096;
    while (true)
    {
      i = NormalizerImpl.compose(arrayOfChar2, 0, arrayOfChar2.length, arrayOfChar1, 0, arrayOfChar1.length, paramInt, localUnicodeSet);
      if (i <= arrayOfChar1.length)
        return new String(arrayOfChar1, 0, i);
      arrayOfChar1 = new char[i];
    }
  }

  public static String decompose(String paramString, boolean paramBoolean)
  {
    return decompose(paramString, paramBoolean, 0);
  }

  public static String decompose(String paramString, boolean paramBoolean, int paramInt)
  {
    int[] arrayOfInt = new int[1];
    int i = 0;
    UnicodeSet localUnicodeSet = NormalizerImpl.getNX(paramInt);
    if (paramInt == 262432)
    {
      String str = NormalizerImpl.convert(paramString);
      arrayOfChar = new char[str.length() * 3];
      while (true)
      {
        i = NormalizerImpl.decompose(str.toCharArray(), 0, str.length(), arrayOfChar, 0, arrayOfChar.length, paramBoolean, arrayOfInt, localUnicodeSet);
        if (i <= arrayOfChar.length)
          return new String(arrayOfChar, 0, i);
        arrayOfChar = new char[i];
      }
    }
    char[] arrayOfChar = new char[paramString.length() * 3];
    while (true)
    {
      i = NormalizerImpl.decompose(paramString.toCharArray(), 0, paramString.length(), arrayOfChar, 0, arrayOfChar.length, paramBoolean, arrayOfInt, localUnicodeSet);
      if (i <= arrayOfChar.length)
        return new String(arrayOfChar, 0, i);
      arrayOfChar = new char[i];
    }
  }

  public static int normalize(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, Mode paramMode, int paramInt5)
  {
    int i = paramMode.normalize(paramArrayOfChar1, paramInt1, paramInt2, paramArrayOfChar2, paramInt3, paramInt4, paramInt5);
    if (i <= paramInt4 - paramInt3)
      return i;
    throw new IndexOutOfBoundsException(Integer.toString(i));
  }

  public int current()
  {
    if ((this.bufferPos < this.bufferLimit) || (nextNormalize()))
      return getCodePointAt(this.bufferPos);
    return -1;
  }

  public int next()
  {
    if ((this.bufferPos < this.bufferLimit) || (nextNormalize()))
    {
      int i = getCodePointAt(this.bufferPos);
      this.bufferPos += ((i > 65535) ? 2 : 1);
      return i;
    }
    return -1;
  }

  public int previous()
  {
    if ((this.bufferPos > 0) || (previousNormalize()))
    {
      int i = getCodePointAt(this.bufferPos - 1);
      this.bufferPos -= ((i > 65535) ? 2 : 1);
      return i;
    }
    return -1;
  }

  public void reset()
  {
    this.text.setIndex(0);
    this.currentIndex = (this.nextIndex = 0);
    clearBuffer();
  }

  public void setIndexOnly(int paramInt)
  {
    this.text.setIndex(paramInt);
    this.currentIndex = (this.nextIndex = paramInt);
    clearBuffer();
  }

  /**
   * @deprecated
   */
  public int setIndex(int paramInt)
  {
    setIndexOnly(paramInt);
    return current();
  }

  /**
   * @deprecated
   */
  public int getBeginIndex()
  {
    return 0;
  }

  /**
   * @deprecated
   */
  public int getEndIndex()
  {
    return endIndex();
  }

  public int getIndex()
  {
    if (this.bufferPos < this.bufferLimit)
      return this.currentIndex;
    return this.nextIndex;
  }

  public int endIndex()
  {
    return this.text.getLength();
  }

  public void setMode(Mode paramMode)
  {
    this.mode = paramMode;
  }

  public Mode getMode()
  {
    return this.mode;
  }

  public void setText(String paramString)
  {
    UCharacterIterator localUCharacterIterator = UCharacterIterator.getInstance(paramString);
    if (localUCharacterIterator == null)
      throw new InternalError("Could not create a new UCharacterIterator");
    this.text = localUCharacterIterator;
    reset();
  }

  public void setText(CharacterIterator paramCharacterIterator)
  {
    UCharacterIterator localUCharacterIterator = UCharacterIterator.getInstance(paramCharacterIterator);
    if (localUCharacterIterator == null)
      throw new InternalError("Could not create a new UCharacterIterator");
    this.text = localUCharacterIterator;
    this.currentIndex = (this.nextIndex = 0);
    clearBuffer();
  }

  private static long getPrevNorm32(UCharacterIterator paramUCharacterIterator, int paramInt1, int paramInt2, char[] paramArrayOfChar)
  {
    int i = 0;
    if ((i = paramUCharacterIterator.previous()) == -1)
      return 3412047325613260800L;
    paramArrayOfChar[0] = (char)i;
    paramArrayOfChar[1] = ';
    if (paramArrayOfChar[0] < paramInt1)
      return 3412047325613260800L;
    if (!(UTF16.isSurrogate(paramArrayOfChar[0])))
      return NormalizerImpl.getNorm32(paramArrayOfChar[0]);
    if ((UTF16.isLeadSurrogate(paramArrayOfChar[0])) || (paramUCharacterIterator.getIndex() == 0))
    {
      paramArrayOfChar[1] = (char)paramUCharacterIterator.current();
      return 3412047325613260800L;
    }
    if (UTF16.isLeadSurrogate(paramArrayOfChar[1] = (char)paramUCharacterIterator.previous()))
    {
      long l = NormalizerImpl.getNorm32(paramArrayOfChar[1]);
      if ((l & paramInt2) == 3412047033555484672L)
        return 3412047823829467136L;
      return NormalizerImpl.getNorm32FromSurrogatePair(l, paramArrayOfChar[0]);
    }
    paramUCharacterIterator.moveIndex(1);
    return 3412046827397054464L;
  }

  private static int findPreviousIterationBoundary(UCharacterIterator paramUCharacterIterator, IsPrevBoundary paramIsPrevBoundary, int paramInt1, int paramInt2, char[] paramArrayOfChar, int[] paramArrayOfInt)
  {
    boolean bool;
    char[] arrayOfChar1 = new char[2];
    paramArrayOfInt[0] = paramArrayOfChar.length;
    arrayOfChar1[0] = ';
    do
    {
      if ((paramUCharacterIterator.getIndex() <= 0) || (arrayOfChar1[0] == '￿'))
        break;
      bool = paramIsPrevBoundary.isPrevBoundary(paramUCharacterIterator, paramInt1, paramInt2, arrayOfChar1);
      if (paramArrayOfInt[0] < ((arrayOfChar1[1] == 0) ? 1 : 2))
      {
        char[] arrayOfChar2 = new char[paramArrayOfChar.length * 2];
        System.arraycopy(paramArrayOfChar, paramArrayOfInt[0], arrayOfChar2, arrayOfChar2.length - paramArrayOfChar.length - paramArrayOfInt[0], paramArrayOfChar.length - paramArrayOfInt[0]);
        paramArrayOfInt[0] += arrayOfChar2.length - paramArrayOfChar.length;
        paramArrayOfChar = arrayOfChar2;
        arrayOfChar2 = null;
      }
      paramArrayOfChar[(paramArrayOfInt[0] -= 1)] = arrayOfChar1[0];
      if (arrayOfChar1[1] != 0)
        paramArrayOfChar[(paramArrayOfInt[0] -= 1)] = arrayOfChar1[1];
    }
    while (!(bool));
    return (paramArrayOfChar.length - paramArrayOfInt[0]);
  }

  private static int previous(UCharacterIterator paramUCharacterIterator, char[] paramArrayOfChar, int paramInt1, int paramInt2, Mode paramMode, boolean paramBoolean, boolean[] paramArrayOfBoolean, int paramInt3)
  {
    int i3 = paramInt2 - paramInt1;
    int i = 0;
    if (paramArrayOfBoolean != null)
      paramArrayOfBoolean[0] = false;
    int i2 = (char)paramMode.getMinC();
    int k = paramMode.getMask();
    IsPrevBoundary localIsPrevBoundary = paramMode.getPrevBoundary();
    if (localIsPrevBoundary == null)
    {
      int l;
      i = 0;
      if ((l = paramUCharacterIterator.previous()) >= 0)
      {
        i = 1;
        if (UTF16.isTrailSurrogate((char)l))
        {
          int i1 = paramUCharacterIterator.previous();
          if (i1 != -1)
            if (UTF16.isLeadSurrogate((char)i1))
            {
              if (i3 >= 2)
              {
                paramArrayOfChar[1] = (char)l;
                i = 2;
              }
              l = i1;
            }
            else
            {
              paramUCharacterIterator.moveIndex(1);
            }
        }
        if (i3 > 0)
          paramArrayOfChar[0] = (char)l;
      }
      return i;
    }
    char[] arrayOfChar = new char[100];
    int[] arrayOfInt = new int[1];
    int j = findPreviousIterationBoundary(paramUCharacterIterator, localIsPrevBoundary, i2, k, arrayOfChar, arrayOfInt);
    if (j > 0)
      if (paramBoolean)
      {
        i = normalize(arrayOfChar, arrayOfInt[0], arrayOfInt[0] + j, paramArrayOfChar, paramInt1, paramInt2, paramMode, paramInt3);
        if (paramArrayOfBoolean != null)
          paramArrayOfBoolean[0] = (((i != j) || (Utility.arrayRegionMatches(arrayOfChar, 0, paramArrayOfChar, paramInt1, paramInt2))) ? 1 : false);
      }
      else if (i3 > 0)
      {
        System.arraycopy(arrayOfChar, arrayOfInt[0], paramArrayOfChar, 0, (j < i3) ? j : i3);
      }
    return i;
  }

  private static long getNextNorm32(UCharacterIterator paramUCharacterIterator, int paramInt1, int paramInt2, int[] paramArrayOfInt)
  {
    paramArrayOfInt[0] = paramUCharacterIterator.next();
    paramArrayOfInt[1] = 0;
    if (paramArrayOfInt[0] < paramInt1)
      return 3412047325613260800L;
    long l = NormalizerImpl.getNorm32((char)paramArrayOfInt[0]);
    if (UTF16.isLeadSurrogate((char)paramArrayOfInt[0]))
    {
      if (paramUCharacterIterator.current() != -1)
        if (UTF16.isTrailSurrogate((char)(paramArrayOfInt[1] = paramUCharacterIterator.current())))
        {
          paramUCharacterIterator.moveIndex(1);
          if ((l & paramInt2) == 3412048029987897344L)
            return 3412039886729904128L;
          return NormalizerImpl.getNorm32FromSurrogatePair(l, (char)paramArrayOfInt[1]);
        }
      return 3412047325613260800L;
    }
    return l;
  }

  private static int findNextIterationBoundary(UCharacterIterator paramUCharacterIterator, IsNextBoundary paramIsNextBoundary, int paramInt1, int paramInt2, char[] paramArrayOfChar)
  {
    if (paramUCharacterIterator.current() == -1)
      return 0;
    int[] arrayOfInt = new int[2];
    arrayOfInt[0] = paramUCharacterIterator.next();
    paramArrayOfChar[0] = (char)arrayOfInt[0];
    int i = 1;
    if ((UTF16.isLeadSurrogate((char)arrayOfInt[0])) && (paramUCharacterIterator.current() != -1))
      if (UTF16.isTrailSurrogate((char)(arrayOfInt[1] = paramUCharacterIterator.next())))
        paramArrayOfChar[(i++)] = (char)arrayOfInt[1];
      else
        paramUCharacterIterator.moveIndex(-1);
    while (true)
    {
      while (true)
      {
        do
        {
          if (paramUCharacterIterator.current() == -1)
            break label252;
          if (paramIsNextBoundary.isNextBoundary(paramUCharacterIterator, paramInt1, paramInt2, arrayOfInt))
          {
            paramUCharacterIterator.moveIndex((arrayOfInt[1] == 0) ? -1 : -2);
            break label252:
          }
          if (i + ((arrayOfInt[1] == 0) ? 1 : 2) > paramArrayOfChar.length)
            break label192;
          paramArrayOfChar[(i++)] = (char)arrayOfInt[0];
        }
        while (arrayOfInt[1] == 0);
        paramArrayOfChar[(i++)] = (char)arrayOfInt[1];
      }
      label192: char[] arrayOfChar = new char[paramArrayOfChar.length * 2];
      System.arraycopy(paramArrayOfChar, 0, arrayOfChar, 0, i);
      paramArrayOfChar = arrayOfChar;
      paramArrayOfChar[(i++)] = (char)arrayOfInt[0];
      if (arrayOfInt[1] != 0)
        paramArrayOfChar[(i++)] = (char)arrayOfInt[1];
    }
    label252: return i;
  }

  private static int next(UCharacterIterator paramUCharacterIterator, char[] paramArrayOfChar, int paramInt1, int paramInt2, Mode paramMode, boolean paramBoolean, boolean[] paramArrayOfBoolean, int paramInt3)
  {
    int i2 = paramInt2 - paramInt1;
    int i3 = 0;
    if (paramArrayOfBoolean != null)
      paramArrayOfBoolean[0] = false;
    int i1 = (char)paramMode.getMinC();
    int i = paramMode.getMask();
    IsNextBoundary localIsNextBoundary = paramMode.getNextBoundary();
    if (localIsNextBoundary == null)
    {
      i3 = 0;
      int k = paramUCharacterIterator.next();
      if (k != -1)
      {
        i3 = 1;
        if (UTF16.isLeadSurrogate((char)k))
        {
          int l = paramUCharacterIterator.next();
          if (l != -1)
            if (UTF16.isTrailSurrogate((char)l))
              if (i2 >= 2)
              {
                paramArrayOfChar[1] = (char)l;
                i3 = 2;
              }
            else
              paramUCharacterIterator.moveIndex(-1);
        }
        if (i2 > 0)
          paramArrayOfChar[0] = (char)k;
      }
      return i3;
    }
    char[] arrayOfChar = new char[100];
    int[] arrayOfInt = new int[1];
    int j = findNextIterationBoundary(paramUCharacterIterator, localIsNextBoundary, i1, i, arrayOfChar);
    if (j > 0)
      if (paramBoolean)
      {
        i3 = paramMode.normalize(arrayOfChar, arrayOfInt[0], j, paramArrayOfChar, paramInt1, paramInt2, paramInt3);
        if (paramArrayOfBoolean != null)
          paramArrayOfBoolean[0] = (((i3 != j) || (Utility.arrayRegionMatches(arrayOfChar, arrayOfInt[0], paramArrayOfChar, paramInt1, i3))) ? 1 : false);
      }
      else if (i2 > 0)
      {
        System.arraycopy(arrayOfChar, 0, paramArrayOfChar, paramInt1, Math.min(j, i2));
      }
    return i3;
  }

  private void clearBuffer()
  {
    this.bufferLimit = (this.bufferStart = this.bufferPos = 0);
  }

  private boolean nextNormalize()
  {
    clearBuffer();
    this.currentIndex = this.nextIndex;
    this.text.setIndex(this.nextIndex);
    this.bufferLimit = next(this.text, this.buffer, this.bufferStart, this.buffer.length, this.mode, true, null, this.options);
    this.nextIndex = this.text.getIndex();
    return (this.bufferLimit > 0);
  }

  private boolean previousNormalize()
  {
    clearBuffer();
    this.nextIndex = this.currentIndex;
    this.text.setIndex(this.currentIndex);
    this.bufferLimit = previous(this.text, this.buffer, this.bufferStart, this.buffer.length, this.mode, true, null, this.options);
    this.currentIndex = this.text.getIndex();
    this.bufferPos = this.bufferLimit;
    return (this.bufferLimit > 0);
  }

  private int getCodePointAt(int paramInt)
  {
    if (UTF16.isSurrogate(this.buffer[paramInt]))
    {
      if (UTF16.isLeadSurrogate(this.buffer[paramInt]))
      {
        if ((paramInt + 1 >= this.bufferLimit) || (!(UTF16.isTrailSurrogate(this.buffer[(paramInt + 1)]))))
          break label114;
        return UCharacterProperty.getRawSupplementary(this.buffer[paramInt], this.buffer[(paramInt + 1)]);
      }
      if ((UTF16.isTrailSurrogate(this.buffer[paramInt])) && (paramInt > 0) && (UTF16.isLeadSurrogate(this.buffer[(paramInt - 1)])))
        return UCharacterProperty.getRawSupplementary(this.buffer[(paramInt - 1)], this.buffer[paramInt]);
    }
    label114: return this.buffer[paramInt];
  }

  public static boolean isNFSkippable(int paramInt, Mode paramMode)
  {
    return paramMode.isNFSkippable(paramInt);
  }

  public NormalizerBase(String paramString, Mode paramMode)
  {
    this(paramString, paramMode, 0);
  }

  public static String normalize(String paramString, Normalizer.Form paramForm)
  {
    return normalize(paramString, paramForm, 0);
  }

  public static String normalize(String paramString, Normalizer.Form paramForm, int paramInt)
  {
    switch (1.$SwitchMap$java$text$Normalizer$Form[paramForm.ordinal()])
    {
    case 1:
      return NFC.normalize(paramString, paramInt);
    case 2:
      return NFD.normalize(paramString, paramInt);
    case 3:
      return NFKC.normalize(paramString, paramInt);
    case 4:
      return NFKD.normalize(paramString, paramInt);
    }
    throw new IllegalArgumentException("Unexpected normalization form: " + paramForm);
  }

  public static boolean isNormalized(String paramString, Normalizer.Form paramForm)
  {
    return isNormalized(paramString, paramForm, 0);
  }

  public static boolean isNormalized(String paramString, Normalizer.Form paramForm, int paramInt)
  {
    switch (1.$SwitchMap$java$text$Normalizer$Form[paramForm.ordinal()])
    {
    case 1:
      return (NFC.quickCheck(paramString.toCharArray(), 0, paramString.length(), false, NormalizerImpl.getNX(paramInt)) == YES);
    case 2:
      return (NFD.quickCheck(paramString.toCharArray(), 0, paramString.length(), false, NormalizerImpl.getNX(paramInt)) == YES);
    case 3:
      return (NFKC.quickCheck(paramString.toCharArray(), 0, paramString.length(), false, NormalizerImpl.getNX(paramInt)) == YES);
    case 4:
      return (NFKD.quickCheck(paramString.toCharArray(), 0, paramString.length(), false, NormalizerImpl.getNX(paramInt)) == YES);
    }
    throw new IllegalArgumentException("Unexpected normalization form: " + paramForm);
  }

  private static abstract interface IsNextBoundary
  {
    public abstract boolean isNextBoundary(UCharacterIterator paramUCharacterIterator, int paramInt1, int paramInt2, int[] paramArrayOfInt);
  }

  private static final class IsNextNFDSafe
  implements NormalizerBase.IsNextBoundary
  {
    public boolean isNextBoundary(UCharacterIterator paramUCharacterIterator, int paramInt1, int paramInt2, int[] paramArrayOfInt)
    {
      return NormalizerImpl.isNFDSafe(NormalizerBase.access$1100(paramUCharacterIterator, paramInt1, paramInt2, paramArrayOfInt), paramInt2, paramInt2 & 0x3F);
    }
  }

  private static final class IsNextTrueStarter
  implements NormalizerBase.IsNextBoundary
  {
    public boolean isNextBoundary(UCharacterIterator paramUCharacterIterator, int paramInt1, int paramInt2, int[] paramArrayOfInt)
    {
      int i = paramInt2 << 2 & 0xF;
      long l = NormalizerBase.access$1100(paramUCharacterIterator, paramInt1, paramInt2 | i, paramArrayOfInt);
      return NormalizerImpl.isTrueStarter(l, paramInt2, i);
    }
  }

  private static abstract interface IsPrevBoundary
  {
    public abstract boolean isPrevBoundary(UCharacterIterator paramUCharacterIterator, int paramInt1, int paramInt2, char[] paramArrayOfChar);
  }

  private static final class IsPrevNFDSafe
  implements NormalizerBase.IsPrevBoundary
  {
    public boolean isPrevBoundary(UCharacterIterator paramUCharacterIterator, int paramInt1, int paramInt2, char[] paramArrayOfChar)
    {
      return NormalizerImpl.isNFDSafe(NormalizerBase.access$1000(paramUCharacterIterator, paramInt1, paramInt2, paramArrayOfChar), paramInt2, paramInt2 & 0x3F);
    }
  }

  private static final class IsPrevTrueStarter
  implements NormalizerBase.IsPrevBoundary
  {
    public boolean isPrevBoundary(UCharacterIterator paramUCharacterIterator, int paramInt1, int paramInt2, char[] paramArrayOfChar)
    {
      int i = paramInt2 << 2 & 0xF;
      long l = NormalizerBase.access$1000(paramUCharacterIterator, paramInt1, paramInt2 | i, paramArrayOfChar);
      return NormalizerImpl.isTrueStarter(l, paramInt2, i);
    }
  }

  public static class Mode
  {
    private int modeValue;

    private Mode(int paramInt)
    {
      this.modeValue = paramInt;
    }

    protected int normalize(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, UnicodeSet paramUnicodeSet)
    {
      int i = paramInt2 - paramInt1;
      int j = paramInt4 - paramInt3;
      if (i > j)
        return i;
      System.arraycopy(paramArrayOfChar1, paramInt1, paramArrayOfChar2, paramInt3, i);
      return i;
    }

    protected int normalize(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, int paramInt5)
    {
      return normalize(paramArrayOfChar1, paramInt1, paramInt2, paramArrayOfChar2, paramInt3, paramInt4, NormalizerImpl.getNX(paramInt5));
    }

    protected String normalize(String paramString, int paramInt)
    {
      return paramString;
    }

    protected int getMinC()
    {
      return -1;
    }

    protected int getMask()
    {
      return -1;
    }

    protected NormalizerBase.IsPrevBoundary getPrevBoundary()
    {
      return null;
    }

    protected NormalizerBase.IsNextBoundary getNextBoundary()
    {
      return null;
    }

    protected NormalizerBase.QuickCheckResult quickCheck(char[] paramArrayOfChar, int paramInt1, int paramInt2, boolean paramBoolean, UnicodeSet paramUnicodeSet)
    {
      if (paramBoolean)
        return NormalizerBase.MAYBE;
      return NormalizerBase.NO;
    }

    protected boolean isNFSkippable(int paramInt)
    {
      return true;
    }
  }

  private static final class NFCMode extends NormalizerBase.Mode
  {
    private NFCMode(int paramInt)
    {
      super(paramInt, null);
    }

    protected int normalize(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, UnicodeSet paramUnicodeSet)
    {
      return NormalizerImpl.compose(paramArrayOfChar1, paramInt1, paramInt2, paramArrayOfChar2, paramInt3, paramInt4, 0, paramUnicodeSet);
    }

    protected String normalize(String paramString, int paramInt)
    {
      return NormalizerBase.compose(paramString, false, paramInt);
    }

    protected int getMinC()
    {
      return NormalizerImpl.getFromIndexesArr(6);
    }

    protected NormalizerBase.IsPrevBoundary getPrevBoundary()
    {
      return new NormalizerBase.IsPrevTrueStarter(null);
    }

    protected NormalizerBase.IsNextBoundary getNextBoundary()
    {
      return new NormalizerBase.IsNextTrueStarter(null);
    }

    protected int getMask()
    {
      return 65297;
    }

    protected NormalizerBase.QuickCheckResult quickCheck(char[] paramArrayOfChar, int paramInt1, int paramInt2, boolean paramBoolean, UnicodeSet paramUnicodeSet)
    {
      return NormalizerImpl.quickCheck(paramArrayOfChar, paramInt1, paramInt2, NormalizerImpl.getFromIndexesArr(6), 17, 0, paramBoolean, paramUnicodeSet);
    }

    protected boolean isNFSkippable(int paramInt)
    {
      return NormalizerImpl.isNFSkippable(paramInt, this, 65473L);
    }
  }

  private static final class NFDMode extends NormalizerBase.Mode
  {
    private NFDMode(int paramInt)
    {
      super(paramInt, null);
    }

    protected int normalize(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, UnicodeSet paramUnicodeSet)
    {
      int[] arrayOfInt = new int[1];
      return NormalizerImpl.decompose(paramArrayOfChar1, paramInt1, paramInt2, paramArrayOfChar2, paramInt3, paramInt4, false, arrayOfInt, paramUnicodeSet);
    }

    protected String normalize(String paramString, int paramInt)
    {
      return NormalizerBase.decompose(paramString, false, paramInt);
    }

    protected int getMinC()
    {
      return 768;
    }

    protected NormalizerBase.IsPrevBoundary getPrevBoundary()
    {
      return new NormalizerBase.IsPrevNFDSafe(null);
    }

    protected NormalizerBase.IsNextBoundary getNextBoundary()
    {
      return new NormalizerBase.IsNextNFDSafe(null);
    }

    protected int getMask()
    {
      return 65284;
    }

    protected NormalizerBase.QuickCheckResult quickCheck(char[] paramArrayOfChar, int paramInt1, int paramInt2, boolean paramBoolean, UnicodeSet paramUnicodeSet)
    {
      return NormalizerImpl.quickCheck(paramArrayOfChar, paramInt1, paramInt2, NormalizerImpl.getFromIndexesArr(8), 4, 0, paramBoolean, paramUnicodeSet);
    }

    protected boolean isNFSkippable(int paramInt)
    {
      return NormalizerImpl.isNFSkippable(paramInt, this, 65284L);
    }
  }

  private static final class NFKCMode extends NormalizerBase.Mode
  {
    private NFKCMode(int paramInt)
    {
      super(paramInt, null);
    }

    protected int normalize(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, UnicodeSet paramUnicodeSet)
    {
      return NormalizerImpl.compose(paramArrayOfChar1, paramInt1, paramInt2, paramArrayOfChar2, paramInt3, paramInt4, 4096, paramUnicodeSet);
    }

    protected String normalize(String paramString, int paramInt)
    {
      return NormalizerBase.compose(paramString, true, paramInt);
    }

    protected int getMinC()
    {
      return NormalizerImpl.getFromIndexesArr(7);
    }

    protected NormalizerBase.IsPrevBoundary getPrevBoundary()
    {
      return new NormalizerBase.IsPrevTrueStarter(null);
    }

    protected NormalizerBase.IsNextBoundary getNextBoundary()
    {
      return new NormalizerBase.IsNextTrueStarter(null);
    }

    protected int getMask()
    {
      return 65314;
    }

    protected NormalizerBase.QuickCheckResult quickCheck(char[] paramArrayOfChar, int paramInt1, int paramInt2, boolean paramBoolean, UnicodeSet paramUnicodeSet)
    {
      return NormalizerImpl.quickCheck(paramArrayOfChar, paramInt1, paramInt2, NormalizerImpl.getFromIndexesArr(7), 34, 4096, paramBoolean, paramUnicodeSet);
    }

    protected boolean isNFSkippable(int paramInt)
    {
      return NormalizerImpl.isNFSkippable(paramInt, this, 65474L);
    }
  }

  private static final class NFKDMode extends NormalizerBase.Mode
  {
    private NFKDMode(int paramInt)
    {
      super(paramInt, null);
    }

    protected int normalize(char[] paramArrayOfChar1, int paramInt1, int paramInt2, char[] paramArrayOfChar2, int paramInt3, int paramInt4, UnicodeSet paramUnicodeSet)
    {
      int[] arrayOfInt = new int[1];
      return NormalizerImpl.decompose(paramArrayOfChar1, paramInt1, paramInt2, paramArrayOfChar2, paramInt3, paramInt4, true, arrayOfInt, paramUnicodeSet);
    }

    protected String normalize(String paramString, int paramInt)
    {
      return NormalizerBase.decompose(paramString, true, paramInt);
    }

    protected int getMinC()
    {
      return 768;
    }

    protected NormalizerBase.IsPrevBoundary getPrevBoundary()
    {
      return new NormalizerBase.IsPrevNFDSafe(null);
    }

    protected NormalizerBase.IsNextBoundary getNextBoundary()
    {
      return new NormalizerBase.IsNextNFDSafe(null);
    }

    protected int getMask()
    {
      return 65288;
    }

    protected NormalizerBase.QuickCheckResult quickCheck(char[] paramArrayOfChar, int paramInt1, int paramInt2, boolean paramBoolean, UnicodeSet paramUnicodeSet)
    {
      return NormalizerImpl.quickCheck(paramArrayOfChar, paramInt1, paramInt2, NormalizerImpl.getFromIndexesArr(9), 8, 4096, paramBoolean, paramUnicodeSet);
    }

    protected boolean isNFSkippable(int paramInt)
    {
      return NormalizerImpl.isNFSkippable(paramInt, this, 65288L);
    }
  }

  public static final class QuickCheckResult
  {
    private int resultValue;

    private QuickCheckResult(int paramInt)
    {
      this.resultValue = paramInt;
    }
  }
}