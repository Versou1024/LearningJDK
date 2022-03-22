package sun.org.mozilla.javascript.internal;

import java.io.Serializable;
import sun.org.mozilla.javascript.internal.xml.XMLLib;

public class NativeGlobal
  implements Serializable, IdFunctionCall
{
  static final long serialVersionUID = 6080442165748707530L;
  private static final String URI_DECODE_RESERVED = ";/?:@&=+$,#";
  private static final Object FTAG = new Object();
  private static final int Id_decodeURI = 1;
  private static final int Id_decodeURIComponent = 2;
  private static final int Id_encodeURI = 3;
  private static final int Id_encodeURIComponent = 4;
  private static final int Id_escape = 5;
  private static final int Id_eval = 6;
  private static final int Id_isFinite = 7;
  private static final int Id_isNaN = 8;
  private static final int Id_isXMLName = 9;
  private static final int Id_parseFloat = 10;
  private static final int Id_parseInt = 11;
  private static final int Id_unescape = 12;
  private static final int Id_uneval = 13;
  private static final int LAST_SCOPE_FUNCTION_ID = 13;
  private static final int Id_new_CommonError = 14;

  public static void init(Context paramContext, Scriptable paramScriptable, boolean paramBoolean)
  {
    Object localObject;
    NativeGlobal localNativeGlobal = new NativeGlobal();
    for (int i = 1; i <= 13; ++i)
    {
      String str1;
      int k = 1;
      switch (i)
      {
      case 1:
        str1 = "decodeURI";
        break;
      case 2:
        str1 = "decodeURIComponent";
        break;
      case 3:
        str1 = "encodeURI";
        break;
      case 4:
        str1 = "encodeURIComponent";
        break;
      case 5:
        str1 = "escape";
        break;
      case 6:
        str1 = "eval";
        break;
      case 7:
        str1 = "isFinite";
        break;
      case 8:
        str1 = "isNaN";
        break;
      case 9:
        str1 = "isXMLName";
        break;
      case 10:
        str1 = "parseFloat";
        break;
      case 11:
        str1 = "parseInt";
        k = 2;
        break;
      case 12:
        str1 = "unescape";
        break;
      case 13:
        str1 = "uneval";
        break;
      default:
        throw Kit.codeBug();
      }
      localObject = new IdFunctionObject(localNativeGlobal, FTAG, i, str1, k, paramScriptable);
      if (paramBoolean)
        ((IdFunctionObject)localObject).sealObject();
      ((IdFunctionObject)localObject).exportAsScopeProperty();
    }
    ScriptableObject.defineProperty(paramScriptable, "NaN", ScriptRuntime.NaNobj, 2);
    ScriptableObject.defineProperty(paramScriptable, "Infinity", ScriptRuntime.wrapNumber((1.0D / 0.0D)), 2);
    ScriptableObject.defineProperty(paramScriptable, "undefined", Undefined.instance, 2);
    String[] arrayOfString = Kit.semicolonSplit("ConversionError;EvalError;RangeError;ReferenceError;SyntaxError;TypeError;URIError;InternalError;JavaException;");
    for (int j = 0; j < arrayOfString.length; ++j)
    {
      String str2 = arrayOfString[j];
      localObject = ScriptRuntime.newObject(paramContext, paramScriptable, "Error", ScriptRuntime.emptyArgs);
      ((Scriptable)localObject).put("name", (Scriptable)localObject, str2);
      if ((paramBoolean) && (localObject instanceof ScriptableObject))
        ((ScriptableObject)localObject).sealObject();
      IdFunctionObject localIdFunctionObject = new IdFunctionObject(localNativeGlobal, FTAG, 14, str2, 1, paramScriptable);
      localIdFunctionObject.markAsConstructor((Scriptable)localObject);
      if (paramBoolean)
        localIdFunctionObject.sealObject();
      localIdFunctionObject.exportAsScopeProperty();
    }
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (paramIdFunctionObject.hasTag(FTAG))
    {
      String str;
      boolean bool;
      Object localObject;
      double d;
      int i = paramIdFunctionObject.methodId();
      switch (i)
      {
      case 1:
      case 2:
        str = ScriptRuntime.toString(paramArrayOfObject, 0);
        return decode(str, i == 1);
      case 3:
      case 4:
        str = ScriptRuntime.toString(paramArrayOfObject, 0);
        return encode(str, i == 3);
      case 5:
        return js_escape(paramArrayOfObject);
      case 6:
        return js_eval(paramContext, paramScriptable1, paramArrayOfObject);
      case 7:
        if (paramArrayOfObject.length < 1)
        {
          bool = false;
        }
        else
        {
          d = ScriptRuntime.toNumber(paramArrayOfObject[0]);
          bool = (d == d) && (d != (1.0D / 0.0D)) && (d != (-1.0D / 0.0D));
        }
        return ScriptRuntime.wrapBoolean(bool);
      case 8:
        if (paramArrayOfObject.length < 1)
        {
          bool = true;
        }
        else
        {
          d = ScriptRuntime.toNumber(paramArrayOfObject[0]);
          bool = d != d;
        }
        return ScriptRuntime.wrapBoolean(bool);
      case 9:
        localObject = (paramArrayOfObject.length == 0) ? Undefined.instance : paramArrayOfObject[0];
        XMLLib localXMLLib = XMLLib.extractFromScope(paramScriptable1);
        return ScriptRuntime.wrapBoolean(localXMLLib.isXMLName(paramContext, localObject));
      case 10:
        return js_parseFloat(paramArrayOfObject);
      case 11:
        return js_parseInt(paramArrayOfObject);
      case 12:
        return js_unescape(paramArrayOfObject);
      case 13:
        localObject = (paramArrayOfObject.length != 0) ? paramArrayOfObject[0] : Undefined.instance;
        return ScriptRuntime.uneval(paramContext, paramScriptable1, localObject);
      case 14:
        return NativeError.make(paramContext, paramScriptable1, paramIdFunctionObject, paramArrayOfObject);
      }
    }
    throw paramIdFunctionObject.unknown();
  }

  private Object js_parseInt(Object[] paramArrayOfObject)
  {
    char c;
    String str = ScriptRuntime.toString(paramArrayOfObject, 0);
    int i = ScriptRuntime.toInt32(paramArrayOfObject, 1);
    int j = str.length();
    if (j == 0)
      return ScriptRuntime.NaNobj;
    int k = 0;
    int l = 0;
    do
    {
      c = str.charAt(l);
      if (!(Character.isWhitespace(c)))
        break;
    }
    while (++l < j);
    if (c != '+')
      if ((k = (c == '-') ? 1 : 0) == 0)
        break label90;
    ++l;
    if (i == 0)
    {
      label90: i = -1;
    }
    else
    {
      if ((i < 2) || (i > 36))
        return ScriptRuntime.NaNobj;
      if ((i == 16) && (j - l > 1) && (str.charAt(l) == '0'))
      {
        c = str.charAt(l + 1);
        if ((c == 'x') || (c == 'X'))
          l += 2;
      }
    }
    if (i == -1)
    {
      i = 10;
      if ((j - l > 1) && (str.charAt(l) == '0'))
      {
        c = str.charAt(l + 1);
        if ((c == 'x') || (c == 'X'))
        {
          i = 16;
          l += 2;
        }
        else if (('0' <= c) && (c <= '9'))
        {
          i = 8;
        }
      }
    }
    double d = ScriptRuntime.stringToNumber(???, ++l, i);
    return ScriptRuntime.wrapNumber((k != 0) ? -d : d);
  }

  private Object js_parseFloat(Object[] paramArrayOfObject)
  {
    int k;
    if (paramArrayOfObject.length < 1)
      return ScriptRuntime.NaNobj;
    String str = ScriptRuntime.toString(paramArrayOfObject[0]);
    int i = str.length();
    int j = 0;
    while (true)
    {
      if (j == i)
        return ScriptRuntime.NaNobj;
      k = str.charAt(j);
      if (!(TokenStream.isJSSpace(k)))
        break;
      ++j;
    }
    int l = j;
    if ((k == 43) || (k == 45))
    {
      if (++l == i)
        return ScriptRuntime.NaNobj;
      k = str.charAt(l);
    }
    if (k == 73)
    {
      if ((l + 8 <= i) && (str.regionMatches(l, "Infinity", 0, 8)))
      {
        double d;
        if (str.charAt(j) == '-')
          d = (-1.0D / 0.0D);
        else
          d = (1.0D / 0.0D);
        return ScriptRuntime.wrapNumber(d);
      }
      return ScriptRuntime.NaNobj;
    }
    int i1 = -1;
    int i2 = -1;
    while (l < i)
    {
      switch (str.charAt(l))
      {
      case '.':
        if (i1 != -1)
          break;
        i1 = l;
        break;
      case 'E':
      case 'e':
        if (i2 != -1)
          break;
        i2 = l;
        break;
      case '+':
      case '-':
        if (i2 != l - 1);
        break;
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        break;
      case ',':
      case '/':
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
      case 'D':
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
      case 'S':
      case 'T':
      case 'U':
      case 'V':
      case 'W':
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
      case 'b':
      case 'c':
      case 'd':
      default:
        break;
      }
      ++l;
    }
    str = str.substring(j, l);
    try
    {
      return Double.valueOf(str);
    }
    catch (NumberFormatException localNumberFormatException)
    {
    }
    return ScriptRuntime.NaNobj;
  }

  private Object js_escape(Object[] paramArrayOfObject)
  {
    String str = ScriptRuntime.toString(paramArrayOfObject, 0);
    int i = 7;
    if (paramArrayOfObject.length > 1)
    {
      double d = ScriptRuntime.toNumber(paramArrayOfObject[1]);
      if (d == d)
        if (((i = (int)d) == d) && (0 == (i & 0xFFFFFFF8)))
          break label61;
      throw Context.reportRuntimeError0("msg.bad.esc.mask");
    }
    label61: StringBuffer localStringBuffer = null;
    int j = 0;
    int k = str.length();
    while (j != k)
    {
      int l = str.charAt(j);
      if ((i != 0) && ((((l >= 48) && (l <= 57)) || ((l >= 65) && (l <= 90)) || ((l >= 97) && (l <= 122)) || (l == 64) || (l == 42) || (l == 95) || (l == 45) || (l == 46) || ((0 != (i & 0x4)) && (((l == 47) || (l == 43)))))))
      {
        if (localStringBuffer != null)
          localStringBuffer.append((char)l);
      }
      else
      {
        int i1;
        if (localStringBuffer == null)
        {
          localStringBuffer = new StringBuffer(k + 3);
          localStringBuffer.append(str);
          localStringBuffer.setLength(j);
        }
        if (l < 256)
        {
          if ((l == 32) && (i == 2))
          {
            localStringBuffer.append('+');
            break label369:
          }
          localStringBuffer.append('%');
          i1 = 2;
        }
        else
        {
          localStringBuffer.append('%');
          localStringBuffer.append('u');
          i1 = 4;
        }
        for (int i2 = (i1 - 1) * 4; i2 >= 0; i2 -= 4)
        {
          int i3 = 0xF & l >> i2;
          int i4 = (i3 < 10) ? 48 + i3 : 55 + i3;
          localStringBuffer.append((char)i4);
        }
      }
      label369: ++j;
    }
    return ((localStringBuffer == null) ? str : localStringBuffer.toString());
  }

  private Object js_unescape(Object[] paramArrayOfObject)
  {
    String str = ScriptRuntime.toString(paramArrayOfObject, 0);
    int i = str.indexOf(37);
    if (i >= 0)
    {
      int j = str.length();
      char[] arrayOfChar = str.toCharArray();
      int k = i;
      int l = i;
      while (l != j)
      {
        int i1 = arrayOfChar[l];
        ++l;
        if ((i1 == 37) && (l != j))
        {
          int i2;
          int i3;
          if (arrayOfChar[l] == 'u')
          {
            i3 = l + 1;
            i2 = l + 5;
          }
          else
          {
            i3 = l;
            i2 = l + 2;
          }
          if (i2 <= j)
          {
            int i4 = 0;
            for (int i5 = i3; i5 != i2; ++i5)
              i4 = Kit.xDigitToInt(arrayOfChar[i5], i4);
            if (i4 >= 0)
            {
              i1 = (char)i4;
              l = i2;
            }
          }
        }
        arrayOfChar[k] = i1;
        ++k;
      }
      str = new String(arrayOfChar, 0, k);
    }
    return str;
  }

  private Object js_eval(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    String str = ScriptRuntime.getMessage1("msg.cant.call.indirect", "eval");
    throw constructError(paramContext, "EvalError", str, paramScriptable);
  }

  static boolean isEvalFunction(Object paramObject)
  {
    if (!(paramObject instanceof IdFunctionObject))
      break label33;
    IdFunctionObject localIdFunctionObject = (IdFunctionObject)paramObject;
    label33: return ((localIdFunctionObject.hasTag(FTAG)) && (localIdFunctionObject.methodId() == 6));
  }

  /**
   * @deprecated
   */
  public static EcmaError constructError(Context paramContext, String paramString1, String paramString2, Scriptable paramScriptable)
  {
    return ScriptRuntime.constructError(paramString1, paramString2);
  }

  /**
   * @deprecated
   */
  public static EcmaError constructError(Context paramContext, String paramString1, String paramString2, Scriptable paramScriptable, String paramString3, int paramInt1, int paramInt2, String paramString4)
  {
    return ScriptRuntime.constructError(paramString1, paramString2, paramString3, paramInt1, paramString4, paramInt2);
  }

  private static String encode(String paramString, boolean paramBoolean)
  {
    byte[] arrayOfByte = null;
    StringBuffer localStringBuffer = null;
    int i = 0;
    int j = paramString.length();
    while (i != j)
    {
      int k = paramString.charAt(i);
      if (encodeUnescaped(k, paramBoolean))
      {
        if (localStringBuffer != null)
          localStringBuffer.append(k);
      }
      else
      {
        int i1;
        if (localStringBuffer == null)
        {
          localStringBuffer = new StringBuffer(j + 3);
          localStringBuffer.append(paramString);
          localStringBuffer.setLength(i);
          arrayOfByte = new byte[6];
        }
        if ((56320 <= k) && (k <= 57343))
          throw Context.reportRuntimeError0("msg.bad.uri");
        if ((k < 55296) || (56319 < k))
        {
          int l = k;
        }
        else
        {
          if (++i == j)
            throw Context.reportRuntimeError0("msg.bad.uri");
          i2 = paramString.charAt(i);
          if ((56320 > i2) || (i2 > 57343))
            throw Context.reportRuntimeError0("msg.bad.uri");
          i1 = (k - 55296 << 10) + i2 - 56320 + 65536;
        }
        int i2 = oneUcs4ToUtf8Char(arrayOfByte, i1);
        for (int i3 = 0; i3 < i2; ++i3)
        {
          int i4 = 0xFF & arrayOfByte[i3];
          localStringBuffer.append('%');
          localStringBuffer.append(toHexChar(i4 >>> 4));
          localStringBuffer.append(toHexChar(i4 & 0xF));
        }
      }
      ++i;
    }
    return ((localStringBuffer == null) ? paramString : localStringBuffer.toString());
  }

  private static char toHexChar(int paramInt)
  {
    if (paramInt >> 4 != 0)
      Kit.codeBug();
    return (char)((paramInt < 10) ? paramInt + 48 : paramInt - 10 + 97);
  }

  private static int unHex(char paramChar)
  {
    if (('A' <= paramChar) && (paramChar <= 'F'))
      return (paramChar - 'A' + 10);
    if (('a' <= paramChar) && (paramChar <= 'f'))
      return (paramChar - 'a' + 10);
    if (('0' <= paramChar) && (paramChar <= '9'))
      return (paramChar - '0');
    return -1;
  }

  private static int unHex(char paramChar1, char paramChar2)
  {
    int i = unHex(paramChar1);
    int j = unHex(paramChar2);
    if ((i >= 0) && (j >= 0))
      return (i << 4 | j);
    return -1;
  }

  private static String decode(String paramString, boolean paramBoolean)
  {
    char[] arrayOfChar = null;
    int i = 0;
    int j = 0;
    int k = paramString.length();
    while (j != k)
    {
      int l = paramString.charAt(j);
      if (l != 37)
      {
        if (arrayOfChar != null)
          arrayOfChar[(i++)] = l;
        ++j;
      }
      else
      {
        int i3;
        if (arrayOfChar == null)
        {
          arrayOfChar = new char[k];
          paramString.getChars(0, j, arrayOfChar, 0);
          i = j;
        }
        int i1 = j;
        if (j + 3 > k)
          throw Context.reportRuntimeError0("msg.bad.uri");
        int i2 = unHex(paramString.charAt(j + 1), paramString.charAt(j + 2));
        if (i2 < 0)
          throw Context.reportRuntimeError0("msg.bad.uri");
        j += 3;
        if ((i2 & 0x80) == 0)
        {
          l = (char)i2;
        }
        else
        {
          int i4;
          int i5;
          if ((i2 & 0xC0) == 128)
            throw Context.reportRuntimeError0("msg.bad.uri");
          if ((i2 & 0x20) == 0)
          {
            i3 = 1;
            i4 = i2 & 0x1F;
            i5 = 128;
          }
          else if ((i2 & 0x10) == 0)
          {
            i3 = 2;
            i4 = i2 & 0xF;
            i5 = 2048;
          }
          else if ((i2 & 0x8) == 0)
          {
            i3 = 3;
            i4 = i2 & 0x7;
            i5 = 65536;
          }
          else if ((i2 & 0x4) == 0)
          {
            i3 = 4;
            i4 = i2 & 0x3;
            i5 = 2097152;
          }
          else if ((i2 & 0x2) == 0)
          {
            i3 = 5;
            i4 = i2 & 0x1;
            i5 = 67108864;
          }
          else
          {
            throw Context.reportRuntimeError0("msg.bad.uri");
          }
          if (j + 3 * i3 > k)
            throw Context.reportRuntimeError0("msg.bad.uri");
          for (int i6 = 0; i6 != i3; ++i6)
          {
            if (paramString.charAt(j) != '%')
              throw Context.reportRuntimeError0("msg.bad.uri");
            i2 = unHex(paramString.charAt(j + 1), paramString.charAt(j + 2));
            if ((i2 < 0) || ((i2 & 0xC0) != 128))
              throw Context.reportRuntimeError0("msg.bad.uri");
            i4 = i4 << 6 | i2 & 0x3F;
            j += 3;
          }
          if ((i4 < i5) || (i4 == 65534) || (i4 == 65535))
            i4 = 65533;
          if (i4 >= 65536)
          {
            i4 -= 65536;
            if (i4 > 1048575)
              throw Context.reportRuntimeError0("msg.bad.uri");
            i6 = (char)((i4 >>> 10) + 55296);
            l = (char)((i4 & 0x3FF) + 56320);
            arrayOfChar[(i++)] = i6;
          }
          else
          {
            l = (char)i4;
          }
        }
        if ((paramBoolean) && (";/?:@&=+$,#".indexOf(l) >= 0))
          for (i3 = i1; i3 != j; ++i3)
            arrayOfChar[(i++)] = paramString.charAt(i3);
        else
          arrayOfChar[(i++)] = l;
      }
    }
    return new String(arrayOfChar, 0, i);
  }

  private static boolean encodeUnescaped(char paramChar, boolean paramBoolean)
  {
    if ((('A' <= paramChar) && (paramChar <= 'Z')) || (('a' <= paramChar) && (paramChar <= 'z')) || (('0' <= paramChar) && (paramChar <= '9')))
      return true;
    if ("-_.!~*'()".indexOf(paramChar) >= 0)
      return true;
    if (paramBoolean)
      return (";/?:@&=+$,#".indexOf(paramChar) >= 0);
    return false;
  }

  private static int oneUcs4ToUtf8Char(byte[] paramArrayOfByte, int paramInt)
  {
    int i = 1;
    if ((paramInt & 0xFFFFFF80) == 0)
    {
      paramArrayOfByte[0] = (byte)paramInt;
    }
    else
    {
      int k = paramInt >>> 11;
      for (i = 2; k != 0; ++i)
        k >>>= 5;
      int j = i;
      while (--j > 0)
      {
        paramArrayOfByte[j] = (byte)(paramInt & 0x3F | 0x80);
        paramInt >>>= 6;
      }
      paramArrayOfByte[0] = (byte)(256 - (1 << 8 - i) + paramInt);
    }
    return i;
  }
}