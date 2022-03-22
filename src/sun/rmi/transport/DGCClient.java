package sun.rmi.transport;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.rmi.RemoteException;
import java.rmi.dgc.DGC;
import java.rmi.dgc.Lease;
import java.rmi.dgc.VMID;
import java.rmi.server.ObjID;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sun.misc.GC;
import sun.misc.GC.LatencyRequest;
import sun.rmi.runtime.NewThreadAction;
import sun.rmi.server.UnicastRef;
import sun.rmi.server.Util;
import sun.security.action.GetLongAction;

final class DGCClient
{
  private static long nextSequenceNum = -9223372036854775808L;
  private static VMID vmid = new VMID();
  private static final long leaseValue = ((Long)AccessController.doPrivileged(new GetLongAction("java.rmi.dgc.leaseValue", 600000L))).longValue();
  private static final long cleanInterval = ((Long)AccessController.doPrivileged(new GetLongAction("sun.rmi.dgc.cleanInterval", 180000L))).longValue();
  private static final long gcInterval = ((Long)AccessController.doPrivileged(new GetLongAction("sun.rmi.dgc.client.gcInterval", 3600000L))).longValue();
  private static final int dirtyFailureRetries = 5;
  private static final int cleanFailureRetries = 5;
  private static final ObjID[] emptyObjIDArray = new ObjID[0];
  private static final ObjID dgcID = new ObjID(2);

  static void registerRefs(Endpoint paramEndpoint, List paramList)
  {
    EndpointEntry localEndpointEntry;
    do
      localEndpointEntry = EndpointEntry.lookup(paramEndpoint);
    while (!(localEndpointEntry.registerRefs(paramList)));
  }

  private static synchronized long getNextSequenceNum()
  {
    return (nextSequenceNum++);
  }

  private static long computeRenewTime(long paramLong1, long paramLong2)
  {
    return (paramLong1 + paramLong2 / 2L);
  }

  private static class EndpointEntry
  {
    private Endpoint endpoint;
    private DGC dgc;
    private Map refTable = new HashMap(5);
    private Set invalidRefs = new HashSet(5);
    private boolean removed = false;
    private long renewTime = 9223372036854775807L;
    private long expirationTime = -9223372036854775808L;
    private int dirtyFailures = 0;
    private long dirtyFailureStartTime;
    private long dirtyFailureDuration;
    private Thread renewCleanThread;
    private boolean interruptible = false;
    private ReferenceQueue refQueue = new ReferenceQueue();
    private Set pendingCleans = new HashSet(5);
    private static Map endpointTable;
    private static GC.LatencyRequest gcLatencyRequest;

    public static EndpointEntry lookup(Endpoint paramEndpoint)
    {
      synchronized (endpointTable)
      {
        EndpointEntry localEndpointEntry = (EndpointEntry)endpointTable.get(paramEndpoint);
        if (localEndpointEntry == null)
        {
          localEndpointEntry = new EndpointEntry(paramEndpoint);
          endpointTable.put(paramEndpoint, localEndpointEntry);
          if (gcLatencyRequest == null)
            gcLatencyRequest = GC.requestLatency(DGCClient.access$000());
        }
        return localEndpointEntry;
      }
    }

    private EndpointEntry(Endpoint paramEndpoint)
    {
      this.endpoint = paramEndpoint;
      try
      {
        LiveRef localLiveRef = new LiveRef(DGCClient.access$100(), paramEndpoint, false);
        this.dgc = ((DGC)Util.createProxy(DGCImpl.class, new UnicastRef(localLiveRef), true));
      }
      catch (RemoteException localRemoteException)
      {
        throw new Error("internal error creating DGC stub");
      }
      this.renewCleanThread = ((Thread)AccessController.doPrivileged(new NewThreadAction(new RenewCleanThread(this, null), "RenewClean-" + paramEndpoint, true)));
      this.renewCleanThread.start();
    }

    public boolean registerRefs(List paramList)
    {
      label40: label197: long l;
      if ((!($assertionsDisabled)) && (Thread.holdsLock(this)))
        throw new AssertionError();
      HashSet localHashSet = null;
      synchronized (this)
      {
        if (!(this.removed))
          break label40;
        return false;
        Iterator localIterator = paramList.iterator();
        while (localIterator.hasNext())
        {
          LiveRef localLiveRef1 = (LiveRef)localIterator.next();
          if ((!($assertionsDisabled)) && (!(localLiveRef1.getEndpoint().equals(this.endpoint))))
            throw new AssertionError();
          RefEntry localRefEntry = (RefEntry)this.refTable.get(localLiveRef1);
          if (localRefEntry == null)
          {
            LiveRef localLiveRef2 = (LiveRef)localLiveRef1.clone();
            localRefEntry = new RefEntry(this, localLiveRef2);
            this.refTable.put(localLiveRef2, localRefEntry);
            if (localHashSet == null)
              localHashSet = new HashSet(5);
            localHashSet.add(localRefEntry);
          }
          localRefEntry.addInstanceToRefSet(localLiveRef1);
        }
        if (localHashSet != null)
          break label197;
        return true;
        localHashSet.addAll(this.invalidRefs);
        this.invalidRefs.clear();
        l = DGCClient.access$300();
      }
      makeDirtyCall(localHashSet, l);
      return true;
    }

    private void removeRefEntry(RefEntry paramRefEntry)
    {
      if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this))))
        throw new AssertionError();
      if ((!($assertionsDisabled)) && (this.removed))
        throw new AssertionError();
      if ((!($assertionsDisabled)) && (!(this.refTable.containsKey(paramRefEntry.getRef()))))
        throw new AssertionError();
      this.refTable.remove(paramRefEntry.getRef());
      this.invalidRefs.remove(paramRefEntry);
      if (this.refTable.isEmpty())
        synchronized (endpointTable)
        {
          endpointTable.remove(this.endpoint);
          Transport localTransport = this.endpoint.getOutboundTransport();
          localTransport.free(this.endpoint);
          if (endpointTable.isEmpty())
          {
            if ((!($assertionsDisabled)) && (gcLatencyRequest == null))
              throw new AssertionError();
            gcLatencyRequest.cancel();
            gcLatencyRequest = null;
          }
          this.removed = true;
        }
    }

    private void makeDirtyCall(Set paramSet, long paramLong)
    {
      ObjID[] arrayOfObjID;
      long l2;
      long l4;
      if ((!($assertionsDisabled)) && (Thread.holdsLock(this)))
        throw new AssertionError();
      if (paramSet != null)
        arrayOfObjID = createObjIDArray(paramSet);
      else
        arrayOfObjID = DGCClient.access$400();
      long l1 = System.currentTimeMillis();
      try
      {
        Lease localLease = this.dgc.dirty(arrayOfObjID, paramLong, new Lease(DGCClient.access$500(), DGCClient.access$600()));
        l2 = localLease.getValue();
        long l3 = DGCClient.access$700(l1, l2);
        l4 = l1 + l2;
        synchronized (this)
        {
          this.dirtyFailures = 0;
          setRenewTime(l3);
          this.expirationTime = l4;
        }
      }
      catch (Exception localException)
      {
        l2 = System.currentTimeMillis();
        synchronized (this)
        {
          this.dirtyFailures += 1;
          if (this.dirtyFailures == 1)
          {
            this.dirtyFailureStartTime = l1;
            this.dirtyFailureDuration = (l2 - l1);
            setRenewTime(l2);
          }
          else
          {
            int i = this.dirtyFailures - 2;
            if (i == 0)
              this.dirtyFailureDuration = Math.max(this.dirtyFailureDuration + l2 - l1 >> 1, 1000L);
            l4 = l2 + (this.dirtyFailureDuration << i);
            if ((l4 < this.expirationTime) || (this.dirtyFailures < 5) || (l4 < this.dirtyFailureStartTime + DGCClient.access$600()))
              setRenewTime(l4);
            else
              setRenewTime(9223372036854775807L);
          }
          if (paramSet != null)
          {
            this.invalidRefs.addAll(paramSet);
            Iterator localIterator = paramSet.iterator();
            while (localIterator.hasNext())
            {
              RefEntry localRefEntry = (RefEntry)localIterator.next();
              localRefEntry.markDirtyFailed();
            }
          }
          if (this.renewTime >= this.expirationTime)
            this.invalidRefs.addAll(this.refTable.values());
        }
      }
    }

    private void setRenewTime(long paramLong)
    {
      if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this))))
        throw new AssertionError();
      if (paramLong < this.renewTime)
      {
        this.renewTime = paramLong;
        if (this.interruptible)
          AccessController.doPrivileged(new PrivilegedAction(this)
          {
            public Object run()
            {
              DGCClient.EndpointEntry.access$800(this.this$0).interrupt();
              return null;
            }
          });
      }
      else
      {
        this.renewTime = paramLong;
      }
    }

    private void processPhantomRefs(RefEntry.PhantomLiveRef paramPhantomLiveRef)
    {
      if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this))))
        throw new AssertionError();
      HashSet localHashSet1 = null;
      HashSet localHashSet2 = null;
      do
      {
        RefEntry localRefEntry = paramPhantomLiveRef.getRefEntry();
        localRefEntry.removeInstanceFromRefSet(paramPhantomLiveRef);
        if (localRefEntry.isRefSetEmpty())
        {
          if (localRefEntry.hasDirtyFailed())
          {
            if (localHashSet1 == null)
              localHashSet1 = new HashSet(5);
            localHashSet1.add(localRefEntry);
          }
          else
          {
            if (localHashSet2 == null)
              localHashSet2 = new HashSet(5);
            localHashSet2.add(localRefEntry);
          }
          removeRefEntry(localRefEntry);
        }
      }
      while ((paramPhantomLiveRef = (RefEntry.PhantomLiveRef)this.refQueue.poll()) != null);
      if (localHashSet1 != null)
        this.pendingCleans.add(new CleanRequest(createObjIDArray(localHashSet1), DGCClient.access$300(), true));
      if (localHashSet2 != null)
        this.pendingCleans.add(new CleanRequest(createObjIDArray(localHashSet2), DGCClient.access$300(), false));
    }

    private void makeCleanCalls()
    {
      if ((!($assertionsDisabled)) && (Thread.holdsLock(this)))
        throw new AssertionError();
      Iterator localIterator = this.pendingCleans.iterator();
      while (localIterator.hasNext())
      {
        CleanRequest localCleanRequest = (CleanRequest)localIterator.next();
        try
        {
          this.dgc.clean(localCleanRequest.objIDs, localCleanRequest.sequenceNum, DGCClient.access$500(), localCleanRequest.strong);
          localIterator.remove();
        }
        catch (Exception localException)
        {
          if (++localCleanRequest.failures >= 5)
            localIterator.remove();
        }
      }
    }

    private static ObjID[] createObjIDArray(Set paramSet)
    {
      ObjID[] arrayOfObjID = new ObjID[paramSet.size()];
      Iterator localIterator = paramSet.iterator();
      for (int i = 0; i < arrayOfObjID.length; ++i)
        arrayOfObjID[i] = ((RefEntry)localIterator.next()).getRef().getObjID();
      return arrayOfObjID;
    }

    static
    {
      endpointTable = new HashMap(5);
      gcLatencyRequest = null;
    }

    private static class CleanRequest
    {
      final ObjID[] objIDs;
      final long sequenceNum;
      final boolean strong;
      int failures = 0;

      CleanRequest(ObjID[] paramArrayOfObjID, long paramLong, boolean paramBoolean)
      {
        this.objIDs = paramArrayOfObjID;
        this.sequenceNum = paramLong;
        this.strong = paramBoolean;
      }
    }

    private class RefEntry
    {
      private LiveRef ref;
      private Set refSet = new HashSet(5);
      private boolean dirtyFailed = false;

      public RefEntry(, LiveRef paramLiveRef)
      {
        this.ref = paramLiveRef;
      }

      public LiveRef getRef()
      {
        return this.ref;
      }

      public void addInstanceToRefSet()
      {
        if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this.this$0))))
          throw new AssertionError();
        if ((!($assertionsDisabled)) && (!(paramLiveRef.equals(this.ref))))
          throw new AssertionError();
        this.refSet.add(new PhantomLiveRef(this, paramLiveRef));
      }

      public void removeInstanceFromRefSet()
      {
        if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this.this$0))))
          throw new AssertionError();
        if ((!($assertionsDisabled)) && (!(this.refSet.contains(paramPhantomLiveRef))))
          throw new AssertionError();
        this.refSet.remove(paramPhantomLiveRef);
      }

      public boolean isRefSetEmpty()
      {
        if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this.this$0))))
          throw new AssertionError();
        return (this.refSet.size() == 0);
      }

      public void markDirtyFailed()
      {
        if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this.this$0))))
          throw new AssertionError();
        this.dirtyFailed = true;
      }

      public boolean hasDirtyFailed()
      {
        if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this.this$0))))
          throw new AssertionError();
        return this.dirtyFailed;
      }

      private class PhantomLiveRef extends PhantomReference
      {
        public PhantomLiveRef(, LiveRef paramLiveRef)
        {
          super(paramLiveRef, DGCClient.EndpointEntry.access$1300(paramRefEntry.this$0));
        }

        public DGCClient.EndpointEntry.RefEntry getRefEntry()
        {
          return this.this$1;
        }
      }
    }

    private class PhantomLiveRef extends PhantomReference
    {
      public PhantomLiveRef(, LiveRef paramLiveRef)
      {
        super(paramLiveRef, DGCClient.EndpointEntry.access$1300(paramRefEntry.this$0));
      }

      public DGCClient.EndpointEntry.RefEntry getRefEntry()
      {
        return this.this$1;
      }
    }

    private class RenewCleanThread
  implements Runnable
    {
      public void run()
      {
        do
        {
          long l1;
          long l3;
          DGCClient.EndpointEntry.RefEntry.PhantomLiveRef localPhantomLiveRef = null;
          int i = 0;
          Set localSet = null;
          long l2 = -9223372036854775808L;
          synchronized (this.this$0)
          {
            l3 = DGCClient.EndpointEntry.access$900(this.this$0) - System.currentTimeMillis();
            l1 = Math.max(l3, 3412041054961008641L);
            if (!(DGCClient.EndpointEntry.access$1000(this.this$0).isEmpty()))
              l1 = Math.min(l1, DGCClient.access$1100());
            DGCClient.EndpointEntry.access$1202(this.this$0, true);
          }
          try
          {
            localPhantomLiveRef = (DGCClient.EndpointEntry.RefEntry.PhantomLiveRef)DGCClient.EndpointEntry.access$1300(this.this$0).remove(l1);
          }
          catch (InterruptedException localInterruptedException)
          {
          }
          synchronized (this.this$0)
          {
            DGCClient.EndpointEntry.access$1202(this.this$0, false);
            Thread.interrupted();
            if (localPhantomLiveRef != null)
              DGCClient.EndpointEntry.access$1400(this.this$0, localPhantomLiveRef);
            l3 = System.currentTimeMillis();
            if (l3 > DGCClient.EndpointEntry.access$900(this.this$0))
            {
              i = 1;
              if (!(DGCClient.EndpointEntry.access$1500(this.this$0).isEmpty()))
              {
                localSet = DGCClient.EndpointEntry.access$1500(this.this$0);
                DGCClient.EndpointEntry.access$1502(this.this$0, new HashSet(5));
              }
              l2 = DGCClient.access$300();
            }
          }
          if (i != 0)
            DGCClient.EndpointEntry.access$1600(this.this$0, localSet, l2);
          if (!(DGCClient.EndpointEntry.access$1000(this.this$0).isEmpty()))
            DGCClient.EndpointEntry.access$1700(this.this$0);
        }
        while ((!(DGCClient.EndpointEntry.access$1800(this.this$0))) || (!(DGCClient.EndpointEntry.access$1000(this.this$0).isEmpty())));
      }
    }
  }
}