package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

public class GenericArrayTypeImpl
  implements GenericArrayType
{
  private Type genericComponentType;

  private GenericArrayTypeImpl(Type paramType)
  {
    this.genericComponentType = paramType;
  }

  public static GenericArrayTypeImpl make(Type paramType)
  {
    return new GenericArrayTypeImpl(paramType);
  }

  public Type getGenericComponentType()
  {
    return this.genericComponentType;
  }

  public String toString()
  {
    Type localType = getGenericComponentType();
    StringBuilder localStringBuilder = new StringBuilder();
    if (localType instanceof Class)
      localStringBuilder.append(((Class)localType).getName());
    else
      localStringBuilder.append(localType.toString());
    localStringBuilder.append("[]");
    return localStringBuilder.toString();
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof GenericArrayType)
    {
      GenericArrayType localGenericArrayType = (GenericArrayType)paramObject;
      Type localType = localGenericArrayType.getGenericComponentType();
      return ((this.genericComponentType == null) ? false : (localType == null) ? true : this.genericComponentType.equals(localType));
    }
    return false;
  }

  public int hashCode()
  {
    return ((this.genericComponentType == null) ? 0 : this.genericComponentType.hashCode());
  }
}