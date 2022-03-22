package sun.awt;

import com.sun.awt.AWTUtilities;
import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Container;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dialog;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.MouseInfoPeer;
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
import java.io.FilePermission;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.awt.im.SimpleInputMethodWindow;
import sun.awt.image.ByteArrayImageSource;
import sun.awt.image.FileImageSource;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.ToolkitImage;
import sun.awt.image.URLImageSource;
import sun.font.FontDesignMetrics;
import sun.misc.SoftCache;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;

public abstract class SunToolkit extends java.awt.Toolkit
  implements WindowClosingSupport, WindowClosingListener, ComponentFactory, InputMethodSupport
{
  private static final Logger log;
  public static final int GRAB_EVENT_MASK = -2147483648;
  private static Field syncLWRequestsField;
  private static Method wakeupMethod;
  private static Field componentKeyField;
  private static Field menuComponentKeyField;
  private static Field trayIconKeyField;
  private static Field isPostedField;
  private static final String POST_EVENT_QUEUE_KEY = "PostEventQueue";
  private static final ReentrantLock AWT_LOCK;
  private static final Condition AWT_LOCK_COND;
  protected static final Hashtable peerMap;
  private static final Map appContextMap;
  static SoftCache imgCache;
  private static Locale startupLocale;
  private static String dataTransfererClassName;
  private transient WindowClosingListener windowClosingListener;
  private static DefaultMouseInfoPeer mPeer;
  private static Dialog.ModalExclusionType DEFAULT_MODAL_EXCLUSION_TYPE;
  private ModalityListenerList modalityListeners;
  public static final int WAIT_TIME = 10000;
  private static final int MAX_ITERS = 20;
  private static final int MIN_ITERS = 0;
  private static final int MINIMAL_EDELAY = 0;
  private boolean eventDispatched;
  private boolean queueEmpty;
  private final Object waitLock;
  static Method eqNoEvents;
  private static boolean checkedSystemAAFontSettings;
  private static boolean useSystemAAFontSettings;
  private static boolean lastExtraCondition;
  private static RenderingHints desktopFontHints;
  public static final String DESKTOPFONTHINTS = "awt.font.desktophints";
  private static Method consumeNextKeyTypedMethod;
  private static Method setMostRecentFocusOwnerMethod;

  public SunToolkit()
  {
    DebugHelper.init();
    this.windowClosingListener = null;
    this.modalityListeners = new ModalityListenerList();
    this.eventDispatched = false;
    this.queueEmpty = false;
    this.waitLock = "Wait Lock";
    ThreadGroup localThreadGroup = null;
    String str = System.getProperty("awt.threadgroup", "");
    if (str.length() != 0)
      try
      {
        Constructor localConstructor = Class.forName(str).getConstructor(new Class[] { String.class });
        localThreadGroup = (ThreadGroup)localConstructor.newInstance(new Object[] { "AWT-ThreadGroup" });
      }
      catch (Exception localException)
      {
        System.err.println("Failed loading " + str + ": " + localException);
      }
    1 local1 = new Runnable(this)
    {
      public void run()
      {
        EventQueue localEventQueue;
        String str = java.awt.Toolkit.getProperty("AWT.EventQueueClass", "java.awt.EventQueue");
        try
        {
          localEventQueue = (EventQueue)Class.forName(str).newInstance();
        }
        catch (Exception localException)
        {
          System.err.println("Failed loading " + str + ": " + localException);
          localEventQueue = new EventQueue();
        }
        AppContext localAppContext = AppContext.getAppContext();
        localAppContext.put(AppContext.EVENT_QUEUE_KEY, localEventQueue);
        PostEventQueue localPostEventQueue = new PostEventQueue(localEventQueue);
        localAppContext.put("PostEventQueue", localPostEventQueue);
      }
    };
    if (localThreadGroup != null)
    {
      Thread localThread = new Thread(localThreadGroup, local1, "EventQueue-Init");
      localThread.start();
      try
      {
        localThread.join();
      }
      catch (InterruptedException localInterruptedException)
      {
        localInterruptedException.printStackTrace();
      }
    }
    else
    {
      local1.run();
    }
  }

  public boolean useBufferPerWindow()
  {
    return false;
  }

  public abstract WindowPeer createWindow(Window paramWindow)
    throws HeadlessException;

  public abstract FramePeer createFrame(Frame paramFrame)
    throws HeadlessException;

  public abstract DialogPeer createDialog(Dialog paramDialog)
    throws HeadlessException;

  public abstract ButtonPeer createButton(Button paramButton)
    throws HeadlessException;

  public abstract TextFieldPeer createTextField(TextField paramTextField)
    throws HeadlessException;

  public abstract ChoicePeer createChoice(Choice paramChoice)
    throws HeadlessException;

  public abstract LabelPeer createLabel(Label paramLabel)
    throws HeadlessException;

  public abstract ListPeer createList(java.awt.List paramList)
    throws HeadlessException;

  public abstract CheckboxPeer createCheckbox(Checkbox paramCheckbox)
    throws HeadlessException;

  public abstract ScrollbarPeer createScrollbar(Scrollbar paramScrollbar)
    throws HeadlessException;

  public abstract ScrollPanePeer createScrollPane(ScrollPane paramScrollPane)
    throws HeadlessException;

  public abstract TextAreaPeer createTextArea(TextArea paramTextArea)
    throws HeadlessException;

  public abstract FileDialogPeer createFileDialog(FileDialog paramFileDialog)
    throws HeadlessException;

  public abstract MenuBarPeer createMenuBar(MenuBar paramMenuBar)
    throws HeadlessException;

  public abstract MenuPeer createMenu(Menu paramMenu)
    throws HeadlessException;

  public abstract PopupMenuPeer createPopupMenu(PopupMenu paramPopupMenu)
    throws HeadlessException;

  public abstract MenuItemPeer createMenuItem(MenuItem paramMenuItem)
    throws HeadlessException;

  public abstract CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem paramCheckboxMenuItem)
    throws HeadlessException;

  public abstract DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent paramDragGestureEvent)
    throws InvalidDnDOperationException;

  public abstract TrayIconPeer createTrayIcon(TrayIcon paramTrayIcon)
    throws HeadlessException, AWTException;

  public abstract SystemTrayPeer createSystemTray(SystemTray paramSystemTray);

  public abstract boolean isTraySupported();

  public abstract FontPeer getFontPeer(String paramString, int paramInt);

  public abstract RobotPeer createRobot(Robot paramRobot, GraphicsDevice paramGraphicsDevice)
    throws AWTException;

  public KeyboardFocusManagerPeer createKeyboardFocusManagerPeer(KeyboardFocusManager paramKeyboardFocusManager)
    throws HeadlessException
  {
    KeyboardFocusManagerPeerImpl localKeyboardFocusManagerPeerImpl = new KeyboardFocusManagerPeerImpl(paramKeyboardFocusManager);
    return localKeyboardFocusManagerPeerImpl;
  }

  public static final void awtLock()
  {
    AWT_LOCK.lock();
  }

  public static final boolean awtTryLock()
  {
    return AWT_LOCK.tryLock();
  }

  public static final void awtUnlock()
  {
    AWT_LOCK.unlock();
  }

  public static final void awtLockWait()
    throws InterruptedException
  {
    AWT_LOCK_COND.await();
  }

  public static final void awtLockWait(long paramLong)
    throws InterruptedException
  {
    AWT_LOCK_COND.await(paramLong, TimeUnit.MILLISECONDS);
  }

  public static final void awtLockNotify()
  {
    AWT_LOCK_COND.signal();
  }

  public static final void awtLockNotifyAll()
  {
    AWT_LOCK_COND.signalAll();
  }

  public static final boolean isAWTLockHeldByCurrentThread()
  {
    return AWT_LOCK.isHeldByCurrentThread();
  }

  public static AppContext createNewAppContext()
  {
    EventQueue localEventQueue;
    ThreadGroup localThreadGroup = Thread.currentThread().getThreadGroup();
    String str = java.awt.Toolkit.getProperty("AWT.EventQueueClass", "java.awt.EventQueue");
    try
    {
      localEventQueue = (EventQueue)Class.forName(str).newInstance();
    }
    catch (Exception localException)
    {
      System.err.println("Failed loading " + str + ": " + localException);
      localEventQueue = new EventQueue();
    }
    AppContext localAppContext = new AppContext(localThreadGroup);
    localAppContext.put(AppContext.EVENT_QUEUE_KEY, localEventQueue);
    PostEventQueue localPostEventQueue = new PostEventQueue(localEventQueue);
    localAppContext.put("PostEventQueue", localPostEventQueue);
    return localAppContext;
  }

  private static Object getPrivateKey(Object paramObject)
  {
    Object localObject = null;
    try
    {
      if (paramObject instanceof java.awt.Component)
      {
        if (componentKeyField == null)
          componentKeyField = getField(java.awt.Component.class, "privateKey");
        localObject = componentKeyField.get(paramObject);
      }
      else if (paramObject instanceof MenuComponent)
      {
        if (menuComponentKeyField == null)
          menuComponentKeyField = getField(MenuComponent.class, "privateKey");
        localObject = menuComponentKeyField.get(paramObject);
      }
      else if (paramObject instanceof TrayIcon)
      {
        if (trayIconKeyField == null)
          trayIconKeyField = getField(TrayIcon.class, "privateKey");
        localObject = trayIconKeyField.get(paramObject);
      }
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    return localObject;
  }

  private static Field getField(Class paramClass, String paramString)
  {
    return ((Field)AccessController.doPrivileged(new PrivilegedAction(paramClass, paramString)
    {
      public Field run()
      {
        Field localField;
        try
        {
          localField = this.val$klass.getDeclaredField(this.val$fieldName);
          if ((!($assertionsDisabled)) && (localField == null))
            throw new AssertionError();
          localField.setAccessible(true);
          return localField;
        }
        catch (SecurityException localSecurityException)
        {
          if (!($assertionsDisabled))
            throw new AssertionError();
        }
        catch (NoSuchFieldException localNoSuchFieldException)
        {
          if (!($assertionsDisabled))
            throw new AssertionError();
        }
        return null;
      }
    }));
  }

  static void wakeupEventQueue(EventQueue paramEventQueue, boolean paramBoolean)
  {
    if (wakeupMethod == null)
      wakeupMethod = (Method)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          try
          {
            Method localMethod = EventQueue.class.getDeclaredMethod("wakeup", new Class[] { Boolean.TYPE });
            if (localMethod != null)
              localMethod.setAccessible(true);
            return localMethod;
          }
          catch (NoSuchMethodException localNoSuchMethodException)
          {
            if (!($assertionsDisabled))
              throw new AssertionError();
          }
          catch (SecurityException localSecurityException)
          {
            if (!($assertionsDisabled))
              throw new AssertionError();
          }
          return null;
        }
      });
    try
    {
      if (wakeupMethod != null)
        wakeupMethod.invoke(paramEventQueue, new Object[] { Boolean.valueOf(paramBoolean) });
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
  }

  protected static Object targetToPeer(Object paramObject)
  {
    if ((paramObject != null) && (!(GraphicsEnvironment.isHeadless())))
      return peerMap.get(getPrivateKey(paramObject));
    return null;
  }

  protected static void targetCreatedPeer(Object paramObject1, Object paramObject2)
  {
    if ((paramObject1 != null) && (paramObject2 != null) && (!(GraphicsEnvironment.isHeadless())))
      peerMap.put(getPrivateKey(paramObject1), paramObject2);
  }

  protected static void targetDisposedPeer(Object paramObject1, Object paramObject2)
  {
    if ((paramObject1 != null) && (paramObject2 != null) && (!(GraphicsEnvironment.isHeadless())))
    {
      Object localObject = getPrivateKey(paramObject1);
      if (peerMap.get(localObject) == paramObject2)
        peerMap.remove(localObject);
    }
  }

  private static native boolean setAppContext(Object paramObject, AppContext paramAppContext);

  private static native AppContext getAppContext(Object paramObject);

  public static AppContext targetToAppContext(Object paramObject)
  {
    if ((paramObject == null) || (GraphicsEnvironment.isHeadless()))
      return null;
    AppContext localAppContext = getAppContext(paramObject);
    if (localAppContext == null)
      localAppContext = (AppContext)appContextMap.get(paramObject);
    return localAppContext;
  }

  public static void setLWRequestStatus(Window paramWindow, boolean paramBoolean)
  {
    if (syncLWRequestsField == null)
      syncLWRequestsField = getField(Window.class, "syncLWRequests");
    try
    {
      if (syncLWRequestsField != null)
        syncLWRequestsField.setBoolean(paramWindow, paramBoolean);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
  }

  public static void checkAndSetPolicy(Container paramContainer, boolean paramBoolean)
  {
    Object localObject = KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy();
    String str1 = java.awt.Toolkit.getDefaultToolkit().getClass().getName();
    if (!("sun.awt.X11.XToolkit".equals(str1)))
    {
      paramContainer.setFocusTraversalPolicy((FocusTraversalPolicy)localObject);
      return;
    }
    String str2 = localObject.getClass().getName();
    if (DefaultFocusTraversalPolicy.class != localObject.getClass())
      if (str2.startsWith("java.awt."))
      {
        if (paramBoolean)
          localObject = createLayoutPolicy();
      }
      else if (str2.startsWith("javax.swing."))
      {
        if (paramBoolean)
          break label108:
        localObject = new DefaultFocusTraversalPolicy();
      }
    else if (paramBoolean)
      localObject = createLayoutPolicy();
    label108: paramContainer.setFocusTraversalPolicy((FocusTraversalPolicy)localObject);
  }

  private static FocusTraversalPolicy createLayoutPolicy()
  {
    FocusTraversalPolicy localFocusTraversalPolicy = null;
    try
    {
      Class localClass = Class.forName("javax.swing.LayoutFocusTraversalPolicy");
      localFocusTraversalPolicy = (FocusTraversalPolicy)localClass.newInstance();
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    catch (InstantiationException localInstantiationException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    return localFocusTraversalPolicy;
  }

  public static void insertTargetMapping(Object paramObject, AppContext paramAppContext)
  {
    if ((!(GraphicsEnvironment.isHeadless())) && (!(setAppContext(paramObject, paramAppContext))))
      appContextMap.put(paramObject, paramAppContext);
  }

  public static void postEvent(AppContext paramAppContext, AWTEvent paramAWTEvent)
  {
    if (paramAWTEvent == null)
      throw new NullPointerException();
    setSystemGenerated(paramAWTEvent);
    AppContext localAppContext = targetToAppContext(paramAWTEvent.getSource());
    if ((localAppContext != null) && (!(localAppContext.equals(paramAppContext))))
      log.fine("Event posted on wrong app context : " + paramAWTEvent);
    PostEventQueue localPostEventQueue = (PostEventQueue)paramAppContext.get("PostEventQueue");
    if (localPostEventQueue != null)
      localPostEventQueue.postEvent(paramAWTEvent);
  }

  public static void postPriorityEvent(AWTEvent paramAWTEvent)
  {
    if (isPostedField == null)
      isPostedField = getField(AWTEvent.class, "isPosted");
    PeerEvent localPeerEvent = new PeerEvent(java.awt.Toolkit.getDefaultToolkit(), new Runnable(paramAWTEvent)
    {
      public void run()
      {
        try
        {
          SunToolkit.access$000().setBoolean(this.val$e, true);
        }
        catch (IllegalArgumentException localIllegalArgumentException)
        {
          if (!($assertionsDisabled))
            throw new AssertionError();
        }
        catch (IllegalAccessException localIllegalAccessException)
        {
          if (!($assertionsDisabled))
            throw new AssertionError();
        }
        ((java.awt.Component)this.val$e.getSource()).dispatchEvent(this.val$e);
      }
    }
    , 2L);
    postEvent(targetToAppContext(paramAWTEvent.getSource()), localPeerEvent);
  }

  public static void flushPendingEvents()
  {
    AppContext localAppContext = AppContext.getAppContext();
    PostEventQueue localPostEventQueue = (PostEventQueue)localAppContext.get("PostEventQueue");
    if (localPostEventQueue != null)
      localPostEventQueue.flush();
  }

  public static boolean isPostEventQueueEmpty()
  {
    AppContext localAppContext = AppContext.getAppContext();
    PostEventQueue localPostEventQueue = (PostEventQueue)localAppContext.get("PostEventQueue");
    if (localPostEventQueue != null)
      return localPostEventQueue.noEvents();
    return true;
  }

  public static void executeOnEventHandlerThread(Object paramObject, Runnable paramRunnable)
  {
    executeOnEventHandlerThread(new PeerEvent(paramObject, paramRunnable, 3412048253326196737L));
  }

  public static void executeOnEventHandlerThread(Object paramObject, Runnable paramRunnable, long paramLong)
  {
    executeOnEventHandlerThread(new PeerEvent(paramObject, paramRunnable, 3412048253326196737L, paramLong)
    {
      public long getWhen()
      {
        return this.val$when;
      }
    });
  }

  public static void executeOnEventHandlerThread(PeerEvent paramPeerEvent)
  {
    postEvent(targetToAppContext(paramPeerEvent.getSource()), paramPeerEvent);
  }

  public static void invokeLaterOnAppContext(AppContext paramAppContext, Runnable paramRunnable)
  {
    postEvent(paramAppContext, new PeerEvent(java.awt.Toolkit.getDefaultToolkit(), paramRunnable, 3412048253326196737L));
  }

  public static void executeOnEDTAndWait(Object paramObject, Runnable paramRunnable)
    throws InterruptedException, InvocationTargetException
  {
    if (EventQueue.isDispatchThread())
      throw new Error("Cannot call executeOnEDTAndWait from any event dispatcher thread");
    1AWTInvocationLock local1AWTInvocationLock = new Object()
    {
    };
    PeerEvent localPeerEvent = new PeerEvent(paramObject, paramRunnable, local1AWTInvocationLock, true, 3412048098707374081L);
    synchronized (local1AWTInvocationLock)
    {
      executeOnEventHandlerThread(localPeerEvent);
      local1AWTInvocationLock.wait();
    }
    ??? = localPeerEvent.getThrowable();
    if (??? != null)
      throw new InvocationTargetException((Throwable)???);
  }

  private static EventQueue getNextQueue(Object paramObject)
  {
    EventQueue localEventQueue = null;
    try
    {
      Field localField = getField(EventQueue.class, "nextQueue");
      localEventQueue = (EventQueue)localField.get(paramObject);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    return localEventQueue;
  }

  private static Thread getDispatchThread(Object paramObject)
  {
    Thread localThread = null;
    try
    {
      Field localField = getField(EventQueue.class, "dispatchThread");
      localThread = (Thread)localField.get(paramObject);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    return localThread;
  }

  public static boolean isDispatchThreadForAppContext(Object paramObject)
  {
    AppContext localAppContext = targetToAppContext(paramObject);
    Object localObject = (EventQueue)localAppContext.get(AppContext.EVENT_QUEUE_KEY);
    for (EventQueue localEventQueue = getNextQueue(localObject); localEventQueue != null; localEventQueue = getNextQueue(localObject))
      localObject = localEventQueue;
    return (Thread.currentThread() == getDispatchThread(localObject));
  }

  public Dimension getScreenSize()
  {
    return new Dimension(getScreenWidth(), getScreenHeight());
  }

  protected abstract int getScreenWidth();

  protected abstract int getScreenHeight();

  public FontMetrics getFontMetrics(Font paramFont)
  {
    return FontDesignMetrics.getMetrics(paramFont);
  }

  public String[] getFontList()
  {
    String[] arrayOfString = { "Dialog", "SansSerif", "Serif", "Monospaced", "DialogInput" };
    return arrayOfString;
  }

  public PanelPeer createPanel(Panel paramPanel)
  {
    return ((PanelPeer)createComponent(paramPanel));
  }

  public CanvasPeer createCanvas(Canvas paramCanvas)
  {
    return ((CanvasPeer)createComponent(paramCanvas));
  }

  public void disableBackgroundErase(Canvas paramCanvas)
  {
    disableBackgroundEraseImpl(paramCanvas);
  }

  public void disableBackgroundErase(java.awt.Component paramComponent)
  {
    disableBackgroundEraseImpl(paramComponent);
  }

  private void disableBackgroundEraseImpl(java.awt.Component paramComponent)
  {
    AWTAccessor.getComponentAccessor().setBackgroundEraseDisabled(paramComponent, true);
  }

  public static boolean getSunAwtNoerasebackground()
  {
    return ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.awt.noerasebackground"))).booleanValue();
  }

  public static boolean getSunAwtErasebackgroundonresize()
  {
    return ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.awt.erasebackgroundonresize"))).booleanValue();
  }

  static synchronized Image getImageFromHash(java.awt.Toolkit paramToolkit, URL paramURL)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      try
      {
        Permission localPermission = paramURL.openConnection().getPermission();
        if (localPermission != null)
          try
          {
            localSecurityManager.checkPermission(localPermission);
          }
          catch (SecurityException localSecurityException)
          {
            if ((localPermission instanceof FilePermission) && (localPermission.getActions().indexOf("read") != -1))
              localSecurityManager.checkRead(localPermission.getName());
            else if ((localPermission instanceof SocketPermission) && (localPermission.getActions().indexOf("connect") != -1))
              localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
            else
              throw localSecurityException;
          }
      }
      catch (IOException localIOException)
      {
        localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
      }
    Image localImage = (Image)imgCache.get(paramURL);
    if (localImage == null)
      try
      {
        localImage = paramToolkit.createImage(new URLImageSource(paramURL));
        imgCache.put(paramURL, localImage);
      }
      catch (Exception localException)
      {
      }
    return localImage;
  }

  static synchronized Image getImageFromHash(java.awt.Toolkit paramToolkit, String paramString)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkRead(paramString);
    Image localImage = (Image)imgCache.get(paramString);
    if (localImage == null)
      try
      {
        localImage = paramToolkit.createImage(new FileImageSource(paramString));
        imgCache.put(paramString, localImage);
      }
      catch (Exception localException)
      {
      }
    return localImage;
  }

  public Image getImage(String paramString)
  {
    return getImageFromHash(this, paramString);
  }

  public Image getImage(URL paramURL)
  {
    return getImageFromHash(this, paramURL);
  }

  public Image createImage(String paramString)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkRead(paramString);
    return createImage(new FileImageSource(paramString));
  }

  public Image createImage(URL paramURL)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      try
      {
        Permission localPermission = paramURL.openConnection().getPermission();
        if (localPermission != null)
          try
          {
            localSecurityManager.checkPermission(localPermission);
          }
          catch (SecurityException localSecurityException)
          {
            if ((localPermission instanceof FilePermission) && (localPermission.getActions().indexOf("read") != -1))
              localSecurityManager.checkRead(localPermission.getName());
            else if ((localPermission instanceof SocketPermission) && (localPermission.getActions().indexOf("connect") != -1))
              localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
            else
              throw localSecurityException;
          }
      }
      catch (IOException localIOException)
      {
        localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
      }
    return createImage(new URLImageSource(paramURL));
  }

  public Image createImage(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    return createImage(new ByteArrayImageSource(paramArrayOfByte, paramInt1, paramInt2));
  }

  public Image createImage(ImageProducer paramImageProducer)
  {
    return new ToolkitImage(paramImageProducer);
  }

  public int checkImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    int i;
    if (!(paramImage instanceof ToolkitImage))
      return 32;
    ToolkitImage localToolkitImage = (ToolkitImage)paramImage;
    if ((paramInt1 == 0) || (paramInt2 == 0))
      i = 32;
    else
      i = localToolkitImage.getImageRep().check(paramImageObserver);
    return (localToolkitImage.check(paramImageObserver) | i);
  }

  public boolean prepareImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    if ((paramInt1 == 0) || (paramInt2 == 0))
      return true;
    if (!(paramImage instanceof ToolkitImage))
      return true;
    ToolkitImage localToolkitImage = (ToolkitImage)paramImage;
    if (localToolkitImage.hasError())
    {
      if (paramImageObserver != null)
        paramImageObserver.imageUpdate(paramImage, 192, -1, -1, -1, -1);
      return false;
    }
    ImageRepresentation localImageRepresentation = localToolkitImage.getImageRep();
    return localImageRepresentation.prepare(paramImageObserver);
  }

  public static BufferedImage getScaledIconImage(List<Image> paramList, int paramInt1, int paramInt2)
  {
    label84: int k;
    int l;
    if ((paramInt1 == 0) || (paramInt2 == 0))
      return null;
    Object localObject1 = null;
    int i = 0;
    int j = 0;
    double d1 = 3.0D;
    double d2 = 0D;
    Object localObject2 = paramList.iterator();
    while (true)
    {
      while (true)
      {
        do
        {
          if (!(((Iterator)localObject2).hasNext()))
            break label445;
          localObject3 = (Image)((Iterator)localObject2).next();
          if (localObject3 != null)
            break label84;
        }
        while (!(log.isLoggable(Level.FINEST)));
        log.finest("SunToolkit.getScaledIconImage: Skipping the image passed into Java because it's null.");
      }
      if (localObject3 instanceof ToolkitImage)
      {
        ImageRepresentation localImageRepresentation = ((ToolkitImage)localObject3).getImageRep();
        localImageRepresentation.reconstruct(32);
      }
      try
      {
        k = ((Image)localObject3).getWidth(null);
        l = ((Image)localObject3).getHeight(null);
      }
      catch (Exception localException)
      {
        while (true)
          if (log.isLoggable(Level.FINEST))
            log.finest("SunToolkit.getScaledIconImage: Perhaps the image passed into Java is broken. Skipping this icon.");
      }
      if ((k > 0) && (l > 0))
      {
        double d3 = Math.min(paramInt1 / k, paramInt2 / l);
        int i1 = 0;
        int i2 = 0;
        double d4 = 1D;
        if (d3 >= 2.0D)
        {
          d3 = Math.floor(d3);
          i1 = k * (int)d3;
          i2 = l * (int)d3;
          d4 = 1D - 0.5D / d3;
        }
        else if (d3 >= 1D)
        {
          d3 = 1D;
          i1 = k;
          i2 = l;
          d4 = 0D;
        }
        else if (d3 >= 0.75D)
        {
          d3 = 0.75D;
          i1 = k * 3 / 4;
          i2 = l * 3 / 4;
          d4 = 0.29999999999999999D;
        }
        else if (d3 >= 0.66659999999999997D)
        {
          d3 = 0.66659999999999997D;
          i1 = k * 2 / 3;
          i2 = l * 2 / 3;
          d4 = 0.33000000000000002D;
        }
        else
        {
          d5 = Math.ceil(1D / d3);
          d3 = 1D / d5;
          i1 = (int)Math.round(k / d5);
          i2 = (int)Math.round(l / d5);
          d4 = 1D - 1D / d5;
        }
        double d5 = (paramInt1 - i1) / paramInt1 + (paramInt2 - i2) / paramInt2 + d4;
        if (d5 < d1)
        {
          d1 = d5;
          d2 = d3;
          localObject1 = localObject3;
          i = i1;
          j = i2;
        }
        if (d5 == 0D)
          break;
      }
    }
    if (localObject1 == null)
      label445: return null;
    localObject2 = new BufferedImage(paramInt1, paramInt2, 2);
    Object localObject3 = ((BufferedImage)localObject2).createGraphics();
    ((Graphics2D)localObject3).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    try
    {
      k = (paramInt1 - i) / 2;
      l = (paramInt2 - j) / 2;
      if (log.isLoggable(Level.FINER))
        log.finer("WWindowPeer.getScaledIconData() result : w : " + paramInt1 + " h : " + paramInt2 + " iW : " + localObject1.getWidth(null) + " iH : " + localObject1.getHeight(null) + " sim : " + d1 + " sf : " + d2 + " adjW : " + i + " adjH : " + j + " x : " + k + " y : " + l);
      ((Graphics2D)localObject3).drawImage(localObject1, k, l, i, j, null);
    }
    finally
    {
      ((Graphics2D)localObject3).dispose();
    }
    return ((BufferedImage)(BufferedImage)localObject2);
  }

  public static DataBufferInt getScaledIconData(List<Image> paramList, int paramInt1, int paramInt2)
  {
    BufferedImage localBufferedImage = getScaledIconImage(paramList, paramInt1, paramInt2);
    if (localBufferedImage == null)
    {
      if (log.isLoggable(Level.FINEST))
        log.finest("SunToolkit.getScaledIconData: Perhaps the image passed into Java is broken. Skipping this icon.");
      return null;
    }
    WritableRaster localWritableRaster = localBufferedImage.getRaster();
    DataBuffer localDataBuffer = localWritableRaster.getDataBuffer();
    return ((DataBufferInt)localDataBuffer);
  }

  protected EventQueue getSystemEventQueueImpl()
  {
    return getSystemEventQueueImplPP();
  }

  static EventQueue getSystemEventQueueImplPP()
  {
    return getSystemEventQueueImplPP(AppContext.getAppContext());
  }

  public static EventQueue getSystemEventQueueImplPP(AppContext paramAppContext)
  {
    EventQueue localEventQueue = (EventQueue)paramAppContext.get(AppContext.EVENT_QUEUE_KEY);
    return localEventQueue;
  }

  public static Container getNativeContainer(java.awt.Component paramComponent)
  {
    return java.awt.Toolkit.getNativeContainer(paramComponent);
  }

  public Window createInputMethodWindow(String paramString, sun.awt.im.InputContext paramInputContext)
  {
    return new SimpleInputMethodWindow(paramString, paramInputContext);
  }

  public boolean enableInputMethodsForTextComponent()
  {
    return false;
  }

  public static Locale getStartupLocale()
  {
    if (startupLocale == null)
    {
      String str3;
      String str4;
      String str1 = (String)AccessController.doPrivileged(new GetPropertyAction("user.language", "en"));
      String str2 = (String)AccessController.doPrivileged(new GetPropertyAction("user.region"));
      if (str2 != null)
      {
        int i = str2.indexOf(95);
        if (i >= 0)
        {
          str3 = str2.substring(0, i);
          str4 = str2.substring(i + 1);
        }
        else
        {
          str3 = str2;
          str4 = "";
        }
      }
      else
      {
        str3 = (String)AccessController.doPrivileged(new GetPropertyAction("user.country", ""));
        str4 = (String)AccessController.doPrivileged(new GetPropertyAction("user.variant", ""));
      }
      startupLocale = new Locale(str1, str3, str4);
    }
    return startupLocale;
  }

  public Locale getDefaultKeyboardLocale()
  {
    return getStartupLocale();
  }

  protected static void setDataTransfererClassName(String paramString)
  {
    dataTransfererClassName = paramString;
  }

  public static String getDataTransfererClassName()
  {
    if (dataTransfererClassName == null)
      java.awt.Toolkit.getDefaultToolkit();
    return dataTransfererClassName;
  }

  public WindowClosingListener getWindowClosingListener()
  {
    return this.windowClosingListener;
  }

  public void setWindowClosingListener(WindowClosingListener paramWindowClosingListener)
  {
    this.windowClosingListener = paramWindowClosingListener;
  }

  public java.lang.RuntimeException windowClosingNotify(WindowEvent paramWindowEvent)
  {
    if (this.windowClosingListener != null)
      return this.windowClosingListener.windowClosingNotify(paramWindowEvent);
    return null;
  }

  public java.lang.RuntimeException windowClosingDelivered(WindowEvent paramWindowEvent)
  {
    if (this.windowClosingListener != null)
      return this.windowClosingListener.windowClosingDelivered(paramWindowEvent);
    return null;
  }

  protected synchronized MouseInfoPeer getMouseInfoPeer()
  {
    if (mPeer == null)
      mPeer = new DefaultMouseInfoPeer();
    return mPeer;
  }

  public static boolean needsXEmbed()
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.awt.noxembed", "false"));
    if ("true".equals(str))
      return false;
    java.awt.Toolkit localToolkit = java.awt.Toolkit.getDefaultToolkit();
    if (localToolkit instanceof SunToolkit)
      return ((SunToolkit)localToolkit).needsXEmbedImpl();
    return false;
  }

  protected boolean needsXEmbedImpl()
  {
    return false;
  }

  protected final boolean isXEmbedServerRequested()
  {
    return ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.awt.xembedserver"))).booleanValue();
  }

  public static boolean isModalExcludedSupported()
  {
    java.awt.Toolkit localToolkit = java.awt.Toolkit.getDefaultToolkit();
    return localToolkit.isModalExclusionTypeSupported(DEFAULT_MODAL_EXCLUSION_TYPE);
  }

  protected boolean isModalExcludedSupportedImpl()
  {
    return false;
  }

  public static void setModalExcluded(Window paramWindow)
  {
    paramWindow.setModalExclusionType(DEFAULT_MODAL_EXCLUSION_TYPE);
  }

  public static boolean isModalExcluded(Window paramWindow)
  {
    return (paramWindow.getModalExclusionType().compareTo(DEFAULT_MODAL_EXCLUSION_TYPE) >= 0);
  }

  public boolean isModalityTypeSupported(Dialog.ModalityType paramModalityType)
  {
    return ((paramModalityType == Dialog.ModalityType.MODELESS) || (paramModalityType == Dialog.ModalityType.APPLICATION_MODAL));
  }

  public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType paramModalExclusionType)
  {
    return (paramModalExclusionType == Dialog.ModalExclusionType.NO_EXCLUDE);
  }

  public void addModalityListener(ModalityListener paramModalityListener)
  {
    this.modalityListeners.add(paramModalityListener);
  }

  public void removeModalityListener(ModalityListener paramModalityListener)
  {
    this.modalityListeners.remove(paramModalityListener);
  }

  public void notifyModalityPushed(Dialog paramDialog)
  {
    notifyModalityChange(1300, paramDialog);
  }

  public void notifyModalityPopped(Dialog paramDialog)
  {
    notifyModalityChange(1301, paramDialog);
  }

  final void notifyModalityChange(int paramInt, Dialog paramDialog)
  {
    ModalityEvent localModalityEvent = new ModalityEvent(paramDialog, this.modalityListeners, paramInt);
    localModalityEvent.dispatch();
  }

  public static boolean isLightweightOrUnknown(java.awt.Component paramComponent)
  {
    if ((paramComponent.isLightweight()) || (!(getDefaultToolkit() instanceof SunToolkit)))
      return true;
    return ((!(paramComponent instanceof Button)) && (!(paramComponent instanceof Canvas)) && (!(paramComponent instanceof Checkbox)) && (!(paramComponent instanceof Choice)) && (!(paramComponent instanceof Label)) && (!(paramComponent instanceof java.awt.List)) && (!(paramComponent instanceof Panel)) && (!(paramComponent instanceof Scrollbar)) && (!(paramComponent instanceof ScrollPane)) && (!(paramComponent instanceof TextArea)) && (!(paramComponent instanceof TextField)) && (!(paramComponent instanceof Window)));
  }

  static Method getMethod(Class paramClass, String paramString, Class[] paramArrayOfClass)
  {
    Method localMethod = null;
    try
    {
      localMethod = (Method)AccessController.doPrivileged(new PrivilegedExceptionAction(paramClass, paramString, paramArrayOfClass)
      {
        public Object run()
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

  public void realSync()
    throws sun.awt.SunToolkit.OperationTimedOut, SunToolkit.InfiniteLoop
  {
    int i = 0;
    do
    {
      sync();
      for (int j = 0; j < 0; ++j)
        syncNativeQueue();
      while ((syncNativeQueue()) && (j < 20))
        ++j;
      if (j >= 20)
        throw new SunToolkit.InfiniteLoop();
      for (j = 0; j < 0; ++j)
        waitForIdle();
      while ((waitForIdle()) && (j < 20))
        ++j;
      if (j >= 20)
        throw new SunToolkit.InfiniteLoop();
      ++i;
    }
    while ((((syncNativeQueue()) || (waitForIdle()))) && (i < 20));
  }

  protected abstract boolean syncNativeQueue();

  private boolean isEQEmpty()
  {
    EventQueue localEventQueue = getSystemEventQueueImpl();
    synchronized (SunToolkit.class)
    {
      if (eqNoEvents == null)
        eqNoEvents = getMethod(EventQueue.class, "noEvents", null);
    }
    try
    {
      return ((Boolean)eqNoEvents.invoke(localEventQueue, new Object[0])).booleanValue();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return false;
  }

  protected boolean waitForIdle()
  {
    flushPendingEvents();
    boolean bool = isEQEmpty();
    this.queueEmpty = false;
    this.eventDispatched = false;
    synchronized (this.waitLock)
    {
      postEvent(AppContext.getAppContext(), new PeerEvent(this, getSystemEventQueueImpl(), null, 4L)
      {
        public void dispatch()
        {
          for (int i = 0; i < 0; ++i)
            this.this$0.syncNativeQueue();
          while ((this.this$0.syncNativeQueue()) && (i < 20))
            ++i;
          SunToolkit.flushPendingEvents();
          synchronized (SunToolkit.access$100(this.this$0))
          {
            SunToolkit.access$202(this.this$0, SunToolkit.access$300(this.this$0));
            SunToolkit.access$402(this.this$0, true);
            SunToolkit.access$100(this.this$0).notifyAll();
          }
        }
      });
      try
      {
        while (!(this.eventDispatched))
          this.waitLock.wait();
      }
      catch (InterruptedException localInterruptedException2)
      {
        monitorexit;
        return false;
      }
    }
    try
    {
      Thread.sleep(3412047875369074688L);
    }
    catch (InterruptedException localInterruptedException1)
    {
      throw new java.lang.RuntimeException("Interrupted");
    }
    flushPendingEvents();
    synchronized (this.waitLock)
    {
      return (((!(this.queueEmpty)) || (!(isEQEmpty())) || (!(bool))) ? 1 : false);
    }
  }

  public abstract void grab(Window paramWindow);

  public abstract void ungrab(Window paramWindow);

  public static native void closeSplashScreen();

  private void fireDesktopFontPropertyChanges()
  {
    setDesktopProperty("awt.font.desktophints", getDesktopFontHints());
  }

  public static void setAAFontSettingsCondition(boolean paramBoolean)
  {
    if (paramBoolean != lastExtraCondition)
    {
      lastExtraCondition = paramBoolean;
      if (checkedSystemAAFontSettings)
      {
        checkedSystemAAFontSettings = false;
        java.awt.Toolkit localToolkit = java.awt.Toolkit.getDefaultToolkit();
        if (localToolkit instanceof SunToolkit)
          ((SunToolkit)localToolkit).fireDesktopFontPropertyChanges();
      }
    }
  }

  private static RenderingHints getDesktopAAHintsByName(String paramString)
  {
    Object localObject = null;
    paramString = paramString.toLowerCase(Locale.ENGLISH);
    if (paramString.equals("on"))
      localObject = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
    else if (paramString.equals("gasp"))
      localObject = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
    else if ((paramString.equals("lcd")) || (paramString.equals("lcd_hrgb")))
      localObject = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
    else if (paramString.equals("lcd_hbgr"))
      localObject = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
    else if (paramString.equals("lcd_vrgb"))
      localObject = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB;
    else if (paramString.equals("lcd_vbgr"))
      localObject = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;
    if (localObject != null)
    {
      RenderingHints localRenderingHints = new RenderingHints(null);
      localRenderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, localObject);
      return localRenderingHints;
    }
    return null;
  }

  private static boolean useSystemAAFontSettings()
  {
    if (!(checkedSystemAAFontSettings))
    {
      useSystemAAFontSettings = true;
      String str = null;
      java.awt.Toolkit localToolkit = java.awt.Toolkit.getDefaultToolkit();
      if (localToolkit instanceof SunToolkit)
        str = (String)AccessController.doPrivileged(new GetPropertyAction("awt.useSystemAAFontSettings"));
      if (str != null)
      {
        useSystemAAFontSettings = Boolean.valueOf(str).booleanValue();
        if (!(useSystemAAFontSettings))
          desktopFontHints = getDesktopAAHintsByName(str);
      }
      if (useSystemAAFontSettings)
        useSystemAAFontSettings = lastExtraCondition;
      checkedSystemAAFontSettings = true;
    }
    return useSystemAAFontSettings;
  }

  protected RenderingHints getDesktopAAHints()
  {
    return null;
  }

  public static RenderingHints getDesktopFontHints()
  {
    if (useSystemAAFontSettings())
    {
      java.awt.Toolkit localToolkit = java.awt.Toolkit.getDefaultToolkit();
      if (localToolkit instanceof SunToolkit)
      {
        RenderingHints localRenderingHints = ((SunToolkit)localToolkit).getDesktopAAHints();
        return ((RenderingHints)localRenderingHints);
      }
      return null;
    }
    if (desktopFontHints != null)
      return ((RenderingHints)(RenderingHints)desktopFontHints.clone());
    return null;
  }

  public abstract boolean isDesktopSupported();

  public static synchronized void consumeNextKeyTyped(KeyEvent paramKeyEvent)
  {
    if (consumeNextKeyTypedMethod == null)
      consumeNextKeyTypedMethod = getMethod(DefaultKeyboardFocusManager.class, "consumeNextKeyTyped", new Class[] { KeyEvent.class });
    try
    {
      consumeNextKeyTypedMethod.invoke(KeyboardFocusManager.getCurrentKeyboardFocusManager(), new Object[] { paramKeyEvent });
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      localIllegalAccessException.printStackTrace();
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      localInvocationTargetException.printStackTrace();
    }
  }

  public static void setMostRecentFocusOwner(Window paramWindow, java.awt.Component paramComponent)
  {
    synchronized (SunToolkit.class)
    {
      if (setMostRecentFocusOwnerMethod == null)
        setMostRecentFocusOwnerMethod = getMethod(KeyboardFocusManager.class, "setMostRecentFocusOwner", new Class[] { Window.class, java.awt.Component.class });
    }
    try
    {
      setMostRecentFocusOwnerMethod.invoke(null, new Object[] { paramWindow, paramComponent });
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      localIllegalAccessException.printStackTrace();
      return;
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      localInvocationTargetException.printStackTrace();
      return;
    }
  }

  public abstract boolean isWindowOpacityControlSupported();

  public abstract boolean isWindowShapingSupported();

  public abstract boolean isWindowTranslucencySupported();

  public boolean isTranslucencyCapable(GraphicsConfiguration paramGraphicsConfiguration)
  {
    return false;
  }

  public static boolean getSunAwtUntrustedTopLevelTranslucency()
  {
    return ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.awt.untrustedTopLevelTranslucency"))).booleanValue();
  }

  public static boolean isContainingTopLevelOpaque(java.awt.Component paramComponent)
  {
    while ((paramComponent != null) && (!(paramComponent instanceof Window)))
      paramComponent = paramComponent.getParent();
    return ((paramComponent != null) && (AWTUtilities.isWindowOpaque((Window)paramComponent)));
  }

  public static boolean isContainingTopLevelTranslucent(java.awt.Component paramComponent)
  {
    while ((paramComponent != null) && (!(paramComponent instanceof Window)))
      paramComponent = paramComponent.getParent();
    return ((paramComponent != null) && (AWTUtilities.getWindowOpacity((Window)paramComponent) < 1F));
  }

  public boolean needUpdateWindow()
  {
    return false;
  }

  public static void setSystemGenerated(AWTEvent paramAWTEvent)
  {
    AWTAccessor.getAWTEventAccessor().setSystemGenerated(paramAWTEvent);
  }

  public static boolean isSystemGenerated(AWTEvent paramAWTEvent)
  {
    return AWTAccessor.getAWTEventAccessor().isSystemGenerated(paramAWTEvent);
  }

  static
  {
    log = Logger.getLogger("sun.awt.SunToolkit");
    AWT_LOCK = new ReentrantLock();
    AWT_LOCK_COND = AWT_LOCK.newCondition();
    peerMap = AWTAutoShutdown.getInstance().getPeerMap();
    appContextMap = Collections.synchronizedMap(new WeakHashMap());
    imgCache = new SoftCache();
    startupLocale = null;
    dataTransfererClassName = null;
    mPeer = null;
    DEFAULT_MODAL_EXCLUSION_TYPE = (Dialog.ModalExclusionType)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        Dialog.ModalExclusionType localModalExclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        try
        {
          Field localField = Dialog.class.getDeclaredField("DEFAULT_MODAL_EXCLUSION_TYPE");
          localField.setAccessible(true);
          localModalExclusionType = (Dialog.ModalExclusionType)localField.get(null);
        }
        catch (Exception localException)
        {
        }
        return localModalExclusionType;
      }
    });
    lastExtraCondition = true;
    consumeNextKeyTypedMethod = null;
  }

  public static class InfiniteLoop extends java.lang.RuntimeException
  {
  }

  static class ModalityListenerList
  implements ModalityListener
  {
    Vector<ModalityListener> listeners = new Vector();

    void add(ModalityListener paramModalityListener)
    {
      this.listeners.addElement(paramModalityListener);
    }

    void remove(ModalityListener paramModalityListener)
    {
      this.listeners.removeElement(paramModalityListener);
    }

    public void modalityPushed(ModalityEvent paramModalityEvent)
    {
      Iterator localIterator = this.listeners.iterator();
      while (localIterator.hasNext())
        ((ModalityListener)localIterator.next()).modalityPushed(paramModalityEvent);
    }

    public void modalityPopped(ModalityEvent paramModalityEvent)
    {
      Iterator localIterator = this.listeners.iterator();
      while (localIterator.hasNext())
        ((ModalityListener)localIterator.next()).modalityPopped(paramModalityEvent);
    }
  }

  public static class OperationTimedOut extends java.lang.RuntimeException
  {
    public OperationTimedOut(String paramString)
    {
      super(paramString);
    }

    public OperationTimedOut()
    {
    }
  }
}