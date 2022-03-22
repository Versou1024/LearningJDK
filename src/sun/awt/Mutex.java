package sun.awt;

public class Mutex
{
  private boolean locked;
  private Thread owner;

  public synchronized void lock()
  {
    if ((this.locked) && (Thread.currentThread() == this.owner))
      throw new IllegalMonitorStateException();
    do
      if (!(this.locked))
      {
        this.locked = true;
        this.owner = Thread.currentThread();
      }
      else
      {
        try
        {
          super.wait();
        }
        catch (InterruptedException localInterruptedException)
        {
        }
      }
    while (this.owner != Thread.currentThread());
  }

  public synchronized void unlock()
  {
    if (Thread.currentThread() != this.owner)
      throw new IllegalMonitorStateException();
    this.owner = null;
    this.locked = false;
    super.notify();
  }

  protected boolean isOwned()
  {
    return ((this.locked) && (Thread.currentThread() == this.owner));
  }
}