package sun.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class BytePackedRaster extends SunWritableRaster
{
  int dataBitOffset;
  int scanlineStride;
  int pixelBitStride;
  int bitMask;
  byte[] data;
  int shiftOffset;
  int type;
  private int maxX;
  private int maxY;

  private static native void initIDs();

  public BytePackedRaster(SampleModel paramSampleModel, Point paramPoint)
  {
    this(paramSampleModel, paramSampleModel.createDataBuffer(), new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public BytePackedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Point paramPoint)
  {
    this(paramSampleModel, paramDataBuffer, new Rectangle(paramPoint.x, paramPoint.y, paramSampleModel.getWidth(), paramSampleModel.getHeight()), paramPoint, null);
  }

  public BytePackedRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer, Rectangle paramRectangle, Point paramPoint, BytePackedRaster paramBytePackedRaster)
  {
    super(paramSampleModel, paramDataBuffer, paramRectangle, paramPoint, paramBytePackedRaster);
    this.maxX = (this.minX + this.width);
    this.maxY = (this.minY + this.height);
    if (!(paramDataBuffer instanceof DataBufferByte))
      throw new RasterFormatException("BytePackedRasters must havebyte DataBuffers");
    DataBufferByte localDataBufferByte = (DataBufferByte)paramDataBuffer;
    this.data = localDataBufferByte.getData();
    if (localDataBufferByte.getNumBanks() != 1)
      throw new RasterFormatException("DataBuffer for BytePackedRasters must only have 1 bank.");
    int i = localDataBufferByte.getOffset();
    if (paramSampleModel instanceof MultiPixelPackedSampleModel)
    {
      MultiPixelPackedSampleModel localMultiPixelPackedSampleModel = (MultiPixelPackedSampleModel)paramSampleModel;
      this.type = 11;
      this.pixelBitStride = localMultiPixelPackedSampleModel.getPixelBitStride();
      if ((this.pixelBitStride != 1) && (this.pixelBitStride != 2) && (this.pixelBitStride != 4))
        throw new RasterFormatException("BytePackedRasters must have a bit depth of 1, 2, or 4");
      this.scanlineStride = localMultiPixelPackedSampleModel.getScanlineStride();
      this.dataBitOffset = (localMultiPixelPackedSampleModel.getDataBitOffset() + i * 8);
      int j = paramRectangle.x - paramPoint.x;
      int k = paramRectangle.y - paramPoint.y;
      this.dataBitOffset += j * this.pixelBitStride + k * this.scanlineStride * 8;
      this.bitMask = ((1 << this.pixelBitStride) - 1);
      this.shiftOffset = (8 - this.pixelBitStride);
    }
    else
    {
      throw new RasterFormatException("BytePackedRasters must haveMultiPixelPackedSampleModel");
    }
    verify(false);
  }

  public int getDataBitOffset()
  {
    return this.dataBitOffset;
  }

  public int getScanlineStride()
  {
    return this.scanlineStride;
  }

  public int getPixelBitStride()
  {
    return this.pixelBitStride;
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
    int i = this.dataBitOffset + (paramInt1 - this.minX) * this.pixelBitStride;
    int j = this.data[((paramInt2 - this.minY) * this.scanlineStride + (i >> 3))] & 0xFF;
    int k = this.shiftOffset - (i & 0x7);
    arrayOfByte[0] = (byte)(j >> k & this.bitMask);
    return arrayOfByte;
  }

  public Object getDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    return getByteData(paramInt1, paramInt2, paramInt3, paramInt4, (byte[])(byte[])paramObject);
  }

  public Object getPixelData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    byte[] arrayOfByte1;
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramObject == null)
      arrayOfByte1 = new byte[this.numDataElements * paramInt3 * paramInt4];
    else
      arrayOfByte1 = (byte[])(byte[])paramObject;
    int i = this.pixelBitStride;
    int j = this.dataBitOffset + (paramInt1 - this.minX) * i;
    int k = (paramInt2 - this.minY) * this.scanlineStride;
    int l = 0;
    byte[] arrayOfByte2 = this.data;
    for (int i1 = 0; i1 < paramInt4; ++i1)
    {
      int i2 = j;
      for (int i3 = 0; i3 < paramInt3; ++i3)
      {
        int i4 = this.shiftOffset - (i2 & 0x7);
        arrayOfByte1[(l++)] = (byte)(this.bitMask & arrayOfByte2[(k + (i2 >> 3))] >> i4);
        i2 += i;
      }
      k += this.scanlineStride;
    }
    return arrayOfByte1;
  }

  public byte[] getByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, byte[] paramArrayOfByte)
  {
    return getByteData(paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfByte);
  }

  public byte[] getByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfByte == null)
      paramArrayOfByte = new byte[paramInt3 * paramInt4];
    int i = this.pixelBitStride;
    int j = this.dataBitOffset + (paramInt1 - this.minX) * i;
    int k = (paramInt2 - this.minY) * this.scanlineStride;
    int l = 0;
    byte[] arrayOfByte = this.data;
    for (int i1 = 0; i1 < paramInt4; ++i1)
    {
      int i3;
      int i2 = j;
      for (int i4 = 0; (i4 < paramInt3) && ((i2 & 0x7) != 0); ++i4)
      {
        i5 = this.shiftOffset - (i2 & 0x7);
        paramArrayOfByte[(l++)] = (byte)(this.bitMask & arrayOfByte[(k + (i2 >> 3))] >> i5);
        i2 += i;
      }
      int i5 = k + (i2 >> 3);
      switch (i)
      {
      case 1:
        while (true)
        {
          if (i4 >= paramInt3 - 7)
            break label710;
          i3 = arrayOfByte[(i5++)];
          paramArrayOfByte[(l++)] = (byte)(i3 >> 7 & 0x1);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 6 & 0x1);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 5 & 0x1);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 4 & 0x1);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 3 & 0x1);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 2 & 0x1);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 1 & 0x1);
          paramArrayOfByte[(l++)] = (byte)(i3 & 0x1);
          i2 += 8;
          i4 += 8;
        }
      case 2:
        while (true)
        {
          if (i4 >= paramInt3 - 7)
            break label710;
          i3 = arrayOfByte[(i5++)];
          paramArrayOfByte[(l++)] = (byte)(i3 >> 6 & 0x3);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 4 & 0x3);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 2 & 0x3);
          paramArrayOfByte[(l++)] = (byte)(i3 & 0x3);
          i3 = arrayOfByte[(i5++)];
          paramArrayOfByte[(l++)] = (byte)(i3 >> 6 & 0x3);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 4 & 0x3);
          paramArrayOfByte[(l++)] = (byte)(i3 >> 2 & 0x3);
          paramArrayOfByte[(l++)] = (byte)(i3 & 0x3);
          i2 += 16;
          i4 += 8;
        }
      case 4:
        while (i4 < paramInt3 - 7)
        {
          i3 = arrayOfByte[(i5++)];
          paramArrayOfByte[(l++)] = (byte)(i3 >> 4 & 0xF);
          paramArrayOfByte[(l++)] = (byte)(i3 & 0xF);
          i3 = arrayOfByte[(i5++)];
          paramArrayOfByte[(l++)] = (byte)(i3 >> 4 & 0xF);
          paramArrayOfByte[(l++)] = (byte)(i3 & 0xF);
          i3 = arrayOfByte[(i5++)];
          paramArrayOfByte[(l++)] = (byte)(i3 >> 4 & 0xF);
          paramArrayOfByte[(l++)] = (byte)(i3 & 0xF);
          i3 = arrayOfByte[(i5++)];
          paramArrayOfByte[(l++)] = (byte)(i3 >> 4 & 0xF);
          paramArrayOfByte[(l++)] = (byte)(i3 & 0xF);
          i2 += 32;
          i4 += 8;
        }
      case 3:
      }
      while (i4 < paramInt3)
      {
        label710: int i6 = this.shiftOffset - (i2 & 0x7);
        paramArrayOfByte[(l++)] = (byte)(this.bitMask & arrayOfByte[(k + (i2 >> 3))] >> i6);
        i2 += i;
        ++i4;
      }
      k += this.scanlineStride;
    }
    return paramArrayOfByte;
  }

  public void setDataElements(int paramInt1, int paramInt2, Object paramObject)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 >= this.maxX) || (paramInt2 >= this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    byte[] arrayOfByte = (byte[])(byte[])paramObject;
    int i = this.dataBitOffset + (paramInt1 - this.minX) * this.pixelBitStride;
    int j = (paramInt2 - this.minY) * this.scanlineStride + (i >> 3);
    int k = this.shiftOffset - (i & 0x7);
    int l = this.data[j];
    l = (byte)(l & (this.bitMask << k ^ 0xFFFFFFFF));
    l = (byte)(l | (arrayOfByte[0] & this.bitMask) << k);
    this.data[j] = l;
    notifyChanged();
  }

  public void setDataElements(int paramInt1, int paramInt2, Raster paramRaster)
  {
    if ((!(paramRaster instanceof BytePackedRaster)) || (((BytePackedRaster)paramRaster).pixelBitStride != this.pixelBitStride))
    {
      super.setDataElements(paramInt1, paramInt2, paramRaster);
      return;
    }
    int i = paramRaster.getMinX();
    int j = paramRaster.getMinY();
    int k = i + paramInt1;
    int l = j + paramInt2;
    int i1 = paramRaster.getWidth();
    int i2 = paramRaster.getHeight();
    if ((k < this.minX) || (l < this.minY) || (k + i1 > this.maxX) || (l + i2 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    setDataElements(k, l, i, j, i1, i2, (BytePackedRaster)paramRaster);
  }

  private void setDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, BytePackedRaster paramBytePackedRaster)
  {
    int i2;
    int i3;
    int i4;
    int i5;
    int i7;
    int i9;
    int i10;
    if ((paramInt5 <= 0) || (paramInt6 <= 0))
      return;
    byte[] arrayOfByte1 = paramBytePackedRaster.data;
    byte[] arrayOfByte2 = this.data;
    int i = paramBytePackedRaster.scanlineStride;
    int j = this.scanlineStride;
    int k = paramBytePackedRaster.dataBitOffset + 8 * (paramInt4 - paramBytePackedRaster.minY) * i + (paramInt3 - paramBytePackedRaster.minX) * paramBytePackedRaster.pixelBitStride;
    int l = this.dataBitOffset + 8 * (paramInt2 - this.minY) * j + (paramInt1 - this.minX) * this.pixelBitStride;
    int i1 = paramInt5 * this.pixelBitStride;
    if ((k & 0x7) == (l & 0x7))
    {
      int i6;
      i2 = l & 0x7;
      if (i2 != 0)
      {
        i3 = 8 - i2;
        i4 = k >> 3;
        i5 = l >> 3;
        i6 = 255 >> i2;
        if (i1 < i3)
        {
          i6 &= 255 << i3 - i1;
          i3 = i1;
        }
        for (int i8 = 0; i8 < paramInt6; ++i8)
        {
          i10 = arrayOfByte2[i5];
          i10 &= (i6 ^ 0xFFFFFFFF);
          i10 |= arrayOfByte1[i4] & i6;
          arrayOfByte2[i5] = (byte)i10;
          i4 += i;
          i5 += j;
        }
        k += i3;
        l += i3;
        i1 -= i3;
      }
      if (i1 >= 8)
      {
        i3 = k >> 3;
        i4 = l >> 3;
        i5 = i1 >> 3;
        if ((i5 == i) && (i == j))
          System.arraycopy(arrayOfByte1, i3, arrayOfByte2, i4, i * paramInt6);
        else
          for (i6 = 0; i6 < paramInt6; ++i6)
          {
            System.arraycopy(arrayOfByte1, i3, arrayOfByte2, i4, i5);
            i3 += i;
            i4 += j;
          }
        i6 = i5 * 8;
        k += i6;
        l += i6;
        i1 -= i6;
      }
      if (i1 > 0)
      {
        i3 = k >> 3;
        i4 = l >> 3;
        i5 = 65280 >> i1 & 0xFF;
        for (i7 = 0; i7 < paramInt6; ++i7)
        {
          i9 = arrayOfByte2[i4];
          i9 &= (i5 ^ 0xFFFFFFFF);
          i9 |= arrayOfByte1[i3] & i5;
          arrayOfByte2[i4] = (byte)i9;
          i3 += i;
          i4 += j;
        }
      }
    }
    else
    {
      int i11;
      int i13;
      int i18;
      int i20;
      i2 = l & 0x7;
      if ((i2 != 0) || (i1 < 8))
      {
        i3 = 8 - i2;
        i4 = k >> 3;
        i5 = l >> 3;
        i7 = k & 0x7;
        i9 = 8 - i7;
        i10 = 255 >> i2;
        if (i1 < i3)
        {
          i10 &= 255 << i3 - i1;
          i3 = i1;
        }
        i11 = arrayOfByte1.length - 1;
        for (i13 = 0; i13 < paramInt6; ++i13)
        {
          int i15 = arrayOfByte1[i4];
          i18 = 0;
          if (i4 < i11)
            i18 = arrayOfByte1[(i4 + 1)];
          i20 = arrayOfByte2[i5];
          i20 &= (i10 ^ 0xFFFFFFFF);
          i20 |= (i15 << i7 | (i18 & 0xFF) >> i9) >> i2 & i10;
          arrayOfByte2[i5] = (byte)i20;
          i4 += i;
          i5 += j;
        }
        k += i3;
        l += i3;
        i1 -= i3;
      }
      if (i1 >= 8)
      {
        i3 = k >> 3;
        i4 = l >> 3;
        i5 = i1 >> 3;
        i7 = k & 0x7;
        i9 = 8 - i7;
        for (i10 = 0; i10 < paramInt6; ++i10)
        {
          i11 = i3 + i10 * i;
          i13 = i4 + i10 * j;
          int i16 = arrayOfByte1[i11];
          for (i18 = 0; i18 < i5; ++i18)
          {
            i20 = arrayOfByte1[(i11 + 1)];
            int i21 = i16 << i7 | (i20 & 0xFF) >> i9;
            arrayOfByte2[i13] = (byte)i21;
            i16 = i20;
            ++i11;
            ++i13;
          }
        }
        i10 = i5 * 8;
        k += i10;
        l += i10;
        i1 -= i10;
      }
      if (i1 > 0)
      {
        i3 = k >> 3;
        i4 = l >> 3;
        i5 = 65280 >> i1 & 0xFF;
        i7 = k & 0x7;
        i9 = 8 - i7;
        i10 = arrayOfByte1.length - 1;
        for (int i12 = 0; i12 < paramInt6; ++i12)
        {
          int i14 = arrayOfByte1[i3];
          int i17 = 0;
          if (i3 < i10)
            i17 = arrayOfByte1[(i3 + 1)];
          int i19 = arrayOfByte2[i4];
          i19 &= (i5 ^ 0xFFFFFFFF);
          i19 |= (i14 << i7 | (i17 & 0xFF) >> i9) & i5;
          arrayOfByte2[i4] = (byte)i19;
          i3 += i;
          i4 += j;
        }
      }
    }
  }

  public void setRect(int paramInt1, int paramInt2, Raster paramRaster)
  {
    int i3;
    if ((!(paramRaster instanceof BytePackedRaster)) || (((BytePackedRaster)paramRaster).pixelBitStride != this.pixelBitStride))
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
    setDataElements(i1, i2, k, l, i, j, (BytePackedRaster)paramRaster);
    notifyChanged();
  }

  public void setDataElements(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object paramObject)
  {
    putByteData(paramInt1, paramInt2, paramInt3, paramInt4, (byte[])(byte[])paramObject);
  }

  public void putByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, byte[] paramArrayOfByte)
  {
    putByteData(paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfByte);
  }

  public void putByteData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if ((paramInt3 == 0) || (paramInt4 == 0))
      return;
    int i = this.pixelBitStride;
    int j = this.dataBitOffset + (paramInt1 - this.minX) * i;
    int k = (paramInt2 - this.minY) * this.scanlineStride;
    int l = 0;
    byte[] arrayOfByte = this.data;
    for (int i1 = 0; i1 < paramInt4; ++i1)
    {
      int i3;
      int i2 = j;
      for (int i4 = 0; (i4 < paramInt3) && ((i2 & 0x7) != 0); ++i4)
      {
        i5 = this.shiftOffset - (i2 & 0x7);
        i3 = arrayOfByte[(k + (i2 >> 3))];
        i3 &= (this.bitMask << i5 ^ 0xFFFFFFFF);
        i3 |= (paramArrayOfByte[(l++)] & this.bitMask) << i5;
        arrayOfByte[(k + (i2 >> 3))] = (byte)i3;
        i2 += i;
      }
      int i5 = k + (i2 >> 3);
      switch (i)
      {
      case 1:
        while (true)
        {
          if (i4 >= paramInt3 - 7)
            break label776;
          i3 = (paramArrayOfByte[(l++)] & 0x1) << 7;
          i3 |= (paramArrayOfByte[(l++)] & 0x1) << 6;
          i3 |= (paramArrayOfByte[(l++)] & 0x1) << 5;
          i3 |= (paramArrayOfByte[(l++)] & 0x1) << 4;
          i3 |= (paramArrayOfByte[(l++)] & 0x1) << 3;
          i3 |= (paramArrayOfByte[(l++)] & 0x1) << 2;
          i3 |= (paramArrayOfByte[(l++)] & 0x1) << 1;
          i3 |= paramArrayOfByte[(l++)] & 0x1;
          arrayOfByte[(i5++)] = (byte)i3;
          i2 += 8;
          i4 += 8;
        }
      case 2:
        while (true)
        {
          if (i4 >= paramInt3 - 7)
            break label776;
          i3 = (paramArrayOfByte[(l++)] & 0x3) << 6;
          i3 |= (paramArrayOfByte[(l++)] & 0x3) << 4;
          i3 |= (paramArrayOfByte[(l++)] & 0x3) << 2;
          i3 |= paramArrayOfByte[(l++)] & 0x3;
          arrayOfByte[(i5++)] = (byte)i3;
          i3 = (paramArrayOfByte[(l++)] & 0x3) << 6;
          i3 |= (paramArrayOfByte[(l++)] & 0x3) << 4;
          i3 |= (paramArrayOfByte[(l++)] & 0x3) << 2;
          i3 |= paramArrayOfByte[(l++)] & 0x3;
          arrayOfByte[(i5++)] = (byte)i3;
          i2 += 16;
          i4 += 8;
        }
      case 4:
        while (i4 < paramInt3 - 7)
        {
          i3 = (paramArrayOfByte[(l++)] & 0xF) << 4;
          i3 |= paramArrayOfByte[(l++)] & 0xF;
          arrayOfByte[(i5++)] = (byte)i3;
          i3 = (paramArrayOfByte[(l++)] & 0xF) << 4;
          i3 |= paramArrayOfByte[(l++)] & 0xF;
          arrayOfByte[(i5++)] = (byte)i3;
          i3 = (paramArrayOfByte[(l++)] & 0xF) << 4;
          i3 |= paramArrayOfByte[(l++)] & 0xF;
          arrayOfByte[(i5++)] = (byte)i3;
          i3 = (paramArrayOfByte[(l++)] & 0xF) << 4;
          i3 |= paramArrayOfByte[(l++)] & 0xF;
          arrayOfByte[(i5++)] = (byte)i3;
          i2 += 32;
          i4 += 8;
        }
      case 3:
      }
      while (i4 < paramInt3)
      {
        label776: int i6 = this.shiftOffset - (i2 & 0x7);
        i3 = arrayOfByte[(k + (i2 >> 3))];
        i3 &= (this.bitMask << i6 ^ 0xFFFFFFFF);
        i3 |= (paramArrayOfByte[(l++)] & this.bitMask) << i6;
        arrayOfByte[(k + (i2 >> 3))] = (byte)i3;
        i2 += i;
        ++i4;
      }
      k += this.scanlineStride;
    }
    notifyChanged();
  }

  public int[] getPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    if (paramArrayOfInt == null)
      paramArrayOfInt = new int[paramInt3 * paramInt4];
    int i = this.pixelBitStride;
    int j = this.dataBitOffset + (paramInt1 - this.minX) * i;
    int k = (paramInt2 - this.minY) * this.scanlineStride;
    int l = 0;
    byte[] arrayOfByte = this.data;
    for (int i1 = 0; i1 < paramInt4; ++i1)
    {
      int i3;
      int i2 = j;
      for (int i4 = 0; (i4 < paramInt3) && ((i2 & 0x7) != 0); ++i4)
      {
        i5 = this.shiftOffset - (i2 & 0x7);
        paramArrayOfInt[(l++)] = (this.bitMask & arrayOfByte[(k + (i2 >> 3))] >> i5);
        i2 += i;
      }
      int i5 = k + (i2 >> 3);
      switch (i)
      {
      case 1:
        while (true)
        {
          if (i4 >= paramInt3 - 7)
            break label686;
          i3 = arrayOfByte[(i5++)];
          paramArrayOfInt[(l++)] = (i3 >> 7 & 0x1);
          paramArrayOfInt[(l++)] = (i3 >> 6 & 0x1);
          paramArrayOfInt[(l++)] = (i3 >> 5 & 0x1);
          paramArrayOfInt[(l++)] = (i3 >> 4 & 0x1);
          paramArrayOfInt[(l++)] = (i3 >> 3 & 0x1);
          paramArrayOfInt[(l++)] = (i3 >> 2 & 0x1);
          paramArrayOfInt[(l++)] = (i3 >> 1 & 0x1);
          paramArrayOfInt[(l++)] = (i3 & 0x1);
          i2 += 8;
          i4 += 8;
        }
      case 2:
        while (true)
        {
          if (i4 >= paramInt3 - 7)
            break label686;
          i3 = arrayOfByte[(i5++)];
          paramArrayOfInt[(l++)] = (i3 >> 6 & 0x3);
          paramArrayOfInt[(l++)] = (i3 >> 4 & 0x3);
          paramArrayOfInt[(l++)] = (i3 >> 2 & 0x3);
          paramArrayOfInt[(l++)] = (i3 & 0x3);
          i3 = arrayOfByte[(i5++)];
          paramArrayOfInt[(l++)] = (i3 >> 6 & 0x3);
          paramArrayOfInt[(l++)] = (i3 >> 4 & 0x3);
          paramArrayOfInt[(l++)] = (i3 >> 2 & 0x3);
          paramArrayOfInt[(l++)] = (i3 & 0x3);
          i2 += 16;
          i4 += 8;
        }
      case 4:
        while (i4 < paramInt3 - 7)
        {
          i3 = arrayOfByte[(i5++)];
          paramArrayOfInt[(l++)] = (i3 >> 4 & 0xF);
          paramArrayOfInt[(l++)] = (i3 & 0xF);
          i3 = arrayOfByte[(i5++)];
          paramArrayOfInt[(l++)] = (i3 >> 4 & 0xF);
          paramArrayOfInt[(l++)] = (i3 & 0xF);
          i3 = arrayOfByte[(i5++)];
          paramArrayOfInt[(l++)] = (i3 >> 4 & 0xF);
          paramArrayOfInt[(l++)] = (i3 & 0xF);
          i3 = arrayOfByte[(i5++)];
          paramArrayOfInt[(l++)] = (i3 >> 4 & 0xF);
          paramArrayOfInt[(l++)] = (i3 & 0xF);
          i2 += 32;
          i4 += 8;
        }
      case 3:
      }
      while (i4 < paramInt3)
      {
        label686: int i6 = this.shiftOffset - (i2 & 0x7);
        paramArrayOfInt[(l++)] = (this.bitMask & arrayOfByte[(k + (i2 >> 3))] >> i6);
        i2 += i;
        ++i4;
      }
      k += this.scanlineStride;
    }
    return paramArrayOfInt;
  }

  public void setPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt)
  {
    if ((paramInt1 < this.minX) || (paramInt2 < this.minY) || (paramInt1 + paramInt3 > this.maxX) || (paramInt2 + paramInt4 > this.maxY))
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    int i = this.pixelBitStride;
    int j = this.dataBitOffset + (paramInt1 - this.minX) * i;
    int k = (paramInt2 - this.minY) * this.scanlineStride;
    int l = 0;
    byte[] arrayOfByte = this.data;
    for (int i1 = 0; i1 < paramInt4; ++i1)
    {
      int i3;
      int i2 = j;
      for (int i4 = 0; (i4 < paramInt3) && ((i2 & 0x7) != 0); ++i4)
      {
        i5 = this.shiftOffset - (i2 & 0x7);
        i3 = arrayOfByte[(k + (i2 >> 3))];
        i3 &= (this.bitMask << i5 ^ 0xFFFFFFFF);
        i3 |= (paramArrayOfInt[(l++)] & this.bitMask) << i5;
        arrayOfByte[(k + (i2 >> 3))] = (byte)i3;
        i2 += i;
      }
      int i5 = k + (i2 >> 3);
      switch (i)
      {
      case 1:
        while (true)
        {
          if (i4 >= paramInt3 - 7)
            break label764;
          i3 = (paramArrayOfInt[(l++)] & 0x1) << 7;
          i3 |= (paramArrayOfInt[(l++)] & 0x1) << 6;
          i3 |= (paramArrayOfInt[(l++)] & 0x1) << 5;
          i3 |= (paramArrayOfInt[(l++)] & 0x1) << 4;
          i3 |= (paramArrayOfInt[(l++)] & 0x1) << 3;
          i3 |= (paramArrayOfInt[(l++)] & 0x1) << 2;
          i3 |= (paramArrayOfInt[(l++)] & 0x1) << 1;
          i3 |= paramArrayOfInt[(l++)] & 0x1;
          arrayOfByte[(i5++)] = (byte)i3;
          i2 += 8;
          i4 += 8;
        }
      case 2:
        while (true)
        {
          if (i4 >= paramInt3 - 7)
            break label764;
          i3 = (paramArrayOfInt[(l++)] & 0x3) << 6;
          i3 |= (paramArrayOfInt[(l++)] & 0x3) << 4;
          i3 |= (paramArrayOfInt[(l++)] & 0x3) << 2;
          i3 |= paramArrayOfInt[(l++)] & 0x3;
          arrayOfByte[(i5++)] = (byte)i3;
          i3 = (paramArrayOfInt[(l++)] & 0x3) << 6;
          i3 |= (paramArrayOfInt[(l++)] & 0x3) << 4;
          i3 |= (paramArrayOfInt[(l++)] & 0x3) << 2;
          i3 |= paramArrayOfInt[(l++)] & 0x3;
          arrayOfByte[(i5++)] = (byte)i3;
          i2 += 16;
          i4 += 8;
        }
      case 4:
        while (i4 < paramInt3 - 7)
        {
          i3 = (paramArrayOfInt[(l++)] & 0xF) << 4;
          i3 |= paramArrayOfInt[(l++)] & 0xF;
          arrayOfByte[(i5++)] = (byte)i3;
          i3 = (paramArrayOfInt[(l++)] & 0xF) << 4;
          i3 |= paramArrayOfInt[(l++)] & 0xF;
          arrayOfByte[(i5++)] = (byte)i3;
          i3 = (paramArrayOfInt[(l++)] & 0xF) << 4;
          i3 |= paramArrayOfInt[(l++)] & 0xF;
          arrayOfByte[(i5++)] = (byte)i3;
          i3 = (paramArrayOfInt[(l++)] & 0xF) << 4;
          i3 |= paramArrayOfInt[(l++)] & 0xF;
          arrayOfByte[(i5++)] = (byte)i3;
          i2 += 32;
          i4 += 8;
        }
      case 3:
      }
      while (i4 < paramInt3)
      {
        label764: int i6 = this.shiftOffset - (i2 & 0x7);
        i3 = arrayOfByte[(k + (i2 >> 3))];
        i3 &= (this.bitMask << i6 ^ 0xFFFFFFFF);
        i3 |= (paramArrayOfInt[(l++)] & this.bitMask) << i6;
        arrayOfByte[(k + (i2 >> 3))] = (byte)i3;
        i2 += i;
        ++i4;
      }
      k += this.scanlineStride;
    }
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
    return new BytePackedRaster(localSampleModel, this.dataBuffer, new Rectangle(paramInt5, paramInt6, paramInt3, paramInt4), new Point(this.sampleModelTranslateX + i, this.sampleModelTranslateY + j), this);
  }

  public WritableRaster createCompatibleWritableRaster(int paramInt1, int paramInt2)
  {
    if ((paramInt1 <= 0) || (paramInt2 <= 0))
      throw new RasterFormatException("negative " + "height");
    SampleModel localSampleModel = this.sampleModel.createCompatibleSampleModel(paramInt1, paramInt2);
    return new BytePackedRaster(localSampleModel, new Point(0, 0));
  }

  public WritableRaster createCompatibleWritableRaster()
  {
    return createCompatibleWritableRaster(this.width, this.height);
  }

  private void verify(boolean paramBoolean)
  {
    if (this.dataBitOffset < 0)
      throw new RasterFormatException("Data offsets must be >= 0");
    int i = this.dataBitOffset + (this.height - 1) * this.scanlineStride * 8 + (this.width - 1) * this.pixelBitStride + this.pixelBitStride - 1;
    if (i / 8 >= this.data.length)
      throw new RasterFormatException("raster dimensions overflow array bounds");
    if ((paramBoolean) && (this.height > 1))
    {
      i = this.width * this.pixelBitStride - 1;
      if (i / 8 >= this.scanlineStride)
        throw new RasterFormatException("data for adjacent scanlines overlaps");
    }
  }

  public String toString()
  {
    return new String("BytePackedRaster: width = " + this.width + " height = " + this.height + " #channels " + this.numBands + " xOff = " + this.sampleModelTranslateX + " yOff = " + this.sampleModelTranslateY);
  }

  static
  {
    NativeLibLoader.loadLibraries();
    initIDs();
  }
}