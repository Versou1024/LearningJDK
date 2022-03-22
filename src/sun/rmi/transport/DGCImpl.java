package sun.rmi.transport;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.dgc.DGC;
import java.rmi.dgc.Lease;
import java.rmi.dgc.VMID;
import java.rmi.server.LogStream;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import sun.rmi.runtime.Log;
import sun.rmi.runtime.RuntimeUtil;
import sun.rmi.runtime.RuntimeUtil.GetInstanceAction;
import sun.rmi.server.UnicastRef;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.Util;
import sun.security.action.GetLongAction;
import sun.security.action.GetPropertyAction;

final class DGCImpl
  implements DGC
{
  static final Log dgcLog = Log.getLog("sun.rmi.dgc", "dgc", LogStream.parseLevel((String)AccessController.doPrivileged(new GetPropertyAction("sun.rmi.dgc.logLevel"))));
  private static final long leaseValue = ((Long)AccessController.doPrivileged(new GetLongAction("java.rmi.dgc.leaseValue", 600000L))).longValue();
  private static final long leaseCheckInterval = ((Long)AccessController.doPrivileged(new GetLongAction("sun.rmi.dgc.checkInterval", leaseValue / 2L))).longValue();
  private static final ScheduledExecutorService scheduler = ((RuntimeUtil)AccessController.doPrivileged(new RuntimeUtil.GetInstanceAction())).getScheduler();
  private static DGCImpl dgc;
  private Map<VMID, LeaseInfo> leaseTable = new HashMap();
  private Future<?> checker = null;

  static DGCImpl getDGCImpl()
  {
    return dgc;
  }

  public Lease dirty(ObjID[] paramArrayOfObjID, long paramLong, Lease paramLease)
  {
    VMID localVMID = paramLease.getVMID();
    long l = leaseValue;
    if (dgcLog.isLoggable(Log.VERBOSE))
      dgcLog.log(Log.VERBOSE, "vmid = " + localVMID);
    if (localVMID == null)
    {
      localVMID = new VMID();
      if (dgcLog.isLoggable(Log.BRIEF))
      {
        try
        {
          ??? = RemoteServer.getClientHost();
        }
        catch (ServerNotActiveException localServerNotActiveException)
        {
          ??? = "<unknown host>";
        }
        dgcLog.log(Log.BRIEF, " assigning vmid " + localVMID + " to client " + ((String)???));
      }
    }
    paramLease = new Lease(localVMID, l);
    synchronized (this.leaseTable)
    {
      LeaseInfo localLeaseInfo = (LeaseInfo)this.leaseTable.get(localVMID);
      if (localLeaseInfo == null)
      {
        this.leaseTable.put(localVMID, new LeaseInfo(localVMID, l));
        if (this.checker == null)
          this.checker = scheduler.scheduleWithFixedDelay(new Runnable(this)
          {
            public void run()
            {
              DGCImpl.access$000(this.this$0);
            }
          }
          , leaseCheckInterval, leaseCheckInterval, TimeUnit.MILLISECONDS);
      }
      else
      {
        localLeaseInfo.renew(l);
      }
    }
    ??? = paramArrayOfObjID;
    int i = ???.length;
    for (int j = 0; j < i; ++j)
    {
      Object localObject3 = ???[j];
      if (dgcLog.isLoggable(Log.VERBOSE))
        dgcLog.log(Log.VERBOSE, "id = " + localObject3 + ", vmid = " + localVMID + ", duration = " + l);
      ObjectTable.referenced(localObject3, paramLong, localVMID);
    }
    return ((Lease)paramLease);
  }

  public void clean(ObjID[] paramArrayOfObjID, long paramLong, VMID paramVMID, boolean paramBoolean)
  {
    ObjID[] arrayOfObjID = paramArrayOfObjID;
    int i = arrayOfObjID.length;
    for (int j = 0; j < i; ++j)
    {
      ObjID localObjID = arrayOfObjID[j];
      if (dgcLog.isLoggable(Log.VERBOSE))
        dgcLog.log(Log.VERBOSE, "id = " + localObjID + ", vmid = " + paramVMID + ", strong = " + paramBoolean);
      ObjectTable.unreferenced(localObjID, paramLong, paramVMID, paramBoolean);
    }
  }

  void registerTarget(VMID paramVMID, Target paramTarget)
  {
    synchronized (this.leaseTable)
    {
      LeaseInfo localLeaseInfo = (LeaseInfo)this.leaseTable.get(paramVMID);
      if (localLeaseInfo == null)
        paramTarget.vmidDead(paramVMID);
      else
        localLeaseInfo.notifySet.add(paramTarget);
    }
  }

  void unregisterTarget(VMID paramVMID, Target paramTarget)
  {
    synchronized (this.leaseTable)
    {
      LeaseInfo localLeaseInfo = (LeaseInfo)this.leaseTable.get(paramVMID);
      if (localLeaseInfo != null)
        localLeaseInfo.notifySet.remove(paramTarget);
    }
  }

  private void checkLeases()
  {
    Object localObject2;
    Object localObject3;
    long l = System.currentTimeMillis();
    ArrayList localArrayList = new ArrayList();
    synchronized (this.leaseTable)
    {
      localObject2 = this.leaseTable.values().iterator();
      while (((Iterator)localObject2).hasNext())
      {
        localObject3 = (LeaseInfo)((Iterator)localObject2).next();
        if (((LeaseInfo)localObject3).expired(l))
        {
          localArrayList.add(localObject3);
          ((Iterator)localObject2).remove();
        }
      }
      if (this.leaseTable.isEmpty())
      {
        this.checker.cancel(false);
        this.checker = null;
      }
    }
    ??? = localArrayList.iterator();
    while (((Iterator)???).hasNext())
    {
      localObject2 = (LeaseInfo)((Iterator)???).next();
      localObject3 = ((LeaseInfo)localObject2).notifySet.iterator();
      while (((Iterator)localObject3).hasNext())
      {
        Target localTarget = (Target)((Iterator)localObject3).next();
        localTarget.vmidDead(((LeaseInfo)localObject2).vmid);
      }
    }
  }

  static
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Void run()
      {
        ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
          Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
          try
          {
            DGCImpl.access$102(new DGCImpl(null));
            ObjID localObjID = new ObjID(2);
            LiveRef localLiveRef = new LiveRef(localObjID, 0);
            UnicastServerRef localUnicastServerRef = new UnicastServerRef(localLiveRef);
            Remote localRemote = Util.createProxy(DGCImpl.class, new UnicastRef(localLiveRef), true);
            localUnicastServerRef.setSkeleton(DGCImpl.access$100());
            Target localTarget = new Target(DGCImpl.access$100(), localUnicastServerRef, localRemote, localObjID, true);
            ObjectTable.putTarget(localTarget);
          }
          catch (RemoteException localRemoteException)
          {
            throw new Error("exception initializing server-side DGC", localRemoteException);
          }
        }
        finally
        {
          Thread.currentThread().setContextClassLoader(localClassLoader);
        }
        return null;
      }
    });
  }

  private static class LeaseInfo
  {
    VMID vmid;
    long expiration;
    Set<Target> notifySet = new HashSet();

    LeaseInfo(VMID paramVMID, long paramLong)
    {
      this.vmid = paramVMID;
      this.expiration = (System.currentTimeMillis() + paramLong);
    }

    synchronized void renew(long paramLong)
    {
      long l = System.currentTimeMillis() + paramLong;
      if (l > this.expiration)
        this.expiration = l;
    }

    boolean expired(long paramLong)
    {
      if (this.expiration < paramLong)
      {
        if (DGCImpl.dgcLog.isLoggable(Log.BRIEF))
          DGCImpl.dgcLog.log(Log.BRIEF, this.vmid.toString());
        return true;
      }
      return false;
    }
  }
}