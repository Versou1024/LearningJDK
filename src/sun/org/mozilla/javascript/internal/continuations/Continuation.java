package sun.org.mozilla.javascript.internal.continuations;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.IdFunctionObject;
import sun.org.mozilla.javascript.internal.IdScriptableObject;
import sun.org.mozilla.javascript.internal.Interpreter;
import sun.org.mozilla.javascript.internal.Scriptable;

public final class Continuation extends IdScriptableObject
  implements Function
{
  static final long serialVersionUID = 1794167133757605367L;
  private static final Object FTAG = new Object();
  private Object implementation;
  private static final int Id_constructor = 1;
  private static final int MAX_PROTOTYPE_ID = 1;

  public static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    Continuation localContinuation = new Continuation();
    localContinuation.exportAsJSClass(1, paramScriptable, paramBoolean);
  }

  public Object getImplementation()
  {
    return this.implementation;
  }

  public void initImplementation(Object paramObject)
  {
    this.implementation = paramObject;
  }

  public String getClassName()
  {
    return "Continuation";
  }

  public Scriptable construct(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    throw Context.reportRuntimeError("Direct call is not supported");
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    return Interpreter.restartContinuation(this, paramContext, paramScriptable1, paramArrayOfObject);
  }

  public static boolean isContinuationConstructor(IdFunctionObject paramIdFunctionObject)
  {
    return ((paramIdFunctionObject.hasTag(FTAG)) && (paramIdFunctionObject.methodId() == 1));
  }

  protected void initPrototypeId(int paramInt)
  {
    String str;
    int i;
    switch (paramInt)
    {
    case 1:
      i = 0;
      str = "constructor";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(FTAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(paramIdFunctionObject.hasTag(FTAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case 1:
      throw Context.reportRuntimeError("Direct call is not supported");
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  protected int findPrototypeId(String paramString)
  {
    int i = 0;
    String str = null;
    if (paramString.length() == 11)
    {
      str = "constructor";
      i = 1;
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    return i;
  }
}