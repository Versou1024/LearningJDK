package sun.org.mozilla.javascript.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class LazilyLoadedCtor
{
  private static Method getter;
  private static Method setter;
  private String ctorName;
  private String className;
  private boolean sealed;
  private boolean isReplaced;

  public LazilyLoadedCtor(ScriptableObject paramScriptableObject, String paramString1, String paramString2, boolean paramBoolean)
  {
    this.className = paramString2;
    this.ctorName = paramString1;
    this.sealed = paramBoolean;
    if (getter == null)
    {
      Method[] arrayOfMethod = FunctionObject.getMethodList(super.getClass());
      getter = FunctionObject.findSingleMethod(arrayOfMethod, "getProperty");
      setter = FunctionObject.findSingleMethod(arrayOfMethod, "setProperty");
    }
    paramScriptableObject.defineProperty(paramString1, this, getter, setter, 2);
  }

  public Object getProperty(ScriptableObject paramScriptableObject)
  {
    synchronized (paramScriptableObject)
    {
      if (this.isReplaced)
        break label128;
      int i = 0;
      Class localClass = Kit.classOrNull(this.className);
      if (localClass == null)
        i = 1;
      else
        try
        {
          ScriptableObject.defineClass(paramScriptableObject, localClass, this.sealed);
          this.isReplaced = true;
        }
        catch (InvocationTargetException localInvocationTargetException)
        {
          Throwable localThrowable = localInvocationTargetException.getTargetException();
          if (localThrowable instanceof RuntimeException)
            throw ((RuntimeException)localThrowable);
          i = 1;
        }
        catch (RhinoException localRhinoException)
        {
          i = 1;
        }
        catch (InstantiationException localInstantiationException)
        {
          i = 1;
        }
        catch (IllegalAccessException localIllegalAccessException)
        {
          i = 1;
        }
        catch (SecurityException localSecurityException)
        {
          i = 1;
        }
        catch (LinkageError localLinkageError)
        {
          i = 1;
        }
      if (i == 0)
        break label128;
      paramScriptableObject.delete(this.ctorName);
      label128: return Scriptable.NOT_FOUND;
    }
    return paramScriptableObject.get(this.ctorName, paramScriptableObject);
  }

  public Object setProperty(ScriptableObject paramScriptableObject, Object paramObject)
  {
    synchronized (paramScriptableObject)
    {
      this.isReplaced = true;
      return paramObject;
    }
  }
}