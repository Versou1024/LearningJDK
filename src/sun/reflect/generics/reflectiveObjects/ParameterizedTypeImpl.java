package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

public class ParameterizedTypeImpl
  implements ParameterizedType
{
  private Type[] actualTypeArguments;
  private Class<?> rawType;
  private Type ownerType;

  private ParameterizedTypeImpl(Class<?> paramClass, Type[] paramArrayOfType, Type paramType)
  {
    this.actualTypeArguments = paramArrayOfType;
    this.rawType = paramClass;
    if (paramType != null)
      this.ownerType = paramType;
    else
      this.ownerType = paramClass.getDeclaringClass();
    validateConstructorArguments();
  }

  private void validateConstructorArguments()
  {
    TypeVariable[] arrayOfTypeVariable = this.rawType.getTypeParameters();
    if (arrayOfTypeVariable.length != this.actualTypeArguments.length)
      throw new MalformedParameterizedTypeException();
    for (int i = 0; i < this.actualTypeArguments.length; ++i);
  }

  public static ParameterizedTypeImpl make(Class<?> paramClass, Type[] paramArrayOfType, Type paramType)
  {
    return new ParameterizedTypeImpl(paramClass, paramArrayOfType, paramType);
  }

  public Type[] getActualTypeArguments()
  {
    return ((Type[])this.actualTypeArguments.clone());
  }

  public Class<?> getRawType()
  {
    return this.rawType;
  }

  public Type getOwnerType()
  {
    return this.ownerType;
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof ParameterizedType)
    {
      ParameterizedType localParameterizedType = (ParameterizedType)paramObject;
      if (this == localParameterizedType)
        return true;
      Type localType1 = localParameterizedType.getOwnerType();
      Type localType2 = localParameterizedType.getRawType();
      if (this.ownerType == null)
        if (localType1 != null)
          break label106;
      else
        if (!(this.ownerType.equals(localType1)))
          break label106;
      if (this.rawType == null)
        if (localType2 != null)
          break label106;
      label106: return ((this.rawType.equals(localType2)) && (Arrays.equals(this.actualTypeArguments, localParameterizedType.getActualTypeArguments())));
    }
    return false;
  }

  public int hashCode()
  {
    return (Arrays.hashCode(this.actualTypeArguments) ^ ((this.ownerType == null) ? 0 : this.ownerType.hashCode()) ^ ((this.rawType == null) ? 0 : this.rawType.hashCode()));
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    if (this.ownerType != null)
    {
      if (this.ownerType instanceof Class)
        localStringBuilder.append(((Class)this.ownerType).getName());
      else
        localStringBuilder.append(this.ownerType.toString());
      localStringBuilder.append(".");
      if (this.ownerType instanceof ParameterizedTypeImpl)
        localStringBuilder.append(this.rawType.getName().replace(((ParameterizedTypeImpl)this.ownerType).rawType.getName() + "$", ""));
      else
        localStringBuilder.append(this.rawType.getName());
    }
    else
    {
      localStringBuilder.append(this.rawType.getName());
    }
    if ((this.actualTypeArguments != null) && (this.actualTypeArguments.length > 0))
    {
      localStringBuilder.append("<");
      int i = 1;
      Type[] arrayOfType = this.actualTypeArguments;
      int j = arrayOfType.length;
      for (int k = 0; k < j; ++k)
      {
        Type localType = arrayOfType[k];
        if (i == 0)
          localStringBuilder.append(", ");
        if (localType instanceof Class)
          localStringBuilder.append(((Class)localType).getName());
        else
          localStringBuilder.append(localType.toString());
        i = 0;
      }
      localStringBuilder.append(">");
    }
    return localStringBuilder.toString();
  }
}