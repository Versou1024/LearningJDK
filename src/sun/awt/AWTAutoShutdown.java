package sun.awt;

import java.awt.AWTEvent;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;

public final class AWTAutoShutdown
  implements Runnable
{
  private static final AWTAutoShutdown theInstance = new AWTAutoShutdown();
  private final Object mainLock = new Object();
  private final Object activationLock = new Object();
  private final HashSet busyThreadSet = new HashSet(7);
  private boolean toolkitThreadBusy = false;
  private final Hashtable peerMap = new PeerMap();
  private Thread blockerThread = null;
  private boolean timeoutPassed = false;
  private static final int SAFETY_TIMEOUT = 1000;

  public static AWTAutoShutdown getInstance()
  {
    return theInstance;
  }

  public static void notifyToolkitThreadBusy()
  {
    getInstance().setToolkitBusy(true);
  }

  public static void notifyToolkitThreadFree()
  {
    getInstance().setToolkitBusy(false);
  }

  public void notifyThreadBusy(Thread paramThread)
  {
    synchronized (this.activationLock)
    {
      synchronized (this.mainLock)
      {
        if (this.blockerThread == null)
        {
          activateBlockerThread();
        }
        else if (isReadyToShutdown())
        {
          this.mainLock.notifyAll();
          this.timeoutPassed = false;
        }
        this.busyThreadSet.add(paramThread);
      }
    }
  }

  public void notifyThreadFree(Thread paramThread)
  {
    synchronized (this.activationLock)
    {
      synchronized (this.mainLock)
      {
        this.busyThreadSet.remove(paramThread);
        if (isReadyToShutdown())
        {
          this.mainLock.notifyAll();
          this.timeoutPassed = false;
        }
      }
    }
  }

  void notifyPeerMapUpdated()
  {
    synchronized (this.activationLock)
    {
      synchronized (this.mainLock)
      {
        if ((!(isReadyToShutdown())) && (this.blockerThread == null))
        {
          activateBlockerThread();
        }
        else
        {
          this.mainLock.notifyAll();
          this.timeoutPassed = false;
        }
      }
    }
  }

  private boolean isReadyToShutdown()
  {
    return ((!(this.toolkitThreadBusy)) && (this.peerMap.isEmpty()) && (this.busyThreadSet.isEmpty()));
  }

  private void setToolkitBusy(boolean paramBoolean)
  {
    if (paramBoolean != this.toolkitThreadBusy)
      synchronized (this.activationLock)
      {
        synchronized (this.mainLock)
        {
          if (paramBoolean != this.toolkitThreadBusy)
            if (paramBoolean)
            {
              if (this.blockerThread == null)
              {
                activateBlockerThread();
              }
              else if (isReadyToShutdown())
              {
                this.mainLock.notifyAll();
                this.timeoutPassed = false;
              }
              this.toolkitThreadBusy = paramBoolean;
            }
            else
            {
              this.toolkitThreadBusy = paramBoolean;
              if (isReadyToShutdown())
              {
                this.mainLock.notifyAll();
                this.timeoutPassed = false;
              }
            }
        }
      }
  }

  public void run()
  {
    Thread localThread = Thread.currentThread();
    int i = 0;
    synchronized (this.mainLock)
    {
      try
      {
        this.mainLock.notifyAll();
        if (this.blockerThread == localThread)
        {
          this.mainLock.wait();
          this.timeoutPassed = false;
          while (true)
          {
            while (true)
            {
              if (!(isReadyToShutdown()));
              if (!(this.timeoutPassed))
                break;
              this.timeoutPassed = false;
              this.blockerThread = null;
            }
            this.timeoutPassed = true;
            this.mainLock.wait(1000L);
          }
        }
      }
      catch (InterruptedException localInterruptedException)
      {
        i = 1;
      }
      finally
      {
        if (this.blockerThread == localThread)
          this.blockerThread = null;
      }
    }
    if (i == 0)
      AppContext.stopEventDispatchThreads();
  }

  static AWTEvent getShutdownEvent()
  {
    return new AWTEvent(getInstance(), 0)
    {
    };
  }

  private void activateBlockerThread()
  {
    Thread localThread = new Thread(this, "AWT-Shutdown");
    localThread.setDaemon(false);
    this.blockerThread = localThread;
    localThread.start();
    try
    {
      this.mainLock.wait();
    }
    catch (InterruptedException localInterruptedException)
    {
      System.err.println("AWT blocker activation interrupted:");
      localInterruptedException.printStackTrace();
    }
  }

  public Hashtable getPeerMap()
  {
    return this.peerMap;
  }

  static final class PeerMap extends Hashtable
  {
    public Object put(Object paramObject1, Object paramObject2)
    {
      Object localObject = super.put(paramObject1, paramObject2);
      AWTAutoShutdown.getInstance().notifyPeerMapUpdated();
      return localObject;
    }

    public Object remove(Object paramObject)
    {
      Object localObject = super.remove(paramObject);
      AWTAutoShutdown.getInstance().notifyPeerMapUpdated();
      return localObject;
    }
  }
}