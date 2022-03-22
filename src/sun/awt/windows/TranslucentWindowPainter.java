package sun.awt.windows;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.DestSurfaceProvider;
import sun.java2d.InvalidPipeException;
import sun.java2d.Surface;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.opengl.WGLSurfaceData;
import sun.java2d.pipe.BufferedContext;
import sun.java2d.pipe.RenderQueue;
import sun.java2d.pipe.hw.AccelGraphicsConfig;
import sun.java2d.pipe.hw.AccelSurface;
import sun.java2d.pipe.hw.ContextCapabilities;
import sun.security.action.GetPropertyAction;

public abstract class TranslucentWindowPainter
{
  protected Window window;
  protected WWindowPeer peer;
  private static final boolean forceOpt = Boolean.valueOf((String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.twp.forceopt", "false"))).booleanValue();
  private static final boolean forceSW = Boolean.valueOf((String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.twp.forcesw", "false"))).booleanValue();

  public static TranslucentWindowPainter createInstance(WWindowPeer paramWWindowPeer)
  {
    GraphicsConfiguration localGraphicsConfiguration = paramWWindowPeer.getGraphicsConfiguration();
    if ((!(forceSW)) && (localGraphicsConfiguration instanceof AccelGraphicsConfig))
    {
      String str = localGraphicsConfiguration.getClass().getSimpleName();
      AccelGraphicsConfig localAccelGraphicsConfig = (AccelGraphicsConfig)localGraphicsConfiguration;
      if (((localAccelGraphicsConfig.getContextCapabilities().getCaps() & 0x100) != 0) || (forceOpt))
      {
        if (str.startsWith("D3D"))
          return new VIOptD3DWindowPainter(paramWWindowPeer);
        if ((forceOpt) && (str.startsWith("WGL")))
          return new VIOptWGLWindowPainter(paramWWindowPeer);
      }
    }
    return new BIWindowPainter(paramWWindowPeer);
  }

  protected TranslucentWindowPainter(WWindowPeer paramWWindowPeer)
  {
    this.peer = paramWWindowPeer;
    this.window = ((Window)paramWWindowPeer.getTarget());
  }

  protected abstract Image getBackBuffer();

  protected abstract boolean update(Image paramImage);

  public abstract void flush();

  public void updateWindow(Image paramImage)
  {
    boolean bool = false;
    if ((paramImage != null) && (((this.window.getWidth() != paramImage.getWidth(null)) || (this.window.getHeight() != paramImage.getHeight(null)))))
    {
      Image localImage = getBackBuffer();
      Graphics2D localGraphics2D = (Graphics2D)paramImage.getGraphics();
      localGraphics2D.drawImage(paramImage, 0, 0, null);
      paramImage = localImage;
    }
    do
    {
      if (paramImage == null)
      {
        paramImage = getBackBuffer();
        this.window.paintAll(paramImage.getGraphics());
      }
      this.peer.paintAppletWarning((Graphics2D)paramImage.getGraphics(), paramImage.getWidth(null), paramImage.getHeight(null));
      bool = update(paramImage);
      if (!(bool))
        paramImage = null;
    }
    while (!(bool));
  }

  private static final Image clearImage(Image paramImage)
  {
    Graphics2D localGraphics2D = (Graphics2D)paramImage.getGraphics();
    int i = paramImage.getWidth(null);
    int j = paramImage.getHeight(null);
    localGraphics2D.setComposite(AlphaComposite.Src);
    localGraphics2D.setColor(new Color(0, 0, 0, 0));
    localGraphics2D.fillRect(0, 0, i, j);
    return paramImage;
  }

  private static class BIWindowPainter extends TranslucentWindowPainter
  {
    private WeakReference<BufferedImage> biRef;

    protected BIWindowPainter(WWindowPeer paramWWindowPeer)
    {
      super(paramWWindowPeer);
    }

    private BufferedImage getBIBackBuffer()
    {
      int i = this.window.getWidth();
      int j = this.window.getHeight();
      BufferedImage localBufferedImage = (this.biRef == null) ? null : (BufferedImage)this.biRef.get();
      if ((localBufferedImage == null) || (localBufferedImage.getWidth() != i) || (localBufferedImage.getHeight() != j))
      {
        if (localBufferedImage != null)
        {
          localBufferedImage.flush();
          localBufferedImage = null;
        }
        localBufferedImage = new BufferedImage(i, j, 3);
        this.biRef = new WeakReference(localBufferedImage);
      }
      return ((BufferedImage)TranslucentWindowPainter.access$000(localBufferedImage));
    }

    protected Image getBackBuffer()
    {
      return getBIBackBuffer();
    }

    protected boolean update(Image paramImage)
    {
      VolatileImage localVolatileImage = null;
      if (paramImage instanceof BufferedImage)
      {
        localObject = (BufferedImage)paramImage;
        int[] arrayOfInt1 = ((DataBufferInt)((BufferedImage)localObject).getRaster().getDataBuffer()).getData();
        this.peer.updateWindowImpl(arrayOfInt1, ((BufferedImage)localObject).getWidth(), ((BufferedImage)localObject).getHeight());
        return true;
      }
      if (paramImage instanceof VolatileImage)
      {
        localVolatileImage = (VolatileImage)paramImage;
        if (paramImage instanceof DestSurfaceProvider)
        {
          localObject = ((DestSurfaceProvider)paramImage).getDestSurface();
          if (localObject instanceof BufImgSurfaceData)
          {
            int i = localVolatileImage.getWidth();
            int j = localVolatileImage.getHeight();
            BufImgSurfaceData localBufImgSurfaceData = (BufImgSurfaceData)localObject;
            int[] arrayOfInt3 = ((DataBufferInt)localBufImgSurfaceData.getRaster(0, 0, i, j).getDataBuffer()).getData();
            this.peer.updateWindowImpl(arrayOfInt3, i, j);
            return true;
          }
        }
      }
      java.lang.Object localObject = getBIBackBuffer();
      Graphics2D localGraphics2D = (Graphics2D)((BufferedImage)localObject).getGraphics();
      localGraphics2D.setComposite(AlphaComposite.Src);
      localGraphics2D.drawImage(paramImage, 0, 0, null);
      int[] arrayOfInt2 = ((DataBufferInt)((BufferedImage)localObject).getRaster().getDataBuffer()).getData();
      this.peer.updateWindowImpl(arrayOfInt2, ((BufferedImage)localObject).getWidth(), ((BufferedImage)localObject).getHeight());
      return (!(localVolatileImage.contentsLost()));
    }

    public void flush()
    {
      if (this.biRef != null)
        this.biRef.clear();
    }
  }

  private static class VIOptD3DWindowPainter extends TranslucentWindowPainter.VIOptWindowPainter
  {
    protected VIOptD3DWindowPainter(WWindowPeer paramWWindowPeer)
    {
      super(paramWWindowPeer);
    }

    protected boolean updateWindowAccel(long paramLong, int paramInt1, int paramInt2)
    {
      return D3DSurfaceData.updateWindowAccelImpl(paramLong, this.peer.getData(), paramInt1, paramInt2);
    }
  }

  private static class VIOptWGLWindowPainter extends TranslucentWindowPainter.VIOptWindowPainter
  {
    protected VIOptWGLWindowPainter(WWindowPeer paramWWindowPeer)
    {
      super(paramWWindowPeer);
    }

    protected boolean updateWindowAccel(long paramLong, int paramInt1, int paramInt2)
    {
      return WGLSurfaceData.updateWindowAccelImpl(paramLong, this.peer, paramInt1, paramInt2);
    }
  }

  private static abstract class VIOptWindowPainter extends TranslucentWindowPainter.VIWindowPainter
  {
    protected VIOptWindowPainter(WWindowPeer paramWWindowPeer)
    {
      super(paramWWindowPeer);
    }

    protected abstract boolean updateWindowAccel(long paramLong, int paramInt1, int paramInt2);

    protected boolean update(Image paramImage)
    {
      if (paramImage instanceof DestSurfaceProvider)
      {
        Surface localSurface = ((DestSurfaceProvider)paramImage).getDestSurface();
        if (localSurface instanceof AccelSurface)
        {
          int i = paramImage.getWidth(null);
          int j = paramImage.getHeight(null);
          boolean[] arrayOfBoolean = { false };
          AccelSurface localAccelSurface = (AccelSurface)localSurface;
          RenderQueue localRenderQueue = localAccelSurface.getContext().getRenderQueue();
          localRenderQueue.lock();
          try
          {
            localAccelSurface.getContext();
            BufferedContext.validateContext(localAccelSurface);
            localRenderQueue.flushAndInvokeNow(new Runnable(this, localAccelSurface, arrayOfBoolean, i, j)
            {
              public void run()
              {
                long l = this.val$as.getNativeOps();
                this.val$arr[0] = this.this$0.updateWindowAccel(l, this.val$w, this.val$h);
              }
            });
          }
          catch (InvalidPipeException localInvalidPipeException)
          {
          }
          finally
          {
            localRenderQueue.unlock();
          }
          return arrayOfBoolean[0];
        }
      }
      return super.update(paramImage);
    }
  }

  private static class VIWindowPainter extends TranslucentWindowPainter.BIWindowPainter
  {
    private WeakReference<VolatileImage> viRef;

    protected VIWindowPainter(WWindowPeer paramWWindowPeer)
    {
      super(paramWWindowPeer);
    }

    protected Image getBackBuffer()
    {
      int i = this.window.getWidth();
      int j = this.window.getHeight();
      GraphicsConfiguration localGraphicsConfiguration = this.peer.getGraphicsConfiguration();
      VolatileImage localVolatileImage = (this.viRef == null) ? null : (VolatileImage)this.viRef.get();
      if ((localVolatileImage == null) || (localVolatileImage.getWidth() != i) || (localVolatileImage.getHeight() != j) || (localVolatileImage.validate(localGraphicsConfiguration) == 2))
      {
        if (localVolatileImage != null)
        {
          localVolatileImage.flush();
          localVolatileImage = null;
        }
        if (localGraphicsConfiguration instanceof AccelGraphicsConfig)
        {
          AccelGraphicsConfig localAccelGraphicsConfig = (AccelGraphicsConfig)localGraphicsConfiguration;
          localVolatileImage = localAccelGraphicsConfig.createCompatibleVolatileImage(i, j, 3, 2);
        }
        if (localVolatileImage == null)
          localVolatileImage = localGraphicsConfiguration.createCompatibleVolatileImage(i, j, 3);
        localVolatileImage.validate(localGraphicsConfiguration);
        this.viRef = new WeakReference(localVolatileImage);
      }
      return TranslucentWindowPainter.access$000(localVolatileImage);
    }

    public void flush()
    {
      if (this.viRef != null)
      {
        VolatileImage localVolatileImage = (VolatileImage)this.viRef.get();
        if (localVolatileImage != null)
        {
          localVolatileImage.flush();
          localVolatileImage = null;
        }
        this.viRef.clear();
      }
    }
  }
}