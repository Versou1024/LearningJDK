package sun.java2d.loops;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Float;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import sun.awt.SunHints;
import sun.java2d.SunGraphics2D;

public final class GraphicsPrimitiveMgr
{
  private static final boolean debugTrace = 0;
  private static GraphicsPrimitive[] primitives;
  private static GraphicsPrimitive[] generalPrimitives;
  private static boolean needssort = true;
  private static Comparator primSorter;
  private static Comparator primFinder;

  private static native void initIDs(Class paramClass1, Class paramClass2, Class paramClass3, Class paramClass4, Class paramClass5, Class paramClass6, Class paramClass7, Class paramClass8, Class paramClass9, Class paramClass10, Class paramClass11);

  private static native void registerNativeLoops();

  public static synchronized void register(GraphicsPrimitive[] paramArrayOfGraphicsPrimitive)
  {
    GraphicsPrimitive[] arrayOfGraphicsPrimitive1 = primitives;
    int i = 0;
    int j = paramArrayOfGraphicsPrimitive.length;
    if (arrayOfGraphicsPrimitive1 != null)
      i = arrayOfGraphicsPrimitive1.length;
    GraphicsPrimitive[] arrayOfGraphicsPrimitive2 = new GraphicsPrimitive[i + j];
    if (arrayOfGraphicsPrimitive1 != null)
      System.arraycopy(arrayOfGraphicsPrimitive1, 0, arrayOfGraphicsPrimitive2, 0, i);
    System.arraycopy(paramArrayOfGraphicsPrimitive, 0, arrayOfGraphicsPrimitive2, i, j);
    needssort = true;
    primitives = arrayOfGraphicsPrimitive2;
  }

  public static synchronized void registerGeneral(GraphicsPrimitive paramGraphicsPrimitive)
  {
    if (generalPrimitives == null)
    {
      generalPrimitives = { paramGraphicsPrimitive };
      return;
    }
    int i = generalPrimitives.length;
    GraphicsPrimitive[] arrayOfGraphicsPrimitive = new GraphicsPrimitive[i + 1];
    System.arraycopy(generalPrimitives, 0, arrayOfGraphicsPrimitive, 0, i);
    arrayOfGraphicsPrimitive[i] = paramGraphicsPrimitive;
    generalPrimitives = arrayOfGraphicsPrimitive;
  }

  public static synchronized GraphicsPrimitive locate(int paramInt, SurfaceType paramSurfaceType)
  {
    return locate(paramInt, SurfaceType.OpaqueColor, CompositeType.Src, paramSurfaceType);
  }

  public static synchronized GraphicsPrimitive locate(int paramInt, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    GraphicsPrimitive localGraphicsPrimitive = locatePrim(paramInt, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    if (localGraphicsPrimitive == null)
    {
      localGraphicsPrimitive = locateGeneral(paramInt);
      if (localGraphicsPrimitive != null)
      {
        localGraphicsPrimitive = localGraphicsPrimitive.makePrimitive(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
        if ((localGraphicsPrimitive != null) && (GraphicsPrimitive.traceflags != 0))
          localGraphicsPrimitive = localGraphicsPrimitive.traceWrap();
      }
    }
    return localGraphicsPrimitive;
  }

  public static synchronized GraphicsPrimitive locatePrim(int paramInt, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    PrimitiveSpec localPrimitiveSpec = new PrimitiveSpec(null);
    for (SurfaceType localSurfaceType2 = paramSurfaceType2; localSurfaceType2 != null; localSurfaceType2 = localSurfaceType2.getSuperType())
      for (SurfaceType localSurfaceType1 = paramSurfaceType1; localSurfaceType1 != null; localSurfaceType1 = localSurfaceType1.getSuperType())
        for (CompositeType localCompositeType = paramCompositeType; localCompositeType != null; localCompositeType = localCompositeType.getSuperType())
        {
          localPrimitiveSpec.uniqueID = GraphicsPrimitive.makeUniqueID(paramInt, localSurfaceType1, localCompositeType, localSurfaceType2);
          GraphicsPrimitive localGraphicsPrimitive = locate(localPrimitiveSpec);
          if (localGraphicsPrimitive != null)
            return localGraphicsPrimitive;
        }
    return null;
  }

  private static GraphicsPrimitive locateGeneral(int paramInt)
  {
    if (generalPrimitives == null)
      return null;
    for (int i = 0; i < generalPrimitives.length; ++i)
    {
      GraphicsPrimitive localGraphicsPrimitive = generalPrimitives[i];
      if (localGraphicsPrimitive.getPrimTypeID() == paramInt)
        return localGraphicsPrimitive;
    }
    return null;
  }

  private static GraphicsPrimitive locate(PrimitiveSpec paramPrimitiveSpec)
  {
    if (needssort)
    {
      if (GraphicsPrimitive.traceflags != 0)
        for (int i = 0; i < primitives.length; ++i)
          primitives[i] = primitives[i].traceWrap();
      Arrays.sort(primitives, primSorter);
      needssort = false;
    }
    GraphicsPrimitive[] arrayOfGraphicsPrimitive = primitives;
    if (arrayOfGraphicsPrimitive == null)
      return null;
    int j = Arrays.binarySearch(arrayOfGraphicsPrimitive, paramPrimitiveSpec, primFinder);
    if (j >= 0)
    {
      GraphicsPrimitive localGraphicsPrimitive = arrayOfGraphicsPrimitive[j];
      if (localGraphicsPrimitive instanceof GraphicsPrimitiveProxy)
      {
        localGraphicsPrimitive = ((GraphicsPrimitiveProxy)localGraphicsPrimitive).instantiate();
        arrayOfGraphicsPrimitive[j] = localGraphicsPrimitive;
      }
      return localGraphicsPrimitive;
    }
    return null;
  }

  private static void writeLog(String paramString)
  {
  }

  public static void testPrimitiveInstantiation()
  {
    testPrimitiveInstantiation(false);
  }

  public static void testPrimitiveInstantiation(boolean paramBoolean)
  {
    int i = 0;
    int j = 0;
    GraphicsPrimitive[] arrayOfGraphicsPrimitive = primitives;
    for (int k = 0; k < arrayOfGraphicsPrimitive.length; ++k)
    {
      Object localObject = arrayOfGraphicsPrimitive[k];
      if (localObject instanceof GraphicsPrimitiveProxy)
      {
        GraphicsPrimitive localGraphicsPrimitive = ((GraphicsPrimitiveProxy)localObject).instantiate();
        if ((!(localGraphicsPrimitive.getSignature().equals(((GraphicsPrimitive)localObject).getSignature()))) || (localGraphicsPrimitive.getUniqueID() != ((GraphicsPrimitive)localObject).getUniqueID()))
        {
          System.out.println("r.getSignature == " + localGraphicsPrimitive.getSignature());
          System.out.println("r.getUniqueID == " + localGraphicsPrimitive.getUniqueID());
          System.out.println("p.getSignature == " + ((GraphicsPrimitive)localObject).getSignature());
          System.out.println("p.getUniqueID == " + ((GraphicsPrimitive)localObject).getUniqueID());
          throw new RuntimeException("Primitive " + localObject + " returns wrong signature for " + localGraphicsPrimitive.getClass());
        }
        ++j;
        localObject = localGraphicsPrimitive;
        if (paramBoolean)
          System.out.println(localObject);
      }
      else
      {
        if (paramBoolean)
          System.out.println(localObject + " (not proxied).");
        ++i;
      }
    }
    System.out.println(i + " graphics primitives were not proxied.");
    System.out.println(j + " proxied graphics primitives resolved correctly.");
    System.out.println((i + j) + " total graphics primitives");
  }

  public static void main(String[] paramArrayOfString)
  {
    if (needssort)
    {
      Arrays.sort(primitives, primSorter);
      needssort = false;
    }
    testPrimitiveInstantiation(paramArrayOfString.length > 0);
  }

  static
  {
    initIDs(GraphicsPrimitive.class, SurfaceType.class, CompositeType.class, SunGraphics2D.class, Color.class, AffineTransform.class, XORComposite.class, AlphaComposite.class, Path2D.class, Path2D.Float.class, SunHints.class);
    CustomComponent.register();
    GeneralRenderer.register();
    registerNativeLoops();
    primSorter = new Comparator()
    {
      public int compare(Object paramObject1, Object paramObject2)
      {
        int i = ((GraphicsPrimitive)paramObject1).getUniqueID();
        int j = ((GraphicsPrimitive)paramObject2).getUniqueID();
        return ((i < j) ? -1 : (i == j) ? 0 : 1);
      }
    };
    primFinder = new Comparator()
    {
      public int compare(Object paramObject1, Object paramObject2)
      {
        int i = ((GraphicsPrimitive)paramObject1).getUniqueID();
        int j = ((GraphicsPrimitiveMgr.PrimitiveSpec)paramObject2).uniqueID;
        return ((i < j) ? -1 : (i == j) ? 0 : 1);
      }
    };
  }

  private static class PrimitiveSpec
  {
    public int uniqueID;
  }
}