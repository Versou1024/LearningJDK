package sun.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class SunWritableRaster extends WritableRaster
{
  protected RasterListener listener;
  private boolean isStolen;

  public SunWritableRaster(SampleModel paramSampleModel, Point paramPoint)
  {
    super(paramSampleModel, paramPoint);
    this.isStolen = false;
  }

  public SunWritableRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Point paramPoint)
  {
    super(paramSampleModel, paramDataBuffer, paramPoint);
    this.isStolen = true;
  }

  public SunWritableRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Rectangle paramRectangle, Point paramPoint, WritableRaster paramWritableRaster)
  {
    super(paramSampleModel, paramDataBuffer, paramRectangle, paramPoint, paramWritableRaster);
    this.isStolen = true;
  }

  public void setRasterListener(RasterListener paramRasterListener)
  {
    boolean bool;
    if (paramRasterListener == null)
      return;
    synchronized (this)
    {
      if (this.listener == null)
      {
        this.listener = paramRasterListener;
        bool = this.isStolen;
      }
      else
      {
        bool = true;
      }
    }
    if (bool)
      paramRasterListener.rasterStolen();
  }

  public void notifyChanged()
  {
    if (this.listener != null)
      this.listener.rasterChanged();
  }

  public void notifyStolen()
  {
    setStolen(true);
  }

  public void setStolen(boolean paramBoolean)
  {
    this.isStolen = paramBoolean;
    if ((this.listener != null) && (this.isStolen))
      this.listener.rasterStolen();
  }

  public DataBuffer getDataBuffer()
  {
    notifyStolen();
    return super.getDataBuffer();
  }

  public void setDataElements(int paramInt1, int paramInt2, Object paramObject)
  {
    super.setDataElements(paramInt1, paramInt2, paramObject);
    notifyChanged();
  }

  public void setDataElements(int paramInt1, int paramInt2, Raster paramRaster)
  {
    super.setDataElements(paramInt1, paramInt2, paramRaster);
    notifyChanged();
  }

  public void setDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    super.setDataElements(paramInt1, paramInt2, paramInt3, paramInt4, paramObject);
    notifyChanged();
  }

  public void setRect(Raster paramRaster)
  {
    super.setRect(paramRaster);
    notifyChanged();
  }

  public void setRect(int paramInt1, int paramInt2, Raster paramRaster)
  {
    super.setRect(paramInt1, paramInt2, paramRaster);
    notifyChanged();
  }

  public void setPixel(int paramInt1, int paramInt2, int[] paramArrayOfInt)
  {
    super.setPixel(paramInt1, paramInt2, paramArrayOfInt);
    notifyChanged();
  }

  public void setPixel(int paramInt1, int paramInt2, float[] paramArrayOfFloat)
  {
    super.setPixel(paramInt1, paramInt2, paramArrayOfFloat);
    notifyChanged();
  }

  public void setPixel(int paramInt1, int paramInt2, double[] paramArrayOfDouble)
  {
    super.setPixel(paramInt1, paramInt2, paramArrayOfDouble);
    notifyChanged();
  }

  public void setPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt)
  {
    super.setPixels(paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfInt);
    notifyChanged();
  }

  public void setPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, float[] paramArrayOfFloat)
  {
    super.setPixels(paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfFloat);
    notifyChanged();
  }

  public void setPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, double[] paramArrayOfDouble)
  {
    super.setPixels(paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfDouble);
    notifyChanged();
  }

  public void setSample(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.setSample(paramInt1, paramInt2, paramInt3, paramInt4);
    notifyChanged();
  }

  public void setSample(int paramInt1, int paramInt2, int paramInt3, float paramFloat)
  {
    super.setSample(paramInt1, paramInt2, paramInt3, paramFloat);
    notifyChanged();
  }

  public void setSample(int paramInt1, int paramInt2, int paramInt3, double paramDouble)
  {
    super.setSample(paramInt1, paramInt2, paramInt3, paramDouble);
    notifyChanged();
  }

  public void setSamples(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int[] paramArrayOfInt)
  {
    super.setSamples(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramArrayOfInt);
    notifyChanged();
  }

  public void setSamples(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, float[] paramArrayOfFloat)
  {
    super.setSamples(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramArrayOfFloat);
    notifyChanged();
  }

  public void setSamples(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, double[] paramArrayOfDouble)
  {
    super.setSamples(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramArrayOfDouble);
    notifyChanged();
  }
}