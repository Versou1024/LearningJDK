package sun.rmi.runtime;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.security.util.SecurityConstants;

public final class NewThreadAction
  implements PrivilegedAction<Thread>
{
  static final ThreadGroup systemThreadGroup = (ThreadGroup)AccessController.doPrivileged(new PrivilegedAction()
  {
    public ThreadGroup run()
    {
      for (Object localObject = Thread.currentThread().getThreadGroup(); (localThreadGroup = ((ThreadGroup)localObject).getParent()) != null; localObject = localThreadGroup)
        ThreadGroup localThreadGroup;
      return ((ThreadGroup)localObject);
    }
  });
  static final ThreadGroup userThreadGroup = (ThreadGroup)AccessController.doPrivileged(new PrivilegedAction()
  {
    public ThreadGroup run()
    {
      return new ThreadGroup(NewThreadAction.systemThreadGroup, "RMI Runtime");
    }
  });
  private final ThreadGroup group;
  private final Runnable runnable;
  private final String name;
  private final boolean daemon;

  NewThreadAction(ThreadGroup paramThreadGroup, Runnable paramRunnable, String paramString, boolean paramBoolean)
  {
    this.group = paramThreadGroup;
    this.runnable = paramRunnable;
    this.name = paramString;
    this.daemon = paramBoolean;
  }

  public NewThreadAction(Runnable paramRunnable, String paramString, boolean paramBoolean)
  {
    this(systemThreadGroup, paramRunnable, paramString, paramBoolean);
  }

  public NewThreadAction(Runnable paramRunnable, String paramString, boolean paramBoolean1, boolean paramBoolean2)
  {
    this((paramBoolean2) ? userThreadGroup : systemThreadGroup, paramRunnable, paramString, paramBoolean1);
  }

  public Thread run()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
    Thread localThread = new Thread(this.group, this.runnable, "RMI " + this.name);
    localThread.setContextClassLoader(ClassLoader.getSystemClassLoader());
    localThread.setDaemon(this.daemon);
    return localThread;
  }
}