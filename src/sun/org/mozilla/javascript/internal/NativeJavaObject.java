package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Hashtable;

public class NativeJavaObject
  implements Scriptable, Wrapper, Serializable
{
  static final long serialVersionUID = -6948590651130498591L;
  private static final int JSTYPE_UNDEFINED = 0;
  private static final int JSTYPE_NULL = 1;
  private static final int JSTYPE_BOOLEAN = 2;
  private static final int JSTYPE_NUMBER = 3;
  private static final int JSTYPE_STRING = 4;
  private static final int JSTYPE_JAVA_CLASS = 5;
  private static final int JSTYPE_JAVA_OBJECT = 6;
  private static final int JSTYPE_JAVA_ARRAY = 7;
  private static final int JSTYPE_OBJECT = 8;
  static final byte CONVERSION_TRIVIAL = 1;
  static final byte CONVERSION_NONTRIVIAL = 0;
  static final byte CONVERSION_NONE = 99;
  protected Scriptable prototype;
  protected Scriptable parent;
  protected transient Object javaObject;
  protected transient Class staticType;
  protected transient JavaMembers members;
  private transient Hashtable fieldAndMethods;
  private static final Object COERCED_INTERFACE_KEY = new Object();
  private static Method adapter_writeAdapterObject;
  private static Method adapter_readAdapterObject;

  public NativeJavaObject()
  {
  }

  public NativeJavaObject(Scriptable paramScriptable, Object paramObject, Class paramClass)
  {
    this.parent = paramScriptable;
    this.javaObject = paramObject;
    this.staticType = paramClass;
    initMembers();
  }

  protected void initMembers()
  {
    Class localClass;
    if (this.javaObject != null)
      localClass = this.javaObject.getClass();
    else
      localClass = this.staticType;
    this.members = JavaMembers.lookupClass(this.parent, localClass, this.staticType);
    this.fieldAndMethods = this.members.getFieldAndMethodsObjects(this, this.javaObject, false);
  }

  public boolean has(String paramString, Scriptable paramScriptable)
  {
    return this.members.has(paramString, false);
  }

  public boolean has(int paramInt, Scriptable paramScriptable)
  {
    return false;
  }

  public Object get(String paramString, Scriptable paramScriptable)
  {
    if (this.fieldAndMethods != null)
    {
      Object localObject = this.fieldAndMethods.get(paramString);
      if (localObject != null)
        return localObject;
    }
    return this.members.get(this, paramString, this.javaObject, false);
  }

  public Object get(int paramInt, Scriptable paramScriptable)
  {
    throw this.members.reportMemberNotFound(Integer.toString(paramInt));
  }

  public void put(String paramString, Scriptable paramScriptable, Object paramObject)
  {
    if ((this.prototype == null) || (this.members.has(paramString, false)))
      this.members.put(this, paramString, this.javaObject, paramObject, false);
    else
      this.prototype.put(paramString, this.prototype, paramObject);
  }

  public void put(int paramInt, Scriptable paramScriptable, Object paramObject)
  {
    throw this.members.reportMemberNotFound(Integer.toString(paramInt));
  }

  public boolean hasInstance(Scriptable paramScriptable)
  {
    return false;
  }

  public void delete(String paramString)
  {
  }

  public void delete(int paramInt)
  {
  }

  public Scriptable getPrototype()
  {
    if ((this.prototype == null) && (this.javaObject instanceof String))
      return ScriptableObject.getClassPrototype(this.parent, "String");
    return this.prototype;
  }

  public void setPrototype(Scriptable paramScriptable)
  {
    this.prototype = paramScriptable;
  }

  public Scriptable getParentScope()
  {
    return this.parent;
  }

  public void setParentScope(Scriptable paramScriptable)
  {
    this.parent = paramScriptable;
  }

  public Object[] getIds()
  {
    return this.members.getIds(false);
  }

  /**
   * @deprecated
   */
  public static Object wrap(Scriptable paramScriptable, Object paramObject, Class paramClass)
  {
    Context localContext = Context.getContext();
    return localContext.getWrapFactory().wrap(localContext, paramScriptable, paramObject, paramClass);
  }

  public Object unwrap()
  {
    return this.javaObject;
  }

  public String getClassName()
  {
    return "JavaObject";
  }

  public Object getDefaultValue(Class paramClass)
  {
    Object localObject1;
    if ((paramClass == null) && (this.javaObject instanceof Boolean))
      paramClass = ScriptRuntime.BooleanClass;
    if ((paramClass == null) || (paramClass == ScriptRuntime.StringClass))
    {
      localObject1 = this.javaObject.toString();
    }
    else
    {
      String str;
      if (paramClass == ScriptRuntime.BooleanClass)
        str = "booleanValue";
      else if (paramClass == ScriptRuntime.NumberClass)
        str = "doubleValue";
      else
        throw Context.reportRuntimeError0("msg.default.value");
      Object localObject2 = get(str, this);
      if (localObject2 instanceof Function)
      {
        Function localFunction = (Function)localObject2;
        localObject1 = localFunction.call(Context.getContext(), localFunction.getParentScope(), this, ScriptRuntime.emptyArgs);
      }
      else if ((paramClass == ScriptRuntime.NumberClass) && (this.javaObject instanceof Boolean))
      {
        boolean bool = ((Boolean)this.javaObject).booleanValue();
        localObject1 = ScriptRuntime.wrapNumber((bool) ? 1D : 0D);
      }
      else
      {
        localObject1 = this.javaObject.toString();
      }
    }
    return localObject1;
  }

  public static boolean canConvert(Object paramObject, Class paramClass)
  {
    int i = getConversionWeight(paramObject, paramClass);
    return (i < 99);
  }

  static int getConversionWeight(Object paramObject, Class paramClass)
  {
    int i = getJSTypeCode(paramObject);
    switch (i)
    {
    case 0:
      if ((paramClass != ScriptRuntime.StringClass) && (paramClass != ScriptRuntime.ObjectClass))
        break label428;
      return 1;
    case 1:
      if (paramClass.isPrimitive())
        break label428;
      return 1;
    case 2:
      if (paramClass == Boolean.TYPE)
        return 1;
      if (paramClass == ScriptRuntime.BooleanClass)
        return 2;
      if (paramClass == ScriptRuntime.ObjectClass)
        return 3;
      if (paramClass != ScriptRuntime.StringClass)
        break label428;
      return 4;
    case 3:
      if (paramClass.isPrimitive())
      {
        if (paramClass == Double.TYPE)
          return 1;
        if (paramClass == Boolean.TYPE)
          break label428;
        return (1 + getSizeRank(paramClass));
      }
      if (paramClass == ScriptRuntime.StringClass)
        return 9;
      if (paramClass == ScriptRuntime.ObjectClass)
        return 10;
      if (!(ScriptRuntime.NumberClass.isAssignableFrom(paramClass)))
        break label428;
      return 2;
    case 4:
      if (paramClass == ScriptRuntime.StringClass)
        return 1;
      if (paramClass.isInstance(paramObject))
        return 2;
      if (!(paramClass.isPrimitive()))
        break label428;
      if (paramClass == Character.TYPE)
        return 3;
      if (paramClass == Boolean.TYPE)
        break label428;
      return 4;
    case 5:
      if (paramClass == ScriptRuntime.ClassClass)
        return 1;
      if (paramClass == ScriptRuntime.ObjectClass)
        return 3;
      if (paramClass != ScriptRuntime.StringClass)
        break label428;
      return 4;
    case 6:
    case 7:
      Object localObject = paramObject;
      if (localObject instanceof Wrapper)
        localObject = ((Wrapper)localObject).unwrap();
      if (paramClass.isInstance(localObject))
        return 0;
      if (paramClass == ScriptRuntime.StringClass)
        return 2;
      if ((!(paramClass.isPrimitive())) || (paramClass == Boolean.TYPE))
        break label428;
      return ((i == 7) ? 0 : 2 + getSizeRank(paramClass));
    case 8:
      if (paramClass == paramObject.getClass())
        return 1;
      if (paramClass.isArray())
      {
        if (!(paramObject instanceof NativeArray))
          break label428;
        return 1;
      }
      if (paramClass == ScriptRuntime.ObjectClass)
        return 2;
      if (paramClass == ScriptRuntime.StringClass)
        return 3;
      if (paramClass == ScriptRuntime.DateClass)
      {
        if (!(paramObject instanceof NativeDate))
          break label428;
        return 1;
      }
      if (paramClass.isInterface())
      {
        if ((paramObject instanceof Function) && (paramClass.getMethods().length == 1))
          return 1;
        return 11;
      }
      if ((!(paramClass.isPrimitive())) || (paramClass == Boolean.TYPE))
        break label428;
      return (3 + getSizeRank(paramClass));
    }
    label428: return 99;
  }

  static int getSizeRank(Class paramClass)
  {
    if (paramClass == Double.TYPE)
      return 1;
    if (paramClass == Float.TYPE)
      return 2;
    if (paramClass == Long.TYPE)
      return 3;
    if (paramClass == Integer.TYPE)
      return 4;
    if (paramClass == Short.TYPE)
      return 5;
    if (paramClass == Character.TYPE)
      return 6;
    if (paramClass == Byte.TYPE)
      return 7;
    if (paramClass == Boolean.TYPE)
      return 99;
    return 8;
  }

  private static int getJSTypeCode(Object paramObject)
  {
    if (paramObject == null)
      return 1;
    if (paramObject == Undefined.instance)
      return 0;
    if (paramObject instanceof String)
      return 4;
    if (paramObject instanceof Number)
      return 3;
    if (paramObject instanceof Boolean)
      return 2;
    if (paramObject instanceof Serializable)
    {
      if (paramObject instanceof NativeJavaClass)
        return 5;
      if (paramObject instanceof NativeJavaArray)
        return 7;
      if (paramObject instanceof Wrapper)
        return 6;
      return 8;
    }
    if (paramObject instanceof Class)
      return 5;
    Class localClass = paramObject.getClass();
    if (localClass.isArray())
      return 7;
    return 6;
  }

  /**
   * @deprecated
   */
  public static Object coerceType(Class paramClass, Object paramObject)
  {
    return coerceTypeImpl(paramClass, paramObject);
  }

  static Object coerceTypeImpl(Class paramClass, Object paramObject)
  {
    if ((paramObject != null) && (paramObject.getClass() == paramClass))
      return paramObject;
    switch (getJSTypeCode(paramObject))
    {
    case 1:
      if (paramClass.isPrimitive())
        reportConversionError(paramObject, paramClass);
      return null;
    case 0:
      if ((paramClass == ScriptRuntime.StringClass) || (paramClass == ScriptRuntime.ObjectClass))
        return "undefined";
      reportConversionError("undefined", paramClass);
      break;
    case 2:
      if ((paramClass == Boolean.TYPE) || (paramClass == ScriptRuntime.BooleanClass) || (paramClass == ScriptRuntime.ObjectClass))
        return paramObject;
      if (paramClass == ScriptRuntime.StringClass)
        return paramObject.toString();
      reportConversionError(paramObject, paramClass);
      break;
    case 3:
      if (paramClass == ScriptRuntime.StringClass)
        return ScriptRuntime.toString(paramObject);
      if (paramClass == ScriptRuntime.ObjectClass)
        return coerceToNumber(Double.TYPE, paramObject);
      if (((paramClass.isPrimitive()) && (paramClass != Boolean.TYPE)) || (ScriptRuntime.NumberClass.isAssignableFrom(paramClass)))
        return coerceToNumber(paramClass, paramObject);
      reportConversionError(paramObject, paramClass);
      break;
    case 4:
      if ((paramClass == ScriptRuntime.StringClass) || (paramClass.isInstance(paramObject)))
        return paramObject;
      if ((paramClass == Character.TYPE) || (paramClass == ScriptRuntime.CharacterClass))
      {
        if (((String)paramObject).length() == 1)
          return new Character(((String)paramObject).charAt(0));
        return coerceToNumber(paramClass, paramObject);
      }
      if (((paramClass.isPrimitive()) && (paramClass != Boolean.TYPE)) || (ScriptRuntime.NumberClass.isAssignableFrom(paramClass)))
        return coerceToNumber(paramClass, paramObject);
      reportConversionError(paramObject, paramClass);
      break;
    case 5:
      if (paramObject instanceof Wrapper)
        paramObject = ((Wrapper)paramObject).unwrap();
      if ((paramClass == ScriptRuntime.ClassClass) || (paramClass == ScriptRuntime.ObjectClass))
        return paramObject;
      if (paramClass == ScriptRuntime.StringClass)
        return paramObject.toString();
      reportConversionError(paramObject, paramClass);
      break;
    case 6:
    case 7:
      if (paramClass.isPrimitive())
      {
        if (paramClass == Boolean.TYPE)
          reportConversionError(paramObject, paramClass);
        return coerceToNumber(paramClass, paramObject);
      }
      if (paramObject instanceof Wrapper)
        paramObject = ((Wrapper)paramObject).unwrap();
      if (paramClass == ScriptRuntime.StringClass)
        return paramObject.toString();
      if (paramClass.isInstance(paramObject))
        return paramObject;
      reportConversionError(paramObject, paramClass);
      break;
    case 8:
      Object localObject1;
      Object localObject4;
      Object localObject5;
      if (paramClass == ScriptRuntime.StringClass)
        return ScriptRuntime.toString(paramObject);
      if (paramClass.isPrimitive())
      {
        if (paramClass == Boolean.TYPE)
          reportConversionError(paramObject, paramClass);
        return coerceToNumber(paramClass, paramObject);
      }
      if (paramClass.isInstance(paramObject))
        return paramObject;
      if ((paramClass == ScriptRuntime.DateClass) && (paramObject instanceof NativeDate))
      {
        double d = ((NativeDate)paramObject).getJSTimeValue();
        return new Date(()d);
      }
      if ((paramClass.isArray()) && (paramObject instanceof NativeArray))
      {
        localObject1 = (NativeArray)paramObject;
        long l = ((NativeArray)localObject1).getLength();
        localObject4 = paramClass.getComponentType();
        localObject5 = Array.newInstance((Class)localObject4, (int)l);
        for (int i = 0; i < l; ++i)
          try
          {
            Array.set(localObject5, i, coerceType((Class)localObject4, ((NativeArray)localObject1).get(i, (Serializable)localObject1)));
          }
          catch (EvaluatorException localEvaluatorException)
          {
            reportConversionError(paramObject, paramClass);
          }
        return localObject5;
      }
      if (paramObject instanceof Wrapper)
      {
        paramObject = ((Wrapper)paramObject).unwrap();
        if (paramClass.isInstance(paramObject))
          return paramObject;
        reportConversionError(paramObject, paramClass);
        break label736:
      }
      if ((paramClass.isInterface()) && (paramObject instanceof Callable))
      {
        if (paramObject instanceof ScriptableObject)
        {
          localObject1 = (ScriptableObject)paramObject;
          Object localObject2 = Kit.makeHashKeyFromPair(COERCED_INTERFACE_KEY, paramClass);
          Object localObject3 = ((ScriptableObject)localObject1).getAssociatedValue(localObject2);
          if (localObject3 != null)
            return localObject3;
          localObject4 = Context.getContext();
          localObject5 = InterfaceAdapter.create((Context)localObject4, paramClass, (Callable)paramObject);
          localObject5 = ((ScriptableObject)localObject1).associateValue(localObject2, localObject5);
          return localObject5;
        }
        reportConversionError(paramObject, paramClass);
        break label736:
      }
      reportConversionError(paramObject, paramClass);
    }
    label736: return paramObject;
  }

  private static Object coerceToNumber(Class paramClass, Object paramObject)
  {
    double d1;
    double d2;
    Class localClass = paramObject.getClass();
    if ((paramClass == Character.TYPE) || (paramClass == ScriptRuntime.CharacterClass))
    {
      if (localClass == ScriptRuntime.CharacterClass)
        return paramObject;
      return new Character((char)(int)toInteger(paramObject, ScriptRuntime.CharacterClass, 0D, 65535.0D));
    }
    if ((paramClass == ScriptRuntime.ObjectClass) || (paramClass == ScriptRuntime.DoubleClass) || (paramClass == Double.TYPE))
      return new Double(toDouble(paramObject));
    if ((paramClass == ScriptRuntime.FloatClass) || (paramClass == Float.TYPE))
    {
      if (localClass == ScriptRuntime.FloatClass)
        return paramObject;
      d1 = toDouble(paramObject);
      if ((Double.isInfinite(d1)) || (Double.isNaN(d1)) || (d1 == 0D))
        return new Float((float)d1);
      d2 = Math.abs(d1);
      if (d2 < 0.0000000000000000000000000000000000000000000014013D)
        return new Float(-0.0D);
      if (d2 > 340282346638528860000000000000000000000.0D)
        return new Float((1.0F / -1.0F));
      return new Float((float)d1);
    }
    if ((paramClass == ScriptRuntime.IntegerClass) || (paramClass == Integer.TYPE))
    {
      if (localClass == ScriptRuntime.IntegerClass)
        return paramObject;
      return new Integer((int)toInteger(paramObject, ScriptRuntime.IntegerClass, -2147483648.0D, 2147483647.0D));
    }
    if ((paramClass == ScriptRuntime.LongClass) || (paramClass == Long.TYPE))
    {
      if (localClass == ScriptRuntime.LongClass)
        return paramObject;
      d1 = Double.longBitsToDouble(4890909195324358655L);
      d2 = Double.longBitsToDouble(-4332462841530417152L);
      return new Long(toInteger(paramObject, ScriptRuntime.LongClass, d2, d1));
    }
    if ((paramClass == ScriptRuntime.ShortClass) || (paramClass == Short.TYPE))
    {
      if (localClass == ScriptRuntime.ShortClass)
        return paramObject;
      return new Short((short)(int)toInteger(paramObject, ScriptRuntime.ShortClass, -32768.0D, 32767.0D));
    }
    if ((paramClass == ScriptRuntime.ByteClass) || (paramClass == Byte.TYPE))
    {
      if (localClass == ScriptRuntime.ByteClass)
        return paramObject;
      return new Byte((byte)(int)toInteger(paramObject, ScriptRuntime.ByteClass, -128.0D, 127.0D));
    }
    return new Double(toDouble(paramObject));
  }

  private static double toDouble(Object paramObject)
  {
    Method localMethod;
    if (paramObject instanceof Number)
      return ((Number)paramObject).doubleValue();
    if (paramObject instanceof String)
      return ScriptRuntime.toNumber((String)paramObject);
    if (paramObject instanceof Serializable)
    {
      if (paramObject instanceof Wrapper)
        return toDouble(((Wrapper)paramObject).unwrap());
      return ScriptRuntime.toNumber(paramObject);
    }
    try
    {
      localMethod = paramObject.getClass().getMethod("doubleValue", null);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      localMethod = null;
    }
    catch (SecurityException localSecurityException)
    {
      localMethod = null;
    }
    if (localMethod != null)
      try
      {
        return ((Number)localMethod.invoke(paramObject, null)).doubleValue();
      }
      catch (IllegalAccessException localIllegalAccessException)
      {
        reportConversionError(paramObject, Double.TYPE);
      }
      catch (InvocationTargetException localInvocationTargetException)
      {
        reportConversionError(paramObject, Double.TYPE);
      }
    return ScriptRuntime.toNumber(paramObject.toString());
  }

  private static long toInteger(Object paramObject, Class paramClass, double paramDouble1, double paramDouble2)
  {
    double d = toDouble(paramObject);
    if ((Double.isInfinite(d)) || (Double.isNaN(d)))
      reportConversionError(ScriptRuntime.toString(paramObject), paramClass);
    if (d > 0D)
      d = Math.floor(d);
    else
      d = Math.ceil(d);
    if ((d < paramDouble1) || (d > paramDouble2))
      reportConversionError(ScriptRuntime.toString(paramObject), paramClass);
    return ()d;
  }

  static void reportConversionError(Object paramObject, Class paramClass)
  {
    throw Context.reportRuntimeError2("msg.conversion.not.allowed", String.valueOf(paramObject), JavaMembers.javaSignature(paramClass));
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    paramObjectOutputStream.defaultWriteObject();
    if (this.javaObject != null)
    {
      Class localClass = this.javaObject.getClass();
      if (localClass.getName().startsWith("adapter"))
      {
        paramObjectOutputStream.writeBoolean(true);
        if (adapter_writeAdapterObject == null)
          throw new IOException();
        Object[] arrayOfObject = { this.javaObject, paramObjectOutputStream };
        try
        {
          adapter_writeAdapterObject.invoke(null, arrayOfObject);
        }
        catch (Exception localException)
        {
          throw new IOException();
        }
      }
      else
      {
        paramObjectOutputStream.writeBoolean(false);
        paramObjectOutputStream.writeObject(this.javaObject);
      }
    }
    else
    {
      paramObjectOutputStream.writeBoolean(false);
      paramObjectOutputStream.writeObject(this.javaObject);
    }
    if (this.staticType != null)
      paramObjectOutputStream.writeObject(this.staticType.getClass().getName());
    else
      paramObjectOutputStream.writeObject(null);
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    if (paramObjectInputStream.readBoolean())
    {
      if (adapter_readAdapterObject == null)
        throw new ClassNotFoundException();
      localObject = { this, paramObjectInputStream };
      try
      {
        this.javaObject = adapter_readAdapterObject.invoke(null, localObject);
      }
      catch (Exception localException)
      {
        throw new IOException();
      }
    }
    else
    {
      this.javaObject = paramObjectInputStream.readObject();
    }
    Object localObject = (String)paramObjectInputStream.readObject();
    if (localObject != null)
      this.staticType = Class.forName((String)localObject);
    else
      this.staticType = null;
    initMembers();
  }

  static
  {
    Class[] arrayOfClass = new Class[2];
    Class localClass = Kit.classOrNull("sun.org.mozilla.javascript.internal.JavaAdapter");
    if (localClass != null)
      try
      {
        arrayOfClass[0] = ScriptRuntime.ObjectClass;
        arrayOfClass[1] = Kit.classOrNull("java.io.ObjectOutputStream");
        adapter_writeAdapterObject = localClass.getMethod("writeAdapterObject", arrayOfClass);
        arrayOfClass[0] = ScriptRuntime.ScriptableClass;
        arrayOfClass[1] = Kit.classOrNull("java.io.ObjectInputStream");
        adapter_readAdapterObject = localClass.getMethod("readAdapterObject", arrayOfClass);
      }
      catch (Exception localException)
      {
        adapter_writeAdapterObject = null;
        adapter_readAdapterObject = null;
      }
  }
}