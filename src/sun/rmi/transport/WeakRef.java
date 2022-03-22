package sun.rmi.transport;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import sun.rmi.runtime.Log;

class WeakRef extends WeakReference
{
  private int hashValue;
  private Object strongRef = null;

  public WeakRef(Object paramObject)
  {
    super(paramObject);
    setHashValue(paramObject);
  }

  public WeakRef(Object paramObject, ReferenceQueue paramReferenceQueue)
  {
    super(paramObject, paramReferenceQueue);
    setHashValue(paramObject);
  }

  public synchronized void pin()
  {
    if (this.strongRef == null)
    {
      this.strongRef = get();
      if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE))
        DGCImpl.dgcLog.log(Log.VERBOSE, "strongRef = " + this.strongRef);
    }
  }

  public synchronized void unpin()
  {
    if (this.strongRef != null)
    {
      if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE))
        DGCImpl.dgcLog.log(Log.VERBOSE, "strongRef = " + this.strongRef);
      this.strongRef = null;
    }
  }

  private void setHashValue(Object paramObject)
  {
    if (paramObject != null)
      this.hashValue = System.identityHashCode(paramObject);
    else
      this.hashValue = 0;
  }

  public int hashCode()
  {
    return this.hashValue;
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof WeakRef)
    {
      if (paramObject == this)
        return true;
      Object localObject = get();
      return ((localObject != null) && (localObject == ((WeakRef)paramObject).get()));
    }
    return false;
  }
}