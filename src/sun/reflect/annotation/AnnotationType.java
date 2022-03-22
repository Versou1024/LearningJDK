package sun.reflect.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

public class AnnotationType
{
  private final Map<String, Class> memberTypes = new HashMap();
  private final Map<String, Object> memberDefaults = new HashMap();
  private final Map<String, Method> members = new HashMap();
  private RetentionPolicy retention = RetentionPolicy.RUNTIME;
  private boolean inherited = false;

  public static synchronized AnnotationType getInstance(Class paramClass)
  {
    AnnotationType localAnnotationType = SharedSecrets.getJavaLangAccess().getAnnotationType(paramClass);
    if (localAnnotationType == null)
      localAnnotationType = new AnnotationType(paramClass);
    return localAnnotationType;
  }

  private AnnotationType(Class<?> paramClass)
  {
    if (!(paramClass.isAnnotation()))
      throw new IllegalArgumentException("Not an annotation type");
    Method[] arrayOfMethod = (Method[])AccessController.doPrivileged(new PrivilegedAction(this, paramClass)
    {
      public Method[] run()
      {
        return this.val$annotationClass.getDeclaredMethods();
      }
    });
    Object localObject1 = arrayOfMethod;
    int i = localObject1.length;
    for (int j = 0; j < i; ++j)
    {
      Object localObject2 = localObject1[j];
      if (localObject2.getParameterTypes().length != 0)
        throw new IllegalArgumentException(localObject2 + " has params");
      String str = localObject2.getName();
      Class localClass = localObject2.getReturnType();
      this.memberTypes.put(str, invocationHandlerReturnType(localClass));
      this.members.put(str, localObject2);
      Object localObject3 = localObject2.getDefaultValue();
      if (localObject3 != null)
        this.memberDefaults.put(str, localObject3);
      this.members.put(str, localObject2);
    }
    SharedSecrets.getJavaLangAccess().setAnnotationType(paramClass, this);
    if ((paramClass != Retention.class) && (paramClass != Inherited.class))
    {
      localObject1 = (Retention)paramClass.getAnnotation(Retention.class);
      this.retention = ((localObject1 == null) ? RetentionPolicy.CLASS : ((Retention)localObject1).value());
      this.inherited = paramClass.isAnnotationPresent(Inherited.class);
    }
  }

  public static Class invocationHandlerReturnType(Class paramClass)
  {
    if (paramClass == Byte.TYPE)
      return Byte.class;
    if (paramClass == Character.TYPE)
      return Character.class;
    if (paramClass == Double.TYPE)
      return Double.class;
    if (paramClass == Float.TYPE)
      return Float.class;
    if (paramClass == Integer.TYPE)
      return Integer.class;
    if (paramClass == Long.TYPE)
      return Long.class;
    if (paramClass == Short.TYPE)
      return Short.class;
    if (paramClass == Boolean.TYPE)
      return Boolean.class;
    return paramClass;
  }

  public Map<String, Class> memberTypes()
  {
    return this.memberTypes;
  }

  public Map<String, Method> members()
  {
    return this.members;
  }

  public Map<String, Object> memberDefaults()
  {
    return this.memberDefaults;
  }

  public RetentionPolicy retention()
  {
    return this.retention;
  }

  public boolean isInherited()
  {
    return this.inherited;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer("Annotation Type:\n");
    localStringBuffer.append("   Member types: " + this.memberTypes + "\n");
    localStringBuffer.append("   Member defaults: " + this.memberDefaults + "\n");
    localStringBuffer.append("   Retention policy: " + this.retention + "\n");
    localStringBuffer.append("   Inherited: " + this.inherited);
    return localStringBuffer.toString();
  }
}