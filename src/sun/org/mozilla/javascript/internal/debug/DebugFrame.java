package sun.org.mozilla.javascript.internal.debug;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Scriptable;

public abstract interface DebugFrame
{
  public abstract void onEnter(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject);

  public abstract void onLineChange(Context paramContext, int paramInt);

  public abstract void onExceptionThrown(Context paramContext, Throwable paramThrowable);

  public abstract void onExit(Context paramContext, boolean paramBoolean, Object paramObject);
}