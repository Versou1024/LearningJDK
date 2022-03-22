package sun.org.mozilla.javascript.internal;

public class BaseFunction extends IdScriptableObject
  implements Function
{
  static final long serialVersionUID = 5311394446546053859L;
  private static final Object FUNCTION_TAG = new Object();
  private static final int Id_length = 1;
  private static final int Id_arity = 2;
  private static final int Id_name = 3;
  private static final int Id_prototype = 4;
  private static final int Id_arguments = 5;
  private static final int MAX_INSTANCE_ID = 5;
  private static final int Id_constructor = 1;
  private static final int Id_toString = 2;
  private static final int Id_toSource = 3;
  private static final int Id_apply = 4;
  private static final int Id_call = 5;
  private static final int MAX_PROTOTYPE_ID = 5;
  private Object prototypeProperty;
  private boolean isPrototypePropertyImmune;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    BaseFunction localBaseFunction = new BaseFunction();
    localBaseFunction.isPrototypePropertyImmune = true;
    localBaseFunction.exportAsJSClass(5, paramScriptable, paramBoolean);
  }

  public BaseFunction()
  {
  }

  public BaseFunction(Scriptable paramScriptable1, Scriptable paramScriptable2)
  {
    super(paramScriptable1, paramScriptable2);
  }

  public String getClassName()
  {
    return "Function";
  }

  public boolean hasInstance(Scriptable paramScriptable)
  {
    Object localObject = ScriptableObject.getProperty(this, "prototype");
    if (localObject instanceof Scriptable)
      return ScriptRuntime.jsDelegatesTo(paramScriptable, (Scriptable)localObject);
    throw ScriptRuntime.typeError1("msg.instanceof.bad.prototype", getFunctionName());
  }

  protected int getMaxInstanceId()
  {
    return 5;
  }

  protected int findInstanceIdInfo(String paramString)
  {
    int j;
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 4:
      str = "name";
      i = 3;
      break;
    case 5:
      str = "arity";
      i = 2;
      break;
    case 6:
      str = "length";
      i = 1;
      break;
    case 9:
      int k = paramString.charAt(0);
      if (k == 97)
      {
        str = "arguments";
        i = 5;
      }
      else if (k == 112)
      {
        str = "prototype";
        i = 4;
      }
    case 7:
    case 8:
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
      j = 7;
      break;
    case 4:
      j = (this.isPrototypePropertyImmune) ? 7 : 2;
      break;
    case 5:
      j = 6;
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
      return "length";
    case 2:
      return "arity";
    case 3:
      return "name";
    case 4:
      return "prototype";
    case 5:
      return "arguments";
    }
    return super.getInstanceIdName(paramInt);
  }

  protected Object getInstanceIdValue(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return ScriptRuntime.wrapInt(getLength());
    case 2:
      return ScriptRuntime.wrapInt(getArity());
    case 3:
      return getFunctionName();
    case 4:
      return getPrototypeProperty();
    case 5:
      return getArguments();
    }
    return super.getInstanceIdValue(paramInt);
  }

  protected void setInstanceIdValue(int paramInt, Object paramObject)
  {
    if (paramInt == 4)
    {
      if (!(this.isPrototypePropertyImmune))
        this.prototypeProperty = ((paramObject != null) ? paramObject : UniqueTag.NULL_VALUE);
      return;
    }
    if (paramInt == 5)
    {
      if (paramObject == NOT_FOUND)
        Kit.codeBug();
      defaultPut("arguments", paramObject);
    }
    super.setInstanceIdValue(paramInt, paramObject);
  }

  protected void fillConstructorProperties(IdFunctionObject paramIdFunctionObject)
  {
    paramIdFunctionObject.setPrototype(this);
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
      i = 1;
      str = "toString";
      break;
    case 3:
      i = 1;
      str = "toSource";
      break;
    case 4:
      i = 2;
      str = "apply";
      break;
    case 5:
      i = 1;
      str = "call";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(FUNCTION_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    BaseFunction localBaseFunction;
    int j;
    if (!(paramIdFunctionObject.hasTag(FUNCTION_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case 1:
      return jsConstructor(paramContext, paramScriptable1, paramArrayOfObject);
    case 2:
      localBaseFunction = realFunction(paramScriptable2, paramIdFunctionObject);
      j = ScriptRuntime.toInt32(paramArrayOfObject, 0);
      return localBaseFunction.decompile(j, 0);
    case 3:
      localBaseFunction = realFunction(paramScriptable2, paramIdFunctionObject);
      j = 0;
      int k = 2;
      if (paramArrayOfObject.length != 0)
      {
        j = ScriptRuntime.toInt32(paramArrayOfObject[0]);
        if (j >= 0)
          k = 0;
        else
          j = 0;
      }
      return localBaseFunction.decompile(j, k);
    case 4:
    case 5:
      return ScriptRuntime.applyOrCall((i == 4) ? 1 : false, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  private BaseFunction realFunction(Scriptable paramScriptable, IdFunctionObject paramIdFunctionObject)
  {
    Object localObject = paramScriptable.getDefaultValue(ScriptRuntime.FunctionClass);
    if (localObject instanceof BaseFunction)
      return ((BaseFunction)localObject);
    throw ScriptRuntime.typeError1("msg.incompat.call", paramIdFunctionObject.getFunctionName());
  }

  public void setImmunePrototypeProperty(Object paramObject)
  {
    if (this.isPrototypePropertyImmune)
      throw new IllegalStateException();
    this.prototypeProperty = ((paramObject != null) ? paramObject : UniqueTag.NULL_VALUE);
    this.isPrototypePropertyImmune = true;
  }

  protected Scriptable getClassPrototype()
  {
    Object localObject = getPrototypeProperty();
    if (localObject instanceof Scriptable)
      return ((Scriptable)localObject);
    return getClassPrototype(this, "Object");
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    return Undefined.instance;
  }

  public Scriptable construct(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    Object localObject;
    Scriptable localScriptable1 = createObject(paramContext, paramScriptable);
    if (localScriptable1 != null)
    {
      localObject = call(paramContext, paramScriptable, localScriptable1, paramArrayOfObject);
      if (localObject instanceof Scriptable)
        localScriptable1 = (Scriptable)localObject;
    }
    else
    {
      localObject = call(paramContext, paramScriptable, null, paramArrayOfObject);
      if (!(localObject instanceof Scriptable))
        throw new IllegalStateException("Bad implementaion of call as constructor, name=" + getFunctionName() + " in " + getClass().getName());
      localScriptable1 = (Scriptable)localObject;
      if (localScriptable1.getPrototype() == null)
        localScriptable1.setPrototype(getClassPrototype());
      if (localScriptable1.getParentScope() == null)
      {
        Scriptable localScriptable2 = getParentScope();
        if (localScriptable1 != localScriptable2)
          localScriptable1.setParentScope(localScriptable2);
      }
    }
    return localScriptable1;
  }

  public Scriptable createObject(Context paramContext, Scriptable paramScriptable)
  {
    NativeObject localNativeObject = new NativeObject();
    localNativeObject.setPrototype(getClassPrototype());
    localNativeObject.setParentScope(getParentScope());
    return localNativeObject;
  }

  String decompile(int paramInt1, int paramInt2)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = (0 != (paramInt2 & 0x1)) ? 1 : 0;
    if (i == 0)
    {
      localStringBuffer.append("function ");
      localStringBuffer.append(getFunctionName());
      localStringBuffer.append("() {\n\t");
    }
    localStringBuffer.append("[native code, arity=");
    localStringBuffer.append(getArity());
    localStringBuffer.append("]\n");
    if (i == 0)
      localStringBuffer.append("}\n");
    return localStringBuffer.toString();
  }

  public int getArity()
  {
    return 0;
  }

  public int getLength()
  {
    return 0;
  }

  public String getFunctionName()
  {
    return "";
  }

  final Object getPrototypeProperty()
  {
    Object localObject1 = this.prototypeProperty;
    if (localObject1 == null)
      synchronized (this)
      {
        localObject1 = this.prototypeProperty;
        if (localObject1 == null)
        {
          setupDefaultPrototype();
          localObject1 = this.prototypeProperty;
        }
      }
    else if (localObject1 == UniqueTag.NULL_VALUE)
      localObject1 = null;
    return localObject1;
  }

  private void setupDefaultPrototype()
  {
    NativeObject localNativeObject = new NativeObject();
    localNativeObject.defineProperty("constructor", this, 2);
    this.prototypeProperty = localNativeObject;
    Scriptable localScriptable = getObjectPrototype(this);
    if (localScriptable != localNativeObject)
      localNativeObject.setPrototype(localScriptable);
  }

  private Object getArguments()
  {
    Object localObject = defaultGet("arguments");
    if (localObject != NOT_FOUND)
      return localObject;
    Context localContext = Context.getContext();
    NativeCall localNativeCall = ScriptRuntime.findFunctionActivation(localContext, this);
    return ((localNativeCall == null) ? null : localNativeCall.get("arguments", localNativeCall));
  }

  private static Object jsConstructor(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    int i = paramArrayOfObject.length;
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("function ");
    if (paramContext.getLanguageVersion() != 120)
      localStringBuffer.append("anonymous");
    localStringBuffer.append('(');
    for (int j = 0; j < i - 1; ++j)
    {
      if (j > 0)
        localStringBuffer.append(',');
      localStringBuffer.append(ScriptRuntime.toString(paramArrayOfObject[j]));
    }
    localStringBuffer.append(") {");
    if (i != 0)
    {
      str1 = ScriptRuntime.toString(paramArrayOfObject[(i - 1)]);
      localStringBuffer.append(str1);
    }
    localStringBuffer.append('}');
    String str1 = localStringBuffer.toString();
    int[] arrayOfInt = new int[1];
    String str2 = Context.getSourcePositionFromStack(arrayOfInt);
    if (str2 == null)
    {
      str2 = "<eval'ed string>";
      arrayOfInt[0] = 1;
    }
    String str3 = ScriptRuntime.makeUrlForGeneratedScript(false, str2, arrayOfInt[0]);
    Scriptable localScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
    ErrorReporter localErrorReporter = DefaultErrorReporter.forEval(paramContext.getErrorReporter());
    return paramContext.compileFunction(localScriptable, str1, new Interpreter(), localErrorReporter, str3, 1, null);
  }

  protected int findPrototypeId(String paramString)
  {
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 4:
      str = "call";
      i = 5;
      break;
    case 5:
      str = "apply";
      i = 4;
      break;
    case 8:
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
      break;
    case 11:
      str = "constructor";
      i = 1;
    case 6:
    case 7:
    case 9:
    case 10:
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    return i;
  }
}