package sun.org.mozilla.javascript.internal.regexp;

import sun.org.mozilla.javascript.internal.BaseFunction;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.ScriptRuntime;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.Undefined;

class NativeRegExpCtor extends BaseFunction
{
  static final long serialVersionUID = -5733330028285400526L;
  private static final int Id_multiline = 1;
  private static final int Id_STAR = 2;
  private static final int Id_input = 3;
  private static final int Id_UNDERSCORE = 4;
  private static final int Id_lastMatch = 5;
  private static final int Id_AMPERSAND = 6;
  private static final int Id_lastParen = 7;
  private static final int Id_PLUS = 8;
  private static final int Id_leftContext = 9;
  private static final int Id_BACK_QUOTE = 10;
  private static final int Id_rightContext = 11;
  private static final int Id_QUOTE = 12;
  private static final int DOLLAR_ID_BASE = 12;
  private static final int Id_DOLLAR_1 = 13;
  private static final int Id_DOLLAR_2 = 14;
  private static final int Id_DOLLAR_3 = 15;
  private static final int Id_DOLLAR_4 = 16;
  private static final int Id_DOLLAR_5 = 17;
  private static final int Id_DOLLAR_6 = 18;
  private static final int Id_DOLLAR_7 = 19;
  private static final int Id_DOLLAR_8 = 20;
  private static final int Id_DOLLAR_9 = 21;
  private static final int MAX_INSTANCE_ID = 21;

  public String getFunctionName()
  {
    return "RegExp";
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if ((paramArrayOfObject.length > 0) && (paramArrayOfObject[0] instanceof NativeRegExp) && (((paramArrayOfObject.length == 1) || (paramArrayOfObject[1] == Undefined.instance))))
      return paramArrayOfObject[0];
    return construct(paramContext, paramScriptable1, paramArrayOfObject);
  }

  public Scriptable construct(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    NativeRegExp localNativeRegExp = new NativeRegExp();
    localNativeRegExp.compile(paramContext, paramScriptable, paramArrayOfObject);
    ScriptRuntime.setObjectProtoAndParent(localNativeRegExp, paramScriptable);
    return localNativeRegExp;
  }

  private static RegExpImpl getImpl()
  {
    Context localContext = Context.getCurrentContext();
    return ((RegExpImpl)ScriptRuntime.getRegExpProxy(localContext));
  }

  protected int getMaxInstanceId()
  {
    return (super.getMaxInstanceId() + 21);
  }

  protected int findInstanceIdInfo(String paramString)
  {
    label663: int j;
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 2:
      switch (paramString.charAt(1))
      {
      case '&':
        if (paramString.charAt(0) == '$')
          i = 6;
        break;
      case '\'':
        if (paramString.charAt(0) == '$')
          i = 12;
        break;
      case '*':
        if (paramString.charAt(0) == '$')
          i = 2;
        break;
      case '+':
        if (paramString.charAt(0) == '$')
          i = 8;
        break;
      case '1':
        if (paramString.charAt(0) == '$')
          i = 13;
        break;
      case '2':
        if (paramString.charAt(0) == '$')
          i = 14;
        break;
      case '3':
        if (paramString.charAt(0) == '$')
          i = 15;
        break;
      case '4':
        if (paramString.charAt(0) == '$')
          i = 16;
        break;
      case '5':
        if (paramString.charAt(0) == '$')
          i = 17;
        break;
      case '6':
        if (paramString.charAt(0) == '$')
          i = 18;
        break;
      case '7':
        if (paramString.charAt(0) == '$')
          i = 19;
        break;
      case '8':
        if (paramString.charAt(0) == '$')
          i = 20;
        break;
      case '9':
        if (paramString.charAt(0) == '$')
          i = 21;
        break;
      case '_':
        if (paramString.charAt(0) == '$')
          i = 4;
        break;
      case '`':
        if (paramString.charAt(0) == '$')
        {
          i = 10;
          break label663:
        }
      case '(':
      case ')':
      case ',':
      case '-':
      case '.':
      case '/':
      case '0':
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
      }
      break;
    case 5:
      str = "input";
      i = 3;
      break;
    case 9:
      int k = paramString.charAt(4);
      if (k == 77)
      {
        str = "lastMatch";
        i = 5;
      }
      else if (k == 80)
      {
        str = "lastParen";
        i = 7;
      }
      else if (k == 105)
      {
        str = "multiline";
        i = 1;
      }
      break;
    case 11:
      str = "leftContext";
      i = 9;
      break;
    case 12:
      str = "rightContext";
      i = 11;
    case 3:
    case 4:
    case 6:
    case 7:
    case 8:
    case 10:
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    if (i == 0)
      return super.findInstanceIdInfo(paramString);
    switch (i)
    {
    case 1:
    case 2:
    case 3:
    case 4:
      j = 4;
      break;
    default:
      j = 5;
    }
    return instanceIdInfo(j, super.getMaxInstanceId() + i);
  }

  protected String getInstanceIdName(int paramInt)
  {
    int i = paramInt - super.getMaxInstanceId();
    if ((1 <= i) && (i <= 21))
    {
      switch (i)
      {
      case 1:
        return "multiline";
      case 2:
        return "$*";
      case 3:
        return "input";
      case 4:
        return "$_";
      case 5:
        return "lastMatch";
      case 6:
        return "$&";
      case 7:
        return "lastParen";
      case 8:
        return "$+";
      case 9:
        return "leftContext";
      case 10:
        return "$`";
      case 11:
        return "rightContext";
      case 12:
        return "$'";
      }
      int j = i - 12 - 1;
      char[] arrayOfChar = { '$', (char)(49 + j) };
      return new String(arrayOfChar);
    }
    return super.getInstanceIdName(paramInt);
  }

  protected Object getInstanceIdValue(int paramInt)
  {
    int i = paramInt - super.getMaxInstanceId();
    if ((1 <= i) && (i <= 21))
    {
      Object localObject;
      RegExpImpl localRegExpImpl = getImpl();
      switch (i)
      {
      case 1:
      case 2:
        return ScriptRuntime.wrapBoolean(localRegExpImpl.multiline);
      case 3:
      case 4:
        localObject = localRegExpImpl.input;
        break;
      case 5:
      case 6:
        localObject = localRegExpImpl.lastMatch;
        break;
      case 7:
      case 8:
        localObject = localRegExpImpl.lastParen;
        break;
      case 9:
      case 10:
        localObject = localRegExpImpl.leftContext;
        break;
      case 11:
      case 12:
        localObject = localRegExpImpl.rightContext;
        break;
      default:
        int j = i - 12 - 1;
        localObject = localRegExpImpl.getParenSubString(j);
      }
      return ((localObject == null) ? "" : localObject.toString());
    }
    return super.getInstanceIdValue(paramInt);
  }

  protected void setInstanceIdValue(int paramInt, Object paramObject)
  {
    int i = paramInt - super.getMaxInstanceId();
    switch (i)
    {
    case 1:
    case 2:
      getImpl().multiline = ScriptRuntime.toBoolean(paramObject);
      return;
    case 3:
    case 4:
      getImpl().input = ScriptRuntime.toString(paramObject);
      return;
    }
    super.setInstanceIdValue(paramInt, paramObject);
  }
}