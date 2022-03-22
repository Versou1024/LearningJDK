package sun.reflect.generics.visitor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.tree.ArrayTypeSignature;
import sun.reflect.generics.tree.BooleanSignature;
import sun.reflect.generics.tree.BottomSignature;
import sun.reflect.generics.tree.ByteSignature;
import sun.reflect.generics.tree.CharSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.DoubleSignature;
import sun.reflect.generics.tree.FloatSignature;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.IntSignature;
import sun.reflect.generics.tree.LongSignature;
import sun.reflect.generics.tree.ShortSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeArgument;
import sun.reflect.generics.tree.TypeSignature;
import sun.reflect.generics.tree.TypeVariableSignature;
import sun.reflect.generics.tree.VoidDescriptor;
import sun.reflect.generics.tree.Wildcard;

public class Reifier
  implements TypeTreeVisitor<Type>
{
  private Type resultType;
  private GenericsFactory factory;

  private Reifier(GenericsFactory paramGenericsFactory)
  {
    this.factory = paramGenericsFactory;
  }

  private GenericsFactory getFactory()
  {
    return this.factory;
  }

  public static Reifier make(GenericsFactory paramGenericsFactory)
  {
    return new Reifier(paramGenericsFactory);
  }

  private Type[] reifyTypeArguments(TypeArgument[] paramArrayOfTypeArgument)
  {
    Type[] arrayOfType = new Type[paramArrayOfTypeArgument.length];
    for (int i = 0; i < paramArrayOfTypeArgument.length; ++i)
    {
      paramArrayOfTypeArgument[i].accept(this);
      arrayOfType[i] = this.resultType;
    }
    return arrayOfType;
  }

  public Type getResult()
  {
    if ((!($assertionsDisabled)) && (this.resultType == null))
      throw new AssertionError();
    return this.resultType;
  }

  public void visitFormalTypeParameter(FormalTypeParameter paramFormalTypeParameter)
  {
    this.resultType = getFactory().makeTypeVariable(paramFormalTypeParameter.getName(), paramFormalTypeParameter.getBounds());
  }

  public void visitClassTypeSignature(ClassTypeSignature paramClassTypeSignature)
  {
    List localList = paramClassTypeSignature.getPath();
    if ((!($assertionsDisabled)) && (localList.isEmpty()))
      throw new AssertionError();
    Iterator localIterator = localList.iterator();
    SimpleClassTypeSignature localSimpleClassTypeSignature = (SimpleClassTypeSignature)localIterator.next();
    StringBuilder localStringBuilder = new StringBuilder(localSimpleClassTypeSignature.getName());
    boolean bool = localSimpleClassTypeSignature.getDollar();
    while ((localIterator.hasNext()) && (localSimpleClassTypeSignature.getTypeArguments().length == 0))
    {
      localSimpleClassTypeSignature = (SimpleClassTypeSignature)localIterator.next();
      bool = localSimpleClassTypeSignature.getDollar();
      localStringBuilder.append((bool) ? "$" : ".").append(localSimpleClassTypeSignature.getName());
    }
    if ((!($assertionsDisabled)) && (localIterator.hasNext()) && (localSimpleClassTypeSignature.getTypeArguments().length <= 0))
      throw new AssertionError();
    Type localType = getFactory().makeNamedType(localStringBuilder.toString());
    if (localSimpleClassTypeSignature.getTypeArguments().length == 0)
    {
      if ((!($assertionsDisabled)) && (localIterator.hasNext()))
        throw new AssertionError();
      this.resultType = localType;
    }
    else
    {
      if ((!($assertionsDisabled)) && (localSimpleClassTypeSignature.getTypeArguments().length <= 0))
        throw new AssertionError();
      Type[] arrayOfType = reifyTypeArguments(localSimpleClassTypeSignature.getTypeArguments());
      ParameterizedType localParameterizedType = getFactory().makeParameterizedType(localType, arrayOfType, null);
      bool = false;
      while (localIterator.hasNext())
      {
        localSimpleClassTypeSignature = (SimpleClassTypeSignature)localIterator.next();
        bool = localSimpleClassTypeSignature.getDollar();
        localStringBuilder.append((bool) ? "$" : ".").append(localSimpleClassTypeSignature.getName());
        localType = getFactory().makeNamedType(localStringBuilder.toString());
        arrayOfType = reifyTypeArguments(localSimpleClassTypeSignature.getTypeArguments());
        localParameterizedType = getFactory().makeParameterizedType(localType, arrayOfType, localParameterizedType);
      }
      this.resultType = localParameterizedType;
    }
  }

  public void visitArrayTypeSignature(ArrayTypeSignature paramArrayTypeSignature)
  {
    paramArrayTypeSignature.getComponentType().accept(this);
    Type localType = this.resultType;
    this.resultType = getFactory().makeArrayType(localType);
  }

  public void visitTypeVariableSignature(TypeVariableSignature paramTypeVariableSignature)
  {
    this.resultType = getFactory().findTypeVariable(paramTypeVariableSignature.getIdentifier());
  }

  public void visitWildcard(Wildcard paramWildcard)
  {
    this.resultType = getFactory().makeWildcard(paramWildcard.getUpperBounds(), paramWildcard.getLowerBounds());
  }

  public void visitSimpleClassTypeSignature(SimpleClassTypeSignature paramSimpleClassTypeSignature)
  {
    this.resultType = getFactory().makeNamedType(paramSimpleClassTypeSignature.getName());
  }

  public void visitBottomSignature(BottomSignature paramBottomSignature)
  {
  }

  public void visitByteSignature(ByteSignature paramByteSignature)
  {
    this.resultType = getFactory().makeByte();
  }

  public void visitBooleanSignature(BooleanSignature paramBooleanSignature)
  {
    this.resultType = getFactory().makeBool();
  }

  public void visitShortSignature(ShortSignature paramShortSignature)
  {
    this.resultType = getFactory().makeShort();
  }

  public void visitCharSignature(CharSignature paramCharSignature)
  {
    this.resultType = getFactory().makeChar();
  }

  public void visitIntSignature(IntSignature paramIntSignature)
  {
    this.resultType = getFactory().makeInt();
  }

  public void visitLongSignature(LongSignature paramLongSignature)
  {
    this.resultType = getFactory().makeLong();
  }

  public void visitFloatSignature(FloatSignature paramFloatSignature)
  {
    this.resultType = getFactory().makeFloat();
  }

  public void visitDoubleSignature(DoubleSignature paramDoubleSignature)
  {
    this.resultType = getFactory().makeDouble();
  }

  public void visitVoidDescriptor(VoidDescriptor paramVoidDescriptor)
  {
    this.resultType = getFactory().makeVoid();
  }
}