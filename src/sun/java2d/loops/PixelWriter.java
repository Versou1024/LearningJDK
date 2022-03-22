package sun.java2d.loops;

import java.awt.image.WritableRaster;

abstract class PixelWriter
{
  protected WritableRaster dstRast;

  public void setRaster(WritableRaster paramWritableRaster)
  {
    this.dstRast = paramWritableRaster;
  }

  public abstract void writePixel(int paramInt1, int paramInt2);
}