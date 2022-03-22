package sun.awt.windows;

abstract class WObjectPeer
{
  long pData;
  boolean destroyed = false;
  Object target;
  private volatile boolean disposed;
  protected Error createError = null;

  public static WObjectPeer getPeerForTarget(Object paramObject)
  {
    WObjectPeer localWObjectPeer = (WObjectPeer)WToolkit.targetToPeer(paramObject);
    return localWObjectPeer;
  }

  public long getData()
  {
    return this.pData;
  }

  public Object getTarget()
  {
    return this.target;
  }

  protected abstract void disposeImpl();

  public final void dispose()
  {
    int i = 0;
    synchronized (this)
    {
      if (!(this.disposed))
        this.disposed = (i = 1);
    }
    if (i != 0)
      disposeImpl();
  }

  protected final boolean isDisposed()
  {
    return this.disposed;
  }

  private static native void initIDs();

  static
  {
    initIDs();
  }
}