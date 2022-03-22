package sun.awt.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class OffScreenImageSource
  implements ImageProducer
{
  BufferedImage image;
  int width;
  int height;
  Hashtable properties;
  private ImageConsumer theConsumer;

  public OffScreenImageSource(BufferedImage paramBufferedImage, Hashtable paramHashtable)
  {
    this.image = paramBufferedImage;
    if (paramHashtable != null)
      this.properties = paramHashtable;
    else
      this.properties = new Hashtable();
    this.width = paramBufferedImage.getWidth();
    this.height = paramBufferedImage.getHeight();
  }

  public OffScreenImageSource(BufferedImage paramBufferedImage)
  {
    this(paramBufferedImage, null);
  }

  public synchronized void addConsumer(ImageConsumer paramImageConsumer)
  {
    this.theConsumer = paramImageConsumer;
    produce();
  }

  public synchronized boolean isConsumer(ImageConsumer paramImageConsumer)
  {
    return (paramImageConsumer == this.theConsumer);
  }

  public synchronized void removeConsumer(ImageConsumer paramImageConsumer)
  {
    if (this.theConsumer == paramImageConsumer)
      this.theConsumer = null;
  }

  public void startProduction(ImageConsumer paramImageConsumer)
  {
    addConsumer(paramImageConsumer);
  }

  public void requestTopDownLeftRightResend(ImageConsumer paramImageConsumer)
  {
  }

  private void sendPixels()
  {
    Object localObject;
    int i1;
    int i3;
    ColorModel localColorModel = this.image.getColorModel();
    WritableRaster localWritableRaster = this.image.getRaster();
    int i = localWritableRaster.getNumDataElements();
    int j = localWritableRaster.getDataBuffer().getDataType();
    int[] arrayOfInt = new int[this.width * i];
    int k = 1;
    if (localColorModel instanceof IndexColorModel)
    {
      byte[] arrayOfByte = new byte[this.width];
      this.theConsumer.setColorModel(localColorModel);
      if (localWritableRaster instanceof ByteComponentRaster)
      {
        k = 0;
        for (i1 = 0; i1 < this.height; ++i1)
        {
          localWritableRaster.getDataElements(0, i1, this.width, 1, arrayOfByte);
          this.theConsumer.setPixels(0, i1, this.width, 1, localColorModel, arrayOfByte, 0, this.width);
        }
      }
      else if (localWritableRaster instanceof BytePackedRaster)
      {
        k = 0;
        for (i1 = 0; i1 < this.height; ++i1)
        {
          localWritableRaster.getPixels(0, i1, this.width, 1, arrayOfInt);
          for (i3 = 0; i3 < this.width; ++i3)
            arrayOfByte[i3] = (byte)arrayOfInt[i3];
          this.theConsumer.setPixels(0, i1, this.width, 1, localColorModel, arrayOfByte, 0, this.width);
        }
      }
      else if ((j == 2) || (j == 3))
      {
        k = 0;
        for (i1 = 0; i1 < this.height; ++i1)
        {
          localWritableRaster.getPixels(0, i1, this.width, 1, arrayOfInt);
          this.theConsumer.setPixels(0, i1, this.width, 1, localColorModel, arrayOfInt, 0, this.width);
        }
      }
    }
    else if (localColorModel instanceof DirectColorModel)
    {
      this.theConsumer.setColorModel(localColorModel);
      k = 0;
      switch (j)
      {
      case 3:
        for (int l = 0; l < this.height; ++l)
        {
          localWritableRaster.getDataElements(0, l, this.width, 1, arrayOfInt);
          this.theConsumer.setPixels(0, l, this.width, 1, localColorModel, arrayOfInt, 0, this.width);
        }
        break;
      case 0:
        localObject = new byte[this.width];
        for (i1 = 0; i1 < this.height; ++i1)
        {
          localWritableRaster.getDataElements(0, i1, this.width, 1, localObject);
          for (i3 = 0; i3 < this.width; ++i3)
            arrayOfInt[i3] = (localObject[i3] & 0xFF);
          this.theConsumer.setPixels(0, i1, this.width, 1, localColorModel, arrayOfInt, 0, this.width);
        }
        break;
      case 1:
        short[] arrayOfShort = new short[this.width];
        for (i3 = 0; i3 < this.height; ++i3)
        {
          localWritableRaster.getDataElements(0, i3, this.width, 1, arrayOfShort);
          for (int i4 = 0; i4 < this.width; ++i4)
            arrayOfInt[i4] = (arrayOfShort[i4] & 0xFFFF);
          this.theConsumer.setPixels(0, i3, this.width, 1, localColorModel, arrayOfInt, 0, this.width);
        }
        break;
      case 2:
      default:
        k = 1;
      }
    }
    if (k != 0)
    {
      localObject = ColorModel.getRGBdefault();
      this.theConsumer.setColorModel((ColorModel)localObject);
      for (int i2 = 0; i2 < this.height; ++i2)
      {
        for (i3 = 0; i3 < this.width; ++i3)
          arrayOfInt[i3] = this.image.getRGB(i3, i2);
        this.theConsumer.setPixels(0, i2, this.width, 1, (ColorModel)localObject, arrayOfInt, 0, this.width);
      }
    }
  }

  private void produce()
  {
    try
    {
      this.theConsumer.setDimensions(this.image.getWidth(), this.image.getHeight());
      this.theConsumer.setProperties(this.properties);
      sendPixels();
      this.theConsumer.imageComplete(2);
    }
    catch (NullPointerException localNullPointerException)
    {
      if (this.theConsumer != null)
        this.theConsumer.imageComplete(1);
    }
  }
}