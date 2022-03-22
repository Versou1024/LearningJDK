package sun.java2d.loops;

public class GraphicsPrimitiveProxy extends GraphicsPrimitive
{
  private Class owner;
  private String relativeClassName;

  public GraphicsPrimitiveProxy(Class paramClass, String paramString1, String paramString2, int paramInt, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramString2, paramInt, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    this.owner = paramClass;
    this.relativeClassName = paramString1;
  }

  public GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    throw new InternalError("makePrimitive called on a Proxy!");
  }

  GraphicsPrimitive instantiate()
  {
    String str = getPackageName(this.owner.getName()) + "." + this.relativeClassName;
    try
    {
      Class localClass = Class.forName(str);
      GraphicsPrimitive localGraphicsPrimitive = (GraphicsPrimitive)localClass.newInstance();
      if (!(satisfiesSameAs(localGraphicsPrimitive)))
        throw new RuntimeException("Primitive " + localGraphicsPrimitive + " incompatible with proxy for " + str);
      return localGraphicsPrimitive;
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new RuntimeException(localClassNotFoundException.toString());
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new RuntimeException(localInstantiationException.toString());
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new RuntimeException(localIllegalAccessException.toString());
    }
  }

  private static String getPackageName(String paramString)
  {
    int i = paramString.lastIndexOf(46);
    if (i < 0)
      return paramString;
    return paramString.substring(0, i);
  }

  public GraphicsPrimitive traceWrap()
  {
    return instantiate().traceWrap();
  }
}