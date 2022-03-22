package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;

public class FillRect extends GraphicsPrimitive
{
  public static final String methodSignature = "FillRect(...)".toString();
  public static final int primTypeID = makePrimTypeID();

  public static FillRect locate(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return ((FillRect)GraphicsPrimitiveMgr.locate(primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2));
  }

  protected FillRect(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public FillRect(long paramLong, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramLong, methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public native void FillRect(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return new General(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public GraphicsPrimitive traceWrap()
  {
    return new TraceFillRect(this);
  }

  static
  {
    GraphicsPrimitiveMgr.registerGeneral(new FillRect(null, null, null));
  }

  public static class General extends FillRect
  {
    public MaskFill fillop;

    public General(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
    {
      super(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
      this.fillop = MaskFill.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    }

    public void FillRect(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      this.fillop.MaskFill(paramSunGraphics2D, paramSurfaceData, paramSunGraphics2D.composite, paramInt1, paramInt2, paramInt3, paramInt4, null, 0, 0);
    }
  }

  private static class TraceFillRect extends FillRect
  {
    FillRect target;

    public TraceFillRect(FillRect paramFillRect)
    {
      super(paramFillRect.getSourceType(), paramFillRect.getCompositeType(), paramFillRect.getDestType());
      this.target = paramFillRect;
    }

    public GraphicsPrimitive traceWrap()
    {
      return this;
    }

    public void FillRect(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      tracePrimitive(this.target);
      this.target.FillRect(paramSunGraphics2D, paramSurfaceData, paramInt1, paramInt2, paramInt3, paramInt4);
    }
  }
}