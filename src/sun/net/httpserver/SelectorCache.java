package sun.net.httpserver;

import java.io.IOException;
import java.nio.channels.Selector;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.ListIterator;

public class SelectorCache
{
  static SelectorCache cache = null;
  LinkedList<SelectorWrapper> freeSelectors = new LinkedList();

  private SelectorCache()
  {
    CacheCleaner localCacheCleaner = (CacheCleaner)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public SelectorCache.CacheCleaner run()
      {
        SelectorCache.CacheCleaner localCacheCleaner = new SelectorCache.CacheCleaner(this.this$0);
        localCacheCleaner.setDaemon(true);
        return localCacheCleaner;
      }
    });
    localCacheCleaner.start();
  }

  public static SelectorCache getSelectorCache()
  {
    synchronized (SelectorCache.class)
    {
      if (cache == null)
        cache = new SelectorCache();
    }
    return cache;
  }

  synchronized Selector getSelector()
    throws IOException
  {
    Selector localSelector;
    SelectorWrapper localSelectorWrapper = null;
    if (this.freeSelectors.size() > 0)
    {
      localSelectorWrapper = (SelectorWrapper)this.freeSelectors.remove();
      localSelector = localSelectorWrapper.getSelector();
    }
    else
    {
      localSelector = Selector.open();
    }
    return localSelector;
  }

  synchronized void freeSelector(Selector paramSelector)
  {
    this.freeSelectors.add(new SelectorWrapper(paramSelector, null));
  }

  class CacheCleaner extends Thread
  {
    public void run()
    {
      long l = ServerConfig.getSelCacheTimeout() * 1000L;
      while (true)
      {
        try
        {
          Thread.sleep(l);
        }
        catch (Exception localException)
        {
        }
        synchronized (this.this$0.freeSelectors)
        {
          ListIterator localListIterator = this.this$0.freeSelectors.listIterator();
          while (localListIterator.hasNext())
          {
            SelectorCache.SelectorWrapper localSelectorWrapper = (SelectorCache.SelectorWrapper)localListIterator.next();
            if (localSelectorWrapper.getDeleteFlag())
            {
              try
              {
                localSelectorWrapper.getSelector().close();
              }
              catch (IOException localIOException)
              {
              }
              localListIterator.remove();
            }
            else
            {
              localSelectorWrapper.setDeleteFlag(true);
            }
          }
        }
      }
    }
  }

  private static class SelectorWrapper
  {
    private Selector sel;
    private boolean deleteFlag;

    private SelectorWrapper(Selector paramSelector)
    {
      this.sel = paramSelector;
      this.deleteFlag = false;
    }

    public Selector getSelector()
    {
      return this.sel;
    }

    public boolean getDeleteFlag()
    {
      return this.deleteFlag;
    }

    public void setDeleteFlag(boolean paramBoolean)
    {
      this.deleteFlag = paramBoolean;
    }
  }
}