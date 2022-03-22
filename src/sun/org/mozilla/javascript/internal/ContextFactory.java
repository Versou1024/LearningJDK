package sun.org.mozilla.javascript.internal;

public class ContextFactory
{
  private static volatile boolean hasCustomGlobal;
  private static ContextFactory global = new ContextFactory();
  private volatile boolean sealed;
  private final Object listenersLock = new Object();
  private volatile Object listeners;
  private boolean disabledListening;
  private ClassLoader applicationClassLoader;

  public static ContextFactory getGlobal()
  {
    return global;
  }

  public static boolean hasExplicitGlobal()
  {
    return hasCustomGlobal;
  }

  public static void initGlobal(ContextFactory paramContextFactory)
  {
    if (paramContextFactory == null)
      throw new IllegalArgumentException();
    if (hasCustomGlobal)
      throw new IllegalStateException();
    hasCustomGlobal = true;
    global = paramContextFactory;
  }

  protected Context makeContext()
  {
    return new Context();
  }

  protected boolean hasFeature(Context paramContext, int paramInt)
  {
    int i;
    switch (paramInt)
    {
    case 1:
      i = paramContext.getLanguageVersion();
      return ((i == 100) || (i == 110) || (i == 120));
    case 2:
      return false;
    case 3:
      return false;
    case 4:
      i = paramContext.getLanguageVersion();
      return (i == 120);
    case 5:
      return true;
    case 6:
      i = paramContext.getLanguageVersion();
      return ((i == 0) || (i >= 160));
    case 7:
      return false;
    case 8:
      return false;
    case 9:
      return false;
    }
    throw new IllegalArgumentException(String.valueOf(paramInt));
  }

  protected GeneratedClassLoader createClassLoader(ClassLoader paramClassLoader)
  {
    return new DefiningClassLoader(paramClassLoader);
  }

  public final ClassLoader getApplicationClassLoader()
  {
    return this.applicationClassLoader;
  }

  public final void initApplicationClassLoader(ClassLoader paramClassLoader)
  {
    if (paramClassLoader == null)
      throw new IllegalArgumentException("loader is null");
    if (this.applicationClassLoader != null)
      throw new IllegalStateException("applicationClassLoader can only be set once");
    checkNotSealed();
    this.applicationClassLoader = paramClassLoader;
  }

  protected Object doTopCall(Callable paramCallable, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    return paramCallable.call(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
  }

  protected void observeInstructionCount(Context paramContext, int paramInt)
  {
  }

  protected void onContextCreated(Context paramContext)
  {
    Object localObject = this.listeners;
    int i = 0;
    while (true)
    {
      Listener localListener = (Listener)Kit.getListener(localObject, i);
      if (localListener == null)
        return;
      localListener.contextCreated(paramContext);
      ++i;
    }
  }

  protected void onContextReleased(Context paramContext)
  {
    Object localObject = this.listeners;
    int i = 0;
    while (true)
    {
      Listener localListener = (Listener)Kit.getListener(localObject, i);
      if (localListener == null)
        return;
      localListener.contextReleased(paramContext);
      ++i;
    }
  }

  public final void addListener(Listener paramListener)
  {
    checkNotSealed();
    synchronized (this.listenersLock)
    {
      if (this.disabledListening)
        throw new IllegalStateException();
      this.listeners = Kit.addListener(this.listeners, paramListener);
    }
  }

  public final void removeListener(Listener paramListener)
  {
    checkNotSealed();
    synchronized (this.listenersLock)
    {
      if (this.disabledListening)
        throw new IllegalStateException();
      this.listeners = Kit.removeListener(this.listeners, paramListener);
    }
  }

  final void disableContextListening()
  {
    checkNotSealed();
    synchronized (this.listenersLock)
    {
      this.disabledListening = true;
      this.listeners = null;
    }
  }

  public final boolean isSealed()
  {
    return this.sealed;
  }

  public final void seal()
  {
    checkNotSealed();
    this.sealed = true;
  }

  protected final void checkNotSealed()
  {
    if (this.sealed)
      throw new IllegalStateException();
  }

  public final Object call(ContextAction paramContextAction)
  {
    return Context.call(this, paramContextAction);
  }

  public static abstract interface Listener
  {
    public abstract void contextCreated(Context paramContext);

    public abstract void contextReleased(Context paramContext);
  }
}