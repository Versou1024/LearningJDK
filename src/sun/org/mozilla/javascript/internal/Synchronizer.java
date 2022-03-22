package sun.org.mozilla.javascript.internal;

public class Synchronizer extends Delegator
{
  public Synchronizer(Scriptable paramScriptable)
  {
    super(paramScriptable);
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    synchronized (paramScriptable2)
    {
      return ((Function)this.obj).call(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    }
  }
}