package sun.awt;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.InvocationEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppContext
{
  private static final Logger log = Logger.getLogger("sun.awt.AppContext");
  public static final Object EVENT_QUEUE_KEY = new StringBuffer("EventQueue");
  private static final Map<ThreadGroup, AppContext> threadGroup2appContext = Collections.synchronizedMap(new IdentityHashMap());
  private static AppContext mainAppContext = null;
  private final HashMap table = new HashMap();
  private final ThreadGroup threadGroup;
  private PropertyChangeSupport changeSupport = null;
  public static final String DISPOSED_PROPERTY_NAME = "disposed";
  public static final String GUI_DISPOSED = "guidisposed";
  private boolean isDisposed = false;
  private static int numAppContexts;
  private final ClassLoader contextClassLoader;
  private static MostRecentThreadAppContext mostRecentThreadAppContext;
  private long DISPOSAL_TIMEOUT = 5000L;
  private long THREAD_INTERRUPT_TIMEOUT = 1000L;
  private MostRecentKeyValue mostRecentKeyValue = null;
  private MostRecentKeyValue shadowMostRecentKeyValue = null;

  public static Set<AppContext> getAppContexts()
  {
    synchronized (threadGroup2appContext)
    {
      return new HashSet(threadGroup2appContext.values());
    }
  }

  public boolean isDisposed()
  {
    return this.isDisposed;
  }

  AppContext(ThreadGroup paramThreadGroup)
  {
    numAppContexts += 1;
    this.threadGroup = paramThreadGroup;
    threadGroup2appContext.put(paramThreadGroup, this);
    this.contextClassLoader = ((ClassLoader)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        return Thread.currentThread().getContextClassLoader();
      }
    }));
  }

  public static final AppContext getAppContext()
  {
    if (numAppContexts == 1)
      return mainAppContext;
    Thread localThread = Thread.currentThread();
    Object localObject = null;
    MostRecentThreadAppContext localMostRecentThreadAppContext = mostRecentThreadAppContext;
    if ((localMostRecentThreadAppContext != null) && (localMostRecentThreadAppContext.thread == localThread))
      localObject = localMostRecentThreadAppContext.appContext;
    else
      localObject = (AppContext)AccessController.doPrivileged(new PrivilegedAction(localThread)
      {
        public Object run()
        {
          ThreadGroup localThreadGroup1 = this.val$currentThread.getThreadGroup();
          ThreadGroup localThreadGroup2 = localThreadGroup1;
          for (AppContext localAppContext = (AppContext)AppContext.access$200().get(localThreadGroup2); localAppContext == null; localAppContext = (AppContext)AppContext.access$200().get(localThreadGroup2))
          {
            localThreadGroup2 = localThreadGroup2.getParent();
            if (localThreadGroup2 == null)
              throw new RuntimeException("Invalid ThreadGroup");
          }
          for (ThreadGroup localThreadGroup3 = localThreadGroup1; localThreadGroup3 != localThreadGroup2; localThreadGroup3 = localThreadGroup3.getParent())
            AppContext.access$200().put(localThreadGroup3, localAppContext);
          AppContext.access$302(new MostRecentThreadAppContext(this.val$currentThread, localAppContext));
          return localAppContext;
        }
      });
    if (localObject == mainAppContext)
    {
      SecurityManager localSecurityManager = System.getSecurityManager();
      if ((localSecurityManager != null) && (localSecurityManager instanceof AWTSecurityManager))
      {
        AWTSecurityManager localAWTSecurityManager = (AWTSecurityManager)localSecurityManager;
        AppContext localAppContext = localAWTSecurityManager.getAppContext();
        if (localAppContext != null)
          localObject = localAppContext;
      }
    }
    return ((AppContext)localObject);
  }

  public void dispose()
    throws IllegalThreadStateException
  {
    if (this.threadGroup.parentOf(Thread.currentThread().getThreadGroup()))
      throw new IllegalThreadStateException("Current Thread is contained within AppContext to be disposed.");
    synchronized (this)
    {
      if (!(this.isDisposed))
        break label40;
      return;
      label40: this.isDisposed = true;
    }
    ??? = this.changeSupport;
    if (??? != null)
      ((PropertyChangeSupport)???).firePropertyChange("disposed", false, true);
    Object localObject3 = new Object();
    Object localObject4 = new Runnable(this, (PropertyChangeSupport)???, localObject3)
    {
      public void run()
      {
        Window[] arrayOfWindow = Window.getOwnerlessWindows();
        ??? = arrayOfWindow;
        int i = ???.length;
        for (int j = 0; j < i; ++j)
        {
          Object localObject2 = ???[j];
          try
          {
            localObject2.dispose();
          }
          catch (Throwable localThrowable)
          {
            if (AppContext.access$400().isLoggable(Level.FINER))
              AppContext.access$400().log(Level.FINER, "exception occured while disposing app context", localThrowable);
          }
        }
        AccessController.doPrivileged(new PrivilegedAction(this)
        {
          public Object run()
          {
            if ((!(GraphicsEnvironment.isHeadless())) && (SystemTray.isSupported()))
            {
              SystemTray localSystemTray = SystemTray.getSystemTray();
              TrayIcon[] arrayOfTrayIcon1 = localSystemTray.getTrayIcons();
              TrayIcon[] arrayOfTrayIcon2 = arrayOfTrayIcon1;
              int i = arrayOfTrayIcon2.length;
              for (int j = 0; j < i; ++j)
              {
                TrayIcon localTrayIcon = arrayOfTrayIcon2[j];
                localSystemTray.remove(localTrayIcon);
              }
            }
            return null;
          }
        });
        if (this.val$changeSupport != null)
          this.val$changeSupport.firePropertyChange("guidisposed", false, true);
        synchronized (this.val$notificationLock)
        {
          this.val$notificationLock.notifyAll();
        }
      }
    };
    synchronized (localObject3)
    {
      SunToolkit.postEvent(this, new InvocationEvent(Toolkit.getDefaultToolkit(), (Runnable)localObject4));
      try
      {
        localObject3.wait(this.DISPOSAL_TIMEOUT);
      }
      catch (InterruptedException localInterruptedException1)
      {
      }
    }
    localObject4 = new Runnable(this, localObject3)
    {
      public void run()
      {
        synchronized (this.val$notificationLock)
        {
          this.val$notificationLock.notifyAll();
        }
      }
    };
    synchronized (localObject3)
    {
      SunToolkit.postEvent(this, new InvocationEvent(Toolkit.getDefaultToolkit(), (Runnable)localObject4));
      try
      {
        localObject3.wait(this.DISPOSAL_TIMEOUT);
      }
      catch (InterruptedException localInterruptedException2)
      {
      }
    }
    this.threadGroup.interrupt();
    long l1 = System.currentTimeMillis();
    long l2 = l1 + this.THREAD_INTERRUPT_TIMEOUT;
    label216: if ((this.threadGroup.activeCount() > 0) && (System.currentTimeMillis() < l2));
    try
    {
      Thread.sleep(10L);
    }
    catch (InterruptedException i)
    {
      break label216:
      this.threadGroup.stop();
      l1 = System.currentTimeMillis();
      l2 = l1 + this.THREAD_INTERRUPT_TIMEOUT;
      label270: if ((this.threadGroup.activeCount() > 0) && (System.currentTimeMillis() < l2));
      try
      {
        Thread.sleep(10L);
      }
      catch (InterruptedException i)
      {
        break label270:
        int i = this.threadGroup.activeGroupCount();
        if (i > 0)
        {
          localObject8 = new ThreadGroup[i];
          i = this.threadGroup.enumerate(localObject8);
          for (int j = 0; j < i; ++j)
            threadGroup2appContext.remove(localObject8[j]);
        }
        threadGroup2appContext.remove(this.threadGroup);
        Object localObject8 = mostRecentThreadAppContext;
        if ((localObject8 != null) && (((MostRecentThreadAppContext)localObject8).appContext == this))
          mostRecentThreadAppContext = null;
        try
        {
          this.threadGroup.destroy();
        }
        catch (IllegalThreadStateException localIllegalThreadStateException)
        {
        }
        synchronized (this.table)
        {
          this.table.clear();
        }
        numAppContexts -= 1;
        this.mostRecentKeyValue = null;
      }
    }
  }

  static void stopEventDispatchThreads()
  {
    Iterator localIterator = getAppContexts().iterator();
    while (true)
    {
      AppContext localAppContext;
      while (true)
      {
        if (!(localIterator.hasNext()))
          return;
        localAppContext = (AppContext)localIterator.next();
        if (!(localAppContext.isDisposed()))
          break;
      }
      PostShutdownEventRunnable localPostShutdownEventRunnable = new PostShutdownEventRunnable(localAppContext);
      if (localAppContext != getAppContext())
      {
        CreateThreadAction localCreateThreadAction = new CreateThreadAction(localAppContext, localPostShutdownEventRunnable);
        Thread localThread = (Thread)AccessController.doPrivileged(localCreateThreadAction);
        localThread.start();
      }
      else
      {
        localPostShutdownEventRunnable.run();
      }
    }
  }

  public Object get(Object paramObject)
  {
    synchronized (this.table)
    {
      MostRecentKeyValue localMostRecentKeyValue1 = this.mostRecentKeyValue;
      if ((localMostRecentKeyValue1 == null) || (localMostRecentKeyValue1.key != paramObject))
        break label31;
      return localMostRecentKeyValue1.value;
      label31: Object localObject1 = this.table.get(paramObject);
      if (this.mostRecentKeyValue != null)
        break label79;
      this.mostRecentKeyValue = new MostRecentKeyValue(paramObject, localObject1);
      this.shadowMostRecentKeyValue = new MostRecentKeyValue(paramObject, localObject1);
      break label109:
      label79: MostRecentKeyValue localMostRecentKeyValue2 = this.mostRecentKeyValue;
      this.shadowMostRecentKeyValue.setPair(paramObject, localObject1);
      this.mostRecentKeyValue = this.shadowMostRecentKeyValue;
      this.shadowMostRecentKeyValue = localMostRecentKeyValue2;
      label109: return localObject1;
    }
  }

  public Object put(Object paramObject1, Object paramObject2)
  {
    synchronized (this.table)
    {
      MostRecentKeyValue localMostRecentKeyValue = this.mostRecentKeyValue;
      if ((localMostRecentKeyValue != null) && (localMostRecentKeyValue.key == paramObject1))
        localMostRecentKeyValue.value = paramObject2;
      return this.table.put(paramObject1, paramObject2);
    }
  }

  public Object remove(Object paramObject)
  {
    synchronized (this.table)
    {
      MostRecentKeyValue localMostRecentKeyValue = this.mostRecentKeyValue;
      if ((localMostRecentKeyValue != null) && (localMostRecentKeyValue.key == paramObject))
        localMostRecentKeyValue.value = null;
      return this.table.remove(paramObject);
    }
  }

  public ThreadGroup getThreadGroup()
  {
    return this.threadGroup;
  }

  public ClassLoader getContextClassLoader()
  {
    return this.contextClassLoader;
  }

  public String toString()
  {
    return super.getClass().getName() + "[threadGroup=" + this.threadGroup.getName() + "]";
  }

  public synchronized PropertyChangeListener[] getPropertyChangeListeners()
  {
    if (this.changeSupport == null)
      return new PropertyChangeListener[0];
    return this.changeSupport.getPropertyChangeListeners();
  }

  public synchronized void addPropertyChangeListener(String paramString, PropertyChangeListener paramPropertyChangeListener)
  {
    if (paramPropertyChangeListener == null)
      return;
    if (this.changeSupport == null)
      this.changeSupport = new PropertyChangeSupport(this);
    this.changeSupport.addPropertyChangeListener(paramString, paramPropertyChangeListener);
  }

  public synchronized void removePropertyChangeListener(String paramString, PropertyChangeListener paramPropertyChangeListener)
  {
    if ((paramPropertyChangeListener == null) || (this.changeSupport == null))
      return;
    this.changeSupport.removePropertyChangeListener(paramString, paramPropertyChangeListener);
  }

  public synchronized PropertyChangeListener[] getPropertyChangeListeners(String paramString)
  {
    if (this.changeSupport == null)
      return new PropertyChangeListener[0];
    return this.changeSupport.getPropertyChangeListeners(paramString);
  }

  static
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        Object localObject = Thread.currentThread().getThreadGroup();
        for (ThreadGroup localThreadGroup = ((ThreadGroup)localObject).getParent(); localThreadGroup != null; localThreadGroup = ((ThreadGroup)localObject).getParent())
          localObject = localThreadGroup;
        AppContext.access$002(new AppContext((ThreadGroup)localObject));
        AppContext.access$102(1);
        return AppContext.access$000();
      }
    });
    mostRecentThreadAppContext = null;
  }

  static final class CreateThreadAction
  implements PrivilegedAction
  {
    private final AppContext appContext;
    private final Runnable runnable;

    public CreateThreadAction(AppContext paramAppContext, Runnable paramRunnable)
    {
      this.appContext = paramAppContext;
      this.runnable = paramRunnable;
    }

    public Object run()
    {
      Thread localThread = new Thread(this.appContext.getThreadGroup(), this.runnable);
      localThread.setContextClassLoader(this.appContext.getContextClassLoader());
      localThread.setPriority(6);
      localThread.setDaemon(true);
      return localThread;
    }
  }

  static final class PostShutdownEventRunnable
  implements Runnable
  {
    private final AppContext appContext;

    public PostShutdownEventRunnable(AppContext paramAppContext)
    {
      this.appContext = paramAppContext;
    }

    public void run()
    {
      EventQueue localEventQueue = (EventQueue)this.appContext.get(AppContext.EVENT_QUEUE_KEY);
      if (localEventQueue != null)
        localEventQueue.postEvent(AWTAutoShutdown.getShutdownEvent());
    }
  }
}