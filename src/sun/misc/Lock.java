package sun.misc;

public class Lock
{
  private boolean locked = false;

  public final synchronized void lock()
    throws InterruptedException
  {
    while (this.locked)
      super.wait();
    this.locked = true;
  }

  public final synchronized void unlock()
  {
    this.locked = false;
    super.notifyAll();
  }
}