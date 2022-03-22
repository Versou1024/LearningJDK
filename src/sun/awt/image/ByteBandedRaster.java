package sun.awt.image;

import I;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class ByteBandedRaster extends SunWritableRaster
{
  int[] dataOffsets;
  int scanlineStride;
  byte[][] data;
  private int maxX;
  private int maxY;

  public ByteBandedRaster(SampleModel paramSampleModel, Point paramPoint)
  {
    this(paramSampleModel, paramSampleModel.createDataBuffer(), new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public ByteBandedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Point paramPoint)
  {
    this(paramSampleModel, paramDataBuffer, new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public ByteBandedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Rectangle paramRectangle, Point paramPoint, ByteBandedRaster paramByteBandedRaster)
  {
    super(paramSampleModel, paramDataBuffer, paramRectangle, paramPoint, paramByteBandedRaster);
    this.maxX = (this.minX + this.width);
    this.maxY = (this.minY + this.height);
    if (!(paramDataBuffer instanceof DataBufferByte))
      throw new RasterFormatException("ByteBandedRaster must havebyte DataBuffers");
    DataBufferByte localDataBufferByte = (DataBufferByte)paramDataBuffer;
    if (paramSampleModel instanceof BandedSampleModel)
    {
      BandedSampleModel localBandedSampleModel = (BandedSampleModel)paramSampleModel;
      this.scanlineStride = localBandedSampleModel.getScanlineStride();
      int[] arrayOfInt1 = localBandedSampleModel.getBankIndices();
      int[] arrayOfInt2 = localBandedSampleModel.getBandOffsets();
      int[] arrayOfInt3 = localDataBufferByte.getOffsets();
      this.dataOffsets = new int[arrayOfInt1.length];
      this.data = new byte[arrayOfInt1.length][];
      int i = paramRectangle.x - paramPoint.x;
      int j = paramRectangle.y - paramPoint.y;
      for (int k = 0; k < arrayOfInt1.length; ++k)
      {
        this.data[k] = localDataBufferByte.getData(arrayOfInt1[k]);
        this.dataOffsets[k] = (arrayOfInt3[arrayOfInt1[k]] + i + j * this.scanlineStride + arrayOfInt2[k]);
      }
    }
    else
    {
      throw new RasterFormatException("ByteBandedRasters must haveBandedSampleModels");
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

  public byte[][] getDataStorage()
  {
    return this.data;
  }

  public byte[] getDataStorage(int paramInt)
  {
    return this.data[paramInt];
  }

  public Object getDataElements(int paramInt1, int paramInt2, Object paramObject)
  {
    byte[] arrayOfByte;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 >= this.maxX) || (paramInt2 >= this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramObject == null)
      arrayOfByte = new byte[this.numDataElements];
    else
      arrayOfByte = (byte[])(byte[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
      arrayOfByte[j] = this.data[j][(this.dataOffsets[j] + i)];
    return arrayOfByte;
  }

  public Object getDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    byte[] arrayOfByte1;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramObject == null)
      arrayOfByte1 = new byte[this.numDataElements * paramInt3 * paramInt4];
    else
      arrayOfByte1 = (byte[])(byte[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
    {
      int k = j;
      byte[] arrayOfByte2 = this.data[j];
      int l = this.dataOffsets[j];
      int i1 = i;
      int i2 = 0;
      while (i2 < paramInt4)
      {
        int i3 = l + i1;
        for (int i4 = 0; i4 < paramInt3; ++i4)
        {
          arrayOfByte1[k] = arrayOfByte2[(i3++)];
          k += this.numDataElements;
        }
        ++i2;
        i1 += this.scanlineStride;
      }
    }
    return arrayOfByte1;
  }

  public byte[] getByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfByte == null)
      paramArrayOfByte = new byte[this.scanlineStride * paramInt4];
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX + this.dataOffsets[paramInt5];
    if (this.scanlineStride == paramInt3)
    {
      System.arraycopy(this.data[paramInt5], i, paramArrayOfByte, 0, paramInt3 * paramInt4);
    }
    else
    {
      int j = 0;
      int k = 0;
      while (k < paramInt4)
      {
        System.arraycopy(this.data[paramInt5], i, paramArrayOfByte, j, paramInt3);
        j += paramInt3;
        ++k;
        i += this.scanlineStride;
      }
    }
    return paramArrayOfByte;
  }

  public byte[] getByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfByte == null)
      paramArrayOfByte = new byte[this.numDataElements * this.scanlineStride * paramInt4];
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
    {
      int k = j;
      byte[] arrayOfByte = this.data[j];
      int l = this.dataOffsets[j];
      int i1 = i;
      int i2 = 0;
      while (i2 < paramInt4)
      {
        int i3 = l + i1;
        for (int i4 = 0; i4 < paramInt3; ++i4)
        {
          paramArrayOfByte[k] = arrayOfByte[(i3++)];
          k += this.numDataElements;
        }
        ++i2;
        i1 += this.scanlineStride;
      }
    }
    return paramArrayOfByte;
  }

  public void setDataElements(int paramInt1, int paramInt2, Object paramObject)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 >= this.maxX) || (paramInt2 >= this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    byte[] arrayOfByte = (byte[])(byte[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
      this.data[j][(this.dataOffsets[j] + i)] = arrayOfByte[j];
    notifyChanged();
  }

  public void setDataElements(int paramInt1, int paramInt2, Raster paramRaster)
  {
    int i = paramRaster.getMinX() + paramInt1;
    int j = paramRaster.getMinY() + paramInt2;
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
    notifyChanged();
  }

  public void setDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    byte[] arrayOfByte1 = (byte[])(byte[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
    {
      int k = j;
      byte[] arrayOfByte2 = this.data[j];
      int l = this.dataOffsets[j];
      int i1 = i;
      int i2 = 0;
      while (i2 < paramInt4)
      {
        int i3 = l + i1;
        for (int i4 = 0; i4 < paramInt3; ++i4)
        {
          arrayOfByte2[(i3++)] = arrayOfByte1[k];
          k += this.numDataElements;
        }
        ++i2;
        i1 += this.scanlineStride;
      }
    }
    notifyChanged();
  }

  public void putByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX + this.dataOffsets[paramInt5];
    int j = 0;
    if (this.scanlineStride == paramInt3)
    {
      System.arraycopy(paramArrayOfByte, 0, this.data[paramInt5], i, paramInt3 * paramInt4);
    }
    else
    {
      int k = 0;
      while (k < paramInt4)
      {
        System.arraycopy(paramArrayOfByte, j, this.data[paramInt5], i, paramInt3);
        j += paramInt3;
        ++k;
        i += this.scanlineStride;
      }
    }
    notifyChanged();
  }

  public void putByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
    for (int j = 0; j < this.numDataElements; ++j)
    {
      int k = j;
      byte[] arrayOfByte = this.data[j];
      int l = this.dataOffsets[j];
      int i1 = i;
      int i2 = 0;
      while (i2 < paramInt4)
      {
        int i3 = l + i1;
        for (int i4 = 0; i4 < paramInt3; ++i4)
        {
          arrayOfByte[(i3++)] = paramArrayOfByte[k];
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
    if ((paramInt1 + paramInt3 < paramInt1) || (paramInt1 + paramInt3 > this.width + this.minX))
      throw new RasterFormatException("(x + width) is outside raster");
    if ((paramInt2 + paramInt4 < paramInt2) || (paramInt2 + paramInt4 > this.height + this.minY))
      throw new RasterFormatException("(y + height) is outside raster");
    if (paramArrayOfInt != null)
      localSampleModel = this.sampleModel.createSubsetSampleModel(paramArrayOfInt);
    else
      localSampleModel = this.sampleModel;
    int i = paramInt5 - paramInt1;
    int j = paramInt6 - paramInt2;
    notifyStolen();
    return new ByteBandedRaster(localSampleModel, this.dataBuffer, new Rectangle(paramInt5, paramInt6, paramInt3, paramInt4), new Point(this.sampleModelTranslateX + i, this.sampleModelTranslateY + j), this);
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
    return new ByteBandedRaster(localSampleModel, new Point(0, 0));
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
    if (this.data.length == 1)
    {
      if (this.data[0].length >= i * this.numDataElements)
        return;
      throw new RasterFormatException("Data array too small (it is " + this.data[0].length + " and should be " + (i * this.numDataElements) + " )");
    }
    for (k = 0; k < this.numDataElements; ++k)
      if (this.data[k].length < i)
        throw new RasterFormatException("Data array too small (it is " + this.data[k].length + " and should be " + i + " )");
  }

  public String toString()
  {
    return new String("ByteBandedRaster: width = " + this.width + " height = " + this.height + " #bands " + this.numDataElements + " minX = " + this.minX + " minY = " + this.minY);
  }
}