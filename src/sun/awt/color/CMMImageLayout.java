package sun.awt.color;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import sun.awt.image.ByteComponentRaster;
import sun.awt.image.IntegerComponentRaster;
import sun.awt.image.ShortComponentRaster;

class CMMImageLayout
{
  private static final int typeBase = 256;
  public static final int typeComponentUByte = 256;
  public static final int typeComponentUShort12 = 257;
  public static final int typeComponentUShort = 258;
  public static final int typePixelUByte = 259;
  public static final int typePixelUShort12 = 260;
  public static final int typePixelUShort = 261;
  public static final int typeShort555 = 262;
  public static final int typeShort565 = 263;
  public static final int typeInt101010 = 264;
  public static final int typeIntRGBPacked = 265;
  public int Type;
  public int NumCols;
  public int NumRows;
  public int OffsetColumn;
  public int OffsetRow;
  public int NumChannels;
  public Object[] chanData;
  public int[] DataOffsets;
  public int[] sampleInfo;

  public CMMImageLayout(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    this.Type = 256;
    this.chanData = new Object[paramInt2];
    this.DataOffsets = new int[paramInt2];
    this.NumCols = paramInt1;
    this.NumRows = 1;
    this.OffsetColumn = paramInt2;
    this.OffsetRow = (this.NumCols * this.OffsetColumn);
    this.NumChannels = paramInt2;
    for (int i = 0; i < paramInt2; ++i)
    {
      this.chanData[i] = paramArrayOfByte;
      this.DataOffsets[i] = i;
    }
  }

  public CMMImageLayout(short[] paramArrayOfShort, int paramInt1, int paramInt2)
  {
    this.Type = 258;
    this.chanData = new Object[paramInt2];
    this.DataOffsets = new int[paramInt2];
    this.NumCols = paramInt1;
    this.NumRows = 1;
    this.OffsetColumn = (paramInt2 * 2);
    this.OffsetRow = (this.NumCols * this.OffsetColumn);
    this.NumChannels = paramInt2;
    for (int i = 0; i < paramInt2; ++i)
    {
      this.chanData[i] = paramArrayOfShort;
      this.DataOffsets[i] = (i * 2);
    }
  }

  public CMMImageLayout(BufferedImage paramBufferedImage)
  {
    this.Type = paramBufferedImage.getType();
    switch (this.Type)
    {
    case 1:
    case 2:
    case 4:
      this.NumChannels = 3;
      this.NumCols = paramBufferedImage.getWidth();
      this.NumRows = paramBufferedImage.getHeight();
      i = 3;
      if (this.Type == 2)
        i = 4;
      this.chanData = new Object[i];
      this.DataOffsets = new int[i];
      this.sampleInfo = new int[i];
      this.OffsetColumn = 4;
      IntegerComponentRaster localIntegerComponentRaster = (IntegerComponentRaster)paramBufferedImage.getRaster();
      this.OffsetRow = (localIntegerComponentRaster.getScanlineStride() * 4);
      j = localIntegerComponentRaster.getDataOffset(0) * 4;
      localObject = localIntegerComponentRaster.getDataStorage();
      for (k = 0; k < 3; ++k)
      {
        this.chanData[k] = localObject;
        this.DataOffsets[k] = j;
        if (this.Type == 4)
          this.sampleInfo[k] = (3 - k);
        else
          this.sampleInfo[k] = (k + 1);
      }
      if (this.Type != 2)
        return;
      this.chanData[3] = localObject;
      this.DataOffsets[3] = j;
      this.sampleInfo[3] = 0;
      break;
    case 5:
    case 6:
      this.NumChannels = 3;
      this.NumCols = paramBufferedImage.getWidth();
      this.NumRows = paramBufferedImage.getHeight();
      if (this.Type == 5)
      {
        this.OffsetColumn = 3;
        i = 3;
      }
      else
      {
        this.OffsetColumn = 4;
        i = 4;
      }
      this.chanData = new Object[i];
      this.DataOffsets = new int[i];
      localByteComponentRaster = (ByteComponentRaster)paramBufferedImage.getRaster();
      this.OffsetRow = localByteComponentRaster.getScanlineStride();
      j = localByteComponentRaster.getDataOffset(0);
      localObject = localByteComponentRaster.getDataStorage();
      for (k = 0; k < i; ++k)
      {
        this.chanData[k] = localObject;
        this.DataOffsets[k] = (j - k);
      }
      break;
    case 10:
      this.Type = 256;
      this.NumChannels = 1;
      this.NumCols = paramBufferedImage.getWidth();
      this.NumRows = paramBufferedImage.getHeight();
      this.chanData = new Object[1];
      this.DataOffsets = new int[1];
      this.OffsetColumn = 1;
      localByteComponentRaster = (ByteComponentRaster)paramBufferedImage.getRaster();
      this.OffsetRow = localByteComponentRaster.getScanlineStride();
      this.chanData[0] = localByteComponentRaster.getDataStorage();
      this.DataOffsets[0] = localByteComponentRaster.getDataOffset(0);
      break;
    case 11:
      this.Type = 258;
      this.NumChannels = 1;
      this.NumCols = paramBufferedImage.getWidth();
      this.NumRows = paramBufferedImage.getHeight();
      this.chanData = new Object[1];
      this.DataOffsets = new int[1];
      this.OffsetColumn = 2;
      ShortComponentRaster localShortComponentRaster = (ShortComponentRaster)paramBufferedImage.getRaster();
      this.OffsetRow = (localShortComponentRaster.getScanlineStride() * 2);
      this.chanData[0] = localShortComponentRaster.getDataStorage();
      this.DataOffsets[0] = (localShortComponentRaster.getDataOffset(0) * 2);
      break;
    case 3:
    case 7:
    case 8:
    case 9:
    default:
      throw new IllegalArgumentException("CMMImageLayout - bad image type passed to constructor");
    }
  }

  public CMMImageLayout(BufferedImage paramBufferedImage, SinglePixelPackedSampleModel paramSinglePixelPackedSampleModel, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.Type = 265;
    this.NumChannels = 3;
    this.NumCols = paramBufferedImage.getWidth();
    this.NumRows = paramBufferedImage.getHeight();
    int i = 3;
    if (paramInt4 >= 0)
      i = 4;
    this.chanData = new Object[i];
    this.DataOffsets = new int[i];
    this.sampleInfo = new int[i];
    this.OffsetColumn = 4;
    this.OffsetRow = (paramSinglePixelPackedSampleModel.getScanlineStride() * 4);
    WritableRaster localWritableRaster = paramBufferedImage.getRaster();
    DataBufferInt localDataBufferInt = (DataBufferInt)localWritableRaster.getDataBuffer();
    int[] arrayOfInt = localDataBufferInt.getData();
    int j = (localDataBufferInt.getOffset() - localWritableRaster.getSampleModelTranslateY() * paramSinglePixelPackedSampleModel.getScanlineStride() - localWritableRaster.getSampleModelTranslateX()) * 4;
    for (int k = 0; k < i; ++k)
    {
      this.chanData[k] = arrayOfInt;
      this.DataOffsets[k] = j;
    }
    this.sampleInfo[0] = paramInt1;
    this.sampleInfo[1] = paramInt2;
    this.sampleInfo[2] = paramInt3;
    if (paramInt4 >= 0)
      this.sampleInfo[3] = paramInt4;
  }

  public CMMImageLayout(BufferedImage paramBufferedImage, ComponentSampleModel paramComponentSampleModel)
  {
    ColorModel localColorModel = paramBufferedImage.getColorModel();
    int i = localColorModel.getNumColorComponents();
    boolean bool = localColorModel.hasAlpha();
    WritableRaster localWritableRaster = paramBufferedImage.getRaster();
    int[] arrayOfInt1 = paramComponentSampleModel.getBankIndices();
    int[] arrayOfInt2 = paramComponentSampleModel.getBandOffsets();
    this.NumChannels = i;
    this.NumCols = paramBufferedImage.getWidth();
    this.NumRows = paramBufferedImage.getHeight();
    if (bool);
    this.chanData = new Object[++i];
    this.DataOffsets = new int[i];
    switch (paramComponentSampleModel.getDataType())
    {
    case 0:
      this.Type = 256;
      this.OffsetColumn = paramComponentSampleModel.getPixelStride();
      this.OffsetRow = paramComponentSampleModel.getScanlineStride();
      localObject = (DataBufferByte)localWritableRaster.getDataBuffer();
      arrayOfInt3 = ((DataBufferByte)localObject).getOffsets();
      for (j = 0; j < i; ++j)
      {
        this.chanData[j] = ((DataBufferByte)localObject).getData(arrayOfInt1[j]);
        this.DataOffsets[j] = (arrayOfInt3[arrayOfInt1[j]] - localWritableRaster.getSampleModelTranslateY() * paramComponentSampleModel.getScanlineStride() - localWritableRaster.getSampleModelTranslateX() * paramComponentSampleModel.getPixelStride() + arrayOfInt2[j]);
      }
      break;
    case 1:
      this.Type = 258;
      this.OffsetColumn = (paramComponentSampleModel.getPixelStride() * 2);
      this.OffsetRow = (paramComponentSampleModel.getScanlineStride() * 2);
      localObject = (DataBufferUShort)localWritableRaster.getDataBuffer();
      arrayOfInt3 = ((DataBufferUShort)localObject).getOffsets();
      for (j = 0; j < i; ++j)
      {
        this.chanData[j] = ((DataBufferUShort)localObject).getData(arrayOfInt1[j]);
        this.DataOffsets[j] = ((arrayOfInt3[arrayOfInt1[j]] - localWritableRaster.getSampleModelTranslateY() * paramComponentSampleModel.getScanlineStride() - localWritableRaster.getSampleModelTranslateX() * paramComponentSampleModel.getPixelStride() + arrayOfInt2[j]) * 2);
      }
      break;
    default:
      throw new IllegalArgumentException("CMMImageLayout - bad image type passed to constructor");
    }
  }

  public CMMImageLayout(Raster paramRaster, ComponentSampleModel paramComponentSampleModel)
  {
    int i = paramRaster.getNumBands();
    int[] arrayOfInt1 = paramComponentSampleModel.getBankIndices();
    int[] arrayOfInt2 = paramComponentSampleModel.getBandOffsets();
    this.NumChannels = i;
    this.NumCols = paramRaster.getWidth();
    this.NumRows = paramRaster.getHeight();
    this.chanData = new Object[i];
    this.DataOffsets = new int[i];
    switch (paramComponentSampleModel.getDataType())
    {
    case 0:
      this.Type = 256;
      this.OffsetColumn = paramComponentSampleModel.getPixelStride();
      this.OffsetRow = paramComponentSampleModel.getScanlineStride();
      localObject = (DataBufferByte)paramRaster.getDataBuffer();
      arrayOfInt3 = ((DataBufferByte)localObject).getOffsets();
      for (j = 0; j < i; ++j)
      {
        this.chanData[j] = ((DataBufferByte)localObject).getData(arrayOfInt1[j]);
        this.DataOffsets[j] = (arrayOfInt3[arrayOfInt1[j]] + (paramRaster.getMinY() - paramRaster.getSampleModelTranslateY()) * paramComponentSampleModel.getScanlineStride() + (paramRaster.getMinX() - paramRaster.getSampleModelTranslateX()) * paramComponentSampleModel.getPixelStride() + arrayOfInt2[j]);
      }
      break;
    case 1:
      this.Type = 258;
      this.OffsetColumn = (paramComponentSampleModel.getPixelStride() * 2);
      this.OffsetRow = (paramComponentSampleModel.getScanlineStride() * 2);
      localObject = (DataBufferUShort)paramRaster.getDataBuffer();
      arrayOfInt3 = ((DataBufferUShort)localObject).getOffsets();
      for (j = 0; j < i; ++j)
      {
        this.chanData[j] = ((DataBufferUShort)localObject).getData(arrayOfInt1[j]);
        this.DataOffsets[j] = ((arrayOfInt3[arrayOfInt1[j]] + (paramRaster.getMinY() - paramRaster.getSampleModelTranslateY()) * paramComponentSampleModel.getScanlineStride() + (paramRaster.getMinX() - paramRaster.getSampleModelTranslateX()) * paramComponentSampleModel.getPixelStride() + arrayOfInt2[j]) * 2);
      }
      break;
    default:
      throw new IllegalArgumentException("CMMImageLayout - bad image type passed to constructor");
    }
  }
}