package sun.java2d.loops;

import java.awt.Composite;
import java.awt.geom.AffineTransform;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;

public class TransformHelper extends GraphicsPrimitive
{
  public static final String methodSignature = "TransformHelper(...)".toString();
  public static final int primTypeID = makePrimTypeID();
  private static RenderCache helpercache = new RenderCache(10);

  public static TransformHelper locate(SurfaceType paramSurfaceType)
  {
    return ((TransformHelper)GraphicsPrimitiveMgr.locate(primTypeID, paramSurfaceType, CompositeType.SrcNoEa, SurfaceType.IntArgbPre));
  }

  public static synchronized TransformHelper getFromCache(SurfaceType paramSurfaceType)
  {
    Object localObject = helpercache.get(paramSurfaceType, null, null);
    if (localObject != null)
      return ((TransformHelper)localObject);
    TransformHelper localTransformHelper = locate(paramSurfaceType);
    if (localTransformHelper == null)
      break label41:
    helpercache.put(paramSurfaceType, null, null, localTransformHelper);
    label41: return localTransformHelper;
  }

  protected TransformHelper(SurfaceType paramSurfaceType)
  {
    super(methodSignature, primTypeID, paramSurfaceType, CompositeType.SrcNoEa, SurfaceType.IntArgbPre);
  }

  public TransformHelper(long paramLong, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramLong, methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public native void Transform(MaskBlit paramMaskBlit, SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, AffineTransform paramAffineTransform, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, int paramInt9, int[] paramArrayOfInt, int paramInt10, int paramInt11);

  public GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return null;
  }

  public GraphicsPrimitive traceWrap()
  {
    return new TraceTransformHelper(this);
  }

  private static class TraceTransformHelper extends TransformHelper
  {
    TransformHelper target;

    public TraceTransformHelper(TransformHelper paramTransformHelper)
    {
      super(paramTransformHelper.getSourceType());
      this.target = paramTransformHelper;
    }

    public GraphicsPrimitive traceWrap()
    {
      return this;
    }

    public void Transform(MaskBlit paramMaskBlit, SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, AffineTransform paramAffineTransform, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, int paramInt9, int[] paramArrayOfInt, int paramInt10, int paramInt11)
    {
      tracePrimitive(this.target);
      this.target.Transform(paramMaskBlit, paramSurfaceData1, paramSurfaceData2, paramComposite, paramRegion, paramAffineTransform, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, paramInt9, paramArrayOfInt, paramInt10, paramInt11);
    }
  }
}