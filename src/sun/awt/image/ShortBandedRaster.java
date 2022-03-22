package sun.awt.image;

import I;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class ShortBandedRaster extends SunWritableRaster
{
  int[] dataOffsets;
  int scanlineStride;
  short[][] data;
  private int maxX;
  private int maxY;

  public ShortBandedRaster(SampleModel paramSampleModel, Point paramPoint)
  {
    this(paramSampleModel, paramSampleModel.createDataBuffer(), new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public ShortBandedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Point paramPoint)
  {
    this(paramSampleModel, paramDataBuffer, new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public ShortBandedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Rectangle paramRectangle, Point paramPoint, ShortBandedRaster paramShortBandedRaster)
  {
    super(paramSampleModel, paramDataBuffer, paramRectangle, paramPoint, paramShortBandedRaster);
    this.maxX = (this.minX + this.width);
    this.maxY = (this.minY + this.height);
    if (!(paramDataBuffer instanceof DataBufferUShort))
      throw new RasterFormatException("ShortBandedRaster must have ushort DataBuffers");
    DataBufferUShort localDataBufferUShort = (DataBufferUShort)paramDataBuffer;
    if (paramSampleModel instanceof BandedSampleModel)
    {
      BandedSampleModel localBandedSampleModel = (BandedSampleModel)paramSampleModel;
      this.scanlineStride = localBandedSampleModel.getScanlineStride();
      int[] arrayOfInt1 = localBandedSampleModel.getBankIndices();
      int[] arrayOfInt2 = localBandedSampleModel.getBandOffsets();
      int[] arrayOfInt3 = localDataBufferUShort.getOffsets();
      this.dataOffsets = new int[arrayOfInt1.length];
      this.data = new short[arrayOfInt1.length][];
      int i = paramRectangle.x - paramPoint.x;
      int j = paramRectangle.y - paramPoint.y;
      for (int k = 0; k < arrayOfInt1.length; ++k)
      {
        this.data[k] = localDataBufferUShort.getData(arrayOfInt1[k]);
        this.dataOffsets[k] = (arrayOfInt3[arrayOfInt1[k]] + i + j * this.scanlineStride + arrayOfInt2[k]);
      }
    }
    else
    {
      throw new RasterFormatException("ShortBandedRasters must have BandedSampleModels");
    }
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
    return 1;
  }

  public short[][] getDataStorage()
  {
    return this.data;
  }

  public short[] getDataStorage(int paramInt)
  {
    return this.data[paramInt];
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
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
      arrayOfShort[j] = this.data[j][(this.dataOffsets[j] + i)];
    return arrayOfShort;
  }

  public Object getDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    short[] arrayOfShort1;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramObject == null)
      arrayOfShort1 = new short[this.numDataElements * paramInt3 * paramInt4];
    else
      arrayOfShort1 = (short[])(short[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
    {
      int k = j;
      short[] arrayOfShort2 = this.data[j];
      int l = this.dataOffsets[j];
      int i1 = i;
      int i2 = 0;
      while (i2 < paramInt4)
      {
        int i3 = l + i1;
        for (int i4 = 0; i4 < paramInt3; ++i4)
        {
          arrayOfShort1[k] = arrayOfShort2[(i3++)];
          k += this.numDataElements;
        }
        ++i2;
        i1 += this.scanlineStride;
      }
    }
    return arrayOfShort1;
  }

  public short[] getShortData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, short[] paramArrayOfShort)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfShort == null)
      paramArrayOfShort = new short[this.scanlineStride * paramInt4];
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX + this.dataOffsets[paramInt5];
    if (this.scanlineStride == paramInt3)
    {
      System.arraycopy(this.data[paramInt5], i, paramArrayOfShort, 0, paramInt3 * paramInt4);
    }
    else
    {
      int j = 0;
      int k = 0;
      while (k < paramInt4)
      {
        System.arraycopy(this.data[paramInt5], i, paramArrayOfShort, j, paramInt3);
        j += paramInt3;
        ++k;
        i += this.scanlineStride;
      }
    }
    return paramArrayOfShort;
  }

  public short[] getShortData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, short[] paramArrayOfShort)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfShort == null)
      paramArrayOfShort = new short[this.numDataElements * this.scanlineStride * paramInt4];
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
    {
      int k = j;
      short[] arrayOfShort = this.data[j];
      int l = this.dataOffsets[j];
      int i1 = i;
      int i2 = 0;
      while (i2 < paramInt4)
      {
        int i3 = l + i1;
        for (int i4 = 0; i4 < paramInt3; ++i4)
        {
          paramArrayOfShort[k] = arrayOfShort[(i3++)];
          k += this.numDataElements;
        }
        ++i2;
        i1 += this.scanlineStride;
      }
    }
    return paramArrayOfShort;
  }

  public void setDataElements(int paramInt1, int paramInt2, Object paramObject)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 >= this.maxX) || (paramInt2 >= this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    short[] arrayOfShort = (short[])(short[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
      this.data[j][(this.dataOffsets[j] + i)] = arrayOfShort[j];
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
    short[] arrayOfShort1 = (short[])(short[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
    {
      int k = j;
      short[] arrayOfShort2 = this.data[j];
      int l = this.dataOffsets[j];
      int i1 = i;
      int i2 = 0;
      while (i2 < paramInt4)
      {
        int i3 = l + i1;
        for (int i4 = 0; i4 < paramInt3; ++i4)
        {
          arrayOfShort2[(i3++)] = arrayOfShort1[k];
          k += this.numDataElements;
        }
        ++i2;
        i1 += this.scanlineStride;
      }
    }
    notifyChanged();
  }

  public void putShortData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, short[] paramArrayOfShort)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX + this.dataOffsets[paramInt5];
    int j = 0;
    if (this.scanlineStride == paramInt3)
    {
      System.arraycopy(paramArrayOfShort, 0, this.data[paramInt5], i, paramInt3 * paramInt4);
    }
    else
    {
      int k = 0;
      while (k < paramInt4)
      {
        System.arraycopy(paramArrayOfShort, j, this.data[paramInt5], i, paramInt3);
        j += paramInt3;
        ++k;
        i += this.scanlineStride;
      }
    }
    notifyChanged();
  }

  public void putShortData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, short[] paramArrayOfShort)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
    {
      int k = j;
      short[] arrayOfShort = this.data[j];
      int l = this.dataOffsets[j];
      int i1 = i;
      int i2 = 0;
      while (i2 < paramInt4)
      {
        int i3 = l + i1;
        for (int i4 = 0; i4 < paramInt3; ++i4)
        {
          arrayOfShort[(i3++)] = paramArrayOfShort[k];
          k += this.numDataElements;
        }
        ++i2;
        i1 += this.scanlineStride;
      }
    }
    notifyChanged();
  }

  public WritableRaster createWritableChild(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int[] paramArrayOfInt)
  {
    SampleModel localSampleModel;
    if (paramInt1 < this.minX)
      throw new RasterFormatException("x lies outside raster");
    if (paramInt2 < this.minY)
      throw new RasterFormatException("y lies outside raster");
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
    return new ShortBandedRaster(localSampleModel, this.dataBuffer, new Rectangle(paramInt5, paramInt6, paramInt3, paramInt4), new Point(this.sampleModelTranslateX + i, this.sampleModelTranslateY + j), this);
  }

  public Raster createChild(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int[] paramArrayOfInt)
  {
    return createWritableChild(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramArrayOfInt);
  }

  public WritableRaster createCompatibleWritableRaster(int paramInt1, int paramInt2)
  {
    if ((paramInt1 <= 0) || (paramInt2 <= 0))
      throw new RasterFormatException("negative " + "height");
    SampleModel localSampleModel = this.sampleModel.createCompatibleSampleModel(paramInt1, paramInt2);
    return new ShortBandedRaster(localSampleModel, new Point(0, 0));
  }

  public WritableRaster createCompatibleWritableRaster()
  {
    return createCompatibleWritableRaster(this.width, this.height);
  }

  private void verify(boolean paramBoolean)
  {
    for (int i = 0; i < this.dataOffsets.length; ++i)
      if (this.dataOffsets[i] < 0)
        throw new RasterFormatException("Data offsets for band " + i + "(" + this.dataOffsets[i] + ") must be >= 0");
    i = 0;
    for (int k = 0; k < this.numDataElements; ++k)
    {
      int j = (this.height - 1) * this.scanlineStride + this.width - 1 + this.dataOffsets[k];
      if (j > i)
        i = j;
    }
    for (k = 0; k < this.numDataElements; ++k)
      if (this.data[k].length < i)
        throw new RasterFormatException("Data array too small (should be " + i + " )");
  }

  public String toString()
  {
    return new String("ShortBandedRaster: width = " + this.width + " height = " + this.height + " #numBands " + this.numBands + " #dataElements " + this.numDataElements);
  }
}