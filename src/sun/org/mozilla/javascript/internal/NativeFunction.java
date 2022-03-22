package sun.org.mozilla.javascript.internal;

import sun.org.mozilla.javascript.internal.debug.DebuggableScript;

public abstract class NativeFunction extends BaseFunction
{
  public final void initScriptFunction(Context paramContext, Scriptable paramScriptable)
  {
    ScriptRuntime.setFunctionProtoAndParent(this, paramScriptable);
  }

  final String decompile(int paramInt1, int paramInt2)
  {
    String str = getEncodedSource();
    if (str == null)
      return super.decompile(paramInt1, paramInt2);
    UintMap localUintMap = new UintMap(1);
    localUintMap.put(1, paramInt1);
    return Decompiler.decompile(str, paramInt2, localUintMap);
  }

  public int getLength()
  {
    int i = getParamCount();
    if (getLanguageVersion() != 120)
      return i;
    Context localContext = Context.getContext();
    NativeCall localNativeCall = ScriptRuntime.findFunctionActivation(localContext, this);
    if (localNativeCall == null)
      return i;
    return localNativeCall.originalArgs.length;
  }

  public int getArity()
  {
    return getParamCount();
  }

  /**
   * @deprecated
   */
  public String jsGet_name()
  {
    return getFunctionName();
  }

  public String getEncodedSource()
  {
    return null;
  }

  public DebuggableScript getDebuggableView()
  {
    return null;
  }

  protected abstract int getLanguageVersion();

  protected abstract int getParamCount();

  protected abstract int getParamAndVarCount();

  protected abstract String getParamOrVarName(int paramInt);
}