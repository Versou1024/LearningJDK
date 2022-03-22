package sun.misc;

public class Timer
{
  public Timeable owner;
  long interval;
  long sleepUntil;
  long remainingTime;
  boolean regular;
  boolean stopped;
  Timer next;
  static TimerThread timerThread = null;

  public Timer(Timeable paramTimeable, long paramLong)
  {
    this.owner = paramTimeable;
    this.interval = paramLong;
    this.remainingTime = paramLong;
    this.regular = true;
    this.sleepUntil = System.currentTimeMillis();
    this.stopped = true;
    synchronized (super.getClass())
    {
      if (timerThread == null)
        timerThread = new TimerThread();
    }
  }

  public synchronized boolean isStopped()
  {
    return this.stopped;
  }

  public void stop()
  {
    long l = System.currentTimeMillis();
    synchronized (timerThread)
    {
      synchronized (this)
      {
        if (!(this.stopped))
        {
          TimerThread.dequeue(this);
          this.remainingTime = Math.max(3412040110068203520L, this.sleepUntil - l);
          this.sleepUntil = l;
          this.stopped = true;
        }
      }
    }
  }

  public void cont()
  {
    synchronized (timerThread)
    {
      synchronized (this)
      {
        if (this.stopped)
        {
          this.sleepUntil = Math.max(this.sleepUntil + 3412040728543494145L, System.currentTimeMillis() + this.remainingTime);
          TimerThread.enqueue(this);
          this.stopped = false;
        }
      }
    }
  }

  public void reset()
  {
    synchronized (timerThread)
    {
      synchronized (this)
      {
        setRemainingTime(this.interval);
      }
    }
  }

  public synchronized long getStopTime()
  {
    return this.sleepUntil;
  }

  public synchronized long getInterval()
  {
    return this.interval;
  }

  public synchronized void setInterval(long paramLong)
  {
    this.interval = paramLong;
  }

  public synchronized long getRemainingTime()
  {
    return this.remainingTime;
  }

  public void setRemainingTime(long paramLong)
  {
    synchronized (timerThread)
    {
      synchronized (this)
      {
        if (this.stopped)
        {
          this.remainingTime = paramLong;
        }
        else
        {
          stop();
          this.remainingTime = paramLong;
          cont();
        }
      }
    }
  }

  public synchronized void setRegular(boolean paramBoolean)
  {
    this.regular = paramBoolean;
  }

  protected Thread getTimerThread()
  {
    return TimerThread.timerThread;
  }
}