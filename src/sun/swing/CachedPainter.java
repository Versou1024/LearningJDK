package sun.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.HashMap;
import java.util.Map;

public abstract class CachedPainter
{
  private static final Map<Object, ImageCache> cacheMap = new HashMap();

  private static ImageCache getCache(Object paramObject)
  {
    synchronized (CachedPainter.class)
    {
      ImageCache localImageCache = (ImageCache)cacheMap.get(paramObject);
      if (localImageCache == null)
      {
        localImageCache = new ImageCache(1);
        cacheMap.put(paramObject, localImageCache);
      }
      return localImageCache;
    }
  }

  public CachedPainter(int paramInt)
  {
    getCache(super.getClass()).setMaxCount(paramInt);
  }

  public void paint(Component paramComponent, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object[] paramArrayOfObject)
  {
    if ((paramInt3 <= 0) || (paramInt4 <= 0))
      return;
    if (paramComponent != null)
      synchronized (paramComponent.getTreeLock())
      {
        synchronized (CachedPainter.class)
        {
          paint0(paramComponent, paramGraphics, paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfObject);
        }
      }
    else
      synchronized (CachedPainter.class)
      {
        paint0(paramComponent, paramGraphics, paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfObject);
      }
  }

  private void paint0(Component paramComponent, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object[] paramArrayOfObject)
  {
    Class localClass = super.getClass();
    GraphicsConfiguration localGraphicsConfiguration = getGraphicsConfiguration(paramComponent);
    ImageCache localImageCache = getCache(localClass);
    Image localImage = localImageCache.getImage(localClass, localGraphicsConfiguration, paramInt3, paramInt4, paramArrayOfObject);
    int i = 0;
    do
    {
      int j = 0;
      if (localImage instanceof VolatileImage)
        switch (((VolatileImage)localImage).validate(localGraphicsConfiguration))
        {
        case 2:
          ((VolatileImage)localImage).flush();
          localImage = null;
          break;
        case 1:
          j = 1;
        }
      if (localImage == null)
      {
        localImage = createImage(paramComponent, paramInt3, paramInt4, localGraphicsConfiguration, paramArrayOfObject);
        localImageCache.setImage(localClass, localGraphicsConfiguration, paramInt3, paramInt4, paramArrayOfObject, localImage);
        j = 1;
      }
      if (j != 0)
      {
        Graphics localGraphics = localImage.getGraphics();
        paintToImage(paramComponent, localImage, localGraphics, paramInt3, paramInt4, paramArrayOfObject);
        localGraphics.dispose();
      }
      paintImage(paramComponent, paramGraphics, paramInt1, paramInt2, paramInt3, paramInt4, localImage, paramArrayOfObject);
    }
    while ((localImage instanceof VolatileImage) && (((VolatileImage)localImage).contentsLost()) && (++i < 3));
  }

  protected abstract void paintToImage(Component paramComponent, Image paramImage, Graphics paramGraphics, int paramInt1, int paramInt2, Object[] paramArrayOfObject);

  protected void paintImage(Component paramComponent, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Image paramImage, Object[] paramArrayOfObject)
  {
    paramGraphics.drawImage(paramImage, paramInt1, paramInt2, null);
  }

  protected Image createImage(Component paramComponent, int paramInt1, int paramInt2, GraphicsConfiguration paramGraphicsConfiguration, Object[] paramArrayOfObject)
  {
    if (paramGraphicsConfiguration == null)
      return new BufferedImage(paramInt1, paramInt2, 1);
    return paramGraphicsConfiguration.createCompatibleVolatileImage(paramInt1, paramInt2);
  }

  protected void flush()
  {
    synchronized (CachedPainter.class)
    {
      getCache(super.getClass()).flush();
    }
  }

  private GraphicsConfiguration getGraphicsConfiguration(Component paramComponent)
  {
    if (paramComponent == null)
      return null;
    return paramComponent.getGraphicsConfiguration();
  }
}