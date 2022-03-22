package sun.org.mozilla.javascript.internal;

public final class NativeCall extends IdScriptableObject
{
  static final long serialVersionUID = -7471457301304454454L;
  private static final Object CALL_TAG = new Object();
  private static final int Id_constructor = 1;
  private static final int MAX_PROTOTYPE_ID = 1;
  NativeFunction function;
  Object[] originalArgs;
  transient NativeCall parentActivationCall;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeCall localNativeCall = new NativeCall();
    localNativeCall.exportAsJSClass(1, paramScriptable, paramBoolean);
  }

  NativeCall()
  {
  }

  NativeCall(NativeFunction paramNativeFunction, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    this.function = paramNativeFunction;
    setParentScope(paramScriptable);
    this.originalArgs = ((paramArrayOfObject == null) ? ScriptRuntime.emptyArgs : paramArrayOfObject);
    int i = paramNativeFunction.getParamAndVarCount();
    int j = paramNativeFunction.getParamCount();
    if (i != 0)
      for (k = 0; k != j; ++k)
      {
        str = paramNativeFunction.getParamOrVarName(k);
        Object localObject = (k < paramArrayOfObject.length) ? paramArrayOfObject[k] : Undefined.instance;
        defineProperty(str, localObject, 4);
      }
    if (!(super.has("arguments", this)))
      defineProperty("arguments", new Arguments(this), 4);
    if (i != 0)
      for (k = j; k != i; ++k)
      {
        str = paramNativeFunction.getParamOrVarName(k);
        if (!(super.has(str, this)))
          defineProperty(str, Undefined.instance, 4);
      }
  }

  public String getClassName()
  {
    return "Call";
  }

  protected int findPrototypeId(String paramString)
  {
    return ((paramString.equals("constructor")) ? 1 : 0);
  }

  protected void initPrototypeId(int paramInt)
  {
    String str;
    int i;
    if (paramInt == 1)
    {
      i = 1;
      str = "constructor";
    }
    else
    {
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(CALL_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(paramIdFunctionObject.hasTag(CALL_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    if (i == 1)
    {
      if (paramScriptable2 != null)
        throw Context.reportRuntimeError1("msg.only.from.new", "Call");
      ScriptRuntime.checkDeprecated(paramContext, "Call");
      NativeCall localNativeCall = new NativeCall();
      localNativeCall.setPrototype(getObjectPrototype(paramScriptable1));
      return localNativeCall;
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }
}