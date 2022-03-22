package sun.java2d.loops;

import java.awt.Composite;
import java.awt.geom.AffineTransform;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;

public class TransformBlit extends GraphicsPrimitive
{
  public static final String methodSignature = "TransformBlit(...)".toString();
  public static final int primTypeID = makePrimTypeID();
  private static RenderCache blitcache = new RenderCache(10);

  public static TransformBlit locate(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return ((TransformBlit)GraphicsPrimitiveMgr.locate(primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2));
  }

  public static TransformBlit getFromCache(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    Object localObject = blitcache.get(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    if (localObject != null)
      return ((TransformBlit)localObject);
    TransformBlit localTransformBlit = locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    if (localTransformBlit == null)
      break label46:
    blitcache.put(paramSurfaceType1, paramCompositeType, paramSurfaceType2, localTransformBlit);
    label46: return localTransformBlit;
  }

  protected TransformBlit(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public TransformBlit(long paramLong, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramLong, methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public native void Transform(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, AffineTransform paramAffineTransform, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7);

  public GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return null;
  }

  public GraphicsPrimitive traceWrap()
  {
    return new TraceTransformBlit(this);
  }

  static
  {
    GraphicsPrimitiveMgr.registerGeneral(new TransformBlit(null, null, null));
  }

  private static class TraceTransformBlit extends TransformBlit
  {
    TransformBlit target;

    public TraceTransformBlit(TransformBlit paramTransformBlit)
    {
      super(paramTransformBlit.getSourceType(), paramTransformBlit.getCompositeType(), paramTransformBlit.getDestType());
      this.target = paramTransformBlit;
    }

    public GraphicsPrimitive traceWrap()
    {
      return this;
    }

    public void Transform(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, AffineTransform paramAffineTransform, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
    {
      tracePrimitive(this.target);
      this.target.Transform(paramSurfaceData1, paramSurfaceData2, paramComposite, paramRegion, paramAffineTransform, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7);
    }
  }
}