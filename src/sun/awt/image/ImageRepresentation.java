package sun.awt.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class ImageRepresentation extends ImageWatched
  implements ImageConsumer
{
  InputStreamImageSource src;
  ToolkitImage image;
  int tag;
  long pData;
  int width = -1;
  int height = -1;
  int hints;
  int availinfo;
  Rectangle newbits;
  BufferedImage bimage;
  WritableRaster biRaster;
  protected ColorModel cmodel;
  ColorModel srcModel = null;
  int[] srcLUT = null;
  int srcLUTtransIndex = -1;
  int numSrcLUT = 0;
  boolean forceCMhint;
  int sstride;
  boolean isDefaultBI = false;
  boolean isSameCM = false;
  static boolean s_useNative;
  private boolean consuming = false;
  private int numWaiters;

  private static native void initIDs();

  public ImageRepresentation(ToolkitImage paramToolkitImage, ColorModel paramColorModel, boolean paramBoolean)
  {
    this.image = paramToolkitImage;
    if (this.image.getSource() instanceof InputStreamImageSource)
      this.src = ((InputStreamImageSource)this.image.getSource());
    setColorModel(paramColorModel);
    this.forceCMhint = paramBoolean;
  }

  public synchronized void reconstruct(int paramInt)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    int i = paramInt & (this.availinfo ^ 0xFFFFFFFF);
    if (((this.availinfo & 0x40) == 0) && (i != 0))
    {
      this.numWaiters += 1;
      try
      {
        startProduction();
        for (i = paramInt & (this.availinfo ^ 0xFFFFFFFF); ((this.availinfo & 0x40) == 0) && (i != 0); i = paramInt & (this.availinfo ^ 0xFFFFFFFF))
          try
          {
            wait();
          }
          catch (InterruptedException localInterruptedException)
          {
            Thread.currentThread().interrupt();
            decrementWaiters();
            return;
          }
      }
      finally
      {
        decrementWaiters();
      }
    }
  }

  public void setDimensions(int paramInt1, int paramInt2)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    this.image.setDimensions(paramInt1, paramInt2);
    newInfo(this.image, 3, 0, 0, paramInt1, paramInt2);
    if ((paramInt1 <= 0) || (paramInt2 <= 0))
    {
      imageComplete(1);
      return;
    }
    if ((this.width != paramInt1) || (this.height != paramInt2))
      this.bimage = null;
    this.width = paramInt1;
    this.height = paramInt2;
    this.availinfo |= 3;
  }

  public int getWidth()
  {
    return this.width;
  }

  public int getHeight()
  {
    return this.height;
  }

  ColorModel getColorModel()
  {
    return this.cmodel;
  }

  BufferedImage getBufferedImage()
  {
    return this.bimage;
  }

  protected BufferedImage createImage(ColorModel paramColorModel, WritableRaster paramWritableRaster, boolean paramBoolean, Hashtable paramHashtable)
  {
    BufferedImage localBufferedImage = new BufferedImage(paramColorModel, paramWritableRaster, paramBoolean, null);
    localBufferedImage.setAccelerationPriority(this.image.getAccelerationPriority());
    return localBufferedImage;
  }

  public void setProperties(Hashtable<?, ?> paramHashtable)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    this.image.setProperties(paramHashtable);
    newInfo(this.image, 4, 0, 0, 0, 0);
  }

  public void setColorModel(ColorModel paramColorModel)
  {
    Object localObject;
    if (this.src != null)
      this.src.checkSecurity(null, false);
    this.srcModel = paramColorModel;
    if (paramColorModel instanceof IndexColorModel)
    {
      if (paramColorModel.getTransparency() == 3)
      {
        this.cmodel = ColorModel.getRGBdefault();
        this.srcLUT = null;
      }
      else
      {
        localObject = (IndexColorModel)paramColorModel;
        this.numSrcLUT = ((IndexColorModel)localObject).getMapSize();
        this.srcLUT = new int[Math.max(this.numSrcLUT, 256)];
        ((IndexColorModel)localObject).getRGBs(this.srcLUT);
        this.srcLUTtransIndex = ((IndexColorModel)localObject).getTransparentPixel();
        this.cmodel = paramColorModel;
      }
    }
    else if (this.cmodel == null)
    {
      this.cmodel = paramColorModel;
      this.srcLUT = null;
    }
    else if (paramColorModel instanceof DirectColorModel)
    {
      localObject = (DirectColorModel)paramColorModel;
      if ((((DirectColorModel)localObject).getRedMask() == 16711680) && (((DirectColorModel)localObject).getGreenMask() == 65280) && (((DirectColorModel)localObject).getBlueMask() == 255))
      {
        this.cmodel = paramColorModel;
        this.srcLUT = null;
      }
    }
    this.isSameCM = (this.cmodel == paramColorModel);
  }

  void createBufferedImage()
  {
    this.isDefaultBI = false;
    try
    {
      this.biRaster = this.cmodel.createCompatibleWritableRaster(this.width, this.height);
      this.bimage = createImage(this.cmodel, this.biRaster, this.cmodel.isAlphaPremultiplied(), null);
    }
    catch (Exception localException)
    {
      this.cmodel = ColorModel.getRGBdefault();
      this.biRaster = this.cmodel.createCompatibleWritableRaster(this.width, this.height);
      this.bimage = createImage(this.cmodel, this.biRaster, false, null);
    }
    int i = this.bimage.getType();
    if ((this.cmodel == ColorModel.getRGBdefault()) || (i == 1) || (i == 3))
    {
      this.isDefaultBI = true;
    }
    else if (this.cmodel instanceof DirectColorModel)
    {
      DirectColorModel localDirectColorModel = (DirectColorModel)this.cmodel;
      if ((localDirectColorModel.getRedMask() == 16711680) && (localDirectColorModel.getGreenMask() == 65280) && (localDirectColorModel.getBlueMask() == 255))
        this.isDefaultBI = true;
    }
  }

  private void convertToRGB()
  {
    int i1;
    int i2;
    int i = this.bimage.getWidth();
    int j = this.bimage.getHeight();
    int k = i * j;
    int[] arrayOfInt1 = new int[k];
    if ((this.cmodel instanceof IndexColorModel) && (this.biRaster instanceof ByteComponentRaster) && (this.biRaster.getNumDataElements() == 1))
    {
      localObject = (ByteComponentRaster)this.biRaster;
      byte[] arrayOfByte = ((ByteComponentRaster)localObject).getDataStorage();
      i1 = ((ByteComponentRaster)localObject).getDataOffset(0);
      for (i2 = 0; i2 < k; ++i2)
        arrayOfInt1[i2] = this.srcLUT[(arrayOfByte[(i1 + i2)] & 0xFF)];
    }
    else
    {
      localObject = null;
      int l = 0;
      for (i1 = 0; i1 < j; ++i1)
        for (i2 = 0; i2 < i; ++i2)
        {
          localObject = this.biRaster.getDataElements(i2, i1, localObject);
          arrayOfInt1[(l++)] = this.cmodel.getRGB(localObject);
        }
    }
    this.isSameCM = false;
    this.cmodel = ColorModel.getRGBdefault();
    Object localObject = new DataBufferInt(arrayOfInt1, arrayOfInt1.length);
    int[] arrayOfInt2 = { 16711680, 65280, 255, -16777216 };
    this.biRaster = Raster.createPackedRaster((DataBuffer)localObject, i, j, i, arrayOfInt2, null);
    this.bimage = createImage(this.cmodel, this.biRaster, this.cmodel.isAlphaPremultiplied(), null);
    CachingSurfaceManager.restoreLocalAcceleration(this.bimage);
    this.srcLUT = null;
    this.isDefaultBI = true;
  }

  public void setHints(int paramInt)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    this.hints = paramInt;
  }

  public native void setICMpixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt, byte[] paramArrayOfByte, int paramInt5, int paramInt6, IntegerComponentRaster paramIntegerComponentRaster);

  public native void setBytePixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, int paramInt5, int paramInt6, ByteComponentRaster paramByteComponentRaster, int paramInt7);

  public native int setDiffICM(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt, int paramInt5, int paramInt6, IndexColorModel paramIndexColorModel, byte[] paramArrayOfByte, int paramInt7, int paramInt8, ByteComponentRaster paramByteComponentRaster, int paramInt9);

  public void setPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, ColorModel paramColorModel, byte[] paramArrayOfByte, int paramInt5, int paramInt6)
  {
    int i = paramInt5;
    Object localObject1 = null;
    if (this.src != null)
      this.src.checkSecurity(null, false);
    synchronized (this)
    {
      if (this.bimage == null)
      {
        if (this.cmodel == null)
          this.cmodel = paramColorModel;
        createBufferedImage();
      }
      if ((!(this.isSameCM)) || (this.cmodel == paramColorModel) || (this.srcLUT == null) || (!(paramColorModel instanceof IndexColorModel)) || (!(this.biRaster instanceof ByteComponentRaster)))
        break label264;
      Object localObject2 = (IndexColorModel)paramColorModel;
      Object localObject3 = (ByteComponentRaster)this.biRaster;
      int i1 = this.numSrcLUT;
      if (setDiffICM(paramInt1, paramInt2, paramInt3, paramInt4, this.srcLUT, this.srcLUTtransIndex, this.numSrcLUT, (IndexColorModel)localObject2, paramArrayOfByte, paramInt5, paramInt6, (ByteComponentRaster)localObject3, ((ByteComponentRaster)localObject3).getDataOffset(0)) == 0)
      {
        convertToRGB();
        break label264:
      }
      ((ByteComponentRaster)localObject3).notifyChanged();
      if (i1 != this.numSrcLUT)
      {
        bool = ((IndexColorModel)localObject2).hasAlpha();
        if (this.srcLUTtransIndex != -1)
          bool = true;
        i4 = ((IndexColorModel)localObject2).getPixelSize();
        localObject2 = new IndexColorModel(i4, this.numSrcLUT, this.srcLUT, 0, bool, this.srcLUTtransIndex, 0);
        this.cmodel = ((ColorModel)localObject2);
        this.bimage = createImage((ColorModel)localObject2, (WritableRaster)localObject3, false, null);
      }
      return;
      label264: if (!(this.isDefaultBI))
        break label547;
      localObject3 = (IntegerComponentRaster)this.biRaster;
      if ((this.srcLUT == null) || (!(paramColorModel instanceof IndexColorModel)))
        break label450;
      if (paramColorModel == this.srcModel)
        break label322;
      ((IndexColorModel)paramColorModel).getRGBs(this.srcLUT);
      this.srcModel = paramColorModel;
      label322: if (!(s_useNative))
        break label357;
      ((IntegerComponentRaster)localObject3).notifyChanged();
      setICMpixels(paramInt1, paramInt2, paramInt3, paramInt4, this.srcLUT, paramArrayOfByte, paramInt5, paramInt6, (IntegerComponentRaster)localObject3);
      break label544:
      label357: int[] arrayOfInt = new int[paramInt3 * paramInt4];
      boolean bool = false;
      int i4 = 0;
      while (i4 < paramInt4)
      {
        j = i;
        for (int i6 = 0; i6 < paramInt3; ++i6)
          arrayOfInt[(bool++)] = this.srcLUT[(paramArrayOfByte[(j++)] & 0xFF)];
        ++i4;
        i += paramInt6;
      }
      ((IntegerComponentRaster)localObject3).setDataElements(paramInt1, paramInt2, paramInt3, paramInt4, arrayOfInt);
      break label544:
      label450: arrayOfInt = new int[paramInt3];
      int i3 = paramInt2;
      while (i3 < paramInt2 + paramInt4)
      {
        j = i;
        for (int i5 = 0; i5 < paramInt3; ++i5)
          arrayOfInt[i5] = paramColorModel.getRGB(paramArrayOfByte[(j++)] & 0xFF);
        ((IntegerComponentRaster)localObject3).setDataElements(paramInt1, i3, paramInt3, 1, arrayOfInt);
        ++i3;
        i += paramInt6;
      }
      this.availinfo |= 8;
      label544: break label798:
      label547: if ((this.cmodel != paramColorModel) || (!(this.biRaster instanceof ByteComponentRaster)) || (this.biRaster.getNumDataElements() != 1))
        break label713;
      localObject2 = (ByteComponentRaster)this.biRaster;
      if (paramInt3 * paramInt4 <= 200)
        break label682;
      if ((paramInt5 != 0) || (paramInt6 != paramInt3))
        break label622;
      ((ByteComponentRaster)localObject2).putByteData(paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfByte);
      break label710:
      label622: localObject3 = new byte[paramInt3];
      int j = paramInt5;
      for (int i2 = paramInt2; i2 < paramInt2 + paramInt4; ++i2)
      {
        System.arraycopy(paramArrayOfByte, j, localObject3, 0, paramInt3);
        ((ByteComponentRaster)localObject2).putByteData(paramInt1, i2, paramInt3, 1, localObject3);
        j += paramInt6;
      }
      break label710:
      label682: ((ByteComponentRaster)localObject2).notifyChanged();
      setBytePixels(paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfByte, paramInt5, paramInt6, (ByteComponentRaster)localObject2, ((ByteComponentRaster)localObject2).getDataOffset(0));
      label710: break label798:
      label713: int k = paramInt2;
      while (k < paramInt2 + paramInt4)
      {
        j = i;
        for (int l = paramInt1; l < paramInt1 + paramInt3; ++l)
          this.bimage.setRGB(l, k, paramColorModel.getRGB(paramArrayOfByte[(j++)] & 0xFF));
        ++k;
        i += paramInt6;
      }
      label798: this.availinfo |= 8;
    }
    if ((this.availinfo & 0x10) == 0)
      newInfo(this.image, 8, paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public void setPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, ColorModel paramColorModel, int[] paramArrayOfInt, int paramInt5, int paramInt6)
  {
    int i = paramInt5;
    if (this.src != null)
      this.src.checkSecurity(null, false);
    synchronized (this)
    {
      int k;
      Object localObject1;
      if (this.bimage == null)
      {
        if (this.cmodel == null)
          this.cmodel = paramColorModel;
        createBufferedImage();
      }
      int[] arrayOfInt1 = new int[paramInt3];
      if (this.cmodel instanceof IndexColorModel)
        convertToRGB();
      if ((paramColorModel == this.cmodel) && (this.biRaster instanceof IntegerComponentRaster))
      {
        localObject1 = (IntegerComponentRaster)this.biRaster;
        if ((paramInt5 == 0) && (paramInt6 == paramInt3))
        {
          ((IntegerComponentRaster)localObject1).setDataElements(paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfInt);
        }
        else
        {
          k = paramInt2;
          while (k < paramInt2 + paramInt4)
          {
            System.arraycopy(paramArrayOfInt, i, arrayOfInt1, 0, paramInt3);
            ((IntegerComponentRaster)localObject1).setDataElements(paramInt1, k, paramInt3, 1, arrayOfInt1);
            ++k;
            i += paramInt6;
          }
        }
      }
      else
      {
        int j;
        if ((paramColorModel.getTransparency() != 1) && (this.cmodel.getTransparency() == 1))
          convertToRGB();
        if (this.isDefaultBI)
        {
          localObject1 = (IntegerComponentRaster)this.biRaster;
          int[] arrayOfInt2 = ((IntegerComponentRaster)localObject1).getDataStorage();
          if (this.cmodel.equals(paramColorModel))
          {
            int i2 = ((IntegerComponentRaster)localObject1).getScanlineStride();
            int i4 = paramInt2 * i2 + paramInt1;
            k = 0;
            while (k < paramInt4)
            {
              System.arraycopy(paramArrayOfInt, i, arrayOfInt2, i4, paramInt3);
              i4 += i2;
              ++k;
              i += paramInt6;
            }
            ((IntegerComponentRaster)localObject1).notifyChanged();
          }
          else
          {
            k = paramInt2;
            while (k < paramInt2 + paramInt4)
            {
              j = i;
              for (int i3 = 0; i3 < paramInt3; ++i3)
                arrayOfInt1[i3] = paramColorModel.getRGB(paramArrayOfInt[(j++)]);
              ((IntegerComponentRaster)localObject1).setDataElements(paramInt1, k, paramInt3, 1, arrayOfInt1);
              ++k;
              i += paramInt6;
            }
          }
          this.availinfo |= 8;
        }
        else
        {
          localObject1 = null;
          k = paramInt2;
          while (k < paramInt2 + paramInt4)
          {
            j = i;
            for (int i1 = paramInt1; i1 < paramInt1 + paramInt3; ++i1)
            {
              int l = paramColorModel.getRGB(paramArrayOfInt[(j++)]);
              localObject1 = this.cmodel.getDataElements(l, localObject1);
              this.biRaster.setDataElements(i1, k, localObject1);
            }
            ++k;
            i += paramInt6;
          }
          this.availinfo |= 8;
        }
      }
    }
    if ((this.availinfo & 0x10) == 0)
      newInfo(this.image, 8, paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public BufferedImage getOpaqueRGBImage()
  {
    if (this.bimage.getType() == 2)
    {
      int i = this.bimage.getWidth();
      int j = this.bimage.getHeight();
      int k = i * j;
      DataBufferInt localDataBufferInt = (DataBufferInt)this.biRaster.getDataBuffer();
      int[] arrayOfInt1 = localDataBufferInt.getData();
      CachingSurfaceManager.restoreLocalAcceleration(this.bimage);
      for (int l = 0; l < k; ++l)
        if (arrayOfInt1[l] >>> 24 != 255)
          return this.bimage;
      DirectColorModel localDirectColorModel = new DirectColorModel(24, 16711680, 65280, 255);
      int[] arrayOfInt2 = { 16711680, 65280, 255 };
      WritableRaster localWritableRaster = Raster.createPackedRaster(localDataBufferInt, i, j, i, arrayOfInt2, null);
      try
      {
        BufferedImage localBufferedImage = createImage(localDirectColorModel, localWritableRaster, false, null);
        CachingSurfaceManager.restoreLocalAcceleration(localBufferedImage);
        return localBufferedImage;
      }
      catch (Exception localException)
      {
        return this.bimage;
      }
    }
    return this.bimage;
  }

  public void imageComplete(int paramInt)
  {
    int i;
    int j;
    if (this.src != null)
      this.src.checkSecurity(null, false);
    switch (paramInt)
    {
    case 4:
    default:
      i = 1;
      j = 128;
      break;
    case 1:
      this.image.addInfo(64);
      i = 1;
      j = 64;
      dispose();
      break;
    case 3:
      i = 1;
      j = 32;
      break;
    case 2:
      i = 0;
      j = 16;
    }
    synchronized (this)
    {
      if (i != 0)
      {
        this.image.getSource().removeConsumer(this);
        this.consuming = false;
        this.newbits = null;
        if (this.bimage != null)
          this.bimage = getOpaqueRGBImage();
      }
      this.availinfo |= j;
      notifyAll();
    }
    newInfo(this.image, j, 0, 0, this.width, this.height);
    this.image.infoDone(paramInt);
  }

  void startProduction()
  {
    if (!(this.consuming))
    {
      this.consuming = true;
      this.image.getSource().startProduction(this);
    }
  }

  private synchronized void checkConsumption()
  {
    if ((isWatcherListEmpty()) && (this.numWaiters == 0) && ((this.availinfo & 0x20) == 0))
      dispose();
  }

  public synchronized void notifyWatcherListEmpty()
  {
    checkConsumption();
  }

  private synchronized void decrementWaiters()
  {
    this.numWaiters -= 1;
    checkConsumption();
  }

  public boolean prepare(ImageObserver paramImageObserver)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x40) != 0)
    {
      if (paramImageObserver != null)
        paramImageObserver.imageUpdate(this.image, 192, -1, -1, -1, -1);
      return false;
    }
    int i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    if (i == 0)
    {
      addWatcher(paramImageObserver);
      startProduction();
      i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    }
    return i;
  }

  public int check(ImageObserver paramImageObserver)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x60) == 0)
      addWatcher(paramImageObserver);
    return this.availinfo;
  }

  public boolean drawToBufImage(Graphics paramGraphics, ToolkitImage paramToolkitImage, int paramInt1, int paramInt2, Color paramColor, ImageObserver paramImageObserver)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x40) != 0)
    {
      if (paramImageObserver != null)
        paramImageObserver.imageUpdate(this.image, 192, -1, -1, -1, -1);
      return false;
    }
    int i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    int j = ((this.availinfo & 0x80) != 0) ? 1 : 0;
    if ((i == 0) && (j == 0))
    {
      addWatcher(paramImageObserver);
      startProduction();
      i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    }
    if ((i != 0) || (0 != (this.availinfo & 0x10)))
      paramGraphics.drawImage(this.bimage, paramInt1, paramInt2, paramColor, null);
    return i;
  }

  public boolean drawToBufImage(Graphics paramGraphics, ToolkitImage paramToolkitImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor, ImageObserver paramImageObserver)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x40) != 0)
    {
      if (paramImageObserver != null)
        paramImageObserver.imageUpdate(this.image, 192, -1, -1, -1, -1);
      return false;
    }
    int i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    int j = ((this.availinfo & 0x80) != 0) ? 1 : 0;
    if ((i == 0) && (j == 0))
    {
      addWatcher(paramImageObserver);
      startProduction();
      i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    }
    if ((i != 0) || (0 != (this.availinfo & 0x10)))
      paramGraphics.drawImage(this.bimage, paramInt1, paramInt2, paramInt3, paramInt4, paramColor, null);
    return i;
  }

  public boolean drawToBufImage(Graphics paramGraphics, ToolkitImage paramToolkitImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, Color paramColor, ImageObserver paramImageObserver)
  {
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x40) != 0)
    {
      if (paramImageObserver != null)
        paramImageObserver.imageUpdate(this.image, 192, -1, -1, -1, -1);
      return false;
    }
    int i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    int j = ((this.availinfo & 0x80) != 0) ? 1 : 0;
    if ((i == 0) && (j == 0))
    {
      addWatcher(paramImageObserver);
      startProduction();
      i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    }
    if ((i != 0) || (0 != (this.availinfo & 0x10)))
      paramGraphics.drawImage(this.bimage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, paramColor, null);
    return i;
  }

  public boolean drawToBufImage(Graphics paramGraphics, ToolkitImage paramToolkitImage, AffineTransform paramAffineTransform, ImageObserver paramImageObserver)
  {
    Graphics2D localGraphics2D = (Graphics2D)paramGraphics;
    if (this.src != null)
      this.src.checkSecurity(null, false);
    if ((this.availinfo & 0x40) != 0)
    {
      if (paramImageObserver != null)
        paramImageObserver.imageUpdate(this.image, 192, -1, -1, -1, -1);
      return false;
    }
    int i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    int j = ((this.availinfo & 0x80) != 0) ? 1 : 0;
    if ((i == 0) && (j == 0))
    {
      addWatcher(paramImageObserver);
      startProduction();
      i = ((this.availinfo & 0x20) != 0) ? 1 : 0;
    }
    if ((i != 0) || (0 != (this.availinfo & 0x10)))
      localGraphics2D.drawImage(this.bimage, paramAffineTransform, null);
    return i;
  }

  synchronized void abort()
  {
    this.image.getSource().removeConsumer(this);
    this.consuming = false;
    this.newbits = null;
    this.bimage = null;
    this.biRaster = null;
    this.cmodel = null;
    this.srcLUT = null;
    this.isDefaultBI = false;
    this.isSameCM = false;
    newInfo(this.image, 128, -1, -1, -1, -1);
    this.availinfo &= -121;
  }

  synchronized void dispose()
  {
    this.image.getSource().removeConsumer(this);
    this.consuming = false;
    this.newbits = null;
    this.availinfo &= -57;
  }

  public void setAccelerationPriority(float paramFloat)
  {
    if (this.bimage != null)
      this.bimage.setAccelerationPriority(paramFloat);
  }

  static
  {
    NativeLibLoader.loadLibraries();
    initIDs();
    s_useNative = true;
  }
}