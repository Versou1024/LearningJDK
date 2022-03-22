package sun.java2d.opengl;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import sun.awt.image.CachingSurfaceManager;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;

public class WGLCachingSurfaceManager extends CachingSurfaceManager
{
  private int transparency;

  public WGLCachingSurfaceManager(BufferedImage paramBufferedImage)
  {
    super(paramBufferedImage);
    this.transparency = paramBufferedImage.getColorModel().getTransparency();
    if ((accelerationThreshold == 0) && (this.localAccelerationEnabled))
    {
      GraphicsConfiguration localGraphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      initAcceleratedSurface(localGraphicsConfiguration, paramBufferedImage.getWidth(), paramBufferedImage.getHeight());
    }
  }

  protected boolean isDestSurfaceAccelerated(SurfaceData paramSurfaceData)
  {
    return paramSurfaceData instanceof WGLSurfaceData;
  }

  protected boolean isOperationSupported(SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean)
  {
    return ((paramSurfaceData instanceof WGLSurfaceData) && (((paramColor == null) || (this.transparency == 1))));
  }

  protected SurfaceData createAccelSurface(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2)
  {
    if (paramGraphicsConfiguration instanceof WGLGraphicsConfig)
      return WGLSurfaceData.createData((WGLGraphicsConfig)paramGraphicsConfiguration, paramInt1, paramInt2, paramGraphicsConfiguration.getColorModel(this.transparency), this.bImg, 3);
    return null;
  }

  public String toString()
  {
    return new String("WGLCachingSurfaceManager@" + Integer.toHexString(hashCode()) + " transparency: " + "TRANSLUCENT");
  }
}