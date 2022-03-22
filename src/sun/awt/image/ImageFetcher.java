package sun.awt.image;

import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import sun.awt.AppContext;

class ImageFetcher extends Thread
{
  static final int HIGH_PRIORITY = 8;
  static final int LOW_PRIORITY = 3;
  static final int ANIM_PRIORITY = 2;
  static final int TIMEOUT = 5000;

  private ImageFetcher(ThreadGroup paramThreadGroup, int paramInt)
  {
    super(paramThreadGroup, "Image Fetcher " + paramInt);
    setDaemon(true);
  }

  public static void add(ImageFetchable paramImageFetchable)
  {
    FetcherInfo localFetcherInfo = FetcherInfo.getFetcherInfo();
    synchronized (localFetcherInfo.waitList)
    {
      if (!(localFetcherInfo.waitList.contains(paramImageFetchable)))
      {
        localFetcherInfo.waitList.addElement(paramImageFetchable);
        if ((localFetcherInfo.numWaiting == 0) && (localFetcherInfo.numFetchers < localFetcherInfo.fetchers.length))
          createFetchers(localFetcherInfo);
        localFetcherInfo.waitList.notify();
      }
    }
  }

  public static void remove(ImageFetchable paramImageFetchable)
  {
    FetcherInfo localFetcherInfo = FetcherInfo.getFetcherInfo();
    synchronized (localFetcherInfo.waitList)
    {
      if (localFetcherInfo.waitList.contains(paramImageFetchable))
        localFetcherInfo.waitList.removeElement(paramImageFetchable);
    }
  }

  public static boolean isFetcher(Thread paramThread)
  {
    FetcherInfo localFetcherInfo = FetcherInfo.getFetcherInfo();
    synchronized (localFetcherInfo.waitList)
    {
      int i = 0;
      while (true)
      {
        if (i >= localFetcherInfo.fetchers.length)
          break label42;
        if (localFetcherInfo.fetchers[i] == paramThread)
          return true;
        label42: ++i;
      }
    }
    return false;
  }

  public static boolean amFetcher()
  {
    return isFetcher(Thread.currentThread());
  }

  private static ImageFetchable nextImage()
  {
    FetcherInfo localFetcherInfo = FetcherInfo.getFetcherInfo();
    synchronized (localFetcherInfo.waitList)
    {
      ImageFetchable localImageFetchable = null;
      long l1 = System.currentTimeMillis() + 5000L;
      while (localImageFetchable == null)
      {
        while (localFetcherInfo.waitList.size() == 0)
        {
          long l2 = System.currentTimeMillis();
          if (l2 >= l1)
            return null;
          try
          {
            localFetcherInfo.numWaiting += 1;
            localFetcherInfo.waitList.wait(l1 - l2);
          }
          catch (InterruptedException localInterruptedException)
          {
            Object localObject1 = null;
            return localObject1;
          }
          finally
          {
            localFetcherInfo.numWaiting -= 1;
          }
        }
        localImageFetchable = (ImageFetchable)localFetcherInfo.waitList.elementAt(0);
        localFetcherInfo.waitList.removeElement(localImageFetchable);
      }
      return localImageFetchable;
    }
  }

  public void run()
  {
    Thread localThread1;
    int i;
    FetcherInfo localFetcherInfo = FetcherInfo.getFetcherInfo();
    try
    {
      fetchloop();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    finally
    {
      synchronized (localFetcherInfo.waitList)
      {
        Thread localThread2 = Thread.currentThread();
        for (int j = 0; j < localFetcherInfo.fetchers.length; ++j)
          if (localFetcherInfo.fetchers[j] == localThread2)
          {
            localFetcherInfo.fetchers[j] = null;
            localFetcherInfo.numFetchers -= 1;
          }
      }
    }
  }

  private void fetchloop()
  {
    Thread localThread = Thread.currentThread();
    while (isFetcher(localThread))
    {
      Thread.interrupted();
      localThread.setPriority(8);
      ImageFetchable localImageFetchable = nextImage();
      if (localImageFetchable == null)
        return;
      try
      {
        localImageFetchable.doFetch();
      }
      catch (Exception localException)
      {
        System.err.println("Uncaught error fetching image:");
        localException.printStackTrace();
      }
      stoppingAnimation(localThread);
    }
  }

  static void startingAnimation()
  {
    FetcherInfo localFetcherInfo = FetcherInfo.getFetcherInfo();
    Thread localThread = Thread.currentThread();
    synchronized (localFetcherInfo.waitList)
    {
      int i = 0;
      while (true)
      {
        if (i >= localFetcherInfo.fetchers.length)
          break label103;
        if (localFetcherInfo.fetchers[i] == localThread)
        {
          localFetcherInfo.fetchers[i] = null;
          localFetcherInfo.numFetchers -= 1;
          localThread.setName("Image Animator " + i);
          if (localFetcherInfo.waitList.size() > localFetcherInfo.numWaiting)
            createFetchers(localFetcherInfo);
          return;
        }
        label103: ++i;
      }
    }
    localThread.setPriority(2);
    localThread.setName("Image Animator");
  }

  private static void stoppingAnimation(Thread paramThread)
  {
    FetcherInfo localFetcherInfo = FetcherInfo.getFetcherInfo();
    synchronized (localFetcherInfo.waitList)
    {
      int i = -1;
      for (int j = 0; j < localFetcherInfo.fetchers.length; ++j)
      {
        if (localFetcherInfo.fetchers[j] == paramThread)
          return;
        if (localFetcherInfo.fetchers[j] == null)
          i = j;
      }
      if (i < 0)
        break label106;
      localFetcherInfo.fetchers[i] = paramThread;
      localFetcherInfo.numFetchers += 1;
      paramThread.setName("Image Fetcher " + i);
      label106: return;
    }
  }

  private static void createFetchers(FetcherInfo paramFetcherInfo)
  {
    Object localObject2;
    AppContext localAppContext = AppContext.getAppContext();
    Object localObject1 = localAppContext.getThreadGroup();
    try
    {
      if (((ThreadGroup)localObject1).getParent() != null)
      {
        localObject2 = localObject1;
      }
      else
      {
        localObject1 = Thread.currentThread().getThreadGroup();
        for (ThreadGroup localThreadGroup = ((ThreadGroup)localObject1).getParent(); (localThreadGroup != null) && (localThreadGroup.getParent() != null); localThreadGroup = ((ThreadGroup)localObject1).getParent())
          localObject1 = localThreadGroup;
        localObject2 = localObject1;
      }
    }
    catch (SecurityException localSecurityException)
    {
      localObject2 = localAppContext.getThreadGroup();
    }
    Object localObject3 = localObject2;
    AccessController.doPrivileged(new PrivilegedAction(paramFetcherInfo, localObject3)
    {
      public Object run()
      {
        for (int i = 0; i < this.val$info.fetchers.length; ++i)
          if (this.val$info.fetchers[i] == null)
          {
            this.val$info.fetchers[i] = new ImageFetcher(this.val$fetcherGroup, i, null);
            this.val$info.fetchers[i].start();
            this.val$info.numFetchers += 1;
            break;
          }
        return null;
      }
    });
  }
}