package sun.awt.image;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.image.BufferedImage;
import sun.java2d.SurfaceData;
import sun.java2d.SurfaceManagerFactory;
import sun.java2d.loops.CompositeType;

public abstract class SurfaceManager
{
  private static ImageAccessor imgaccessor;

  public static void setImageAccessor(ImageAccessor paramImageAccessor)
  {
    if (imgaccessor != null)
      throw new InternalError("Attempt to set ImageAccessor twice");
    imgaccessor = paramImageAccessor;
  }

  public static SurfaceManager getManager(Image paramImage)
  {
    SurfaceManager localSurfaceManager = imgaccessor.getSurfaceManager(paramImage);
    if (localSurfaceManager == null)
      try
      {
        BufferedImage localBufferedImage = (BufferedImage)paramImage;
        localSurfaceManager = SurfaceManagerFactory.createCachingManager(localBufferedImage);
        setManager(localBufferedImage, localSurfaceManager);
      }
      catch (ClassCastException localClassCastException)
      {
        throw new IllegalArgumentException("Invalid Image variant");
      }
    return localSurfaceManager;
  }

  public static void setManager(Image paramImage, SurfaceManager paramSurfaceManager)
  {
    imgaccessor.setSurfaceManager(paramImage, paramSurfaceManager);
  }

  public abstract SurfaceData getSourceSurfaceData(SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean);

  public abstract SurfaceData getDestSurfaceData();

  public abstract SurfaceData restoreContents();

  public void acceleratedSurfaceLost()
  {
  }

  public ImageCapabilities getCapabilities(GraphicsConfiguration paramGraphicsConfiguration)
  {
    return new ImageCapabilities(false);
  }

  public void flush()
  {
  }

  public void setAccelerationPriority(float paramFloat)
  {
  }

  public static abstract class ImageAccessor
  {
    public abstract SurfaceManager getSurfaceManager(Image paramImage);

    public abstract void setSurfaceManager(Image paramImage, SurfaceManager paramSurfaceManager);
  }
}