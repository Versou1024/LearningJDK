package sun.org.mozilla.javascript.internal;

import java.lang.reflect.Method;

public class InterfaceAdapter
{
  private final Object proxyHelper;

  static Object create(Context paramContext, Class paramClass, Callable paramCallable)
  {
    Object localObject3;
    if (!(paramClass.isInterface()))
      throw new IllegalArgumentException();
    Method[] arrayOfMethod = paramClass.getMethods();
    if (arrayOfMethod.length == 0)
      throw Context.reportRuntimeError2("msg.no.empty.interface.conversion", String.valueOf(paramCallable), paramClass.getName());
    int i = 0;
    Object localObject1 = arrayOfMethod[0].getParameterTypes();
    for (int j = 1; j != arrayOfMethod.length; ++j)
    {
      localObject3 = arrayOfMethod[j].getParameterTypes();
      if (localObject3.length != localObject1.length)
        break label142:
      for (int k = 0; k != localObject1.length; ++k)
        if (localObject3[k] != localObject1[k])
          break label142:
    }
    i = 1;
    if (i == 0)
      throw Context.reportRuntimeError2("msg.no.function.interface.conversion", String.valueOf(paramCallable), paramClass.getName());
    label142: localObject1 = ScriptableObject.getTopLevelScope((Scriptable)paramCallable);
    Object localObject2 = ScriptableObject.getProperty((Scriptable)localObject1, "JavaAdapter");
    if (localObject2 != Scriptable.NOT_FOUND)
    {
      localObject3 = (Function)localObject2;
      Scriptable localScriptable = ScriptRuntime.newObject(paramContext, (Scriptable)localObject1, "Object", new Object[0]);
      for (int l = 0; l < arrayOfMethod.length; ++l)
      {
        localObject4 = arrayOfMethod[l].getName();
        ScriptableObject.putProperty(localScriptable, (String)localObject4, paramCallable);
      }
      Object[] arrayOfObject = { paramClass, localScriptable };
      Object localObject4 = ((Function)localObject3).construct(paramContext, (Scriptable)localObject1, arrayOfObject);
      if (localObject4 instanceof Wrapper)
        localObject4 = ((Wrapper)localObject4).unwrap();
      return localObject4;
    }
    throw Context.reportRuntimeError2("msg.conversion.not.allowed", String.valueOf(paramCallable), paramClass.getName());
  }

  private InterfaceAdapter(ContextFactory paramContextFactory, Class paramClass)
  {
    this.proxyHelper = VMBridge.instance.getInterfaceProxyHelper(paramContextFactory, new Class[] { paramClass });
  }

  public Object invoke(ContextFactory paramContextFactory, Object paramObject, Scriptable paramScriptable, Method paramMethod, Object[] paramArrayOfObject)
  {
    1 local1 = new ContextAction(this, paramObject, paramScriptable, paramMethod, paramArrayOfObject)
    {
      public Object run()
      {
        return this.this$0.invokeImpl(paramContext, this.val$target, this.val$topScope, this.val$method, this.val$args);
      }
    };
    return paramContextFactory.call(local1);
  }

  Object invokeImpl(Context paramContext, Object paramObject, Scriptable paramScriptable, Method paramMethod, Object[] paramArrayOfObject)
  {
    int i = (paramArrayOfObject == null) ? 0 : paramArrayOfObject.length;
    String str = paramMethod.getName();
    Callable localCallable = (Callable)paramObject;
    Scriptable localScriptable = paramScriptable;
    Object[] arrayOfObject = new Object[i + 1];
    arrayOfObject[i] = paramMethod.getName();
    if (i != 0)
    {
      localObject = paramContext.getWrapFactory();
      for (int j = 0; j != i; ++j)
        arrayOfObject[j] = ((WrapFactory)localObject).wrap(paramContext, paramScriptable, paramArrayOfObject[j], null);
    }
    Object localObject = localCallable.call(paramContext, paramScriptable, localScriptable, arrayOfObject);
    Class localClass = paramMethod.getReturnType();
    if (localClass == Void.TYPE)
      localObject = null;
    else
      localObject = Context.jsToJava(localObject, localClass);
    return localObject;
  }
}