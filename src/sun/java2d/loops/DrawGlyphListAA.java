package sun.java2d.loops;

import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;

public class DrawGlyphListAA extends GraphicsPrimitive
{
  public static final String methodSignature = "DrawGlyphListAA(...)".toString();
  public static final int primTypeID = makePrimTypeID();

  public static DrawGlyphListAA locate(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return ((DrawGlyphListAA)GraphicsPrimitiveMgr.locate(primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2));
  }

  protected DrawGlyphListAA(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public DrawGlyphListAA(long paramLong, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramLong, methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public native void DrawGlyphListAA(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, GlyphList paramGlyphList);

  public GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return new General(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public GraphicsPrimitive traceWrap()
  {
    return new TraceDrawGlyphListAA(this);
  }

  static
  {
    GraphicsPrimitiveMgr.registerGeneral(new DrawGlyphListAA(null, null, null));
  }

  public static class General extends DrawGlyphListAA
  {
    MaskFill maskop;

    public General(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
    {
      super(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
      this.maskop = MaskFill.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    }

    public void DrawGlyphListAA(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, GlyphList paramGlyphList)
    {
      paramGlyphList.getBounds();
      int i = paramGlyphList.getNumGlyphs();
      Region localRegion = paramSunGraphics2D.getCompClip();
      int j = localRegion.getLoX();
      int k = localRegion.getLoY();
      int l = localRegion.getHiX();
      int i1 = localRegion.getHiY();
      for (int i2 = 0; i2 < i; ++i2)
      {
        paramGlyphList.setGlyphIndex(i2);
        int[] arrayOfInt = paramGlyphList.getMetrics();
        int i3 = arrayOfInt[0];
        int i4 = arrayOfInt[1];
        int i5 = arrayOfInt[2];
        int i6 = i3 + i5;
        int i7 = i4 + arrayOfInt[3];
        int i8 = 0;
        if (i3 < j)
        {
          i8 = j - i3;
          i3 = j;
        }
        if (i4 < k)
        {
          i8 += (k - i4) * i5;
          i4 = k;
        }
        if (i6 > l)
          i6 = l;
        if (i7 > i1)
          i7 = i1;
        if ((i6 > i3) && (i7 > i4))
        {
          byte[] arrayOfByte = paramGlyphList.getGrayBits();
          this.maskop.MaskFill(paramSunGraphics2D, paramSurfaceData, paramSunGraphics2D.composite, i3, i4, i6 - i3, i7 - i4, arrayOfByte, i8, i5);
        }
      }
    }
  }

  private static class TraceDrawGlyphListAA extends DrawGlyphListAA
  {
    DrawGlyphListAA target;

    public TraceDrawGlyphListAA(DrawGlyphListAA paramDrawGlyphListAA)
    {
      super(paramDrawGlyphListAA.getSourceType(), paramDrawGlyphListAA.getCompositeType(), paramDrawGlyphListAA.getDestType());
      this.target = paramDrawGlyphListAA;
    }

    public GraphicsPrimitive traceWrap()
    {
      return this;
    }

    public void DrawGlyphListAA(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, GlyphList paramGlyphList)
    {
      tracePrimitive(this.target);
      this.target.DrawGlyphListAA(paramSunGraphics2D, paramSurfaceData, paramGlyphList);
    }
  }
}