package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import sun.reflect.misc.MethodUtil;

final class MemberBox
  implements Serializable
{
  static final long serialVersionUID = 6358550398665688245L;
  private static final Class[] primitives = { Boolean.TYPE, Byte.TYPE, Character.TYPE, Double.TYPE, Float.TYPE, Integer.TYPE, Long.TYPE, Short.TYPE, Void.TYPE };
  private transient Member memberObject;
  transient Class[] argTypes;

  MemberBox(Method paramMethod)
  {
    init(paramMethod);
  }

  MemberBox(Constructor paramConstructor)
  {
    init(paramConstructor);
  }

  private void init(Method paramMethod)
  {
    this.memberObject = paramMethod;
    this.argTypes = paramMethod.getParameterTypes();
  }

  private void init(Constructor paramConstructor)
  {
    this.memberObject = paramConstructor;
    this.argTypes = paramConstructor.getParameterTypes();
  }

  Method method()
  {
    return ((Method)this.memberObject);
  }

  Constructor ctor()
  {
    return ((Constructor)this.memberObject);
  }

  boolean isMethod()
  {
    return this.memberObject instanceof Method;
  }

  boolean isCtor()
  {
    return this.memberObject instanceof Constructor;
  }

  boolean isStatic()
  {
    return Modifier.isStatic(this.memberObject.getModifiers());
  }

  String getName()
  {
    return this.memberObject.getName();
  }

  Class getDeclaringClass()
  {
    return this.memberObject.getDeclaringClass();
  }

  String toJavaDeclaration()
  {
    Object localObject;
    StringBuffer localStringBuffer = new StringBuffer();
    if (isMethod())
    {
      localObject = method();
      localStringBuffer.append(((Method)localObject).getReturnType());
      localStringBuffer.append(' ');
      localStringBuffer.append(((Method)localObject).getName());
    }
    else
    {
      localObject = ctor();
      String str = ((Constructor)localObject).getDeclaringClass().getName();
      int i = str.lastIndexOf(46);
      if (i >= 0)
        str = str.substring(i + 1);
      localStringBuffer.append(str);
    }
    localStringBuffer.append(JavaMembers.liveConnectSignature(this.argTypes));
    return ((String)localStringBuffer.toString());
  }

  public String toString()
  {
    return this.memberObject.toString();
  }

  Object invoke(Object paramObject, Object[] paramArrayOfObject)
  {
    Object localObject = method();
    try
    {
      return MethodUtil.invoke((Method)localObject, paramObject, paramArrayOfObject);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      Method localMethod = searchAccessibleMethod((Method)localObject, this.argTypes);
      if (localMethod != null)
      {
        this.memberObject = localMethod;
        localObject = localMethod;
      }
      else if (!(VMBridge.instance.tryToMakeAccessible(localObject)))
      {
        throw Context.throwAsScriptRuntimeEx(localIllegalAccessException);
      }
      return MethodUtil.invoke((Method)localObject, paramObject, paramArrayOfObject);
    }
    catch (Exception localException)
    {
      throw Context.throwAsScriptRuntimeEx(localException);
    }
  }

  Object newInstance(Object[] paramArrayOfObject)
  {
    Constructor localConstructor = ctor();
    try
    {
      return localConstructor.newInstance(paramArrayOfObject);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      if (!(VMBridge.instance.tryToMakeAccessible(localConstructor)))
        throw Context.throwAsScriptRuntimeEx(localIllegalAccessException);
      return localConstructor.newInstance(paramArrayOfObject);
    }
    catch (Exception localException)
    {
      throw Context.throwAsScriptRuntimeEx(localException);
    }
  }

  private static Method searchAccessibleMethod(Method paramMethod, Class[] paramArrayOfClass)
  {
    int i = paramMethod.getModifiers();
    if ((Modifier.isPublic(i)) && (!(Modifier.isStatic(i))))
    {
      Class localClass1 = paramMethod.getDeclaringClass();
      if (!(Modifier.isPublic(localClass1.getModifiers())))
      {
        String str = paramMethod.getName();
        Class[] arrayOfClass = localClass1.getInterfaces();
        int j = 0;
        int k = arrayOfClass.length;
        while (j != k)
        {
          Class localClass2 = arrayOfClass[j];
          if (Modifier.isPublic(localClass2.getModifiers()))
            try
            {
              return localClass2.getMethod(str, paramArrayOfClass);
            }
            catch (NoSuchMethodException localNoSuchMethodException2)
            {
            }
            catch (SecurityException localSecurityException2)
            {
            }
          ++j;
        }
        do
        {
          localClass1 = localClass1.getSuperclass();
          if (localClass1 == null)
            break label171:
        }
        while (!(Modifier.isPublic(localClass1.getModifiers())));
        try
        {
          Method localMethod = localClass1.getMethod(str, paramArrayOfClass);
          k = localMethod.getModifiers();
          if ((Modifier.isPublic(k)) && (!(Modifier.isStatic(k))))
            return localMethod;
        }
        catch (NoSuchMethodException localNoSuchMethodException1)
        {
        }
        catch (SecurityException localSecurityException1)
        {
        }
      }
    }
    label171: return null;
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    Member localMember = readMember(paramObjectInputStream);
    if (localMember instanceof Method)
      init((Method)localMember);
    else
      init((Constructor)localMember);
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    paramObjectOutputStream.defaultWriteObject();
    writeMember(paramObjectOutputStream, this.memberObject);
  }

  private static void writeMember(ObjectOutputStream paramObjectOutputStream, Member paramMember)
    throws IOException
  {
    if (paramMember == null)
    {
      paramObjectOutputStream.writeBoolean(false);
      return;
    }
    paramObjectOutputStream.writeBoolean(true);
    if ((!(paramMember instanceof Method)) && (!(paramMember instanceof Constructor)))
      throw new IllegalArgumentException("not Method or Constructor");
    paramObjectOutputStream.writeBoolean(paramMember instanceof Method);
    paramObjectOutputStream.writeObject(paramMember.getName());
    paramObjectOutputStream.writeObject(paramMember.getDeclaringClass());
    if (paramMember instanceof Method)
      writeParameters(paramObjectOutputStream, ((Method)paramMember).getParameterTypes());
    else
      writeParameters(paramObjectOutputStream, ((Constructor)paramMember).getParameterTypes());
  }

  private static Member readMember(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    if (!(paramObjectInputStream.readBoolean()))
      return null;
    boolean bool = paramObjectInputStream.readBoolean();
    String str = (String)paramObjectInputStream.readObject();
    Class localClass = (Class)paramObjectInputStream.readObject();
    Class[] arrayOfClass = readParameters(paramObjectInputStream);
    try
    {
      if (bool)
        return localClass.getMethod(str, arrayOfClass);
      return localClass.getConstructor(arrayOfClass);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new IOException("Cannot find member: " + localNoSuchMethodException);
    }
  }

  private static void writeParameters(ObjectOutputStream paramObjectOutputStream, Class[] paramArrayOfClass)
    throws IOException
  {
    paramObjectOutputStream.writeShort(paramArrayOfClass.length);
    for (int i = 0; i < paramArrayOfClass.length; ++i)
    {
      Class localClass = paramArrayOfClass[i];
      paramObjectOutputStream.writeBoolean(localClass.isPrimitive());
      if (!(localClass.isPrimitive()))
      {
        paramObjectOutputStream.writeObject(localClass);
      }
      else
      {
        for (int j = 0; j < primitives.length; ++j)
          if (localClass.equals(primitives[j]))
          {
            paramObjectOutputStream.writeByte(j);
            break label113:
          }
        label113: throw new IllegalArgumentException("Primitive " + localClass + " not found");
      }
    }
  }

  private static Class[] readParameters(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    Class[] arrayOfClass = new Class[paramObjectInputStream.readShort()];
    for (int i = 0; i < arrayOfClass.length; ++i)
      if (!(paramObjectInputStream.readBoolean()))
        arrayOfClass[i] = ((Class)paramObjectInputStream.readObject());
      else
        arrayOfClass[i] = primitives[paramObjectInputStream.readByte()];
    return arrayOfClass;
  }
}