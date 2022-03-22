package sun.misc;

class TimerTickThread extends Thread
{
  static final int MAX_POOL_SIZE = 3;
  static int curPoolSize = 0;
  static TimerTickThread pool = null;
  TimerTickThread next = null;
  Timer timer;
  long lastSleepUntil;

  protected static synchronized TimerTickThread call(Timer paramTimer, long paramLong)
  {
    TimerTickThread localTimerTickThread1 = pool;
    if (localTimerTickThread1 == null)
    {
      localTimerTickThread1 = new TimerTickThread();
      localTimerTickThread1.timer = paramTimer;
      localTimerTickThread1.lastSleepUntil = paramLong;
      localTimerTickThread1.start();
    }
    else
    {
      pool = pool.next;
      localTimerTickThread1.timer = paramTimer;
      localTimerTickThread1.lastSleepUntil = paramLong;
      synchronized (localTimerTickThread1)
      {
        localTimerTickThread1.notify();
      }
    }
    return localTimerTickThread1;
  }

  private boolean returnToPool()
  {
    synchronized (getClass())
    {
      if (curPoolSize < 3)
        break label18;
      return false;
      label18: this.next = pool;
      pool = this;
      curPoolSize += 1;
      this.timer = null;
    }
    while (this.timer == null)
      synchronized (this)
      {
        try
        {
          wait();
        }
        catch (InterruptedException localInterruptedException)
        {
        }
      }
    synchronized (getClass())
    {
      curPoolSize -= 1;
    }
    return true;
  }

  public void run()
  {
    do
    {
      this.timer.owner.tick(this.timer);
      synchronized (TimerThread.timerThread)
      {
        synchronized (this.timer)
        {
          if (this.lastSleepUntil == this.timer.sleepUntil)
            TimerThread.requeue(this.timer);
        }
      }
    }
    while (returnToPool());
  }
}