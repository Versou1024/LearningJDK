package sun.awt.datatransfer;

public abstract interface ToolkitThreadBlockedHandler
{
  public abstract void lock();

  public abstract void unlock();

  public abstract void enter();

  public abstract void exit();
}