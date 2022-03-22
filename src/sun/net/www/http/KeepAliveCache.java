package sun.net.www.http;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sun.security.action.GetIntegerAction;

public class KeepAliveCache extends Hashtable
  implements Runnable
{
  private static final long serialVersionUID = -2937172892064557949L;
  static final int MAX_CONNECTIONS = 5;
  static int result = -1;
  static final int LIFETIME = 5000;
  private Thread keepAliveTimer = null;

  static int getMaxConnections()
  {
    if (result == -1)
    {
      result = ((Integer)AccessController.doPrivileged(new GetIntegerAction("http.maxConnections", 5))).intValue();
      if (result <= 0)
        result = 5;
    }
    return result;
  }

  public synchronized void put(URL paramURL, Object paramObject, HttpClient paramHttpClient)
  {
    int i = (this.keepAliveTimer == null) ? 1 : 0;
    if ((i == 0) && (!(this.keepAliveTimer.isAlive())))
      i = 1;
    if (i != 0)
    {
      clear();
      localObject = this;
      AccessController.doPrivileged(new PrivilegedAction(this, (KeepAliveCache)localObject)
      {
        public Object run()
        {
          Object localObject = Thread.currentThread().getThreadGroup();
          ThreadGroup localThreadGroup = null;
          while ((localThreadGroup = ((ThreadGroup)localObject).getParent()) != null)
            localObject = localThreadGroup;
          KeepAliveCache.access$002(this.this$0, new Thread((ThreadGroup)localObject, this.val$cache, "Keep-Alive-Timer"));
          KeepAliveCache.access$000(this.this$0).setDaemon(true);
          KeepAliveCache.access$000(this.this$0).setPriority(8);
          KeepAliveCache.access$000(this.this$0).start();
          return null;
        }
      });
    }
    Object localObject = new KeepAliveKey(paramURL, paramObject);
    ClientVector localClientVector = (ClientVector)super.get(localObject);
    if (localClientVector == null)
    {
      int j = paramHttpClient.getKeepAliveTimeout();
      localClientVector = new ClientVector(5000);
      localClientVector.put(paramHttpClient);
      super.put(localObject, localClientVector);
    }
    else
    {
      localClientVector.put(paramHttpClient);
    }
  }

  public synchronized void remove(HttpClient paramHttpClient, Object paramObject)
  {
    KeepAliveKey localKeepAliveKey = new KeepAliveKey(paramHttpClient.url, paramObject);
    ClientVector localClientVector = (ClientVector)super.get(localKeepAliveKey);
    if (localClientVector != null)
    {
      localClientVector.remove(paramHttpClient);
      if (localClientVector.empty())
        removeVector(localKeepAliveKey);
    }
  }

  synchronized void removeVector(KeepAliveKey paramKeepAliveKey)
  {
    super.remove(paramKeepAliveKey);
  }

  public synchronized Object get(URL paramURL, Object paramObject)
  {
    KeepAliveKey localKeepAliveKey = new KeepAliveKey(paramURL, paramObject);
    ClientVector localClientVector = (ClientVector)super.get(localKeepAliveKey);
    if (localClientVector == null)
      return null;
    return localClientVector.get();
  }

  public void run()
  {
    do
    {
      try
      {
        Thread.sleep(5000L);
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      synchronized (this)
      {
        long l = System.currentTimeMillis();
        Iterator localIterator = keySet().iterator();
        ArrayList localArrayList = new ArrayList();
        while (localIterator.hasNext())
        {
          KeepAliveKey localKeepAliveKey = (KeepAliveKey)localIterator.next();
          ClientVector localClientVector1 = (ClientVector)get(localKeepAliveKey);
          synchronized (localClientVector1)
          {
            for (int i = 0; i < localClientVector1.size(); ++i)
            {
              KeepAliveEntry localKeepAliveEntry = (KeepAliveEntry)localClientVector1.elementAt(i);
              if (l - localKeepAliveEntry.idleStartTime <= localClientVector1.nap)
                break;
              HttpClient localHttpClient = localKeepAliveEntry.hc;
              localHttpClient.closeServer();
            }
            localClientVector1.subList(0, i).clear();
            if (localClientVector1.size() == 0)
              localArrayList.add(localKeepAliveKey);
          }
        }
        localIterator = localArrayList.iterator();
        while (localIterator.hasNext())
          removeVector((KeepAliveKey)localIterator.next());
      }
    }
    while (size() > 0);
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    throw new NotSerializableException();
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    throw new NotSerializableException();
  }
}