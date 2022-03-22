package sun.awt.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.security.AccessController;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import sun.awt.DisplayChangedListener;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.BlitBg;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.security.action.GetPropertyAction;

public abstract class CachingSurfaceManager extends SurfaceManager
  implements RasterListener, DisplayChangedListener
{
  protected BufferedImage bImg;
  protected SurfaceData sdDefault;
  protected SurfaceData sdAccel;
  protected HashMap<Object, SurfaceData> accelSurfaces;
  protected boolean localAccelerationEnabled = false;
  protected static boolean accelerationEnabled = true;
  protected static boolean allowRasterSteal = false;
  protected static int accelerationThreshold = 1;

  public CachingSurfaceManager(BufferedImage paramBufferedImage)
  {
    this.bImg = paramBufferedImage;
    this.sdDefault = BufImgSurfaceData.createData(paramBufferedImage);
    if ((accelerationEnabled) && (paramBufferedImage.getAccelerationPriority() > 0F))
    {
      localObject = paramBufferedImage.getRaster();
      if (localObject instanceof SunWritableRaster)
      {
        this.localAccelerationEnabled = true;
        ((SunWritableRaster)localObject).setRasterListener(this);
        if (this.localAccelerationEnabled)
          this.accelSurfaces = new HashMap(2);
      }
    }
    Object localObject = GraphicsEnvironment.getLocalGraphicsEnvironment();
    if ((accelerationEnabled) && (this.localAccelerationEnabled) && (localObject instanceof SunGraphicsEnvironment))
      ((SunGraphicsEnvironment)localObject).addDisplayChangedListener(this);
  }

  public SurfaceData getSourceSurfaceData(SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean)
  {
    if ((this.localAccelerationEnabled) && (paramSurfaceData != this.sdAccel) && (isDestSurfaceAccelerated(paramSurfaceData)) && (isOperationSupported(paramSurfaceData, paramCompositeType, paramColor, paramBoolean)) && (this.sdDefault.increaseNumCopies() > accelerationThreshold))
    {
      validate(paramSurfaceData.getDeviceConfiguration());
      if ((this.sdAccel != null) && (!(this.sdAccel.isSurfaceLost())))
        return this.sdAccel;
    }
    return this.sdDefault;
  }

  public SurfaceData getDestSurfaceData()
  {
    return this.sdDefault;
  }

  protected abstract boolean isDestSurfaceAccelerated(SurfaceData paramSurfaceData);

  protected boolean isValidAccelSurface(GraphicsConfiguration paramGraphicsConfiguration)
  {
    return (getAccelSurface(paramGraphicsConfiguration) != null);
  }

  protected SurfaceData getAccelSurface(GraphicsConfiguration paramGraphicsConfiguration)
  {
    return ((this.accelSurfaces != null) ? (SurfaceData)this.accelSurfaces.get(paramGraphicsConfiguration) : null);
  }

  protected abstract boolean isOperationSupported(SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean);

  protected abstract SurfaceData createAccelSurface(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2);

  protected void initAcceleratedSurface(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2)
  {
    try
    {
      this.sdAccel = getAccelSurface(paramGraphicsConfiguration);
      if (this.sdAccel == null)
      {
        this.sdAccel = createAccelSurface(paramGraphicsConfiguration, paramInt1, paramInt2);
        if (this.sdAccel != null)
          this.accelSurfaces.put(paramGraphicsConfiguration, this.sdAccel);
      }
    }
    catch (NullPointerException localNullPointerException)
    {
      this.sdAccel = null;
    }
    catch (OutOfMemoryError localOutOfMemoryError)
    {
      this.sdAccel = null;
    }
    catch (InvalidPipeException localInvalidPipeException)
    {
      this.sdAccel = null;
    }
  }

  protected Color getTransparentPixelColor()
  {
    return null;
  }

  protected void copyDefaultToAccelerated()
  {
    if (this.accelSurfaces != null)
    {
      Collection localCollection = this.accelSurfaces.values();
      Iterator localIterator = localCollection.iterator();
      while (localIterator.hasNext())
      {
        SurfaceData localSurfaceData = (SurfaceData)localIterator.next();
        try
        {
          if ((localSurfaceData != this.sdDefault) && (!(localSurfaceData.isSurfaceLost())))
          {
            Object localObject;
            SurfaceType localSurfaceType1 = this.sdDefault.getSurfaceType();
            SurfaceType localSurfaceType2 = localSurfaceData.getSurfaceType();
            Color localColor = getTransparentPixelColor();
            if (localColor == null)
            {
              localObject = Blit.getFromCache(localSurfaceType1, CompositeType.SrcNoEa, localSurfaceType2);
              ((Blit)localObject).Blit(this.sdDefault, localSurfaceData, AlphaComposite.Src, null, 0, 0, 0, 0, this.bImg.getWidth(), this.bImg.getHeight());
            }
            else
            {
              localObject = BlitBg.getFromCache(localSurfaceType1, CompositeType.SrcNoEa, localSurfaceType2);
              ((BlitBg)localObject).BlitBg(this.sdDefault, localSurfaceData, AlphaComposite.SrcOver, null, localColor.getRGB(), 0, 0, 0, 0, this.bImg.getWidth(), this.bImg.getHeight());
            }
          }
        }
        catch (Exception localException)
        {
          if (localSurfaceData != null)
            localSurfaceData.setSurfaceLost(true);
        }
      }
      this.sdDefault.setNeedsBackup(false);
    }
  }

  public void validate(GraphicsConfiguration paramGraphicsConfiguration)
  {
    if (this.localAccelerationEnabled)
    {
      int i = 0;
      this.sdAccel = getAccelSurface(paramGraphicsConfiguration);
      if (this.sdAccel == null)
      {
        initAcceleratedSurface(paramGraphicsConfiguration, this.bImg.getWidth(), this.bImg.getHeight());
        if (this.sdAccel != null)
        {
          i = 1;
          break label90:
        }
        return;
      }
      if (this.sdAccel.isSurfaceLost())
        try
        {
          restoreAcceleratedSurface();
          i = 1;
          this.sdAccel.setSurfaceLost(false);
        }
        catch (InvalidPipeException localInvalidPipeException)
        {
          flush();
          return;
        }
      if ((this.sdDefault.needsBackup()) || (i != 0))
        label90: copyDefaultToAccelerated();
    }
  }

  public void rasterChanged()
  {
    this.sdDefault.setNeedsBackup(true);
  }

  public void rasterStolen()
  {
    if (!(allowRasterSteal))
      this.localAccelerationEnabled = false;
  }

  public SurfaceData restoreContents()
  {
    return this.sdDefault;
  }

  protected void restoreAcceleratedSurface()
  {
  }

  public void displayChanged()
  {
    if (!(accelerationEnabled))
      return;
    invalidateAcceleratedSurfaces();
  }

  public void paletteChanged()
  {
    this.sdDefault.setNeedsBackup(true);
  }

  public void setAccelerationPriority(float paramFloat)
  {
    if (paramFloat == 0F)
      setLocalAccelerationEnabled(false);
  }

  public boolean isLocalAccelerationEnabled()
  {
    return this.localAccelerationEnabled;
  }

  public void setLocalAccelerationEnabled(boolean paramBoolean)
  {
    this.localAccelerationEnabled = paramBoolean;
    if (!(this.localAccelerationEnabled))
      flush();
    else if (this.accelSurfaces == null)
      this.accelSurfaces = new HashMap(2);
  }

  public static void restoreLocalAcceleration(Image paramImage)
  {
    if (paramImage instanceof BufferedImage)
    {
      localObject = ((BufferedImage)paramImage).getRaster();
      if (localObject instanceof SunWritableRaster)
        ((SunWritableRaster)localObject).setStolen(false);
    }
    Object localObject = SurfaceManager.getManager(paramImage);
    if ((paramImage.getAccelerationPriority() > 0F) && (localObject instanceof CachingSurfaceManager))
      ((CachingSurfaceManager)localObject).setLocalAccelerationEnabled(true);
  }

  public ImageCapabilities getCapabilities(GraphicsConfiguration paramGraphicsConfiguration)
  {
    return new ImageCapabilitiesGc(this, paramGraphicsConfiguration);
  }

  public synchronized void flush()
  {
    this.sdAccel = null;
    if (this.accelSurfaces != null)
    {
      HashMap localHashMap = this.accelSurfaces;
      this.accelSurfaces = ((this.localAccelerationEnabled) ? new HashMap(2) : null);
      Collection localCollection = localHashMap.values();
      Iterator localIterator = localCollection.iterator();
      while (localIterator.hasNext())
      {
        SurfaceData localSurfaceData = (SurfaceData)localIterator.next();
        localSurfaceData.flush();
      }
    }
  }

  public synchronized void invalidateAcceleratedSurfaces()
  {
    this.sdAccel = null;
    if (this.accelSurfaces != null)
    {
      HashMap localHashMap = this.accelSurfaces;
      this.accelSurfaces = ((this.localAccelerationEnabled) ? new HashMap(2) : null);
      Collection localCollection = localHashMap.values();
      Iterator localIterator = localCollection.iterator();
      while (localIterator.hasNext())
      {
        SurfaceData localSurfaceData = (SurfaceData)localIterator.next();
        localSurfaceData.invalidate();
      }
    }
  }

  static
  {
    String str1 = (String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.managedimages"));
    if ((str1 != null) && (str1.equals("false")))
    {
      accelerationEnabled = false;
      System.out.println("Disabling managed images");
    }
    String str2 = (String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.accthreshold"));
    if (str2 != null)
      try
      {
        int i = Integer.parseInt(str2);
        if (i >= 0)
        {
          accelerationThreshold = i;
          System.out.println("New Acceleration Threshold: " + accelerationThreshold);
        }
      }
      catch (NumberFormatException localNumberFormatException)
      {
        System.err.println("Error setting new threshold:" + localNumberFormatException);
      }
    String str3 = (String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.allowrastersteal"));
    if ((str3 != null) && (str3.equals("true")))
    {
      allowRasterSteal = true;
      System.out.println("Raster steal allowed");
    }
  }

  class ImageCapabilitiesGc extends ImageCapabilities
  {
    GraphicsConfiguration gc;

    public ImageCapabilitiesGc(, GraphicsConfiguration paramGraphicsConfiguration)
    {
      super(false);
      this.gc = paramGraphicsConfiguration;
    }

    public boolean isAccelerated()
    {
      GraphicsConfiguration localGraphicsConfiguration = this.gc;
      if (localGraphicsConfiguration == null)
        localGraphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      return this.this$0.isValidAccelSurface(localGraphicsConfiguration);
    }
  }
}