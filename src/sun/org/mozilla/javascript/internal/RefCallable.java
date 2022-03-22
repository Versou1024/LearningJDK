package sun.org.mozilla.javascript.internal;

public abstract interface RefCallable extends Callable
{
  public abstract Ref refCall(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject);
}