package sun.awt.image;

import I;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

public class ByteComponentRaster extends SunWritableRaster
{
  protected int bandOffset;
  protected int[] dataOffsets;
  protected int scanlineStride;
  protected int pixelStride;
  protected byte[] data;
  int type;
  private int maxX;
  private int maxY;

  private static native void initIDs();

  public ByteComponentRaster(SampleModel paramSampleModel, Point paramPoint)
  {
    this(paramSampleModel, paramSampleModel.createDataBuffer(), new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public ByteComponentRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Point paramPoint)
  {
    this(paramSampleModel, paramDataBuffer, new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public ByteComponentRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Rectangle paramRectangle, Point paramPoint, ByteComponentRaster paramByteComponentRaster)
  {
    super(paramSampleModel, paramDataBuffer, paramRectangle, paramPoint, paramByteComponentRaster);
    this.maxX = (this.minX + this.width);
    this.maxY = (this.minY + this.height);
    if (!(paramDataBuffer instanceof DataBufferByte))
      throw new RasterFormatException("ByteComponentRasters must have byte DataBuffers");
    DataBufferByte localDataBufferByte = (DataBufferByte)paramDataBuffer;
    this.data = localDataBufferByte.getData();
    if (localDataBufferByte.getNumBanks() != 1)
      throw new RasterFormatException("DataBuffer for ByteComponentRasters must only have 1 bank.");
    int i = localDataBufferByte.getOffset();
    if (paramSampleModel instanceof ComponentSampleModel)
    {
      localObject = (ComponentSampleModel)paramSampleModel;
      this.type = 1;
      this.scanlineStride = ((ComponentSampleModel)localObject).getScanlineStride();
      this.pixelStride = ((ComponentSampleModel)localObject).getPixelStride();
      this.dataOffsets = ((ComponentSampleModel)localObject).getBandOffsets();
      j = paramRectangle.x - paramPoint.x;
      k = paramRectangle.y - paramPoint.y;
      for (int l = 0; l < getNumDataElements(); ++l)
        this.dataOffsets[l] += i + j * this.pixelStride + k * this.scanlineStride;
    }
    else if (paramSampleModel instanceof SinglePixelPackedSampleModel)
    {
      localObject = (SinglePixelPackedSampleModel)paramSampleModel;
      this.type = 7;
      this.scanlineStride = ((SinglePixelPackedSampleModel)localObject).getScanlineStride();
      this.pixelStride = 1;
      this.dataOffsets = new int[1];
      this.dataOffsets[0] = i;
      j = paramRectangle.x - paramPoint.x;
      k = paramRectangle.y - paramPoint.y;
      this.dataOffsets[0] += j * this.pixelStride + k * this.scanlineStride;
    }
    else
    {
      throw new RasterFormatException("IntegerComponentRasters must have ComponentSampleModel or SinglePixelPackedSampleModel");
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

  public byte[] getDataStorage()
  {
    return this.data;
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
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    for (int j = 0; j < this.numDataElements; ++j)
      arrayOfByte[j] = this.data[(this.dataOffsets[j] + i)];
    return arrayOfByte;
  }

  public Object getDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    byte[] arrayOfByte;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramObject == null)
      arrayOfByte = new byte[paramInt3 * paramInt4 * this.numDataElements];
    else
      arrayOfByte = (byte[])(byte[])paramObject;
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
          arrayOfByte[(k++)] = this.data[(this.dataOffsets[i2] + j)];
        ++l;
        j += this.pixelStride;
      }
      ++i1;
      i += this.scanlineStride;
    }
    return arrayOfByte;
  }

  public byte[] getByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfByte == null)
      paramArrayOfByte = new byte[this.scanlineStride * paramInt4];
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride + this.dataOffsets[paramInt5];
    int k = 0;
    if (this.pixelStride == 1)
    {
      if (this.scanlineStride == paramInt3)
      {
        System.arraycopy(this.data, i, paramArrayOfByte, 0, paramInt3 * paramInt4);
        break label247:
      }
      int i1 = 0;
      while (true)
      {
        if (i1 >= paramInt4)
          break label247;
        System.arraycopy(this.data, i, paramArrayOfByte, k, paramInt3);
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
        paramArrayOfByte[(k++)] = this.data[j];
        ++l;
        j += this.pixelStride;
      }
      ++i2;
      i += this.scanlineStride;
    }
    label247: return paramArrayOfByte;
  }

  public byte[] getByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfByte == null)
      paramArrayOfByte = new byte[this.numDataElements * this.scanlineStride * paramInt4];
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
          paramArrayOfByte[(k++)] = this.data[(this.dataOffsets[i2] + j)];
        ++l;
        j += this.pixelStride;
      }
      ++i1;
      i += this.scanlineStride;
    }
    return paramArrayOfByte;
  }

  public void setDataElements(int paramInt1, int paramInt2, Object paramObject)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 >= this.maxX) || (paramInt2 >= this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    byte[] arrayOfByte = (byte[])(byte[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    for (int j = 0; j < this.numDataElements; ++j)
      this.data[(this.dataOffsets[j] + i)] = arrayOfByte[j];
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
    if (paramRaster instanceof ByteComponentRaster)
    {
      ByteComponentRaster localByteComponentRaster = (ByteComponentRaster)paramRaster;
      byte[] arrayOfByte = localByteComponentRaster.getDataStorage();
      if (this.numDataElements == 1)
      {
        int l = localByteComponentRaster.getDataOffset(0);
        int i1 = localByteComponentRaster.getScanlineStride();
        int i2 = l;
        int i3 = this.dataOffsets[0] + (paramInt2 - this.minY) * this.scanlineStride + paramInt1 - this.minX;
        if (this.pixelStride == localByteComponentRaster.getPixelStride())
        {
          paramInt3 *= this.pixelStride;
          for (int i4 = 0; i4 < paramInt4; ++i4)
          {
            System.arraycopy(arrayOfByte, i2, this.data, i3, paramInt3);
            i2 += i1;
            i3 += this.scanlineStride;
          }
          notifyChanged();
          return;
        }
      }
    }
    for (int k = 0; k < paramInt4; ++k)
    {
      localObject = paramRaster.getDataElements(i, j + k, paramInt3, 1, localObject);
      setDataElements(paramInt1, paramInt2 + k, paramInt3, 1, localObject);
    }
    notifyChanged();
  }

  public void setDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    int j;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    byte[] arrayOfByte = (byte[])(byte[])paramObject;
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    int k = 0;
    if (this.numDataElements == 1)
    {
      int i3 = 0;
      int i5 = i + this.dataOffsets[0];
      for (int i1 = 0; i1 < paramInt4; ++i1)
      {
        j = i;
        System.arraycopy(arrayOfByte, i3, this.data, i5, paramInt3);
        i3 += paramInt3;
        i5 += this.scanlineStride;
      }
      notifyChanged();
      return;
    }
    int i2 = 0;
    while (i2 < paramInt4)
    {
      j = i;
      int l = 0;
      while (l < paramInt3)
      {
        for (int i4 = 0; i4 < this.numDataElements; ++i4)
          this.data[(this.dataOffsets[i4] + j)] = arrayOfByte[(k++)];
        ++l;
        j += this.pixelStride;
      }
      ++i2;
      i += this.scanlineStride;
    }
    notifyChanged();
  }

  public void putByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride + this.dataOffsets[paramInt5];
    int k = 0;
    if (this.pixelStride == 1)
    {
      if (this.scanlineStride == paramInt3)
      {
        System.arraycopy(paramArrayOfByte, 0, this.data, i, paramInt3 * paramInt4);
        break label231:
      }
      int i1 = 0;
      while (true)
      {
        if (i1 >= paramInt4)
          break label231;
        System.arraycopy(paramArrayOfByte, k, this.data, i, paramInt3);
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
        this.data[j] = paramArrayOfByte[(k++)];
        ++l;
        j += this.pixelStride;
      }
      ++i2;
      i += this.scanlineStride;
    }
    label231: notifyChanged();
  }

  public void putByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
  {
    int j;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    int k = 0;
    if (this.numDataElements == 1)
    {
      i += this.dataOffsets[0];
      if (this.pixelStride == 1)
      {
        if (this.scanlineStride == paramInt3)
        {
          System.arraycopy(paramArrayOfByte, 0, this.data, i, paramInt3 * paramInt4);
          break label336:
        }
        int i2 = 0;
        while (true)
        {
          if (i2 >= paramInt4)
            break label336;
          System.arraycopy(paramArrayOfByte, k, this.data, i, paramInt3);
          k += paramInt3;
          i += this.scanlineStride;
          ++i2;
        }
      }
      int i3 = 0;
      while (true)
      {
        if (i3 >= paramInt4)
          break label336;
        j = i;
        int l = 0;
        while (l < paramInt3)
        {
          this.data[j] = paramArrayOfByte[(k++)];
          ++l;
          j += this.pixelStride;
        }
        ++i3;
        i += this.scanlineStride;
      }
    }
    int i4 = 0;
    while (i4 < paramInt4)
    {
      j = i;
      int i1 = 0;
      while (i1 < paramInt3)
      {
        for (int i5 = 0; i5 < this.numDataElements; ++i5)
          this.data[(this.dataOffsets[i5] + j)] = paramArrayOfByte[(k++)];
        ++i1;
        j += this.pixelStride;
      }
      ++i4;
      i += this.scanlineStride;
    }
    label336: notifyChanged();
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
    return new ByteComponentRaster(localSampleModel, this.dataBuffer, new Rectangle(paramInt5, paramInt6, paramInt3, paramInt4), new Point(this.sampleModelTranslateX + i, this.sampleModelTranslateY + j), this);
  }

  public WritableRaster createCompatibleWritableRaster(int paramInt1, int paramInt2)
  {
    if ((paramInt1 <= 0) || (paramInt2 <= 0))
      throw new RasterFormatException("negative " + "height");
    SampleModel localSampleModel = this.sampleModel.createCompatibleSampleModel(paramInt1, paramInt2);
    return new ByteComponentRaster(localSampleModel, new Point(0, 0));
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
      int j = (this.height - 1) * this.scanlineStride + (this.width - 1) * this.pixelStride + this.dataOffsets[k];
      if (j > i)
        i = j;
    }
    if (this.data.length < i)
      throw new RasterFormatException("Data array too small (should be " + i + " )");
  }

  public String toString()
  {
    return new String("ByteComponentRaster: width = " + this.width + " height = " + this.height + " #numDataElements " + this.numDataElements + " dataOff[0] = " + this.dataOffsets[0]);
  }

  static
  {
    NativeLibLoader.loadLibraries();
    initIDs();
  }
}