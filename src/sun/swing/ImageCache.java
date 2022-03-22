package sun.swing;

import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class ImageCache
{
  private int maxCount;
  private final LinkedList<SoftReference<Entry>> entries;

  public ImageCache(int paramInt)
  {
    this.maxCount = paramInt;
    this.entries = new LinkedList();
  }

  void setMaxCount(int paramInt)
  {
    this.maxCount = paramInt;
  }

  public void flush()
  {
    this.entries.clear();
  }

  private Entry getEntry(Object paramObject, GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, Object[] paramArrayOfObject)
  {
    ListIterator localListIterator = this.entries.listIterator();
    while (localListIterator.hasNext())
    {
      SoftReference localSoftReference = (SoftReference)localListIterator.next();
      localEntry = (Entry)localSoftReference.get();
      if (localEntry == null)
      {
        localListIterator.remove();
      }
      else if (localEntry.equals(paramGraphicsConfiguration, paramInt1, paramInt2, paramArrayOfObject))
      {
        localListIterator.remove();
        this.entries.addFirst(localSoftReference);
        return localEntry;
      }
    }
    Entry localEntry = new Entry(paramGraphicsConfiguration, paramInt1, paramInt2, paramArrayOfObject);
    if (this.entries.size() >= this.maxCount)
      this.entries.removeLast();
    this.entries.addFirst(new SoftReference(localEntry));
    return localEntry;
  }

  public Image getImage(Object paramObject, GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, Object[] paramArrayOfObject)
  {
    Entry localEntry = getEntry(paramObject, paramGraphicsConfiguration, paramInt1, paramInt2, paramArrayOfObject);
    return localEntry.getImage();
  }

  public void setImage(Object paramObject, GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, Object[] paramArrayOfObject, Image paramImage)
  {
    Entry localEntry = getEntry(paramObject, paramGraphicsConfiguration, paramInt1, paramInt2, paramArrayOfObject);
    localEntry.setImage(paramImage);
  }

  private static class Entry
  {
    private final GraphicsConfiguration config;
    private final int w;
    private final int h;
    private final Object[] args;
    private Image image;

    Entry(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, Object[] paramArrayOfObject)
    {
      this.config = paramGraphicsConfiguration;
      this.args = paramArrayOfObject;
      this.w = paramInt1;
      this.h = paramInt2;
    }

    public void setImage(Image paramImage)
    {
      this.image = paramImage;
    }

    public Image getImage()
    {
      return this.image;
    }

    public String toString()
    {
      String str = super.toString() + "[ graphicsConfig=" + this.config + ", image=" + this.image + ", w=" + this.w + ", h=" + this.h;
      if (this.args != null)
        for (int i = 0; i < this.args.length; ++i)
          str = str + ", " + this.args[i];
      str = str + "]";
      return str;
    }

    public boolean equals(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, Object[] paramArrayOfObject)
    {
      if ((this.w == paramInt1) && (this.h == paramInt2) && ((((this.config != null) && (this.config.equals(paramGraphicsConfiguration))) || ((this.config == null) && (paramGraphicsConfiguration == null)))))
      {
        if ((this.args == null) && (paramArrayOfObject == null))
          return true;
        if ((this.args != null) && (paramArrayOfObject != null) && (this.args.length == paramArrayOfObject.length))
        {
          for (int i = paramArrayOfObject.length - 1; i >= 0; --i)
          {
            Object localObject1 = this.args[i];
            Object localObject2 = paramArrayOfObject[i];
            if (((localObject1 == null) && (localObject2 != null)) || ((localObject1 != null) && (!(localObject1.equals(localObject2)))))
              return false;
          }
          return true;
        }
      }
      return false;
    }
  }
}