package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.visitor.Reifier;

public class TypeVariableImpl<D extends GenericDeclaration> extends LazyReflectiveObjectGenerator
  implements TypeVariable<D>
{
  D genericDeclaration;
  private String name;
  private Type[] bounds;
  private FieldTypeSignature[] boundASTs;

  private TypeVariableImpl(D paramD, String paramString, FieldTypeSignature[] paramArrayOfFieldTypeSignature, GenericsFactory paramGenericsFactory)
  {
    super(paramGenericsFactory);
    this.genericDeclaration = paramD;
    this.name = paramString;
    this.boundASTs = paramArrayOfFieldTypeSignature;
  }

  private FieldTypeSignature[] getBoundASTs()
  {
    if ((!($assertionsDisabled)) && (this.bounds != null))
      throw new AssertionError();
    return this.boundASTs;
  }

  public static <T extends GenericDeclaration> TypeVariableImpl<T> make(T paramT, String paramString, FieldTypeSignature[] paramArrayOfFieldTypeSignature, GenericsFactory paramGenericsFactory)
  {
    return new TypeVariableImpl(paramT, paramString, paramArrayOfFieldTypeSignature, paramGenericsFactory);
  }

  public Type[] getBounds()
  {
    if (this.bounds == null)
    {
      FieldTypeSignature[] arrayOfFieldTypeSignature = getBoundASTs();
      Type[] arrayOfType = new Type[arrayOfFieldTypeSignature.length];
      for (int i = 0; i < arrayOfFieldTypeSignature.length; ++i)
      {
        Reifier localReifier = getReifier();
        arrayOfFieldTypeSignature[i].accept(localReifier);
        arrayOfType[i] = localReifier.getResult();
      }
      this.bounds = arrayOfType;
    }
    return ((Type[])this.bounds.clone());
  }

  public D getGenericDeclaration()
  {
    return this.genericDeclaration;
  }

  public String getName()
  {
    return this.name;
  }

  public String toString()
  {
    return getName();
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof TypeVariable)
    {
      TypeVariable localTypeVariable = (TypeVariable)paramObject;
      GenericDeclaration localGenericDeclaration = localTypeVariable.getGenericDeclaration();
      String str = localTypeVariable.getName();
      if (this.genericDeclaration == null)
        if (localGenericDeclaration != null)
          break label83;
      else
        if (!(this.genericDeclaration.equals(localGenericDeclaration)))
          break label83;
      if (this.name == null)
        if (str != null)
          break label83;
      label83: return (this.name.equals(str));
    }
    return false;
  }

  public int hashCode()
  {
    return (this.genericDeclaration.hashCode() ^ this.name.hashCode());
  }
}