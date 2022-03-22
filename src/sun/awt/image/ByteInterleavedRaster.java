package sun.awt.image;

import I;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

public class ByteInterleavedRaster extends ByteComponentRaster
{
  boolean inOrder;
  int dbOffset;
  int dbOffsetPacked;
  boolean packed;
  int[] bitMasks;
  int[] bitOffsets;
  private int maxX;
  private int maxY;

  public ByteInterleavedRaster(SampleModel paramSampleModel, Point paramPoint)
  {
    this(paramSampleModel, paramSampleModel.createDataBuffer(), new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public ByteInterleavedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Point paramPoint)
  {
    this(paramSampleModel, paramDataBuffer, new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  private boolean isInterleaved(ComponentSampleModel paramComponentSampleModel)
  {
    int i = this.sampleModel.getNumBands();
    if (i == 1)
      return true;
    int[] arrayOfInt1 = paramComponentSampleModel.getBankIndices();
    for (int j = 0; j < i; ++j)
      if (arrayOfInt1[j] != 0)
        return false;
    int[] arrayOfInt2 = paramComponentSampleModel.getBandOffsets();
    int k = arrayOfInt2[0];
    int l = k;
    for (int i1 = 1; i1 < i; ++i1)
    {
      int i2 = arrayOfInt2[i1];
      if (i2 < k)
        k = i2;
      if (i2 > l)
        l = i2;
    }
    return (l - k < paramComponentSampleModel.getPixelStride());
  }

  public ByteInterleavedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Rectangle paramRectangle, Point paramPoint, ByteInterleavedRaster paramByteInterleavedRaster)
  {
    super(paramSampleModel, paramDataBuffer, paramRectangle, paramPoint, paramByteInterleavedRaster);
    this.packed = false;
    this.maxX = (this.minX + this.width);
    this.maxY = (this.minY + this.height);
    if (!(paramDataBuffer instanceof DataBufferByte))
      throw new RasterFormatException("ByteInterleavedRasters must have byte DataBuffers");
    DataBufferByte localDataBufferByte = (DataBufferByte)paramDataBuffer;
    this.data = localDataBufferByte.getData();
    int i = paramRectangle.x - paramPoint.x;
    int j = paramRectangle.y - paramPoint.y;
    if ((paramSampleModel instanceof PixelInterleavedSampleModel) || ((paramSampleModel instanceof ComponentSampleModel) && (isInterleaved((ComponentSampleModel)paramSampleModel))))
    {
      localObject = (ComponentSampleModel)paramSampleModel;
      this.scanlineStride = ((ComponentSampleModel)localObject).getScanlineStride();
      this.pixelStride = ((ComponentSampleModel)localObject).getPixelStride();
      this.dataOffsets = ((ComponentSampleModel)localObject).getBandOffsets();
      for (int l = 0; l < getNumDataElements(); ++l)
        this.dataOffsets[l] += i * this.pixelStride + j * this.scanlineStride;
    }
    else if (paramSampleModel instanceof SinglePixelPackedSampleModel)
    {
      localObject = (SinglePixelPackedSampleModel)paramSampleModel;
      this.packed = true;
      this.bitMasks = ((SinglePixelPackedSampleModel)localObject).getBitMasks();
      this.bitOffsets = ((SinglePixelPackedSampleModel)localObject).getBitOffsets();
      this.scanlineStride = ((SinglePixelPackedSampleModel)localObject).getScanlineStride();
      this.pixelStride = 1;
      this.dataOffsets = new int[1];
      this.dataOffsets[0] = localDataBufferByte.getOffset();
      this.dataOffsets[0] += i * this.pixelStride + j * this.scanlineStride;
    }
    else
    {
      throw new RasterFormatException("ByteInterleavedRasters must have PixelInterleavedSampleModel, SinglePixelPackedSampleModel or interleaved ComponentSampleModel.  Sample model is " + paramSampleModel);
    }
    this.bandOffset = this.dataOffsets[0];
    this.dbOffsetPacked = (paramDataBuffer.getOffset() - this.sampleModelTranslateY * this.scanlineStride - this.sampleModelTranslateX * this.pixelStride);
    this.dbOffset = (this.dbOffsetPacked - i * this.pixelStride + j * this.scanlineStride);
    this.inOrder = false;
    if (this.numDataElements == this.pixelStride)
    {
      this.inOrder = true;
      for (int k = 1; k < this.numDataElements; ++k)
        if (this.dataOffsets[k] - this.dataOffsets[0] != k)
        {
          this.inOrder = false;
          break;
        }
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
    return getByteData(paramInt1, paramInt2, paramInt3, paramInt4, (byte[])(byte[])paramObject);
  }

  public byte[] getByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfByte == null)
      paramArrayOfByte = new byte[paramInt3 * paramInt4];
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride + this.dataOffsets[paramInt5];
    int k = 0;
    if (this.pixelStride == 1)
    {
      if (this.scanlineStride == paramInt3)
      {
        System.arraycopy(this.data, i, paramArrayOfByte, 0, paramInt3 * paramInt4);
        break label244:
      }
      int i1 = 0;
      while (true)
      {
        if (i1 >= paramInt4)
          break label244;
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
    label244: return paramArrayOfByte;
  }

  public byte[] getByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
  {
    int i11;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfByte == null)
      paramArrayOfByte = new byte[this.numDataElements * paramInt3 * paramInt4];
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    int k = 0;
    if (this.inOrder)
    {
      i += this.dataOffsets[0];
      i11 = paramInt3 * this.pixelStride;
      if (this.scanlineStride == i11)
      {
        System.arraycopy(this.data, i, paramArrayOfByte, k, i11 * paramInt4);
      }
      else
      {
        int i5 = 0;
        while (i5 < paramInt4)
        {
          System.arraycopy(this.data, i, paramArrayOfByte, k, i11);
          k += i11;
          ++i5;
          i += this.scanlineStride;
        }
      }
    }
    else
    {
      int j;
      if (this.numDataElements == 1)
      {
        i += this.dataOffsets[0];
        int i6 = 0;
        while (true)
        {
          if (i6 >= paramInt4)
            break label848;
          j = i;
          int l = 0;
          while (l < paramInt3)
          {
            paramArrayOfByte[(k++)] = this.data[j];
            ++l;
            j += this.pixelStride;
          }
          ++i6;
          i += this.scanlineStride;
        }
      }
      if (this.numDataElements == 2)
      {
        i += this.dataOffsets[0];
        i11 = this.dataOffsets[1] - this.dataOffsets[0];
        int i7 = 0;
        while (i7 < paramInt4)
        {
          j = i;
          int i1 = 0;
          while (i1 < paramInt3)
          {
            paramArrayOfByte[(k++)] = this.data[j];
            paramArrayOfByte[(k++)] = this.data[(j + i11)];
            ++i1;
            j += this.pixelStride;
          }
          ++i7;
          i += this.scanlineStride;
        }
      }
      else
      {
        int i13;
        if (this.numDataElements == 3)
        {
          i += this.dataOffsets[0];
          i11 = this.dataOffsets[1] - this.dataOffsets[0];
          i13 = this.dataOffsets[2] - this.dataOffsets[0];
          int i8 = 0;
          while (i8 < paramInt4)
          {
            j = i;
            int i2 = 0;
            while (i2 < paramInt3)
            {
              paramArrayOfByte[(k++)] = this.data[j];
              paramArrayOfByte[(k++)] = this.data[(j + i11)];
              paramArrayOfByte[(k++)] = this.data[(j + i13)];
              ++i2;
              j += this.pixelStride;
            }
            ++i8;
            i += this.scanlineStride;
          }
        }
        else if (this.numDataElements == 4)
        {
          i += this.dataOffsets[0];
          i11 = this.dataOffsets[1] - this.dataOffsets[0];
          i13 = this.dataOffsets[2] - this.dataOffsets[0];
          int i14 = this.dataOffsets[3] - this.dataOffsets[0];
          int i9 = 0;
          while (i9 < paramInt4)
          {
            j = i;
            int i3 = 0;
            while (i3 < paramInt3)
            {
              paramArrayOfByte[(k++)] = this.data[j];
              paramArrayOfByte[(k++)] = this.data[(j + i11)];
              paramArrayOfByte[(k++)] = this.data[(j + i13)];
              paramArrayOfByte[(k++)] = this.data[(j + i14)];
              ++i3;
              j += this.pixelStride;
            }
            ++i9;
            i += this.scanlineStride;
          }
        }
        else
        {
          int i10 = 0;
          while (i10 < paramInt4)
          {
            j = i;
            int i4 = 0;
            while (i4 < paramInt3)
            {
              for (int i12 = 0; i12 < this.numDataElements; ++i12)
                paramArrayOfByte[(k++)] = this.data[(this.dataOffsets[i12] + j)];
              ++i4;
              j += this.pixelStride;
            }
            ++i10;
            i += this.scanlineStride;
          }
        }
      }
    }
    label848: return paramArrayOfByte;
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
    int i = paramRaster.getMinX();
    int j = paramRaster.getMinY();
    int k = paramInt1 + i;
    int l = paramInt2 + j;
    int i1 = paramRaster.getWidth();
    int i2 = paramRaster.getHeight();
    if ((k < this.minX) || (l < this.minY) || (k + i1 > this.maxX) || (l + i2 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    setDataElements(k, l, i, j, i1, i2, paramRaster);
  }

  private void setDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, Raster paramRaster)
  {
    if ((paramInt5 <= 0) || (paramInt6 <= 0))
      return;
    int i = paramRaster.getMinX();
    int j = paramRaster.getMinY();
    Object localObject = null;
    if (paramRaster instanceof ByteInterleavedRaster)
    {
      ByteInterleavedRaster localByteInterleavedRaster = (ByteInterleavedRaster)paramRaster;
      byte[] arrayOfByte = localByteInterleavedRaster.getDataStorage();
      if ((this.inOrder) && (localByteInterleavedRaster.inOrder) && (this.pixelStride == localByteInterleavedRaster.pixelStride))
      {
        int l = localByteInterleavedRaster.getDataOffset(0);
        int i1 = localByteInterleavedRaster.getScanlineStride();
        int i2 = localByteInterleavedRaster.getPixelStride();
        int i3 = l + (paramInt4 - j) * i1 + (paramInt3 - i) * i2;
        int i4 = this.dataOffsets[0] + (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
        int i5 = paramInt5 * this.pixelStride;
        for (int i6 = 0; i6 < paramInt6; ++i6)
        {
          System.arraycopy(arrayOfByte, i3, this.data, i4, i5);
          i3 += i1;
          i4 += this.scanlineStride;
        }
        notifyChanged();
        return;
      }
    }
    for (int k = 0; k < paramInt6; ++k)
    {
      localObject = paramRaster.getDataElements(i, j + k, paramInt5, 1, localObject);
      setDataElements(paramInt1, paramInt2 + k, paramInt5, 1, localObject);
    }
    notifyChanged();
  }

  public void setDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    putByteData(paramInt1, paramInt2, paramInt3, paramInt4, (byte[])(byte[])paramObject);
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
    int i11;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = (paramInt2 - this.minY) * this.scanlineStride + (paramInt1 - this.minX) * this.pixelStride;
    int k = 0;
    if (this.inOrder)
    {
      i += this.dataOffsets[0];
      i11 = paramInt3 * this.pixelStride;
      if (i11 == this.scanlineStride)
      {
        System.arraycopy(paramArrayOfByte, 0, this.data, i, i11 * paramInt4);
      }
      else
      {
        int i5 = 0;
        while (i5 < paramInt4)
        {
          System.arraycopy(paramArrayOfByte, k, this.data, i, i11);
          k += i11;
          ++i5;
          i += this.scanlineStride;
        }
      }
    }
    else
    {
      int j;
      if (this.numDataElements == 1)
      {
        i += this.dataOffsets[0];
        int i6 = 0;
        while (true)
        {
          if (i6 >= paramInt4)
            break label829;
          j = i;
          int l = 0;
          while (l < paramInt3)
          {
            this.data[j] = paramArrayOfByte[(k++)];
            ++l;
            j += this.pixelStride;
          }
          ++i6;
          i += this.scanlineStride;
        }
      }
      if (this.numDataElements == 2)
      {
        i += this.dataOffsets[0];
        i11 = this.dataOffsets[1] - this.dataOffsets[0];
        int i7 = 0;
        while (i7 < paramInt4)
        {
          j = i;
          int i1 = 0;
          while (i1 < paramInt3)
          {
            this.data[j] = paramArrayOfByte[(k++)];
            this.data[(j + i11)] = paramArrayOfByte[(k++)];
            ++i1;
            j += this.pixelStride;
          }
          ++i7;
          i += this.scanlineStride;
        }
      }
      else
      {
        int i13;
        if (this.numDataElements == 3)
        {
          i += this.dataOffsets[0];
          i11 = this.dataOffsets[1] - this.dataOffsets[0];
          i13 = this.dataOffsets[2] - this.dataOffsets[0];
          int i8 = 0;
          while (i8 < paramInt4)
          {
            j = i;
            int i2 = 0;
            while (i2 < paramInt3)
            {
              this.data[j] = paramArrayOfByte[(k++)];
              this.data[(j + i11)] = paramArrayOfByte[(k++)];
              this.data[(j + i13)] = paramArrayOfByte[(k++)];
              ++i2;
              j += this.pixelStride;
            }
            ++i8;
            i += this.scanlineStride;
          }
        }
        else if (this.numDataElements == 4)
        {
          i += this.dataOffsets[0];
          i11 = this.dataOffsets[1] - this.dataOffsets[0];
          i13 = this.dataOffsets[2] - this.dataOffsets[0];
          int i14 = this.dataOffsets[3] - this.dataOffsets[0];
          int i9 = 0;
          while (i9 < paramInt4)
          {
            j = i;
            int i3 = 0;
            while (i3 < paramInt3)
            {
              this.data[j] = paramArrayOfByte[(k++)];
              this.data[(j + i11)] = paramArrayOfByte[(k++)];
              this.data[(j + i13)] = paramArrayOfByte[(k++)];
              this.data[(j + i14)] = paramArrayOfByte[(k++)];
              ++i3;
              j += this.pixelStride;
            }
            ++i9;
            i += this.scanlineStride;
          }
        }
        else
        {
          int i10 = 0;
          while (i10 < paramInt4)
          {
            j = i;
            int i4 = 0;
            while (i4 < paramInt3)
            {
              for (int i12 = 0; i12 < this.numDataElements; ++i12)
                this.data[(this.dataOffsets[i12] + j)] = paramArrayOfByte[(k++)];
              ++i4;
              j += this.pixelStride;
            }
            ++i10;
            i += this.scanlineStride;
          }
        }
      }
    }
    label829: notifyChanged();
  }

  public int getSample(int paramInt1, int paramInt2, int paramInt3)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 >= this.maxX) || (paramInt2 >= this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (this.packed)
    {
      i = paramInt2 * this.scanlineStride + paramInt1 + this.dbOffsetPacked;
      int j = this.data[i];
      return ((j & this.bitMasks[paramInt3]) >>> this.bitOffsets[paramInt3]);
    }
    int i = paramInt2 * this.scanlineStride + paramInt1 * this.pixelStride + this.dbOffset;
    return (this.data[(i + this.dataOffsets[paramInt3])] & 0xFF);
  }

  public void setSample(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 >= this.maxX) || (paramInt2 >= this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (this.packed)
    {
      i = paramInt2 * this.scanlineStride + paramInt1 + this.dbOffsetPacked;
      int j = this.bitMasks[paramInt3];
      int k = this.data[i];
      k = (byte)(k & (j ^ 0xFFFFFFFF));
      k = (byte)(k | paramInt4 << this.bitOffsets[paramInt3] & j);
      this.data[i] = k;
    }
    else
    {
      i = paramInt2 * this.scanlineStride + paramInt1 * this.pixelStride + this.dbOffset;
      this.data[(i + this.dataOffsets[paramInt3])] = (byte)paramInt4;
    }
    notifyChanged();
  }

  public int[] getSamples(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int[] paramArrayOfInt)
  {
    int[] arrayOfInt;
    int k;
    int l;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfInt != null)
      arrayOfInt = paramArrayOfInt;
    else
      arrayOfInt = new int[paramInt3 * paramInt4];
    int i = paramInt2 * this.scanlineStride + paramInt1 * this.pixelStride;
    int j = 0;
    if (this.packed)
    {
      i += this.dbOffsetPacked;
      k = this.bitMasks[paramInt5];
      l = this.bitOffsets[paramInt5];
      for (int i1 = 0; i1 < paramInt4; ++i1)
      {
        int i3 = i;
        for (int i4 = 0; i4 < paramInt3; ++i4)
        {
          int i5 = this.data[(i3++)];
          arrayOfInt[(j++)] = ((i5 & k) >>> l);
        }
        i += this.scanlineStride;
      }
    }
    else
    {
      i += this.dbOffset + this.dataOffsets[paramInt5];
      for (k = 0; k < paramInt4; ++k)
      {
        l = i;
        for (int i2 = 0; i2 < paramInt3; ++i2)
        {
          arrayOfInt[(j++)] = (this.data[l] & 0xFF);
          l += this.pixelStride;
        }
        i += this.scanlineStride;
      }
    }
    return arrayOfInt;
  }

  public void setSamples(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int[] paramArrayOfInt)
  {
    int k;
    int l;
    int i1;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = paramInt2 * this.scanlineStride + paramInt1 * this.pixelStride;
    int j = 0;
    if (this.packed)
    {
      i += this.dbOffsetPacked;
      k = this.bitMasks[paramInt5];
      for (l = 0; l < paramInt4; ++l)
      {
        i1 = i;
        for (int i2 = 0; i2 < paramInt3; ++i2)
        {
          int i3 = this.data[i1];
          i3 = (byte)(i3 & (k ^ 0xFFFFFFFF));
          int i4 = paramArrayOfInt[(j++)];
          i3 = (byte)(i3 | i4 << this.bitOffsets[paramInt5] & k);
          this.data[(i1++)] = i3;
        }
        i += this.scanlineStride;
      }
    }
    else
    {
      i += this.dbOffset + this.dataOffsets[paramInt5];
      for (k = 0; k < paramInt4; ++k)
      {
        l = i;
        for (i1 = 0; i1 < paramInt3; ++i1)
        {
          this.data[l] = (byte)paramArrayOfInt[(j++)];
          l += this.pixelStride;
        }
        i += this.scanlineStride;
      }
    }
    notifyChanged();
  }

  public int[] getPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt)
  {
    int[] arrayOfInt;
    int k;
    int i3;
    int i4;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfInt != null)
      arrayOfInt = paramArrayOfInt;
    else
      arrayOfInt = new int[paramInt3 * paramInt4 * this.numBands];
    int i = paramInt2 * this.scanlineStride + paramInt1 * this.pixelStride;
    int j = 0;
    if (this.packed)
    {
      i += this.dbOffsetPacked;
      for (k = 0; k < paramInt4; ++k)
      {
        for (int l = 0; l < paramInt3; ++l)
        {
          i3 = this.data[(i + l)];
          for (i4 = 0; i4 < this.numBands; ++i4)
            arrayOfInt[(j++)] = ((i3 & this.bitMasks[i4]) >>> this.bitOffsets[i4]);
        }
        i += this.scanlineStride;
      }
    }
    else
    {
      int i1;
      i += this.dbOffset;
      k = this.dataOffsets[0];
      if (this.numBands == 1)
      {
        for (i1 = 0; i1 < paramInt4; ++i1)
        {
          i3 = i + k;
          for (i4 = 0; i4 < paramInt3; ++i4)
          {
            arrayOfInt[(j++)] = (this.data[i3] & 0xFF);
            i3 += this.pixelStride;
          }
          i += this.scanlineStride;
        }
      }
      else
      {
        int i7;
        if (this.numBands == 2)
        {
          i1 = this.dataOffsets[1] - k;
          for (i3 = 0; i3 < paramInt4; ++i3)
          {
            i4 = i + k;
            for (i7 = 0; i7 < paramInt3; ++i7)
            {
              arrayOfInt[(j++)] = (this.data[i4] & 0xFF);
              arrayOfInt[(j++)] = (this.data[(i4 + i1)] & 0xFF);
              i4 += this.pixelStride;
            }
            i += this.scanlineStride;
          }
        }
        else
        {
          int i5;
          int i10;
          if (this.numBands == 3)
          {
            i1 = this.dataOffsets[1] - k;
            i3 = this.dataOffsets[2] - k;
            for (i5 = 0; i5 < paramInt4; ++i5)
            {
              i7 = i + k;
              for (i10 = 0; i10 < paramInt3; ++i10)
              {
                arrayOfInt[(j++)] = (this.data[i7] & 0xFF);
                arrayOfInt[(j++)] = (this.data[(i7 + i1)] & 0xFF);
                arrayOfInt[(j++)] = (this.data[(i7 + i3)] & 0xFF);
                i7 += this.pixelStride;
              }
              i += this.scanlineStride;
            }
          }
          else if (this.numBands == 4)
          {
            i1 = this.dataOffsets[1] - k;
            i3 = this.dataOffsets[2] - k;
            i5 = this.dataOffsets[3] - k;
            for (int i8 = 0; i8 < paramInt4; ++i8)
            {
              i10 = i + k;
              for (int i11 = 0; i11 < paramInt3; ++i11)
              {
                arrayOfInt[(j++)] = (this.data[i10] & 0xFF);
                arrayOfInt[(j++)] = (this.data[(i10 + i1)] & 0xFF);
                arrayOfInt[(j++)] = (this.data[(i10 + i3)] & 0xFF);
                arrayOfInt[(j++)] = (this.data[(i10 + i5)] & 0xFF);
                i10 += this.pixelStride;
              }
              i += this.scanlineStride;
            }
          }
          else
          {
            for (int i2 = 0; i2 < paramInt4; ++i2)
            {
              i3 = i;
              for (int i6 = 0; i6 < paramInt3; ++i6)
              {
                for (int i9 = 0; i9 < this.numBands; ++i9)
                  arrayOfInt[(j++)] = (this.data[(i3 + this.dataOffsets[i9])] & 0xFF);
                i3 += this.pixelStride;
              }
              i += this.scanlineStride;
            }
          }
        }
      }
    }
    return arrayOfInt;
  }

  public void setPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt)
  {
    int k;
    int i3;
    int i4;
    int i7;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = paramInt2 * this.scanlineStride + paramInt1 * this.pixelStride;
    int j = 0;
    if (this.packed)
    {
      i += this.dbOffsetPacked;
      for (k = 0; k < paramInt4; ++k)
      {
        for (int l = 0; l < paramInt3; ++l)
        {
          i3 = 0;
          for (i4 = 0; i4 < this.numBands; ++i4)
          {
            i7 = paramArrayOfInt[(j++)];
            i3 |= i7 << this.bitOffsets[i4] & this.bitMasks[i4];
          }
          this.data[(i + l)] = (byte)i3;
        }
        i += this.scanlineStride;
      }
    }
    else
    {
      int i1;
      i += this.dbOffset;
      k = this.dataOffsets[0];
      if (this.numBands == 1)
      {
        for (i1 = 0; i1 < paramInt4; ++i1)
        {
          i3 = i + k;
          for (i4 = 0; i4 < paramInt3; ++i4)
          {
            this.data[i3] = (byte)paramArrayOfInt[(j++)];
            i3 += this.pixelStride;
          }
          i += this.scanlineStride;
        }
      }
      else if (this.numBands == 2)
      {
        i1 = this.dataOffsets[1] - k;
        for (i3 = 0; i3 < paramInt4; ++i3)
        {
          i4 = i + k;
          for (i7 = 0; i7 < paramInt3; ++i7)
          {
            this.data[i4] = (byte)paramArrayOfInt[(j++)];
            this.data[(i4 + i1)] = (byte)paramArrayOfInt[(j++)];
            i4 += this.pixelStride;
          }
          i += this.scanlineStride;
        }
      }
      else
      {
        int i5;
        int i10;
        if (this.numBands == 3)
        {
          i1 = this.dataOffsets[1] - k;
          i3 = this.dataOffsets[2] - k;
          for (i5 = 0; i5 < paramInt4; ++i5)
          {
            i7 = i + k;
            for (i10 = 0; i10 < paramInt3; ++i10)
            {
              this.data[i7] = (byte)paramArrayOfInt[(j++)];
              this.data[(i7 + i1)] = (byte)paramArrayOfInt[(j++)];
              this.data[(i7 + i3)] = (byte)paramArrayOfInt[(j++)];
              i7 += this.pixelStride;
            }
            i += this.scanlineStride;
          }
        }
        else if (this.numBands == 4)
        {
          i1 = this.dataOffsets[1] - k;
          i3 = this.dataOffsets[2] - k;
          i5 = this.dataOffsets[3] - k;
          for (int i8 = 0; i8 < paramInt4; ++i8)
          {
            i10 = i + k;
            for (int i11 = 0; i11 < paramInt3; ++i11)
            {
              this.data[i10] = (byte)paramArrayOfInt[(j++)];
              this.data[(i10 + i1)] = (byte)paramArrayOfInt[(j++)];
              this.data[(i10 + i3)] = (byte)paramArrayOfInt[(j++)];
              this.data[(i10 + i5)] = (byte)paramArrayOfInt[(j++)];
              i10 += this.pixelStride;
            }
            i += this.scanlineStride;
          }
        }
        else
        {
          for (int i2 = 0; i2 < paramInt4; ++i2)
          {
            i3 = i;
            for (int i6 = 0; i6 < paramInt3; ++i6)
            {
              for (int i9 = 0; i9 < this.numBands; ++i9)
                this.data[(i3 + this.dataOffsets[i9])] = (byte)paramArrayOfInt[(j++)];
              i3 += this.pixelStride;
            }
            i += this.scanlineStride;
          }
        }
      }
    }
    notifyChanged();
  }

  public void setRect(int paramInt1, int paramInt2, Raster paramRaster)
  {
    int i3;
    if (!(paramRaster instanceof ByteInterleavedRaster))
    {
      super.setRect(paramInt1, paramInt2, paramRaster);
      return;
    }
    int i = paramRaster.getWidth();
    int j = paramRaster.getHeight();
    int k = paramRaster.getMinX();
    int l = paramRaster.getMinY();
    int i1 = paramInt1 + k;
    int i2 = paramInt2 + l;
    if (i1 < this.minX)
    {
      i3 = this.minX - i1;
      i -= i3;
      k += i3;
      i1 = this.minX;
    }
    if (i2 < this.minY)
    {
      i3 = this.minY - i2;
      j -= i3;
      l += i3;
      i2 = this.minY;
    }
    if (i1 + i > this.maxX)
      i = this.maxX - i1;
    if (i2 + j > this.maxY)
      j = this.maxY - i2;
    setDataElements(i1, i2, k, l, i, j, paramRaster);
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
    return new ByteInterleavedRaster(localSampleModel, this.dataBuffer, new Rectangle(paramInt5, paramInt6, paramInt3, paramInt4), new Point(this.sampleModelTranslateX + i, this.sampleModelTranslateY + j), this);
  }

  public WritableRaster createCompatibleWritableRaster(int paramInt1, int paramInt2)
  {
    if ((paramInt1 <= 0) || (paramInt2 <= 0))
      throw new RasterFormatException("negative " + "height");
    SampleModel localSampleModel = this.sampleModel.createCompatibleSampleModel(paramInt1, paramInt2);
    return new ByteInterleavedRaster(localSampleModel, new Point(0, 0));
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
    return new String("ByteInterleavedRaster: width = " + this.width + " height = " + this.height + " #numDataElements " + this.numDataElements + " dataOff[0] = " + this.dataOffsets[0]);
  }
}