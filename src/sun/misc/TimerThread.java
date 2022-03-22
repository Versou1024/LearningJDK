package sun.misc;

import java.io.PrintStream;

class TimerThread extends Thread
{
  public static boolean debug = false;
  static TimerThread timerThread;
  static boolean notified = false;
  static Timer timerQueue = null;

  protected TimerThread()
  {
    super("TimerThread");
    timerThread = this;
    start();
  }

  public synchronized void run()
  {
    label0: if (timerQueue == null);
    try
    {
      wait();
    }
    catch (InterruptedException localTimer)
    {
      break label0:
      notified = false;
      long l1 = timerQueue.sleepUntil - System.currentTimeMillis();
      if (l1 > 3412047239713914880L)
        try
        {
          wait(l1);
        }
        catch (InterruptedException localInterruptedException2)
        {
        }
      if (!(notified))
      {
        Timer localTimer = timerQueue;
        timerQueue = timerQueue.next;
        TimerTickThread localTimerTickThread = TimerTickThread.call(localTimer, localTimer.sleepUntil);
        if (debug)
        {
          long l2 = System.currentTimeMillis() - localTimer.sleepUntil;
          System.out.println("tick(" + localTimerTickThread.getName() + "," + localTimer.interval + "," + l2 + ")");
          if (l2 > 250L)
            System.out.println("*** BIG DELAY ***");
        }
      }
    }
  }

  protected static void enqueue(Timer paramTimer)
  {
    Object localObject = null;
    Timer localTimer = timerQueue;
    if ((localTimer == null) || (paramTimer.sleepUntil <= localTimer.sleepUntil))
    {
      paramTimer.next = timerQueue;
      timerQueue = paramTimer;
      notified = true;
      timerThread.notify();
    }
    else
    {
      do
      {
        localObject = localTimer;
        localTimer = localTimer.next;
      }
      while ((localTimer != null) && (paramTimer.sleepUntil > localTimer.sleepUntil));
      paramTimer.next = localTimer;
      localObject.next = paramTimer;
    }
    if (debug)
    {
      long l1 = System.currentTimeMillis();
      System.out.print(Thread.currentThread().getName() + ": enqueue " + paramTimer.interval + ": ");
      for (localTimer = timerQueue; localTimer != null; localTimer = localTimer.next)
      {
        long l2 = localTimer.sleepUntil - l1;
        System.out.print(localTimer.interval + "(" + l2 + ") ");
      }
      System.out.println();
    }
  }

  protected static boolean dequeue(Timer paramTimer)
  {
    Object localObject = null;
    for (Timer localTimer = timerQueue; (localTimer != null) && (localTimer != paramTimer); localTimer = localTimer.next)
      localObject = localTimer;
    if (localTimer == null)
    {
      if (debug)
        System.out.println(Thread.currentThread().getName() + ": dequeue " + paramTimer.interval + ": no-op");
      return false;
    }
    if (localObject == null)
    {
      timerQueue = paramTimer.next;
      notified = true;
      timerThread.notify();
    }
    else
    {
      localObject.next = paramTimer.next;
    }
    paramTimer.next = null;
    if (debug)
    {
      long l1 = System.currentTimeMillis();
      System.out.print(Thread.currentThread().getName() + ": dequeue " + paramTimer.interval + ": ");
      for (localTimer = timerQueue; localTimer != null; localTimer = localTimer.next)
      {
        long l2 = localTimer.sleepUntil - l1;
        System.out.print(localTimer.interval + "(" + l2 + ") ");
      }
      System.out.println();
    }
    return true;
  }

  protected static void requeue(Timer paramTimer)
  {
    if (!(paramTimer.stopped))
    {
      long l = System.currentTimeMillis();
      if (paramTimer.regular)
        paramTimer.sleepUntil += paramTimer.interval;
      else
        paramTimer.sleepUntil = (l + paramTimer.interval);
      enqueue(paramTimer);
    }
    else if (debug)
    {
      System.out.println(Thread.currentThread().getName() + ": requeue " + paramTimer.interval + ": no-op");
    }
  }
}