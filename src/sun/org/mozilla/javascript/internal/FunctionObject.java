package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class FunctionObject extends BaseFunction
{
  static final long serialVersionUID = -5332312783643935019L;
  private static final short VARARGS_METHOD = -1;
  private static final short VARARGS_CTOR = -2;
  private static boolean sawSecurityException;
  public static final int JAVA_UNSUPPORTED_TYPE = 0;
  public static final int JAVA_STRING_TYPE = 1;
  public static final int JAVA_INT_TYPE = 2;
  public static final int JAVA_BOOLEAN_TYPE = 3;
  public static final int JAVA_DOUBLE_TYPE = 4;
  public static final int JAVA_SCRIPTABLE_TYPE = 5;
  public static final int JAVA_OBJECT_TYPE = 6;
  MemberBox member;
  private String functionName;
  private transient byte[] typeTags;
  private int parmsLength;
  private transient boolean hasVoidReturn;
  private transient int returnTypeTag;
  private boolean isStatic;

  public FunctionObject(String paramString, Member paramMember, Scriptable paramScriptable)
  {
    if (paramMember instanceof Constructor)
    {
      this.member = new MemberBox((Constructor)paramMember);
      this.isStatic = true;
    }
    else
    {
      this.member = new MemberBox((Method)paramMember);
      this.isStatic = this.member.isStatic();
    }
    String str = this.member.getName();
    this.functionName = paramString;
    Class[] arrayOfClass = this.member.argTypes;
    int i = arrayOfClass.length;
    if ((i == 4) && (((arrayOfClass[1].isArray()) || (arrayOfClass[2].isArray()))))
    {
      if (arrayOfClass[1].isArray())
      {
        if ((!(this.isStatic)) || (arrayOfClass[0] != ScriptRuntime.ContextClass) || (arrayOfClass[1].getComponentType() != ScriptRuntime.ObjectClass) || (arrayOfClass[2] != ScriptRuntime.FunctionClass) || (arrayOfClass[3] != Boolean.TYPE))
          throw Context.reportRuntimeError1("msg.varargs.ctor", str);
        this.parmsLength = -2;
      }
      else
      {
        if ((!(this.isStatic)) || (arrayOfClass[0] != ScriptRuntime.ContextClass) || (arrayOfClass[1] != ScriptRuntime.ScriptableClass) || (arrayOfClass[2].getComponentType() != ScriptRuntime.ObjectClass) || (arrayOfClass[3] != ScriptRuntime.FunctionClass))
          throw Context.reportRuntimeError1("msg.varargs.fun", str);
        this.parmsLength = -1;
      }
    }
    else
    {
      this.parmsLength = i;
      if (i > 0)
      {
        this.typeTags = new byte[i];
        for (int j = 0; j != i; ++j)
        {
          int k = getTypeTag(arrayOfClass[j]);
          if (k == 0)
            throw Context.reportRuntimeError2("msg.bad.parms", arrayOfClass[j].getName(), str);
          this.typeTags[j] = (byte)k;
        }
      }
    }
    if (this.member.isMethod())
    {
      localObject = this.member.method();
      Class localClass = ((Method)localObject).getReturnType();
      if (localClass == Void.TYPE)
        this.hasVoidReturn = true;
      else
        this.returnTypeTag = getTypeTag(localClass);
    }
    else
    {
      localObject = this.member.getDeclaringClass();
      if (!(ScriptRuntime.ScriptableClass.isAssignableFrom((Class)localObject)))
        throw Context.reportRuntimeError1("msg.bad.ctor.return", ((Class)localObject).getName());
    }
    ScriptRuntime.setFunctionProtoAndParent(this, paramScriptable);
  }

  public static int getTypeTag(Class paramClass)
  {
    if (paramClass == ScriptRuntime.StringClass)
      return 1;
    if ((paramClass == ScriptRuntime.IntegerClass) || (paramClass == Integer.TYPE))
      return 2;
    if ((paramClass == ScriptRuntime.BooleanClass) || (paramClass == Boolean.TYPE))
      return 3;
    if ((paramClass == ScriptRuntime.DoubleClass) || (paramClass == Double.TYPE))
      return 4;
    if (ScriptRuntime.ScriptableClass.isAssignableFrom(paramClass))
      return 5;
    if (paramClass == ScriptRuntime.ObjectClass)
      return 6;
    return 0;
  }

  public static Object convertArg(Context paramContext, Scriptable paramScriptable, Object paramObject, int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      if (paramObject instanceof String)
        return paramObject;
      return ScriptRuntime.toString(paramObject);
    case 2:
      if (paramObject instanceof Integer)
        return paramObject;
      return new Integer(ScriptRuntime.toInt32(paramObject));
    case 3:
      if (paramObject instanceof Boolean)
        return paramObject;
      return ((ScriptRuntime.toBoolean(paramObject)) ? Boolean.TRUE : Boolean.FALSE);
    case 4:
      if (paramObject instanceof Double)
        return paramObject;
      return new Double(ScriptRuntime.toNumber(paramObject));
    case 5:
      if (paramObject instanceof Scriptable)
        return paramObject;
      return ScriptRuntime.toObject(paramContext, paramScriptable, paramObject);
    case 6:
      return paramObject;
    }
    throw new IllegalArgumentException();
  }

  public int getArity()
  {
    return ((this.parmsLength < 0) ? 1 : this.parmsLength);
  }

  public int getLength()
  {
    return getArity();
  }

  public String getFunctionName()
  {
    return ((this.functionName == null) ? "" : this.functionName);
  }

  public Member getMethodOrConstructor()
  {
    if (this.member.isMethod())
      return this.member.method();
    return this.member.ctor();
  }

  static Method findSingleMethod(Method[] paramArrayOfMethod, String paramString)
  {
    Object localObject = null;
    int i = 0;
    int j = paramArrayOfMethod.length;
    while (i != j)
    {
      Method localMethod = paramArrayOfMethod[i];
      if ((localMethod != null) && (paramString.equals(localMethod.getName())))
      {
        if (localObject != null)
          throw Context.reportRuntimeError2("msg.no.overload", paramString, localMethod.getDeclaringClass().getName());
        localObject = localMethod;
      }
      ++i;
    }
    return localObject;
  }

  static Method[] getMethodList(Class paramClass)
  {
    Method[] arrayOfMethod1 = null;
    try
    {
      if (!(sawSecurityException))
        arrayOfMethod1 = paramClass.getDeclaredMethods();
    }
    catch (SecurityException localSecurityException)
    {
      sawSecurityException = true;
    }
    if (arrayOfMethod1 == null)
      arrayOfMethod1 = paramClass.getMethods();
    int i = 0;
    for (int j = 0; j < arrayOfMethod1.length; ++j)
    {
      if (sawSecurityException)
        if (arrayOfMethod1[j].getDeclaringClass() == paramClass)
          break label78;
      else
        if (Modifier.isPublic(arrayOfMethod1[j].getModifiers()))
          break label78;
      arrayOfMethod1[j] = null;
      label78: ++i;
    }
    Method[] arrayOfMethod2 = new Method[i];
    int k = 0;
    for (int l = 0; l < arrayOfMethod1.length; ++l)
      if (arrayOfMethod1[l] != null)
        arrayOfMethod2[(k++)] = arrayOfMethod1[l];
    return arrayOfMethod2;
  }

  public void addAsConstructor(Scriptable paramScriptable1, Scriptable paramScriptable2)
  {
    ScriptRuntime.setFunctionProtoAndParent(this, paramScriptable1);
    setImmunePrototypeProperty(paramScriptable2);
    paramScriptable2.setParentScope(this);
    defineProperty(paramScriptable2, "constructor", this, 7);
    String str = paramScriptable2.getClassName();
    defineProperty(paramScriptable1, str, this, 2);
    setParentScope(paramScriptable1);
  }

  /**
   * @deprecated
   */
  public static Object convertArg(Context paramContext, Scriptable paramScriptable, Object paramObject, Class paramClass)
  {
    int i = getTypeTag(paramClass);
    if (i == 0)
      throw Context.reportRuntimeError1("msg.cant.convert", paramClass.getName());
    return convertArg(paramContext, paramScriptable, paramObject, i);
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    Object localObject1;
    Object localObject3;
    int i = 0;
    if (this.parmsLength < 0)
    {
      if (this.parmsLength == -1)
      {
        Object[] arrayOfObject = { paramContext, paramScriptable2, paramArrayOfObject, this };
        localObject1 = this.member.invoke(null, arrayOfObject);
        i = 1;
      }
      else
      {
        int j = (paramScriptable2 == null) ? 1 : 0;
        Boolean localBoolean = (j != 0) ? Boolean.TRUE : Boolean.FALSE;
        localObject3 = { paramContext, paramArrayOfObject, this, localBoolean };
        localObject1 = (this.member.isCtor()) ? this.member.newInstance(localObject3) : this.member.invoke(null, localObject3);
      }
    }
    else
    {
      Object localObject2;
      int k;
      if (!(this.isStatic))
      {
        localObject2 = this.member.getDeclaringClass();
        if (!(((Class)localObject2).isInstance(paramScriptable2)))
        {
          k = 0;
          if (paramScriptable2 == paramScriptable1)
          {
            localObject3 = getParentScope();
            if (paramScriptable1 != localObject3)
            {
              k = ((Class)localObject2).isInstance(localObject3);
              if (k != 0)
                paramScriptable2 = (Scriptable)localObject3;
            }
          }
          if (k == 0)
            throw ScriptRuntime.typeError1("msg.incompat.call", this.functionName);
        }
      }
      if (this.parmsLength == paramArrayOfObject.length)
      {
        localObject2 = paramArrayOfObject;
        for (k = 0; k != this.parmsLength; ++k)
        {
          localObject3 = paramArrayOfObject[k];
          Object localObject4 = convertArg(paramContext, paramScriptable1, localObject3, this.typeTags[k]);
          if (localObject3 != localObject4)
          {
            if (localObject2 == paramArrayOfObject)
              localObject2 = (Object[])(Object[])paramArrayOfObject.clone();
            localObject2[k] = localObject4;
          }
        }
      }
      else if (this.parmsLength == 0)
      {
        localObject2 = ScriptRuntime.emptyArgs;
      }
      else
      {
        localObject2 = new Object[this.parmsLength];
        for (int l = 0; l != this.parmsLength; ++l)
        {
          localObject3 = (l < paramArrayOfObject.length) ? paramArrayOfObject[l] : Undefined.instance;
          localObject2[l] = convertArg(paramContext, paramScriptable1, localObject3, this.typeTags[l]);
        }
      }
      if (this.member.isMethod())
      {
        localObject1 = this.member.invoke(paramScriptable2, localObject2);
        i = 1;
      }
      else
      {
        localObject1 = this.member.newInstance(localObject2);
      }
    }
    if (i != 0)
      if (this.hasVoidReturn)
        localObject1 = Undefined.instance;
      else if (this.returnTypeTag == 0)
        localObject1 = paramContext.getWrapFactory().wrap(paramContext, paramScriptable1, localObject1, null);
    return localObject1;
  }

  public Scriptable createObject(Context paramContext, Scriptable paramScriptable)
  {
    Scriptable localScriptable;
    if ((this.member.isCtor()) || (this.parmsLength == -2))
      return null;
    try
    {
      localScriptable = (Scriptable)this.member.getDeclaringClass().newInstance();
    }
    catch (Exception localException)
    {
      throw Context.throwAsScriptRuntimeEx(localException);
    }
    localScriptable.setPrototype(getClassPrototype());
    localScriptable.setParentScope(getParentScope());
    return localScriptable;
  }

  boolean isVarArgsMethod()
  {
    return (this.parmsLength == -1);
  }

  boolean isVarArgsConstructor()
  {
    return (this.parmsLength == -2);
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    Object localObject;
    paramObjectInputStream.defaultReadObject();
    if (this.parmsLength > 0)
    {
      localObject = this.member.argTypes;
      this.typeTags = new byte[this.parmsLength];
      for (int i = 0; i != this.parmsLength; ++i)
        this.typeTags[i] = (byte)getTypeTag(localObject[i]);
    }
    if (this.member.isMethod())
    {
      localObject = this.member.method();
      Class localClass = ((Method)localObject).getReturnType();
      if (localClass == Void.TYPE)
        this.hasVoidReturn = true;
      else
        this.returnTypeTag = getTypeTag(localClass);
    }
  }
}