package sun.org.mozilla.javascript.internal;

public abstract class VMBridge
{
  static final VMBridge instance = makeInstance();

  private static VMBridge makeInstance()
  {
    for (int i = 0; i != 3; ++i)
    {
      String str;
      if (i == 0)
        str = "sun.org.mozilla.javascript.internal.VMBridge_custom";
      else if (i == 1)
        str = "sun.org.mozilla.javascript.internal.jdk13.VMBridge_jdk13";
      else
        str = "sun.org.mozilla.javascript.internal.jdk11.VMBridge_jdk11";
      Class localClass = Kit.classOrNull(str);
      if (localClass != null)
      {
        VMBridge localVMBridge = (VMBridge)Kit.newInstanceOrNull(localClass);
        if (localVMBridge != null)
          return localVMBridge;
      }
    }
    throw new IllegalStateException("Failed to create VMBridge instance");
  }

  protected abstract Object getThreadContextHelper();

  protected abstract Context getContext(Object paramObject);

  protected abstract void setContext(Object paramObject, Context paramContext);

  protected abstract ClassLoader getCurrentThreadClassLoader();

  protected abstract boolean tryToMakeAccessible(Object paramObject);

  protected Object getInterfaceProxyHelper(ContextFactory paramContextFactory, Class[] paramArrayOfClass)
  {
    throw Context.reportRuntimeError("VMBridge.getInterfaceProxyHelper is not supported");
  }

  protected Object newInterfaceProxy(Object paramObject1, ContextFactory paramContextFactory, InterfaceAdapter paramInterfaceAdapter, Object paramObject2, Scriptable paramScriptable)
  {
    throw Context.reportRuntimeError("VMBridge.newInterfaceProxy is not supported");
  }
}