package sun.net.www.http;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.net.ProgressSource;
import sun.net.www.MeteredStream;

public class KeepAliveStream extends MeteredStream
  implements Hurryable
{
  HttpClient hc;
  boolean hurried;
  protected boolean queuedForCleanup = false;
  private static KeepAliveStreamCleaner queue = new KeepAliveStreamCleaner();
  private static Thread cleanerThread = null;
  private static boolean startCleanupThread;

  public KeepAliveStream(InputStream paramInputStream, ProgressSource paramProgressSource, int paramInt, HttpClient paramHttpClient)
  {
    super(paramInputStream, paramProgressSource, paramInt);
    this.hc = paramHttpClient;
  }

  public void close()
    throws IOException
  {
    if (this.closed)
      return;
    if (this.queuedForCleanup)
      return;
    try
    {
      if (this.expected > this.count)
      {
        long l1 = this.expected - this.count;
        if (l1 <= available())
        {
          long l2 = 3412040075708465152L;
          while (l2 < l1)
          {
            l1 -= l2;
            l2 = skip(l1);
          }
        }
        else if ((this.expected <= KeepAliveStreamCleaner.MAX_DATA_REMAINING) && (!(this.hurried)))
        {
          queueForCleanup(new KeepAliveCleanerEntry(this, this.hc));
        }
        else
        {
          this.hc.closeServer();
        }
      }
      if ((!(this.closed)) && (!(this.hurried)) && (!(this.queuedForCleanup)))
        this.hc.finished();
    }
    finally
    {
      if (this.pi != null)
        this.pi.finishTracking();
      if (!(this.queuedForCleanup))
      {
        this.in = null;
        this.hc = null;
        this.closed = true;
      }
    }
  }

  public boolean markSupported()
  {
    return false;
  }

  public void mark(int paramInt)
  {
  }

  public void reset()
    throws IOException
  {
    throw new IOException("mark/reset not supported");
  }

  public synchronized boolean hurry()
  {
    try
    {
      if ((this.closed) || (this.count >= this.expected))
        return false;
      if (this.in.available() < this.expected - this.count)
        return false;
      byte[] arrayOfByte = new byte[this.expected - this.count];
      DataInputStream localDataInputStream = new DataInputStream(this.in);
      localDataInputStream.readFully(arrayOfByte);
      this.in = new ByteArrayInputStream(arrayOfByte);
      this.hurried = true;
      return true;
    }
    catch (IOException localIOException)
    {
    }
    return false;
  }

  private static synchronized void queueForCleanup(KeepAliveCleanerEntry paramKeepAliveCleanerEntry)
  {
    if ((queue != null) && (!(paramKeepAliveCleanerEntry.getQueuedForCleanup())))
    {
      if (!(queue.offer(paramKeepAliveCleanerEntry)))
      {
        paramKeepAliveCleanerEntry.getHttpClient().closeServer();
        return;
      }
      paramKeepAliveCleanerEntry.setQueuedForCleanup();
    }
    startCleanupThread = cleanerThread == null;
    if ((!(startCleanupThread)) && (!(cleanerThread.isAlive())))
      startCleanupThread = true;
    if (startCleanupThread)
      AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          Object localObject = Thread.currentThread().getThreadGroup();
          ThreadGroup localThreadGroup = null;
          while ((localThreadGroup = ((ThreadGroup)localObject).getParent()) != null)
            localObject = localThreadGroup;
          KeepAliveStream.access$002(new Thread((ThreadGroup)localObject, KeepAliveStream.access$100(), "Keep-Alive-SocketCleaner"));
          KeepAliveStream.access$000().setDaemon(true);
          KeepAliveStream.access$000().setPriority(8);
          KeepAliveStream.access$000().start();
          return null;
        }
      });
  }

  protected int remainingToRead()
  {
    return (this.expected - this.count);
  }

  protected void setClosed()
  {
    this.in = null;
    this.hc = null;
    this.closed = true;
  }
}