package sun.java2d.loops;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.PrintStream;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;

public class BlitBg extends GraphicsPrimitive
{
  public static final String methodSignature = "BlitBg(...)".toString();
  public static final int primTypeID = makePrimTypeID();
  private static RenderCache blitcache = new RenderCache(20);

  public static BlitBg locate(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return ((BlitBg)GraphicsPrimitiveMgr.locate(primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2));
  }

  public static BlitBg getFromCache(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    Object localObject = blitcache.get(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    if (localObject != null)
      return ((BlitBg)localObject);
    BlitBg localBlitBg = locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    if (localBlitBg == null)
    {
      System.out.println("blitbg loop not found for:");
      System.out.println("src:  " + paramSurfaceType1);
      System.out.println("comp: " + paramCompositeType);
      System.out.println("dst:  " + paramSurfaceType2);
    }
    else
    {
      blitcache.put(paramSurfaceType1, paramCompositeType, paramSurfaceType2, localBlitBg);
    }
    return localBlitBg;
  }

  protected BlitBg(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public BlitBg(long paramLong, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramLong, methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public native void BlitBg(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7);

  public GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return new General(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public GraphicsPrimitive traceWrap()
  {
    return new TraceBlitBg(this);
  }

  static
  {
    GraphicsPrimitiveMgr.registerGeneral(new BlitBg(null, null, null));
  }

  private static class General extends BlitBg
  {
    CompositeType compositeType;
    private static Font defaultFont = new Font("Dialog", 0, 12);

    public General(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
    {
      super(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
      this.compositeType = paramCompositeType;
    }

    public void BlitBg(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
    {
      ColorModel localColorModel = paramSurfaceData2.getColorModel();
      boolean bool1 = paramInt1 >>> 24 != 255;
      if ((!(localColorModel.hasAlpha())) && (bool1))
        localColorModel = ColorModel.getRGBdefault();
      WritableRaster localWritableRaster = localColorModel.createCompatibleWritableRaster(paramInt6, paramInt7);
      boolean bool2 = localColorModel.isAlphaPremultiplied();
      BufferedImage localBufferedImage = new BufferedImage(localColorModel, localWritableRaster, bool2, null);
      SurfaceData localSurfaceData = BufImgSurfaceData.createData(localBufferedImage);
      Color localColor = new Color(paramInt1, bool1);
      SunGraphics2D localSunGraphics2D = new SunGraphics2D(localSurfaceData, localColor, localColor, defaultFont);
      FillRect localFillRect = FillRect.locate(SurfaceType.AnyColor, CompositeType.SrcNoEa, localSurfaceData.getSurfaceType());
      Blit localBlit1 = Blit.getFromCache(paramSurfaceData1.getSurfaceType(), CompositeType.SrcOverNoEa, localSurfaceData.getSurfaceType());
      Blit localBlit2 = Blit.getFromCache(localSurfaceData.getSurfaceType(), this.compositeType, paramSurfaceData2.getSurfaceType());
      localFillRect.FillRect(localSunGraphics2D, localSurfaceData, 0, 0, paramInt6, paramInt7);
      localBlit1.Blit(paramSurfaceData1, localSurfaceData, AlphaComposite.SrcOver, null, paramInt2, paramInt3, 0, 0, paramInt6, paramInt7);
      localBlit2.Blit(localSurfaceData, paramSurfaceData2, paramComposite, paramRegion, 0, 0, paramInt4, paramInt5, paramInt6, paramInt7);
    }
  }

  private static class TraceBlitBg extends BlitBg
  {
    BlitBg target;

    public TraceBlitBg(BlitBg paramBlitBg)
    {
      super(paramBlitBg.getSourceType(), paramBlitBg.getCompositeType(), paramBlitBg.getDestType());
      this.target = paramBlitBg;
    }

    public GraphicsPrimitive traceWrap()
    {
      return this;
    }

    public void BlitBg(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
    {
      tracePrimitive(this.target);
      this.target.BlitBg(paramSurfaceData1, paramSurfaceData2, paramComposite, paramRegion, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7);
    }
  }
}