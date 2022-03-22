package sun.org.mozilla.javascript.internal.regexp;

import J;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.IdFunctionObject;
import sun.org.mozilla.javascript.internal.IdScriptableObject;
import sun.org.mozilla.javascript.internal.Kit;
import sun.org.mozilla.javascript.internal.ScriptRuntime;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.Undefined;

public class NativeRegExp extends IdScriptableObject
  implements Function
{
  static final long serialVersionUID = 4965263491464903264L;
  private static final Object REGEXP_TAG = new Object();
  public static final int JSREG_GLOB = 1;
  public static final int JSREG_FOLD = 2;
  public static final int JSREG_MULTILINE = 4;
  public static final int TEST = 0;
  public static final int MATCH = 1;
  public static final int PREFIX = 2;
  private static final boolean debug = 0;
  private static final byte REOP_EMPTY = 0;
  private static final byte REOP_ALT = 1;
  private static final byte REOP_BOL = 2;
  private static final byte REOP_EOL = 3;
  private static final byte REOP_WBDRY = 4;
  private static final byte REOP_WNONBDRY = 5;
  private static final byte REOP_QUANT = 6;
  private static final byte REOP_STAR = 7;
  private static final byte REOP_PLUS = 8;
  private static final byte REOP_OPT = 9;
  private static final byte REOP_LPAREN = 10;
  private static final byte REOP_RPAREN = 11;
  private static final byte REOP_DOT = 12;
  private static final byte REOP_CCLASS = 13;
  private static final byte REOP_DIGIT = 14;
  private static final byte REOP_NONDIGIT = 15;
  private static final byte REOP_ALNUM = 16;
  private static final byte REOP_NONALNUM = 17;
  private static final byte REOP_SPACE = 18;
  private static final byte REOP_NONSPACE = 19;
  private static final byte REOP_BACKREF = 20;
  private static final byte REOP_FLAT = 21;
  private static final byte REOP_FLAT1 = 22;
  private static final byte REOP_JUMP = 23;
  private static final byte REOP_DOTSTAR = 24;
  private static final byte REOP_ANCHOR = 25;
  private static final byte REOP_EOLONLY = 26;
  private static final byte REOP_UCFLAT = 27;
  private static final byte REOP_UCFLAT1 = 28;
  private static final byte REOP_UCCLASS = 29;
  private static final byte REOP_NUCCLASS = 30;
  private static final byte REOP_BACKREFi = 31;
  private static final byte REOP_FLATi = 32;
  private static final byte REOP_FLAT1i = 33;
  private static final byte REOP_UCFLATi = 34;
  private static final byte REOP_UCFLAT1i = 35;
  private static final byte REOP_ANCHOR1 = 36;
  private static final byte REOP_NCCLASS = 37;
  private static final byte REOP_DOTSTARMIN = 38;
  private static final byte REOP_LPARENNON = 39;
  private static final byte REOP_RPARENNON = 40;
  private static final byte REOP_ASSERT = 41;
  private static final byte REOP_ASSERT_NOT = 42;
  private static final byte REOP_ASSERTTEST = 43;
  private static final byte REOP_ASSERTNOTTEST = 44;
  private static final byte REOP_MINIMALSTAR = 45;
  private static final byte REOP_MINIMALPLUS = 46;
  private static final byte REOP_MINIMALOPT = 47;
  private static final byte REOP_MINIMALQUANT = 48;
  private static final byte REOP_ENDCHILD = 49;
  private static final byte REOP_CLASS = 50;
  private static final byte REOP_REPEAT = 51;
  private static final byte REOP_MINIMALREPEAT = 52;
  private static final byte REOP_END = 53;
  private static final int OFFSET_LEN = 2;
  private static final int INDEX_LEN = 2;
  private static final int Id_lastIndex = 1;
  private static final int Id_source = 2;
  private static final int Id_global = 3;
  private static final int Id_ignoreCase = 4;
  private static final int Id_multiline = 5;
  private static final int MAX_INSTANCE_ID = 5;
  private static final int Id_compile = 1;
  private static final int Id_toString = 2;
  private static final int Id_toSource = 3;
  private static final int Id_exec = 4;
  private static final int Id_test = 5;
  private static final int Id_prefix = 6;
  private static final int MAX_PROTOTYPE_ID = 6;
  private RECompiled re;
  double lastIndex;

  public static void init(Context paramContext, Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeRegExp localNativeRegExp = new NativeRegExp();
    localNativeRegExp.re = ((RECompiled)compileRE("", null, false));
    localNativeRegExp.activatePrototypeMap(6);
    localNativeRegExp.setParentScope(paramScriptable);
    localNativeRegExp.setPrototype(getObjectPrototype(paramScriptable));
    NativeRegExpCtor localNativeRegExpCtor = new NativeRegExpCtor();
    ScriptRuntime.setFunctionProtoAndParent(localNativeRegExpCtor, paramScriptable);
    localNativeRegExpCtor.setImmunePrototypeProperty(localNativeRegExp);
    if (paramBoolean)
    {
      localNativeRegExp.sealObject();
      localNativeRegExpCtor.sealObject();
    }
    defineProperty(paramScriptable, "RegExp", localNativeRegExpCtor, 2);
  }

  NativeRegExp(Scriptable paramScriptable, Object paramObject)
  {
    this.re = ((RECompiled)paramObject);
    this.lastIndex = 0D;
    ScriptRuntime.setObjectProtoAndParent(this, paramScriptable);
  }

  public String getClassName()
  {
    return "RegExp";
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    return execSub(paramContext, paramScriptable1, paramArrayOfObject, 1);
  }

  public Scriptable construct(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    return ((Scriptable)execSub(paramContext, paramScriptable, paramArrayOfObject, 1));
  }

  Scriptable compile(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    if ((paramArrayOfObject.length > 0) && (paramArrayOfObject[0] instanceof NativeRegExp))
    {
      if ((paramArrayOfObject.length > 1) && (paramArrayOfObject[1] != Undefined.instance))
        throw ScriptRuntime.typeError0("msg.bad.regexp.compile");
      localObject = (NativeRegExp)paramArrayOfObject[0];
      this.re = ((NativeRegExp)localObject).re;
      this.lastIndex = ((NativeRegExp)localObject).lastIndex;
      return this;
    }
    Object localObject = (paramArrayOfObject.length == 0) ? "" : ScriptRuntime.toString(paramArrayOfObject[0]);
    String str = ((paramArrayOfObject.length > 1) && (paramArrayOfObject[1] != Undefined.instance)) ? ScriptRuntime.toString(paramArrayOfObject[1]) : null;
    this.re = ((RECompiled)compileRE((String)localObject, str, false));
    this.lastIndex = 0D;
    return ((Scriptable)this);
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append('/');
    if (this.re.source.length != 0)
      localStringBuffer.append(this.re.source);
    else
      localStringBuffer.append("(?:)");
    localStringBuffer.append('/');
    if ((this.re.flags & 0x1) != 0)
      localStringBuffer.append('g');
    if ((this.re.flags & 0x2) != 0)
      localStringBuffer.append('i');
    if ((this.re.flags & 0x4) != 0)
      localStringBuffer.append('m');
    return localStringBuffer.toString();
  }

  NativeRegExp()
  {
  }

  private static RegExpImpl getImpl(Context paramContext)
  {
    return ((RegExpImpl)ScriptRuntime.getRegExpProxy(paramContext));
  }

  private Object execSub(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject, int paramInt)
  {
    String str;
    Object localObject;
    RegExpImpl localRegExpImpl = getImpl(paramContext);
    if (paramArrayOfObject.length == 0)
    {
      str = localRegExpImpl.input;
      if (str == null)
        reportError("msg.no.re.input.for", toString());
    }
    else
    {
      str = ScriptRuntime.toString(paramArrayOfObject[0]);
    }
    double d = ((this.re.flags & 0x1) != 0) ? this.lastIndex : 0D;
    if ((d < 0D) || (str.length() < d))
    {
      this.lastIndex = 0D;
      localObject = null;
    }
    else
    {
      int[] arrayOfInt = { (int)d };
      localObject = executeRegExp(paramContext, paramScriptable, localRegExpImpl, str, arrayOfInt, paramInt);
      if ((this.re.flags & 0x1) != 0)
        this.lastIndex = arrayOfInt[0];
    }
    return localObject;
  }

  static Object compileRE(String paramString1, String paramString2, boolean paramBoolean)
  {
    RECompiled localRECompiled = new RECompiled();
    localRECompiled.source = paramString1.toCharArray();
    int i = paramString1.length();
    int j = 0;
    if (paramString2 != null)
      for (int k = 0; k < paramString2.length(); ++k)
      {
        char c = paramString2.charAt(k);
        if (c == 'g')
          j |= 1;
        else if (c == 'i')
          j |= 2;
        else if (c == 'm')
          j |= 4;
        else
          reportError("msg.invalid.re.flag", String.valueOf(c));
      }
    localRECompiled.flags = j;
    CompilerState localCompilerState = new CompilerState(localRECompiled.source, i, j);
    if ((paramBoolean) && (i > 0))
    {
      localCompilerState.result = new RENode(21);
      localCompilerState.result.chr = localCompilerState.cpbegin[0];
      localCompilerState.result.length = i;
      localCompilerState.result.flatIndex = 0;
      localCompilerState.progLength += 5;
    }
    else if (!(parseDisjunction(localCompilerState)))
    {
      return null;
    }
    localRECompiled.program = new byte[localCompilerState.progLength + 1];
    if (localCompilerState.classCount != 0)
    {
      localRECompiled.classList = new RECharSet[localCompilerState.classCount];
      localRECompiled.classCount = localCompilerState.classCount;
    }
    int l = emitREBytecode(localCompilerState, localRECompiled, 0, localCompilerState.result);
    localRECompiled.program[(l++)] = 53;
    localRECompiled.parenCount = localCompilerState.parenCount;
    switch (localRECompiled.program[0])
    {
    case 28:
    case 35:
      localRECompiled.anchorCh = (char)getIndex(localRECompiled.program, 1);
      break;
    case 22:
    case 33:
      localRECompiled.anchorCh = (char)(localRECompiled.program[1] & 0xFF);
      break;
    case 21:
    case 32:
      int i1 = getIndex(localRECompiled.program, 1);
      localRECompiled.anchorCh = localRECompiled.source[i1];
    case 23:
    case 24:
    case 25:
    case 26:
    case 27:
    case 29:
    case 30:
    case 31:
    case 34:
    }
    return localRECompiled;
  }

  static boolean isDigit(char paramChar)
  {
    return (('0' <= paramChar) && (paramChar <= '9'));
  }

  private static boolean isWord(char paramChar)
  {
    return ((Character.isLetter(paramChar)) || (isDigit(paramChar)) || (paramChar == '_'));
  }

  private static boolean isLineTerm(char paramChar)
  {
    return ScriptRuntime.isJSLineTerminator(paramChar);
  }

  private static boolean isREWhiteSpace(int paramInt)
  {
    return ((paramInt == 32) || (paramInt == 9) || (paramInt == 10) || (paramInt == 13) || (paramInt == 8232) || (paramInt == 8233) || (paramInt == 12) || (paramInt == 11) || (paramInt == 160) || (Character.getType((char)paramInt) == 12));
  }

  private static char upcase(char paramChar)
  {
    if (paramChar < 128)
    {
      if (('a' <= paramChar) && (paramChar <= 'z'))
        return (char)(paramChar + '￠');
      return paramChar;
    }
    int i = Character.toUpperCase(paramChar);
    if ((paramChar >= 128) && (i < 128))
      return paramChar;
    return i;
  }

  private static char downcase(char paramChar)
  {
    if (paramChar < 128)
    {
      if (('A' <= paramChar) && (paramChar <= 'Z'))
        return (char)(paramChar + ' ');
      return paramChar;
    }
    int i = Character.toLowerCase(paramChar);
    if ((paramChar >= 128) && (i < 128))
      return paramChar;
    return i;
  }

  private static int toASCIIHexDigit(int paramInt)
  {
    if (paramInt < 48)
      return -1;
    if (paramInt <= 57)
      return (paramInt - 48);
    paramInt |= 32;
    if ((97 <= paramInt) && (paramInt <= 102))
      return (paramInt - 97 + 10);
    return -1;
  }

  private static boolean parseDisjunction(CompilerState paramCompilerState)
  {
    if (!(parseAlternative(paramCompilerState)))
      return false;
    char[] arrayOfChar = paramCompilerState.cpbegin;
    int i = paramCompilerState.cp;
    if ((i != arrayOfChar.length) && (arrayOfChar[i] == '|'))
    {
      paramCompilerState.cp += 1;
      RENode localRENode = new RENode(1);
      localRENode.kid = paramCompilerState.result;
      if (!(parseDisjunction(paramCompilerState)))
        return false;
      localRENode.kid2 = paramCompilerState.result;
      paramCompilerState.result = localRENode;
      paramCompilerState.progLength += 9;
    }
    return true;
  }

  private static boolean parseAlternative(CompilerState paramCompilerState)
  {
    RENode localRENode1 = null;
    RENode localRENode2 = null;
    char[] arrayOfChar = paramCompilerState.cpbegin;
    while (true)
    {
      if ((paramCompilerState.cp == paramCompilerState.cpend) || (arrayOfChar[paramCompilerState.cp] == '|') || ((paramCompilerState.parenNesting != 0) && (arrayOfChar[paramCompilerState.cp] == ')')))
      {
        if (localRENode1 == null)
          paramCompilerState.result = new RENode(0);
        else
          paramCompilerState.result = localRENode1;
        return true;
      }
      if (!(parseTerm(paramCompilerState)))
        return false;
      if (localRENode1 != null)
        break;
      localRENode1 = paramCompilerState.result;
    }
    if (localRENode2 == null)
    {
      localRENode1.next = paramCompilerState.result;
      localRENode2 = paramCompilerState.result;
      while (true)
      {
        if (localRENode2.next == null);
        localRENode2 = localRENode2.next;
      }
    }
    localRENode2.next = paramCompilerState.result;
    localRENode2 = localRENode2.next;
    while (true)
    {
      if (localRENode2.next == null);
      localRENode2 = localRENode2.next;
    }
  }

  private static boolean calculateBitmapSize(CompilerState paramCompilerState, RENode paramRENode, char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    int i = 0;
    int i2 = 0;
    int i3 = 0;
    paramRENode.bmsize = 0;
    if (paramInt1 == paramInt2)
      return true;
    if (paramArrayOfChar[paramInt1] == '^')
      ++paramInt1;
    while (true)
    {
      int i4;
      while (true)
      {
        int j;
        if (paramInt1 == paramInt2)
          break label814;
        i4 = 0;
        int l = 2;
        switch (paramArrayOfChar[paramInt1])
        {
        case '\\':
          int k;
          int i1;
          ++paramInt1;
          j = paramArrayOfChar[(++paramInt1)];
          switch (j)
          {
          case 98:
            i4 = 8;
            break;
          case 102:
            i4 = 12;
            break;
          case 110:
            i4 = 10;
            break;
          case 114:
            i4 = 13;
            break;
          case 116:
            i4 = 9;
            break;
          case 118:
            i4 = 11;
            break;
          case 99:
            if ((paramInt1 + 1 < paramInt2) && (Character.isLetter(paramArrayOfChar[(paramInt1 + 1)])))
              i4 = (char)(paramArrayOfChar[(paramInt1++)] & 0x1F);
            else
              i4 = 92;
            break;
          case 117:
            l += 2;
          case 120:
            k = 0;
            for (i1 = 0; (i1 < l) && (paramInt1 < paramInt2); ++i1)
            {
              j = paramArrayOfChar[(paramInt1++)];
              k = Kit.xDigitToInt(j, k);
              if (k < 0)
              {
                paramInt1 -= i1 + 1;
                k = 92;
                break;
              }
            }
            i4 = k;
            break;
          case 100:
            if (i3 != 0)
            {
              reportError("msg.bad.range", "");
              return false;
            }
            i4 = 57;
            break;
          case 68:
          case 83:
          case 87:
          case 115:
          case 119:
            if (i3 != 0)
            {
              reportError("msg.bad.range", "");
              return false;
            }
            paramRENode.bmsize = 65535;
            return true;
          case 48:
          case 49:
          case 50:
          case 51:
          case 52:
          case 53:
          case 54:
          case 55:
            k = j - 48;
            j = paramArrayOfChar[paramInt1];
            if ((48 <= j) && (j <= 55))
            {
              ++paramInt1;
              k = 8 * k + j - 48;
              j = paramArrayOfChar[paramInt1];
              if ((48 <= j) && (j <= 55))
              {
                ++paramInt1;
                i1 = 8 * k + j - 48;
                if (i1 <= 255)
                  k = i1;
                else
                  --paramInt1;
              }
            }
            i4 = k;
            break;
          case 56:
          case 57:
          case 58:
          case 59:
          case 60:
          case 61:
          case 62:
          case 63:
          case 64:
          case 65:
          case 66:
          case 67:
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
          case 84:
          case 85:
          case 86:
          case 88:
          case 89:
          case 90:
          case 91:
          case 92:
          case 93:
          case 94:
          case 95:
          case 96:
          case 97:
          case 101:
          case 103:
          case 104:
          case 105:
          case 106:
          case 107:
          case 108:
          case 109:
          case 111:
          case 112:
          case 113:
          default:
            i4 = j;
          }
          break;
        default:
          i4 = paramArrayOfChar[(paramInt1++)];
        }
        if (i3 != 0)
        {
          if (i > i4)
          {
            reportError("msg.bad.range", "");
            return false;
          }
          i3 = 0;
          break;
        }
        if ((paramInt1 >= paramInt2 - 1) || (paramArrayOfChar[paramInt1] != '-'))
          break;
        ++paramInt1;
        i3 = 1;
        i = (char)i4;
      }
      if ((paramCompilerState.flags & 0x2) != 0)
      {
        int i5 = upcase((char)i4);
        int i6 = downcase((char)i4);
        i4 = (i5 >= i6) ? i5 : i6;
      }
      if (i4 > i2)
        i2 = i4;
    }
    label814: paramRENode.bmsize = i2;
    return true;
  }

  private static void doFlat(CompilerState paramCompilerState, char paramChar)
  {
    paramCompilerState.result = new RENode(21);
    paramCompilerState.result.chr = paramChar;
    paramCompilerState.result.length = 1;
    paramCompilerState.result.flatIndex = -1;
    paramCompilerState.progLength += 3;
  }

  private static int getDecimalValue(char paramChar, CompilerState paramCompilerState, int paramInt, String paramString)
  {
    int i = 0;
    int j = paramCompilerState.cp;
    char[] arrayOfChar = paramCompilerState.cpbegin;
    int k = paramChar - '0';
    while (paramCompilerState.cp != paramCompilerState.cpend)
    {
      paramChar = arrayOfChar[paramCompilerState.cp];
      if (!(isDigit(paramChar)))
        break;
      if (i == 0)
      {
        int l = paramChar - '0';
        if (k < (paramInt - l) / 10)
        {
          k = k * 10 + l;
        }
        else
        {
          i = 1;
          k = paramInt;
        }
      }
      paramCompilerState.cp += 1;
    }
    if (i != 0)
      reportError(paramString, String.valueOf(arrayOfChar, j, paramCompilerState.cp - j));
    return k;
  }

  private static boolean parseTerm(CompilerState paramCompilerState)
  {
    int i1;
    int i5;
    char[] arrayOfChar = paramCompilerState.cpbegin;
    char c = arrayOfChar[(paramCompilerState.cp++)];
    int i = 2;
    int j = paramCompilerState.parenCount;
    int i2 = paramCompilerState.cp;
    switch (c)
    {
    case '^':
      paramCompilerState.result = new RENode(2);
      paramCompilerState.progLength += 1;
      return true;
    case '$':
      paramCompilerState.result = new RENode(3);
      paramCompilerState.progLength += 1;
      return true;
    case '\\':
      if (paramCompilerState.cp < paramCompilerState.cpend)
      {
        int k;
        int l;
        c = arrayOfChar[(paramCompilerState.cp++)];
        switch (c)
        {
        case 'b':
          paramCompilerState.result = new RENode(4);
          paramCompilerState.progLength += 1;
          return true;
        case 'B':
          paramCompilerState.result = new RENode(5);
          paramCompilerState.progLength += 1;
          return true;
        case '0':
          for (k = 0; paramCompilerState.cp < paramCompilerState.cpend; k = l)
          {
            c = arrayOfChar[paramCompilerState.cp];
            if ((c < '0') || (c > '7'))
              break;
            paramCompilerState.cp += 1;
            l = 8 * k + c - '0';
            if (l > 255)
              break;
          }
          c = (char)k;
          doFlat(paramCompilerState, c);
          break;
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          i1 = paramCompilerState.cp - 1;
          k = getDecimalValue(c, paramCompilerState, 65535, "msg.overlarge.backref");
          if ((k > 9) && (k > paramCompilerState.parenCount))
          {
            paramCompilerState.cp = i1;
            for (k = 0; paramCompilerState.cp < paramCompilerState.cpend; k = l)
            {
              c = arrayOfChar[paramCompilerState.cp];
              if ((c < '0') || (c > '7'))
                break;
              paramCompilerState.cp += 1;
              l = 8 * k + c - '0';
              if (l > 255)
                break;
            }
            c = (char)k;
            doFlat(paramCompilerState, c);
          }
          else
          {
            paramCompilerState.result = new RENode(20);
            paramCompilerState.result.parenIndex = (k - 1);
            paramCompilerState.progLength += 3;
          }
          break;
        case 'f':
          c = '\f';
          doFlat(paramCompilerState, c);
          break;
        case 'n':
          c = '\n';
          doFlat(paramCompilerState, c);
          break;
        case 'r':
          c = '\r';
          doFlat(paramCompilerState, c);
          break;
        case 't':
          c = '\t';
          doFlat(paramCompilerState, c);
          break;
        case 'v':
          c = '\11';
          doFlat(paramCompilerState, c);
          break;
        case 'c':
          if ((paramCompilerState.cp + 1 < paramCompilerState.cpend) && (Character.isLetter(arrayOfChar[(paramCompilerState.cp + 1)])))
          {
            c = (char)(arrayOfChar[(paramCompilerState.cp++)] & 0x1F);
          }
          else
          {
            paramCompilerState.cp -= 1;
            c = '\\';
          }
          doFlat(paramCompilerState, c);
          break;
        case 'u':
          i += 2;
        case 'x':
          int i3 = 0;
          for (i5 = 0; (i5 < i) && (paramCompilerState.cp < paramCompilerState.cpend); ++i5)
          {
            c = arrayOfChar[(paramCompilerState.cp++)];
            i3 = Kit.xDigitToInt(c, i3);
            if (i3 < 0)
            {
              paramCompilerState.cp -= i5 + 2;
              i3 = arrayOfChar[(paramCompilerState.cp++)];
              break;
            }
          }
          c = (char)i3;
          doFlat(paramCompilerState, c);
          break;
        case 'd':
          paramCompilerState.result = new RENode(14);
          paramCompilerState.progLength += 1;
          break;
        case 'D':
          paramCompilerState.result = new RENode(15);
          paramCompilerState.progLength += 1;
          break;
        case 's':
          paramCompilerState.result = new RENode(18);
          paramCompilerState.progLength += 1;
          break;
        case 'S':
          paramCompilerState.result = new RENode(19);
          paramCompilerState.progLength += 1;
          break;
        case 'w':
          paramCompilerState.result = new RENode(16);
          paramCompilerState.progLength += 1;
          break;
        case 'W':
          paramCompilerState.result = new RENode(17);
          paramCompilerState.progLength += 1;
          break;
        case ':':
        case ';':
        case '<':
        case '=':
        case '>':
        case '?':
        case '@':
        case 'A':
        case 'C':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'T':
        case 'U':
        case 'V':
        case 'X':
        case 'Y':
        case 'Z':
        case '[':
        case '\\':
        case ']':
        case '^':
        case '_':
        case '`':
        case 'a':
        case 'e':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'o':
        case 'p':
        case 'q':
        default:
          paramCompilerState.result = new RENode(21);
          paramCompilerState.result.chr = c;
          paramCompilerState.result.length = 1;
          paramCompilerState.result.flatIndex = (paramCompilerState.cp - 1);
          paramCompilerState.progLength += 3;
          break label1783:
          reportError("msg.trail.backslash", "");
          return false;
        }
      }
    case '(':
      RENode localRENode2 = null;
      i1 = paramCompilerState.cp;
      if ((paramCompilerState.cp + 1 < paramCompilerState.cpend) && (arrayOfChar[paramCompilerState.cp] == '?'))
      {
        if (((c = arrayOfChar[(paramCompilerState.cp + 1)]) == '=') || (c == '!') || (c == ':'))
        {
          paramCompilerState.cp += 2;
          if (c == '=')
          {
            localRENode2 = new RENode(41);
            paramCompilerState.progLength += 4;
            break label1414:
          }
          if (c != '!')
            break label1414;
          localRENode2 = new RENode(42);
          paramCompilerState.progLength += 4;
        }
      }
      else
      {
        localRENode2 = new RENode(10);
        paramCompilerState.progLength += 6;
        localRENode2.parenIndex = (paramCompilerState.parenCount++);
      }
      paramCompilerState.parenNesting += 1;
      if (!(parseDisjunction(paramCompilerState)))
        return false;
      if ((paramCompilerState.cp == paramCompilerState.cpend) || (arrayOfChar[paramCompilerState.cp] != ')'))
      {
        reportError("msg.unterm.paren", "");
        return false;
      }
      paramCompilerState.cp += 1;
      paramCompilerState.parenNesting -= 1;
      if (localRENode2 != null)
      {
        localRENode2.kid = paramCompilerState.result;
        paramCompilerState.result = localRENode2;
      }
      break;
    case ')':
      reportError("msg.re.unmatched.right.paren", "");
      return false;
    case '[':
      paramCompilerState.result = new RENode(50);
      i1 = paramCompilerState.cp;
      paramCompilerState.result.startIndex = i1;
      while (true)
      {
        if (paramCompilerState.cp == paramCompilerState.cpend)
        {
          reportError("msg.unterm.class", "");
          return false;
        }
        if (arrayOfChar[paramCompilerState.cp] == '\\')
        {
          paramCompilerState.cp += 1;
        }
        else if (arrayOfChar[paramCompilerState.cp] == ']')
        {
          paramCompilerState.result.kidlen = (paramCompilerState.cp - i1);
          break;
        }
        paramCompilerState.cp += 1;
      }
      paramCompilerState.result.index = (paramCompilerState.classCount++);
      if (!(calculateBitmapSize(paramCompilerState, paramCompilerState.result, arrayOfChar, i1, paramCompilerState.cp++)))
        return false;
      paramCompilerState.progLength += 3;
      break;
    case '.':
      paramCompilerState.result = new RENode(12);
      paramCompilerState.progLength += 1;
      break;
    case '*':
    case '+':
    case '?':
      reportError("msg.bad.quant", String.valueOf(arrayOfChar[(paramCompilerState.cp - 1)]));
      return false;
    default:
      label1414: paramCompilerState.result = new RENode(21);
      paramCompilerState.result.chr = c;
      paramCompilerState.result.length = 1;
      paramCompilerState.result.flatIndex = (paramCompilerState.cp - 1);
      paramCompilerState.progLength += 3;
    }
    label1783: RENode localRENode1 = paramCompilerState.result;
    if (paramCompilerState.cp == paramCompilerState.cpend)
      return true;
    int i4 = 0;
    switch (arrayOfChar[paramCompilerState.cp])
    {
    case '+':
      paramCompilerState.result = new RENode(6);
      paramCompilerState.result.min = 1;
      paramCompilerState.result.max = -1;
      paramCompilerState.progLength += 8;
      i4 = 1;
      break;
    case '*':
      paramCompilerState.result = new RENode(6);
      paramCompilerState.result.min = 0;
      paramCompilerState.result.max = -1;
      paramCompilerState.progLength += 8;
      i4 = 1;
      break;
    case '?':
      paramCompilerState.result = new RENode(6);
      paramCompilerState.result.min = 0;
      paramCompilerState.result.max = 1;
      paramCompilerState.progLength += 8;
      i4 = 1;
      break;
    case '{':
      i5 = 0;
      int i6 = -1;
      int i7 = paramCompilerState.cp;
      c = arrayOfChar[(++paramCompilerState.cp)];
      if (isDigit(c))
      {
        paramCompilerState.cp += 1;
        i5 = getDecimalValue(c, paramCompilerState, 65535, "msg.overlarge.min");
        c = arrayOfChar[paramCompilerState.cp];
        if (c == ',')
        {
          c = arrayOfChar[(++paramCompilerState.cp)];
          if (!(isDigit(c)))
            break label2133;
          paramCompilerState.cp += 1;
          i6 = getDecimalValue(c, paramCompilerState, 65535, "msg.overlarge.max");
          c = arrayOfChar[paramCompilerState.cp];
          if (i5 <= i6)
            break label2133;
          reportError("msg.max.lt.min", String.valueOf(arrayOfChar[paramCompilerState.cp]));
          return false;
        }
        i6 = i5;
        if (c == '}')
        {
          label2133: paramCompilerState.result = new RENode(6);
          paramCompilerState.result.min = i5;
          paramCompilerState.result.max = i6;
          paramCompilerState.progLength += 12;
          i4 = 1;
        }
      }
      if (i4 == 0)
        paramCompilerState.cp = i7;
    }
    if (i4 == 0)
      return true;
    paramCompilerState.cp += 1;
    paramCompilerState.result.kid = localRENode1;
    paramCompilerState.result.parenIndex = j;
    paramCompilerState.result.parenCount = (paramCompilerState.parenCount - j);
    if ((paramCompilerState.cp < paramCompilerState.cpend) && (arrayOfChar[paramCompilerState.cp] == '?'))
    {
      paramCompilerState.cp += 1;
      paramCompilerState.result.greedy = false;
    }
    else
    {
      paramCompilerState.result.greedy = true;
    }
    return true;
  }

  private static void resolveForwardJump(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (paramInt1 > paramInt2)
      throw Kit.codeBug();
    addIndex(paramArrayOfByte, paramInt1, paramInt2 - paramInt1);
  }

  private static int getOffset(byte[] paramArrayOfByte, int paramInt)
  {
    return getIndex(paramArrayOfByte, paramInt);
  }

  private static int addIndex(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (paramInt2 < 0)
      throw Kit.codeBug();
    if (paramInt2 > 65535)
      throw Context.reportRuntimeError("Too complex regexp");
    paramArrayOfByte[paramInt1] = (byte)(paramInt2 >> 8);
    paramArrayOfByte[(paramInt1 + 1)] = (byte)paramInt2;
    return (paramInt1 + 2);
  }

  private static int getIndex(byte[] paramArrayOfByte, int paramInt)
  {
    return ((paramArrayOfByte[paramInt] & 0xFF) << 8 | paramArrayOfByte[(paramInt + 1)] & 0xFF);
  }

  private static int emitREBytecode(CompilerState paramCompilerState, RECompiled paramRECompiled, int paramInt, RENode paramRENode)
  {
    byte[] arrayOfByte = paramRECompiled.program;
    while (paramRENode != null)
    {
      int j;
      arrayOfByte[(paramInt++)] = paramRENode.op;
      switch (paramRENode.op)
      {
      case 0:
        --paramInt;
        break;
      case 1:
        RENode localRENode = paramRENode.kid2;
        int i = paramInt;
        paramInt = emitREBytecode(paramCompilerState, paramRECompiled, paramInt += 2, paramRENode.kid);
        arrayOfByte[(paramInt++)] = 23;
        j = paramInt;
        resolveForwardJump(arrayOfByte, i, paramInt += 2);
        paramInt = emitREBytecode(paramCompilerState, paramRECompiled, paramInt, localRENode);
        arrayOfByte[(paramInt++)] = 23;
        i = paramInt;
        resolveForwardJump(arrayOfByte, j, paramInt += 2);
        resolveForwardJump(arrayOfByte, i, paramInt);
        break;
      case 21:
        while ((paramRENode.flatIndex != -1) && (paramRENode.next != null) && (paramRENode.next.op == 21) && (paramRENode.flatIndex + paramRENode.length == paramRENode.next.flatIndex))
        {
          paramRENode.length += paramRENode.next.length;
          paramRENode.next = paramRENode.next.next;
        }
        if ((paramRENode.flatIndex != -1) && (paramRENode.length > 1))
        {
          if ((paramCompilerState.flags & 0x2) != 0)
            arrayOfByte[(paramInt - 1)] = 32;
          else
            arrayOfByte[(paramInt - 1)] = 21;
          paramInt = addIndex(arrayOfByte, paramInt, paramRENode.flatIndex);
          paramInt = addIndex(arrayOfByte, paramInt, paramRENode.length);
        }
        else if (paramRENode.chr < 256)
        {
          if ((paramCompilerState.flags & 0x2) != 0)
            arrayOfByte[(paramInt - 1)] = 33;
          else
            arrayOfByte[(paramInt - 1)] = 22;
          arrayOfByte[(paramInt++)] = (byte)paramRENode.chr;
        }
        else
        {
          if ((paramCompilerState.flags & 0x2) != 0)
            arrayOfByte[(paramInt - 1)] = 35;
          else
            arrayOfByte[(paramInt - 1)] = 28;
          paramInt = addIndex(arrayOfByte, paramInt, paramRENode.chr);
        }
        break;
      case 10:
        paramInt = addIndex(arrayOfByte, paramInt, paramRENode.parenIndex);
        paramInt = emitREBytecode(paramCompilerState, paramRECompiled, paramInt, paramRENode.kid);
        arrayOfByte[(paramInt++)] = 11;
        paramInt = addIndex(arrayOfByte, paramInt, paramRENode.parenIndex);
        break;
      case 20:
        paramInt = addIndex(arrayOfByte, paramInt, paramRENode.parenIndex);
        break;
      case 41:
        j = paramInt;
        paramInt = emitREBytecode(paramCompilerState, paramRECompiled, paramInt += 2, paramRENode.kid);
        arrayOfByte[(paramInt++)] = 43;
        resolveForwardJump(arrayOfByte, j, paramInt);
        break;
      case 42:
        j = paramInt;
        paramInt = emitREBytecode(paramCompilerState, paramRECompiled, paramInt += 2, paramRENode.kid);
        arrayOfByte[(paramInt++)] = 44;
        resolveForwardJump(arrayOfByte, j, paramInt);
        break;
      case 6:
        if ((paramRENode.min == 0) && (paramRENode.max == -1))
        {
          arrayOfByte[(paramInt - 1)] = ((paramRENode.greedy) ? 7 : 45);
        }
        else if ((paramRENode.min == 0) && (paramRENode.max == 1))
        {
          arrayOfByte[(paramInt - 1)] = ((paramRENode.greedy) ? 9 : 47);
        }
        else if ((paramRENode.min == 1) && (paramRENode.max == -1))
        {
          arrayOfByte[(paramInt - 1)] = ((paramRENode.greedy) ? 8 : 46);
        }
        else
        {
          if (!(paramRENode.greedy))
            arrayOfByte[(paramInt - 1)] = 48;
          paramInt = addIndex(arrayOfByte, paramInt, paramRENode.min);
          paramInt = addIndex(arrayOfByte, paramInt, paramRENode.max + 1);
        }
        paramInt = addIndex(arrayOfByte, paramInt, paramRENode.parenCount);
        paramInt = addIndex(arrayOfByte, paramInt, paramRENode.parenIndex);
        j = paramInt;
        paramInt = emitREBytecode(paramCompilerState, paramRECompiled, paramInt += 2, paramRENode.kid);
        arrayOfByte[(paramInt++)] = 49;
        resolveForwardJump(arrayOfByte, j, paramInt);
        break;
      case 50:
        paramInt = addIndex(arrayOfByte, paramInt, paramRENode.index);
        paramRECompiled.classList[paramRENode.index] = new RECharSet(paramRENode.bmsize, paramRENode.startIndex, paramRENode.kidlen);
      }
      paramRENode = paramRENode.next;
    }
    return paramInt;
  }

  private static void pushProgState(REGlobalData paramREGlobalData, int paramInt1, int paramInt2, REBackTrackData paramREBackTrackData, int paramInt3, int paramInt4)
  {
    paramREGlobalData.stateStackTop = new REProgState(paramREGlobalData.stateStackTop, paramInt1, paramInt2, paramREGlobalData.cp, paramREBackTrackData, paramInt3, paramInt4);
  }

  private static REProgState popProgState(REGlobalData paramREGlobalData)
  {
    REProgState localREProgState = paramREGlobalData.stateStackTop;
    paramREGlobalData.stateStackTop = localREProgState.previous;
    return localREProgState;
  }

  private static void pushBackTrackState(REGlobalData paramREGlobalData, byte paramByte, int paramInt)
  {
    paramREGlobalData.backTrackStackTop = new REBackTrackData(paramREGlobalData, paramByte, paramInt);
  }

  private static boolean flatNMatcher(REGlobalData paramREGlobalData, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3)
  {
    if (paramREGlobalData.cp + paramInt2 > paramInt3)
      return false;
    for (int i = 0; i < paramInt2; ++i)
      if (paramREGlobalData.regexp.source[(paramInt1 + i)] != paramArrayOfChar[(paramREGlobalData.cp + i)])
        return false;
    paramREGlobalData.cp += paramInt2;
    return true;
  }

  private static boolean flatNIMatcher(REGlobalData paramREGlobalData, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3)
  {
    if (paramREGlobalData.cp + paramInt2 > paramInt3)
      return false;
    for (int i = 0; i < paramInt2; ++i)
      if (upcase(paramREGlobalData.regexp.source[(paramInt1 + i)]) != upcase(paramArrayOfChar[(paramREGlobalData.cp + i)]))
        return false;
    paramREGlobalData.cp += paramInt2;
    return true;
  }

  private static boolean backrefMatcher(REGlobalData paramREGlobalData, int paramInt1, char[] paramArrayOfChar, int paramInt2)
  {
    int k = paramREGlobalData.parens_index(paramInt1);
    if (k == -1)
      return true;
    int i = paramREGlobalData.parens_length(paramInt1);
    if (paramREGlobalData.cp + i > paramInt2)
      return false;
    if ((paramREGlobalData.regexp.flags & 0x2) != 0)
    {
      j = 0;
      while (true)
      {
        if (j >= i)
          break label127;
        if (upcase(paramArrayOfChar[(k + j)]) != upcase(paramArrayOfChar[(paramREGlobalData.cp + j)]))
          return false;
        ++j;
      }
    }
    for (int j = 0; j < i; ++j)
      if (paramArrayOfChar[(k + j)] != paramArrayOfChar[(paramREGlobalData.cp + j)])
        return false;
    label127: paramREGlobalData.cp += i;
    return true;
  }

  private static void addCharacterToCharSet(RECharSet paramRECharSet, char paramChar)
  {
    int i = paramChar / '\b';
    if (paramChar > paramRECharSet.length)
      throw new RuntimeException();
    int tmp26_25 = i;
    byte[] tmp26_22 = paramRECharSet.bits;
    tmp26_22[tmp26_25] = (byte)(tmp26_22[tmp26_25] | '\1' << (paramChar & 0x7));
  }

  private static void addCharacterRangeToCharSet(RECharSet paramRECharSet, char paramChar1, char paramChar2)
  {
    int j = paramChar1 / '\b';
    int k = paramChar2 / '\b';
    if ((paramChar2 > paramRECharSet.length) || (paramChar1 > paramChar2))
      throw new RuntimeException();
    paramChar1 = (char)(paramChar1 & 0x7);
    paramChar2 = (char)(paramChar2 & 0x7);
    if (j == k)
    {
      int tmp58_56 = j;
      byte[] tmp58_53 = paramRECharSet.bits;
      tmp58_53[tmp58_56] = (byte)(tmp58_53[tmp58_56] | 255 >> 7 - paramChar2 - paramChar1 << paramChar1);
    }
    else
    {
      int tmp84_82 = j;
      byte[] tmp84_79 = paramRECharSet.bits;
      tmp84_79[tmp84_82] = (byte)(tmp84_79[tmp84_82] | 255 << paramChar1);
      for (int i = j + 1; i < k; ++i)
        paramRECharSet.bits[i] = -1;
      int tmp124_122 = k;
      byte[] tmp124_119 = paramRECharSet.bits;
      tmp124_119[tmp124_122] = (byte)(tmp124_119[tmp124_122] | 255 >> '\7' - paramChar2);
    }
  }

  private static void processCharSet(REGlobalData paramREGlobalData, RECharSet paramRECharSet)
  {
    synchronized (paramRECharSet)
    {
      if (!(paramRECharSet.converted))
      {
        processCharSetImpl(paramREGlobalData, paramRECharSet);
        paramRECharSet.converted = true;
      }
    }
  }

  private static void processCharSetImpl(REGlobalData paramREGlobalData, RECharSet paramRECharSet)
  {
    int i = paramRECharSet.startIndex;
    int j = i + paramRECharSet.strlength;
    char c1 = ';
    int i3 = 0;
    paramRECharSet.sense = true;
    int k = paramRECharSet.length / 8 + 1;
    paramRECharSet.bits = new byte[k];
    if (i == j)
      return;
    if (paramREGlobalData.regexp.source[i] == '^')
    {
      paramRECharSet.sense = false;
      ++i;
    }
    while (true)
    {
      char c2;
      do
      {
        while (true)
        {
          char c3;
          while (true)
          {
            int i1;
            while (true)
            {
              if (i == j)
                return;
              i1 = 2;
              switch (paramREGlobalData.regexp.source[i])
              {
              case '\\':
                int l;
                ++i;
                c3 = paramREGlobalData.regexp.source[(++i)];
                switch (c3)
                {
                case 'b':
                  c2 = '\b';
                  break;
                case 'f':
                  c2 = '\f';
                  break;
                case 'n':
                  c2 = '\n';
                  break;
                case 'r':
                  c2 = '\r';
                  break;
                case 't':
                  c2 = '\t';
                  break;
                case 'v':
                  c2 = '\11';
                  break;
                case 'c':
                  if ((i + 1 < j) && (isWord(paramREGlobalData.regexp.source[(i + 1)])))
                  {
                    c2 = (char)(paramREGlobalData.regexp.source[(i++)] & 0x1F);
                    break label907:
                  }
                  --i;
                  c2 = '\\';
                  break;
                case 'u':
                  i1 += 2;
                case 'x':
                  l = 0;
                  for (i2 = 0; (i2 < i1) && (i < j); ++i2)
                  {
                    c3 = paramREGlobalData.regexp.source[(i++)];
                    int i4 = toASCIIHexDigit(c3);
                    if (i4 < 0)
                    {
                      i -= i2 + 1;
                      l = 92;
                      break;
                    }
                    l = l << 4 | i4;
                  }
                  c2 = (char)l;
                  break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                  l = c3 - '0';
                  c3 = paramREGlobalData.regexp.source[i];
                  if (('0' <= c3) && (c3 <= '7'))
                  {
                    ++i;
                    l = 8 * l + c3 - '0';
                    c3 = paramREGlobalData.regexp.source[i];
                    if (('0' <= c3) && (c3 <= '7'))
                    {
                      ++i;
                      i2 = 8 * l + c3 - '0';
                      if (i2 <= 255)
                        l = i2;
                      else
                        --i;
                    }
                  }
                  c2 = (char)l;
                  break;
                case 'd':
                  addCharacterRangeToCharSet(paramRECharSet, '0', '9');
                case 'D':
                case 's':
                case 'S':
                case 'w':
                case 'W':
                case '8':
                case '9':
                case ':':
                case ';':
                case '<':
                case '=':
                case '>':
                case '?':
                case '@':
                case 'A':
                case 'B':
                case 'C':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'T':
                case 'U':
                case 'V':
                case 'X':
                case 'Y':
                case 'Z':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '_':
                case '`':
                case 'a':
                case 'e':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'o':
                case 'p':
                case 'q':
                }
              }
            }
            addCharacterRangeToCharSet(paramRECharSet, ', '/');
            addCharacterRangeToCharSet(paramRECharSet, ':', (char)paramRECharSet.length);
          }
          int i2 = paramRECharSet.length;
          while (true)
          {
            if (i2 < 0);
            if (isREWhiteSpace(i2))
              addCharacterToCharSet(paramRECharSet, (char)i2);
            --i2;
          }
          i2 = paramRECharSet.length;
          while (true)
          {
            if (i2 < 0);
            if (!(isREWhiteSpace(i2)))
              addCharacterToCharSet(paramRECharSet, (char)i2);
            --i2;
          }
          i2 = paramRECharSet.length;
          while (true)
          {
            if (i2 < 0);
            if (isWord((char)i2))
              addCharacterToCharSet(paramRECharSet, (char)i2);
            --i2;
          }
          i2 = paramRECharSet.length;
          while (true)
          {
            if (i2 < 0);
            if (!(isWord((char)i2)))
              addCharacterToCharSet(paramRECharSet, (char)i2);
            --i2;
          }
          c2 = c3;
          break label907:
          c2 = paramREGlobalData.regexp.source[(i++)];
          label907: if (i3 == 0)
            break;
          if ((paramREGlobalData.regexp.flags & 0x2) != 0)
          {
            addCharacterRangeToCharSet(paramRECharSet, upcase(c1), upcase(c2));
            addCharacterRangeToCharSet(paramRECharSet, downcase(c1), downcase(c2));
          }
          else
          {
            addCharacterRangeToCharSet(paramRECharSet, c1, c2);
          }
          i3 = 0;
        }
        if ((paramREGlobalData.regexp.flags & 0x2) != 0)
        {
          addCharacterToCharSet(paramRECharSet, upcase(c2));
          addCharacterToCharSet(paramRECharSet, downcase(c2));
        }
        else
        {
          addCharacterToCharSet(paramRECharSet, c2);
        }
      }
      while ((i >= j - 1) || (paramREGlobalData.regexp.source[i] != '-'));
      ++i;
      i3 = 1;
      c1 = c2;
    }
  }

  private static boolean classMatcher(REGlobalData paramREGlobalData, RECharSet paramRECharSet, char paramChar)
  {
    if (!(paramRECharSet.converted))
      processCharSet(paramREGlobalData, paramRECharSet);
    int i = paramChar / '\b';
    if (paramRECharSet.sense)
    {
      if ((paramRECharSet.length != 0) && (paramChar <= paramRECharSet.length) && ((paramRECharSet.bits[i] & '\1' << (paramChar & 0x7)) != 0))
        break label90;
      return false;
    }
    label90: return ((paramRECharSet.length == 0) || (paramChar > paramRECharSet.length) || ((paramRECharSet.bits[i] & '\1' << (paramChar & 0x7)) == 0));
  }

  private static boolean executeREBytecode(REGlobalData paramREGlobalData, char[] paramArrayOfChar, int paramInt)
  {
    int i = 0;
    byte[] arrayOfByte = paramREGlobalData.regexp.program;
    boolean bool = false;
    int k = 0;
    int j = 53;
    int l = arrayOfByte[(i++)];
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
                    label322: label379: int i3;
                    while (true)
                    {
                      while (true)
                      {
                        while (true)
                        {
                          while (true)
                          {
                            int i4;
                            while (true)
                            {
                              int i1;
                              char c;
                              switch (l)
                              {
                              case 0:
                                bool = true;
                                break;
                              case 2:
                                if (paramREGlobalData.cp != 0)
                                {
                                  if ((paramREGlobalData.multiline) || ((paramREGlobalData.regexp.flags & 0x4) != 0))
                                  {
                                    if (isLineTerm(paramArrayOfChar[(paramREGlobalData.cp - 1)]))
                                      break label322;
                                    bool = false;
                                    break label2440:
                                  }
                                  bool = false;
                                  break label2440:
                                }
                                bool = true;
                                break;
                              case 3:
                                if (paramREGlobalData.cp != paramInt)
                                {
                                  if ((paramREGlobalData.multiline) || ((paramREGlobalData.regexp.flags & 0x4) != 0))
                                  {
                                    if (isLineTerm(paramArrayOfChar[paramREGlobalData.cp]))
                                      break label379;
                                    bool = false;
                                    break label2440:
                                  }
                                  bool = false;
                                  break label2440:
                                }
                                bool = true;
                                break;
                              case 4:
                                bool = (((paramREGlobalData.cp == 0) || (!(isWord(paramArrayOfChar[(paramREGlobalData.cp - 1)])))) ? 1 : 0) ^ (((paramREGlobalData.cp >= paramInt) || (!(isWord(paramArrayOfChar[paramREGlobalData.cp])))) ? 1 : 0);
                                break;
                              case 5:
                                bool = (((paramREGlobalData.cp == 0) || (!(isWord(paramArrayOfChar[(paramREGlobalData.cp - 1)])))) ? 1 : 0) ^ (((paramREGlobalData.cp < paramInt) && (isWord(paramArrayOfChar[paramREGlobalData.cp]))) ? 1 : 0);
                                break;
                              case 12:
                                bool = (paramREGlobalData.cp != paramInt) && (!(isLineTerm(paramArrayOfChar[paramREGlobalData.cp])));
                                if (!(bool))
                                  break label2440;
                                paramREGlobalData.cp += 1;
                                break;
                              case 14:
                                bool = (paramREGlobalData.cp != paramInt) && (isDigit(paramArrayOfChar[paramREGlobalData.cp]));
                                if (!(bool))
                                  break label2440;
                                paramREGlobalData.cp += 1;
                                break;
                              case 15:
                                bool = (paramREGlobalData.cp != paramInt) && (!(isDigit(paramArrayOfChar[paramREGlobalData.cp])));
                                if (!(bool))
                                  break label2440;
                                paramREGlobalData.cp += 1;
                                break;
                              case 18:
                                bool = (paramREGlobalData.cp != paramInt) && (isREWhiteSpace(paramArrayOfChar[paramREGlobalData.cp]));
                                if (!(bool))
                                  break label2440;
                                paramREGlobalData.cp += 1;
                                break;
                              case 19:
                                bool = (paramREGlobalData.cp != paramInt) && (!(isREWhiteSpace(paramArrayOfChar[paramREGlobalData.cp])));
                                if (!(bool))
                                  break label2440;
                                paramREGlobalData.cp += 1;
                                break;
                              case 16:
                                bool = (paramREGlobalData.cp != paramInt) && (isWord(paramArrayOfChar[paramREGlobalData.cp]));
                                if (!(bool))
                                  break label2440;
                                paramREGlobalData.cp += 1;
                                break;
                              case 17:
                                bool = (paramREGlobalData.cp != paramInt) && (!(isWord(paramArrayOfChar[paramREGlobalData.cp])));
                                if (!(bool))
                                  break label2440;
                                paramREGlobalData.cp += 1;
                                break;
                              case 21:
                                i1 = getIndex(arrayOfByte, i);
                                i4 = getIndex(arrayOfByte, i += 2);
                                i += 2;
                                bool = flatNMatcher(paramREGlobalData, i1, i4, paramArrayOfChar, paramInt);
                                break;
                              case 32:
                                i1 = getIndex(arrayOfByte, i);
                                i4 = getIndex(arrayOfByte, i += 2);
                                i += 2;
                                bool = flatNIMatcher(paramREGlobalData, i1, i4, paramArrayOfChar, paramInt);
                                break;
                              case 22:
                                i1 = (char)(arrayOfByte[(i++)] & 0xFF);
                                bool = (paramREGlobalData.cp != paramInt) && (paramArrayOfChar[paramREGlobalData.cp] == i1);
                                if (bool)
                                  paramREGlobalData.cp += 1;
                                break;
                              case 33:
                                i1 = (char)(arrayOfByte[(i++)] & 0xFF);
                                bool = (paramREGlobalData.cp != paramInt) && (upcase(paramArrayOfChar[paramREGlobalData.cp]) == upcase(i1));
                                if (bool)
                                  paramREGlobalData.cp += 1;
                                break;
                              case 28:
                                c = (char)getIndex(arrayOfByte, i);
                                i += 2;
                                bool = (paramREGlobalData.cp != paramInt) && (paramArrayOfChar[paramREGlobalData.cp] == c);
                                if (bool)
                                  paramREGlobalData.cp += 1;
                                break;
                              case 35:
                                c = (char)getIndex(arrayOfByte, i);
                                i += 2;
                                bool = (paramREGlobalData.cp != paramInt) && (upcase(paramArrayOfChar[paramREGlobalData.cp]) == upcase(c));
                                if (bool)
                                  paramREGlobalData.cp += 1;
                                break;
                              case 1:
                                pushProgState(paramREGlobalData, 0, 0, null, k, j);
                                i2 = i + getOffset(arrayOfByte, i);
                                i4 = arrayOfByte[(i2++)];
                                pushBackTrackState(paramREGlobalData, i4, i2);
                                ++i;
                                l = arrayOfByte[(i += 2)];
                              case 23:
                              case 10:
                              case 11:
                              case 20:
                              case 50:
                              case 41:
                              case 42:
                              case 43:
                              case 44:
                              case 6:
                              case 7:
                              case 8:
                              case 9:
                              case 45:
                              case 46:
                              case 47:
                              case 48:
                              case 49:
                              case 51:
                              case 52:
                              case 53:
                              case 13:
                              case 24:
                              case 25:
                              case 26:
                              case 27:
                              case 29:
                              case 30:
                              case 31:
                              case 34:
                              case 36:
                              case 37:
                              case 38:
                              case 39:
                              case 40:
                              }
                            }
                            REProgState localREProgState2 = popProgState(paramREGlobalData);
                            k = localREProgState2.continuation_pc;
                            j = localREProgState2.continuation_op;
                            i2 = getOffset(arrayOfByte, i);
                            i += i2;
                            l = arrayOfByte[(i++)];
                          }
                          i2 = getIndex(arrayOfByte, i);
                          i += 2;
                          paramREGlobalData.set_parens(i2, paramREGlobalData.cp, 0);
                          l = arrayOfByte[(i++)];
                        }
                        i5 = getIndex(arrayOfByte, i);
                        i += 2;
                        i2 = paramREGlobalData.parens_index(i5);
                        paramREGlobalData.set_parens(i5, i2, paramREGlobalData.cp - i2);
                        if (i5 > paramREGlobalData.lastParen)
                          paramREGlobalData.lastParen = i5;
                        l = arrayOfByte[(i++)];
                      }
                      int i2 = getIndex(arrayOfByte, i);
                      i += 2;
                      bool = backrefMatcher(paramREGlobalData, i2, paramArrayOfChar, paramInt);
                      break label2440:
                      i2 = getIndex(arrayOfByte, i);
                      i += 2;
                      if ((paramREGlobalData.cp != paramInt) && (classMatcher(paramREGlobalData, paramREGlobalData.regexp.classList[i2], paramArrayOfChar[paramREGlobalData.cp])))
                      {
                        paramREGlobalData.cp += 1;
                        bool = true;
                        break label2440:
                      }
                      bool = false;
                      break label2440:
                      pushProgState(paramREGlobalData, 0, 0, paramREGlobalData.backTrackStackTop, k, j);
                      if (l == 41)
                        i2 = 43;
                      else
                        i2 = 44;
                      pushBackTrackState(paramREGlobalData, i2, i + getOffset(arrayOfByte, i));
                      ++i;
                      l = arrayOfByte[(i += 2)];
                    }
                    REProgState localREProgState1 = popProgState(paramREGlobalData);
                    paramREGlobalData.cp = localREProgState1.index;
                    paramREGlobalData.backTrackStackTop = localREProgState1.backTrack;
                    k = localREProgState1.continuation_pc;
                    j = localREProgState1.continuation_op;
                    if (bool)
                    {
                      if (l == 43)
                        bool = true;
                      else
                        bool = false;
                    }
                    else
                    {
                      if (l == 43)
                        break label1546:
                      bool = true;
                    }
                    label1546: break label2440:
                    i6 = 0;
                    switch (l)
                    {
                    case 7:
                      i6 = 1;
                    case 45:
                      i3 = 0;
                      i5 = -1;
                      break;
                    case 8:
                      i6 = 1;
                    case 46:
                      i3 = 1;
                      i5 = -1;
                      break;
                    case 9:
                      i6 = 1;
                    case 47:
                      i3 = 0;
                      i5 = 1;
                      break;
                    case 6:
                      i6 = 1;
                    case 48:
                      i3 = getOffset(arrayOfByte, i);
                      i5 = getOffset(arrayOfByte, i += 2) - 1;
                      i += 2;
                      break;
                    default:
                      throw Kit.codeBug();
                    }
                    pushProgState(paramREGlobalData, i3, i5, null, k, j);
                    if (i6 != 0)
                    {
                      j = 51;
                      k = i;
                      pushBackTrackState(paramREGlobalData, 51, i);
                      ++i;
                      l = arrayOfByte[(i += 6)];
                    }
                    else if (i3 != 0)
                    {
                      j = 52;
                      k = i;
                      ++i;
                      l = arrayOfByte[(i += 6)];
                    }
                    else
                    {
                      pushBackTrackState(paramREGlobalData, 52, i);
                      popProgState(paramREGlobalData);
                      i = (i += 4) + getOffset(arrayOfByte, i);
                      l = arrayOfByte[(i++)];
                    }
                  }
                  i = k;
                  l = j;
                }
                localObject = popProgState(paramREGlobalData);
                if (!(bool))
                {
                  if (((REProgState)localObject).min == 0)
                    bool = true;
                  k = ((REProgState)localObject).continuation_pc;
                  j = ((REProgState)localObject).continuation_op;
                  i = (i += 4) + getOffset(arrayOfByte, i);
                  break label2440:
                }
                if ((((REProgState)localObject).min == 0) && (paramREGlobalData.cp == ((REProgState)localObject).index))
                {
                  bool = false;
                  k = ((REProgState)localObject).continuation_pc;
                  j = ((REProgState)localObject).continuation_op;
                  i = (i += 4) + getOffset(arrayOfByte, i);
                  break label2440:
                }
                i5 = ((REProgState)localObject).min;
                i6 = ((REProgState)localObject).max;
                if (i5 != 0)
                  --i5;
                if (i6 != -1)
                  --i6;
                if (i6 == 0)
                {
                  bool = true;
                  k = ((REProgState)localObject).continuation_pc;
                  j = ((REProgState)localObject).continuation_op;
                  i = (i += 4) + getOffset(arrayOfByte, i);
                  break label2440:
                }
                pushProgState(paramREGlobalData, i5, i6, null, ((REProgState)localObject).continuation_pc, ((REProgState)localObject).continuation_op);
                j = 51;
                k = i;
                pushBackTrackState(paramREGlobalData, 51, i);
                i7 = getIndex(arrayOfByte, i);
                i8 = getIndex(arrayOfByte, i += 2);
                ++i;
                l = arrayOfByte[(i += 4)];
                for (i9 = 0; i9 < i7; ++i9)
                  paramREGlobalData.set_parens(i8 + i9, -1, 0);
              }
              localObject = popProgState(paramREGlobalData);
              if (bool)
                break label2221;
              if ((((REProgState)localObject).max != -1) && (((REProgState)localObject).max <= 0))
                break;
              pushProgState(paramREGlobalData, ((REProgState)localObject).min, ((REProgState)localObject).max, null, ((REProgState)localObject).continuation_pc, ((REProgState)localObject).continuation_op);
              j = 52;
              k = i;
              i5 = getIndex(arrayOfByte, i);
              i6 = getIndex(arrayOfByte, i += 2);
              i += 4;
              for (i7 = 0; i7 < i5; ++i7)
                paramREGlobalData.set_parens(i6 + i7, -1, 0);
              l = arrayOfByte[(i++)];
            }
            k = ((REProgState)localObject).continuation_pc;
            j = ((REProgState)localObject).continuation_op;
            break label2440:
            if ((((REProgState)localObject).min == 0) && (paramREGlobalData.cp == ((REProgState)localObject).index))
            {
              label2221: bool = false;
              k = ((REProgState)localObject).continuation_pc;
              j = ((REProgState)localObject).continuation_op;
              break label2440:
            }
            int i5 = ((REProgState)localObject).min;
            int i6 = ((REProgState)localObject).max;
            if (i5 != 0)
              --i5;
            if (i6 != -1);
            pushProgState(paramREGlobalData, i5, --i6, null, ((REProgState)localObject).continuation_pc, ((REProgState)localObject).continuation_op);
            if (i5 == 0)
              break;
            j = 52;
            k = i;
            int i7 = getIndex(arrayOfByte, i);
            int i8 = getIndex(arrayOfByte, i += 2);
            i += 4;
            for (int i9 = 0; i9 < i7; ++i9)
              paramREGlobalData.set_parens(i8 + i9, -1, 0);
            l = arrayOfByte[(i++)];
          }
          k = ((REProgState)localObject).continuation_pc;
          j = ((REProgState)localObject).continuation_op;
          pushBackTrackState(paramREGlobalData, 52, i);
          popProgState(paramREGlobalData);
          i = (i += 4) + getOffset(arrayOfByte, i);
          l = arrayOfByte[(i++)];
        }
        return true;
        throw Kit.codeBug();
        label2440: if (bool)
          break label2554;
        Object localObject = paramREGlobalData.backTrackStackTop;
        if (localObject == null)
          break;
        paramREGlobalData.backTrackStackTop = ((REBackTrackData)localObject).previous;
        paramREGlobalData.lastParen = ((REBackTrackData)localObject).lastParen;
        if (((REBackTrackData)localObject).parens != null)
          paramREGlobalData.parens = ((long[])(long[])((REBackTrackData)localObject).parens.clone());
        paramREGlobalData.cp = ((REBackTrackData)localObject).cp;
        paramREGlobalData.stateStackTop = ((REBackTrackData)localObject).stateStackTop;
        j = paramREGlobalData.stateStackTop.continuation_op;
        k = paramREGlobalData.stateStackTop.continuation_pc;
        i = ((REBackTrackData)localObject).continuation_pc;
        l = ((REBackTrackData)localObject).continuation_op;
      }
      return false;
      label2554: l = arrayOfByte[(i++)];
    }
  }

  private static boolean matchRegExp(REGlobalData paramREGlobalData, RECompiled paramRECompiled, char[] paramArrayOfChar, int paramInt1, int paramInt2, boolean paramBoolean)
  {
    if (paramRECompiled.parenCount != 0)
      paramREGlobalData.parens = new long[paramRECompiled.parenCount];
    else
      paramREGlobalData.parens = null;
    paramREGlobalData.backTrackStackTop = null;
    paramREGlobalData.stateStackTop = null;
    paramREGlobalData.multiline = paramBoolean;
    paramREGlobalData.regexp = paramRECompiled;
    paramREGlobalData.lastParen = 0;
    int i = paramREGlobalData.regexp.anchorCh;
    for (int j = paramInt1; j <= paramInt2; ++j)
    {
      if (i >= 0)
        while (true)
        {
          if (j == paramInt2)
            return false;
          int k = paramArrayOfChar[j];
          if (k == i)
            break;
          if (((paramREGlobalData.regexp.flags & 0x2) != 0) && (upcase(k) == upcase((char)i)))
            break;
          ++j;
        }
      paramREGlobalData.cp = j;
      for (int l = 0; l < paramRECompiled.parenCount; ++l)
        paramREGlobalData.set_parens(l, -1, 0);
      boolean bool = executeREBytecode(paramREGlobalData, paramArrayOfChar, paramInt2);
      paramREGlobalData.backTrackStackTop = null;
      paramREGlobalData.stateStackTop = null;
      if (bool)
      {
        paramREGlobalData.skipped = (j - paramInt1);
        return true;
      }
    }
    return false;
  }

  Object executeRegExp(Context paramContext, Scriptable paramScriptable, RegExpImpl paramRegExpImpl, String paramString, int[] paramArrayOfInt, int paramInt)
  {
    Object localObject1;
    Scriptable localScriptable;
    Object localObject2;
    REGlobalData localREGlobalData = new REGlobalData();
    int i = paramArrayOfInt[0];
    char[] arrayOfChar = paramString.toCharArray();
    int j = arrayOfChar.length;
    if (i > j)
      i = j;
    boolean bool = matchRegExp(localREGlobalData, this.re, arrayOfChar, i, j, paramRegExpImpl.multiline);
    if (!(bool))
    {
      if (paramInt != 2)
        return null;
      return Undefined.instance;
    }
    int k = localREGlobalData.cp;
    int l = k;
    paramArrayOfInt[0] = l;
    int i1 = l - i + localREGlobalData.skipped;
    int i2 = k;
    k -= i1;
    if (paramInt == 0)
    {
      localObject1 = Boolean.TRUE;
      localScriptable = null;
    }
    else
    {
      localObject2 = getTopLevelScope(paramScriptable);
      localObject1 = ScriptRuntime.newObject(paramContext, (Scriptable)localObject2, "Array", null);
      localScriptable = (Scriptable)localObject1;
      String str1 = new String(arrayOfChar, k, i1);
      localScriptable.put(0, localScriptable, str1);
    }
    if (this.re.parenCount == 0)
    {
      paramRegExpImpl.parens = null;
      label343: paramRegExpImpl.lastParen = SubString.emptySubString;
    }
    else
    {
      localObject2 = null;
      paramRegExpImpl.parens = new SubString[this.re.parenCount];
      for (int i3 = 0; i3 < this.re.parenCount; ++i3)
      {
        int i4 = localREGlobalData.parens_index(i3);
        if (i4 != -1)
        {
          int i5 = localREGlobalData.parens_length(i3);
          localObject2 = new SubString(arrayOfChar, i4, i5);
          paramRegExpImpl.parens[i3] = localObject2;
          if (paramInt == 0)
            break label343:
          String str2 = ((SubString)localObject2).toString();
          localScriptable.put(i3 + 1, localScriptable, str2);
        }
        else if (paramInt != 0)
        {
          localScriptable.put(i3 + 1, localScriptable, Undefined.instance);
        }
      }
      paramRegExpImpl.lastParen = ((SubString)localObject2);
    }
    if (paramInt != 0)
    {
      localScriptable.put("index", localScriptable, new Integer(i + localREGlobalData.skipped));
      localScriptable.put("input", localScriptable, paramString);
    }
    if (paramRegExpImpl.lastMatch == null)
    {
      paramRegExpImpl.lastMatch = new SubString();
      paramRegExpImpl.leftContext = new SubString();
      paramRegExpImpl.rightContext = new SubString();
    }
    paramRegExpImpl.lastMatch.charArray = arrayOfChar;
    paramRegExpImpl.lastMatch.index = k;
    paramRegExpImpl.lastMatch.length = i1;
    paramRegExpImpl.leftContext.charArray = arrayOfChar;
    if (paramContext.getLanguageVersion() == 120)
    {
      paramRegExpImpl.leftContext.index = i;
      paramRegExpImpl.leftContext.length = localREGlobalData.skipped;
    }
    else
    {
      paramRegExpImpl.leftContext.index = 0;
      paramRegExpImpl.leftContext.length = (i + localREGlobalData.skipped);
    }
    paramRegExpImpl.rightContext.charArray = arrayOfChar;
    paramRegExpImpl.rightContext.index = i2;
    paramRegExpImpl.rightContext.length = (j - i2);
    return localObject1;
  }

  int getFlags()
  {
    return this.re.flags;
  }

  private static void reportError(String paramString1, String paramString2)
  {
    String str = ScriptRuntime.getMessage1(paramString1, paramString2);
    throw ScriptRuntime.constructError("SyntaxError", str);
  }

  protected int getMaxInstanceId()
  {
    return 5;
  }

  protected int findInstanceIdInfo(String paramString)
  {
    int j;
    int k;
    int i = 0;
    String str = null;
    int l = paramString.length();
    if (l == 6)
    {
      k = paramString.charAt(0);
      if (k == 103)
      {
        str = "global";
        i = 3;
      }
      else if (k == 115)
      {
        str = "source";
        i = 2;
      }
    }
    else if (l == 9)
    {
      k = paramString.charAt(0);
      if (k == 108)
      {
        str = "lastIndex";
        i = 1;
      }
      else if (k == 109)
      {
        str = "multiline";
        i = 5;
      }
    }
    else if (l == 10)
    {
      str = "ignoreCase";
      i = 4;
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    if (i == 0)
      return super.findInstanceIdInfo(paramString);
    switch (i)
    {
    case 1:
      j = 6;
      break;
    case 2:
    case 3:
    case 4:
    case 5:
      j = 7;
      break;
    default:
      throw new IllegalStateException();
    }
    return instanceIdInfo(j, i);
  }

  protected String getInstanceIdName(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return "lastIndex";
    case 2:
      return "source";
    case 3:
      return "global";
    case 4:
      return "ignoreCase";
    case 5:
      return "multiline";
    }
    return super.getInstanceIdName(paramInt);
  }

  protected Object getInstanceIdValue(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return ScriptRuntime.wrapNumber(this.lastIndex);
    case 2:
      return new String(this.re.source);
    case 3:
      return ScriptRuntime.wrapBoolean((this.re.flags & 0x1) != 0);
    case 4:
      return ScriptRuntime.wrapBoolean((this.re.flags & 0x2) != 0);
    case 5:
      return ScriptRuntime.wrapBoolean((this.re.flags & 0x4) != 0);
    }
    return super.getInstanceIdValue(paramInt);
  }

  protected void setInstanceIdValue(int paramInt, Object paramObject)
  {
    if (paramInt == 1)
    {
      this.lastIndex = ScriptRuntime.toNumber(paramObject);
      return;
    }
    super.setInstanceIdValue(paramInt, paramObject);
  }

  protected void initPrototypeId(int paramInt)
  {
    String str;
    int i;
    switch (paramInt)
    {
    case 1:
      i = 1;
      str = "compile";
      break;
    case 2:
      i = 0;
      str = "toString";
      break;
    case 3:
      i = 0;
      str = "toSource";
      break;
    case 4:
      i = 1;
      str = "exec";
      break;
    case 5:
      i = 1;
      str = "test";
      break;
    case 6:
      i = 1;
      str = "prefix";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(REGEXP_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(paramIdFunctionObject.hasTag(REGEXP_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case 1:
      return realThis(paramScriptable2, paramIdFunctionObject).compile(paramContext, paramScriptable1, paramArrayOfObject);
    case 2:
    case 3:
      return realThis(paramScriptable2, paramIdFunctionObject).toString();
    case 4:
      return realThis(paramScriptable2, paramIdFunctionObject).execSub(paramContext, paramScriptable1, paramArrayOfObject, 1);
    case 5:
      Object localObject = realThis(paramScriptable2, paramIdFunctionObject).execSub(paramContext, paramScriptable1, paramArrayOfObject, 0);
      return ((Boolean.TRUE.equals(localObject)) ? Boolean.TRUE : Boolean.FALSE);
    case 6:
      return realThis(paramScriptable2, paramIdFunctionObject).execSub(paramContext, paramScriptable1, paramArrayOfObject, 2);
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  private static NativeRegExp realThis(Scriptable paramScriptable, IdFunctionObject paramIdFunctionObject)
  {
    if (!(paramScriptable instanceof NativeRegExp))
      throw incompatibleCallError(paramIdFunctionObject);
    return ((NativeRegExp)paramScriptable);
  }

  protected int findPrototypeId(String paramString)
  {
    int j;
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 4:
      j = paramString.charAt(0);
      if (j == 101)
      {
        str = "exec";
        i = 4;
      }
      else if (j == 116)
      {
        str = "test";
        i = 5;
      }
      break;
    case 6:
      str = "prefix";
      i = 6;
      break;
    case 7:
      str = "compile";
      i = 1;
      break;
    case 8:
      j = paramString.charAt(3);
      if (j == 111)
      {
        str = "toSource";
        i = 3;
      }
      else if (j == 116)
      {
        str = "toString";
        i = 2;
      }
    case 5:
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    return i;
  }
}