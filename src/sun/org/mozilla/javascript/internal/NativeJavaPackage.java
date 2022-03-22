package sun.org.mozilla.javascript.internal;

public class NativeJavaPackage extends ScriptableObject
{
  static final long serialVersionUID = 7445054382212031523L;
  private String packageName;
  private ClassLoader classLoader;

  NativeJavaPackage(boolean paramBoolean, String paramString, ClassLoader paramClassLoader)
  {
    this.packageName = paramString;
    this.classLoader = paramClassLoader;
  }

  /**
   * @deprecated
   */
  public NativeJavaPackage(String paramString, ClassLoader paramClassLoader)
  {
    this(false, paramString, paramClassLoader);
  }

  /**
   * @deprecated
   */
  public NativeJavaPackage(String paramString)
  {
    this(false, paramString, Context.getCurrentContext().getApplicationClassLoader());
  }

  public String getClassName()
  {
    return "JavaPackage";
  }

  public boolean has(String paramString, Scriptable paramScriptable)
  {
    return true;
  }

  public boolean has(int paramInt, Scriptable paramScriptable)
  {
    return false;
  }

  public void put(String paramString, Scriptable paramScriptable, Object paramObject)
  {
  }

  public void put(int paramInt, Scriptable paramScriptable, Object paramObject)
  {
    throw Context.reportRuntimeError0("msg.pkg.int");
  }

  public Object get(String paramString, Scriptable paramScriptable)
  {
    return getPkgProperty(paramString, paramScriptable, true);
  }

  public Object get(int paramInt, Scriptable paramScriptable)
  {
    return NOT_FOUND;
  }

  void forcePackage(String paramString, Scriptable paramScriptable)
  {
    NativeJavaPackage localNativeJavaPackage;
    int i = paramString.indexOf(46);
    if (i == -1)
      i = paramString.length();
    String str1 = paramString.substring(0, i);
    Object localObject = super.get(str1, this);
    if ((localObject != null) && (localObject instanceof NativeJavaPackage))
    {
      localNativeJavaPackage = (NativeJavaPackage)localObject;
    }
    else
    {
      String str2 = this.packageName + "." + str1;
      localNativeJavaPackage = new NativeJavaPackage(true, str2, this.classLoader);
      ScriptRuntime.setObjectProtoAndParent(localNativeJavaPackage, paramScriptable);
      super.put(str1, this, localNativeJavaPackage);
    }
    if (i < paramString.length())
      localNativeJavaPackage.forcePackage(paramString.substring(i + 1), paramScriptable);
  }

  synchronized Object getPkgProperty(String paramString, Scriptable paramScriptable, boolean paramBoolean)
  {
    Object localObject3;
    Object localObject1 = super.get(paramString, paramScriptable);
    if (localObject1 != NOT_FOUND)
      return localObject1;
    String str = this.packageName + '.' + paramString;
    Context localContext = Context.getContext();
    ClassShutter localClassShutter = localContext.getClassShutter();
    Object localObject2 = null;
    if ((localClassShutter == null) || (localClassShutter.visibleToScripts(str)))
    {
      localObject3 = null;
      if (this.classLoader != null)
        localObject3 = Kit.classOrNull(this.classLoader, str);
      else
        localObject3 = Kit.classOrNull(str);
      if (localObject3 != null)
      {
        localObject2 = new NativeJavaClass(getTopLevelScope(this), (Class)localObject3);
        ((Scriptable)localObject2).setPrototype(getPrototype());
      }
    }
    if ((localObject2 == null) && (paramBoolean))
    {
      localObject3 = new NativeJavaPackage(true, str, this.classLoader);
      ScriptRuntime.setObjectProtoAndParent((ScriptableObject)localObject3, getParentScope());
      localObject2 = localObject3;
    }
    if (localObject2 != null)
      super.put(paramString, paramScriptable, localObject2);
    return localObject2;
  }

  public Object getDefaultValue(Class paramClass)
  {
    return toString();
  }

  public String toString()
  {
    return "[JavaPackage " + this.packageName + "]";
  }
}