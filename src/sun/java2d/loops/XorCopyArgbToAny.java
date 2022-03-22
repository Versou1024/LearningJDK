package sun.java2d.loops;

import java.awt.Color;
import java.awt.Composite;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import sun.awt.image.IntegerComponentRaster;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.SpanIterator;

class XorCopyArgbToAny extends Blit
{
  XorCopyArgbToAny()
  {
    super(SurfaceType.IntArgb, CompositeType.Xor, SurfaceType.Any);
  }

  public void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    Raster localRaster = paramSurfaceData1.getRaster(paramInt1, paramInt2, paramInt5, paramInt6);
    IntegerComponentRaster localIntegerComponentRaster = (IntegerComponentRaster)localRaster;
    int[] arrayOfInt1 = localIntegerComponentRaster.getDataStorage();
    WritableRaster localWritableRaster = (WritableRaster)paramSurfaceData2.getRaster(paramInt3, paramInt4, paramInt5, paramInt6);
    ColorModel localColorModel = paramSurfaceData2.getColorModel();
    Region localRegion = CustomComponent.getRegionOfInterest(paramSurfaceData1, paramSurfaceData2, paramRegion, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    SpanIterator localSpanIterator = localRegion.getSpanIterator();
    int i = ((XORComposite)paramComposite).getXorColor().getRGB();
    Object localObject1 = localColorModel.getDataElements(i, null);
    Object localObject2 = null;
    Object localObject3 = null;
    int j = localIntegerComponentRaster.getScanlineStride();
    paramInt1 -= paramInt3;
    paramInt2 -= paramInt4;
    int[] arrayOfInt2 = new int[4];
    while (localSpanIterator.nextSpan(arrayOfInt2))
    {
      int k = localIntegerComponentRaster.getDataOffset(0) + (paramInt2 + arrayOfInt2[1]) * j + paramInt1 + arrayOfInt2[0];
      for (int l = arrayOfInt2[1]; l < arrayOfInt2[3]; ++l)
      {
        int i1 = k;
        for (int i2 = arrayOfInt2[0]; i2 < arrayOfInt2[2]; ++i2)
        {
          localObject2 = localColorModel.getDataElements(arrayOfInt1[(i1++)], localObject2);
          localObject3 = localWritableRaster.getDataElements(i2, l, localObject3);
          switch (localColorModel.getTransferType())
          {
          case 0:
            byte[] arrayOfByte1 = (byte[])(byte[])localObject2;
            byte[] arrayOfByte2 = (byte[])(byte[])localObject3;
            byte[] arrayOfByte3 = (byte[])(byte[])localObject1;
            for (int i3 = 0; i3 < arrayOfByte2.length; ++i3)
            {
              int tmp325_323 = i3;
              byte[] tmp325_321 = arrayOfByte2;
              tmp325_321[tmp325_323] = (byte)(tmp325_321[tmp325_323] ^ arrayOfByte1[i3] ^ arrayOfByte3[i3]);
            }
            break;
          case 1:
          case 2:
            short[] arrayOfShort1 = (short[])(short[])localObject2;
            short[] arrayOfShort2 = (short[])(short[])localObject3;
            short[] arrayOfShort3 = (short[])(short[])localObject1;
            for (int i4 = 0; i4 < arrayOfShort2.length; ++i4)
            {
              int tmp395_393 = i4;
              short[] tmp395_391 = arrayOfShort2;
              tmp395_391[tmp395_393] = (short)(tmp395_391[tmp395_393] ^ arrayOfShort1[i4] ^ arrayOfShort3[i4]);
            }
            break;
          case 3:
            int[] arrayOfInt3 = (int[])(int[])localObject2;
            int[] arrayOfInt4 = (int[])(int[])localObject3;
            int[] arrayOfInt5 = (int[])(int[])localObject1;
            for (int i5 = 0; i5 < arrayOfInt4.length; ++i5)
              arrayOfInt4[i5] ^= arrayOfInt3[i5] ^ arrayOfInt5[i5];
            break;
          case 4:
            float[] arrayOfFloat1 = (float[])(float[])localObject2;
            float[] arrayOfFloat2 = (float[])(float[])localObject3;
            float[] arrayOfFloat3 = (float[])(float[])localObject1;
            for (int i6 = 0; i6 < arrayOfFloat2.length; ++i6)
            {
              int i7 = Float.floatToIntBits(arrayOfFloat2[i6]) ^ Float.floatToIntBits(arrayOfFloat1[i6]) ^ Float.floatToIntBits(arrayOfFloat3[i6]);
              arrayOfFloat2[i6] = Float.intBitsToFloat(i7);
            }
            break;
          case 5:
            double[] arrayOfDouble1 = (double[])(double[])localObject2;
            double[] arrayOfDouble2 = (double[])(double[])localObject3;
            double[] arrayOfDouble3 = (double[])(double[])localObject1;
            for (int i8 = 0; i8 < arrayOfDouble2.length; ++i8)
            {
              long l1 = Double.doubleToLongBits(arrayOfDouble2[i8]) ^ Double.doubleToLongBits(arrayOfDouble1[i8]) ^ Double.doubleToLongBits(arrayOfDouble3[i8]);
              arrayOfDouble2[i8] = Double.longBitsToDouble(l1);
            }
            break;
          default:
            throw new InternalError("Unsupported XOR pixel type");
          }
          localWritableRaster.setDataElements(i2, l, localObject3);
        }
        k += j;
      }
    }
  }
}