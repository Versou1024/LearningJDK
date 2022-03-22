package sun.awt.image;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.lang.ref.WeakReference;

public abstract class ImageWatched
{
  public static Link endlink = new Link();
  public Link watcherList = endlink;

  public synchronized void addWatcher(ImageObserver paramImageObserver)
  {
    if ((paramImageObserver != null) && (!(isWatcher(paramImageObserver))))
      this.watcherList = new WeakLink(paramImageObserver, this.watcherList);
  }

  public synchronized boolean isWatcher(ImageObserver paramImageObserver)
  {
    return this.watcherList.isWatcher(paramImageObserver);
  }

  public void removeWatcher(ImageObserver paramImageObserver)
  {
    synchronized (this)
    {
      this.watcherList = this.watcherList.removeWatcher(paramImageObserver);
    }
    if (this.watcherList == endlink)
      notifyWatcherListEmpty();
  }

  public boolean isWatcherListEmpty()
  {
    synchronized (this)
    {
      this.watcherList = this.watcherList.removeWatcher(null);
    }
    return (this.watcherList == endlink);
  }

  public void newInfo(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    if (this.watcherList.newInfo(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5))
      removeWatcher(null);
  }

  protected abstract void notifyWatcherListEmpty();

  public static class Link
  {
    public boolean isWatcher(ImageObserver paramImageObserver)
    {
      return false;
    }

    public Link removeWatcher(ImageObserver paramImageObserver)
    {
      return this;
    }

    public boolean newInfo(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      return false;
    }
  }

  public static class WeakLink extends ImageWatched.Link
  {
    private WeakReference<ImageObserver> myref;
    private ImageWatched.Link next;

    public WeakLink(ImageObserver paramImageObserver, ImageWatched.Link paramLink)
    {
      this.myref = new WeakReference(paramImageObserver);
      this.next = paramLink;
    }

    public boolean isWatcher(ImageObserver paramImageObserver)
    {
      return ((this.myref.get() == paramImageObserver) || (this.next.isWatcher(paramImageObserver)));
    }

    public ImageWatched.Link removeWatcher(ImageObserver paramImageObserver)
    {
      ImageObserver localImageObserver = (ImageObserver)this.myref.get();
      if (localImageObserver == null)
        return this.next.removeWatcher(paramImageObserver);
      if (localImageObserver == paramImageObserver)
        return this.next;
      this.next = this.next.removeWatcher(paramImageObserver);
      return this;
    }

    public boolean newInfo(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      boolean bool = this.next.newInfo(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
      ImageObserver localImageObserver = (ImageObserver)this.myref.get();
      if (localImageObserver == null)
      {
        bool = true;
      }
      else if (!(localImageObserver.imageUpdate(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5)))
      {
        this.myref.clear();
        bool = true;
      }
      return bool;
    }
  }
}