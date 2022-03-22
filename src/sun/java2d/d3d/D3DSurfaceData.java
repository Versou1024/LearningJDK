package sun.java2d.d3d;

import java.awt.AlphaComposite;
import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import sun.awt.SunToolkit;
import sun.awt.image.DataBufferNative;
import sun.awt.image.PixelConverter.ArgbPre;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.SurfaceManager;
import sun.awt.image.WritableRasterNative;
import sun.awt.windows.WComponentPeer;
import sun.java2d.InvalidPipeException;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.hw.AccelSurface;
import sun.java2d.pipe.hw.ExtendedBufferCapabilities;
import sun.java2d.pipe.hw.ExtendedBufferCapabilities.VSyncType;

public class D3DSurfaceData extends SurfaceData
  implements AccelSurface
{
  public static final int D3D_DEVICE_RESOURCE = 100;
  public static final int ST_INT_ARGB = 0;
  public static final int ST_INT_ARGB_PRE = 1;
  public static final int ST_INT_ARGB_BM = 2;
  public static final int ST_INT_RGB = 3;
  public static final int ST_INT_BGR = 4;
  public static final int ST_USHORT_565_RGB = 5;
  public static final int ST_USHORT_555_RGB = 6;
  public static final int ST_BYTE_INDEXED = 7;
  public static final int ST_BYTE_INDEXED_BM = 8;
  public static final int ST_3BYTE_BGR = 9;
  public static final int SWAP_DISCARD = 1;
  public static final int SWAP_FLIP = 2;
  public static final int SWAP_COPY = 3;
  private static final String DESC_D3D_SURFACE = "D3D Surface";
  private static final String DESC_D3D_SURFACE_RTT = "D3D Surface (render-to-texture)";
  private static final String DESC_D3D_TEXTURE = "D3D Texture";
  static final SurfaceType D3DSurface = SurfaceType.Any.deriveSubType("D3D Surface", PixelConverter.ArgbPre.instance);
  static final SurfaceType D3DSurfaceRTT = D3DSurface.deriveSubType("D3D Surface (render-to-texture)");
  static final SurfaceType D3DTexture = SurfaceType.Any.deriveSubType("D3D Texture");
  private int type;
  private int width;
  private int height;
  private int nativeWidth;
  private int nativeHeight;
  protected WComponentPeer peer;
  private Image offscreenImage;
  protected D3DGraphicsDevice graphicsDevice;
  private int swapEffect;
  private ExtendedBufferCapabilities.VSyncType syncType;
  private int backBuffersNum;
  private WritableRasterNative wrn;
  protected static D3DRenderer d3dRenderPipe;
  protected static sun.java2d.pipe.PixelToParallelogramConverter d3dTxRenderPipe;
  protected static ParallelogramPipe d3dAAPgramPipe;
  protected static D3DTextRenderer d3dTextPipe;
  protected static D3DDrawImage d3dImagePipe;

  private native boolean initTexture(long paramLong, boolean paramBoolean1, boolean paramBoolean2);

  private native boolean initFlipBackbuffer(long paramLong1, long paramLong2, int paramInt1, int paramInt2, int paramInt3);

  private native boolean initRTSurface(long paramLong, boolean paramBoolean);

  private native void initOps(int paramInt1, int paramInt2, int paramInt3);

  protected D3DSurfaceData(WComponentPeer paramWComponentPeer, D3DGraphicsConfig paramD3DGraphicsConfig, int paramInt1, int paramInt2, Image paramImage, ColorModel paramColorModel, int paramInt3, int paramInt4, ExtendedBufferCapabilities.VSyncType paramVSyncType, int paramInt5)
  {
    super(getCustomSurfaceType(paramInt5), paramColorModel);
    this.graphicsDevice = paramD3DGraphicsConfig.getD3DDevice();
    this.peer = paramWComponentPeer;
    this.type = paramInt5;
    this.width = paramInt1;
    this.height = paramInt2;
    this.offscreenImage = paramImage;
    this.backBuffersNum = paramInt3;
    this.swapEffect = paramInt4;
    this.syncType = paramVSyncType;
    initOps(this.graphicsDevice.getScreen(), paramInt1, paramInt2);
    if (paramInt5 == 1)
      setSurfaceLost(true);
    else
      initSurface();
  }

  public static D3DSurfaceData createData(WComponentPeer paramWComponentPeer, Image paramImage)
  {
    int i;
    D3DGraphicsConfig localD3DGraphicsConfig = getGC(paramWComponentPeer);
    if ((localD3DGraphicsConfig == null) || (!(paramWComponentPeer.isAccelCapable())))
      return null;
    BufferCapabilities localBufferCapabilities = paramWComponentPeer.getBackBufferCaps();
    ExtendedBufferCapabilities.VSyncType localVSyncType = ExtendedBufferCapabilities.VSyncType.VSYNC_DEFAULT;
    if (localBufferCapabilities instanceof ExtendedBufferCapabilities)
      localVSyncType = ((ExtendedBufferCapabilities)localBufferCapabilities).getVSync();
    Rectangle localRectangle = paramWComponentPeer.getBounds();
    BufferCapabilities.FlipContents localFlipContents = localBufferCapabilities.getFlipContents();
    if (localFlipContents == BufferCapabilities.FlipContents.COPIED)
      i = 3;
    else if (localFlipContents == BufferCapabilities.FlipContents.PRIOR)
      i = 2;
    else
      i = 1;
    return new D3DSurfaceData(paramWComponentPeer, localD3DGraphicsConfig, localRectangle.width, localRectangle.height, paramImage, paramWComponentPeer.getColorModel(), paramWComponentPeer.getBackBuffersNum(), i, localVSyncType, 4);
  }

  public static D3DSurfaceData createData(WComponentPeer paramWComponentPeer)
  {
    D3DGraphicsConfig localD3DGraphicsConfig = getGC(paramWComponentPeer);
    if ((localD3DGraphicsConfig == null) || (!(paramWComponentPeer.isAccelCapable())))
      return null;
    return new D3DWindowSurfaceData(paramWComponentPeer, localD3DGraphicsConfig);
  }

  public static D3DSurfaceData createData(D3DGraphicsConfig paramD3DGraphicsConfig, int paramInt1, int paramInt2, ColorModel paramColorModel, Image paramImage, int paramInt3)
  {
    if (paramInt3 == 5)
    {
      int i = (paramColorModel.getTransparency() == 1) ? 1 : 0;
      int j = (i != 0) ? 8 : 4;
      if (!(paramD3DGraphicsConfig.getD3DDevice().isCapPresent(j)))
        paramInt3 = 2;
    }
    D3DSurfaceData localD3DSurfaceData = null;
    try
    {
      localD3DSurfaceData = new D3DSurfaceData(null, paramD3DGraphicsConfig, paramInt1, paramInt2, paramImage, paramColorModel, 0, 1, ExtendedBufferCapabilities.VSyncType.VSYNC_DEFAULT, paramInt3);
    }
    catch (InvalidPipeException localInvalidPipeException)
    {
      if ((paramInt3 == 5) && (((SunVolatileImage)paramImage).getForcedAccelSurfaceType() != 5))
      {
        paramInt3 = 2;
        localD3DSurfaceData = new D3DSurfaceData(null, paramD3DGraphicsConfig, paramInt1, paramInt2, paramImage, paramColorModel, 0, 1, ExtendedBufferCapabilities.VSyncType.VSYNC_DEFAULT, paramInt3);
      }
    }
    return localD3DSurfaceData;
  }

  private static SurfaceType getCustomSurfaceType(int paramInt)
  {
    switch (paramInt)
    {
    case 3:
      return D3DTexture;
    case 5:
      return D3DSurfaceRTT;
    }
    return D3DSurface;
  }

  private boolean initSurfaceNow()
  {
    boolean bool = getTransparency() == 1;
    switch (this.type)
    {
    case 2:
      return initRTSurface(getNativeOps(), bool);
    case 3:
      return initTexture(getNativeOps(), false, bool);
    case 5:
      return initTexture(getNativeOps(), true, bool);
    case 1:
    case 4:
      return initFlipBackbuffer(getNativeOps(), this.peer.getData(), this.backBuffersNum, this.swapEffect, this.syncType.id());
    }
    return false;
  }

  protected void initSurface()
  {
    synchronized (this)
    {
      this.wrn = null;
    }
    ??? = new Object(this)
    {
      boolean success = false;
    };
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      localD3DRenderQueue.flushAndInvokeNow(new Runnable(this, (1Status)???)
      {
        public void run()
        {
          this.val$status.success = D3DSurfaceData.access$000(this.this$0);
        }
      });
      if (!(((1Status)???).success))
        throw new InvalidPipeException("Error creating D3DSurface");
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  public final D3DContext getContext()
  {
    return this.graphicsDevice.getContext();
  }

  public final int getType()
  {
    return this.type;
  }

  private static native int dbGetPixelNative(long paramLong, int paramInt1, int paramInt2);

  private static native void dbSetPixelNative(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  public synchronized Raster getRaster(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (this.wrn == null)
    {
      DirectColorModel localDirectColorModel = (DirectColorModel)getColorModel();
      int i = 0;
      int j = this.width;
      if ((localDirectColorModel.getPixelSize() == 24) || (localDirectColorModel.getPixelSize() == 32))
        i = 3;
      else
        i = 1;
      SinglePixelPackedSampleModel localSinglePixelPackedSampleModel = new SinglePixelPackedSampleModel(i, this.width, this.height, j, localDirectColorModel.getMasks());
      D3DDataBufferNative localD3DDataBufferNative = new D3DDataBufferNative(this, i, this.width, this.height);
      this.wrn = WritableRasterNative.createNativeRaster(localSinglePixelPackedSampleModel, localD3DDataBufferNative);
    }
    return this.wrn;
  }

  public boolean canRenderLCDText(SunGraphics2D paramSunGraphics2D)
  {
    return ((this.graphicsDevice.isCapPresent(65536)) && (paramSunGraphics2D.compositeState <= 0) && (paramSunGraphics2D.paintState <= 0) && (paramSunGraphics2D.surfaceData.getTransparency() == 1) && (paramSunGraphics2D.antialiasHint != 2));
  }

  public void validatePipe(SunGraphics2D paramSunGraphics2D)
  {
    Object localObject;
    int i = 0;
    if (paramSunGraphics2D.compositeState >= 2)
    {
      super.validatePipe(paramSunGraphics2D);
      paramSunGraphics2D.imagepipe = d3dImagePipe;
      return;
    }
    if (((paramSunGraphics2D.compositeState <= 0) && (paramSunGraphics2D.paintState <= 1)) || ((paramSunGraphics2D.compositeState == 1) && (paramSunGraphics2D.paintState <= 1) && (((AlphaComposite)paramSunGraphics2D.composite).getRule() == 3)) || ((paramSunGraphics2D.compositeState == 2) && (paramSunGraphics2D.paintState <= 1)))
    {
      localObject = d3dTextPipe;
    }
    else
    {
      super.validatePipe(paramSunGraphics2D);
      localObject = paramSunGraphics2D.textpipe;
      i = 1;
    }
    sun.java2d.pipe.PixelToParallelogramConverter localPixelToParallelogramConverter1 = null;
    D3DRenderer localD3DRenderer = null;
    if (paramSunGraphics2D.antialiasHint != 2)
      if (paramSunGraphics2D.paintState <= 1)
      {
        if (paramSunGraphics2D.compositeState <= 2)
        {
          localPixelToParallelogramConverter1 = d3dTxRenderPipe;
          localD3DRenderer = d3dRenderPipe;
        }
      }
      else if ((paramSunGraphics2D.compositeState <= 1) && (D3DPaints.isValid(paramSunGraphics2D)))
      {
        localPixelToParallelogramConverter1 = d3dTxRenderPipe;
        localD3DRenderer = d3dRenderPipe;
      }
    else if (paramSunGraphics2D.paintState <= 1)
      if ((this.graphicsDevice.isCapPresent(524288)) && (((paramSunGraphics2D.imageComp == CompositeType.SrcOverNoEa) || (paramSunGraphics2D.imageComp == CompositeType.SrcOver))))
      {
        if (i == 0)
        {
          super.validatePipe(paramSunGraphics2D);
          i = 1;
        }
        sun.java2d.pipe.PixelToParallelogramConverter localPixelToParallelogramConverter2 = new sun.java2d.pipe.PixelToParallelogramConverter(paramSunGraphics2D.shapepipe, d3dAAPgramPipe, 0.125D, 0.499D, false);
        paramSunGraphics2D.drawpipe = localPixelToParallelogramConverter2;
        paramSunGraphics2D.fillpipe = localPixelToParallelogramConverter2;
        paramSunGraphics2D.shapepipe = localPixelToParallelogramConverter2;
      }
      else if (paramSunGraphics2D.compositeState == 2)
      {
        localPixelToParallelogramConverter1 = d3dTxRenderPipe;
        localD3DRenderer = d3dRenderPipe;
      }
    if (localPixelToParallelogramConverter1 != null)
    {
      if (paramSunGraphics2D.transformState >= 3)
      {
        paramSunGraphics2D.drawpipe = localPixelToParallelogramConverter1;
        paramSunGraphics2D.fillpipe = localPixelToParallelogramConverter1;
      }
      else if (paramSunGraphics2D.strokeState != 0)
      {
        paramSunGraphics2D.drawpipe = localPixelToParallelogramConverter1;
        paramSunGraphics2D.fillpipe = localD3DRenderer;
      }
      else
      {
        paramSunGraphics2D.drawpipe = localD3DRenderer;
        paramSunGraphics2D.fillpipe = localD3DRenderer;
      }
      paramSunGraphics2D.shapepipe = localPixelToParallelogramConverter1;
    }
    else if (i == 0)
    {
      super.validatePipe(paramSunGraphics2D);
    }
    paramSunGraphics2D.textpipe = ((TextPipe)localObject);
    paramSunGraphics2D.imagepipe = d3dImagePipe;
  }

  protected MaskFill getMaskFill(SunGraphics2D paramSunGraphics2D)
  {
    if ((paramSunGraphics2D.paintState > 1) && (((!(D3DPaints.isValid(paramSunGraphics2D))) || (!(this.graphicsDevice.isCapPresent(16))))))
      return null;
    return super.getMaskFill(paramSunGraphics2D);
  }

  public boolean copyArea(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    if ((paramSunGraphics2D.transformState < 3) && (paramSunGraphics2D.compositeState < 2))
    {
      paramInt1 += paramSunGraphics2D.transX;
      paramInt2 += paramSunGraphics2D.transY;
      d3dRenderPipe.copyArea(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
      return true;
    }
    return false;
  }

  public void flush()
  {
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      RenderBuffer localRenderBuffer = localD3DRenderQueue.getBuffer();
      localD3DRenderQueue.ensureCapacityAndAlignment(12, 4);
      localRenderBuffer.putInt(72);
      localRenderBuffer.putLong(getNativeOps());
      localD3DRenderQueue.flushNow();
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  static void dispose(long paramLong)
  {
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      RenderBuffer localRenderBuffer = localD3DRenderQueue.getBuffer();
      localD3DRenderQueue.ensureCapacityAndAlignment(12, 4);
      localRenderBuffer.putInt(73);
      localRenderBuffer.putLong(paramLong);
      localD3DRenderQueue.flushNow();
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  static void swapBuffers(D3DSurfaceData paramD3DSurfaceData, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Object localObject1;
    long l = paramD3DSurfaceData.getNativeOps();
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    if (D3DRenderQueue.isRenderQueueThread())
    {
      if (localD3DRenderQueue.tryLock())
        break label65;
      localObject1 = (Component)paramD3DSurfaceData.getPeer().getTarget();
      SunToolkit.executeOnEventHandlerThread(localObject1, new Runnable((Component)localObject1, paramInt1, paramInt2, paramInt3, paramInt4)
      {
        public void run()
        {
          this.val$target.repaint(this.val$x1, this.val$y1, this.val$x2, this.val$y2);
        }
      });
      return;
    }
    localD3DRenderQueue.lock();
    try
    {
      label65: localObject1 = localD3DRenderQueue.getBuffer();
      localD3DRenderQueue.ensureCapacityAndAlignment(28, 4);
      ((RenderBuffer)localObject1).putInt(80);
      ((RenderBuffer)localObject1).putLong(l);
      ((RenderBuffer)localObject1).putInt(paramInt1);
      ((RenderBuffer)localObject1).putInt(paramInt2);
      ((RenderBuffer)localObject1).putInt(paramInt3);
      ((RenderBuffer)localObject1).putInt(paramInt4);
      localD3DRenderQueue.flushNow();
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  public Object getDestination()
  {
    return this.offscreenImage;
  }

  public Rectangle getBounds()
  {
    if ((this.type == 4) || (this.type == 1))
    {
      Rectangle localRectangle = this.peer.getBounds();
      localRectangle.x = (localRectangle.y = 0);
      return localRectangle;
    }
    return new Rectangle(this.width, this.height);
  }

  public Rectangle getNativeBounds()
  {
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      Rectangle localRectangle = new Rectangle(this.nativeWidth, this.nativeHeight);
      return localRectangle;
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  public GraphicsConfiguration getDeviceConfiguration()
  {
    return this.graphicsDevice.getDefaultConfiguration();
  }

  public SurfaceData getReplacement()
  {
    return restoreContents(this.offscreenImage);
  }

  private static D3DGraphicsConfig getGC(WComponentPeer paramWComponentPeer)
  {
    GraphicsConfiguration localGraphicsConfiguration;
    if (paramWComponentPeer != null)
    {
      localGraphicsConfiguration = paramWComponentPeer.getGraphicsConfiguration();
    }
    else
    {
      GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice localGraphicsDevice = localGraphicsEnvironment.getDefaultScreenDevice();
      localGraphicsConfiguration = localGraphicsDevice.getDefaultConfiguration();
    }
    return ((localGraphicsConfiguration instanceof D3DGraphicsConfig) ? (D3DGraphicsConfig)localGraphicsConfiguration : null);
  }

  void restoreSurface()
  {
    initSurface();
  }

  WComponentPeer getPeer()
  {
    return this.peer;
  }

  public void setSurfaceLost(boolean paramBoolean)
  {
    super.setSurfaceLost(paramBoolean);
    if ((paramBoolean) && (this.offscreenImage != null))
    {
      SurfaceManager localSurfaceManager = SurfaceManager.getManager(this.offscreenImage);
      localSurfaceManager.acceleratedSurfaceLost();
    }
  }

  private static native long getNativeResourceNative(long paramLong, int paramInt);

  public long getNativeResource(int paramInt)
  {
    return getNativeResourceNative(getNativeOps(), paramInt);
  }

  public static native boolean updateWindowAccelImpl(long paramLong1, long paramLong2, int paramInt1, int paramInt2);

  static
  {
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    d3dImagePipe = new D3DDrawImage();
    d3dTextPipe = new D3DTextRenderer(localD3DRenderQueue);
    d3dRenderPipe = new D3DRenderer(localD3DRenderQueue);
    if (GraphicsPrimitive.tracingEnabled())
    {
      d3dTextPipe = d3dTextPipe.traceWrap();
      d3dRenderPipe = d3dRenderPipe.traceWrap();
    }
    d3dAAPgramPipe = d3dRenderPipe.getAAParallelogramPipe();
    d3dTxRenderPipe = new sun.java2d.pipe.PixelToParallelogramConverter(d3dRenderPipe, d3dRenderPipe, 1D, 0.25D, true);
    D3DBlitLoops.register();
    D3DMaskFill.register();
    D3DMaskBlit.register();
  }

  static class D3DDataBufferNative extends DataBufferNative
  {
    int pixel;

    protected D3DDataBufferNative(SurfaceData paramSurfaceData, int paramInt1, int paramInt2, int paramInt3)
    {
      super(paramSurfaceData, paramInt1, paramInt2, paramInt3);
    }

    protected int getElem(int paramInt1, int paramInt2, SurfaceData paramSurfaceData)
    {
      int i;
      D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
      localD3DRenderQueue.lock();
      try
      {
        localD3DRenderQueue.flushAndInvokeNow(new Runnable(this, paramSurfaceData, paramInt1, paramInt2)
        {
          public void run()
          {
            this.this$0.pixel = D3DSurfaceData.access$100(this.val$sData.getNativeOps(), this.val$x, this.val$y);
          }
        });
      }
      finally
      {
        i = this.pixel;
        localD3DRenderQueue.unlock();
      }
      return i;
    }

    protected void setElem(int paramInt1, int paramInt2, int paramInt3, SurfaceData paramSurfaceData)
    {
      D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
      localD3DRenderQueue.lock();
      try
      {
        localD3DRenderQueue.flushAndInvokeNow(new Runnable(this, paramSurfaceData, paramInt1, paramInt2, paramInt3)
        {
          public void run()
          {
            D3DSurfaceData.access$200(this.val$sData.getNativeOps(), this.val$x, this.val$y, this.val$pixel);
          }
        });
        paramSurfaceData.markDirty();
      }
      finally
      {
        localD3DRenderQueue.unlock();
      }
    }
  }

  public static class D3DWindowSurfaceData extends D3DSurfaceData
  {
    public D3DWindowSurfaceData(WComponentPeer paramWComponentPeer, D3DGraphicsConfig paramD3DGraphicsConfig)
    {
      super(paramWComponentPeer, paramD3DGraphicsConfig, paramWComponentPeer.getBounds().width, paramWComponentPeer.getBounds().height, null, paramWComponentPeer.getColorModel(), 1, 3, ExtendedBufferCapabilities.VSyncType.VSYNC_DEFAULT, 1);
    }

    public SurfaceData getReplacement()
    {
      ScreenUpdateManager localScreenUpdateManager = ScreenUpdateManager.getInstance();
      return localScreenUpdateManager.getReplacementScreenSurface(this.peer, this);
    }

    public Object getDestination()
    {
      return this.peer.getTarget();
    }

    void restoreSurface()
    {
      Window localWindow = this.graphicsDevice.getFullScreenWindow();
      if ((localWindow != null) && (localWindow != this.peer.getTarget()))
        throw new InvalidPipeException("Can't restore onscreen surface when in full-screen mode");
      super.restoreSurface();
      setSurfaceLost(false);
      D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
      localD3DRenderQueue.lock();
      try
      {
        getContext().invalidateContext();
      }
      finally
      {
        localD3DRenderQueue.unlock();
      }
    }
  }
}