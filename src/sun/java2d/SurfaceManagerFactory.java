package sun.java2d;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import sun.awt.image.BufImgVolatileSurfaceManager;
import sun.awt.image.CachingSurfaceManager;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.SurfaceManager;
import sun.awt.image.VolatileSurfaceManager;
import sun.java2d.d3d.D3DCachingSurfaceManager;
import sun.java2d.d3d.D3DGraphicsConfig;
import sun.java2d.d3d.D3DGraphicsDevice;
import sun.java2d.d3d.D3DVolatileSurfaceManager;
import sun.java2d.loops.CompositeType;
import sun.java2d.opengl.WGLCachingSurfaceManager;
import sun.java2d.opengl.WGLGraphicsConfig;
import sun.java2d.opengl.WGLVolatileSurfaceManager;
import sun.java2d.windows.WindowsFlags;

public class SurfaceManagerFactory
{
  public static SurfaceManager createCachingManager(BufferedImage paramBufferedImage)
  {
    if (WindowsFlags.isOGLEnabled())
      return new WGLCachingSurfaceManager(paramBufferedImage);
    if (D3DGraphicsDevice.isD3DAvailable())
      return new D3DCachingSurfaceManager(paramBufferedImage);
    return new CachingSurfaceManager(paramBufferedImage)
    {
      protected SurfaceData createAccelSurface(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2)
      {
        return null;
      }

      protected boolean isDestSurfaceAccelerated(SurfaceData paramSurfaceData)
      {
        return false;
      }

      protected boolean isOperationSupported(SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean)
      {
        return false;
      }
    };
  }

  public static VolatileSurfaceManager createVolatileManager(SunVolatileImage paramSunVolatileImage, Object paramObject)
  {
    GraphicsConfiguration localGraphicsConfiguration = paramSunVolatileImage.getGraphicsConfig();
    if (localGraphicsConfiguration instanceof WGLGraphicsConfig)
      return new WGLVolatileSurfaceManager(paramSunVolatileImage, paramObject);
    if (localGraphicsConfiguration instanceof D3DGraphicsConfig)
      return new D3DVolatileSurfaceManager(paramSunVolatileImage, paramObject);
    return new BufImgVolatileSurfaceManager(paramSunVolatileImage, paramObject);
  }
}