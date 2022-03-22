package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;

class XorDrawPolygonsANY extends DrawPolygons
{
  XorDrawPolygonsANY()
  {
    super(SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any);
  }

  public void DrawPolygons(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean)
  {
    PixelWriter localPixelWriter = GeneralRenderer.createXorPixelWriter(paramSunGraphics2D, paramSurfaceData);
    int i = 0;
    Region localRegion = paramSunGraphics2D.getCompClip();
    for (int j = 0; j < paramInt1; ++j)
    {
      int k = paramArrayOfInt3[j];
      GeneralRenderer.doDrawPoly(paramSurfaceData, localPixelWriter, paramArrayOfInt1, paramArrayOfInt2, i, k, localRegion, paramInt2, paramInt3, paramBoolean);
      i += k;
    }
  }
}