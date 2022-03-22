package sun.awt.windows;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.dnd.peer.DropTargetPeer;
import java.awt.event.InvocationEvent;
import java.awt.event.KeyEvent;
import java.awt.event.PaintEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.awt.CausedFocusEvent.Cause;
import sun.awt.DebugHelper;
import sun.awt.DisplayChangedListener;
import sun.awt.GlobalCursorManager;
import sun.awt.PaintEventDispatcher;
import sun.awt.RepaintArea;
import sun.awt.SunToolkit;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsEnvironment;
import sun.awt.event.IgnorePaintEvent;
import sun.awt.image.SunVolatileImage;
import sun.awt.image.ToolkitImage;
import sun.java2d.InvalidPipeException;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DSurfaceData.D3DWindowSurfaceData;
import sun.java2d.opengl.OGLSurfaceData;
import sun.java2d.pipe.Region;

public abstract class WComponentPeer extends WObjectPeer
  implements ComponentPeer, DropTargetPeer, DisplayChangedListener
{
  protected long hwnd;
  private static final DebugHelper dbg = DebugHelper.create(WComponentPeer.class);
  private static final Logger shapeLog = Logger.getLogger("sun.awt.windows.shape.WComponentPeer");
  SurfaceData surfaceData;
  private RepaintArea paintArea;
  protected Win32GraphicsConfig winGraphicsConfig;
  boolean isLayouting = false;
  boolean paintPending = false;
  int oldWidth = -1;
  int oldHeight = -1;
  private int numBackBuffers = 0;
  private VolatileImage backBuffer = null;
  private BufferCapabilities backBufferCaps = null;
  private Color foreground;
  private Color background;
  private Font font;
  int nDropTargets;
  long nativeDropTargetContext;
  public int serialNum = 0;
  private static final double BANDING_DIVISOR = 4.0D;
  static final Font defaultFont;
  private int updateX1;
  private int updateY1;
  private int updateX2;
  private int updateY2;

  static native void wheelInit();

  public native boolean isObscured();

  public boolean canDetermineObscurity()
  {
    return true;
  }

  public synchronized native void pShow();

  public synchronized native void hide();

  public synchronized native void enable();

  public synchronized native void disable();

  public long getHWnd()
  {
    return this.hwnd;
  }

  public native Point getLocationOnScreen();

  public void setVisible(boolean paramBoolean)
  {
    if (paramBoolean)
      show();
    else
      hide();
  }

  public void show()
  {
    Dimension localDimension = ((Component)this.target).getSize();
    this.oldHeight = localDimension.height;
    this.oldWidth = localDimension.width;
    pShow();
  }

  public void setEnabled(boolean paramBoolean)
  {
    if (paramBoolean)
      enable();
    else
      disable();
  }

  private native void reshapeNoCheck(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public void setBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    this.paintPending = ((paramInt3 != this.oldWidth) || (paramInt4 != this.oldHeight));
    if ((paramInt5 & 0x4000) != 0)
      reshapeNoCheck(paramInt1, paramInt2, paramInt3, paramInt4);
    else
      reshape(paramInt1, paramInt2, paramInt3, paramInt4);
    if ((paramInt3 != this.oldWidth) || (paramInt4 != this.oldHeight))
    {
      try
      {
        replaceSurfaceData();
      }
      catch (InvalidPipeException localInvalidPipeException)
      {
      }
      this.oldWidth = paramInt3;
      this.oldHeight = paramInt4;
    }
    this.serialNum += 1;
  }

  void dynamicallyLayoutContainer()
  {
    Container localContainer = (Container)this.target;
    WToolkit.executeOnEventHandlerThread(localContainer, new Runnable(this, localContainer)
    {
      public void run()
      {
        this.val$cont.invalidate();
        this.val$cont.validate();
        if ((this.this$0.surfaceData instanceof D3DSurfaceData.D3DWindowSurfaceData) || (this.this$0.surfaceData instanceof OGLSurfaceData))
          try
          {
            this.this$0.replaceSurfaceData();
          }
          catch (InvalidPipeException localInvalidPipeException)
          {
          }
      }
    });
  }

  void paintDamagedAreaImmediately()
  {
    updateWindow();
    WToolkit.getWToolkit();
    WToolkit.flushPendingEvents();
    this.paintArea.paint(this.target, shouldClearRectBeforePaint());
  }

  synchronized native void updateWindow();

  public void paint(Graphics paramGraphics)
  {
    ((Component)this.target).paint(paramGraphics);
  }

  public void repaint(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
  }

  private native int[] createPrintedPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public void print(Graphics paramGraphics)
  {
    Component localComponent = (Component)this.target;
    int i = localComponent.getWidth();
    int j = localComponent.getHeight();
    int k = (int)(j / 4.0D);
    if (k == 0)
      k = j;
    int l = 0;
    while (l < j)
    {
      int i1 = l + k - 1;
      if (i1 >= j)
        i1 = j - 1;
      int i2 = i1 - l + 1;
      int[] arrayOfInt = createPrintedPixels(0, l, i, i2);
      if (arrayOfInt != null)
      {
        BufferedImage localBufferedImage = new BufferedImage(i, i2, 2);
        localBufferedImage.setRGB(0, 0, i, i2, arrayOfInt, 0, i);
        paramGraphics.drawImage(localBufferedImage, 0, l, null);
        localBufferedImage.flush();
      }
      l += k;
    }
    localComponent.print(paramGraphics);
  }

  public void coalescePaintEvent(PaintEvent paramPaintEvent)
  {
    Rectangle localRectangle = paramPaintEvent.getUpdateRect();
    if (!(paramPaintEvent instanceof IgnorePaintEvent))
      this.paintArea.add(localRectangle, paramPaintEvent.getID());
  }

  public synchronized native void reshape(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public boolean handleJavaKeyEvent(KeyEvent paramKeyEvent)
  {
    return false;
  }

  native void nativeHandleEvent(AWTEvent paramAWTEvent);

  public void handleEvent(AWTEvent paramAWTEvent)
  {
    int i = paramAWTEvent.getID();
    if ((((Component)this.target).isEnabled()) && (paramAWTEvent instanceof KeyEvent) && (!(((KeyEvent)paramAWTEvent).isConsumed())) && (handleJavaKeyEvent((KeyEvent)paramAWTEvent)))
      return;
    switch (i)
    {
    case 800:
      this.paintPending = false;
    case 801:
      if ((!(this.isLayouting)) && (!(this.paintPending)))
        this.paintArea.paint(this.target, shouldClearRectBeforePaint());
      return;
    }
    nativeHandleEvent(paramAWTEvent);
  }

  public Dimension getMinimumSize()
  {
    return ((Component)this.target).getSize();
  }

  public Dimension getPreferredSize()
  {
    return getMinimumSize();
  }

  public void layout()
  {
  }

  public Rectangle getBounds()
  {
    return ((Component)this.target).getBounds();
  }

  public boolean isFocusable()
  {
    return false;
  }

  public GraphicsConfiguration getGraphicsConfiguration()
  {
    if (this.winGraphicsConfig != null)
      return this.winGraphicsConfig;
    return ((Component)this.target).getGraphicsConfiguration();
  }

  public SurfaceData getSurfaceData()
  {
    return this.surfaceData;
  }

  public void replaceSurfaceData()
  {
    replaceSurfaceData(this.numBackBuffers, this.backBufferCaps);
  }

  public void replaceSurfaceData(int paramInt, BufferCapabilities paramBufferCapabilities)
  {
    SurfaceData localSurfaceData = null;
    VolatileImage localVolatileImage = null;
    synchronized (((Component)this.target).getTreeLock())
    {
      synchronized (this)
      {
        if (this.pData != 3412047308433391616L)
          break label40;
        monitorexit;
        return;
        label40: this.numBackBuffers = paramInt;
        Win32GraphicsConfig localWin32GraphicsConfig = (Win32GraphicsConfig)getGraphicsConfiguration();
        ScreenUpdateManager localScreenUpdateManager = ScreenUpdateManager.getInstance();
        localSurfaceData = this.surfaceData;
        localScreenUpdateManager.dropScreenSurface(localSurfaceData);
        this.surfaceData = localScreenUpdateManager.createScreenSurface(localWin32GraphicsConfig, this, this.numBackBuffers, true);
        if (localSurfaceData == null)
          break label95;
        localSurfaceData.invalidate();
        label95: localVolatileImage = this.backBuffer;
        if (this.numBackBuffers <= 0)
          break label126;
        this.backBufferCaps = paramBufferCapabilities;
        this.backBuffer = localWin32GraphicsConfig.createBackBuffer(this);
        break label143:
        label126: if (this.backBuffer == null)
          break label143;
        this.backBufferCaps = null;
        label143: this.backBuffer = null;
      }
    }
    if (localSurfaceData != null)
    {
      localSurfaceData.flush();
      localSurfaceData = null;
    }
    if (localVolatileImage != null)
    {
      localVolatileImage.flush();
      localSurfaceData = null;
    }
  }

  public void replaceSurfaceDataLater()
  {
    2 local2 = new Runnable(this)
    {
      public void run()
      {
        if (!(this.this$0.isDisposed()))
          try
          {
            this.this$0.replaceSurfaceData();
          }
          catch (InvalidPipeException localInvalidPipeException)
          {
          }
      }
    };
    if (!(PaintEventDispatcher.getPaintEventDispatcher().queueSurfaceDataReplacing((Component)this.target, local2)))
      postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(), local2));
  }

  public void displayChanged()
  {
    try
    {
      replaceSurfaceData();
    }
    catch (InvalidPipeException localInvalidPipeException)
    {
    }
  }

  public void paletteChanged()
  {
  }

  public ColorModel getColorModel()
  {
    GraphicsConfiguration localGraphicsConfiguration = getGraphicsConfiguration();
    if (localGraphicsConfiguration != null)
      return localGraphicsConfiguration.getColorModel();
    return null;
  }

  public ColorModel getDeviceColorModel()
  {
    Win32GraphicsConfig localWin32GraphicsConfig = (Win32GraphicsConfig)getGraphicsConfiguration();
    if (localWin32GraphicsConfig != null)
      return localWin32GraphicsConfig.getDeviceColorModel();
    return null;
  }

  public ColorModel getColorModel(int paramInt)
  {
    GraphicsConfiguration localGraphicsConfiguration = getGraphicsConfiguration();
    if (localGraphicsConfiguration != null)
      return localGraphicsConfiguration.getColorModel(paramInt);
    return null;
  }

  public Toolkit getToolkit()
  {
    return Toolkit.getDefaultToolkit();
  }

  public Graphics getGraphics()
  {
    SurfaceData localSurfaceData = this.surfaceData;
    if ((!(isDisposed())) && (localSurfaceData != null))
    {
      Object localObject1 = this.background;
      if (localObject1 == null)
        localObject1 = SystemColor.window;
      Object localObject2 = this.foreground;
      if (localObject2 == null)
        localObject2 = SystemColor.windowText;
      Font localFont = this.font;
      if (localFont == null)
        localFont = defaultFont;
      ScreenUpdateManager localScreenUpdateManager = ScreenUpdateManager.getInstance();
      return localScreenUpdateManager.createGraphics(localSurfaceData, this, (Color)localObject2, (Color)localObject1, localFont);
    }
    return ((Graphics)(Graphics)null);
  }

  public FontMetrics getFontMetrics(Font paramFont)
  {
    return WFontMetrics.getFontMetrics(paramFont);
  }

  private synchronized native void _dispose();

  protected void disposeImpl()
  {
    SurfaceData localSurfaceData = this.surfaceData;
    this.surfaceData = null;
    ScreenUpdateManager.getInstance().dropScreenSurface(localSurfaceData);
    localSurfaceData.invalidate();
    WToolkit.targetDisposedPeer(this.target, this);
    _dispose();
  }

  public synchronized void setForeground(Color paramColor)
  {
    this.foreground = paramColor;
    _setForeground(paramColor.getRGB());
  }

  public synchronized void setBackground(Color paramColor)
  {
    this.background = paramColor;
    _setBackground(paramColor.getRGB());
  }

  public Color getBackgroundNoSync()
  {
    return this.background;
  }

  public native void _setForeground(int paramInt);

  public native void _setBackground(int paramInt);

  public synchronized void setFont(Font paramFont)
  {
    this.font = paramFont;
    _setFont(paramFont);
  }

  public synchronized native void _setFont(Font paramFont);

  public final void updateCursorImmediately()
  {
    WGlobalCursorManager.getCursorManager().updateCursorImmediately();
  }

  static native boolean processSynchronousLightweightTransfer(Component paramComponent1, Component paramComponent2, boolean paramBoolean1, boolean paramBoolean2, long paramLong);

  public boolean requestFocus(Component paramComponent, boolean paramBoolean1, boolean paramBoolean2, long paramLong, CausedFocusEvent.Cause paramCause)
  {
    if (processSynchronousLightweightTransfer((Component)this.target, paramComponent, paramBoolean1, paramBoolean2, paramLong))
      return true;
    return _requestFocus(paramComponent, paramBoolean1, paramBoolean2, paramLong, paramCause);
  }

  public native boolean _requestFocus(Component paramComponent, boolean paramBoolean1, boolean paramBoolean2, long paramLong, CausedFocusEvent.Cause paramCause);

  public Image createImage(ImageProducer paramImageProducer)
  {
    return new ToolkitImage(paramImageProducer);
  }

  public Image createImage(int paramInt1, int paramInt2)
  {
    Win32GraphicsConfig localWin32GraphicsConfig = (Win32GraphicsConfig)getGraphicsConfiguration();
    return localWin32GraphicsConfig.createAcceleratedImage((Component)this.target, paramInt1, paramInt2);
  }

  public VolatileImage createVolatileImage(int paramInt1, int paramInt2)
  {
    return new SunVolatileImage((Component)this.target, paramInt1, paramInt2);
  }

  public boolean prepareImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    return getToolkit().prepareImage(paramImage, paramInt1, paramInt2, paramImageObserver);
  }

  public int checkImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    return getToolkit().checkImage(paramImage, paramInt1, paramInt2, paramImageObserver);
  }

  public String toString()
  {
    return getClass().getName() + "[" + this.target + "]";
  }

  WComponentPeer(Component paramComponent)
  {
    this.target = paramComponent;
    this.paintArea = new RepaintArea();
    Container localContainer = WToolkit.getNativeContainer(paramComponent);
    WComponentPeer localWComponentPeer = (WComponentPeer)WToolkit.targetToPeer(localContainer);
    create(localWComponentPeer);
    checkCreation();
    this.winGraphicsConfig = ((Win32GraphicsConfig)getGraphicsConfiguration());
    ScreenUpdateManager localScreenUpdateManager = ScreenUpdateManager.getInstance();
    this.surfaceData = localScreenUpdateManager.createScreenSurface(this.winGraphicsConfig, this, this.numBackBuffers, false);
    initialize();
    start();
  }

  abstract void create(WComponentPeer paramWComponentPeer);

  protected void checkCreation()
  {
    if ((this.hwnd == 3412046964836007936L) || (this.pData == 3412046964836007936L))
    {
      if (this.createError != null)
        throw this.createError;
      throw new InternalError("couldn't create component peer");
    }
  }

  synchronized native void start();

  void initialize()
  {
    if (((Component)this.target).isVisible())
      show();
    Color localColor = ((Component)this.target).getForeground();
    if (localColor != null)
      setForeground(localColor);
    Font localFont = ((Component)this.target).getFont();
    if (localFont != null)
      setFont(localFont);
    if (!(((Component)this.target).isEnabled()))
      disable();
    Rectangle localRectangle = ((Component)this.target).getBounds();
    setBounds(localRectangle.x, localRectangle.y, localRectangle.width, localRectangle.height, 3);
  }

  void handleRepaint(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
  }

  void handleExpose(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    postPaintIfNecessary(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  void handlePaint(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    postPaintIfNecessary(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  private void postPaintIfNecessary(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (!(((Component)this.target).getIgnoreRepaint()))
    {
      PaintEvent localPaintEvent = PaintEventDispatcher.getPaintEventDispatcher().createPaintEvent((Component)this.target, paramInt1, paramInt2, paramInt3, paramInt4);
      if (localPaintEvent != null)
        postEvent(localPaintEvent);
    }
  }

  void postEvent(AWTEvent paramAWTEvent)
  {
    WToolkit.postEvent(WToolkit.targetToAppContext(this.target), paramAWTEvent);
  }

  public void beginLayout()
  {
    this.isLayouting = true;
  }

  public void endLayout()
  {
    if ((!(this.paintArea.isEmpty())) && (!(this.paintPending)) && (!(((Component)this.target).getIgnoreRepaint())))
      postEvent(new PaintEvent((Component)this.target, 800, new Rectangle()));
    this.isLayouting = false;
  }

  public native void beginValidate();

  public native void endValidate();

  public Dimension minimumSize()
  {
    return getMinimumSize();
  }

  public Dimension preferredSize()
  {
    return getPreferredSize();
  }

  public synchronized void addDropTarget(DropTarget paramDropTarget)
  {
    if (this.nDropTargets == 0)
      this.nativeDropTargetContext = addNativeDropTarget();
    this.nDropTargets += 1;
  }

  public synchronized void removeDropTarget(DropTarget paramDropTarget)
  {
    this.nDropTargets -= 1;
    if (this.nDropTargets == 0)
    {
      removeNativeDropTarget();
      this.nativeDropTargetContext = 3412047463052214272L;
    }
  }

  native long addNativeDropTarget();

  native void removeNativeDropTarget();

  native boolean nativeHandlesWheelScrolling();

  public boolean handlesWheelScrolling()
  {
    return nativeHandlesWheelScrolling();
  }

  public boolean isPaintPending()
  {
    return ((this.paintPending) && (this.isLayouting));
  }

  public void createBuffers(int paramInt, BufferCapabilities paramBufferCapabilities)
    throws AWTException
  {
    Win32GraphicsConfig localWin32GraphicsConfig = (Win32GraphicsConfig)getGraphicsConfiguration();
    localWin32GraphicsConfig.assertOperationSupported((Component)this.target, paramInt, paramBufferCapabilities);
    try
    {
      replaceSurfaceData(paramInt - 1, paramBufferCapabilities);
    }
    catch (InvalidPipeException localInvalidPipeException)
    {
      throw new AWTException(localInvalidPipeException.getMessage());
    }
  }

  public void destroyBuffers()
  {
    replaceSurfaceData(0, null);
  }

  public void flip(int paramInt1, int paramInt2, int paramInt3, int paramInt4, BufferCapabilities.FlipContents paramFlipContents)
  {
    VolatileImage localVolatileImage = this.backBuffer;
    if (localVolatileImage == null)
      throw new IllegalStateException("Buffers have not been created");
    Win32GraphicsConfig localWin32GraphicsConfig = (Win32GraphicsConfig)getGraphicsConfiguration();
    localWin32GraphicsConfig.flip(this, (Component)this.target, localVolatileImage, paramInt1, paramInt2, paramInt3, paramInt4, paramFlipContents);
  }

  public synchronized Image getBackBuffer()
  {
    VolatileImage localVolatileImage = this.backBuffer;
    if (localVolatileImage == null)
      throw new IllegalStateException("Buffers have not been created");
    return localVolatileImage;
  }

  public BufferCapabilities getBackBufferCaps()
  {
    return this.backBufferCaps;
  }

  public int getBackBuffersNum()
  {
    return this.numBackBuffers;
  }

  public boolean shouldClearRectBeforePaint()
  {
    return true;
  }

  native void pSetParent(ComponentPeer paramComponentPeer);

  public void reparent(ContainerPeer paramContainerPeer)
  {
    pSetParent(paramContainerPeer);
  }

  public boolean isReparentSupported()
  {
    return true;
  }

  public void setBoundsOperation(int paramInt)
  {
  }

  private static final boolean isContainingTopLevelAccelCapable(Component paramComponent)
  {
    while ((paramComponent != null) && (!(paramComponent instanceof WEmbeddedFrame)))
      paramComponent = paramComponent.getParent();
    if (paramComponent == null)
      return true;
    return ((WEmbeddedFramePeer)paramComponent.getPeer()).isAccelCapable();
  }

  public boolean isAccelCapable()
  {
    if (!(isContainingTopLevelAccelCapable((Component)this.target)))
      return false;
    boolean bool = SunToolkit.isContainingTopLevelTranslucent((Component)this.target);
    return ((!(bool)) || (Win32GraphicsEnvironment.isVistaOS()));
  }

  private native void setRectangularShape(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Region paramRegion);

  public void applyShape(Region paramRegion)
  {
    if (shapeLog.isLoggable(Level.FINER))
      shapeLog.finer("*** INFO: Setting shape: PEER: " + this + "; TARGET: " + this.target + "; SHAPE: " + paramRegion);
    if (paramRegion != null)
      setRectangularShape(paramRegion.getLoX(), paramRegion.getLoY(), paramRegion.getHiX(), paramRegion.getHiY(), (paramRegion.isRectangular()) ? null : paramRegion);
    else
      setRectangularShape(0, 0, 0, 0, null);
  }

  static
  {
    wheelInit();
    defaultFont = new Font("Dialog", 0, 12);
  }
}