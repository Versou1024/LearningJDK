package sun.awt.image;

import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import sun.java2d.SurfaceData;

public class WritableRasterNative extends WritableRaster
{
  public static WritableRasterNative createNativeRaster(SampleModel paramSampleModel, DataBuffer paramDataBuffer)
  {
    return new WritableRasterNative(paramSampleModel, paramDataBuffer);
  }

  protected WritableRasterNative(SampleModel paramSampleModel, DataBuffer paramDataBuffer)
  {
    super(paramSampleModel, paramDataBuffer, new Point(0, 0));
  }

  public static WritableRasterNative createNativeRaster(ColorModel paramColorModel, SurfaceData paramSurfaceData, int paramInt1, int paramInt2)
  {
    int[] arrayOfInt;
    DirectColorModel localDirectColorModel;
    Object localObject1 = null;
    int i = 0;
    int j = paramInt1;
    switch (paramColorModel.getPixelSize())
    {
    case 8:
    case 12:
      if (paramColorModel.getPixelSize() == 8)
        i = 0;
      else
        i = 1;
      localObject2 = new int[1];
      localObject2[0] = 0;
      localObject1 = new PixelInterleavedSampleModel(i, paramInt1, paramInt2, 1, j, localObject2);
      break;
    case 15:
    case 16:
      i = 1;
      arrayOfInt = new int[3];
      localDirectColorModel = (DirectColorModel)paramColorModel;
      arrayOfInt[0] = localDirectColorModel.getRedMask();
      arrayOfInt[1] = localDirectColorModel.getGreenMask();
      arrayOfInt[2] = localDirectColorModel.getBlueMask();
      localObject1 = new SinglePixelPackedSampleModel(i, paramInt1, paramInt2, j, arrayOfInt);
      break;
    case 24:
    case 32:
      i = 3;
      arrayOfInt = new int[3];
      localDirectColorModel = (DirectColorModel)paramColorModel;
      arrayOfInt[0] = localDirectColorModel.getRedMask();
      arrayOfInt[1] = localDirectColorModel.getGreenMask();
      arrayOfInt[2] = localDirectColorModel.getBlueMask();
      localObject1 = new SinglePixelPackedSampleModel(i, paramInt1, paramInt2, j, arrayOfInt);
      break;
    default:
      throw new InternalError("Unsupported depth " + paramColorModel.getPixelSize());
    }
    Object localObject2 = new DataBufferNative(paramSurfaceData, i, paramInt1, paramInt2);
    return ((WritableRasterNative)(WritableRasterNative)new WritableRasterNative((SampleModel)localObject1, (DataBuffer)localObject2));
  }
}