package sun.rmi.transport;

import java.lang.ref.ReferenceQueue;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.dgc.VMID;
import java.rmi.server.ExportException;
import java.rmi.server.ObjID;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import sun.misc.GC;
import sun.misc.GC.LatencyRequest;
import sun.rmi.runtime.Log;
import sun.rmi.runtime.NewThreadAction;
import sun.security.action.GetLongAction;

public final class ObjectTable
{
  private static final long gcInterval = ((Long)AccessController.doPrivileged(new GetLongAction("sun.rmi.dgc.server.gcInterval", 3600000L))).longValue();
  private static final Object tableLock = new Object();
  private static final Map<ObjectEndpoint, Target> objTable = new HashMap();
  private static final Map<WeakRef, Target> implTable = new HashMap();
  private static final Object keepAliveLock = new Object();
  private static int keepAliveCount = 0;
  private static Thread reaper = null;
  static final ReferenceQueue reapQueue = new ReferenceQueue();
  private static GC.LatencyRequest gcLatencyRequest = null;

  static Target getTarget(ObjectEndpoint paramObjectEndpoint)
  {
    synchronized (tableLock)
    {
      return ((Target)objTable.get(paramObjectEndpoint));
    }
  }

  public static Target getTarget(Remote paramRemote)
  {
    synchronized (tableLock)
    {
      return ((Target)implTable.get(new WeakRef(paramRemote)));
    }
  }

  public static Remote getStub(Remote paramRemote)
    throws NoSuchObjectException
  {
    Target localTarget = getTarget(paramRemote);
    if (localTarget == null)
      throw new NoSuchObjectException("object not exported");
    return localTarget.getStub();
  }

  public static boolean unexportObject(Remote paramRemote, boolean paramBoolean)
    throws NoSuchObjectException
  {
    synchronized (tableLock)
    {
      Target localTarget = getTarget(paramRemote);
      if (localTarget == null)
        throw new NoSuchObjectException("object not exported");
      if (!(localTarget.unexport(paramBoolean)))
        break label41;
      removeTarget(localTarget);
      return true;
      label41: return false;
    }
  }

  static void putTarget(Target paramTarget)
    throws ExportException
  {
    ObjectEndpoint localObjectEndpoint = paramTarget.getObjectEndpoint();
    WeakRef localWeakRef = paramTarget.getWeakImpl();
    if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE))
      DGCImpl.dgcLog.log(Log.VERBOSE, "add object " + localObjectEndpoint);
    Remote localRemote = paramTarget.getImpl();
    if (localRemote == null)
      throw new ExportException("internal error: attempt to export collected object");
    synchronized (tableLock)
    {
      if (objTable.containsKey(localObjectEndpoint))
        throw new ExportException("internal error: ObjID already in use");
      if (implTable.containsKey(localWeakRef))
        throw new ExportException("object already exported");
      objTable.put(localObjectEndpoint, paramTarget);
      implTable.put(localWeakRef, paramTarget);
      if (!(paramTarget.isPermanent()))
        incrementKeepAliveCount();
    }
  }

  private static void removeTarget(Target paramTarget)
  {
    ObjectEndpoint localObjectEndpoint = paramTarget.getObjectEndpoint();
    WeakRef localWeakRef = paramTarget.getWeakImpl();
    if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE))
      DGCImpl.dgcLog.log(Log.VERBOSE, "remove object " + localObjectEndpoint);
    objTable.remove(localObjectEndpoint);
    implTable.remove(localWeakRef);
    paramTarget.markRemoved();
  }

  static void referenced(ObjID paramObjID, long paramLong, VMID paramVMID)
  {
    synchronized (tableLock)
    {
      ObjectEndpoint localObjectEndpoint = new ObjectEndpoint(paramObjID, Transport.currentTransport());
      Target localTarget = (Target)objTable.get(localObjectEndpoint);
      if (localTarget != null)
        localTarget.referenced(paramLong, paramVMID);
    }
  }

  static void unreferenced(ObjID paramObjID, long paramLong, VMID paramVMID, boolean paramBoolean)
  {
    synchronized (tableLock)
    {
      ObjectEndpoint localObjectEndpoint = new ObjectEndpoint(paramObjID, Transport.currentTransport());
      Target localTarget = (Target)objTable.get(localObjectEndpoint);
      if (localTarget != null)
        localTarget.unreferenced(paramLong, paramVMID, paramBoolean);
    }
  }

  static void incrementKeepAliveCount()
  {
    synchronized (keepAliveLock)
    {
      keepAliveCount += 1;
      if (reaper == null)
      {
        reaper = (Thread)AccessController.doPrivileged(new NewThreadAction(new Reaper(null), "Reaper", false));
        reaper.start();
      }
      if (gcLatencyRequest == null)
        gcLatencyRequest = GC.requestLatency(gcInterval);
    }
  }

  static void decrementKeepAliveCount()
  {
    synchronized (keepAliveLock)
    {
      keepAliveCount -= 1;
      if (keepAliveCount == 0)
      {
        if (reaper == null)
          throw new AssertionError();
        AccessController.doPrivileged(new PrivilegedAction()
        {
          public Void run()
          {
            ObjectTable.access$100().interrupt();
            return null;
          }
        });
        reaper = null;
        gcLatencyRequest.cancel();
        gcLatencyRequest = null;
      }
    }
  }

  private static class Reaper
  implements Runnable
  {
    public void run()
    {
      WeakRef localWeakRef;
      try
      {
        do
        {
          localWeakRef = (WeakRef)ObjectTable.reapQueue.remove();
          synchronized (ObjectTable.access$200())
          {
            Target localTarget = (Target)ObjectTable.access$300().get(localWeakRef);
            if (localTarget != null)
            {
              if (!(localTarget.isEmpty()))
                throw new Error("object with known references collected");
              if (localTarget.isPermanent())
                throw new Error("permanent object collected");
              ObjectTable.access$400(localTarget);
            }
          }
        }
        while (!(Thread.interrupted()));
      }
      catch (InterruptedException localInterruptedException)
      {
      }
    }
  }
}