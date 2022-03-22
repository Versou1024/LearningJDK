package sun.awt.image;

import I;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

public class ShortInterleavedRaster extends ShortComponentRaster
{
  private int maxX;
  private int maxY;

  public ShortInterleavedRaster(SampleModel paramSampleModel, Point paramPoint)
  {
    this(paramSampleModel, paramSampleModel.createDataBuffer(), new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public ShortInterleavedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Point paramPoint)
  {
    this(paramSampleModel, paramDataBuffer, new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public ShortInterleavedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Rectangle paramRectangle, Point paramPoint, ShortInterleavedRaster paramShortInterleavedRaster)
  {
    super(paramSampleModel, paramDataBuffer, paramRectangle, paramPoint, paramShortInterleavedRaster);
    this.maxX = (this.minX + this.width);
    this.maxY = (this.minY + this.height);
    if (!(paramDataBuffer instanceof DataBufferUShort))
      throw new RasterFormatException("ShortInterleavedRasters must have ushort DataBuffers");
    DataBufferUShort localDataBufferUShort = (DataBufferUShort)paramDataBuffer;
    this.data = localDataBufferUShort.getData();
    if ((paramSampleModel instanceof PixelInterleavedSampleModel) || ((paramSampleModel instanceof ComponentSampleModel) && (paramSampleModel.getNumBands() == 1)))
    {
      localObject = (ComponentSampleModel)paramSampleModel;
      this.scanlineStride = ((ComponentSampleModel)localObject).getScanlineStride();
      this.pixelStride = ((ComponentSampleModel)localObject).getPixelStride();
      this.dataOffsets = ((ComponentSampleModel)localObject).getBandOffsets();
      i = paramRectangle.x - paramPoint.x;
      j = paramRectangle.y - paramPoint.y;
      for (int k = 0; k < getNumDataElements(); ++k)
        this.dataOffsets[k] += i * this.pixelStride + j * this.scanlineStride;
    }
    else if (paramSampleModel instanceof SinglePixelPackedSampleModel)
    {
      localObject = (SinglePixelPackedSampleModel)paramSampleModel;
      this.scanlineStride = ((SinglePixelPackedSampleModel)localObject).getScanlineStride();
      this.pixelStride = 1;
      this.dataOffsets = new int[1];
      this.dataOffsets[0] = localDataBufferUShort.getOffset();
      i = paramRectangle.x - paramPoint.x;
      j = paramRectangle.y - paramPoint.y;
      this.dataOffsets[0] += i + j * this.scanlineStride;
    }
    else
    {
      throw new RasterFormatException("ShortInterleavedRasters must have PixelInterleavedSampleModel, SinglePixelPackedSampleModel or 1 band ComponentSampleModel.  Sample model is " + paramSampleModel);
    }
    this.bandOffset = this.dataOffsets[0];
    verify(false);
  }

  public int[] getDataOffsets()
  {
    return ((int[])(int[])this.dataOffsets.clone());
  }

  public int getDataOffset(int paramInt)
  {
    return this.dataOffsets[paramInt];
  }

  public int getScanlineStride()
  {
    return this.scanlineStride;
  }

  public int getPixelStride()
  {
    return this.pixelStride;
  }

  public short[] getDataStorage()
  {
    return this.data;
  }

  public Object getDataElements(int paramInt1, int paramInt2, Object paramObject)
  {
    short[] arrayOfShort;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 >= this.maxX) || (paramInt2 >= this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramObject == null)
      arrayOfShort = new short[this.numDataElements];
    else
      arrayOfShort = (short[])(short[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    for (int j = 0; j < this.numDataElements; ++j)
      arrayOfShort[j] = this.data[(this.dataOffsets[j] + i)];
    return arrayOfShort;
  }

  public Object getDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    short[] arrayOfShort;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramObject == null)
      arrayOfShort = new short[paramInt3 * paramInt4 * this.numDataElements];
    else
      arrayOfShort = (short[])(short[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    int k = 0;
    int i1 = 0;
    while (i1 < paramInt4)
    {
      int j = i;
      int l = 0;
      while (l < paramInt3)
      {
        for (int i2 = 0; i2 < this.numDataElements; ++i2)
          arrayOfShort[(k++)] = this.data[(this.dataOffsets[i2] + j)];
        ++l;
        j += this.pixelStride;
      }
      ++i1;
      i += this.scanlineStride;
    }
    return arrayOfShort;
  }

  public short[] getShortData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, short[] paramArrayOfShort)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfShort == null)
      paramArrayOfShort = new short[this.numDataElements * paramInt3 * paramInt4];
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride + this.dataOffsets[paramInt5];
    int k = 0;
    if (this.pixelStride == 1)
    {
      if (this.scanlineStride == paramInt3)
      {
        System.arraycopy(this.data, i, paramArrayOfShort, 0, paramInt3 * paramInt4);
        break label249:
      }
      int i1 = 0;
      while (true)
      {
        if (i1 >= paramInt4)
          break label249;
        System.arraycopy(this.data, i, paramArrayOfShort, k, paramInt3);
        k += paramInt3;
        ++i1;
        i += this.scanlineStride;
      }
    }
    int i2 = 0;
    while (i2 < paramInt4)
    {
      int j = i;
      int l = 0;
      while (l < paramInt3)
      {
        paramArrayOfShort[(k++)] = this.data[j];
        ++l;
        j += this.pixelStride;
      }
      ++i2;
      i += this.scanlineStride;
    }
    label249: return paramArrayOfShort;
  }

  public short[] getShortData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, short[] paramArrayOfShort)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfShort == null)
      paramArrayOfShort = new short[this.numDataElements * paramInt3 * paramInt4];
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    int k = 0;
    int i1 = 0;
    while (i1 < paramInt4)
    {
      int j = i;
      int l = 0;
      while (l < paramInt3)
      {
        for (int i2 = 0; i2 < this.numDataElements; ++i2)
          paramArrayOfShort[(k++)] = this.data[(this.dataOffsets[i2] + j)];
        ++l;
        j += this.pixelStride;
      }
      ++i1;
      i += this.scanlineStride;
    }
    return paramArrayOfShort;
  }

  public void setDataElements(int paramInt1, int paramInt2, Object paramObject)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 >= this.maxX) || (paramInt2 >= this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    short[] arrayOfShort = (short[])(short[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    for (int j = 0; j < this.numDataElements; ++j)
      this.data[(this.dataOffsets[j] + i)] = arrayOfShort[j];
    notifyChanged();
  }

  public void setDataElements(int paramInt1, int paramInt2, Raster paramRaster)
  {
    int i = paramInt1 + paramRaster.getMinX();
    int j = paramInt2 + paramRaster.getMinY();
    int k = paramRaster.getWidth();
    int l = paramRaster.getHeight();
    if ((i < this.minX) || (j < this.minY) || (i + k > this.maxX) || (j + l > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    setDataElements(i, j, k, l, paramRaster);
  }

  private void setDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Raster paramRaster)
  {
    if ((paramInt3 <= 0) || (paramInt4 <= 0))
      return;
    int i = paramRaster.getMinX();
    int j = paramRaster.getMinY();
    Object localObject = null;
    for (int k = 0; k < paramInt4; ++k)
    {
      localObject = paramRaster.getDataElements(i, j + k, paramInt3, 1, localObject);
      setDataElements(paramInt1, paramInt2 + k, paramInt3, 1, localObject);
    }
  }

  public void setDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    short[] arrayOfShort = (short[])(short[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    int k = 0;
    int i1 = 0;
    while (i1 < paramInt4)
    {
      int j = i;
      int l = 0;
      while (l < paramInt3)
      {
        for (int i2 = 0; i2 < this.numDataElements; ++i2)
          this.data[(this.dataOffsets[i2] + j)] = arrayOfShort[(k++)];
        ++l;
        j += this.pixelStride;
      }
      ++i1;
      i += this.scanlineStride;
    }
    notifyChanged();
  }

  public void putShortData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, short[] paramArrayOfShort)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride + this.dataOffsets[paramInt5];
    int k = 0;
    if (this.pixelStride == 1)
    {
      if (this.scanlineStride == paramInt3)
      {
        System.arraycopy(paramArrayOfShort, 0, this.data, i, paramInt3 * paramInt4);
        break label231:
      }
      int i1 = 0;
      while (true)
      {
        if (i1 >= paramInt4)
          break label231;
        System.arraycopy(paramArrayOfShort, k, this.data, i, paramInt3);
        k += paramInt3;
        ++i1;
        i += this.scanlineStride;
      }
    }
    int i2 = 0;
    while (i2 < paramInt4)
    {
      int j = i;
      int l = 0;
      while (l < paramInt3)
      {
        this.data[j] = paramArrayOfShort[(k++)];
        ++l;
        j += this.pixelStride;
      }
      ++i2;
      i += this.scanlineStride;
    }
    label231: notifyChanged();
  }

  public void putShortData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, short[] paramArrayOfShort)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    int k = 0;
    int i1 = 0;
    while (i1 < paramInt4)
    {
      int j = i;
      int l = 0;
      while (l < paramInt3)
      {
        for (int i2 = 0; i2 < this.numDataElements; ++i2)
          this.data[(this.dataOffsets[i2] + j)] = paramArrayOfShort[(k++)];
        ++l;
        j += this.pixelStride;
      }
      ++i1;
      i += this.scanlineStride;
    }
    notifyChanged();
  }

  public Raster createChild(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int[] paramArrayOfInt)
  {
    WritableRaster localWritableRaster = createWritableChild(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramArrayOfInt);
    return localWritableRaster;
  }

  public WritableRaster createWritableChild(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int[] paramArrayOfInt)
  {
    SampleModel localSampleModel;
    if (paramInt1 < this.minX)
      throw new RasterFormatException("x lies outside the raster");
    if (paramInt2 < this.minY)
      throw new RasterFormatException("y lies outside the raster");
    if ((paramInt1 + paramInt3 < paramInt1) || (paramInt1 + paramInt3 > this.minX + this.width))
      throw new RasterFormatException("(x + width) is outside of Raster");
    if ((paramInt2 + paramInt4 < paramInt2) || (paramInt2 + paramInt4 > this.minY + this.height))
      throw new RasterFormatException("(y + height) is outside of Raster");
    if (paramArrayOfInt != null)
      localSampleModel = this.sampleModel.createSubsetSampleModel(paramArrayOfInt);
    else
      localSampleModel = this.sampleModel;
    int i = paramInt5 - paramInt1;
    int j = paramInt6 - paramInt2;
    notifyStolen();
    return new ShortInterleavedRaster(localSampleModel, this.dataBuffer, new Rectangle(paramInt5, paramInt6, paramInt3, paramInt4), new Point(this.sampleModelTranslateX + i, this.sampleModelTranslateY + j), this);
  }

  public WritableRaster createCompatibleWritableRaster(int paramInt1, int paramInt2)
  {
    if ((paramInt1 <= 0) || (paramInt2 <= 0))
      throw new RasterFormatException("negative " + "height");
    SampleModel localSampleModel = this.sampleModel.createCompatibleSampleModel(paramInt1, paramInt2);
    return new ShortInterleavedRaster(localSampleModel, new Point(0, 0));
  }

  public WritableRaster createCompatibleWritableRaster()
  {
    return createCompatibleWritableRaster(this.width, this.height);
  }

  private void verify(boolean paramBoolean)
  {
    int i = 0;
    for (int k = 0; k < this.numDataElements; ++k)
    {
      int j = (this.height - 1) * this.scanlineStride + (this.width - 1) * this.pixelStride + this.dataOffsets[k];
      if (j > i)
        i = j;
    }
    if (this.data.length < i)
      throw new RasterFormatException("Data array too small (should be " + i + " )");
  }

  public String toString()
  {
    return new String("ShortInterleavedRaster: width = " + this.width + " height = " + this.height + " #numDataElements " + this.numDataElements);
  }
}