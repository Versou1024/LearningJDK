package sun.java2d.opengl;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.VolatileImage;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsDevice;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.SurfaceManager;
import sun.awt.windows.WComponentPeer;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;
import sun.java2d.SunGraphics2D;
import sun.java2d.Surface;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.hw.AccelDeviceEventListener;
import sun.java2d.pipe.hw.AccelDeviceEventNotifier;
import sun.java2d.pipe.hw.AccelSurface;
import sun.java2d.pipe.hw.AccelTypedVolatileImage;
import sun.java2d.pipe.hw.ContextCapabilities;
import sun.java2d.windows.GDIWindowSurfaceData;

public class WGLGraphicsConfig extends Win32GraphicsConfig
  implements OGLGraphicsConfig
{
  protected static boolean wglAvailable;
  private static ImageCapabilities imageCaps = new WGLImageCaps(null);
  private BufferCapabilities bufferCaps;
  private long pConfigInfo;
  private ContextCapabilities oglCaps;
  private OGLContext context;
  private Object disposerReferent = new Object();

  public static native int getDefaultPixFmt(int paramInt);

  private static native boolean initWGL();

  private static native long getWGLConfigInfo(int paramInt1, int paramInt2);

  private static native int getOGLCapabilities(long paramLong);

  protected WGLGraphicsConfig(Win32GraphicsDevice paramWin32GraphicsDevice, int paramInt, long paramLong, ContextCapabilities paramContextCapabilities)
  {
    super(paramWin32GraphicsDevice, paramInt);
    this.pConfigInfo = paramLong;
    this.oglCaps = paramContextCapabilities;
    this.context = new OGLContext(OGLRenderQueue.getInstance(), this);
    Disposer.addRecord(this.disposerReferent, new WGLGCDisposerRecord(this.pConfigInfo, paramWin32GraphicsDevice.getScreen()));
  }

  public static WGLGraphicsConfig getConfig(Win32GraphicsDevice paramWin32GraphicsDevice, int paramInt)
  {
    if (!(wglAvailable))
      return null;
    long l = 3412047291253522432L;
    String[] arrayOfString = new String[1];
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      OGLContext.invalidateCurrentContext();
      WGLGetConfigInfo localWGLGetConfigInfo = new WGLGetConfigInfo(paramWin32GraphicsDevice.getScreen(), paramInt, null);
      localOGLRenderQueue.flushAndInvokeNow(localWGLGetConfigInfo);
      l = localWGLGetConfigInfo.getConfigInfo();
      OGLContext.setScratchSurface(l);
      localOGLRenderQueue.flushAndInvokeNow(new Runnable(arrayOfString)
      {
        public void run()
        {
          this.val$ids[0] = OGLContext.getOGLIdString();
        }
      });
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
    if (l == 3412046810217185280L)
      return null;
    int i = getOGLCapabilities(l);
    OGLContext.OGLContextCaps localOGLContextCaps = new OGLContext.OGLContextCaps(i, arrayOfString[0]);
    return new WGLGraphicsConfig(paramWin32GraphicsDevice, paramInt, l, localOGLContextCaps);
  }

  public static boolean isWGLAvailable()
  {
    return wglAvailable;
  }

  public final boolean isCapPresent(int paramInt)
  {
    return ((this.oglCaps.getCaps() & paramInt) != 0);
  }

  public final long getNativeConfigInfo()
  {
    return this.pConfigInfo;
  }

  public final OGLContext getContext()
  {
    return this.context;
  }

  public synchronized void displayChanged()
  {
    super.displayChanged();
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      OGLContext.invalidateCurrentContext();
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
  }

  public ColorModel getColorModel(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return new DirectColorModel(24, 16711680, 65280, 255);
    case 2:
      return new DirectColorModel(25, 16711680, 65280, 255, 16777216);
    case 3:
      ColorSpace localColorSpace = ColorSpace.getInstance(1000);
      return new DirectColorModel(localColorSpace, 32, 16711680, 65280, 255, -16777216, true, 3);
    }
    return null;
  }

  public String toString()
  {
    return "WGLGraphicsConfig[dev=" + this.screen + ",pixfmt=" + this.visual + "]";
  }

  public SurfaceData createSurfaceData(WComponentPeer paramWComponentPeer, int paramInt)
  {
    Object localObject = WGLSurfaceData.createData(paramWComponentPeer);
    if (localObject == null)
      localObject = GDIWindowSurfaceData.createData(paramWComponentPeer);
    return ((SurfaceData)localObject);
  }

  public void assertOperationSupported(Component paramComponent, int paramInt, BufferCapabilities paramBufferCapabilities)
    throws AWTException
  {
    if (paramInt > 2)
      throw new AWTException("Only double or single buffering is supported");
    BufferCapabilities localBufferCapabilities = getBufferCapabilities();
    if (!(localBufferCapabilities.isPageFlipping()))
      throw new AWTException("Page flipping is not supported");
    if (paramBufferCapabilities.getFlipContents() == BufferCapabilities.FlipContents.PRIOR)
      throw new AWTException("FlipContents.PRIOR is not supported");
  }

  public VolatileImage createBackBuffer(WComponentPeer paramWComponentPeer)
  {
    Component localComponent = (Component)paramWComponentPeer.getTarget();
    return new SunVolatileImage(localComponent, localComponent.getWidth(), localComponent.getHeight(), Boolean.TRUE);
  }

  public void flip(WComponentPeer paramWComponentPeer, Component paramComponent, VolatileImage paramVolatileImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, BufferCapabilities.FlipContents paramFlipContents)
  {
    Object localObject1;
    if (paramFlipContents == BufferCapabilities.FlipContents.COPIED)
    {
      Object localObject2;
      localObject1 = SurfaceManager.getManager(paramVolatileImage);
      SurfaceData localSurfaceData1 = ((SurfaceManager)localObject1).getDestSurfaceData();
      if (localSurfaceData1 instanceof WGLSurfaceData.WGLVSyncOffScreenSurfaceData)
      {
        localObject2 = (WGLSurfaceData.WGLVSyncOffScreenSurfaceData)localSurfaceData1;
        SurfaceData localSurfaceData2 = ((WGLSurfaceData.WGLVSyncOffScreenSurfaceData)localObject2).getFlipSurface();
        SunGraphics2D localSunGraphics2D = new SunGraphics2D(localSurfaceData2, Color.black, Color.white, null);
        try
        {
          localSunGraphics2D.drawImage(paramVolatileImage, 0, 0, null);
        }
        finally
        {
          localSunGraphics2D.dispose();
        }
      }
      else
      {
        localObject2 = paramWComponentPeer.getGraphics();
        try
        {
          ((Graphics)localObject2).drawImage(paramVolatileImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt1, paramInt2, paramInt3, paramInt4, null);
        }
        finally
        {
          ((Graphics)localObject2).dispose();
        }
        return;
      }
    }
    else if (paramFlipContents == BufferCapabilities.FlipContents.PRIOR)
    {
      return;
    }
    OGLSurfaceData.swapBuffers(paramWComponentPeer.getData());
    if (paramFlipContents == BufferCapabilities.FlipContents.BACKGROUND)
    {
      localObject1 = paramVolatileImage.getGraphics();
      try
      {
        ((Graphics)localObject1).setColor(paramComponent.getBackground());
        ((Graphics)localObject1).fillRect(0, 0, paramVolatileImage.getWidth(), paramVolatileImage.getHeight());
      }
      finally
      {
        ((Graphics)localObject1).dispose();
      }
    }
  }

  public BufferCapabilities getBufferCapabilities()
  {
    if (this.bufferCaps == null)
    {
      boolean bool = isCapPresent(65536);
      this.bufferCaps = new WGLBufferCaps(bool);
    }
    return this.bufferCaps;
  }

  public ImageCapabilities getImageCapabilities()
  {
    return imageCaps;
  }

  public VolatileImage createCompatibleVolatileImage(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((paramInt4 == 4) || (paramInt4 == 1) || (paramInt4 == 0) || (paramInt3 == 2))
      return null;
    if (paramInt4 == 5)
    {
      if (isCapPresent(12))
        break label74;
      return null;
    }
    if (paramInt4 == 2)
    {
      int i = (paramInt3 == 1) ? 1 : 0;
      if ((i == 0) && (!(isCapPresent(2))))
        return null;
    }
    label74: AccelTypedVolatileImage localAccelTypedVolatileImage = new AccelTypedVolatileImage(this, paramInt1, paramInt2, paramInt3, paramInt4);
    Surface localSurface = localAccelTypedVolatileImage.getDestSurface();
    if ((!(localSurface instanceof AccelSurface)) || (((AccelSurface)localSurface).getType() != paramInt4))
    {
      localAccelTypedVolatileImage.flush();
      localAccelTypedVolatileImage = null;
    }
    return localAccelTypedVolatileImage;
  }

  public ContextCapabilities getContextCapabilities()
  {
    return this.oglCaps;
  }

  public void addDeviceEventListener(AccelDeviceEventListener paramAccelDeviceEventListener)
  {
    AccelDeviceEventNotifier.addListener(paramAccelDeviceEventListener, this.screen.getScreen());
  }

  public void removeDeviceEventListener(AccelDeviceEventListener paramAccelDeviceEventListener)
  {
    AccelDeviceEventNotifier.removeListener(paramAccelDeviceEventListener);
  }

  static
  {
    wglAvailable = initWGL();
  }

  private static class WGLBufferCaps extends BufferCapabilities
  {
    public WGLBufferCaps(boolean paramBoolean)
    {
      super(WGLGraphicsConfig.access$400(), WGLGraphicsConfig.access$400(), (paramBoolean) ? BufferCapabilities.FlipContents.UNDEFINED : null);
    }
  }

  private static class WGLGCDisposerRecord
  implements DisposerRecord
  {
    private long pCfgInfo;
    private int screen;

    public WGLGCDisposerRecord(long paramLong, int paramInt)
    {
      this.pCfgInfo = paramLong;
    }

    public void dispose()
    {
      OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
      localOGLRenderQueue.lock();
      try
      {
        localOGLRenderQueue.flushAndInvokeNow(new Runnable(this)
        {
          public void run()
          {
            AccelDeviceEventNotifier.eventOccured(WGLGraphicsConfig.WGLGCDisposerRecord.access$300(this.this$0), 0);
            AccelDeviceEventNotifier.eventOccured(WGLGraphicsConfig.WGLGCDisposerRecord.access$300(this.this$0), 1);
          }
        });
      }
      finally
      {
        localOGLRenderQueue.unlock();
      }
      if (this.pCfgInfo != 3412047308433391616L)
      {
        OGLRenderQueue.disposeGraphicsConfig(this.pCfgInfo);
        this.pCfgInfo = 3412048098707374080L;
      }
    }
  }

  private static class WGLGetConfigInfo
  implements Runnable
  {
    private int screen;
    private int pixfmt;
    private long cfginfo;

    private WGLGetConfigInfo(int paramInt1, int paramInt2)
    {
      this.screen = paramInt1;
      this.pixfmt = paramInt2;
    }

    public void run()
    {
      this.cfginfo = WGLGraphicsConfig.access$200(this.screen, this.pixfmt);
    }

    public long getConfigInfo()
    {
      return this.cfginfo;
    }
  }

  private static class WGLImageCaps extends ImageCapabilities
  {
    private WGLImageCaps()
    {
      super(true);
    }

    public boolean isTrueVolatile()
    {
      return true;
    }
  }
}