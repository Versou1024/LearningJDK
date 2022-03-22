package sun.awt.image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.Hashtable;

public class ToolkitImage extends Image
{
  ImageProducer source;
  InputStreamImageSource src;
  ImageRepresentation imagerep;
  private int width = -1;
  private int height = -1;
  private Hashtable properties;
  private int availinfo;

  protected ToolkitImage()
  {
  }

  public ToolkitImage(ImageProducer paramImageProducer)
  {
    this.source = paramImageProducer;
    if (paramImageProducer instanceof InputStreamImageSource)
      this.src = ((InputStreamImageSource)paramImageProducer);
  }

  public ImageProducer getSource()
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    return this.source;
  }

  public int getWidth()
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x1) == 0)
      reconstruct(1);
    return this.width;
  }

  public synchronized int getWidth(ImageObserver paramImageObserver)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x1) == 0)
    {
      addWatcher(paramImageObserver, true);
      if ((this.availinfo & 0x1) == 0)
        return -1;
    }
    return this.width;
  }

  public int getHeight()
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x2) == 0)
      reconstruct(2);
    return this.height;
  }

  public synchronized int getHeight(ImageObserver paramImageObserver)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x2) == 0)
    {
      addWatcher(paramImageObserver, true);
      if ((this.availinfo & 0x2) == 0)
        return -1;
    }
    return this.height;
  }

  public Object getProperty(String paramString, ImageObserver paramImageObserver)
  {
    if (paramString == null)
      throw new NullPointerException("null property name is not allowed");
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if (this.properties == null)
    {
      addWatcher(paramImageObserver, true);
      if (this.properties == null)
        return null;
    }
    Object localObject = this.properties.get(paramString);
    if (localObject == null)
      localObject = Image.UndefinedProperty;
    return localObject;
  }

  public boolean hasError()
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    return ((this.availinfo & 0x40) != 0);
  }

  public int check(ImageObserver paramImageObserver)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if (((this.availinfo & 0x40) == 0) && (((this.availinfo ^ 0xFFFFFFFF) & 0x7) != 0))
      addWatcher(paramImageObserver, false);
    return this.availinfo;
  }

  public void preload(ImageObserver paramImageObserver)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x20) == 0)
      addWatcher(paramImageObserver, true);
  }

  private synchronized void addWatcher(ImageObserver paramImageObserver, boolean paramBoolean)
  {
    if ((this.availinfo & 0x40) != 0)
    {
      if (paramImageObserver != null)
        paramImageObserver.imageUpdate(this, 192, -1, -1, -1, -1);
      return;
    }
    ImageRepresentation localImageRepresentation = getImageRep();
    localImageRepresentation.addWatcher(paramImageObserver);
    if (paramBoolean)
      localImageRepresentation.startProduction();
  }

  private synchronized void reconstruct(int paramInt)
  {
    if ((paramInt & (this.availinfo ^ 0xFFFFFFFF)) != 0)
    {
      if ((this.availinfo & 0x40) != 0)
        return;
      ImageRepresentation localImageRepresentation = getImageRep();
      localImageRepresentation.startProduction();
      do
      {
        if ((paramInt & (this.availinfo ^ 0xFFFFFFFF)) == 0)
          return;
        try
        {
          wait();
        }
        catch (InterruptedException localInterruptedException)
        {
          Thread.currentThread().interrupt();
          return;
        }
      }
      while ((this.availinfo & 0x40) == 0);
      return;
    }
  }

  synchronized void addInfo(int paramInt)
  {
    this.availinfo |= paramInt;
    notifyAll();
  }

  void setDimensions(int paramInt1, int paramInt2)
  {
    this.width = paramInt1;
    this.height = paramInt2;
    addInfo(3);
  }

  void setProperties(Hashtable paramHashtable)
  {
    if (paramHashtable == null)
      paramHashtable = new Hashtable();
    this.properties = paramHashtable;
    addInfo(4);
  }

  synchronized void infoDone(int paramInt)
  {
    if ((paramInt == 1) || (((this.availinfo ^ 0xFFFFFFFF) & 0x3) != 0))
      addInfo(64);
    else if ((this.availinfo & 0x4) == 0)
      setProperties(null);
  }

  public void flush()
  {
    ImageRepresentation localImageRepresentation;
    if (this.src != null)
      this.src.checkSecurity(null, false);
    synchronized (this)
    {
      this.availinfo &= -65;
      localImageRepresentation = this.imagerep;
      this.imagerep = null;
    }
    if (localImageRepresentation != null)
      localImageRepresentation.abort();
    if (this.src != null)
      this.src.flush();
  }

  protected ImageRepresentation makeImageRep()
  {
    return new ImageRepresentation(this, ColorModel.getRGBdefault(), false);
  }

  public synchronized ImageRepresentation getImageRep()
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if (this.imagerep == null)
      this.imagerep = makeImageRep();
    return this.imagerep;
  }

  public Graphics getGraphics()
  {
    throw new UnsupportedOperationException("getGraphics() not valid for images created with createImage(producer)");
  }

  public ColorModel getColorModel()
  {
    ImageRepresentation localImageRepresentation = getImageRep();
    return localImageRepresentation.getColorModel();
  }

  public BufferedImage getBufferedImage()
  {
    ImageRepresentation localImageRepresentation = getImageRep();
    return localImageRepresentation.getBufferedImage();
  }

  public void setAccelerationPriority(float paramFloat)
  {
    super.setAccelerationPriority(paramFloat);
    ImageRepresentation localImageRepresentation = getImageRep();
    localImageRepresentation.setAccelerationPriority(this.accelerationPriority);
  }

  static
  {
    NativeLibLoader.loadLibraries();
  }
}