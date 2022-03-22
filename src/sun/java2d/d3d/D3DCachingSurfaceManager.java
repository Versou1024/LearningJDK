package sun.java2d.d3d;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import sun.awt.image.CachingSurfaceManager;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;

public class D3DCachingSurfaceManager extends CachingSurfaceManager
{
  private int transparency;

  public D3DCachingSurfaceManager(BufferedImage paramBufferedImage)
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
    return paramSurfaceData instanceof D3DSurfaceData;
  }

  protected boolean isOperationSupported(SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean)
  {
    return ((paramSurfaceData instanceof D3DSurfaceData) && (((paramColor == null) || (this.transparency == 1))));
  }

  protected SurfaceData createAccelSurface(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2)
  {
    if (paramGraphicsConfiguration instanceof D3DGraphicsConfig)
      return D3DSurfaceData.createData((D3DGraphicsConfig)paramGraphicsConfiguration, paramInt1, paramInt2, paramGraphicsConfiguration.getColorModel(this.transparency), this.bImg, 3);
    return null;
  }

  public String toString()
  {
    return new String("D3DCachingSurfaceManager@" + Integer.toHexString(hashCode()) + " transparency: " + "TRANSLUCENT");
  }

  protected void restoreAcceleratedSurface()
  {
    if (this.sdAccel != null)
      ((D3DSurfaceData)this.sdAccel).restoreSurface();
  }
}