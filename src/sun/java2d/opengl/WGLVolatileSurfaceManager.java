package sun.java2d.opengl;

import java.awt.BufferCapabilities.FlipContents;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.image.ColorModel;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.VolatileSurfaceManager;
import sun.awt.windows.WComponentPeer;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.hw.ExtendedBufferCapabilities;
import sun.java2d.pipe.hw.ExtendedBufferCapabilities.VSyncType;

public class WGLVolatileSurfaceManager extends VolatileSurfaceManager
{
  private boolean accelerationEnabled;

  public WGLVolatileSurfaceManager(SunVolatileImage paramSunVolatileImage, Object paramObject)
  {
    super(paramSunVolatileImage, paramObject);
    int i = paramSunVolatileImage.getTransparency();
    WGLGraphicsConfig localWGLGraphicsConfig = (WGLGraphicsConfig)paramSunVolatileImage.getGraphicsConfig();
    this.accelerationEnabled = ((i == 1) || ((i == 3) && (((localWGLGraphicsConfig.isCapPresent(12)) || (localWGLGraphicsConfig.isCapPresent(2))))));
  }

  protected boolean isAccelerationEnabled()
  {
    return this.accelerationEnabled;
  }

  protected SurfaceData initAcceleratedSurface()
  {
    WGLSurfaceData.WGLOffScreenSurfaceData localWGLOffScreenSurfaceData;
    Component localComponent = this.vImg.getComponent();
    WComponentPeer localWComponentPeer = (localComponent != null) ? (WComponentPeer)localComponent.getPeer() : null;
    try
    {
      Object localObject1;
      Object localObject2;
      int i = 0;
      boolean bool = false;
      if (this.context instanceof Boolean)
      {
        bool = ((Boolean)this.context).booleanValue();
        if (bool)
        {
          localObject1 = localWComponentPeer.getBackBufferCaps();
          if (localObject1 instanceof ExtendedBufferCapabilities)
          {
            localObject2 = (ExtendedBufferCapabilities)localObject1;
            if ((((ExtendedBufferCapabilities)localObject2).getVSync() == ExtendedBufferCapabilities.VSyncType.VSYNC_ON) && (((ExtendedBufferCapabilities)localObject2).getFlipContents() == BufferCapabilities.FlipContents.COPIED))
            {
              i = 1;
              bool = false;
            }
          }
        }
      }
      if (bool)
      {
        localWGLOffScreenSurfaceData = WGLSurfaceData.createData(localWComponentPeer, this.vImg, 4);
      }
      else
      {
        localObject1 = (WGLGraphicsConfig)this.vImg.getGraphicsConfig();
        localObject2 = ((WGLGraphicsConfig)localObject1).getColorModel(this.vImg.getTransparency());
        int j = this.vImg.getForcedAccelSurfaceType();
        if (j == 0)
          j = (((WGLGraphicsConfig)localObject1).isCapPresent(12)) ? 5 : 2;
        if (i != 0)
          localWGLOffScreenSurfaceData = WGLSurfaceData.createData(localWComponentPeer, this.vImg, j);
        else
          localWGLOffScreenSurfaceData = WGLSurfaceData.createData((WGLGraphicsConfig)localObject1, this.vImg.getWidth(), this.vImg.getHeight(), (ColorModel)localObject2, this.vImg, j);
      }
    }
    catch (NullPointerException localNullPointerException)
    {
      localWGLOffScreenSurfaceData = null;
    }
    catch (OutOfMemoryError localOutOfMemoryError)
    {
      localWGLOffScreenSurfaceData = null;
    }
    return ((SurfaceData)(SurfaceData)localWGLOffScreenSurfaceData);
  }

  protected boolean isConfigValid(GraphicsConfiguration paramGraphicsConfiguration)
  {
    return ((paramGraphicsConfiguration == null) || (paramGraphicsConfiguration == this.vImg.getGraphicsConfig()));
  }

  public void initContents()
  {
    if (this.vImg.getForcedAccelSurfaceType() != 3)
      super.initContents();
  }
}