package sun.rmi.runtime;

import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import sun.security.action.GetIntegerAction;

public final class RuntimeUtil
{
  private static final Log runtimeLog = Log.getLog("sun.rmi.runtime", null, false);
  private static final int schedulerThreads = ((Integer)AccessController.doPrivileged(new GetIntegerAction("sun.rmi.runtime.schedulerThreads", 1))).intValue();
  private static final Permission GET_INSTANCE_PERMISSION = new RuntimePermission("sun.rmi.runtime.RuntimeUtil.getInstance");
  private static final RuntimeUtil instance = new RuntimeUtil();
  private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(schedulerThreads, new ThreadFactory(???)
  {
    private final AtomicInteger count = new AtomicInteger(0);

    public Thread newThread()
    {
      try
      {
        return ((Thread)AccessController.doPrivileged(new NewThreadAction(paramRunnable, "Scheduler(" + this.count.getAndIncrement() + ")", true)));
      }
      catch (Throwable localThrowable)
      {
        RuntimeUtil.access$000().log(Level.WARNING, "scheduler thread factory throws", localThrowable);
      }
      return null;
    }
  });

  private static RuntimeUtil getInstance()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPermission(GET_INSTANCE_PERMISSION);
    return instance;
  }

  public ScheduledThreadPoolExecutor getScheduler()
  {
    return this.scheduler;
  }

  public static class GetInstanceAction
  implements PrivilegedAction<RuntimeUtil>
  {
    public RuntimeUtil run()
    {
      return RuntimeUtil.access$100();
    }
  }
}