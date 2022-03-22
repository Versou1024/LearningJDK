package sun.org.mozilla.javascript.internal.debug;

import sun.org.mozilla.javascript.internal.Context;

public abstract interface Debugger
{
  public abstract void handleCompilationDone(Context paramContext, DebuggableScript paramDebuggableScript, String paramString);

  public abstract DebugFrame getFrame(Context paramContext, DebuggableScript paramDebuggableScript);
}