package sun.java2d.loops;

import java.awt.Composite;
import java.awt.image.BufferedImage;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;

public class MaskFill extends GraphicsPrimitive
{
  public static final String methodSignature = "MaskFill(...)".toString();
  public static final int primTypeID = makePrimTypeID();
  private static RenderCache fillcache = new RenderCache(10);

  public static MaskFill locate(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return ((MaskFill)GraphicsPrimitiveMgr.locate(primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2));
  }

  public static MaskFill locatePrim(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return ((MaskFill)GraphicsPrimitiveMgr.locatePrim(primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2));
  }

  public static MaskFill getFromCache(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    Object localObject = fillcache.get(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    if (localObject != null)
      return ((MaskFill)localObject);
    MaskFill localMaskFill = locatePrim(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    if (localMaskFill != null)
      fillcache.put(paramSurfaceType1, paramCompositeType, paramSurfaceType2, localMaskFill);
    return localMaskFill;
  }

  protected MaskFill(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public MaskFill(long paramLong, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramLong, methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public native void MaskFill(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, int paramInt5, int paramInt6);

  public GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    if ((SurfaceType.OpaqueColor.equals(paramSurfaceType1)) || (SurfaceType.AnyColor.equals(paramSurfaceType1)))
    {
      if (CompositeType.Xor.equals(paramCompositeType))
        throw new InternalError("Cannot construct MaskFill for XOR mode");
      return new General(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    }
    throw new InternalError("MaskFill can only fill with colors");
  }

  public GraphicsPrimitive traceWrap()
  {
    return new TraceMaskFill(this);
  }

  static
  {
    GraphicsPrimitiveMgr.registerGeneral(new MaskFill(null, null, null));
  }

  private static class General extends MaskFill
  {
    FillRect fillop;
    MaskBlit maskop;

    public General(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
    {
      super(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
      this.fillop = FillRect.locate(paramSurfaceType1, CompositeType.SrcNoEa, SurfaceType.IntArgb);
      this.maskop = MaskBlit.locate(SurfaceType.IntArgb, paramCompositeType, paramSurfaceType2);
    }

    public void MaskFill(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, int paramInt5, int paramInt6)
    {
      BufferedImage localBufferedImage = new BufferedImage(paramInt3, paramInt4, 2);
      SurfaceData localSurfaceData = BufImgSurfaceData.createData(localBufferedImage);
      int i = paramSunGraphics2D.pixel;
      paramSunGraphics2D.pixel = localSurfaceData.pixelFor(paramSunGraphics2D.getColor());
      this.fillop.FillRect(paramSunGraphics2D, localSurfaceData, 0, 0, paramInt3, paramInt4);
      paramSunGraphics2D.pixel = i;
      this.maskop.MaskBlit(localSurfaceData, paramSurfaceData, paramComposite, null, 0, 0, paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfByte, paramInt5, paramInt6);
    }
  }

  private static class TraceMaskFill extends MaskFill
  {
    MaskFill target;

    public TraceMaskFill(MaskFill paramMaskFill)
    {
      super(paramMaskFill.getSourceType(), paramMaskFill.getCompositeType(), paramMaskFill.getDestType());
      this.target = paramMaskFill;
    }

    public GraphicsPrimitive traceWrap()
    {
      return this;
    }

    public void MaskFill(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, int paramInt5, int paramInt6)
    {
      tracePrimitive(this.target);
      this.target.MaskFill(paramSunGraphics2D, paramSurfaceData, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfByte, paramInt5, paramInt6);
    }
  }
}