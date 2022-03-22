package sun.org.mozilla.javascript.internal;

public class WrapFactory
{
  private boolean javaPrimitiveWrap = true;

  public Object wrap(Context paramContext, Scriptable paramScriptable, Object paramObject, Class paramClass)
  {
    if ((paramObject == null) || (paramObject == Undefined.instance) || (paramObject instanceof Scriptable))
      return paramObject;
    if ((paramClass != null) && (paramClass.isPrimitive()))
    {
      if (paramClass == Void.TYPE)
        return Undefined.instance;
      if (paramClass == Character.TYPE)
        return new Integer(((Character)paramObject).charValue());
      return paramObject;
    }
    if (!(isJavaPrimitiveWrap()))
    {
      if ((paramObject instanceof String) || (paramObject instanceof Number) || (paramObject instanceof Boolean))
        return paramObject;
      if (paramObject instanceof Character)
        return String.valueOf(((Character)paramObject).charValue());
    }
    Class localClass = paramObject.getClass();
    if (localClass.isArray())
      return NativeJavaArray.wrap(paramScriptable, paramObject);
    return wrapAsJavaObject(paramContext, paramScriptable, paramObject, paramClass);
  }

  public Scriptable wrapNewObject(Context paramContext, Scriptable paramScriptable, Object paramObject)
  {
    if (paramObject instanceof Scriptable)
      return ((Scriptable)paramObject);
    Class localClass = paramObject.getClass();
    if (localClass.isArray())
      return NativeJavaArray.wrap(paramScriptable, paramObject);
    return wrapAsJavaObject(paramContext, paramScriptable, paramObject, null);
  }

  public Scriptable wrapAsJavaObject(Context paramContext, Scriptable paramScriptable, Object paramObject, Class paramClass)
  {
    NativeJavaObject localNativeJavaObject = new NativeJavaObject(paramScriptable, paramObject, paramClass);
    return localNativeJavaObject;
  }

  public final boolean isJavaPrimitiveWrap()
  {
    return this.javaPrimitiveWrap;
  }

  public final void setJavaPrimitiveWrap(boolean paramBoolean)
  {
    Context localContext = Context.getCurrentContext();
    if ((localContext != null) && (localContext.isSealed()))
      Context.onSealedMutation();
    this.javaPrimitiveWrap = paramBoolean;
  }
}