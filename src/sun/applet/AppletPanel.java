package sun.applet;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InvocationEvent;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import sun.awt.AppContext;
import sun.awt.EmbeddedFrame;
import sun.awt.SunToolkit;
import sun.misc.MessageUtils;
import sun.misc.PerformanceLogger;
import sun.misc.Queue;
import sun.security.util.SecurityConstants;

public abstract class AppletPanel extends Panel
  implements AppletStub, Runnable
{
  Applet applet;
  protected boolean doInit = true;
  protected AppletClassLoader loader;
  public static final int APPLET_DISPOSE = 0;
  public static final int APPLET_LOAD = 1;
  public static final int APPLET_INIT = 2;
  public static final int APPLET_START = 3;
  public static final int APPLET_STOP = 4;
  public static final int APPLET_DESTROY = 5;
  public static final int APPLET_QUIT = 6;
  public static final int APPLET_ERROR = 7;
  public static final int APPLET_RESIZE = 51234;
  public static final int APPLET_LOADING = 51235;
  public static final int APPLET_LOADING_COMPLETED = 51236;
  protected int status;
  protected Thread handler;
  Dimension defaultAppletSize = new Dimension(10, 10);
  Dimension currentAppletSize = new Dimension(10, 10);
  MessageUtils mu = new MessageUtils();
  Thread loaderThread = null;
  boolean loadAbortRequest = false;
  private static int threadGroupNumber = 0;
  private AppletListener listeners;
  private Queue queue = null;
  private EventQueue appEvtQ = null;
  private static HashMap classloaders = new HashMap();
  private boolean jdk11Applet = false;
  private boolean jdk12Applet = false;
  private static AppletMessageHandler amh = new AppletMessageHandler("appletpanel");

  protected abstract String getCode();

  protected abstract String getJarFiles();

  protected abstract String getSerializedObject();

  protected abstract void updateHostIPFile(String paramString);

  public abstract int getWidth();

  public abstract int getHeight();

  public abstract boolean hasInitialFocus();

  protected void setupAppletAppContext()
  {
  }

  protected synchronized void createAppletThread()
  {
    String str = "applet-" + getCode();
    if (this.loader == null)
      this.loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
    this.loader.grab();
    ThreadGroup localThreadGroup = this.loader.getThreadGroup();
    this.handler = new Thread(localThreadGroup, this, "thread " + str);
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        this.this$0.handler.setContextClassLoader(this.this$0.loader);
        return null;
      }
    });
    this.handler.start();
  }

  void joinAppletThread()
    throws InterruptedException
  {
    if (this.handler != null)
    {
      this.handler.join();
      this.handler = null;
    }
  }

  protected void release()
  {
    if (this.loader != null)
    {
      this.loader.release();
      this.loader = null;
    }
  }

  public void init()
  {
    try
    {
      this.defaultAppletSize.width = getWidth();
      this.currentAppletSize.width = this.defaultAppletSize.width;
      this.defaultAppletSize.height = getHeight();
      this.currentAppletSize.height = this.defaultAppletSize.height;
    }
    catch (NumberFormatException localNumberFormatException)
    {
      this.status = 7;
      showAppletStatus("badattribute.exception");
      showAppletLog("badattribute.exception");
      showAppletException(localNumberFormatException);
    }
    setLayout(new BorderLayout());
    String str = getParameter("codebase_lookup");
    if (this.loader == null)
      this.loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
    if ((str != null) && (str.equals("false")))
      this.loader.setCodebaseLookup(false);
    else
      this.loader.setCodebaseLookup(true);
    createAppletThread();
  }

  public Dimension minimumSize()
  {
    return new Dimension(this.defaultAppletSize.width, this.defaultAppletSize.height);
  }

  public Dimension preferredSize()
  {
    return new Dimension(this.currentAppletSize.width, this.currentAppletSize.height);
  }

  public synchronized void addAppletListener(AppletListener paramAppletListener)
  {
    this.listeners = AppletEventMulticaster.add(this.listeners, paramAppletListener);
  }

  public synchronized void removeAppletListener(AppletListener paramAppletListener)
  {
    this.listeners = AppletEventMulticaster.remove(this.listeners, paramAppletListener);
  }

  public void dispatchAppletEvent(int paramInt, Object paramObject)
  {
    if (this.listeners != null)
    {
      AppletEvent localAppletEvent = new AppletEvent(this, paramInt, paramObject);
      this.listeners.appletStateChanged(localAppletEvent);
    }
  }

  public void sendEvent(int paramInt)
  {
    synchronized (this)
    {
      if (this.queue == null)
        this.queue = new Queue();
      Integer localInteger = new Integer(paramInt);
      this.queue.enqueue(localInteger);
      notifyAll();
    }
    if (paramInt == 6)
    {
      try
      {
        joinAppletThread();
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      if (this.loader == null)
        this.loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
      release();
    }
  }

  synchronized AppletEvent getNextEvent()
    throws InterruptedException
  {
    while ((this.queue == null) || (this.queue.isEmpty()))
      wait();
    Integer localInteger = (Integer)this.queue.dequeue();
    return new AppletEvent(this, localInteger.intValue(), null);
  }

  boolean emptyEventQueue()
  {
    return ((this.queue == null) || (this.queue.isEmpty()));
  }

  private void setExceptionStatus(AccessControlException paramAccessControlException)
  {
    Permission localPermission = paramAccessControlException.getPermission();
    if ((localPermission instanceof RuntimePermission) && (localPermission.getName().startsWith("modifyThread")))
    {
      if (this.loader == null)
        this.loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
      this.loader.setExceptionStatus();
    }
  }

  public void run()
  {
    Thread localThread = Thread.currentThread();
    if (localThread == this.loaderThread)
    {
      runLoader();
      return;
    }
    int i = 0;
    while ((i == 0) && (!(localThread.isInterrupted())))
    {
      AppletEvent localAppletEvent;
      try
      {
        localAppletEvent = getNextEvent();
      }
      catch (InterruptedException localInterruptedException1)
      {
        showAppletStatus("bail");
        return;
      }
      try
      {
        switch (localAppletEvent.getID())
        {
        case 1:
          if (!(okToLoad()))
            break label625:
          if (this.loaderThread == null)
          {
            setLoaderThread(new Thread(this));
            this.loaderThread.start();
            this.loaderThread.join();
            setLoaderThread(null);
          }
          break;
        case 2:
          if ((this.status != 1) && (this.status != 5))
          {
            showAppletStatus("notloaded");
          }
          else
          {
            this.applet.resize(this.defaultAppletSize);
            if (this.doInit)
            {
              if (PerformanceLogger.loggingEnabled())
              {
                PerformanceLogger.setTime("Applet Init");
                PerformanceLogger.outputLog();
              }
              this.applet.init();
            }
            Font localFont = getFont();
            if ((localFont == null) || (("dialog".equals(localFont.getFamily().toLowerCase(Locale.ENGLISH))) && (localFont.getSize() == 12) && (localFont.getStyle() == 0)))
              setFont(new Font("Dialog", 0, 12));
            this.doInit = true;
            try
            {
              AppletPanel localAppletPanel1 = this;
              EventQueue.invokeAndWait(new Runnable(this, localAppletPanel1)
              {
                public void run()
                {
                  this.val$p.validate();
                }
              });
            }
            catch (InterruptedException localInterruptedException2)
            {
            }
            catch (InvocationTargetException localInvocationTargetException1)
            {
            }
            this.status = 2;
            showAppletStatus("inited");
          }
          break;
        case 3:
          if ((this.status != 2) && (this.status != 4))
          {
            showAppletStatus("notinited");
          }
          else
          {
            this.applet.resize(this.currentAppletSize);
            this.applet.start();
            try
            {
              AppletPanel localAppletPanel2 = this;
              Applet localApplet3 = this.applet;
              EventQueue.invokeAndWait(new Runnable(this, localAppletPanel2, localApplet3)
              {
                public void run()
                {
                  this.val$p.validate();
                  this.val$a.setVisible(true);
                  if (this.this$0.hasInitialFocus())
                    AppletPanel.access$000(this.this$0);
                }
              });
            }
            catch (InterruptedException localInterruptedException3)
            {
            }
            catch (InvocationTargetException localInvocationTargetException2)
            {
            }
            this.status = 3;
            showAppletStatus("started");
          }
          break;
        case 4:
          if (this.status != 3)
          {
            showAppletStatus("notstarted");
          }
          else
          {
            this.status = 4;
            try
            {
              Applet localApplet1 = this.applet;
              EventQueue.invokeAndWait(new Runnable(this, localApplet1)
              {
                public void run()
                {
                  this.val$a.setVisible(false);
                }
              });
            }
            catch (InterruptedException localInterruptedException4)
            {
            }
            catch (InvocationTargetException localInvocationTargetException3)
            {
            }
            try
            {
              this.applet.stop();
            }
            catch (AccessControlException localAccessControlException1)
            {
              setExceptionStatus(localAccessControlException1);
              throw localAccessControlException1;
            }
            showAppletStatus("stopped");
          }
          break;
        case 5:
          if ((this.status != 4) && (this.status != 2))
          {
            showAppletStatus("notstopped");
          }
          else
          {
            this.status = 5;
            try
            {
              this.applet.destroy();
            }
            catch (AccessControlException localAccessControlException2)
            {
              setExceptionStatus(localAccessControlException2);
              throw localAccessControlException2;
            }
            showAppletStatus("destroyed");
          }
          break;
        case 0:
          if ((this.status != 5) && (this.status != 1))
          {
            showAppletStatus("notdestroyed");
          }
          else
          {
            this.status = 0;
            try
            {
              Applet localApplet2 = this.applet;
              EventQueue.invokeAndWait(new Runnable(this, localApplet2)
              {
                public void run()
                {
                  this.this$0.remove(this.val$a);
                }
              });
            }
            catch (InterruptedException localInterruptedException5)
            {
            }
            catch (InvocationTargetException localInvocationTargetException4)
            {
            }
            this.applet = null;
            showAppletStatus("disposed");
            i = 1;
          }
          break;
        case 6:
          return;
        }
      }
      catch (Exception localException)
      {
        this.status = 7;
        if (localException.getMessage() != null)
          showAppletStatus("exception2", localException.getClass().getName(), localException.getMessage());
        else
          showAppletStatus("exception", localException.getClass().getName());
        showAppletException(localException);
      }
      catch (ThreadDeath localThreadDeath)
      {
        showAppletStatus("death");
        return;
      }
      catch (Error localError)
      {
        label625: this.status = 7;
        if (localError.getMessage() != null)
          showAppletStatus("error2", localError.getClass().getName(), localError.getMessage());
        else
          showAppletStatus("error", localError.getClass().getName());
        showAppletException(localError);
      }
      clearLoadAbortRequest();
    }
  }

  private Component getMostRecentFocusOwnerForWindow(Window paramWindow)
  {
    Method localMethod = (Method)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        Method localMethod = null;
        try
        {
          localMethod = KeyboardFocusManager.class.getDeclaredMethod("getMostRecentFocusOwner", new Class[] { Window.class });
          localMethod.setAccessible(true);
        }
        catch (Exception localException)
        {
          localException.printStackTrace();
        }
        return localMethod;
      }
    });
    if (localMethod != null)
      try
      {
        return ((Component)localMethod.invoke(null, new Object[] { paramWindow }));
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
    return paramWindow.getMostRecentFocusOwner();
  }

  private void setDefaultFocus()
  {
    Component localComponent = null;
    Container localContainer = getParent();
    if (localContainer != null)
      if (localContainer instanceof Window)
      {
        localComponent = getMostRecentFocusOwnerForWindow((Window)localContainer);
        if ((localComponent == localContainer) || (localComponent == null))
          localComponent = localContainer.getFocusTraversalPolicy().getInitialComponent((Window)localContainer);
      }
      else if (localContainer.isFocusCycleRoot())
      {
        localComponent = localContainer.getFocusTraversalPolicy().getDefaultComponent(localContainer);
      }
    if (localComponent != null)
    {
      if (localContainer instanceof EmbeddedFrame)
        ((EmbeddedFrame)localContainer).synthesizeWindowActivation(true);
      localComponent.requestFocusInWindow();
    }
  }

  private void runLoader()
  {
    if (this.status != 0)
    {
      showAppletStatus("notdisposed");
      return;
    }
    dispatchAppletEvent(51235, null);
    this.status = 1;
    this.loader = getClassLoader(getCodeBase(), getClassLoaderCacheKey());
    String str = getCode();
    setupAppletAppContext();
    try
    {
      loadJarFiles(this.loader);
      this.applet = createApplet(this.loader);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      this.status = 7;
      showAppletStatus("notfound", str);
      showAppletLog("notfound", str);
      showAppletException(localClassNotFoundException);
      return;
    }
    catch (InstantiationException localInstantiationException)
    {
      this.status = 7;
      showAppletStatus("nocreate", str);
      showAppletLog("nocreate", str);
      showAppletException(localInstantiationException);
      return;
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      this.status = 7;
      showAppletStatus("noconstruct", str);
      showAppletLog("noconstruct", str);
      showAppletException(localIllegalAccessException);
      return;
    }
    catch (Exception localException)
    {
      this.status = 7;
      showAppletStatus("exception", localException.getMessage());
      showAppletException(localException);
      return;
    }
    catch (ThreadDeath localThreadDeath)
    {
      this.status = 7;
      showAppletStatus("death");
      return;
    }
    catch (Error localError)
    {
      this.status = 7;
      showAppletStatus("error", localError.getMessage());
      showAppletException(localError);
      return;
    }
    finally
    {
      dispatchAppletEvent(51236, null);
    }
    if (this.applet != null)
    {
      this.applet.setStub(this);
      this.applet.hide();
      add("Center", this.applet);
      showAppletStatus("loaded");
      validate();
    }
  }

  protected Applet createApplet(AppletClassLoader paramAppletClassLoader)
    throws ClassNotFoundException, IllegalAccessException, IOException, InstantiationException, InterruptedException
  {
    Object localObject1;
    String str1 = getSerializedObject();
    String str2 = getCode();
    if ((str2 != null) && (str1 != null))
    {
      System.err.println(amh.getMessage("runloader.err"));
      throw new InstantiationException("Either \"code\" or \"object\" should be specified, but not both.");
    }
    if ((str2 == null) && (str1 == null))
    {
      localObject1 = "nocode";
      this.status = 7;
      showAppletStatus((String)localObject1);
      showAppletLog((String)localObject1);
      repaint();
    }
    if (str2 != null)
    {
      this.applet = ((Applet)paramAppletClassLoader.loadCode(str2).newInstance());
      this.doInit = true;
    }
    else
    {
      localObject1 = (InputStream)AccessController.doPrivileged(new PrivilegedAction(this, paramAppletClassLoader, str1)
      {
        public Object run()
        {
          return this.val$loader.getResourceAsStream(this.val$serName);
        }
      });
      AppletObjectInputStream localAppletObjectInputStream = new AppletObjectInputStream((InputStream)localObject1, paramAppletClassLoader);
      Object localObject2 = localAppletObjectInputStream.readObject();
      this.applet = ((Applet)localObject2);
      this.doInit = false;
    }
    findAppletJDKLevel(this.applet);
    if (Thread.interrupted())
    {
      try
      {
        this.status = 0;
        this.applet = null;
        showAppletStatus("death");
      }
      finally
      {
        Thread.currentThread().interrupt();
      }
      return null;
    }
    return ((Applet)this.applet);
  }

  protected void loadJarFiles(AppletClassLoader paramAppletClassLoader)
    throws IOException, InterruptedException
  {
    String str1 = getJarFiles();
    if (str1 != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(str1, ",", false);
      while (localStringTokenizer.hasMoreTokens())
      {
        label21: String str2 = localStringTokenizer.nextToken().trim();
        try
        {
          paramAppletClassLoader.addJar(str2);
        }
        catch (IllegalArgumentException localIllegalArgumentException)
        {
          break label21:
        }
      }
    }
  }

  protected synchronized void stopLoading()
  {
    if (this.loaderThread != null)
      this.loaderThread.interrupt();
    else
      setLoadAbortRequest();
  }

  protected synchronized boolean okToLoad()
  {
    return (!(this.loadAbortRequest));
  }

  protected synchronized void clearLoadAbortRequest()
  {
    this.loadAbortRequest = false;
  }

  protected synchronized void setLoadAbortRequest()
  {
    this.loadAbortRequest = true;
  }

  private synchronized void setLoaderThread(Thread paramThread)
  {
    this.loaderThread = paramThread;
  }

  public boolean isActive()
  {
    return (this.status == 3);
  }

  public void appletResize(int paramInt1, int paramInt2)
  {
    this.currentAppletSize.width = paramInt1;
    this.currentAppletSize.height = paramInt2;
    Dimension localDimension = new Dimension(this.currentAppletSize.width, this.currentAppletSize.height);
    if (this.loader != null)
    {
      localObject = this.loader.getAppContext();
      if (localObject != null)
        this.appEvtQ = ((EventQueue)((AppContext)localObject).get(AppContext.EVENT_QUEUE_KEY));
    }
    Object localObject = this;
    if (this.appEvtQ != null)
      this.appEvtQ.postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(), new Runnable(this, (AppletPanel)localObject, localDimension)
      {
        public void run()
        {
          if (this.val$ap != null)
            this.val$ap.dispatchAppletEvent(51234, this.val$currentSize);
        }
      }));
  }

  public void setBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.setBounds(paramInt1, paramInt2, paramInt3, paramInt4);
    this.currentAppletSize.width = paramInt3;
    this.currentAppletSize.height = paramInt4;
  }

  public Applet getApplet()
  {
    return this.applet;
  }

  protected void showAppletStatus(String paramString)
  {
    getAppletContext().showStatus(amh.getMessage(paramString));
  }

  protected void showAppletStatus(String paramString, Object paramObject)
  {
    getAppletContext().showStatus(amh.getMessage(paramString, paramObject));
  }

  protected void showAppletStatus(String paramString, Object paramObject1, Object paramObject2)
  {
    getAppletContext().showStatus(amh.getMessage(paramString, paramObject1, paramObject2));
  }

  protected void showAppletLog(String paramString)
  {
    System.out.println(amh.getMessage(paramString));
  }

  protected void showAppletLog(String paramString, Object paramObject)
  {
    System.out.println(amh.getMessage(paramString, paramObject));
  }

  protected void showAppletException(Throwable paramThrowable)
  {
    paramThrowable.printStackTrace();
    repaint();
  }

  public String getClassLoaderCacheKey()
  {
    return getCodeBase().toString();
  }

  public static synchronized void flushClassLoader(String paramString)
  {
    classloaders.remove(paramString);
  }

  public static synchronized void flushClassLoaders()
  {
    classloaders = new HashMap();
  }

  protected AppletClassLoader createClassLoader(URL paramURL)
  {
    return new AppletClassLoader(paramURL);
  }

  synchronized AppletClassLoader getClassLoader(URL paramURL, String paramString)
  {
    AppletClassLoader localAppletClassLoader = (AppletClassLoader)classloaders.get(paramString);
    if (localAppletClassLoader == null)
    {
      AccessControlContext localAccessControlContext = getAccessControlContext(paramURL);
      localAppletClassLoader = (AppletClassLoader)AccessController.doPrivileged(new PrivilegedAction(this, paramURL, paramString)
      {
        public Object run()
        {
          AppletClassLoader localAppletClassLoader1 = this.this$0.createClassLoader(this.val$codebase);
          synchronized (super.getClass())
          {
            AppletClassLoader localAppletClassLoader2 = (AppletClassLoader)AppletPanel.access$100().get(this.val$key);
            if (localAppletClassLoader2 != null)
              break label53;
            AppletPanel.access$100().put(this.val$key, localAppletClassLoader1);
            return localAppletClassLoader1;
            label53: return localAppletClassLoader2;
          }
        }
      }
      , localAccessControlContext);
    }
    return localAppletClassLoader;
  }

  private AccessControlContext getAccessControlContext(URL paramURL)
  {
    Permission localPermission;
    Object localObject1 = (PermissionCollection)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        Policy localPolicy = Policy.getPolicy();
        if (localPolicy != null)
          return localPolicy.getPermissions(new CodeSource(null, (Certificate[])null));
        return null;
      }
    });
    if (localObject1 == null)
      localObject1 = new Permissions();
    ((PermissionCollection)localObject1).add(SecurityConstants.CREATE_CLASSLOADER_PERMISSION);
    URLConnection localURLConnection = null;
    try
    {
      localURLConnection = paramURL.openConnection();
      localPermission = localURLConnection.getPermission();
    }
    catch (IOException localIOException)
    {
      localPermission = null;
    }
    if (localPermission != null)
      ((PermissionCollection)localObject1).add(localPermission);
    if (localPermission instanceof FilePermission)
    {
      localObject2 = localPermission.getName();
      int i = ((String)localObject2).lastIndexOf(File.separatorChar);
      if (i != -1)
      {
        localObject2 = ((String)localObject2).substring(0, i + 1);
        if (((String)localObject2).endsWith(File.separator))
          localObject2 = ((String)localObject2) + "-";
        ((PermissionCollection)localObject1).add(new FilePermission((String)localObject2, "read"));
      }
    }
    else
    {
      localObject2 = paramURL;
      if (localURLConnection instanceof JarURLConnection)
        localObject2 = ((JarURLConnection)localURLConnection).getJarFileURL();
      localObject3 = ((URL)localObject2).getHost();
      if ((localObject3 != null) && (((String)localObject3).length() > 0))
      {
        updateHostIPFile((String)localObject3);
        ((PermissionCollection)localObject1).add(new SocketPermission((String)localObject3, "connect,accept"));
      }
    }
    Object localObject2 = new ProtectionDomain(new CodeSource(paramURL, (Certificate[])null), (PermissionCollection)localObject1);
    Object localObject3 = new AccessControlContext(new ProtectionDomain[] { localObject2 });
    return ((AccessControlContext)(AccessControlContext)(AccessControlContext)localObject3);
  }

  public Thread getAppletHandlerThread()
  {
    return this.handler;
  }

  public int getAppletWidth()
  {
    return this.currentAppletSize.width;
  }

  public int getAppletHeight()
  {
    return this.currentAppletSize.height;
  }

  public static void changeFrameAppContext(Frame paramFrame, AppContext paramAppContext)
  {
    AppContext localAppContext = SunToolkit.targetToAppContext(paramFrame);
    if (localAppContext == paramAppContext)
      return;
    synchronized (Window.class)
    {
      Object localObject1 = null;
      Vector localVector = (Vector)localAppContext.get(Window.class);
      if (localVector != null)
      {
        Iterator localIterator = localVector.iterator();
        while (localIterator.hasNext())
        {
          WeakReference localWeakReference = (WeakReference)localIterator.next();
          if (localWeakReference.get() == paramFrame)
          {
            localObject1 = localWeakReference;
            break;
          }
        }
        if (localObject1 != null)
          localVector.remove(localObject1);
      }
      SunToolkit.insertTargetMapping(paramFrame, paramAppContext);
      localVector = (Vector)paramAppContext.get(Window.class);
      if (localVector == null)
      {
        localVector = new Vector();
        paramAppContext.put(Window.class, localVector);
      }
      localVector.add(localObject1);
    }
  }

  private void findAppletJDKLevel(Applet paramApplet)
  {
    Class localClass1 = paramApplet.getClass();
    synchronized (localClass1)
    {
      Boolean localBoolean1 = this.loader.isJDK11Target(localClass1);
      Boolean localBoolean2 = this.loader.isJDK12Target(localClass1);
      if ((localBoolean1 == null) && (localBoolean2 == null))
        break label78;
      this.jdk11Applet = ((localBoolean1 == null) ? false : localBoolean1.booleanValue());
      this.jdk12Applet = ((localBoolean2 == null) ? false : localBoolean2.booleanValue());
      return;
      label78: String str1 = localClass1.getName();
      str1 = str1.replace('.', '/');
      String str2 = str1 + ".class";
      InputStream localInputStream = null;
      byte[] arrayOfByte = new byte[8];
      try
      {
        localInputStream = (InputStream)AccessController.doPrivileged(new PrivilegedAction(this, str2)
        {
          public Object run()
          {
            return this.this$0.loader.getResourceAsStream(this.val$resourceName);
          }
        });
        int i = localInputStream.read(arrayOfByte, 0, 8);
        localInputStream.close();
        if (i != 8)
        {
          monitorexit;
          return;
        }
      }
      catch (IOException localIOException)
      {
        monitorexit;
        return;
      }
      int j = readShort(arrayOfByte, 6);
      if (j >= 46)
        break label205;
      this.jdk11Applet = true;
      break label217:
      label205: if (j != 46)
        break label217;
      this.jdk12Applet = true;
      label217: this.loader.setJDK11Target(localClass1, this.jdk11Applet);
      this.loader.setJDK12Target(localClass1, this.jdk12Applet);
    }
  }

  protected boolean isJDK11Applet()
  {
    return this.jdk11Applet;
  }

  protected boolean isJDK12Applet()
  {
    return this.jdk12Applet;
  }

  private int readShort(byte[] paramArrayOfByte, int paramInt)
  {
    int i = readByte(paramArrayOfByte[paramInt]);
    int j = readByte(paramArrayOfByte[(paramInt + 1)]);
    return (i << 8 | j);
  }

  private int readByte(byte paramByte)
  {
    return (paramByte & 0xFF);
  }
}