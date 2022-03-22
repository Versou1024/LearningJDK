package sun.java2d;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.XORComposite;

public class SunCompositeContext
  implements CompositeContext
{
  ColorModel srcCM;
  ColorModel dstCM;
  Composite composite;
  CompositeType comptype;

  public SunCompositeContext(AlphaComposite paramAlphaComposite, ColorModel paramColorModel1, ColorModel paramColorModel2)
  {
    if (paramColorModel1 == null)
      throw new NullPointerException("Source color model cannot be null");
    if (paramColorModel2 == null)
      throw new NullPointerException("Destination color model cannot be null");
    this.srcCM = paramColorModel1;
    this.dstCM = paramColorModel2;
    this.composite = paramAlphaComposite;
    this.comptype = CompositeType.forAlphaComposite(paramAlphaComposite);
  }

  public SunCompositeContext(XORComposite paramXORComposite, ColorModel paramColorModel1, ColorModel paramColorModel2)
  {
    if (paramColorModel1 == null)
      throw new NullPointerException("Source color model cannot be null");
    if (paramColorModel2 == null)
      throw new NullPointerException("Destination color model cannot be null");
    this.srcCM = paramColorModel1;
    this.dstCM = paramColorModel2;
    this.composite = paramXORComposite;
    this.comptype = CompositeType.Xor;
  }

  public void dispose()
  {
  }

  public void compose(Raster paramRaster1, Raster paramRaster2, WritableRaster paramWritableRaster)
  {
    WritableRaster localWritableRaster;
    if (paramRaster2 != paramWritableRaster)
      paramWritableRaster.setDataElements(0, 0, paramRaster2);
    if (paramRaster1 instanceof WritableRaster)
    {
      localWritableRaster = (WritableRaster)paramRaster1;
    }
    else
    {
      localWritableRaster = paramRaster1.createCompatibleWritableRaster();
      localWritableRaster.setDataElements(0, 0, paramRaster1);
    }
    int i = Math.min(localWritableRaster.getWidth(), paramRaster2.getWidth());
    int j = Math.min(localWritableRaster.getHeight(), paramRaster2.getHeight());
    BufferedImage localBufferedImage1 = new BufferedImage(this.srcCM, localWritableRaster, this.srcCM.isAlphaPremultiplied(), null);
    BufferedImage localBufferedImage2 = new BufferedImage(this.dstCM, paramWritableRaster, this.dstCM.isAlphaPremultiplied(), null);
    SurfaceData localSurfaceData1 = BufImgSurfaceData.createData(localBufferedImage1);
    SurfaceData localSurfaceData2 = BufImgSurfaceData.createData(localBufferedImage2);
    Blit localBlit = Blit.getFromCache(localSurfaceData1.getSurfaceType(), this.comptype, localSurfaceData2.getSurfaceType());
    localBlit.Blit(localSurfaceData1, localSurfaceData2, this.composite, null, 0, 0, 0, 0, i, j);
  }
}