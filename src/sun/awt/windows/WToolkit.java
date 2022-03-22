package sun.awt.windows;

import java.awt.AWTException;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.JobAttributes;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PageAttributes;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.PrintJob;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.image.ColorModel;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.RobotPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.SystemTrayPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.TrayIconPeer;
import java.awt.peer.WindowPeer;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.awt.AWTAutoShutdown;
import sun.awt.DebugHelper;
import sun.awt.SunToolkit;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsEnvironment;
import sun.font.FontManager;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.opengl.OGLRenderQueue;
import sun.misc.PerformanceLogger;
import sun.print.PrintJob2D;
import sun.security.action.LoadLibraryAction;

public class WToolkit extends SunToolkit
  implements Runnable
{
  private static final Logger log = Logger.getLogger("sun.awt.windows.WToolkit");
  private static final DebugHelper dbg = DebugHelper.create(WToolkit.class);
  static java.awt.GraphicsConfiguration config;
  WClipboard clipboard;
  private Hashtable cacheFontPeer;
  private WDesktopProperties wprops;
  protected boolean dynamicLayoutSetting = false;
  private static boolean loaded = false;
  public static final String DATA_TRANSFERER_CLASS_NAME = "sun.awt.windows.WDataTransferer";
  private final Object anchor = new Object();
  static ColorModel screenmodel;
  private static final String prefix = "DnD.Cursor.";
  private static final String postfix = ".32x32";
  private static final String awtPrefix = "awt.";
  private static final String dndPrefix = "DnD.";

  private static native void initIDs();

  public static void loadLibraries()
  {
    if (!(loaded))
    {
      AccessController.doPrivileged(new LoadLibraryAction("awt"));
      loaded = true;
    }
  }

  private static native void printWindowsVersion();

  private static native void disableCustomPalette();

  public static void resetGC()
  {
    if (GraphicsEnvironment.isHeadless())
      config = null;
    else
      config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
  }

  public static native boolean embeddedInit();

  public static native boolean embeddedDispose();

  public native void embeddedEventLoopIdleProcessing();

  private static native void postDispose();

  public WToolkit()
  {
    if (PerformanceLogger.loggingEnabled())
      PerformanceLogger.setTime("WToolkit construction");
    Disposer.addRecord(this.anchor, new ToolkitDisposer());
    synchronized (this)
    {
      Thread localThread = new Thread(this, "AWT-Windows");
      localThread.setDaemon(true);
      localThread.setPriority(6);
      AWTAutoShutdown.notifyToolkitThreadBusy();
      localThread.start();
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException)
      {
      }
    }
    SunToolkit.setDataTransfererClassName("sun.awt.windows.WDataTransferer");
    setDynamicLayout(true);
  }

  public void run()
  {
    boolean bool = init();
    if (bool)
    {
      ??? = (ThreadGroup)AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          Object localObject = Thread.currentThread().getThreadGroup();
          for (ThreadGroup localThreadGroup = ((ThreadGroup)localObject).getParent(); localThreadGroup != null; localThreadGroup = ((ThreadGroup)localObject).getParent())
            localObject = localThreadGroup;
          return localObject;
        }
      });
      Runtime.getRuntime().addShutdownHook(new Thread((ThreadGroup)???, new Runnable(this)
      {
        public void run()
        {
          WToolkit.access$200(this.this$0);
        }
      }));
    }
    synchronized (this)
    {
      notifyAll();
    }
    if (bool)
      eventLoop();
  }

  private native boolean init();

  private native void eventLoop();

  private native void shutdown();

  public static native void startSecondaryEventLoop();

  public static native void quitSecondaryEventLoop();

  public ButtonPeer createButton(Button paramButton)
  {
    WButtonPeer localWButtonPeer = new WButtonPeer(paramButton);
    targetCreatedPeer(paramButton, localWButtonPeer);
    return localWButtonPeer;
  }

  public TextFieldPeer createTextField(TextField paramTextField)
  {
    WTextFieldPeer localWTextFieldPeer = new WTextFieldPeer(paramTextField);
    targetCreatedPeer(paramTextField, localWTextFieldPeer);
    return localWTextFieldPeer;
  }

  public LabelPeer createLabel(Label paramLabel)
  {
    WLabelPeer localWLabelPeer = new WLabelPeer(paramLabel);
    targetCreatedPeer(paramLabel, localWLabelPeer);
    return localWLabelPeer;
  }

  public ListPeer createList(List paramList)
  {
    WListPeer localWListPeer = new WListPeer(paramList);
    targetCreatedPeer(paramList, localWListPeer);
    return localWListPeer;
  }

  public CheckboxPeer createCheckbox(Checkbox paramCheckbox)
  {
    WCheckboxPeer localWCheckboxPeer = new WCheckboxPeer(paramCheckbox);
    targetCreatedPeer(paramCheckbox, localWCheckboxPeer);
    return localWCheckboxPeer;
  }

  public ScrollbarPeer createScrollbar(Scrollbar paramScrollbar)
  {
    WScrollbarPeer localWScrollbarPeer = new WScrollbarPeer(paramScrollbar);
    targetCreatedPeer(paramScrollbar, localWScrollbarPeer);
    return localWScrollbarPeer;
  }

  public ScrollPanePeer createScrollPane(ScrollPane paramScrollPane)
  {
    WScrollPanePeer localWScrollPanePeer = new WScrollPanePeer(paramScrollPane);
    targetCreatedPeer(paramScrollPane, localWScrollPanePeer);
    return localWScrollPanePeer;
  }

  public TextAreaPeer createTextArea(TextArea paramTextArea)
  {
    WTextAreaPeer localWTextAreaPeer = new WTextAreaPeer(paramTextArea);
    targetCreatedPeer(paramTextArea, localWTextAreaPeer);
    return localWTextAreaPeer;
  }

  public ChoicePeer createChoice(Choice paramChoice)
  {
    WChoicePeer localWChoicePeer = new WChoicePeer(paramChoice);
    targetCreatedPeer(paramChoice, localWChoicePeer);
    return localWChoicePeer;
  }

  public FramePeer createFrame(Frame paramFrame)
  {
    WFramePeer localWFramePeer = new WFramePeer(paramFrame);
    targetCreatedPeer(paramFrame, localWFramePeer);
    return localWFramePeer;
  }

  public CanvasPeer createCanvas(java.awt.Canvas paramCanvas)
  {
    WCanvasPeer localWCanvasPeer = new WCanvasPeer(paramCanvas);
    targetCreatedPeer(paramCanvas, localWCanvasPeer);
    return localWCanvasPeer;
  }

  public PanelPeer createPanel(Panel paramPanel)
  {
    WPanelPeer localWPanelPeer = new WPanelPeer(paramPanel);
    targetCreatedPeer(paramPanel, localWPanelPeer);
    return localWPanelPeer;
  }

  public WindowPeer createWindow(Window paramWindow)
  {
    WWindowPeer localWWindowPeer = new WWindowPeer(paramWindow);
    targetCreatedPeer(paramWindow, localWWindowPeer);
    return localWWindowPeer;
  }

  public DialogPeer createDialog(Dialog paramDialog)
  {
    WDialogPeer localWDialogPeer = new WDialogPeer(paramDialog);
    targetCreatedPeer(paramDialog, localWDialogPeer);
    return localWDialogPeer;
  }

  public FileDialogPeer createFileDialog(FileDialog paramFileDialog)
  {
    WFileDialogPeer localWFileDialogPeer = new WFileDialogPeer(paramFileDialog);
    targetCreatedPeer(paramFileDialog, localWFileDialogPeer);
    return localWFileDialogPeer;
  }

  public MenuBarPeer createMenuBar(MenuBar paramMenuBar)
  {
    WMenuBarPeer localWMenuBarPeer = new WMenuBarPeer(paramMenuBar);
    targetCreatedPeer(paramMenuBar, localWMenuBarPeer);
    return localWMenuBarPeer;
  }

  public MenuPeer createMenu(Menu paramMenu)
  {
    WMenuPeer localWMenuPeer = new WMenuPeer(paramMenu);
    targetCreatedPeer(paramMenu, localWMenuPeer);
    return localWMenuPeer;
  }

  public PopupMenuPeer createPopupMenu(PopupMenu paramPopupMenu)
  {
    WPopupMenuPeer localWPopupMenuPeer = new WPopupMenuPeer(paramPopupMenu);
    targetCreatedPeer(paramPopupMenu, localWPopupMenuPeer);
    return localWPopupMenuPeer;
  }

  public MenuItemPeer createMenuItem(MenuItem paramMenuItem)
  {
    WMenuItemPeer localWMenuItemPeer = new WMenuItemPeer(paramMenuItem);
    targetCreatedPeer(paramMenuItem, localWMenuItemPeer);
    return localWMenuItemPeer;
  }

  public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem paramCheckboxMenuItem)
  {
    WCheckboxMenuItemPeer localWCheckboxMenuItemPeer = new WCheckboxMenuItemPeer(paramCheckboxMenuItem);
    targetCreatedPeer(paramCheckboxMenuItem, localWCheckboxMenuItemPeer);
    return localWCheckboxMenuItemPeer;
  }

  public RobotPeer createRobot(Robot paramRobot, GraphicsDevice paramGraphicsDevice)
  {
    return new WRobotPeer(paramGraphicsDevice);
  }

  public WEmbeddedFramePeer createEmbeddedFrame(WEmbeddedFrame paramWEmbeddedFrame)
  {
    WEmbeddedFramePeer localWEmbeddedFramePeer = new WEmbeddedFramePeer(paramWEmbeddedFrame);
    targetCreatedPeer(paramWEmbeddedFrame, localWEmbeddedFramePeer);
    return localWEmbeddedFramePeer;
  }

  WPrintDialogPeer createWPrintDialog(WPrintDialog paramWPrintDialog)
  {
    WPrintDialogPeer localWPrintDialogPeer = new WPrintDialogPeer(paramWPrintDialog);
    targetCreatedPeer(paramWPrintDialog, localWPrintDialogPeer);
    return localWPrintDialogPeer;
  }

  WPageDialogPeer createWPageDialog(WPageDialog paramWPageDialog)
  {
    WPageDialogPeer localWPageDialogPeer = new WPageDialogPeer(paramWPageDialog);
    targetCreatedPeer(paramWPageDialog, localWPageDialogPeer);
    return localWPageDialogPeer;
  }

  public TrayIconPeer createTrayIcon(TrayIcon paramTrayIcon)
  {
    WTrayIconPeer localWTrayIconPeer = new WTrayIconPeer(paramTrayIcon);
    targetCreatedPeer(paramTrayIcon, localWTrayIconPeer);
    return localWTrayIconPeer;
  }

  public SystemTrayPeer createSystemTray(SystemTray paramSystemTray)
  {
    return new WSystemTrayPeer(paramSystemTray);
  }

  public boolean isTraySupported()
  {
    return (!(isProtectedMode()));
  }

  protected native void setDynamicLayoutNative(boolean paramBoolean);

  public void setDynamicLayout(boolean paramBoolean)
  {
    if (paramBoolean == this.dynamicLayoutSetting)
      return;
    this.dynamicLayoutSetting = paramBoolean;
    setDynamicLayoutNative(paramBoolean);
  }

  protected boolean isDynamicLayoutSet()
  {
    return this.dynamicLayoutSetting;
  }

  protected native boolean isDynamicLayoutSupportedNative();

  public boolean isDynamicLayoutActive()
  {
    return ((isDynamicLayoutSet()) && (isDynamicLayoutSupported()));
  }

  public boolean isFrameStateSupported(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
    case 1:
    case 6:
      return true;
    }
    return false;
  }

  static native ColorModel makeColorModel();

  static ColorModel getStaticColorModel()
  {
    if (GraphicsEnvironment.isHeadless())
      throw new IllegalArgumentException();
    if (config == null)
      resetGC();
    return config.getColorModel();
  }

  public ColorModel getColorModel()
  {
    return getStaticColorModel();
  }

  public Insets getScreenInsets(java.awt.GraphicsConfiguration paramGraphicsConfiguration)
  {
    return getScreenInsets(((Win32GraphicsDevice)paramGraphicsConfiguration.getDevice()).getScreen());
  }

  public int getScreenResolution()
  {
    Win32GraphicsEnvironment localWin32GraphicsEnvironment = (Win32GraphicsEnvironment)GraphicsEnvironment.getLocalGraphicsEnvironment();
    return localWin32GraphicsEnvironment.getXResolution();
  }

  protected native int getScreenWidth();

  protected native int getScreenHeight();

  protected native Insets getScreenInsets(int paramInt);

  public FontMetrics getFontMetrics(Font paramFont)
  {
    if (FontManager.usePlatformFontMetrics())
      return WFontMetrics.getFontMetrics(paramFont);
    return super.getFontMetrics(paramFont);
  }

  public FontPeer getFontPeer(String paramString, int paramInt)
  {
    Object localObject = null;
    String str = paramString.toLowerCase();
    if (null != this.cacheFontPeer)
    {
      localObject = (FontPeer)this.cacheFontPeer.get(str + paramInt);
      if (null != localObject)
        return localObject;
    }
    localObject = new WFontPeer(paramString, paramInt);
    if (localObject != null)
    {
      if (null == this.cacheFontPeer)
        this.cacheFontPeer = new Hashtable(5, 0.89999997615814209F);
      if (null != this.cacheFontPeer)
        this.cacheFontPeer.put(str + paramInt, localObject);
    }
    return ((FontPeer)localObject);
  }

  private native void nativeSync();

  public void sync()
  {
    nativeSync();
    OGLRenderQueue.sync();
    D3DRenderQueue.sync();
  }

  public PrintJob getPrintJob(Frame paramFrame, String paramString, Properties paramProperties)
  {
    return getPrintJob(paramFrame, paramString, null, null);
  }

  public PrintJob getPrintJob(Frame paramFrame, String paramString, JobAttributes paramJobAttributes, PageAttributes paramPageAttributes)
  {
    if (GraphicsEnvironment.isHeadless())
      throw new IllegalArgumentException();
    PrintJob2D localPrintJob2D = new PrintJob2D(paramFrame, paramString, paramJobAttributes, paramPageAttributes);
    if (!(localPrintJob2D.printDialog()))
      localPrintJob2D = null;
    return localPrintJob2D;
  }

  public native void beep();

  public boolean getLockingKeyState(int paramInt)
  {
    if ((paramInt != 20) && (paramInt != 144) && (paramInt != 145) && (paramInt != 262))
      throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
    return getLockingKeyStateNative(paramInt);
  }

  public native boolean getLockingKeyStateNative(int paramInt);

  public void setLockingKeyState(int paramInt, boolean paramBoolean)
  {
    if ((paramInt != 20) && (paramInt != 144) && (paramInt != 145) && (paramInt != 262))
      throw new IllegalArgumentException("invalid key for Toolkit.setLockingKeyState");
    setLockingKeyStateNative(paramInt, paramBoolean);
  }

  public native void setLockingKeyStateNative(int paramInt, boolean paramBoolean);

  public Clipboard getSystemClipboard()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkSystemClipboardAccess();
    synchronized (this)
    {
      if (this.clipboard == null)
        this.clipboard = new WClipboard();
    }
    return this.clipboard;
  }

  protected native void loadSystemColors(int[] paramArrayOfInt);

  public static final Object targetToPeer(Object paramObject)
  {
    return SunToolkit.targetToPeer(paramObject);
  }

  public static final void targetDisposedPeer(Object paramObject1, Object paramObject2)
  {
    SunToolkit.targetDisposedPeer(paramObject1, paramObject2);
  }

  public InputMethodDescriptor getInputMethodAdapterDescriptor()
  {
    return new WInputMethodDescriptor();
  }

  public Map mapInputMethodHighlight(InputMethodHighlight paramInputMethodHighlight)
  {
    return WInputMethod.mapInputMethodHighlight(paramInputMethodHighlight);
  }

  public boolean enableInputMethodsForTextComponent()
  {
    return true;
  }

  public Locale getDefaultKeyboardLocale()
  {
    Locale localLocale = WInputMethod.getNativeLocale();
    if (localLocale == null)
      return super.getDefaultKeyboardLocale();
    return localLocale;
  }

  public Cursor createCustomCursor(Image paramImage, Point paramPoint, String paramString)
    throws java.lang.IndexOutOfBoundsException
  {
    return new WCustomCursor(paramImage, paramPoint, paramString);
  }

  public Dimension getBestCursorSize(int paramInt1, int paramInt2)
  {
    return new Dimension(WCustomCursor.getCursorWidth(), WCustomCursor.getCursorHeight());
  }

  public native int getMaximumCursorColors();

  static void paletteChanged()
  {
    ((Win32GraphicsEnvironment)GraphicsEnvironment.getLocalGraphicsEnvironment()).paletteChanged();
  }

  public static void displayChanged()
  {
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        ((Win32GraphicsEnvironment)GraphicsEnvironment.getLocalGraphicsEnvironment()).displayChanged();
      }
    });
  }

  public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent paramDragGestureEvent)
    throws InvalidDnDOperationException
  {
    return WDragSourceContextPeer.createDragSourceContextPeer(paramDragGestureEvent);
  }

  public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> paramClass, DragSource paramDragSource, Component paramComponent, int paramInt, DragGestureListener paramDragGestureListener)
  {
    if (MouseDragGestureRecognizer.class.equals(paramClass))
      return new WMouseDragGestureRecognizer(paramDragSource, paramComponent, paramInt, paramDragGestureListener);
    return null;
  }

  static Field getField(Class paramClass, String paramString)
  {
    Field localField = null;
    try
    {
      localField = (Field)AccessController.doPrivileged(new PrivilegedExceptionAction(paramClass, paramString)
      {
        public Object run()
          throws Exception
        {
          Field localField = this.val$clz.getDeclaredField(this.val$fieldName);
          localField.setAccessible(true);
          return localField;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      localPrivilegedActionException.printStackTrace();
    }
    return localField;
  }

  static Method getMethod(Class paramClass, String paramString, Class[] paramArrayOfClass)
  {
    Method localMethod = null;
    try
    {
      localMethod = (Method)AccessController.doPrivileged(new PrivilegedExceptionAction(paramClass, paramString, paramArrayOfClass)
      {
        public Method run()
          throws Exception
        {
          Method localMethod = this.val$clz.getDeclaredMethod(this.val$methodName, this.val$params);
          localMethod.setAccessible(true);
          return localMethod;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      localPrivilegedActionException.printStackTrace();
    }
    return localMethod;
  }

  protected Object lazilyLoadDesktopProperty(String paramString)
  {
    if (paramString.startsWith("DnD.Cursor."))
    {
      ??? = paramString.substring("DnD.Cursor.".length(), paramString.length()) + ".32x32";
      try
      {
        return Cursor.getSystemCustomCursor((String)???);
      }
      catch (AWTException localAWTException)
      {
        throw new RuntimeException("cannot load system cursor: " + ((String)???), localAWTException);
      }
    }
    if (paramString.equals("awt.dynamicLayoutSupported"))
      return Boolean.valueOf(isDynamicLayoutSupported());
    if ((WDesktopProperties.isWindowsProperty(paramString)) || (paramString.startsWith("awt.")) || (paramString.startsWith("DnD.")))
      synchronized (this)
      {
        lazilyInitWProps();
        return this.desktopProperties.get(paramString);
      }
    return super.lazilyLoadDesktopProperty(paramString);
  }

  private synchronized void lazilyInitWProps()
  {
    if (this.wprops == null)
    {
      this.wprops = new WDesktopProperties(this);
      updateProperties();
    }
  }

  private synchronized boolean isDynamicLayoutSupported()
  {
    boolean bool = isDynamicLayoutSupportedNative();
    lazilyInitWProps();
    Boolean localBoolean = (Boolean)this.desktopProperties.get("awt.dynamicLayoutSupported");
    if (log.isLoggable(Level.FINE))
      log.fine("In WTK.isDynamicLayoutSupported()   nativeDynamic == " + bool + "   wprops.dynamic == " + localBoolean);
    if ((localBoolean == null) || (bool != localBoolean.booleanValue()))
    {
      windowsSettingChange();
      return bool;
    }
    return localBoolean.booleanValue();
  }

  private void windowsSettingChange()
  {
    EventQueue.invokeLater(new Runnable(this)
    {
      public void run()
      {
        WToolkit.access$300(this.this$0);
      }
    });
  }

  private synchronized void updateProperties()
  {
    if (null == this.wprops)
      return;
    Map localMap = this.wprops.getProperties();
    Iterator localIterator = localMap.keySet().iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      Object localObject = localMap.get(str);
      if (log.isLoggable(Level.FINE))
        log.fine("changed " + str + " to " + localObject);
      setDesktopProperty(str, localObject);
    }
  }

  public synchronized void addPropertyChangeListener(String paramString, PropertyChangeListener paramPropertyChangeListener)
  {
    if ((WDesktopProperties.isWindowsProperty(paramString)) || (paramString.startsWith("awt.")) || (paramString.startsWith("DnD.")))
      lazilyInitWProps();
    super.addPropertyChangeListener(paramString, paramPropertyChangeListener);
  }

  protected synchronized void initializeDesktopProperties()
  {
    this.desktopProperties.put("DnD.Autoscroll.initialDelay", Integer.valueOf(50));
    this.desktopProperties.put("DnD.Autoscroll.interval", Integer.valueOf(50));
    try
    {
      this.desktopProperties.put("Shell.shellFolderManager", Class.forName("sun.awt.shell.Win32ShellFolderManager2"));
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
    }
  }

  protected synchronized RenderingHints getDesktopAAHints()
  {
    if (this.wprops == null)
      return null;
    return this.wprops.getDesktopAAHints();
  }

  public boolean isModalityTypeSupported(Dialog.ModalityType paramModalityType)
  {
    return ((paramModalityType == null) || (paramModalityType == Dialog.ModalityType.MODELESS) || (paramModalityType == Dialog.ModalityType.DOCUMENT_MODAL) || (paramModalityType == Dialog.ModalityType.APPLICATION_MODAL) || (paramModalityType == Dialog.ModalityType.TOOLKIT_MODAL));
  }

  public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType paramModalExclusionType)
  {
    return ((paramModalExclusionType == null) || (paramModalExclusionType == Dialog.ModalExclusionType.NO_EXCLUDE) || (paramModalExclusionType == Dialog.ModalExclusionType.APPLICATION_EXCLUDE) || (paramModalExclusionType == Dialog.ModalExclusionType.TOOLKIT_EXCLUDE));
  }

  public static WToolkit getWToolkit()
  {
    WToolkit localWToolkit = (WToolkit)Toolkit.getDefaultToolkit();
    return localWToolkit;
  }

  public boolean useBufferPerWindow()
  {
    return (!(Win32GraphicsEnvironment.isDWMCompositionEnabled()));
  }

  public void grab(Window paramWindow)
  {
    if (paramWindow.getPeer() != null)
      ((WWindowPeer)paramWindow.getPeer()).grab();
  }

  public void ungrab(Window paramWindow)
  {
    if (paramWindow.getPeer() != null)
      ((WWindowPeer)paramWindow.getPeer()).ungrab();
  }

  public native boolean syncNativeQueue();

  public boolean isDesktopSupported()
  {
    return true;
  }

  public java.awt.peer.DesktopPeer createDesktopPeer(Desktop paramDesktop)
  {
    return new WDesktopPeer();
  }

  private static native boolean isProtectedMode();

  public native boolean isWindowOpacityControlSupported();

  public boolean isWindowShapingSupported()
  {
    return true;
  }

  public native boolean isWindowTranslucencySupported();

  public boolean isTranslucencyCapable(java.awt.GraphicsConfiguration paramGraphicsConfiguration)
  {
    return true;
  }

  public boolean needUpdateWindow()
  {
    return true;
  }

  static
  {
    loadLibraries();
    Win32GraphicsEnvironment.init();
    initIDs();
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        String str = System.getProperty("browser");
        if ((str != null) && (str.equals("sun.plugin")))
          WToolkit.access$000();
        return null;
      }
    });
  }

  static class ToolkitDisposer
  implements DisposerRecord
  {
    public void dispose()
    {
      WToolkit.access$100();
    }
  }
}