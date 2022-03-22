package sun.java2d.loops;

import java.awt.geom.Path2D.Float;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;

class SetFillPathANY extends FillPath
{
  SetFillPathANY()
  {
    super(SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any);
  }

  public void FillPath(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int paramInt1, int paramInt2, Path2D.Float paramFloat)
  {
    PixelWriter localPixelWriter = GeneralRenderer.createSolidPixelWriter(paramSunGraphics2D, paramSurfaceData);
    ProcessPath.fillPath(new PixelWriterDrawHandler(paramSurfaceData, localPixelWriter, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.strokeHint), paramFloat, paramInt1, paramInt2);
  }
}