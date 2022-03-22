package sun.org.mozilla.javascript.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;

class JavaMembers
{
  private Class cl;
  private Hashtable members = new Hashtable(23);
  private Hashtable fieldAndMethods;
  private Hashtable staticMembers = new Hashtable(7);
  private Hashtable staticFieldAndMethods;
  MemberBox[] ctors;

  JavaMembers(Scriptable paramScriptable, Class paramClass)
  {
    this.cl = paramClass;
    reflect(paramScriptable);
  }

  boolean has(String paramString, boolean paramBoolean)
  {
    Hashtable localHashtable = (paramBoolean) ? this.staticMembers : this.members;
    Object localObject = localHashtable.get(paramString);
    if (localObject != null)
      return true;
    return (null != findExplicitFunction(paramString, paramBoolean));
  }

  Object get(Scriptable paramScriptable, String paramString, Object paramObject, boolean paramBoolean)
  {
    Object localObject2;
    Class localClass;
    Hashtable localHashtable = (paramBoolean) ? this.staticMembers : this.members;
    Object localObject1 = localHashtable.get(paramString);
    if ((!(paramBoolean)) && (localObject1 == null))
      localObject1 = this.staticMembers.get(paramString);
    if (localObject1 == null)
    {
      localObject1 = getExplicitFunction(paramScriptable, paramString, paramObject, paramBoolean);
      if (localObject1 == null)
        return Scriptable.NOT_FOUND;
    }
    if (localObject1 instanceof Scriptable)
      return localObject1;
    Context localContext = Context.getContext();
    try
    {
      Object localObject3;
      if (localObject1 instanceof BeanProperty)
      {
        localObject3 = (BeanProperty)localObject1;
        localObject2 = ((BeanProperty)localObject3).getter.invoke(paramObject, Context.emptyArgs);
        localClass = ((BeanProperty)localObject3).getter.method().getReturnType();
      }
      else
      {
        localObject3 = (Field)localObject1;
        localObject2 = ((Field)localObject3).get((paramBoolean) ? null : paramObject);
        localClass = ((Field)localObject3).getType();
      }
    }
    catch (Exception localException)
    {
      throw Context.throwAsScriptRuntimeEx(localException);
    }
    paramScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
    return localContext.getWrapFactory().wrap(localContext, paramScriptable, localObject2, localClass);
  }

  void put(Scriptable paramScriptable, String paramString, Object paramObject1, Object paramObject2, boolean paramBoolean)
  {
    Object localObject2;
    Object localObject3;
    Hashtable localHashtable = (paramBoolean) ? this.staticMembers : this.members;
    Object localObject1 = localHashtable.get(paramString);
    if ((!(paramBoolean)) && (localObject1 == null))
      localObject1 = this.staticMembers.get(paramString);
    if (localObject1 == null)
      throw reportMemberNotFound(paramString);
    if (localObject1 instanceof FieldAndMethods)
    {
      localObject2 = (FieldAndMethods)localHashtable.get(paramString);
      localObject1 = ((FieldAndMethods)localObject2).field;
    }
    if (localObject1 instanceof BeanProperty)
    {
      localObject2 = (BeanProperty)localObject1;
      if (((BeanProperty)localObject2).setter == null)
        throw reportMemberNotFound(paramString);
      if ((((BeanProperty)localObject2).setters == null) || (paramObject2 == null))
      {
        localObject3 = localObject2.setter.argTypes[0];
        Object[] arrayOfObject = { Context.jsToJava(paramObject2, (Class)localObject3) };
        try
        {
          ((BeanProperty)localObject2).setter.invoke(paramObject1, arrayOfObject);
        }
        catch (Exception localException)
        {
          throw Context.throwAsScriptRuntimeEx(localException);
        }
      }
      else
      {
        localObject3 = { paramObject2 };
        ((BeanProperty)localObject2).setters.call(Context.getContext(), ScriptableObject.getTopLevelScope(paramScriptable), paramScriptable, localObject3);
      }
    }
    else
    {
      if (!(localObject1 instanceof Field))
      {
        localObject2 = (localObject1 == null) ? "msg.java.internal.private" : "msg.java.method.assign";
        throw Context.reportRuntimeError1((String)localObject2, paramString);
      }
      localObject2 = (Field)localObject1;
      localObject3 = Context.jsToJava(paramObject2, ((Field)localObject2).getType());
      try
      {
        ((Field)localObject2).set(paramObject1, localObject3);
      }
      catch (IllegalAccessException localIllegalAccessException)
      {
        throw new RuntimeException("unexpected IllegalAccessException accessing Java field");
      }
      catch (IllegalArgumentException localIllegalArgumentException)
      {
        throw Context.reportRuntimeError3("msg.java.internal.field.type", paramObject2.getClass().getName(), localObject2, paramObject1.getClass().getName());
      }
    }
  }

  Object[] getIds(boolean paramBoolean)
  {
    Hashtable localHashtable = (paramBoolean) ? this.staticMembers : this.members;
    int i = localHashtable.size();
    Object[] arrayOfObject = new Object[i];
    Enumeration localEnumeration = localHashtable.keys();
    for (int j = 0; j < i; ++j)
      arrayOfObject[j] = localEnumeration.nextElement();
    return arrayOfObject;
  }

  static String javaSignature(Class paramClass)
  {
    if (!(paramClass.isArray()))
      return paramClass.getName();
    int i = 0;
    do
    {
      ++i;
      paramClass = paramClass.getComponentType();
    }
    while (paramClass.isArray());
    String str1 = paramClass.getName();
    String str2 = "[]";
    if (i == 1)
      return str1.concat(str2);
    int j = str1.length() + i * str2.length();
    StringBuffer localStringBuffer = new StringBuffer(j);
    localStringBuffer.append(str1);
    while (i != 0)
    {
      --i;
      localStringBuffer.append(str2);
    }
    return localStringBuffer.toString();
  }

  static String liveConnectSignature(Class[] paramArrayOfClass)
  {
    int i = paramArrayOfClass.length;
    if (i == 0)
      return "()";
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append('(');
    for (int j = 0; j != i; ++j)
    {
      if (j != 0)
        localStringBuffer.append(',');
      localStringBuffer.append(javaSignature(paramArrayOfClass[j]));
    }
    localStringBuffer.append(')');
    return localStringBuffer.toString();
  }

  private MemberBox findExplicitFunction(String paramString, boolean paramBoolean)
  {
    Object localObject1;
    Object localObject2;
    int i = paramString.indexOf(40);
    if (i < 0)
      return null;
    Hashtable localHashtable = (paramBoolean) ? this.staticMembers : this.members;
    MemberBox[] arrayOfMemberBox = null;
    int j = ((paramBoolean) && (i == 0)) ? 1 : 0;
    if (j != 0)
    {
      arrayOfMemberBox = this.ctors;
    }
    else
    {
      String str = paramString.substring(0, i);
      localObject1 = localHashtable.get(str);
      if ((!(paramBoolean)) && (localObject1 == null))
        localObject1 = this.staticMembers.get(str);
      if (localObject1 instanceof NativeJavaMethod)
      {
        localObject2 = (NativeJavaMethod)localObject1;
        arrayOfMemberBox = ((NativeJavaMethod)localObject2).methods;
      }
    }
    if (arrayOfMemberBox != null)
      for (int k = 0; k < arrayOfMemberBox.length; ++k)
      {
        localObject1 = arrayOfMemberBox[k].argTypes;
        localObject2 = liveConnectSignature(localObject1);
        if ((i + ((String)localObject2).length() == paramString.length()) && (paramString.regionMatches(i, (String)localObject2, 0, ((String)localObject2).length())))
          return arrayOfMemberBox[k];
      }
    return ((MemberBox)(MemberBox)null);
  }

  private Object getExplicitFunction(Scriptable paramScriptable, String paramString, Object paramObject, boolean paramBoolean)
  {
    Hashtable localHashtable = (paramBoolean) ? this.staticMembers : this.members;
    Object localObject1 = null;
    MemberBox localMemberBox = findExplicitFunction(paramString, paramBoolean);
    if (localMemberBox != null)
    {
      Object localObject2;
      Scriptable localScriptable = ScriptableObject.getFunctionPrototype(paramScriptable);
      if (localMemberBox.isCtor())
      {
        localObject2 = new NativeJavaConstructor(localMemberBox);
        ((NativeJavaConstructor)localObject2).setPrototype(localScriptable);
        localObject1 = localObject2;
        localHashtable.put(paramString, localObject2);
      }
      else
      {
        localObject2 = localMemberBox.getName();
        localObject1 = localHashtable.get(localObject2);
        if ((localObject1 instanceof NativeJavaMethod) && (((NativeJavaMethod)localObject1).methods.length > 1))
        {
          NativeJavaMethod localNativeJavaMethod = new NativeJavaMethod(localMemberBox, paramString);
          localNativeJavaMethod.setPrototype(localScriptable);
          localHashtable.put(paramString, localNativeJavaMethod);
          localObject1 = localNativeJavaMethod;
        }
      }
    }
    return localObject1;
  }

  private void reflect(Scriptable paramScriptable)
  {
    label161: Object localObject1;
    Object localObject2;
    Object localObject3;
    Object localObject4;
    Object localObject5;
    Object localObject8;
    Object localObject9;
    Method[] arrayOfMethod = this.cl.getMethods();
    for (int i = 0; i < arrayOfMethod.length; ++i)
    {
      Method localMethod = arrayOfMethod[i];
      int k = localMethod.getModifiers();
      if (!(Modifier.isPublic(k)))
        break label161:
      boolean bool1 = Modifier.isStatic(k);
      localObject2 = (bool1) ? this.staticMembers : this.members;
      localObject3 = localMethod.getName();
      localObject4 = ((Hashtable)localObject2).get(localObject3);
      if (localObject4 == null)
      {
        ((Hashtable)localObject2).put(localObject3, localMethod);
      }
      else
      {
        if (localObject4 instanceof ObjArray)
        {
          localObject5 = (ObjArray)localObject4;
        }
        else
        {
          if (!(localObject4 instanceof Method))
            Kit.codeBug();
          localObject5 = new ObjArray();
          ((ObjArray)localObject5).add(localObject4);
          ((Hashtable)localObject2).put(localObject3, localObject5);
        }
        ((ObjArray)localObject5).add(localMethod);
      }
    }
    for (i = 0; i != 2; ++i)
    {
      j = (i == 0) ? 1 : 0;
      localObject1 = (j != 0) ? this.staticMembers : this.members;
      Enumeration localEnumeration = ((Hashtable)localObject1).keys();
      while (localEnumeration.hasMoreElements())
      {
        localObject2 = (String)localEnumeration.nextElement();
        localObject4 = ((Hashtable)localObject1).get(localObject2);
        if (localObject4 instanceof Method)
        {
          localObject3 = new MemberBox[1];
          localObject3[0] = new MemberBox((Method)localObject4);
        }
        else
        {
          localObject5 = (ObjArray)localObject4;
          int i2 = ((ObjArray)localObject5).size();
          if (i2 < 2)
            Kit.codeBug();
          localObject3 = new MemberBox[i2];
          for (int i3 = 0; i3 != i2; ++i3)
          {
            localObject9 = (Method)((ObjArray)localObject5).get(i3);
            localObject3[i3] = new MemberBox((Method)localObject9);
          }
        }
        localObject5 = new NativeJavaMethod(localObject3);
        if (paramScriptable != null)
          ScriptRuntime.setFunctionProtoAndParent((BaseFunction)localObject5, paramScriptable);
        ((Hashtable)localObject1).put(localObject2, localObject5);
      }
    }
    Field[] arrayOfField = this.cl.getFields();
    for (int j = 0; j < arrayOfField.length; ++j)
    {
      localObject1 = arrayOfField[j];
      int i1 = ((Field)localObject1).getModifiers();
      if (!(Modifier.isPublic(i1)))
        break label644:
      boolean bool2 = Modifier.isStatic(i1);
      localObject3 = (bool2) ? this.staticMembers : this.members;
      localObject4 = ((Field)localObject1).getName();
      localObject5 = ((Hashtable)localObject3).get(localObject4);
      if (localObject5 == null)
      {
        ((Hashtable)localObject3).put(localObject4, localObject1);
      }
      else
      {
        Object localObject7;
        if (localObject5 instanceof NativeJavaMethod)
        {
          localObject7 = (NativeJavaMethod)localObject5;
          localObject8 = new FieldAndMethods(paramScriptable, ((NativeJavaMethod)localObject7).methods, (Field)localObject1);
          localObject9 = (bool2) ? this.staticFieldAndMethods : this.fieldAndMethods;
          if (localObject9 == null)
          {
            localObject9 = new Hashtable(4);
            if (bool2)
              this.staticFieldAndMethods = ((Hashtable)localObject9);
            else
              this.fieldAndMethods = ((Hashtable)localObject9);
          }
          ((Hashtable)localObject9).put(localObject4, localObject8);
          label644: ((Hashtable)localObject3).put(localObject4, localObject8);
        }
        else if (localObject5 instanceof Field)
        {
          localObject7 = (Field)localObject5;
          if (((Field)localObject7).getDeclaringClass().isAssignableFrom(((Field)localObject1).getDeclaringClass()))
            ((Hashtable)localObject3).put(localObject4, localObject1);
        }
        else
        {
          Kit.codeBug();
        }
      }
    }
    for (j = 0; j != 2; ++j)
    {
      l = (j == 0) ? 1 : 0;
      Hashtable localHashtable1 = (l != 0) ? this.staticMembers : this.members;
      Hashtable localHashtable2 = new Hashtable();
      localObject3 = localHashtable1.keys();
      while (true)
      {
        Object localObject10;
        while (true)
        {
          while (true)
          {
            while (true)
            {
              if (!(((Enumeration)localObject3).hasMoreElements()))
                break label1042;
              localObject4 = (String)((Enumeration)localObject3).nextElement();
              boolean bool3 = ((String)localObject4).startsWith("get");
              boolean bool4 = ((String)localObject4).startsWith("is");
              if ((!(bool3)) && (!(bool4)))
                break label1039;
              localObject8 = ((String)localObject4).substring((bool3) ? 3 : 2);
              if (((String)localObject8).length() != 0)
                break;
            }
            localObject9 = localObject8;
            char c1 = ((String)localObject8).charAt(0);
            if (Character.isUpperCase(c1))
              if (((String)localObject8).length() == 1)
              {
                localObject9 = ((String)localObject8).toLowerCase();
              }
              else
              {
                char c2 = ((String)localObject8).charAt(1);
                if (!(Character.isUpperCase(c2)))
                  localObject9 = Character.toLowerCase(c1) + ((String)localObject8).substring(1);
              }
            if (!(localHashtable1.containsKey(localObject9)))
              break;
          }
          localObject10 = localHashtable1.get(localObject4);
          if (localObject10 instanceof NativeJavaMethod)
            break;
        }
        NativeJavaMethod localNativeJavaMethod1 = (NativeJavaMethod)localObject10;
        MemberBox localMemberBox1 = extractGetMethod(localNativeJavaMethod1.methods, l);
        if (localMemberBox1 != null)
        {
          NativeJavaMethod localNativeJavaMethod2 = null;
          MemberBox localMemberBox2 = null;
          NativeJavaMethod localNativeJavaMethod3 = null;
          String str = "set".concat((String)localObject8);
          if (localHashtable1.containsKey(str))
          {
            localObject10 = localHashtable1.get(str);
            if (localObject10 instanceof NativeJavaMethod)
            {
              localNativeJavaMethod2 = (NativeJavaMethod)localObject10;
              localObject11 = localMemberBox1.method().getReturnType();
              localMemberBox2 = extractSetMethod((Class)localObject11, localNativeJavaMethod2.methods, l);
              if (localNativeJavaMethod2.methods.length > 1)
                localNativeJavaMethod3 = localNativeJavaMethod2;
            }
          }
          Object localObject11 = new BeanProperty(localMemberBox1, localMemberBox2, localNativeJavaMethod3);
          label1039: localHashtable2.put(localObject9, localObject11);
        }
      }
      label1042: localObject3 = localHashtable2.keys();
      while (((Enumeration)localObject3).hasMoreElements())
      {
        localObject4 = ((Enumeration)localObject3).nextElement();
        Object localObject6 = localHashtable2.get(localObject4);
        localHashtable1.put(localObject4, localObject6);
      }
    }
    Constructor[] arrayOfConstructor = this.cl.getConstructors();
    this.ctors = new MemberBox[arrayOfConstructor.length];
    for (int l = 0; l != arrayOfConstructor.length; ++l)
      this.ctors[l] = new MemberBox(arrayOfConstructor[l]);
  }

  private static MemberBox extractGetMethod(MemberBox[] paramArrayOfMemberBox, boolean paramBoolean)
  {
    for (int i = 0; i < paramArrayOfMemberBox.length; ++i)
    {
      MemberBox localMemberBox = paramArrayOfMemberBox[i];
      if ((localMemberBox.argTypes.length == 0) && (((!(paramBoolean)) || (localMemberBox.isStatic()))))
      {
        Class localClass = localMemberBox.method().getReturnType();
        if (localClass == Void.TYPE)
          break;
        return localMemberBox;
      }
    }
    return null;
  }

  private static MemberBox extractSetMethod(Class paramClass, MemberBox[] paramArrayOfMemberBox, boolean paramBoolean)
  {
    for (int i = 1; i <= 2; ++i)
      for (int j = 0; j < paramArrayOfMemberBox.length; ++j)
      {
        MemberBox localMemberBox = paramArrayOfMemberBox[j];
        if ((((!(paramBoolean)) || (localMemberBox.isStatic()))) && (localMemberBox.method().getReturnType() == Void.TYPE))
        {
          Class[] arrayOfClass = localMemberBox.argTypes;
          if (arrayOfClass.length == 1)
          {
            if (i == 1)
            {
              if (arrayOfClass[0] != paramClass)
                break label102;
              return localMemberBox;
            }
            if (i != 2)
              Kit.codeBug();
            label102: if (arrayOfClass[0].isAssignableFrom(paramClass))
              return localMemberBox;
          }
        }
      }
    return null;
  }

  Hashtable getFieldAndMethodsObjects(Scriptable paramScriptable, Object paramObject, boolean paramBoolean)
  {
    Hashtable localHashtable1 = (paramBoolean) ? this.staticFieldAndMethods : this.fieldAndMethods;
    if (localHashtable1 == null)
      return null;
    int i = localHashtable1.size();
    Hashtable localHashtable2 = new Hashtable(i);
    Enumeration localEnumeration = localHashtable1.elements();
    while (i-- > 0)
    {
      FieldAndMethods localFieldAndMethods1 = (FieldAndMethods)localEnumeration.nextElement();
      FieldAndMethods localFieldAndMethods2 = new FieldAndMethods(paramScriptable, localFieldAndMethods1.methods, localFieldAndMethods1.field);
      localFieldAndMethods2.javaObject = paramObject;
      localHashtable2.put(localFieldAndMethods1.field.getName(), localFieldAndMethods2);
    }
    return localHashtable2;
  }

  static JavaMembers lookupClass(Scriptable paramScriptable, Class paramClass1, Class paramClass2)
  {
    ClassCache localClassCache = ClassCache.get(paramScriptable);
    Hashtable localHashtable = localClassCache.classTable;
    Object localObject = paramClass1;
    JavaMembers localJavaMembers = (JavaMembers)localHashtable.get(localObject);
    if (localJavaMembers != null)
      return localJavaMembers;
    try
    {
      localJavaMembers = new JavaMembers(localClassCache.scope, (Class)localObject);
    }
    catch (SecurityException localSecurityException)
    {
      while (true)
        if ((paramClass2 != null) && (paramClass2.isInterface()))
        {
          localObject = paramClass2;
          paramClass2 = null;
        }
        else
        {
          Class localClass = ((Class)localObject).getSuperclass();
          if (localClass == null)
            if (((Class)localObject).isInterface())
              localClass = ScriptRuntime.ObjectClass;
            else
              throw localSecurityException;
          localObject = localClass;
        }
    }
    if (localClassCache.isCachingEnabled())
      localHashtable.put(localObject, localJavaMembers);
    return ((JavaMembers)localJavaMembers);
  }

  RuntimeException reportMemberNotFound(String paramString)
  {
    return Context.reportRuntimeError2("msg.java.member.not.found", this.cl.getName(), paramString);
  }
}