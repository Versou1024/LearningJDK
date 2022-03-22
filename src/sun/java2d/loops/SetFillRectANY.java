package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;

class SetFillRectANY extends FillRect
{
  SetFillRectANY()
  {
    super(SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any);
  }

  public void FillRect(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    PixelWriter localPixelWriter = GeneralRenderer.createSolidPixelWriter(paramSunGraphics2D, paramSurfaceData);
    Region localRegion = paramSunGraphics2D.getCompClip().getBoundsIntersectionXYWH(paramInt1, paramInt2, paramInt3, paramInt4);
    GeneralRenderer.doSetRect(paramSurfaceData, localPixelWriter, localRegion.getLoX(), localRegion.getLoY(), localRegion.getHiX(), localRegion.getHiY());
  }
}