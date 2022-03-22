package sun.org.mozilla.javascript.internal;

import java.lang.reflect.Field;

class FieldAndMethods extends NativeJavaMethod
{
  static final long serialVersionUID = -9222428244284796755L;
  Field field;
  Object javaObject;

  FieldAndMethods(Scriptable paramScriptable, MemberBox[] paramArrayOfMemberBox, Field paramField)
  {
    super(paramArrayOfMemberBox);
    this.field = paramField;
    setParentScope(paramScriptable);
    setPrototype(ScriptableObject.getFunctionPrototype(paramScriptable));
  }

  public Object getDefaultValue(Class paramClass)
  {
    Class localClass;
    if (paramClass == ScriptRuntime.FunctionClass)
      return this;
    try
    {
      localObject = this.field.get(this.javaObject);
      localClass = this.field.getType();
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw Context.reportRuntimeError1("msg.java.internal.private", this.field.getName());
    }
    Context localContext = Context.getContext();
    Object localObject = localContext.getWrapFactory().wrap(localContext, this, localObject, localClass);
    if (localObject instanceof Scriptable)
      localObject = ((Scriptable)localObject).getDefaultValue(paramClass);
    return localObject;
  }
}