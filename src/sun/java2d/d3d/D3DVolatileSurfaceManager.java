package sun.java2d.d3d;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.ColorModel;
import sun.awt.Win32GraphicsConfig;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.SurfaceManager;
import sun.awt.image.VolatileSurfaceManager;
import sun.awt.windows.WComponentPeer;
import sun.java2d.InvalidPipeException;
import sun.java2d.SurfaceData;
import sun.java2d.windows.GDIWindowSurfaceData;

public class D3DVolatileSurfaceManager extends VolatileSurfaceManager
{
  private boolean accelerationEnabled;
  private int restoreCountdown;

  public D3DVolatileSurfaceManager(SunVolatileImage paramSunVolatileImage, Object paramObject)
  {
    super(paramSunVolatileImage, paramObject);
    int i = paramSunVolatileImage.getTransparency();
    D3DGraphicsDevice localD3DGraphicsDevice = (D3DGraphicsDevice)paramSunVolatileImage.getGraphicsConfig().getDevice();
    this.accelerationEnabled = ((i == 1) || ((i == 3) && (((localD3DGraphicsDevice.isCapPresent(2)) || (localD3DGraphicsDevice.isCapPresent(4))))));
  }

  protected boolean isAccelerationEnabled()
  {
    return this.accelerationEnabled;
  }

  public void setAccelerationEnabled(boolean paramBoolean)
  {
    this.accelerationEnabled = paramBoolean;
  }

  protected SurfaceData initAcceleratedSurface()
  {
    D3DSurfaceData localD3DSurfaceData;
    Component localComponent = this.vImg.getComponent();
    WComponentPeer localWComponentPeer = (localComponent != null) ? (WComponentPeer)localComponent.getPeer() : null;
    try
    {
      boolean bool = false;
      if (this.context instanceof Boolean)
        bool = ((Boolean)this.context).booleanValue();
      if (bool)
      {
        localD3DSurfaceData = D3DSurfaceData.createData(localWComponentPeer, this.vImg);
      }
      else
      {
        D3DGraphicsConfig localD3DGraphicsConfig = (D3DGraphicsConfig)this.vImg.getGraphicsConfig();
        ColorModel localColorModel = localD3DGraphicsConfig.getColorModel(this.vImg.getTransparency());
        int i = this.vImg.getForcedAccelSurfaceType();
        if (i == 0)
          i = 5;
        localD3DSurfaceData = D3DSurfaceData.createData(localD3DGraphicsConfig, this.vImg.getWidth(), this.vImg.getHeight(), localColorModel, this.vImg, i);
      }
    }
    catch (NullPointerException localNullPointerException)
    {
      localD3DSurfaceData = null;
    }
    catch (OutOfMemoryError localOutOfMemoryError)
    {
      localD3DSurfaceData = null;
    }
    catch (InvalidPipeException localInvalidPipeException)
    {
      localD3DSurfaceData = null;
    }
    return localD3DSurfaceData;
  }

  protected boolean isConfigValid(GraphicsConfiguration paramGraphicsConfiguration)
  {
    return ((paramGraphicsConfiguration == null) || (paramGraphicsConfiguration == this.vImg.getGraphicsConfig()));
  }

  private synchronized void setRestoreCountdown(int paramInt)
  {
    this.restoreCountdown = paramInt;
  }

  protected void restoreAcceleratedSurface()
  {
    synchronized (this)
    {
      if (this.restoreCountdown > 0)
      {
        this.restoreCountdown -= 1;
        throw new InvalidPipeException("Will attempt to restore surface  in " + this.restoreCountdown);
      }
    }
    ??? = initAcceleratedSurface();
    if (??? != null)
      this.sdAccel = ((SurfaceData)???);
    else
      throw new InvalidPipeException("could not restore surface");
  }

  public SurfaceData restoreContents()
  {
    acceleratedSurfaceLost();
    return super.restoreContents();
  }

  static void handleVItoScreenOp(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2)
  {
    if ((paramSurfaceData1 instanceof D3DSurfaceData) && (paramSurfaceData2 instanceof GDIWindowSurfaceData))
    {
      D3DSurfaceData localD3DSurfaceData = (D3DSurfaceData)paramSurfaceData1;
      SurfaceManager localSurfaceManager = SurfaceManager.getManager((Image)localD3DSurfaceData.getDestination());
      if (localSurfaceManager instanceof D3DVolatileSurfaceManager)
      {
        D3DVolatileSurfaceManager localD3DVolatileSurfaceManager = (D3DVolatileSurfaceManager)localSurfaceManager;
        if (localD3DVolatileSurfaceManager != null)
        {
          localD3DSurfaceData.setSurfaceLost(true);
          GDIWindowSurfaceData localGDIWindowSurfaceData = (GDIWindowSurfaceData)paramSurfaceData2;
          WComponentPeer localWComponentPeer = localGDIWindowSurfaceData.getPeer();
          if (D3DScreenUpdateManager.canUseD3DOnScreen(localWComponentPeer, (Win32GraphicsConfig)localWComponentPeer.getGraphicsConfiguration(), localWComponentPeer.getBackBuffersNum()))
            localD3DVolatileSurfaceManager.setRestoreCountdown(10);
          else
            localD3DVolatileSurfaceManager.setAccelerationEnabled(false);
        }
      }
    }
  }

  public void initContents()
  {
    if (this.vImg.getForcedAccelSurfaceType() != 3)
      super.initContents();
  }
}