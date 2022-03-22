package sun.awt.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.awt.peer.ComponentPeer;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;

public abstract class OffScreenSurfaceManager extends CachingSurfaceManager
{
  private WeakReference<SurfaceData> bisdRef;

  public OffScreenSurfaceManager(Component paramComponent, BufferedImage paramBufferedImage)
  {
    super(paramBufferedImage);
    if (!(accelerationEnabled))
      return;
    ComponentPeer localComponentPeer = null;
    if (paramComponent != null)
      localComponentPeer = paramComponent.getPeer();
    if (localComponentPeer != null)
    {
      localGraphicsConfiguration = localComponentPeer.getGraphicsConfiguration();
    }
    else
    {
      GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice localGraphicsDevice = localGraphicsEnvironment.getDefaultScreenDevice();
      localGraphicsConfiguration = localGraphicsDevice.getDefaultConfiguration();
    }
    initAcceleratedSurface(localGraphicsConfiguration, this.bImg.getWidth(), this.bImg.getHeight());
    if (this.sdAccel != null)
      this.sdDefault = this.sdAccel;
  }

  public SurfaceData getSourceSurfaceData(SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean)
  {
    if ((accelerationEnabled) && (paramSurfaceData != this.sdAccel) && (isDestSurfaceAccelerated(paramSurfaceData)))
    {
      validate(paramSurfaceData.getDeviceConfiguration());
      if (this.sdAccel != null)
        return this.sdAccel;
    }
    return this.sdDefault;
  }

  protected synchronized void copyDefaultToAccelerated()
  {
    if (this.accelSurfaces != null)
    {
      int i = 0;
      Collection localCollection = this.accelSurfaces.values();
      SurfaceData localSurfaceData1 = (this.bisdRef == null) ? null : (SurfaceData)this.bisdRef.get();
      Iterator localIterator = localCollection.iterator();
      while (localIterator.hasNext())
      {
        SurfaceData localSurfaceData2 = (SurfaceData)localIterator.next();
        if ((this.sdDefault != null) && (this.sdDefault != localSurfaceData2))
          try
          {
            if (localSurfaceData1 == null)
            {
              localObject = createTempImage();
              localSurfaceData1 = BufImgSurfaceData.createData((BufferedImage)localObject);
              this.bisdRef = new WeakReference(localSurfaceData1);
            }
            Object localObject = this.sdDefault.getSurfaceType();
            SurfaceType localSurfaceType1 = localSurfaceData1.getSurfaceType();
            SurfaceType localSurfaceType2 = localSurfaceData2.getSurfaceType();
            Blit localBlit = Blit.getFromCache((SurfaceType)localObject, CompositeType.SrcNoEa, localSurfaceType1);
            localBlit.Blit(this.sdDefault, localSurfaceData1, AlphaComposite.Src, null, 0, 0, 0, 0, this.bImg.getWidth(), this.bImg.getHeight());
            localBlit = Blit.getFromCache(localSurfaceType1, CompositeType.SrcNoEa, localSurfaceType2);
            localBlit.Blit(localSurfaceData1, localSurfaceData2, AlphaComposite.Src, null, 0, 0, 0, 0, this.bImg.getWidth(), this.bImg.getHeight());
          }
          catch (Exception localException)
          {
            if (localSurfaceData2 != null)
              localSurfaceData2.setSurfaceLost(true);
            i = 1;
          }
      }
      if (i == 0)
        this.sdDefault.setNeedsBackup(false);
    }
  }

  protected boolean isOperationSupported(SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean)
  {
    return true;
  }

  public synchronized void flush()
  {
    this.sdAccel = null;
    if (this.accelSurfaces != null)
    {
      HashMap localHashMap = this.accelSurfaces;
      this.accelSurfaces = new HashMap(2);
      this.accelSurfaces.put(this.sdDefault.getDeviceConfiguration(), this.sdDefault);
      Collection localCollection = localHashMap.values();
      Iterator localIterator = localCollection.iterator();
      while (localIterator.hasNext())
      {
        SurfaceData localSurfaceData = (SurfaceData)localIterator.next();
        if (localSurfaceData != this.sdDefault)
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
      this.accelSurfaces = new HashMap(2);
      this.accelSurfaces.put(this.sdDefault.getDeviceConfiguration(), this.sdDefault);
      Collection localCollection = localHashMap.values();
      Iterator localIterator = localCollection.iterator();
      while (localIterator.hasNext())
      {
        SurfaceData localSurfaceData = (SurfaceData)localIterator.next();
        if (localSurfaceData != this.sdDefault)
          localSurfaceData.invalidate();
      }
    }
  }

  protected BufferedImage createTempImage()
  {
    ColorModel localColorModel = this.bImg.getColorModel();
    WritableRaster localWritableRaster = localColorModel.createCompatibleWritableRaster(this.bImg.getWidth(), this.bImg.getHeight());
    return new BufferedImage(localColorModel, localWritableRaster, localColorModel.isAlphaPremultiplied(), null);
  }
}