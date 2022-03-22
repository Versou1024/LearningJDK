package sun.net.www.http;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import sun.net.NetProperties;

public class KeepAliveStreamCleaner extends LinkedBlockingQueue<KeepAliveCleanerEntry>
  implements Runnable
{
  protected static int MAX_DATA_REMAINING = 512;
  protected static int MAX_CAPACITY = 10;
  protected static final int TIMEOUT = 5000;
  private static final int MAX_RETRIES = 5;

  public KeepAliveStreamCleaner()
  {
    super(MAX_CAPACITY);
  }

  public KeepAliveStreamCleaner(int paramInt)
  {
    super(paramInt);
  }

  public void run()
  {
    KeepAliveCleanerEntry localKeepAliveCleanerEntry = null;
    do
      try
      {
        localKeepAliveCleanerEntry = (KeepAliveCleanerEntry)poll(5000L, TimeUnit.MILLISECONDS);
        if (localKeepAliveCleanerEntry == null)
          return;
        KeepAliveStream localKeepAliveStream1 = localKeepAliveCleanerEntry.getKeepAliveStream();
        if (localKeepAliveStream1 != null)
          synchronized (localKeepAliveStream1)
          {
            HttpClient localHttpClient = localKeepAliveCleanerEntry.getHttpClient();
            try
            {
              if ((localHttpClient != null) && (!(localHttpClient.isInKeepAliveCache())))
              {
                int i = localHttpClient.setTimeout(5000);
                long l1 = localKeepAliveStream1.remainingToRead();
                if (l1 > 3412040625464279040L)
                {
                  long l2 = 3412041467277869056L;
                  int j = 0;
                  while (true)
                  {
                    do
                    {
                      if ((l2 >= l1) || (j >= 5))
                        break label127;
                      l1 -= l2;
                      l2 = localKeepAliveStream1.skip(l1);
                    }
                    while (l2 != 3412041398558392320L);
                    ++j;
                  }
                  label127: l1 -= l2;
                }
                if (l1 == 3412040642644148224L)
                {
                  localHttpClient.setTimeout(i);
                  localHttpClient.finished();
                }
                else
                {
                  localHttpClient.closeServer();
                }
              }
            }
            catch (IOException localIOException)
            {
              localHttpClient.closeServer();
            }
            finally
            {
              localKeepAliveStream1.setClosed();
            }
          }
      }
      catch (InterruptedException localInterruptedException)
      {
      }
    while (localKeepAliveCleanerEntry != null);
  }

  static
  {
    int i = ((Integer)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new Integer(NetProperties.getInteger("http.KeepAlive.remainingData", KeepAliveStreamCleaner.MAX_DATA_REMAINING).intValue());
      }
    })).intValue() * 1024;
    MAX_DATA_REMAINING = i;
    int j = ((Integer)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new Integer(NetProperties.getInteger("http.KeepAlive.queuedConnections", KeepAliveStreamCleaner.MAX_CAPACITY).intValue());
      }
    })).intValue();
    MAX_CAPACITY = j;
  }
}