package sun.awt.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.ImageCapabilities;
import java.awt.image.BufferedImage;
import sun.awt.DisplayChangedListener;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;

public abstract class VolatileSurfaceManager extends SurfaceManager
  implements DisplayChangedListener
{
  protected SunVolatileImage vImg;
  protected SurfaceData sdAccel;
  protected SurfaceData sdBackup;
  protected SurfaceData sdCurrent;
  protected SurfaceData sdPrevious;
  protected boolean lostSurface;
  protected Object context;

  protected VolatileSurfaceManager(SunVolatileImage paramSunVolatileImage, Object paramObject)
  {
    this.vImg = paramSunVolatileImage;
    this.context = paramObject;
    GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    if (localGraphicsEnvironment instanceof SunGraphicsEnvironment)
      ((SunGraphicsEnvironment)localGraphicsEnvironment).addDisplayChangedListener(this);
  }

  public void initialize()
  {
    if (isAccelerationEnabled())
    {
      this.sdAccel = initAcceleratedSurface();
      if (this.sdAccel != null)
        this.sdCurrent = this.sdAccel;
    }
    if ((this.sdCurrent == null) && (this.vImg.getForcedAccelSurfaceType() == 0))
      this.sdCurrent = getBackupSurface();
  }

  public SurfaceData getSourceSurfaceData(SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean)
  {
    return this.sdCurrent;
  }

  public SurfaceData getDestSurfaceData()
  {
    return this.sdCurrent;
  }

  protected abstract boolean isAccelerationEnabled();

  public int validate(GraphicsConfiguration paramGraphicsConfiguration)
  {
    int i = 0;
    boolean bool = this.lostSurface;
    this.lostSurface = false;
    if (isAccelerationEnabled())
    {
      if (!(isConfigValid(paramGraphicsConfiguration)))
      {
        i = 2;
      }
      else if (this.sdAccel == null)
      {
        this.sdAccel = initAcceleratedSurface();
        if (this.sdAccel != null)
        {
          this.sdCurrent = this.sdAccel;
          this.sdBackup = null;
          i = 1;
        }
        else
        {
          this.sdCurrent = getBackupSurface();
        }
      }
      else if (this.sdAccel.isSurfaceLost())
      {
        try
        {
          restoreAcceleratedSurface();
          this.sdCurrent = this.sdAccel;
          this.sdAccel.setSurfaceLost(false);
          this.sdBackup = null;
          i = 1;
        }
        catch (InvalidPipeException localInvalidPipeException)
        {
          this.sdCurrent = getBackupSurface();
        }
      }
      else if (bool)
      {
        i = 1;
      }
    }
    else if (this.sdAccel != null)
    {
      this.sdCurrent = getBackupSurface();
      this.sdAccel = null;
      i = 1;
    }
    if ((i != 2) && (this.sdCurrent != this.sdPrevious))
    {
      this.sdPrevious = this.sdCurrent;
      i = 1;
    }
    if (i == 1)
      initContents();
    return i;
  }

  public boolean contentsLost()
  {
    return this.lostSurface;
  }

  protected abstract SurfaceData initAcceleratedSurface();

  protected SurfaceData getBackupSurface()
  {
    if (this.sdBackup == null)
    {
      BufferedImage localBufferedImage = this.vImg.getBackupImage();
      this.sdBackup = BufImgSurfaceData.createData(localBufferedImage);
    }
    return this.sdBackup;
  }

  public void initContents()
  {
    if (this.sdCurrent != null)
    {
      Graphics2D localGraphics2D = this.vImg.createGraphics();
      localGraphics2D.clearRect(0, 0, this.vImg.getWidth(), this.vImg.getHeight());
      localGraphics2D.dispose();
    }
  }

  public SurfaceData restoreContents()
  {
    return getBackupSurface();
  }

  public void acceleratedSurfaceLost()
  {
    if ((isAccelerationEnabled()) && (this.sdCurrent == this.sdAccel))
      this.lostSurface = true;
  }

  protected void restoreAcceleratedSurface()
  {
  }

  public void displayChanged()
  {
    if (!(isAccelerationEnabled()))
      return;
    this.lostSurface = true;
    if (this.sdAccel != null)
    {
      this.sdBackup = null;
      this.sdCurrent = getBackupSurface();
      SurfaceData localSurfaceData = this.sdAccel;
      this.sdAccel = null;
      localSurfaceData.invalidate();
    }
    this.vImg.updateGraphicsConfig();
  }

  public void paletteChanged()
  {
    this.lostSurface = true;
  }

  protected boolean isConfigValid(GraphicsConfiguration paramGraphicsConfiguration)
  {
    return ((paramGraphicsConfiguration == null) || (paramGraphicsConfiguration.getDevice() == this.vImg.getGraphicsConfig().getDevice()));
  }

  public ImageCapabilities getCapabilities(GraphicsConfiguration paramGraphicsConfiguration)
  {
    if (isConfigValid(paramGraphicsConfiguration))
      return new ImageCapabilities(false);
    return super.getCapabilities(paramGraphicsConfiguration);
  }

  public void flush()
  {
    this.lostSurface = true;
    SurfaceData localSurfaceData = this.sdAccel;
    this.sdAccel = null;
    if (localSurfaceData != null)
      localSurfaceData.flush();
  }

  private class AcceleratedImageCapabilities extends ImageCapabilities
  {
    AcceleratedImageCapabilities()
    {
      super(false);
    }

    public boolean isAccelerated()
    {
      return (this.this$0.sdCurrent == this.this$0.sdAccel);
    }

    public boolean isTrueVolatile()
    {
      return isAccelerated();
    }
  }
}