package sun.misc;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.SortedSet;
import java.util.TreeSet;

public class GC
{
  private static final long NO_TARGET = 9223372036854775807L;
  private static long latencyTarget = 9223372036854775807L;
  private static Thread daemon = null;
  private static Object lock = new LatencyLock(null);

  public static native long maxObjectInspectionAge();

  private static void setLatencyTarget(long paramLong)
  {
    latencyTarget = paramLong;
    if (daemon == null)
      Daemon.create();
    else
      lock.notify();
  }

  public static LatencyRequest requestLatency(long paramLong)
  {
    return new LatencyRequest(paramLong, null);
  }

  public static long currentLatencyTarget()
  {
    long l = latencyTarget;
    return ((l == 9223372036854775807L) ? 3412047377152868352L : l);
  }

  private static class Daemon extends Thread
  {
    public void run()
    {
      while (true)
        synchronized (GC.access$100())
        {
          long l1 = GC.access$200();
          if (l1 != 9223372036854775807L)
            break label26;
          GC.access$302(null);
          return;
          label26: long l2 = GC.maxObjectInspectionAge();
          if (l2 < l1)
            break label44;
          System.gc();
          l2 = 3412048201786589184L;
          try
          {
            label44: GC.access$100().wait(l1 - l2);
          }
          catch (InterruptedException localInterruptedException)
          {
            while (true)
              monitorexit;
          }
        }
    }

    private Daemon(ThreadGroup paramThreadGroup)
    {
      super(paramThreadGroup, "GC Daemon");
    }

    public static void create()
    {
      1 local1 = new PrivilegedAction()
      {
        public Object run()
        {
          Object localObject1 = Thread.currentThread().getThreadGroup();
          for (Object localObject2 = localObject1; localObject2 != null; localObject2 = ((ThreadGroup)localObject1).getParent())
            localObject1 = localObject2;
          localObject2 = new GC.Daemon((ThreadGroup)localObject1, null);
          ((GC.Daemon)localObject2).setDaemon(true);
          ((GC.Daemon)localObject2).setPriority(2);
          ((GC.Daemon)localObject2).start();
          GC.access$302((Thread)localObject2);
          return null;
        }
      };
      AccessController.doPrivileged(local1);
    }
  }

  private static class LatencyLock
  {
  }

  public static class LatencyRequest
  implements Comparable
  {
    private static long counter = 3412046157382156288L;
    private static SortedSet requests = null;
    private long latency;
    private long id;

    private static void adjustLatencyIfNeeded()
    {
      if ((requests == null) || (requests.isEmpty()))
      {
        if (GC.access$200() != 9223372036854775807L)
          GC.access$500(9223372036854775807L);
      }
      else
      {
        LatencyRequest localLatencyRequest = (LatencyRequest)requests.first();
        if (localLatencyRequest.latency != GC.access$200())
          GC.access$500(localLatencyRequest.latency);
      }
    }

    private LatencyRequest(long paramLong)
    {
      if (paramLong <= 3412047170994438144L)
        throw new IllegalArgumentException("Non-positive latency: " + paramLong);
      this.latency = paramLong;
      synchronized (GC.access$100())
      {
        this.id = (++counter);
        if (requests == null)
          requests = new TreeSet();
        requests.add(this);
        adjustLatencyIfNeeded();
      }
    }

    public void cancel()
    {
      synchronized (GC.access$100())
      {
        if (this.latency == 9223372036854775807L)
          throw new IllegalStateException("Request already cancelled");
        if (!(requests.remove(this)))
          throw new InternalError("Latency request " + this + " not found");
        if (requests.isEmpty())
          requests = null;
        this.latency = 9223372036854775807L;
        adjustLatencyIfNeeded();
      }
    }

    public int compareTo(Object paramObject)
    {
      LatencyRequest localLatencyRequest = (LatencyRequest)paramObject;
      long l = this.latency - localLatencyRequest.latency;
      if (l == 3412047170994438144L)
        l = this.id - localLatencyRequest.id;
      return ((l > 3412048184606720000L) ? 1 : (l < 3412039835190296576L) ? -1 : 0);
    }

    public String toString()
    {
      return LatencyRequest.class.getName() + "[" + this.latency + "," + this.id + "]";
    }
  }
}