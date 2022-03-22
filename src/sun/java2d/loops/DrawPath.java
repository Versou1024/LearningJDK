package sun.java2d.loops;

import java.awt.geom.Path2D.Float;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;

public class DrawPath extends GraphicsPrimitive
{
  public static final String methodSignature = "DrawPath(...)".toString();
  public static final int primTypeID = makePrimTypeID();

  public static DrawPath locate(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return ((DrawPath)GraphicsPrimitiveMgr.locate(primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2));
  }

  protected DrawPath(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public DrawPath(long paramLong, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramLong, methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public native void DrawPath(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int paramInt1, int paramInt2, Path2D.Float paramFloat);

  public GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    throw new InternalError("DrawPath not implemented for " + paramSurfaceType1 + " with " + paramCompositeType);
  }

  public GraphicsPrimitive traceWrap()
  {
    return new TraceDrawPath(this);
  }

  private static class TraceDrawPath extends DrawPath
  {
    DrawPath target;

    public TraceDrawPath(DrawPath paramDrawPath)
    {
      super(paramDrawPath.getSourceType(), paramDrawPath.getCompositeType(), paramDrawPath.getDestType());
      this.target = paramDrawPath;
    }

    public GraphicsPrimitive traceWrap()
    {
      return this;
    }

    public void DrawPath(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int paramInt1, int paramInt2, Path2D.Float paramFloat)
    {
      tracePrimitive(this.target);
      this.target.DrawPath(paramSunGraphics2D, paramSurfaceData, paramInt1, paramInt2, paramFloat);
    }
  }
}