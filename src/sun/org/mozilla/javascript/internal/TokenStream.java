package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.Reader;

class TokenStream
{
  private static final int EOF_CHAR = -1;
  private boolean dirtyLine;
  String regExpFlags;
  private String line;
  private boolean fromEval;
  private String string = "";
  private double number;
  private char[] stringBuffer = new char[128];
  private int stringBufferTop;
  private ObjToIntMap allStrings = new ObjToIntMap(50);
  private final int[] ungetBuffer = new int[3];
  private int ungetCursor;
  private boolean hitEOF = false;
  private int lineStart = 0;
  private int lineno;
  private int lineEndChar = -1;
  private String sourceString;
  private Reader sourceReader;
  private char[] sourceBuffer;
  private int sourceEnd;
  private int sourceCursor;
  private boolean xmlIsAttribute;
  private boolean xmlIsTagContent;
  private int xmlOpenTagsCount;
  private Parser parser;

  TokenStream(Parser paramParser, Reader paramReader, String paramString, int paramInt)
  {
    this.parser = paramParser;
    this.lineno = paramInt;
    if (paramReader != null)
    {
      if (paramString != null)
        Kit.codeBug();
      this.sourceReader = paramReader;
      this.sourceBuffer = new char[512];
      this.sourceEnd = 0;
    }
    else
    {
      if (paramString == null)
        Kit.codeBug();
      this.sourceString = paramString;
      this.sourceEnd = paramString.length();
    }
    this.sourceCursor = 0;
  }

  String tokenToString(int paramInt)
  {
    return "";
  }

  static boolean isKeyword(String paramString)
  {
    return (0 != stringToKeyword(paramString));
  }

  private static int stringToKeyword(String paramString)
  {
    int j;
    String str1 = paramString;
    int i = 0;
    String str2 = null;
    switch (str1.length())
    {
    case 2:
      j = str1.charAt(1);
      if (j == 102)
      {
        if (str1.charAt(0) != 'i')
          break label1671;
        i = 108;
        break label1696:
      }
      if (j == 110)
      {
        if (str1.charAt(0) != 'i')
          break label1671;
        i = 51;
        break label1696:
      }
      if ((j == 111) && (str1.charAt(0) == 'd'))
        i = 114;
      break;
    case 3:
      switch (str1.charAt(0))
      {
      case 'f':
        if ((str1.charAt(2) == 'r') && (str1.charAt(1) == 'o'))
          i = 115;
        break;
      case 'i':
        if ((str1.charAt(2) == 't') && (str1.charAt(1) == 'n'))
          i = 123;
        break;
      case 'n':
        if ((str1.charAt(2) == 'w') && (str1.charAt(1) == 'e'))
          i = 30;
        break;
      case 't':
        if ((str1.charAt(2) == 'y') && (str1.charAt(1) == 'r'))
          i = 77;
        break;
      case 'v':
        if ((str1.charAt(2) == 'r') && (str1.charAt(1) == 'a'))
        {
          i = 118;
          break label1696:
        }
      }
      break;
    case 4:
      switch (str1.charAt(0))
      {
      case 'b':
        str2 = "byte";
        i = 123;
        break;
      case 'c':
        j = str1.charAt(3);
        if (j == 101)
        {
          if ((str1.charAt(2) != 's') || (str1.charAt(1) != 'a'))
            break label1671;
          i = 111;
          break label1696:
        }
        if ((j == 114) && (str1.charAt(2) == 'a') && (str1.charAt(1) == 'h'))
          i = 123;
        break;
      case 'e':
        j = str1.charAt(3);
        if (j == 101)
        {
          if ((str1.charAt(2) != 's') || (str1.charAt(1) != 'l'))
            break label1671;
          i = 109;
          break label1696:
        }
        if ((j == 109) && (str1.charAt(2) == 'u') && (str1.charAt(1) == 'n'))
          i = 123;
        break;
      case 'g':
        str2 = "goto";
        i = 123;
        break;
      case 'l':
        str2 = "long";
        i = 123;
        break;
      case 'n':
        str2 = "null";
        i = 41;
        break;
      case 't':
        j = str1.charAt(3);
        if (j == 101)
        {
          if ((str1.charAt(2) != 'u') || (str1.charAt(1) != 'r'))
            break label1671;
          i = 44;
          break label1696:
        }
        if ((j == 115) && (str1.charAt(2) == 'i') && (str1.charAt(1) == 'h'))
          i = 42;
        break;
      case 'v':
        str2 = "void";
        i = 122;
        break;
      case 'w':
        str2 = "with";
        i = 119;
        break label1671:
      case 'd':
      case 'f':
      case 'h':
      case 'i':
      case 'j':
      case 'k':
      case 'm':
      case 'o':
      case 'p':
      case 'q':
      case 'r':
      case 's':
      case 'u':
      }
      break;
    case 5:
      switch (str1.charAt(2))
      {
      case 'a':
        str2 = "class";
        i = 123;
        break;
      case 'e':
        str2 = "break";
        i = 116;
        break;
      case 'i':
        str2 = "while";
        i = 113;
        break;
      case 'l':
        str2 = "false";
        i = 43;
        break;
      case 'n':
        j = str1.charAt(0);
        if (j == 99)
        {
          str2 = "const";
          i = 123;
        }
        else if (j == 102)
        {
          str2 = "final";
          i = 123;
        }
        break;
      case 'o':
        j = str1.charAt(0);
        if (j == 102)
        {
          str2 = "float";
          i = 123;
        }
        else if (j == 115)
        {
          str2 = "short";
          i = 123;
        }
        break;
      case 'p':
        str2 = "super";
        i = 123;
        break;
      case 'r':
        str2 = "throw";
        i = 49;
        break;
      case 't':
        str2 = "catch";
        i = 120;
        break label1671:
      case 'b':
      case 'c':
      case 'd':
      case 'f':
      case 'g':
      case 'h':
      case 'j':
      case 'k':
      case 'm':
      case 'q':
      case 's':
      }
      break;
    case 6:
      switch (str1.charAt(1))
      {
      case 'a':
        str2 = "native";
        i = 123;
        break;
      case 'e':
        j = str1.charAt(0);
        if (j == 100)
        {
          str2 = "delete";
          i = 31;
        }
        else if (j == 114)
        {
          str2 = "return";
          i = 4;
        }
        break;
      case 'h':
        str2 = "throws";
        i = 123;
        break;
      case 'm':
        str2 = "import";
        i = 107;
        break;
      case 'o':
        str2 = "double";
        i = 123;
        break;
      case 't':
        str2 = "static";
        i = 123;
        break;
      case 'u':
        str2 = "public";
        i = 123;
        break;
      case 'w':
        str2 = "switch";
        i = 110;
        break;
      case 'x':
        str2 = "export";
        i = 106;
        break;
      case 'y':
        str2 = "typeof";
        i = 32;
        break label1671:
      case 'b':
      case 'c':
      case 'd':
      case 'f':
      case 'g':
      case 'i':
      case 'j':
      case 'k':
      case 'l':
      case 'n':
      case 'p':
      case 'q':
      case 'r':
      case 's':
      case 'v':
      }
      break;
    case 7:
      switch (str1.charAt(1))
      {
      case 'a':
        str2 = "package";
        i = 123;
        break;
      case 'e':
        str2 = "default";
        i = 112;
        break;
      case 'i':
        str2 = "finally";
        i = 121;
        break;
      case 'o':
        str2 = "boolean";
        i = 123;
        break;
      case 'r':
        str2 = "private";
        i = 123;
        break;
      case 'x':
        str2 = "extends";
        i = 123;
        break label1671:
      }
      break;
    case 8:
      switch (str1.charAt(0))
      {
      case 'a':
        str2 = "abstract";
        i = 123;
        break;
      case 'c':
        str2 = "continue";
        i = 117;
        break;
      case 'd':
        str2 = "debugger";
        i = 123;
        break;
      case 'f':
        str2 = "function";
        i = 105;
        break;
      case 'v':
        str2 = "volatile";
        i = 123;
        break label1671:
      }
      break;
    case 9:
      j = str1.charAt(0);
      if (j == 105)
      {
        str2 = "interface";
        i = 123;
      }
      else if (j == 112)
      {
        str2 = "protected";
        i = 123;
      }
      else if (j == 116)
      {
        str2 = "transient";
        i = 123;
      }
      break;
    case 10:
      j = str1.charAt(1);
      if (j == 109)
      {
        str2 = "implements";
        i = 123;
      }
      else if (j == 110)
      {
        str2 = "instanceof";
        i = 52;
      }
      break;
    case 12:
      str2 = "synchronized";
      i = 123;
    case 11:
    }
    if ((str2 != null) && (str2 != str1) && (!(str2.equals(str1))))
      label1671: i = 0;
    if (i == 0)
      label1696: return 0;
    return (i & 0xFF);
  }

  final int getLineno()
  {
    return this.lineno;
  }

  final String getString()
  {
    return this.string;
  }

  final double getNumber()
  {
    return this.number;
  }

  final boolean eof()
  {
    return this.hitEOF;
  }

  final int getToken()
    throws IOException
  {
    while (true)
    {
      int k;
      while (true)
      {
        while (true)
        {
          boolean bool;
          int i1;
          do
          {
            i = getChar();
            if (i == -1)
              return 0;
            if (i == 10)
            {
              this.dirtyLine = false;
              return 1;
            }
          }
          while (isJSSpace(i));
          if (i != 45)
            this.dirtyLine = true;
          if (i == 64)
            return 143;
          int j = 0;
          if (i == 92)
          {
            i = getChar();
            if (i == 117)
            {
              bool = true;
              j = 1;
              this.stringBufferTop = 0;
            }
            else
            {
              bool = false;
              ungetChar(i);
              i = 92;
            }
          }
          else
          {
            bool = Character.isJavaIdentifierStart((char)i);
            if (bool)
            {
              this.stringBufferTop = 0;
              addToString(i);
            }
          }
          if (bool)
          {
            int i2;
            k = j;
            while (true)
            {
              while (true)
              {
                while (j != 0)
                {
                  int l = 0;
                  for (i2 = 0; i2 != 4; ++i2)
                  {
                    i = getChar();
                    l = Kit.xDigitToInt(i, l);
                    if (l < 0)
                      break;
                  }
                  if (l < 0)
                  {
                    this.parser.addError("msg.invalid.escape");
                    return -1;
                  }
                  addToString(l);
                  j = 0;
                }
                i = getChar();
                if (i != 92)
                  break label238;
                i = getChar();
                if (i != 117)
                  break;
                j = 1;
                k = 1;
              }
              this.parser.addError("msg.illegal.character");
              return -1;
              label238: if (i == -1)
                break;
              if (!(Character.isJavaIdentifierPart((char)i)))
                break;
              addToString(i);
            }
            ungetChar(i);
            String str1 = getStringFromBuffer();
            if (k == 0)
            {
              i2 = stringToKeyword(str1);
              if (i2 != 0)
              {
                if (i2 != 123)
                  return i2;
                if (!(this.parser.compilerEnv.isReservedKeywordAsIdentifier()))
                  return i2;
                this.parser.addWarning("msg.reserved.keyword", str1);
              }
            }
            this.string = ((String)this.allStrings.intern(str1));
            return 38;
          }
          if ((isDigit(i)) || ((i == 46) && (isDigit(peekChar()))))
          {
            this.stringBufferTop = 0;
            k = 10;
            if (i == 48)
            {
              i = getChar();
              if ((i == 120) || (i == 88))
              {
                k = 16;
                i = getChar();
              }
              else if (isDigit(i))
              {
                k = 8;
              }
              else
              {
                addToString(48);
              }
            }
            if (k == 16)
              while (true)
              {
                if (0 > Kit.xDigitToInt(i, 0))
                  break label526;
                addToString(i);
                i = getChar();
              }
            while ((48 <= i) && (i <= 57))
            {
              if ((k == 8) && (i >= 56))
              {
                this.parser.addWarning("msg.bad.octal.literal", (i == 56) ? "8" : "9");
                k = 10;
              }
              addToString(i);
              i = getChar();
            }
            label526: i1 = 1;
            if ((k == 10) && (((i == 46) || (i == 101) || (i == 69))))
            {
              i1 = 0;
              if (i == 46)
                do
                {
                  addToString(i);
                  i = getChar();
                }
                while (isDigit(i));
              if ((i == 101) || (i == 69))
              {
                addToString(i);
                i = getChar();
                if ((i == 43) || (i == 45))
                {
                  addToString(i);
                  i = getChar();
                }
                if (!(isDigit(i)))
                {
                  this.parser.addError("msg.missing.exponent");
                  return -1;
                }
                do
                {
                  addToString(i);
                  i = getChar();
                }
                while (isDigit(i));
              }
            }
            ungetChar(i);
            String str3 = getStringFromBuffer();
            if ((k == 10) && (i1 == 0))
              try
              {
                d = Double.valueOf(str3).doubleValue();
              }
              catch (NumberFormatException localNumberFormatException)
              {
                this.parser.addError("msg.caught.nfe");
                return -1;
              }
            double d = ScriptRuntime.stringToNumber(str3, 0, k);
            this.number = d;
            return 39;
          }
          if ((i == 34) || (i == 39))
          {
            k = i;
            this.stringBufferTop = 0;
            i = getChar();
            while (true)
            {
              int i4;
              while (true)
              {
                while (true)
                {
                  while (true)
                  {
                    if (i == k)
                      break label1169;
                    if ((i == 10) || (i == -1))
                    {
                      ungetChar(i);
                      this.parser.addError("msg.unterminated.string.lit");
                      return -1;
                    }
                    if (i != 92)
                      break label1156;
                    i = getChar();
                    switch (i)
                    {
                    case 98:
                      i = 8;
                      break;
                    case 102:
                      i = 12;
                      break;
                    case 110:
                      i = 10;
                      break;
                    case 114:
                      i = 13;
                      break;
                    case 116:
                      i = 9;
                      break;
                    case 118:
                      i = 11;
                      break;
                    case 117:
                      int i3 = this.stringBufferTop;
                      addToString(117);
                      i1 = 0;
                      i4 = 0;
                      while (true)
                      {
                        while (true)
                        {
                          if (i4 == 4)
                            break label972;
                          i = getChar();
                          i1 = Kit.xDigitToInt(i, i1);
                          if (i1 >= 0)
                            break;
                        }
                        addToString(i);
                        ++i4;
                      }
                      this.stringBufferTop = i3;
                      i = i1;
                      break;
                    case 120:
                      label972: i = getChar();
                      i1 = Kit.xDigitToInt(i, 0);
                      if (i1 >= 0)
                        break;
                      addToString(120);
                    case 10:
                    }
                  }
                  i4 = i;
                  i = getChar();
                  i1 = Kit.xDigitToInt(i, i1);
                  if (i1 >= 0)
                    break;
                  addToString(120);
                  addToString(i4);
                }
                i = i1;
                break label1156:
                i = getChar();
              }
              if ((48 <= i) && (i < 56))
              {
                i4 = i - 48;
                i = getChar();
                if ((48 <= i) && (i < 56))
                {
                  i4 = 8 * i4 + i - 48;
                  i = getChar();
                  if ((48 <= i) && (i < 56) && (i4 <= 31))
                  {
                    i4 = 8 * i4 + i - 48;
                    i = getChar();
                  }
                }
                ungetChar(i);
                i = i4;
              }
              label1156: addToString(i);
              i = getChar();
            }
            label1169: String str2 = getStringFromBuffer();
            this.string = ((String)this.allStrings.intern(str2));
            return 40;
          }
          switch (i)
          {
          case 59:
            return 78;
          case 91:
            return 79;
          case 93:
            return 80;
          case 123:
            return 81;
          case 125:
            return 82;
          case 40:
            return 83;
          case 41:
            return 84;
          case 44:
            return 85;
          case 63:
            return 98;
          case 58:
            if (matchChar(58))
              return 140;
            return 99;
          case 46:
            if (matchChar(46))
              return 139;
            if (matchChar(40))
              return 142;
            return 104;
          case 124:
            if (matchChar(124))
              return 100;
            if (matchChar(61))
              return 87;
            return 9;
          case 94:
            if (matchChar(61))
              return 88;
            return 10;
          case 38:
            if (matchChar(38))
              return 101;
            if (matchChar(61))
              return 89;
            return 11;
          case 61:
            if (matchChar(61))
            {
              if (matchChar(61))
                return 45;
              return 12;
            }
            return 86;
          case 33:
            if (matchChar(61))
            {
              if (matchChar(61))
                return 46;
              return 13;
            }
            return 26;
          case 60:
            if (!(matchChar(33)))
              break label1825;
            if (!(matchChar(45)))
              break label1819;
            if (!(matchChar(45)))
              break;
            skipLine();
          case 62:
          case 42:
          case 47:
          case 37:
          case 126:
          case 43:
          case 45:
          case 34:
          case 35:
          case 36:
          case 39:
          case 48:
          case 49:
          case 50:
          case 51:
          case 52:
          case 53:
          case 54:
          case 55:
          case 56:
          case 57:
          case 64:
          case 65:
          case 66:
          case 67:
          case 68:
          case 69:
          case 70:
          case 71:
          case 72:
          case 73:
          case 74:
          case 75:
          case 76:
          case 77:
          case 78:
          case 79:
          case 80:
          case 81:
          case 82:
          case 83:
          case 84:
          case 85:
          case 86:
          case 87:
          case 88:
          case 89:
          case 90:
          case 92:
          case 95:
          case 96:
          case 97:
          case 98:
          case 99:
          case 100:
          case 101:
          case 102:
          case 103:
          case 104:
          case 105:
          case 106:
          case 107:
          case 108:
          case 109:
          case 110:
          case 111:
          case 112:
          case 113:
          case 114:
          case 115:
          case 116:
          case 117:
          case 118:
          case 119:
          case 120:
          case 121:
          case 122:
          }
        }
        ungetChar(45);
        label1819: ungetChar(33);
        if (matchChar(60))
        {
          if (matchChar(61))
            label1825: return 90;
          return 18;
        }
        if (matchChar(61))
          return 15;
        return 14;
        if (matchChar(62))
        {
          if (matchChar(62))
          {
            if (matchChar(61))
              return 92;
            return 20;
          }
          if (matchChar(61))
            return 91;
          return 19;
        }
        if (matchChar(61))
          return 17;
        return 16;
        if (matchChar(61))
          return 95;
        return 23;
        if (!(matchChar(47)))
          break;
        skipLine();
      }
      if (matchChar(42))
      {
        k = 0;
        while (true)
        {
          while (true)
            do
            {
              while (true)
              {
                i = getChar();
                if (i == -1)
                {
                  this.parser.addError("msg.unterminated.comment");
                  return -1;
                }
                if (i != 42)
                  break;
                k = 1;
              }
              if (i != 47)
                break label2017;
            }
            while (k == 0);
          label2017: k = 0;
        }
      }
      if (matchChar(61))
        return 96;
      return 24;
      if (matchChar(61))
        return 97;
      return 25;
      return 27;
      if (matchChar(61))
        return 93;
      if (matchChar(43))
        return 102;
      return 21;
      if (matchChar(61))
      {
        i = 94;
        break label2139:
      }
      if (!(matchChar(45)))
        break label2136;
      if ((this.dirtyLine) || (!(matchChar(62))))
        break;
      skipLine();
    }
    int i = 103;
    break label2139:
    label2136: i = 22;
    label2139: this.dirtyLine = true;
    return i;
    this.parser.addError("msg.illegal.character");
    return -1;
  }

  private static boolean isAlpha(int paramInt)
  {
    if (paramInt <= 90)
      return (65 <= paramInt);
    return ((97 <= paramInt) && (paramInt <= 122));
  }

  static boolean isDigit(int paramInt)
  {
    return ((48 <= paramInt) && (paramInt <= 57));
  }

  static boolean isJSSpace(int paramInt)
  {
    if (paramInt <= 127)
      return ((paramInt == 32) || (paramInt == 9) || (paramInt == 12) || (paramInt == 11));
    return ((paramInt == 160) || (Character.getType((char)paramInt) == 12));
  }

  private static boolean isJSFormatChar(int paramInt)
  {
    return ((paramInt > 127) && (Character.getType((char)paramInt) == 16));
  }

  void readRegExp(int paramInt)
    throws IOException
  {
    this.stringBufferTop = 0;
    if (paramInt == 96)
      addToString(61);
    else if (paramInt != 24)
      Kit.codeBug();
    while ((i = getChar()) != 47)
    {
      int i;
      if ((i == 10) || (i == -1))
      {
        ungetChar(i);
        throw this.parser.reportError("msg.unterminated.re.lit");
      }
      if (i == 92)
      {
        addToString(i);
        i = getChar();
      }
      addToString(i);
    }
    int j = this.stringBufferTop;
    while (true)
    {
      while (true)
      {
        while (matchChar(103))
          addToString(103);
        if (!(matchChar(105)))
          break;
        addToString(105);
      }
      if (!(matchChar(109)))
        break;
      addToString(109);
    }
    if (isAlpha(peekChar()))
      throw this.parser.reportError("msg.invalid.re.flag");
    this.string = new String(this.stringBuffer, 0, j);
    this.regExpFlags = new String(this.stringBuffer, j, this.stringBufferTop - j);
  }

  boolean isXMLAttribute()
  {
    return this.xmlIsAttribute;
  }

  int getFirstXMLToken()
    throws IOException
  {
    this.xmlOpenTagsCount = 0;
    this.xmlIsAttribute = false;
    this.xmlIsTagContent = false;
    ungetChar(60);
    return getNextXMLToken();
  }

  int getNextXMLToken()
    throws IOException
  {
    this.stringBufferTop = 0;
    for (int i = getChar(); i != -1; i = getChar())
    {
      if (this.xmlIsTagContent)
      {
        switch (i)
        {
        case 62:
          addToString(i);
          this.xmlIsTagContent = false;
          this.xmlIsAttribute = false;
          break;
        case 47:
          addToString(i);
          if (peekChar() == 62)
          {
            i = getChar();
            addToString(i);
            this.xmlIsTagContent = false;
            this.xmlOpenTagsCount -= 1;
          }
          break;
        case 123:
          ungetChar(i);
          this.string = getStringFromBuffer();
          return 141;
        case 34:
        case 39:
          addToString(i);
          if (!(readQuotedString(i)))
            return -1;
        case 61:
          addToString(i);
          this.xmlIsAttribute = true;
          break;
        case 9:
        case 10:
        case 13:
        case 32:
          addToString(i);
          break;
        default:
          addToString(i);
          this.xmlIsAttribute = false;
        }
        if ((this.xmlIsTagContent) || (this.xmlOpenTagsCount != 0))
          break label686;
        this.string = getStringFromBuffer();
        return 144;
      }
      switch (i)
      {
      case 60:
        addToString(i);
        i = peekChar();
        switch (i)
        {
        case 33:
          i = getChar();
          addToString(i);
          i = peekChar();
          switch (i)
          {
          case 45:
            i = getChar();
            addToString(i);
            i = getChar();
            if (i == 45)
            {
              addToString(i);
              if (readXmlComment())
                break label686;
              return -1;
            }
            this.stringBufferTop = 0;
            this.string = null;
            this.parser.addError("msg.XML.bad.form");
            return -1;
          case 91:
            i = getChar();
            addToString(i);
            if ((getChar() == 67) && (getChar() == 68) && (getChar() == 65) && (getChar() == 84) && (getChar() == 65) && (getChar() == 91))
            {
              addToString(67);
              addToString(68);
              addToString(65);
              addToString(84);
              addToString(65);
              addToString(91);
              if (readCDATA())
                break label686;
              return -1;
            }
            this.stringBufferTop = 0;
            this.string = null;
            this.parser.addError("msg.XML.bad.form");
            return -1;
          }
          if (!(readEntity()))
            return -1;
        case 63:
          i = getChar();
          addToString(i);
          if (!(readPI()))
            return -1;
        case 47:
          i = getChar();
          addToString(i);
          if (this.xmlOpenTagsCount == 0)
          {
            this.stringBufferTop = 0;
            this.string = null;
            this.parser.addError("msg.XML.bad.form");
            return -1;
          }
          this.xmlIsTagContent = true;
          this.xmlOpenTagsCount -= 1;
          break;
        default:
          this.xmlIsTagContent = true;
          this.xmlOpenTagsCount += 1;
        }
        break;
      case 123:
        ungetChar(i);
        this.string = getStringFromBuffer();
        label686: return 141;
      default:
        addToString(i);
      }
    }
    this.stringBufferTop = 0;
    this.string = null;
    this.parser.addError("msg.XML.bad.form");
    return -1;
  }

  private boolean readQuotedString(int paramInt)
    throws IOException
  {
    for (int i = getChar(); i != -1; i = getChar())
    {
      addToString(i);
      if (i == paramInt)
        return true;
    }
    this.stringBufferTop = 0;
    this.string = null;
    this.parser.addError("msg.XML.bad.form");
    return false;
  }

  private boolean readXmlComment()
    throws IOException
  {
    int i = getChar();
    while (true)
    {
      do
      {
        if (i == -1)
          break label69;
        addToString(i);
        if ((i != 45) || (peekChar() != 45))
          break label61;
        i = getChar();
        addToString(i);
      }
      while (peekChar() != 62);
      i = getChar();
      addToString(i);
      return true;
      label61: i = getChar();
    }
    label69: this.stringBufferTop = 0;
    this.string = null;
    this.parser.addError("msg.XML.bad.form");
    return false;
  }

  private boolean readCDATA()
    throws IOException
  {
    int i = getChar();
    while (true)
    {
      do
      {
        if (i == -1)
          break label69;
        addToString(i);
        if ((i != 93) || (peekChar() != 93))
          break label61;
        i = getChar();
        addToString(i);
      }
      while (peekChar() != 62);
      i = getChar();
      addToString(i);
      return true;
      label61: i = getChar();
    }
    label69: this.stringBufferTop = 0;
    this.string = null;
    this.parser.addError("msg.XML.bad.form");
    return false;
  }

  private boolean readEntity()
    throws IOException
  {
    int i = 1;
    for (int j = getChar(); j != -1; j = getChar())
    {
      addToString(j);
      switch (j)
      {
      case 60:
        ++i;
        break;
      case 62:
        if (--i == 0)
          return true;
      }
    }
    this.stringBufferTop = 0;
    this.string = null;
    this.parser.addError("msg.XML.bad.form");
    return false;
  }

  private boolean readPI()
    throws IOException
  {
    for (int i = getChar(); i != -1; i = getChar())
    {
      addToString(i);
      if ((i == 63) && (peekChar() == 62))
      {
        i = getChar();
        addToString(i);
        return true;
      }
    }
    this.stringBufferTop = 0;
    this.string = null;
    this.parser.addError("msg.XML.bad.form");
    return false;
  }

  private String getStringFromBuffer()
  {
    return new String(this.stringBuffer, 0, this.stringBufferTop);
  }

  private void addToString(int paramInt)
  {
    int i = this.stringBufferTop;
    if (i == this.stringBuffer.length)
    {
      char[] arrayOfChar = new char[this.stringBuffer.length * 2];
      System.arraycopy(this.stringBuffer, 0, arrayOfChar, 0, i);
      this.stringBuffer = arrayOfChar;
    }
    this.stringBuffer[i] = (char)paramInt;
    this.stringBufferTop = (i + 1);
  }

  private void ungetChar(int paramInt)
  {
    if ((this.ungetCursor != 0) && (this.ungetBuffer[(this.ungetCursor - 1)] == 10))
      Kit.codeBug();
    this.ungetBuffer[(this.ungetCursor++)] = paramInt;
  }

  private boolean matchChar(int paramInt)
    throws IOException
  {
    int i = getChar();
    if (i == paramInt)
      return true;
    ungetChar(i);
    return false;
  }

  private int peekChar()
    throws IOException
  {
    int i = getChar();
    ungetChar(i);
    return i;
  }

  private int getChar()
    throws IOException
  {
    int i;
    if (this.ungetCursor != 0)
      return this.ungetBuffer[(--this.ungetCursor)];
    while (true)
    {
      while (true)
      {
        if (this.sourceString != null)
        {
          if (this.sourceCursor == this.sourceEnd)
          {
            this.hitEOF = true;
            return -1;
          }
          i = this.sourceString.charAt(this.sourceCursor++);
        }
        else
        {
          if ((this.sourceCursor == this.sourceEnd) && (!(fillSourceBuffer())))
          {
            this.hitEOF = true;
            return -1;
          }
          i = this.sourceBuffer[(this.sourceCursor++)];
        }
        if (this.lineEndChar < 0)
          break label169;
        if ((this.lineEndChar != 13) || (i != 10))
          break;
        this.lineEndChar = 10;
      }
      this.lineEndChar = -1;
      this.lineStart = (this.sourceCursor - 1);
      this.lineno += 1;
      if (i <= 127)
      {
        label169: if ((i != 10) && (i != 13))
          break label223;
        this.lineEndChar = i;
        i = 10;
        break label223:
      }
      if (!(isJSFormatChar(i)))
        break;
    }
    if (ScriptRuntime.isJSLineTerminator(i))
    {
      this.lineEndChar = i;
      i = 10;
    }
    label223: return i;
  }

  private void skipLine()
    throws IOException
  {
    int i;
    while (((i = getChar()) != -1) && (i != 10));
    ungetChar(i);
  }

  final int getOffset()
  {
    int i = this.sourceCursor - this.lineStart;
    if (this.lineEndChar >= 0)
      --i;
    return i;
  }

  final String getLine()
  {
    int j;
    if (this.sourceString != null)
    {
      i = this.sourceCursor;
      if (this.lineEndChar >= 0)
        --i;
      else
        while (i != this.sourceEnd)
        {
          j = this.sourceString.charAt(i);
          if (ScriptRuntime.isJSLineTerminator(j))
            break;
          ++i;
        }
      return this.sourceString.substring(this.lineStart, i);
    }
    int i = this.sourceCursor - this.lineStart;
    if (this.lineEndChar >= 0)
      --i;
    else
      while (true)
      {
        j = this.lineStart + i;
        if (j == this.sourceEnd)
        {
          try
          {
            if (!(fillSourceBuffer()))
              break label156:
          }
          catch (IOException localIOException)
          {
            break label156:
          }
          j = this.lineStart + i;
        }
        int k = this.sourceBuffer[j];
        if (ScriptRuntime.isJSLineTerminator(k))
          break;
        ++i;
      }
    label156: return new String(this.sourceBuffer, this.lineStart, i);
  }

  private boolean fillSourceBuffer()
    throws IOException
  {
    if (this.sourceString != null)
      Kit.codeBug();
    if (this.sourceEnd == this.sourceBuffer.length)
      if (this.lineStart != 0)
      {
        System.arraycopy(this.sourceBuffer, this.lineStart, this.sourceBuffer, 0, this.sourceEnd - this.lineStart);
        this.sourceEnd -= this.lineStart;
        this.sourceCursor -= this.lineStart;
        this.lineStart = 0;
      }
      else
      {
        char[] arrayOfChar = new char[this.sourceBuffer.length * 2];
        System.arraycopy(this.sourceBuffer, 0, arrayOfChar, 0, this.sourceEnd);
        this.sourceBuffer = arrayOfChar;
      }
    int i = this.sourceReader.read(this.sourceBuffer, this.sourceEnd, this.sourceBuffer.length - this.sourceEnd);
    if (i < 0)
      return false;
    this.sourceEnd += i;
    return true;
  }
}