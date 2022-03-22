package sun.org.mozilla.javascript.internal;

final class NativeBoolean extends IdScriptableObject
{
  static final long serialVersionUID = -3716996899943880933L;
  private static final Object BOOLEAN_TAG = new Object();
  private static final int Id_constructor = 1;
  private static final int Id_toString = 2;
  private static final int Id_toSource = 3;
  private static final int Id_valueOf = 4;
  private static final int MAX_PROTOTYPE_ID = 4;
  private boolean booleanValue;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeBoolean localNativeBoolean = new NativeBoolean(false);
    localNativeBoolean.exportAsJSClass(4, paramScriptable, paramBoolean);
  }

  private NativeBoolean(boolean paramBoolean)
  {
    this.booleanValue = paramBoolean;
  }

  public String getClassName()
  {
    return "Boolean";
  }

  public Object getDefaultValue(Class paramClass)
  {
    if (paramClass == ScriptRuntime.BooleanClass)
      return ScriptRuntime.wrapBoolean(this.booleanValue);
    return super.getDefaultValue(paramClass);
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
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(BOOLEAN_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(paramIdFunctionObject.hasTag(BOOLEAN_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    if (i == 1)
    {
      bool = ScriptRuntime.toBoolean(paramArrayOfObject, 0);
      if (paramScriptable2 == null)
        return new NativeBoolean(bool);
      return ScriptRuntime.wrapBoolean(bool);
    }
    if (!(paramScriptable2 instanceof NativeBoolean))
      throw incompatibleCallError(paramIdFunctionObject);
    boolean bool = ((NativeBoolean)paramScriptable2).booleanValue;
    switch (i)
    {
    case 2:
      return ((bool) ? "true" : "false");
    case 3:
      return ((bool) ? "(new Boolean(true))" : "(new Boolean(false))");
    case 4:
      return ScriptRuntime.wrapBoolean(bool);
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  protected int findPrototypeId(String paramString)
  {
    int i = 0;
    String str = null;
    int k = paramString.length();
    if (k == 7)
    {
      str = "valueOf";
      i = 4;
    }
    else if (k == 8)
    {
      int j = paramString.charAt(3);
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
    }
    else if (k == 11)
    {
      str = "constructor";
      i = 1;
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    return i;
  }
}