package sun.java2d.opengl;

import java.io.PrintStream;
import java.util.Set;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;

public class OGLRenderQueue extends RenderQueue
{
  private static OGLRenderQueue theInstance;
  private final QueueFlusher flusher = new QueueFlusher(this);

  public static synchronized OGLRenderQueue getInstance()
  {
    if (theInstance == null)
      theInstance = new OGLRenderQueue();
    return theInstance;
  }

  public static void sync()
  {
    if (theInstance != null)
    {
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

  public static void disposeGraphicsConfig(long paramLong)
  {
    OGLRenderQueue localOGLRenderQueue = getInstance();
    localOGLRenderQueue.lock();
    try
    {
      OGLContext.setScratchSurface(paramLong);
      RenderBuffer localRenderBuffer = localOGLRenderQueue.getBuffer();
      localOGLRenderQueue.ensureCapacityAndAlignment(12, 4);
      localRenderBuffer.putInt(74);
      localRenderBuffer.putLong(paramLong);
      localOGLRenderQueue.flushNow();
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
  }

  public static boolean isQueueFlusherThread()
  {
    return (Thread.currentThread() == getInstance().flusher);
  }

  public void flushNow()
  {
    try
    {
      this.flusher.flushNow();
    }
    catch (Exception localException)
    {
      System.err.println("exception in flushNow:");
      localException.printStackTrace();
    }
  }

  public void flushAndInvokeNow(Runnable paramRunnable)
  {
    try
    {
      this.flusher.flushAndInvokeNow(paramRunnable);
    }
    catch (Exception localException)
    {
      System.err.println("exception in flushAndInvokeNow:");
      localException.printStackTrace();
    }
  }

  private native void flushBuffer(long paramLong, int paramInt);

  private void flushBuffer()
  {
    int i = this.buf.position();
    if (i > 0)
      flushBuffer(this.buf.getAddress(), i);
    this.buf.clear();
    this.refSet.clear();
  }

  private class QueueFlusher extends Thread
  {
    private boolean needsFlush;
    private Runnable task;
    private Error error;

    public QueueFlusher()
    {
      super("Java2D Queue Flusher");
      setDaemon(true);
      setPriority(10);
      start();
    }

    public synchronized void flushNow()
    {
      this.needsFlush = true;
      notify();
      label9: if (this.needsFlush);
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException)
      {
        break label9:
        if (this.error != null)
          throw this.error;
      }
    }

    public synchronized void flushAndInvokeNow()
    {
      this.task = paramRunnable;
      flushNow();
    }

    public synchronized void run()
    {
      boolean bool = false;
      label2: if (!(this.needsFlush));
      try
      {
        bool = false;
        wait(100L);
        if (!(this.needsFlush))
          if ((bool = this.this$0.tryLock()))
            if (OGLRenderQueue.access$000(this.this$0).position() > 0)
              this.needsFlush = true;
            else
              this.this$0.unlock();
      }
      catch (InterruptedException localException)
      {
        break label2:
        try
        {
          this.error = null;
          OGLRenderQueue.access$100(this.this$0);
          if (this.task != null)
            this.task.run();
        }
        catch (Error localError)
        {
          this.error = localError;
        }
        catch (Exception localException)
        {
          System.err.println("exception in QueueFlusher:");
          localException.printStackTrace();
        }
        finally
        {
          if (bool)
            this.this$0.unlock();
          this.task = null;
          this.needsFlush = false;
          notify();
        }
      }
    }
  }
}