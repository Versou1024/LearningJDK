package sun.org.mozilla.javascript.internal;

final class NativeString extends IdScriptableObject
{
  static final long serialVersionUID = 920268368584188687L;
  private static final Object STRING_TAG = new Object();
  private static final int Id_length = 1;
  private static final int MAX_INSTANCE_ID = 1;
  private static final int ConstructorId_fromCharCode = -1;
  private static final int Id_constructor = 1;
  private static final int Id_toString = 2;
  private static final int Id_toSource = 3;
  private static final int Id_valueOf = 4;
  private static final int Id_charAt = 5;
  private static final int Id_charCodeAt = 6;
  private static final int Id_indexOf = 7;
  private static final int Id_lastIndexOf = 8;
  private static final int Id_split = 9;
  private static final int Id_substring = 10;
  private static final int Id_toLowerCase = 11;
  private static final int Id_toUpperCase = 12;
  private static final int Id_substr = 13;
  private static final int Id_concat = 14;
  private static final int Id_slice = 15;
  private static final int Id_bold = 16;
  private static final int Id_italics = 17;
  private static final int Id_fixed = 18;
  private static final int Id_strike = 19;
  private static final int Id_small = 20;
  private static final int Id_big = 21;
  private static final int Id_blink = 22;
  private static final int Id_sup = 23;
  private static final int Id_sub = 24;
  private static final int Id_fontsize = 25;
  private static final int Id_fontcolor = 26;
  private static final int Id_link = 27;
  private static final int Id_anchor = 28;
  private static final int Id_equals = 29;
  private static final int Id_equalsIgnoreCase = 30;
  private static final int Id_match = 31;
  private static final int Id_search = 32;
  private static final int Id_replace = 33;
  private static final int MAX_PROTOTYPE_ID = 33;
  private String string;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeString localNativeString = new NativeString("");
    localNativeString.exportAsJSClass(33, paramScriptable, paramBoolean);
  }

  private NativeString(String paramString)
  {
    this.string = paramString;
  }

  public String getClassName()
  {
    return "String";
  }

  protected int getMaxInstanceId()
  {
    return 1;
  }

  protected int findInstanceIdInfo(String paramString)
  {
    if (paramString.equals("length"))
      return instanceIdInfo(7, 1);
    return super.findInstanceIdInfo(paramString);
  }

  protected String getInstanceIdName(int paramInt)
  {
    if (paramInt == 1)
      return "length";
    return super.getInstanceIdName(paramInt);
  }

  protected Object getInstanceIdValue(int paramInt)
  {
    if (paramInt == 1)
      return ScriptRuntime.wrapInt(this.string.length());
    return super.getInstanceIdValue(paramInt);
  }

  protected void fillConstructorProperties(IdFunctionObject paramIdFunctionObject)
  {
    addIdFunctionProperty(paramIdFunctionObject, STRING_TAG, -1, "fromCharCode", 1);
    super.fillConstructorProperties(paramIdFunctionObject);
  }

  protected void initPrototypeId(int paramInt)
  {
    String str;
    int i;
    switch (paramInt)
    {
    case 1:
      i = 1;
      str = "constructor";
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
      i = 0;
      str = "valueOf";
      break;
    case 5:
      i = 1;
      str = "charAt";
      break;
    case 6:
      i = 1;
      str = "charCodeAt";
      break;
    case 7:
      i = 1;
      str = "indexOf";
      break;
    case 8:
      i = 1;
      str = "lastIndexOf";
      break;
    case 9:
      i = 2;
      str = "split";
      break;
    case 10:
      i = 2;
      str = "substring";
      break;
    case 11:
      i = 0;
      str = "toLowerCase";
      break;
    case 12:
      i = 0;
      str = "toUpperCase";
      break;
    case 13:
      i = 2;
      str = "substr";
      break;
    case 14:
      i = 1;
      str = "concat";
      break;
    case 15:
      i = 2;
      str = "slice";
      break;
    case 16:
      i = 0;
      str = "bold";
      break;
    case 17:
      i = 0;
      str = "italics";
      break;
    case 18:
      i = 0;
      str = "fixed";
      break;
    case 19:
      i = 0;
      str = "strike";
      break;
    case 20:
      i = 0;
      str = "small";
      break;
    case 21:
      i = 0;
      str = "big";
      break;
    case 22:
      i = 0;
      str = "blink";
      break;
    case 23:
      i = 0;
      str = "sup";
      break;
    case 24:
      i = 0;
      str = "sub";
      break;
    case 25:
      i = 0;
      str = "fontsize";
      break;
    case 26:
      i = 0;
      str = "fontcolor";
      break;
    case 27:
      i = 0;
      str = "link";
      break;
    case 28:
      i = 0;
      str = "anchor";
      break;
    case 29:
      i = 1;
      str = "equals";
      break;
    case 30:
      i = 1;
      str = "equalsIgnoreCase";
      break;
    case 31:
      i = 1;
      str = "match";
      break;
    case 32:
      i = 1;
      str = "search";
      break;
    case 33:
      i = 1;
      str = "replace";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(STRING_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    String str1;
    if (!(paramIdFunctionObject.hasTag(STRING_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case -1:
      int j = paramArrayOfObject.length;
      if (j < 1)
        return "";
      StringBuffer localStringBuffer = new StringBuffer(j);
      for (int l = 0; l != j; ++l)
        localStringBuffer.append(ScriptRuntime.toUint16(paramArrayOfObject[l]));
      return localStringBuffer.toString();
    case 1:
      str1 = (paramArrayOfObject.length >= 1) ? ScriptRuntime.toString(paramArrayOfObject[0]) : "";
      if (paramScriptable2 == null)
        return new NativeString(str1);
      return str1;
    case 2:
    case 4:
      return realThis(paramScriptable2, paramIdFunctionObject).string;
    case 3:
      str1 = realThis(paramScriptable2, paramIdFunctionObject).string;
      return "(new String(\"" + ScriptRuntime.escapeString(str1) + "\"))";
    case 5:
    case 6:
      str1 = ScriptRuntime.toString(paramScriptable2);
      double d = ScriptRuntime.toInteger(paramArrayOfObject, 0);
      if ((d < 0D) || (d >= str1.length()))
      {
        if (i == 5)
          return "";
        return ScriptRuntime.NaNobj;
      }
      char c = str1.charAt((int)d);
      if (i == 5)
        return String.valueOf(c);
      return ScriptRuntime.wrapInt(c);
    case 7:
      return ScriptRuntime.wrapInt(js_indexOf(ScriptRuntime.toString(paramScriptable2), paramArrayOfObject));
    case 8:
      return ScriptRuntime.wrapInt(js_lastIndexOf(ScriptRuntime.toString(paramScriptable2), paramArrayOfObject));
    case 9:
      return js_split(paramContext, paramScriptable1, ScriptRuntime.toString(paramScriptable2), paramArrayOfObject);
    case 10:
      return js_substring(paramContext, ScriptRuntime.toString(paramScriptable2), paramArrayOfObject);
    case 11:
      return ScriptRuntime.toString(paramScriptable2).toLowerCase();
    case 12:
      return ScriptRuntime.toString(paramScriptable2).toUpperCase();
    case 13:
      return js_substr(ScriptRuntime.toString(paramScriptable2), paramArrayOfObject);
    case 14:
      return js_concat(ScriptRuntime.toString(paramScriptable2), paramArrayOfObject);
    case 15:
      return js_slice(ScriptRuntime.toString(paramScriptable2), paramArrayOfObject);
    case 16:
      return tagify(paramScriptable2, "b", null, null);
    case 17:
      return tagify(paramScriptable2, "i", null, null);
    case 18:
      return tagify(paramScriptable2, "tt", null, null);
    case 19:
      return tagify(paramScriptable2, "strike", null, null);
    case 20:
      return tagify(paramScriptable2, "small", null, null);
    case 21:
      return tagify(paramScriptable2, "big", null, null);
    case 22:
      return tagify(paramScriptable2, "blink", null, null);
    case 23:
      return tagify(paramScriptable2, "sup", null, null);
    case 24:
      return tagify(paramScriptable2, "sub", null, null);
    case 25:
      return tagify(paramScriptable2, "font", "size", paramArrayOfObject);
    case 26:
      return tagify(paramScriptable2, "font", "color", paramArrayOfObject);
    case 27:
      return tagify(paramScriptable2, "a", "href", paramArrayOfObject);
    case 28:
      return tagify(paramScriptable2, "a", "name", paramArrayOfObject);
    case 29:
    case 30:
      str1 = ScriptRuntime.toString(paramScriptable2);
      String str2 = ScriptRuntime.toString(paramArrayOfObject, 0);
      return ScriptRuntime.wrapBoolean((i == 29) ? str1.equals(str2) : str1.equalsIgnoreCase(str2));
    case 31:
    case 32:
    case 33:
      int k;
      if (i == 31)
        k = 1;
      else if (i == 32)
        k = 3;
      else
        k = 2;
      return ScriptRuntime.checkRegExpProxy(paramContext).action(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject, k);
    case 0:
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  private static NativeString realThis(Scriptable paramScriptable, IdFunctionObject paramIdFunctionObject)
  {
    if (!(paramScriptable instanceof NativeString))
      throw incompatibleCallError(paramIdFunctionObject);
    return ((NativeString)paramScriptable);
  }

  private static String tagify(Object paramObject, String paramString1, String paramString2, Object[] paramArrayOfObject)
  {
    String str = ScriptRuntime.toString(paramObject);
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append('<');
    localStringBuffer.append(paramString1);
    if (paramString2 != null)
    {
      localStringBuffer.append(' ');
      localStringBuffer.append(paramString2);
      localStringBuffer.append("=\"");
      localStringBuffer.append(ScriptRuntime.toString(paramArrayOfObject, 0));
      localStringBuffer.append('"');
    }
    localStringBuffer.append('>');
    localStringBuffer.append(str);
    localStringBuffer.append("</");
    localStringBuffer.append(paramString1);
    localStringBuffer.append('>');
    return localStringBuffer.toString();
  }

  public String toString()
  {
    return this.string;
  }

  public Object get(int paramInt, Scriptable paramScriptable)
  {
    if ((0 <= paramInt) && (paramInt < this.string.length()))
      return this.string.substring(paramInt, paramInt + 1);
    return super.get(paramInt, paramScriptable);
  }

  public void put(int paramInt, Scriptable paramScriptable, Object paramObject)
  {
    if ((0 <= paramInt) && (paramInt < this.string.length()))
      return;
    super.put(paramInt, paramScriptable, paramObject);
  }

  private static int js_indexOf(String paramString, Object[] paramArrayOfObject)
  {
    String str = ScriptRuntime.toString(paramArrayOfObject, 0);
    double d = ScriptRuntime.toInteger(paramArrayOfObject, 1);
    if (d > paramString.length())
      return -1;
    if (d < 0D)
      d = 0D;
    return paramString.indexOf(str, (int)d);
  }

  private static int js_lastIndexOf(String paramString, Object[] paramArrayOfObject)
  {
    String str = ScriptRuntime.toString(paramArrayOfObject, 0);
    double d = ScriptRuntime.toNumber(paramArrayOfObject, 1);
    if ((d != d) || (d > paramString.length()))
      d = paramString.length();
    else if (d < 0D)
      d = 0D;
    return paramString.lastIndexOf(str, (int)d);
  }

  private static int find_split(Context paramContext, Scriptable paramScriptable1, String paramString1, String paramString2, int paramInt, RegExpProxy paramRegExpProxy, Scriptable paramScriptable2, int[] paramArrayOfInt1, int[] paramArrayOfInt2, boolean[] paramArrayOfBoolean, String[][] paramArrayOfString)
  {
    int i = paramArrayOfInt1[0];
    int j = paramString1.length();
    if ((paramInt == 120) && (paramScriptable2 == null) && (paramString2.length() == 1) && (paramString2.charAt(0) == ' '))
    {
      if (i == 0)
      {
        while ((i < j) && (Character.isWhitespace(paramString1.charAt(i))))
          ++i;
        paramArrayOfInt1[0] = i;
      }
      if (i == j)
        return -1;
      while ((i < j) && (!(Character.isWhitespace(paramString1.charAt(i)))))
        ++i;
      for (int k = i; (k < j) && (Character.isWhitespace(paramString1.charAt(k))); ++k);
      paramArrayOfInt2[0] = (k - i);
      return i;
    }
    if (i > j)
      return -1;
    if (paramScriptable2 != null)
      return paramRegExpProxy.find_split(paramContext, paramScriptable1, paramString1, paramString2, paramScriptable2, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfBoolean, paramArrayOfString);
    if ((paramInt != 0) && (paramInt < 130) && (j == 0))
      return -1;
    if (paramString2.length() == 0)
    {
      if (paramInt == 120)
      {
        if (i == j)
        {
          paramArrayOfInt2[0] = 1;
          return i;
        }
        return (i + 1);
      }
      return ((i == j) ? -1 : i + 1);
    }
    if (paramArrayOfInt1[0] >= j)
      return j;
    i = paramString1.indexOf(paramString2, paramArrayOfInt1[0]);
    return ((i != -1) ? i : j);
  }

  private static Object js_split(Context paramContext, Scriptable paramScriptable, String paramString, Object[] paramArrayOfObject)
  {
    Scriptable localScriptable1 = getTopLevelScope(paramScriptable);
    Scriptable localScriptable2 = ScriptRuntime.newObject(paramContext, localScriptable1, "Array", null);
    if (paramArrayOfObject.length < 1)
    {
      localScriptable2.put(0, localScriptable2, paramString);
      return localScriptable2;
    }
    int i = ((paramArrayOfObject.length > 1) && (paramArrayOfObject[1] != Undefined.instance)) ? 1 : 0;
    long l = 3412047153814568960L;
    if (i != 0)
    {
      l = ScriptRuntime.toUint32(paramArrayOfObject[1]);
      if (l > paramString.length())
        l = 1 + paramString.length();
    }
    String str1 = null;
    int[] arrayOfInt = new int[1];
    Object localObject1 = null;
    RegExpProxy localRegExpProxy = null;
    if (paramArrayOfObject[0] instanceof Scriptable)
    {
      localRegExpProxy = ScriptRuntime.getRegExpProxy(paramContext);
      if (localRegExpProxy != null)
      {
        localObject2 = (Scriptable)paramArrayOfObject[0];
        if (localRegExpProxy.isRegExp((Scriptable)localObject2))
          localObject1 = localObject2;
      }
    }
    if (localObject1 == null)
    {
      str1 = ScriptRuntime.toString(paramArrayOfObject[0]);
      arrayOfInt[0] = str1.length();
    }
    Object localObject2 = { 0 };
    int k = 0;
    boolean[] arrayOfBoolean = { false };
    [Ljava.lang.String[] arrayOfString; = { null };
    int i1 = paramContext.getLanguageVersion();
    while (((j = find_split(paramContext, paramScriptable, paramString, str1, i1, localRegExpProxy, localObject1, localObject2, arrayOfInt, arrayOfBoolean, arrayOfString;)) >= 0) && (((i == 0) || (k < l))))
    {
      int j;
      String str2;
      if (j > paramString.length())
        break;
      if (paramString.length() == 0)
        str2 = paramString;
      else
        str2 = paramString.substring(localObject2[0], j);
      localScriptable2.put(k, localScriptable2, str2);
      ++k;
      if ((localObject1 != null) && (arrayOfBoolean[0] == 1))
      {
        int i2 = arrayOfString;[0].length;
        for (int i3 = 0; i3 < i2; ++i3)
        {
          if ((i != 0) && (k >= l))
            break;
          localScriptable2.put(k, localScriptable2, arrayOfString;[0][i3]);
          ++k;
        }
        arrayOfBoolean[0] = false;
      }
      localObject2[0] = (j + arrayOfInt[0]);
      if ((i1 < 130) && (i1 != 0) && (i == 0) && (localObject2[0] == paramString.length()))
        break;
    }
    return localScriptable2;
  }

  private static String js_substring(Context paramContext, String paramString, Object[] paramArrayOfObject)
  {
    double d2;
    int i = paramString.length();
    double d1 = ScriptRuntime.toInteger(paramArrayOfObject, 0);
    if (d1 < 0D)
      d1 = 0D;
    else if (d1 > i)
      d1 = i;
    if ((paramArrayOfObject.length <= 1) || (paramArrayOfObject[1] == Undefined.instance))
    {
      d2 = i;
    }
    else
    {
      d2 = ScriptRuntime.toInteger(paramArrayOfObject[1]);
      if (d2 < 0D)
        d2 = 0D;
      else if (d2 > i)
        d2 = i;
      if (d2 < d1)
        if (paramContext.getLanguageVersion() != 120)
        {
          double d3 = d1;
          d1 = d2;
          d2 = d3;
        }
        else
        {
          d2 = d1;
        }
    }
    return paramString.substring((int)d1, (int)d2);
  }

  int getLength()
  {
    return this.string.length();
  }

  private static String js_substr(String paramString, Object[] paramArrayOfObject)
  {
    double d2;
    if (paramArrayOfObject.length < 1)
      return paramString;
    double d1 = ScriptRuntime.toInteger(paramArrayOfObject[0]);
    int i = paramString.length();
    if (d1 < 0D)
    {
      d1 += i;
      if (d1 < 0D)
        d1 = 0D;
    }
    else if (d1 > i)
    {
      d1 = i;
    }
    if (paramArrayOfObject.length == 1)
    {
      d2 = i;
    }
    else
    {
      d2 = ScriptRuntime.toInteger(paramArrayOfObject[1]);
      if (d2 < 0D)
        d2 = 0D;
      d2 += d1;
      if (d2 > i)
        d2 = i;
    }
    return paramString.substring((int)d1, (int)d2);
  }

  private static String js_concat(String paramString, Object[] paramArrayOfObject)
  {
    int i = paramArrayOfObject.length;
    if (i == 0)
      return paramString;
    if (i == 1)
    {
      String str1 = ScriptRuntime.toString(paramArrayOfObject[0]);
      return paramString.concat(str1);
    }
    int j = paramString.length();
    String[] arrayOfString = new String[i];
    for (int k = 0; k != i; ++k)
    {
      String str2 = ScriptRuntime.toString(paramArrayOfObject[k]);
      arrayOfString[k] = str2;
      j += str2.length();
    }
    StringBuffer localStringBuffer = new StringBuffer(j);
    localStringBuffer.append(paramString);
    for (int l = 0; l != i; ++l)
      localStringBuffer.append(arrayOfString[l]);
    return localStringBuffer.toString();
  }

  private static String js_slice(String paramString, Object[] paramArrayOfObject)
  {
    if (paramArrayOfObject.length != 0)
    {
      double d2;
      double d1 = ScriptRuntime.toInteger(paramArrayOfObject[0]);
      int i = paramString.length();
      if (d1 < 0D)
      {
        d1 += i;
        if (d1 < 0D)
          d1 = 0D;
      }
      else if (d1 > i)
      {
        d1 = i;
      }
      if (paramArrayOfObject.length == 1)
      {
        d2 = i;
      }
      else
      {
        d2 = ScriptRuntime.toInteger(paramArrayOfObject[1]);
        if (d2 < 0D)
        {
          d2 += i;
          if (d2 < 0D)
            d2 = 0D;
        }
        else if (d2 > i)
        {
          d2 = i;
        }
        if (d2 < d1)
          d2 = d1;
      }
      return paramString.substring((int)d1, (int)d2);
    }
    return paramString;
  }

  protected int findPrototypeId(String paramString)
  {
    int j;
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 3:
      j = paramString.charAt(2);
      if (j == 98)
      {
        if ((paramString.charAt(0) != 's') || (paramString.charAt(1) != 'u'))
          break label803;
        i = 24;
        break label822:
      }
      if (j == 103)
      {
        if ((paramString.charAt(0) != 'b') || (paramString.charAt(1) != 'i'))
          break label803;
        i = 21;
        break label822:
      }
      if ((j == 112) && (paramString.charAt(0) == 's') && (paramString.charAt(1) == 'u'))
        i = 23;
      break;
    case 4:
      j = paramString.charAt(0);
      if (j == 98)
      {
        str = "bold";
        i = 16;
      }
      else if (j == 108)
      {
        str = "link";
        i = 27;
      }
      break;
    case 5:
      switch (paramString.charAt(4))
      {
      case 'd':
        str = "fixed";
        i = 18;
        break;
      case 'e':
        str = "slice";
        i = 15;
        break;
      case 'h':
        str = "match";
        i = 31;
        break;
      case 'k':
        str = "blink";
        i = 22;
        break;
      case 'l':
        str = "small";
        i = 20;
        break;
      case 't':
        str = "split";
        i = 9;
        break label803:
      case 'f':
      case 'g':
      case 'i':
      case 'j':
      case 'm':
      case 'n':
      case 'o':
      case 'p':
      case 'q':
      case 'r':
      case 's':
      }
      break;
    case 6:
      switch (paramString.charAt(1))
      {
      case 'e':
        str = "search";
        i = 32;
        break;
      case 'h':
        str = "charAt";
        i = 5;
        break;
      case 'n':
        str = "anchor";
        i = 28;
        break;
      case 'o':
        str = "concat";
        i = 14;
        break;
      case 'q':
        str = "equals";
        i = 29;
        break;
      case 't':
        str = "strike";
        i = 19;
        break;
      case 'u':
        str = "substr";
        i = 13;
        break label803:
      case 'f':
      case 'g':
      case 'i':
      case 'j':
      case 'k':
      case 'l':
      case 'm':
      case 'p':
      case 'r':
      case 's':
      }
      break;
    case 7:
      switch (paramString.charAt(1))
      {
      case 'a':
        str = "valueOf";
        i = 4;
        break;
      case 'e':
        str = "replace";
        i = 33;
        break;
      case 'n':
        str = "indexOf";
        i = 7;
        break;
      case 't':
        str = "italics";
        i = 17;
        break label803:
      }
      break;
    case 8:
      j = paramString.charAt(4);
      if (j == 114)
      {
        str = "toString";
        i = 2;
      }
      else if (j == 115)
      {
        str = "fontsize";
        i = 25;
      }
      else if (j == 117)
      {
        str = "toSource";
        i = 3;
      }
      break;
    case 9:
      j = paramString.charAt(0);
      if (j == 102)
      {
        str = "fontcolor";
        i = 26;
      }
      else if (j == 115)
      {
        str = "substring";
        i = 10;
      }
      break;
    case 10:
      str = "charCodeAt";
      i = 6;
      break;
    case 11:
      switch (paramString.charAt(2))
      {
      case 'L':
        str = "toLowerCase";
        i = 11;
        break;
      case 'U':
        str = "toUpperCase";
        i = 12;
        break;
      case 'n':
        str = "constructor";
        i = 1;
        break;
      case 's':
        str = "lastIndexOf";
        i = 8;
        break label803:
      }
      break;
    case 16:
      str = "equalsIgnoreCase";
      i = 30;
    case 12:
    case 13:
    case 14:
    case 15:
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      label803: i = 0;
    label822: return i;
  }
}