package sun.java2d.loops;

import java.awt.image.WritableRaster;

class SolidPixelWriter extends PixelWriter
{
  protected Object srcData;

  SolidPixelWriter(Object paramObject)
  {
    this.srcData = paramObject;
  }

  public void writePixel(int paramInt1, int paramInt2)
  {
    this.dstRast.setDataElements(paramInt1, paramInt2, this.srcData);
  }
}