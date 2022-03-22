package sun.java2d.loops;

import java.awt.Composite;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import sun.awt.image.IntegerComponentRaster;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.SpanIterator;

class OpaqueCopyAnyToArgb extends Blit
{
  OpaqueCopyAnyToArgb()
  {
    super(SurfaceType.Any, CompositeType.SrcNoEa, SurfaceType.IntArgb);
  }

  public void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    Raster localRaster1 = paramSurfaceData1.getRaster(paramInt1, paramInt2, paramInt5, paramInt6);
    ColorModel localColorModel = paramSurfaceData1.getColorModel();
    Raster localRaster2 = paramSurfaceData2.getRaster(paramInt3, paramInt4, paramInt5, paramInt6);
    IntegerComponentRaster localIntegerComponentRaster = (IntegerComponentRaster)localRaster2;
    int[] arrayOfInt1 = localIntegerComponentRaster.getDataStorage();
    Region localRegion = CustomComponent.getRegionOfInterest(paramSurfaceData1, paramSurfaceData2, paramRegion, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    SpanIterator localSpanIterator = localRegion.getSpanIterator();
    Object localObject = null;
    int i = localIntegerComponentRaster.getScanlineStride();
    paramInt1 -= paramInt3;
    paramInt2 -= paramInt4;
    int[] arrayOfInt2 = new int[4];
    while (localSpanIterator.nextSpan(arrayOfInt2))
    {
      int j = localIntegerComponentRaster.getDataOffset(0) + arrayOfInt2[1] * i + arrayOfInt2[0];
      for (int k = arrayOfInt2[1]; k < arrayOfInt2[3]; ++k)
      {
        int l = j;
        for (int i1 = arrayOfInt2[0]; i1 < arrayOfInt2[2]; ++i1)
        {
          localObject = localRaster1.getDataElements(i1 + paramInt1, k + paramInt2, localObject);
          arrayOfInt1[(l++)] = localColorModel.getRGB(localObject);
        }
        j += i;
      }
    }
  }
}