package sun.org.mozilla.javascript.internal;

public abstract interface Function
{
  public abstract Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject);

  public abstract Scriptable construct(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject);
}