package sun.org.mozilla.javascript.internal;

public class Delegator
  implements Function
{
  protected Scriptable obj = null;

  public Delegator()
  {
  }

  public Delegator(Scriptable paramScriptable)
  {
    this.obj = paramScriptable;
  }

  protected Delegator newInstance()
  {
    try
    {
      return ((Delegator)super.getClass().newInstance());
    }
    catch (Exception localException)
    {
      throw Context.throwAsScriptRuntimeEx(localException);
    }
  }

  public Scriptable getDelegee()
  {
    return this.obj;
  }

  public void setDelegee(Scriptable paramScriptable)
  {
    this.obj = paramScriptable;
  }

  public String getClassName()
  {
    return this.obj.getClassName();
  }

  public Object get(String paramString, Scriptable paramScriptable)
  {
    return this.obj.get(paramString, paramScriptable);
  }

  public Object get(int paramInt, Scriptable paramScriptable)
  {
    return this.obj.get(paramInt, paramScriptable);
  }

  public boolean has(String paramString, Scriptable paramScriptable)
  {
    return this.obj.has(paramString, paramScriptable);
  }

  public boolean has(int paramInt, Scriptable paramScriptable)
  {
    return this.obj.has(paramInt, paramScriptable);
  }

  public void put(String paramString, Scriptable paramScriptable, Object paramObject)
  {
    this.obj.put(paramString, paramScriptable, paramObject);
  }

  public void put(int paramInt, Scriptable paramScriptable, Object paramObject)
  {
    this.obj.put(paramInt, paramScriptable, paramObject);
  }

  public void delete(String paramString)
  {
    this.obj.delete(paramString);
  }

  public void delete(int paramInt)
  {
    this.obj.delete(paramInt);
  }

  public Scriptable getPrototype()
  {
    return this.obj.getPrototype();
  }

  public void setPrototype(Scriptable paramScriptable)
  {
    this.obj.setPrototype(paramScriptable);
  }

  public Scriptable getParentScope()
  {
    return this.obj.getParentScope();
  }

  public void setParentScope(Scriptable paramScriptable)
  {
    this.obj.setParentScope(paramScriptable);
  }

  public Object[] getIds()
  {
    return this.obj.getIds();
  }

  public Object getDefaultValue(Class paramClass)
  {
    return (((paramClass == null) || (paramClass == ScriptRuntime.ScriptableClass) || (paramClass == ScriptRuntime.FunctionClass)) ? this : this.obj.getDefaultValue(paramClass));
  }

  public boolean hasInstance(Scriptable paramScriptable)
  {
    return this.obj.hasInstance(paramScriptable);
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    return ((Function)this.obj).call(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
  }

  public Scriptable construct(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    if (this.obj == null)
    {
      Object localObject;
      Delegator localDelegator = newInstance();
      if (paramArrayOfObject.length == 0)
        localObject = new NativeObject();
      else
        localObject = ScriptRuntime.toObject(paramContext, paramScriptable, paramArrayOfObject[0]);
      localDelegator.setDelegee((Scriptable)localObject);
      return localDelegator;
    }
    return ((Scriptable)((Function)this.obj).construct(paramContext, paramScriptable, paramArrayOfObject));
  }
}