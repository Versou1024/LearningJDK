package sun.rmi.transport;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.dgc.VMID;
import java.rmi.server.ObjID;
import java.rmi.server.Unreferenced;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import sun.rmi.runtime.Log;
import sun.rmi.runtime.NewThreadAction;
import sun.rmi.server.Dispatcher;

public final class Target
{
  private final ObjID id;
  private final boolean permanent;
  private final WeakRef weakImpl;
  private volatile Dispatcher disp;
  private final Remote stub;
  private final Vector refSet = new Vector();
  private final Hashtable sequenceTable = new Hashtable(5);
  private final AccessControlContext acc;
  private final ClassLoader ccl;
  private int callCount = 0;
  private boolean removed = false;
  private volatile Transport exportedTransport = null;
  private static int nextThreadNum = 0;

  public Target(Remote paramRemote1, Dispatcher paramDispatcher, Remote paramRemote2, ObjID paramObjID, boolean paramBoolean)
  {
    this.weakImpl = new WeakRef(paramRemote1, ObjectTable.reapQueue);
    this.disp = paramDispatcher;
    this.stub = paramRemote2;
    this.id = paramObjID;
    this.acc = AccessController.getContext();
    ClassLoader localClassLoader1 = Thread.currentThread().getContextClassLoader();
    ClassLoader localClassLoader2 = paramRemote1.getClass().getClassLoader();
    if (checkLoaderAncestry(localClassLoader1, localClassLoader2))
      this.ccl = localClassLoader1;
    else
      this.ccl = localClassLoader2;
    this.permanent = paramBoolean;
    if (paramBoolean)
      pinImpl();
  }

  private static boolean checkLoaderAncestry(ClassLoader paramClassLoader1, ClassLoader paramClassLoader2)
  {
    if (paramClassLoader2 == null)
      return true;
    if (paramClassLoader1 == null)
      return false;
    for (ClassLoader localClassLoader = paramClassLoader1; localClassLoader != null; localClassLoader = localClassLoader.getParent())
      if (localClassLoader == paramClassLoader2)
        return true;
    return false;
  }

  public Remote getStub()
  {
    return this.stub;
  }

  ObjectEndpoint getObjectEndpoint()
  {
    return new ObjectEndpoint(this.id, this.exportedTransport);
  }

  WeakRef getWeakImpl()
  {
    return this.weakImpl;
  }

  Dispatcher getDispatcher()
  {
    return this.disp;
  }

  AccessControlContext getAccessControlContext()
  {
    return this.acc;
  }

  ClassLoader getContextClassLoader()
  {
    return this.ccl;
  }

  Remote getImpl()
  {
    return ((Remote)this.weakImpl.get());
  }

  boolean isPermanent()
  {
    return this.permanent;
  }

  synchronized void pinImpl()
  {
    this.weakImpl.pin();
  }

  synchronized void unpinImpl()
  {
    if ((!(this.permanent)) && (this.refSet.isEmpty()))
      this.weakImpl.unpin();
  }

  void setExportedTransport(Transport paramTransport)
  {
    if (this.exportedTransport == null)
      this.exportedTransport = paramTransport;
  }

  synchronized void referenced(long paramLong, VMID paramVMID)
  {
    SequenceEntry localSequenceEntry = (SequenceEntry)this.sequenceTable.get(paramVMID);
    if (localSequenceEntry == null)
      this.sequenceTable.put(paramVMID, new SequenceEntry(paramLong));
    else if (localSequenceEntry.sequenceNum < paramLong)
      localSequenceEntry.update(paramLong);
    else
      return;
    if (!(this.refSet.contains(paramVMID)))
    {
      pinImpl();
      if (getImpl() == null)
        return;
      if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE))
        DGCImpl.dgcLog.log(Log.VERBOSE, "add to dirty set: " + paramVMID);
      this.refSet.addElement(paramVMID);
      DGCImpl.getDGCImpl().registerTarget(paramVMID, this);
    }
  }

  synchronized void unreferenced(long paramLong, VMID paramVMID, boolean paramBoolean)
  {
    SequenceEntry localSequenceEntry = (SequenceEntry)this.sequenceTable.get(paramVMID);
    if ((localSequenceEntry == null) || (localSequenceEntry.sequenceNum > paramLong))
      return;
    if (paramBoolean)
      localSequenceEntry.retain(paramLong);
    else if (!(localSequenceEntry.keep))
      this.sequenceTable.remove(paramVMID);
    if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE))
      DGCImpl.dgcLog.log(Log.VERBOSE, "remove from dirty set: " + paramVMID);
    refSetRemove(paramVMID);
  }

  private synchronized void refSetRemove(VMID paramVMID)
  {
    DGCImpl.getDGCImpl().unregisterTarget(paramVMID, this);
    if ((this.refSet.removeElement(paramVMID)) && (this.refSet.isEmpty()))
    {
      if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE))
        DGCImpl.dgcLog.log(Log.VERBOSE, "reference set is empty: target = " + this);
      Remote localRemote = getImpl();
      if (localRemote instanceof Unreferenced)
      {
        Unreferenced localUnreferenced = (Unreferenced)localRemote;
        Thread localThread = (Thread)AccessController.doPrivileged(new NewThreadAction(new Runnable(this, localUnreferenced)
        {
          public void run()
          {
            this.val$unrefObj.unreferenced();
          }
        }
        , "Unreferenced-" + (nextThreadNum++), false, true));
        AccessController.doPrivileged(new PrivilegedAction(this, localThread)
        {
          public Object run()
          {
            this.val$t.setContextClassLoader(Target.access$000(this.this$0));
            return null;
          }
        });
        localThread.start();
      }
      unpinImpl();
    }
  }

  synchronized boolean unexport(boolean paramBoolean)
  {
    if ((paramBoolean == true) || (this.callCount == 0) || (this.disp == null))
    {
      this.disp = null;
      unpinImpl();
      DGCImpl localDGCImpl = DGCImpl.getDGCImpl();
      Enumeration localEnumeration = this.refSet.elements();
      while (localEnumeration.hasMoreElements())
      {
        VMID localVMID = (VMID)localEnumeration.nextElement();
        localDGCImpl.unregisterTarget(localVMID, this);
      }
      return true;
    }
    return false;
  }

  synchronized void markRemoved()
  {
    if (this.removed)
      throw new AssertionError();
    this.removed = true;
    if ((!(this.permanent)) && (this.callCount == 0))
      ObjectTable.decrementKeepAliveCount();
    if (this.exportedTransport != null)
      this.exportedTransport.targetUnexported();
  }

  synchronized void incrementCallCount()
    throws NoSuchObjectException
  {
    if (this.disp != null)
      this.callCount += 1;
    else
      throw new NoSuchObjectException("object not accepting new calls");
  }

  synchronized void decrementCallCount()
  {
    if (--this.callCount < 0)
      throw new Error("internal error: call count less than zero");
    if ((!(this.permanent)) && (this.removed) && (this.callCount == 0))
      ObjectTable.decrementKeepAliveCount();
  }

  boolean isEmpty()
  {
    return this.refSet.isEmpty();
  }

  public synchronized void vmidDead(VMID paramVMID)
  {
    if (DGCImpl.dgcLog.isLoggable(Log.BRIEF))
      DGCImpl.dgcLog.log(Log.BRIEF, "removing endpoint " + paramVMID + " from reference set");
    this.sequenceTable.remove(paramVMID);
    refSetRemove(paramVMID);
  }
}