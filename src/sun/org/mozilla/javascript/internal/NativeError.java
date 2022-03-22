package sun.org.mozilla.javascript.internal;

final class NativeError extends IdScriptableObject
{
  static final long serialVersionUID = -5338413581437645187L;
  private static final Object ERROR_TAG = new Object();
  private static final int Id_constructor = 1;
  private static final int Id_toString = 2;
  private static final int Id_toSource = 3;
  private static final int MAX_PROTOTYPE_ID = 3;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeError localNativeError = new NativeError();
    ScriptableObject.putProperty(localNativeError, "name", "Error");
    ScriptableObject.putProperty(localNativeError, "message", "");
    ScriptableObject.putProperty(localNativeError, "fileName", "");
    ScriptableObject.putProperty(localNativeError, "lineNumber", new Integer(0));
    localNativeError.exportAsJSClass(3, paramScriptable, paramBoolean);
  }

  static NativeError make(Context paramContext, Scriptable paramScriptable, IdFunctionObject paramIdFunctionObject, Object[] paramArrayOfObject)
  {
    Scriptable localScriptable = (Scriptable)(Scriptable)paramIdFunctionObject.get("prototype", paramIdFunctionObject);
    NativeError localNativeError = new NativeError();
    localNativeError.setPrototype(localScriptable);
    localNativeError.setParentScope(paramScriptable);
    if (paramArrayOfObject.length >= 1)
    {
      ScriptableObject.putProperty(localNativeError, "message", ScriptRuntime.toString(paramArrayOfObject[0]));
      if (paramArrayOfObject.length >= 2)
      {
        ScriptableObject.putProperty(localNativeError, "fileName", paramArrayOfObject[1]);
        if (paramArrayOfObject.length >= 3)
        {
          int i = ScriptRuntime.toInt32(paramArrayOfObject[2]);
          ScriptableObject.putProperty(localNativeError, "lineNumber", new Integer(i));
        }
      }
    }
    return localNativeError;
  }

  public String getClassName()
  {
    return "Error";
  }

  public String toString()
  {
    return js_toString(this);
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
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(ERROR_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(paramIdFunctionObject.hasTag(ERROR_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case 1:
      return make(paramContext, paramScriptable1, paramIdFunctionObject, paramArrayOfObject);
    case 2:
      return js_toString(paramScriptable2);
    case 3:
      return js_toSource(paramContext, paramScriptable1, paramScriptable2);
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  private static String js_toString(Scriptable paramScriptable)
  {
    return getString(paramScriptable, "name") + ": " + getString(paramScriptable, "message");
  }

  private static String js_toSource(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2)
  {
    Object localObject1 = ScriptableObject.getProperty(paramScriptable2, "name");
    Object localObject2 = ScriptableObject.getProperty(paramScriptable2, "message");
    Object localObject3 = ScriptableObject.getProperty(paramScriptable2, "fileName");
    Object localObject4 = ScriptableObject.getProperty(paramScriptable2, "lineNumber");
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("(new ");
    if (localObject1 == NOT_FOUND)
      localObject1 = Undefined.instance;
    localStringBuffer.append(ScriptRuntime.toString(localObject1));
    localStringBuffer.append("(");
    if ((localObject2 != NOT_FOUND) || (localObject3 != NOT_FOUND) || (localObject4 != NOT_FOUND))
    {
      if (localObject2 == NOT_FOUND)
        localObject2 = "";
      localStringBuffer.append(ScriptRuntime.uneval(paramContext, paramScriptable1, localObject2));
      if ((localObject3 != NOT_FOUND) || (localObject4 != NOT_FOUND))
      {
        localStringBuffer.append(", ");
        if (localObject3 == NOT_FOUND)
          localObject3 = "";
        localStringBuffer.append(ScriptRuntime.uneval(paramContext, paramScriptable1, localObject3));
        if (localObject4 != NOT_FOUND)
        {
          int i = ScriptRuntime.toInt32(localObject4);
          if (i != 0)
          {
            localStringBuffer.append(", ");
            localStringBuffer.append(ScriptRuntime.toString(i));
          }
        }
      }
    }
    localStringBuffer.append("))");
    return ((String)(String)localStringBuffer.toString());
  }

  private static String getString(Scriptable paramScriptable, String paramString)
  {
    Object localObject = ScriptableObject.getProperty(paramScriptable, paramString);
    if (localObject == NOT_FOUND)
      return "";
    return ScriptRuntime.toString(localObject);
  }

  protected int findPrototypeId(String paramString)
  {
    int i = 0;
    String str = null;
    int k = paramString.length();
    if (k == 8)
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