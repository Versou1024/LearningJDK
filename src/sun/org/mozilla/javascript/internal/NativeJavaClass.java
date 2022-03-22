package sun.org.mozilla.javascript.internal;

import java.lang.reflect.Modifier;
import java.util.Hashtable;

public class NativeJavaClass extends NativeJavaObject
  implements Function
{
  static final long serialVersionUID = -6460763940409461664L;
  private Hashtable staticFieldAndMethods;

  public NativeJavaClass()
  {
  }

  public NativeJavaClass(Scriptable paramScriptable, Class paramClass)
  {
    this.parent = paramScriptable;
    this.javaObject = paramClass;
    initMembers();
  }

  protected void initMembers()
  {
    Class localClass = (Class)this.javaObject;
    this.members = JavaMembers.lookupClass(this.parent, localClass, localClass);
    this.staticFieldAndMethods = this.members.getFieldAndMethodsObjects(this, localClass, true);
  }

  public String getClassName()
  {
    return "JavaClass";
  }

  public boolean has(String paramString, Scriptable paramScriptable)
  {
    return this.members.has(paramString, true);
  }

  public Object get(String paramString, Scriptable paramScriptable)
  {
    if (paramString.equals("prototype"))
      return null;
    Object localObject = Scriptable.NOT_FOUND;
    if (this.staticFieldAndMethods != null)
    {
      localObject = this.staticFieldAndMethods.get(paramString);
      if (localObject != null)
        return localObject;
    }
    if (this.members.has(paramString, true))
    {
      localObject = this.members.get(this, paramString, this.javaObject, true);
    }
    else
    {
      Class localClass = findNestedClass(getClassObject(), paramString);
      if (localClass == null)
        throw this.members.reportMemberNotFound(paramString);
      NativeJavaClass localNativeJavaClass = new NativeJavaClass(ScriptableObject.getTopLevelScope(this), localClass);
      localNativeJavaClass.setParentScope(this);
      localObject = localNativeJavaClass;
    }
    return localObject;
  }

  public void put(String paramString, Scriptable paramScriptable, Object paramObject)
  {
    this.members.put(this, paramString, this.javaObject, paramObject, true);
  }

  public Object[] getIds()
  {
    return this.members.getIds(true);
  }

  public Class getClassObject()
  {
    return ((Class)super.unwrap());
  }

  public Object getDefaultValue(Class paramClass)
  {
    if ((paramClass == null) || (paramClass == ScriptRuntime.StringClass))
      return toString();
    if (paramClass == ScriptRuntime.BooleanClass)
      return Boolean.TRUE;
    if (paramClass == ScriptRuntime.NumberClass)
      return ScriptRuntime.NaNobj;
    return this;
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if ((paramArrayOfObject.length == 1) && (paramArrayOfObject[0] instanceof Scriptable))
    {
      Class localClass = getClassObject();
      Scriptable localScriptable = (Scriptable)paramArrayOfObject[0];
      do
      {
        if (localScriptable instanceof Wrapper)
        {
          Object localObject = ((Wrapper)localScriptable).unwrap();
          if (localClass.isInstance(localObject))
            return localScriptable;
        }
        localScriptable = localScriptable.getPrototype();
      }
      while (localScriptable != null);
    }
    return construct(paramContext, paramScriptable1, paramArrayOfObject);
  }

  public Scriptable construct(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    Object localObject3;
    Object localObject4;
    Class localClass = getClassObject();
    int i = localClass.getModifiers();
    if ((!(Modifier.isInterface(i))) && (!(Modifier.isAbstract(i))))
    {
      localObject1 = this.members.ctors;
      int j = NativeJavaMethod.findFunction(paramContext, localObject1, paramArrayOfObject);
      if (j < 0)
      {
        localObject3 = NativeJavaMethod.scriptSignature(paramArrayOfObject);
        throw Context.reportRuntimeError2("msg.no.java.ctor", localClass.getName(), localObject3);
      }
      return constructSpecific(paramContext, paramScriptable, paramArrayOfObject, localObject1[j]);
    }
    Object localObject1 = ScriptableObject.getTopLevelScope(this);
    Object localObject2 = "";
    try
    {
      localObject3 = ((Scriptable)localObject1).get("JavaAdapter", (Scriptable)localObject1);
      if (localObject3 != NOT_FOUND)
      {
        localObject4 = (Function)localObject3;
        Object[] arrayOfObject = { this, paramArrayOfObject[0] };
        return ((Function)localObject4).construct(paramContext, (Scriptable)localObject1, arrayOfObject);
      }
    }
    catch (Exception localException)
    {
      localObject4 = localException.getMessage();
      if (localObject4 != null)
        localObject2 = localObject4;
    }
    throw Context.reportRuntimeError2("msg.cant.instantiate", localObject2, localClass.getName());
  }

  static Scriptable constructSpecific(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject, MemberBox paramMemberBox)
  {
    Scriptable localScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
    Class localClass = paramMemberBox.getDeclaringClass();
    Class[] arrayOfClass = paramMemberBox.argTypes;
    Object[] arrayOfObject = paramArrayOfObject;
    for (int i = 0; i < paramArrayOfObject.length; ++i)
    {
      Object localObject2 = paramArrayOfObject[i];
      Object localObject3 = Context.jsToJava(localObject2, arrayOfClass[i]);
      if (localObject3 != localObject2)
      {
        if (paramArrayOfObject == arrayOfObject)
          paramArrayOfObject = (Object[])(Object[])arrayOfObject.clone();
        paramArrayOfObject[i] = localObject3;
      }
    }
    Object localObject1 = paramMemberBox.newInstance(paramArrayOfObject);
    return paramContext.getWrapFactory().wrapNewObject(paramContext, localScriptable, localObject1);
  }

  public String toString()
  {
    return "[JavaClass " + getClassObject().getName() + "]";
  }

  public boolean hasInstance(Scriptable paramScriptable)
  {
    if ((paramScriptable instanceof Wrapper) && (!(paramScriptable instanceof NativeJavaClass)))
    {
      Object localObject = ((Wrapper)paramScriptable).unwrap();
      return getClassObject().isInstance(localObject);
    }
    return false;
  }

  private static Class findNestedClass(Class paramClass, String paramString)
  {
    String str = paramClass.getName() + '$' + paramString;
    ClassLoader localClassLoader = paramClass.getClassLoader();
    if (localClassLoader == null)
      return Kit.classOrNull(str);
    return Kit.classOrNull(localClassLoader, str);
  }
}