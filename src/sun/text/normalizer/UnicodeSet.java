package sun.text.normalizer;

import I;
import java.text.ParsePosition;
import java.util.Iterator;
import java.util.TreeSet;

public class UnicodeSet
  implements UnicodeMatcher
{
  private static final int LOW = 0;
  private static final int HIGH = 1114112;
  public static final int MIN_VALUE = 0;
  public static final int MAX_VALUE = 1114111;
  private int len;
  private int[] list;
  private int[] rangeList;
  private int[] buffer;
  TreeSet strings;
  private String pat;
  private static final int START_EXTRA = 16;
  private static final int GROW_EXTRA = 16;
  private static UnicodeSet INCLUSIONS = null;
  static final VersionInfo NO_VERSION = VersionInfo.getInstance(0, 0, 0, 0);
  public static final int IGNORE_SPACE = 1;

  public UnicodeSet()
  {
    this.strings = new TreeSet();
    this.pat = null;
    this.list = new int[17];
    this.list[(this.len++)] = 1114112;
  }

  public UnicodeSet(int paramInt1, int paramInt2)
  {
    complement(paramInt1, paramInt2);
  }

  public UnicodeSet(String paramString)
  {
    applyPattern(paramString, null, null, 1);
  }

  public UnicodeSet set(UnicodeSet paramUnicodeSet)
  {
    this.list = ((int[])(int[])paramUnicodeSet.list.clone());
    this.len = paramUnicodeSet.len;
    this.pat = paramUnicodeSet.pat;
    this.strings = ((TreeSet)paramUnicodeSet.strings.clone());
    return this;
  }

  public final UnicodeSet applyPattern(String paramString)
  {
    return applyPattern(paramString, null, null, 1);
  }

  private static void _appendToPat(StringBuffer paramStringBuffer, String paramString, boolean paramBoolean)
  {
    int i = 0;
    while (i < paramString.length())
    {
      _appendToPat(paramStringBuffer, UTF16.charAt(paramString, i), paramBoolean);
      i += UTF16.getCharCount(i);
    }
  }

  private static void _appendToPat(StringBuffer paramStringBuffer, int paramInt, boolean paramBoolean)
  {
    if ((paramBoolean) && (Utility.isUnprintable(paramInt)) && (Utility.escapeUnprintable(paramStringBuffer, paramInt)))
      return;
    switch (paramInt)
    {
    case 36:
    case 38:
    case 45:
    case 58:
    case 91:
    case 92:
    case 93:
    case 94:
    case 123:
    case 125:
      paramStringBuffer.append('\\');
      break;
    default:
      if (UCharacterProperty.isRuleWhiteSpace(paramInt))
        paramStringBuffer.append('\\');
    }
    UTF16.append(paramStringBuffer, paramInt);
  }

  private StringBuffer _toPattern(StringBuffer paramStringBuffer, boolean paramBoolean)
  {
    if (this.pat != null)
    {
      int j = 0;
      int i = 0;
      while (i < this.pat.length())
      {
        int k = UTF16.charAt(this.pat, i);
        i += UTF16.getCharCount(k);
        if ((paramBoolean) && (Utility.isUnprintable(k)))
        {
          if (j % 2 == 1)
            paramStringBuffer.setLength(paramStringBuffer.length() - 1);
          Utility.escapeUnprintable(paramStringBuffer, k);
          j = 0;
        }
        else
        {
          UTF16.append(paramStringBuffer, k);
          if (k == 92)
            ++j;
          else
            j = 0;
        }
      }
      return paramStringBuffer;
    }
    return _generatePattern(paramStringBuffer, paramBoolean);
  }

  public StringBuffer _generatePattern(StringBuffer paramStringBuffer, boolean paramBoolean)
  {
    int j;
    int k;
    int l;
    paramStringBuffer.append('[');
    int i = getRangeCount();
    if ((i > 1) && (getRangeStart(0) == 0) && (getRangeEnd(i - 1) == 1114111))
    {
      paramStringBuffer.append('^');
      for (j = 1; j < i; ++j)
      {
        k = getRangeEnd(j - 1) + 1;
        l = getRangeStart(j) - 1;
        _appendToPat(paramStringBuffer, k, paramBoolean);
        if (k != l)
        {
          if (k + 1 != l)
            paramStringBuffer.append('-');
          _appendToPat(paramStringBuffer, l, paramBoolean);
        }
      }
    }
    else
    {
      for (j = 0; j < i; ++j)
      {
        k = getRangeStart(j);
        l = getRangeEnd(j);
        _appendToPat(paramStringBuffer, k, paramBoolean);
        if (k != l)
        {
          if (k + 1 != l)
            paramStringBuffer.append('-');
          _appendToPat(paramStringBuffer, l, paramBoolean);
        }
      }
    }
    if (this.strings.size() > 0)
    {
      Iterator localIterator = this.strings.iterator();
      while (localIterator.hasNext())
      {
        paramStringBuffer.append('{');
        _appendToPat(paramStringBuffer, (String)localIterator.next(), paramBoolean);
        paramStringBuffer.append('}');
      }
    }
    return paramStringBuffer.append(']');
  }

  public UnicodeSet add(int paramInt1, int paramInt2)
  {
    if ((paramInt1 < 0) || (paramInt1 > 1114111))
      throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(paramInt1, 6));
    if ((paramInt2 < 0) || (paramInt2 > 1114111))
      throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(paramInt2, 6));
    if (paramInt1 < paramInt2)
      add(range(paramInt1, paramInt2), 2, 0);
    else if (paramInt1 == paramInt2)
      add(paramInt1);
    return this;
  }

  public final UnicodeSet add(int paramInt)
  {
    if ((paramInt < 0) || (paramInt > 1114111))
      throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(paramInt, 6));
    int i = findCodePoint(paramInt);
    if ((i & 0x1) != 0)
      return this;
    if (paramInt == this.list[i] - 1)
    {
      this.list[i] = paramInt;
      if (paramInt == 1114111)
      {
        ensureCapacity(this.len + 1);
        this.list[(this.len++)] = 1114112;
      }
      if ((i > 0) && (paramInt == this.list[(i - 1)]))
      {
        System.arraycopy(this.list, i + 1, this.list, i - 1, this.len - i - 1);
        this.len -= 2;
      }
    }
    else if ((i > 0) && (paramInt == this.list[(i - 1)]))
    {
      this.list[(i - 1)] += 1;
    }
    else
    {
      if (this.len + 2 > this.list.length)
      {
        int[] arrayOfInt = new int[this.len + 2 + 16];
        if (i != 0)
          System.arraycopy(this.list, 0, arrayOfInt, 0, i);
        System.arraycopy(this.list, i, arrayOfInt, i + 2, this.len - i);
        this.list = arrayOfInt;
      }
      else
      {
        System.arraycopy(this.list, i, this.list, i + 2, this.len - i);
      }
      this.list[i] = paramInt;
      this.list[(i + 1)] = (paramInt + 1);
      this.len += 2;
    }
    this.pat = null;
    return this;
  }

  public final UnicodeSet add(String paramString)
  {
    int i = getSingleCP(paramString);
    if (i < 0)
    {
      this.strings.add(paramString);
      this.pat = null;
    }
    else
    {
      add(i, i);
    }
    return this;
  }

  private static int getSingleCP(String paramString)
  {
    if (paramString.length() < 1)
      throw new IllegalArgumentException("Can't use zero-length strings in UnicodeSet");
    if (paramString.length() > 2)
      return -1;
    if (paramString.length() == 1)
      return paramString.charAt(0);
    int i = UTF16.charAt(paramString, 0);
    if (i > 65535)
      return i;
    return -1;
  }

  public UnicodeSet complement(int paramInt1, int paramInt2)
  {
    if ((paramInt1 < 0) || (paramInt1 > 1114111))
      throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(paramInt1, 6));
    if ((paramInt2 < 0) || (paramInt2 > 1114111))
      throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(paramInt2, 6));
    if (paramInt1 <= paramInt2)
      xor(range(paramInt1, paramInt2), 2, 0);
    this.pat = null;
    return this;
  }

  public UnicodeSet complement()
  {
    if (this.list[0] == 0)
    {
      System.arraycopy(this.list, 1, this.list, 0, this.len - 1);
      this.len -= 1;
    }
    else
    {
      ensureCapacity(this.len + 1);
      System.arraycopy(this.list, 0, this.list, 1, this.len);
      this.list[0] = 0;
      this.len += 1;
    }
    this.pat = null;
    return this;
  }

  public boolean contains(int paramInt)
  {
    if ((paramInt < 0) || (paramInt > 1114111))
      throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(paramInt, 6));
    int i = findCodePoint(paramInt);
    return ((i & 0x1) != 0);
  }

  private final int findCodePoint(int paramInt)
  {
    if (paramInt < this.list[0])
      return 0;
    if ((this.len >= 2) && (paramInt >= this.list[(this.len - 2)]))
      return (this.len - 1);
    int i = 0;
    int j = this.len - 1;
    while (true)
    {
      int k = i + j >>> 1;
      if (k == i)
        return j;
      if (paramInt < this.list[k])
        j = k;
      else
        i = k;
    }
  }

  public UnicodeSet addAll(UnicodeSet paramUnicodeSet)
  {
    add(paramUnicodeSet.list, paramUnicodeSet.len, 0);
    this.strings.addAll(paramUnicodeSet.strings);
    return this;
  }

  public UnicodeSet retainAll(UnicodeSet paramUnicodeSet)
  {
    retain(paramUnicodeSet.list, paramUnicodeSet.len, 0);
    this.strings.retainAll(paramUnicodeSet.strings);
    return this;
  }

  public UnicodeSet removeAll(UnicodeSet paramUnicodeSet)
  {
    retain(paramUnicodeSet.list, paramUnicodeSet.len, 2);
    this.strings.removeAll(paramUnicodeSet.strings);
    return this;
  }

  public UnicodeSet clear()
  {
    this.list[0] = 1114112;
    this.len = 1;
    this.pat = null;
    this.strings.clear();
    return this;
  }

  public int getRangeCount()
  {
    return (this.len / 2);
  }

  public int getRangeStart(int paramInt)
  {
    return this.list[(paramInt * 2)];
  }

  public int getRangeEnd(int paramInt)
  {
    return (this.list[(paramInt * 2 + 1)] - 1);
  }

  UnicodeSet applyPattern(String paramString, ParsePosition paramParsePosition, SymbolTable paramSymbolTable, int paramInt)
  {
    int i = (paramParsePosition == null) ? 1 : 0;
    if (i != 0)
      paramParsePosition = new ParsePosition(0);
    StringBuffer localStringBuffer = new StringBuffer();
    RuleCharacterIterator localRuleCharacterIterator = new RuleCharacterIterator(paramString, paramSymbolTable, paramParsePosition);
    applyPattern(localRuleCharacterIterator, paramSymbolTable, localStringBuffer, paramInt);
    if (localRuleCharacterIterator.inVariable())
      syntaxError(localRuleCharacterIterator, "Extra chars in variable value");
    this.pat = localStringBuffer.toString();
    if (i != 0)
    {
      int j = paramParsePosition.getIndex();
      if ((paramInt & 0x1) != 0)
        j = Utility.skipWhitespace(paramString, j);
      if (j != paramString.length())
        throw new IllegalArgumentException("Parse of \"" + paramString + "\" failed at " + j);
    }
    return this;
  }

  void applyPattern(RuleCharacterIterator paramRuleCharacterIterator, SymbolTable paramSymbolTable, StringBuffer paramStringBuffer, int paramInt)
  {
    int i3;
    int i = 3;
    if ((paramInt & 0x1) != 0)
      i |= 4;
    StringBuffer localStringBuffer1 = new StringBuffer();
    StringBuffer localStringBuffer2 = null;
    int j = 0;
    UnicodeSet localUnicodeSet1 = null;
    Object localObject = null;
    int k = 0;
    int l = 0;
    int i1 = 0;
    char c = ';
    int i2 = 0;
    clear();
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
                while (true)
                {
                  while (true)
                  {
                    UnicodeSet localUnicodeSet2;
                    int i4;
                    while (true)
                    {
                      if ((i1 == 2) || (paramRuleCharacterIterator.atEnd()))
                        break label1249;
                      i3 = 0;
                      bool = false;
                      localUnicodeSet2 = null;
                      i4 = 0;
                      if (resemblesPropertyPattern(paramRuleCharacterIterator, i))
                      {
                        i4 = 2;
                        break label289:
                      }
                      localObject = paramRuleCharacterIterator.getPos(localObject);
                      i3 = paramRuleCharacterIterator.next(i);
                      bool = paramRuleCharacterIterator.isEscaped();
                      if ((i3 != 91) || (bool))
                        break;
                      if (i1 == 1)
                      {
                        paramRuleCharacterIterator.setPos(localObject);
                        i4 = 1;
                        break label289:
                      }
                      i1 = 1;
                      localStringBuffer1.append('[');
                      localObject = paramRuleCharacterIterator.getPos(localObject);
                      i3 = paramRuleCharacterIterator.next(i);
                      bool = paramRuleCharacterIterator.isEscaped();
                      if ((i3 == 94) && (!(bool)))
                      {
                        i2 = 1;
                        localStringBuffer1.append('^');
                        localObject = paramRuleCharacterIterator.getPos(localObject);
                        i3 = paramRuleCharacterIterator.next(i);
                        bool = paramRuleCharacterIterator.isEscaped();
                      }
                      if (i3 == 45)
                      {
                        bool = true;
                        break label289:
                      }
                      paramRuleCharacterIterator.setPos(localObject);
                    }
                    if (paramSymbolTable != null)
                    {
                      UnicodeMatcher localUnicodeMatcher = paramSymbolTable.lookupMatcher(i3);
                      if (localUnicodeMatcher != null)
                        try
                        {
                          localUnicodeSet2 = (UnicodeSet)localUnicodeMatcher;
                          i4 = 3;
                        }
                        catch (ClassCastException localClassCastException)
                        {
                          syntaxError(paramRuleCharacterIterator, "Syntax error");
                        }
                    }
                    label289: if (i4 == 0)
                      break;
                    if (k == 1)
                    {
                      if (c != 0)
                        syntaxError(paramRuleCharacterIterator, "Char expected after operator");
                      add(l, l);
                      _appendToPat(localStringBuffer1, l, false);
                      k = c = 0;
                    }
                    if ((c == '-') || (c == '&'))
                      localStringBuffer1.append(c);
                    if (localUnicodeSet2 == null)
                    {
                      if (localUnicodeSet1 == null)
                        localUnicodeSet1 = new UnicodeSet();
                      localUnicodeSet2 = localUnicodeSet1;
                    }
                    switch (i4)
                    {
                    case 1:
                      localUnicodeSet2.applyPattern(paramRuleCharacterIterator, paramSymbolTable, localStringBuffer1, paramInt);
                      break;
                    case 2:
                      paramRuleCharacterIterator.skipIgnored(i);
                      localUnicodeSet2.applyPropertyPattern(paramRuleCharacterIterator, localStringBuffer1, paramSymbolTable);
                      break;
                    case 3:
                      localUnicodeSet2._toPattern(localStringBuffer1, false);
                    }
                    j = 1;
                    if (i1 == 0)
                    {
                      set(localUnicodeSet2);
                      i1 = 2;
                      break label1249:
                    }
                    switch (c)
                    {
                    case '-':
                      removeAll(localUnicodeSet2);
                      break;
                    case '&':
                      retainAll(localUnicodeSet2);
                      break;
                    case '\0':
                      addAll(localUnicodeSet2);
                    }
                    c = ';
                    k = 2;
                  }
                  if (i1 == 0)
                    syntaxError(paramRuleCharacterIterator, "Missing '['");
                  if (bool)
                    break label1105;
                  switch (i3)
                  {
                  case 93:
                    if (k == 1)
                    {
                      add(l, l);
                      _appendToPat(localStringBuffer1, l, false);
                    }
                    if (c == '-')
                    {
                      add(c, c);
                      localStringBuffer1.append(c);
                    }
                    else if (c == '&')
                    {
                      syntaxError(paramRuleCharacterIterator, "Trailing '&'");
                    }
                    localStringBuffer1.append(']');
                    i1 = 2;
                  case 45:
                  case 38:
                  case 94:
                  case 123:
                  case 36:
                  }
                }
                if (c != 0)
                  break label764;
                if (k == 0)
                  break;
                c = (char)i3;
              }
              add(i3, i3);
              i3 = paramRuleCharacterIterator.next(i);
              bool = paramRuleCharacterIterator.isEscaped();
              if ((i3 != 93) || (bool))
                break;
              localStringBuffer1.append("-]");
              i1 = 2;
            }
            label764: syntaxError(paramRuleCharacterIterator, "'-' not after char or set");
            if ((k != 2) || (c != 0))
              break;
            c = (char)i3;
          }
          syntaxError(paramRuleCharacterIterator, "'&' not after set");
          syntaxError(paramRuleCharacterIterator, "'^' not after '['");
          if (c != 0)
            syntaxError(paramRuleCharacterIterator, "Missing operand after operator");
          if (k == 1)
          {
            add(l, l);
            _appendToPat(localStringBuffer1, l, false);
          }
          k = 0;
          if (localStringBuffer2 == null)
            localStringBuffer2 = new StringBuffer();
          else
            localStringBuffer2.setLength(0);
          int i5 = 0;
          while (!(paramRuleCharacterIterator.atEnd()))
          {
            i3 = paramRuleCharacterIterator.next(i);
            bool = paramRuleCharacterIterator.isEscaped();
            if ((i3 == 125) && (!(bool)))
            {
              i5 = 1;
              break;
            }
            UTF16.append(localStringBuffer2, i3);
          }
          if ((localStringBuffer2.length() < 1) || (i5 == 0))
            syntaxError(paramRuleCharacterIterator, "Invalid multicharacter string");
          add(localStringBuffer2.toString());
          localStringBuffer1.append('{');
          _appendToPat(localStringBuffer1, localStringBuffer2.toString(), false);
          localStringBuffer1.append('}');
        }
        localObject = paramRuleCharacterIterator.getPos(localObject);
        i3 = paramRuleCharacterIterator.next(i);
        boolean bool = paramRuleCharacterIterator.isEscaped();
        int i6 = ((i3 == 93) && (!(bool))) ? 1 : 0;
        if ((paramSymbolTable == null) && (i6 == 0))
        {
          i3 = 36;
          paramRuleCharacterIterator.setPos(localObject);
          break label1105:
        }
        if ((i6 == 0) || (c != 0))
          break;
        if (k == 1)
        {
          add(l, l);
          _appendToPat(localStringBuffer1, l, false);
        }
        add(65535);
        j = 1;
        localStringBuffer1.append('$').append(']');
        i1 = 2;
      }
      syntaxError(paramRuleCharacterIterator, "Unquoted '$'");
      switch (k)
      {
      case 0:
        k = 1;
        l = i3;
        break;
      case 1:
        if (c == '-')
        {
          if (l >= i3)
            syntaxError(paramRuleCharacterIterator, "Invalid range");
          add(l, i3);
          _appendToPat(localStringBuffer1, l, false);
          localStringBuffer1.append(c);
          _appendToPat(localStringBuffer1, i3, false);
          k = c = 0;
        }
        else
        {
          add(l, l);
          _appendToPat(localStringBuffer1, l, false);
          l = i3;
        }
        break;
      case 2:
        if (c != 0)
          label1105: syntaxError(paramRuleCharacterIterator, "Set expected after operator");
        l = i3;
        k = 1;
      }
    }
    if (i1 != 2)
      label1249: syntaxError(paramRuleCharacterIterator, "Missing ']'");
    paramRuleCharacterIterator.skipIgnored(i);
    if (i2 != 0)
      complement();
    if (j != 0)
      paramStringBuffer.append(localStringBuffer1.toString());
    else
      _generatePattern(paramStringBuffer, false);
  }

  private static void syntaxError(RuleCharacterIterator paramRuleCharacterIterator, String paramString)
  {
    throw new IllegalArgumentException("Error: " + paramString + " at \"" + Utility.escape(paramRuleCharacterIterator.toString()) + '"');
  }

  private void ensureCapacity(int paramInt)
  {
    if (paramInt <= this.list.length)
      return;
    int[] arrayOfInt = new int[paramInt + 16];
    System.arraycopy(this.list, 0, arrayOfInt, 0, this.len);
    this.list = arrayOfInt;
  }

  private void ensureBufferCapacity(int paramInt)
  {
    if ((this.buffer != null) && (paramInt <= this.buffer.length))
      return;
    this.buffer = new int[paramInt + 16];
  }

  private int[] range(int paramInt1, int paramInt2)
  {
    if (this.rangeList == null)
    {
      this.rangeList = { paramInt1, paramInt2 + 1, 1114112 };
    }
    else
    {
      this.rangeList[0] = paramInt1;
      this.rangeList[1] = (paramInt2 + 1);
    }
    return this.rangeList;
  }

  private UnicodeSet xor(int[] paramArrayOfInt, int paramInt1, int paramInt2)
  {
    int i1;
    ensureBufferCapacity(this.len + paramInt1);
    int i = 0;
    int j = 0;
    int k = 0;
    int l = this.list[(i++)];
    if ((paramInt2 == 1) || (paramInt2 == 2))
    {
      i1 = 0;
      if (paramArrayOfInt[j] == 0)
        i1 = paramArrayOfInt[(++j)];
    }
    else
    {
      i1 = paramArrayOfInt[(j++)];
    }
    while (true)
    {
      while (true)
      {
        while (l < i1)
        {
          this.buffer[(k++)] = l;
          l = this.list[(i++)];
        }
        if (i1 >= l)
          break;
        this.buffer[(k++)] = i1;
        i1 = paramArrayOfInt[(j++)];
      }
      if (l == 1114112)
        break;
      l = this.list[(i++)];
      i1 = paramArrayOfInt[(j++)];
    }
    this.buffer[(k++)] = 1114112;
    this.len = k;
    int[] arrayOfInt = this.list;
    this.list = this.buffer;
    this.buffer = arrayOfInt;
    this.pat = null;
    return this;
  }

  private UnicodeSet add(int[] paramArrayOfInt, int paramInt1, int paramInt2)
  {
    ensureBufferCapacity(this.len + paramInt1);
    int i = 0;
    int j = 0;
    int k = 0;
    int l = this.list[(i++)];
    int i1 = paramArrayOfInt[(j++)];
    while (true)
      switch (paramInt2)
      {
      case 0:
        if (l < i1)
        {
          if ((k > 0) && (l <= this.buffer[(k - 1)]))
          {
            l = max(this.list[i], this.buffer[(--k)]);
          }
          else
          {
            this.buffer[(k++)] = l;
            l = this.list[i];
          }
          ++i;
          paramInt2 ^= 1;
        }
        else if (i1 < l)
        {
          if ((k > 0) && (i1 <= this.buffer[(k - 1)]))
          {
            i1 = max(paramArrayOfInt[j], this.buffer[(--k)]);
          }
          else
          {
            this.buffer[(k++)] = i1;
            i1 = paramArrayOfInt[j];
          }
          ++j;
          paramInt2 ^= 2;
        }
        else
        {
          if (l == 1114112)
            break;
          if ((k > 0) && (l <= this.buffer[(k - 1)]))
          {
            l = max(this.list[i], this.buffer[(--k)]);
          }
          else
          {
            this.buffer[(k++)] = l;
            l = this.list[i];
          }
          ++i;
          paramInt2 ^= 1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        break;
      case 3:
        if (i1 <= l)
        {
          if (l == 1114112)
            break;
          this.buffer[(k++)] = l;
        }
        else
        {
          if (i1 == 1114112)
            break;
          this.buffer[(k++)] = i1;
        }
        l = this.list[(i++)];
        paramInt2 ^= 1;
        i1 = paramArrayOfInt[(j++)];
        paramInt2 ^= 2;
        break;
      case 1:
        if (l < i1)
        {
          this.buffer[(k++)] = l;
          l = this.list[(i++)];
          paramInt2 ^= 1;
        }
        else if (i1 < l)
        {
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        else
        {
          if (l == 1114112)
            break;
          l = this.list[(i++)];
          paramInt2 ^= 1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        break;
      case 2:
        if (i1 < l)
        {
          this.buffer[(k++)] = i1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        else if (l < i1)
        {
          l = this.list[(i++)];
          paramInt2 ^= 1;
        }
        else
        {
          if (l == 1114112)
            break;
          l = this.list[(i++)];
          paramInt2 ^= 1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
      }
    this.buffer[(k++)] = 1114112;
    this.len = k;
    int[] arrayOfInt = this.list;
    this.list = this.buffer;
    this.buffer = arrayOfInt;
    this.pat = null;
    return this;
  }

  private UnicodeSet retain(int[] paramArrayOfInt, int paramInt1, int paramInt2)
  {
    ensureBufferCapacity(this.len + paramInt1);
    int i = 0;
    int j = 0;
    int k = 0;
    int l = this.list[(i++)];
    int i1 = paramArrayOfInt[(j++)];
    while (true)
      switch (paramInt2)
      {
      case 0:
        if (l < i1)
        {
          l = this.list[(i++)];
          paramInt2 ^= 1;
        }
        else if (i1 < l)
        {
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        else
        {
          if (l == 1114112)
            break;
          this.buffer[(k++)] = l;
          l = this.list[(i++)];
          paramInt2 ^= 1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        break;
      case 3:
        if (l < i1)
        {
          this.buffer[(k++)] = l;
          l = this.list[(i++)];
          paramInt2 ^= 1;
        }
        else if (i1 < l)
        {
          this.buffer[(k++)] = i1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        else
        {
          if (l == 1114112)
            break;
          this.buffer[(k++)] = l;
          l = this.list[(i++)];
          paramInt2 ^= 1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        break;
      case 1:
        if (l < i1)
        {
          l = this.list[(i++)];
          paramInt2 ^= 1;
        }
        else if (i1 < l)
        {
          this.buffer[(k++)] = i1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        else
        {
          if (l == 1114112)
            break;
          l = this.list[(i++)];
          paramInt2 ^= 1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        break;
      case 2:
        if (i1 < l)
        {
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
        else if (l < i1)
        {
          this.buffer[(k++)] = l;
          l = this.list[(i++)];
          paramInt2 ^= 1;
        }
        else
        {
          if (l == 1114112)
            break;
          l = this.list[(i++)];
          paramInt2 ^= 1;
          i1 = paramArrayOfInt[(j++)];
          paramInt2 ^= 2;
        }
      }
    this.buffer[(k++)] = 1114112;
    this.len = k;
    int[] arrayOfInt = this.list;
    this.list = this.buffer;
    this.buffer = arrayOfInt;
    this.pat = null;
    return this;
  }

  private static final int max(int paramInt1, int paramInt2)
  {
    return ((paramInt1 > paramInt2) ? paramInt1 : paramInt2);
  }

  private static synchronized UnicodeSet getInclusions()
  {
    if (INCLUSIONS == null)
    {
      UCharacterProperty localUCharacterProperty = UCharacterProperty.getInstance();
      INCLUSIONS = localUCharacterProperty.getInclusions();
    }
    return INCLUSIONS;
  }

  private UnicodeSet applyFilter(Filter paramFilter)
  {
    clear();
    int i = -1;
    UnicodeSet localUnicodeSet = getInclusions();
    int j = localUnicodeSet.getRangeCount();
    for (int k = 0; k < j; ++k)
    {
      int l = localUnicodeSet.getRangeStart(k);
      int i1 = localUnicodeSet.getRangeEnd(k);
      for (int i2 = l; i2 <= i1; ++i2)
        if (paramFilter.contains(i2))
        {
          if (i < 0)
            i = i2;
        }
        else if (i >= 0)
        {
          add(i, i2 - 1);
          i = -1;
        }
    }
    if (i >= 0)
      add(i, 1114111);
    return this;
  }

  private static String mungeCharName(String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 0;
    while (true)
    {
      while (true)
      {
        do
        {
          if (i >= paramString.length())
            break label75;
          j = UTF16.charAt(paramString, i);
          i += UTF16.getCharCount(j);
          if (!(UCharacterProperty.isRuleWhiteSpace(j)))
            break label66;
        }
        while (localStringBuffer.length() == 0);
        if (localStringBuffer.charAt(localStringBuffer.length() - 1) != ' ')
          break;
      }
      int j = 32;
      label66: UTF16.append(localStringBuffer, j);
    }
    if ((localStringBuffer.length() != 0) && (localStringBuffer.charAt(localStringBuffer.length() - 1) == ' '))
      label75: localStringBuffer.setLength(localStringBuffer.length() - 1);
    return localStringBuffer.toString();
  }

  /**
   * @deprecated
   */
  public UnicodeSet applyPropertyAlias(String paramString1, String paramString2, SymbolTable paramSymbolTable)
  {
    if (paramString1.equals("Age"))
    {
      VersionInfo localVersionInfo = VersionInfo.getInstance(mungeCharName(paramString2));
      applyFilter(new VersionFilter(localVersionInfo));
      return this;
    }
    throw new IllegalArgumentException("Unsupported property");
  }

  private static boolean resemblesPropertyPattern(RuleCharacterIterator paramRuleCharacterIterator, int paramInt)
  {
    int i = 0;
    paramInt &= -3;
    Object localObject = paramRuleCharacterIterator.getPos(null);
    int j = paramRuleCharacterIterator.next(paramInt);
    if ((j == 91) || (j == 92))
    {
      int k = paramRuleCharacterIterator.next(paramInt & 0xFFFFFFFB);
      i = ((k == 78) || (k == 112) || (k == 80)) ? 1 : (j == 91) ? 0 : (k == 58) ? 1 : 0;
    }
    paramRuleCharacterIterator.setPos(localObject);
    return i;
  }

  private UnicodeSet applyPropertyPattern(String paramString, ParsePosition paramParsePosition, SymbolTable paramSymbolTable)
  {
    String str1;
    String str2;
    int i = paramParsePosition.getIndex();
    if (i + 5 > paramString.length())
      return null;
    int j = 0;
    int k = 0;
    int l = 0;
    if (paramString.regionMatches(i, "[:", 0, 2))
    {
      j = 1;
      i = Utility.skipWhitespace(paramString, i + 2);
      if ((i < paramString.length()) && (paramString.charAt(i) == '^'))
      {
        ++i;
        l = 1;
      }
    }
    else if ((paramString.regionMatches(true, i, "\\p", 0, 2)) || (paramString.regionMatches(i, "\\N", 0, 2)))
    {
      i1 = paramString.charAt(i + 1);
      l = (i1 == 80) ? 1 : 0;
      k = (i1 == 78) ? 1 : 0;
      i = Utility.skipWhitespace(paramString, i + 2);
      if ((i == paramString.length()) || (paramString.charAt(i++) != '{'))
        return null;
    }
    else
    {
      return null;
    }
    int i1 = paramString.indexOf((j != 0) ? ":]" : "}", i);
    if (i1 < 0)
      return null;
    int i2 = paramString.indexOf(61, i);
    if ((i2 >= 0) && (i2 < i1) && (k == 0))
    {
      str1 = paramString.substring(i, i2);
      str2 = paramString.substring(i2 + 1, i1);
    }
    else
    {
      str1 = paramString.substring(i, i1);
      str2 = "";
      if (k != 0)
      {
        str2 = str1;
        str1 = "na";
      }
    }
    applyPropertyAlias(str1, str2, paramSymbolTable);
    if (l != 0)
      complement();
    paramParsePosition.setIndex(i1 + ((j != 0) ? 2 : 1));
    return this;
  }

  private void applyPropertyPattern(RuleCharacterIterator paramRuleCharacterIterator, StringBuffer paramStringBuffer, SymbolTable paramSymbolTable)
  {
    String str = paramRuleCharacterIterator.lookahead();
    ParsePosition localParsePosition = new ParsePosition(0);
    applyPropertyPattern(str, localParsePosition, paramSymbolTable);
    if (localParsePosition.getIndex() == 0)
      syntaxError(paramRuleCharacterIterator, "Invalid property pattern");
    paramRuleCharacterIterator.jumpahead(localParsePosition.getIndex());
    paramStringBuffer.append(str.substring(0, localParsePosition.getIndex()));
  }

  private static abstract interface Filter
  {
    public abstract boolean contains(int paramInt);
  }

  private static class VersionFilter
  implements UnicodeSet.Filter
  {
    VersionInfo version;

    VersionFilter(VersionInfo paramVersionInfo)
    {
      this.version = paramVersionInfo;
    }

    public boolean contains(int paramInt)
    {
      VersionInfo localVersionInfo = UCharacter.getAge(paramInt);
      return ((localVersionInfo != UnicodeSet.NO_VERSION) && (localVersionInfo.compareTo(this.version) <= 0));
    }
  }
}