package sun.org.mozilla.javascript.internal;

import java.lang.reflect.Method;

public class NativeJavaMethod extends BaseFunction
{
  static final long serialVersionUID = -3440381785576412928L;
  private static final int PREFERENCE_EQUAL = 0;
  private static final int PREFERENCE_FIRST_ARG = 1;
  private static final int PREFERENCE_SECOND_ARG = 2;
  private static final int PREFERENCE_AMBIGUOUS = 3;
  private static final boolean debug = 0;
  MemberBox[] methods;
  private String functionName;

  NativeJavaMethod(MemberBox[] paramArrayOfMemberBox)
  {
    this.functionName = paramArrayOfMemberBox[0].getName();
    this.methods = paramArrayOfMemberBox;
  }

  NativeJavaMethod(MemberBox paramMemberBox, String paramString)
  {
    this.functionName = paramString;
    this.methods = { paramMemberBox };
  }

  public NativeJavaMethod(Method paramMethod, String paramString)
  {
    this(new MemberBox(paramMethod), paramString);
  }

  public String getFunctionName()
  {
    return this.functionName;
  }

  static String scriptSignature(Object[] paramArrayOfObject)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i != paramArrayOfObject.length; ++i)
    {
      String str;
      Object localObject1 = paramArrayOfObject[i];
      if (localObject1 == null)
        str = "null";
      else if (localObject1 instanceof Boolean)
        str = "boolean";
      else if (localObject1 instanceof String)
        str = "string";
      else if (localObject1 instanceof Number)
        str = "number";
      else if (localObject1 instanceof Scriptable)
        if (localObject1 instanceof Undefined)
        {
          str = "undefined";
        }
        else if (localObject1 instanceof Wrapper)
        {
          Object localObject2 = ((Wrapper)localObject1).unwrap();
          str = localObject2.getClass().getName();
        }
        else if (localObject1 instanceof Function)
        {
          str = "function";
        }
        else
        {
          str = "object";
        }
      else
        str = JavaMembers.javaSignature(localObject1.getClass());
      if (i != 0)
        localStringBuffer.append(',');
      localStringBuffer.append(str);
    }
    return localStringBuffer.toString();
  }

  String decompile(int paramInt1, int paramInt2)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = (0 != (paramInt2 & 0x1)) ? 1 : 0;
    if (i == 0)
    {
      localStringBuffer.append("function ");
      localStringBuffer.append(getFunctionName());
      localStringBuffer.append("() {");
    }
    localStringBuffer.append("/*\n");
    localStringBuffer.append(toString());
    localStringBuffer.append((i != 0) ? "*/\n" : "*/}\n");
    return localStringBuffer.toString();
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 0;
    int j = this.methods.length;
    while (i != j)
    {
      Method localMethod = this.methods[i].method();
      localStringBuffer.append(JavaMembers.javaSignature(localMethod.getReturnType()));
      localStringBuffer.append(' ');
      localStringBuffer.append(localMethod.getName());
      localStringBuffer.append(JavaMembers.liveConnectSignature(this.methods[i].argTypes));
      localStringBuffer.append('\n');
      ++i;
    }
    return localStringBuffer.toString();
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    Object localObject3;
    if (this.methods.length == 0)
      throw new RuntimeException("No methods defined for call");
    int i = findFunction(paramContext, this.methods, paramArrayOfObject);
    if (i < 0)
    {
      localObject1 = this.methods[0].method().getDeclaringClass();
      localObject2 = ((Class)localObject1).getName() + '.' + getFunctionName() + '(' + scriptSignature(paramArrayOfObject) + ')';
      throw Context.reportRuntimeError1("msg.java.no_such_method", localObject2);
    }
    Object localObject1 = this.methods[i];
    Object localObject2 = ((MemberBox)localObject1).argTypes;
    Object[] arrayOfObject = paramArrayOfObject;
    for (int j = 0; j < paramArrayOfObject.length; ++j)
    {
      localObject4 = paramArrayOfObject[j];
      localObject5 = Context.jsToJava(localObject4, localObject2[j]);
      if (localObject5 != localObject4)
      {
        if (arrayOfObject == paramArrayOfObject)
          paramArrayOfObject = (Object[])(Object[])paramArrayOfObject.clone();
        paramArrayOfObject[j] = localObject5;
      }
    }
    if (((MemberBox)localObject1).isStatic())
    {
      localObject3 = null;
    }
    else
    {
      localObject4 = paramScriptable2;
      localObject5 = ((MemberBox)localObject1).getDeclaringClass();
      while (true)
      {
        if (localObject4 == null)
          throw Context.reportRuntimeError3("msg.nonjava.method", getFunctionName(), ScriptRuntime.toString(paramScriptable2), ((Class)localObject5).getName());
        if (localObject4 instanceof Wrapper)
        {
          localObject3 = ((Wrapper)localObject4).unwrap();
          if (((Class)localObject5).isInstance(localObject3))
            break;
        }
        localObject4 = ((Scriptable)localObject4).getPrototype();
      }
    }
    Object localObject4 = ((MemberBox)localObject1).invoke(localObject3, paramArrayOfObject);
    Object localObject5 = ((MemberBox)localObject1).method().getReturnType();
    Object localObject6 = paramContext.getWrapFactory().wrap(paramContext, paramScriptable1, localObject4, (Class)localObject5);
    if ((localObject6 == null) && (localObject5 == Void.TYPE))
      localObject6 = Undefined.instance;
    return localObject6;
  }

  static int findFunction(Context paramContext, MemberBox[] paramArrayOfMemberBox, Object[] paramArrayOfObject)
  {
    Class[] arrayOfClass3;
    if (paramArrayOfMemberBox.length == 0)
      return -1;
    if (paramArrayOfMemberBox.length == 1)
    {
      MemberBox localMemberBox1 = paramArrayOfMemberBox[0];
      localObject = localMemberBox1.argTypes;
      i = localObject.length;
      if (i != paramArrayOfObject.length)
        return -1;
      for (arrayOfClass2 = 0; arrayOfClass2 != i; ++arrayOfClass2)
        if (!(NativeJavaObject.canConvert(paramArrayOfObject[arrayOfClass2], localObject[arrayOfClass2])))
          return -1;
      return 0;
    }
    Class[] arrayOfClass1 = -1;
    Object localObject = null;
    int i = 0;
    for (Class[] arrayOfClass2 = 0; arrayOfClass2 < paramArrayOfMemberBox.length; ++arrayOfClass2)
    {
      MemberBox localMemberBox2 = paramArrayOfMemberBox[arrayOfClass2];
      arrayOfClass3 = localMemberBox2.argTypes;
      if (arrayOfClass3.length != paramArrayOfObject.length)
        break label360:
      for (int l = 0; l < arrayOfClass3.length; ++l)
        if (!(NativeJavaObject.canConvert(paramArrayOfObject[l], arrayOfClass3[l])))
          break label360:
      if (arrayOfClass1 < 0)
      {
        arrayOfClass1 = arrayOfClass2;
      }
      else
      {
        l = 0;
        int i1 = 0;
        for (int i2 = -1; i2 != i; ++i2)
        {
          int i3;
          if (i2 == -1)
            i3 = arrayOfClass1;
          else
            i3 = localObject[i2];
          MemberBox localMemberBox4 = paramArrayOfMemberBox[i3];
          int i4 = preferSignature(paramArrayOfObject, arrayOfClass3, localMemberBox4.argTypes);
          if (i4 == 3)
            break;
          if (i4 == 1)
          {
            ++l;
          }
          else if (i4 == 2)
          {
            ++i1;
          }
          else
          {
            if (i4 != 0)
              Kit.codeBug();
            if ((!(localMemberBox4.isStatic())) || (!(localMemberBox4.getDeclaringClass().isAssignableFrom(localMemberBox2.getDeclaringClass()))))
              break label360;
            if (i2 == -1)
            {
              arrayOfClass1 = arrayOfClass2;
              break label360:
            }
            localObject[i2] = arrayOfClass2;
            break label360:
          }
        }
        if (l == 1 + i)
        {
          arrayOfClass1 = arrayOfClass2;
          label360: i = 0;
        }
        else
        {
          if (i1 == 1 + i)
            break label360:
          if (localObject == null)
            localObject = new int[paramArrayOfMemberBox.length - 1];
          localObject[i] = arrayOfClass2;
          ++i;
        }
      }
    }
    if (arrayOfClass1 < 0)
      return -1;
    if (i == 0)
      return arrayOfClass1;
    StringBuffer localStringBuffer = new StringBuffer();
    for (int j = -1; j != i; ++j)
    {
      int k;
      if (j == -1)
        arrayOfClass3 = arrayOfClass1;
      else
        k = localObject[j];
      localStringBuffer.append("\n    ");
      localStringBuffer.append(paramArrayOfMemberBox[k].toJavaDeclaration());
    }
    MemberBox localMemberBox3 = paramArrayOfMemberBox[arrayOfClass1];
    String str1 = localMemberBox3.getName();
    String str2 = localMemberBox3.getDeclaringClass().getName();
    if (paramArrayOfMemberBox[0].isMethod())
      throw Context.reportRuntimeError3("msg.constructor.ambiguous", str1, scriptSignature(paramArrayOfObject), localStringBuffer.toString());
    throw Context.reportRuntimeError4("msg.method.ambiguous", str2, str1, scriptSignature(paramArrayOfObject), localStringBuffer.toString());
  }

  private static int preferSignature(Object[] paramArrayOfObject, Class[] paramArrayOfClass1, Class[] paramArrayOfClass2)
  {
    int i = 0;
    for (int j = 0; j < paramArrayOfObject.length; ++j)
    {
      int i1;
      Class localClass1 = paramArrayOfClass1[j];
      Class localClass2 = paramArrayOfClass2[j];
      if (localClass1 == localClass2)
        break label143:
      Object localObject = paramArrayOfObject[j];
      int k = NativeJavaObject.getConversionWeight(localObject, localClass1);
      int l = NativeJavaObject.getConversionWeight(localObject, localClass2);
      if (k < l)
        i1 = 1;
      else if (k > l)
        i1 = 2;
      else if (k == 0)
        if (localClass1.isAssignableFrom(localClass2))
          i1 = 2;
        else if (localClass2.isAssignableFrom(localClass1))
          i1 = 1;
        else
          i1 = 3;
      else
        i1 = 3;
      i |= i1;
      label143: if (i == 3)
        break;
    }
    return i;
  }

  private static void printDebug(String paramString, MemberBox paramMemberBox, Object[] paramArrayOfObject)
  {
  }
}