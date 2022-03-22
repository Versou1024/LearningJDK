package sun.java2d.d3d;

import java.util.Set;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;

public class D3DRenderQueue extends RenderQueue
{
  private static D3DRenderQueue theInstance;
  private static Thread rqThread;

  public static synchronized D3DRenderQueue getInstance()
  {
    if (theInstance == null)
    {
      theInstance = new D3DRenderQueue();
      theInstance.flushAndInvokeNow(new Runnable()
      {
        public void run()
        {
          D3DRenderQueue.access$002(Thread.currentThread());
        }
      });
    }
    return theInstance;
  }

  public static void sync()
  {
    if (theInstance != null)
    {
      D3DScreenUpdateManager localD3DScreenUpdateManager = (D3DScreenUpdateManager)ScreenUpdateManager.getInstance();
      localD3DScreenUpdateManager.runUpdateNow();
      theInstance.lock();
      try
      {
        theInstance.ensureCapacity(4);
        theInstance.getBuffer().putInt(76);
        theInstance.flushNow();
      }
      finally
      {
        theInstance.unlock();
      }
    }
  }

  public static void restoreDevices()
  {
    D3DRenderQueue localD3DRenderQueue = getInstance();
    localD3DRenderQueue.lock();
    try
    {
      localD3DRenderQueue.ensureCapacity(4);
      localD3DRenderQueue.getBuffer().putInt(77);
      localD3DRenderQueue.flushNow();
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  public static boolean isRenderQueueThread()
  {
    return (Thread.currentThread() == rqThread);
  }

  public static void disposeGraphicsConfig(long paramLong)
  {
    D3DRenderQueue localD3DRenderQueue = getInstance();
    localD3DRenderQueue.lock();
    try
    {
      RenderBuffer localRenderBuffer = localD3DRenderQueue.getBuffer();
      localD3DRenderQueue.ensureCapacityAndAlignment(12, 4);
      localRenderBuffer.putInt(74);
      localRenderBuffer.putLong(paramLong);
      localD3DRenderQueue.flushNow();
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  public void flushNow()
  {
    flushBuffer(null);
  }

  public void flushAndInvokeNow(Runnable paramRunnable)
  {
    flushBuffer(paramRunnable);
  }

  private native void flushBuffer(long paramLong, int paramInt, Runnable paramRunnable);

  private void flushBuffer(Runnable paramRunnable)
  {
    int i = this.buf.position();
    if ((i > 0) || (paramRunnable != null))
      flushBuffer(this.buf.getAddress(), i, paramRunnable);
    this.buf.clear();
    this.refSet.clear();
  }
}