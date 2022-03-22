package sun.org.mozilla.javascript.internal;

import sun.org.mozilla.javascript.internal.debug.DebuggableScript;

final class InterpretedFunction extends NativeFunction
  implements Script
{
  static final long serialVersionUID = 541475680333911468L;
  InterpreterData idata;
  SecurityController securityController;
  Object securityDomain;
  Scriptable[] functionRegExps;

  private InterpretedFunction(InterpreterData paramInterpreterData, Object paramObject)
  {
    this.idata = paramInterpreterData;
    Context localContext = Context.getContext();
    SecurityController localSecurityController = localContext.getSecurityController();
    if (localSecurityController != null)
    {
      localObject = localSecurityController.getDynamicSecurityDomain(paramObject);
    }
    else
    {
      if (paramObject != null)
        throw new IllegalArgumentException();
      localObject = null;
    }
    this.securityController = localSecurityController;
    this.securityDomain = localObject;
  }

  private InterpretedFunction(InterpretedFunction paramInterpretedFunction, int paramInt)
  {
    this.idata = paramInterpretedFunction.idata.itsNestedFunctions[paramInt];
    this.securityController = paramInterpretedFunction.securityController;
    this.securityDomain = paramInterpretedFunction.securityDomain;
  }

  static InterpretedFunction createScript(InterpreterData paramInterpreterData, Object paramObject)
  {
    InterpretedFunction localInterpretedFunction = new InterpretedFunction(paramInterpreterData, paramObject);
    return localInterpretedFunction;
  }

  static InterpretedFunction createFunction(Context paramContext, Scriptable paramScriptable, InterpreterData paramInterpreterData, Object paramObject)
  {
    InterpretedFunction localInterpretedFunction = new InterpretedFunction(paramInterpreterData, paramObject);
    localInterpretedFunction.initInterpretedFunction(paramContext, paramScriptable);
    return localInterpretedFunction;
  }

  static InterpretedFunction createFunction(Context paramContext, Scriptable paramScriptable, InterpretedFunction paramInterpretedFunction, int paramInt)
  {
    InterpretedFunction localInterpretedFunction = new InterpretedFunction(paramInterpretedFunction, paramInt);
    localInterpretedFunction.initInterpretedFunction(paramContext, paramScriptable);
    return localInterpretedFunction;
  }

  Scriptable[] createRegExpWraps(Context paramContext, Scriptable paramScriptable)
  {
    if (this.idata.itsRegExpLiterals == null)
      Kit.codeBug();
    RegExpProxy localRegExpProxy = ScriptRuntime.checkRegExpProxy(paramContext);
    int i = this.idata.itsRegExpLiterals.length;
    Scriptable[] arrayOfScriptable = new Scriptable[i];
    for (int j = 0; j != i; ++j)
      arrayOfScriptable[j] = localRegExpProxy.wrapRegExp(paramContext, paramScriptable, this.idata.itsRegExpLiterals[j]);
    return arrayOfScriptable;
  }

  private void initInterpretedFunction(Context paramContext, Scriptable paramScriptable)
  {
    initScriptFunction(paramContext, paramScriptable);
    if (this.idata.itsRegExpLiterals != null)
      this.functionRegExps = createRegExpWraps(paramContext, paramScriptable);
  }

  public String getFunctionName()
  {
    return ((this.idata.itsName == null) ? "" : this.idata.itsName);
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(ScriptRuntime.hasTopCall(paramContext)))
      return ScriptRuntime.doTopCall(this, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    return Interpreter.interpret(this, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
  }

  public Object exec(Context paramContext, Scriptable paramScriptable)
  {
    if (this.idata.itsFunctionType != 0)
      throw new IllegalStateException();
    if (!(ScriptRuntime.hasTopCall(paramContext)))
      return ScriptRuntime.doTopCall(this, paramContext, paramScriptable, paramScriptable, ScriptRuntime.emptyArgs);
    return Interpreter.interpret(this, paramContext, paramScriptable, paramScriptable, ScriptRuntime.emptyArgs);
  }

  public String getEncodedSource()
  {
    return Interpreter.getEncodedSource(this.idata);
  }

  public DebuggableScript getDebuggableView()
  {
    return this.idata;
  }

  protected int getLanguageVersion()
  {
    return this.idata.languageVersion;
  }

  protected int getParamCount()
  {
    return this.idata.argCount;
  }

  protected int getParamAndVarCount()
  {
    return this.idata.argNames.length;
  }

  protected String getParamOrVarName(int paramInt)
  {
    return this.idata.argNames[paramInt];
  }
}