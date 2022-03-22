package sun.awt.image;

import sun.java2d.SurfaceData;

public class BufImgVolatileSurfaceManager extends VolatileSurfaceManager
{
  public BufImgVolatileSurfaceManager(SunVolatileImage paramSunVolatileImage, Object paramObject)
  {
    super(paramSunVolatileImage, paramObject);
  }

  protected boolean isAccelerationEnabled()
  {
    return false;
  }

  protected SurfaceData initAcceleratedSurface()
  {
    return null;
  }
}