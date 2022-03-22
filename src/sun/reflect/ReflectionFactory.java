package sun.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;

public class ReflectionFactory
{
  private static boolean initted = false;
  private static Permission reflectionFactoryAccessPerm = new RuntimePermission("reflectionFactoryAccess");
  private static ReflectionFactory soleInstance = new ReflectionFactory();
  private static volatile LangReflectAccess langReflectAccess;
  private static boolean noInflation = false;
  private static int inflationThreshold = 15;

  public static ReflectionFactory getReflectionFactory()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPermission(reflectionFactoryAccessPerm);
    return soleInstance;
  }

  public void setLangReflectAccess(LangReflectAccess paramLangReflectAccess)
  {
    langReflectAccess = paramLangReflectAccess;
  }

  public FieldAccessor newFieldAccessor(Field paramField, boolean paramBoolean)
  {
    checkInitted();
    return UnsafeFieldAccessorFactory.newFieldAccessor(paramField, paramBoolean);
  }

  public MethodAccessor newMethodAccessor(Method paramMethod)
  {
    checkInitted();
    if (noInflation)
      return new MethodAccessorGenerator().generateMethod(paramMethod.getDeclaringClass(), paramMethod.getName(), paramMethod.getParameterTypes(), paramMethod.getReturnType(), paramMethod.getExceptionTypes(), paramMethod.getModifiers());
    NativeMethodAccessorImpl localNativeMethodAccessorImpl = new NativeMethodAccessorImpl(paramMethod);
    DelegatingMethodAccessorImpl localDelegatingMethodAccessorImpl = new DelegatingMethodAccessorImpl(localNativeMethodAccessorImpl);
    localNativeMethodAccessorImpl.setParent(localDelegatingMethodAccessorImpl);
    return localDelegatingMethodAccessorImpl;
  }

  public ConstructorAccessor newConstructorAccessor(Constructor paramConstructor)
  {
    checkInitted();
    Class localClass = paramConstructor.getDeclaringClass();
    if (Modifier.isAbstract(localClass.getModifiers()))
      return new InstantiationExceptionConstructorAccessorImpl(null);
    if (localClass == Class.class)
      return new InstantiationExceptionConstructorAccessorImpl("Can not instantiate java.lang.Class");
    if (Reflection.isSubclassOf(localClass, ConstructorAccessorImpl.class))
      return new BootstrapConstructorAccessorImpl(paramConstructor);
    if (noInflation)
      return new MethodAccessorGenerator().generateConstructor(paramConstructor.getDeclaringClass(), paramConstructor.getParameterTypes(), paramConstructor.getExceptionTypes(), paramConstructor.getModifiers());
    NativeConstructorAccessorImpl localNativeConstructorAccessorImpl = new NativeConstructorAccessorImpl(paramConstructor);
    DelegatingConstructorAccessorImpl localDelegatingConstructorAccessorImpl = new DelegatingConstructorAccessorImpl(localNativeConstructorAccessorImpl);
    localNativeConstructorAccessorImpl.setParent(localDelegatingConstructorAccessorImpl);
    return localDelegatingConstructorAccessorImpl;
  }

  public Field newField(Class paramClass1, String paramString1, Class paramClass2, int paramInt1, int paramInt2, String paramString2, byte[] paramArrayOfByte)
  {
    return langReflectAccess().newField(paramClass1, paramString1, paramClass2, paramInt1, paramInt2, paramString2, paramArrayOfByte);
  }

  public Method newMethod(Class paramClass1, String paramString1, Class[] paramArrayOfClass1, Class paramClass2, Class[] paramArrayOfClass2, int paramInt1, int paramInt2, String paramString2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3)
  {
    return langReflectAccess().newMethod(paramClass1, paramString1, paramArrayOfClass1, paramClass2, paramArrayOfClass2, paramInt1, paramInt2, paramString2, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfByte3);
  }

  public Constructor newConstructor(Class paramClass, Class[] paramArrayOfClass1, Class[] paramArrayOfClass2, int paramInt1, int paramInt2, String paramString, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    return langReflectAccess().newConstructor(paramClass, paramArrayOfClass1, paramArrayOfClass2, paramInt1, paramInt2, paramString, paramArrayOfByte1, paramArrayOfByte2);
  }

  public MethodAccessor getMethodAccessor(Method paramMethod)
  {
    return langReflectAccess().getMethodAccessor(paramMethod);
  }

  public void setMethodAccessor(Method paramMethod, MethodAccessor paramMethodAccessor)
  {
    langReflectAccess().setMethodAccessor(paramMethod, paramMethodAccessor);
  }

  public ConstructorAccessor getConstructorAccessor(Constructor paramConstructor)
  {
    return langReflectAccess().getConstructorAccessor(paramConstructor);
  }

  public void setConstructorAccessor(Constructor paramConstructor, ConstructorAccessor paramConstructorAccessor)
  {
    langReflectAccess().setConstructorAccessor(paramConstructor, paramConstructorAccessor);
  }

  public Method copyMethod(Method paramMethod)
  {
    return langReflectAccess().copyMethod(paramMethod);
  }

  public Field copyField(Field paramField)
  {
    return langReflectAccess().copyField(paramField);
  }

  public Constructor copyConstructor(Constructor paramConstructor)
  {
    return langReflectAccess().copyConstructor(paramConstructor);
  }

  public Constructor newConstructorForSerialization(Class paramClass, Constructor paramConstructor)
  {
    if (paramConstructor.getDeclaringClass() == paramClass)
      return paramConstructor;
    SerializationConstructorAccessorImpl localSerializationConstructorAccessorImpl = new MethodAccessorGenerator().generateSerializationConstructor(paramClass, paramConstructor.getParameterTypes(), paramConstructor.getExceptionTypes(), paramConstructor.getModifiers(), paramConstructor.getDeclaringClass());
    Constructor localConstructor = newConstructor(paramConstructor.getDeclaringClass(), paramConstructor.getParameterTypes(), paramConstructor.getExceptionTypes(), paramConstructor.getModifiers(), langReflectAccess().getConstructorSlot(paramConstructor), langReflectAccess().getConstructorSignature(paramConstructor), langReflectAccess().getConstructorAnnotations(paramConstructor), langReflectAccess().getConstructorParameterAnnotations(paramConstructor));
    setConstructorAccessor(localConstructor, localSerializationConstructorAccessorImpl);
    return localConstructor;
  }

  static int inflationThreshold()
  {
    return inflationThreshold;
  }

  private static void checkInitted()
  {
    if (initted)
      return;
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        if (System.out == null)
          return null;
        String str = System.getProperty("sun.reflect.noInflation");
        if ((str != null) && (str.equals("true")))
          ReflectionFactory.access$002(true);
        str = System.getProperty("sun.reflect.inflationThreshold");
        if (str != null)
          try
          {
            ReflectionFactory.access$102(Integer.parseInt(str));
          }
          catch (NumberFormatException localNumberFormatException)
          {
            throw ((RuntimeException)new RuntimeException("Unable to parse property sun.reflect.inflationThreshold").initCause(localNumberFormatException));
          }
        ReflectionFactory.access$202(true);
        return null;
      }
    });
  }

  private static LangReflectAccess langReflectAccess()
  {
    if (langReflectAccess == null)
      Modifier.isPublic(1);
    return langReflectAccess;
  }

  public static final class GetReflectionFactoryAction
  implements PrivilegedAction
  {
    public Object run()
    {
      return ReflectionFactory.getReflectionFactory();
    }
  }
}