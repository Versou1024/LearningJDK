package sun.org.mozilla.javascript.internal;

public class Decompiler
{
  public static final int ONLY_BODY_FLAG = 1;
  public static final int TO_SOURCE_FLAG = 2;
  public static final int INITIAL_INDENT_PROP = 1;
  public static final int INDENT_GAP_PROP = 2;
  public static final int CASE_GAP_PROP = 3;
  private static final int FUNCTION_END = 147;
  private char[] sourceBuffer = new char[128];
  private int sourceTop;
  private static final boolean printSource = 0;

  String getEncodedSource()
  {
    return sourceToString(0);
  }

  int getCurrentOffset()
  {
    return this.sourceTop;
  }

  int markFunctionStart(int paramInt)
  {
    int i = getCurrentOffset();
    addToken(105);
    append((char)paramInt);
    return i;
  }

  int markFunctionEnd(int paramInt)
  {
    int i = getCurrentOffset();
    append(147);
    return i;
  }

  void addToken(int paramInt)
  {
    if ((0 > paramInt) || (paramInt > 146))
      throw new IllegalArgumentException();
    append((char)paramInt);
  }

  void addEOL(int paramInt)
  {
    if ((0 > paramInt) || (paramInt > 146))
      throw new IllegalArgumentException();
    append((char)paramInt);
    append('\1');
  }

  void addName(String paramString)
  {
    addToken(38);
    appendString(paramString);
  }

  void addString(String paramString)
  {
    addToken(40);
    appendString(paramString);
  }

  void addRegexp(String paramString1, String paramString2)
  {
    addToken(47);
    appendString('/' + paramString1 + '/' + paramString2);
  }

  void addNumber(double paramDouble)
  {
    addToken(39);
    long l = ()paramDouble;
    if (l != paramDouble)
    {
      l = Double.doubleToLongBits(paramDouble);
      append('D');
      append((char)(int)(l >> 48));
      append((char)(int)(l >> 32));
      append((char)(int)(l >> 16));
      append((char)(int)l);
    }
    else
    {
      if (l < 3412047325613260800L)
        Kit.codeBug();
      if (l <= 65535L)
      {
        append('S');
        append((char)(int)l);
      }
      else
      {
        append('J');
        append((char)(int)(l >> 48));
        append((char)(int)(l >> 32));
        append((char)(int)(l >> 16));
        append((char)(int)l);
      }
    }
  }

  private void appendString(String paramString)
  {
    int i = paramString.length();
    int j = 1;
    if (i >= 32768)
      j = 2;
    int k = this.sourceTop + j + i;
    if (k > this.sourceBuffer.length)
      increaseSourceCapacity(k);
    if (i >= 32768)
    {
      this.sourceBuffer[this.sourceTop] = (char)(0x8000 | i >>> 16);
      this.sourceTop += 1;
    }
    this.sourceBuffer[this.sourceTop] = (char)i;
    this.sourceTop += 1;
    paramString.getChars(0, i, this.sourceBuffer, this.sourceTop);
    this.sourceTop = k;
  }

  private void append(char paramChar)
  {
    if (this.sourceTop == this.sourceBuffer.length)
      increaseSourceCapacity(this.sourceTop + 1);
    this.sourceBuffer[this.sourceTop] = paramChar;
    this.sourceTop += 1;
  }

  private void increaseSourceCapacity(int paramInt)
  {
    if (paramInt <= this.sourceBuffer.length)
      Kit.codeBug();
    int i = this.sourceBuffer.length * 2;
    if (i < paramInt)
      i = paramInt;
    char[] arrayOfChar = new char[i];
    System.arraycopy(this.sourceBuffer, 0, arrayOfChar, 0, this.sourceTop);
    this.sourceBuffer = arrayOfChar;
  }

  private String sourceToString(int paramInt)
  {
    if ((paramInt < 0) || (this.sourceTop < paramInt))
      Kit.codeBug();
    return new String(this.sourceBuffer, paramInt, this.sourceTop - paramInt);
  }

  public static String decompile(String paramString, int paramInt, UintMap paramUintMap)
  {
    int i6;
    int i7;
    int i = paramString.length();
    if (i == 0)
      return "";
    int j = paramUintMap.getInt(1, 0);
    if (j < 0)
      throw new IllegalArgumentException();
    int k = paramUintMap.getInt(2, 4);
    if (k < 0)
      throw new IllegalArgumentException();
    int l = paramUintMap.getInt(3, 2);
    if (l < 0)
      throw new IllegalArgumentException();
    StringBuffer localStringBuffer = new StringBuffer();
    int i1 = (0 != (paramInt & 0x1)) ? 1 : 0;
    int i2 = (0 != (paramInt & 0x2)) ? 1 : 0;
    int i3 = 0;
    int i4 = 0;
    int i5 = 0;
    if (paramString.charAt(i5) == 132)
    {
      ++i5;
      i6 = -1;
    }
    else
    {
      i6 = paramString.charAt(i5 + 1);
    }
    if (i2 == 0)
    {
      localStringBuffer.append('\n');
      for (i7 = 0; i7 < j; ++i7)
        localStringBuffer.append(' ');
    }
    else if (i6 == 2)
    {
      localStringBuffer.append('(');
    }
    while (true)
    {
      while (true)
      {
        while (true)
        {
          while (true)
          {
            if (i5 >= i)
              break label2200;
            switch (paramString.charAt(i5))
            {
            case '&':
            case '/':
              i5 = printSourceString(paramString, i5 + 1, false, localStringBuffer);
            case '(':
            case '\'':
            case ',':
            case '+':
            case ')':
            case '*':
            case 'i':
            case '':
            case 'U':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'O':
            case 'P':
            case '\1':
            case 'h':
            case '\30':
            case '\31':
            case 'l':
            case 'm':
            case 's':
            case '3':
            case 'w':
            case 'q':
            case 'r':
            case 'M':
            case 'x':
            case 'y':
            case '1':
            case 'n':
            case 't':
            case 'u':
            case 'o':
            case 'p':
            case '\4':
            case 'v':
            case 'N':
            case 'V':
            case ']':
            case '^':
            case '_':
            case '`':
            case 'a':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '[':
            case '\\':
            case 'b':
            case '@':
            case 'c':
            case 'd':
            case 'e':
            case '\t':
            case '\n':
            case '\11':
            case '-':
            case '.':
            case '\f':
            case '\r':
            case '\15':
            case '\14':
            case '\17':
            case '\16':
            case '4':
            case '\18':
            case '\19':
            case '\20':
            case ' ':
            case 'z':
            case '\26':
            case '\27':
            case '\28':
            case '\29':
            case 'f':
            case 'g':
            case '\21':
            case '\22':
            case '\23':
            case '\24':
            case '\25':
            case '':
            case '':
            case '':
            case '':
            case '\2':
            case '\3':
            case '\5':
            case '\6':
            case '\7':
            case '\b':
            case '!':
            case '"':
            case '#':
            case '$':
            case '%':
            case '0':
            case '2':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'j':
            case 'k':
            case '{':
            case '|':
            case '}':
            case '~':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            case '':
            }
          }
          i5 = printSourceString(paramString, i5 + 1, true, localStringBuffer);
        }
        i5 = printSourceNumber(paramString, i5 + 1, localStringBuffer);
      }
      localStringBuffer.append("true");
      break label2194:
      localStringBuffer.append("false");
      break label2194:
      localStringBuffer.append("null");
      break label2194:
      localStringBuffer.append("this");
      break label2194:
      ++i5;
      localStringBuffer.append("function ");
      break label2194:
      break label2194:
      localStringBuffer.append(", ");
      break label2194:
      ++i3;
      if (1 == getNext(paramString, i, i5))
        j += k;
      localStringBuffer.append('{');
      break label2194:
      --i3;
      if ((i1 != 0) && (i3 == 0))
        break label2194:
      localStringBuffer.append('}');
      switch (getNext(paramString, i, i5))
      {
      case 1:
      case 147:
        j -= k;
        break;
      case 109:
      case 113:
        j -= k;
        localStringBuffer.append(' ');
      }
      break label2194:
      localStringBuffer.append('(');
      break label2194:
      localStringBuffer.append(')');
      if (81 == getNext(paramString, i, i5))
      {
        localStringBuffer.append(' ');
        break label2194:
        localStringBuffer.append('[');
        break label2194:
        localStringBuffer.append(']');
        break label2194:
        if (i2 != 0)
          break label2194:
        i7 = 1;
        if (i4 == 0)
        {
          i4 = 1;
          if (i1 != 0)
          {
            localStringBuffer.setLength(0);
            j -= k;
            i7 = 0;
          }
        }
        if (i7 != 0)
          localStringBuffer.append('\n');
        if (i5 + 1 < i)
        {
          int i8 = 0;
          int i9 = paramString.charAt(i5 + 1);
          if ((i9 == 111) || (i9 == 112))
          {
            i8 = k - l;
          }
          else if (i9 == 82)
          {
            i8 = k;
          }
          else if (i9 == 38)
          {
            int i10 = getSourceStringEnd(paramString, i5 + 2);
            if (paramString.charAt(i10) == 'c');
          }
          for (i8 = k; i8 < j; ++i8)
            localStringBuffer.append(' ');
          break label2194:
          localStringBuffer.append('.');
          break label2194:
          localStringBuffer.append("new ");
          break label2194:
          localStringBuffer.append("delete ");
          break label2194:
          localStringBuffer.append("if ");
          break label2194:
          localStringBuffer.append("else ");
          break label2194:
          localStringBuffer.append("for ");
          break label2194:
          localStringBuffer.append(" in ");
          break label2194:
          localStringBuffer.append("with ");
          break label2194:
          localStringBuffer.append("while ");
          break label2194:
          localStringBuffer.append("do ");
          break label2194:
          localStringBuffer.append("try ");
          break label2194:
          localStringBuffer.append("catch ");
          break label2194:
          localStringBuffer.append("finally ");
          break label2194:
          localStringBuffer.append("throw ");
          break label2194:
          localStringBuffer.append("switch ");
          break label2194:
          localStringBuffer.append("break");
          if (38 == getNext(paramString, i, i5))
          {
            localStringBuffer.append(' ');
            break label2194:
            localStringBuffer.append("continue");
            if (38 == getNext(paramString, i, i5))
            {
              localStringBuffer.append(' ');
              break label2194:
              localStringBuffer.append("case ");
              break label2194:
              localStringBuffer.append("default");
              break label2194:
              localStringBuffer.append("return");
              if (78 != getNext(paramString, i, i5))
              {
                localStringBuffer.append(' ');
                break label2194:
                localStringBuffer.append("var ");
                break label2194:
                localStringBuffer.append(';');
                if (1 != getNext(paramString, i, i5))
                {
                  localStringBuffer.append(' ');
                  break label2194:
                  localStringBuffer.append(" = ");
                  break label2194:
                  localStringBuffer.append(" += ");
                  break label2194:
                  localStringBuffer.append(" -= ");
                  break label2194:
                  localStringBuffer.append(" *= ");
                  break label2194:
                  localStringBuffer.append(" /= ");
                  break label2194:
                  localStringBuffer.append(" %= ");
                  break label2194:
                  localStringBuffer.append(" |= ");
                  break label2194:
                  localStringBuffer.append(" ^= ");
                  break label2194:
                  localStringBuffer.append(" &= ");
                  break label2194:
                  localStringBuffer.append(" <<= ");
                  break label2194:
                  localStringBuffer.append(" >>= ");
                  break label2194:
                  localStringBuffer.append(" >>>= ");
                  break label2194:
                  localStringBuffer.append(" ? ");
                  break label2194:
                  localStringBuffer.append(':');
                  break label2194:
                  if (1 == getNext(paramString, i, i5))
                  {
                    localStringBuffer.append(':');
                  }
                  else
                  {
                    localStringBuffer.append(" : ");
                    break label2194:
                    localStringBuffer.append(" || ");
                    break label2194:
                    localStringBuffer.append(" && ");
                    break label2194:
                    localStringBuffer.append(" | ");
                    break label2194:
                    localStringBuffer.append(" ^ ");
                    break label2194:
                    localStringBuffer.append(" & ");
                    break label2194:
                    localStringBuffer.append(" === ");
                    break label2194:
                    localStringBuffer.append(" !== ");
                    break label2194:
                    localStringBuffer.append(" == ");
                    break label2194:
                    localStringBuffer.append(" != ");
                    break label2194:
                    localStringBuffer.append(" <= ");
                    break label2194:
                    localStringBuffer.append(" < ");
                    break label2194:
                    localStringBuffer.append(" >= ");
                    break label2194:
                    localStringBuffer.append(" > ");
                    break label2194:
                    localStringBuffer.append(" instanceof ");
                    break label2194:
                    localStringBuffer.append(" << ");
                    break label2194:
                    localStringBuffer.append(" >> ");
                    break label2194:
                    localStringBuffer.append(" >>> ");
                    break label2194:
                    localStringBuffer.append("typeof ");
                    break label2194:
                    localStringBuffer.append("void ");
                    break label2194:
                    localStringBuffer.append('!');
                    break label2194:
                    localStringBuffer.append('~');
                    break label2194:
                    localStringBuffer.append('+');
                    break label2194:
                    localStringBuffer.append('-');
                    break label2194:
                    localStringBuffer.append("++");
                    break label2194:
                    localStringBuffer.append("--");
                    break label2194:
                    localStringBuffer.append(" + ");
                    break label2194:
                    localStringBuffer.append(" - ");
                    break label2194:
                    localStringBuffer.append(" * ");
                    break label2194:
                    localStringBuffer.append(" / ");
                    break label2194:
                    localStringBuffer.append(" % ");
                    break label2194:
                    localStringBuffer.append("::");
                    break label2194:
                    localStringBuffer.append("..");
                    break label2194:
                    localStringBuffer.append(".(");
                    break label2194:
                    localStringBuffer.append('@');
                    break label2194:
                    throw new RuntimeException();
                  }
                }
              }
            }
          }
        }
      }
      label2194: ++i5;
    }
    if (i2 == 0)
      if (i1 == 0)
        label2200: localStringBuffer.append('\n');
    else if (i6 == 2)
      localStringBuffer.append(')');
    return localStringBuffer.toString();
  }

  private static int getNext(String paramString, int paramInt1, int paramInt2)
  {
    return ((paramInt2 + 1 < paramInt1) ? paramString.charAt(paramInt2 + 1) : 0);
  }

  private static int getSourceStringEnd(String paramString, int paramInt)
  {
    return printSourceString(paramString, paramInt, false, null);
  }

  private static int printSourceString(String paramString, int paramInt, boolean paramBoolean, StringBuffer paramStringBuffer)
  {
    int i = paramString.charAt(paramInt);
    ++paramInt;
    if ((0x8000 & i) != 0)
    {
      i = (0x7FFF & i) << 16 | paramString.charAt(paramInt);
      ++paramInt;
    }
    if (paramStringBuffer != null)
    {
      String str = paramString.substring(paramInt, paramInt + i);
      if (!(paramBoolean))
      {
        paramStringBuffer.append(str);
      }
      else
      {
        paramStringBuffer.append('"');
        paramStringBuffer.append(ScriptRuntime.escapeString(str));
        paramStringBuffer.append('"');
      }
    }
    return (paramInt + i);
  }

  private static int printSourceNumber(String paramString, int paramInt, StringBuffer paramStringBuffer)
  {
    double d = 0D;
    int i = paramString.charAt(paramInt);
    ++paramInt;
    if (i == 83)
    {
      if (paramStringBuffer != null)
      {
        int j = paramString.charAt(paramInt);
        d = j;
      }
      ++paramInt;
    }
    else if ((i == 74) || (i == 68))
    {
      if (paramStringBuffer != null)
      {
        long l = paramString.charAt(paramInt) << '0';
        l |= paramString.charAt(paramInt + 1) << ' ';
        l |= paramString.charAt(paramInt + 2) << '\16';
        l |= paramString.charAt(paramInt + 3);
        if (i == 74)
          d = l;
        else
          d = Double.longBitsToDouble(l);
      }
      paramInt += 4;
    }
    else
    {
      throw new RuntimeException();
    }
    if (paramStringBuffer != null)
      paramStringBuffer.append(ScriptRuntime.numberToString(d, 10));
    return paramInt;
  }
}