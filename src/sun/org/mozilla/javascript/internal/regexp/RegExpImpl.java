package sun.org.mozilla.javascript.internal.regexp;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Kit;
import sun.org.mozilla.javascript.internal.RegExpProxy;
import sun.org.mozilla.javascript.internal.ScriptRuntime;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;
import sun.org.mozilla.javascript.internal.Undefined;

public class RegExpImpl
  implements RegExpProxy
{
  String input;
  boolean multiline;
  SubString[] parens;
  SubString lastMatch;
  SubString lastParen;
  SubString leftContext;
  SubString rightContext;

  public boolean isRegExp(Scriptable paramScriptable)
  {
    return paramScriptable instanceof NativeRegExp;
  }

  public Object compileRegExp(Context paramContext, String paramString1, String paramString2)
  {
    return NativeRegExp.compileRE(paramString1, paramString2, false);
  }

  public Scriptable wrapRegExp(Context paramContext, Scriptable paramScriptable, Object paramObject)
  {
    return new NativeRegExp(paramScriptable, paramObject);
  }

  public Object action(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject, int paramInt)
  {
    Object localObject1;
    GlobData localGlobData = new GlobData();
    localGlobData.mode = paramInt;
    switch (paramInt)
    {
    case 1:
      localGlobData.optarg = 1;
      localObject1 = matchOrReplace(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject, this, localGlobData, false);
      return ((localGlobData.arrayobj == null) ? localObject1 : localGlobData.arrayobj);
    case 3:
      localGlobData.optarg = 1;
      return matchOrReplace(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject, this, localGlobData, false);
    case 2:
      localObject1 = (paramArrayOfObject.length < 2) ? Undefined.instance : paramArrayOfObject[1];
      String str = null;
      Function localFunction = null;
      if (localObject1 instanceof Function)
        localFunction = (Function)localObject1;
      else
        str = ScriptRuntime.toString(localObject1);
      localGlobData.optarg = 2;
      localGlobData.lambda = localFunction;
      localGlobData.repstr = str;
      localGlobData.dollar = ((str == null) ? -1 : str.indexOf(36));
      localGlobData.charBuf = null;
      localGlobData.leftIndex = 0;
      Object localObject2 = matchOrReplace(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject, this, localGlobData, true);
      SubString localSubString1 = this.rightContext;
      if (localGlobData.charBuf == null)
      {
        if ((localGlobData.global) || (localObject2 == null) || (!(localObject2.equals(Boolean.TRUE))))
          return localGlobData.str;
        SubString localSubString2 = this.leftContext;
        replace_glob(localGlobData, paramContext, paramScriptable1, this, localSubString2.index, localSubString2.length);
      }
      localGlobData.charBuf.append(localSubString1.charArray, localSubString1.index, localSubString1.length);
      return localGlobData.charBuf.toString();
    }
    throw Kit.codeBug();
  }

  private static Object matchOrReplace(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject, RegExpImpl paramRegExpImpl, GlobData paramGlobData, boolean paramBoolean)
  {
    NativeRegExp localNativeRegExp;
    String str = ScriptRuntime.toString(paramScriptable2);
    paramGlobData.str = str;
    Scriptable localScriptable = ScriptableObject.getTopLevelScope(paramScriptable1);
    if (paramArrayOfObject.length == 0)
    {
      localObject1 = NativeRegExp.compileRE("", "", false);
      localNativeRegExp = new NativeRegExp(localScriptable, localObject1);
    }
    else if (paramArrayOfObject[0] instanceof NativeRegExp)
    {
      localNativeRegExp = (NativeRegExp)paramArrayOfObject[0];
    }
    else
    {
      localObject1 = ScriptRuntime.toString(paramArrayOfObject[0]);
      if (paramGlobData.optarg < paramArrayOfObject.length)
      {
        paramArrayOfObject[0] = localObject1;
        localObject2 = ScriptRuntime.toString(paramArrayOfObject[paramGlobData.optarg]);
      }
      else
      {
        localObject2 = null;
      }
      Object localObject3 = NativeRegExp.compileRE((String)localObject1, (String)localObject2, paramBoolean);
      localNativeRegExp = new NativeRegExp(localScriptable, localObject3);
    }
    paramGlobData.regexp = localNativeRegExp;
    paramGlobData.global = ((localNativeRegExp.getFlags() & 0x1) != 0);
    Object localObject1 = { 0 };
    Object localObject2 = null;
    if (paramGlobData.mode == 3)
    {
      localObject2 = localNativeRegExp.executeRegExp(paramContext, paramScriptable1, paramRegExpImpl, str, localObject1, 0);
      if ((localObject2 != null) && (localObject2.equals(Boolean.TRUE)))
        localObject2 = new Integer(paramRegExpImpl.leftContext.length);
      else
        localObject2 = new Integer(-1);
    }
    else if (paramGlobData.global)
    {
      localNativeRegExp.lastIndex = 0D;
      for (int i = 0; localObject1[0] <= str.length(); ++i)
      {
        localObject2 = localNativeRegExp.executeRegExp(paramContext, paramScriptable1, paramRegExpImpl, str, localObject1, 0);
        if (localObject2 == null)
          break;
        if (!(localObject2.equals(Boolean.TRUE)))
          break;
        if (paramGlobData.mode == 1)
        {
          match_glob(paramGlobData, paramContext, paramScriptable1, i, paramRegExpImpl);
        }
        else
        {
          if (paramGlobData.mode != 2)
            Kit.codeBug();
          SubString localSubString = paramRegExpImpl.lastMatch;
          int j = paramGlobData.leftIndex;
          int k = localSubString.index - j;
          paramGlobData.leftIndex = (localSubString.index + localSubString.length);
          replace_glob(paramGlobData, paramContext, paramScriptable1, paramRegExpImpl, j, k);
        }
        if (paramRegExpImpl.lastMatch.length == 0)
        {
          if (localObject1[0] == str.length())
            break;
          localObject1[0] += 1;
        }
      }
    }
    else
    {
      localObject2 = localNativeRegExp.executeRegExp(paramContext, paramScriptable1, paramRegExpImpl, str, localObject1, (paramGlobData.mode == 2) ? 0 : 1);
    }
    return localObject2;
  }

  public int find_split(Context paramContext, Scriptable paramScriptable1, String paramString1, String paramString2, Scriptable paramScriptable2, int[] paramArrayOfInt1, int[] paramArrayOfInt2, boolean[] paramArrayOfBoolean, String[][] paramArrayOfString)
  {
    SubString localSubString;
    int i = paramArrayOfInt1[0];
    int j = paramString1.length();
    int l = paramContext.getLanguageVersion();
    NativeRegExp localNativeRegExp = (NativeRegExp)paramScriptable2;
    while (true)
    {
      i1 = paramArrayOfInt1[0];
      paramArrayOfInt1[0] = i;
      Object localObject = localNativeRegExp.executeRegExp(paramContext, paramScriptable1, this, paramString1, paramArrayOfInt1, 0);
      if (localObject != Boolean.TRUE)
      {
        paramArrayOfInt1[0] = i1;
        paramArrayOfInt2[0] = 1;
        paramArrayOfBoolean[0] = false;
        return j;
      }
      i = paramArrayOfInt1[0];
      paramArrayOfInt1[0] = i1;
      paramArrayOfBoolean[0] = true;
      localSubString = this.lastMatch;
      paramArrayOfInt2[0] = localSubString.length;
      if ((paramArrayOfInt2[0] != 0) || (i != paramArrayOfInt1[0]))
        break;
      if (i == j)
      {
        if (l == 120)
        {
          paramArrayOfInt2[0] = 1;
          k = i;
          break label176:
        }
        k = -1;
        break label176:
      }
      ++i;
    }
    int k = i - paramArrayOfInt2[0];
    label176: int i1 = (this.parens == null) ? 0 : this.parens.length;
    paramArrayOfString[0] = new String[i1];
    for (int i2 = 0; i2 < i1; ++i2)
    {
      localSubString = getParenSubString(i2);
      paramArrayOfString[0][i2] = localSubString.toString();
    }
    return k;
  }

  SubString getParenSubString(int paramInt)
  {
    if ((this.parens != null) && (paramInt < this.parens.length))
    {
      SubString localSubString = this.parens[paramInt];
      if (localSubString != null)
        return localSubString;
    }
    return SubString.emptySubString;
  }

  private static void match_glob(GlobData paramGlobData, Context paramContext, Scriptable paramScriptable, int paramInt, RegExpImpl paramRegExpImpl)
  {
    if (paramGlobData.arrayobj == null)
    {
      localObject = ScriptableObject.getTopLevelScope(paramScriptable);
      paramGlobData.arrayobj = ScriptRuntime.newObject(paramContext, (Scriptable)localObject, "Array", null);
    }
    Object localObject = paramRegExpImpl.lastMatch;
    String str = ((SubString)localObject).toString();
    paramGlobData.arrayobj.put(paramInt, paramGlobData.arrayobj, str);
  }

  private static void replace_glob(GlobData paramGlobData, Context paramContext, Scriptable paramScriptable, RegExpImpl paramRegExpImpl, int paramInt1, int paramInt2)
  {
    int i;
    String str;
    Object localObject1;
    int k;
    Object localObject2;
    if (paramGlobData.lambda != null)
    {
      Scriptable localScriptable;
      localObject1 = paramRegExpImpl.parens;
      k = (localObject1 == null) ? 0 : localObject1.length;
      localObject2 = new Object[k + 3];
      localObject2[0] = paramRegExpImpl.lastMatch.toString();
      for (int l = 0; l < k; ++l)
      {
        localScriptable = localObject1[l];
        if (localScriptable != null)
          localObject2[(l + 1)] = localScriptable.toString();
        else
          localObject2[(l + 1)] = Undefined.instance;
      }
      localObject2[(k + 1)] = new Integer(paramRegExpImpl.leftContext.length);
      localObject2[(k + 2)] = paramGlobData.str;
      if (paramRegExpImpl != ScriptRuntime.getRegExpProxy(paramContext))
        Kit.codeBug();
      RegExpImpl localRegExpImpl = new RegExpImpl();
      localRegExpImpl.multiline = paramRegExpImpl.multiline;
      localRegExpImpl.input = paramRegExpImpl.input;
      ScriptRuntime.setRegExpProxy(paramContext, localRegExpImpl);
      try
      {
        localScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
        Object localObject3 = paramGlobData.lambda.call(paramContext, localScriptable, localScriptable, localObject2);
        str = ScriptRuntime.toString(localObject3);
      }
      finally
      {
        ScriptRuntime.setRegExpProxy(paramContext, paramRegExpImpl);
      }
      i = str.length();
    }
    else
    {
      str = null;
      i = paramGlobData.repstr.length();
      if (paramGlobData.dollar >= 0)
      {
        localObject1 = new int[1];
        k = paramGlobData.dollar;
        do
        {
          localObject2 = interpretDollar(paramContext, paramRegExpImpl, paramGlobData.repstr, k, localObject1);
          if (localObject2 != null)
          {
            i += ((SubString)localObject2).length - localObject1[0];
            k += localObject1[0];
          }
          k = paramGlobData.repstr.indexOf(36, ++k);
        }
        while (k >= 0);
      }
    }
    int j = paramInt2 + i + paramRegExpImpl.rightContext.length;
    StringBuffer localStringBuffer = paramGlobData.charBuf;
    if (localStringBuffer == null)
    {
      localStringBuffer = new StringBuffer(j);
      paramGlobData.charBuf = localStringBuffer;
    }
    else
    {
      localStringBuffer.ensureCapacity(paramGlobData.charBuf.length() + j);
    }
    localStringBuffer.append(paramRegExpImpl.leftContext.charArray, paramInt1, paramInt2);
    if (paramGlobData.lambda != null)
      localStringBuffer.append(str);
    else
      do_replace(paramGlobData, paramContext, paramRegExpImpl);
  }

  private static SubString interpretDollar(Context paramContext, RegExpImpl paramRegExpImpl, String paramString, int paramInt, int[] paramArrayOfInt)
  {
    if (paramString.charAt(paramInt) != '$')
      Kit.codeBug();
    int k = paramContext.getLanguageVersion();
    if ((k != 0) && (k <= 140) && (paramInt > 0) && (paramString.charAt(paramInt - 1) == '\\'))
      return null;
    int l = paramString.length();
    if (paramInt + 1 >= l)
      return null;
    char c = paramString.charAt(paramInt + 1);
    if (NativeRegExp.isDigit(c))
    {
      int j;
      if ((k != 0) && (k <= 140))
      {
        if (c == '0')
          return null;
        i = 0;
        i1 = paramInt;
        while (true)
        {
          if (++i1 >= l)
            break label265;
          if (!(NativeRegExp.isDigit(c = paramString.charAt(i1))))
            break label265;
          j = 10 * i + c - '0';
          if (j < i)
            break label265:
          i = j;
        }
      }
      int i2 = (paramRegExpImpl.parens == null) ? 0 : paramRegExpImpl.parens.length;
      int i = c - '0';
      if (i > i2)
        return null;
      int i1 = paramInt + 2;
      if (paramInt + 2 < l)
      {
        c = paramString.charAt(paramInt + 2);
        if (NativeRegExp.isDigit(c))
        {
          j = 10 * i + c - '0';
          if (j <= i2)
          {
            ++i1;
            i = j;
          }
        }
      }
      if (i == 0)
        return null;
      label265: --i;
      paramArrayOfInt[0] = (i1 - paramInt);
      return paramRegExpImpl.getParenSubString(i);
    }
    paramArrayOfInt[0] = 2;
    switch (c)
    {
    case '$':
      return new SubString("$");
    case '&':
      return paramRegExpImpl.lastMatch;
    case '+':
      return paramRegExpImpl.lastParen;
    case '`':
      if (k == 120)
      {
        paramRegExpImpl.leftContext.index = 0;
        paramRegExpImpl.leftContext.length = paramRegExpImpl.lastMatch.index;
      }
      return paramRegExpImpl.leftContext;
    case '\'':
      return paramRegExpImpl.rightContext;
    }
    return null;
  }

  private static void do_replace(GlobData paramGlobData, Context paramContext, RegExpImpl paramRegExpImpl)
  {
    StringBuffer localStringBuffer = paramGlobData.charBuf;
    int i = 0;
    String str = paramGlobData.repstr;
    int j = paramGlobData.dollar;
    if (j != -1)
    {
      int[] arrayOfInt = new int[1];
      do
      {
        int l = j - i;
        localStringBuffer.append(str.substring(i, j));
        i = j;
        SubString localSubString = interpretDollar(paramContext, paramRegExpImpl, str, j, arrayOfInt);
        if (localSubString != null)
        {
          l = localSubString.length;
          if (l > 0)
            localStringBuffer.append(localSubString.charArray, localSubString.index, l);
          i += arrayOfInt[0];
          j += arrayOfInt[0];
        }
        j = str.indexOf(36, ++j);
      }
      while (j >= 0);
    }
    int k = str.length();
    if (k > i)
      localStringBuffer.append(str.substring(i, k));
  }
}