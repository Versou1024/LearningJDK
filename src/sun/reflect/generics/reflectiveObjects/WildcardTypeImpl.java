package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.visitor.Reifier;

public class WildcardTypeImpl extends LazyReflectiveObjectGenerator
  implements WildcardType
{
  private Type[] upperBounds;
  private Type[] lowerBounds;
  private FieldTypeSignature[] upperBoundASTs;
  private FieldTypeSignature[] lowerBoundASTs;

  private WildcardTypeImpl(FieldTypeSignature[] paramArrayOfFieldTypeSignature1, FieldTypeSignature[] paramArrayOfFieldTypeSignature2, GenericsFactory paramGenericsFactory)
  {
    super(paramGenericsFactory);
    this.upperBoundASTs = paramArrayOfFieldTypeSignature1;
    this.lowerBoundASTs = paramArrayOfFieldTypeSignature2;
  }

  public static WildcardTypeImpl make(FieldTypeSignature[] paramArrayOfFieldTypeSignature1, FieldTypeSignature[] paramArrayOfFieldTypeSignature2, GenericsFactory paramGenericsFactory)
  {
    return new WildcardTypeImpl(paramArrayOfFieldTypeSignature1, paramArrayOfFieldTypeSignature2, paramGenericsFactory);
  }

  private FieldTypeSignature[] getUpperBoundASTs()
  {
    if ((!($assertionsDisabled)) && (this.upperBounds != null))
      throw new AssertionError();
    return this.upperBoundASTs;
  }

  private FieldTypeSignature[] getLowerBoundASTs()
  {
    if ((!($assertionsDisabled)) && (this.lowerBounds != null))
      throw new AssertionError();
    return this.lowerBoundASTs;
  }

  public Type[] getUpperBounds()
  {
    if (this.upperBounds == null)
    {
      FieldTypeSignature[] arrayOfFieldTypeSignature = getUpperBoundASTs();
      Type[] arrayOfType = new Type[arrayOfFieldTypeSignature.length];
      for (int i = 0; i < arrayOfFieldTypeSignature.length; ++i)
      {
        Reifier localReifier = getReifier();
        arrayOfFieldTypeSignature[i].accept(localReifier);
        arrayOfType[i] = localReifier.getResult();
      }
      this.upperBounds = arrayOfType;
    }
    return ((Type[])this.upperBounds.clone());
  }

  public Type[] getLowerBounds()
  {
    if (this.lowerBounds == null)
    {
      FieldTypeSignature[] arrayOfFieldTypeSignature = getLowerBoundASTs();
      Type[] arrayOfType = new Type[arrayOfFieldTypeSignature.length];
      for (int i = 0; i < arrayOfFieldTypeSignature.length; ++i)
      {
        Reifier localReifier = getReifier();
        arrayOfFieldTypeSignature[i].accept(localReifier);
        arrayOfType[i] = localReifier.getResult();
      }
      this.lowerBounds = arrayOfType;
    }
    return ((Type[])this.lowerBounds.clone());
  }

  public String toString()
  {
    Type[] arrayOfType1 = getLowerBounds();
    Object localObject1 = arrayOfType1;
    StringBuilder localStringBuilder = new StringBuilder();
    if (arrayOfType1.length > 0)
    {
      localStringBuilder.append("? super ");
    }
    else
    {
      Type[] arrayOfType2 = getUpperBounds();
      if ((arrayOfType2.length > 0) && (!(arrayOfType2[0].equals(Object.class))))
      {
        localObject1 = arrayOfType2;
        localStringBuilder.append("? extends ");
      }
      else
      {
        return "?";
      }
    }
    if ((!($assertionsDisabled)) && (localObject1.length <= 0))
      throw new AssertionError();
    int i = 1;
    Object localObject2 = localObject1;
    int j = localObject2.length;
    for (int k = 0; k < j; ++k)
    {
      Object localObject3 = localObject2[k];
      if (i == 0)
        localStringBuilder.append(" & ");
      i = 0;
      if (localObject3 instanceof Class)
        localStringBuilder.append(((Class)localObject3).getName());
      else
        localStringBuilder.append(localObject3.toString());
    }
    return ((String)localStringBuilder.toString());
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof WildcardType)
    {
      WildcardType localWildcardType = (WildcardType)paramObject;
      return ((Arrays.equals(getLowerBounds(), localWildcardType.getLowerBounds())) && (Arrays.equals(getUpperBounds(), localWildcardType.getUpperBounds())));
    }
    return false;
  }

  public int hashCode()
  {
    Type[] arrayOfType1 = getLowerBounds();
    Type[] arrayOfType2 = getUpperBounds();
    return (Arrays.hashCode(arrayOfType1) ^ Arrays.hashCode(arrayOfType2));
  }
}