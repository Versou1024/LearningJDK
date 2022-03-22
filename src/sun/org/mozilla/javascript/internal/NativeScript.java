package sun.org.mozilla.javascript.internal;

class NativeScript extends BaseFunction
{
  static final long serialVersionUID = -6795101161980121700L;
  private static final Object SCRIPT_TAG = new Object();
  private static final int Id_constructor = 1;
  private static final int Id_toString = 2;
  private static final int Id_compile = 3;
  private static final int Id_exec = 4;
  private static final int MAX_PROTOTYPE_ID = 4;
  private Script script;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeScript localNativeScript = new NativeScript(null);
    localNativeScript.exportAsJSClass(4, paramScriptable, paramBoolean);
  }

  private NativeScript(Script paramScript)
  {
    this.script = paramScript;
  }

  public String getClassName()
  {
    return "Script";
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (this.script != null)
      return this.script.exec(paramContext, paramScriptable1);
    return Undefined.instance;
  }

  public Scriptable construct(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    throw Context.reportRuntimeError0("msg.script.is.not.constructor");
  }

  public int getLength()
  {
    return 0;
  }

  public int getArity()
  {
    return 0;
  }

  String decompile(int paramInt1, int paramInt2)
  {
    if (this.script instanceof NativeFunction)
      return ((NativeFunction)this.script).decompile(paramInt1, paramInt2);
    return super.decompile(paramInt1, paramInt2);
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
    case 4:
      i = 0;
      str = "exec";
      break;
    case 3:
      i = 1;
      str = "compile";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(SCRIPT_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    Object localObject1;
    Object localObject2;
    if (!(paramIdFunctionObject.hasTag(SCRIPT_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case 1:
      localObject1 = (paramArrayOfObject.length == 0) ? "" : ScriptRuntime.toString(paramArrayOfObject[0]);
      localObject2 = compile(paramContext, (String)localObject1);
      NativeScript localNativeScript = new NativeScript((Script)localObject2);
      ScriptRuntime.setObjectProtoAndParent(localNativeScript, paramScriptable1);
      return localNativeScript;
    case 2:
      localObject1 = realThis(paramScriptable2, paramIdFunctionObject);
      localObject2 = ((NativeScript)localObject1).script;
      if (localObject2 == null)
        return "";
      return paramContext.decompileScript((Script)localObject2, 0);
    case 4:
      throw Context.reportRuntimeError1("msg.cant.call.indirect", "exec");
    case 3:
      localObject1 = realThis(paramScriptable2, paramIdFunctionObject);
      localObject2 = ScriptRuntime.toString(paramArrayOfObject, 0);
      ((NativeScript)localObject1).script = compile(paramContext, (String)localObject2);
      return localObject1;
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  private static NativeScript realThis(Scriptable paramScriptable, IdFunctionObject paramIdFunctionObject)
  {
    if (!(paramScriptable instanceof NativeScript))
      throw incompatibleCallError(paramIdFunctionObject);
    return ((NativeScript)paramScriptable);
  }

  private static Script compile(Context paramContext, String paramString)
  {
    int[] arrayOfInt = { 0 };
    String str = Context.getSourcePositionFromStack(arrayOfInt);
    if (str == null)
    {
      str = "<Script object>";
      arrayOfInt[0] = 1;
    }
    ErrorReporter localErrorReporter = DefaultErrorReporter.forEval(paramContext.getErrorReporter());
    return paramContext.compileString(paramString, null, localErrorReporter, str, arrayOfInt[0], null);
  }

  protected int findPrototypeId(String paramString)
  {
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 4:
      str = "exec";
      i = 4;
      break;
    case 7:
      str = "compile";
      i = 3;
      break;
    case 8:
      str = "toString";
      i = 2;
      break;
    case 11:
      str = "constructor";
      i = 1;
    case 5:
    case 6:
    case 9:
    case 10:
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    return i;
  }
}