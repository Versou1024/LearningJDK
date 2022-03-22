package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;

class SetDrawRectANY extends DrawRect
{
  SetDrawRectANY()
  {
    super(SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any);
  }

  public void DrawRect(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    PixelWriter localPixelWriter = GeneralRenderer.createSolidPixelWriter(paramSunGraphics2D, paramSurfaceData);
    GeneralRenderer.doDrawRect(localPixelWriter, paramSunGraphics2D, paramSurfaceData, paramInt1, paramInt2, paramInt3, paramInt4);
  }
}