package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import sun.org.mozilla.javascript.internal.debug.DebuggableObject;

public abstract class ScriptableObject
  implements Scriptable, Serializable, DebuggableObject
{
  public static final int EMPTY = 0;
  public static final int READONLY = 1;
  public static final int DONTENUM = 2;
  public static final int PERMANENT = 4;
  private Scriptable prototypeObject;
  private Scriptable parentScopeObject;
  private static final Object HAS_STATIC_ACCESSORS = Void.TYPE;
  private static final Slot REMOVED = new Slot(null);
  private transient Slot[] slots;
  private int count;
  private transient Slot lastAccess = REMOVED;
  private volatile transient Hashtable associatedValues;

  static void checkValidAttributes(int paramInt)
  {
    if ((paramInt & 0xFFFFFFF8) != 0)
      throw new IllegalArgumentException(String.valueOf(paramInt));
  }

  public ScriptableObject()
  {
  }

  public ScriptableObject(Scriptable paramScriptable1, Scriptable paramScriptable2)
  {
    if (paramScriptable1 == null)
      throw new IllegalArgumentException();
    this.parentScopeObject = paramScriptable1;
    this.prototypeObject = paramScriptable2;
  }

  public abstract String getClassName();

  public boolean has(String paramString, Scriptable paramScriptable)
  {
    return (null != getNamedSlot(paramString));
  }

  public boolean has(int paramInt, Scriptable paramScriptable)
  {
    return (null != getSlot(null, paramInt));
  }

  public Object get(String paramString, Scriptable paramScriptable)
  {
    Slot localSlot = getNamedSlot(paramString);
    if (localSlot == null)
      return DebuggableObject.NOT_FOUND;
    if (localSlot instanceof GetterSlot)
    {
      GetterSlot localGetterSlot = (GetterSlot)localSlot;
      if (localGetterSlot.getter != null)
        return getByGetter(localGetterSlot, paramScriptable);
    }
    return localSlot.value;
  }

  public Object get(int paramInt, Scriptable paramScriptable)
  {
    Slot localSlot = getSlot(null, paramInt);
    if (localSlot == null)
      return DebuggableObject.NOT_FOUND;
    return localSlot.value;
  }

  public void put(String paramString, Scriptable paramScriptable, Object paramObject)
  {
    Slot localSlot = this.lastAccess;
    if ((paramString != localSlot.stringKey) || (localSlot.wasDeleted != 0))
    {
      int i = paramString.hashCode();
      localSlot = getSlot(paramString, i);
      if (localSlot == null)
      {
        if (paramScriptable != this)
        {
          paramScriptable.put(paramString, paramScriptable, paramObject);
          return;
        }
        localSlot = addSlot(paramString, i, null);
      }
    }
    if ((paramScriptable == this) && (isSealed()))
      throw Context.reportRuntimeError1("msg.modify.sealed", paramString);
    if ((localSlot.attributes & 0x1) != 0)
      return;
    if (localSlot instanceof GetterSlot)
    {
      GetterSlot localGetterSlot = (GetterSlot)localSlot;
      if (localGetterSlot.setter != null)
        setBySetter(localGetterSlot, paramScriptable, paramObject);
      return;
    }
    if (this == paramScriptable)
      localSlot.value = paramObject;
    else
      paramScriptable.put(paramString, paramScriptable, paramObject);
  }

  public void put(int paramInt, Scriptable paramScriptable, Object paramObject)
  {
    Slot localSlot = getSlot(null, paramInt);
    if (localSlot == null)
    {
      if (paramScriptable != this)
      {
        paramScriptable.put(paramInt, paramScriptable, paramObject);
        return;
      }
      localSlot = addSlot(null, paramInt, null);
    }
    if ((paramScriptable == this) && (isSealed()))
      throw Context.reportRuntimeError1("msg.modify.sealed", Integer.toString(paramInt));
    if ((localSlot.attributes & 0x1) != 0)
      return;
    if (this == paramScriptable)
      localSlot.value = paramObject;
    else
      paramScriptable.put(paramInt, paramScriptable, paramObject);
  }

  public void delete(String paramString)
  {
    removeSlot(paramString, paramString.hashCode());
  }

  public void delete(int paramInt)
  {
    removeSlot(null, paramInt);
  }

  /**
   * @deprecated
   */
  public final int getAttributes(String paramString, Scriptable paramScriptable)
  {
    return getAttributes(paramString);
  }

  /**
   * @deprecated
   */
  public final int getAttributes(int paramInt, Scriptable paramScriptable)
  {
    return getAttributes(paramInt);
  }

  /**
   * @deprecated
   */
  public final void setAttributes(String paramString, Scriptable paramScriptable, int paramInt)
  {
    setAttributes(paramString, paramInt);
  }

  /**
   * @deprecated
   */
  public void setAttributes(int paramInt1, Scriptable paramScriptable, int paramInt2)
  {
    setAttributes(paramInt1, paramInt2);
  }

  public int getAttributes(String paramString)
  {
    Slot localSlot = getNamedSlot(paramString);
    if (localSlot == null)
      throw Context.reportRuntimeError1("msg.prop.not.found", paramString);
    return localSlot.attributes;
  }

  public int getAttributes(int paramInt)
  {
    Slot localSlot = getSlot(null, paramInt);
    if (localSlot == null)
      throw Context.reportRuntimeError1("msg.prop.not.found", String.valueOf(paramInt));
    return localSlot.attributes;
  }

  public void setAttributes(String paramString, int paramInt)
  {
    checkValidAttributes(paramInt);
    Slot localSlot = getNamedSlot(paramString);
    if (localSlot == null)
      throw Context.reportRuntimeError1("msg.prop.not.found", paramString);
    localSlot.attributes = (short)paramInt;
  }

  public void setAttributes(int paramInt1, int paramInt2)
  {
    checkValidAttributes(paramInt2);
    Slot localSlot = getSlot(null, paramInt1);
    if (localSlot == null)
      throw Context.reportRuntimeError1("msg.prop.not.found", String.valueOf(paramInt1));
    localSlot.attributes = (short)paramInt2;
  }

  public Scriptable getPrototype()
  {
    return this.prototypeObject;
  }

  public void setPrototype(Scriptable paramScriptable)
  {
    this.prototypeObject = paramScriptable;
  }

  public Scriptable getParentScope()
  {
    return this.parentScopeObject;
  }

  public void setParentScope(Scriptable paramScriptable)
  {
    this.parentScopeObject = paramScriptable;
  }

  public Object[] getIds()
  {
    return getIds(false);
  }

  public Object[] getAllIds()
  {
    return getIds(true);
  }

  public Object getDefaultValue(Class paramClass)
  {
    Context localContext = null;
    for (int i = 0; i < 2; ++i)
    {
      int j;
      String str2;
      Object[] arrayOfObject;
      if (paramClass == ScriptRuntime.StringClass)
        j = (i == 0) ? 1 : 0;
      else
        j = (i == 1) ? 1 : 0;
      if (j != 0)
      {
        str2 = "toString";
        arrayOfObject = ScriptRuntime.emptyArgs;
      }
      else
      {
        str2 = "valueOf";
        arrayOfObject = new Object[1];
        if (paramClass == null)
          localObject1 = "undefined";
        else if (paramClass == ScriptRuntime.StringClass)
          localObject1 = "string";
        else if (paramClass == ScriptRuntime.ScriptableClass)
          localObject1 = "object";
        else if (paramClass == ScriptRuntime.FunctionClass)
          localObject1 = "function";
        else if ((paramClass == ScriptRuntime.BooleanClass) || (paramClass == Boolean.TYPE))
          localObject1 = "boolean";
        else if ((paramClass == ScriptRuntime.NumberClass) || (paramClass == ScriptRuntime.ByteClass) || (paramClass == Byte.TYPE) || (paramClass == ScriptRuntime.ShortClass) || (paramClass == Short.TYPE) || (paramClass == ScriptRuntime.IntegerClass) || (paramClass == Integer.TYPE) || (paramClass == ScriptRuntime.FloatClass) || (paramClass == Float.TYPE) || (paramClass == ScriptRuntime.DoubleClass) || (paramClass == Double.TYPE))
          localObject1 = "number";
        else
          throw Context.reportRuntimeError1("msg.invalid.type", paramClass.toString());
        arrayOfObject[0] = localObject1;
      }
      Object localObject1 = getProperty(this, str2);
      if (!(localObject1 instanceof Function))
        break label366:
      Function localFunction = (Function)localObject1;
      if (localContext == null)
        localContext = Context.getContext();
      localObject1 = localFunction.call(localContext, localFunction.getParentScope(), this, arrayOfObject);
      if (localObject1 != null)
      {
        if (!(localObject1 instanceof DebuggableObject))
          return localObject1;
        if ((paramClass == ScriptRuntime.ScriptableClass) || (paramClass == ScriptRuntime.FunctionClass))
          return localObject1;
        if ((j != 0) && (localObject1 instanceof Wrapper))
        {
          Object localObject2 = ((Wrapper)localObject1).unwrap();
          label366: if (localObject2 instanceof String)
            return localObject2;
        }
      }
    }
    String str1 = (paramClass == null) ? "undefined" : paramClass.getName();
    throw ScriptRuntime.typeError1("msg.default.value", str1);
  }

  public boolean hasInstance(Scriptable paramScriptable)
  {
    return ScriptRuntime.jsDelegatesTo(paramScriptable, this);
  }

  protected Object equivalentValues(Object paramObject)
  {
    return ((this == paramObject) ? Boolean.TRUE : DebuggableObject.NOT_FOUND);
  }

  public static void defineClass(Scriptable paramScriptable, Class paramClass)
    throws IllegalAccessException, InstantiationException, InvocationTargetException
  {
    defineClass(paramScriptable, paramClass, false, false);
  }

  public static void defineClass(Scriptable paramScriptable, Class paramClass, boolean paramBoolean)
    throws IllegalAccessException, InstantiationException, InvocationTargetException
  {
    defineClass(paramScriptable, paramClass, paramBoolean, false);
  }

  public static String defineClass(Scriptable paramScriptable, Class paramClass, boolean paramBoolean1, boolean paramBoolean2)
    throws IllegalAccessException, InstantiationException, InvocationTargetException
  {
    Method[] arrayOfMethod = FunctionObject.getMethodList(paramClass);
    for (int i = 0; i < arrayOfMethod.length; ++i)
    {
      localObject1 = arrayOfMethod[i];
      if (!(((Method)localObject1).getName().equals("init")))
        break label187:
      Class[] arrayOfClass = ((Method)localObject1).getParameterTypes();
      if ((arrayOfClass.length == 3) && (arrayOfClass[0] == ScriptRuntime.ContextClass) && (arrayOfClass[1] == ScriptRuntime.ScriptableClass) && (arrayOfClass[2] == Boolean.TYPE) && (Modifier.isStatic(((Method)localObject1).getModifiers())))
      {
        localObject2 = { Context.getContext(), paramScriptable, (paramBoolean1) ? Boolean.TRUE : Boolean.FALSE };
        ((Method)localObject1).invoke(null, localObject2);
        return null;
      }
      if ((arrayOfClass.length == 1) && (arrayOfClass[0] == ScriptRuntime.ScriptableClass) && (Modifier.isStatic(((Method)localObject1).getModifiers())))
      {
        localObject2 = { paramScriptable };
        ((Method)localObject1).invoke(null, localObject2);
        label187: return null;
      }
    }
    Constructor[] arrayOfConstructor = paramClass.getConstructors();
    Object localObject1 = null;
    for (int j = 0; j < arrayOfConstructor.length; ++j)
      if (arrayOfConstructor[j].getParameterTypes().length == 0)
      {
        localObject1 = arrayOfConstructor[j];
        break;
      }
    if (localObject1 == null)
      throw Context.reportRuntimeError1("msg.zero.arg.ctor", paramClass.getName());
    Scriptable localScriptable1 = (DebuggableObject)((Constructor)localObject1).newInstance(ScriptRuntime.emptyArgs);
    Object localObject2 = localScriptable1.getClassName();
    Scriptable localScriptable2 = null;
    if (paramBoolean2)
    {
      Class localClass = paramClass.getSuperclass();
      if (ScriptRuntime.ScriptableClass.isAssignableFrom(localClass))
      {
        String str1 = defineClass(paramScriptable, localClass, paramBoolean1, paramBoolean2);
        if (str1 != null)
          localScriptable2 = getClassPrototype(paramScriptable, str1);
      }
    }
    if (localScriptable2 == null)
      localScriptable2 = getObjectPrototype(paramScriptable);
    localScriptable1.setPrototype(localScriptable2);
    Object localObject3 = FunctionObject.findSingleMethod(arrayOfMethod, "jsConstructor");
    if (localObject3 == null)
    {
      if (arrayOfConstructor.length == 1)
        localObject3 = arrayOfConstructor[0];
      else if (arrayOfConstructor.length == 2)
        if (arrayOfConstructor[0].getParameterTypes().length == 0)
          localObject3 = arrayOfConstructor[1];
        else if (arrayOfConstructor[1].getParameterTypes().length == 0)
          localObject3 = arrayOfConstructor[0];
      if (localObject3 == null)
        throw Context.reportRuntimeError1("msg.ctor.multiple.parms", paramClass.getName());
    }
    FunctionObject localFunctionObject = new FunctionObject((String)localObject2, (Member)localObject3, paramScriptable);
    if (localFunctionObject.isVarArgsMethod())
      throw Context.reportRuntimeError1("msg.varargs.ctor", ((Member)localObject3).getName());
    localFunctionObject.addAsConstructor(paramScriptable, localScriptable1);
    Method localMethod = null;
    for (int k = 0; k < arrayOfMethod.length; ++k)
    {
      Object localObject4;
      if (arrayOfMethod[k] == localObject3)
        break label899:
      String str2 = arrayOfMethod[k].getName();
      if (str2.equals("finishInit"))
      {
        localObject4 = arrayOfMethod[k].getParameterTypes();
        label899: label707: if ((localObject4.length == 3) && (localObject4[0] == ScriptRuntime.ScriptableClass) && (localObject4[1] == FunctionObject.class) && (localObject4[2] == ScriptRuntime.ScriptableClass) && (Modifier.isStatic(arrayOfMethod[k].getModifiers())))
          localMethod = arrayOfMethod[k];
      }
      else
      {
        Object localObject5;
        if (str2.indexOf(36) != -1)
          break label899:
        if (str2.equals("jsConstructor"))
          break label899:
        localObject4 = null;
        if (str2.startsWith("jsFunction_"))
        {
          localObject4 = "jsFunction_";
        }
        else
        {
          if (str2.startsWith("jsStaticFunction_"))
          {
            localObject4 = "jsStaticFunction_";
            if (Modifier.isStatic(arrayOfMethod[k].getModifiers()))
              break label707;
            throw Context.reportRuntimeError("jsStaticFunction must be used with static method.");
          }
          if (str2.startsWith("jsGet_"))
          {
            localObject4 = "jsGet_";
          }
          else
          {
            if (!(str2.startsWith("jsSet_")))
              break label899;
            localObject4 = "jsSet_";
          }
        }
        str2 = str2.substring(((String)localObject4).length());
        if (localObject4 == "jsSet_")
          break label899:
        if (localObject4 == "jsGet_")
        {
          if (!(localScriptable1 instanceof ScriptableObject))
            throw Context.reportRuntimeError2("msg.extend.scriptable", localScriptable1.getClass().toString(), str2);
          localObject5 = FunctionObject.findSingleMethod(arrayOfMethod, "jsSet_" + str2);
          int l = 0x6 | ((localObject5 != null) ? 0 : 1);
          ((ScriptableObject)localScriptable1).defineProperty(str2, null, arrayOfMethod[k], (Method)localObject5, l);
        }
        else
        {
          localObject5 = new FunctionObject(str2, arrayOfMethod[k], localScriptable1);
          if (((FunctionObject)localObject5).isVarArgsConstructor())
            throw Context.reportRuntimeError1("msg.varargs.fun", ((Member)localObject3).getName());
          Scriptable localScriptable3 = (localObject4 == "jsStaticFunction_") ? localFunctionObject : localScriptable1;
          defineProperty(localScriptable3, str2, localObject5, 2);
          if (paramBoolean1)
            ((FunctionObject)localObject5).sealObject();
        }
      }
    }
    if (localMethod != null)
    {
      Object[] arrayOfObject = { paramScriptable, localFunctionObject, localScriptable1 };
      localMethod.invoke(null, arrayOfObject);
    }
    if (paramBoolean1)
    {
      localFunctionObject.sealObject();
      if (localScriptable1 instanceof ScriptableObject)
        ((ScriptableObject)localScriptable1).sealObject();
    }
    return ((String)(String)(String)(String)(String)localObject2);
  }

  public void defineProperty(String paramString, Object paramObject, int paramInt)
  {
    put(paramString, this, paramObject);
    setAttributes(paramString, paramInt);
  }

  public static void defineProperty(Scriptable paramScriptable, String paramString, Object paramObject, int paramInt)
  {
    if (!(paramScriptable instanceof ScriptableObject))
    {
      paramScriptable.put(paramString, paramScriptable, paramObject);
      return;
    }
    ScriptableObject localScriptableObject = (ScriptableObject)paramScriptable;
    localScriptableObject.defineProperty(paramString, paramObject, paramInt);
  }

  public void defineProperty(String paramString, Class paramClass, int paramInt)
  {
    int i = paramString.length();
    if (i == 0)
      throw new IllegalArgumentException();
    char[] arrayOfChar = new char[3 + i];
    paramString.getChars(0, i, arrayOfChar, 3);
    arrayOfChar[3] = Character.toUpperCase(arrayOfChar[3]);
    arrayOfChar[0] = 'g';
    arrayOfChar[1] = 'e';
    arrayOfChar[2] = 't';
    String str1 = new String(arrayOfChar);
    arrayOfChar[0] = 's';
    String str2 = new String(arrayOfChar);
    Method[] arrayOfMethod = FunctionObject.getMethodList(paramClass);
    Method localMethod1 = FunctionObject.findSingleMethod(arrayOfMethod, str1);
    Method localMethod2 = FunctionObject.findSingleMethod(arrayOfMethod, str2);
    if (localMethod2 == null)
      paramInt |= 1;
    defineProperty(paramString, null, localMethod1, (localMethod2 == null) ? null : localMethod2, paramInt);
  }

  public void defineProperty(String paramString, Object paramObject, Method paramMethod1, Method paramMethod2, int paramInt)
  {
    if ((paramObject == null) && (Modifier.isStatic(paramMethod1.getModifiers())))
      paramObject = HAS_STATIC_ACCESSORS;
    Class[] arrayOfClass = paramMethod1.getParameterTypes();
    if (arrayOfClass.length != 0)
    {
      if ((arrayOfClass.length == 1) && (arrayOfClass[0] == ScriptRuntime.ScriptableObjectClass))
        break label71;
      throw Context.reportRuntimeError1("msg.bad.getter.parms", paramMethod1.toString());
    }
    if (paramObject != null)
      throw Context.reportRuntimeError1("msg.obj.getter.parms", paramMethod1.toString());
    if (paramMethod2 != null)
    {
      if (((paramObject == HAS_STATIC_ACCESSORS) ? 1 : false) != Modifier.isStatic(paramMethod2.getModifiers()))
        label71: throw Context.reportRuntimeError0("msg.getter.static");
      arrayOfClass = paramMethod2.getParameterTypes();
      if (arrayOfClass.length == 2)
      {
        if (arrayOfClass[0] != ScriptRuntime.ScriptableObjectClass)
          throw Context.reportRuntimeError0("msg.setter2.parms");
        if (paramObject != null)
          break label178;
        throw Context.reportRuntimeError1("msg.setter1.parms", paramMethod2.toString());
      }
      if (arrayOfClass.length == 1)
      {
        if (paramObject == null)
          break label178;
        throw Context.reportRuntimeError1("msg.setter2.expected", paramMethod2.toString());
      }
      throw Context.reportRuntimeError0("msg.setter.parms");
      label178: localObject = arrayOfClass[(arrayOfClass.length - 1)];
      int i = FunctionObject.getTypeTag((Class)localObject);
      if (i == 0)
        throw Context.reportRuntimeError2("msg.setter2.expected", ((Class)localObject).getName(), paramMethod2.toString());
    }
    Object localObject = new GetterSlot(null);
    ((GetterSlot)localObject).delegateTo = paramObject;
    ((GetterSlot)localObject).getter = new MemberBox(paramMethod1);
    if (paramMethod2 != null)
      ((GetterSlot)localObject).setter = new MemberBox(paramMethod2);
    ((GetterSlot)localObject).attributes = (short)paramInt;
    Slot localSlot = addSlot(paramString, paramString.hashCode(), (Slot)localObject);
    if (localSlot != localObject)
      throw new RuntimeException("Property already exists");
  }

  public void defineFunctionProperties(String[] paramArrayOfString, Class paramClass, int paramInt)
  {
    Method[] arrayOfMethod = FunctionObject.getMethodList(paramClass);
    for (int i = 0; i < paramArrayOfString.length; ++i)
    {
      String str = paramArrayOfString[i];
      Method localMethod = FunctionObject.findSingleMethod(arrayOfMethod, str);
      if (localMethod == null)
        throw Context.reportRuntimeError2("msg.method.not.found", str, paramClass.getName());
      FunctionObject localFunctionObject = new FunctionObject(str, localMethod, this);
      defineProperty(str, localFunctionObject, paramInt);
    }
  }

  public static Scriptable getObjectPrototype(Scriptable paramScriptable)
  {
    return getClassPrototype(paramScriptable, "Object");
  }

  public static Scriptable getFunctionPrototype(Scriptable paramScriptable)
  {
    return getClassPrototype(paramScriptable, "Function");
  }

  public static Scriptable getClassPrototype(Scriptable paramScriptable, String paramString)
  {
    Object localObject2;
    paramScriptable = getTopLevelScope(paramScriptable);
    Object localObject1 = getProperty(paramScriptable, paramString);
    if (localObject1 instanceof BaseFunction)
    {
      localObject2 = ((BaseFunction)localObject1).getPrototypeProperty();
    }
    else if (localObject1 instanceof DebuggableObject)
    {
      Scriptable localScriptable = (DebuggableObject)localObject1;
      localObject2 = localScriptable.get("prototype", localScriptable);
    }
    else
    {
      return null;
    }
    if (localObject2 instanceof DebuggableObject)
      return ((DebuggableObject)localObject2);
    return null;
  }

  public static Scriptable getTopLevelScope(Scriptable paramScriptable)
  {
    while (true)
    {
      Scriptable localScriptable = paramScriptable.getParentScope();
      if (localScriptable == null)
        return paramScriptable;
      paramScriptable = localScriptable;
    }
  }

  public synchronized void sealObject()
  {
    if (this.count >= 0)
      this.count = (-1 - this.count);
  }

  public final boolean isSealed()
  {
    return (this.count < 0);
  }

  public static Object getProperty(Scriptable paramScriptable, String paramString)
  {
    Object localObject;
    Scriptable localScriptable = paramScriptable;
    do
    {
      localObject = paramScriptable.get(paramString, localScriptable);
      if (localObject != DebuggableObject.NOT_FOUND)
        break;
      paramScriptable = paramScriptable.getPrototype();
    }
    while (paramScriptable != null);
    return localObject;
  }

  public static Object getProperty(Scriptable paramScriptable, int paramInt)
  {
    Object localObject;
    Scriptable localScriptable = paramScriptable;
    do
    {
      localObject = paramScriptable.get(paramInt, localScriptable);
      if (localObject != DebuggableObject.NOT_FOUND)
        break;
      paramScriptable = paramScriptable.getPrototype();
    }
    while (paramScriptable != null);
    return localObject;
  }

  public static boolean hasProperty(Scriptable paramScriptable, String paramString)
  {
    return (null != getBase(paramScriptable, paramString));
  }

  public static boolean hasProperty(Scriptable paramScriptable, int paramInt)
  {
    return (null != getBase(paramScriptable, paramInt));
  }

  public static void putProperty(Scriptable paramScriptable, String paramString, Object paramObject)
  {
    Scriptable localScriptable = getBase(paramScriptable, paramString);
    if (localScriptable == null)
      localScriptable = paramScriptable;
    localScriptable.put(paramString, paramScriptable, paramObject);
  }

  public static void putProperty(Scriptable paramScriptable, int paramInt, Object paramObject)
  {
    Scriptable localScriptable = getBase(paramScriptable, paramInt);
    if (localScriptable == null)
      localScriptable = paramScriptable;
    localScriptable.put(paramInt, paramScriptable, paramObject);
  }

  public static boolean deleteProperty(Scriptable paramScriptable, String paramString)
  {
    Scriptable localScriptable = getBase(paramScriptable, paramString);
    if (localScriptable == null)
      return true;
    localScriptable.delete(paramString);
    return (!(localScriptable.has(paramString, paramScriptable)));
  }

  public static boolean deleteProperty(Scriptable paramScriptable, int paramInt)
  {
    Scriptable localScriptable = getBase(paramScriptable, paramInt);
    if (localScriptable == null)
      return true;
    localScriptable.delete(paramInt);
    return (!(localScriptable.has(paramInt, paramScriptable)));
  }

  public static Object[] getPropertyIds(Scriptable paramScriptable)
  {
    if (paramScriptable == null)
      return ScriptRuntime.emptyArgs;
    Object localObject = paramScriptable.getIds();
    ObjToIntMap localObjToIntMap = null;
    while (true)
    {
      Object[] arrayOfObject;
      while (true)
      {
        while (true)
        {
          paramScriptable = paramScriptable.getPrototype();
          if (paramScriptable == null)
            break label128:
          arrayOfObject = paramScriptable.getIds();
          if (arrayOfObject.length != 0)
            break;
        }
        if (localObjToIntMap != null)
          break label100;
        if (localObject.length != 0)
          break;
        localObject = arrayOfObject;
      }
      localObjToIntMap = new ObjToIntMap(localObject.length + arrayOfObject.length);
      for (int i = 0; i != localObject.length; ++i)
        localObjToIntMap.intern(localObject[i]);
      localObject = null;
      for (i = 0; i != arrayOfObject.length; ++i)
        label100: localObjToIntMap.intern(arrayOfObject[i]);
    }
    if (localObjToIntMap != null)
      label128: localObject = localObjToIntMap.getKeys();
    return ((Object)localObject);
  }

  public static Object callMethod(Scriptable paramScriptable, String paramString, Object[] paramArrayOfObject)
  {
    return callMethod(null, paramScriptable, paramString, paramArrayOfObject);
  }

  public static Object callMethod(Context paramContext, Scriptable paramScriptable, String paramString, Object[] paramArrayOfObject)
  {
    Object localObject = getProperty(paramScriptable, paramString);
    if (!(localObject instanceof Function))
      throw ScriptRuntime.notFunctionError(paramScriptable, paramString);
    Function localFunction = (Function)localObject;
    Scriptable localScriptable = getTopLevelScope(paramScriptable);
    if (paramContext != null)
      return localFunction.call(paramContext, localScriptable, paramScriptable, paramArrayOfObject);
    return Context.call(null, localFunction, localScriptable, paramScriptable, paramArrayOfObject);
  }

  private static Scriptable getBase(Scriptable paramScriptable, String paramString)
  {
    do
    {
      if (paramScriptable.has(paramString, paramScriptable))
        break;
      paramScriptable = paramScriptable.getPrototype();
    }
    while (paramScriptable != null);
    return paramScriptable;
  }

  private static Scriptable getBase(Scriptable paramScriptable, int paramInt)
  {
    do
    {
      if (paramScriptable.has(paramInt, paramScriptable))
        break;
      paramScriptable = paramScriptable.getPrototype();
    }
    while (paramScriptable != null);
    return paramScriptable;
  }

  public final Object getAssociatedValue(Object paramObject)
  {
    Hashtable localHashtable = this.associatedValues;
    if (localHashtable == null)
      return null;
    return localHashtable.get(paramObject);
  }

  public static Object getTopScopeValue(Scriptable paramScriptable, Object paramObject)
  {
    paramScriptable = getTopLevelScope(paramScriptable);
    do
    {
      if (paramScriptable instanceof ScriptableObject)
      {
        ScriptableObject localScriptableObject = (ScriptableObject)paramScriptable;
        Object localObject = localScriptableObject.getAssociatedValue(paramObject);
        if (localObject != null)
          return localObject;
      }
      paramScriptable = paramScriptable.getPrototype();
    }
    while (paramScriptable != null);
    return null;
  }

  public final Object associateValue(Object paramObject1, Object paramObject2)
  {
    if (paramObject2 == null)
      throw new IllegalArgumentException();
    Hashtable localHashtable = this.associatedValues;
    if (localHashtable == null)
      synchronized (this)
      {
        localHashtable = this.associatedValues;
        if (localHashtable == null)
        {
          localHashtable = new Hashtable();
          this.associatedValues = localHashtable;
        }
      }
    return Kit.initHash(localHashtable, paramObject1, paramObject2);
  }

  private Object getByGetter(GetterSlot paramGetterSlot, Scriptable paramScriptable)
  {
    label54: Object localObject;
    Object[] arrayOfObject;
    if (paramGetterSlot.delegateTo == null)
    {
      if (paramScriptable != this)
      {
        Class localClass = paramGetterSlot.getter.getDeclaringClass();
        do
        {
          if (localClass.isInstance(paramScriptable))
            break label54;
          paramScriptable = paramScriptable.getPrototype();
          if (paramScriptable == this)
            break label54:
        }
        while (paramScriptable != null);
        paramScriptable = this;
      }
      localObject = paramScriptable;
      arrayOfObject = ScriptRuntime.emptyArgs;
    }
    else
    {
      localObject = paramGetterSlot.delegateTo;
      arrayOfObject = { this };
    }
    return paramGetterSlot.getter.invoke(localObject, arrayOfObject);
  }

  private void setBySetter(GetterSlot paramGetterSlot, Scriptable paramScriptable, Object paramObject)
  {
    Object localObject1;
    Object[] arrayOfObject;
    if ((paramScriptable != this) && (((paramGetterSlot.delegateTo != null) || (!(paramGetterSlot.setter.getDeclaringClass().isInstance(paramScriptable))))))
    {
      paramScriptable.put(paramGetterSlot.stringKey, paramScriptable, paramObject);
      return;
    }
    Context localContext = Context.getContext();
    Class[] arrayOfClass = paramGetterSlot.setter.argTypes;
    Class localClass = arrayOfClass[(arrayOfClass.length - 1)];
    int i = FunctionObject.getTypeTag(localClass);
    Object localObject3 = FunctionObject.convertArg(localContext, paramScriptable, paramObject, i);
    if (paramGetterSlot.delegateTo == null)
    {
      localObject1 = paramScriptable;
      arrayOfObject = { localObject3 };
    }
    else
    {
      if (paramScriptable != this)
        Kit.codeBug();
      localObject1 = paramGetterSlot.delegateTo;
      arrayOfObject = { this, localObject3 };
    }
    if (((ScriptableObject)paramScriptable).isSealed())
      throw Context.reportRuntimeError1("msg.modify.sealed", paramGetterSlot.stringKey);
    Object localObject2 = paramGetterSlot.setter.invoke(localObject1, arrayOfObject);
    if (paramGetterSlot.setter.method().getReturnType() != Void.TYPE)
    {
      Slot localSlot = new Slot(null);
      localSlot.intKey = paramGetterSlot.intKey;
      localSlot.stringKey = paramGetterSlot.stringKey;
      localSlot.attributes = paramGetterSlot.attributes;
      localSlot.value = localObject2;
      synchronized (this)
      {
        int j = getSlotPosition(this.slots, paramGetterSlot.stringKey, paramGetterSlot.intKey);
        if ((j >= 0) && (this.slots[j] == paramGetterSlot))
        {
          this.slots[j] = localSlot;
          this.lastAccess = localSlot;
        }
      }
    }
  }

  private Slot getNamedSlot(String paramString)
  {
    Slot localSlot = this.lastAccess;
    if ((paramString == localSlot.stringKey) && (localSlot.wasDeleted == 0))
      return localSlot;
    int i = paramString.hashCode();
    Slot[] arrayOfSlot = this.slots;
    int j = getSlotPosition(arrayOfSlot, paramString, i);
    if (j < 0)
      return null;
    localSlot = arrayOfSlot[j];
    localSlot.stringKey = paramString;
    this.lastAccess = localSlot;
    return localSlot;
  }

  private Slot getSlot(String paramString, int paramInt)
  {
    Slot[] arrayOfSlot = this.slots;
    int i = getSlotPosition(arrayOfSlot, paramString, paramInt);
    return ((i < 0) ? null : arrayOfSlot[i]);
  }

  private static int getSlotPosition(Slot[] paramArrayOfSlot, String paramString, int paramInt)
  {
    if (paramArrayOfSlot != null)
    {
      int i = (paramInt & 0x7FFFFFFF) % paramArrayOfSlot.length;
      int j = i;
      do
      {
        Slot localSlot = paramArrayOfSlot[j];
        if (localSlot == null)
          break;
        if ((localSlot != REMOVED) && (localSlot.intKey == paramInt) && (((localSlot.stringKey == paramString) || ((paramString != null) && (paramString.equals(localSlot.stringKey))))))
          return j;
        if (++j == paramArrayOfSlot.length)
          j = 0;
      }
      while (j != i);
    }
    return -1;
  }

  private synchronized Slot addSlot(String paramString, int paramInt, Slot paramSlot)
  {
    if (isSealed())
    {
      String str = (paramString != null) ? paramString : Integer.toString(paramInt);
      throw Context.reportRuntimeError1("msg.add.sealed", str);
    }
    if (this.slots == null)
      this.slots = new Slot[5];
    return addSlotImpl(paramString, paramInt, paramSlot);
  }

  private Slot addSlotImpl(String paramString, int paramInt, Slot paramSlot)
  {
    int i = (paramInt & 0x7FFFFFFF) % this.slots.length;
    int j = i;
    while (true)
    {
      Slot localSlot = this.slots[j];
      if ((localSlot == null) || (localSlot == REMOVED))
      {
        if (4 * (this.count + 1) > 3 * this.slots.length)
        {
          grow();
          return addSlotImpl(paramString, paramInt, paramSlot);
        }
        localSlot = (paramSlot == null) ? new Slot(null) : paramSlot;
        localSlot.stringKey = paramString;
        localSlot.intKey = paramInt;
        this.slots[j] = localSlot;
        this.count += 1;
        return localSlot;
      }
      if ((localSlot.intKey == paramInt) && (((localSlot.stringKey == paramString) || ((paramString != null) && (paramString.equals(localSlot.stringKey))))))
        return localSlot;
      if (++j == this.slots.length)
        j = 0;
      if (j == i)
        throw new IllegalStateException();
    }
  }

  private synchronized void removeSlot(String paramString, int paramInt)
  {
    if (isSealed())
    {
      String str = (paramString != null) ? paramString : Integer.toString(paramInt);
      throw Context.reportRuntimeError1("msg.remove.sealed", str);
    }
    int i = getSlotPosition(this.slots, paramString, paramInt);
    if (i >= 0)
    {
      Slot localSlot = this.slots[i];
      if ((localSlot.attributes & 0x4) == 0)
      {
        localSlot.wasDeleted = 1;
        if (localSlot == this.lastAccess)
          this.lastAccess = REMOVED;
        this.count -= 1;
        if (this.count != 0)
          this.slots[i] = REMOVED;
        else
          this.slots[i] = null;
      }
    }
  }

  private void grow()
  {
    Slot[] arrayOfSlot = new Slot[this.slots.length * 2 + 1];
    for (int i = this.slots.length - 1; i >= 0; --i)
    {
      Slot localSlot = this.slots[i];
      if (localSlot != null)
      {
        if (localSlot == REMOVED)
          break label86:
        int j = (localSlot.intKey & 0x7FFFFFFF) % arrayOfSlot.length;
        while (true)
        {
          do
            if (arrayOfSlot[j] == null)
              break label81;
          while (++j != arrayOfSlot.length);
          j = 0;
        }
        label81: label86: arrayOfSlot[j] = localSlot;
      }
    }
    this.slots = arrayOfSlot;
  }

  Object[] getIds(boolean paramBoolean)
  {
    Slot[] arrayOfSlot = this.slots;
    Object[] arrayOfObject1 = ScriptRuntime.emptyArgs;
    if (arrayOfSlot == null)
      return arrayOfObject1;
    int i = 0;
    for (int j = 0; j < arrayOfSlot.length; ++j)
    {
      Slot localSlot = arrayOfSlot[j];
      if (localSlot != null)
      {
        if (localSlot == REMOVED)
          break label113:
        if ((paramBoolean) || ((localSlot.attributes & 0x2) == 0))
        {
          if (i == 0)
            arrayOfObject1 = new Object[arrayOfSlot.length - j];
          label113: arrayOfObject1[(i++)] = new Integer(localSlot.intKey);
        }
      }
    }
    if (i == arrayOfObject1.length)
      return arrayOfObject1;
    Object[] arrayOfObject2 = new Object[i];
    System.arraycopy(arrayOfObject1, 0, arrayOfObject2, 0, i);
    return arrayOfObject2;
  }

  private synchronized void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    paramObjectOutputStream.defaultWriteObject();
    int i = this.count;
    if (i < 0)
      i = -1 - this.count;
    Slot[] arrayOfSlot = this.slots;
    if (arrayOfSlot == null)
    {
      if (i != 0)
        Kit.codeBug();
      paramObjectOutputStream.writeInt(0);
    }
    else
    {
      paramObjectOutputStream.writeInt(arrayOfSlot.length);
      for (int j = 0; i != 0; ++j)
      {
        Slot localSlot = arrayOfSlot[j];
        if ((localSlot != null) && (localSlot != REMOVED))
        {
          --i;
          paramObjectOutputStream.writeObject(localSlot);
        }
      }
    }
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    this.lastAccess = REMOVED;
    int i = paramObjectInputStream.readInt();
    if (i != 0)
    {
      this.slots = new Slot[i];
      int j = this.count;
      int k = 0;
      if (j < 0)
      {
        j = -1 - j;
        k = 1;
      }
      this.count = 0;
      for (int l = 0; l != j; ++l)
      {
        Slot localSlot = (Slot)paramObjectInputStream.readObject();
        addSlotImpl(localSlot.stringKey, localSlot.intKey, localSlot);
      }
      if (k != 0)
        this.count = (-1 - this.count);
    }
  }

  private static final class GetterSlot extends ScriptableObject.Slot
  {
    static final long serialVersionUID = -4900574849788797588L;
    Object delegateTo;
    MemberBox getter;
    MemberBox setter;

    private GetterSlot()
    {
      super(null);
    }
  }

  private static class Slot
  implements Serializable
  {
    static final long serialVersionUID = -3539051633409902634L;
    int intKey;
    String stringKey;
    Object value;
    short attributes;
    transient byte wasDeleted;

    private void readObject(ObjectInputStream paramObjectInputStream)
      throws IOException, ClassNotFoundException
    {
      paramObjectInputStream.defaultReadObject();
      if (this.stringKey != null)
        this.intKey = this.stringKey.hashCode();
    }
  }
}