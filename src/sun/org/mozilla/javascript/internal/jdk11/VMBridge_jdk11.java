package sun.org.mozilla.javascript.internal.jdk11;

import java.util.Hashtable;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.VMBridge;

public class VMBridge_jdk11 extends VMBridge
{
  private Hashtable threadsWithContext = new Hashtable();

  protected Object getThreadContextHelper()
  {
    return Thread.currentThread();
  }

  protected Context getContext(Object paramObject)
  {
    Thread localThread = (Thread)paramObject;
    return ((Context)this.threadsWithContext.get(localThread));
  }

  protected void setContext(Object paramObject, Context paramContext)
  {
    Thread localThread = (Thread)paramObject;
    if (paramContext == null)
      this.threadsWithContext.remove(localThread);
    else
      this.threadsWithContext.put(localThread, paramContext);
  }

  protected ClassLoader getCurrentThreadClassLoader()
  {
    return null;
  }

  protected boolean tryToMakeAccessible(Object paramObject)
  {
    return false;
  }
}