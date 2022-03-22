package sun.awt.windows;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.peer.WindowPeer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.awt.AWTAccessor;
import sun.awt.AWTAccessor.WindowAccessor;
import sun.awt.AppContext;
import sun.awt.DebugHelper;
import sun.awt.EmbeddedFrame;
import sun.awt.SunToolkit;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsEnvironment;

public class WWindowPeer extends WPanelPeer
  implements WindowPeer
{
  private static final DebugHelper dbg = DebugHelper.create(WWindowPeer.class);
  private static final Logger log = Logger.getLogger("sun.awt.windows.WWindowPeer");
  private WWindowPeer modalBlocker = null;
  private static Field peerField;
  private static Method getWindowsMethod;
  static Vector allWindows;
  protected boolean visible = false;
  private volatile int sysX = 0;
  private volatile int sysY = 0;
  private volatile int sysW = 0;
  private volatile int sysH = 0;
  private float opacity = 1F;
  private boolean isOpaque = true;
  private volatile TranslucentWindowPainter painter;

  private static native void initIDs();

  protected void disposeImpl()
  {
    GraphicsConfiguration localGraphicsConfiguration = getGraphicsConfiguration();
    ((Win32GraphicsDevice)localGraphicsConfiguration.getDevice()).removeDisplayChangedListener(this);
    allWindows.removeElement(this);
    TranslucentWindowPainter localTranslucentWindowPainter = this.painter;
    if (localTranslucentWindowPainter != null)
      localTranslucentWindowPainter.flush();
    super.disposeImpl();
  }

  public void toFront()
  {
    updateFocusableWindowState();
    _toFront();
  }

  native void _toFront();

  public native void toBack();

  public native void setAlwaysOnTopNative(boolean paramBoolean);

  public void setAlwaysOnTop(boolean paramBoolean)
  {
    if (((paramBoolean) && (((Window)this.target).isVisible())) || (!(paramBoolean)))
      setAlwaysOnTopNative(paramBoolean);
  }

  public void updateFocusableWindowState()
  {
    setFocusableWindow(((Window)this.target).isFocusableWindow());
  }

  native void setFocusableWindow(boolean paramBoolean);

  public void setTitle(String paramString)
  {
    if (paramString == null)
      paramString = new String("");
    _setTitle(paramString);
  }

  native void _setTitle(String paramString);

  public void setResizable(boolean paramBoolean)
  {
    _setResizable(paramBoolean);
  }

  public native void _setResizable(boolean paramBoolean);

  WWindowPeer(Window paramWindow)
  {
    super(paramWindow);
  }

  void initialize()
  {
    super.initialize();
    updateInsets(this.insets_);
    Font localFont = ((Window)this.target).getFont();
    if (localFont == null)
    {
      localFont = defaultFont;
      ((Window)this.target).setFont(localFont);
      setFont(localFont);
    }
    GraphicsConfiguration localGraphicsConfiguration = getGraphicsConfiguration();
    ((Win32GraphicsDevice)localGraphicsConfiguration.getDevice()).addDisplayChangedListener(this);
    updateIconImages();
  }

  native void createAwtWindow(WComponentPeer paramWComponentPeer);

  void create(WComponentPeer paramWComponentPeer)
  {
    createAwtWindow(paramWComponentPeer);
  }

  protected void realShow()
  {
    super.show();
    this.visible = true;
  }

  public void show()
  {
    updateFocusableWindowState();
    boolean bool = ((Window)this.target).isAlwaysOnTop();
    updateGC();
    resetTargetGC();
    realShow();
    updateMinimumSize();
    if ((((Window)this.target).isAlwaysOnTopSupported()) && (bool))
      setAlwaysOnTop(bool);
    updateWindow(null);
  }

  public void hide()
  {
    super.hide();
    this.visible = false;
  }

  native void updateInsets(Insets paramInsets);

  static native int getSysMinWidth();

  static native int getSysMinHeight();

  static native int getSysIconWidth();

  static native int getSysIconHeight();

  static native int getSysSmIconWidth();

  static native int getSysSmIconHeight();

  native void setIconImagesData(int[] paramArrayOfInt1, int paramInt1, int paramInt2, int[] paramArrayOfInt2, int paramInt3, int paramInt4);

  synchronized native void reshapeFrame(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public boolean requestWindowFocus()
  {
    return false;
  }

  public boolean focusAllowedFor()
  {
    Window localWindow = (Window)this.target;
    if ((!(localWindow.isVisible())) || (!(localWindow.isEnabled())) || (!(localWindow.isFocusable())))
      return false;
    return (!(isModalBlocked()));
  }

  public void updateMinimumSize()
  {
    Dimension localDimension = null;
    if (((Component)this.target).isMinimumSizeSet())
      localDimension = ((Component)this.target).getMinimumSize();
    if (localDimension != null)
    {
      int i = getSysMinWidth();
      int j = getSysMinHeight();
      int k = (localDimension.width >= i) ? localDimension.width : i;
      int l = (localDimension.height >= j) ? localDimension.height : j;
      setMinSize(k, l);
    }
    else
    {
      setMinSize(0, 0);
    }
  }

  public void updateIconImages()
  {
    List localList = ((Window)this.target).getIconImages();
    if ((localList == null) || (localList.size() == 0))
    {
      setIconImagesData(null, 0, 0, null, 0, 0);
    }
    else
    {
      int i = getSysIconWidth();
      int j = getSysIconHeight();
      int k = getSysSmIconWidth();
      int l = getSysSmIconHeight();
      DataBufferInt localDataBufferInt1 = SunToolkit.getScaledIconData(localList, i, j);
      DataBufferInt localDataBufferInt2 = SunToolkit.getScaledIconData(localList, k, l);
      if ((localDataBufferInt1 != null) && (localDataBufferInt2 != null))
        setIconImagesData(localDataBufferInt1.getData(), i, j, localDataBufferInt2.getData(), k, l);
      else
        setIconImagesData(null, 0, 0, null, 0, 0);
    }
  }

  native void setMinSize(int paramInt1, int paramInt2);

  public boolean isModalBlocked()
  {
    return (this.modalBlocker != null);
  }

  public void setModalBlocked(Dialog paramDialog, boolean paramBoolean)
  {
    synchronized (((Component)getTarget()).getTreeLock())
    {
      WWindowPeer localWWindowPeer = (WWindowPeer)paramDialog.getPeer();
      if (paramBoolean)
      {
        this.modalBlocker = localWWindowPeer;
        if (localWWindowPeer instanceof WFileDialogPeer)
          ((WFileDialogPeer)localWWindowPeer).blockWindow(this);
        else if (localWWindowPeer instanceof WPrintDialogPeer)
          ((WPrintDialogPeer)localWWindowPeer).blockWindow(this);
        else
          modalDisable(this.modalBlocker);
      }
      else
      {
        this.modalBlocker = null;
        if (localWWindowPeer instanceof WFileDialogPeer)
          ((WFileDialogPeer)localWWindowPeer).unblockWindow(this);
        else if (localWWindowPeer instanceof WPrintDialogPeer)
          ((WPrintDialogPeer)localWWindowPeer).unblockWindow(this);
        modalEnable();
      }
    }
  }

  native void modalDisable(WWindowPeer paramWWindowPeer);

  native void modalDisableByHWnd(long paramLong);

  native void modalEnable();

  public long[] getWindowHandles()
  {
    Window localWindow;
    Window[] arrayOfWindow1 = null;
    try
    {
      AppContext localAppContext = SunToolkit.targetToAppContext(this.target);
      if (localAppContext != null)
        arrayOfWindow1 = (Window[])(Window[])getWindowsMethod.invoke(Window.class, new Object[] { localAppContext });
    }
    catch (Exception localException1)
    {
      if (log.isLoggable(Level.FINER))
        log.log(Level.FINER, "Exception occured in WWindowPeer.getWindowHandles()", localException1);
    }
    if (arrayOfWindow1 == null)
      return null;
    long[] arrayOfLong = new long[arrayOfWindow1.length];
    Arrays.fill(arrayOfLong, 3412047308433391616L);
    int i = 0;
    Window[] arrayOfWindow2 = arrayOfWindow1;
    int j = arrayOfWindow2.length;
    for (int k = 0; k < j; ++k)
    {
      localWindow = arrayOfWindow2[k];
      if (localWindow instanceof EmbeddedFrame)
        try
        {
          WEmbeddedFramePeer localWEmbeddedFramePeer = (WEmbeddedFramePeer)peerField.get(localWindow);
          if (localWEmbeddedFramePeer != null)
            arrayOfLong[(i++)] = localWEmbeddedFramePeer.getHWnd();
        }
        catch (Exception localException2)
        {
          if (log.isLoggable(Level.FINER))
            log.log(Level.FINER, "Exception occured in WWindowPeer.getWindowHandles()", localException2);
        }
    }
    arrayOfWindow2 = arrayOfWindow1;
    j = arrayOfWindow2.length;
    for (k = 0; k < j; ++k)
    {
      localWindow = arrayOfWindow2[k];
      if (!(localWindow instanceof EmbeddedFrame))
        try
        {
          WWindowPeer localWWindowPeer = (WWindowPeer)peerField.get(localWindow);
          if (localWWindowPeer != null)
            arrayOfLong[(i++)] = localWWindowPeer.getHWnd();
        }
        catch (Exception localException3)
        {
          if (log.isLoggable(Level.FINER))
            log.log(Level.FINER, "Exception occured in WWindowPeer.getWindowHandles()", localException3);
        }
    }
    return arrayOfLong;
  }

  void draggedToNewScreen()
  {
    SunToolkit.executeOnEventHandlerThread((Component)this.target, new Runnable(this)
    {
      public void run()
      {
        this.this$0.displayChanged();
      }
    });
  }

  void clearLocalGC()
  {
  }

  public void updateGC()
  {
    Win32GraphicsDevice localWin32GraphicsDevice2;
    int i = getScreenImOn();
    Win32GraphicsDevice localWin32GraphicsDevice1 = (Win32GraphicsDevice)this.winGraphicsConfig.getDevice();
    GraphicsDevice[] arrayOfGraphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    if (i >= arrayOfGraphicsDevice.length)
      localWin32GraphicsDevice2 = (Win32GraphicsDevice)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    else
      localWin32GraphicsDevice2 = (Win32GraphicsDevice)arrayOfGraphicsDevice[i];
    this.winGraphicsConfig = ((Win32GraphicsConfig)localWin32GraphicsDevice2.getDefaultConfiguration());
    if (localWin32GraphicsDevice1 != localWin32GraphicsDevice2)
    {
      localWin32GraphicsDevice1.removeDisplayChangedListener(this);
      localWin32GraphicsDevice2.addDisplayChangedListener(this);
    }
  }

  public void displayChanged()
  {
    updateGC();
    super.displayChanged();
  }

  private native int getScreenImOn();

  public void grab()
  {
    nativeGrab();
  }

  public void ungrab()
  {
    nativeUngrab();
  }

  private native void nativeGrab();

  private native void nativeUngrab();

  private final boolean hasWarningWindow()
  {
    return (((Window)this.target).getWarningString() != null);
  }

  boolean isTargetUndecorated()
  {
    return true;
  }

  Rectangle constrainBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i3;
    int i4;
    Win32GraphicsConfig localWin32GraphicsConfig = this.winGraphicsConfig;
    if ((!(hasWarningWindow())) || (localWin32GraphicsConfig == null))
      return new Rectangle(paramInt1, paramInt2, paramInt3, paramInt4);
    int i = paramInt1;
    int j = paramInt2;
    int k = paramInt3;
    int l = paramInt4;
    Window localWindow = (Window)this.target;
    Rectangle localRectangle = localWin32GraphicsConfig.getBounds();
    Insets localInsets = localWindow.getToolkit().getScreenInsets(localWin32GraphicsConfig);
    int i1 = localRectangle.width - localInsets.left - localInsets.right;
    int i2 = localRectangle.height - localInsets.top - localInsets.bottom;
    if ((!(this.visible)) || (isTargetUndecorated()))
    {
      i3 = localRectangle.x + localInsets.left;
      i4 = localRectangle.y + localInsets.top;
      if (k > i1)
        k = i1;
      if (l > i2)
        l = i2;
      if (i < i3)
        i = i3;
      else if (i + k > i3 + i1)
        i = i3 + i1 - k;
      if (j < i4)
        j = i4;
      else if (j + l > i4 + i2)
        j = i4 + i2 - l;
    }
    else
    {
      i3 = Math.max(i1, this.sysW);
      i4 = Math.max(i2, this.sysH);
      if (k > i3)
        k = i3;
      if (l > i4)
        l = i4;
    }
    return new Rectangle(i, j, k, l);
  }

  public void setBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    Rectangle localRectangle = constrainBounds(paramInt1, paramInt2, paramInt3, paramInt4);
    this.sysX = localRectangle.x;
    this.sysY = localRectangle.y;
    this.sysW = localRectangle.width;
    this.sysH = localRectangle.height;
    super.setBounds(localRectangle.x, localRectangle.y, localRectangle.width, localRectangle.height, paramInt5);
  }

  public void print(Graphics paramGraphics)
  {
    Shape localShape = AWTAccessor.getWindowAccessor().getShape((Window)this.target);
    if (localShape != null)
      paramGraphics.setClip(localShape);
    super.print(paramGraphics);
  }

  private void replaceSurfaceDataRecursively(Component paramComponent)
  {
    if (paramComponent instanceof Container)
    {
      localObject = ((Container)paramComponent).getComponents();
      int i = localObject.length;
      for (int j = 0; j < i; ++j)
      {
        Component localComponent = localObject[j];
        replaceSurfaceDataRecursively(localComponent);
      }
    }
    Object localObject = paramComponent.getPeer();
    if (localObject instanceof WComponentPeer)
      ((WComponentPeer)localObject).replaceSurfaceDataLater();
  }

  private native void setOpacity(int paramInt);

  public void setOpacity(float paramFloat)
  {
    if (!(((SunToolkit)((Window)this.target).getToolkit()).isWindowOpacityControlSupported()))
      return;
    if ((paramFloat < 0F) || (paramFloat > 1F))
      throw new IllegalArgumentException("The value of opacity should be in the range [0.0f .. 1.0f].");
    if (((this.opacity == 1F) && (paramFloat < 1F)) || ((this.opacity < 1F) && (paramFloat == 1F) && (!(Win32GraphicsEnvironment.isVistaOS()))))
      replaceSurfaceDataRecursively((Component)getTarget());
    this.opacity = paramFloat;
    int i = (int)(paramFloat * 255.0F);
    if (i < 0)
      i = 0;
    if (i > 255)
      i = 255;
    setOpacity(i);
    updateWindow(null);
  }

  private native void setOpaqueImpl(boolean paramBoolean);

  public void setOpaque(boolean paramBoolean)
  {
    Object localObject;
    Window localWindow = (Window)getTarget();
    SunToolkit localSunToolkit = (SunToolkit)localWindow.getToolkit();
    if ((!(localSunToolkit.isWindowTranslucencySupported())) || (!(localSunToolkit.isTranslucencyCapable(localWindow.getGraphicsConfiguration()))))
      return;
    int i = (this.isOpaque != paramBoolean) ? 1 : 0;
    boolean bool = Win32GraphicsEnvironment.isVistaOS();
    if ((i != 0) && (!(bool)))
      replaceSurfaceDataRecursively(localWindow);
    this.isOpaque = paramBoolean;
    setOpaqueImpl(paramBoolean);
    if (i != 0)
      if (paramBoolean)
      {
        localObject = this.painter;
        if (localObject != null)
        {
          ((TranslucentWindowPainter)localObject).flush();
          this.painter = null;
        }
      }
      else
      {
        this.painter = TranslucentWindowPainter.createInstance(this);
      }
    if ((i != 0) && (bool))
    {
      localObject = AWTAccessor.getWindowAccessor().getShape(localWindow);
      if (localObject != null)
        AWTAccessor.getWindowAccessor().setShape(localWindow, (Shape)localObject);
    }
    updateWindow(null);
  }

  native void updateWindowImpl(int[] paramArrayOfInt, int paramInt1, int paramInt2);

  public void updateWindow(BufferedImage paramBufferedImage)
  {
    if (this.isOpaque)
      return;
    TranslucentWindowPainter localTranslucentWindowPainter = this.painter;
    if (localTranslucentWindowPainter != null)
      localTranslucentWindowPainter.updateWindow(paramBufferedImage);
    else if (log.isLoggable(Level.FINER))
      log.log(Level.FINER, "Translucent window painter is null in updateWindow");
  }

  public void paintAppletWarning(Graphics2D paramGraphics2D, int paramInt1, int paramInt2)
  {
  }

  public void handleEvent(AWTEvent paramAWTEvent)
  {
    if ((!(this.isOpaque)) && (paramAWTEvent.getID() == 801))
      updateWindow(null);
    else
      super.handleEvent(paramAWTEvent);
  }

  static
  {
    initIDs();
    peerField = WToolkit.getField(Component.class, "peer");
    getWindowsMethod = WToolkit.getMethod(Window.class, "getWindows", new Class[] { AppContext.class });
    allWindows = new Vector();
  }
}