package sun.org.mozilla.javascript.internal;

public abstract class SecurityController
{
  private static SecurityController global;

  static SecurityController global()
  {
    return global;
  }

  public static boolean hasGlobal()
  {
    return (global != null);
  }

  public static void initGlobal(SecurityController paramSecurityController)
  {
    if (paramSecurityController == null)
      throw new IllegalArgumentException();
    if (global != null)
      throw new SecurityException("Cannot overwrite already installed global SecurityController");
    global = paramSecurityController;
  }

  public abstract GeneratedClassLoader createClassLoader(ClassLoader paramClassLoader, Object paramObject);

  public static GeneratedClassLoader createLoader(ClassLoader paramClassLoader, Object paramObject)
  {
    GeneratedClassLoader localGeneratedClassLoader;
    Context localContext = Context.getContext();
    if (paramClassLoader == null)
      paramClassLoader = localContext.getApplicationClassLoader();
    SecurityController localSecurityController = localContext.getSecurityController();
    if (localSecurityController == null)
    {
      localGeneratedClassLoader = localContext.createClassLoader(paramClassLoader);
    }
    else
    {
      Object localObject = localSecurityController.getDynamicSecurityDomain(paramObject);
      localGeneratedClassLoader = localSecurityController.createClassLoader(paramClassLoader, localObject);
    }
    return localGeneratedClassLoader;
  }

  public abstract Object getDynamicSecurityDomain(Object paramObject);

  public Object callWithDomain(Object paramObject, Context paramContext, Callable paramCallable, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    return execWithDomain(paramContext, paramScriptable1, new Script(this, paramCallable, paramScriptable2, paramArrayOfObject)
    {
      public Object exec(, Scriptable paramScriptable)
      {
        return this.val$callable.call(paramContext, paramScriptable, this.val$thisObj, this.val$args);
      }
    }
    , paramObject);
  }

  /**
   * @deprecated
   */
  public Object execWithDomain(Context paramContext, Scriptable paramScriptable, Script paramScript, Object paramObject)
  {
    throw new IllegalStateException("callWithDomain should be overridden");
  }
}