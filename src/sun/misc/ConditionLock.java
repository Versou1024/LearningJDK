package sun.misc;

public final class ConditionLock extends Lock
{
  private int state = 0;

  public ConditionLock()
  {
  }

  public ConditionLock(int paramInt)
  {
    this.state = paramInt;
  }

  public synchronized void lockWhen(int paramInt)
    throws InterruptedException
  {
    while (this.state != paramInt)
      wait();
    lock();
  }

  public synchronized void unlockWith(int paramInt)
  {
    this.state = paramInt;
    unlock();
  }
}