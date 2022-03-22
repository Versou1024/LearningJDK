package sun.reflect.generics.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;
import sun.reflect.generics.scope.Scope;
import sun.reflect.generics.tree.FieldTypeSignature;

public class CoreReflectionFactory
  implements GenericsFactory
{
  private GenericDeclaration decl;
  private Scope scope;

  private CoreReflectionFactory(GenericDeclaration paramGenericDeclaration, Scope paramScope)
  {
    this.decl = paramGenericDeclaration;
    this.scope = paramScope;
  }

  private GenericDeclaration getDecl()
  {
    return this.decl;
  }

  private Scope getScope()
  {
    return this.scope;
  }

  private ClassLoader getDeclsLoader()
  {
    if (this.decl instanceof Class)
      return ((Class)this.decl).getClassLoader();
    if (this.decl instanceof Method)
      return ((Method)this.decl).getDeclaringClass().getClassLoader();
    if ((!($assertionsDisabled)) && (!(this.decl instanceof Constructor)))
      throw new AssertionError("Constructor expected");
    return ((Constructor)this.decl).getDeclaringClass().getClassLoader();
  }

  public static CoreReflectionFactory make(GenericDeclaration paramGenericDeclaration, Scope paramScope)
  {
    return new CoreReflectionFactory(paramGenericDeclaration, paramScope);
  }

  public TypeVariable<?> makeTypeVariable(String paramString, FieldTypeSignature[] paramArrayOfFieldTypeSignature)
  {
    return TypeVariableImpl.make(getDecl(), paramString, paramArrayOfFieldTypeSignature, this);
  }

  public WildcardType makeWildcard(FieldTypeSignature[] paramArrayOfFieldTypeSignature1, FieldTypeSignature[] paramArrayOfFieldTypeSignature2)
  {
    return WildcardTypeImpl.make(paramArrayOfFieldTypeSignature1, paramArrayOfFieldTypeSignature2, this);
  }

  public ParameterizedType makeParameterizedType(Type paramType1, Type[] paramArrayOfType, Type paramType2)
  {
    return ParameterizedTypeImpl.make((Class)paramType1, paramArrayOfType, paramType2);
  }

  public TypeVariable<?> findTypeVariable(String paramString)
  {
    return getScope().lookup(paramString);
  }

  public Type makeNamedType(String paramString)
  {
    try
    {
      return Class.forName(paramString, false, getDeclsLoader());
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new TypeNotPresentException(paramString, localClassNotFoundException);
    }
  }

  public Type makeArrayType(Type paramType)
  {
    return GenericArrayTypeImpl.make(paramType);
  }

  public Type makeByte()
  {
    return Byte.TYPE;
  }

  public Type makeBool()
  {
    return Boolean.TYPE;
  }

  public Type makeShort()
  {
    return Short.TYPE;
  }

  public Type makeChar()
  {
    return Character.TYPE;
  }

  public Type makeInt()
  {
    return Integer.TYPE;
  }

  public Type makeLong()
  {
    return Long.TYPE;
  }

  public Type makeFloat()
  {
    return Float.TYPE;
  }

  public Type makeDouble()
  {
    return Double.TYPE;
  }

  public Type makeVoid()
  {
    return Void.TYPE;
  }
}