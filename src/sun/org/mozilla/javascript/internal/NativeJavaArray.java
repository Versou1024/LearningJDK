package sun.org.mozilla.javascript.internal;

import java.lang.reflect.Array;

public class NativeJavaArray extends NativeJavaObject
{
  static final long serialVersionUID = -924022554283675333L;
  Object array;
  int length;
  Class cls;
  Scriptable prototype;

  public String getClassName()
  {
    return "JavaArray";
  }

  public static NativeJavaArray wrap(Scriptable paramScriptable, Object paramObject)
  {
    return new NativeJavaArray(paramScriptable, paramObject);
  }

  public Object unwrap()
  {
    return this.array;
  }

  public NativeJavaArray(Scriptable paramScriptable, Object paramObject)
  {
    super(paramScriptable, null, ScriptRuntime.ObjectClass);
    Class localClass = paramObject.getClass();
    if (!(localClass.isArray()))
      throw new RuntimeException("Array expected");
    this.array = paramObject;
    this.length = Array.getLength(paramObject);
    this.cls = localClass.getComponentType();
  }

  public boolean has(String paramString, Scriptable paramScriptable)
  {
    return ((paramString.equals("length")) || (super.has(paramString, paramScriptable)));
  }

  public boolean has(int paramInt, Scriptable paramScriptable)
  {
    return ((0 <= paramInt) && (paramInt < this.length));
  }

  public Object get(String paramString, Scriptable paramScriptable)
  {
    if (paramString.equals("length"))
      return new Integer(this.length);
    Object localObject = super.get(paramString, paramScriptable);
    if ((localObject == NOT_FOUND) && (!(ScriptableObject.hasProperty(getPrototype(), paramString))))
      throw Context.reportRuntimeError2("msg.java.member.not.found", this.array.getClass().getName(), paramString);
    return localObject;
  }

  public Object get(int paramInt, Scriptable paramScriptable)
  {
    if ((0 <= paramInt) && (paramInt < this.length))
    {
      Context localContext = Context.getContext();
      Object localObject = Array.get(this.array, paramInt);
      return localContext.getWrapFactory().wrap(localContext, this, localObject, this.cls);
    }
    return Undefined.instance;
  }

  public void put(String paramString, Scriptable paramScriptable, Object paramObject)
  {
    if (!(paramString.equals("length")))
      super.put(paramString, paramScriptable, paramObject);
  }

  public void put(int paramInt, Scriptable paramScriptable, Object paramObject)
  {
    if ((0 <= paramInt) && (paramInt < this.length))
    {
      Array.set(this.array, paramInt, Context.jsToJava(paramObject, this.cls));
      return;
    }
    super.put(paramInt, paramScriptable, paramObject);
  }

  public Object getDefaultValue(Class paramClass)
  {
    if ((paramClass == null) || (paramClass == ScriptRuntime.StringClass))
      return this.array.toString();
    if (paramClass == ScriptRuntime.BooleanClass)
      return Boolean.TRUE;
    if (paramClass == ScriptRuntime.NumberClass)
      return ScriptRuntime.NaNobj;
    return this;
  }

  public Object[] getIds()
  {
    Object[] arrayOfObject = new Object[this.length];
    int i = this.length;
    while (--i >= 0)
      arrayOfObject[i] = new Integer(i);
    return arrayOfObject;
  }

  public boolean hasInstance(Scriptable paramScriptable)
  {
    if (!(paramScriptable instanceof Wrapper))
      return false;
    Object localObject = ((Wrapper)paramScriptable).unwrap();
    return this.cls.isInstance(localObject);
  }

  public Scriptable getPrototype()
  {
    if (this.prototype == null)
      this.prototype = ScriptableObject.getClassPrototype(getParentScope(), "Array");
    return this.prototype;
  }
}